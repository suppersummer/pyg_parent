package com.pyg.cart.service;

import entity.Cart;

import java.util.List;

public interface CartService {

    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    List<Cart> findCartListFromRedis(String name);

    void saveCartListToRedis(String name, List<Cart> cartList_redis);

    List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis);
}
