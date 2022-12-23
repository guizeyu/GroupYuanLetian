package com.advancedweb2022groupylt.finalpj;

import com.advancedweb2022groupylt.finalpj.bean.entity.timeRecord.TimeRecord;
import com.advancedweb2022groupylt.finalpj.bean.entity.timeRecord.TimeRecordPrimaryKey;
import com.advancedweb2022groupylt.finalpj.repository.TimeRecordRepository;
import com.advancedweb2022groupylt.finalpj.service.jwtService.JWTService;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/*@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FinalpjApplicationTests
{
    @Resource
    private TimeRecordRepository repo;

    @Test
    public void test()
    {
        TimeRecord record = new TimeRecord();
        record.setUsername("123456");
        record.setIdx(1);
        record.setSeconds(100);
        repo.save(record);
    }

    @Test
    public void testGet()
    {
        System.out.println(repo.findById(new TimeRecordPrimaryKey("123456",1)).orElse(new TimeRecord()));
    }
}*/
