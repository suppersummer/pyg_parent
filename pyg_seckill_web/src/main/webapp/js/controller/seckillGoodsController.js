app.controller('seckillGoodsController',function ($scope,$location,$interval, seckillGoodsService) {
    //查询秒杀商品列表
    $scope.findList=function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.seckillGoodsList=response;
            }
        )
    }

    //加载商品详情页 1.获取url上的id
    $scope.loadSeckillGoods=function () {
        var id=$location.search()['id'];
        //调用服务获取商品详情信息
        seckillGoodsService.loadSeckillGoods(id).success(
            function (response) {
                $scope.seckillGoods=response;

            //计算剩余x天x小时x分钟x秒
            // 先计算剩余抢购多少秒
            var  second =  Math.floor((new Date($scope.seckillGoods.endTime).getTime()-new Date().getTime())/1000);
            var name= $interval(function () {
               if (second>0){
                   second-=1;
                   $scope.timetitle=convertTimeString(second);
               }else{
                   $interval.cancel(name);
               }

            },1000);

            }
        )

    }

    //计算读秒操作
    //转换秒为  XXX天 10:22:33
    convertTimeString=function(allsecond){
        var day=Math.floor(allsecond/(60*60*24));
        var hours=Math.floor((allsecond-day*60*60*24)/(60*60))
        var min=Math.floor( (allsecond -day*60*60*24 - hours*60*60)/60);
        var seconds= allsecond -day*60*60*24 - hours*60*60 -min*60;//秒数
        var timeString="";
        if (day>0){
            timeString=day+"天";
        }
        return timeString+hours+":"+min+":"+seconds;
    }

    //提交秒杀订单
    $scope.submitOrder=function (id) {
        seckillGoodsService.submitOrder(id).success(
            function (response) {
                if (response.success){
                    location.href="pay.html";
                }else{
                    alert(response.message);
                }
            }
        )
    }
})