var servicePath = "device_spare.do";

$(function(){
	$("input.ui-button").button();
	
	/*为每一个匹配的元素的特定事件绑定一个事件处理函数*/
    $("#body-mdl span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});
    
    /*品名*/
	setReferChooser($("#search_device_type_id"),$("#device_type_id_referchooser"));
	setReferChooser($("#add_device_type_id"),$("#device_type_id_referchooser"));
	
	/*品牌*/
	setReferChooser($("#search_brand_id"),$("#device_brand_id_referchooser"));
	setReferChooser($("#add_brand_id"),$("#device_brand_id_referchooser"));
	setReferChooser($("#update_brand_id"),$("#device_brand_id_referchooser"));
	
	// 备品种类
	$("#search_device_spare_type,#add_device_spare_type").select2Buttons();
	
	// 需要订购
	$("#search_order_flg").buttonset();
	
	$("#search_adjust_time_start,#search_adjust_time_end").datepicker({
		showButtonPanel : true,
		dateFormat : "yy/mm/dd",
		currentText : "今天"
	});
	
	//清除
    $("#resetbutton").click(function(){
    	reset();
    });
    
    //检索
    $("#searchbutton").click(function(){
    	findit();
    });
    
    //取消新建
    $("#gobackbutton").click(function(){
    	$("#search").show();
    	$("#add").hide();
    });
    $("#add .ui-icon-circle-triangle-w").click(function(){
    	$("#search").show();
    	$("#add").hide();
    });

    //更新取消
    $("#goback2button").click(function(){
    	$("#search").show();
    	$("#update").hide();
    });
    $("#update .ui-icon-circle-triangle-w").click(function(){
    	$("#search").show();
    	$("#update").hide();
    });
    
    $("#add_device_spare_type").change(function(){
    	var value = this.value;
    	if(value == 1){//消耗备品
    		$("#add_order_cycle").closest("tr").show();
    		$("#add_location").closest("tr").hide();
    	} else if (value == 2){//备件
    		$("#add_location").closest("tr").show();
    		$("#add_order_cycle").closest("tr").hide();
    	} else {
    		$("#add_order_cycle,#add_location").closest("tr").show();
    	}
    });
    
    //取消管理
    $("#cancelbutton").click(cancelManage);
    
    //盘点
    $("#inventorybutton").click(stock);

   	$("#update_brand_detail_button").click(function(){
		if ($("#update_brand_id").val()) {
			showBrandDetail($("#update_brand_id").val());
		}
	});

    findit();
});

/**
 * 盘点
 * 
 */
function stock(){
	// 得到选中行的ID
	var rowID = $("#list").jqGrid("getGridParam", "selrow");
	var rowData = $("#list").getRowData(rowID);
	
	var this_dialog = $("#message_dialog");
	if (this_dialog.length === 0) {
		$("body.outer").append("<div id='message_dialog'/>");
		this_dialog = $("#message_dialog");
	}
	
	var content =`<div class="ui-widget-content">
					<table class="condform">
						<tr>
							<td class="ui-state-default td-title">品名</td>
					   		<td class="td-content">${rowData.device_type_name}</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">型号</td>
					   		<td class="td-content">${rowData.model_name}</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">备品种类</td>
					   		<td class="td-content">${rowData.device_spare_type_name}</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">当前有效库存</td>
					   		<td class="td-content">${rowData.available_inventory}</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">修正有效库存</td>
					   		<td class="td-content"><input id="inventory_adjust_inventory" type="text" class="ui-widget-content"></td>
						</tr>
						<tr>
						<td class="ui-state-default td-title">调整备注</td>
					   		<td class="td-content"><textarea id="inventory_comment" class="ui-widget-content" style="resize: none;" cols="50"></textarea></td>
						</tr>
					</table>
				  </div>`;
	
	this_dialog.html(content);
	this_dialog.dialog({
		title : "盘点",
		width : 450,
		height : 'auto',
		resizable : false,
		modal : true,
		buttons : {
			"确认":function(){
				var data = {
					"device_type_id" : rowData.device_type_id,
					"model_name" : rowData.model_name,
					"device_spare_type" : rowData.device_spare_type,
					"adjust_inventory":$("#inventory_adjust_inventory").val().trim(),
					"comment":$("#inventory_comment").val()
				};
				
				$.ajax({
					beforeSend : ajaxRequestType,
					async : true,
					url : servicePath + '?method=doStock',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : function(xhrobj, textStatus) {
						var resInfo = null;
						try {
							// 以Object形式读取JSON
							eval('resInfo =' + xhrobj.responseText);
							if (resInfo.errors.length > 0) {
								// 共通出错信息框
								treatBackMessages(null, resInfo.errors);
							} else {
								findit();
								this_dialog.dialog('close');
							}
						}catch(e){}
					}
				});
			},
			"取消":function(){
				this_dialog.dialog('close');
			}
		}
	});
};

/**
 * 取消管理
 * 
 */
function cancelManage(){
	// 得到选中行的ID
	var rowID = $("#list").jqGrid("getGridParam", "selrow");
	var rowData = $("#list").getRowData(rowID);
	
	var warnData = `确认取消管理品名为[${rowData.device_type_name}]，型号为[${rowData.model_name}]，备品种类为[${rowData.device_spare_type_name}]的记录？`;
	
	warningConfirm(warnData,function(){
		var data = {
			"device_type_id" : rowData.device_type_id,
			"model_name" : rowData.model_name,
			"device_spare_type" : rowData.device_spare_type
		};

		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : servicePath + '?method=doCancelManage',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj, textStatus) {
				var resInfo = null;
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					if (resInfo.errors.length > 0) {
						// 共通出错信息框
						treatBackMessages(null, resInfo.errors);
					} else {
						findit();
					}
				}catch(e){}
			}
		});
	},function(){},"取消管理");
};

