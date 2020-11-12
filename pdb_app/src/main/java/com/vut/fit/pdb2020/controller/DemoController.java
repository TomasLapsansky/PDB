package com.vut.fit.pdb2020.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {

    @GetMapping("/demo/user")
    public String user() {
        return "user";
    }

    @GetMapping("/demo/page")
    public String page() {
        return "page";
    }

}
