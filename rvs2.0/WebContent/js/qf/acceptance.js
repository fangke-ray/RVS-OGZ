/** 模块名 */
var modelname = "维修对象";
/** 一览数据对象 */
var listdata = {};
var reception_listdata = {};
/** 服务器处理路径 */
var servicePath = "acceptance.do";

/** 医院autocomplete **/
var customers = {};
var curpagenum;
var reception_import = function() {
	var data = {};
	var rowids = $("#uld_list").jqGrid("getGridParam", "selarrrow");
	for (var i in rowids) {
		var rowdata = $("#uld_list").getRowData(rowids[i]);
		data["materials.sorc_no[" + i + "]"] = rowdata["sorc_no"];
		data["materials.model_id[" + i + "]"] = rowdata["model_id"];
		data["materials.model_name[" + i + "]"] = rowdata["model_name"];
		data["materials.serial_no[" + i + "]"] = rowdata["serial_no"];
		data["materials.agreed_date[" + i + "]"] = rowdata["agreed_date_o"];
		data["materials.level[" + i + "]"] = rowdata["level"];
		data["materials.direct_flg[" + i + "]"] = rowdata["direct_flg"];
		data["materials.customer_name[" + i + "]"] = rowdata["customer_name"];
		data["materials.ocm[" + i + "]"] = rowdata["ocm"];
		data["materials.ocm_rank[" + i + "]"] = rowdata["ocm_rank"];
		data["materials.ocm_deliver_date[" + i + "]"] = rowdata["ocm_deliver_date_o"];
		data["materials.osh_deliver_date[" + i + "]"] = rowdata["osh_deliver_date_o"];
		data["materials.area[" + i + "]"] = rowdata["area"];
		data["materials.bound_out_ocm[" + i + "]"] = rowdata["bound_out_ocm"];
		data["materials.service_repair_flg[" + i + "]"] = rowdata["service_repair_flg"];
		data["materials.fix_type[" + i + "]"] = rowdata["fix_type"];
		data["materials.selectable[" + i + "]"] = rowdata["selectable"];
	}
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: false, 
		url: servicePath + '?method=doimport', 
		cache: false, 
		data: data, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete:  function(xhrobj, textStatus){
			var resInfo = null;

			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
			
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					for (var i in rowids) {
						var rowdata = $("#uld_list").getRowData(rowids[i]);
						griddata_remove(listdata, "material_id", rowdata["material_id"], false);
					}
					load_list();
					loadImpListData();
				}
			} catch(e) {
				
			}
		}
	});
};

var uploadfile = function() {

	// 覆盖层
	panelOverlay++;
	makeWindowOverlay();

	// ajax enctype="multipart/form-data"
	$.ajaxFileUpload({
		url : 'upload.do?method=doAccept', // 需要链接到服务器地址
		secureuri : false,
		fileElementId : 'file', // 文件选择框的id属性
		dataType : 'json', // 服务器返回的格式
		success : function(responseText, textStatus) {
			panelOverlay--;
			killWindowOverlay();

			var resInfo = null;

			try {
				// 以Object形式读取JSON
				eval('resInfo =' + responseText);
			
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					listdata = resInfo.list;
					load_list();
				}
			} catch(e) {
				
			}
		}
	});
};

