package com.nt.sms_module_worker.entity;


import java.sql.Clob;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table (name = "sms_gateway", schema="reddbsms")
public class SmsGatewayEntity {
        
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sms_gateway_seq")
        @SequenceGenerator(name = "sms_gateway_seq", allocationSize = 1)
        @Column(name = "GID")
        private Long GID = null;

        @Column(name = "config_conditions_ID", unique = false,nullable = true)
        private Long config_conditions_ID = null;
        
        @Column(name = "SMSMessage", unique = false,nullable = true)
        private String SMSMessage = null;

        @Column(name = "Message_raw", unique = false,nullable = true)
        private String Message_raw = null;

        @Column(name = "order_type_mainID", unique = false,nullable = true)
        private Long order_type_mainID = null;

        @Column(name = "OrderType", unique = false,nullable = true)
        private String OrderType = null;

        @Column(name = "PhoneNumber", unique = false,nullable = true)
        private String PhoneNumber = null;

        @Column(name = "payloadGW", unique = false,nullable = true)
        private Clob payloadGW = null;

        @Column(name = "PayloadMQ", unique = false,nullable = true)
        private Clob PayloadMQ = null;

        @Column(name = "Is_Status", unique = false,nullable = true)
        private Integer Is_Status = 0;

        @Column(name = "Remark", unique = false,nullable = true)
        private String Remark = null;

        @Column(name = "Created_Date", unique = false,nullable = true)
        private Timestamp Created_Date = null;

        @Column(name = "Receive_Date", unique = false,nullable = true)
        private Timestamp Receive_Date = null;

        @Column(name = "Send_Date", unique = false,nullable = true)
        private Timestamp Send_Date = null;

        @Column(name = "RefID", unique = false,nullable = true)
        private String RefID = null;

        @Column(name = "DATE_START", unique = false,nullable = true)
        private Timestamp date_Start = null;

        @Column(name = "DATE_END", unique = false,nullable = true)
        private Timestamp date_End = null;

        @Column(name = "transaction_id", unique = false,nullable = true)
        private String transaction_id = null;
}
