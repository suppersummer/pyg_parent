//服务层
app.service('loginService',function($http){
	//获取当前登录名
	    this.getLoginUser=function () {
			return $http.get('../getLoginUser.do');
        }

});
