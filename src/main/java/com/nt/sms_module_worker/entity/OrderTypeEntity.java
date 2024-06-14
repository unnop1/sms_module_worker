package com.nt.sms_module_worker.entity;


import java.sql.Timestamp;

import jakarta.persistence.*;
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
@Table (name = "ORDER_TYPE", schema="${replace_schema}")
public class OrderTypeEntity {
        
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_type_seq")
        @SequenceGenerator(name = "order_type_seq", allocationSize = 1)
        @Column(name = "TYPEID")
        private Long TYPEID = null;

        @Column(name = "MAINID", unique = false,nullable = true)
        private Long MainID = null;

        @Column(name = "ODERTYPE_NAME", unique = false,nullable = true)
        private String OrderTypeName = null;

        @Column(name = "IS_ENABLE", unique = false,nullable = true)
        private Integer IsEnable = 1;

        @Column(name = "IS_DELETE", unique = false,nullable = true)
        private Integer IsDelete = 0;

        @Column(name = "CREATED_DATE", unique = false,nullable = true)
        private Timestamp CreatedDate = null;

        @Column(name = "CREATED_BY", unique = false,nullable = true)
        private String CreatedBy = null;

        @Column(name = "UPDATED_DATE", unique = false,nullable = true)
        private Timestamp UpdatedDate = null;

        @Column(name = "UPDATED_BY", unique = false,nullable = true)
        private String Updated_By = null;
}
