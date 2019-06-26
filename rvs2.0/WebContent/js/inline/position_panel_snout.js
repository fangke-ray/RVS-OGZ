/** 一览数据对象 */
var listdata = {};

/** 服务器处理路径 */
var servicePath = "position_panel_snout.do";
var hasPcs = (typeof pcsO === "object");

// 已启动作业时间
var p_time = 0;
// 定时处理对象
var oInterval, ttInterval;
// 定时处理间隔（1分钟）
var iInterval = 60000;
// 取到的标准作业时间
var leagal_overline;
var t_operator_cost = 0;
var t_run_cost = 0;
var pauseOptions = "";
var breakOptions = "";
var stepOptions = "";

/** 中断信息弹出框 */
var makeBreakDialog = function(jBreakDialog) {
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
						comments : $("#edit_comments").val(),
						serial_no : $("#material_details td:eq(5)").text()
					}

					if (hasPcs) {
						pcsO.valuePcs(data);
					}

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
							doFinish_ajaxSuccess(xhrobj, textStatus);
							try {
								// 以Object形式读取JSON
								eval('resInfo =' + xhrobj.responseText);
								if (resInfo.errors.length === 0) {
									jBreakDialog.dialog("close");
								} else {
									// 共通出错信息框
									treatBackMessages(null, resInfo.errors);
								}
							} catch (e) {
								alert("name: " + e.name + " message: " + e.message + " lineNumber: "
										+ e.lineNumber + " fileName: " + e.fileName);
							};
						}
					});
				}
			}, "关闭" : function(){ $(this).dialog("close"); }
		}
	});
}

/** 正常中断生成 */
var makeStep = function() {

	var jBreakDialog = $("#break_dialog");
	if (jBreakDialog.length === 0) {
		$("body.outer").append("<div id='break_dialog'/>");
		jBreakDialog = $("#break_dialog");
	}

	jBreakDialog.hide();

	// 导入中断画面
	jBreakDialog.load("widget.do?method=breakoperator",
		function(responseText, textStatus, XMLHttpRequest) {

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
		makeBreakDialog(jBreakDialog);

	});
	$("#break_dialog").show();
};

/** 不良中断生成 */
var makeBreak = function() {

	var jBreakDialog = $("#break_dialog");
	if (jBreakDialog.length === 0) {
		$("body.outer").append("<div id='break_dialog'/>");
		jBreakDialog = $("#break_dialog");
	}

	jBreakDialog.hide();

	// 导入中断画面
	jBreakDialog.load("widget.do?method=breakoperator",
		function(responseText, textStatus, XMLHttpRequest) {

		// 设定中断理由
		$("#break_reason").html(breakOptions);
		$("#break_reason").select2Buttons();

		$("#breakForm").validate({
			rules : {
				break_reason : {
					required : true
				},
				comments : {
					required : function() {
						return ($("#break_reason").val() != null && $("#break_reason").val() != "" && parseInt($("#break_reason").val()) < 10);
					}
				}
			}
		});
		makeBreakDialog(jBreakDialog);

	});
	$("#break_dialog").show();
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
						comments : $("#pauseo_edit_comments").val()
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
							doFinish_ajaxSuccess(xhr, status);
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
		serial_no : $("#material_details td:eq(5)").text()
	}

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

