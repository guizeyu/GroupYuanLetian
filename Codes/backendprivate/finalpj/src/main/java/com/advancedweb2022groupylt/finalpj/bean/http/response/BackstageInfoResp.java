package com.advancedweb2022groupylt.finalpj.bean.http.response;

import java.util.List;

public class BackstageInfoResp
{
    private List<InfoItem> information;
    private Message message;

    public List<InfoItem> getInformation()
    {
        return information;
    }

    public void setInformation(List<InfoItem> information)
    {
        this.information = information;
    }

    public Message getMessage()
    {
        return message;
    }

    public void setMessage(Message message)
    {
        this.message = message;
    }

    public static class InfoItem
    {
        private String type,info;

        public InfoItem()
        {
        }

        public InfoItem(String type, String info)
        {
            this.type = type;
            this.info = info;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public String getInfo()
        {
            return info;
        }

        public void setInfo(String info)
        {
            this.info = info;
        }
    }
}
