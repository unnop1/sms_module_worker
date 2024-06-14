package com.nt.sms_module_worker.repo;


import com.nt.sms_module_worker.entity.FixedPhoneNumberEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FixedPhoneRepo extends JpaRepository<FixedPhoneNumberEntity,Long> {

    @Query("SELECT * FROM fixed_phonenumber")
    List<FixedPhoneNumberEntity> findAllPhoneNumbers();
}
