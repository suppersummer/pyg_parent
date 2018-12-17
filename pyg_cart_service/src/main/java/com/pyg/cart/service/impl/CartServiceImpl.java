package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl  implements CartService{
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 添加商品到购物车列表
     * @param itemId
     * @param num 数量
     * @return  购物车列表
     */
    //4如果购物车列表为空，新建一个购物车列表，将item的信息存到orderItem里，再将购物车存到购物车列表里
    //5.如果购物车列表不为空，将item的信息存到orderItem里，再将购物车存到购物车列表里
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
      //1.根据itemid查item
        TbItem item = itemMapper.selectByPrimaryKey (itemId);
        //2.将item中的数据存到OrderItem中

        //3.在item中查询商家id，循环List<cart>  判断购物车是否存在
        String sellerId = item.getSellerId ();
        String sellerName = item.getSeller ();
        System.out.println (sellerName);
        Cart cart= searchCartFormCartList(cartList,sellerId);
        if (cart==null){//4.不存在创建购物车，新建一个该商家购物车，将orderitem添加进去
            cart = new Cart ();
            cart.setSellerId (sellerId);
            cart.setSellerName (sellerName);
            //将商品添加到cart中List<OrderItem>中
            List<TbOrderItem> orderItemList=new ArrayList<> ();
            TbOrderItem orderItem=creatOrderItem(item,num);
            orderItemList.add (orderItem);
            cart.setOrderItemList (orderItemList);
            //将新建的购物车添加到购物车列表中
            cartList.add (cart);
        }else {//存在该商家购物车
            //5存在判断该商品是否存在，不存在将orderitem添加进去(购物明细)
            TbOrderItem orderItem=searchOrderItemByItemId(cart.getOrderItemList (),itemId);
            if (orderItem==null){//不存在该商品，可以直接添加
               orderItem = creatOrderItem (item, num);
                cart.getOrderItemList ().add (orderItem);

            }else{
                //5.存在，添加数量和小计
                orderItem.setNum (orderItem.getNum ()+num);
                orderItem.setTotalFee (new BigDecimal (item.getPrice ().doubleValue ()*orderItem.getNum ()));

                //当num被减为0，则移除订单明细orderItem
                if(orderItem.getNum ()<=0){
                    cart.getOrderItemList ().remove (orderItem);
                }
                //当List<orderItem>为空，则移除购物车列表
                if (cart.getOrderItemList ().size ()<=0){
                    cartList.remove (cart);
                }
            }


            }


        return cartList;
    }

    /**
     * 登陆时，查询redis中数据
     * @return 返回购物车列表
     */
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        List cartList = (List) redisTemplate.boundHashOps ("cartList").get (name);
        if (cartList==null){
            return new ArrayList<> ();
        }
            return cartList;
    }

    /**
     * 将购物车列表存到redis中
     * @param name  登录名
     * @param cartList_redis   用户车列表
     */
    @Override
    public void saveCartListToRedis(String name, List<Cart> cartList_redis) {
        redisTemplate.boundHashOps ("cartList").put (name,cartList_redis);
    }

    /**
     * 合并购物车
     * @param cartList_cookie cookie中获取的购物车列表
     * @param cartList_redis  redis中获取的购物车列表
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis) {
//将cookie中的数据遍历成商品，循环添加到cartList_redis中，有现成的方法
//addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num)
        for (Cart cart : cartList_cookie) {
            List<TbOrderItem> orderItemList = cart.getOrderItemList ();
            for (TbOrderItem orderItem : orderItemList) {
                cartList_redis=addGoodsToCartList(cartList_redis,orderItem.getItemId (),orderItem.getNum ());
            }
        }

        return cartList_redis;
    }

    /**
     * 通过itemId 查看订单列表中 是否存在该商品
     * @param orderItemList  订单详情列表
     * @return  订单详情
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (itemId.equals (orderItem.getItemId ())){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 订单明细 的内容来自 商品信息  orderItem-->item
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem creatOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem ();
        orderItem.setGoodsId (item.getGoodsId ());
        orderItem.setNum (num);
        orderItem.setItemId (item.getId ());
        orderItem.setPicPath (item.getImage ());
        orderItem.setPrice (item.getPrice ());
        orderItem.setTotalFee (new BigDecimal (item.getPrice ().doubleValue ()*num));
        orderItem.setTitle (item.getTitle ());
        orderItem.setSellerId (item.getSellerId ());

        return  orderItem;
    }

    /**
     * 根据购物车列表中购物车的商家id，判断该商家是否存在
     * @param cartList
     * @param sellerId
     */
    private Cart searchCartFormCartList(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals (cart.getSellerId ())){
                return cart;
            }
        }
        return null;
    }
}
