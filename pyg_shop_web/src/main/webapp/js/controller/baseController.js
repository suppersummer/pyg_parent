app.controller('baseController',function ($scope) {

    //定义分页工具条参数
    $scope.paginationConf={
        currentPage: 1,
        totalItems: 0,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();
        }
    }

    //刷新页面
    $scope.reloadList=function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    $scope.selectIds=[];
    //当勾选复选框添加id元素，取消勾选，移除id元素
    $scope.updateSelection=function($event,id){
        if($event.target.checked){//true 勾选
            $scope.selectIds.push(id);
        }else{//取消勾选，移除元素
            var index=$scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }
    }
})