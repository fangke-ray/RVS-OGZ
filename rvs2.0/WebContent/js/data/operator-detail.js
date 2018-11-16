// Your turst our mission
var opd_modelname = "工作日报";

var opd_finddetail = function(operator_id, action_time) {
	opd_postData = {
		"operator_id": operator_id,
		"action_time" : action_time
	};
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		data : opd_postData,
		cache : false,
		url : "operatorProduction.do?method=getDetail",
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus) {
			opd_search_handleComplete(xhrobj, textStatus);
		}
	});
};
/** 一览数据对象 */
var opd_listdata = {};
var opd_postData = {};
var main_process_code;

var leakinput = function() {
	var commentcells = $("td[aria\\-describedby='operator_detail_list_leak'][title='true']").prev("td");
	commentcells.css("background-color", "#FFA050");
	commentcells.click(fixleakpause);
}

var countmins = function(datetime) {
	var hour = datetime.substring(11,13);
	var minute = datetime.substring(14,16);
	return hour * 60 + parseInt(minute, 10);
}

var slideTime = function(datetime) {
	var timevalue = $("#splitter").slider( "option", "value" );
	var minutes0 = parseInt(timevalue % 60, 10),
    	hours0 = parseInt(timevalue / 60, 10);
    if (minutes0 < 10) minutes0 = "0" + minutes0;

	var minm = $("#splitter").slider( "option", "min");
	var maxm = $("#splitter").slider( "option", "max");

    $("#selectHM").text(hours0 + ":" + minutes0);
    $("#spareHM").text("距开始时间" + (timevalue - minm) + "分，距结束时间" + (maxm - timevalue) + "分");
}

var fixleakpause = function() {
	var cell = $(this);
	var this_dialog = $("#operator_detail_dialog"); 
	this_dialog.hide();
	this_dialog.load("widgets/pause-workreport.jsp?s="+new Date().getMilliseconds(), function() {
		$("#pause-workreport-reason").show();
		$("#pause-workreport-split").hide();

		$("#pause_reason").select2Buttons();

		$("#splitter").slider({slide: slideTime})
			.next().find("input.ui-button").button()
			.click(function(){this_dialog.dialog('close')});

		this_dialog.dialog({
			position : [520, 40],
			title : null,
			width : 500,
			show : "",
			height : 'auto',
			resizable : false,
			modal : true,
			buttons : {
				"分割时段": function(){
					this_dialog.dialog("option", "height", 200);
					this_dialog.next(".ui-dialog-buttonpane").find("button:eq(0)").hide();

					var line_no = cell.parent().find("td:first").text() - 1;

					$("#splitter").slider( "option", "min", countmins(opd_listdata[line_no].pause_start_time) );
					$("#splitter").slider( "option", "max", countmins(opd_listdata[line_no].pause_finish_time) );
					$("#splitter").slider( "option", "value", countmins(opd_listdata[line_no].pause_start_time) + 1 );

					$("#pause-workreport-reason").hide();
					$("#pause-workreport-split").show();
					// this_dialog.dialog('close');
				},
				"确定": function(){
					if (this_dialog.next(".ui-dialog-buttonpane").find("button:eq(0)").is(":hidden")) {
						if ($("#selectHM").text() != "") {
							var line_no = cell.parent().find("td:first").text() - 1;
							var finish_time = opd_listdata[line_no].pause_finish_time;
							var spare_time = finish_time.substring(0, 11) + $("#selectHM").text() + ":00";
							opd_listdata[line_no].pause_finish_time = spare_time;

							var neodata = {};
							neodata.pause_start_time = spare_time;
							neodata.pause_finish_time = finish_time;
							neodata.leak = true;

							opd_listdata.splice(line_no + 1, 0, neodata);

							opd_loadData();
						}
					} else {
						var reasonText = $("#pause_reason").find("option:selected").text();
						var reason = $("#pause_reason").val();
						var comments = $("#edit_comments").val();

						if (reason === '49' && !comments) {
							treatBackMessages("#searcharea", [{errmsg:"请填写备注!"}]);
							return;
						}
						if (comments && reason == '') {
							reason = '49';
						}
//						alert($("#pause_reason").find("option:selected").text());
//						alert(reason);
						cell.text(reasonText +" " +comments);
						cell.prev("td").text(comments).prev("td").text(reason);
					}

					this_dialog.dialog('close');
				},
				"取消": function(){
					this_dialog.dialog('close');
				}
			}
		});
		if ($("div#operator_detail_dialog").length > 1) {
			$("div#operator_detail_dialog:eq(0)").remove();// TODO BUG??
		}
	});
};