var treatPause = function(resInfo) {

	$("#storagearea").hide();
	$("#manualarea").hide();
	$("#material_details").show();
	$("#position_status").text("暂停中");
	$("#position_status").css("color", "#0080FF");
	$("#pausebutton").hide();
	$("#finishbutton").disable();
	$("#breakbutton").disable();
	$("#stepbutton").disable();
	$("#continuebutton").show();
	$("#p_rate div:animated").stop();

	if (resInfo) {
		$("#material_details td:eq(1)").text(resInfo.snout_origin);
		$("#material_details td:eq(3)").text(resInfo.model_name);
		$("#material_details td:eq(5)").text(resInfo.serial_no);

		if (resInfo.action_time) {
			$("#material_details td:eq(7)").text(resInfo.action_time);
		} else {
			var thistime=new Date();
			var hours=thistime.getHours() 
			var minutes=thistime.getMinutes() 
	
			$("#material_details td:eq(7)").text(prevzero(hours) + ":" + prevzero(minutes));
		}
		$("#material_details td:eq(9)").text(minuteFormat(resInfo.leagal_overline) + ":00");
		leagal_overline = resInfo.leagal_overline;
	
		$("#dtl_process_time label").text(minuteFormat(resInfo.spent_mins));
		var frate = parseInt((resInfo.spent_mins) / leagal_overline * 100);
		if (frate > 99) {
			frate = 99;
		}
		$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
	
		var p_operator_cost = $("#p_operator_cost").text();
	
		// 工程检查票
		if (resInfo.pcses && resInfo.pcses.length > 0 && hasPcs) {
			pcsO.generate(resInfo.pcses, true);
		}
	}

	clearInterval(oInterval);
}

var treatStart = function(resInfo) {

	$("#storagearea").hide();
	$("#manualarea").hide();
	$("#material_details").show();
	$("#position_status").text("处理中");
	$("#position_status").css("color", "#58b848");

	$("#material_details td:eq(1)").text(resInfo.snout_origin);
	$("#material_details td:eq(3)").text(resInfo.model_name);
	$("#material_details td:eq(5)").text(resInfo.serial_no);

	if (resInfo.action_time) {
		$("#material_details td:eq(7)").text(resInfo.action_time);
	} else {
		var thistime=new Date();
		var hours=thistime.getHours() 
		var minutes=thistime.getMinutes() 

		$("#material_details td:eq(7)").text(prevzero(hours) + ":" + prevzero(minutes));
	}
	$("#material_details td:eq(9)").text(minuteFormat(resInfo.leagal_overline) + ":00");
	leagal_overline = resInfo.leagal_overline;

	$("#dtl_process_time label").text(minuteFormat(resInfo.spent_mins));
	var frate = parseInt((resInfo.spent_mins) / leagal_overline * 100);
	if (frate > 99) {
		frate = 99;
	}
	$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
	p_time = resInfo.spent_mins - 1;

	$("#p_operator_cost").text(resInfo.spent_mins);
	var p_operator_cost = $("#p_operator_cost").text();

//	if (p_operator_cost.indexOf(':') < 0) {
//		t_operator_cost = p_operator_cost;
//	} else {
		t_operator_cost = convertMinute(p_operator_cost);// + resInfo.spent_mins;
//	}

	$("#continuebutton").hide();
	$("#finishbutton").enable();
	$("#breakbutton").enable();
	$("#stepbutton").enable();
	$("#pausebutton").show();
	ctime();
	oInterval = setInterval(ctime,iInterval);

	// 工程检查票
	if (resInfo.pcses && resInfo.pcses.length > 0 && hasPcs) {
		pcsO.generate(resInfo.pcses, true);
	}
	$("#scanner_inputer").focus();
}

