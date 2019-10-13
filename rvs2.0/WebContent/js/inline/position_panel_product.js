/** 一览数据对象 */
var listdata = {};

/** 服务器处理路径 */
var servicePath = "position_panel_man.do";
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
var stepOptions = "";
var breakOptions = "";

var device_safety_guide = {};

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
						comments : $("#edit_comments").val()
					}

					hasPcs && pcsO.valuePcs(data, true);

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
											doFinish_ajaxSuccess(xhrobj, textStatus);
											try {
												// 以Object形式读取JSON
												eval('resInfo =' + xhrobj.responseText);
												if (resInfo.errors.length === 0) {
													getWaitings();
													jBreakDialog.dialog("close");
												}
											} catch (e) {
												alert("name: " + e.name + " message: " + e.message + " lineNumber: "
														+ e.lineNumber + " fileName: " + e.fileName);
											};
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
								doFinish_ajaxSuccess(xhrobj, textStatus);
								try {
									// 以Object形式读取JSON
									eval('resInfo =' + xhrobj.responseText);
									if (resInfo.errors.length === 0) {
										getWaitings();
										jBreakDialog.dialog("close");
									}
								} catch (e) {
									alert("name: " + e.name + " message: " + e.message + " lineNumber: "
											+ e.lineNumber + " fileName: " + e.fileName);
								};
							}
						});
					}
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
		width : 760,
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
					};

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
	// 重新读入刚才暂停的作业对象
	var data = {
		material_id : $("#pauseo_material_id").val(),
		workstauts : $("#hidden_workstauts").val()
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

var douse_complete = function(xhrobj) {
	var resInfo = $.parseJSON(xhrobj.responseText);
	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages("#inlineForm", resInfo.errors);
		if (resInfo.sReferChooser) {
			$("#snouts").find("tbody").html(resInfo.sReferChooser);
			mySetReferChooser();
		}
		return;
	}

	treatUsesnout(xhrobj);
	// 工程检查票
	if (resInfo.pcses && resInfo.pcses.length > 0 && hasPcs) {
		pcsO.generate(resInfo.pcses, true);
		pcsO.loadCache();
	}
}

var mySetReferChooser = function() {
	var target = $("#input_snout");
	var shower = target.prev("input:text");
	var jthis = $("#usesnoutarea .referchooser");
	jthis.css({"top" : shower.position().top, "left" : shower.position().left - 40}).show("fast");
}

var dounuse = function(serial_no) {
	var data = {serial_no : serial_no};
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : "position_panel_snout.do" + '?method=dounusesnout',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : douse_complete
	});
}

var douse = function(serial_no) {
	var data = {serial_no : serial_no, process_code : "001"};
	// 检查是否第一个
	if (!(serial_no == $("#snouts tr.firstMatchSnout .referId").text())) {
		warningConfirm("您选择的不是该型号最早完成的先端组件，继续吗？"
		, function(){douse_send(data);}
		);
	} else {
		douse_send(data);
	}
}
var douse_send = function(data) {
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : "position_panel_snout.do" + '?method=dousesnout',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : douse_complete
	});
}

