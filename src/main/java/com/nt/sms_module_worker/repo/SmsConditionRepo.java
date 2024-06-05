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
        WHERE is_enable = 1
        AND ( orderType =:orderType OR orderType IS NULL )
        AND ( date_Start <= :start_time OR date_Start IS NULL )
       AND ( date_End >= :end_time  OR date_End IS NULL ) 
       """, nativeQuery = true)
    public List<ConfigConditionsEntity> findSmsCondition(@Param(value = "orderType")String orderType, @Param(value = "start_time")Timestamp startTime, @Param(value = "end_time")Timestamp edTime);

    
}
