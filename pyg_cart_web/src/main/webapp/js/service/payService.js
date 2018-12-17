app.service('payService',function ($http) {
    //获取预支付url
    this.createNative=function () {
        return $http.post('../pay/createNative.do');
    }
    this.queryOrderStatus=function (out_trade_no) {
        return $http.get('../pay/queryOrderStatus.do?out_trade_no='+out_trade_no);
    }
})