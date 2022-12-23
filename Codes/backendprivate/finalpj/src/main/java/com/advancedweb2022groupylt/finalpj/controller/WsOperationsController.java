package com.advancedweb2022groupylt.finalpj.controller;

import com.advancedweb2022groupylt.finalpj.bean.http.response.ConnCheckResp;
import com.advancedweb2022groupylt.finalpj.bean.http.response.ForceOfflineResp;
import com.advancedweb2022groupylt.finalpj.service.logic.ws.WsOperationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class WsOperationsController
{
    @Resource
    private WsOperationService wsOperationService;

    @PostMapping("/websocketConnectionCheck")
    public ConnCheckResp checkConnection(@RequestHeader(name = "token") String token)
    {
        return wsOperationService.checkConnection(token);
    }

    /*
         a user can force offline his account's another ws-connection with server
         (maybe in another browser ,
         or a previous connection which hasn't been correctly closed)
    */
    @PostMapping("/websocketForceOffline")
    public ForceOfflineResp forceOffline(@RequestHeader(name = "token") String token)
    {
        return wsOperationService.forceOffline(token);
    }
}