var showLoadInput=function(rid) {
	var linkna = "widgets/qf/acceptance-edit.jsp";

	$("#uld_listedit").hide();
	// 导入编辑画面
	$("#uld_listedit").load(linkna, function(responseText, textStatus, XMLHttpRequest) {
		// 读取修改行
		var rowData = $("#uld_list").getRowData(rid);

		// 数据取得
		$("#material_id").val(rowData.material_id);
		$("#edit_sorcno").val(rowData.sorc_no);
		$("#edit_modelname").val(rowData.model_id);
		$("#inp_modelname").val(rowData.model_name);
		$("#edit_serialno").val(rowData.serial_no);
		$("#edit_ocm").val(rowData.ocm); 
		$("#edit_ocm_rank").val(rowData.ocm_rank);
		$("#edit_ocm_deliver_date").val(rowData.ocm_deliver_date_o);
		$("#edit_osh_deliver_date").val(rowData.osh_deliver_date_o);
		$("#edit_customer_name").val(rowData.customer_name); 
		$("#edit_level").val(rowData.level); 
		$("#edit_direct").val(rowData.direct_flg);
		$("#edit_service_repair").val(rowData.service_repair_flg);
		$("#edit_fix_type").val(rowData.fix_type);
		$("#edit_selectable").val(rowData.selectable);
		$("#edit_area").val(rowData.area);
		$("#edit_bound_out_ocm").val(rowData.bound_out_ocm);

		$("#edit_agreed_date").parents("tr").eq(0).hide();

		$("#ins_material").validate({
			rules : {
				sorcno : {
					required : true
				},
				serialno : {
					required : true
				},
				modelname : {
					required : true
				}
			}
		});

		$("#edit_ocm_deliver_date,#edit_agreed_date").datepicker({
			showButtonPanel : true,
			dateFormat : "yy/mm/dd",
			currentText : "今天"
		});

		$("#edit_direct,#edit_service_repair,#edit_fix_type,#edit_selectable,#edit_ocm,#edit_level,#edit_ocm_rank,#edit_area,#edit_bound_out_ocm").select2Buttons();

		setReferChooser($("#edit_modelname"));
		$(".ui-button[value='清空']").button();
		$("#uld_listedit").dialog({
			position : 'auto', // [ 800, 20 ]
			title : "维修对象信息编辑",
			width : 'auto',
			show: "blind",
			height :  'auto' , //550
			resizable : false,
			modal : true,
			minHeight : 200,
			buttons : {
				"确定":function(){
					if ($("#ins_material").valid()) {
						var key_id = $("#material_id").val();
						// 更新内容
						griddata_update(listdata, "material_id", key_id, "sorc_no", $("#edit_sorcno").val(), false);
						griddata_update(listdata, "material_id", key_id, "serial_no", $("#edit_serialno").val(), false);
						griddata_update(listdata, "material_id", key_id, "ocm_rank", $("#edit_ocm_rank").val(), false);
						griddata_update(listdata, "material_id", key_id, "ocm", $("#edit_ocm").val(), false);
						griddata_update(listdata, "material_id", key_id, "ocm_deliver_date", $("#edit_ocm_deliver_date").val(), false);
						griddata_update(listdata, "material_id", key_id, "osh_deliver_date", $("#edit_osh_deliver_date").val(), false);
						griddata_update(listdata, "material_id", key_id, "customer_name", $("#edit_customer_name").val(), false);
						griddata_update(listdata, "material_id", key_id, "level", $("#edit_level").val(), false);
						griddata_update(listdata, "material_id", key_id, "direct_flg", $("#edit_direct").val() == "" ? "0" : $("#edit_direct").val(), false);
						griddata_update(listdata, "material_id", key_id, "service_repair_flg", $("#edit_service_repair").val(), false);
						griddata_update(listdata, "material_id", key_id, "fix_type", $("#edit_fix_type").val(), false);
						griddata_update(listdata, "material_id", key_id, "selectable", $("#edit_selectable").val(), false);
						// 地区
						griddata_update(listdata, "material_id", key_id, "area", $("#edit_area").val(), false);
						griddata_update(listdata, "material_id", key_id, "bound_out_ocm", $("#edit_bound_out_ocm").val(), false);

						$("#uld_list").jqGrid().clearGridData();
						$("#uld_list").jqGrid('setGridParam',{data:listdata}).trigger("reloadGrid", [{current:true}]);
						$(this).dialog("close");
					}
				}, "关闭" : function(){ $(this).dialog("close"); }
			}
		});
		$("#uld_listedit").show();
	});

};

