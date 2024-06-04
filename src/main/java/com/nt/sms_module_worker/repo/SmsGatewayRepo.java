package com.nt.sms_module_worker.repo;

import java.sql.Timestamp;
import java.util.List;
import com.nt.sms_module_worker.entity.SmsGatewayEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface SmsGatewayRepo extends JpaRepository<SmsGatewayEntity,Long> {
    @Query(value = """
        SELECT COUNT(*) FROM sms_gateway WHERE TRUNC(created_date) = TRUNC(sysdate)
                """
                , nativeQuery = true)
    public Integer countTotalAllToday();
    
}
