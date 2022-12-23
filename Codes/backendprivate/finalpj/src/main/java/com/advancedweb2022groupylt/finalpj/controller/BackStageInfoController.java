package com.advancedweb2022groupylt.finalpj.controller;

import com.advancedweb2022groupylt.finalpj.bean.http.response.BackstageInfoResp;
import com.advancedweb2022groupylt.finalpj.service.logic.http.BackStageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class BackStageInfoController
{
    @Resource
    private BackStageService backStageService;

    /* get all info about player :
        last login time
        current login time
        time spend in each room ...
     */
    @PostMapping("/backstage")
    public BackstageInfoResp queryBackStageInfo(@RequestHeader(name = "token") String token)
    {
        return backStageService.queryInfoByToken(token);
    }
}
