package com.pyg.task.service;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {
        @Autowired
        private RedisTemplate redisTemplate;
        @Autowired
        private TbSeckillGoodsMapper seckillGoodsMapper;
    /**
     * 定时任务； cron表达式规定什么时候执行job
     */
    @Scheduled(cron = "10,20 * * * * ?")
    public void refreshSeckillGoods(){
        SimpleDateFormat format=new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        System.out.println ("执行了任务调度"+format.format (new Date ()));
        //---------------------------------------------------------------------------
        try {
            //任务：将redis中的秒杀商品定时添加到数据库中
            //在redis中查找秒杀商品 的id们
            Set idList = redisTemplate.boundHashOps ("seckillGoods").keys ();
            //在数据库中补充符合条件的秒杀商品
            TbSeckillGoodsExample example=new TbSeckillGoodsExample ();
            TbSeckillGoodsExample.Criteria criteria = example.createCriteria ();
            criteria.andStatusEqualTo ("1");
            criteria.andStockCountGreaterThan (0);
            criteria.andStartTimeLessThan (new Date ());
            criteria.andEndTimeGreaterThan (new Date ());
            if (idList!=null && idList.size ()>0){
                criteria.andIdNotIn (new ArrayList<> (idList));
            }
            List<TbSeckillGoods> tbSeckillGoods = seckillGoodsMapper.selectByExample (example);
            //添加到redis中
            for (TbSeckillGoods seckillGood : tbSeckillGoods) {
                redisTemplate.boundHashOps ("seckillGoods").put (seckillGood.getId (),seckillGood);
            }
            System.out.println ("将"+tbSeckillGoods.size()+"条商品装入缓存");
        } catch (Exception e) {
            e.printStackTrace ();
        }

    }

    /**
     * 移除过期秒杀商品
     */
    @Scheduled(cron ="30 * * * * ?" )
    public void removeSeckillGoods(){
        //结束时间小于等于当前时间，就移除
        System.out.println("移除秒杀商品任务在执行");
        //查询缓存中的秒杀商品列表，发现过期就移除
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps ("seckillGoods").values ();
        for (TbSeckillGoods seckillGood : seckillGoods) {
            Date endTime = seckillGood.getEndTime ();
            if (endTime.getTime ()<new Date ().getTime ()){
                //将到期数据保存数据库
                seckillGoodsMapper.updateByPrimaryKey (seckillGood);
                redisTemplate.boundHashOps ("seckillGoods").delete (seckillGood.getId ());
            }

        }
    }

}
