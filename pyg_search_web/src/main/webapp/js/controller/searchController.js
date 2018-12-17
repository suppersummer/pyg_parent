 //控制层 
app.controller('searchController' ,function($scope,$location,searchService){

    $scope. loadSearch=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();
    }


    //封装查询条件
    $scope.searchMap={'keywords':'','category':'','price':'','brand':'','spec':{},'pageNo':1,'pageSize':20,'sortField':'','sort':''};//spec规格列表有好几个

$scope.search=function () {
    $scope.searchMap.pageNo=parseInt( $scope.searchMap.pageNo);
    searchService.search($scope.searchMap).success(function (response) {
        //得到resultMap
        $scope.resultMap=response;

       // $scope.searchMap.pageNo=parseInt( $scope.searchMap.pageNo);
        //分页处理--------显示哪几页
        $scope.buildPageLable();

    })
}

//分页处理--------显示哪几页
    $scope.buildPageLable=function () {


        //构建分页标签
        var startPage=1;
        var endPage= $scope.resultMap.totalPages;
        var pageNo=$scope.searchMap.pageNo;

        //前后点
        $scope.startDot=false;
        $scope.lastDot=false;

        $scope.pageList=[];
        //分页显示，1，如果总页数小于5就显示全部   2.如果当前页小于3，则显示前5页   3.如果当前页大于endPage-2就显示最后5页  4.其余显示当前页pageNo，前两页和两页
        if(endPage<=5){
            for(var i=startPage;i<=endPage;i++){
                $scope.pageList.push(i);
            }
        }else if(pageNo<=3) {//判断是不是前两页
            for (var i = startPage; i <= 5; i++) {
                $scope.pageList.push(i); }
                $scope.startDot=false;
                $scope.lastDot=true;

        }else if (pageNo >= endPage - 2) {//判断是不是最后两页
            for (var i = endPage - 4; i <= endPage; i++) {
                $scope.pageList.push(i); }
                $scope.startDot=true;
                $scope.lastDot=false;

        } else {//当前页在中间的情况
            for (var i = pageNo - 2; i <= pageNo + 2; i++) {
                $scope.pageList.push(i); }
                $scope.startDot=true;
                $scope.lastDot=true;

        }

    }


//点击页面，分页显示列表
    $scope.queryByPage=function (page) {
        page=parseInt( page);
        if(page<1 ){
            page=1;
        }
         if(page>$scope.resultMap.totalPages){
            page=$scope.resultMap.totalPages;
        }
        $scope.searchMap.pageNo=page;
        $scope.search();
    }

//添加搜索选项的方法
$scope.addSearchItem=function (key, value) {
    //先判断传入的是brand/category/price   还是spec
    if(key=='brand' || key=='category' || key=='price' ){//传入的是brand/category/price
        $scope.searchMap[key]=value;
    }else{//传入的是spec
        $scope.searchMap.spec[key]=value;
    }
    $scope.search();
}

    $scope.removeSearchItem=function (key) {
        //先判断传入的是brand/category/price   还是spec
        if(key=='brand' || key=='category' || key=='price' ){//传入的是brand/category/price
            $scope.searchMap[key]='';
        }else{//传入的是spec
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }

    //排序
    $scope.sortByOrder=function (sortField, sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;

        //重新搜索
        $scope.search();
    }

    //品牌列表的隐藏
    $scope.keywordsIsBrand=function () {
        //1.获取关键字 2.判断是否包含品牌中的字段，包含则隐藏品牌，并在searchMap.brand中添加该品牌,并返回一个true，不包含返回false 3.在搜索click中添加该方法
       var keywords= $scope.searchMap.keywords;
       var brandList= $scope.resultMap.brandList;
       for(var i=0;i<brandList.length;i++){
           if(keywords.indexOf(brandList[i].text)>=0){
               $scope.searchMap.brand=brandList[i].text;
               return true;
           }
       }
       return false;
    }

});



