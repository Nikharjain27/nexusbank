package com.nexusbank.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nexusbank.auth.security.JwtAuthenticationFilter;
import com.nexusbank.auth.security.JwtService;
import com.nexusbank.auth.security.NexusBankUserDetails;
import com.nexusbank.auth.security.NexusBankUserDetailsService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private NexusBankUserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {

        jwtService = mock(JwtService.class);

        userDetailsService = mock(NexusBankUserDetailsService.class);

        filter = new JwtAuthenticationFilter(
                jwtService,
                userDetailsService);

        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void requestWithoutAuthorizationHeaderShouldContinueChain()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    @Test
    void requestWithNonBearerAuthorizationHeaderShouldContinueChain()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Basic abc123");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    @Test
    void invalidJwtShouldClearAuthenticationAndContinueChain()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractClaims("invalid-token"))
                .thenThrow(
                        new IllegalArgumentException(
                                "Invalid token"));

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(jwtService)
                .extractClaims("invalid-token");

        verifyNoInteractions(userDetailsService);

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    @Test
    void validJwtShouldAuthenticateCustomer()
            throws Exception {

        UUID publicId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        NexusBankUserDetails userDetails = mock(NexusBankUserDetails.class);

        when(jwtService.extractClaims("valid-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(publicId.toString());

        when(userDetailsService.loadUserByPublicId(publicId))
                .thenReturn(userDetails);

        /*
         * doReturn avoids Mockito wildcard-capture
         * problems with Collection<? extends GrantedAuthority>
         */
        doReturn(
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_CUSTOMER")))
                .when(userDetails).getAuthorities();

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(jwtService)
                .extractClaims("valid-token");

        verify(userDetailsService)
                .loadUserByPublicId(publicId);

        verify(userDetails)
                .getAuthorities();

        verify(filterChain)
                .doFilter(request, response);

        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        assertNotNull(authentication);

        assertTrue(
                authentication instanceof UsernamePasswordAuthenticationToken);

        assertSame(
                userDetails,
                authentication.getPrincipal());

        assertTrue(
                authentication
                        .getAuthorities()
                        .contains(
                                new SimpleGrantedAuthority(
                                        "ROLE_CUSTOMER")));

        assertTrue(
                authentication.isAuthenticated());
    }

    @Test
    void validJwtShouldAuthenticateAdmin()
            throws Exception {

        UUID publicId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        NexusBankUserDetails userDetails = mock(NexusBankUserDetails.class);

        when(jwtService.extractClaims("admin-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(publicId.toString());

        when(userDetailsService.loadUserByPublicId(publicId))
                .thenReturn(userDetails);

        doReturn(
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_ADMIN")))
                .when(userDetails).getAuthorities();

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer admin-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        assertNotNull(authentication);

        assertSame(
                userDetails,
                authentication.getPrincipal());

        assertTrue(
                authentication
                        .getAuthorities()
                        .contains(
                                new SimpleGrantedAuthority(
                                        "ROLE_ADMIN")));

        assertTrue(
                authentication.isAuthenticated());

        verify(jwtService)
                .extractClaims("admin-token");

        verify(userDetailsService)
                .loadUserByPublicId(publicId);

        verify(userDetails)
                .getAuthorities();

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void malformedSubjectShouldNotAuthenticateUser()
            throws Exception {

        Claims claims = mock(Claims.class);

        when(jwtService.extractClaims("malformed-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("not-a-valid-uuid");

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer malformed-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(jwtService)
                .extractClaims("malformed-token");

        verifyNoInteractions(userDetailsService);

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    @Test
    void unknownUserShouldNotAuthenticateUser()
            throws Exception {

        UUID publicId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        when(jwtService.extractClaims("unknown-user-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(publicId.toString());

        when(userDetailsService.loadUserByPublicId(publicId))
                .thenThrow(
                        new RuntimeException(
                                "User not found"));

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer unknown-user-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(jwtService)
                .extractClaims("unknown-user-token");

        verify(userDetailsService)
                .loadUserByPublicId(publicId);

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }

    @Test
    void existingAuthenticationShouldNotBeOverwritten()
            throws Exception {

        var existingAuthentication = new UsernamePasswordAuthenticationToken(
                "existing-user",
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_ADMIN")));

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        existingAuthentication);

        UUID publicId = UUID.randomUUID();

        Claims claims = mock(Claims.class);

        NexusBankUserDetails userDetails = mock(NexusBankUserDetails.class);

        when(jwtService.extractClaims("valid-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn(publicId.toString());

        when(userDetailsService.loadUserByPublicId(publicId))
                .thenReturn(userDetails);

        doReturn(
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_CUSTOMER")))
                .when(userDetails).getAuthorities();

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        assertSame(
                existingAuthentication,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());

        verify(jwtService)
                .extractClaims("valid-token");

        verify(userDetailsService)
                .loadUserByPublicId(publicId);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void bearerPrefixWithoutTokenShouldNotAuthenticateUser()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer ");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractClaims(""))
                .thenThrow(
                        new IllegalArgumentException(
                                "Invalid token"));

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(jwtService)
                .extractClaims("");

        verifyNoInteractions(userDetailsService);

        verify(filterChain)
                .doFilter(request, response);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication());
    }
}