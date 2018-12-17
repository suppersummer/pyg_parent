app.controller('cartController' ,function($scope,cartService){
 //查找购物车列表
 $scope.findcartlist=function () {
     cartService.findcartlist().success(function (responce) {
         $scope.cartList=responce;
         
         //计算总金额，总数量
         $scope.total={'totalNum':0,'totalMoney':0.0};

         for(var i=0;i<$scope.cartList.length;i++){
             var car=$scope.cartList[i];
             for(var j=0;j<car.orderItemList.length;j++){
                 $scope.total.totalMoney+=car.orderItemList[j].totalFee;
                 $scope.total.totalNum+=car.orderItemList[j].num;
             }
         }

     });
 }
//添加删除购物车数据
 $scope.addGoodstoCartList=function (itemId,num) {
     cartService.addGoodstoCartList(itemId,num).success(function (responce) {
        if (responce.success){//成功则刷新
            $scope.findcartlist();

        }else{
            alert(responce.message);
        }
     })
 }
 //----------------getOrderInfo页面-------------------------------------------------------------
    //显示地址信息address
    $scope.findAddressList=function () {
        cartService.findAddressList().success(function (responce) {
            //返回的是一个地址列表， 直接接收
            $scope.addressList=responce;

       //显示寄送的默认地址
            for(var i=0;i<$scope.addressList.length;i++){
                if($scope.addressList[i].isDefault=='1'){
                    $scope.selectedAddress=$scope.addressList[i];
                    return;
                }
            }


        })
    }
//---------------先点击触发selecteAddress方法，把address存起来，在循环address时isselected方法拿当前的address与存起来的作比较----------------
    //选择地址
    $scope.selecteAddress=function (address) {
        $scope.selectedAddress=address;
    }

    //判断是否应该加样式
    $scope.isselected=function (address) {
        if ($scope.selectedAddress==address){
            return true;
        }
        return false;
    }

    //点击提交订单，生成order订单表（按商家分表）和订单详情orderItem表（按商品分表）
    //需要传递的信息1.支付方式paymentType 2.选择的地址$scope.selectedAddress （其他信息在redis和别的地方获取）
    $scope.order={'paymentType':'1'};
 //改变支付方式的方法
    $scope.changeType=function (type) {
        $scope.order.paymentType=type;
    }
//------提交订单的方法  提交传到后端的内容：收货地址，支付方式
    $scope.submitOrder=function () {
        //将收件人地址绑定到order中
        $scope.order.receiverAreaName=$scope.selectedAddress.address;
        $scope.order.receiver=$scope.selectedAddress.contact;
        $scope.order.receiverMobile=$scope.selectedAddress.mobile;
        cartService.submitOrder($scope.order).success(function (responce) {
            if(responce.success){//支付成功后判断是在线支付还是货到付款
                if($scope.order.paymentType=='1'){
                    location.href='pay.html';
                }else{
                    location.href='paysuccess.html';
                }

            }else{
                location.href='payfail.html';
            }
        })
    }

});