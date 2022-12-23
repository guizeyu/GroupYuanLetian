package com.advancedweb2022groupylt.finalpj.service.logic.ws;

import com.advancedweb2022groupylt.finalpj.bean.http.response.ConnCheckResp;
import com.advancedweb2022groupylt.finalpj.bean.http.response.ForceOfflineResp;
import com.advancedweb2022groupylt.finalpj.bean.http.response.Message;
import com.advancedweb2022groupylt.finalpj.service.jwtService.JWTService;
import com.advancedweb2022groupylt.finalpj.service.jwtService.JWTStatus;
import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.UserRoomTable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WsOperationServiceImpl implements WsOperationService
{
    @Resource
    private JWTService jwtService;
    @Resource
    private UserRoomTable userRoomTable;

    @Override
    public ConnCheckResp checkConnection(String token)
    {
        ConnCheckResp resp = new ConnCheckResp();
        JWTStatus jwtStatus = jwtService.checkStatus(token);
        if (jwtStatus==JWTStatus.VALID)
        {
            UserRoomTable.UserSessionEntry entry = userRoomTable.getUserInfo(jwtService.getUsername(token));
            resp.setConnected(entry!=null);
            resp.setMessage(new Message(true,"operation ok"));
        }
        else
        {
            resp.setMessage(new Message(false,"jwt invalid or expired"));
        }
        return resp;
    }

    @Override
    public ForceOfflineResp forceOffline(String token)
    {
        ForceOfflineResp resp = new ForceOfflineResp();
        JWTStatus jwtStatus = jwtService.checkStatus(token);
        if (jwtStatus==JWTStatus.VALID)
        {
            userRoomTable.removeUser(jwtService.getUsername(token));
            resp.setMessage(new Message(true,"operation ok"));
        }
        else
        {
            resp.setMessage(new Message(false,"jwt invalid or expired"));
        }
        return resp;
    }
}
