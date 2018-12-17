package com.pyg.seckill.service;
import java.util.List;
import com.pyg.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

	/**
	 * 生成订单
	 * @param name 用户名
	 * @param id
	 */
    void submitOrder(String name, Long id);

	/**
	 * 根据用户名在redis中查询订单
	 * @param name
	 * @return
	 */
	TbSeckillOrder findOrderByredis(String name);

	/**
	 * 交易成功修改订单状态
	 * @param out_trade_no 订单id（存到order表中）
	 * @param transaction_id 交易流水号
	 * @param name  用户名，用来在redis中获取订单
	 */
    void updateOrderStatus(String out_trade_no, Object transaction_id, String name);

	/**
	 * 支付超时，关闭微信订单，回复库存，删除秒杀订单
	 * @param out_trade_no 订单号----关微信订单
	 * @param name  恢复库存
	 */
	void closeSeckillOrder(String out_trade_no, String name);
}
