package com.nt.sms_module_worker.entity;


import java.sql.Clob;
import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table (name = "config_conditions", schema="${replace_schema}")//"reddbsms"
public class ConfigConditionsEntity {
        
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "config_conditions_seq")
        @SequenceGenerator(name = "config_conditions_seq", allocationSize = 1)
        @Column(name = "conditions_ID")
        private Long conditionsID;

        @Column(name = "order_type_MainID", unique = false,nullable = true)
        private Long order_type_MainID = null;
        
        @Column(name = "orderType", unique = false,nullable = true)
        private String orderType = null;

        @Column(name = "refID", unique = false,nullable = true)
        private String refID = null;

        @Column(name = "date_Start", unique = false,nullable = true)
        private Timestamp date_Start = null;

        @Column(name = "date_End", unique = false,nullable = true)
        private Timestamp date_End = null;

        @Column(name = "message", unique = false,nullable = true)
        private String message = null;

        @Column(name = "conditions_or", unique = false,nullable = true)
        private Clob conditions_or = null;

        @Column(name = "conditions_and", unique = false,nullable = true)
        private Clob conditions_and = null;

        @Column(name = "created_Date", unique = false,nullable = true)
        private Timestamp created_Date = null;

        @Column(name = "created_By", unique = false,nullable = true)
        private String created_By = null;

        @Column(name = "updated_Date", unique = false,nullable = true)
        private Timestamp updated_Date = null;

        @Column(name = "updated_By", unique = false,nullable = true)
        private String updated_By = null;

        @Column(name = "is_delete", unique = false,nullable = true)
        private Integer is_delete = 0;

        @Column(name = "is_enable", unique = false,nullable = true)
        private Integer is_enable = 1;

        @Column(name = "is_Delete_By", unique = false,nullable = true)
        private String is_Delete_By = null;

        @Column(name = "is_Delete_Date", unique = false,nullable = true)
        private Timestamp is_Delete_Date = null;

        @Column(name = "conditions_or_select", unique = false,nullable = true)
        private Clob conditions_or_select = null;

        @Column(name = "conditions_and_select", unique = false,nullable = true)
        private Clob conditions_and_select = null;

        @Column(name = "is_pdpa", unique = false,nullable = true)
        private Integer is_pdpa=null;

        @Column(name = "is_period_time", unique = false,nullable = true)
        private Integer is_period_time=null;

        @Column(name = "time_Start", unique = false,nullable = true)
        private String time_Start=null;

        @Column(name = "time_End", unique = false,nullable = true)
        private String time_End=null;

        
}
