package com.nt.sms_module_worker.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nt.sms_module_worker.entity.ConfigConditionsEntity;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface SmsConditionRepo extends JpaRepository<ConfigConditionsEntity,Long> {

    @SuppressWarnings("null")
    @Query(value = """
        SELECT * FROM  config_conditions
        WHERE IS_ENABLE = 1
        AND ( ORDERTYPE =:orderType OR ORDERTYPE IS NULL )
        AND ( DATE_START <= :start_time OR DATE_START IS NULL )
       AND ( DATE_END >= :end_time  OR DATE_END IS NULL ) 
       """, nativeQuery = true)
    public List<ConfigConditionsEntity> findSmsCondition(@Param(value = "orderType")String orderType, @Param(value = "start_time")Timestamp startTime, @Param(value = "end_time")Timestamp edTime);

    
}
