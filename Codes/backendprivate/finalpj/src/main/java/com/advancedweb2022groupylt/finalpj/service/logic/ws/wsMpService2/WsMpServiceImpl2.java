package com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2;

import cn.hutool.log.StaticLog;
import com.advancedweb2022groupylt.finalpj.bean.http.request.SetSceneRequest;
import com.advancedweb2022groupylt.finalpj.bean.http.response.SetSceneResponse;
import com.advancedweb2022groupylt.finalpj.service.logic.ws.WebsocketMultiplayerService;
import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.msgDealer.*;
import com.advancedweb2022groupylt.finalpj.util.SessionUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;

@Service
public class WsMpServiceImpl2 implements WebsocketMultiplayerService
{
    // command mode : set a dealer for every command
    private static final Map<String, IMsgDealer> CMD_DEALER_MAP = new HashMap<>();

/*    private static void putDealerHeartbeatWrapped(String cmdName,IMsgDealer dealer)
    {
        CMD_DEALER_MAP.put(cmdName,new HeartBeatWrapper(dealer));
    }*/

    static
    {
        CMD_DEALER_MAP.put("heartbeat",new HeartBeatWrapper(new HeartBeat()));
        CMD_DEALER_MAP.put("choose_scene",new HeartBeatWrapper(new ChooseScene()));
        CMD_DEALER_MAP.put("chat_to_all",new HeartBeatWrapper(new PublicChat()));
        CMD_DEALER_MAP.put("chat_private",new HeartBeatWrapper(new PrivateChat()));
        CMD_DEALER_MAP.put("character_move_to",new HeartBeatWrapper(new Movement()));
    }
    
    @Resource
    private UserRoomTable userRoomTable;

    @Override
    public void connect(String username, Session session)
    {
        StaticLog.info("{} connecting",username);
        UserRoomTable.UserSessionEntry entry = userRoomTable.getUserInfo(username);
        if (entry!=null) // this is a fake user
        {
            StaticLog.info("{} is already connected , so refusing the new connection",username);
            SessionUtils.closeSessionWithoutException(session);
            return;
        }
        userRoomTable.addUser(username, session);
    }

    @Override
    public void dealWithMessage(String username, String message)
    {
//        StaticLog.info("{} : {}",username,message);
        // can't log here , because there are too many movements
        try
        {
            JSONObject jsonObject = JSONObject.parseObject(message);
            String cmdName = jsonObject.get("command").toString();
            if (!cmdName.equals("character_move_to"))
                StaticLog.info("{} : {}",username,message);
            IMsgDealer dealer = CMD_DEALER_MAP.get(cmdName);
            if (dealer == null)
                StaticLog.error("invalid command : {} from user {}", cmdName, username);
            else dealer.deal(username, jsonObject,userRoomTable);
        }
        catch(Exception e)
        {
            StaticLog.error(e.getMessage());
        }
    }

    @Override
    public void clientClose(String username,Session session)
    {
        UserRoomTable.UserSessionEntry entry = userRoomTable.getUserInfo(username);
        if (entry==null||!entry.wsSession.equals(session)) // this is a fake user
        {
            SessionUtils.closeSessionWithoutException(session);
            return;
        }
        userRoomTable.removeUser(username);
    }

    @Override
    public int getScene(String username)
    {
        UserRoomTable.UserSessionEntry entry = userRoomTable.getUserInfo(username);
        if (entry==null) return -1;
        return entry.getCurrentRoomIdx();
    }

    @Override
    public SetSceneResponse setScene(SetSceneRequest request)
    {
        return userRoomTable.changeChatRoom(request.getUsername(),request.getScene());
    }
}
