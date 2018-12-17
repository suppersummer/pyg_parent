package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import entity.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {
//添加商品到购物车        alert("skuid"+$scope.sku.id+"数量  "+$scope.num)
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Reference
    private CartService cartService;

    /** 查询购物车列表
     * 获取cookie！！！中购物车列表的信息(转换格式)
     * @return 查找到的购物车列表
     */
    @RequestMapping("findcartlist")
    public List<Cart> findcartList(){
//-------------------不合并-------------------------------------------------------------------------------------------------------------
//        //判断是否登陆-------未登录时获取不到name，会报空指针异常
//        String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
//        if ("anonymousUser".equals (name)){//没登陆 cookie
//            //1.获取cookie中购物车列表的信息(转换格式)
//            String cartList_string = CookieUtil.getCookieValue (request, "cartList", "utf-8");
//            if (StringUtils.isEmpty (cartList_string)){
//                cartList_string="[]"; }
//            List<Cart> cartList_cookie = JSON.parseArray (cartList_string, Cart.class);
//            return  cartList_cookie;
//        }else{//查询redis中购物车列表
//            //在服务层中查询
//            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
//            return cartList_redis;
//        }
//-----------------------合并--------------------------------------------------------------------------------------------------------
//1.先查询出cookie中购物车列表（判断为null返回一个空）
        String cartList_string = CookieUtil.getCookieValue (request, "cartList", "utf-8");
            if (StringUtils.isEmpty (cartList_string)){
                cartList_string="[]"; }
        List<Cart> cartList_cookie = JSON.parseArray (cartList_string, Cart.class);
//2.判断是否登陆
        String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        if ("anonymousUser".equals (name)){//没登陆 ，直接返回cookie中购物车列表
//  未登录 返回cookie的查询结果
            return cartList_cookie;}
        else{
//   登陆  a.调用service查询redis中数据
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
//         b.调用service方法，合并购物车
            cartList_redis = cartService.mergeCartList(cartList_cookie,cartList_redis);
//         c.将调用服务层合并结果存到redis中，清空cookie
            cartService.saveCartListToRedis(name,cartList_redis);
            CookieUtil.deleteCookie (request,response,"cartList");
//         d.返回redis结果
            return cartList_redis;
        }




    }

    /**
     * 添加商品到购物车列表
     * @param itemId
     * @param num 数量
     * @return Result
     */
    @RequestMapping("addGoodstoCartList")
    //允许被跨越的注解
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodstoCartList(Long itemId,Integer num){
        //1.判断是否登陆
        //2.根据itemId查寻到商品信息（item对象）
//List<Cart>
        //3.没登陆状态，在cookie中获取购物车列表（字符串类型）-->转换
        //4如果购物车列表为空，新建一个购物车列表，将item的信息存到orderItem里，再将购物车存到购物车列表里
        //5.如果购物车列表不为空，将item的信息存到orderItem里，再将购物车存到购物车列表里
        //6.将添加后的购物车列表写回到cookie中。

        //3.登陆状态，在redis中获取购物车列表
        //4如果购物车列表为空，新建一个购物车列表，将item的信息存到orderItem里，再将购物车存到购物车列表里
        //5.如果购物车列表不为空，将item的信息存到orderItem里，再将购物车存到购物车列表里
        //6.将添加后的购物车列表写回到redis中。
//-------以上思路，根本没分清哪些在控制端完成，哪些在服务端完成-----------------------------------------------------------------------------------------------------------------
        //没登陆
        try {

            //判断是否登陆-------未登录时获取不到name，会报空指针异常
            String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
            System.out.println ("当前登陆用户"+name);
            if ("anonymousUser".equals (name)){//没登陆 cookie
            //在cookie中查找cartList的方法  findcartList这个方法中会判断有没有登陆
            List<Cart> cartList = findcartList ();

            //2.调用服务层，将商品添加到购物车列表中
            cartList= cartService.addGoodsToCartList(cartList,itemId,num);
            String cartList_cookie = JSON.toJSONString (cartList);
            //3.添加后的购物车列表写回到客户端中
            CookieUtil.setCookie (request,response,"cartList",cartList_cookie,3600*24*7,"utf-8");
           }
            else{//登陆了
                //查找到购物车列表（redis中的）
                List<Cart> cartList_redis = findcartList ();
                //调用将商品添加到商家购物车中
                cartList_redis= cartService.addGoodsToCartList(cartList_redis,itemId,num);
                //将商家购物车添加到redis中的购物车列表中 ---服务端
                cartService.saveCartListToRedis(name,cartList_redis);
            }
            return new Result (true,"添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (true,"添加购物车失败");
        }



    }
}
