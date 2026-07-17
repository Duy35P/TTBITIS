package com.bitis.luckydraw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminAuthController {

    @GetMapping("/quanly/login")
    public String login() {
        return "quanly/login";
    }
}
