app.service("uploadService",function ($http) {
    this.uploadFile=function () {
        var formData=new FormData();//创建一个新表单,h5提供了把表单对象
        formData.append("file",file.files[0]);//"file":上传项名,file：id名.files[0]：可上传多个所以是个数组
        return $http({
            url:"../uploadFile.do",
            method:'POST',
            data:formData,
            headers: {'Content-Type':undefined},//能将Content-Type 设置为 multipart/form-data.
            transformRequest: angular.identity//将序列化我们的formdata object.
        })
    }
})