/**
 * 清除 
 *
 */
function reset(){
	$("#searchform input[type='text']").val("");
	$("#searchform input[type='hidden']").val("");
	$("#searchform select").val("").trigger("change");
	$("#search_order_flg input[value='1']").attr("checked","checked").trigger("change");
};

function findit(){
	var data ={
		"device_type_id":$("#search_device_type_id").val(),
		"model_name":$("#search_model_name").val(),
		"device_spare_type":$("#search_device_spare_type").val(),
		"brand_id":$("#search_brand_id").val(),
		"order_flg":$("#search_order_flg input:checked").val(),
		"adjust_time_start" : $("#search_adjust_time_start").val(),
		"adjust_time_end" : $("#search_adjust_time_end").val()
	};
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=search',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					$("#consumables_benchmark_price").text(resInfo.totalPrice.consumables_benchmark_price + " RMB");
					$("#consumables_inventory_price").text(resInfo.totalPrice.consumables_inventory_price + " RMB");
					$("#part_inventory_price").text(resInfo.totalPrice.part_inventory_price + " RMB");
					list(resInfo.spareList);
					$("#cancelbutton,#inventorybutton").disable();
				}
			}catch(e){}
		}
	});
};

function showAdd(){
	$("#search").hide();
	$("#add").show();
	
	//初始化数据
	$("#addform input[type='text']").val("");
	$("#addform input[type='hidden']").val("");
	$("#addform textarea").val("");
	$("#addform select").val("").trigger("change");
	$("#add_order_cycle,#add_location").closest("tr").show();
	
	$("#newbutton").unbind("click").bind("click",function(){
		var data = {
			"device_type_id" : $("#add_device_type_id").val(),
			"model_name" : $("#add_model_name").val(),
			"device_spare_type" : $("#add_device_spare_type").val(),
			"order_cycle" : $("#add_order_cycle").val(),
			"brand_id" : $("#add_brand_id").val(),
			"price" : $("#add_price").val(),
			"safety_lever" : $("#add_safety_lever").val(),
			"benchmark" : $("#add_benchmark").val(),
			"available_inventory" : $("#add_available_inventory").val(),
			"location" : $("#add_location").val(),
			"comment" : $("#add_comment").val()
		};
		
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : servicePath + '?method=doInsert',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj, textStatus) {
				var resInfo = null;
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					if (resInfo.errors.length > 0) {
						// 共通出错信息框
						treatBackMessages(null, resInfo.errors);
					} else {
						findit();
						$("#search").show();
						$("#add").hide();
					}
				}catch(e){}
			}
		});
	});
};

function enableButton(){
	//得到选中行的ID	
	var rowID = $("#list").jqGrid("getGridParam", "selrow");
	if(rowID > 0){
		$("#cancelbutton,#inventorybutton").enable();
	}else{
		$("#cancelbutton,#inventorybutton").disable();
	}
};

