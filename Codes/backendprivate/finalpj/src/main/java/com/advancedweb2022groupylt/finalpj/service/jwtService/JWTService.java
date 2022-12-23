package com.advancedweb2022groupylt.finalpj.service.jwtService;

import java.util.Map;

public interface JWTService
{
    String generateToken(String username);

    JWTStatus checkStatus(String token);

    Map<String,String> getPayload(String token);

    default String getUsername(String token)
    {
        return getPayload(token).get("username");
    }
}
