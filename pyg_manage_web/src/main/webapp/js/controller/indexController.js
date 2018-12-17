app.controller('indexController',function ($scope,$controller,loginService) {
    //controller方法中一定要传controller
    $scope.loginName=function () {
        //调用方法时，记得打括号
        loginService.loginName().success(function (response) {
            $scope.loginName=response.loginName;
        })
    }
})