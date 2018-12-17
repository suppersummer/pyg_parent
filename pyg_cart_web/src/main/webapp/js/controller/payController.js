app.controller('payController',function ($scope,$location, payService) {
    //生成二维码
 $scope.createNative=function () {
     payService.createNative().success(//订单号，总金额，预支付url
         function (responce) {
             $scope.out_trade_no=responce.out_trade_no;
             $scope.total_fee=(responce.total_fee /100).toFixed(2);

             //生成二维码
             var qr = window.qr = new QRious({
                 element: document.getElementById('17ewm'),
                 size: 250,
                 level:'H',
                 value: responce.code_url
             });

         //立马开始查询订单支付状态
             $scope.queryOrderStatus($scope.out_trade_no);
         }
     )
 }
    //查询订单支付状态
    $scope.queryOrderStatus=function (out_trade_no) {
        payService.  queryOrderStatus(out_trade_no).success(
            function (responce) {
        //支付成功，跳转到支付成功页面
                if (responce.success){
                    location.href='paysuccess.html?money='+total_fee;
                }else{
                    //超时
                    if(responce.message=="pay_time_out"){
                        alert("支付超时,点击确定重新生成。。。");
                        $scope.createNative();
                    }
                    //支付失败，跳转到支付失败页面
                   location.href='payfail.html';
                }

            })

    }

    //获取成功页面的money
    $scope.getMoney=function () {
       $scope.money= $location.search()["money"];
    }

})