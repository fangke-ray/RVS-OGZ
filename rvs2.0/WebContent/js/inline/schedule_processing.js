/** 模块名 */
var modelname = "维修对象";
/** 服务器处理路径 */
var servicePath = "scheduleProcessing.do";
var fff = '已完成';
var position_eval_data = 2;
var caseId = 0;
var keepSearchData;

$(function() {
	var forManufactor = ($("#listarea .areatitle").html() === "制品一览");
	if (!forManufactor) {
		setInterval(findSchedule, "300000");
	}

	$("input.ui-button").button();
	$("#removefromplanbutton").disable();

	$("#infoes").buttonset();
	$("a.areacloser").hover(
		function (){$(this).addClass("ui-state-hover");}, 
		function (){$(this).removeClass("ui-state-hover");}
	);

	$("#position_eval, #position_eval2").click(function(){
		if (this.value === "正在") {
			this.value = "通过";
			position_eval_data = 2;
		} else if (this.value === "通过") {
			this.value = "未到";
			position_eval_data = 0;
		} else if (this.value === "未到") {
			this.value = "正在";
			position_eval_data = 1;
		}
	});

	$("#searchbutton").addClass("ui-button-primary");
	$("#searchbutton").click(function (){
		findit();
	});

	$("input[name=expedited]").bind('click', function(){
		$("#scheduled_expedited").val($(this).val());
	});
	$("input[name=bo]").bind('click',function(){
		$("#cond_work_procedure_order_template").val($(this).val());
	});
	
	$("#resetbutton").click(function(){
		resetSearch();
	});

	$("#searchpro1button").click(function(){
		resetSearch();
		var today = new Date();
		var sToday = today.getFullYear() + "/";
		if (today.getMonth() < 9) {
			sToday += "0" + (today.getMonth() + 1);
		} else {
			sToday += (today.getMonth() + 1);
		}
		if (today.getDate() < 10) {
			sToday += "/" + "0" + today.getDate();
		} else {
			sToday += "/" + today.getDate();
		}
		$("#search_complete_date_start").val(sToday);
		$("#search_complete_date_end").val(sToday);
		findit();
	});

	$("#searchpro2button").click(function(){
		resetSearch();
		$("#position_eval").val("通过");
		position_eval_data = 2;
		if ($("#pReferChooser .referId:contains('00000000046')").length) {
			$("#search_position_id").val("00000000046");
			$("#inp_position_id").val("出检");
		} else {
			$("#search_position_id").val("00000000108");
			$("#inp_position_id").val("QA");
		}
		findit();
	});

	$("#search_scheduled_date_start, #search_scheduled_date_end, #search_arrival_plan_date_start, #search_arrival_plan_date_end, " +
			"#search_complete_date_start, #search_complete_date_end").datepicker({
		showButtonPanel:true,
		currentText: "今天"
	});
	
	$("#resignbutton").click(function() {
		var rowid = $("#list").jqGrid("getGridParam", "selrow");
	
		var rowData = $("#list").getRowData(rowid);
	
		selectedMaterial.material_id = rowData["material_id"];
		selectedMaterial.sorc_no = rowData["sorc_no"];
		selectedMaterial.model_name = rowData["model_name"];
		selectedMaterial.serial_no = rowData["serial_no"];
		selectedMaterial.line_name = rowData["line_name"];
		process_resign();
	}); // v1 close
	$("#pausebutton").click(break_plan);

	$("#plandecbutton, #planallbutton, #plannsbutton, #plancombutton").click(function(){
		var lineId = $(this).val();
		$("#select_line").val(lineId)
		findSchedule();
	});
	$("#planoutbutton").click(findOutSchedule);
	$("#decplannedbutton").click(updateDecPlannedSchedule);
	$("#nsplannedbutton").click(updateNSPlannedSchedule);
	$("#complannedbutton").click(updateComPlannedSchedule);
	$("#removefromplanbutton").click(deleteSchedule);

	$("#searcharea span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	$("#listarea span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$("#functionarea").show("fade");
			$("#gbox_list").show("blind");
			$("#chooser").show("blind");
			$("#tuli").show("fade");
		} else {
			$("#tuli").hide("fade");
			$("#chooser").hide("blind")
			$("#gbox_list").hide("blind");
			$("#functionarea").hide("fade");
		}
	});
	
	$("#search_level, #search_section").select2Buttons();
	$("#search_category_id").select2Buttons();
	setReferChooser($("#search_position_id"));

	$("#cond_work_procedure_order_template_flg_set").buttonset();
	$("#scheduled_expedited_set").buttonset();
	$("#colchooser").buttonset();
	
	$("#colchooser input:checkbox").click(function (){
		var colid = $(this).attr("id");
		var colchk = $(this).attr("checked");

		var colsname;
		if ("colchooser_rec" === colid) {
			colsname = ['agreed_date','inline_time','section_name','ocmName'];
		} else if ("colchooser_par" === colid) {
			colsname = ['arrival_plan_date','bo_contents','order_date'];
		} else if ("colchooser_prc" === colid) {
			colsname = ['processing_position','dismantle_time','dec_finish_date','ns_processing_position','ns_finish_date','break_message'];
		} else if ("colchooser_she" === colid) {
			colsname = ['dec_plan_date','ns_plan_date','com_plan_date','am_pm','scheduled_manager_comment'];
		} else {
			return;
		}

		if ("checked" === colchk) {
			$("#list").jqGrid('showCol', colsname);
		} else {
			$("#list").jqGrid('hideCol', colsname);
		}

		$("#list").jqGrid('setGridWidth', '1248');

	});
		
	$("#reportbutton").click(exportReport);
	$("#todayreportbutton").click(exportSchedule);
	$("#forbutton").click(moveOutOfLine);
	if (typeof(chooseAndPrintTickets) == "function") {
		$("#ticketbutton").click(chooseAndPrintTickets);
	}
	if (typeof(setReaccpect) == "function") {
		$("#reacceptbutton").click(setReaccpect);
	}

	initGrid();
	
	findboth();
});
var resetSearch = function() {
	$("#search_category_id").val("").trigger("change");
	$("#search_sorc_no").val("");
	$("#search_serialno").val("");
	$("#search_scheduled_date_start").val("");
	$("#search_scheduled_date_end").val("");
	$("#search_section").val("").trigger("change");
	$("#search_level").val("").trigger("change");
	$("#search_arrival_plan_date_start").val("");
	$("#search_arrival_plan_date_end").val("");
	$("#search_complete_date_start").val("");
	$("#search_complete_date_end").val("");
	$("#search_position_id").val("");
	$("#inp_position_id").val("");
	$("#scheduled_expedited_a").attr("checked","checked").trigger("change");
	$("#cond_work_procedure_order_template").val("");
	$("#cond_work_procedure_order_template_a").attr("checked","checked").trigger("change");
	$("#position_eval").val("通过");
	position_eval_data = 1;
}

