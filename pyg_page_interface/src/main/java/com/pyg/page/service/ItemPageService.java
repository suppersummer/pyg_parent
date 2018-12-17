package com.pyg.page.service;

/**
 * 生成和删除静态页面的服务
 */
public interface ItemPageService {
    //根据传递的spu商品id，生成对应的详情页面
    public void genHtml(Long goodsId);
    //删除静态页面的方法
    void delHtml(Long id);
}
