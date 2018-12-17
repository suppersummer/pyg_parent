 //控制层 
app.controller('contentController' ,function($scope,contentService){
$scope.contentList=[];//用数组接收分类的广告，用分类的id做下标
//获取某一类型的广告列表
$scope.findListByCategoryId=function (categoryId) {
    contentService.findListByCategoryId(categoryId).success(function (response) {
		$scope.contentList[categoryId]=response;//用数组接收分类的广告，用分类的id做下标
    })
}
//将keywords传到搜索页面
    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
});
