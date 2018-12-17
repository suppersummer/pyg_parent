package com.pyg.shop.controller;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbGoods;
import com.pyg.pojo.TbItem;
import com.pyg.sellergoods.service.GoodsService;
import entity.Goods;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.List;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Autowired
	private JmsTemplate jmsTemplate;
	//商品审核导入solr索引库的队列
	@Autowired
	private Destination queueSolrDestination;
	//删除商品 ，删除solr索引库的队列
	@Autowired
	private Destination queueSolrDeleteDestination;
	//商品审核页面生成，发布订阅
	@Autowired
	private Destination topicPageDestination;
	@Autowired
	private  Destination pageTopicDeleteDestination;
//	@Reference
//	private ItemSearchService itemSearchService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			//删除solr索引库
//			itemSearchService.deleteByGoodsIds (Arrays.asList (ids));
			//使用activemq发送消息，让itemSearchService去消费消息，删除solr索引库
			jmsTemplate.send (queueSolrDeleteDestination, new MessageCreator () {
				@Override
				public Message createMessage(Session session) throws JMSException {
					//将要删除的发送到active上
					return session.createObjectMessage (ids);
				}
			});
			//删除静态页面
			jmsTemplate.send (pageTopicDeleteDestination, new MessageCreator () {
				@Override
				public Message createMessage(Session session) throws JMSException {
					//将要删除的发送到active上
					return session.createObjectMessage (ids);
				}
			});
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	//批量审核
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			goodsService.updateStatus (ids,status);
			//判断，审核通过商品，存solr 不通过删除
			if ("1".equals (status)){
				//1.goodId-->itemList
				List<TbItem> itemList= goodsService.findItemListByGoodsIdsAnddStatus (ids, status);
				//2.将itemList 存到solr库
//				itemSearchService.importList (itemList);
				//2.使用activemq发送消息，先将消息转为String类型(用json转)
				String itemStr = JSON.toJSONString (itemList);//这就是消息的内容

				jmsTemplate.send (queueSolrDestination, new MessageCreator () {
				@Override
				public Message createMessage(Session session) throws JMSException {

					return session.createTextMessage (itemStr);
				}



			});
			}
			//生成静态页面
			for (Long goodsId : ids) {
//				itemPageService.genHtml (goodsId);
				//使用active发送消息（发布订阅消息）
				jmsTemplate.send (topicPageDestination, new MessageCreator () {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage (ids);
					}
				});
			}
			return new Result(true, "操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败");
		}
	}
//	@Reference
//	private ItemPageService itemPageService;
//	@RequestMapping("genHtml")
//	public Result genHtml(Long goodsId){
//      //获取数据
//	//条用服务
//		try {
//	itemPageService.genHtml (goodsId);
//			return new Result(false, "操作成功");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new Result(false, "操作失败");
//		}
//	}
	
}
