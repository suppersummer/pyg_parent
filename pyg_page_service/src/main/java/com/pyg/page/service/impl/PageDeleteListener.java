package com.pyg.page.service.impl;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

//商品删除，删除静态页面的监听类
@Component
public class PageDeleteListener implements MessageListener  {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage= (ObjectMessage) message;
        try {
            Long[] ids = (Long[]) objectMessage.getObject ();
            System.out.println ("接收到的消息"+ids);
            for (Long id : ids) {
               itemPageService.delHtml (id);
            }
        } catch (JMSException e) {
            e.printStackTrace ();
        }

    }
}