function exportReport() {
	var data = {
		"sorc_no" : $("#search_sorc_no").val(),
		"serial_no" : $("#search_serialno").val(),
		"scheduled_date_start" : $("#search_scheduled_date_start").val(),
		"scheduled_date_end" : $("#search_scheduled_date_end").val(),
		"level" : $("#search_level").val(),
		"section_id" : $("#search_section").val(),
		"arrival_plan_date_start" : $("#search_arrival_plan_date_start").val(),
		"arrival_plan_date_end" : $("#search_arrival_plan_date_end").val(),
		"complete_date_start" : $("#search_complete_date_start").val(),
		"complete_date_end" : $("#search_complete_date_end").val(),
		"position_id" : $("#search_position_id").val(),
		"position_eval" : position_eval_data,
		"scheduled_expedited": $("#scheduled_expedited").val(),
		"bo_flg": $("#cond_work_procedure_order_template").val(),
		"category_id":$("#search_category_id").val()
		};

	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: servicePath + '?method=report', 
		cache: false, 
		data: data, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete: function(xhrobj, textStatus){
			var resInfo = null;

			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
		
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages("#searcharea", resInfo.errors);
			} else {
				var iframe = document.createElement("iframe");
	            iframe.src = servicePath+"?method=export&filePath=" + resInfo.filePath;
	            iframe.style.display = "none";
	            document.body.appendChild(iframe);
			}
						
		}
	});
}

function exportSchedule() {
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: servicePath + '?method=reportSchedule', 
		cache: false, 
		data: [], 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete:  function(xhrobj, textStatus){
			var resInfo = null;

			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
		
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages("#searcharea", resInfo.errors);
			} else {
				var iframe = document.createElement("iframe");
	            iframe.src = servicePath+"?method=exportSchedule&filePath=" + resInfo.filePath;
	            iframe.style.display = "none";
	            document.body.appendChild(iframe);
			}
						
		}
	});
}

