package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.order.service.OrderService;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbPayLog;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private WeixinPayService weixinPayService;
   @Reference
    private OrderService orderService;
    @Autowired
    private IdWorker idWorker;

    @RequestMapping("/createNative")
    public Map createNative(){
        try {//调用服务层返回父  订单号，总金额，预支付url
        //String out_trade_no, String total_fee 是在redis中获取 pay_log表
        //假设有父订单 订单号
//        String out_trade_no=idWorker.nextId ()+"";
      //总金额 单位是分
//        String total_fee="1";
            //从redis中获取payLog(在服务层中获取redis)
            String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
           TbPayLog payLog= orderService.findPayLogfromRedis(name);
            String out_trade_no = payLog.getOutTradeNo ();
            Long total_fee = payLog.getTotalFee ();


            Map map = weixinPayService.createNative (out_trade_no, total_fee+"");
            return map;
        } catch (Exception e) {
            e.printStackTrace ();
            return new HashMap ();
        }
    }

    /**
     * 查看订单交易状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("queryOrderStatus")
    public Result queryOrderStatus(String out_trade_no){
        //调用服务层，获取交易状态
        Result result=null;
        try {
            int count=0;
            while (true){
                Map map = weixinPayService.queryOrderStatus (out_trade_no);

                if (map==null){
                    result=new Result (false,"交易失败");//map抛异常返回null
                    break;
                }
                if ("SUCCESS".equals (map.get ("return_code"))){
                    //交易状态是已支付
                    if("SUCCESS".equals(map.get("trade_state"))){
                        //交易成功，修改父订单和子订单状态,父订单记录交易流水号和支付时间
                        orderService.updateOrderStatus(out_trade_no,map.get ("transaction_id"));


                        result=new Result (true,"支付成功");
                        break;
                    }
                    if("CLOSED".equals(map.get("trade_state"))){
                        //交易关闭，修改父订单和子订单状态
                        result=new Result (true,"支付关闭");
                        break;
                    }
                    if("REVOKED".equals(map.get("trade_state"))){
                        //交易撤销，修改父订单和子订单状态
                        result=new Result (true,"支付撤销");
                        break;
                    }
                }
                Thread.sleep (3000);//间隔3秒
                count++;
                if (count>=100){
                    System.out.println ("超时");
                    result=new Result (false,"pay_time_out");
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace ();
        }
        return result;
    }
}
