var servicePath = "device_backup.do";
var obj_manage_level = {};
var obj_status = {};

$(function(){
	$("input.ui-button").button();

	/*为每一个匹配的元素的特定事件绑定一个事件处理函数*/
	$("span.ui-icon,#searchform span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	setReferChooser($("#search_device_type_id"),$("#device_type_id_referchooser"));
	setReferChooser($("#add_device_type_id"),$("#device_type_id_referchooser"));

	setReferChooser($("#search_position_id"),$("#position_id_referchooser"));

	$("#searchform select").select2Buttons();

	$("#searchbutton").click(function(){
		$("#search_device_type_id").data("post", $("#search_device_type_id").val());
		$("#search_model_name").data("post", $("#search_model_name").val());
		$("#search_manage_code").data("post", $("#search_manage_code").val());
		$("#search_line_id").data("post", $("#search_line_id").val());
		$("#search_position_id").data("post", $("#search_position_id").val());
		findit();
	});
	$("#setbutton").click(showDetail);
	$("#resetbutton").click(reset);

	$("#submitbutton").click(submit);
	$("#reportbutton").click(makeReport);

	obj_manage_level = selecOptions2Object($("#hidden_goManage_level").val());
	obj_status = selecOptions2Object($("#hidden_goStatus").val());

	//更新取消
	$("#gobackbutton, #setarea .ui-icon-circle-triangle-w").click(function(){
		$("#searcharea").show();
		$("#setarea").hide();
	});

	$("#pires").on("click", "button", function(){
		var $button = $(this);
		if ($button.attr("usage") == undefined) {
			$button.attr("usage", "1")
				.text("可以");
		} else if ($button.attr("usage") == "1") {
			$button.attr("usage", "0")
				.text("有时可以");
		} else {
			$button.removeAttr("usage")
				.text("不可以");
		}
		checkEvaluation();
		return false;
	});

	showlist([]);
	findit();
});

/**
 * 清除
 *
 */
function reset(){
	$("#searchform input[type='text']").data("post", "").val("");
	$("#searchform input[type='hidden']").data("post", "").val("");
	$("#searchform select").data("post", "").val("").trigger("change");
};

function findit(){

	var data ={
		"device_type_id":$("#search_device_type_id").data("post"),
		"model_name":$("#search_model_name").data("post"),
		"manage_code":$("#search_manage_code").data("post"),
		"line_id":$("#search_line_id").data("post"),
		"position_id":$("#search_position_id").data("post")
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
			var resInfo = $.parseJSON(xhrobj.responseText);
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				showlist(resInfo.spareList);
			}
		}
	});
};

var showlist = function(listdata){
	if($("#gbox_list").length > 0) {
		$("#list").jqGrid().clearGridData();
		$("#list").jqGrid('setGridParam',{data:listdata}).trigger("reloadGrid", [{current:false}]);
	} else {
		$("#list").jqGrid({
			data:listdata,
			height: 461,
			width:992,
			rowheight: 23,
			datatype: "local",
			colNames:['设备工具管理ID','管理编号','device_type_id','品名','型号',
							'管理<br>等级','状态','责任工程','管理中可替换品','同型号<br>备品数','评价','对应'],
			colModel:[
				{name:'manage_id',index:'manage_id',hidden:true},
				{name:'manage_code',index:'manage_code',width:50},
				{name:'device_type_id',index:'device_type_id',hidden:true},
				{name:'name',index:'name',width:100},
				{name:'model_name',index:'model_name',width:120},
				{name:'manage_level',index:'manage_level',width:50,align:'center',
					formatter : 'select',
					editoptions : {
							value : $("#hidden_goManage_level").val()
					}
				},
				{name:'status',index:'status',width:60,align:'center',
						formatter : 'select',
						editoptions : {
								value : $("#hidden_goStatus").val()
						}
				},
				{name:'line_name',index:'line_name',width:90},
				{name:'backup_in_manage',index:'backup_in_manage',width:210},
				{name:'spare_available_inventory',index : 'spare_available_inventory',width:60,align:'right',
					sorttype:'integer',formatter:'integer', formatoptions:{defaultValue:'-'}},
				{name:'evaluation',index:'evaluation',width:50,align:'center',width:40,
					formatter : function(value, rdata) {
						if (value) {
							if (value >= 4) {
								return '◎';
							} else if (value >= 2) {
								return '○';
							} else {
								return '△';
							}
						} else {
							return '×';
						}
					}
				},
				{name:'corresponding',index:'corresponding',width:120,align:'left'}
			],
			rownumbers:true,
			toppager : false,
			rowNum : 20,
			sortorder:"asc",
			sortname:"id",
			multiselect: false,
			pager : "#listpager",
			viewrecords : true,
			ondblClickRow : showDetail,
			onSelectRow: function(){$("#setbutton").enable()},
			gridview : true,
			pagerpos : 'right',
			pgbuttons : true,
			pginput : false,
			recordpos : 'left',
			viewsortcols : [true, 'vertical', true],
			gridComplete:function(){$("#setbutton").disable()}
		});
	}
}