function opd_initGrid() {
	$("#operator_detail_list").jqGrid({
		toppager : true,
		data : [],
		height : 461,
		width : 768,
		rowheight : 23,
		datatype : "local",
		colNames : ['开始时间', '','结束时间','', '处理对象', '机型', '工位','工位名称','原因','备注', '特记事项','leak'],//注意不要随意改变表头顺序，特别是后四位
		colModel : [{
					name : 'pause_start_time',
					sortable : false,
					width : 85,
					align : 'center',
					formatter:'date', 
					formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'H:i'}
				}, {
					name: 'pause_start_time_hidden',
					hidden: true,
					formatter:function(a,b,c){return c.pause_start_time}
				},{
					name : 'pause_finish_time',
					sortable : false,
					width : 85,
					align : 'center',
					formatter:'date', 
					formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'H:i'}
				}, {
					name: 'pause_finish_time_hidden',
					hidden: true,
					formatter:function(a,b,c){return c.pause_finish_time}
				}, {
					name : 'sorc_no',
					sortable : false,
					width : 120
				}, {
					name : 'model_name',
					sortable : false,
					width : 120
				}, {
					name : 'process_code',
					sortable : false,
					hidden: true
				}, {
					name : 'position_name',
					sortable : false,
					width : 80
				}, {
					name: 'reason',
					hidden: true
				}, {
					name: 'comments',
					hidden: true
				}, {
					name: 'leak_comment',
					width : 160,
					formatter:function(a,b,c) {
						var rt = "";
						if (c.reasonText) {
							rt += c.reasonText+" ";
						}
						if(c.comments) {
							rt += c.comments;
						}
						
						if (c.process_code && c.process_code != main_process_code && c.operate_result) {
							if (c.operate_result == "5") {
								return "辅助工作"
							} else if (c.operate_result == "2" && c.pace && c.pace == "0") {
								return "全代工";
							} else if (c.operate_result == "2" && c.pace && c.pace> 0) {
								return "半代工";
							}
						}
						return rt;
					}
				}, {
					name : 'leak',
					hidden : true
				}],
		rowNum : 50,
		rownumbers : true,
		toppager : false,
		pager : "#operator_detail_listpager",
		viewrecords : true,
		caption : opd_modelname + "一览",
		hidegrid : false, // 启用或者禁用控制表格显示、隐藏的按钮
		gridview : true, // Speed up
		pagerpos : 'right',
		pgbuttons : true,
		// forceFit : true,// 调整列宽度不会改变表格的宽度。
		pginput : false,
		recordpos : 'left',
		gridComplete : leakinput
	});
}
/*
 * Ajax通信成功的处理
 */
function opd_search_handleComplete(xhrobj, textStatus) {

	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		setLabel(resInfo.detail);
		opd_listdata = resInfo.list;
		opd_loadData();

		$("#can_edit_overtime").val(resInfo.editable);

		setOvertime(resInfo.overtime, resInfo.editable);
		if (resInfo.isAdmin === true) {
			$("#reportbutton").show();
		} else {
			$("#reportbutton").hide();
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message
				+ " lineNumber: " + e.lineNumber + " fileName: "
				+ e.fileName);
	}
};

function setOvertime(overtime, editable) {
	if (overtime) {
		if (overtime.pause_start_time) {
			var start = new Date(overtime.pause_start_time);
			var data = start.getHours()+":"+((start.getMinutes().toString().length==1) ? ("0"+start.getMinutes()):start.getMinutes());
			$("#edit_overtime_start").val(data);
			$("#label_overtime_start").text(data);
		}
		if (overtime.pause_finish_time) {
			var end = new Date(overtime.pause_finish_time);
			var data = end.getHours()+":"+((end.getMinutes().toString().length==1) ? ("0"+end.getMinutes()):end.getMinutes());
			$("#edit_overtime_end").val(data);
			$("#label_overtime_end").text(data);
		}
		$("#edit_overtime_reason").val(overtime.overwork_reason);
		$("#edit_overtime_comment").val(overtime.comments);
		$("#label_overtime_reason").text(overtime.overwork_reason_name);
		$("#label_overtime_comment").text(overtime.comments);

		if (editable === true) {
			$("#label_overtime_start").hide();
			$("#label_overtime_end").hide();
			$("#label_overtime_reason").hide();
			$("#label_overtime_comment").hide();
			$("#edit_overtime_reason").select2Buttons();
		} else {
			$("#edit_overtime_start").hide();
			$("#edit_overtime_end").hide();
			$("#edit_overtime_reason").hide();
			$("#edit_overtime_comment").hide();
		}
	}
}
function setLabel(data) {
	$("#label_action_time").text(data.action_time);
	$("#label_line_name").text(data.line_name);
	$("#label_process_code").text(data.position_name);
	$("#label_operator_name").text(data.name);
	main_process_code = data.process_code;
}
function opd_loadData() {
	$("#operator_detail_list").jqGrid().clearGridData();
	$("#operator_detail_list").jqGrid('setGridParam', {data : opd_listdata})
		.trigger("reloadGrid", [{current : false}]);
}

function exportReport() {
	var rowData = $("#operator_detail_list").getRowData();
	for (var i in rowData) {
		if (rowData[i].leak === 'true') {
			if (!rowData[i].reason) {
				treatBackMessages("#searcharea", [{errmsg:"工作日报信息不完整,请确认信息完整后再进行导出操作!"}]);
				return;
			}
		}
	}
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: 'operatorProduction.do?method=report', 
		cache: false, 
		data: opd_postData, 
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
	            iframe.src = "operatorProduction.do?method=export&addition="+
	            	"-"+$("#label_action_time").text().replace("-","")
	            	+ "&filePath=" + resInfo.filePath;
	            iframe.style.display = "none";
	            document.body.appendChild(iframe);
			}
						
		}
	});
}

var initDetailView = function() {
	$("input.ui-button").button();
	$("#reportbutton").click(exportReport);
	opd_initGrid();
}

$(function() {

});