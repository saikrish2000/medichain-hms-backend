package com.hospital.dto.auth;

import com.hospital.enums.BloodGroup;
import com.hospital.enums.Role;
import com.hospital.entity.User.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Password must contain uppercase, lowercase and a number")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Please enter a valid Indian mobile number")
    private String phone;

    private LocalDate dateOfBirth;

    private Gender gender;

    private BloodGroup bloodGroup;

    @NotNull(message = "Please select a role")
    private Role role;

    // Extra fields for specific roles
    private String licenseNumber;     // Doctor, Nurse, Independent Nurse
    private String specialization;    // Doctor
    private String shopName;          // Medical Shop Owner
    private String centerName;        // Diagnostic Center Owner
    private String address;
    private String city;
    private String state;
    private String pincode;

    private String preferredLanguage = "en";

    public String getUsernameOrEmail() { return username; }
}
