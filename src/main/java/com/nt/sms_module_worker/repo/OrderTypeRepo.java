package com.nt.sms_module_worker.repo;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.nt.sms_module_worker.entity.OrderTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTypeRepo extends JpaRepository<OrderTypeEntity,Long> {

    @SuppressWarnings("null")
    @Query(value = """
            SELECT * FROM order_type 
            WHERE OrderType_Name=:order_type
            AND Is_Delete=0
        """ 
        ,nativeQuery = true)
    public OrderTypeEntity getOrderType(@Param(value="order_type") String orderType);

}
