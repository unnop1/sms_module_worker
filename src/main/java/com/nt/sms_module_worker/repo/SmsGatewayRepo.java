package com.nt.sms_module_worker.repo;

import com.nt.sms_module_worker.entity.SmsGatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface SmsGatewayRepo extends JpaRepository<SmsGatewayEntity,Long> {
    @Query(value = """
        SELECT COUNT(*) FROM sms_gateway WHERE TRUNC(CREATED_DATE) = TRUNC(sysdate)
                """
                , nativeQuery = true)
    public Integer countTotalAllToday();
    
}
