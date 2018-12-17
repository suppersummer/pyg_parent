package com.pyg.sellergoods.service;

import com.pyg.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌的接口
 */
public interface BrandService {
    /*
    *@Desc 查询所有品牌列表
    *@param 
    *@return java.util.List<com.pyg.pojo.TbBrand>
    **/
    public List<TbBrand> findAll();

    /*
    *@Desc 分页查询品牌数据
    *@param pageNum 当前页面
    *@param pageSize 每页显示条数
    *@return entity.PageResult
    **/
    public PageResult findPage(int pageNum,int pageSize);

    /*
    *@Desc 添加品牌
    *@param brand web层封装的json对象
    *@return void
    **/
    public void add(TbBrand brand);

    /*
    *@Desc 根据id查询某个品牌
    *@param id 主键
    *@return com.pyg.pojo.TbBrand
    **/
    public TbBrand findOne(Long id);


    /*
    *@Desc 修改品牌
    *@param brand web层封装的json对象 里面有id值
    *@return void
    **/
    public void update(TbBrand brand);

    /*
    *@Desc 批量删除
    *@param ids 主键数组
    *@return void
    **/
    public void delete(Long[] ids);

    /*
    *@Desc 分页条件查询
    *@param brand 前端提交过来的条件
    *@param pageNum 当前页码
    *@param pageSize 每页显示条数
    *@return entity.PageResult
    **/
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    List<Map> selectOptionList();
}
