var servicePath = "partial_on_shelf.do";

$(function(){
	$("#partial_on_shelf span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});
	
	$("#partial_on_shelf .ui-button").button();
	
	
	// 开始
	$("#startbutton").click(doStart);
	$("#breakbutton").click(doBreak);
	$("#endbutton").click(function(){
		warningConfirm("是否结束作业！",function(){
			doEnd();
		},function(){});
	});
	
	onShelfInit();
});

function reset(){
	list([]);
	$("#startbutton").enable().removeClass("ui-state-focus");
	$("#breakbutton,#endbutton").disable().removeClass("ui-state-focus");
	$("#label_warehouse_date,#label_dn_no").text("");
	
	$("#hide_key").val("");
	$("#kind_quantity").find("tbody tr:nth-child(n+2)").remove();
	
	$("#partial_details").hide();
	$("#partial_details td:eq(1),#partial_details td:eq(3)").text("");
	$("#dtl_process_time label").text("");
	clearInterval(oInterval);
	oInterval = null;
	$("#p_rate div:animated").stop();
	p_time = 0;
	leagal_overline = null;
};

function onShelfInit(){
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
					//现品作业信息
					var fact_production_feature = resInfo.unfinish;
					
					if(fact_production_feature){
						$("#startbutton").disable().removeClass("ui-state-focus");
						$("#breakbutton,#endbutton").enable().removeClass("ui-state-focus");
						
						$("#label_warehouse_date").text(fact_production_feature.warehouse_date);
						$("#label_dn_no").text(fact_production_feature.dn_no);
						
						$("#hide_key").val(fact_production_feature.partial_warehouse_key);
						
						$("#kind_quantity").find("tbody tr:nth-child(n+2)").remove();
						
						//零件入库DN编号
						var partialWarehouseDnList = resInfo.partialWarehouseDnList;
						var content = "";
						
						partialWarehouseDnList.forEach(function(item,index){
							let dn_no = item.dn_no;
							let warehouse_date = item.warehouse_date;
							content += `<tr>
											<td class="td-content-text" style="text-align:left;" colspan="2">${warehouse_date}</td>
											<td class="td-content-text" style="text-align:left;" colspan="2">${dn_no}</td>
										</tr>`;
						});
						
						$("#kind_quantity tbody").append(content);
						
						
						list(resInfo.partialWarehouseDetailList);
						setSpecKindQuantity(resInfo.specKindQuantityList,resInfo.packList);
						enableMenu("onshelfbutton");
						
						setRate(fact_production_feature,resInfo.leagal_overline,resInfo.spent_mins);
					}else{
						enableMenu("");
						reset();
					}
				}
			}catch(e){}
		}
	});
};

function setRate(factProductionFeature,leagalOverline,spent_mins){
	$("#partial_details").show();
	//开始时间
	$("#partial_details td:eq(1)").text(factProductionFeature.action_time);
	leagal_overline = leagalOverline;
	
	var frate = parseInt(spent_mins / leagal_overline * 100);
	if (frate > 99) {
		frate = 99;
	}
	$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
	
	p_time = spent_mins;
	
	//作业标准时间
	$("#partial_details td:eq(3)").text(minuteFormat(leagalOverline));
	ctime();
	clearInterval(oInterval);
	oInterval = null;
	oInterval = setInterval(ctime,iInterval);
};

function doStart(){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=searchUnOnShelf',
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
					// 零件入库单信息
					var partialWarehouseList = resInfo.partialWarehouseList || [];
					var length = partialWarehouseList.length;
					if(length == 0){
						reset();
						infoPop("没有待上架零件入库单！");
						return;
					}
					
					if(length == 1){
						$("#hide_key").val(partialWarehouseList[0].key);
						choose();
					}else{
						setPartialWarehouse(partialWarehouseList);
					}
				}
			}catch(e){}
		}
	});
};

function doBreak(){
	var data = getUpdateData();
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=checkQuantity',
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
					confirmBreak(data);
				}
			} catch (e) {}
		}
	});
};

function doEnd(){
	var data = getUpdateData();
	data["endFlg"] = "1";
	data["step"] = "3";
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=checkQuantity',
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
					confirmEnd(data);
				}
			} catch (e) {}
		}
	});
};

