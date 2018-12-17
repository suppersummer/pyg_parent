package com.pyg.sellergoods.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.*;
import com.pyg.pojo.*;
import com.pyg.pojo.TbGoodsExample.Criteria;
import com.pyg.sellergoods.service.GoodsService;
import entity.Goods;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods ().setAuditStatus ("0");
		goodsMapper.insert (goods.getGoods ());
		Long id = goods.getGoods ().getId ();
		goods.getGoodsDesc ().setGoodsId (id);
		goodsDescMapper.insert (goods.getGoodsDesc ());
		//添加sku，是一个列表数据，需要循环添加
          insertItem (goods);



	}
	private void insertItem(Goods goods){
          //是否启用规格
          if("1".equals (goods.getGoods ().getIsEnableSpec ())){//启用规格
              for (TbItem item : goods.getItemList ()) {
                  //title:商品名加配置...配置spec：{"机身内存":"16G","网络":"联通3G"}(对象，用parseObject)
                  //spec是json类型，要把它换成mapleixing
                  String title=goods.getGoods ().getGoodsName ();
                  Map<String,String> spec = JSON.parseObject (item.getSpec (), Map.class);
                  for (String key : spec.keySet ()) {
                      title+=" "+spec.get (key); }
                  item.setTitle (title);
                  setItem(goods,item);//设置参数
                  itemMapper.insert (item);
              }
          }else{//不启用规格.SPU就是SKU 只有一条数据
              TbItem item=new TbItem ();
              item.setPrice (goods.getGoods ().getPrice ());//SPU的价格就是SKU的价格
              item.setTitle (goods.getGoods ().getGoodsName ());

              setItem(goods,item);
              item.setNum (99999);
              item.setStatus ("1");//正常
              item.setIsDefault ("1");
              item.setSpec ("{}");

              itemMapper.insert (item);
          }
      }
	private void setItem(Goods goods,TbItem item){
		//image:获取goodsDesc中item_images字段中第一张图片的url
		List<Map> images = JSON.parseArray (goods.getGoodsDesc ().getItemImages (), Map.class);
		if (images.size ()>0){
			item.setImage ((String) images.get (0).get ("url"));
		}
		//categoryId:goods表中category3Id
		item.setCategoryid (goods.getGoods ().getCategory3Id ());
		item.setCreateTime (new Date ());
		item.setUpdateTime (new Date ());
		item.setGoodsId (goods.getGoods ().getId ());
		item.setSellerId(goods.getGoods().getSellerId());//添加基本信息时，在web层获取过登陆用户
		//Category 分类名称
		item.setCategory (itemCatMapper.selectByPrimaryKey (goods.getGoods ().getCategory3Id ()).getName ());
		item.setBrand (brandMapper.selectByPrimaryKey (goods.getGoods ().getBrandId ()).getName ());
		//获取店铺名称
		item.setSeller (sellerMapper.selectByPrimaryKey (goods.getGoods ().getSellerId ()).getNickName ());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
        //修改基本信息
	    goodsMapper.updateByPrimaryKey(goods.getGoods ());
	    goodsDescMapper.updateByPrimaryKey (goods.getGoodsDesc ());
	    //修改规格列表信息，先删后添加
          TbItemExample example=new TbItemExample ();
          TbItemExample.Criteria criteria = example.createCriteria ();
          criteria.andGoodsIdEqualTo (goods.getGoods ().getId ());
          itemMapper.deleteByExample (example)  ;
          //重新添加
          insertItem (goods);


	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
	    Goods goods=new Goods ();
          TbGoods tbGoods = goodsMapper.selectByPrimaryKey (id);
          TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey (id);
          TbItemExample example=new TbItemExample ();
          TbItemExample.Criteria criteria = example.createCriteria ();
          criteria.andGoodsIdEqualTo (id);
          List<TbItem> tbItems = itemMapper.selectByExample (example);
          goods.setGoods (tbGoods);
          goods.setGoodsDesc (tbGoodsDesc);
          goods.setItemList (tbItems);
          return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
                TbGoods goods = goodsMapper.selectByPrimaryKey (id);
                goods.setIsDelete ("1");//1：逻辑删除
                goodsMapper.updateByPrimaryKey (goods);

            }
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo (goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
		}
		criteria.andIsDeleteIsNull ();
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
//批量审核
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey (id);
            goods.setAuditStatus (status);
            goodsMapper.updateByPrimaryKey (goods);
        }

    }
	//根据商品id和状态查询Item表信息（sku）solr库中存储的是sku信息
	@Override
	public List<TbItem> findItemListByGoodsIdsAnddStatus(Long[] goodsIds, String status) {
		TbItemExample example=new TbItemExample ();
		TbItemExample.Criteria criteria = example.createCriteria ();
		criteria.andStatusEqualTo (status);
		criteria.andGoodsIdIn (Arrays.asList (goodsIds));
		return  itemMapper.selectByExample (example);

	}

}
