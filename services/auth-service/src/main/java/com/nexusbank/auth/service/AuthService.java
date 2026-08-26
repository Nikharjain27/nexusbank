package com.nexusbank.auth.service;

import com.nexusbank.auth.dto.auth.RegisterRequest;
import com.nexusbank.auth.dto.auth.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
}