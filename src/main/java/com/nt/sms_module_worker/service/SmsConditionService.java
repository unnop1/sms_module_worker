package com.nt.sms_module_worker.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.apache.kafka.common.protocol.types.Field.Bool;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.model.dto.SmsConditionData;
import com.nt.sms_module_worker.model.dto.OrderTypeData;
import com.nt.sms_module_worker.model.dto.ReceivedData;

@Component
public class SmsConditionService {

  @Autowired
  private JdbcDatabaseService jbdcDB;

  private final RabbitTemplate rabbitTemplate;

  public SmsConditionService(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publish(String exchangeName, String routingKey,String message) throws Exception {
    System.out.println("Sending message...");
    rabbitTemplate.convertAndSend("", routingKey, message);
    // receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
  }

  public String getQueryOrderTypeSmsCondition(ReceivedData receivedData) {
    // Get the current date
    String currentDate = LocalDate.now().toString();

    String tableName = "sms_conditions";

    String query = "SELECT * FROM "+ tableName +
                  " WHERE ( OrderType ='" + receivedData.getOrder_type() + "'" + " OR OrderType IS NULL )" +
                  " AND ( serviceType ='" + receivedData.getServiceType() + "'" + " OR serviceType IS NULL )" +
                  " AND ( by_offeringId ='" + receivedData.getOfferingId() + "'" + " OR by_offeringId IS NULL )" +
                  " AND ( Chanel ='" + receivedData.getChannel() + "'" + " OR Chanel IS NULL )" +
                  " AND ( Frequency ='" + receivedData.getFrequency() + "'" + " OR Frequency IS NULL )" +
                  " AND ( DateStart <='" + currentDate + "'" + " OR DateStart IS NULL )" +
                  " AND ( DateEnd >='" + currentDate + "'" + " OR DateEnd IS NULL )";        
                  
    System.out.println("query condition : "+query);
    return query;
    
  }

  public List<SmsConditionData> getListSmsCondition(String query) throws SQLException {
      // String query = "SELECT * FROM conditions";
      List<SmsConditionData> conditionDataList = new ArrayList<>();
      String databaseName = "admin_red_sms";
      Connection con = jbdcDB.getConnection();

      PreparedStatement statement = con.prepareStatement(query);
      ResultSet rs = statement.executeQuery();

      try{          
          while (rs.next()) {
              SmsConditionData smsConditionData = new SmsConditionData();
              smsConditionData.setOrderType(rs.getString("OrderType"));
              smsConditionData.setChanel(rs.getString("Chanel"));
              smsConditionData.setFrequency(rs.getString("Frequency"));
              smsConditionData.setMessage(rs.getString("Message"));
              smsConditionData.setSMSID(rs.getLong("SMSID"));
              smsConditionData.setOrder_type_MainID(rs.getLong("order_type_MainID"));
              smsConditionData.setServiceType(rs.getInt("serviceType"));
              smsConditionData.setBy_offeringId(rs.getString("by_offeringId"));
              smsConditionData.setDateStart(rs.getDate("DateStart"));
              smsConditionData.setDateEnd(rs.getDate("DateEnd"));
              smsConditionData.setIsEnable(rs.getBoolean("IsEnable"));
              smsConditionData.setIsDelete(rs.getBoolean("IsDelete"));
              smsConditionData.setCreatedDate(rs.getTimestamp("CreatedDate"));
              smsConditionData.setCreatedBy_UserID(rs.getLong("CreatedBy_UserID"));
              smsConditionData.setUpdatedDate(rs.getTimestamp("UpdatedDate"));
              smsConditionData.setUpdatedBy_UserID(rs.getLong("UpdatedBy_UserID"));

              conditionDataList.add(smsConditionData);
          }
          
      } catch (SQLException e) {
          System.out.println("Error: " + e.getMessage());
          
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
      return conditionDataList;
  }

}