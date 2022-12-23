package com.advancedweb2022groupylt.finalpj.bean.http.response;

public class ConnCheckResp
{
    private boolean connected;
    private Message message=new Message();

    public boolean isConnected()
    {
        return connected;
    }

    public void setConnected(boolean connected)
    {
        this.connected = connected;
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
