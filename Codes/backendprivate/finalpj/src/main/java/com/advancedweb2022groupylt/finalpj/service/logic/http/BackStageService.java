package com.advancedweb2022groupylt.finalpj.service.logic.http;

import com.advancedweb2022groupylt.finalpj.bean.http.response.BackstageInfoResp;

public interface BackStageService
{
    BackstageInfoResp queryInfo(String username);

    BackstageInfoResp queryInfoByToken(String token);
}
