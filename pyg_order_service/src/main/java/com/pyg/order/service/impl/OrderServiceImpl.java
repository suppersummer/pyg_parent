package com.pyg.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pyg.mapper.TbOrderItemMapper;
import com.pyg.mapper.TbPayLogMapper;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojo.TbPayLog;
import entity.Cart;
import net.sf.jsqlparser.statement.replace.Replace;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbOrderMapper;
import com.pyg.pojo.TbOrder;
import com.pyg.pojo.TbOrderExample;
import com.pyg.pojo.TbOrderExample.Criteria;
import com.pyg.order.service.OrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbPayLogMapper payLogMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加------生成订单的方法
	 */
	@Override
	public void add(TbOrder order) throws Exception{
	//获取redis中购物车的数据 (购物车列表   每一个cart生成一个订单  一个订单按商品id 生成多个orderItem表)
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps ("cartList").get (order.getUserId ());
		List<Long> idsList=new ArrayList<> ();
		//将数据添加到数据库	id自增长
		double payment=0.0;
		for (Cart cart : cartList) {
		Long orderId=idWorker.nextId ();
		order.setOrderId (orderId);
		idsList.add (orderId);
		//生成订单明细  和   支付总金额
		for (TbOrderItem orderItem : cart.getOrderItemList ()) {
			payment+=orderItem.getTotalFee ().doubleValue ();
			//订单明细表orderItem id自增长
			Long orderItemId=idWorker.nextId ();
			orderItem.setId (orderItemId);
			orderItem.setOrderId (orderId);
		orderItemMapper.insert (orderItem);
		}
		order.setPayment (new BigDecimal (payment));//支付总价格
		order.setCreateTime (new Date ());
		order.setUpdateTime (new Date ());
		order.setSellerId (cart.getSellerId ());
		order.setStatus ("1");//未支付
			orderMapper.insert(order);

		}
		//生成支付日志(微信支付)  保存到数据库，存到redis中 -- pay_log
		if ("1".equals (order.getPaymentType ())){
			TbPayLog payLog=new TbPayLog ();
			//补全表信息
			payLog.setOutTradeNo (idWorker.nextId ()+"");
			payLog.setCreateTime (new Date ());
			payLog.setTotalFee ((long) (payment*100));//按分存
			payLog.setPayType ("1");//支付类型
			payLog.setTradeState ("0");//未支付
			payLog.setUserId (order.getUserId ());
			idsList.toString ().replace ("[","").replace ("]","").replace (" ","");
			//保存数据库
			payLogMapper.insert (payLog);
			//保存到redis中
			redisTemplate.boundHashOps ("payLog").put (order.getUserId (),payLog);
		}


	//清空redis中数据
	redisTemplate.boundHashOps ("cartList").delete (order.getUserId ());

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 在redis中查找payLog
	 *
	 * @param name 用户名
	 * @return paylog
	 */
	@Override
	public TbPayLog findPayLogfromRedis(String name) {
		TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps ("payLog").get (name);
		return payLog;


	}

	/**
	 * 交易成功，修改父订单和子订单状态,父订单记录交易流水号和支付时间
	 * @param out_trade_no   订单号
	 * @param transaction_id 交易流水号
	 */
	@Override
	public void updateOrderStatus(String out_trade_no, Object transaction_id) {
		TbPayLog payLog = payLogMapper.selectByPrimaryKey (out_trade_no);
		payLog.setTradeState ("1");
		payLog.setPayTime (new Date ());
		payLog.setTransactionId ((String) transaction_id);
		payLogMapper.updateByPrimaryKey (payLog);
		//子订单
		String OrderIds = payLog.getOrderList ();
		String[] idsList = OrderIds.split (",");
		for (String id : idsList) {
			//查到order每个表，修改支付状态
			TbOrder order = orderMapper.selectByPrimaryKey ((long) Integer.getInteger (id));
			order.setStatus ("2");
			order.setPaymentTime (new Date ());
			orderMapper.updateByPrimaryKey (order);
		}
		//删除redis中的父订单记录
		redisTemplate.boundHashOps ("payLog").delete (payLog.getUserId ());
	}

}