var showInput=function(rid, manual) {

	var linkna = "widgets/qf/acceptance-edit.jsp";

	$("#uld_listedit").hide();
	// 导入编辑画面
	$("#uld_listedit").load(linkna, function(responseText, textStatus, XMLHttpRequest) {

		$("#edit_agreed_date").parents("tr").eq(0).hide();

		$("#edit_customer_name").autocomplete({
			source : customers,
			minLength :2,
			delay : 100
		});

		if (manual != 1) {
			// 读取修改行
			var rowData = $("#imp_list").getRowData(rid);

			// 数据取得
			$("#material_id").val(rowData.material_id);
			$("#edit_sorcno").val(rowData.sorc_no);
			$("#inp_modelname").val(rowData.model_name);
			$("#edit_modelname").val(rowData.model_id);
			$("#edit_serialno").val(rowData.serial_no);
			$("#edit_ocm").val(rowData.ocm); 
			$("#edit_ocm_rank").val(rowData.ocm_rank); 
			$("#edit_ocm_deliver_date").val(rowData.ocm_deliver_date); 
			$("#edit_osh_deliver_date").val(rowData.osh_deliver_date); 
			$("#edit_level").val(rowData.level);
			$("#edit_direct").val(rowData.direct_flg);
			$("#edit_service_repair").val(rowData.service_repair_flg);
			$("#edit_fix_type").val(rowData.fix_type);
			$("#edit_selectable").val(rowData.selectable);
			$("#edit_area").val(rowData.area);
			$("#edit_bound_out_ocm").val(rowData.bound_out_ocm);
		} else {
			$("#edit_break_back_flg").closest("tr").show();
			$("#edit_break_back_flg").change(function(){
				var value = this.value;
				//[备品]或者[RC品]
				if (value == 3 || value == 4){
					$("#edit_modelname").next().show();
				} else {
				//其他
					$("#edit_modelname").next().hide();
				}
			})
		}

		$("#edit_ocm_deliver_date,#edit_osh_deliver_date").datepicker({
			showButtonPanel : true,
			dateFormat : "yy/mm/dd",
			currentText : "今天"
		});

		$("#edit_direct,#edit_service_repair,#edit_fix_type,#edit_selectable,#edit_ocm,#edit_level,#edit_ocm_rank,#edit_area,#edit_bound_out_ocm").select2Buttons();

		setReferChooser($("#edit_modelname"));
		$(".ui-button[value='清空']").button();
		$("#uld_listedit").dialog({
			position : 'auto', // [ 800, 20 ]
			title : "维修对象信息编辑",
			width : 'auto',
			show: "blind",
			height :  'auto' , //550
			resizable : false,
			modal : true,
			minHeight : 200,
			buttons : {
				"确定":function(){
					$("#ins_material").validate({
						rules : {
							serialno : {
								required : true
							}
						}
					});
					
					var $input = $("#edit_modelname").next();
					// 型号手动输入标记
					var inputFlg = false;
					
					if ($input.is(":visible") && $input.val().trim() != "") {
						inputFlg = true;
						$("#inp_modelname").rules("remove");
					} else {
						$("#inp_modelname").rules("add",{required:true});
					}
					
					if ($("#ins_material").valid()) {
						var data = {
							"material_id":$("#material_id").val(),
							"sorc_no":$("#edit_sorcno").val(),
							"serial_no":$("#edit_serialno").val(),
							"ocm":$("#edit_ocm").val(),
							"ocm_rank":$("#edit_ocm_rank").val(),
							"customer_name":$("#edit_customer_name").val(),
							"ocm_deliver_date":$("#edit_ocm_deliver_date").val(),
							"osh_deliver_date":$("#edit_osh_deliver_date").val(),
							"level":$("#edit_level").val(),
							"direct_flg":$("#edit_direct").val() == "" ? "0" : $("#edit_direct").val(),
							"service_repair_flg":$("#edit_service_repair").val(),
							"fix_type":$("#edit_fix_type").val(),
							"selectable":$("#edit_selectable").val(),
							"area":$("#edit_area").val(),
							"bound_out_ocm":$("#edit_bound_out_ocm").val(),
							"break_back_flg":$("#edit_break_back_flg").val()
						}
						
						if (inputFlg) {
							data["model_id"] = "00000000000";
							data["model_name"] = $input.val().trim();
							data["scheduled_manager_comment"] = $input.val().trim();
						} else {
							data["model_id"] = $("#edit_modelname").val();
							data["model_name"] = $("#inp_modelname").val();
						}
						
						$.ajax({
							beforeSend : ajaxRequestType,
							async : false,
							url : servicePath + '?method=doinsert',
							cache : false,
							data : data,
							type : "post",
							dataType : "json",
							success : ajaxSuccessCheck,
							error : ajaxError,
							complete : function(xhrobj, textStatus) {
								insert_handleComplete(xhrobj, manual);
							}
						});
					}
				}, "关闭" : function(){ $(this).dialog("close"); }
			}
		});
		$("#uld_listedit").show();
	});

};

/**
 * 小票打印
 */
