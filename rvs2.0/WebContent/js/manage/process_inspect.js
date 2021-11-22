/** 服务器处理路径 */
var servicePath = "process_inspect.do";
var modelname = "作业监察信息";
function json2str(o) {var arr = []; var fmt = function(s) { if (typeof s == 'object' && s != null) return json2str(s); return /^(string|number)$/.test(typeof s) ? "'" + s + "'" : s; }; for (var i in o) arr.push("'" + i + "':" + fmt(o[i])); return '{' + arr.join(',') + '}'; };
// 界面设置
$(function() {

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

	$("#line_id, #summary\\.perform_option, #summary\\.line_id").select2Buttons();
	$("#search_unqualified_set, #search_file_type_set").buttonset();

	$("#inspect_date_from, #inspect_date_to, #summary\\.filing_date, #summary\\.inspect_date").datepicker({
		showButtonPanel:true,
		currentText: "今天"
	});

	setReferChooser($("#model_id"), $("#model_referchooser"));
	setReferChooser($("#operator_id"), $("#operator_referchooser"));
	setReferChooser($("#inspector_id"), $("#inspector_referchooser"));

	setReferChooser($("#summary\\.model_id"), $("#model_referchooser"));
	setReferChooser($("#summary\\.operator_id"), $("#operator_referchooser"));
	setReferChooser($("#summary\\.inspector_id"), $("#inspector_referchooser"));

	// 检索处理
	$("#searchbutton").click(function() {
		// 保存检索条件
		$("#line_id").data("post", $("#line_id").val());
		$("#process_name").data("post", $("#process_name").val());
		$("#inspect_date_from").data("post", $("#inspect_date_from").val());
		$("#inspect_date_to").data("post", $("#inspect_date_to").val());
		$("#operator_id").data("post", $("#operator_id").val());
		$("#inspector_id").data("post", $("#inspector_id").val()),
		$("#search_unqualified_set").data("post", $("#search_unqualified_set input:checked").val());
		$("#model_id").data("post", $("#model_id").val());
		$("#serial_no").data("post", $("#serial_no").val());
		$("#search_file_type_set").data("post", $("#search_file_type_set input:checked").val());

		// 查询
		findit();
	});

	// 清空检索条件
	$("#resetbutton").click(function() {
		$("#line_id").val("").trigger("change").data("post", "");
		$("#process_name").val("").trigger("change").data("post", "");
		$("#inspect_date_from").val("").data("post", "");
		$("#inspect_date_to").val("").data("post", "");
		$("#operator_id").val("").trigger("change").data("post", "");
		$("#inspector_id").val("").trigger("change").data("post", "");
		$("#search_unqualified_set").data("post", "");
		$("#model_id").val("").trigger("change").data("post", "");
		$("#serial_no").val("").trigger("change").data("post", "");
		$("#search_file_type_set").trigger("change").data("post", "");
	});

	findit();

	$("#openSummaryButton").click(popupUploadSummary);
	$("#openAchievementButton").click(popupUploadAchievement);

	$("#uploadSummaryButton").click(uploadSummaryFile);
	$("#uploadAchievementButton").click(uploadAchievementFile);

	$("#saveSummaryButton").click(saveUploadSummary);
	$("#closeSummaryButton").click(function() {$("#upload_summary_dialog").dialog("close");});

	$("#process_inspect_detail_infoes").on("click", "input:radio", function() {
		$("div.process_inspect_detail_tabcontent").hide();
		var tab = $("div.process_inspect_detail_tabcontent[for='"+this.id+"']");
		tab.show();
	});


});

