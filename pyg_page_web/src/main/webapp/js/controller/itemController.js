//商品详情页（控制层）
app.controller('itemController', function ($http,$scope) {
    //数据操作
    $scope.addNum = function (num) {
        $scope.num = $scope.num + num;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    }

    //勾选的结果，存在这个集合里
    $scope.specification = {};
    //点击选择规格，赋值
    $scope.selectSpecification = function (key, value) {
        $scope.specification[key] = value;

        //根据选择的规格去skuList中找对应skutitle
        for(var i=0;i<skuList.length;i++){
            if ( matchObject($scope.specification,skuList[i].spec)){
                $scope.sku=skuList[i];
                return;
            }
            //如过没匹配上
            $scope.sku={id:0,title:'------------',price:0};
        }
    }
    //判断两个json是否相等
    matchObject=function (map1,map2) {
        for(var k in map1){
            if(map1[k] != map2[k]){
                return false;
            }
        }
        for(var k in map2){
            if(map2[k] != map1[k]){
                return false;
            }
        }
        return true;
    }

    //判断用不用打勾勾
    $scope.isSelected = function (key, value) {
        if ($scope.specification[key] == value) {
            return true;
        }
        return false;
    }
    //页面一点击加载默认的sku
    $scope.loadSku=function () {
        $scope.sku=skuList[0];
        $scope.specification=JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //添加方法到购物车
    $scope.addGoodsToCartList=function () {
        //传递什么数据
        // alert("skuid"+$scope.sku.id+"数量  "+$scope.num);
    $http.get('http://localhost:9107/cart/addGoodstoCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(function (response) {
        if(response.success){
            //成功后跳转到购物车页面
            location.href='http://localhost:9107/cart.html';
        }else{
            alert(response.message);
        }
    });
    }
});