package com.pyg.search.service.impl;

import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.util.Arrays;

@Component
public class itemDeleteListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage= (ObjectMessage) message;
        try {
            Long[] ids = (Long[]) objectMessage.getObject ();
            itemSearchService.deleteByGoodsIds (Arrays.asList (ids));
            System.out.println ("商品删除后监听到的消息--"+ids+"-----------------------------------");
        } catch (JMSException e) {
            e.printStackTrace ();
        }

    }
}
