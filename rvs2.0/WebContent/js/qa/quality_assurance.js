/** 一览数据对象 */
var listdata = {};
var result_listdata = {};
/** 服务器处理路径 */
var servicePath = "qualityAssurance.do";
var hasPcs = (typeof pcsO === "object");

var lOptions = {};
var oOptions = {};

var pauseOptions = "";
var stepOptions = "";
var allowScan = false;

var repairActionName = "品保";
var productActionName = "最终检查";
var repairListName = "维修品";
var productListName = "制品";

var tenyearTag = "";
{
	tenyearTag = (new Date().getFullYear() + "").substring(1, 3);
};

var colNamesRepairL = ['受理时间', '同意时间', '修理完成时间', '修理单号', '型号 ID', '型号', '机身号', 'RC', '等级', '加急', '特记','工程检查票出检'];
var colModelRepairL = [{
						name : 'reception_time',
						index : 'reception_time',
						width : 40,
						align : 'center', 
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'm-d'}
					}, {
						name : 'agreed_date',
						index : 'agreed_date',
						width : 40,
						align : 'center', 
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d', newformat: 'm-d'}
					}, {
						name : 'finish_time',
						index : 'finish_time',
						width : 65,
						align : 'center', 
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d', newformat: 'm-d'}
					}, {
						name : 'sorc_no',
						index : 'sorc_no',
						width : 80
					}, {
						name : 'model_id',
						index : 'model_id',
						hidden : true
					}, {
						name : 'model_name',
						index : 'model_id',
						width : 80
					}, {
						name : 'serial_no',
						index : 'serial_no',
						width : 50,
						align : 'center'
					}, {
						name : 'ocm',
						index : 'ocm',
						width : 65, formatter: 'select', editoptions:{value: oOptions}
					}, {
						name : 'level',
						index : 'level',
						width : 35,
						align : 'center', formatter: 'select', editoptions:{value: lOptions}
					}, {
						name : 'scheduled_expedited',
						index : 'scheduled_expedited',
						width : 35,
						align : 'center', formatter: 'select', editoptions:{value: "0:;1:加急;2:直送快速"}
					}, {
						name:'status',index:'status', width:65
					},{
						name:'qa_check_time',index:'qa_check_time', width:65,align:'center',formatter:function(data, row, record) {
							if (data == null || data == "") {
								return "";
							}
							if (isL) {
								return '<input type="button" value="确认出检" class="click_start" onclick="doStart(\''+record["material_id"]+'\')"/>';
							} else {
								return "已出检";
							}
						}
					}];
var colNamesProductL = ['开始日期', '开始时间', '总组完成时间', 'QC完成时间', '型号 ID', '型号' , '机身号'];
var colModelProductL = [{
						name : 'agreed_date',
						index : 'agreed_date',
						width : 35,
						align : 'center', 
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d', newformat: 'm-d'}
					}, {
						name : 'inline_time',
						index : 'inline_time',
						width : 35,
						align : 'center', 
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'H:i'}
					}, {
						name : 'finish_time',
						index : 'finish_time',
						width : 65,
						align : 'center',
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'm-d H:i'}
					}, {
						name : 'outline_time',
						index : 'outline_time',
						width : 65,
						align : 'center',
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'm-d H:i'}
					},
				{name:'model_id',index:'model_id', hidden:true},
				{name:'model_name',index:'model_id', width:125},
				{name:'serial_no',index:'serial_no', width:50, align:'center'}];

var colNamesRepairF = ['受理时间', '同意时间', '修理完成时间', '品保时间', '修理单号',
					 '型号 ID', '型号', '机身号', 'RC', '等级', '工程检查票'];