function initGrid() {
	var forManufactor = ($("#listarea .areatitle").html() === "制品一览");
	$("#list").jqGrid({
			data: [], 
			height: 231, 
			width: 1248, 
			rowheight: 23, 
			datatype: "local", 
			colNames: ['维修对象ID','修理单号','机种', '型号 ID', '型号', '机身号', '等级', '直送', '客户<br>同意','RC','投线日期','入库<br>预定日',
			'零件<br>到货', '零件<br>BO', '零件缺品详细', (forManufactor ? '课室' : '维修课'),'进展<br>工位号','进展<br>工位名','拆镜<br>时间','零件订<br>购安排','分解产<br>出安排','分解实<br>际产出'
			,'NS进<br>展工位','NS产<br>出安排','NS实<br>际产出','纳期','总组出<br>货安排','AM/PM','加急','工程内发现/不良','break_message_level','remain_days','备注','fix_type'],
			colModel: [
				{name:'material_id', index:'material_id', key: true,hidden:true},
				{name:'sorc_no',index:'sorc_no', width:105, hidden:forManufactor},
				{name:'category_name',index:'category_name', width:50},	
				{name:'model_id',index:'model_id', hidden:true},				
				{name:'model_name',index:'model_name', width:125},
				{name:'serial_no',index:'serial_no', width:50},
				{name:'levelName',index:'levelName', width:35, align:'center', hidden:forManufactor},
				{name:'direct_flg',index:'direct_flg', align:'center', width:30, formatter:'select', editoptions:{value:"1:直送;0:"}, hidden:forManufactor},
				{name:'agreed_date',index:'agreed_date', width:50, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'ocmName',index:'ocmName', width:65, hidden:true},
				{name:'inline_time',index:'inline_time', width:50, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'arrival_plan_date',index:'arrival_plan_date', width:50, align:'center', 
				formatter:function(a,b,row) {
					if ("9999/12/31" == a) {
						return "未定";
					}
					
					if (a) {
						var d = new Date(a);
						return (d.getMonth()+1)+"-"+d.getDate();
					}
					
					return "";
				}, hidden:forManufactor},
				{name:'arrival_date',index:'arrival_date', width:50, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'bo_flg',index:'bo_flg', width:50, align:'center',formatter:function(a,b,row){
					if (a && a==="1") {
						return "BO"
					}
					return "";
				}, hidden:forManufactor},
				{name:'bo_contents',index:'bo_contents', width:150, hidden:true, formatter:function(a,b,row){
					var content = null;
					try {
						eval('content =' + decodeText(data));
						if (content) {
//									return "分解缺品零件:" + content.dec +",NS缺品零件:"+ content.ns +",总组缺品零件:"+content.com;
							var value = "";
							for(var key in content){
								if(key == "00000000000"){//全局
									value += (" 全局: " + encodeText(content[key]));
								}else{
									value += (" " +$('#search_bo_occur_line option[value="' + key +  '"]').text().trim() + ": " + encodeText(content[key]));
								}
							}
							return value;
						}else{
							return "";
						}
					} catch (e) {
						return "";
					}

					return "";
				}},
				{name:'section_name',index:'section_name', width:35, hidden:!forManufactor},
				{name:'process_code',index:'process_code', width:35, align:'center', hidden:!forManufactor},
				{name:'processing_position',index:'processing_position', width:35, align:'center', hidden:!forManufactor},
				{name:'dismantle_time',index:'dismantle_time', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'order_date',index:'order_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'dec_plan_date',index:'dec_plan_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'dec_finish_date',index:'dec_finish_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'ns_processing_position',index:'ns_processing_position', width:35, align:'center', hidden:true},
				{name:'ns_plan_date',index:'ns_plan_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'ns_finish_date',index:'ns_finish_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'com_plan_date',index:'com_plan_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'com_finish_date',index:'com_finish_date', width:35, align:'center', formatter:function(a,b,row) {
					if ("9999/12/31" == a) {
						return "未定";
					}
					
					if (a) {
						var d = new Date(a);
						return mdTextOfDate(d);
					}
					
					return "";
				}},
				{name:'am_pm',index:'am_pm', width:35, align:'center', hidden:true,
					formatter:function(a,b,row){
						if (a && a==="1") {
							return "AM"
						} else if (a && a==="2"){
							return "PM"
						} else if (a && a==="3"){
							return "AM顺延一天"
						} else if (a && a==="4"){
							return "PM顺延一天"
						}
						return "";
					}},
				{name:'scheduled_expedited',index:'scheduled_expedited', width:35, align:'center',
					formatter:function(a,b,row){
						if (a && a==="2") {
							return "直送快速"
						}
						if (a && a==="1") {
							return "加急"
						}
						return "";
					}},
				{name:'break_message',index:'break_message', width:80, hidden:!forManufactor},
				{name:'break_message_level',index:'break_message_level', width:80, hidden:true},
				{name:'remain_days',index:'remain_days', width:80, hidden:true},
				{name:'scheduled_manager_comment',index:'scheduled_manager_comment', width:80, hidden:true},
				{name:'fix_type',index:'fix_type', width:80, hidden:true}
			],
			rowNum: 50, 
			toppager: false, 
			pager: "#listpager", 
			viewrecords: true, 
			caption: "", 
			rownumbers : true,
			ondblClickRow: function(rid, iRow, iCol, e) {
				if (forManufactor) return;
				var data = $("#list").getRowData(rid);
				var material_id = data["material_id"];
				edit_schedule_popMaterialDetail(material_id, true);
			}, 
			multiselect: true, 
			gridview: true, // Speed up
			pagerpos: 'right', 
			pgbuttons: true, 
			pginput: false, 
			recordpos: 'left', 
			hidegrid: false, 
			viewsortcols: [true, 'vertical', true], 
			onSelectRow: enablebuttons, 
			onSelectAll: enablebuttons, 
			gridComplete: function(){enablebuttons(); showremain();}
		});
		
		
		$("#planned_list").jqGrid({
			toppager: true, 
			data: [], 
			height: 231, 
			width: 1248, 
			rowheight: 23, 
			datatype: "local", 
			colNames: [ '维修对象ID','','计划工程', '修理单号', '机种', '型号 ID', '型号', '机身号', '等级', '直送', '客户<br>同意', 'RC', '投线日期', '入库<br>预定日', '零件<br>BO', '加急', '维修课', '进展<br>工位', '总组出<br>货安排','完成时间','remain_days'], 
			colModel: [
				{name: 'material_id', index: 'material_id', hidden: true},
				{name: 'line_id', index: 'line_id', hidden: true},
				{name: 'line_name', index: 'line_name', width: 60},
				{name: 'sorc_no', index: 'sorc_no', width: 105},
				{name:'category_name',index:'category_name', width:50},	
				{name: 'model_id', index: 'model_id', hidden: true},
				{name: 'model_name', index: 'model_name', width: 125},
				{name: 'serial_no', index: 'serial_no', width: 50},
				{name: 'levelName', index: 'levelName', width: 35, align: 'center'},
				{name:'direct_flg',index:'direct_flg', align:'center', width:30, formatter:'select', editoptions:{value:"1:直送;0:"}},
				{name: 'agreed_date', index: 'agreed_date', width: 50, align: 'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}},
				{name:'ocmName',index:'ocmName', width:65},
				{name: 'inline_time', index: 'inline_time', width: 50, align: 'center', hidden: true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'arrival_plan_date',index:'arrival_plan_date', width:50, align:'center', 
				formatter:function(a,b,row) {
							if ("9999/12/31" == a) {
								return "未定";
							}
							
							if (a) {
								var d = new Date(a);
								return (d.getMonth()+1)+"-"+d.getDate();
							}
							
							return "";
						}},
				{name:'bo_flg',index:'bo_flg', width:50, align:'center',formatter:function(a,b,row){
							if (a && a==="1") {
								return "BO"
							}
							return "";
						}},
				{name:'scheduled_expedited',index:'scheduled_expedited', width:35, align:'center',formatter:function(a,b,row){
						if (a && a==="2") {
							return "直送快速"
						}
						if (a && a==="1") {
							return "加急"
						}
						return "";
				}},
				{name:'section_name',index:'section_name', width:35},
				{name: 'processing_position', index: 'processing_position', width: 35, align: 'center', formatter:function(a,b,row){
					if (row['finish_date']) {
						return "已完成";
					}
					return a;
				}},
				{name: 'com_plan_date', index: 'com_plan_date', width: 35, align: 'center', formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name: 'finish_date', index:'finish_date', hidden:true},
				{name:'remain_days',index:'remain_days', width:80, hidden:true}
			], 
//			rowNum: 50, 
			toppager: false, 
			pager: "#planned_listpager", 
			viewrecords: true, 
			caption: "", 
			ondblClickRow: function(rid, iRow, iCol, e) {
				var data = $("#planned_list").getRowData(rid);
				var material_id = data["material_id"];
				edit_schedule_popMaterialDetail(material_id, true);
			}, 
			multiselect: true, 
			gridview: true, // Speed up
			pagerpos: 'right', 
			pgbuttons: true, 
			rownumbers : true,
			pginput: false, 
			recordpos: 'left', 
			hidegrid: false, 
			deselectAfterSort: false, 
			viewsortcols: [true, 'vertical', true], 
			onSelectRow: enablebuttons2, 
			onSelectAll: enablebuttons2, 
			gridComplete: checkout_complete
		});
}

var treatPlanedList = function(listdata) {
	$("#planned_list").jqGrid().clearGridData();
	$("#planned_list").jqGrid('setGridParam', {data: listdata}).trigger("reloadGrid", [{current: false}]);
}

function planned_list_handleComplete(xhrobj, textStatus) {

	var resInfo = null;

	// 以Object形式读取JSON
	eval('resInfo =' + xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages("#searcharea", resInfo.errors);
	} else {
		// 读取一览
		var planned_listdata = resInfo.schedule_list;
		treatPlanedList(planned_listdata);
		$("#list").jqGrid("resetSelection");
		enablebuttons();
		$("#planned_list").jqGrid("resetSelection");
		enablebuttons2();
	}
};

var edit_schedule_popMaterialDetail = function(material_id, is_modal){
	
	var this_dialog = $("#detail_dialog");
	if (this_dialog.length === 0) {
		$("body.outer").append("<div id='detail_dialog'/>");
		this_dialog = $("#detail_dialog");
	}

	this_dialog.html("");
	this_dialog.hide();
	// 导入详细画面
	this_dialog.load("widget.do?method=materialDetail&material_id=" + material_id , function(responseText, textStatus, XMLHttpRequest) {
		$.ajax({
			data:{
				"id": material_id		
			},
			url : "material.do?method=getDetial",
			type : "post",
			complete : function(xhrobj, textStatus){
				var resInfo = null;
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					setLabelText(resInfo.materialForm, resInfo.materialPartialFormList, resInfo.processForm, resInfo.timesOptions, material_id);
					if (caseId == 2) {
						case2();
					} else {
						case0();
					}
				} catch (e) {
					alert("name: " + e.name + " message: " + e.message + " lineNumber: "
							+ e.lineNumber + " fileName: " + e.fileName);
				};
				
				this_dialog.dialog({
					position : [400, 20],
					title : "维修对象详细画面",
					width : 820,
					show : "",
					height :  'auto',
					resizable : false,
					modal : is_modal,
					buttons :  {
						"取消":function(){
							this_dialog.dialog('close');
						}
					}
				});
			
				this_dialog.show();
			}
		});
	});

}