var doInit_ajaxSuccess = function(xhrobj, textStatus){
	//return;
	var resInfo = null;
//	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
	
			if (resInfo.workstauts == -1) {
				showBreakOfInfect(resInfo.infectString);
				return;
			}

			// 建立等待区一览
			var reason = "";
			var waiting_html = "";
			for (var iwaiting = 0; iwaiting < resInfo.waitings.length; iwaiting++) {
				var waiting = resInfo.waitings[iwaiting];
				if (reason != waiting.waitingat) {
					reason = waiting.waitingat;
					waiting_html += '<div class="ui-state-default w_group" style="width: 420px; margin-top: 12px; margin-bottom: 8px; padding: 2px;">'+ reason +':</div>'
				}
				waiting_html += '<div class="waiting tube" model_id="' + waiting.model_id + '" model_name="' + waiting.model_name + '" serial_no="' + waiting.serial_no + '">' +
									'<div class="tube-liquid' + expeditedColor(waiting.expedited)  + '">' +
										(waiting.sorc_no == null ? "" : waiting.sorc_no + ' | ') + waiting.model_name + ' | ' + waiting.serial_no +
									'</div>' +
								 '<div class="click_start"><input type="button" value="》开始"></div>' +
								'</div>'
			}
			var $waiting_html = $(waiting_html);
			$waiting_html.find("input:button").button().click(function(){
				var $tube = $(this).parent().parent();
				var chosedData = {
					model_id : $tube.attr("model_id"),
					model_name : $tube.attr("model_name"),
					serial_no : $tube.attr("serial_no")
				}
				doStart(null, chosedData);
			});
			$("#waitings").html($waiting_html);

			// 计算当前用时
			var p_operator_cost = $("#p_operator_cost").text();
			if (p_operator_cost.indexOf(':') < 0) {
				t_operator_cost = p_operator_cost;
				$("#p_operator_cost").text(minuteFormat(t_operator_cost));
			}

			// 计算总用时
			var p_run_cost = $("#p_run_cost").text();
			if (p_run_cost.indexOf(':') < 0) {
				if (p_run_cost != "0" && p_run_cost != "") {
					t_run_cost = p_run_cost;
					$("#p_run_cost").text(minuteFormat(t_run_cost));
					ttInterval = setInterval(ttime, iInterval);
				}
			}

			if (resInfo.infectString) {
				$("#toInfect").show()
				.find("td:eq(1)").html(decodeText(resInfo.infectString));
			} else {
				$("#toInfect").hide();
			}

			// 暂停理由
			$("#input_model_id").html(resInfo.modelOptions).select2Buttons();
			pauseOptions = resInfo.pauseOptions;
			breakOptions = resInfo.breakOptions;
			stepOptions = resInfo.stepOptions;
			if (stepOptions == "") {
				$("#stepbutton").hide();
			}
			if (breakOptions == "") {
				$("#breakbutton").hide();
			}

			if (resInfo.workstauts == 1) {
				treatStart(resInfo);
				if (hasPcs) {
					pcsO.loadCache();
				}
			} else if (resInfo.workstauts == 2) {
				treatPause(resInfo);
				if (hasPcs) {
					pcsO.loadCache();
				}
			} else {
				$("#input_model_id").disable();
				$("#input_snout_no").disable();
				$("#startbutton").disable();
			}

		}
//	} catch (e) {
//		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
//				+ e.lineNumber + " fileName: " + e.fileName);
//	};
};

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

var expeditedColor = function(expedited) {
	if (expedited == -1) return ' tube-gray'; // 普通
	if (expedited == 1) return ' tube-blue'; // 加急
	return ' tube-green'; // 当日
}

var doInit=function(){
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=jsinit',
		cache : false,
		data : {},
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : doInit_ajaxSuccess
	});
};

//$(document).ready(function() {
$(function() {
	$("input.ui-button").button();

	$("#material_details").hide();
	$("#continuebutton").hide();
	$("#manualdetailarea").hide();

	
	if (hasPcs) {
		pcsO.init($("#manualdetailarea"), false);
	}

	doInit();

	$("#startbutton").click(doStart);
	$("#finishbutton").click(doFinish);
	$("#pausebutton").click(makePause);
	$("#breakbutton").click(makeBreak);
	$("#stepbutton").click(makeStep);
	$("#continuebutton").click(endPause);

	// 输入框触发，配合浏览器
	$("#scanner_inputer").keypress(function(){
	if (this.value.length === 11) {
		doSetOrigin();
	}
	});
	$("#scanner_inputer").keyup(function(){
	if (this.value.length >= 11) {
		doSetOrigin();
	}
	});

});

var doSetOrigin = function() {
	var data = {
		material_id : $("#scanner_inputer").val()
	}
	$("#scanner_inputer").attr("value", "");

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=checkScan',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : doSetOrigin_ajaxSuccess
	});
}

var doSetOrigin_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = $.parseJSON(xhrobj.responseText);
	
	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages(null, resInfo.errors);
	} else {
		var mForm = resInfo.mForm;
		$("#scanner_inputer").val(mForm.material_id).disable();
		$("#input_model_id").val(mForm.model_id).enable().trigger("change");
		$("#input_snout_no").val(mForm.serial_no).enable();
		$("#startbutton").enable();
		if (resInfo.Continue) {
			$("#startbutton").trigger("click");
		}
	}
}