var colModelRepairF = [{
						name : 'reception_time',
						index : 'reception_time',
						width : 35,
						align : 'center', 
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'm-d'}
					}, {
						name : 'agreed_date',
						index : 'agreed_date',
						width : 35,
						align : 'center', 
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d', newformat: 'm-d'}
					}, {
						name : 'finish_time',
						index : 'finish_time',
						width : 65,
						align : 'center', 
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d', newformat: 'm-d'}
					}, {
						name : 'quotation_time',
						index : 'quotation_time',
						width : 65,
						align : 'center', 
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'H:i'}
					}, {
						name : 'sorc_no',
						index : 'sorc_no',
						width : 105
					}, {
						name : 'model_id',
						index : 'model_id',
						hidden : true
					}, {
						name : 'model_name',
						index : 'model_id',
						width : 125
					}, {
						name : 'serial_no',
						index : 'serial_no',
						width : 50,
						align : 'center'
					}, {
						name : 'ocm',
						index : 'ocm',
						width : 65, formatter: 'select', editoptions:{value: oOptions}
					}, {
						name : 'level',
						index : 'level',
						width : 35,
						align : 'center', formatter: 'select', editoptions:{value: lOptions}
					}, {
						name : 'scheduled_expedited',
						index : 'scheduled_expedited',
						width : 85,
						align : 'center',
						formatter : function(value, options, rData){
//							if (rData['level'] > 5) {
//								return "(无)";
//							} else  {
								return "<a href='javascript:downPdf(\"" + rData['sorc_no'] + "\");' >" + rData['sorc_no'] + ".zip</a>";
//							}
		   				}
					}];
var colNamesProductF = ['总组完成时间', 'QA完成时间', '型号 ID', '型号' , '机身号', '检查责任者', '工程检查票'];
var colModelProductF = [
				{
					name : 'finish_time',
					index : 'finish_time',
					width : 65,
					align : 'center', 
					sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d', newformat: 'm-d'}
				},
				{name:'quotation_time',index:'quotation_time', width:65, align:'center',
						sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'H:i'}
				},
				{name:'model_id',index:'model_id', hidden:true},
				{name:'model_name',index:'model_id', width:125},
				{name:'serial_no',index:'serial_no', width:50, align:'center'},
				{name:'operator_name',index:'operator_name', width:50, align:'center'}, 
				{
					name : 'scheduled_expedited',
					index : 'scheduled_expedited',
					width : 85,
					align : 'center',
					formatter : function(value, options, rData){
						return "<a href='javascript:downPdf(\"" + rData['serial_no'] + "\", \"" 
							+ ("MA" + rData['model_name'].substring(0, 2) + "-" + tenyearTag) + rData['serial_no'].substring(0, 1)  +  "\");' >" 
							+ rData['serial_no'] + ".zip</a>";
					}
				}
			]

var downPdf = function(sorc_no) {
	if ($("iframe").length > 0) {
		$("iframe").attr("src", "download.do"+"?method=output&fileName="+ sorc_no +".zip&from=pcs");
	} else {
		var iframe = document.createElement("iframe");
        iframe.src = "download.do"+"?method=output&fileName="+ sorc_no +".zip&from=pcs";
        iframe.style.display = "none";
        document.body.appendChild(iframe);
	}
}

var showBreakOfInfect = function(infectString) {
	var $break_dialog = $('#break_dialog');
	$break_dialog.html(decodeText(infectString));
	if ($break_dialog.html().indexOf("opd_pop") >= 0) {
		$break_dialog.find("span").attr("id", "opd_loader_past");
	}

	var closeButtons = {
		"退出回首页":function() {
				window.location.href = "./panel.do?method=init";
		}
	}
	if (infectString.indexOf("点检") >= 0) {
		closeButtons ={
			"进行点检":function() {
				window.location.href = "./usage_check.do?from=position";
			},
			"退出回首页":function() {
					window.location.href = "./panel.do?method=init";
			}
		}
	}

	$break_dialog.dialog({
		modal : true,
		resizable:false,
		dialogClass : 'ui-error-dialog',
		width : 'auto',
		title : "工位工作不能进行",
		closeOnEscape: false,
		close: function(){
			window.location.href = "./panel.do?method=init";
		},
		buttons : closeButtons
	});
}

