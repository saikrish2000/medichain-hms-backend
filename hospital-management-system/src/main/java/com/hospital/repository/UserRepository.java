package com.hospital.repository;

import com.hospital.entity.User;
import com.hospital.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);

    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);

    List<User> findByRole(Role role);
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);

    @Query("SELECT u FROM User u WHERE u.branch.id = :branchId AND u.role = :role")
    List<User> findByBranchIdAndRole(Long branchId, Role role);
}
