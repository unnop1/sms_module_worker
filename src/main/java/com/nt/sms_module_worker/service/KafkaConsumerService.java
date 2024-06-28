package com.nt.sms_module_worker.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nt.sms_module_worker.model.dto.distribute.DataSmsMessage;
import com.nt.sms_module_worker.model.dto.distribute.ReceivedData;
import com.nt.sms_module_worker.model.dto.distribute.SendSmsGatewayData;
import com.nt.sms_module_worker.util.DateTime;
import com.nt.sms_module_worker.util.MapString;
import com.nt.sms_module_worker.entity.ConfigConditionsEntity;
import com.nt.sms_module_worker.entity.FixedPhoneNumberEntity;
import com.nt.sms_module_worker.entity.OrderTypeEntity;
import com.nt.sms_module_worker.entity.SmsGatewayEntity;
import com.nt.sms_module_worker.log.LogFile;
import com.nt.sms_module_worker.model.dao.pdpa.consent.ConsentResp;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
    
// Method 
@EnableKafka
@Component
public class KafkaConsumerService {

    @Value("${smsgateway.is_skip_send}")
    private boolean isSkipSendSms=false;

    @Value("${spring.rabbitmq.exchange-name}")
    private String exchangeSmsGateway;

    private Integer MaxRetrySendSmsCount = 3;  

    @Autowired
    private PDPAService pdpaService;

    @Autowired
    private SmsConditionService smsConditionService;

    @Autowired
    private OrderTypeService orderTypeService;

    @Autowired
    private SmsGatewayService smsGatewayService;

    @Autowired
    private FixedPhoneService fixedPhoneService;
    