function showEdit(){
	var rowID = $("#list").jqGrid("getGridParam", "selrow");// 得到选中行的ID	
	var rowData = $("#list").getRowData(rowID);
	
	var data = {
		"device_type_id" : rowData.device_type_id,
		"model_name" : rowData.model_name,
		"device_spare_type" : rowData.device_spare_type
	};
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=detail',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var deviceSpareForm = resInfo.deviceSpareForm;
					
					//备品种类
					var device_spare_type = deviceSpareForm.device_spare_type;
					
					$("#update_devices_type_name").text(deviceSpareForm.device_type_name);
					$("#update_model_name").text(deviceSpareForm.model_name);
					$("#update_device_spare_type_name").text(deviceSpareForm.device_spare_type_name);
					$("#update_order_cycle").val(deviceSpareForm.order_cycle);
					$("#update_brand_name").val(deviceSpareForm.brand_name);
					$("#update_brand_id").val(deviceSpareForm.brand_id);
					$("#update_price").val(deviceSpareForm.price);
					$("#update_safety_lever").val(deviceSpareForm.safety_lever);
					$("#update_benchmark").val(deviceSpareForm.benchmark);
					$("#update_available_inventory").text(deviceSpareForm.available_inventory);
					$("#update_total_benchmark_price").text(deviceSpareForm.total_benchmark_price);
					$("#update_total_available_price").text(deviceSpareForm.total_available_price);
					$("#update_location").val(deviceSpareForm.location);
					$("#update_comment").val(deviceSpareForm.comment);
					
					if(device_spare_type == 1){//消耗备品
						$("#update_order_cycle").closest("tr").show();
						$("#update_location").closest("tr").hide();
					}else if(device_spare_type == 2){//备件
						$("#update_order_cycle").closest("tr").hide();
						$("#update_location").closest("tr").show();
					}
					
					// 设备工具备品调整记录 
					adjustList(resInfo.adjustList);
					
					$("#update").show();
					$("#search").hide();
				}
			}catch(e){}
		}
	});
	
	$("#updatebutton").unbind("click").click(function(){
		var data = {
			"device_type_id" : rowData.device_type_id,
			"model_name" : rowData.model_name,
			"device_spare_type" : rowData.device_spare_type,
			"order_cycle" : $("#update_order_cycle").val(),
			"brand_id" : $("#update_brand_id").val(),
			"price" : $("#update_price").val(),
			"safety_lever" : $("#update_safety_lever").val(),
			"benchmark" : $("#update_benchmark").val(),
			"location" : $("#update_location").val(),
			"comment" : $("#update_comment").val()
		};
		
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : servicePath + '?method=doUpdate',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj, textStatus) {
				var resInfo = null;
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					if (resInfo.errors.length > 0) {
						// 共通出错信息框
						treatBackMessages(null, resInfo.errors);
					} else {
						findit();
						$("#update").hide();
						$("#search").show();
					}
				}catch(e){}
			}
		});
	});
	
	$("#managebutton").unbind("click").click(function(){
		var this_dialog = $("#message_dialog");
		if (this_dialog.length === 0) {
			$("body.outer").append("<div id='message_dialog'/>");
			this_dialog = $("#message_dialog");
		}
		
		var reason = $("#hidden_manage_reason_type").html();
		
		var content =`<div class="ui-widget-content">
						<table class="condform">
							<tr>
								<td class="ui-state-default td-title">理由</td>
						   		<td class="td-content"><select id="manage_reason">${reason}</select></td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">数量</td>
						   		<td class="td-content"><input id="manage_available_inventory" type="text" class="ui-widget-content"></td>
							</tr>
							<td class="ui-state-default td-title">调整备注</td>
					   			<td class="td-content"><textarea id="manage_comment" class="ui-widget-content" style="resize: none;" cols="50"></textarea></td>
					   		</tr>
						</table>
					  </div>`;
					  
		this_dialog.html(content);
		$("#manage_reason").select2Buttons();
		
		this_dialog.dialog({
			title : "管理",
			width : 450,
			height : 'auto',
			resizable : false,
			modal : true,
			buttons : {
				"确认":function(){
					var data = {
						"device_type_id" : rowData.device_type_id,
						"model_name" : rowData.model_name,
						"device_spare_type" : rowData.device_spare_type,
						"reason_type" : $("#manage_reason").val(),
						"adjust_inventory" : $("#manage_available_inventory").val(),
						"comment" : $("#manage_comment").val()
					};
					
					$.ajax({
						beforeSend : ajaxRequestType,
						async : true,
						url : servicePath + '?method=doManage',
						cache : false,
						data : data,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : function(xhrobj, textStatus) {
							var resInfo = null;
							try {
								// 以Object形式读取JSON
								eval('resInfo =' + xhrobj.responseText);
								if (resInfo.errors.length > 0) {
									// 共通出错信息框
									treatBackMessages(null, resInfo.errors);
								} else {
									findit();
									this_dialog.dialog('close');
									$("#update").hide();
									$("#search").show();
								}
							}catch(e){}
						}
					});
					
					
				},
				"取消":function(){
					this_dialog.dialog('close');
				}
			}
		});
		
	});
};

/**
 * 设备工具备品一览
 * @param listdata
 */
