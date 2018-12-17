 //控制层 ,注意：service还需要被注入html
app.controller('typeTemplateController' ,function($scope,$controller ,brandService,specificationService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承

	//品牌下拉列表数据
    $scope.brandList={data:[]};
	//规格下拉列表数据scopeList={data:[{id:1,text:'联想1'},{id:2,text:'华为1'},{id:3,text:'小米1'}]},是一个List<Map>或List<pojo>
	//要从后台传回这样的数据需要，在mapper中封装数据
    $scope.specList={data:[]};
    //定义两个方法获取brand和scope的数据
	$scope.findBrandList=function () {
		brandService.selectOptionList().success(function (response) {
            $scope.brandList.data=response;
        })
    }
    $scope.findSpecList=function () {
        specificationService.selectOptionList().success(function (response) {
            $scope.specList.data=response;
        })
    }

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	//提取json字符串数据中某个属性，，返回拼接字符串，逗号隔开
	$scope.jsonToString=function (jsonString, key) {
		var json=JSON.parse(jsonString);
		var value="";
		for(var i=0;i<json.length;i++){
			if(i>0){
				value+=",";
			}
			value+=json[i][key];
		}
		return value;
    }

	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;//数据库找到的品牌和规格是字符串形式，需要转成json格式，数据通过angular从前台获取来
				$scope.entity.brandIds=JSON.parse($scope.entity.brandIds);//转换品牌列表
				$scope.entity.specIds=JSON.parse($scope.entity.specIds);//转换规格列表

			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		typeTemplateService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});	
