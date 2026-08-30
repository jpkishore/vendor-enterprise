package com.platform.auth.service;

import com.platform.auth.config.JwtProperties;
import com.platform.auth.dto.auth.LoginRequest;
import com.platform.auth.dto.auth.LoginResponse;
import com.platform.auth.dto.auth.RegisterRequest;
import com.platform.auth.dto.auth.RegisterResponse;
import com.platform.auth.entity.RefreshToken;
import com.platform.auth.entity.Role;
import com.platform.auth.entity.User;
import com.platform.auth.entity.enums.RoleStatus;
import com.platform.auth.exception.*;
import com.platform.auth.repository.RoleRepository;
import com.platform.auth.repository.UserRepository;
import com.platform.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest(
                "kishore",
                "kishore@example.com",
                "Password@123",
                "Kishore",
                "K",
                "9876543210","CUSTOMER"
        );

        when(userRepository.existsByEmailIgnoreCase(
                request.email()
        )).thenReturn(false);

        when(userRepository.existsByUsernameIgnoreCase(
                request.username()
        )).thenReturn(false);

        Role customerRole = new Role();

        customerRole.setName("Customer");
        customerRole.setCode("CUSTOMER");
        customerRole.setStatus(RoleStatus.ACTIVE);

        when(roleRepository.findByCodeIgnoreCase("CUSTOMER"))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode(
                request.password()
        )).thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setId(1L);

        savedUser.setUsername("kishore");
        savedUser.setEmail("kishore@example.com");
        savedUser.setPasswordHash("encoded-password");
        savedUser.setFirstName("Kishore");
        savedUser.setLastName("K");
        savedUser.setPhone("9876543210");

        savedUser.setEmailVerified(false);
        savedUser.setAccountEnabled(true);
        savedUser.setAccountLocked(false);
        savedUser.setFailedLoginAttempts(0);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegisterResponse response =
                authService.register(request);

        assertThat(response).isNotNull();

        assertThat(response.userId())
                .isEqualTo(1L);

        assertThat(response.username())
                .isEqualTo("kishore");

        assertThat(response.email())
                .isEqualTo("kishore@example.com");

        assertThat(response.message())
                .isEqualTo("User registered successfully");

        verify(passwordEncoder)
                .encode("Password@123");

        verify(userRepository)
                .save(any(User.class));

        verify(roleRepository)
                .findByCodeIgnoreCase("CUSTOMER");
    }

    @Test
    void shouldRejectRegistrationWhenCustomerRoleDoesNotExist() {

        RegisterRequest request = new RegisterRequest(
                "kishore",
                "kishore@example.com",
                "Password@123",
                "Kishore",
                "K",
                "9876543210","CUSTOMER"
        );

        when(userRepository.existsByEmailIgnoreCase(
                request.email()
        )).thenReturn(false);

        when(userRepository.existsByUsernameIgnoreCase(
                request.username()
        )).thenReturn(false);

        // CUSTOMER role does NOT exist
        when(roleRepository.findByCodeIgnoreCase("CUSTOMER"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> authService.register(request)
        )
                .isInstanceOf(RoleNotConfiguredException.class)
                .hasMessage(
                        "Default CUSTOMER role is not configured"
                );

        verify(roleRepository)
                .findByCodeIgnoreCase("CUSTOMER");

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest(
                "kishore",
                "kishore@example.com",
                "Password@123",
                "Kishore",
                "K",
                "9876543210","CUSTOMER"
        );

        when(userRepository.existsByEmailIgnoreCase(
                request.email()
        )).thenReturn(true);

        assertThatThrownBy(
                () -> authService.register(request)
        )
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email is already registered");

        verify(userRepository)
                .existsByEmailIgnoreCase(request.email());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectRegistrationWhenUsernameAlreadyExists() {

        RegisterRequest request = new RegisterRequest(
                "kishore",
                "kishore@example.com",
                "Password@123",
                "Kishore",
                "K",
                "9876543210","CUSTOMER"
        );

        when(userRepository.existsByEmailIgnoreCase(
                request.email()
        )).thenReturn(false);

        when(userRepository.existsByUsernameIgnoreCase(
                request.username()
        )).thenReturn(true);

        assertThatThrownBy(
                () -> authService.register(request)
        )
                .isInstanceOf(
                        UsernameAlreadyExistsException.class
                )
                .hasMessage("Username is already taken");

        verify(userRepository)
                .existsByUsernameIgnoreCase(
                        request.username()
                );

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldRejectRegistrationWhenCustomerRoleIsInactive() {

        RegisterRequest request = new RegisterRequest(
                "kishore",
                "kishore@example.com",
                "Password@123",
                "Kishore",
                "K",
                "9876543210","CUSTOMER"
        );

        when(userRepository.existsByEmailIgnoreCase(
                request.email()
        )).thenReturn(false);

        when(userRepository.existsByUsernameIgnoreCase(
                request.username()
        )).thenReturn(false);

        Role customerRole = new Role();

        customerRole.setName("Customer");
        customerRole.setCode("CUSTOMER");
        customerRole.setStatus(RoleStatus.INACTIVE);

        when(roleRepository.findByCodeIgnoreCase("CUSTOMER"))
                .thenReturn(Optional.of(customerRole));

        assertThatThrownBy(
                () -> authService.register(request)
        )
                .isInstanceOf(InactiveRoleException.class)
                .hasMessage(
                        "Default CUSTOMER role is inactive"
                );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {

        User user = new User();

        user.setId(1L);
        user.setUsername("kishore");
        user.setEmail("kishore@example.com");
        user.setPasswordHash("encoded-password");
        user.setAccountEnabled(true);
        user.setAccountLocked(false);

        when(userRepository.findByUsernameOrEmailIgnoreCase("kishore"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password@123",
                "encoded-password"
        )).thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn("jwt-access-token");

        LoginRequest request =
                new LoginRequest(
                        "kishore",
                        "Password@123"
                );

        LoginResponse response =
                authService.login(request);

        assertThat(response).isNotNull();

        assertThat(response.accessToken())
                .isEqualTo("jwt-access-token");

        assertThat(response.tokenType())
                .isEqualTo("Bearer");

        assertThat(response.userId())
                .isEqualTo(1L);

        assertThat(response.username())
                .isEqualTo("kishore");

        assertThat(response.email())
                .isEqualTo("kishore@example.com");

        verify(passwordEncoder)
                .matches(
                        "Password@123",
                        "encoded-password"
                );

        verify(jwtService)
                .generateAccessToken(user);
    }

    @Test
    void shouldRejectInvalidPassword() {

        LoginRequest request = new LoginRequest(
                "kishore",
                "WrongPassword"
        );

        User user = new User();

        user.setId(1L);
        user.setUsername("kishore");
        user.setEmail("kishore@example.com");
        user.setPasswordHash("encoded-password");
        user.setAccountEnabled(true);
        user.setAccountLocked(false);

        // AuthService.login() calls this first
        when(userRepository.findByUsernameOrEmailIgnoreCase(
                request.usernameOrEmail()
        )).thenReturn(Optional.of(user));

        // AuthService.login() falls through to password check
        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(false);

        var exception = org.assertj.core.api.Assertions
                .catchThrowable(
                        () -> authService.login(request)
                );

        assertThat(exception)
                .isInstanceOf(BadCredentialsException.class);

        assertThat(exception.getMessage())
                .isEqualTo(
                        "Invalid username/email or password"
                );

        verify(userRepository)
                .findByUsernameOrEmailIgnoreCase(
                        request.usernameOrEmail()
                );

        verify(passwordEncoder)
                .matches(
                        request.password(),
                        user.getPasswordHash()
                );

        verify(jwtService, never())
                .generateAccessToken(any());

        verify(refreshTokenService, never())
                .createRefreshToken(any());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {

        User user = new User();

        user.setId(1L);
        user.setUsername("kishore");
        user.setEmail("kishore@example.com");
        user.setAccountEnabled(true);
        user.setAccountLocked(false);

        RefreshToken oldToken = new RefreshToken();

        oldToken.setUser(user);

        when(refreshTokenService.validateRefreshToken(
                "old-refresh-token"
        )).thenReturn(oldToken);

        when(jwtService.generateAccessToken(user))
                .thenReturn("new-access-token");

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn("new-refresh-token");

        when(jwtProperties.getAccessTokenExpirationMinutes())
                .thenReturn(15L);

        LoginResponse response =
                authService.refresh(
                        "old-refresh-token"
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.accessToken())
                .isEqualTo("new-access-token");

        assertThat(response.refreshToken())
                .isEqualTo("new-refresh-token");

        assertThat(response.tokenType())
                .isEqualTo("Bearer");

        assertThat(response.expiresIn())
                .isEqualTo(900);

        assertThat(response.userId())
                .isEqualTo(1L);

        verify(refreshTokenService)
                .validateRefreshToken(
                        "old-refresh-token"
                );

        verify(refreshTokenService)
                .revoke(oldToken);

        verify(jwtService)
                .generateAccessToken(user);

        verify(refreshTokenService)
                .createRefreshToken(user);
    }

    @Test
    void shouldRejectRefreshWhenAccountDisabled() {

        User user = new User();

        user.setId(1L);
        user.setUsername("kishore");
        user.setEmail("kishore@example.com");
        user.setAccountEnabled(false);
        user.setAccountLocked(false);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);

        when(refreshTokenService.validateRefreshToken(
                "refresh-token"
        )).thenReturn(refreshToken);

        assertThatThrownBy(
                () -> authService.refresh(
                        "refresh-token"
                )
        )
                .isInstanceOf(
                        AccountDisabledException.class
                );

        verify(jwtService, never())
                .generateAccessToken(any(User.class));

        verify(refreshTokenService, never())
                .createRefreshToken(any(User.class));
    }
}