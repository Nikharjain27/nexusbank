package com.nexusbank.auth.service;

import com.nexusbank.auth.domain.entity.Role;
import com.nexusbank.auth.domain.entity.User;
import com.nexusbank.auth.dto.auth.RegisterRequest;
import com.nexusbank.auth.dto.auth.RegisterResponse;
import com.nexusbank.auth.repository.RoleRepository;
import com.nexusbank.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String CUSTOMER_ROLE = "ROLE_CUSTOMER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {

        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        Role customerRole = roleRepository.findByName(CUSTOMER_ROLE)
                .orElseThrow(() ->
                        new IllegalStateException("Default customer role is not configured")
                );

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(
                normalizedEmail,
                encodedPassword,
                request.getFirstName().trim(),
                request.getLastName().trim()
        );

        user.getRoles().add(customerRole);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getPublicId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                "User registered successfully"
        );
    }
}