function setPartialWarehouse(list){
	var $dialog = $("#partial_warehouse_dialog");
	
	var content = `<div class="ui-widget-content">
						<table class="condform">
							<thead>
								<tr>
									<th class="ui-state-default td-title"></th>
									<th class="ui-state-default td-title">入库单编号</th>
									<th class="ui-state-default td-title">DN 编号</th>
								</tr>
							</thead>
							<tbody>`;
	list.forEach(function(item,index){
		var key = item.key,
		warehouse_no = item.warehouse_no,
		dn_no = item.dn_no;
		content +=`<tr key="${key}">
			<td class="td-content"><input type="button" class="ui-button" value="选择"></td>
			<td class="td-content">${warehouse_no}</td>
			<td class="td-content">${dn_no}</td>
		</tr>`;
	});
	content += `</tbody></table></div>`;
	
	$dialog.html("").hide().append(content).find(".ui-button").button();
	
	$dialog.dialog({
		resizable : false,
		modal : true,
		title : "请选择零件入库单",
		width : 400,
		buttons : {
			"取消" : function() {
				$(this).dialog("close");
			} 
		}
	});
	
	$dialog.find(".ui-button").removeClass("ui-state-focus").each(function(){
		$(this).click(function(){
			var key = $(this).closest("tr").attr("key");
			$("#hide_key").val(key);
			$dialog.dialog("close");
			choose();
		});
	});
};

function choose(){
	var data = {
		"production_type" : "40",//作业内容 D：上架
		"partial_warehouse_key" : $("#hide_key").val().trim()
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
					onShelfInit();
				}
			} catch (e) {}
		}
	});
};

function setSpecKindQuantity(list,packList){
	var content = `<tr>
						<td class="ui-state-default td-title">规格种别</td>
						<td class="ui-state-default td-title">核对总数</td>
						<td class="ui-state-default td-title">上次上架数量</td>
						<td class="ui-state-default td-title">本次上架数量</td>
					</tr>`;
	list.forEach(function(item,index){
		var spec_kind = item.spec_kind;
		var spec_kind_name = item.spec_kind_name;
		var collation_quantity = item.collation_quantity;
		
		content +=`<tr spec_kind = "${spec_kind}">
						<td class="ui-state-default td-title">${spec_kind_name}</td>
						<td class="td-content" total_split_quantity = "${collation_quantity}">${collation_quantity}</td>
						<td class="td-content quantity"></td>
						<td class="td-content"><input type="text" class="ui-widget-content"></td>
				   </tr>`;
	});
	
	$("#kind_quantity tbody").append(content);
	
	packList.forEach(function(item,index){
		var spec_kind = item.spec_kind;
		var quantity = item.quantity;
		$("#kind_quantity tr[spec_kind='" + spec_kind + "']").find("td.quantity").attr("split_quantity",quantity).text(quantity);
	});
};

function getUpdateData(){
	var data = {};
	$("#kind_quantity tbody tr[spec_kind]").each(function(index,tr){
		var $tr = $(tr);
		
		data["fact_partial_warehouse.spec_kind[" + index + "]"] = $tr.attr("spec_kind");
		data["fact_partial_warehouse.total_split_quantity[" + index + "]"] = $tr.find("td[total_split_quantity]").attr("total_split_quantity");
		data["fact_partial_warehouse.split_quantity[" + index + "]"] = $tr.find("td[split_quantity]").attr("split_quantity") || "0";
		data["fact_partial_warehouse.quantity[" + index + "]"] = $tr.find("input[type='text']").val().trim();
	});
	
	return data;
};

function confirmEnd(data){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doFinish',
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
					onShelfInit();
				}
			} catch (e) {}
		}
	});
};

function confirmBreak(data){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doBreak',
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
					onShelfInit();
				}
			} catch (e) {}
		}
	});
};


function list(listdata){
	if ($("#gbox_onshelflist").length > 0) {
		$("#onshelflist").jqGrid().clearGridData();// 清除
		$("#onshelflist").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
	} else {
		$("#onshelflist").jqGrid({
			data : listdata,// 数据
			height :461,// rowheight*rowNum+1
			width : 1178,
			rowheight : 23,
			shrinkToFit:true,
			datatype : "local",
			colNames : ['','','零件编码','零件名称','规格种别','入库数量'],
			colModel : [{name : 'key',index : 'key',hidden : true},
			            {name : 'partial_id',index : 'partial_id',hidden : true},
			            {name : 'code',index : 'code',width:200},
			            {name : 'partial_name',index : 'partial_name',width:200},
			            {name : 'spec_kind_name',index : 'spec_kind_name',width:200},
			            {name : 'quantity',index : 'quantity',width:200,align:'right'},
			],
			rowNum : 20,
			toppager : false,
			pager : "#onshelflistpager",
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
