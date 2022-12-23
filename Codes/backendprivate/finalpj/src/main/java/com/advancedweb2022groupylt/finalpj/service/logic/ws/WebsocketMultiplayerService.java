package com.advancedweb2022groupylt.finalpj.service.logic.ws;

import cn.hutool.core.codec.Base64;
import com.advancedweb2022groupylt.finalpj.bean.http.request.SetSceneRequest;
import com.advancedweb2022groupylt.finalpj.bean.http.response.SetSceneResponse;

import javax.websocket.Session;

public interface WebsocketMultiplayerService
{
    // web-socket
    void connect(String username, Session session);

    default void dealWithRawMessage(String username,String message)
    {
        String decodedMessage = Base64.decodeStr(message);
        dealWithMessage(username, decodedMessage);
    }

    void dealWithMessage(String username,String message);

    void clientClose(String username,Session session);

    //http
    int getScene(String username);

    SetSceneResponse setScene(SetSceneRequest request);
}
