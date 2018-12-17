package com.pyg.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSeckillOrderMapper;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.pojo.TbSeckillOrderExample;
import com.pyg.pojo.TbSeckillOrderExample.Criteria;
import com.pyg.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class  SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private IdWorker idWorker;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 生成订单
	 *
	 * @param name 用户名
	 * @param id
	 */
	@Override
	public void submitOrder(String name, Long id) {
		//根据id在redis中查找商品（order表中的数据需要goods表的内容）
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps ("seckillGoods").get (id);
		//减缓存，存入数据 （直到库存为0，同步数据库，其余都在redis库中做更改）
		seckillGoods.setStockCount (seckillGoods.getStockCount ()-1);
		if (seckillGoods.getStockCount ()==0){
			//同步数据库，删除redis中秒杀商品
			seckillGoodsMapper.updateByPrimaryKey (seckillGoods);
			redisTemplate.boundHashOps ("seckillGoods").delete (id);
		}else {
			//库存不为0将更改过的商品信息放回redis中
			redisTemplate.boundHashOps ("seckillGoods").put (id,seckillGoods);
		}
		//生成秒杀订单
		TbSeckillOrder seckillOrder = new TbSeckillOrder ();
		seckillOrder.setId (idWorker.nextId ());
		seckillOrder.setCreateTime (new Date ());
		seckillOrder.setMoney (seckillGoods.getCostPrice ());
		seckillOrder.setSeckillId (id);
		seckillOrder.setStatus ("1");//未支付
		seckillOrder.setUserId (name);

		//将秒杀订单暂存到redis中
		redisTemplate.boundHashOps ("seckillOrder").put (name,seckillOrder);
	}

	/**
	 * 根据用户名在redis中查询订单
	 *
	 * @param name
	 * @return
	 */
	@Override
	public TbSeckillOrder findOrderByredis(String name) {
		return (TbSeckillOrder)redisTemplate.boundHashOps ("seckillOrder").get (name);
	}

	/**
	 * 交易成功修改订单状态
	 *
	 * @param out_trade_no   订单id（存到order表中）
	 * @param transaction_id 交易流水号
	 * @param name           用户名，用来在redis中获取订单
	 */
	@Override
	public void updateOrderStatus(String out_trade_no, Object transaction_id, String name) {
		//在redis中查找订单
		TbSeckillOrder seckillOrder = findOrderByredis (name);
		//设置状态，流水信息，支付时间
		seckillOrder.setStatus ("2");//交易成功
		seckillOrder.setTransactionId ((String) transaction_id);
		seckillOrder.setPayTime (new Date ());

		//交易成功后，将订单表保存到数据库中
		seckillOrderMapper.insert (seckillOrder);
		//删除redis中Goods数据
		redisTemplate.boundHashOps ("seckillOrder").delete (name);
	}

	/**
	 * 支付超时，关闭微信订单，回复库存，删除秒杀订单
	 * @param out_trade_no 订单号----关微信订单
	 * @param name  恢复库存
	 */
	@Override
	public void closeSeckillOrder(String out_trade_no, String name) {
		TbSeckillOrder seckillOrder = findOrderByredis (name);
		//从redis中获取
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps ("seckillGoods").get (seckillOrder.getSeckillId ());
		//恢复库存
		seckillGoods.setStockCount (seckillGoods.getStockCount ()+1);
		redisTemplate.boundHashOps ("seckillGoods").put (seckillOrder.getSeckillId (),seckillGoods);
		//删除秒杀订单
		redisTemplate.boundHashOps ("seckillOrder").delete (name);
	}

}
