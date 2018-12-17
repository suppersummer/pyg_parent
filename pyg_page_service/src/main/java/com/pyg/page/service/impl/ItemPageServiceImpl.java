package com.pyg.page.service.impl;


import com.pyg.mapper.TbGoodsDescMapper;
import com.pyg.mapper.TbGoodsMapper;
import com.pyg.mapper.TbItemCatMapper;
import com.pyg.mapper.TbItemMapper;
import com.pyg.page.service.ItemPageService;


import com.pyg.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemPageServiceImpl implements ItemPageService {
    //根据传递的spu商品id，生成对应的详情页面
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDeskMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
   /* @Value ("${pageDir}")
    private String pageDir;*///配置地址的文件
    private String pageDir = "E:/code/pyg_parent/pyg_page_web/src/main/webapp/";
    @Override
    public void genHtml(Long goodsId) {
      //准备数据
        Map dataModel=new HashMap ();
        TbGoods goods = goodsMapper.selectByPrimaryKey (goodsId);
        dataModel.put ("goods",goods);
        TbGoodsDesc goodsDesc = goodsDeskMapper.selectByPrimaryKey (goodsId);
        dataModel.put ("goodsDesc",goodsDesc);
     //   面包屑                    itemMapper.selectByExample ()
        String category1Id = itemCatMapper.selectByPrimaryKey (goods.getCategory1Id ()).getName ();
        String category2Id = itemCatMapper.selectByPrimaryKey (goods.getCategory2Id ()).getName ();
        String category3Id = itemCatMapper.selectByPrimaryKey (goods.getCategory3Id ()).getName ();
        dataModel.put ("category1Id",category1Id);
        dataModel.put ("category2Id",category2Id);
        dataModel.put ("category3Id",category3Id);
        //sku列表
        TbItemExample example=new TbItemExample ();
        TbItemExample.Criteria criteria = example.createCriteria ();
         criteria.andGoodsIdEqualTo (goodsId);
         criteria.andStatusEqualTo ("1");//状态
        example.setOrderByClause ("is_default desc");//排序，在example中排
        List<TbItem> itemList = itemMapper.selectByExample (example);
        dataModel.put ("itemList",itemList);

        try {
            //读取模板
            Configuration configuration = freeMarkerConfig.getConfiguration ();
            Template template = configuration.getTemplate ("item.ftl");
            //
            Writer out=new OutputStreamWriter(new FileOutputStream (pageDir+goodsId+".html"), "UTF-8");
            System.out.println (pageDir);

            template.process (dataModel,out);
            out.close ();

            //调用freemarker引擎，生成静态页面
        } catch (Exception e) {
            e.printStackTrace ();
        }


    }
//根据传递的商品id。删除对应的静态页面
    @Override
    public void delHtml(Long goodsId) {
        //文件位置
        String path=pageDir+goodsId+".html";
        new File (path).delete ();
    }
}
