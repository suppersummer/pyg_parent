app.service('cartService',function($http){
    //查找购物车列表
    this.findcartlist=function () {
      return $http.get('../cart/findcartlist.do');
  }
    //添加删除购物车数据
    this.addGoodstoCartList=function (itemId,num) {
        return $http.get('../cart/addGoodstoCartList.do?itemId='+itemId+'&num='+num);
    }
    //获取地址列表
    this.findAddressList=function () {
        return $http.get('../address/findListByUser.do');
    }
    //生成order订单
    this.submitOrder=function (order) {
        return $http.post('../order/add.do',order);
    }
});