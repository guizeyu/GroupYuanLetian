package com.advancedweb2022groupylt.finalpj.controller;

import com.advancedweb2022groupylt.finalpj.service.logic.ws.WebsocketMultiplayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

// from my point of view , it behaves like a controller , so I call it websocket controller
@ServerEndpoint("/ws/{username}")
@Component
public class WebsocketController
{
    /*
        note : don't change "static" !!!
        websocketController will be constructed many times ,
        but the service could be only autowired once , so we have to make it static
     */
    private static WebsocketMultiplayerService websocketMultiplayerService;

    @Autowired
    public void setWebsocketMultiplayerService(WebsocketMultiplayerService websocketMultiplayerService)
    {
        WebsocketController.websocketMultiplayerService = websocketMultiplayerService;
    }

    @OnOpen
    public void open(@PathParam("username")String username, Session session)
    {
        websocketMultiplayerService.connect(username, session);
    }

    @OnMessage
    public void dealMessage(@PathParam("username")String username,String message)
    {
        //websocketMultiplayerService.dealWithRawMessage(username, message); todo:delete this code
        websocketMultiplayerService.dealWithMessage(username, message);
    }

    @OnClose
    public void close(@PathParam("username")String username,Session session)
    {
        websocketMultiplayerService.clientClose(username,session);
    }
}