var break_plan = function() {
	var selectedIds  = $("#list").jqGrid("getGridParam","selarrrow"); 
	var rowData = $("#list").getRowData(selectedIds[0]);
	var data = {
		id : rowData.material_id
	}
	$.ajax({
		url: servicePath + '?method=doupdateToPuse',
		async: false, 
		beforeSend: ajaxRequestType, 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		data: data,
		type: "post",
		complete: function(){
			findit();
		}
	});
};

/** 根据条件使按钮有效/无效化 */
var enablebuttons = function() {
	var rowids = $("#list").jqGrid("getGridParam", "selarrrow");
	if (rowids == undefined) return;

	if (rowids.length === 0) {
		$("#resignbutton").disable();
		$("#pausebutton").disable();
		$("#forbutton").disable();
		$("#decplannedbutton").disable();
		$("#nsplannedbutton").disable();
		$("#complannedbutton").disable();
		$("#nogoodbutton").disable();
		$("#reccdbutton").disable();
		$("#cleanbutton").disable();
	} else if (rowids.length === 1) {
		$("#resignbutton").enable();
		$("#pausebutton").enable();
		$("#forbutton").enable();
		$("#decplannedbutton").enable();
		$("#nsplannedbutton").enable();
		$("#complannedbutton").enable();

		var rowid = $("#list").jqGrid("getGridParam", "selrow");
		var rowdata = $("#list").jqGrid('getRowData', rowid);
		var break_message = rowdata["break_message_level"]; // rowdata["break_message"] || rowdata["break_message_level"]
		if (break_message.trim() != "") {
			$("#nogoodbutton").enable();
		} else {
			$("#nogoodbutton").disable();
		}
		$("#reccdbutton").enable();
		$("#cleanbutton").enable();

	} else {
		$("#resignbutton").disable();
		$("#pausebutton").disable();
		$("#forbutton").disable();
		$("#decplannedbutton").enable();
		$("#nsplannedbutton").enable();
		$("#complannedbutton").enable();
		$("#nogoodbutton").disable();
		$("#reccdbutton").disable();
		$("#cleanbutton").disable();
	}
	if ($("#chooser").css("display") === "none") {
		$("#gbox_list").hide();
	}
}