var doFinish = function(type) {

	var data = {};
	var empty = false;
	if (hasPcs) {
		empty = pcsO.valuePcs(data);
	}

	if (empty) {
		errorPop("工程检查票存在没有点检的选项，不能检测通过。");
	} else {
		data.position_id = $("#wk_position_id").val();
	
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : servicePath + '?method=dofinish',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj) {
				var resInfo = null;
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
				} catch (e) {
					errorPop("v");
				}
	
				$("#scanner_inputer").attr("value", "");
				$("#material_details").hide();
				$("#scanner_container").show();
				$("#devicearea").hide();
				$("#pcsarea").hide();

				hasPcs && pcsO.clearCache();

				doInit();
			}
		});
	}
};

var doForbid = function(type) {
	var data = {};
	var empty = false;
	if (hasPcs) {
		empty = pcsO.valuePcs(data);
	}

	if (empty) {
		errorPop("工程检查票存在没有点检的选项。");
	} else {
		warningConfirm("请确定是否当前维修对象未通过品保，这将会使其退回经理处要求返工。",function(){
			$.ajax({
				beforeSend : ajaxRequestType,
				async : false,
				url : servicePath + '?method=doforbid',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : function(xhrobj) {

					hasPcs && pcsO.clearCache();

					$("#scanner_inputer").attr("value", "");
					$("#material_details").hide();
					$("#scanner_container").show();
					$("#devicearea").hide();
					$("#pcsarea").hide();

//					var resInfo = $.parseJSON(xhrobj.responseText);
//					if (resInfo.alarm_messsage_id) {
//						popDefectiveAnalysis(resInfo.alarm_messsage_id, true, doInit);
//					} else {
						doInit();
//					}
				}
			});
		});
	}
};

var doPcsFinish=function(){
	var data = {};
	var empty = false;
	if (hasPcs) {
		empty = pcsO.valuePcs(data);
	}

	if (empty) {
		errorPop("存在没有点检的选项，不能确认工程检查票。");
	} else {
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : servicePath + '?method=dopcsfinish',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj){
				var resInfo = null;
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
			
					if (resInfo.errors.length > 0) {
						// 共通出错信息框
						treatBackMessages(null, resInfo.errors);
					} else {
						treatStart(resInfo);
					}
				} catch (e) {
					alert("name: " + e.name + " message: " + e.message + " lineNumber: "
							+ e.lineNumber + " fileName: " + e.fileName);
				};
			}
		});
	}
};

function acceptted_list(result_listdata) {

	if ($("#gbox_exd_list").length > 0) {
		$("#exd_list").jqGrid().clearGridData();
		$("#exd_list").jqGrid('setGridParam', {data : result_listdata}).trigger("reloadGrid", [{current : false}]);
	} else {
		$("#exd_list").jqGrid({
			toppager : true,
			data : result_listdata,
			// height: 461,
			width : 992,
			rowheight : 23,
			datatype : "local",
			colNames: (g_depa == 1 ? colNamesRepairF : colNamesProductF),
			colModel: (g_depa == 1 ? colModelRepairF : colModelProductF),
			rowNum : 20,
			toppager : false,
			pager : "#exd_listpager",
			viewrecords : true,
			hidegrid : false,
			caption: "今日" + (g_depa == 1 ? repairActionName : productActionName) + "完成一览",
			gridview : true, // Speed up
			pagerpos : 'right',
			pgbuttons : true,
			pginput : false,
			recordpos : 'left',
			viewsortcols : [true, 'vertical', true],
			gridComplete : function() {
			}
		});
	}
};

