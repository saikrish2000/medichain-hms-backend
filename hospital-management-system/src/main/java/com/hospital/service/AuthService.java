package com.hospital.service;

import com.hospital.dto.auth.AuthResponse;
import com.hospital.dto.auth.LoginRequest;
import com.hospital.dto.auth.RegisterRequest;
import com.hospital.entity.*;
import com.hospital.enums.Role;
import com.hospital.exception.BadRequestException;
import com.hospital.repository.UserRepository;
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
        auditService.log(up.getId(), up.getUsername(),
            "USER_LOGIN","User",up.getId(),"unknown","SUCCESS");
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

        // Auto-create profile based on role
        notificationService.sendEmailVerification(user.getEmail(), user.getFullName(), user.getEmailVerificationToken());

        // Log them in directly
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        String access  = tokenProvider.generateToken(auth);
        String refresh = tokenProvider.generateRefreshToken(up.getId());
        return buildResponse(up, access, refresh);
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
