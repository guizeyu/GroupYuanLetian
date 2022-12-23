package com.advancedweb2022groupylt.finalpj.repository;

import com.advancedweb2022groupylt.finalpj.bean.entity.timeRecord.TimeRecord;
import com.advancedweb2022groupylt.finalpj.bean.entity.timeRecord.TimeRecordPrimaryKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeRecordRepository extends CrudRepository<TimeRecord, TimeRecordPrimaryKey>
{
}