var doInit_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {

			lOptions = resInfo.lOptions;
			oOptions = resInfo.oOptions;

			if (resInfo.workstauts == -1) {
				showBreakOfInfect(resInfo.infectString);
				return;
			}

			load_list(resInfo.waitings);
			acceptted_list(resInfo.finished);
			if (resInfo.pauseOptions) pauseOptions = resInfo.pauseOptions;
			if (resInfo.stepOptions) stepOptions = resInfo.stepOptions;

			if (!stepOptions) {
				$("#stepbutton").hide().attr("hide", "empty");
			} else {
				$("#stepbutton").show();
			}

			// 存在进行中作业的时候
			if(resInfo.workstauts != 0) {
				treatStart(resInfo);

				hasPcs && pcsO.loadCache();
			} else {

				posClockObj.stopClock();
				$("#uld_listarea").addClass("waitForStart");
				allowScan = true;
				if ($("#scanner_inputer").val()) {
					doStart();
				}
			}
		}

	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var doInit=function(){
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=jsinit',
		cache : false,
		data : {position_id : $("#wk_position_id").val()},
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : doInit_ajaxSuccess
	});
};

var g_depa = 0;
var scanner_length = 11;
var isL = $("#passbutton").length > 0;

$(function() {
	g_depa = $("#department").val();
	if (g_depa == 2) {
		document.title = productActionName;
		$("#executearea .areatitle").text(productActionName + "处理");
		$("#shipbutton").val(productActionName);
		scanner_length = 7;
		$("#show_sorc_no").closest("tr").hide()
			.nextAll().hide()
			.end().closest("tbody")
			.append('<tr><td class="ui-state-default td-title">作业标准时间</td><td id="dtl_standard_time" style="text-align:right;"></td><td class="ui-state-default td-title">作业经过时间</td>' +
					'<td id="dtl_process_time"><div class="roll_cell"><div class="roll_seconds">0 1 2 3 4 5 6 7 8 9</div></div><div class="roll_cell"><div class="roll_tenseconds">0 1 2 3 4 5 6</div></div><label style="float:right;"></label></td></tr>' +
					'<tr><td class="ui-state-default td-title">标准进行度</td><td colspan=3><div class="waiting tube" id="p_rate" style="height: 20px; margin: auto;"></div></td></tr>');
	}
	isL = $("#passbutton").length > 0;

	// 画面项目效果
	$("input.ui-button").button();
	$("a.areacloser").hover(function() {
		$(this).addClass("ui-state-hover");
	}, function() {
		$(this).removeClass("ui-state-hover");
	});

	// 输入框触发，配合浏览器
	$("#scanner_inputer").keypress(function(){
	if (this.value.length === scanner_length) {
		doStart();
	}
	});
	$("#scanner_inputer").keyup(function(){
	if (this.value.length >= scanner_length) {
		doStart();
	}
	});

	$("#pcscombutton").click(doPcsFinish);
	$("#passbutton").click(doFinish);
	$("#forbidbutton").click(doForbid);
	$("#pausebutton").click(makePause);
	$("#continuebutton").click(endPause);
	$("#stepbutton").click(makeStep);

	if ($("#devicearea").length > 0) {
		$("#devicearea div").removeClass("dwidth-full").addClass("dwidth-middleright");
		$("#devicearea").next().removeClass("areaencloser");
	}
	hasPcs && pcsO.init($("#pcsarea"), true);

	if (typeof posClockObj == "undefined") {
		loadJs("js/inline/position_panel_clock.js",
		function(){
			posClockObj.init(null, $("#dtl_standard_time"), $("#dtl_process_time"), $("#p_rate"));
			doInit();
		});
	} else {
		doInit();
	}
});

function load_list(t_listdata) {

	if ($("#gbox_uld_list").length > 0) {
		$("#uld_list").jqGrid().clearGridData();
		$("#uld_list").jqGrid('setGridParam', {data : t_listdata}).trigger("reloadGrid", [{current : false}]);
	} else {
		$("#uld_list").jqGrid({
			toppager : true,
			data : t_listdata,
			// height: 461,
			width : 992,
			rowheight : 23,
			datatype : "local",
			colNames: (g_depa == 1 ? colNamesRepairL : colNamesProductL),
			colModel: (g_depa == 1 ? colModelRepairL : colModelProductL),
			rowNum : 20,
			toppager : false,
			pager : "#uld_listpager",
			viewrecords : true,
			caption : "待" + (g_depa == 1 ? repairActionName : productActionName) + (g_depa == 1 ? repairListName : productListName) + "一览",
			gridview : true, // Speed up
			pagerpos : 'right',
			pgbuttons : true,
			pginput : false,
			recordpos : 'left',
			viewsortcols : [true, 'vertical', true],
			gridComplete : function() {
				$("#uld_list").find(".click_start").button();
			}
		});
	}

};

