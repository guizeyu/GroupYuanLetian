package com.advancedweb2022groupylt.finalpj.service.logic.http;

import com.advancedweb2022groupylt.finalpj.bean.http.request.LoginRequest;
import com.advancedweb2022groupylt.finalpj.bean.http.request.RegisterRequest;
import com.advancedweb2022groupylt.finalpj.bean.http.response.LoginResponse;
import com.advancedweb2022groupylt.finalpj.bean.http.response.RegisterResponse;

public interface AuthService
{
    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);
}
