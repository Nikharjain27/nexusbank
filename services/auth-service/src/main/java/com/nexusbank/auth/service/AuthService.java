package com.nexusbank.auth.service;

import com.nexusbank.auth.dto.auth.LoginRequest;
import com.nexusbank.auth.dto.auth.LoginResponse;
import com.nexusbank.auth.dto.auth.RefreshTokenRequest;
import com.nexusbank.auth.dto.auth.RegisterRequest;
import com.nexusbank.auth.dto.auth.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);
}