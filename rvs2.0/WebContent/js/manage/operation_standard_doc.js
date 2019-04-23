var servicePath = "operation_standard_doc.do";

$(function(){
	$("input.ui-button").button();
	
	/* 为每一个匹配的元素的特定事件绑定一个事件处理函数 */
    $("#search span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});
    
	$("#search_category_id,#search_line_id").select2Buttons();
	
	setReferChooser($("#search_model_id"),$("#model_id_referchooser"));
	setReferChooser($("#configure_model_id"),$("#conf_model_id_referchooser"),null,function(TR){
		enableConfigureButton();
	});
	
	setReferChooser($("#search_position_id"),$("#process_code_referchooser"),null,function(TR){
		if (TR == null) return;
		var value = $(TR).find("td:last-child").text().trim();
		$("#search_process_code").val(value);
	});
	setReferChooser($("#configure_position_id"),$("#conf_process_code_referchooser"),null,function(TR){
		enableConfigureButton();
		
		if (TR == null) return;
		var value = $(TR).find("td:last-child").text().trim();
		$("#configure_process_code").val(value);
	});
	
	setReferChooser($("#copy_model_id"),$("#copy_model_id_referchooser"));
	
	// 清除
    $("#resetbutton").click(reset);
    
    // 检索
    $("#searchbutton").click(findit);
    
    // 删除型号配置
    $("#delModelButton").click(delModel);
    
    // 复制于
    $("#copybutton").click(copy);
    
    $("#gobackbutton,#update span.ui-icon").click(function(){
    	$("#update").hide();
    	$("#search").show();
    });
    
    $("#add_row").click(addRow);
    
    $(document).on("click","#updateform tbody input.subtract",subClick);
    
    // 配置
    $("#configurebutton").click(function(){
    	var data = {
    		"model_id" : $("#configure_model_id").val().trim(),
    		"model_name" : $("#configure_model_name").val().trim(),
    		"position_id" : $("#configure_position_id").val().trim(),
    		"process_code" : $("#configure_process_code").val().trim()
    	};
    	showEdit(data);
    });
    
    // 更新
    $("#updatebutton").click(update);
    
    // 删除型号工位配置
    $("#delModelAndPositionButton").click(delModelAndPosition);
    
    enableConfigureButton();
    
    findit();
});
function addRow(){
	var len = $("#updateform tbody tr").length;
	
	// 最多12行
	if(len >= 12){
		return;
	}
	
	var tr = '<tr>';
		tr +='<td class="ui-state-default"></td>';
		tr +='<td class="td-content"><input type="text" class="ui-widget-content url"><input type="button" class="ui-button" value="打开文件" onclick="openPDF(this);"/></td>';
		tr +='<td class="td-content"><input type="text" class="ui-widget-content no"></td>';
		tr +='<td class="td-content"><input type="button" class="ui-button subtract" value="-"/></td>';
		tr +='</tr>';
	
	var $tr = $(tr);
	$tr.find("input.ui-button").button();
	
	$("#updateform tbody").append($tr);
	
	resetRowNum();
	
	len = $("#updateform tbody tr").length;
	// 二次check
	if(len >= 12){
		$("#add_row").disable().removeClass("ui-state-focus");
	}
};
function resetRowNum(){
	$("#updateform tbody tr").each(function(index,tr){
		$(this).find("td:eq(0)").text(index + 1);
	});
};
function subClick(){
	var $tr = $(this).closest("tr");
	$tr.remove();
	resetRowNum();
	
	if($("#add_row").prop("disabled")){
		$("#add_row").enable();
	}
};
function copy(){
	var model_id = $("#configure_model_id").val().trim();
	if(!model_id){
		errorPop("请选择配置型号。")
		return;
	}
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=searchAllModel',
		cache : false,
		data : null,
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
					$("#copy_model_id_referchooser table.subform").html(resInfo.mReferChooser);
					$("#copy_model_name,#copy_model_id").val("");
					
					var position_id = $("#configure_position_id").val().trim();
					var process_code = $("#configure_process_code").val().trim();
					
					var data = {
						"model_id" : model_id,
						"position_id" : position_id,
						"process_code" : process_code
					};
					
					var buttons = {};
					buttons["复制全部工位配置"] = function () {
						data["copy_model_id"] = $("#copy_model_id").val().trim();
						data["copy_model_name"] = $("#copy_model_name").val().trim();
						data["flg"] = "1";
						copyConfirm(data,$(this));
					};
					
					if(position_id){
						buttons["复制" + process_code + "工位配置"] = function () {
							data["copy_model_id"] = $("#copy_model_id").val().trim();
							data["copy_model_name"] = $("#copy_model_name").val().trim();
							data["flg"] = "0";
							copyConfirm(data,$(this));
						};
					}
					
					buttons["取消"] = function () {
						$(this).dialog('close');
					};
					
					$("#copyModelDialog").dialog({
						title: "复制来源型号",
						width: 400,
						height: 'auto',
						resizable: false,
						modal: true,
						show:'blind',
						buttons: buttons
					});
				}
			}catch(e){}
		}
	});
};
function copyConfirm(data,$dialog){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doCopy',
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
					$dialog.dialog('close');
					$("#configureform input[type='text'],input[type='hidden']").val("");
					enableConfigureButton();
					findit();
				}
			}catch(e){}
		}
	});
};
function enableConfigureButton(){
	var model_id = $("#configure_model_id").val().trim();
	var position_id = $("#configure_position_id").val().trim();
	
	if(model_id && position_id){
		$("#configurebutton").enable();
	}else{
		$("#configurebutton").disable();
	}
	
	if(model_id){
		$("#copybutton").enable();
	}else{
		$("#copybutton").disable();
	}
};
function reset(){
	$("#searchform input[type='text']").val("");
	$("#searchform input[type='hidden']").val("");
	$("#searchform select").val("").trigger("change");
};
function findit(){
	var data = {
		"category_id" : $("#search_category_id").val().trim(),
		"line_id" : $("#search_line_id").val().trim(),
		"model_id" : $("#search_model_id").val().trim(),
		"position_id" : $("#search_position_id").val().trim()
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
					list(resInfo.list);
				}
			}catch(e){}
		}
	});
};
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
			colNames : ['机种','型号','工程','工位代码','工位名','配置文档数','model_id','position_id'],
			colModel : [{name : 'category_name',index : 'category_name',width:70},
						{name : 'model_name',index : 'model_name',width:100},
						{name : 'line_name',index : 'line_name',width:50},
						{name : 'process_code',index : 'process_code',width:40,align:'center'},
						{name : 'position_name',index : 'position_name',width:100},
						{name : 'total_doc',index : 'total_doc',width:40,align:'right',sorttype:'number'},
						{name : 'model_id',index : 'model_id',hidden:true},
						{name : 'position_id',index : 'position_id',hidden:true}
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
			onSelectRow : function(rowId){
				$("#delModelButton").enable();
			},
			ondblClickRow : function(rid, iRow, iCol, e) {
				var rowData = $("#list").getRowData(rid);
				var data = {
					"model_id" : rowData.model_id,
					"model_name" : rowData.model_name,
					"position_id" : rowData.position_id,
					"process_code" : rowData.process_code
				};
				
				showEdit(data);
			},
			viewsortcols : [ true, 'vertical', true ],
			gridComplete : function() {
				$("#delModelButton").disable();
			}
		});
	}
};
function delModel(){
	// 得到选中行的ID
	var rowID = $("#list").jqGrid("getGridParam", "selrow");
	var rowData = $("#list").jqGrid('getRowData', rowID);
	
	var warnData = "是否要删除型号【"+ rowData.model_name + "】的全部配置。";
	warningConfirm(warnData,function(){
		var data = {
			"model_id" : rowData.model_id
		};

		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : servicePath + '?method=doDelete',
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
	},function(){})
};
function showEdit(data){
	
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=getDetail',
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
					var list = resInfo.list;
					var content = "";

					list.forEach(function(item,index){
						content +='<tr>';
						content +='<td class="ui-state-default">' + (index+1) + '</td>';
						content +='<td class="td-content"><input type="text" class="ui-widget-content url" value="' + item.doc_url + '"><input type="button" class="ui-button" value="打开文件" onclick="openPDF(this);"/></td>';
						content +='<td class="td-content"><input type="text" class="ui-widget-content no" value="' + item.page_no + '"></td>';
						content +='<td class="td-content"><input type="button" class="ui-button subtract" value="-"/></td>';
						content +='</tr>';
					});
					var $content = $(content);
					$content.find("input.ui-button").button();
					
					$("#updateform tbody").html($content);
					$("#updateform").data("data",data);
					
					$("#update span.areatitle span:eq(0)").text(data.model_name);
					$("#update span.areatitle span:eq(1)").text(data.process_code);
					
					$("#search").hide();
					$("#update").show();
					
					if(list.length < 12){
						$("#add_row").enable();
					}else{
						$("#add_row").disable();
					}
				}
			}catch(e){}
		}
	});
};
function openPDF(btn){
	var $tr = $(btn).closest("tr");
	var url = $tr.find("input.url").val().trim();
	var page = $tr.find("input.no").val().trim();

	if(url && page){
		window.sessionStorage.setItem("url",url);
		window.sessionStorage.setItem("page",page);
		window.open("pages/pdfView.html","_blank");
	}
};
function update(){
	var len = $("#updateform tbody tr").length;
	
	if(len == 0){
		errorPop("请至少创建一条作业基准书明细。");
		return;
	}

	var formData = $("#updateform").data("data");
	var data = {
		"model_id" : formData.model_id,
		"position_id" : formData.position_id
	};
	
	$("#updateform tbody tr").each(function(index,tr){
		var $tr = $(tr);
		data["operation_standard_doc.model_id[" + index + "]"] = formData.model_id;
		data["operation_standard_doc.position_id[" + index + "]"] = formData.position_id;
		data["operation_standard_doc.doc_seq[" + index + "]"] = (index+1);
		data["operation_standard_doc.doc_url[" + index + "]"] = $tr.find("input.url").val().trim();
		data["operation_standard_doc.page_no[" + index + "]"] = $tr.find("input.no").val().trim();
	});
	
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
};
function delModelAndPosition(){
	var modelName = $("#update span.areatitle > span:eq(0)").text().trim();
	var processCode = $("#update span.areatitle > span:eq(1)").text().trim();
	
	var warnData = "是否要删除型号【"+ modelName + "】,工位【" + processCode + "】的全部配置。";
	
	warningConfirm(warnData,function(){
		var formData = $("#updateform").data("data");
		var data = {
			"model_id" : formData.model_id,
			"position_id" : formData.position_id
		};
		
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : servicePath + '?method=doDelete',
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
	},function(){})
};