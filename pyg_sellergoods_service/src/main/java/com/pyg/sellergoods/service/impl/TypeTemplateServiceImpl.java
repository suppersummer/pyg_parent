package com.pyg.sellergoods.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSpecificationOptionMapper;
import com.pyg.mapper.TbTypeTemplateMapper;
import com.pyg.pojo.TbSpecificationOption;
import com.pyg.pojo.TbSpecificationOptionExample;
import com.pyg.pojo.TbTypeTemplate;
import com.pyg.pojo.TbTypeTemplateExample;
import com.pyg.pojo.TbTypeTemplateExample.Criteria;
import com.pyg.sellergoods.service.TypeTemplateService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		//将查询结果存到redis中，key是模板id，值是规格/品牌
			List<TbTypeTemplate> templateList = findAll ();
			for (TbTypeTemplate template : templateList) {
				//规格specList   注意：规格中没有规格列表
				List<Map> spec = findSpecByTypeId (template.getId ());
				redisTemplate.boundHashOps ("specList").put (template.getId (),spec);

				//将品牌存到redis中
				redisTemplate.boundHashOps ("brandList").put (template.getId (),JSON.parseArray (template.getBrandIds (),Map.class));
			}
			System.out.println ("缓存所有模板数据");
			PageHelper.startPage(pageNum, pageSize);//分页助手
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();

		//查询
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	//根据模板id，查询规格spec和规格选项specOption
	@Override
	public List<Map> findSpecByTypeId(Long typeId) {
		TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey (typeId);
		//获取规格数据[{"id":27,"text":"网络","options":"16g"},{"id":32,"text":"机身内存"}]
		List<Map> maps = JSON.parseArray (tbTypeTemplate.getSpecIds (), Map.class);
		for (Map map : maps) {
			//根据规格id，选择规格选项列表
			Object specId = map.get ("id");
			TbSpecificationOptionExample example=new TbSpecificationOptionExample ();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria ();
			criteria.andSpecIdEqualTo (new Long ((Integer) (specId)));
			List<TbSpecificationOption> SpecOptionList = specificationOptionMapper.selectByExample (example);
			map.put ("options",SpecOptionList);

		}
		return maps;
	}

}
