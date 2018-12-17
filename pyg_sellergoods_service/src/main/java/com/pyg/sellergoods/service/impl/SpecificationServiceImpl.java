package com.pyg.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbSpecificationMapper;
import com.pyg.mapper.TbSpecificationOptionMapper;
import com.pyg.pojo.TbSpecification;
import com.pyg.pojo.TbSpecificationExample;
import com.pyg.pojo.TbSpecificationExample.Criteria;
import com.pyg.pojo.TbSpecificationOption;
import com.pyg.pojo.TbSpecificationOptionExample;
import com.pyg.pojogroup.Specification;
import com.pyg.sellergoods.service.SpecificationService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	@Transactional
	public void add(Specification specification) {
		//1.添加规格对象
		specificationMapper.insert(specification.getSpecification());
		//2.添加规格选项列表
		for(TbSpecificationOption specificationOption : specification.getSpecificationOptionList()){
			specificationOption.setSpecId(specification.getSpecification().getId());//规格主键就是选项的外键
			specificationOptionMapper.insert(specificationOption);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//修改规格，然后通过规格id将规格列表全删除，再从新循环添加
		specificationMapper.updateByPrimaryKey(specification.getSpecification ());
		//通过外键删列表
		TbSpecificationOptionExample example=new TbSpecificationOptionExample ();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria ();
		criteria.andSpecIdEqualTo (specification.getSpecification ().getId ());
		specificationOptionMapper.deleteByExample (example);
		//循环添加列表
		for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList ()) {
			//输入表格时，并没有输入外键id，所以需要在主表中获取外键id
			tbSpecificationOption.setSpecId (specification.getSpecification ().getId ());
			specificationOptionMapper.insert (tbSpecificationOption);
		}

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//return specificationMapper.selectByPrimaryKey(id);
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey (id);
		TbSpecificationOptionExample example=new TbSpecificationOptionExample ();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria ();
		criteria.andSpecIdEqualTo (id);
		List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample (example);
		Specification specification=new Specification ();
		specification.setSpecification (tbSpecification);
		specification.setSpecificationOptionList (tbSpecificationOptions);
		return specification ;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			specificationMapper.deleteByPrimaryKey(id);
			//思路：先删除规格，再通过规格id（外键）删除规格选项.andSpecIdEqualTo

			TbSpecificationOptionExample example =new TbSpecificationOptionExample ();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria ();
			criteria.andSpecIdEqualTo (id);
			specificationOptionMapper.deleteByExample (example);

		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}


}
