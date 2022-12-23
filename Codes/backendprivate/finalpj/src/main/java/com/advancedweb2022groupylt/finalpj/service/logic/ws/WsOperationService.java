package com.advancedweb2022groupylt.finalpj.service.logic.ws;

import com.advancedweb2022groupylt.finalpj.bean.http.response.ConnCheckResp;
import com.advancedweb2022groupylt.finalpj.bean.http.response.ForceOfflineResp;

public interface WsOperationService
{
    ConnCheckResp checkConnection(String token);

    ForceOfflineResp forceOffline(String token);
}