var showremain = function(){
	// 标记
	var pill =$("#list");

	// 得到显示到界面的id集合
	var mya = pill.getDataIDs();
	// 当前显示多少条
	var length = mya.length;

	for (var i = 0; i < length; i++) {
		var rowdata = pill.jqGrid('getRowData', mya[i]);

		// break_message
		var break_message = rowdata["break_message_level"];
		if (break_message.trim() != "") {
			pill.find("tr#" + mya[i] + " td").css("color", "red");
		}

		var remain_days = rowdata["remain_days"];
		var remainColor = null;
		var remainFColor = null;
		if (remain_days) {
			if (remain_days < -7) {
				remainColor = "#E43838";
				remainFColor = "white";
			} else if (remain_days < -3) {
				remainColor = "#E48E38";
				remainFColor = "white";
			} else if (remain_days < 0) {
				remainColor = "#F9E29F";
				remainFColor = "#333333";
			} else if (remain_days < 2) {
				remainColor = "#E4F438";
				remainFColor = "#333333";
			}
		}
		if (remainColor) {
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='list_sorc_no']").css("background-color", remainColor).css("color", remainFColor);
		}
	}

};

/** 根据条件使按钮有效/无效化 */
var enablebuttons2 = function() {
	var rowids = $("#planned_list").jqGrid("getGridParam", "selarrrow");
	if (rowids === undefined) return;
	if (rowids.length === 0) {
		$("#removefromplanbutton").disable();
	} else {
		
		var flag = true;
		for(var i in rowids) {
			var data = $("#planned_list").getRowData(rowids[i]);
			var lineId = data["line_id"];
			if (lineId !=12 && lineId !=13 && lineId !=14) {
				flag = false;
				break;
			}
		}

		if (flag) {
			$("#removefromplanbutton").enable();
		} else {
			$("#removefromplanbutton").disable();
		}
	}

};

