package com.pyg.order.service;
import java.util.List;
import com.pyg.pojo.TbOrder;

import com.pyg.pojo.TbPayLog;
import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface OrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加------生成订单的方法
	*/
	public void add(TbOrder order) throws  Exception;
	
	
	/**
	 * 修改
	 */
	public void update(TbOrder order);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbOrder findOne(Long id);
	
	
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
	public PageResult findPage(TbOrder order, int pageNum, int pageSize);

	/**
	 * 在redis中查找payLog
	 * @param name  用户名
	 * @return paylog
	 */
	TbPayLog findPayLogfromRedis(String name);

	/**
	 * 交易成功，修改父订单和子订单状态,父订单记录交易流水号和支付时间
	 * @param out_trade_no 订单号
	 * @param transaction_id 交易流水号
	 */
	void updateOrderStatus(String out_trade_no, Object transaction_id);
}
