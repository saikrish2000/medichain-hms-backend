package com.hospital.repository;

import com.hospital.entity.User;
import com.hospital.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<User> findByRole(Role role);
    long countByRole(Role role);
}
