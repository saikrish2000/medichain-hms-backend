package com.hospital.service;

import com.hospital.dto.auth.AuthResponse;
import com.hospital.dto.auth.LoginRequest;
import com.hospital.dto.auth.RegisterRequest;
import com.hospital.entity.User;
import com.hospital.enums.Role;
import com.hospital.exception.BadRequestException;
import com.hospital.repository.UserRepository;
import com.hospital.security.JwtTokenProvider;
import com.hospital.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsernameOrEmail(), request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Update last login
        userRepository.findById(userPrincipal.getId()).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });

        String accessToken  = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getId());

        auditService.log(userPrincipal.getId(), userPrincipal.getUsername(),
                "USER_LOGIN", "User", userPrincipal.getId(), ipAddress, "SUCCESS");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .fullName(userPrincipal.getFullName())
                .role(userPrincipal.getRole())
                .preferredLanguage(userPrincipal.getPreferredLanguage())
                .redirectUrl(getDashboardUrl(userPrincipal.getRole()))
                .build();
    }

    @Transactional
    public String register(RegisterRequest request) {
        // Validations
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .bloodGroup(request.getBloodGroup())
                .preferredLanguage(request.getPreferredLanguage())
                .isActive(true)
                .isVerified(false)
                .emailVerified(false)
                .phoneVerified(false)
                .emailVerificationToken(verificationToken)
                .build();

        userRepository.save(user);

        // Send verification email async
        notificationService.sendEmailVerification(user.getEmail(),
                user.getFullName(), verificationToken);

        log.info("New user registered: {} [{}]", user.getUsername(), user.getRole());
        return "Registration successful! Please check your email to verify your account.";
    }

    @Transactional
    public String verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token."));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);

        // Auto-verify patients, donors (others need admin approval)
        if (user.getRole() == Role.PATIENT ||
            user.getRole() == Role.BLOOD_DONOR ||
            user.getRole() == Role.ORGAN_DONOR) {
            user.setIsVerified(true);
        }

        userRepository.save(user);
        return "Email verified successfully!";
    }

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found with this email."));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(2));
        userRepository.save(user);

        notificationService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);
        return "Password reset instructions have been sent to your email.";
    }

    @Transactional
    public String resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Passwords do not match.");
        }

        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token."));

        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);

        return "Password reset successfully! You can now login.";
    }

    public String getDashboardUrl(String role) {
        return switch (role) {
            case "ADMIN"                  -> "/admin/dashboard";
            case "DOCTOR"                 -> "/doctor/dashboard";
            case "NURSE"                  -> "/nurse/dashboard";
            case "PATIENT"                -> "/patient/dashboard";
            case "BLOOD_BANK_MANAGER"     -> "/blood-bank/dashboard";
            case "AMBULANCE_OPERATOR"     -> "/ambulance/dashboard";
            case "PHARMACIST"             -> "/pharmacy/dashboard";
            case "LAB_TECHNICIAN"         -> "/lab/dashboard";
            case "PHLEBOTOMIST"           -> "/lab/phlebotomist";
            case "MEDICAL_SHOP_OWNER"     -> "/medical-shop/dashboard";
            case "DIAGNOSTIC_CENTER_OWNER"-> "/diagnostic/dashboard";
            case "INDEPENDENT_NURSE"      -> "/nurse/independent/dashboard";
            case "RECEPTIONIST"           -> "/receptionist/dashboard";
            default                       -> "/dashboard";
        };
    }
}