var doStart_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			$("#scanner_inputer").val("");
			$("#input_model_id").attr("value", "").trigger("change");
			$("#input_snout_no").attr("value", "");

			treatStart(resInfo);
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var doStart=function(evt, chosedData){
	
	var data = {
		material_id : $("#scanner_inputer").val(),
		model_id : $("#input_model_id").val(),
		model_name : toLabelValue($("#input_model_id")),
		serial_no : $("#input_snout_no").val()
	}

	if (hasPcs) {
		pcsO.clearCache();
	}

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=dostart',
		cache : false,
		data : chosedData || data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : doStart_ajaxSuccess
	});
};

var doFinish_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		$('div#errstring').dialog("close");

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			if (resInfo.workstauts == 2) {
				treatPause(resInfo);
			} else {
				$("#scanner_inputer").attr("value", "");
				$("#storagearea").show();
				$("#manualarea").show();
				$("#material_details").hide();
				$("#position_status").text("准备中");
				$("#position_status").css("color", "#0080FF");
				$("#manualdetailarea").hide();
	
				$("#material_details td:eq(7)").text("");
				$("#dtl_process_time label").text("");
				$("#p_rate").html("");
				p_time = 0;
				clearInterval(oInterval);
				$("#pauseo_edit").hide();
				$("#pauseo_show").show();
				$("#pauseo_show_sorc_no").text($("#material_details td:eq(1)").text());
				$("#pauseo_show_pause_reason").text(toLabelValue($("#pauseo_edit_pause_reason")));
				$("#pauseo_show_comments").text(toLabelValue($("#pauseo_edit_comments")));
				if (!$("#break_dialog").is(":empty")) {
					$("#break_dialog").dialog("option", "buttons", {
							"再开":function(){
								endPause();
							}
						}
					);
				}
				$("#scanner_inputer").val("").focus().enable();
				$("#input_model_id").disable();
				$("#input_snout_no").disable();

				if (hasPcs) {
					pcsO.clearCache();
				}
			}
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
}

var doFinishPost=function(data){
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
		complete : doFinish_ajaxSuccess
	});
}


var doFinish=function(){
	var data = {};
	var empty = false;
	if (hasPcs) {
		empty = pcsO.valuePcs(data);
	}

	if (empty) {
		warningConfirm("存在没有填的工程检查票选项，可以就这样提交吗？"
		, function(){doFinishPost(data)}
		, function(){
			$('div#errstring').dialog("close");
		});
	}

	if (!empty) {
		doFinishPost(data);
	}
};

var prevzero =function(i) {
	if (i < 10) {
		return "0" + i; 
	} else {
		return "" + i; 
	}
}

// 进行中效果
var ctime=function(){
	p_time++;
	$("#dtl_process_time label").text(minuteFormat(p_time));

	var rate = parseInt((p_time + 1) / leagal_overline * 100);
	//var nextrate = 
	if (rate == 99) return;
	if (rate >= 100) rate = 99;
	var liquid = $("#p_rate div");
	liquid.animate({width : rate + "%"}, iInterval, "linear");
	if (rate > 80) {
		liquid.removeClass("tube-green");
		if (rate > 95) {
			liquid.removeClass("tube-yellow");
			liquid.addClass("tube-orange");
		} else {
			liquid.addClass("tube-yellow");
		}
	}

	$("#p_operator_cost").text(minuteFormat(t_operator_cost));
	t_operator_cost++;
};

// 进行中效果
var ttime=function(){
	$("#p_run_cost").text(minuteFormat(t_run_cost));
	t_run_cost++;
};

var minuteFormat =function(iminute) {
	var hours = parseInt(iminute / 60);
	var minutes = iminute % 60;

	return prevzero(hours) + ":" + prevzero(minutes);
}

var convertMinute =function(sminute) {
	var hours = sminute.replace(/(.*):(.*)/, "$1");
	var minutes = sminute.replace(/(.*):(.*)/, "$2");

	return hours * 60 + parseInt(minutes);
};
