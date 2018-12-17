package com.pyg.cart.service;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 只负责授权
 */
//details 细节
//扫描方式不用set方法，@service用原来的
@Service
public class UserDetailsServiceImpl implements UserDetailsService {


//    public void setSellerService(SellerService sellerService) {
//        this.sellerService = sellerService;
//    }

    @Override
    //username：认证管理器在登陆页面收集的用户名，用来查询数据库的\
    //问题，如何到数据库获取数据进行比对，需要到数据库获取到password的值-就需要使用service层的findOne方法（username就是sellerId）返回对象，获取方法
    //sellerService怎么被注入过来，在配置中把他注入过来，set方法
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ArrayList< GrantedAuthority > authorities=new ArrayList<> ();//权限列表 要从数据库查询
        authorities.add (new SimpleGrantedAuthority ("ROLE_SELLER"));//权限对象
        authorities.add (new SimpleGrantedAuthority ("ROLE_USER"));//权限对象
         //   System.out.println (username+"   "+seller.getPassword ());
        return new User (username,"", authorities);
   }
}