function expeditedText(scheduled_expedited) {
	if (scheduled_expedited === '2') return "直送快速";
	if (scheduled_expedited === '1') return "加急";
	return "";
}

var treatStart = function(resInfo) {
	$("#scanner_inputer").attr("value", "");
	$("#scanner_container").hide();
	$("#material_details").show();
	$("#devicearea").show();
	$("#pcsarea").show();

	var mform = resInfo.mform;
	if (mform) {
		$("#pauseo_material_id").val(mform.material_id);
		$("#show_model_name").text(mform.model_name);
		$("#show_serial_no").text(mform.serial_no);
		$("#show_sorc_no").text(mform.sorc_no);
		$("#show_agreed_date").text(mform.agreed_date);
		$("#show_finish_time").text(mform.finish_time);
		$("#show_level").text(mform.levelName);
		$("#show_scheduled_expedited").text(expeditedText(mform.scheduled_expedited));
	}

	$("#hidden_workstauts").val(resInfo.workstauts);

	// 工程检查票
	if (resInfo.pcses && resInfo.pcses.length > 0 && hasPcs) {
		pcsO.generate(resInfo.pcses, true, $("#passbutton").length > 0, resInfo.pcs_limits);
	}

	if (resInfo.peripheralData && resInfo.peripheralData.length > 0) {
		showPeripheral(resInfo);

		if (resInfo.workstauts == 4) {
			$("#device_details table tbody").find(".manageCode").enable();
			$("#device_details table tbody").find(".manageCode").trigger("change");
			$("#pcsarea").hide();
			$("#finishcheckbutton").enable();
			hasPcs && pcsO.clear();
		} else {
			$("#device_details table tbody").find(".manageCode").disable();
			$("#device_details table tbody").find("input[type=button]").disable();
			$("#finishcheckbutton").disable();
		}
	} else {
		$("#finishcheckbutton").disable();
	}

	if (resInfo.workstauts == 1) {
		$("#pcscombutton").show();
		// if ($("#pcs_pages input").length > 0) $("#pcscombutton").disable(); // V2 disable
		$("#forbidbutton").hide();
		$("#passbutton,#stepbutton").hide();
		$("#pausebutton").show();
		$("#continuebutton").hide();
	} else if (resInfo.workstauts == 1.5) {
		$("#pcscombutton").hide();
		$("#forbidbutton").show();
		$("#passbutton,#stepbutton").not("[hide]").show();
		$("#pausebutton").show();
		$("#continuebutton").hide();
		$("#devicearea").hide();
	} else if (resInfo.workstauts == 2) {
		$("#pcscombutton").show();
		// if ($("#pcs_pages input").length > 0) $("#pcscombutton").disable(); // V2 disable
		$("#forbidbutton").hide();
		$("#passbutton,#stepbutton").hide();
		$("#pausebutton").hide();
		$("#continuebutton").show();
	} else if (resInfo.workstauts == 2.5) {
		$("#pcscombutton").hide();
		$("#forbidbutton").hide();
		$("#passbutton,#stepbutton").hide();
		$("#pausebutton").hide();
		$("#continuebutton").show();
		$("#devicearea").hide();
	} else if (resInfo.workstauts == 1.9) {
		$("#pcsarea").hide();
		$("#pcscombutton").hide();
		$("#forbidbutton").show();
		$("#passbutton,#stepbutton").not("[hide]").show();
		$("#pausebutton").hide();
		$("#continuebutton").hide();
	} else if (resInfo.workstauts == 4) {
		$("#pcscombutton").show().disable();
		$("#forbidbutton").hide();
		$("#passbutton").hide();
		$("#pausebutton").show();
		$("#continuebutton").hide();
	} else if (resInfo.workstauts == 5) {
		$("#pcscombutton").show().disable();
		$("#forbidbutton").hide();
		$("#passbutton").hide();
		$("#pausebutton").hide();
		$("#continuebutton").show();
	}

	if (resInfo.leagal_overline) {
		posClockObj.setLeagalAndSpent(resInfo.leagal_overline, resInfo.spent_mins, resInfo.spent_secs);
	}

	if (resInfo.workstauts == 1 || resInfo.workstauts == 1.5 || resInfo.workstauts == 4) {
		posClockObj.startClock(resInfo.spent_mins, resInfo.spent_secs);
	} else {
		posClockObj.pauseClock();
	}

	$("#uld_listarea").removeClass("waitForStart");
};

