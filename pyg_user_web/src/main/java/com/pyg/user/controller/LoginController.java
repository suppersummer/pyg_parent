package com.pyg.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController
{
    @RequestMapping("getLoginUser")
    public Map getLoginUser(){
        Map result=new HashMap ();
        String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        result.put ("username",name);
        System.out.println (name);
        return result;
    }
}
