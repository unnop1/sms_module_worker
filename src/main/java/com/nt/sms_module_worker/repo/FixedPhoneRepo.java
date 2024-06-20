package com.nt.sms_module_worker.repo;


import com.nt.sms_module_worker.entity.FixedPhoneNumberEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedPhoneRepo extends JpaRepository<FixedPhoneNumberEntity,Long> {

}
