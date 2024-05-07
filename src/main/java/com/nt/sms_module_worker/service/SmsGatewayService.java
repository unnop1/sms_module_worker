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

  public SmsGatewayService() {}

  public Integer countAllToday() throws SQLException {
      String tableName = "sms_gateway";
      String query = "SELECT COUNT(*) FROM " + tableName + " WHERE TRUNC(created_date) = TRUNC(sysdate)";

      try (
          Connection con = JdbcDatabaseService.getConnection();
          PreparedStatement statement = con.prepareStatement(query);
          ResultSet rs = statement.executeQuery()
      ) {
          if (rs.next()) {
              return rs.getInt(1);
          }
      } catch (SQLException e) {
          System.out.println("Error countAll: " + e.getMessage());
          throw e; // Rethrow the exception to propagate it
      }

      return 0; // Return default value if no count found or an exception occurred
  }

  public SmsGatewayData createConditionalMessage(SmsGatewayData smsGatewayData) throws SQLException {
    String tableName = "sms_gateway";
    String insertQuery = "INSERT INTO " + tableName + " (config_conditions_ID, SMSMessage, order_type_MainID, OrderType, PhoneNumber, PayloadMQ, PayloadGW, Is_Status, Created_Date, remark, MESSAGE_RAW, RECEIVE_DATE, SEND_DATE, TRANSACTION_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (
        Connection con = JdbcDatabaseService.getConnection();
    ) {
        String returnCols[] = { "GID" };
        PreparedStatement statement = con.prepareStatement(insertQuery, returnCols);
        statement.setObject(1, smsGatewayData.getConfig_conditions_ID());
        statement.setObject(2, smsGatewayData.getSMSMessage());
        statement.setObject(3, smsGatewayData.getOrder_type_MainID());
        statement.setObject(4, smsGatewayData.getOrderType());
        statement.setObject(5, smsGatewayData.getPhoneNumber());
        statement.setObject(6, smsGatewayData.getPayloadMQ());
        statement.setObject(7, smsGatewayData.getPayloadGW());
        statement.setObject(8, smsGatewayData.getIs_Status());
        statement.setObject(9, smsGatewayData.getCreated_Date());
        statement.setObject(10, smsGatewayData.getRemark());
        statement.setObject(11, smsGatewayData.getMessage_Raw());
        statement.setObject(12, smsGatewayData.getReceive_date());
        statement.setObject(13, smsGatewayData.getSend_Date());
        statement.setObject(14, smsGatewayData.getTransaction_id());

        int rowsInserted = statement.executeUpdate();
        if (rowsInserted > 0) {
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                  long gid = generatedKeys.getLong(1); // Attempt to retrieve the generated key
                  if (!generatedKeys.wasNull()) { // Check if the key retrieved is not null
                      smsGatewayData.setGID(gid); // Set the GID in smsGatewayData
                  } else {
                      throw new SQLException("Creating smsGatewayData failed, no ID obtained.");
                  }
                } else {
                    throw new SQLException("Creating smsGatewayData failed, no ID obtained.");
                }
            }
        }
    } catch (SQLException e) {
        System.out.println("Error createConditionalMessage: " + e.getMessage());
        throw e; // Rethrow the exception to propagate it
    }

    return smsGatewayData;
}


  public void updateConditionalMessageById(Long smsGatewayId, Map<String, Object> updates) throws SQLException {
    String updateQuery = "UPDATE sms_gateway SET ";
    StringBuilder setClause = new StringBuilder();
    for (String key : updates.keySet()) {
        if (setClause.length() > 0) {
            setClause.append(", ");
        }
        setClause.append(key).append(" = ?");
    }
    updateQuery += setClause + " WHERE GID = ?";

    try (
        Connection con = JdbcDatabaseService.getConnection();
        PreparedStatement statement = con.prepareStatement(updateQuery)
    ) {
        int index = 1;
        for (Object value : updates.values()) {
            statement.setObject(index++, value);
        }
        statement.setLong(index, smsGatewayId);

        int rowsUpdated = statement.executeUpdate();
    } catch (SQLException e) {
        System.out.println("Error updateConditionalMessageById: " + e.getMessage());
        throw e; // Rethrow the exception to propagate it
    }
  }

}