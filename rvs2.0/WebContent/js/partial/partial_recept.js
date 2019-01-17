var servicePath = "partial_recept.do";
$(function() {
	$("#partial_recept span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});
	
	$("#partial_recept .ui-button").button();

	// 开始
	$("#startbutton").click(doStart);

	// 结束
	$("#endbutton").click(function(){
		var warningMessage = "是否还要补输入收货清单或修正清单？";
		var yesMessage = "不需要补充";
		if ($("#content tr").length < 2) {
			warningMessage = "此次收货作业中没有导入收货清单，就这样结束作业吗？";
			yesMessage = "以后到入库单管理补充";
		}
		// 是否结束收货作业？
		warningConfirm(warningMessage,function(){
			doEnd();
		},function(){}, "确认将结束收货作业", yesMessage, "不结束，去补充");
	});

	//载入
	$("#uploadbutton").click(uploadfile);
	
	$("#restartbutton").click(function(){
		warningConfirm("是否重新导入？",function(){
			doReImport();
		},function(){});
	})
	
	receptInit();
});

function doReImport(){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doReImport',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					receptInit()
				}
			}catch(e){}
		}
	});
};

function receptInit(){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=jsinit',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					//正在进行中的现品作业信息
					var factProductionFeature = resInfo.factProductionFeature;
					
					//零件入库明细
					var partialWarehouseDetailList = resInfo.partialWarehouseDetailList || [];

					//没有进行中作业信息
					if(!factProductionFeature){
						reset();
						enableMenu();
					}else{
						$("#file").val("");
						
						//开始按钮不可用
						$("#startbutton").disable();
						
						//结束按钮，重新导入按钮可以用
						$("#endbutton,#restartbutton").enable();
						
						//零件入库单号
						var partial_warehouse_key = factProductionFeature.partial_warehouse_key;

						//存在入库单号
						if(partialWarehouseDetailList.length > 0){
							//选择文件按钮，导入按钮不可用
							$("#file,#uploadbutton").disable();
						}else{
							//选择文件按钮，导入按钮可用
							$("#file,#uploadbutton").enable();
						}
						
						$("#content tbody tr:nth-child(n+2)").remove();
						
						//零件入库DN编号
						var partialWarehouseDnList = resInfo.partialWarehouseDnList;
						
						if(partialWarehouseDnList && partialWarehouseDnList.length > 0){
							var content = "";
							
							partialWarehouseDnList.forEach(function(item,index){
								let dn_no = item.dn_no;
								let warehouse_date = item.warehouse_date;
								content += `<tr>
												<td class="td-content-text" style="text-align:left;">${warehouse_date}</td>
												<td class="td-content-text" style="text-align:left;">${dn_no}</td>
											</tr>`;
							});
							
							$("#content tbody").append(content);
						}
						
						//各个规格种别总数量
						if(resInfo.counttQuantityList && resInfo.counttQuantityList.length > 0){
							var counttQuantityList = resInfo.counttQuantityList;
							var content = "";
							counttQuantityList.forEach(function(item,index){
								let specKindName = item.spec_kind_name;
								let quantity = item.quantity;
								content += `<tr>
												<td class="ui-state-default td-title">${specKindName}</td>
												<td class="td-content">${quantity} 箱</td>
											</tr>`;
							});
							
							$("#content tbody").append(content);
						}
						
						list(partialWarehouseDetailList);
						setRate(resInfo);
						
						enableMenu("receptbutton");
					}
				}
			}catch(e){}
		}
	});
};

function reset(){
	$("#file").val("");
	$("#file,#uploadbutton,#endbutton,#restartbutton").disable().removeClass("ui-state-focus");
	$("#startbutton").enable().removeClass("ui-state-focus");
	$("#content tbody tr:nth-child(n+2)").remove();
	list([]);
	
	$("#partial_details").hide();
	$("#partial_details td:eq(1),#partial_details td:eq(3)").text("(缺入库单信息)");
	$("#dtl_process_time label").text("");
	clearInterval(oInterval);
	oInterval = null;
	$("#p_rate div:animated").stop();
	p_time = 0;
	leagal_overline = null;
};

