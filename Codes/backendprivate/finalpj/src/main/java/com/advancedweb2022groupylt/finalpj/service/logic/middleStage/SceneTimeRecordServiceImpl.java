package com.advancedweb2022groupylt.finalpj.service.logic.middleStage;

import com.advancedweb2022groupylt.finalpj.bean.entity.timeRecord.TimeRecord;
import com.advancedweb2022groupylt.finalpj.bean.entity.timeRecord.TimeRecordPrimaryKey;
import com.advancedweb2022groupylt.finalpj.repository.TimeRecordRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/*
    record how much time a user spent in a scene
 */
@Service
public class SceneTimeRecordServiceImpl implements SceneTimeRecordService
{
    @Resource
    private TimeRecordRepository timeRecordRepository;

    @Override
    public int get(String username, int scene)
    {
        return timeRecordRepository.findById(new TimeRecordPrimaryKey(username,scene)).orElse(new TimeRecord(username,scene,0)).getSeconds();
    }

    @Override
    public void add(String username, int scene, int seconds)
    {
        TimeRecord existingRecord = timeRecordRepository.findById(new TimeRecordPrimaryKey(username,scene)).orElse(new TimeRecord(username,scene,0));
        existingRecord.setSeconds(existingRecord.getSeconds()+seconds);
        timeRecordRepository.save(existingRecord);
    }
}
