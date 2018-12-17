package com.pyg.seckill.service.impl;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillGoodsExample;
import com.pyg.pojo.TbSeckillGoodsExample.Criteria;
import com.pyg.seckill.service.SeckillGoodsService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 查询秒杀商品
	 *
	 * @return 商品信息列表
	 */
	@Override
	public List<TbSeckillGoods> findList() {
		//从redis中获取秒杀列表数据
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps ("seckillGoods").values ();
		if (seckillGoodsList==null  || seckillGoodsList.size ()==0){
			//redis中没有，在数据库中取出，存到redis中
			TbSeckillGoodsExample example=new TbSeckillGoodsExample ();
			Criteria criteria = example.createCriteria ();
			//状态为一
			criteria.andStatusEqualTo ("1");//审核通过
			//库存大于0
			criteria.andStockCountGreaterThan (0);
			//开始时间小于当前时间
			criteria.andStartTimeLessThan (new Date ());
			//结束时间大于当前时间
			criteria.andEndTimeGreaterThan (new Date ());

			 seckillGoodsList = seckillGoodsMapper.selectByExample (example);

			//存到redis中
			for (TbSeckillGoods seckillGoods : seckillGoodsList) {

				redisTemplate.boundHashOps ("seckillGoods").put(seckillGoods.getId (),seckillGoods);
			}
		}


		return seckillGoodsList;
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillGoods> page=   (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods){
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id){
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillGoods!=null){			
						if(seckillGoods.getTitle()!=null && seckillGoods.getTitle().length()>0){
				criteria.andTitleLike("%"+seckillGoods.getTitle()+"%");
			}
			if(seckillGoods.getSmallPic()!=null && seckillGoods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+seckillGoods.getSmallPic()+"%");
			}
			if(seckillGoods.getSellerId()!=null && seckillGoods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillGoods.getSellerId()+"%");
			}
			if(seckillGoods.getStatus()!=null && seckillGoods.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillGoods.getStatus()+"%");
			}
			if(seckillGoods.getIntroduction()!=null && seckillGoods.getIntroduction().length()>0){
				criteria.andIntroductionLike("%"+seckillGoods.getIntroduction()+"%");
			}
	
		}
		
		Page<TbSeckillGoods> page= (Page<TbSeckillGoods>)seckillGoodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据id获取商品详情页信息   在redis中
	 *
	 * @param id
	 * @return 商品信息
	 */
	@Override
	public TbSeckillGoods loadSeckillGoods(Long id) {

		return (TbSeckillGoods)redisTemplate.boundHashOps ("seckillGoods").get (id);
	}

}
