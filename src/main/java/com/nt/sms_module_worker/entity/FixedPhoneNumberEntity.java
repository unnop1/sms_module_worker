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
@Table (name = "FIXED_PHONENUMBER", schema="${replace_schema}")
public class FixedPhoneNumberEntity {
        
        @Id
        @Column(name = "ID")
        private Long ID;

        @Column(name = "PHONENUMBER", unique = false,nullable = true)
        private String phoneNumber = null;
}
