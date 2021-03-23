/** 模块名 */
var modelname = "维修对象";
/** 服务器处理路径 */
var servicePath = "schedule.do";
var fff = '已完成';
/** 工位查询方式 */
var position_eval_data = 2;
var position_eval_data2 = 0;
var caseId = 2;
var keepSearchData;

$(function() {
	$("input.ui-button").button();
	$("#removefromplanbutton,#resetPeriodButton").disable();
	$("#updatescheduledbutton").disable();

	$("#infoes > div").buttonset();
	$("a.areacloser").hover(
		function (){$(this).addClass("ui-state-hover");}, 
		function (){$(this).removeClass("ui-state-hover");}
	);

	$("#position_eval").click(function(){
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

	$("#position_eval2").click(function(){
		if (this.value === "正在") {
			this.value = "通过";
			position_eval_data2 = 2;
		} else if (this.value === "通过") {
			this.value = "未到";
			position_eval_data2 = 0;
		} else if (this.value === "未到") {
			this.value = "正在";
			position_eval_data2 = 1;
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
	
	$("#resetbutton").click(clearCondiction);
	
	$("#search_agreed_date_start, #search_agreed_date_end, #search_scheduled_date_start, #search_scheduled_date_end, "+
		"#search_complete_date_start, #search_complete_date_end, #search_arrival_plan_date_start, #search_arrival_plan_date_end," +
		"#search_inline_time_start, #search_inline_time_end").datepicker({
		showButtonPanel:true,
		currentText: "今天"
	});
	
	$("#pick_date").datepicker({
		showButtonPanel:true,
		currentText: "今天",
		minDate:0,
		maxDate:'+6m'
	});
	$("#pick_date").change(function(){
		changeDate();
	});

	$("#resignbutton").click(process_resign);
	$("#pausebutton").click(break_plan);
	$("#importbutton").click(import_reach);
	$("#importpartialbutton").click(import_partial_reach);

	$("#infoes > div > input[type=radio]").click(function(){
		findSchedule();
	});

	$("#decplannedbutton").click(updateDecPlannedSchedule);
	$("#nsplannedbutton").click(updateNSPlannedSchedule);
	$("#complannedbutton").click(updateComPlannedSchedule);
	$("#complannedbutton").next().click(updateComPlannedScheduleForUnknown);
	$("#removefromplanbutton").click(deleteSchedule);
	$("#resetPeriodButton").click(resetSchedulePeriod);
	$("#forbutton").click(moveOutOfLine);
	// $("#pxbutton").click(pxExchange);

	$("#searcharea span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	$("#skkarea span.ui-icon").bind("click", function() {
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

	$("#planned_listarea span.ui-icon").bind("click", function() {
		var now = new Date();
		var day = now.getDate();
		var maxDate = new Date();//半年后
		maxDate.setDate(day+180);
		var minDate = new Date();//明天
		minDate.setDate(day); // day+1
		
		if ($(this).hasClass('ui-icon-circle-triangle-w')) { //减
			var date = $("#pick_date").val();
			if (date) {
				var dd = new Date(date);
				if (dd > minDate) {
					dd.setDate(dd.getDate()-1);
					$("#pick_date").val(dd.getFullYear()+"/"+fillZero(dd.getMonth()+1)+"/"+fillZero(dd.getDate()));
				}
			} else {
				$("#pick_date").val(minDate.getFullYear()+"/"+fillZero(minDate.getMonth()+1)+"/"+fillZero(minDate.getDate()));
			}
			changeDate();
		} else if ($(this).hasClass('ui-icon-circle-triangle-e')){ //加
			var date = $("#pick_date").val();
			if (date) {
				var dd = new Date(date);
				if (dd < maxDate) {
					dd.setDate(dd.getDate()+1);
					$("#pick_date").val(dd.getFullYear()+"/"+fillZero(dd.getMonth()+1)+"/"+fillZero(dd.getDate()));
				}
			} else {
				$("#pick_date").val(minDate.getFullYear()+"/"+fillZero(minDate.getMonth()+1)+"/"+fillZero(minDate.getDate()));
			}
			changeDate();
		}
	});

	$("#search_level, #search_ocm, #search_category_id, #search_section, #sel_period").select2Buttons();

	setReferChooser($("#search_position_id"), $("#pReferChooser"));
	setReferChooser($("#search_position_id2"), $("#pReferChooser2"));

	$("#cond_work_procedure_order_template_flg_set").buttonset();
	$("#scheduled_expedited_set").buttonset();
	$("#colchooser").buttonset();
	$("#search_arrival_delay, #search_expedition_diff, #search_direct_flg").buttonset();

	$("#colchooser input:checkbox").click(function (){
		var colid = $(this).attr("id");
		var colchk = $(this).attr("checked");

		var colsname;
		if ("colchooser_rec" === colid) {
			colsname = ['agreed_date','inline_time','serial_no','ocmName','service_repair_flg'];
		} else if ("colchooser_par" === colid) {
			colsname = ['bo_contents','order_date'];
		} else if ("colchooser_prc" === colid) {
			colsname = ['dismantle_time','dec_finish_date','ns_finish_date','ns_processing_position'];
		} else if ("colchooser_she" === colid) {
			colsname = ['is_late', 'dec_plan_date','ns_plan_date','am_pm'];
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
	initGrid();
	
	$("#support_date").datepicker({
		showButtonPanel:true,
		currentText: "今天",
		minDate: 0,
		maxDate:'+1m'
	});
	$("#support_date").datepicker('setDate', '+0');

	$("#supportbutton").click(findForSupport);

	findboth();

	$("#updatescheduledbutton").click(updateScheduled);
});

var updateScheduled = function() {
	var rowids = $("#list").jqGrid("getGridParam", "selarrrow");
	var data = $("#list").getRowData(rowids[0]);
	var material_id = data["material_id"];
	var scheduled_date = data["scheduled_date_end"];
	update_scheduled(material_id, scheduled_date);
}

var clearCondiction = function() {
	$("#search_category_id").val("").trigger("change");
	$("#search_sorc_no").val("");
	$("#search_serialno").val("");
	$("#search_agreed_date_start").val("");
	$("#search_agreed_date_end").val("");
	$("#search_complete_date_start").val("");
	$("#search_complete_date_end").val("");
	$("#search_scheduled_date_start").val("");
	$("#search_scheduled_date_end").val("");
	$("#search_level").val("").trigger("change");
	$("#search_arrival_plan_date_start").val("");
	$("#search_arrival_plan_date_end").val("");
	$("#search_position_id").val("");
	$("#search_position_id2").val("");
	$("#inp_position_id").val("");
	$("#inp_position_id2").val("");
	$("#search_ocm").val("").trigger("change");
	$("#scheduled_expedited").val("");
	$("#scheduled_expedited_a").attr("checked","checked").trigger("change");
	$("#cond_work_procedure_order_template").val("");
	$("#cond_work_procedure_order_template_a").attr("checked","checked").trigger("change");
	$("#position_eval").val("通过");
	$("#position_eval2").val("未到");
	$("#search_section").val("").trigger("change");
	$("#search_arrival_delay").val("");
	$("#search_arrival_delay_a").attr("checked","checked").trigger("change");
	$("#search_expedition_diff").val("");
	$("#search_expedition_diff_a").attr("checked","checked").trigger("change");
	$("#search_inline_time_start").val("");
	$("#search_inline_time_end").val("");

	position_eval_data = 2;
	position_eval_data2 = 0;
}

var findForSupport = function() {
	clearCondiction();
	if ($("#support_date").val() == "") {
		$("#support_date").datepicker('setDate', '+0');
	}
	$("#search_arrival_plan_date_end").val($("#support_date").val());
	$("#inp_position_id").val("总组接受");
	$("#inp_position_id2").val("出检");
	$("#search_position_id").val($("#pReferChooser").find("td:contains('总组接受')").prev().text());
	$("#search_position_id2").val($("#pReferChooser").find("td:contains('出检')").eq(0).prev().text());
	$("#search_section").val($("#support_section").val()).trigger("change");

	var data = {
		"sorc_no" : $("#search_sorc_no").val(),
		"serial_no" : $("#search_serialno").val(),
		"agreed_date_start" : $("#search_agreed_date_start").val(),
		"agreed_date_end" : $("#search_agreed_date_end").val(),
		"scheduled_date_start" : $("#search_scheduled_date_start").val(),
		"scheduled_date_end" : $("#search_scheduled_date_end").val(),
		"complete_date_start" : $("#search_complete_date_start").val(),
		"complete_date_end" : $("#search_complete_date_end").val(),
		"level" : $("#search_level").val(),
		"section_id" : $("#search_section").val(),
		"arrival_plan_date_start" : $("#search_arrival_plan_date_start").val(),
		"arrival_plan_date_end" : $("#search_arrival_plan_date_end").val(),
		"position_id" : $("#search_position_id").val(),
		"position_eval" : position_eval_data,
		"position_id2" : $("#search_position_id2").val(),
		"position_eval2" : position_eval_data2,
		"scheduled_expedited": $("#scheduled_expedited").val(),
		"arrival_delay": $("#search_arrival_delay input:checked").val(),
		"ocm" : $("#search_ocm").val(),
		"expedition_diff": $("#search_expedition_diff input:checked").val(),
		"bo_flg": $("#cond_work_procedure_order_template").val(),
		"category_id":$("#search_category_id").val(),
		"support_date":$("#support_date").val(),
		"support_count":$("#support_count").val(),
		"direct_flg":$("#search_direct_flg input:checked").val(),
		"inline_time_start" : $("#search_inline_time_start").val(),
		"inline_time_end" : $("#search_inline_time_end").val()
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
}

function exportReport() {
	var data = {
		"sorc_no" : $("#search_sorc_no").val(),
		"serial_no" : $("#search_serialno").val(),
		"agreed_date_start" : $("#search_agreed_date_start").val(),
		"agreed_date_end" : $("#search_agreed_date_end").val(),
		"scheduled_date_start" : $("#search_scheduled_date_start").val(),
		"scheduled_date_end" : $("#search_scheduled_date_end").val(),
		"complete_date_start" : $("#search_complete_date_start").val(),
		"complete_date_end" : $("#search_complete_date_end").val(),
		"level" : $("#search_level").val(),
		"section_id" : $("#search_section").val(),
		"arrival_plan_date_start" : $("#search_arrival_plan_date_start").val(),
		"arrival_plan_date_end" : $("#search_arrival_plan_date_end").val(),
		"position_id" : $("#search_position_id").val(),
		"position_eval" : position_eval_data,
		"position_id2" : $("#search_position_id2").val(),
		"position_eval2" : position_eval_data2,
		"ocm" : $("#search_ocm").val(),
		"scheduled_expedited": $("#scheduled_expedited").val(),
		"arrival_delay": $("#search_arrival_delay input:checked").val(),
		"expedition_diff": $("#search_expedition_diff input:checked").val(),
		"bo_flg": $("#cond_work_procedure_order_template").val(),
		"category_id":$("#search_category_id").val(),
		"direct_flg":$("#search_direct_flg input:checked").val(),
		"inline_time_start" : $("#search_inline_time_start").val(),
		"inline_time_end" : $("#search_inline_time_end").val()
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
	var data = {
		scheduled_assign_date:$("#pick_date").val(),
		line_id: $("#select_line").val()
	}
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: servicePath + '?method=reportSchedule', 
		cache: false, 
		data: data, 
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
				var date = $("#pick_date").val();
				var iframe = document.createElement("iframe");
	            iframe.src = servicePath+"?method=exportSchedule&filePath=" + resInfo.filePath +"&date="+date;
	            iframe.style.display = "none";
	            document.body.appendChild(iframe);
			}
						
		}
	});
}

function initGrid(){
	$("#list").jqGrid({
			data: [], 
			height: 231, 
			width: 1248, 
			rowheight: 23, 
			datatype: "local", 
			colNames: ['维修对象ID','延误', '修理单号','机种', '型号 ID', '型号', '机身号', '等级', '直送', '返修', '客户<br>同意','RC','投线日期',
			'入库<br>预定日', '零件<br>到货', '零件<br>BO', '零件缺品详细', '维修课','进展<br>工位','拆镜<br>时间','零件订<br>购安排','分解产<br>出安排',
			'分解实<br>际产出','NS进<br>展工位','NS产<br>出安排','NS实<br>际产出','纳期','总组出<br>货安排','维修完<br>成剩余','预计<br>完成','AM/PM','加急',
			'工程内发现/不良','dqdngj','remain','备注','已加入计划','in_pa'],
			colModel: [
				{name:'material_id', index:'material_id',hidden:true},
				{name:'is_late',index:'is_late', width:20, hidden:true},
				{name:'sorc_no',index:'sorc_no', width:75, key: true},
				{name:'category_name',index:'category_name', width:50},	
				{name:'model_id',index:'model_id', hidden:true},				
				{name:'model_name',index:'model_name', width:125},
				{name:'serial_no',index:'serial_no', width:50, hidden:true},
				{name:'levelName',index:'levelName', width:35, align:'center'},
				{name:'direct_flg',index:'direct_flg', align:'center', width:30, formatter:'select', editoptions:{value:"1:直送;0:"}},
				{name:'service_repair_flg',index:'service_repair_flg', align:'center', width:30, formatter:'select', editoptions:{value:"1:保内;2:QIS;3:保外;0:"}},
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
							return mdTextOfDate(d);
						}

						return "";
					}},
				{name:'arrival_date',index:'arrival_date', width:50, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'bo_flg',index:'bo_flg', width:50, align:'center',formatter:function(a,b,row){
							if (a && a==="9") {
								return "等待发放"
							}
							if (a && a==="0") {
								return "无BO"
							}
							if (a && a==="1") {
								return "BO"
							}
							if (a && a==="2") {
								return "BO解决"
							}
							return "";
						}},
				{name:'bo_contents',index:'bo_contents', width:150, hidden:true, formatter:function(data,b,row){

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
				{name:'section_name',index:'section_name', width:35},
				{name:'processing_position',index:'processing_position', width:35, align:'center'},
//				{
//					name : 'px',
//					index : 'px',
//					width : 10
//				},
				{name:'dismantle_time',index:'dismantle_time', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'order_date',index:'order_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'dec_plan_date',index:'dec_plan_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'dec_finish_date',index:'dec_finish_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'ns_processing_position',index:'ns_processing_position', width:35, align:'center', hidden:true},
				{name:'ns_plan_date',index:'ns_plan_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'ns_finish_date',index:'ns_finish_date', width:35, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'scheduled_date_end',index:'scheduled_date_end', width:35, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
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
				{name:'countdown',index:'countdown', width:35, align:'center'},
				{name:'expected_finish_time',index:'expected_finish_time', width:35, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
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
				{name:'scheduled_expedited',index:'scheduled_expedited', width:35, align:'center',formatter:function(a,b,row){
							if (a && a==="2") {
								return "直送快速"
							}
							if (a && a==="1") {
								return "加急"
							}
							return "";
						}},
				{name:'break_message',index:'break_message', width:80},
				{name:'break_message_level',index:'break_message_level', width:80, hidden:true},
				{name:'remain_days',index:'remain_days', width:80, hidden:true},
				{name:'scheduled_manager_comment',index:'scheduled_manager_comment', width:80},
				{name:'schedule_assigned',index:'schedule_assigned', hidden:true},
				{name:'in_pa',index:'in_pa', hidden:true}
			],
			sortname : 'scheduled_date_end',
			
			rowNum: 80, 
			toppager: false, 
			pager: "#listpager", 
			viewrecords: true, 
			caption: "", 
			rownumbers : true,
			ondblClickRow: function(rid, iRow, iCol, e) {
				var data = $("#list").getRowData(rid);
				var material_id = data["material_id"];
				var break_message_level = data["break_message_level"];
				edit_message_popMaterialDetail(material_id, break_message_level, true);
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
			colNames: [ '维修对象ID','','计划工程', '修理单号', '机种', '型号 ID', '型号', '机身号', '等级', '直送',
				'客户<br>同意', 'RC', '投线日期', '入库<br>预定日', '零件<br>BO', '加急', '维修课', '进展<br>工位', '纳期',
				'总组出<br>货安排','日计划<br>时段','维修完<br>成剩余','remain_days','完成于'], 
			colModel: [
				{name: 'material_id', index: 'material_id', hidden: true},
				{name: 'line_id', index: 'line_id', hidden: true},
				{name: 'line_name', index: 'line_name', width: 60},
				{name: 'sorc_no', index: 'sorc_no', width: 105},
				{name: 'category_name',index:'category_name', width:50},	
				{name: 'model_id', index: 'model_id', hidden: true},
				{name: 'model_name', index: 'model_name', width: 125},
				{name: 'serial_no', index: 'serial_no', width: 50},
				{name: 'levelName', index: 'levelName', width: 35, align: 'center'},
				{name: 'direct_flg',index:'direct_flg', align:'center', width:30, formatter:'select', editoptions:{value:"1:直送;0:"}},
				{name: 'agreed_date', index: 'agreed_date', width: 50, align: 'center', formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name: 'ocmName',index:'ocmName', width:65},
				{name: 'inline_time', index: 'inline_time', width: 50, align: 'center', hidden: true, formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name: 'arrival_plan_date',index:'arrival_plan_date', width:50, align:'center', 
				formatter:function(a,b,row) {
							if ("9999/12/31" == a) {
								return "未定";
							}
							
							if (a) {
								var d = new Date(a);
								return mdTextOfDate(d);
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
				{name: 'processing_position', index: 'processing_position', width: 35, align: 'center'},
				{name: 'com_plan_date', index: 'com_plan_date', width: 35, align: 'center', formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
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
				{name:'schedule_period',index:'schedule_period', align:'center', width:30, formatter:'select', editoptions:{value:"1:A;2:B;3:C;4:D"}},
				{name:'countdown',index:'countdown', width:35, align:'center'},
				{name:'remain_days',index:'remain_days', width:80, hidden:true},
				{name:'ns_processing_position',index:'ns_processing_position', align:'center', width:30}
			], 
			rowNum: 50, 
			toppager: false, 
			pager: "#planned_listpager", 
			viewrecords: true, 
			caption: "", 
			ondblClickRow: function(rid, iRow, iCol, e) {
				var data = $("#planned_list").getRowData(rid);
				var material_id = data["material_id"];
				var break_message_level = data["break_message_level"];
				edit_schedule_popMaterialDetail(material_id, break_message_level, true);
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
		// 读取一览 TODO
		var listdata = resInfo.material_list;
		if (listdata) {
			treatInline(listdata);
			$("#list").jqGrid("resetSelection");
			enablebuttons();
		}
		// 读取一览
		var planned_listdata = resInfo.schedule_list;
		treatPlanedList(planned_listdata);
		$("#planned_list").jqGrid("resetSelection");
		enablebuttons2();
	}
};

var edit_schedule_popMaterialDetail = function(material_id, break_message_level, is_modal){

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
				var detail_buttons = {
					"确定":function(){
						if (break_message_level != "") { // TODO 判断返回值后
							var data = {
								material_id : selectedMaterial.material_id,
								position_id : selectedMaterial.position_id,
								alarm_messsage_id : selectedMaterial.alarm_messsage_id,
								comment : $("#nogood_comment").val()
							};
							// Ajax提交
							$.ajax({
								beforeSend : ajaxRequestType,
								async : false,
								url : 'alarmMessage.do?method=doreleasebeak',
								cache : false,
								data : data,
								type : "post",
								dataType : "json",
								success : ajaxSuccessCheck,
								error : ajaxError,
								complete : function() {
									doMaterialUpdate(material_id);
									$("#nogood_treat").dialog("close");
								}
							});	
						} else {
							doMaterialUpdate(material_id);
						}
					},
					"取消":function(){
						this_dialog.dialog('close');
					}
				};

				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					// TODO
					if ($("#planned_functionarea").length == 0) {
						case0();
						detail_buttons = {"关闭":function(){
							this_dialog.dialog('close');
						}};
					} else {
						case2();
					}
					setLabelText(resInfo.materialForm, resInfo.materialPartialFormList, resInfo.processForm, resInfo.timesOptions, material_id);
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
					buttons :  detail_buttons
				});
			
				this_dialog.show();
			}
		});
	});
}

var edit_message_popMaterialDetail = function(material_id, break_message_level, is_modal){
	// 有提交的不良的话，先备注
	if (break_message_level != "") {
		
		// TODO
		if ($("#planned_functionarea").length == 0) {
			return;
		}
		$("#nogood_treat").hide();
		// 导入不良处置画面
		$("#nogood_treat").load("widget.do?method=nogoodedit", function(responseText, textStatus, XMLHttpRequest) {
			var selectedId = $("#list").getGridParam("selrow");
			var rowData = $("#list").getRowData(selectedId);
			var data = {
				material_id : material_id
			};

			// Ajax提交
			$.ajax({
				beforeSend : ajaxRequestType,
				async : false,
				url : 'scheduleProcessing.do?method=getwarning',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : function(xhrobj, textStatus) {
					try {
						getWarningComplete(xhrobj, rowData, edit_schedule_popMaterialDetail, break_message_level, is_modal);
					} catch (e) {
						alert("name: " + e.name + " message: " + e.message + " lineNumber: "
								+ e.lineNumber + " fileName: " + e.fileName);
					};
				}
			});
		});
	} else {
		edit_schedule_popMaterialDetail(material_id, break_message_level, is_modal);
	}
}

var break_plan = function() {

	var rowid = $("#list").jqGrid("getGridParam", "selrow");
	var rowData = $("#list").getRowData(rowid);

	var data = {
		id : rowData.material_id
	}

	$("#confirmmessage").text("未修理返还后，维修对象["+encodeText(rowData.sorc_no)+"]将会退出RVS系统中的显示，直接出货，确认操作吗？");
	$("#confirmmessage").dialog({
		resizable : false,
		modal : true,
		title : "返还操作确认",
		buttons : {
			"确认" : function() {
				$(this).dialog("close");

				$.ajax({
					beforeSend : ajaxRequestType,
					async : false,
					url : servicePath + '?method=doStop',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : function() {
						findit();
					}
				});	
			},
			"取消" : function() {
				$(this).dialog("close");
			}
		}
	});

};

/** 根据条件使按钮有效/无效化 */
var enablebuttons = function() {
	var rowids = $("#list").jqGrid("getGridParam", "selarrrow");
	if (rowids.length === 0) {
		$("#resignbutton").disable();
		$("#pausebutton").disable();
		$("#forbutton").disable();
		$("#decplannedbutton").disable();
		$("#nsplannedbutton").disable();
		$("#complannedbutton").disable();
		$("#pxbutton").disable();
		$("#complannedbutton").next().disable();
		$("#updatescheduledbutton").disable();
	} else if (rowids.length === 1) {
		var rowdata = $("#list").getRowData(rowids[0]);

		$("#resignbutton").enable();
		$("#pausebutton").enable();
		$("#forbutton").enable();
		$("#decplannedbutton").enable();
		if (rowdata["ns_processing_position"]) {
			$("#nsplannedbutton").enable();
		} else {
			$("#nsplannedbutton").disable();
		}
		$("#complannedbutton").enable();
		if("未定" == rowdata.com_finish_date) {
			$("#complannedbutton").next().disable();
		} else {
			$("#complannedbutton").next().enable();
		}
		if(rowdata.px)
			$("#pxbutton").enable();
		else
			$("#pxbutton").disable();
		$("#updatescheduledbutton").enable();
	} else {
		$("#resignbutton").disable();
		$("#pausebutton").disable();
		$("#forbutton").disable();
		$("#pxbutton").disable();
		$("#decplannedbutton").enable();
		$("#updatescheduledbutton").disable();

		var hasNS = true;
		for (var ir in rowids) {
			var rowdata = $("#list").jqGrid('getRowData', rowids[ir]);
			if (!rowdata["ns_processing_position"]) {
				hasNS = false;
				break;
			}
		}
		if (hasNS) {
			$("#nsplannedbutton").enable();
		} else {
			$("#nsplannedbutton").disable();
		}
		$("#complannedbutton").enable();
		$("#complannedbutton").next().disable();
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
		// 从上到下获取一条信息
		var rowdata = pill.jqGrid('getRowData', mya[i]);

		// schedule_assigned
//		var schedule_assigned = rowdata["schedule_assigned"];
//		if (schedule_assigned == "1") {
//			pill.find("tr#" + mya[i] + " td").css("background-color", "#F8FB84");
//		}
		// break_message
		var break_message = rowdata["break_message_level"];
		if (break_message.trim() != "") { // 未解决  != ""
			pill.find("tr#" + mya[i] + " td").css("color", "red");
		}

		var in_pa = rowdata["in_pa"];
		if (in_pa && in_pa > 0) {
			pill.find("tr#" + mya[i] + " td").css("background-color", "lightgray");
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
	$("#resetPeriodContent").hide("slide");
	var rowids = $("#planned_list").jqGrid("getGridParam", "selarrrow");
	if (rowids.length === 0) {
		$("#removefromplanbutton,#resetPeriodButton").disable();
	} else if (rowids.length === 1) {
		$("#removefromplanbutton").enable();
		var date = $("#pick_date").val();
		if (date && new Date(date).getDate() == new Date().getDate()) {
			$("#resetPeriodButton").enable();
		}

	} else {
		$("#removefromplanbutton").enable();
		$("#resetPeriodButton").disable();
	}
};

var treatInline = function(listdata) {
	var cur_page = $("#list").jqGrid().getGridParam("page");
	$("#list").jqGrid().clearGridData();
	$("#list").jqGrid('setGridParam', {data: listdata}).trigger("reloadGrid", [{current: true, page : cur_page}]);
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
		"agreed_date_start" : $("#search_agreed_date_start").val(),
		"agreed_date_end" : $("#search_agreed_date_end").val(),
		"scheduled_date_start" : $("#search_scheduled_date_start").val(),
		"scheduled_date_end" : $("#search_scheduled_date_end").val(),
		"complete_date_start" : $("#search_complete_date_start").val(),
		"complete_date_end" : $("#search_complete_date_end").val(),
		"level" : $("#search_level").val(),
		"section_id" : $("#search_section").val(),
		"arrival_plan_date_start" : $("#search_arrival_plan_date_start").val(),
		"arrival_plan_date_end" : $("#search_arrival_plan_date_end").val(),
		"position_id" : $("#search_position_id").val(),
		"position_eval" : position_eval_data,
		"position_id2" : $("#search_position_id2").val(),
		"position_eval2" : position_eval_data2,
		"ocm" : $("#search_ocm").val(),
		"scheduled_expedited": $("#scheduled_expedited").val(),
		"arrival_delay": $("#search_arrival_delay input:checked").val(),
		"expedition_diff": $("#search_expedition_diff input:checked").val(),
		"bo_flg": $("#cond_work_procedure_order_template").val(),
		"category_id":$("#search_category_id").val(),
		"direct_flg":$("#search_direct_flg input:checked").val(),
		"inline_time_start" : $("#search_inline_time_start").val(),
		"inline_time_end" : $("#search_inline_time_end").val()
	};

	keepSearchData = data;

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
		"agreed_date_start" : $("#search_agreed_date_start").val(),
		"agreed_date_end" : $("#search_agreed_date_end").val(),
		"scheduled_date_start" : $("#search_scheduled_date_start").val(),
		"scheduled_date_end" : $("#search_scheduled_date_end").val(),
		"complete_date_start" : $("#search_complete_date_start").val(),
		"complete_date_end" : $("#search_complete_date_end").val(),
		"level" : $("#search_level").val(),
		"section_id" : $("#search_section").val(),
		"arrival_plan_date_start" : $("#search_arrival_plan_date_start").val(),
		"arrival_plan_date_end" : $("#search_arrival_plan_date_end").val(),
		"position_id" : $("#search_position_id").val(),
		"position_eval" : position_eval_data,
		"position_id2" : $("#search_position_id2").val(),
		"position_eval2" : position_eval_data2,
		"ocm" : $("#search_ocm").val(),
		"scheduled_expedited": $("#scheduled_expedited").val(),
		"arrival_delay": $("#search_arrival_delay input:checked").val(),
		"expedition_diff": $("#search_expedition_diff input:checked").val(),
		"bo_flg": $("#cond_work_procedure_order_template").val(),
		"category_id":$("#search_category_id").val()
	};

	keepSearchData = data;
	keepSearchData["scheduled_assign_date"] = $("#pick_date").val();

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

var findSchedule = function(lineId){
	var data = {
		scheduled_assign_date:$("#pick_date").val(),
		line_id: $("#infoes > div > input[name=schedule_line]:checked").val(),
		section_id : $("#infoes > div > input[name=schedule_section]:checked").val()
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

var updateComPlannedScheduleForUnknown = function() {
	$confirm = $("#confirmmessage");
	$confirm.html("<form><textarea name='comment' id='unknown_comment' title='未定理由' style='width: 260px;height: 112px;resize:none;'></textarea></form>");
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
		title: "输入计划日期未定理由",
		buttons: {
			"确认": function() {
				if ($confirm.children("form").valid()) {
					updateSchedule(14, '9999/12/31');
					$("#title_planned").text("未定待另行安排一览");
					$confirm.dialog("close");
				}
			},
			"取消": function() {
				$confirm.dialog("close");
			}
		}
	})
};

var moveOutOfLine = function() {
	$confirm = $("#confirmmessage");
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
					processing_position : rowData.processing_position
					}

					// Ajax提交
					$.ajax({
						beforeSend: ajaxRequestType, 
						async: true, 
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

/**
 * 排入计划实行
 */
var updateSchedule = function(lineId, scheduled_assign_date) {
	var selectedIds  = $("#list").jqGrid("getGridParam","selarrrow"); 
	var ids = [];
		// 读取排入行
	for (var i = 0; i < selectedIds.length; i++) {
		var rowData = $("#list").getRowData(selectedIds[i]);
		ids[ids.length] = rowData.material_id;
	}
	var data = keepSearchData;
	data.ids = ids.join(",");
	data.lineIds = lineId;
	data.scheduled_assign_date = $("#pick_date").val();
	data.line_id = $("#select_line").val();

	if ($("#unknown_comment").length > 0) {
		data.unknown_comment = $("#unknown_comment").val();
		$("#unknown_comment").val("");
	}
	if (scheduled_assign_date) {
		data.scheduled_assign_date = scheduled_assign_date;
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

var resetSchedulePeriod = function() {
	$("#resetPeriodButton").disable();
	$("#resetPeriodContent").show("slide");
	// 读取变更行
	var selectedId = $("#planned_list").jqGrid("getGridParam","selrow");
	var rowData = $("#planned_list").getRowData(selectedId);
	$("#sel_period").unbind("change");
	if(rowData.schedule_period) {
		$("#sel_period").val(rowData.schedule_period).trigger("change");
	} else {$("#sel_period").val("").trigger("change");}
	$("#sel_period").bind("change",function(){
		var data = {
			material_id : rowData.material_id,
			scheduled_date : $("#pick_date").val(),
			scheduled_assign_date : $("#pick_date").val(),
			plan_day_period : this.value
		};

		// Ajax提交
		$.ajax({
			beforeSend: ajaxRequestType, 
			async: false, 
			url: servicePath + '?method=doUpdateSchedulePeriod', 
			cache: false, 
			data: data, 
			type: "post", 
			dataType: "json", 
			success: ajaxSuccessCheck, 
			error: ajaxError, 
			complete: planned_list_handleComplete
		});

	});
}

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
		scheduled_assign_date:$("#pick_date").val(),
		line_id: $("#select_line").val()
	}
	var now = new Date();
	var dd= new Date($("#pick_date").val());
	var day = ($("#pick_date").val() == "" || (now.getDate()+1) == dd.getDate()) ? "明" : dd.getDate(); 
		
	$("#confirmmessage").text("确认要把这"+selectedIds.length+"台维修对象从"+ day +"日计划中移出吗？");
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

function changeDate(){
	var now = new Date();
	var dd= new Date($("#pick_date").val());
	var day = (now.getDate()+1) == dd.getDate() ? "明" : dd.getDate(); 
	$("#title_planned").text(day+"日计划一览");
	
	findSchedule();	
}

var uploadfile = function() {
	// 覆盖层
	panelOverlay++;
	makeWindowOverlay();

	// ajax enctype="multipart/form-data"
	$.ajaxFileUpload({
		url : 'upload.do?method=doreach', // 需要链接到服务器地址
		secureuri : false,
		fileElementId : 'agreed_file', // 文件选择框的id属性
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
//					listdata = resInfo.list;
					findit();
					$("#process_dialog").dialog('close');
					$("#process_dialog").text("导入入库预定日完成。");
					$("#process_dialog").dialog({
						resizable : false,
						modal : true,
						title : "导入日期确认",
						buttons : {
							"确认" : function() {
								$(this).dialog("close");
							}
						}
					});
				}
			} catch(e) {
				
			}
		}
	});
};

var uploadPartialFile = function() {
		// 覆盖层
	panelOverlay++;
	makeWindowOverlay();

	$("#process_dialog").hide();

	// ajax enctype="multipart/form-data"
	$.ajaxFileUpload({
		url : 'upload.do?method=doUploadArrivePlanDate', // 需要链接到服务器地址
		secureuri : false,
		fileElementId : 'agreed_file', // 文件选择框的id属性
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
//					listdata = resInfo.list;
					findit();
					$("#process_dialog").dialog('close');
					$("#process_dialog").text("导入入库预定日完成。");
					$("#process_dialog").dialog({
						resizable : false,
						modal : true,
						title : "导入日期确认",
						buttons : {
							"确认" : function() {
								$(this).dialog("close");
							}
						}
					});
				}
			} catch(e) {
				
			}
		}
	});
};

var import_reach = function() {
	$("#process_dialog").hide();
	// 文件选择
	$("#process_dialog").html("<input name='file' id='agreed_file' type='file'/>")

	$("#process_dialog").dialog({
	//		position : [ 800, 20 ],
		title : "选择上传文件",
		width : 280,
		show: "blind",
		height : 180,// 'auto' ,
		resizable : false,
		modal : true,
		minHeight : 200,
		close : function(){
			$("#process_dialog").html("");
		},
		buttons : {
			"上传":function(){
				uploadfile();
				
			}, "关闭" : function(){ $(this).dialog("close"); }
		}
	});
	$("#process_dialog").show();
};

var import_partial_reach = function() {
	$("#process_dialog").hide();
	// 文件选择
	$("#process_dialog").html("<input name='file' id='agreed_file' type='file'/>")

	$("#process_dialog").dialog({
	//		position : [ 800, 20 ],
		title : "选择上传文件",
		width : 280,
		show: "blind",
		height : 180,// 'auto' ,
		resizable : false,
		modal : true,
		minHeight : 200,
		close : function(){
			$("#process_dialog").html("");
		},
		buttons : {
			"上传":function(){
				uploadPartialFile();
				
			}, "关闭" : function(){ $(this).dialog("close"); }
		}
	});
	$("#process_dialog").show();
};

var doStop=function() {

	var rowid = $("#list").jqGrid("getGridParam", "selrow");
	var rowdata = $("#list").getRowData(rowid);

	$("#confirmmessage").text("未修理返还后，维修对象["+encodeText(rowdata.sorc_no)+"]将会退出RVS系统中的显示，在图象检查后直接出货，确认操作吗？");
	$("#confirmmessage").dialog({
		resizable : false,
		modal : true,
		title : "返还操作确认",
		buttons : {
			"确认" : function() {
				$(this).dialog("close");
				var data = {material_id : rowdata["material_id"]};
			
				$.ajax({
					beforeSend : ajaxRequestType,
					async : false,
					url : 'wip.do?method=dostop',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : function() {
						findit();
					}
				});	
			},
			"取消" : function() {
				$(this).dialog("close");
			}
		}
	});
}

/* 切换分线 */
var pxExchange = function() {

	var selectedId  = $("#list").jqGrid("getGridParam","selrow"); 
	// 读取排入行
	var rowData = $("#list").getRowData(selectedId);

	var data = {material_id : rowData["material_id"]};

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : 'material.do?method=doPxExchange',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrObj, textStatus) {
			// 以Object形式读取JSON
			var resInfo = $.parseJSON(xhrObj.responseText);
			findit();
		}
	});
};

var update_scheduled = function(material_id, agreed_date) {
	var $update_agree_dialog = $("#update_scheduled_dialog");
	$update_agree_dialog.hide();

	// 导入返工画面
	$update_agree_dialog.html("<input type='text' id='edit_update_agreed_date' value='"+(agreed_date=="undefined" ? "" : agreed_date)+"'/>");

	$update_agree_dialog.dialog({
//		position : [ 800, 20 ],
		title : "选择预定纳期日期",
		width : 280,
		show: "blind",
		height : 180,// 'auto' ,
		resizable : false,
		modal : true,
		minHeight : 200,
		close : function(){
			$update_agree_dialog.html("");
		},
		buttons : {
			"确定":function(){
				var date = $("#edit_update_agreed_date").val();
				if (!date) {
					treatBackMessages("#editarea", [{"errmsg":"请选择预定纳期日期。"}]);
					return;
				}

				var data = {
					"material_id" : material_id,
					"scheduled_date_end" : date
				}
				$.ajax({
					data: data,
					async: false, 
					beforeSend: ajaxRequestType, 
					success: ajaxSuccessCheck, 
					error: ajaxError, 
					url : servicePath +"?method=doupdateScheduledDate",
					type : "post",
					complete : function(xhrobj, textStatus){
						findit(keepSearchData);
						$update_agree_dialog.dialog('close');
					}
				})
			}, "关闭" : function(){ $update_agree_dialog.dialog("close"); }
		}
	});
	$("#edit_update_agreed_date").datepicker({
		showButtonPanel:true,
		dateFormat: "yy/mm/dd",
		currentText: "今天"
	});
	
	$("#process_dialog").show();
}