var treatUsesnout = function(xhrobj) {
	var resInfo = null;

	// 以Object形式读取JSON
	eval('resInfo =' + xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages(null, resInfo.errors);
	} else {
		var isLightFix = false;
		if (resInfo.mform && resInfo.mform.level) {
			var level = resInfo.mform.level;
			isLightFix = f_isLightFix(level);
		}
		if (resInfo.snout_model >= 1 && !isLightFix) {
			$("#usesnoutarea").show();
			$("#snoutpane td:eq(1)").text($("#material_details td:eq(3)").text());
			if (resInfo.snout_model == 2) {
				$("#snoutpane td:eq(2), #snoutpane td:eq(3), #snoutpane td:eq(6), #snoutpane td:eq(7), #snouts").show();
				$("#snoutpane td:eq(4), #snoutpane td:eq(5), #unusesnoutbutton").hide();
				// 已使用先端头
				if (resInfo.used_snout){
					$("#snoutpane td:eq(4), #snoutpane td:eq(5)").show();
					$("#snoutpane td:eq(5)").text(resInfo.used_snout);
				}
				// 关联先端头参照
				if (resInfo.sReferChooser != null) {
					$("#snouts").find("tbody").html(resInfo.sReferChooser);
					mySetReferChooser();
				}
			} else if (resInfo.used_snout){
				$("#snoutpane td:eq(2), #snoutpane td:eq(3), #snoutpane td:eq(6), #snoutpane td:eq(7), #snouts").hide();
				$("#snoutpane td:eq(4), #snoutpane td:eq(5), #unusesnoutbutton").show();
				// 使用中的先端头
				$("#snoutpane td:eq(5)").text(resInfo.used_snout);

				$("#unusesnoutbutton").unbind("click");
				$("#unusesnoutbutton").click(function() {
					dounuse($("#snoutpane td:eq(5)").text());
				});
			} else {
				$("#snoutpane td:eq(2), #snoutpane td:eq(3), #snoutpane td:eq(6), #snoutpane td:eq(7), #snouts").show();
				$("#snoutpane td:eq(4), #snoutpane td:eq(5), #unusesnoutbutton").hide();
				// 关联先端头参照
				if (resInfo.sReferChooser != null) {
					$("#snouts").find("tbody").html(resInfo.sReferChooser);
					mySetReferChooser();
				}
			}
			$("#input_snout").val("").prev().val("");

			if (resInfo.leagal_overline) {
				leagal_overline = (resInfo.leagal_overline || 120);
				$("#material_details td:eq(9)").text(minuteFormat(leagal_overline) + (leagal_overline ? ":00" : ""));
			
				var nspent_mins = convertMinute($("#dtl_process_time label").text());
				var frate = parseInt(nspent_mins / leagal_overline * 100);
				if (frate > 99) {
					frate = 99;
				}
				$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
			}
		} else {
			$("#usesnoutarea").hide();
		}
	}
}

var getUsesnout = function(material_id) {
	// 取得可使用先端头信息
	var data = {material_id : material_id};
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : "position_panel_snout.do" + '?method=getMaterialUse',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : treatUsesnout
	});
}

var treatPause = function(resInfo) {
	$("#scanner_container").hide();
	$("#material_details").show();
	$("#other_px_change_button").disable();
	$("#position_status").text("暂停中")
		.css("background-color", "#0080FF");
	$("#pausebutton").hide();
	$("#finishbutton").disable();
	$("#breakbutton").disable();
	$("#stepbutton").disable();
	$("#unusesnoutbutton").disable();
	$("#continuebutton").show();
	$("#p_rate div:animated").stop();

	if (resInfo) {
		$("#material_details td:eq(0) input:hidden").val(resInfo.mform.material_id);
		$("#material_details td:eq(1)").text(resInfo.mform.sorc_no);
		$("#material_details td:eq(3)").text(resInfo.mform.model_name);
		$("#material_details td:eq(5)").text(resInfo.mform.serial_no);

		if (resInfo.action_time) {
			$("#material_details td:eq(7)").text(resInfo.action_time);
		} else {
			var thistime=new Date();
			var hours=thistime.getHours();
			var minutes=thistime.getMinutes();
	
			$("#material_details td:eq(7)").text(fillZero(hours, 2) + ":" + fillZero(minutes, 2));
		}
		$("#material_details td:eq(9)").text(minuteFormat(resInfo.leagal_overline) +  + (leagal_overline ? ":00" : ""));
		leagal_overline = resInfo.leagal_overline;
	
		$("#dtl_process_time label").text(minuteFormat(resInfo.spent_mins));
		var frate = parseInt((resInfo.spent_mins) / leagal_overline * 100);
		if (frate > 99) {
			frate = 99;
		}
		$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
	
		if (resInfo.peripheralData && resInfo.peripheralData.length > 0) {
			showPeripheral(resInfo);
		}

		$("#device_details table tbody").find(".manageCode").disable();
		$("#device_details table tbody").find("input[type=button]").disable();
		$("#finishcheckbutton").disable();

		if (resInfo.workstauts != 5) {
			// 工程检查票
			if (resInfo.pcses && resInfo.pcses.length > 0 && hasPcs) {
				pcsO.generate(resInfo.pcses, true);
			}
		}

		if ($("#usesnoutarea").length > 0) getUsesnout(resInfo.mform.material_id);
	}

	clearInterval(oInterval);
}

