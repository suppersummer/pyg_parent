app.service('seckillGoodsService',function ($http) {
    //加载秒杀商品
    this.findList=function () {
        return $http.get('../seckillGoods/findList.do');
    }

    //加载商品详情页
    this.loadSeckillGoods=function (id) {
        return $http.get('../seckillGoods/loadSeckillGoods.do?id='+id);
    }

    //提交秒杀订单
    this.submitOrder=function (id) {
        return $http.get('../seckillOrder/submitOrder.do?id='+id);
    }
})