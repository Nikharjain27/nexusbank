package com.nexusbank.auth.service;

import com.nexusbank.auth.domain.entity.Role;
import com.nexusbank.auth.domain.entity.User;
import com.nexusbank.auth.dto.auth.LoginRequest;
import com.nexusbank.auth.dto.auth.LoginResponse;
import com.nexusbank.auth.dto.auth.RegisterRequest;
import com.nexusbank.auth.dto.auth.RegisterResponse;
import com.nexusbank.auth.repository.RoleRepository;
import com.nexusbank.auth.repository.UserRepository;
import com.nexusbank.auth.security.JwtService;
import com.nexusbank.auth.service.AuthServiceImpl;
import com.nexusbank.auth.service.RefreshTokenService;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                roleRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                refreshTokenService);
    }

    // ============================================================
    // Registration Tests
    // ============================================================

    @Test
    void registerShouldCreateCustomerUser() {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName(" Nikhar ");
        request.setLastName(" Jain ");
        request.setEmail(" NIKHAR.JAIN@example.com ");
        request.setPassword("password123");

        Role customerRole = new Role(
                "ROLE_CUSTOMER",
                "Customer role");

        when(userRepository.existsByEmail("nikhar.jain@example.com"))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_CUSTOMER"))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.save(userCaptor.capture()))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    var method = User.class.getDeclaredMethod("onCreate");
                    method.setAccessible(true);
                    method.invoke(user);

                    return user;
                });

        RegisterResponse response = authService.register(request);

        User savedUser = userCaptor.getValue();

        assertNotNull(response);
        assertNotNull(response.getUserId());

        assertEquals(
                "nikhar.jain@example.com",
                savedUser.getEmail());

        assertEquals(
                "Nikhar",
                savedUser.getFirstName());

        assertEquals(
                "Jain",
                savedUser.getLastName());

        assertEquals(
                "encoded-password",
                savedUser.getPasswordHash());

        assertTrue(
                savedUser.getRoles().contains(customerRole));

        assertEquals(
                "User registered successfully",
                response.getMessage());

        verify(userRepository)
                .existsByEmail("nikhar.jain@example.com");

        verify(roleRepository)
                .findByName("ROLE_CUSTOMER");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void registerShouldRejectDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Nikhar");
        request.setLastName("Jain");
        request.setEmail("Nikhar.Jain@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("nikhar.jain@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request));

        assertEquals(
                "Email is already registered",
                exception.getMessage());

        verify(userRepository)
                .existsByEmail("nikhar.jain@example.com");

        verify(roleRepository, never())
                .findByName(any());

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void registerShouldRejectWhenCustomerRoleIsMissing() {

        RegisterRequest request = new RegisterRequest();

        request.setFirstName("Nikhar");
        request.setLastName("Jain");
        request.setEmail("nikhar.jain@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("nikhar.jain@example.com"))
                .thenReturn(false);

        when(roleRepository.findByName("ROLE_CUSTOMER"))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authService.register(request));

        assertEquals(
                "Default customer role is not configured",
                exception.getMessage());

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any());
    }

    // ============================================================
    // Login Tests
    // ============================================================

    @Test
    void loginShouldReturnAccessAndRefreshTokens() {

        LoginRequest request = new LoginRequest(
                " NIKHAR.JAIN@example.com ",
                "password123");

        User user = new User(
                "nikhar.jain@example.com",
                "encoded-password",
                "Nikhar",
                "Jain");

        when(userRepository.findByEmail("nikhar.jain@example.com"))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn("refresh-token");

        when(jwtService.getAccessTokenExpiration())
                .thenReturn(java.time.Duration.ofMinutes(15));

        LoginResponse response = authService.login(request);

        assertNotNull(response);

        assertEquals(
                "access-token",
                response.accessToken());

        assertEquals(
                "refresh-token",
                response.refreshToken());

        assertEquals(
                "Bearer",
                response.tokenType());

        assertEquals(
                900,
                response.expiresIn());

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(userRepository)
                .findByEmail("nikhar.jain@example.com");

        verify(jwtService)
                .generateAccessToken(user);

        verify(refreshTokenService)
                .createRefreshToken(user);
    }

    @Test
    void loginShouldRejectInvalidCredentials() {

        LoginRequest request = new LoginRequest(
                "nikhar.jain@example.com",
                "wrong-password");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class))).thenThrow(
                        new AuthenticationException("Invalid credentials") {
                        });

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request));

        assertEquals(
                "Invalid email or password",
                exception.getMessage());

        verify(userRepository, never())
                .findByEmail(any());

        verify(jwtService, never())
                .generateAccessToken(any());

        verify(refreshTokenService, never())
                .createRefreshToken(any());
    }

    @Test
    void loginShouldRejectDisabledUser() {

        LoginRequest request = new LoginRequest(
                "nikhar.jain@example.com",
                "password123");

        User user = new User(
                "nikhar.jain@example.com",
                "encoded-password",
                "Nikhar",
                "Jain");

        // User is enabled by default.
        // This test will be completed once User exposes
        // a domain method for changing account status.

        when(userRepository.findByEmail("nikhar.jain@example.com"))
                .thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class))).thenReturn(
                        mock(org.springframework.security.core.Authentication.class));

        LoginResponse response = authService.login(request);

        assertNotNull(response);
    }

    @Test
    void loginShouldRejectLockedUser() {

        LoginRequest request = new LoginRequest(
                "nikhar.jain@example.com",
                "password123");

        User user = new User(
                "nikhar.jain@example.com",
                "encoded-password",
                "Nikhar",
                "Jain");

        // User is account-non-locked by default.
        // This test will be completed once User exposes
        // a domain method for changing lock status.

        when(userRepository.findByEmail("nikhar.jain@example.com"))
                .thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class))).thenReturn(
                        mock(org.springframework.security.core.Authentication.class));

        LoginResponse response = authService.login(request);

        assertNotNull(response);
    }
}
