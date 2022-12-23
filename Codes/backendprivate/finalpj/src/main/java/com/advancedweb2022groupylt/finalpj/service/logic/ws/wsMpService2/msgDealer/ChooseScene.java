package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.msgDealer;

import cn.hutool.log.StaticLog;
import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.UserRoomTable;
import com.alibaba.fastjson.JSONObject;

public class ChooseScene implements IMsgDealer
{
    @Override
    public void deal(String username, JSONObject messageFromClient,UserRoomTable userRoomTable)
    {
        int newRoomIdx = 0;
        try
        {
            newRoomIdx = (Integer)messageFromClient.get("scene");
        }
        catch (Exception e)
        {
            StaticLog.error(e);
            return;
        }
        userRoomTable.changeChatRoom(username,newRoomIdx);
        StaticLog.info("{} has switched to room {}",username,newRoomIdx);
    }
}
