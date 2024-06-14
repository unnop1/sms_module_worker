package com.nt.sms_module_worker.repo;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.nt.sms_module_worker.entity.OrderTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTypeRepo extends JpaRepository<OrderTypeEntity,Long> {

    @SuppressWarnings("null")
    @Query(value = """
            SELECT * FROM order_type 
            WHERE ORDERTYPE_NAME=:order_type
            AND IS_DELETE=0
        """ 
        ,nativeQuery = true)
    public OrderTypeEntity getOrderType(@Param(value="order_type") String orderType);

}