var treatInline = function(listdata) {
	$("#list").jqGrid().clearGridData();
	$("#list").jqGrid('setGridParam', {data: listdata}).trigger("reloadGrid", [{current: false}]);
}

function searchBoth_handleComplete(xhrobj, textStatus) {
	var resInfo = null;

	// 以Object形式读取JSON
	eval('resInfo =' + xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages("#searcharea", resInfo.errors);
	} else {
		// 读取一览
		var listdata = resInfo.material_list;
		treatInline(listdata);
		// 读取一览
		var planned_listdata = resInfo.schedule_list;
		treatPlanedList(planned_listdata);
	}
}


function search_handleComplete(xhrobj, textStatus) {

	var resInfo = null;

	// 以Object形式读取JSON
	eval('resInfo =' + xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages("#searcharea", resInfo.errors);
	} else {
		treatInline(resInfo.material_list);
	}
};

var checkout_complete = function(){
	enablebuttons2();
	$("#planned_list tr:has(td[aria\\-describedby='planned_list_processing_position'][title='已完成']) td").css("background-color", "#50FF64");

	// 标记
	var pill =$("#planned_list");

	// 得到显示到界面的id集合
	var mya = pill.getDataIDs();
	// 当前显示多少条
	var length = mya.length;

	for (var i = 0; i < length; i++) {
		var rowdata = pill.jqGrid('getRowData', mya[i]);

		var remain_days = rowdata["remain_days"];
		var remainColor = null;
		var remainFColor = null;
		if (remain_days) {
			if (remain_days < -7) {
				remainColor = "#E43838";
				remainFColor = "white";
			} else if (remain_days < -3) {
				remainColor = "#E48E38";
				remainFColor = "white";
			} else if (remain_days < 0) {
				remainColor = "#F9E29F";
				remainFColor = "#333333";
			} else if (remain_days < 2) {
				remainColor = "#E4F438";
				remainFColor = "#333333";
			}
		}
		if (remainColor) {
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='planned_list_sorc_no']").css("background-color", remainColor).css("color", remainFColor);
		}
	}
}

/**
 * 维修对象一览实行
 */
var findit = function() {
	var data = {
		"sorc_no" : $("#search_sorc_no").val(),
		"serial_no" : $("#search_serialno").val(),
		"scheduled_date_start" : $("#search_scheduled_date_start").val(),
		"scheduled_date_end" : $("#search_scheduled_date_end").val(),
		"level" : $("#search_level").val(),
		"section_id" : $("#search_section").val(),
		"arrival_plan_date_start" : $("#search_arrival_plan_date_start").val(),
		"arrival_plan_date_end" : $("#search_arrival_plan_date_end").val(),
		"complete_date_start" : $("#search_complete_date_start").val(),
		"complete_date_end" : $("#search_complete_date_end").val(),
		"position_id" : $("#search_position_id").val(),
		"position_eval" : position_eval_data,
		"scheduled_expedited": $("#scheduled_expedited").val(),
		"bo_flg": $("#cond_work_procedure_order_template").val(),
		"category_id":$("#search_category_id").val()
		};

	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: servicePath + '?method=search', 
		cache: false, 
		data: data, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete: search_handleComplete
	});
};

/**
 * 维修对象一览实行
 */
var findboth = function() {

	var data = {
		"sorc_no" : $("#search_sorc_no").val(),
		"serial_no" : $("#search_serialno").val(),
		"scheduled_date_start" : $("#search_scheduled_date_start").val(),
		"scheduled_date_end" : $("#search_scheduled_date_end").val(),
		"level" : $("#search_level").val(),
		"section_id" : $("#search_section").val(),
		"arrival_plan_date_start" : $("#search_arrival_plan_date_start").val(),
		"arrival_plan_date_end" : $("#search_arrival_plan_date_end").val(),
		"complete_date_start" : $("#search_complete_date_start").val(),
		"complete_date_end" : $("#search_complete_date_end").val(),
		"position_id" : $("#search_position_id").val(),
		"position_eval" : position_eval_data,
		"scheduled_expedited": $("#scheduled_expedited").val(),
		"bo_flg": $("#cond_work_procedure_order_template").val(),
		"category_id":$("#search_category_id").val()
	};

	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: servicePath + '?method=searchBoth', 
		cache: false, 
		data: data, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete: searchBoth_handleComplete
	});
};