var printTicket=function() {

	var data = {};
	var rowids = $("#imp_list").jqGrid("getGridParam", "selarrrow");
	curpagenum = $("#imp_list").jqGrid('getGridParam', 'page');   //当前页码
	var selectedRows = new Array();
	
	var index = 0;
	for (var i in rowids) {
		var rowdata = $("#imp_list").getRowData(rowids[i]);
		// 维修品
		if(rowdata.break_back_flg == 0){
			data["materials.material_id[" + index + "]"] = rowdata["material_id"];
			selectedRows.push(rowdata["material_id"]);
			index++;
		}
	}

	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: false, 
		url: servicePath + '?method=doPrintTicket', 
		cache: false, 
		data: data, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete:  function(xhrobj, textStatus){
			var resInfo = null;

			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
			
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var date = new Date();
					if ($("iframe").length > 0) {
						$("iframe").attr("src", "download.do"+"?method=output&fileName=tickets.pdf&filePath=" + resInfo.tempFile);
					} else {
						var iframe = document.createElement("iframe");
			            iframe.src = "download.do"+"?method=output&fileName=tickets.pdf&filePath=" + resInfo.tempFile;
			            iframe.style.display = "none";
			            document.body.appendChild(iframe);
					}
					loadImpListData(curpagenum,selectedRows);
				}
			} catch(e) {
			}
		}
	});
};

/** 根据条件使按钮有效/无效化 */
var enablebuttons = function() {
	var rowids = $("#uld_list").jqGrid("getGridParam", "selarrrow");
	if (rowids.length === 0) {
		$("#importbutton").disable();
	} else {
		// 没有流水线分类的不能导入
		var flag = true;
		for (var i in rowids) {
			var data = $("#uld_list").getRowData(rowids[i]);
			if (data["model_id"] == "" || data["fix_type"] == "") {
				flag = false;
				break;
			}
		}
		if (flag) {
			$("#importbutton").enable();
		} else {
			$("#importbutton").disable();
		}
	}
};

var enablebuttons2 = function() {
	var rowids = $("#imp_list").jqGrid("getGridParam", "selarrrow");
	if (rowids.length >= 1) {
		var flag = false;
		for (var i in rowids) {
			var data = $("#imp_list").getRowData(rowids[i]);
			// 勾选的记录中存在维修品，则打印现品票按钮可以使用
			if(data.break_back_flg == 0){
				flag = true;
				break;
			}
		}

		if (flag) {
			$("#printbutton").enable();
		} else {
			$("#printbutton").disable();
		}
		
		$("#returnbutton").enable();
		$("#returnbutton input").enable();
	} else {
		$("#printbutton").disable();
		$("#returnbutton").disable();
		$("#returnbutton input").disable();
	}
	
	if (rowids.length > 0) {
		var flag = true;
		var notaccepted = true;
		for (var i in rowids) {
			var data = $("#imp_list").getRowData(rowids[i]);
			
			var break_back_flg = data.break_back_flg;
			if (break_back_flg == 3 || break_back_flg == 4) {//备品或RC品
				if(data["doreception_time"].trim() == ""){
					flag = false;
					break;
				}
			} else {
				if (data["sterilized"] != "0" || data["doreception_time"].trim() == "") {
					flag = false;
					break;
				}
			}
		}
		for (var i in rowids) {
			var data = $("#imp_list").getRowData(rowids[i]);
			if (data["doreception_time"].trim() != "") {
				notaccepted = false;
				break;
			}
		}
	
		if (flag) {
			$("#disinfectionbutton").enable();
			$("#sterilizationbutton").enable();
		} else {
			$("#disinfectionbutton").disable();
			$("#sterilizationbutton").disable();
		}

		if (notaccepted) {
			$("#acceptancebutton").enable();
		} else {
			$("#acceptancebutton").disable();
		}
	} else {
		$("#disinfectionbutton").disable();
		$("#sterilizationbutton").disable();
		$("#acceptancebutton").disable();
	}
};

