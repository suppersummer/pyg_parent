package com.pyg.util;

import com.alibaba.fastjson.JSON;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbItemExample;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    //分为两部分，1.在数据库中查询到所有tb_item表中title的内容 (条件：状态为1)2.创建main方法进行查询
    //难点，用main方法查询的时候，类.方法查询时，@Autowired注解下的TbitemMapper接口是使用不了的
    //解决：在spring配置文件中，将SolrUtil封装成一个bean（扫描SolrUtil需要注解），spring就是一个beanFactory ，用ApplicationContext加载配置文件，创建bean
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;
    public void importData(){
        TbItemExample example=new TbItemExample ();
        TbItemExample.Criteria criteria = example.createCriteria ();
        criteria.andStatusEqualTo ("1");
        List<TbItem> itemList = itemMapper.selectByExample (example);
        for (TbItem item : itemList) {
            String spec = item.getSpec ();
            if (StringUtils.isNotEmpty (spec)){
                Map map = JSON.parseObject (spec, Map.class);
                item.setSpecMap (map);
            }
            System.out.println (item.getTitle ());
        }
    solrTemplate.saveBeans (itemList);
        solrTemplate.commit ();
    }

    public static void main(String[] args) {
       ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean ("solrUtil");
        solrUtil.importData ();
    }

}