var findSchedule = function(){
	var data = {
		line_id: $("#select_line").val()
	}
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: servicePath + '?method=searchSchedule', 
		cache: false, 
		data: data, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete: planned_list_handleComplete
	});
	
}

var findOutSchedule = function() {
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: servicePath + '?method=searchOutSchedule', 
		cache: false, 
		data: [], 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete: planned_list_handleComplete
	});
}
/**
 * 排入分解计划实行
 */
var updateDecPlannedSchedule = function() {
	updateSchedule(12);
};


/**
 * 排入NS计划实行
 */
var updateNSPlannedSchedule = function() {
	updateSchedule(13);
};


/**
 * 排入总计划实行
 */
var updateComPlannedSchedule = function() {
	updateSchedule(14);
};

/**
 * 排入计划实行
 */
var updateSchedule = function(lineId) {
	var selectedIds  = $("#list").jqGrid("getGridParam","selarrrow"); 
	var ids = [];
		// 读取排入行
	for (var i = 0; i < selectedIds.length; i++) {
		var rowData = $("#list").getRowData(selectedIds[i]);
		ids[ids.length] = rowData.material_id;
	}
	var data = {
		ids: ids.join(","),
		lineIds: lineId,
		line_id: $("#select_line").val()
	}
	
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: false, 
		url: servicePath + '?method=doupdateSchedule', 
		cache: false, 
		data: data, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete: planned_list_handleComplete
	});
};

/**
 * 切到删除画面
 */
var deleteSchedule = function(rid) {

	// 读取删除行
	var selectedIds  = $("#planned_list").jqGrid("getGridParam","selarrrow"); 

	var ids = [];
	var lineIds = [];
	
	// 读取排入行
	for (var i = 0; i < selectedIds.length; i++) {
		var rowData = $("#planned_list").getRowData(selectedIds[i]);
		ids[ids.length] = rowData.material_id;
		lineIds[lineIds.length] = rowData.line_id;
	}
	var data = {
		ids:ids.join(","),
		lineIds:lineIds.join(","),
		line_id: $("#select_line").val()
	}
	$("#confirmmessage").text("确认要把这"+selectedIds.length+"台维修对象从今日计划中移出吗？");
	$("#confirmmessage").dialog({
		resizable: false,
		modal: true,
		title: "删除确认",
		buttons: {
			"确认": function() {
				$(this).dialog("close");
				// Ajax提交
				$.ajax({
					beforeSend: ajaxRequestType,
					async: false,
					url: servicePath + '?method=dodeleteSchedule',
					cache: false,
					data: data,
					type: "post",
					dataType: "json",
					success: ajaxSuccessCheck,
					error: ajaxError,
					complete: planned_list_handleComplete
				});
			},
			"取消": function() {
				$(this).dialog("close");
			}
		}
	});
};

var moveOutOfLine = function() {
	var $confirm = $("#confirmmessage");
	$confirm.html("<form><textarea name='comment' id='move_reason' title='移出理由' style='width: 260px;height: 112px;resize:none;'></textarea></form>");
	$confirm.children("form").validate({
		rules : {
			comment : {
				required : true
			}
		}
	});

	$confirm.dialog({
		resizable: false,
		modal: true,
		title: "输入移出流水线理由",
		buttons: {
			"确认": function() {
				if ($confirm.children("form").valid()) {

					var selectedIds  = $("#list").jqGrid("getGridParam","selarrrow"); 
					// 读取排入行
					var rowData = null;
					if (selectedIds.length == 1) {
						rowData = $("#list").getRowData(selectedIds[0]);
					} else {
						return;
					}

					var postData = {move_reason : $("#move_reason").val(),
					material_id : rowData.material_id,
					processing_position : rowData.processing_position,
					levelName: rowData.levelName,
					process_code : rowData.process_code
					}

					// Ajax提交
					$.ajax({
						beforeSend: ajaxRequestType, 
						async: false, 
						url: servicePath + '?method=doupdateToPause', 
						cache: false, 
						data: postData, 
						type: "post", 
						dataType: "json", 
						success: ajaxSuccessCheck, 
						error: ajaxError, 
						complete: function(xhrobj){
							var resInfo = null;

							// 以Object形式读取JSON
							eval('resInfo =' + xhrobj.responseText);

							if (resInfo.errors.length > 0) {
								// 共通出错信息框
								treatBackMessages($confirm, resInfo.errors);
							} else {
								// 读取一览
								var listdata = resInfo.material_list;
								treatInline(listdata);
							}
						}
					});
					$confirm.dialog("close");
				}
			},
			"取消": function() {
				$confirm.dialog("close");
			}
		}
	})
};

var chooseAndPrintTickets = function(resInfo){
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: 'material.do?method=getForProductSerial', 
		cache: false, 
		data: null, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete: function(xhrObj){

			// 以Object形式读取JSON
			var resInfo = $.parseJSON(xhrObj.responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				chooseAndPrintTicketsCb(resInfo);
			}
		}
	});
}