var treatStart = function(resInfo) {

	$("#scanner_inputer").attr("value", "");
	$("#scanner_container").hide();
	$("#material_details").show();
	$("#other_px_change_button").disable();
	with($("#position_status")) {
		text("处理中");
		css("background-color", "#58b848");
	}

	$("#material_details td:eq(0) input:hidden").val(resInfo.mform.material_id);
	$("#material_details td:eq(1)").text(resInfo.mform.sorc_no);
	$("#material_details td:eq(3)").text(resInfo.mform.model_name);
	$("#material_details td:eq(5)").text(resInfo.mform.serial_no);

	if (resInfo.action_time) {
		$("#material_details td:eq(7)").text(resInfo.action_time);
	} else {
		var thistime=new Date();
		var hours=thistime.getHours();
		var minutes=thistime.getMinutes();

		$("#material_details td:eq(7)").text(fillZero(hours, 2) + ":" + fillZero(minutes, 2));
	}
	$("#material_details td:eq(9)").text(minuteFormat(resInfo.leagal_overline) + (leagal_overline ? ":00" : ""));
	leagal_overline = resInfo.leagal_overline;

	$("#dtl_process_time label").text(minuteFormat(resInfo.spent_mins));
	var frate = parseInt((resInfo.spent_mins) / leagal_overline * 100);
	if (frate > 99) {
		frate = 99;
	}
	$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
	p_time = resInfo.spent_mins - 1;

	var p_operator_cost = $("#p_operator_cost").text();

	t_operator_cost = convertMinute(p_operator_cost);// + resInfo.spent_mins;
		
	$("#continuebutton").hide();
	$("#breakbutton").enable();
	$("#stepbutton").enable();
	$("#pausebutton").show();
	$("#finishbutton").enable();
	$("#unusesnoutbutton").enable();

	ctime();
	oInterval = setInterval(ctime,iInterval);
	$("#w_" + resInfo.mform.material_id).hide("drop", {direction: 'right'}, function() {
		var jthis = $(this);
		var jGroup = jthis.prevAll(".w_group");
		jthis.remove();
		if (jGroup.nextUntil(".w_group").length == 0) {
			jGroup.hide("fade", function() {
				jGroup.remove();
			});
		}
	});

	if (resInfo.quality_tip || resInfo.material_comment) {
		showTips(resInfo.quality_tip, resInfo.material_comment);
	}

	if (resInfo.material_comment || (device_safety_guide && device_safety_guide.length)) {
		showSidebar(resInfo.material_comment);
	} else {
		$("#comments_sidebar").hide();
	}

	if (resInfo.peripheralData && resInfo.peripheralData.length > 0) {
		showPeripheral(resInfo);
	}

	if (resInfo.workstauts == 1) {
		$("#device_details table tbody").find(".manageCode").disable();
		$("#device_details table tbody").find("input[type=button]").disable();
		$("#finishcheckbutton").disable();
	} else {
		$("#device_details table tbody").find(".manageCode").enable();
		$("#device_details table tbody").find(".manageCode").trigger("change");
	}

	if (resInfo.workstauts == 4) {
		$("#finishbutton").disable();
		hasPcs && pcsO.clear();
	} else {
		// 工程检查票
		if (resInfo.pcses && resInfo.pcses.length > 0 && hasPcs) {
			pcsO.generate(resInfo.pcses, true);
		};

		if ($("#usesnoutarea").length > 0) getUsesnout(resInfo.mform.material_id);
	};
};

var showTips = function(quality_tip, material_comment) {
	// side_edge TODO
	$("#comments_dialog textarea").val(material_comment);
	$("#comments_dialog").find("img").remove();
	if (quality_tip) {
		$("#comments_dialog").append("<br/><img src='/photos/quality_tip/" + quality_tip.quality_tip_id + "'></img>");
		if (quality_tip.bind_type == 1) {
			document.cookie = "qt4=" + (new Date()).getTime();
		}
	}
 	$("#comments_dialog").dialog({
		modal : false,
		resizable:false,
		width : '576px',
		title : "作业对象相关信息",
		closeOnEscape: false
	});
}

