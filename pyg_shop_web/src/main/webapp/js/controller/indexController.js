app.controller('indexController',function ($scope,loginService) {
    //controller方法中一定要传controller
    $scope.loginName=function () {
        //调用方法时，记得打括号
        loginService.loginName().success(function (response) {
            $scope.nickName=response.nickName;

        })
    }
})