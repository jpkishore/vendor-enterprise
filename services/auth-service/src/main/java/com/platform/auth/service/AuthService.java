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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "CUSTOMER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // 1. Check email
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }

        // 2. Check username
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new UsernameAlreadyExistsException(
                    "Username is already taken"
            );
        }

        // 3. Find default CUSTOMER role
        Role customerRole = roleRepository
                .findByCodeIgnoreCase(request.role() != null ? request.role() : DEFAULT_ROLE )
                .orElseThrow(() ->
                        new RoleNotConfiguredException(
                                "Default CUSTOMER role is not configured"
                        )
                );

        // 4. Verify role is active
        if (customerRole.getStatus() != RoleStatus.ACTIVE) {
            throw new InactiveRoleException(
                    "Default CUSTOMER role is inactive"
            );
        }

        // 5. Create user
        User user = new User();

        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        user.setFirstName(
                request.firstName() != null
                        ? request.firstName().trim()
                        : null
        );

        user.setLastName(
                request.lastName() != null
                        ? request.lastName().trim()
                        : null
        );

        user.setPhone(
                request.phone() != null
                        ? request.phone().trim()
                        : null
        );

        // Security defaults
        user.setEmailVerified(false);
        user.setAccountEnabled(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        // 6. Assign CUSTOMER role
        user.addRole(customerRole);

        // 7. Save
        User savedUser = userRepository.save(user);

        // 8. Return response
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                "User registered successfully"
        );
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByUsernameOrEmailIgnoreCase(request.usernameOrEmail())
                .or(() ->
                        userRepository.findByEmailIgnoreCase(
                                request.usernameOrEmail()
                        )
                )
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid username/email or password"
                        )
                );

        if (!user.isAccountEnabled()) {
            throw new AccountDisabledException(
                    "User account is disabled"
            );
        }

        if (user.isAccountLocked()) {
            throw new AccountLockedException(
                    "User account is locked"
            );
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {

            throw new BadCredentialsException(
                    "Invalid username/email or password"
            );
        }

        String accessToken =
                jwtService.generateAccessToken(user);
        String refreshToken =
                refreshTokenService.createRefreshToken(user);
        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getCode)
                .toList();

        long expiresIn =
                jwtProperties.getAccessTokenExpirationMinutes()
                        * 60;

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }


    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {

        RefreshToken oldRefreshToken =
                refreshTokenService.validateRefreshToken(
                        rawRefreshToken
                );

        User user = oldRefreshToken.getUser();

        if (!user.isAccountEnabled()) {
            throw new AccountDisabledException(
                    "User account is disabled"
            );
        }

        if (user.isAccountLocked()) {
            throw new AccountLockedException(
                    "User account is locked"
            );
        }

        /*
         * Token rotation:
         *
         * Old refresh token becomes invalid.
         * A completely new refresh token is created.
         */

        refreshTokenService.revoke(oldRefreshToken);

        String accessToken =
                jwtService.generateAccessToken(user);

        String newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getCode)
                .toList();

        long expiresIn =
                jwtProperties
                        .getAccessTokenExpirationMinutes()
                        * 60;

        return new LoginResponse(
                accessToken,
                newRefreshToken,
                "Bearer",
                expiresIn,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }

    @Transactional
    public void logout(String rawRefreshToken) {

        RefreshToken refreshToken =
                refreshTokenService.validateRefreshToken(rawRefreshToken);

        refreshTokenService.revoke(refreshToken);
    }
}