function acceptted_list(){
	var jthis = $("#imp_list");
	if ($("#gbox_imp_list").length > 0) {
		jthis.jqGrid().clearGridData();
		jthis.jqGrid('setGridParam',{data:reception_listdata}).trigger("reloadGrid", [{current:true}]);
	} else {
		jthis.jqGrid({
			data : [],
			height: 415,
			width: 992,
			rowheight: 23,
			datatype: "local",
			colNames:['受理对象ID','导入时间','受理时间', '修理单号', '型号 ID', '型号' , '机身号','RC ID','RC','同意日', 
			'等级ID', '等级','受理人员ID','受理人员', '备注', '直送', '返修标记', '流水类型','消毒灭菌ID', '选择式报价'
			,'ocm_rank','customer_name','vip','scheduled_expedited','ocm_deliver_date','osh_deliver_date','area','返送地区', '通箱位置','break_back_flg'],
			colModel:[
				{name:'material_id',index:'material_id', hidden:true},
				{name:'reception_time',index:'reception_time', width:50, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d H:i'}},
				{name:'doreception_time',index:'doreception_time', width:50, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d H:i'}},
				{name:'sorc_no',index:'sorc_no', width:60},
				{name:'model_id',index:'model_id', hidden:true},
				{name:'model_name',index:'model_name', width:125},
				{name:'serial_no',index:'serial_no', width:50},
				{name:'ocm',index:'ocm', hidden:true},
				{name:'ocmName',index:'ocmName', width:65, align:'center'},
				{name:'agreed_date',index:'agreed_date', width:50, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}},
				{name:'level',index:'level', hidden:true},
				{name:'levelName',index:'levelName', width:35, align:'center'},
				{name:'operator_id',index:'operator_id', hidden:true},
				{name:'operator_name',index:'operator_name', width:50, hidden:true},
				{name:'remark',index:'remark', width:105,formatter:function(value, options, rData){
					if(rData.break_back_flg == 3){
						value += "备品";
					} else if(rData.break_back_flg == 4){
						value += "RC品";
					}
					return value;
				}},
				{name:'direct_flg',index:'direct_flg', hidden:true},
				{name:'service_repair_flg',index:'service_repair_flg', hidden:true},
				{name:'fix_type',index:'fix_type', hidden:true},
				{name:'sterilized',index:'sterilized', hidden:true},
				{name:'selectable',index:'selectable', hidden:true},
				{name:'ocm_rank',index:'ocm_rank', hidden:true},
				{name:'customer_name',index:'customer_name', hidden:true},
				{name:'quotation_first',index:'quotation_first', hidden:true},
				{name:'scheduled_expedited',index:'scheduled_expedited', hidden:true},
				{name:'ocm_deliver_date',index:'ocm_deliver_date', hidden:true},
				{name:'osh_deliver_date',index:'osh_deliver_date', hidden:true},
				{name:'area',index:'area', hidden:true},
				{
					name : 'bound_out_ocm',
					index : 'bound_out_ocm',
					formatter: "select", editoptions:{value:$("#g_bound_out_ocm").val()},
					width : 35,
					align : 'center'
				},
				{name:'wip_location',index:'wip_location', width : 35},
				{name:'break_back_flg',index:'break_back_flg', hidden:true}
			],
			rowNum: 50,
			toppager: false,
			pager: "#imp_listpager",
			viewrecords: true,
			caption: "受理成果一览",
			ondblClickRow : function(rid, iRow, iCol, e) {
				showInput(rid);
			},
			multiselect: true,
			//multiboxonly: true,
			gridview: true, // Speed up
			pagerpos: 'right',
			pgbuttons: true,
			pginput: false,
			recordpos: 'left',
			viewsortcols : [true,'vertical',true],
			onSelectRow : enablebuttons2,
			onSelectAll : enablebuttons2,
			gridComplete: function() {
				enablebuttons2();
				var dataIds = jthis.getDataIDs();
				var length = dataIds.length;
				for (var i = 0; i < length; i++) {
					var rowdata = jthis.jqGrid('getRowData', dataIds[i]);
					if (rowdata["break_back_flg"] == "0" && rowdata["sterilized"] != "0") {
						jthis.find("tr#" + dataIds[i] + " td").addClass("waitTicket");
					}
				}
			}
		});

		loadImpListData();

	}
};

var dayworkReport = function() {

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : 'position_panel.do?method=makeReport',
		cache : false,
		data : {},
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function() {
			infoPop("<span>生成报表的指示已发送，请到<a href='daily_work_sheet.do'>工作记录表画面</a>确认！</span>");
		}
	});	
}

