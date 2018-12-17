package com.pyg.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;
//思路：1.为什么要传出一个map集合，而不是String  2.在web中获取到安全认证的信息后，要传回到前端，所以需要注解,但是怎么让前端获取到这个值？这里需要注解一个方法
@RestController
public class LoginController {
    @RequestMapping("/loginName")
    public Map loginName() {
        //context上下文:环境，域对象（在一定范围内存值取值，如请求头里的） holder持有人
        String username = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        Map map=new HashMap ();
        map.put ("loginName",username);
        return map;
    }
}
