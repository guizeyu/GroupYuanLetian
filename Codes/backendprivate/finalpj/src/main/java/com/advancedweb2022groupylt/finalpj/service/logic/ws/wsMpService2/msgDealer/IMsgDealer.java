package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.msgDealer;

import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.UserRoomTable;
import com.alibaba.fastjson.JSONObject;

@FunctionalInterface
public interface IMsgDealer
{
/*    public static int getSceneFromJsonObj(JSONObject jsonObject)
    {
        int res = -1;
        try
        {
            res = Integer.parseInt(jsonObject.get("scene").toString());
        }
        catch (RuntimeException ignored)
        {

        }
        return res;
    }*/

    public abstract void deal(String username, JSONObject messageFromClient, UserRoomTable userRoomTable);
}
