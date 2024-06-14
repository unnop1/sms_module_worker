package com.nt.sms_module_worker.entity;


import java.sql.Clob;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
@Table (name = "CONFIG_CONDITIONS", schema="${replace_schema}")//"reddbsms"
public class ConfigConditionsEntity {
        
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "config_conditions_seq")
        @SequenceGenerator(name = "config_conditions_seq", allocationSize = 1)
        @Column(name = "CONDITIONS_ID")
        private Long conditionsID;

        @Column(name = "ORDER_TYPE_MAINID", unique = false,nullable = true)
        private Long order_type_MainID = null;
        
        @Column(name = "ORDERTYPE", unique = false,nullable = true)
        private String orderType = null;

        @Column(name = "REFID", unique = false,nullable = true)
        private String refID = null;

        @Column(name = "DATE_START", unique = false,nullable = true)
        private Timestamp date_Start = null;

        @Column(name = "DATE_END", unique = false,nullable = true)
        private Timestamp date_End = null;

        @Column(name = "MESSAGE", unique = false,nullable = true)
        private String message = null;

        @JsonBackReference
        @Column(name = "CONDITIONS_OR", unique = false,nullable = true)
        private Clob conditions_or = null;

        @JsonBackReference
        @Column(name = "CONDITIONS_AND", unique = false,nullable = true)
        private Clob conditions_and = null;

        @Column(name = "CREATED_DATE", unique = false,nullable = true)
        private Timestamp created_Date = null;

        @Column(name = "CREATED_BY", unique = false,nullable = true)
        private String created_By = null;

        @Column(name = "UPDATED_DATE", unique = false,nullable = true)
        private Timestamp updated_Date = null;

        @Column(name = "UPDATED_BY", unique = false,nullable = true)
        private String updated_By = null;

        @Column(name = "IS_DELETE", unique = false,nullable = true)
        private Integer is_delete = 0;

        @Column(name = "IS_ENABLE", unique = false,nullable = true)
        private Integer is_enable = 1;

        @Column(name = "IS_DELETE_BY", unique = false,nullable = true)
        private String is_Delete_By = null;

        @Column(name = "IS_DELETE_DATE", unique = false,nullable = true)
        private Timestamp is_Delete_Date = null;

        @JsonBackReference
        @Column(name = "CONDITIONS_OR_SELECT", unique = false,nullable = true)
        private Clob conditions_or_select = null;

        @JsonBackReference
        @Column(name = "CONDITIONS_AND_SELECT", unique = false,nullable = true)
        private Clob conditions_and_select = null;

        @Column(name = "IS_PDPA", unique = false,nullable = true)
        private Integer is_pdpa=null;

        @Column(name = "IS_PERIOD_TIME", unique = false,nullable = true)
        private Integer is_period_time=null;

        @Column(name = "TIME_START", unique = false,nullable = true)
        private String time_Start=null;

        @Column(name = "TIME_END", unique = false,nullable = true)
        private String time_End=null;

        
}
