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

        // String orderTypeName = receivedData.getOrder_type();
        // System.out.println("Processing new_order_type: " + orderTypeName );

        String queryOrderType = orderTypeService.getQueryOrderTypeAvailable(receivedData); 
        OrderTypeData orderTypeData = orderTypeService.getOrderType(queryOrderType);
        if (orderTypeData.getOrderTypeName() == null){
            Instant instant = Instant.now();
            Timestamp createdDate = Timestamp.from(instant);
            SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
            smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
            smsMisMatchConditionGw.setChanel(receivedData.getPublishChannel());
            smsMisMatchConditionGw.setOfferingId(receivedData.getEventData().getEventItem().getOffer().getOfferingType());
            smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
            smsMisMatchConditionGw.setServiceType(receivedData.getEventData().getEventItem().getOffer().getServiceType());
            smsMisMatchConditionGw.setIsStatus(2);
            smsMisMatchConditionGw.setPayloadMQ(messageMq);
            smsMisMatchConditionGw.setRemark("ไม่พบ OrderType "+receivedData.getOrderType());
            smsMisMatchConditionGw.setCreatedDate(createdDate);
            smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
        }else{
            // System.out.println("orderTypeData: " + orderTypeData.getOrderTypeName() );
            boolean isEnableOrderType = orderTypeData.getIsEnable();
            // System.out.println("isEnableOrderType: " + isEnableOrderType );
            if (isEnableOrderType){
                // System.out.println("smsEnableConditionGw remark" );
                String querySmsCondition = smsConditionService.getQueryOrderTypeSmsCondition(receivedData);
                List<SmsConditionData> smsConditions = smsConditionService.getListSmsCondition(querySmsCondition);
                
                if (smsConditions.size() > 0) {    
                    // send sms
                    // System.out.println("smsConditions size: " + smsConditions.size() );
                    for (SmsConditionData condition : smsConditions) {
                        JSONObject jsonData = new JSONObject(messageMq);
                        if (smsConditionService.checkSendSms(condition, jsonData)){
                            // System.out.println("condition.getSMSID(): " + condition.getSMSID());
                            String smsMessage = condition.getMessage();
                            SmsGatewayData smsMatchConditionGw = new SmsGatewayData();
                            Instant instant = Instant.now();
                            Timestamp createdDate = Timestamp.from(instant);
                            smsMatchConditionGw.setSMSMessage(smsMessage);
                            // System.out.println("condition.getConditionsID: " + condition.getConditionsID() );
                            smsMatchConditionGw.setSms_condition_SMSID(condition.getConditionsID());
                            smsMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                            smsMatchConditionGw.setChanel(receivedData.getPublishChannel());
                            smsMatchConditionGw.setIsStatus(0);
                            smsMatchConditionGw.setOfferingId(receivedData.getEventData().getEventItem().getOffer().getOfferingType());
                            smsMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                            smsMatchConditionGw.setServiceType(receivedData.getEventData().getEventItem().getOffer().getServiceType());
                            smsMatchConditionGw.setOrder_type_MainID(orderTypeData.getMainID());
                            smsMatchConditionGw.setPayloadMQ(messageMq);
                            smsMatchConditionGw.setCreatedDate(createdDate);
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
                        }else{
                            Instant instant = Instant.now();
                            Timestamp createdDate = Timestamp.from(instant);
                            SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
                            smsMisMatchConditionGw.setSms_condition_SMSID(condition.getConditionsID());
                            smsMisMatchConditionGw.setSMSMessage(condition.getMessage());
                            smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                            smsMisMatchConditionGw.setFrequency(receivedData.getEventData().getEventItem().getOffer().getFrequency());
                            smsMisMatchConditionGw.setChanel(receivedData.getPublishChannel());
                            smsMisMatchConditionGw.setOfferingId(receivedData.getEventData().getEventItem().getOffer().getOfferingType());
                            smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                            smsMisMatchConditionGw.setServiceType(receivedData.getEventData().getEventItem().getOffer().getServiceType());
                            smsMisMatchConditionGw.setOrder_type_MainID(orderTypeData.getMainID());
                            smsMisMatchConditionGw.setIsStatus(2);
                            smsMisMatchConditionGw.setPayloadMQ(messageMq);
                            smsMisMatchConditionGw.setCreatedDate(createdDate);
                            smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
                        }
                    }

                }else{
                    Instant instant = Instant.now();
                    Timestamp createdDate = Timestamp.from(instant);
                    SmsGatewayData smsMisMatchConditionGw = new SmsGatewayData();
                    smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                    smsMisMatchConditionGw.setChanel(receivedData.getPublishChannel());
                    smsMisMatchConditionGw.setOfferingId(receivedData.getEventData().getEventItem().getOffer().getOfferingType());
                    smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                    smsMisMatchConditionGw.setServiceType(receivedData.getEventData().getEventItem().getOffer().getServiceType());
                    smsMisMatchConditionGw.setOrder_type_MainID(orderTypeData.getMainID());
                    smsMisMatchConditionGw.setIsStatus(2);
                    smsMisMatchConditionGw.setPayloadMQ(messageMq);
                    smsMisMatchConditionGw.setCreatedDate(createdDate);
                    smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
                }
            }else{
                Instant instant = Instant.now();
                Timestamp createdDate = Timestamp.from(instant);
                SmsGatewayData smsNotEnableConditionGw = new SmsGatewayData();
                smsNotEnableConditionGw.setPhoneNumber(receivedData.getMsisdn());
                smsNotEnableConditionGw.setChanel(receivedData.getPublishChannel());
                smsNotEnableConditionGw.setOfferingId(receivedData.getEventData().getEventItem().getOffer().getOfferingType());
                smsNotEnableConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                smsNotEnableConditionGw.setServiceType(receivedData.getEventData().getEventItem().getOffer().getServiceType());
                smsNotEnableConditionGw.setIsStatus(2);
                smsNotEnableConditionGw.setPayloadMQ(messageMq);
                smsNotEnableConditionGw.setCreatedDate(createdDate);
                smsNotEnableConditionGw.setRemark("OrderType "+orderTypeData.getOrderTypeName()+" ถูกปิด");
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