// 初始化查询
var findit = function() {

	var data = {
		line_id : $("#line_id").data("post"),
		process_name : $("#process_name").data("post"),
		inspect_date_from : $("#inspect_date_from").data("post"),
		inspect_date_to : $("#inspect_date_to").data("post"),
		operator_id : $("#operator_id").data("post"),
		inspector_id: $("#inspector_id").data("post"),
		unqualified: $("#search_unqualified_set").data("post"),
		model_id: $("#model_id").data("post"),
		serial_no: $("#serial_no").data("post"),
		file_type: $("#search_file_type_set").data("post")
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

			performOptions = resInfo.performOptions;

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
					colNames : ['','实施选项', '工程名', '作业名', '操作者', '监察者', '监察日', '型号', '机身号'],
					colModel : [
						{name:'process_inspect_key',index:'process_inspect_key', hidden: true, key: true},
						{name:'perform_option',index:'perform_option', width:40, formatter:'select', editoptions:{value: performOptions}},
						{name:'line_name',index:'line_name', width:60},
						{name:'process_name',index:'process_name', width:100},
						{name:'operator_name',index:'operator_name', width:60},
						{name:'inspector_name',index:'inspector_name', width:40},
						{name:'inspect_date',index:'inspect_date', width:50, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'y-m-d'}},
						{name:'model_name',index:'model_name', width:70},
						{name:'serial_no',index:'serial_no', width:40}
					],
					rowNum : 50,
					toppager : false,
					pager : "#listpager",
					viewrecords : true,
					caption : modelname + "一览",
					ondblClickRow : function(rid, iRow, iCol, e) {
						var data = $("#list").getRowData(rid);
						var process_inspect_key = data["process_inspect_key"];
						showDetail(process_inspect_key);
					},
					gridview : true,
					pagerpos : 'right',
					pgbuttons : true,
					pginput : false,
					recordpos : 'left',
					viewsortcols : [true, 'vertical', true]
				});
			}
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

//显示上传汇总界面
var popupUploadSummary = function() {
	var dlg = $("#upload_summary_dialog");
	dlg.find("input[type='text']").val("");
	dlg.find("select").val("").trigger("change");
	dlg.find("textarea").val("");

	var file = $("#uploadSummaryFile");
	file.after(file.clone().val(""));
	file.remove();

	dlg.dialog({
		title : "作业检查汇报画面",
		width : 'auto',
		show : null,
		height :  'auto',
		resizable : false,
		modal : true,
		buttons : null
	}).show();

}

//显示上传检查记录界面
var popupUploadAchievement = function() {
	var rowid = $("#list").jqGrid('getGridParam', 'selrow');
	if (rowid == null) {
		errorPop("请选择相关的作业检查汇报。");
		return;
	}

	var dlg = $("#upload_achievement_dialog");
	dlg.find("input[type='text']").val("");
	dlg.find("select").val("").trigger("change");
	dlg.find("input[type='file']").val("");
	dlg.find("textarea").val("");

	var file = $("#uploadAchievementFile");
	file.after(file.clone().val(""));
	file.remove();

	dlg.dialog({
		title : "作业监察实绩画面",
			width : 'auto',
			show : null,
			height :  'auto',
			resizable : false,
			modal : true,
			buttons : null
	}).show();
}

//显示明细界面
var showDetail = function(process_inspect_key) {

	var this_dialog = $("#detail_dialog");

	if (this_dialog.length === 0) {
		$("body.outer").append("<div id='process_inspect_dialog'/>");
		this_dialog = $("#process_inspect_dialog");
	}

	this_dialog.html("");
	this_dialog.hide();
	// 导入详细画面
	this_dialog.load(servicePath + "?method=detail&process_inspect_key=" + process_inspect_key , function(responseText, textStatus, XMLHttpRequest) {
		this_dialog.dialog({
			title : "作业监查详细画面",
			position : [400, 20],
			width : 'auto',
			show : null,
			height :  'auto',
			resizable : false,
			modal : true,
			buttons : null
		});
	});
	this_dialog.show();
};

