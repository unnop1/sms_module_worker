package com.nt.sms_module_worker.entity;


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
@Table (name = "FIXED_PHONENUMBER", schema="#{SchemaConfiguration.schema}")
public class FixedPhoneNumberEntity {
        
        @Id
        @Column(name = "ID")
        private Long ID;

        @Column(name = "PHONE_NUMBER", unique = false,nullable = true)
        private String phoneNumber = null;
}
