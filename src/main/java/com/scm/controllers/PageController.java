package com.scm.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scm.payload.request.RegisterRequest;

import org.springframework.ui.Model;

@Controller
public class PageController {

    @GetMapping("/")
    public String index(){
        return "redirect:/home";
    }

    @RequestMapping("/home")
    public String home() {
        return "home";
    }

    @RequestMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("isLogin", false);
        return "about";
    }

    @RequestMapping("/services")
    public String servicePage() {
        return "services";
    }

    @RequestMapping("/register")
    public String register(Model model) {
        RegisterRequest userForm=new RegisterRequest();
        model.addAttribute("userForm", userForm);
        return "register";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/contact")
    public String contact() {
        return "contact";
    }

}
