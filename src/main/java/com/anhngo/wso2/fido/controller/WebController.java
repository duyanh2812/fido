package com.anhngo.wso2.fido.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    /**
     * Redirect root URL to the FIDO authentication UI
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    /**
     * Serve the FIDO authentication UI
     */
    @GetMapping("/ui")
    public String ui() {
        return "redirect:/index.html";
    }
} 