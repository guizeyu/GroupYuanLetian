package com.advancedweb2022groupylt.finalpj.service.logic.middleStage;

public interface SceneTimeRecordService
{
    int get(String username,int scene);

    void add(String username,int scene,int seconds);

    default void add(String username,int scene,long ms)
    {
        add(username, scene, (int)(ms/1000));
    }

    default String getTimeStr(String username,int scene)
    {
        int t = get(username, scene);
        int s = t%60;
        t/=60;
        int m = t%60;
        int h = t/60;
        return String.format("%d小时%d分钟%d秒",h,m,s);
    }
}
