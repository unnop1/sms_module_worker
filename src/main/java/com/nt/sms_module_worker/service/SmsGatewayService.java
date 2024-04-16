package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
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

  public SmsGatewayData createConditionalMessage(SmsGatewayData smsGatewayData) throws SQLException {
    PreparedStatement statement = null;
    String databaseName = "admin_red_sms";
    String tableName = "sms_gateway";
    Connection con = jbdcDB.getConnection();
    try {
      // Define the SQL INSERT statement
      String insertQuery = "INSERT INTO "+ tableName +
            " (config_conditions_ID, SMSMessage, order_type_Main_ID, OrderType, PhoneNumber, PayloadMQ, PayloadGW, Is_Status, Created_Date, remark )" + 
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        System.out.println(insertQuery);
      // Create a PreparedStatement with the insertQuery
      String returnCols[] = { "GID" };
      statement = con.prepareStatement(insertQuery, returnCols);

      // Set values for the parameters in the prepared statement
      // statement.setLong(1, smsGatewayData.getSms_condition_SMSID()); // Replace value1 with the actual value for column1
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

      if (smsGatewayData.getOrder_type_Main_ID() != null) {
        statement.setLong(3, smsGatewayData.getOrder_type_Main_ID());
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
        System.out.println("Error: " + e.getMessage());
    } finally {
      // Step 4: Close Connection
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
            updateQuery.append(entry.getKey()).append(" = ").append(entry.getValue());
        }

        // Add the WHERE clause for the sms_conditions_SMSID
        updateQuery.append(" WHERE GID=").append(smsGatewayId);

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
        System.out.println("Error: " + e.getMessage());
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