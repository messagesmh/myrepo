package com.demoshow.tool.controller;

import com.demoshow.tool.bean.payload.MsgPayload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/")
    public String home (Model model) {
        MsgPayload msgPayload = new MsgPayload();
        model.addAttribute("msgpayload", msgPayload);
        return "index";
    }
}
