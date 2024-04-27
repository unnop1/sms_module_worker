package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.model.dto.SmsGatewayData;


@Component
class SmsGatewayService {
  @Autowired
  private JdbcDatabaseService jbdcDB;

  public SmsGatewayService() {}

  public Integer countAllToday() throws SQLException {
    PreparedStatement statement = null;
    String tableName = "sms_gateway";
    Connection con = jbdcDB.getConnection();
    try {

      String query = "SELECT COUNT(*) FROM " + tableName + " WHERE TRUNC(created_date) = TRUNC(sysdate)";
      statement = con.prepareStatement(query);
      
      ResultSet rs = statement.executeQuery();
      
      Integer count = null;
      if (rs.next()) {
          count = rs.getInt(1);
      }
      // System.out.println("query: " + query);
      // System.out.println("count: " + count);
      return count;

    } catch (SQLException e) {
      System.out.println("Error countAll: " + e.getMessage());
    } finally {
      try {
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
    return 0;
  }

  public SmsGatewayData createConditionalMessage(SmsGatewayData smsGatewayData) throws SQLException {
    PreparedStatement statement = null;
    String databaseName = "admin_red_sms";
    String tableName = "sms_gateway";
    Connection con = jbdcDB.getConnection();
    try {
      // Define the SQL INSERT statement
      String insertQuery = "INSERT INTO "+ tableName +
            " (config_conditions_ID, SMSMessage, order_type_MainID, OrderType, PhoneNumber, PayloadMQ, PayloadGW, Is_Status, Created_Date, remark, MESSAGE_RAW, RECEIVE_DATE, SEND_DATE, TRANSACTION_ID )" + 
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

      // Create a PreparedStatement with the insertQuery
      String returnCols[] = { "GID" };
      statement = con.prepareStatement(insertQuery, returnCols);

      if (smsGatewayData.getConfig_conditions_ID() != null) {
        statement.setLong(1, smsGatewayData.getConfig_conditions_ID());
      } else {
        statement.setNull(1, java.sql.Types.BIGINT);
      }

      if (smsGatewayData.getSMSMessage() != null) {
        statement.setString(2, smsGatewayData.getSMSMessage());
      }else {
        statement.setNull(2, java.sql.Types.LONGVARCHAR);
      }

      if (smsGatewayData.getOrder_type_MainID() != null) {
        statement.setLong(3, smsGatewayData.getOrder_type_MainID());
      }else {
        statement.setNull(3, java.sql.Types.BIGINT);
      }

      if (smsGatewayData.getOrderType() != null) {
        statement.setString(4,smsGatewayData.getOrderType());
      }else {
        statement.setNull(4, java.sql.Types.VARCHAR);
      }

      if (smsGatewayData.getPhoneNumber() != null) {
        statement.setString(5,smsGatewayData.getPhoneNumber());
      }else {
        statement.setNull(5, java.sql.Types.VARCHAR);
      }
      
      if (smsGatewayData.getPayloadMQ() != null) {
        statement.setString(6,smsGatewayData.getPayloadMQ());
      }else {
        statement.setNull(6, java.sql.Types.VARCHAR);
      }

      if (smsGatewayData.getPayloadGW() != null) {
        statement.setString(7,smsGatewayData.getPayloadGW());
      }else {
        statement.setNull(7, java.sql.Types.VARCHAR);
      }

      if (smsGatewayData.getIs_Status() != null) {
        statement.setInt(8,smsGatewayData.getIs_Status());
      }else {
        statement.setNull(8, java.sql.Types.INTEGER);
      }

      if (smsGatewayData.getCreated_Date() != null) {
        statement.setTimestamp(9,smsGatewayData.getCreated_Date());
      }else {
        statement.setNull(9, java.sql.Types.TIMESTAMP);
      }

      if (smsGatewayData.getRemark() != null) {
        statement.setString(10,smsGatewayData.getRemark());
      }else {
        statement.setNull(10, java.sql.Types.VARCHAR);
      }

      if (smsGatewayData.getMessage_Raw() != null) {
        statement.setString(11,smsGatewayData.getMessage_Raw());
      }else {
        statement.setNull(11, java.sql.Types.VARCHAR);
      }

      if (smsGatewayData.getReceive_date() != null) {
        statement.setTimestamp(12,smsGatewayData.getReceive_date());
      }else {
        statement.setNull(12, java.sql.Types.TIMESTAMP);
      }

      if (smsGatewayData.getSend_Date() != null) {
        statement.setTimestamp(13,smsGatewayData.getSend_Date());
      }else {
        statement.setNull(13, java.sql.Types.TIMESTAMP);
      }

      if (smsGatewayData.getTransaction_id() != null) {
        statement.setString(14,smsGatewayData.getTransaction_id());
      }else {
        statement.setNull(14, java.sql.Types.VARCHAR);
      }
      
      // Execute the insert operation
      int rowsInserted = statement.executeUpdate();
      // System.out.println("Rows inserted: " + rowsInserted);
      if (rowsInserted > 0) {
          // Retrieve the generated keys
          try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
              smsGatewayData.setGID(generatedKeys.getLong(1));
            }
            else {
                throw new SQLException("Creating smsGatewayData failed, no ID obtained.");
            }
        }
      }
        
    } catch (SQLException e) {
        System.out.println("Error createConditionalMessage: " + e.getMessage());
    } finally {
      try {
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
    // System.out.println("GID: " + smsGatewayData.getGID().toString());
    return smsGatewayData;
  }

  public void updateConditionalMessageById(Long smsGatewayId, Map<String, Object> updates) throws SQLException {
    Connection con = jbdcDB.getConnection();
    PreparedStatement statement = null;
    try {
        // Create the SQL UPDATE statement
        StringBuilder updateQuery = new StringBuilder("UPDATE sms_gateway SET ");
        List<Object> values = new ArrayList<>();

        // Build the SET clause dynamically based on the updates map
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            if (updateQuery.length() > 23) {
                updateQuery.append(", ");
            }
            updateQuery.append(entry.getKey()).append(" = ?");
            values.add(entry.getValue()); // Add the parameter value
        }

        // Add the WHERE clause for the sms_conditions_SMSID
        updateQuery.append(" WHERE GID=?");
        values.add(smsGatewayId); // Add the parameter value

        // Prepare the statement
        statement = con.prepareStatement(updateQuery.toString());

        // Set values for the parameters
        int index = 1;
        for (Object value : values) {
            statement.setObject(index++, value);
        }
        // System.out.println("updateQuery: " + updateQuery);

        // Execute the update operation
        int rowsUpdated = statement.executeUpdate();
        // System.out.println("Rows updated: " + rowsUpdated);
    } catch (SQLException e) {
        System.out.println("Error updateConditionalMessageById: " + e.getMessage());
    } finally {
        // Close resources
        try {
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
}

}