var doStart_ajaxSuccess=function(xhrobj){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			treatStart(resInfo);
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var doStart=function(material_id){
	if (!allowScan) return;
	var data = {
		material_id : material_id || $("#scanner_inputer").val()
	}

	$("#scanner_inputer").attr("value", "");

	hasPcs && pcsO.clearCache();

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doscan',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : doStart_ajaxSuccess
	});
};
/** 暂停信息弹出框 */
var makePauseDialog = function(jBreakDialog) {
	jBreakDialog.dialog({
		title : "暂停信息编辑",
		width : 480,
		show: "blind",
		height : 'auto' ,
		resizable : false,
		modal : true,
		minHeight : 200,
		close : function(){
			jBreakDialog.html("");
		},
		buttons : {
			"确定":function(){
				if ($("#pauseo_edit").parent().valid()) {
					var data = {
						reason : $("#pauseo_edit_pause_reason").val(),
						comments : $("#pauseo_edit_comments").val(),
						workstauts : $("#hidden_workstauts").val()
					}
				
					// Ajax提交
					$.ajax({
						beforeSend : ajaxRequestType,
						async : false,
						url : servicePath + '?method=dopause',
						cache : false,
						data : data,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : function(xhr, status) {
							jBreakDialog.dialog("close");
							// doFinish_ajaxSuccess(xhr, status);
							$("#pausebutton").hide();
							$("#continuebutton").show();
							$("#pcscombutton").hide();
							$("#forbidbutton").hide();
							$("#passbutton,#stepbutton").hide();

							posClockObj.pauseClock();
						}
					});
				}
			}, "关闭" : function(){ $(this).dialog("close"); }
		}
	});
}

/** 暂停信息 */
var makePause = function() {
	var jBreakDialog = $("#break_dialog");
	if (jBreakDialog.length === 0) {
		$("body.outer").append("<div id='break_dialog'/>");
		jBreakDialog = $("#break_dialog");
	}

	jBreakDialog.hide();
	// 导入暂停画面
	jBreakDialog.load("widget.do?method=pauseoperator",
		function(responseText, textStatus, XMLHttpRequest) {
			// 设定暂停理由
			$("#pauseo_edit").show();
			$("#pauseo_show").hide();
			$("#pauseo_edit_pause_reason").html(pauseOptions);
			$("#pauseo_edit_pause_reason").select2Buttons();

			$("#pauseo_edit_pause_reason").val("49").trigger("change");
			$("#pauseo_edit").parent().validate({
				rules : {
					comments : {
						required : function() {
							return "49" === $("#pauseo_edit_pause_reason").val();
						}
					}
				}
			});
			makePauseDialog(jBreakDialog);
		});
};

/** 暂停重开 */
var endPause = function() {
	// 重新读入刚才暂停的维修对象
	var data = {
		material_id : $("#pauseo_material_id").val(),
		workstauts : $("#hidden_workstauts").val()
	};

	// 无论如何关闭暂停窗口
	if ($("#break_dialog").html() != "") {
		$("#break_dialog").dialog("close");
	}

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doendpause',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : doStart_ajaxSuccess
	});
};

