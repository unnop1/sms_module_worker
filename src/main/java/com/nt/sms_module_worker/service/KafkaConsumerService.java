package com.nt.sms_module_worker.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.KafkaHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.sms_module_worker.model.dto.SmsConditionData;
import com.nt.sms_module_worker.model.dto.SmsGatewayData;
import com.nt.sms_module_worker.model.dto.OrderTypeData;
import com.nt.sms_module_worker.model.dto.ReceivedData;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        System.out.println("Received message from topic " + topic);

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

        String orderTypeName = receivedData.getOrder_type();
        System.out.println("Processing new_order_type: " + orderTypeName );

        String queryOrderType = orderTypeService.getQueryOrderTypeAvailable(receivedData); 
        OrderTypeData orderTypeData = orderTypeService.getOrderType(queryOrderType);
        if (orderTypeData == null){
            Instant instant = Instant.now();
            Timestamp createdDate = Timestamp.from(instant);
            SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
            smsMisMatchConditionGw.setPhoneNumber(receivedData.getPhoneNumber());
            smsMisMatchConditionGw.setChanel(receivedData.getChannel());
            smsMisMatchConditionGw.setFrequency(receivedData.getFrequency());
            smsMisMatchConditionGw.setOfferingId(receivedData.getOfferingId());
            smsMisMatchConditionGw.setOrderType(receivedData.getOrder_type().toUpperCase());
            smsMisMatchConditionGw.setServiceType(receivedData.getServiceType());
            smsMisMatchConditionGw.setIsStatus(2);
            smsMisMatchConditionGw.setPayloadMQ(messageMq);
            smsMisMatchConditionGw.setCreatedDate(createdDate);
            smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
        }else{
            System.out.println("orderTypeData: " + orderTypeData.getOrderTypeName() );
            String querySmsCondition = smsConditionService.getQueryOrderTypeSmsCondition(receivedData);
            List<SmsConditionData> smsConditions = smsConditionService.getListSmsCondition(querySmsCondition);
            
            if (smsConditions.size() > 0) {    
                // send sms
                for (SmsConditionData condition : smsConditions) {
                    // System.out.println("condition.getSMSID(): " + condition.getSMSID());
                    String smsMessage = condition.getMessage();
                    SmsGatewayData smsMatchConditionGw = new SmsGatewayData();
                    Instant instant = Instant.now();
                    Timestamp createdDate = Timestamp.from(instant);
                    smsMatchConditionGw.setSMSMessage(smsMessage);
                    smsMatchConditionGw.setPhoneNumber(receivedData.getPhoneNumber());
                    smsMatchConditionGw.setChanel(receivedData.getChannel());
                    smsMatchConditionGw.setFrequency(receivedData.getFrequency());
                    smsMatchConditionGw.setIsStatus(0);
                    smsMatchConditionGw.setOfferingId(receivedData.getOfferingId());
                    smsMatchConditionGw.setOrderType(receivedData.getOrder_type().toUpperCase());
                    smsMatchConditionGw.setServiceType(receivedData.getServiceType());
                    smsMatchConditionGw.setOrder_type_MainID(orderTypeData.getMainID());
                    smsMatchConditionGw.setPayloadMQ(messageMq);
                    smsMatchConditionGw.setCreatedDate(createdDate);
                    smsMatchConditionGw.setSms_condition_SMSID(condition.getSMSID());
                    smsMatchConditionGw = smsGatewayService.createConditionalMessage(smsMatchConditionGw);
                    System.out.println("smsMessage: " + smsMessage + " to phone " + receivedData.getPhoneNumber() );
                    
                    
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
                    smsGatewayService.updateConditionalMessageById(smsMatchConditionGw.getGID(), updateInfo);
                    
                }

            }else{
                Instant instant = Instant.now();
                Timestamp createdDate = Timestamp.from(instant);
                SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
                smsMisMatchConditionGw.setPhoneNumber(receivedData.getPhoneNumber());
                smsMisMatchConditionGw.setChanel(receivedData.getChannel());
                smsMisMatchConditionGw.setFrequency(receivedData.getFrequency());
                smsMisMatchConditionGw.setOfferingId(receivedData.getOfferingId());
                smsMisMatchConditionGw.setOrderType(receivedData.getOrder_type().toUpperCase());
                smsMisMatchConditionGw.setServiceType(receivedData.getServiceType());
                smsMisMatchConditionGw.setOrder_type_MainID(orderTypeData.getMainID());
                smsMisMatchConditionGw.setIsStatus(2);
                smsMisMatchConditionGw.setPayloadMQ(messageMq);
                smsMisMatchConditionGw.setCreatedDate(createdDate);
                smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
            }
        }
    }

    // private void processSuspendedOrderType(String messageMq) {
    //     // Process for new_order_type 
    //     // System.out.println("Processing suspend_order: " + order.getOrder_type());
    // }
}

