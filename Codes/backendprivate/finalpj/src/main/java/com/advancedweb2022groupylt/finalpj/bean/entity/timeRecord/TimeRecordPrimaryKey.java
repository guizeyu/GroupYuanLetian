package com.advancedweb2022groupylt.finalpj.bean.entity.timeRecord;

import javax.persistence.Entity;
import java.io.Serializable;

public class TimeRecordPrimaryKey implements Serializable
{
    private String username;
    private Integer idx;

    public TimeRecordPrimaryKey()
    {
    }

    public TimeRecordPrimaryKey(String username, Integer idx)
    {
        this.username = username;
        this.idx = idx;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public Integer getIdx()
    {
        return idx;
    }

    public void setIdx(Integer idx)
    {
        this.idx = idx;
    }
}
