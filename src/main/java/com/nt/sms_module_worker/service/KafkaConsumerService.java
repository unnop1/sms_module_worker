package com.nt.sms_module_worker.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.sms_module_worker.model.dto.SmsConditionData;
import com.nt.sms_module_worker.model.dto.SmsGatewayData;
import com.nt.sms_module_worker.model.dto.distribute.ReceivedData;
import com.nt.sms_module_worker.model.dto.OrderTypeData;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
    
// Method 
@EnableKafka
@Service
public class KafkaConsumerService {

    private final Integer MaxRetrySendSmsCount = 3;  

    @Autowired
    private SmsConditionService smsConditionService;

    @Autowired
    private OrderTypeService orderTypeService;

    @Autowired
    private SmsGatewayService smsGatewayService;

    @KafkaListener(
        // {"orderType": "new", "payload":"123"}
        topics = {"New", "Suspend", "Reconnect", "Change_Package", "Add_Package", "Delete_Package", "Topup_Recharge", "Package_Expire"}, 
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

    private void processOrderType(String messageMq) throws Exception {
        // Process for new_order_type
        ObjectMapper objectMapper = new ObjectMapper();
        ReceivedData receivedData = objectMapper.readValue(messageMq, ReceivedData.class);

        String queryOrderType = orderTypeService.getQueryOrderTypeAvailable(receivedData); 
        OrderTypeData orderTypeData = orderTypeService.getOrderType(queryOrderType);
        if (orderTypeData.getOrderType_Name() == null){
            Instant instant = Instant.now();
            Timestamp createdDate = Timestamp.from(instant);
            SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
            smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
            smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
            smsMisMatchConditionGw.setIs_Status(2);
            smsMisMatchConditionGw.setPayloadMQ(messageMq);
            smsMisMatchConditionGw.setRemark("ไม่พบ OrderType "+receivedData.getOrderType());
            smsMisMatchConditionGw.setCreated_Date(createdDate);
            smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
        }else{
            boolean isEnableOrderType = orderTypeData.getIs_Enable();
            // System.out.println("isEnableOrderType: " + isEnableOrderType );
            if (isEnableOrderType){
                // System.out.println("smsEnableConditionGw remark" );
                String querySmsCondition = smsConditionService.getQueryOrderTypeSmsCondition(receivedData);
                List<SmsConditionData> smsConditions = smsConditionService.getListSmsCondition(querySmsCondition);
                
                if (smsConditions.size() > 0) {    
                    // send sms
                    // System.out.println("smsConditions size: " + smsConditions.size() );
                    boolean isNotMatchCondition = true;
                    for (SmsConditionData condition : smsConditions) {
                        JSONObject jsonData = new JSONObject(messageMq);
                        if (smsConditionService.checkSendSms(condition, jsonData)){
                            // System.out.println("condition.getSMSID(): " + condition.getSMSID());
                            isNotMatchCondition = false;
                            String smsMessage = condition.getMessage();
                            SmsGatewayData smsMatchConditionGw = new SmsGatewayData();
                            Instant instant = Instant.now();
                            Timestamp createdDate = Timestamp.from(instant);
                            smsMatchConditionGw.setSMSMessage(smsMessage);
                            // System.out.println("condition.getConditionsID: " + condition.getConditionsID() );
                            smsMatchConditionGw.setConfig_conditions_ID(condition.getConditions_ID());
                            smsMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                            smsMatchConditionGw.setIs_Status(0);
                            smsMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                            smsMatchConditionGw.setOrder_type_Main_ID(orderTypeData.getMainID());
                            smsMatchConditionGw.setPayloadMQ(messageMq);
                            smsMatchConditionGw.setCreated_Date(createdDate);
                            smsMatchConditionGw = smsGatewayService.createConditionalMessage(smsMatchConditionGw);
                            // System.out.println("smsMessage: " + smsMessage + " to phone " + receivedData.getMsisdn() );
                            
                            
                            // for (int sendSmsCount = 1; sendSmsCount <= MaxRetrySendSmsCount ; sendSmsCount++) {
                            //     try{
                            //         smsConditionService.publish("","sms_target" , smsMessage);
                            //     }catch (Exception e){
                            //         if (sendSmsCount >= MaxRetrySendSmsCount){
                            //             Map<String, Object> updateInfo = new HashMap<String, Object>();
                            //             updateInfo.put("IsStatus", 3);
                            //             smsGatewayService.updateConditionalMessageById(smsMatchConditionGw.getGID(), updateInfo);
                            //             return;
                            //         }
                            //         System.out.println("Error round "+sendSmsCount+" publishing: " + e.getMessage());
                            //     }
                            // }
                            Map<String, Object> updateInfo = new HashMap<String, Object>();
                            updateInfo.put("IsStatus", 1);
                            // System.out.println("smsMatchConditionGw.getGID: " + smsMatchConditionGw.getGID() );
                            smsGatewayService.updateConditionalMessageById(smsMatchConditionGw.getGID(), updateInfo);
                        }
                    }

                    if (isNotMatchCondition){
                        Instant instant = Instant.now();
                        Timestamp createdDate = Timestamp.from(instant);
                        SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
                        smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                        smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                        smsMisMatchConditionGw.setOrder_type_Main_ID(orderTypeData.getMainID());
                        smsMisMatchConditionGw.setIs_Status(2);
                        smsMisMatchConditionGw.setPayloadMQ(messageMq);
                        smsMisMatchConditionGw.setCreated_Date(createdDate);
                        smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
                    }

                }else{
                    Instant instant = Instant.now();
                    Timestamp createdDate = Timestamp.from(instant);
                    SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
                    smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                    smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                    smsMisMatchConditionGw.setOrder_type_Main_ID(orderTypeData.getMainID());
                    smsMisMatchConditionGw.setIs_Status(2);
                    smsMisMatchConditionGw.setPayloadMQ(messageMq);
                    smsMisMatchConditionGw.setCreated_Date(createdDate);
                    smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
                }
            }else{
                Instant instant = Instant.now();
                Timestamp createdDate = Timestamp.from(instant);
                SmsGatewayData smsNotEnableConditionGw = new SmsGatewayData();
                smsNotEnableConditionGw.setPhoneNumber(receivedData.getMsisdn());
                smsNotEnableConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                smsNotEnableConditionGw.setIs_Status(2);
                smsNotEnableConditionGw.setPayloadMQ(messageMq);
                smsNotEnableConditionGw.setCreated_Date(createdDate);
                smsNotEnableConditionGw.setRemark("OrderType "+orderTypeData.getOrderType_Name()+" ถูกปิด");
                smsGatewayService.createConditionalMessage(smsNotEnableConditionGw);
                // System.out.println("smsNotEnableConditionGw remark: " + smsNotEnableConditionGw.getRemark() );
            }
        
        }
    }

    // private void processSuspendedOrderType(String messageMq) {
    //     // Process for new_order_type 
    //     // System.out.println("Processing suspend_order: " + order.getOrder_type());
    // }
}

