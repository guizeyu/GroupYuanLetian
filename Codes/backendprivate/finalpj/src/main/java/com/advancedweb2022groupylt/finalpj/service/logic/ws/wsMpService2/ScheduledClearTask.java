package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableScheduling
public class ScheduledClearTask // usage : clear dead sessions every 5 minutes
{
    @Resource
    private UserRoomTable userRoomTable;

    @Value("${timesetting.sessiontimeout}")
    private long timeoutInMillisecond;

    @Scheduled(fixedRate = 30000) // every 0.5 minute
    public void clearDeadSessions()
    {
        //StaticLog.info("clearing dead sessions which haven't heartbreak for {} seconds", timeoutInMillisecond /1000);
        userRoomTable.clearDeadSessions(timeoutInMillisecond);
        //StaticLog.info("clear dead session finish");
    }
}
