package com.nt.sms_module_worker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/home")
    public String hello() {
        return "Welcome to red sms module worker!";
    }
}
