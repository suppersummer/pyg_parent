package com.pyg.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/item")
public class ItemSearchController {
@Reference
    private ItemSearchService itemSearchService;

    /**
     * 搜索的方法
     * @param searchMap
     * @return
     */
    @RequestMapping("search")
    public Map<String, Object> search(@RequestBody Map searchMap){

        return itemSearchService.search (searchMap);
    }
}
