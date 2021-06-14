var modelname = "不良对策信息";

/** 一览数据对象 */
var listdata = {};
/** 服务器处理路径 */
var servicePath = "defectiveAnalysis.do";
var typeOptions = "";
var ptlOptions = "";
var riskOptions = "";
var reworkOptions = "0:不需要;1:需要";

var findit = function() {
	var data = {
		omr_notifi_no : $("#omr_notifi_no").data("post"),
		defective_type : $("#defective_type").data("post"),
		step : $("#step").data("post"),
		sponsor_date_from : $("#sponsor_date_from").data("post"),
		sponsor_date_to : $("#sponsor_date_to").data("post"),
		responsibility_of_ptl : $("#responsibility_of_ptl").data("post"),
		capa_risk : $("#capa_risk").data("post"),
		rework_proceed : $("#rework_proceed").data("post"),
		cm_proc_confirmer_date_from : $("#cm_proc_confirmer_date_from").data("post"),
		cm_proc_confirmer_date_to : $("#cm_proc_confirmer_date_to").data("post"),
		line_id : $("#line_id").data("post")
	};

	// Ajax提交
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
		complete : search_handleComplete
	});
};

var g_depa = 0;

$(function() {

	g_depa = $("#department").val();

	$("input.ui-button").button();

	$("#searchbutton").addClass("ui-button-primary");

	$("#searcharea span.ui-icon,#wiparea span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	$("#defective_type, #step, #responsibility_of_ptl, #capa_risk, #line_id, #rework_proceed").select2Buttons();

	$("#sponsor_date_from, #sponsor_date_to, #cm_proc_confirmer_date_from, #cm_proc_confirmer_date_to").datepicker({
		showButtonPanel:true,
		currentText: "今天"
	});

	setReferChooser($("#cond_model_id"), $("#referchooser_model"));
	setReferChooser($("#cond_reciever_id"), $("#referchooser_operator"));

	// 检索处理
	$("#searchbutton").click(function() {
		// 保存检索条件
		$("#omr_notifi_no").data("post", $("#omr_notifi_no").val());
		$("#defective_type").data("post", $("#defective_type").val());
		$("#step").data("post", $("#step").val());
		$("#sponsor_date_from").data("post", $("#sponsor_date_from").val());
		$("#sponsor_date_to").data("post", $("#sponsor_date_to").val());
		$("#responsibility_of_ptl").data("post", $("#responsibility_of_ptl").val());
		$("#capa_risk").data("post", $("#capa_risk").val());
		$("#rework_proceed").data("post", $("#rework_proceed").val());
		$("#cm_proc_confirmer_date_from").data("post", $("#cm_proc_confirmer_date_from").val());
		$("#cm_proc_confirmer_date_to").data("post", $("#cm_proc_confirmer_date_to").val());
		$("#line_id").data("post", $("#line_id").val());
		// 查询
		findit();
	});

	// 清空检索条件
	$("#resetbutton").click(function() {
		$("#omr_notifi_no").val("").trigger("change").data("post", "");
		$("#defective_type").val("").trigger("change").data("post", "");
		$("#step").val("").trigger("change").data("post", "");
		$("#sponsor_date_from").val("").data("post", "");
		$("#sponsor_date_to").val("").data("post", "");
		$("#responsibility_of_ptl").val("").trigger("change").data("post", "");
		$("#capa_risk").val("").trigger("change").data("post", "");
		$("#rework_proceed").val("").trigger("change").data("post", "");
		$("#cm_proc_confirmer_date_from").val("").data("post", "");
		$("#cm_proc_confirmer_date_to").val("").data("post", "");
		$("#line_id").val("").trigger("change").data("post", "");
	});

	if ($("#sponsor_date_from").val()) {
		$("#sponsor_date_from").data("post", $("#sponsor_date_from").val());
		findit();
	}

});

var showDetail = function(alarm_message_id){

	popDefectiveAnalysis(alarm_message_id, true, findit);
};

/*
 * Ajax通信成功的处理
 */
function search_handleComplete(xhrobj, textStatus) {

	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages("#searcharea", resInfo.errors);
		} else {
			listdata = resInfo.list;

			typeOptions = resInfo.typeOptions;
			stepOptions = resInfo.stepOptions;
			ptlOptions = resInfo.ptlOptions;
			riskOptions = resInfo.riskOptions;

			if ($("#gbox_list").length > 0) {
				$("#list").jqGrid().clearGridData();
				$("#list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [{current : false}]);
			} else {
				$("#list").jqGrid({
					toppager : true,
					data : listdata,
					height : 461,
					width : 992,
					rowheight : 23,
					datatype : "local",
					colNames : ['','维修单号', '型号', '不良分类', '对策进度', '不良提出日', '不良现象',
							'工程', '责任区分', '风险大小', '返工', '对策实施确认日', '对策效果确认日'],
					colModel : [
						{name:'alarm_message_id',index:'alarm_message_id', hidden: true, key: true},
						{name:'omr_notifi_no',index:'omr_notifi_no', width:60},
						{name:'model_name',index:'model_name', width:60},
						{name:'defective_type',index:'defective_type', width:60, formatter:'select', editoptions:{value: typeOptions}},
						{name:'step',index:'step', width:50, align:'center', formatter:'select', editoptions:{value: stepOptions}},
						{name:'sponsor_date',index:'sponsor_date', width:50, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}},
						{name:'defective_phenomenon',index:'defective_phenomenon', width:60},
						{name:'line_name',index:'line_name', width:65},
						{name:'responsibility_of_ptl',index:'responsibility_of_ptl', width:65, formatter:'select', editoptions: {value: ptlOptions}},
						{name:'capa_risk',index:'position_name', width:55, formatter:'select', editoptions: {value: riskOptions}},
						{name:'rework_proceed',index:'rework_proceed', width:50, formatter:'select', editoptions: {value: reworkOptions}},
						{name:'cm_proc_confirmer_date',index:'cm_proc_confirmer_date', width:60, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}},
						{name:'cm_effect_confirmer_date',index:'cm_effect_confirmer_date', width:60, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}}
					],
					rowNum : 50,
					toppager : false,
					pager : "#listpager",
					viewrecords : true,
					caption : modelname + "一览",
					ondblClickRow : function(rid, iRow, iCol, e) {
						var data = $("#list").getRowData(rid);
						var alarm_message_id = data["alarm_message_id"];
						showDetail(alarm_message_id);
					},
					// multiselect : true,
					gridview : true, // Speed up
					pagerpos : 'right',
					pgbuttons : true,
					pginput : false,
					recordpos : 'left',
					viewsortcols : [true, 'vertical', true]
				});
				// $("#list").gridResize({minWidth:1248,maxWidth:1248,minHeight:200,
				// maxHeight:900});
			}
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

