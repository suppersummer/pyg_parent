//控制层
app.controller('loginController', function ($scope, loginService) {



    $scope.getLoginUser=function () {
        loginService.getLoginUser().success(function (response) {
            $scope.name=response.username;
        })
    }






});	
