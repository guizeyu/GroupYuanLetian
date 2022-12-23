package com.advancedweb2022groupylt.finalpj.bean.entity.timeRecord;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "time_record")
@IdClass(TimeRecordPrimaryKey.class)
// used to record how much time a user spent in a specific scene
public class TimeRecord
{
    // composed primary-key : consist of username and scene-index
    @Id
    private String username;
    @Id
    private Integer idx;
    private Integer seconds; // time spent in seconds

    public TimeRecord()
    {
    }

    public TimeRecord(String username, Integer idx, Integer seconds)
    {
        this.username = username;
        this.idx = idx;
        this.seconds = seconds;
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

    public Integer getSeconds()
    {
        return seconds;
    }

    public void setSeconds(Integer seconds)
    {
        this.seconds = seconds;
    }

    @Override
    public String toString()
    {
        return "TimeRecord{" +
                "username='" + username + '\'' +
                ", idx=" + idx +
                ", seconds=" + seconds +
                '}';
    }
}
