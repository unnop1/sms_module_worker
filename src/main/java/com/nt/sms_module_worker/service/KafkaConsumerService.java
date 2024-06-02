package com.nt.sms_module_worker.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.util.UUID;
import org.springframework.kafka.support.KafkaHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.sms_module_worker.model.dto.SmsConditionData;
import com.nt.sms_module_worker.model.dto.SmsGatewayData;
import com.nt.sms_module_worker.model.dto.distribute.DataSmsMessage;
import com.nt.sms_module_worker.model.dto.distribute.ReceivedData;
import com.nt.sms_module_worker.model.dto.distribute.SendSmsGatewayData;
import com.nt.sms_module_worker.util.DateTime;
import com.nt.sms_module_worker.util.MapString;
import com.nt.sms_module_worker.model.dto.OrderTypeData;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
    
// Method 
@EnableKafka
@Component
public class KafkaConsumerService {

    private final Integer MaxRetrySendSmsCount = 3;  

    @Autowired
    private SmsConditionService smsConditionService;

    @Autowired
    private OrderTypeService orderTypeService;

    @Autowired
    private SmsGatewayService smsGatewayService;

    @KafkaListener(
        autoStartup="true",
        // {"orderType": "new", "payload":"123"}
        // topics = {"New", "Suspend", "Reconnect", "Change_Package", "Add_Package", "Delete_Package", "Topup_Recharge", "Package_Expire"}, 
        topics = {"NEW", "SUSPEND", "RECONNECT", "CHANGE_PACKAGE", "ADD_PACKAGE", "DELETE_PACKAGE", "TOPUP_RCHARGE", "PACKAGE_EXPIRE"}, 
        groupId = "sms_module.worker"
    )
    public void listening(@Payload String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws Exception {
        // System.out.println("Received message from topic " + topic);

        try {
            
            // Process the message based on the queue name
            processOrderType(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTransactionID(Timestamp ts) throws SQLException{
        String trxDate = DateTime.transactionDateStr(ts);
        Integer lastGID = smsGatewayService.countAllToday();
        String paddedNumber = String.format("%06d", lastGID);
        String transactionID = "REDSMS"+trxDate+ paddedNumber;
        return transactionID;
    }

    private void processOrderType(String messageMq) throws Exception {
        // Process for new_order_type
        Timestamp receiveDate = DateTime.getTimeStampNow();
        ObjectMapper objectMapper = new ObjectMapper();
        ReceivedData receivedData = objectMapper.readValue(messageMq, ReceivedData.class);

        String queryOrderType = orderTypeService.getQueryOrderTypeAvailable(receivedData); 
        OrderTypeData orderTypeData = orderTypeService.getOrderType(queryOrderType);
        System.out.println("orderTypeData: " + orderTypeData.getOrderType_Name() );
        if (orderTypeData.getOrderType_Name() == null){
            Timestamp createdDate = DateTime.getTimeStampNow();        
            SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
            smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
            smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
            smsMisMatchConditionGw.setIs_Status(2);
            smsMisMatchConditionGw.setPayloadMQ(messageMq);
            smsMisMatchConditionGw.setReceive_date(receiveDate);
            smsMisMatchConditionGw.setTransaction_id(getTransactionID(createdDate));
            smsMisMatchConditionGw.setRemark("ไม่พบ OrderType "+receivedData.getOrderType());
            smsMisMatchConditionGw.setCreated_Date(createdDate);
            smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
        }else{
            boolean isEnableOrderType = orderTypeData.getIs_Enable();
            System.out.println("isEnableOrderType: " + isEnableOrderType );
            if (isEnableOrderType){
                // System.out.println("smsEnableConditionGw remark" );
                String querySmsCondition = smsConditionService.getQueryOrderTypeSmsCondition(receivedData);
                List<SmsConditionData> smsConditions = smsConditionService.getListSmsCondition(querySmsCondition);
                System.out.println("smsConditions size: " + smsConditions.size() );
                
                if (smsConditions.size() > 0) {    
                    // send sms   
                    Boolean isAcceptPDPACheck = false;                 
                    for (SmsConditionData condition : smsConditions) {
                        JSONObject jsonData = new JSONObject(messageMq);
                        if (smsConditionService.checkSendSms(condition, jsonData)){
                            if(!isAcceptPDPACheck){
                                // Check PDPA
                                if (!isAcceptedPDPA()){
                                    Timestamp createdDate = DateTime.getTimeStampNow();        
                                    SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
                                    smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                                    smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                                    smsMisMatchConditionGw.setIs_Status(2);
                                    smsMisMatchConditionGw.setPayloadMQ(messageMq);
                                    smsMisMatchConditionGw.setReceive_date(receiveDate);
                                    smsMisMatchConditionGw.setTransaction_id(getTransactionID(createdDate));
                                    smsMisMatchConditionGw.setRemark("not accept pdpa OrderType "+receivedData.getOrderType());
                                    smsMisMatchConditionGw.setCreated_Date(createdDate);
                                    smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
                                    return;
                                }
                                isAcceptPDPACheck = true;
                            }
                            
                            String conditionMessage = condition.getMessage();
                            String smsMessage = MapString.mapPatternToSmsMessage(conditionMessage, jsonData);
                            SmsGatewayData smsMatchConditionGw = new SmsGatewayData();
                            Timestamp createdDate = DateTime.getTimeStampNow(); 
                            smsMatchConditionGw.setSMSMessage(smsMessage);
                            // System.out.println("condition.getConditionsID: " + condition.getConditionsID() );
                            smsMatchConditionGw.setConfig_conditions_ID(condition.getConditions_ID());
                            smsMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                            smsMatchConditionGw.setIs_Status(0);
                            smsMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                            smsMatchConditionGw.setOrder_type_MainID(orderTypeData.getMainID());
                            smsMatchConditionGw.setPayloadMQ(messageMq);
                            smsMatchConditionGw.setReceive_date(receiveDate);
                            smsMatchConditionGw.setTransaction_id(getTransactionID(createdDate));
                            smsMatchConditionGw.setCreated_Date(createdDate);
                            smsMatchConditionGw = smsGatewayService.createConditionalMessage(smsMatchConditionGw);
                            // System.out.println("smsMessage: " + smsMessage + " to phone " + receivedData.getMsisdn() );
                            
                            
                            for (int sendSmsCount = 1; sendSmsCount <= MaxRetrySendSmsCount ; sendSmsCount++) {
                                try{
                                    DataSmsMessage smsData = new DataSmsMessage();
                                    String systemTransRef = UUID.randomUUID().toString();
                                    smsData.setMessage(smsMessage);
                                    smsData.setSystemTransRef(systemTransRef);
                                    smsData.setTarget(receivedData.getMsisdn());
                                    smsData.setSource("my");
                                    smsData.setRequestDate(DateTime.getRequestDateUtcNow());
                                    List<DataSmsMessage> smsMessages = new ArrayList<>();
                                    smsMessages.add(smsData);
                                    
                                    SendSmsGatewayData sendSmsData = new SendSmsGatewayData();
                                    sendSmsData.setBulkRef("BulkTest-e9bfae24-82c5-11ee-b962-0242ac120002");
                                    sendSmsData.setMessages(smsMessages);
                                    smsConditionService.publish("RtcSmsBatchEx","" , sendSmsData);
                                    break;
                                }catch (Exception e){
                                    if (sendSmsCount >= MaxRetrySendSmsCount){
                                        Map<String, Object> updateInfo = new HashMap<String, Object>();
                                        updateInfo.put("Is_Status", 3);
                                        smsGatewayService.updateConditionalMessageById(smsMatchConditionGw.getGID(), updateInfo);
                                        return;
                                    }
                                    System.out.println("Error round "+sendSmsCount+" publishing: " + e.getMessage());
                                }
                            }
                            Timestamp sendDate = DateTime.getTimeStampNow(); 
                            Map<String, Object> updateInfo = new HashMap<String, Object>();
                            updateInfo.put("Is_Status", 1);
                            updateInfo.put("send_date", sendDate);
                            // System.out.println("smsMatchConditionGw.getGID: " + smsMatchConditionGw.getGID() );
                            smsGatewayService.updateConditionalMessageById(smsMatchConditionGw.getGID(), updateInfo);
                        }else{
                            Timestamp createdDate = DateTime.getTimeStampNow(); 
                            SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
                            smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                            smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                            smsMisMatchConditionGw.setOrder_type_MainID(orderTypeData.getMainID());
                            smsMisMatchConditionGw.setIs_Status(2);
                            smsMisMatchConditionGw.setPayloadMQ(messageMq);
                            smsMisMatchConditionGw.setReceive_date(receiveDate);
                            smsMisMatchConditionGw.setTransaction_id(getTransactionID(createdDate));
                            smsMisMatchConditionGw.setCreated_Date(createdDate);
                            smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
                        }
                    }

                }else{
                    Timestamp createdDate = DateTime.getTimeStampNow(); 
                    SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
                    smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                    smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                    smsMisMatchConditionGw.setOrder_type_MainID(orderTypeData.getMainID());
                    smsMisMatchConditionGw.setIs_Status(2);
                    smsMisMatchConditionGw.setTransaction_id(getTransactionID(createdDate));
                    smsMisMatchConditionGw.setPayloadMQ(messageMq);
                    smsMisMatchConditionGw.setReceive_date(receiveDate);
                    smsMisMatchConditionGw.setCreated_Date(createdDate);
                    smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
                }
            }else{
                Timestamp createdDate = DateTime.getTimeStampNow(); 
                SmsGatewayData smsNotEnableConditionGw = new SmsGatewayData();
                smsNotEnableConditionGw.setPhoneNumber(receivedData.getMsisdn());
                smsNotEnableConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                smsNotEnableConditionGw.setIs_Status(2);
                smsNotEnableConditionGw.setTransaction_id(getTransactionID(createdDate));
                smsNotEnableConditionGw.setPayloadMQ(messageMq);
                smsNotEnableConditionGw.setReceive_date(receiveDate);
                smsNotEnableConditionGw.setCreated_Date(createdDate);
                smsNotEnableConditionGw.setRemark("OrderType "+orderTypeData.getOrderType_Name()+" ถูกปิด");
                smsGatewayService.createConditionalMessage(smsNotEnableConditionGw);
                // System.out.println("smsNotEnableConditionGw remark: " + smsNotEnableConditionGw.getRemark() );
            }
        
        }
    }

    private boolean isAcceptedPDPA(){
        return true;
    }
}