var getAutoComplete = function(){
	$.ajax({
		data : null,
		url: "customer.do?method=getAutoComplete",
		async: false, 
		beforeSend: ajaxRequestType, 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		type : "post",
		complete : function(xhrObj){
			var resInfo = $.parseJSON(xhrObj.responseText);
			customers = resInfo.customers;
		}
	});
}

$(function() {
	$("div.ui-button").button();
	$("input.ui-button").button();
	$("#importbutton").disable();
	$("#printbutton").disable();
	$("#disinfectionbutton").disable();
	$("#sterilizationbutton").disable();

	$("#returnbutton").hover(
		function(){$(this).find("div.ui-widget-content").show();},
		function(){$(this).find("div.ui-widget-content").hide();}
	);
	$("a.areacloser").hover(
		function(){$(this).addClass("ui-state-hover");},
		function(){$(this).removeClass("ui-state-hover");}
	);

	$("#editarea").hide();
	$("#detailarea").hide();
	$("#cancelbutton, #editarea span.ui-icon").click(function (){
		showList();
	});

	$("#backbutton, #detailarea span.ui-icon").click(function (){
		showEditBack();
	});

	$("#uploadbutton").click(uploadfile);
	$("#importbutton").click(reception_import);
	$("#manualbutton").click(function() {
		showInput(0,1)
	});
	$("#printbutton").click(printTicket);
	$("#outbutton").click(dayworkReport);
	$("#acceptancebutton").click(doAccept);
	$("#disinfectionbutton").click(doDisinfection);
	$("#sterilizationbutton").click(doSterilization);
	$("#returnRCbutton").click(function() {
		doReturn("RC");
	});
	$("#returnOSHbutton").click(function() {
		doReturn("OSH");
	});
	$("#resetbutton").click(function() {
		listdata = {};
		load_list();
	});

	$("#tcbutton").click(tcLoad);

	load_list();

	getAutoComplete();
	acceptted_list();

	takeWs();
});

// 工位后台推送
function takeWs() {
	$(".if_message-dialog input[value=忽略]").click(function(){
		$(".if_message-dialog").removeClass("show");
	});
	$(".if_message-dialog input[value=刷新]").click(function(){
		$(".if_message-dialog").removeClass("show");
		loadImpListData();
	});

	try {
	// 创建WebSocket  
	var position_ws = new WebSocket(wsPath + "/position");
	// 收到消息时做相应反应
	position_ws.onmessage = function(evt) {
    	var resInfo = {};
    	try {
    		resInfo = $.parseJSON(evt.data);
    		if ("refreshWaiting" == resInfo.method) {
    			$(".if_message-dialog").addClass("show");
    		}
    	} catch(e) {
    	}
    };  
    // 连接上时走这个方法  
	position_ws.onopen = function() {     
		position_ws.send("entach:"+"#00000000009#"+$("#op_id").val());
	}; 
	} catch(e) {
	}	
};

/*
* Ajax通信成功時の処理
*/
function search_handleComplete(Xhrobj, textStatus) {
};