var showSidebar = function(material_comment) {

	var $ul = $("<ul/>");
	var $content = $("<div class='tip_pages'/>");
	
	if (material_comment) {
		$ul.append("<li><input type='radio' id='st_material_comment' name='showTips'><label for='st_material_comment'>作业中产品备注<label></li>");
		$content.append("<div class='tip_page' for='st_material_comment'>" + material_comment + "</img>");
	}
	if (device_safety_guide) {
		for (var idsg in device_safety_guide) {
			var device_type = device_safety_guide[idsg];
			$ul.append("<li><input type='radio' id='st_" + device_type.devices_type_id 
				+ "' name='showTips'><label for='st_" + device_type.devices_type_id + "'>" + device_type.name + "<label></li>");
			$content.append("<div class='tip_page' for='st_" + device_type.devices_type_id + "'>" 
				+ (device_type.hazardous_cautions ? "<pre>危险标示：" + device_type.hazardous_cautions + "</pre>" : "")
				+ (device_type.safety_guide ? "<img src='/photos/safety_guide/" + device_type.devices_type_id + "'></img>" : "")
				+ "</div>");
		}
	}
	$content.children().hide();

	$("#comments_sidebar .comments_area").html("")
		.append($content)
		.append($ul)
		.find("ul").buttonset()
		.find("input:radio").change(function(){
			$("#comments_sidebar .tip_page").hide();
			$("#comments_sidebar .tip_page[for='" + $(this).attr("id") + "']").show();
		})
		.end().find("input:radio:eq(0)").attr("checked", "checked").trigger("change");
	$("#comments_sidebar .comments_area").hide();
	$("#comments_sidebar").removeClass("shown").css({width:"30px",opacity:".5"});
	$("#comments_sidebar .ui-widget-header span").removeClass("icon-enter-2").addClass("icon-share");

	$("#comments_sidebar").show();

}

var doInit_ajaxSuccess = function(xhrobj, textStatus){

	var resInfo = null;
//	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			$("#hidden_workstauts").val(resInfo.workstauts);
			if (resInfo.workstauts == -1) {
				showBreakOfInfect(resInfo.infectString);
				return;
			}
			// 建立等待区一览
			if (resInfo.waitings) {
				showWaitings(resInfo.waitings, resInfo.waitingsOtherPx);
			}

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
			pauseOptions = resInfo.pauseOptions;
			breakOptions = resInfo.breakOptions;
			stepOptions = resInfo.stepOptions;
			if (!stepOptions) {
				$("#stepbutton").hide();
			} else {
				$("#stepbutton").show();
			}
			if (breakOptions == "") {
				$("#breakbutton").hide();
			}

			if(resInfo.waitings && resInfo.waitings.length >= 1) {
				$("#pauseo_material_id").val(resInfo.waitings[0].material_id);
			}

			// 设备危险归类/安全手册信息
			if (resInfo.position_hcsgs) device_safety_guide = resInfo.position_hcsgs;

			if (resInfo.workstauts == 1 || resInfo.workstauts == 4) {
				treatStart(resInfo);
				hasPcs && pcsO.loadCache();
			} else if (resInfo.workstauts == 2 || resInfo.workstauts == 5) {
				treatPause(resInfo);
				hasPcs && pcsO.loadCache();
			} else if (resInfo.workstauts == 3) {
			} else {
				if (device_safety_guide && device_safety_guide.length) {
					showSidebar(null);
				} else {
					$("#comments_sidebar").hide();
				}
			}

			// 如果打开作业中但是没有
			var flowtext = resInfo.past_fingers;
			if (!resInfo.fingers && $("#material_details").is(":visible")) {
				getJustWorkingFingers(resInfo.mform.material_id);
			} else {
				if (resInfo.lightFix) flowtext = resInfo.lightFix + (flowtext ? "<br>" + flowtext : "");
				if (resInfo.fingers) flowtext = resInfo.fingers + (flowtext ? "<br>" + flowtext : "");
				if (flowtext) $("#flowtext").html(flowtext);
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

var getJustWorkingFingers = function(material_id) {
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doPointOut',
		cache : false,
		data : {material_id : material_id},
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj) {
			var resInfo = null;
//			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);

				if (resInfo.fingers) flowtext = resInfo.fingers + (resInfo.past_fingers ? "<br>" + resInfo.past_fingers : "");
				$("#flowtext").html(flowtext);
//			} catch (e) {
//				alert("doPointOut error");
//			};
		}
	});
}

