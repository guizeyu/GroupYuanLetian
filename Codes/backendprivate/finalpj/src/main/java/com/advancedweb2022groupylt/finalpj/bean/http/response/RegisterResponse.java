package com.advancedweb2022groupylt.finalpj.bean.http.response;

public class RegisterResponse
{
    private Message message;

    public RegisterResponse(Message message)
    {
        this.message = message;
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