function load_list(){
	if ($("#gbox_uld_list").length > 0 || listdata.length === 0) {
		$("#uld_list").jqGrid().clearGridData();
		$("#uld_list").jqGrid('setGridParam',{data:listdata}).trigger("reloadGrid", [{current:false}]);
	} else {
		$("#uld_list").jqGrid({
			data:listdata,
			//height: 461,
			width: 992,
			rowheight: 23,
			datatype: "local",
			colNames:['','修理单号','型号 ID','型号','机身号','同意日','level','等级','OCM出库日期','OSH发送日期','备注','销售大区', '','','','','','','','','','',''],
			colModel:[
				{name:'material_id',index:'material_id', hidden:true},
				{name:'sorc_no',index:'sorc_no', width:75},
				{name:'model_id',index:'model_id', hidden:true},
				{name:'model_name',index:'model_id', width:95},
				{name:'serial_no',index:'serial_no', width:50, align:'center'},
				{name:'agreed_date',index:'agreed_date', width:50, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}},
				{name:'level',index:'level', hidden:true},
				{name:'levelName',index:'levelName', width:35, align:'center'},
				{name:'ocm_deliver_date',index:'ocm_deliver_date', width:65, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}},
				{name:'osh_deliver_date',index:'osh_deliver_date', width:65, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}},
				{name:'comment',index:'comment', width:105, formatter:function(value, options, rData){
					var comment = " ";
					if (rData["direct_flg"] == "1") {
						comment += " 直送";
					}
					if (rData["service_repair_flg"] == "1") {
						comment += " 保内返修";
					} else if (rData["service_repair_flg"] == "2") {
						comment += " QIS";
					} else if (rData["service_repair_flg"] == "3") {
						comment += " 备品";
					}
					if (rData["fix_type"] == "1") {
						comment += " 流水线";
					} else if (rData["fix_type"] == "2") {
						comment += " 单元";
					}
					return comment.trim();
				}},
				{name:'area',index:'area', width:45, align:'center', formatter:'select', editoptions:{value:$("#g_area").val()}},
				{name:'agreed_date_o',index:'agreed_date', hidden:true,formatter: function(value, options, rData){
					var date = rData["agreed_date"];
					return date == undefined ? "":date;
				}},
				{name:'ocm_deliver_date_o',index:'ocm_deliver_date', hidden:true,formatter: function(value, options, rData){
					var date = rData["ocm_deliver_date"];
					return date == undefined ? "":date;
				}},
				{name:'osh_deliver_date_o',index:'osh_deliver_date', hidden:true,formatter: function(value, options, rData){
					var date = rData["osh_deliver_date"];
					return date == undefined ? "":date;
				}},
				{name:'customer_name',index:'customer_name', hidden:true},
				{name:'ocm',index:'ocm', width:65, hidden:true},
				{name:'ocm_rank',index:'ocm_rank', hidden:true},
				{name:'direct_flg',index:'direct_flg', hidden:true},
				{name:'selectable',index:'selectable', hidden:true},
				{name:'bound_out_ocm',index:'bound_out_ocm', hidden:true},
				{name:'fix_type',index:'fix_type', hidden:true},
				{name:'service_repair_flg',index:'service_repair_flg', hidden:true}
			],
			rowNum: 50,
			toppager: false,
			pager: "#uld_listpager",
			viewrecords: true,
			caption: "导入数据一览",
			ondblClickRow : function(rid, iRow, iCol, e) {
				showLoadInput(rid);
			},
			multiselect: true,
			gridview: true, // Speed up
			pagerpos: 'right',
			pgbuttons: true,
			pginput: false,
			recordpos: 'left',
			viewsortcols : [true,'vertical',true],
			onSelectRow : enablebuttons,
			onSelectAll : enablebuttons,
			gridComplete: enablebuttons
		});
	}
};

var insert_handleComplete = function(xhrobj, manual) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages("#editarea", resInfo.errors);
		} else {
			if (manual != 1) {
				$("#uld_listedit").dialog('close'); 
			} else {
				// 继续
				$("#material_id").val("");
				$("#edit_sorcno").val("");
				$("#edit_modelname").val("");
				$("#inp_modelname").val("");
				$("#edit_serialno").val("");
				$("#edit_ocm").val("").trigger("change");
				$("#edit_ocm_rank").val("").trigger("change");
				$("#edit_ocm_deliver_date").val("");
				$("#edit_osh_deliver_date").val("");
				$("#edit_customer_name").val("");
				$("#edit_level").val("").trigger("change");
				$("#edit_direct").val("").trigger("change");
				$("#edit_service_repair").val("").trigger("change");
				$("#edit_fix_type").val("").trigger("change");
				$("#edit_selectable").val("").trigger("change");
				$("#edit_area").val("").trigger("change");
				$("#edit_bound_out_ocm").val("").trigger("change");
				$("#edit_break_back_flg").val("0").trigger("change");
				$("#edit_modelname").next().val("");
			}

			loadImpListData();
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
}

