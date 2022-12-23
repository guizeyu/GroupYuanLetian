package com.advancedweb2022groupylt.finalpj.bean.http.response;

public class LoginResponse
{
    private String token;
    private Message message;

    public LoginResponse(String token, Message message)
    {
        this.token = token;
        this.message = message;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public Message getMessage()
    {
        return message;
    }

    public void setMessage(Message message)
    {
        this.message = message;
    }
}
