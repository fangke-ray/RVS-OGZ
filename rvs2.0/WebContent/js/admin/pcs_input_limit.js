var servicePath = "pcs_input_limit.do";

var findit = function() {
	if (!$("#search_line_type").data("post")) {
		errorPop("请按工程检查票类型查询。", $("#search_line_type"));
		return;
	}

	var postData = {
		line_type : $("#search_line_type").data("post"),
		target_model_id : $("#search_target_model_id").val(),
		file_name : $("#search_file_name").val()
	};

	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=search',
		cache: false,
		data: postData,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: search_handleComplete
	});
};

$(function() {

	$("input.ui-button").button();
	$(".ui-buttonset").buttonset();
	$("a.areacloser").hover(
		function (){$(this).addClass("ui-state-hover");},
		function (){$(this).removeClass("ui-state-hover");}
	);
	$("#searcharea span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	$("#searchbutton").click(function (){
		$("#search_line_type").data("post", $("#search_line_type").val());
		findit();
	});

	$("#resetbutton").click(function (){
		$("#search_line_type").val("").trigger("change");
		$("#search_file_name").val("");
		$("#search_target_model").val("");
		$("#search_target_model_id").val("");
	});

	$("#view_button").click(function() {
		var pcs_request_key = $("#list").jqGrid("getGridParam", "selrow");
		showPcsRequest(pcs_request_key);
	});

	$("select").select2Buttons();
	setReferChooser($("#search_target_model_id"), $("#model_refer"));

	initGrid();
})

var initGrid = function(){
	$("#list").jqGrid({
		data: [],
		height: 461,
		width: gridWidthMiddleRight,
		rowheight: 23,
		datatype: "local",
		colNames: ['line_type','模板文档名','设定的项目数','文档更新时间'],
		colModel: [
			{name:'line_type', index:'line_type',hidden:true},
			{name:'file_name', index:'file_name', width:200},
			{name:'items',index:'items', width:30, sorttype:'integer', align:'right'},
			{name:'import_time',index:'import_time', width:40, formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'y-m-d'}, align:'center'}
		],
		rowNum: 50,
		rownumbers:true,
		toppager: false,
		pager: "#listpager",
		viewrecords: true,
		caption: "",
		ondblClickRow: function(rid, iRow, iCol, e) {
			var rowData = $("#list").getRowData(rid);
			showPageForEdit(rowData.file_name);
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
		gridComplete: function(){
			enablebuttons();
		}
	});
}


var search_handleComplete = function(xhrObj){
	var resInfo = $.parseJSON(xhrObj.responseText);
	if (resInfo.errors && resInfo.errors.length > 0) {
		treatBackMessages("#searcharea", resInfo.errors);
		return;
	}

	var gotList = resInfo.lForms;

	var cur_page = $("#list").jqGrid().getGridParam("page");
	$("#list").jqGrid().clearGridData();
	$("#list").jqGrid('setGridParam', {data: gotList}).trigger("reloadGrid", [{current: true, page : cur_page}]);
}

/** 根据条件使按钮有效/无效化 */
var enablebuttons = function() {
	var rowid = $("#list").jqGrid("getGridParam", "selrow");
	if (rowid) {
	} else {
	}
}

var showPageForEdit = function(file_name) {
	var postData = {
		line_type : $("#search_line_type").data("post"),
		file_name : file_name
	};

	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=getPcsInputs',
		cache: false,
		data: postData,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: showPageForEdit_handleComplete
	});
}

var showPageForEdit_handleComplete = function(xhrObj) {
	var resInfo = $.parseJSON(xhrObj.responseText);

	$("#pcs_content_container").hide();
	var $pcs_content = $("#pcs_content").html(resInfo.fileHtml);

	var $pil_tags = $pcs_content.find("pil_tag");
	if ($pil_tags.length == 0) {
		errorPop("此检查票没有输入型点检项目。")
		return;
	}

	$pcs_content.find(".pil_lower,.pil_upper")
		.attr("required", true);

	$pil_tags.each(function(idx, ele){
		var $ele = $(ele);

		var not_allow_pass =  $ele.attr("allow_pass") && $ele.attr("allow_pass") === "no";

		if (not_allow_pass) {
			$ele.append("<checkbox value=0>必</checkbox>");
		} else {
			$ele.append("<checkbox value=1>必</checkbox>");
		}
	}).children("checkbox").click(function(){
		$(this).attr("value", 1-$(this).attr("value"));
	});

	$("#pcs_content_container").dialog({
		title : "工程检查表输入项目",
		width : 1264,
		height :  600,
		resizable : false,
		modal : true,
		buttons : {"确定": function(){
			var postData = {};
			var postIdx = 0;
			var errorMsg = "";
			$pil_tags.each(function(idx, ele){
				var $pil_tag = $(ele);
				var lower = $pil_tag.children(".pil_lower").val().trim();
				var upper = $pil_tag.children(".pil_upper").val().trim();
				var allow_pass = $pil_tag.children("checkbox").attr("value");

				if (lower != "" || upper != "") {
					var tag_code = $pil_tag.attr("key");
					if (lower != "") {
						if (isNaN(lower)) {
							errorMsg += tag_code + "的下限值不是一个数字。\n";
							return;
						} else {
							lower = parseFloat(lower);
							$pil_tag.children(".pil_lower").val(lower);
						}
					}

					if (upper != "") {
						if (isNaN(upper)) {
							errorMsg += tag_code + "的上限值不是一个数字。\n";
							return;
						} else {
							upper = parseFloat(upper);
							$pil_tag.children(".pil_upper").val(upper);
						}
					}

					if (lower != "" && upper != "") {
						if (lower > upper) {
							errorMsg += tag_code + "的下限值大于上限值。\n";
							return;
						}
					}

					postData["pil_tag[" + postIdx + "].tag_code"] = tag_code;
					postData["pil_tag[" + postIdx + "].lower_limit"] = lower;
					postData["pil_tag[" + postIdx + "].upper_limit"] = upper;
					postData["pil_tag[" + postIdx + "].allow_pass"] = allow_pass;

					postIdx++;
				}
			})

			if (errorMsg.length) {
				errorPop(errorMsg);
			} else {
				postData.line_type = $("#search_line_type").data("post");
				postData.file_name = resInfo.file_name;

				doSetLimits(postData);
			}
		},
		"关闭" : function(){
			$("#pcs_content_container").dialog("close");
		}
		}
	});
}

var doSetLimits = function(postData) {
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=doSetLimits',
		cache: false,
		data: postData,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: setLimits_handleComplete
	});	
}

var setLimits_handleComplete = function(xhrObj){
	var resInfo = $.parseJSON(xhrObj.responseText);
	if (resInfo.errors && resInfo.errors.length > 0) {
		treatBackMessages(null, resInfo.errors);
		return;
	}

	$("#pcs_content_container").dialog("close");
	findit();
}
