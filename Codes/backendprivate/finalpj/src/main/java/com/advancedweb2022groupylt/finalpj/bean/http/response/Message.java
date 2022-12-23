package com.advancedweb2022groupylt.finalpj.bean.http.response;

//every response should contain a message , to tell frontend whether the operation or query is successful
public class Message
{
    private boolean success;
    private String information;

    public Message()
    {

    }

    public Message(boolean success, String information)
    {
        this.success = success;
        this.information = information;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getInformation()
    {
        return information;
    }

    public void setInformation(String information)
    {
        this.information = information;
    }
}
