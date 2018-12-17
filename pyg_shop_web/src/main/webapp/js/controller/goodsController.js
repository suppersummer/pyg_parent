 //控制层 
app.controller('goodsController' ,function($scope,$controller ,$location ,uploadService ,typeTemplateService,itemCatService,goodsService){
	
	$controller('baseController',{$scope:$scope});//继承
//添加的商品
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]}
    $scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态
	$scope.itemCatList=[];
	//加载商品分类列表
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(function (response) {
			for(var i=0;i<response.length;i++){
				$scope.itemCatList[response[i].id]=response[i].name;
			}
        })
    }
    //创建sku列表
    $scope.createItemList=function () {
    	    $scope.entity.itemList= [{spec:{},price:0,num:99999,status:'1',isDefault:'0'}];
        var items=$scope.entity.goodsDesc.specificationItems;
        for(var i=0;i<items.length;i++){
            $scope.entity.itemList= addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
    }
    //添加规格列表值的方法name:attributeName value:attributeValue
	addColumn=function (list,name,value) {
		var newList=[];
		for (var i=0;i<list.length;i++){
			var oldL=list[i];
            for (var j=0;j<value.length;j++){
            	var newL=JSON.parse(JSON.stringify(oldL));
            	newL.spec[name]=value[j];
            	newList.push(newL);
			}
		}
		return newList;
    }
	//规格选项的勾选结果 $scope.entity.goodsDesc.specificationItems
	//[{“attributeName”:”规格名称”,”attributeValue”:[“规格选项1”,“规格选项2”.... ]  } , ....  ]
	//勾选复选框添加数据，如果被勾选过添加元素，没选过添加对象name规格名称,value选项名称
	//name 规格名称 value选项名称
	$scope.ischecked=function (name, value) {
        var specItems=$scope.entity.goodsDesc.specificationItems;//勾选结果
		//用规格名称判断是否勾选过
		var object=selectObjectByKey(specItems,name,'attributeName');
		if(object!=null){//该规格有选项被勾选
			if(object.attributeValue.indexOf(value)>=0){
				return true;
			}else {return false;}
		}
    else{//该规格有选项被勾选
		return false;
			 	 }
    }

$scope.updataSpecAttribute=function ($event,name,value) {
	var specItems=$scope.entity.goodsDesc.specificationItems;
   var object= selectObjectByKey(specItems,name,"attributeName");
   if(object!=null){//该规格被勾选过
        if($event.target.checked){
            object.attributeValue.push(value);
		}else {
            object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
            if (object.attributeValue.length == 0) {
                specItems.splice(specItems.indexOf(object), 1);
            }
		      }
   }else{//该规格没被勾选过
	specItems.push({"attributeName":name,"attributeValue":[value]});
   }
}
	//判断规格选项是否被勾选过list:勾选结果,name规格名称
	selectObjectByKey=function (list,name,key) {
		for(var i=0;i<list.length;i++){
			if(list[i][key]==name){
				return list[i];//返回的是一个未勾选完的勾选结果$scope.entity.goodsDesc.specificationItems;
			}
		}
		return null;
    }
//图片上传
	$scope.uploadFile=function () {
        uploadService.uploadFile().success(function (response) {
			if(response.success){
				$scope.image_entity.url=response.message;
				document.getElementById("file").value='';
			}else{
				alter(response.message);
			}
        })
    }
    //添加图片列表
	$scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }
    //移除图片
	$scope.remove_image_entity=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    //读取一级分类
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(function (response) {
			$scope.itemCat1List=response;
        })
    }
    //读取二级分类
	$scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
		//观察，根据选择的值进行二级分类
		if(newValue!=undefined){
		itemCatService.findByParentId(newValue).success(function (response) {
			$scope.itemCat2List=response;
            // $scope.itemCat3List=[];
        })}
    })
    //读取三级分类
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        //观察，根据选择的值进行二级分类
        if(newValue!=undefined){
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat3List=response;

        })}
    })
	//根据三层分类的id 读取模板id
    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        if(newValue!=undefined){
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId=response.typeId;
        })}
    })

    $scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {
        if(newValue!=undefined){
    	//监控模板ID  更新品牌列表
        typeTemplateService.findOne(newValue).success(
            function(response){
                $scope.typeTemplate=response;//获取类型模板
                $scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表

            }
        );
        //监控模板ID  获取规格数据
        typeTemplateService.findSpecByTypeId(newValue).success(function (response) {
			$scope.specList=response;
        })}

    })
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id=$location.search()["id"];
		if(id!=undefined){
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				editor.html($scope.entity.goodsDesc.introduction);
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
               for (var i=0;i<$scope.entity.itemList.length;i++){
                   $scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec)
               }
			}
		);}
	}
	
	//保存 
	$scope.save=function(){
		var serviceObject;//服务层对象
		//提取kindeditor编辑器的内容
		$scope.entity.goodsDesc.introduction=editor.html();
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	//$scope.reloadList();//重新加载
                    // $scope.entity.goodsDesc.introduction=editor.html();
                    // $scope.entity={goods:{},goodsDesc:{},itemList:[]}
                  location.href="goods.html"
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 (goods表里的部分内容)
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});
