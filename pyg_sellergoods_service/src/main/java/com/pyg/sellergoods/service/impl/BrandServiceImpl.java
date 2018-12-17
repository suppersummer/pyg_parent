package com.pyg.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbBrandMapper;
import com.pyg.pojo.TbBrand;
import com.pyg.pojo.TbBrandExample;
import com.pyg.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        //设置了pageHelper 拦截器就起作用了，影响sql语句 影响封装的结果
        PageHelper.startPage(pageNum,pageSize);

        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void add(TbBrand brand) {
        brandMapper.insert(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(TbBrand brand) {
        //update tb_brand set name='' ,firstChar='' where id=3
        brandMapper.updateByPrimaryKey(brand);
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id :ids){
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
        //设置了pageHelper 拦截器就起作用了，影响sql语句 影响封装的结果
        PageHelper.startPage(pageNum,pageSize);

        TbBrandExample example = new TbBrandExample();// select * from tb_brand where ②
        TbBrandExample.Criteria criteria = example.createCriteria();
        if(brand!=null){
            if(brand.getName()!=null&&!"".equals(brand.getName().trim())){
                criteria.andNameLike("%"+brand.getName()+"%");//  select * from tb_brand where name like '%三星%' ③
            }
            if(brand.getFirstChar()!=null&&!"".equals(brand.getFirstChar().trim())){
                criteria.andFirstCharEqualTo(brand.getFirstChar());//select * from tb_brand where firstChar='S' ④
            }
        }
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);//select * from tb_brand ①

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public List<Map> selectOptionList() {

        return brandMapper.selectOptionList();
    }

}