function setRate(resInfo){console.log(resInfo.factProductionFeature.action_time)
	let leagalOverline = resInfo.leagal_overline;
	let spent_mins = resInfo.spent_mins;
	
	$("#partial_details").show();
	//开始时间
	$("#partial_details td:eq(1)").text(resInfo.factProductionFeature.action_time);
	p_time = spent_mins;
	if(resInfo.partialWarehouseDetailList){
		leagal_overline = leagalOverline;
		//作业标准时间
		$("#partial_details td:eq(3)").text(minuteFormat(leagalOverline));
		
		var frate = parseInt(spent_mins / leagal_overline * 100);
		if (frate > 99) {
			frate = 99;
		}
		$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
		
		ctime();
		clearInterval(oInterval);
		oInterval = null;
		oInterval = setInterval(ctime,iInterval);
	}else{
		$("#p_rate").html("<div class='tube-liquid tube-green' style='width:0%;text-align:right;'></div>");
		
//		if(p_time == 0) p_time = 1;
		$("#dtl_process_time label").text(minuteFormat(p_time));
		clearInterval(oInterval);
		oInterval = null;
		oInterval = setInterval(function(){
			p_time++;
			$("#dtl_process_time label").text(minuteFormat(p_time));
		},iInterval);
		$("#partial_details td:eq(3)").text("(缺入库单信息)");
		$("#p_rate div:animated").css("width","0%").stop();
	}
};

function doStart() {
	var data={
		"production_type":"10"//作业内容 A：收货
	};

	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'fact_production_feature.do?method=doStart',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					receptInit();
				}
			} catch (e) {}
		}
	});
};

function doEnd() {
	execEnd();
//	$.ajax({
//		beforeSend : ajaxRequestType,
//		async : true,
//		url : 'fact_production_feature.do?method=jsinit',
//		cache : false,
//		data : null,
//		type : "post",
//		dataType : "json",
//		success : ajaxSuccessCheck,
//		error : ajaxError,
//		complete : function(xhrobj,textStatus){
//			var resInfo = null;
//			try {
//				// 以Object形式读取JSON
//				eval('resInfo =' + xhrobj.responseText);
//				if (resInfo.errors.length > 0) {
//					// 共通出错信息框
//					treatBackMessages(null, resInfo.errors);
//				} else {
//					//现品作业信息
//					var factProductionFeature = resInfo.unfinish;
//					if(!factProductionFeature.partial_warehouse_key){
//						warningConfirm("货物到达验收确认单未导入，结束将不记录作业！",function(){
//							doDelete();
//						},function(){});
//					}else{
//						execEnd();
//					}
//				}
//			} catch (e) {}
//		}
//	});
};

function doDelete(){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'fact_production_feature.do?method=doDelete',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					reset();
					enableMenu();
				}
			} catch (e) {}
		}
	});
}


function execEnd(){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doFinish',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					reset();
					enableMenu();
				}
			} catch (e) {}
		}
	});
};

function uploadfile() {
	$.ajaxFileUpload({
		url : servicePath + '?method=doUpload', // 需要链接到服务器地址
		secureuri : false,
		fileElementId : 'file', // 文件选择框的id属性
		dataType : 'json', // 服务器返回的格式
		success : function(responseText, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					receptInit();
				}
			} catch (e) {
			}
		}
	});
};

function list(listdata){
	if ($("#gbox_receptlist").length > 0) {
		$("#receptlist").jqGrid().clearGridData();// 清除
		$("#receptlist").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
	} else {
		$("#receptlist").jqGrid({
			data : listdata,// 数据
			height :461,// rowheight*rowNum+1
			width : 1178,
			rowheight : 23,
			shrinkToFit:true,
			datatype : "local",
			colNames : ['','','日期','DN 编号','零件编号','零件名称','规格种别','数量'],
			colModel : [{name : 'key',index : 'key',hidden : true},
			            {name : 'partial_id',index : 'partial_id',hidden : true},
			            {name : 'warehouse_date',index : 'warehouse_date',width:60,align:'center'},
			            {name : 'dn_no',index : 'dn_no',width:100},
			            {name : 'code',index : 'code',width:200},
			            {name : 'partial_name',index : 'partial_name',width:200},
			            {name : 'spec_kind_name',index : 'spec_kind_name',width:200},
			            {name : 'quantity',index : 'quantity',width:200,align : 'right'},
			],
			rowNum : 20,
			toppager : false,
			pager : "#receptlistpager",
			viewrecords : true,
			caption : "",
			multiselect : false,
			gridview : true,
			pagerpos : 'right',
			pgbuttons : true, // 翻页按钮
			rownumbers : true,
			pginput : false,					
			recordpos : 'left',
			hidegrid : false,
			deselectAfterSort : false,
			onSelectRow : null,// 当选择行时触发此事件。
			ondblClickRow : function(rid, iRow, iCol, e) {
			},
			viewsortcols : [ true, 'vertical', true ],
			gridComplete : function() {
			}
		});
	}
};