package com.pyg.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
@Component
//一监听到消息就调用方法，把监听到的消息传进去
public class itemSearchListener implements MessageListener {
    @Autowired
    private  ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage= (TextMessage) message;
        try {
            String text = textMessage.getText ();
            //传到方法里的类型是List<TbItem>这个类型，所以需要用json进行转换
            List<TbItem> itemList = JSON.parseArray (text, TbItem.class);
            itemSearchService.importList (itemList);
            System.out.println ("消费消息结束------------------------------");
        } catch (JMSException e) {
            e.printStackTrace ();
        }
    }
}