var getFlags = function(reworked) {
	if (reworked) {
		var retDiv = "<div class='material_flags'>";

		if (reworked == 1) {
			retDiv += "<div class='service_repair_flg'>返</div>";
		}
		retDiv += "</div>";
		return retDiv;
	} else {
		return "";
	}
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
	$("#other_px_change_button").enable();
	$("#continuebutton").hide();
	$("#manualdetailarea").hide();

	$("#position_status").text("准备中")
		.css({"background-color": "#0080FF","color": "white"});

	hasPcs && pcsO.init($("#manualdetailarea"), false);

	doInit();

	// 输入框触发，配合浏览器
	$("#scanner_inputer").keypress(function(){
	if (this.value.length === 6) {
		doStart();
	}
	});
	$("#scanner_inputer").keyup(function(){
	if (this.value.length >= 6) {
		doStart();
	}
	});

	if ($("#snout_origin").length > 0) {
	$("#snout_origin").keypress(function(){
	if (this.value.length === 11) {
		var snout_origin = this.value;
		this.value = "";
		var serial_no = $("#snouts .originId:contains(" + snout_origin + ")").parent().children(".referId").text();
		if (serial_no) checkSnoutPartial(function(){douse(serial_no)}); else errorPop("该先端来源相关的先端头不可使用。");
	}
	});
	$("#snout_origin").keyup(function(){
	if (this.value.length >= 11) {
		var snout_origin = this.value;
		this.value = "";
		var serial_no = $("#snouts .originId:contains(" + snout_origin + ")").parent().children(".referId").text();
		if (serial_no) checkSnoutPartial(function(){douse(serial_no)}); else errorPop("该先端来源相关的先端头不可使用。");
	}
	});
	}

	$("#finishbutton").click(doFinish);
	$("#breakbutton").click(makeBreak);
	$("#stepbutton").click(makeStep);
	$("#pausebutton").click(makePause);
	$("#continuebutton").click(endPause);

	$("#comments_sidebar .ui-widget-header span").on("click",function(){

		if($("#comments_sidebar").hasClass("shown")){
			$("#comments_sidebar .tip_pages").css("overflow-y", "hidden");
			$("#comments_sidebar img").hide();
			$("#comments_sidebar .comments_area").slideUp(200,function(){
				$("#comments_sidebar .ui-widget-header span").removeClass("icon-enter-2").addClass("icon-share");
				$("#comments_sidebar").animate({width:"30px",opacity:".5"},300);
			});
			$("#comments_sidebar").removeClass("shown");
		}else{

			$("#comments_sidebar").animate({width:"1024px",opacity:"1"},300,function(){
				$("#comments_sidebar .ui-widget-header span").removeClass("icon-share").addClass("icon-enter-2");
				$("#comments_sidebar .comments_area").slideDown(200,function(){
					$("#comments_sidebar img").show();
					$("#comments_sidebar .tip_pages").css("overflow-y", "auto");
				});
			});
			$("#comments_sidebar").addClass("shown");
		}
	});

	takeWs();

});