    @KafkaListener(
        autoStartup="true",
        // PRODUCTION
        // topics = {"NEW","SUSPEND","PACKAGEEXPIRE","RECONNECT","CHANGEPACKAGE","ADDPACKAGE","DELETEPACKAGE","TOPUPRECHARGE"}, 

        // DEV
        topics = {"ADDPACKAGE","BULKPREPROVISIONING","CHANGEMSISDN","CHANGEOWNER","CHANGEPACKAGE","CONTRACTMANAGEMENT","CREDITLIMIT","DELETEPACKAGE","EXTENDEXPIRATIONDATE","NEW","NOTIFICATIONPACKAGESTATUSCHANGE","PACKAGEEXPIRE","RECONNECT","SUSPEND","SWAPPOSTPAIDTOPREPAID","SWAPPREPAIDTOPOSTPAID","TERMINATE","TOPUPRECHARGE","VARIETYSERVICE"},
        groupId = "sms_module.worker"
    )
    public void listening(@Payload String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws Exception {
        // System.out.println("Received message from topic " + topic);

        try {
            
            // Process the message based on the queue name
            // System.out.println("isSkipPDPA:"+ pdpaService.getIsSkipPDPA());
            processOrderType(message);
        }catch (SQLException sqle) {
            Timestamp receiveDate = DateTime.getTimeStampNow();
            Clob messageMqClob = new javax.sql.rowset.serial.SerialClob(message.toCharArray());
            SmsGatewayEntity smsGwError = new SmsGatewayEntity();
            smsGwError.setIs_Status(2);
            smsGwError.setPayloadMQ(messageMqClob);
            smsGwError.setReceive_Date(receiveDate);
            smsGwError.setRemark("sql error : "+sqle.getMessage());
            smsGwError.setCreated_Date(receiveDate);
            smsGatewayService.createConditionalMessage(smsGwError);
            sqle.printStackTrace();
        }catch (Exception e) {
            Timestamp receiveDate = DateTime.getTimeStampNow();
            Clob messageMqClob = new javax.sql.rowset.serial.SerialClob(message.toCharArray());
            SmsGatewayEntity smsGwError = new SmsGatewayEntity();
            smsGwError.setIs_Status(2);
            smsGwError.setPayloadMQ(messageMqClob);
            smsGwError.setReceive_Date(receiveDate);
            smsGwError.setRemark("error : "+e.getMessage());
            smsGwError.setCreated_Date(receiveDate);
            smsGatewayService.createConditionalMessage(smsGwError);
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
        Clob messageMqClob = new javax.sql.rowset.serial.SerialClob(messageMq.toCharArray());

        OrderTypeEntity orderTypeData = orderTypeService.getOrderType(receivedData.getOrderType().toUpperCase());
        // System.out.println("orderTypeData: " + orderTypeData.getOrderTypeName() );
        if (orderTypeData == null){
            Timestamp createdDate = DateTime.getTimeStampNow();        
            SmsGatewayEntity smsMisMatchConditionGw = new SmsGatewayEntity();
            smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
            smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
            smsMisMatchConditionGw.setIs_Status(2);
            smsMisMatchConditionGw.setPayloadMQ(messageMqClob);
            smsMisMatchConditionGw.setReceive_Date(receiveDate);
            smsMisMatchConditionGw.setTransaction_id(receivedData.getOrderID());
            smsMisMatchConditionGw.setRemark("ไม่พบ OrderType "+receivedData.getOrderType());
            smsMisMatchConditionGw.setCreated_Date(createdDate);
            smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
        }else{
            Integer isEnableOrderType = orderTypeData.getIsEnable();
            // System.out.println("isEnableOrderType: " + isEnableOrderType );
            if (isEnableOrderType.equals(1)){
                // System.out.println("smsEnableConditionGw remark" );
                // String querySmsCondition = smsConditionService.getQueryOrderTypeSmsCondition(receivedData);
                List<ConfigConditionsEntity> smsConditions = smsConditionService.getListSmsCondition(receivedData.getOrderType());
                // System.out.println("smsConditions size: " + smsConditions.size() );
                
                if (smsConditions.size() > 0) {    
                    // send sms
                    Boolean isMatchCondition = false;
                    Boolean isCheckedPDPA = false;
                    ConsentResp consentPDPA = null;
                    List<String> phoneNumberSendSms = new ArrayList<String>(); 
                    List<FixedPhoneNumberEntity> fixedPhoneNumbers = fixedPhoneService.GetAllFixPhoneNumber();    
                    String phoneNumber = String.format("66%s", MapString.removeLeadingZero(receivedData.getMsisdn()));

                    if (fixedPhoneNumbers.size()>0){
                        for (FixedPhoneNumberEntity fixedPhoneNumber : fixedPhoneNumbers){
                            phoneNumberSendSms.add(fixedPhoneNumber.getPhoneNumber());
                        }
                    }else{
                        phoneNumberSendSms.add(phoneNumber);
                    }
                    for (ConfigConditionsEntity condition : smsConditions) {
                        JSONObject jsonData = new JSONObject(messageMq);
                        if(!pdpaService.mustInRangeTime(condition)){
                                continue;
                        }

                        if (smsConditionService.checkSendSms(condition, jsonData)){
                        // if(true){
                            // Check PDPA
                            isMatchCondition = true;
                            String conditionMessage = condition.getMessage();
                            String smsMessage = MapString.mapPatternToSmsMessage(conditionMessage, jsonData);
                            SmsGatewayEntity smsMatchConditionGw = new SmsGatewayEntity();
                            Timestamp createdDate = DateTime.getTimeStampNow();
                            String systemTransRef = receivedData.getOrderID();
                            
                            if (!isCheckedPDPA){
                                Boolean mustCheckPDPA = pdpaService.mustCheckPDPA(condition);
                                isCheckedPDPA = true;
                                if (mustCheckPDPA){
                                    consentPDPA = pdpaService.getPDPAConsent(receivedData.getMsisdn());
                                    LogFile.logMessage("KafkaConsumerService", "pdpa_consent", receivedData.getMsisdn(),consentPDPA);
                                }
                                // LogFile.logMessageTest("KafkaConsumerService", "pdpa_consent_test", "isCheckedPDPA:"+isCheckedPDPA);
                            }
                            
                            if(consentPDPA != null){
                                if(!consentPDPA.getHaveConsent()){
                                    ObjectMapper pdpaMapper = new ObjectMapper();
                                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Optional: for pretty print
                                    String pdpaJsonStr = pdpaMapper.writeValueAsString(consentPDPA);
                                    createdDate = DateTime.getTimeStampNow(); 
                                    SmsGatewayEntity smsNotEnableConditionGw = new SmsGatewayEntity();
                                    smsNotEnableConditionGw.setPhoneNumber(receivedData.getMsisdn());
                                    smsNotEnableConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                                    smsNotEnableConditionGw.setIs_Status(4);
                                    smsNotEnableConditionGw.setRefID(condition.getRefID());
                                    smsNotEnableConditionGw.setTransaction_id(receivedData.getOrderID());
                                    smsNotEnableConditionGw.setPayloadMQ(messageMqClob);
                                    smsNotEnableConditionGw.setReceive_Date(receiveDate);
                                    smsNotEnableConditionGw.setCreated_Date(createdDate);
                                    smsNotEnableConditionGw.setConsentPDPA(pdpaJsonStr);
                                    smsNotEnableConditionGw.setRemark("ไม่ยอมรับ pdpa กับ orderType : "+orderTypeData.getOrderTypeName());
                                    smsGatewayService.createConditionalMessage(smsNotEnableConditionGw);
                                    return;
                                }
                            }
                            smsMatchConditionGw.setSMSMessage(smsMessage);
                            // System.out.println("condition.getConditionsID: " + condition.getConditionsID() );
                            smsMatchConditionGw.setConfig_conditions_ID(condition.getConditionsID());
                            smsMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                            smsMatchConditionGw.setIs_Status(0);
                            smsMatchConditionGw.setRefID(condition.getRefID());
                            smsMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                            smsMatchConditionGw.setOrder_type_mainID(orderTypeData.getMainID());
                            smsMatchConditionGw.setPayloadMQ(messageMqClob);
                            smsMatchConditionGw.setRemark("sending sms");
                            smsMatchConditionGw.setReceive_Date(receiveDate);
                            smsMatchConditionGw.setTransaction_id(receivedData.getOrderID());
                            smsMatchConditionGw.setCreated_Date(createdDate);
                            
                            // System.out.println("smsMessage: " + smsMessage + " to phone " + receivedData.getMsisdn() );
                            
                            
                            // Message to send sms message
                            SendSmsGatewayData sendSmsData = new SendSmsGatewayData();
                            List<DataSmsMessage> smsMessages = new ArrayList<>();
                            for (int i = 0; i < phoneNumberSendSms.size();i++){
                                DataSmsMessage smsData = new DataSmsMessage();
                                smsData.setMessage(smsMessage);
                                smsData.setSystemTransRef(String.format("RED-%s-%s", systemTransRef, DateTime.getTimeStampNow().toInstant().toEpochMilli()));
                                smsData.setTarget(phoneNumberSendSms.get(i));
                                smsData.setSource("my");
                                smsData.setRequestDate(DateTime.getRequestDateUtcNow());
                                smsMessages.add(smsData);
                            }
                            sendSmsData.setBulkRef("BulkTest-e9bfae24-82c5-11ee-b962-0242ac120002");
                            sendSmsData.setMessages(smsMessages);
                            objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
                            String payloadGwStr = objectMapper.writeValueAsString(sendSmsData);
                            String consentPDPALog = objectMapper.writeValueAsString(consentPDPA);
                            smsMatchConditionGw.setConsentPDPA(consentPDPALog);
                            Clob payloadGwClob = new javax.sql.rowset.serial.SerialClob(payloadGwStr.toCharArray());
                            smsMatchConditionGw.setPayloadGW(payloadGwClob);
                            
                            SmsGatewayEntity smsMatchConditionLogGw = smsGatewayService.createConditionalMessage(smsMatchConditionGw);
                            

                            if(!isSkipSendSms){
                                for (int sendSmsCount = 1; sendSmsCount <= MaxRetrySendSmsCount ; sendSmsCount++) {
                                    try{
                                        smsConditionService.publish(exchangeSmsGateway,"" , sendSmsData);
                                        Timestamp sendDate = DateTime.getTimeStampNow(); 
                                        SmsGatewayEntity updateInfo = new SmsGatewayEntity();
                                        updateInfo.setIs_Status(1);
                                        updateInfo.setRemark("Send sms to gateway successfully");
                                        updateInfo.setSend_Date(sendDate);
                                        // System.out.println("smsMatchConditionGw.getGID: " + smsMatchConditionGw.getGID() );
                                        smsGatewayService.updateConditionalMessageById(smsMatchConditionLogGw.getGID()+1, updateInfo);
                                        break;
                                    }catch (Exception e){
                                        if (sendSmsCount >= MaxRetrySendSmsCount){
                                            SmsGatewayEntity updateInfo = new SmsGatewayEntity();
                                            updateInfo.setIs_Status(3);
                                            smsGatewayService.updateConditionalMessageById(smsMatchConditionLogGw.getGID()+1, updateInfo);
                                            return;
                                        }
                                        SmsGatewayEntity updateInfo = new SmsGatewayEntity();
                                        updateInfo.setIs_Status(5);
                                        updateInfo.setRemark("Error: " + e.getMessage());
                                        smsGatewayService.updateConditionalMessageById(smsMatchConditionLogGw.getGID()+1, updateInfo);
                                        System.out.println("Error round "+sendSmsCount+" publishing: " + e.getMessage());
                                        return;
                                    }
                                }
                            }else{
                                Timestamp sendDate = DateTime.getTimeStampNow(); 
                                SmsGatewayEntity updateInfo = new SmsGatewayEntity();
                                updateInfo.setIs_Status(1);
                                updateInfo.setRemark("Skip Send sms to gateway");
                                updateInfo.setSend_Date(sendDate);
                                smsGatewayService.updateConditionalMessageById(smsMatchConditionLogGw.getGID()+1, updateInfo);
                            }
                            
                        }
                    }

                    if(!isMatchCondition){
                        Timestamp createdDate = DateTime.getTimeStampNow(); 
                        SmsGatewayEntity smsMisMatchConditionGw = new SmsGatewayEntity();
                        smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                        smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                        smsMisMatchConditionGw.setOrder_type_mainID(orderTypeData.getMainID());
                        smsMisMatchConditionGw.setIs_Status(2);
                        smsMisMatchConditionGw.setRemark("not match any conditions");
                        smsMisMatchConditionGw.setTransaction_id(receivedData.getOrderID());
                        smsMisMatchConditionGw.setPayloadMQ(messageMqClob);
                        smsMisMatchConditionGw.setReceive_Date(receiveDate);
                        smsMisMatchConditionGw.setCreated_Date(createdDate);
                        smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
                    }
                }else{
                    Timestamp createdDate = DateTime.getTimeStampNow(); 
                    SmsGatewayEntity smsMisMatchConditionGw = new SmsGatewayEntity();
                    smsMisMatchConditionGw.setPhoneNumber(receivedData.getMsisdn());
                    smsMisMatchConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                    smsMisMatchConditionGw.setOrder_type_mainID(orderTypeData.getMainID());
                    smsMisMatchConditionGw.setIs_Status(2);
                    smsMisMatchConditionGw.setRemark("not found conditions");
                    smsMisMatchConditionGw.setTransaction_id(receivedData.getOrderID());
                    smsMisMatchConditionGw.setPayloadMQ(messageMqClob);
                    smsMisMatchConditionGw.setReceive_Date(receiveDate);
                    smsMisMatchConditionGw.setCreated_Date(createdDate);
                    smsGatewayService.createConditionalMessage(smsMisMatchConditionGw);
                }
            }else{
                Timestamp createdDate = DateTime.getTimeStampNow(); 
                SmsGatewayEntity smsNotEnableConditionGw = new SmsGatewayEntity();
                smsNotEnableConditionGw.setPhoneNumber(receivedData.getMsisdn());
                smsNotEnableConditionGw.setOrderType(receivedData.getOrderType().toUpperCase());
                smsNotEnableConditionGw.setIs_Status(2);
                smsNotEnableConditionGw.setTransaction_id(receivedData.getOrderID());
                smsNotEnableConditionGw.setPayloadMQ(messageMqClob);
                smsNotEnableConditionGw.setReceive_Date(receiveDate);
                smsNotEnableConditionGw.setCreated_Date(createdDate);
                smsNotEnableConditionGw.setRemark("OrderType "+orderTypeData.getOrderTypeName()+" ถูกปิด");
                smsGatewayService.createConditionalMessage(smsNotEnableConditionGw);
                // System.out.println("smsNotEnableConditionGw remark: " + smsNotEnableConditionGw.getRemark() );
            }
        
        }
    }
}

