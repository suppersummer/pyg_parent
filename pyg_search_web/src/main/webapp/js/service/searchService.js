//服务层
app.service('searchService',function($http){
	    	
	//获取某一类型的广告列表
	this.search=function(searchMap){
		return $http.post('../item/search.do',searchMap);
	}

});