// 上传Summary文件
var uploadSummaryFile = function() {

	// 覆盖层
	panelOverlay++;
	makeWindowOverlay();

	$.ajaxFileUpload({
		url : servicePath + '?method=uploadSummaryFile',
		secureuri : false,
		fileElementId : 'uploadSummaryFile', // 文件选择框的id属性
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
					$("#summary\\.file_type").val(resInfo.data.file_type);

					$("#summary\\.perform_option").val(resInfo.data.perform_option);
					$("#summary\\.perform_option").select2Buttons();

					$("#summary\\.filing_date").val(resInfo.data.filing_date);

					$("#summary\\.line_id").val(resInfo.data.line_id);
					$("#summary\\.line_id").select2Buttons();

					$("#summary\\.operator_id").val(resInfo.data.operator_id);
					$("#summary\\.operator_name").val(resInfo.data.operator_name);
					$("#summary\\.inspector_id").val(resInfo.data.inspector_id);
					$("#summary\\.inspector_name").val(resInfo.data.inspector_name);
					$("#summary\\.inspect_date").val(resInfo.data.inspect_date);
					$("#summary\\.model_id").val(resInfo.data.model_id);
					$("#summary\\.model_name").val(resInfo.data.model_name);
					$("#summary\\.serial_no").val(resInfo.data.serial_no);
					$("#summary\\.process_seconds").val(resInfo.data.process_seconds);
					$("#summary\\.standard_seconds").val(resInfo.data.standard_seconds);
					$("#summary\\.situation").val(encodeText(resInfo.data.situation));
					$("#summary\\.countermeasures").val(encodeText(resInfo.data.countermeasures));
					$("#summary\\.conclusion").val(encodeText(resInfo.data.conclusion));
				}
			} catch(e) {

			}
		}
	});
};

var saveUploadSummary = function() {
	// 覆盖层
	panelOverlay++;
	makeWindowOverlay();

	var data = {
		"file_type": $("#summary\\.file_type").val(),
		"perform_option": $("#summary\\.perform_option").val(),
		"filing_date": $("#summary\\.filing_date").val(),
		"line_id": $("#summary\\.line_id").val(),
		"operator_id": $("#summary\\.operator_id").val(),
		"inspector_id": $("#summary\\.inspector_id").val(),
		"inspect_date": $("#summary\\.inspect_date").val(),
		"model_id": $("#summary\\.model_id").val(),
		"serial_no": $("#summary\\.serial_no").val(),
		"process_seconds": $("#summary\\.process_seconds").val(),
		"standard_seconds": $("#summary\\.standard_seconds").val(),
		"situation": decodeText($("#summary\\.situation").val()),
		"countermeasures": decodeText($("#summary\\.countermeasures").val()),
		"conclusion": decodeText($("#summary\\.conclusion").val())
	};

	$.ajaxFileUpload({
		url : servicePath + '?method=doCreateSummary',
		secureuri : false,
		fileElementId : 'uploadSummaryFile', // 文件选择框的id属性
		dataType : 'json', // 服务器返回的格式
		data: data,
		error : ajaxError,
		complete : function(xhrobj, textStatus) {

			panelOverlay--;
			killWindowOverlay();

			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);

				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages("#upload_summary_dialog", resInfo.errors);
				} else {
					$("#upload_summary_dialog").dialog('close');

					if (typeof(refreshList) === "function") refreshList();
					if (typeof(findit) === "function") findit();
				}
			} catch (e) {
				console.log("name: " + e.name + " message: " + e.message + " lineNumber: "
						+ e.lineNumber + " fileName: " + e.fileName);
			};
		}
	});
}

//上传Achievement文件
var uploadAchievementFile = function() {

	var rowid = $("#list").jqGrid('getGridParam', 'selrow');
	if (rowid == null) {return;}

	var rowData = $("#list").jqGrid('getRowData', rowid);

	var data = {
		"process_inspect_key": rowData.process_inspect_key,
		"process_name": $("#achievement\\.process_name").val()
	};

	// 覆盖层
	panelOverlay++;
	makeWindowOverlay();

	$.ajaxFileUpload({
		url : servicePath + '?method=doCreateAchievement',
		secureuri : false,
		fileElementId : 'uploadAchievementFile', // 文件选择框的id属性
		dataType : 'json', // 服务器返回的格式
		data: data,
		complete : function(xhrobj, textStatus) {

			panelOverlay--;
			killWindowOverlay();

			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);

				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages("#searcharea", resInfo.errors);
				} else {
					$("#upload_achievement_dialog").dialog('close');

					if (typeof(refreshList) === "function") refreshList();
					if (typeof(findit) === "function") findit();
				}
			} catch (e) {
				alert("name: " + e.name + " message: " + e.message + " lineNumber: "
						+ e.lineNumber + " fileName: " + e.fileName);
			};
		}
	});
};