var doStart_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors && resInfo.errors.length > 0) {
			if (resInfo.infectString) {
				showBreakOfInfect(resInfo.infectString);
				return;
			} else {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			}
		} else {
			$("#hidden_workstauts").val(resInfo.workstauts);
			if (resInfo.workstauts == 1 || resInfo.workstauts == 4) {
				treatStart(resInfo);
				if (typeof(operator_ws) === "object") operator_ws.send("callLight:");
				getJustWorkingFingers(resInfo.mform.material_id);
			} else if (resInfo.workstauts == 3) {
			} else if (resInfo.workstauts == 3.9) {
				doFinish();
			}
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var doStart=function(){
	var data = {
		serial_no : $("#scanner_inputer").val()
	}
	$("#scanner_inputer").attr("value", "");

	doStartForward(data);
};

var doStartForward=function(data){

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

var doFinish_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			if (resInfo.workstauts == 3) {
			}
			treatBackMessages(null, resInfo.errors);
		} else {
			$("#hidden_workstauts").val(resInfo.workstauts);
			if (resInfo.workstauts == 2 || resInfo.workstauts == 5) {
				treatPause(resInfo);
			} else if (xhrobj.status == 200){
				$("#scanner_inputer").attr("value", "");
				$("#material_details").hide();
				$("#other_px_change_button").enable();
				$("#scanner_container").show();
				$("#position_status").text("准备中")
					.css("background-color", "#0080FF");
				$("#manualdetailarea").hide();
				$("#devicearea").hide();

				if (resInfo.past_fingers) $("#flowtext").text(resInfo.past_fingers);

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
				$("#working").text("");
				$("#scanner_inputer").focus();
				$("#usesnoutarea").hide();

				if ($("#comments_dialog:visible").length > 0) {
					$("#comments_dialog textarea").val("");
					$("#comments_dialog").dialog("close");
				}
				if ($("#comments_sidebar:visible").length > 0) {
					$("#comments_sidebar .comments_area").val("");
					$("#comments_sidebar").hide();
				}

				hasPcs && pcsO.clearCache();
			}
		}
	} catch (e) {
		console.log("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
}

var doFinish=function(){
	var data = {};
	var empty = false;
	if (hasPcs) {
		empty = pcsO.valuePcs(data);
	}

	if (empty) {
		errorPop("请填写完所有的工程检查票选项后，再完成本工位作业。");
	}

	if (!empty) {
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
};

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
	} else {
		liquid.addClass("tube-green");
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
	if (!iminute) return "-";
	var hours = parseInt(iminute / 60);
	var minutes = iminute % 60;

	return fillZero(hours, 2) + ":" + fillZero(minutes, 2);
}

var convertMinute =function(sminute) {
	var hours = sminute.replace(/(.*):(.*)/, "$1");
	var minutes = sminute.replace(/(.*):(.*)/, "$2");

	return hours * 60 + parseInt(minutes);
}

var showWaitings = function(waitings, waitingsOtherPx){
	var reason = "";
	var waiting_html = "";
	var waitingsCount = waitings.length;
	for (var iwaiting = 0; iwaiting < waitingsCount; iwaiting++) {
		var waiting = waitings[iwaiting];
		if (reason != waiting.waitingat) {
			reason = waiting.waitingat;
			waiting_html += '<div class="ui-state-default w_group" style="width: 520px; margin-top: 12px; margin-bottom: 8px; padding: 2px;">'+ reason +':</div>'
		}
		waiting_html += '<div class="waiting tube" id="w_' + waiting.material_id + '">' +
							'<div class="tube-liquid">'
								+ waiting.category_name + ' | ' + waiting.model_name + ' | ' + waiting.serial_no
								+ getFlags(waiting.reworked) +
							'</div>' +
						'</div>'
	}
	$("#waitings").html(waiting_html);
	$("#other_px_area").css("padding-top", "6px").html("<span>等待数</span><span style='padding:0.5em;'>" + (waitingsCount || 0) + "</span>");
}

var getWaitings = function() {
	$.ajax({
		data: null,
		url : servicePath + "?method=refreshWaitings",
		type : "post",
		cache : false,
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus){
			var resInfo = null;
	    	try {
	    		resInfo = $.parseJSON(xhrobj.responseText);
				showWaitings(resInfo.waitings, resInfo.waitingsOtherPx);
	    	} catch(e) {
	    	}
		}
	});
};

// 工位后台推送
function takeWs() {
	var g_pos_id = $("#g_pos_id").val(); 

	if (g_pos_id) {
		// 创建WebSocket  
		var position_ws = new WebSocket(wsPath + "/position");
		// 收到消息时做相应反应
		position_ws.onmessage = function(evt) {
	    	var resInfo = {};
	    	try {
	    		resInfo = $.parseJSON(evt.data);
	    	} catch(e) {
	    	}
    		if ("refreshWaiting" == resInfo.method) {
    			getWaitings();
    		}
    	}
	};  
	// 连接上时走这个方法  
	position_ws.onopen = function() {     
		position_ws.send("entach:" + g_pos_id + "#"+$("#op_id").val());
	}; 
};

var checkSnoutPartial = function(callback) {
	var material_id = $("#pauseo_material_id").val();
	var occur_times = 1;
	var data = {
		material_id: material_id,
		occur_times: occur_times
	};
	callback();
}

