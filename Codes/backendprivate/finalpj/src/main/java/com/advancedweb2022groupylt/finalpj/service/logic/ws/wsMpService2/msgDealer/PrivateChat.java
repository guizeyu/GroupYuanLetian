package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.msgDealer;

import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.UserRoomTable;
import com.advancedweb2022groupylt.finalpj.util.SessionUtils;
import com.alibaba.fastjson.JSONObject;

public class PrivateChat implements IMsgDealer
{
    @Override
    public void deal(String username, JSONObject messageFromClient,UserRoomTable userRoomTable)
    {
//        int scene = IMsgDealer.getSceneFromJsonObj(messageFromClient);
        String toWho = messageFromClient.getString("to_who");
        UserRoomTable.UserSessionEntry fromUser = userRoomTable.getUserInfo(username);
        UserRoomTable.UserSessionEntry toUser = userRoomTable.getUserInfo(toWho);
        if (fromUser==null||toUser==null) return;
        if (fromUser.getCurrentRoomIdx()!=toUser.getCurrentRoomIdx()) return;
        messageFromClient.put("from_who",username);
        SessionUtils.sendMessage(toUser.getWsSession(),messageFromClient.toJSONString(),true);
    }
}
