package com.platform.auth.repository;

import com.platform.auth.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithRolesByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithRolesByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = {"roles"})
    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(u.username) = LOWER(:login)
               OR LOWER(u.email) = LOWER(:login)
            """)
    Optional<User> findByUsernameOrEmailIgnoreCase(
            @Param("login") String login
    );

    @EntityGraph(attributePaths = {"roles"})
    org.springframework.data.domain.Page<User> findAll(
            org.springframework.data.domain.Pageable pageable
    );

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findWithRolesById(Long id);
}