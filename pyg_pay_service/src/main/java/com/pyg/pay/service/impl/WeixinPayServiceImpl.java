package com.pyg.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pyg.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;
import java.util.HashMap;
import java.util.Map;
@Service
public class WeixinPayServiceImpl implements WeixinPayService{
//    @Value ("${appid}")
//    private String appid;
//  @Value ("${partner}")
//    private String partner;
//  @Value ("${partnerkey}")
//    private String partnerkey;
//  @Value ("${notifyurl}")
//    private String notifyurl;
    private String appid="wx8397f8696b538317";
    private String partnerkey="T6m9iK73b0kn9g5v426MKfHQH7X8rKwb";
    /**
     * 生成预支付的url的方法
     * @param out_trade_no ：父订单号
     * @param total_fee    ：订单的支付金额
     * @return 父订单号，总金额，预支付url
     * @throws Exception
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) throws Exception {
        //创建一个map集合来封装参数
        Map<String,String> parm=new HashMap<> ();
        parm.put ("appid",appid);
        parm.put ("mch_id","1473426802");
        parm.put ("nonce_str",WXPayUtil.generateNonceStr ());
        parm.put ("sign",WXPayUtil.generateNonceStr ());
        parm.put ("body","品优购双十二秒杀商品");
        parm.put ("out_trade_no",out_trade_no);
        parm.put ("total_fee",total_fee);
        parm.put ("spbill_create_ip","127.0.0.1");
        parm.put ("notify_url","https://www.jd.com");
        parm.put ("trade_type","NATIVE");


        String url="https://api.mch.weixin.qq.com/pay/unifiedorder";//接口连接
        //创建一个httpClient发请求
        HttpClient client = new HttpClient (url);
        //设置协议
        client.setHttps (true);
        String xmlParse= WXPayUtil.generateSignedXml (parm,partnerkey);
      //  System.out.println ("请求的参数："+xmlParse);
        //设置xml参数（按照api设置）
        client.setXmlParam (xmlParse);
        //发送post请求
        client.post ();
        //返回响应
        String result = client.getContent ();
        //需要取出url 生成二维码  需要将xml--->map             二维码链接	code_url
        Map<String,String> resultMap=WXPayUtil.xmlToMap (result);
        String code_url = resultMap.get ("code_url");
        Map<String,String> returnMap=new HashMap<> ();
        returnMap.put ("out_trade_no",out_trade_no);
        returnMap.put ("code_url",code_url);
        returnMap.put ("total_fee",total_fee);
        return returnMap;
    }

    /**
     * 根据订单号，查询订单状态
     *
     * @param out_trade_no 订单号
     * @return 订单状态信息
     * @throws Exception
     */
    @Override
    public Map queryOrderStatus(String out_trade_no) throws Exception {
        //创建一个map集合来封装参数
        Map<String,String> parm=new HashMap<> ();
        parm.put ("appid",appid);
        parm.put ("mch_id","1473426802");
        parm.put ("out_trade_no",out_trade_no);
        parm.put ("nonce_str",WXPayUtil.generateNonceStr ());
    //发送请求
        String url="https://api.mch.weixin.qq.com/pay/orderquery";
        HttpClient client=new HttpClient (url);
        String xmlParam=WXPayUtil.generateSignedXml (parm,partnerkey);
        client.setXmlParam (xmlParam);
        client.post ();
        client.setHttps (true);
        //获取响应
        String result = client.getContent ();
        //返回结果 返回的是一个xmlString类型，需要转为Map类型
        Map<String,String> map=WXPayUtil.xmlToMap (result);
        System.out.println (map);
        return map;
    }

    /**
     * 根据订单号关闭订单
     *
     * @param out_trade_no 订单号
     * @return 错误码
     */
    @Override
    public Map<String, String> closeOrder(String out_trade_no) throws Exception{
        //创建一个map集合来封装参数
        Map<String,String> parm=new HashMap<> ();
        parm.put ("appid",appid);
        parm.put ("mch_id","1473426802");
        parm.put ("out_trade_no",out_trade_no);
        parm.put ("nonce_str",WXPayUtil.generateNonceStr ());
        //发送请求
        String url="https://api.mch.weixin.qq.com/pay/closeorder";
        HttpClient client=new HttpClient (url);
        String xmlParam=WXPayUtil.generateSignedXml (parm,partnerkey);
        client.setXmlParam (xmlParam);
        client.post ();
        client.setHttps (true);
        //获取响应
        String result = client.getContent ();
        //返回结果 返回的是一个xmlString类型，需要转为Map类型
        Map<String,String> map=WXPayUtil.xmlToMap (result);
        System.out.println (map);
        return map;
    }
}
