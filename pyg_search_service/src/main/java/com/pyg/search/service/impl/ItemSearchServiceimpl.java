package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

 /*搜索商品
    前端给后端的数据，包含查询条件和分页条件
    后端给前端的数据，包含商品列表，分页参数，搜索面板数据
     */

  /*   Map resultMap=new HashMap ();
        Query query = new SimpleQuery ("*:*");
        Criteria criteria=new Criteria ("item_keywords").is ( searchMap.get ("keywords"));
        query.addCriteria (criteria);
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage (query, TbItem.class);

        Long total = scoredPage.getTotalElements ();//总记录数
        List<TbItem> itemList = scoredPage.getContent ();
        resultMap.put ("total",total);
        resultMap.put ("itemList",itemList);
        return resultMap;*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(timeout = 3000)
public class ItemSearchServiceimpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    //searchMap：{"keywords":"三星","category":"手机","price":"1000-1500","brand":"三星","spec":{"网络":"移动4G","机身内存":"64G"}}
    public Map<String, Object> search(Map searchMap) {
//查询需要用solrTemplate->需要配置文件->需要@autowired
//但是查询条件是什么，你需要知道前端怎么定义传过来的数据
//$scope.searchMap={keywords:'三星'}
        //处理关键字中的空格
        String keywords = (String) searchMap.get ("keywords");
        keywords= keywords.replace (" ","");
        searchMap.put ("keywords",keywords);

        Map resultMap=new HashMap ();
        //1.查询高亮后的商品（itemList是展示经过高亮和筛选后的商品，所以筛选是在这个方法中完成的）
        HighlightPage<TbItem> highlightPage = searchHigh (searchMap);
        List<TbItem> itemList = highlightPage.getContent ();
        resultMap.put ("itemList",itemList);
        //2.查询分类数据 分组查询  三级分类 模板id
        List<String> categoryList = findCategoryList (searchMap);
        resultMap.put ("categoryList",categoryList);
        //3.查询品牌和规格数据   模板id  品牌/规格    找第一个Category----------修改判断category是否有值，有值取值没值取第一个
        String category=categoryList.get (0);
        if(searchMap.get ("category")!=null && StringUtils.isNotEmpty ((String) searchMap.get ("category"))){
           category=(String) searchMap.get ("category"); }
        Map<String, Object> specAndBrandMap = searchBrandListAndSpecListByCategory (category);
        resultMap.putAll (specAndBrandMap);
        //分页 返回总页数和总条数
        long total = highlightPage.getTotalElements ();//总记录数
        resultMap.put ("total",total);
        int totalPages = highlightPage.getTotalPages ();//总页数
        resultMap.put ("totalPages",totalPages);

        return resultMap;
    }

    @Override
    public void importList(List itemList) {
        solrTemplate.saveBeans (itemList);
        solrTemplate.commit ();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        Query query=new SimpleQuery ();
        Criteria criteria=new Criteria ("item_goodsid").in (goodsIdList);
        query.addCriteria (criteria);
        solrTemplate.delete (query);
        solrTemplate.commit ();
    }


    //查询高亮后的商品的方法
    private HighlightPage<TbItem> searchHigh(Map searchMap) {
        //高亮查询  1.条件查询数据（在索引中查询和在数据库中查询不一样 ，要用solrTemplate），2.设置高亮选项，设置高亮条件 3将高亮内容封装到itemList中
        HighlightQuery query=new SimpleHighlightQuery ();
        HighlightOptions highlightOptions=new HighlightOptions ();
        highlightOptions.addField ("item_title");
        highlightOptions.setSimplePrefix ("<em style='color:red'>");
        highlightOptions.setSimplePostfix ("</em>");
        query.setHighlightOptions (highlightOptions);//设置高亮选项...选项有高亮域，高亮前缀，高亮后缀
        Criteria criteria=new Criteria ("item_keywords").is (searchMap.get ("keywords"));//fieldname是索引域中的域名
        query.addCriteria (criteria);//条件..注意只是在title中显示高亮，但是在别的部分查到还是会搜索出来，所以是在keywords中查询
    //----------------------------过滤条件---------------------------------------------------------------------------------------------------------------
        //1.category       先获取
        Object category = searchMap.get ("category");
        if (category!=null && StringUtils.isNotEmpty ((String)category))//如果为null就不能进行后面的判断，所以要先判断不为null
        {
            FilterQuery categoryFilter=new SimpleFacetQuery ();
            Criteria cateCriteria=new Criteria ("item_category").is (category);//哪个域，条件是什么
            categoryFilter.addCriteria (cateCriteria);
             query.addFilterQuery (categoryFilter);
        }
        //2.brand
        Object brand = searchMap.get ("brand");
        if (brand!=null && StringUtils.isNotEmpty ((String)brand))//如果为null就不能进行后面的判断，所以要先判断不为null
        {
            FilterQuery brandFilter=new SimpleFacetQuery ();
            Criteria brandCriteria=new Criteria ("item_brand").is (brand);//哪个域，条件是什么
            brandFilter.addCriteria (brandCriteria);
            query.addFilterQuery (brandFilter);
        }
        //3.spec   有多个 需要转成Map
        Object spec = searchMap.get ("spec");
       if (spec!=null){
            Map<String,String> specMap=(Map<String,String>)spec;
           for (String key : specMap.keySet ()) {
               FilterQuery specFilter=new SimpleFacetQuery ();
               Criteria specCriteria=new Criteria ("item_spec_*").is (searchMap.get (key));
               specFilter.addCriteria (specCriteria);
               query.addFilterQuery (specFilter);
           }

       }
        //4.price
        Object price = searchMap.get ("price");  //0-500
       if (price!=null && StringUtils.isNotEmpty ((String)price)){
           String[] split = ((String) price).split ("-");
           if (split[0]!="0"){
               FilterQuery priceFilter=new SimpleFacetQuery ();
               Criteria priceCriteria=new Criteria ("item_price").greaterThanEqual (split[0]);
               priceFilter.addCriteria (priceCriteria);
               query.addFilterQuery (priceFilter);
           }
           if (split[1]!="*"){
               FilterQuery priceFilter=new SimpleFacetQuery ();
               Criteria priceCriteria=new Criteria ("item_price").lessThanEqual (split[1]);
               priceFilter.addCriteria (priceCriteria);
               query.addFilterQuery (priceFilter);
           }
       }
        //5.分页处理
        Integer pageNo = (Integer) searchMap.get ("pageNo");
        Integer pageSize = (Integer) searchMap.get ("pageSize");
                 //开始索引  （pageNo-1）*pageSize   ------      索引长度   pageSize
        query.setOffset ((pageNo-1)*pageSize);
                //每页显示条数
        query.setRows (pageSize);
        //6.按照选择（价格,新品）排序  1。取出需要排序的域，if判断升序降序，在query.设置排序
        String sortField = (String) searchMap.get ("sortField");
        String sort = (String) searchMap.get ("sort");
        if(StringUtils.isNotEmpty (sortField)){
            if ("DESC".equals (sort)){
                Sort sortQ=new Sort (Sort.Direction.DESC,"item_"+sortField);
                query.addSort (sortQ);}
            if ("ASC".equals (sort)){
                Sort sortQ=new Sort (Sort.Direction.ASC,"item_"+sortField);
                query.addSort (sortQ);}
        }

        //通过查询条件，进行查询---------------------------------------------------------------------------------------------
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage (query, TbItem.class);//总记录（未获取高亮）
        //获取查询结果，高亮替换 tbItems中的域
        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted ();//获取总的查询数据，三部分responseHeader，response，highlighting
        for (HighlightEntry<TbItem> hlightEntry : highlighted) {//获取到highlighting部分
            TbItem item = hlightEntry.getEntity ();//获取到第一部分
            List<HighlightEntry.Highlight> highlights = hlightEntry.getHighlights ();//循环到每一个"830972“部分/
            for (HighlightEntry.Highlight highlight : highlights) {
              item.setTitle (highlight.getSnipplets ().get (0));
            }
        }
        long totalElements = highlightPage.getTotalElements ();
        System.out.println (totalElements);
        return highlightPage;
    }
    //2.查询分类数据的方法   select category from tb_item where title like '%三星%' group by category; 需要keywords条件->searchMap中
    public List<String> findCategoryList(Map searchMap){
    List<String> categoryList=new ArrayList<> ();
        Query query=new SimpleQuery ();
        //按关键字查询
        Criteria criteria=new Criteria ("item_keywords").is (searchMap.get ("keywords"));
        query.addCriteria (criteria);
        //设置分组
        GroupOptions groupOptions=new GroupOptions ();
        groupOptions.addGroupByField ("item_category");
        query.setGroupOptions (groupOptions);
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage (query, TbItem.class);
        GroupResult<TbItem> groupResult = groupPage.getGroupResult ("item_category");
        Page<GroupEntry<TbItem>> page = groupResult.getGroupEntries ();
        List<GroupEntry<TbItem>> content = page.getContent ();
        for (GroupEntry<TbItem> entry : content) {
            categoryList.add (entry.getGroupValue ());
        }

        return categoryList;
    }
    //3.查询品牌和规格数据的方法    模板id  品牌/规格    找第一个Category
    //通过redis存储的itemCat中category 找模板id，通过模板id 找redis中存储的brandList和specList
    private Map<String,Object> searchBrandListAndSpecListByCategory(String category) {
        Map<String,Object> resultMap=new HashMap<> ();
        //通过category获取模板id
        Long tempId = (Long) redisTemplate.boundHashOps ("itemCat").get (category);
        //通过模板id获取品牌列表和规格列表
        Object specList = redisTemplate.boundHashOps ("specList").get (tempId);
        Object brandList = redisTemplate.boundHashOps ("brandList").get (tempId);
        //封装到Map返回
        resultMap.put ("specList",specList);
        resultMap.put ("brandList",brandList);
        return  resultMap;
    }
}