var showDetail = function(){
	var rowId=$("#list").jqGrid("getGridParam","selrow");
	var rowData=$("#list").getRowData(rowId);

	var postData = {
		manage_id : rowData.manage_id,
		device_type_id : rowData.device_type_id,
		model_name : rowData.model_name,
		line_name : rowData.line_name
	}

	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=getBackups',
		cache : false,
		data : postData,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus){showDetailComplete(xhrobj, rowData)}
	});
}

var showDetailComplete = function(xhrobj, rowData) {
	var resInfo = $.parseJSON(xhrobj.responseText);
	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages(null, resInfo.errors);
	} else {
		$("#set_manage_code").text(rowData.manage_code);
		$("#set_manage_id").val(rowData.manage_id);
		$("#set_manage_level").text(obj_manage_level[rowData.manage_level]);
		$("#set_status").text(obj_status[rowData.status]);
		$("#set_device_type_name").text(rowData.name);
		$("#set_model_name").text(rowData.model_name);
		$("#set_line_name").text(rowData.line_name);
		$("#set_evaluation").text(rowData.evaluation);
		$("#set_corresponding").val(rowData.corresponding);

		var $piresBody = $("#pires tbody");
		$piresBody.html("");

		if (resInfo.lSameModel.length + resInfo.lOtherModel.length == 0) {
			$piresBody.append("<tr><td colspan=6>没有同样品名的设备！</tr>");
		} else {
			$piresBody.append(drawPiresBody(rowData.manage_code, resInfo.lSameModel));
			$piresBody.append("<tr><td colspan=6>——此处以上为同型号————————————————————————以下为不同型号——</tr>");
			$piresBody.append(drawPiresBody(rowData.manage_code, resInfo.lOtherModel));
		}

		for (var iR in resInfo.relations) {
			var relation = resInfo.relations[iR];
			if (relation["manage_id"] == rowData.manage_id) {
				var $target = $("#pires tr[manage_id='" + relation["backup_manage_id"] + "'] .ub button");
				var free_displace_flg = relation["free_displace_flg"];
				$target.attr("org", free_displace_flg);
				if (free_displace_flg) {
					$target.attr("usage", "1")
						.text("可以");
				} else {
					$target.attr("usage", "0")
						.text("有时可以");
				}
			} else {
				var $target = $("#pires tr[manage_id='" + relation["manage_id"] + "'] .bu button");
				var free_displace_flg = relation["free_displace_flg"];
				$target.attr("org", free_displace_flg);
				if (free_displace_flg) {
					$target.attr("usage", "1")
						.text("可以");
				} else {
					$target.attr("usage", "0")
						.text("有时可以");
				}
			}
		}

		$("#searcharea").hide();
		$("#setarea").show();
	}
}

var drawPiresBody = function(manage_code, devices){
	var retString = "";
	for (var i in devices) {
		var device = devices[i];
		retString += "<tr manage_id='" + device.devices_manage_id + "'><td>" + device.manage_code + "</td><td>" + getStatus(device.manage_level, device.status) 
			+ "</td><td>" + device.model_name + "</td><td>" + getLine(device.section_name, device.line_name)
			+ "</td><td class='ub'><button>不可以</button> 代替" + manage_code 
			+ "作业</td><td class='bu'><button>不可以</button> 用" + manage_code + "代替作业</td></tr>";
	}

	return retString;
}

