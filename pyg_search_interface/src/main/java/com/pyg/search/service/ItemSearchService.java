package com.pyg.search.service;

import java.util.List;
import java.util.Map;
/*
商品搜索接口
 */
public interface ItemSearchService {
    /*搜索商品
    前端给后端的数据，包含查询条件和分页条件
    后端给前端的数据，包含商品列表，分页参数，搜索面板数据
     */
    public Map<String,Object> search(Map searchMap);

    //添加审核通过的数据
    public void importList(List itemList);

    //删除状态变为0的数据（删除）
    public void deleteByGoodsIds(List goodsIdList);
}
