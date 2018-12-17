//定义控制器
app.controller('brandController',function($scope,$controller,brandService){
    //1被继承的控制器的名称 2{}  伪继承
    $controller('baseController',{$scope:$scope});

    //查询所有品牌数据
    $scope.findAll=function(){
        brandService.findAll().success(
            function(response){
                $scope.list=response;
            }
        )
    }

    //分页查询
    $scope.findPage=function (page,rows) {
        brandService.findPage(page,rows).success(
            function (response) {
                $scope.list=response.rows;//当前页列表数据
                $scope.paginationConf.totalItems=response.total;
            }
        )
    }


    //保存操作 什么时候添加什么时候修改
    $scope.save=function () {
        var methodObject;
        if($scope.entity.id!=null){//有id修改操作
            methodObject=brandService.update($scope.entity);
        }else{
            methodObject=brandService.add($scope.entity);
        }
        methodObject.success(
            function (response) {//result {success:true,message:'新增成功'}
                if(response.success){//成功
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
            }
        )
    }

    //数据回显
    $scope.findOne=function(id){
        brandService.findOne(id).success(
            function (response) {//品牌对象
                $scope.entity=response;
            }
        )
    }

    //批量删除
    $scope.dele=function(){
        brandService.dele($scope.selectIds).success(
            function (response) {
                if(response.success){//成功
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }
                $scope.selectIds=[]; //执行后清空数组
            }
        )
    }

    $scope.searchEntity={};//封装搜索条件的对象

    //条件分页查询
    $scope.search=function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.list=response.rows;//当前页列表数据
                $scope.paginationConf.totalItems=response.total;
            }
        )
    }
})