package com.platform.auth.service;

import com.platform.auth.dto.user.UserResponse;
import com.platform.auth.entity.Role;
import com.platform.auth.entity.User;
import com.platform.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        return userRepository
                .findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user = userRepository
                .findWithRolesById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.isAccountEnabled(),
                user.isAccountLocked(),
                user.getRoles()
                        .stream()
                        .map(Role::getCode)
                        .toList()
        );
    }
}