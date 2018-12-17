//服务层
app.service('contentService',function($http){
	    	
	//获取某一类型的广告列表
	this.findListByCategoryId=function(categoryId){
		return $http.get('../content/findListByCategoryId.do?categoryId='+categoryId);
	}

	//跳转到搜索页面

});
