package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.msgDealer;

import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.UserRoomTable;
import com.advancedweb2022groupylt.finalpj.util.SessionUtils;
import com.alibaba.fastjson.JSONObject;

public class Movement implements IMsgDealer
{
    @Override
    public void deal(String username, JSONObject messageFromClient,UserRoomTable userRoomTable)
    {
        UserRoomTable.UserSessionEntry movingUsr = userRoomTable.getUserInfo(username);
        if (movingUsr==null) return;
        messageFromClient.put("username",username);
        String messageToSend = messageFromClient.toJSONString();
        userRoomTable.forEachInRoom(movingUsr.getCurrentRoomIdx(), entry ->
        {
            if (!entry.getUsername().equals(username))
                SessionUtils.sendMessage(entry.getWsSession(),messageToSend,true);
        });
    }
}
