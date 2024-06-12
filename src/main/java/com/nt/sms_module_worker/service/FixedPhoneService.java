package com.nt.sms_module_worker.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nt.sms_module_worker.repo.FixedPhoneRepo;


@Component
class FixedPhoneService {

    @Autowired 
    private FixedPhoneRepo fixedPhoneRepo;

    public List<String> GetAllFixPhoneNumber() throws SQLException {
        return fixedPhoneRepo.findAllPhoneNumbers();
    }

}