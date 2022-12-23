package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.msgDealer;

import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.UserRoomTable;
import com.alibaba.fastjson.JSONObject;

public class HeartBeat implements IMsgDealer
{
    @Override
    public void deal(String username, JSONObject messageFromClient, UserRoomTable userRoomTable)
    {
        // noop , do nothing here.
    }
}
