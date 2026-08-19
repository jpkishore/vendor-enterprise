package com.platform.auth.repository;

import com.platform.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.4");

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {

        User user = createUser(
                "kishore",
                "kishore@example.com"
        );

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername())
                .isEqualTo("kishore");
        assertThat(saved.getEmail())
                .isEqualTo("kishore@example.com");
    }

    @Test
    void shouldFindUserByEmail() {

        User user = createUser(
                "emailuser",
                "emailuser@example.com"
        );

        userRepository.save(user);

        var result =
                userRepository.findByEmailIgnoreCase(
                        "EMAILUSER@EXAMPLE.COM"
                );

        assertThat(result).isPresent();

        assertThat(result.get().getUsername())
                .isEqualTo("emailuser");
    }

    @Test
    void shouldFindUserByUsername() {

        User user = createUser(
                "usernameuser",
                "username@example.com"
        );

        userRepository.save(user);

        var result =
                userRepository.findByUsernameOrEmailIgnoreCase(
                        "USERNAMEUSER"
                );

        assertThat(result).isPresent();

        assertThat(result.get().getEmail())
                .isEqualTo("username@example.com");
    }

    @Test
    void shouldCheckEmailExists() {

        User user = createUser(
                "existsuser",
                "exists@example.com"
        );

        userRepository.save(user);

        assertThat(
                userRepository.existsByEmailIgnoreCase(
                        "exists@example.com"
                )
        ).isTrue();

        assertThat(
                userRepository.existsByEmailIgnoreCase(
                        "notexists@example.com"
                )
        ).isFalse();
    }

    @Test
    void shouldCheckUsernameExists() {

        User user = createUser(
                "existingusername",
                "existing@example.com"
        );

        userRepository.save(user);

        assertThat(
                userRepository.existsByUsernameIgnoreCase(
                        "existingusername"
                )
        ).isTrue();

        assertThat(
                userRepository.existsByUsernameIgnoreCase(
                        "notexistingusername"
                )
        ).isFalse();
    }

    private User createUser(
            String username,
            String email
    ) {

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("encoded-password");

        user.setFirstName("Kishore");
        user.setLastName("K");
        user.setPhone("9876543210");

        user.setEmailVerified(false);
        user.setAccountEnabled(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        return user;
    }
}