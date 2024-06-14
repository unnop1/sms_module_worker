package com.nt.sms_module_worker.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.entity.OrderTypeEntity;
import com.nt.sms_module_worker.repo.OrderTypeRepo;


@Component
public class OrderTypeService {

    @Autowired
    private OrderTypeRepo orderTypeRepo;

    public List<OrderTypeEntity> getListOrderType() throws SQLException {
        List<OrderTypeEntity> orderTypeDataList = orderTypeRepo.findAll();
        return orderTypeDataList;
    }

    public OrderTypeEntity getOrderType(String orderType) throws SQLException {
        OrderTypeEntity orderTypeData = orderTypeRepo.getOrderType(orderType);
    
        return orderTypeData;
    }
    
}
