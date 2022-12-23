package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.msgDealer;

import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.UserRoomTable;
import com.advancedweb2022groupylt.finalpj.util.SessionUtils;
import com.alibaba.fastjson.JSONObject;

public class PublicChat implements IMsgDealer
{
    @Override
    public void deal(String username, JSONObject messageFromClient,UserRoomTable userRoomTable)
    {
        UserRoomTable.UserSessionEntry entry = userRoomTable.getUserInfo(username);
        if (entry!=null)
        {
            messageFromClient.put("from_who",username);
            String messageToEveryBody = messageFromClient.toJSONString();
            userRoomTable.forEachInRoom(entry.getCurrentRoomIdx(), e -> SessionUtils.sendMessage(e.getWsSession(),messageToEveryBody,true));
        }
    }
}
