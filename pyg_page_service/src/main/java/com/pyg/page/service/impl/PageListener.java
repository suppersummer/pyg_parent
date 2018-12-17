package com.pyg.page.service.impl;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;


@Component
public class PageListener implements MessageListener {
    @Autowired
   private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage= (TextMessage) message;
        try { //Long[] ids
            String id = textMessage.getText ();
            System.out.println ("接收到消息");
                itemPageService.genHtml (Long.valueOf (id));
            System.out.println ("页面生成服务结束---------------");

        } catch (JMSException e) {
            e.printStackTrace ();
        }
    }
}