function loadImpListData(curpagenum,selectedRows) {
	$.ajax({
		data : null,
		url: servicePath + "?method=loadData",
		async: true, 
		cache: false, 
		beforeSend: ajaxRequestType, 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		type : "post",
		complete : function(xhrObj){
			var resInfo = $.parseJSON(xhrObj.responseText);

			$("#imp_list").jqGrid().clearGridData();
			reception_listdata = resInfo.list;
			$("#imp_list").jqGrid('setGridParam', {
			datatype : 'local',
			data : reception_listdata
	        }).trigger("reloadGrid");

	        if(curpagenum){
				var maxpage = parseInt((reception_listdata.length - 1) / 50) + 1;
				if (curpagenum > maxpage) {
					curpagenum = maxpage;
				}
	        	$("#imp_list").jqGrid('setGridParam', {
		            page:curpagenum
		        }).trigger("reloadGrid");
	        }
	        
	        if(selectedRows){
	        	for(var i = 0;i < selectedRows.length;i++){
	        		var materialId = selectedRows[i];
	        		var rowID = $("#imp_list").find("td[aria\-describedby='imp_list_material_id'][title='" + materialId + "']").parent().attr("id");
	        		$("#imp_list").setSelection(rowID);
	        	}
	        }
		}
	});
}

var doAccept = function(){
	var data = {};
	var rowids = $("#imp_list").jqGrid("getGridParam", "selarrrow");
	var comm = "";
	curpagenum = $("#imp_list").jqGrid('getGridParam', 'page');   //当前页码
	var selectedRows = new Array();
	for (var i in rowids) {
		var rowdata = $("#imp_list").getRowData(rowids[i]);
		data["materials.material_id[" + i + "]"] = rowdata["material_id"];
		selectedRows.push(rowdata["material_id"]);
	}

	$.ajax({
		data : data,
		url: servicePath + "?method=doAccept",
		async: false, 
		beforeSend: ajaxRequestType, 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		type : "post",
		complete : function(){
			loadImpListData(curpagenum,selectedRows);
		}
	});

}

var doReturn = function(return_ocm){
	var jthis = $("#confirmmessage");

	jthis.text("不修理发还后，这些维修对象将会退出RVS系统中的显示，确认操作吗？");
	jthis.dialog({
		resizable : false,
		modal : true,
		dialogClass : 'ui-warn-dialog', 
		title : "返还操作确认",
		buttons : {
			"确认" : function() {
				var data = {
					"return_ocm":return_ocm
				};
				var rowids = $("#imp_list").jqGrid("getGridParam", "selarrrow");
				
				var flg = false;
				for (var i in rowids) {
					var rowdata = $("#imp_list").getRowData(rowids[i]);
					data["materials.material_id[" + i + "]"] = rowdata["material_id"];
					
					var break_back_flg = rowdata.break_back_flg;
					if (return_ocm == "OSH" && (break_back_flg == 3 || break_back_flg == 4)) {
						flg = true;
						break;
					}
				}
				
				if (flg) {
					errorPop("备品或RC品不能进行此操作。");
					return;
				}

				$.ajax({
					data : data,
					url: servicePath + "?method=doReturn",
					async: false, 
					beforeSend: ajaxRequestType, 
					success: ajaxSuccessCheck, 
					error: ajaxError, 
					type : "post",
					complete : function(){
						jthis.dialog("close");
						loadImpListData();
					}
				});
			},
			"取消" : function() {
				jthis.dialog("close");
			}
		}
	});			
}

function doDisinfection(){
	var rowids = $("#imp_list").jqGrid("getGridParam","selarrrow");
	var ids = [];
	var comm = "";

	for (var i in rowids) {
		var rowdata = $("#imp_list").getRowData(rowids[i]);
		ids[ids.length] = rowdata["material_id"];
	}

	$.ajax({
		data : {ids:ids.join(",")},
		url: servicePath + "?method=doDisinfection",
		async: false, 
		beforeSend: ajaxRequestType, 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		type : "post",
		complete : function(){
			loadImpListData();
		}
	});
}

function doSterilization(){
	var rowids = $("#imp_list").jqGrid("getGridParam","selarrrow");
	var ids = [];
	var comm = "";

	for (var i in rowids) {
		var rowdata = $("#imp_list").getRowData(rowids[i]);
		ids[ids.length] = rowdata["material_id"];
	}

	$.ajax({
		data : {ids:ids.join(",")},
		url: servicePath + "?method=doSterilization",
		async: false, 
		beforeSend: ajaxRequestType, 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		type : "post",
		complete : function(){
			loadImpListData();
		}
	});	
}

var tcLoad = function() {
	$.ajax({
		data : null,
		url: servicePath + "?method=getTcLoad",
		async: true, 
		beforeSend: ajaxRequestType, 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		type : "post",
		complete : tcLoadShow
	});
}