var getStatus = function(manage_level, status) {
	var rets = "";
	rets += (obj_manage_level[manage_level]) + " ";
	if (status == 1) {
		rets += "使用中";
	} else if (status == 4) {
		rets += "保管中";
	} else if (status == 5) {
		rets += "使用中(周边)";
	}
	return rets;
}

var getLine = function(section_name, line_name) {
	if (line_name) {
		return line_name;
	} else if (section_name) {
		return "(" + section_name + ")"
	} else {
		return "未知";
	}
}

var checkEvaluation = function(){
	var $buttons = $("#pires .ub button");
	
	if ($buttons.filter("[usage='1']").length > 0) {
		$("#set_evaluation").text("◎");
	} else if ($buttons.filter("[usage='0']").length > 0) {
		$("#set_evaluation").text("△");
	} else {
		$("#set_evaluation").text("×");
	}
}

var submit = function(){
	if ($("#set_evaluation").text() === "×") {
		if (!$("#set_corresponding").val()) {
			errorPop("无可替换品的设备，需要填写当此设备异常时的应急措施。");
			return;
		}
	}

	var postData = {
		"manage_id" : $("#set_manage_id").val(),
		"corresponding" : $("#set_corresponding").val()
	}
	var idx = 0;
	$("#pires .ub button").each(function(i, ele){
		var $ele = $(ele);
		var usage = $ele.attr("usage");
		var org = $ele.attr("org");
		if (usage == undefined) {
			if (org != undefined) {
				postData["updates.backup_manage_id[" + idx + "]"] = $(ele).closest("tr").attr("manage_id");
				postData["updates.status[" + idx + "]"] = "3"; // delete
				idx++;
			}
		} else {
			if (org != undefined) {
				if (usage != org) {
					postData["updates.backup_manage_id[" + idx + "]"] = $(ele).closest("tr").attr("manage_id");
					postData["updates.free_displace_flg[" + idx + "]"] = $(ele).attr("usage");
					postData["updates.status[" + idx + "]"] = "2"; // update
					idx++;
				}
			} else {
				postData["updates.backup_manage_id[" + idx + "]"] = $(ele).closest("tr").attr("manage_id");
				postData["updates.free_displace_flg[" + idx + "]"] = $(ele).attr("usage");
				postData["updates.status[" + idx + "]"] = "1"; // insert
				idx++;
			}
		}
	});

	$("#pires .bu button").each(function(i, ele){
		var $ele = $(ele);
		var usage = $ele.attr("usage");
		var org = $ele.attr("org");
		if (usage == undefined) {
			if (org != undefined) {
				postData["updates.manage_id[" + idx + "]"] = $(ele).closest("tr").attr("manage_id");
				postData["updates.status[" + idx + "]"] = "3"; // delete
				idx++;
			}
		} else {
			if (org != undefined) {
				if (usage != org) {
					postData["updates.manage_id[" + idx + "]"] = $(ele).closest("tr").attr("manage_id");
					postData["updates.free_displace_flg[" + idx + "]"] = $(ele).attr("usage");
					postData["updates.status[" + idx + "]"] = "2"; // update
					idx++;
				}
			} else {
				postData["updates.manage_id[" + idx + "]"] = $(ele).closest("tr").attr("manage_id");
				postData["updates.free_displace_flg[" + idx + "]"] = $(ele).attr("usage");
				postData["updates.status[" + idx + "]"] = "1"; // insert
				idx++;
			}
		}
	});

	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doSubmit',
		cache : false,
		data : postData,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus){
			var resInfo = $.parseJSON(xhrobj.responseText);
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				$("#searcharea").show();
				$("#setarea").hide();
				findit();
			}
		}
	});
}

var makeReport = function() {
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=makeReport',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus){
			var resInfo = $.parseJSON(xhrobj.responseText);
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				if ($("iframe").length > 0) {
					$("iframe").attr("src", servicePath+"?method=export&filePath=" + resInfo.filePath);
				} else {
					var iframe = document.createElement("iframe");
		            iframe.src = servicePath+"?method=export&filePath=" + resInfo.filePath;
		            iframe.style.display = "none";
		            document.body.appendChild(iframe);
				}
			}
		}
	});
}