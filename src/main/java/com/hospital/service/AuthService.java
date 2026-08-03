package com.hospital.service;

import com.hospital.dto.auth.AuthResponse;
import com.hospital.dto.auth.LoginRequest;
import com.hospital.dto.auth.RegisterRequest;
import com.hospital.entity.*;
import com.hospital.enums.Role;
import com.hospital.exception.BadRequestException;
import com.hospital.repository.*;
import com.hospital.security.JwtTokenProvider;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      tokenProvider;
    private final NotificationService   notificationService;
    private final AuditService          auditService;
    private final PatientRepository     patientRepository;
    private final DoctorRepository      doctorRepository;
    private final NurseRepository       nurseRepository;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsernameOrEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        userRepository.findById(up.getId()).ifPresent(u -> {
            u.setLastLogin(LocalDateTime.now()); userRepository.save(u); });
        String access  = tokenProvider.generateToken(authentication);
        String refresh = tokenProvider.generateRefreshToken(up.getId());
        auditService.log(up.getUsername(), "USER_LOGIN", "User", up.getId(), "Login successful");
        return buildResponse(up, access, refresh);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("Email already registered.");
        if (userRepository.existsByUsername(request.getUsername()))
            throw new BadRequestException("Username already taken.");
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new BadRequestException("Passwords do not match.");

        User user = User.builder()
            .firstName(request.getFirstName()).lastName(request.getLastName())
            .username(request.getUsername()).email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole()).phone(request.getPhone())
            .dateOfBirth(request.getDateOfBirth()).gender(request.getGender())
            .bloodGroup(request.getBloodGroup())
            .preferredLanguage(request.getPreferredLanguage())
            .isActive(true).isVerified(true).emailVerified(true)
            .emailVerificationToken(UUID.randomUUID().toString())
            .build();
        userRepository.save(user);

        // Auto-create role-specific profile entity
        createRoleProfile(user, request);

        notificationService.sendEmailVerification(user.getEmail(), user.getFullName(), user.getEmailVerificationToken());

        // Log them in directly
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        String access  = tokenProvider.generateToken(auth);
        String refresh = tokenProvider.generateRefreshToken(up.getId());
        auditService.log(up.getUsername(), "USER_REGISTER", "User", up.getId(),
                         "Registered with role: " + request.getRole());
        return buildResponse(up, access, refresh);
    }

    private void createRoleProfile(User user, RegisterRequest request) {
        Role role = request.getRole();
        if (role == null) return;

        switch (role) {
            case PATIENT:
                String patientIdNum = "PAT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + user.getId();
                Patient patient = Patient.builder()
                    .user(user)
                    .patientIdNumber(patientIdNum)
                    .build();
                patientRepository.save(patient);
                log.info("Created Patient profile for user {} with patientId {}", user.getId(), patientIdNum);
                break;

            case DOCTOR:
                Doctor doctor = Doctor.builder()
                    .user(user)
                    .licenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber() : "PENDING-" + user.getId())
                    .qualification(request.getQualification() != null ? request.getQualification() : "")
                    .experienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0)
                    .approvalStatus("PENDING")
                    .backgroundCheckStatus("PENDING")
                    .build();
                doctorRepository.save(doctor);
                log.info("Created Doctor profile for user {} (pending approval)", user.getId());
                break;

            case NURSE:
                Nurse nurse = Nurse.builder()
                    .user(user)
                    .licenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber() : "PENDING-" + user.getId())
                    .qualification(request.getQualification() != null ? request.getQualification() : "")
                    .approvalStatus("PENDING")
                    .build();
                nurseRepository.save(nurse);
                log.info("Created Nurse profile for user {} (pending approval)", user.getId());
                break;

            case ADMIN:
            case RECEPTIONIST:
            case PHARMACIST:
            case LAB_TECHNICIAN:
            case BLOOD_BANK_MANAGER:
            case AMBULANCE_OPERATOR:
            default:
                // Admin and other staff don't need a separate profile entity
                log.info("No additional profile entity needed for role {}", role);
                break;
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken))
            throw new BadRequestException("Invalid or expired refresh token");
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadRequestException("User not found"));
        UserPrincipal up = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(up, null, up.getAuthorities());
        String newAccess  = tokenProvider.generateToken(auth);
        String newRefresh = tokenProvider.generateRefreshToken(userId);
        return buildResponse(up, newAccess, newRefresh);
    }

    private AuthResponse buildResponse(UserPrincipal up, String access, String refresh) {
        return AuthResponse.builder()
            .accessToken(access).refreshToken(refresh)
            .userId(up.getId()).username(up.getUsername())
            .email(up.getEmail()).fullName(up.getFullName())
            .role(up.getRole()).build();
    }
}
