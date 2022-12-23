package com.advancedweb2022groupylt.finalpj.controller;

import com.advancedweb2022groupylt.finalpj.bean.http.request.LoginRequest;
import com.advancedweb2022groupylt.finalpj.bean.http.request.RegisterRequest;
import com.advancedweb2022groupylt.finalpj.bean.http.response.LoginResponse;
import com.advancedweb2022groupylt.finalpj.bean.http.response.RegisterResponse;
import com.advancedweb2022groupylt.finalpj.service.logic.http.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class AuthController
{
    @Resource
    private AuthService authService;

    @PostMapping(value = "/register")
    public RegisterResponse register(@RequestBody RegisterRequest request)
    {
        return authService.register(request);
    }

    @PostMapping(value = "/login")
    public LoginResponse login(@RequestBody LoginRequest request)
    {
        return authService.login(request);
    }
}
