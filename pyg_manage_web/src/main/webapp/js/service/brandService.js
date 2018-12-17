//自定义服务 1服务的名称
app.service('brandService',function($http){
    //获取所有品牌数据
    this.findAll=function(){
        return  $http.get('../brand/findAll.do');
    }
    //分页查询
    this.findPage=function (page,rows) {
        return $http.get('http://localhost:9101/brand/findPage.do?page='+page+'&rows='+rows);
    }
    //回显
    this.findOne=function (id) {
        return $http.get('../brand/findOne.do?id='+id);
    }
    //批量删除
    this.dele=function (ids) {
        return $http.get('../brand/delete.do?ids='+ids);
    }
    //条件分页查询
    this.search=function (page,rows,entity) {
        return $http.post('../brand/search.do?page='+page+'&rows='+rows,entity);
    }
    //新增
    this.add=function (entity) {
        return $http.post('../brand/add.do',entity);
    }
    //修改
    this.update=function (entity) {
        return $http.post('../brand/update.do',entity);
    }
    //获取品牌下拉数据
    this.selectOptionList=function () {
        return $http.get('../brand/selectOptionList.do');
    }
});