/** 正常中断 **/
var makeStep = function(){
	var data = {
		material_id : $("#pauseo_material_id").val()
	}
	
	var jBreakDialog = $("#break_dialog");
	if (jBreakDialog.length === 0) {
		$("body.outer").append("<div id='break_dialog'/>");
		jBreakDialog = $("#break_dialog");
	}

	jBreakDialog.hide();

	// 导入中断画面
	jBreakDialog.load("widget.do?method=breakoperator",function(responseText, textStatus, XMLHttpRequest) {

		// 设定中断理由
		$("#break_reason").html(stepOptions);
		$("#break_reason").select2Buttons();

		$("#breakForm").validate({
			rules : {
				break_reason : {
					required : true
				}
			}
		});
		makeStepDialog(jBreakDialog);
	});
	jBreakDialog.show();
};

/** 中断信息弹出框 */
var makeStepDialog = function(jBreakDialog) {
	jBreakDialog.dialog({
		title : "中断生成",
		width : 480,
		show: "blind",
		height : 'auto' ,
		resizable : false,
		modal : true,
		minHeight : 200,
		close : function(){
			jBreakDialog.html("");
		},
		buttons : {
			"确定":function(){
				if ($("#breakForm").valid()) {
					var data = {
						reason : $("#break_reason").val(),
						comments : $("#edit_comments").val()
					}
					
					var invalid = false;
					if (hasPcs) {
						invalid = pcsO.valuePcs(data, true);
					}
					if (invalid) {
						var $invalidInputs = $("#pcs_contents input:text.invalid");
						if ($invalidInputs.length > 0) {
							jBreakDialog.dialog("close");
							errorPop("存在不符合输入范围的输入项，请检查改正或暂时删除后再实行中断。", $invalidInputs.eq(0));
							return;
						}
					}
					
					if (parseInt($("#break_reason").val()) > 70 && $("#pcs_contents input").length > 0) {
						if ($('div#errstring').length == 0) {
							$("body").append("<div id='errstring'/>");
						}
						$('div#errstring').show();
						$('div#errstring').dialog({
							dialogClass : 'ui-warn-dialog',
							modal : true,
							width : 450,
							title : "提示信息",
							buttons :{
								"确定":function(){
									// Ajax提交
									$.ajax({
										beforeSend : ajaxRequestType,
										async : false,
										url : servicePath + '?method=dobreak',
										cache : false,
										data : data,
										type : "post",
										dataType : "json",
										success : ajaxSuccessCheck,
										error : ajaxError,
										complete : function(xhrobj, textStatus) {
											$("#scanner_inputer").attr("value", "");
											$("#material_details").hide();
											$("#scanner_container").show();
											$("#devicearea").hide();
											$("#pcsarea").hide();

											hasPcs && pcsO.clearCache();

											doInit();
											jBreakDialog.dialog("close");
										}
									});
									$('div#errstring').dialog("close");
								},
								"取消":function(){
									$('div#errstring').dialog("close");
								}
							}
						});
						$('div#errstring').html("<span class='errorarea'>请确定与您作业相关的工程检查票项目已经输入或点检。</span>");
					} else {
						// Ajax提交
						$.ajax({
							beforeSend : ajaxRequestType,
							async : false,
							url : servicePath + '?method=dobreak',
							cache : false,
							data : data,
							type : "post",
							dataType : "json",
							success : ajaxSuccessCheck,
							error : ajaxError,
							complete : function(xhrobj, textStatus) {
								$("#scanner_inputer").attr("value", "");
								$("#material_details").hide();
								$("#scanner_container").show();
								$("#devicearea").hide();
								$("#pcsarea").hide();
								doInit();
								jBreakDialog.dialog("close");
							}
						});
					}
				}
			},
			"关闭":function(){ 
				$(this).dialog("close"); 
			}
		}
	})
}