var chooseAndPrintTicketsCb = function(resInfo){
	var $confirm = $("#confirmmessage");

	$confirm.html("<form><table><tr><td colspan=2 class='td-content'><select name='model_id' id='ticket_model'>" +
			resInfo.modelOptions +
			"</select></td></tr>" +
			"<tr><td class='ui-state-default td-title'>起始编号</td><td class='td-content'><input type='text' id='ticket_from' prefix='" + (resInfo.serial_no || "") +"' value='" + (resInfo.serial_no || "") +
			"'></td></tr><tr><td class='ui-state-default td-title'>连续数量</td><td class='td-content'><input type='number' id='ticket_count' value='1'></td></tr></form>");
	$confirm.find("select").select2Buttons();
	$confirm.dialog({
		resizable: false,
		modal: true,
		title: "指定连续打印小票",
		open: function( event, ui ) {
			var textI = $confirm.find("#ticket_from")[0];
			textI.focus();
			textI.selectionStart = textI.selectionEnd = textI.value.length;
		},
		buttons: {
			"确认": function() {
				var model_id = $("#ticket_model").val();
				var model_name = $("#ticket_model").children("option:selected").text();
				var serial_no = $("#ticket_from").val();
				var count = parseInt($("#ticket_count").val());
				$("#ticket_count").val(count);
				console.log(model_id + " " + serial_no + " " + count);

				if (!model_id || !serial_no || !count) {
					errorPop("请填写全部信息后提交。");
					return;
				}
				if (serial_no.length != 7) {
					errorPop("请输入7位的序列号。");
					return;
				}
				if (isNaN(count)) {
					errorPop("请为连续打印序列数量输入一个数值。");
					return;
				}
				if (count < 0 || count > 100) {
					errorPop("请为连续打印序列数量输入一个合理的数值。");
					return;
				}

				var postData = {};
				var eSerialNo = parseInt(serial_no, 10);
				var prefix = $("#ticket_from").attr("prefix");
				for (var iv = 0; iv < count; iv++) {
					var sSerialNo = fillZero(eSerialNo, 7);
					postData["materials.serial_no[" + iv + "]"] = sSerialNo;
					postData["materials.model_name[" + iv + "]"] = model_name;
					eSerialNo++;
					if (sSerialNo.substring(0, 3) != prefix) {
						errorPop("本月已经提供不了更多的序列号。");
						return;
					}
				}
				postData["addition"] = "addition";
				doPrintProductTickets(postData);
				$confirm.dialog("close");
			},
			"取消": function() {
				$confirm.dialog("close");
			}
		}
	});
}

var setReaccpect = function(){
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: 'material.do?method=getForProductSerial', 
		cache: false, 
		data: null, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete: function(xhrObj){

			// 以Object形式读取JSON
			var resInfo = $.parseJSON(xhrObj.responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				setReaccpectInput(resInfo);
			}
		}
	});
}


var setReaccpectInput = function(resInfo) {
	
	var $confirm = $("#confirmmessage");

	$confirm.html("<form><table><tr><td colspan=2 class='td-content'><select name='fix_type' id='insert_fix_type'>" +
			resInfo.fixTypeOptions +
			"</select></td></tr><tr><td colspan=2 class='td-content'><select name='model_id' id='insert_model_id'>" +
			resInfo.modelOptions +
			"</select></td></tr>" +
			"<tr><td class='ui-state-default td-title'>输入修理机序列号</td><td class='td-content'><input type='text' id='insert_serial_no'></td></tr></form>");
	$confirm.find("#insert_fix_type").val("8");
	$confirm.find("select").select2Buttons();
	$confirm.dialog({
		resizable: false,
		modal: true,
		title: "输入中途加入修理机信息",
		buttons: {
			"确认": function() {
				var model_id = $("#insert_model_id").val();
				var fix_type = $("#insert_fix_type").val();
				var serial_no = $("#insert_serial_no").val();

				if (!model_id || !serial_no || !fix_type) {
					errorPop("请填写全部信息后提交。");
					return;
				}
				if (serial_no.length != 7) {
					errorPop("请输入7位的序列号。");
					return;
				}

				var postData = {};

				postData["model_id"] = model_id;
				postData["fix_type"] = fix_type;
				postData["serial_no"] = serial_no;

				doReaccpect(postData);
				$confirm.dialog("close");
			},
			"取消": function() {
				$confirm.dialog("close");
			}
		}
	});
}

var doReaccpect = function(postData) {
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: 'material.do?method=doReaccpect', 
		cache: false, 
		data: postData, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete: function(xhrObj){

			// 以Object形式读取JSON
			var resInfo = $.parseJSON(xhrObj.responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				infoPop("受理加入完成。");
			}
		}
	});
}