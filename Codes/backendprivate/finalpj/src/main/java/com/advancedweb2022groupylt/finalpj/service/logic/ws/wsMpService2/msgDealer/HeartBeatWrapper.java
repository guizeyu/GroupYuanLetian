package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.msgDealer;

import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.UserRoomTable;
import com.alibaba.fastjson.JSONObject;

import java.util.Date;

// design mode : decorator
public class HeartBeatWrapper implements IMsgDealer
{
    private final IMsgDealer dealer;

    public HeartBeatWrapper(IMsgDealer dealer)
    {
        this.dealer = dealer;
    }

    @Override
    public void deal(String username, JSONObject messageFromClient,UserRoomTable userRoomTable)
    {
        UserRoomTable.UserSessionEntry entry = userRoomTable.getUserInfo(username);
        if (entry!=null)
        {
            long currentTime = new Date().getTime();
            entry.setLastActiveTime(currentTime);
            dealer.deal(username, messageFromClient,userRoomTable);
        }
    }
}
