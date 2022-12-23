package com.advancedweb2022groupylt.finalpj.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController
{
    // only for test use , useless after deployed
    @GetMapping(value = "/test")
    public String getTestStr()
    {
        return "hello world";
    }
}
