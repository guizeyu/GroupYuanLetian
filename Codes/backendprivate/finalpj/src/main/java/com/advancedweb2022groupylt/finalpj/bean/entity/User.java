package com.advancedweb2022groupylt.finalpj.bean.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Entity
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private Long lastLoginTime=0L;
    private Long currentLoginTime=0L;
    private Integer scene=0;

    @JsonIgnore // obviously , we can't put password in json-web-token
    private String password;

    public User()
    {
    }

    public User(Long id, String username, String email, String password)
    {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.lastLoginTime = 0L;
        this.currentLoginTime = 0L;
        this.scene = 0;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    private String getTimeStr(long time)
    {
        return time<=0?"-":new Date(time).toString();
    }

    public Long getLastLoginTime()
    {
        return lastLoginTime;
    }

    public String getLastLoginTimeStr()
    {
        return getTimeStr(getLastLoginTime());
    }

    public void setLastLoginTime(Long lastLoginTime)
    {
        this.lastLoginTime = lastLoginTime;
    }

    public Long getCurrentLoginTime()
    {
        return currentLoginTime;
    }

    public String getCurrentLoginTimeStr()
    {
        return getTimeStr(getCurrentLoginTime());
    }

    public void setCurrentLoginTime(Long currentLoginTime)
    {
        this.currentLoginTime = currentLoginTime;
    }

    public Integer getScene()
    {
        return scene;
    }

    public void setScene(Integer scene)
    {
        this.scene = scene;
    }
}