function list(listdata){
	if ($("#gbox_list").length > 0) {
		$("#list").jqGrid().clearGridData();// 清除
		$("#list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
	} else {
		$("#list").jqGrid({
			data : listdata,// 数据
			height :461,// rowheight*rowNum+1
			width : 992,
			rowheight : 23,
			shrinkToFit:true,
			datatype : "local",
			colNames : ['品名','型号','备品种类','订货天数','品牌','单价','Min-Limit','Max-Limit','当前有效库存','合理总额','在库总额','放置位置','管理备注','期间消耗量','device_type_id','device_spare_type','brand_id'],
			colModel : [{name : 'device_type_name',index : 'device_type_name',width:100},
						{name : 'model_name',index : 'model_name',width:100},
						{name : 'device_spare_type_name',index : 'device_spare_type_name',width:80},
						{name : 'order_cycle',index : 'order_cycle',width:80,align:'right',sorttype:'number'
							,formatter:function(value, options, rData){
								if (rData["device_spare_type_name"] == "备件") {
									return "-";
								} else {
									return value;
								}
							}
						},
						{name : 'brand_name',index : 'brand_name',width:100, formatter : function(value, options, rData) {
		                    if(rData.brand_name){
	                            return "<a href='javascript:showBrandDetail(\""+ rData.brand_id +"\")'>" + rData.brand_name + "</a>";
		                    }else{
		                        return "";
		                    }                           
		                }},
						{name : 'price',index : 'price',width:50,align:'right',sorttype:'currency',formatter:'currency',formatoptions:{thousandsSeparator:',',defaultValue: '',decimalPlaces:0}},
						{name : 'safety_lever',index : 'safety_lever',width:100,align:'right',sorttype:'number'},
						{name : 'benchmark',index : 'benchmark',width:100,align:'right',sorttype:'number'},
						{name : 'available_inventory',index : 'available_inventory',width:120,align:'right',sorttype:'number'},
						{name : 'total_benchmark_price',index : 'total_benchmark_price',width:90,align:'right',formatter:'currency',sorttype:'currency',formatoptions:{thousandsSeparator:',',decimalPlaces:0}},
						{name : 'total_available_price',index : 'total_available_price',width:90,align:'right',formatter:'currency',sorttype:'currency',formatoptions:{thousandsSeparator:',',decimalPlaces:0}},
						{name : 'location',index : 'location',width:100},
						{name : 'comment',index : 'comment',width:100},
						{name : 'consumable',index : 'consumable',width:100,align:'right'},
						{name : 'device_type_id',index : 'device_type_id',hidden:true},
						{name : 'device_spare_type',index : 'device_spare_type',hidden:true},
						{name : 'brand_id',index : 'brand_id',hidden:true}
            ],
			rowNum : 20,
			toppager : false,
			pager : "#listpager",
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
			onSelectRow : enableButton,
			ondblClickRow : showEdit,
			viewsortcols : [ true, 'vertical', true ],
			gridComplete : function() {
				var IDS = $("#list").getDataIDs();
				// 当前显示多少条
				var length = IDS.length;
				var pill = $("#list");
				for (var i = 0; i < length; i++) {
					var rowData = pill.jqGrid('getRowData', IDS[i]);
					var available_inventory = rowData.available_inventory * 1;
					var safety_lever = rowData.safety_lever * 1;
					
					if(available_inventory < safety_lever){
						$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_available_inventory']").css({"background-color":"orange","color":"#fff"});
					}
				}
			}
		});
		
		$(".ui-jqgrid-hbox").before('<div class="ui-widget-content" style="padding:4px;">' +
				'<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="addbutton" value="新建备品管理">' +
			'</div>');

		$("#addbutton").button().click(showAdd);
	}
};

/**
 * 设备工具备品调整记录一览
 * @param listdata
 */
function adjustList(listdata){
	if ($("#gbox_adjustlist").length > 0) {
		$("#adjustlist").jqGrid().clearGridData();// 清除
		$("#adjustlist").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
	} else {
		$("#adjustlist").jqGrid({
			data : listdata,// 数据
			height : 461,// rowheight*rowNum+1
			width : 992,
			rowheight : 23,
			shrinkToFit:true,
			datatype : "local",
			colNames : ['调整负责人','调整日时','理由','调整量','调整备注','operator_id','reason_type'],
			colModel : [{name : 'operator_name',index : 'operator_name',width:50},
						{name : 'adjust_time',index : 'adjust_time',width:80,align:'center'},
						{name : 'reason_type_name',index : 'reason_type_name',width:80},
						{name : 'adjust_inventory',index : 'adjust_inventory',width:40,align:'right'},
						{name : 'comment',index : 'comment',width:350},
						{name : 'operator_id',index : 'operator_id',hidden:true},
						{name : 'reason_type',index : 'reason_type',hidden:true}
            ],
			rowNum :20,
			toppager : false,
			pager : "#adjustlistpager",
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
			onSelectRow : null,
			ondblClickRow : null,
			viewsortcols : [ true, 'vertical', true ],
			gridComplete : function() {}
		});
	}
}