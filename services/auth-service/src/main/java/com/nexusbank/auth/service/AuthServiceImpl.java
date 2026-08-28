package com.nexusbank.auth.service;

import com.nexusbank.auth.domain.entity.Role;
import com.nexusbank.auth.domain.entity.User;
import com.nexusbank.auth.dto.auth.LoginRequest;
import com.nexusbank.auth.dto.auth.LoginResponse;
import com.nexusbank.auth.dto.auth.RefreshTokenRequest;
import com.nexusbank.auth.dto.auth.RegisterRequest;
import com.nexusbank.auth.dto.auth.RegisterResponse;
import com.nexusbank.auth.repository.RoleRepository;
import com.nexusbank.auth.repository.UserRepository;
import com.nexusbank.auth.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;

        public AuthServiceImpl(
                        UserRepository userRepository,
                        RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        RefreshTokenService refreshTokenService) {
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
                this.passwordEncoder = passwordEncoder;
                this.authenticationManager = authenticationManager;
                this.jwtService = jwtService;
                this.refreshTokenService = refreshTokenService;
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
                                .orElseThrow(() -> new IllegalStateException(
                                                "Default customer role is not configured"));

                String encodedPassword = passwordEncoder.encode(request.getPassword());

                User user = new User(
                                normalizedEmail,
                                encodedPassword,
                                request.getFirstName().trim(),
                                request.getLastName().trim());

                user.getRoles().add(customerRole);

                User savedUser = userRepository.save(user);

                return new RegisterResponse(
                                savedUser.getPublicId(),
                                savedUser.getEmail(),
                                savedUser.getFirstName(),
                                savedUser.getLastName(),
                                "User registered successfully");
        }

        @Override
        public LoginResponse login(LoginRequest request) {

                String normalizedEmail = request.email()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        normalizedEmail,
                                                        request.password()));
                } catch (AuthenticationException exception) {
                        throw new IllegalArgumentException("Invalid email or password");
                }

                User user = userRepository.findByEmail(normalizedEmail)
                                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

                if (!user.isEnabled()) {
                        throw new IllegalStateException("User account is disabled");
                }

                if (!user.isAccountNonLocked()) {
                        throw new IllegalStateException("User account is locked");
                }

                String accessToken = jwtService.generateAccessToken(user);

                String refreshToken = refreshTokenService.createRefreshToken(user);

                return new LoginResponse(
                                accessToken,
                                refreshToken,
                                "Bearer",
                                jwtService.getAccessTokenExpiration().toSeconds());
        }

        @Override
        public LoginResponse refresh(RefreshTokenRequest request) {

                RefreshTokenService.LoginTokenResult result = refreshTokenService.refresh(request.refreshToken());

                return new LoginResponse(
                                result.accessToken(),
                                result.refreshToken(),
                                "Bearer",
                                result.expiresIn());
        }
}