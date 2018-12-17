package com.pyg.pay.service;

import java.util.Map;

public interface WeixinPayService {
    /**
     * 生成预支付的url的方法
     * @param out_trade_no：父订单号
     * @param total_fee：订单的支付金额
     * @return 订单号，总金额，预支付url
     * @throws Exception
     */
    public Map createNative(String out_trade_no,String total_fee)throws Exception;

    /**
     * 根据订单号，查询订单状态
     * @param out_trade_no 订单号
     * @return 订单状态信息
     * @throws Exception
     */
    public Map queryOrderStatus(String out_trade_no)throws Exception;

    /**
     * 根据订单号关闭订单
     * @param out_trade_no 订单号
     * @return  错误码
     */
    Map<String,String> closeOrder(String out_trade_no) throws Exception;
}
