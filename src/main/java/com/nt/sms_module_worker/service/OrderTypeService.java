package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.model.dto.OrderTypeData;
import com.nt.sms_module_worker.model.dto.distribute.ReceivedData;


@Component
public class OrderTypeService {
    @Autowired
    private JdbcDatabaseService jbdcDB;

    public OrderTypeService() {}

    public List<OrderTypeData> getListOrderType() throws SQLException {
      // String query = "SELECT * FROM conditions";
      List<OrderTypeData> orderTypeDataList = new ArrayList<>();
      String tableName = "order_type";
      Connection con = jbdcDB.getConnection();
      String query = "SELECT * FROM "+ tableName;
      PreparedStatement statement = con.prepareStatement(query);
      ResultSet rs = statement.executeQuery();

      try{
          
          while (rs.next()) {
                OrderTypeData orderTypeData = new OrderTypeData();
                orderTypeData.setTYPEID(rs.getLong("TYPEID"));
                orderTypeData.setMainID(rs.getLong("MainID"));
                orderTypeData.setOrderType_Name(rs.getString("OrderType_Name"));
                orderTypeData.setIs_Delete(rs.getBoolean("Is_Delete"));
                orderTypeData.setIs_Enable(rs.getBoolean("Is_Enable"));
                orderTypeData.setCreated_Date(rs.getTimestamp("Created_Date"));
                orderTypeData.setUpdated_Date(rs.getTimestamp("Updated_Date"));
                orderTypeData.setCreated_By(rs.getLong("Created_By"));
                orderTypeData.setUpdated_By(rs.getLong("Updated_By"));
                orderTypeDataList.add(orderTypeData);
          }
          

      } catch (SQLException e) {
          System.out.println(" " + e.getMessage());
      } finally {
        // Step 4: Close Connection
        try {
            if (rs != null) {
                rs.close();
            }
            if (statement != null) {
              statement.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
      }
      return orderTypeDataList;
    }

    public String getQueryOrderTypeAvailable(ReceivedData receivedData) {
        String tableName = "order_type";
        String query = "SELECT * FROM "+ tableName +
                    " WHERE OrderType_Name='" + receivedData.getOrderType() + "'" +
                    " AND Is_Delete=0" ;

        // System.out.println("query ordertype : "+query);
        return query;
        
    }

    public OrderTypeData getOrderType(String query) throws SQLException {
        // String query = "SELECT * FROM conditions";
        String databaseName = "admin_red_sms";
        Connection con = jbdcDB.getConnection();
  
        OrderTypeData orderTypeData = new OrderTypeData();
        try{
            PreparedStatement statement = con.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
  
            
            if (rs.next()) {
                // Retrieve data from the first row
                orderTypeData.setTYPEID(rs.getLong("TYPEID"));
                orderTypeData.setMainID(rs.getLong("MainID"));
                orderTypeData.setOrderType_Name(rs.getString("OrderType_Name"));
                orderTypeData.setIs_Delete(rs.getBoolean("Is_Delete"));
                orderTypeData.setIs_Enable(rs.getBoolean("Is_Enable"));
                orderTypeData.setCreated_Date(rs.getTimestamp("Created_Date"));
                orderTypeData.setUpdated_Date(rs.getTimestamp("Updated_Date"));
                orderTypeData.setCreated_By(rs.getLong("Created_By"));
                orderTypeData.setUpdated_By(rs.getLong("Updated_By"));

            } 
            
  
        } catch (SQLException e) {
            System.out.println("Error getOrderType: " + e.getMessage());
            
        }
        return orderTypeData;
      }
}
