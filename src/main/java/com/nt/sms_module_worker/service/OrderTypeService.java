package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.model.dto.OrderTypeData;
import com.nt.sms_module_worker.model.dto.distribute.ReceivedData;


@Component
public class OrderTypeService {

    public List<OrderTypeData> getListOrderType() throws SQLException {
        List<OrderTypeData> orderTypeDataList = new ArrayList<>();
        String tableName = "order_type";
        String query = "SELECT * FROM " + tableName;
    
        try (
            Connection con = JdbcDatabaseService.getConnection();
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery()
        ) {
            while (rs.next()) {
                OrderTypeData orderTypeData = new OrderTypeData();
                orderTypeData.setTYPEID(rs.getLong("TYPEID"));
                orderTypeData.setMainID(rs.getLong("MainID"));
                orderTypeData.setOrderType_Name(rs.getString("OrderType_Name"));
                orderTypeData.setIs_Delete(rs.getBoolean("Is_Delete"));
                orderTypeData.setIs_Enable(rs.getBoolean("Is_Enable"));
                orderTypeData.setCreated_Date(rs.getTimestamp("Created_Date"));
                orderTypeData.setUpdated_Date(rs.getTimestamp("Updated_Date"));
                orderTypeData.setCreated_By(rs.getString("Created_By"));
                orderTypeData.setUpdated_By(rs.getString("Updated_By"));
                orderTypeDataList.add(orderTypeData);
            }
        } catch (SQLException e) {
            System.out.println("Error getListOrderType: " + e.getMessage());
            throw e; // Rethrow the exception to propagate it
        }
    
        return orderTypeDataList;
    }

    public String getQueryOrderTypeAvailable(ReceivedData receivedData) {
        String tableName = "order_type";
        String query = "SELECT * FROM "+ tableName +
                    " WHERE OrderType_Name='" + receivedData.getOrderType().toUpperCase() + "'" +
                    " AND Is_Delete=0" ;

        // System.out.println("query ordertype : "+query);
        return query;
        
    }

    public OrderTypeData getOrderType(String query) throws SQLException {
        OrderTypeData orderTypeData = new OrderTypeData();
    
        try (Connection con = JdbcDatabaseService.getConnection();
             PreparedStatement statement = con.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {
    
            if (rs.next()) {
                orderTypeData.setTYPEID(rs.getLong("TYPEID"));
                orderTypeData.setMainID(rs.getLong("MainID"));
                orderTypeData.setOrderType_Name(rs.getString("OrderType_Name"));
                orderTypeData.setIs_Delete(rs.getBoolean("Is_Delete"));
                orderTypeData.setIs_Enable(rs.getBoolean("Is_Enable"));
                orderTypeData.setCreated_Date(rs.getTimestamp("Created_Date"));
                orderTypeData.setUpdated_Date(rs.getTimestamp("Updated_Date"));
                orderTypeData.setCreated_By(rs.getString("Created_By"));
                orderTypeData.setUpdated_By(rs.getString("Updated_By"));
            }
    
        } catch (SQLException e) {
            // Log or handle the exception appropriately
            System.out.println("Error getOrderType: " + e.getMessage());
            throw e; // Rethrow the exception or handle it as per your requirement
        }
    
        return orderTypeData;
    }
    
}
