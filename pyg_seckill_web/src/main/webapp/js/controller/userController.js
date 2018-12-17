//控制层
app.controller('userController', function ($scope, userService) {




    //用户注册
    $scope.reg = function () {
        //前端校验工作   用户名，密码不为空，两次密码是否相同，手机号是否符合标准。。。。
        //验证码是否相同
        if ($scope.entity.phone == null || $scope.entity.phone == '') {
            alert("电话号不能为空");
            return;
        }
        if ($scope.entity.password != $scope.password) {
            alert("两次输入的密码不一致，请重新输入。。");
            return;
        }
        if ($scope.smscode == null || $scope.smscode == '') {
            alert("验证码不能为空");
            return;
        }
        userService.add($scope.entity, $scope.smscode).success(function (response) {
            alert(response.message)
        })
    }
    //发送验证码
    $scope.entity={username:'',phone:''};
    $scope.sendCode = function () {
        if ($scope.entity.phone == null || $scope.entity.phone == '') {
            alert("电话号不能为空");
            return;
        }
        userService.sendCode($scope.entity.phone).success(
            function (response) {
                alert(response.message)
            })
    }
});	
