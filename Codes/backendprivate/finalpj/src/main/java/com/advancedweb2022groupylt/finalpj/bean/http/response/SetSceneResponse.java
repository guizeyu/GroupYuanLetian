package com.advancedweb2022groupylt.finalpj.bean.http.response;

public class SetSceneResponse
{
    private int scene;
    private Message message=new Message();

    public int getScene()
    {
        return scene;
    }

    public void setScene(int scene)
    {
        this.scene = scene;
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
