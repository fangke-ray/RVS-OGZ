/** 一览数据对象 */
var listdata = {};

/** 服务器处理路径 */
var servicePath = "position_panel" + (parseInt(Math.random() * 5) + 1) + ".do";
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

var partial_closer = true;

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
	// 重新读入刚才暂停的维修对象
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
			setSnoutRefers(resInfo.sReferChooser);
			mySetReferChooser();
		}
		return;
	}

	treatUsesnout(xhrobj);
	// 工程检查票
	if (resInfo.pcses && resInfo.pcses.length > 0 && hasPcs) {
		pcsO.generate(resInfo.pcses, true, false, resInfo.pcsLimits);
		pcsO.loadCache();
	}
}

var setSnoutRefers = function(sReferChooser) {
	$("#snouts").find("tbody").html(sReferChooser);
}

var mySetReferChooser = function() {
	var target = $("#input_snout");
	var shower = target.prev("input:text");
	var jthis = $("#usesnoutarea .referchooser");
	jthis.css({"top" : shower.position().top, "left" : shower.position().left - 40}).show("fast");
}

var dounuse = function(serial_no) {
	var data = {serial_no : serial_no, process_code : "301"};
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
	var data = {serial_no : serial_no, process_code : "301"};
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
					setSnoutRefers(resInfo.sReferChooser);
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
					setSnoutRefers(resInfo.sReferChooser);
					mySetReferChooser();
				}
			}
			$("#input_snout").val("").prev().val("");

			if (resInfo.leagal_overline) {
				leagal_overline = (resInfo.leagal_overline || 120);
				$("#material_details td:eq(9)").text(minuteFormat(leagal_overline)); //  + (leagal_overline ? ":00" : "")
			
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

var resetSnoutRefers = function() {
	if ($("#pauseo_material_id").val()) {
		// 取得可使用先端头列表
		var data = {material_id : $("#pauseo_material_id").val()};
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
			complete : function(xhrobj){
				var resInfo = $.parseJSON(xhrobj.responseText);
				setSnoutRefers(resInfo.sReferChooser);
			}
		});
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
//	$("#usesnoutbutton").disable();
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
		$("#material_details td:eq(9)").text(minuteFormat(resInfo.leagal_overline)); //  + (leagal_overline ? ":00" : "")
		leagal_overline = resInfo.leagal_overline;
	
		$("#dtl_process_time label").text(minuteFormat(resInfo.spent_mins));
		var frate = parseInt((resInfo.spent_mins) / leagal_overline * 100);
		if (frate > 99) {
			frate = 99;
		}
		$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
	
		$("#working_detail").hide();

		if (resInfo.peripheralData && resInfo.peripheralData.length > 0) {
			showPeripheral(resInfo);
		}

		$("#device_details table tbody").find(".manageCode").disable();
		$("#device_details table tbody").find("input[type=button]").disable();
		$("#finishcheckbutton").disable();

		if (resInfo.workstauts != 5) {
			// 工程检查票
			if (resInfo.pcses && resInfo.pcses.length > 0 && hasPcs) {
				pcsO.generate(resInfo.pcses, true, false, resInfo.pcsLimits);
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
		if (hasClass("simple")) {
			text("处理中");
		} else {
			text("修理中");
		}
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
	$("#material_details td:eq(9)").text(minuteFormat(resInfo.leagal_overline)); //  + (leagal_overline ? ":00" : "")
	leagal_overline = resInfo.leagal_overline;

	$("#dtl_process_time label").text(minuteFormat(resInfo.spent_mins));
	var frate = parseInt((resInfo.spent_mins) / leagal_overline * 100);
	if (frate > 99) {
		frate = 99;
	}
	$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
	p_time = resInfo.spent_mins - 1;

	var p_operator_cost = $("#p_operator_cost").text();

//	if (p_operator_cost.indexOf(':') < 0) {
//		t_operator_cost = p_operator_cost;
//	} else {
		t_operator_cost = convertMinute(p_operator_cost);// + resInfo.spent_mins;
//	}
		
	$("#working_detail").show();

	if ($('#partialconfirmarea:visible').length > 0) { // 
		partial_closer = false;
		$('#partialconfirmarea').dialog("close");
	}

	$("#continuebutton").hide();
	$("#breakbutton").enable();
	$("#stepbutton").enable();
	$("#pausebutton").show();
	$("#finishbutton").enable();
//	$("#usesnoutbutton").enable();
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
			pcsO.generate(resInfo.pcses, true, false, resInfo.pcsLimits);
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
		title : "维修对象相关信息",
		closeOnEscape: false
	});
}

var showSidebar = function(material_comment) {

	var $ul = $("<ul/>");
	var $content = $("<div class='tip_pages'/>");
	
//	if (quality_tip) {
//		$ul.append("<li><input type='radio' id='st_quality_tip' name='showTips'><label for='st_quality_tip'>质量提示<label></li>");
//		$content.append("<div class='tip_page' for='st_quality_tip'><img src='/photos/quality_tip/" + quality_tip.quality_tip_id + "'></img>");
//		if (quality_tip.bind_type == 1) {
//			document.cookie = "qt4=" + (new Date()).getTime();
//		}
//	}
	if (material_comment) {
		$ul.append("<li><input type='radio' id='st_material_comment' name='showTips'><label for='st_material_comment'>维修对象备注<label></li>");
		$content.append("<div class='tip_page' for='st_material_comment'>" + decodeText(material_comment) + "</img>");
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

//	if (quality_tip) {
//		$("#comments_dialog .comments_area").show();
//		$("#comments_dialog").addClass("shown").css({width:"1024px",opacity:"1"});
//		$("#comments_dialog .ui-widget-header span").removeClass("icon-share").addClass("icon-enter-2");
//	} else {
//	}
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
				showPartialRecept(resInfo);
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

				var flowtext = "";

				if (resInfo.fingers) flowtext = resInfo.fingers + (resInfo.past_fingers ? "<br>" + resInfo.past_fingers : "");
				$("#flowtext").html(flowtext);
//			} catch (e) {
//				alert("doPointOut error");
//			};
		}
	});
}

var expeditedColor = function(expedited, today) {
	if (today == 1) return ' tube-green'; // 当日
//	if (expedited >= 10) return ' tube-yellow'; // 加急
//	if (expedited == 1) return ' tube-blue'; // 加急
	return ' tube-gray'; // 普通 
}

var getFlags = function(expedited, direct_flg, light_fix, reworked) {
	if (expedited || direct_flg || light_fix || reworked) {
		var retDiv = "<div class='material_flags'>";
		if (expedited >= 20) retDiv += "<div class='rapid_direct_flg'><span>直送快速</span></div>";
		else {
			if (expedited >= 10) retDiv += "<div class='tube-yellow'>急</div>";
			else if (expedited == 1) retDiv += "<div class='tube-blue'>急</div>";
			retDiv += (direct_flg ? "<div class='direct_flg'>直</div>" : "");
		}
		if (light_fix == 1) {
			retDiv += "<div class='light_fix'>小</div>";
		}
		if (reworked == 1) {
			retDiv += "<div class='service_repair_flg'>返</div>";
		}
		retDiv += "</div>";
		return retDiv;
	} else {
		return "";
	}
}

var getBlock = function(block_status) {
	if (block_status) {
		var retDiv = "<div class='pa_flags'>";
		if (block_status == 1) retDiv += "<div class='bo_flg'><span>BO</span></div>";
		if (block_status == 2) retDiv += "<div class='bo_flg'><span>PA</span></div>";
		retDiv += "</div>";
		return retDiv;
	} else {
		return "";
	}
}

var getLineMinutes = function(line_minutes){
	if (line_minutes) {
		var retDiv = "<div class='plan_advise'>";
		if (line_minutes > 0) retDiv += "<span title='本工程预计完成总工时'>" + minuteFormat(line_minutes) + "</span>";
		if (line_minutes < 0) retDiv += "<span title='投入此维修品最适合保证工程平衡' class='osusume'>" + minuteFormat(-line_minutes) + "</span>";
		retDiv += "</div>";
		return retDiv;
	} else {
		return "";
	}
}

var getLevel = function(level) {
	if (level) {
		var levelText = "S" + level; // TODO
		if (level == 9 || level == 91 || level == 92 || level == 93 || level == 99) levelText = "D";
		if (level == 96 || level == 97 || level == 98 ) levelText = "M";
		if (level == 56 || level == 57 || level == 58 || level == 59) levelText = "E";
		return "<span class='level level_" + levelText + "'>" + levelText + "</span>";
	}
	return "";
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

	$("#working_detail").hide().click(function(){
		var material_id = $("#pauseo_material_id").val();
		showMaterial(material_id);
	});

	$("#position_status").text("准备中")
		.css({"background-color": "#0080FF","color": "white"});

	hasPcs && pcsO.init($("#manualdetailarea"), false);

	doInit();

	// 输入框触发，配合浏览器
	$("#scanner_inputer").keypress(function(){
	if (this.value.length === 11) {
		doStart();
	}
	});
	$("#scanner_inputer").keyup(function(){
	if (this.value.length >= 11) {
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

	$(".waiting").on('dblclick', function(){
		var material_id = this.id.replace("w_", "");
		showMaterial(material_id);
	});

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

var doStart_ajaxSuccess = function(xhrobj, textStatus, postData){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors && resInfo.errors.length > 0) {
			if (resInfo.infectString) {
				showBreakOfInfect(resInfo.infectString);
				return;
			} else if (postData && postData.scan_part == "1" && resInfo.mform) {
				secondaryConfirm(postData, resInfo.mform);
			} else {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			}
		} else {
			$("#hidden_workstauts").val(resInfo.workstauts);
			if (resInfo.workstauts == 1 || resInfo.workstauts == 4) {
				treatStart(resInfo);
				// if (typeof(operator_ws) === "object") operator_ws.send("callLight:");
				getJustWorkingFingers(resInfo.mform.material_id);
			} else if (resInfo.workstauts == 3) {
				showPartialRecept(resInfo);
			} else if (resInfo.workstauts == 3.9) {
				doFinish();
			}
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var showPartialRecept = function(resInfo, finish){
	var $partialconfirmarea = $('#partialconfirmarea');
	if ($partialconfirmarea.length == 0) {
		$("body").append("<div id='partialconfirmarea'><table id='partialConfirmList'/></div>");
		$partialconfirmarea = $('#partialconfirmarea');
		$("#partialConfirmList").jqGrid({
			data:{},
			height: 346,
			width: 800,
			rowheight: 23,
			datatype: "local",
			colNames:['','','零件编号','零件名称','可签收数量','已签收数量','缺品数量','入库预订日','消耗品','bom'],
			colModel:[
				{
					name:'material_partial_detail_key',
					index:'material_partial_detail_key',
					hidden:true
				},
				{
					name:'partial_id',
					index:'partial_id',
					hidden:true
				},
				{
					name:'code',
					index:'code',
					width:40
				},
				{
					name:'partial_name',
					index:'partial_name',
					width:60
				},
				{
					name:'recept_quantity',
					index:'recept_quantity',
					align:'right',
					width:30,
					formatter:'integer',
					sorttype:'integer'
				},
				{
					name:'cur_quantity',
					index:'cur_quantity',
					width:30,
					align:'right',
					formatter:'integer',
					sorttype:'integer'
				},
				{
					name:'waiting_receive_quantity',
					index:'waiting_receive_quantity',
					width:30,
					align:'right',
					formatter:'integer',
					sorttype:'integer'
				},
				{
					name:'arrival_plan_date',
					index:'arrival_plan_date',
					width:50,
					align:'center',
					formatter:function(cellValue,options,rowData){
						if(rowData.status==3){
							if ("9999/12/31" == cellValue) {
								return "未定";
							}
							return cellValue || "";
						}
						return "";
					}
				},
				{
					name:'append',
					index:'append',
					width:20,
					formatter:'select',
					align:'center',
					editoptions:{value:':;1:消耗品'}
				},
				{
					name:'bom_quantity',
					index:'bom_quantity',
					hidden:true
				}
			],
			rowNum: 69,
			toppager: false,
			pager: null,
			viewrecords: true,
			pagerpos: 'right',
			pgbuttons: true,
			pginput: false,
			hidegrid: false, 
			recordpos:'left',
			multiselect:true,
			onSelectRow:function(rowid,statue){
				var pill = $("#recept_partial_list");
				var $row = pill.find("tr#" + rowid);
				var $cb = $row.find("td[aria\\-describedby='partialConfirmList_cb'] input");
				if ($cb.is(":hidden")) {
					$cb.removeAttr("checked");
					$row.removeClass("ui-state-highlight");
				}
			},
			onSelectAll:function(rowids,statue){
				var pill = $("#recept_partial_list");
				var $rows = pill.find("tr");
				$rows.each(function(){
					var $cb = $(this).find("td[aria\\-describedby='partialConfirmList_cb'] input");
					if ($cb.is(":hidden")) {
						$cb.removeAttr("checked");
						$(this).removeClass("ui-state-highlight");
					}
				});
			},
			gridComplete:function(){
				// 得到显示到界面的id集合
				var IDS = $("#partialConfirmList").getDataIDs();
				// 当前显示多少条
				var length = IDS.length;
				var pill = $("#partialConfirmList");

				for (var i = 0; i < length; i++) {
					// 从上到下获取一条信息
					var rowData = pill.jqGrid('getRowData', IDS[i]);
					var not_quantity_data = rowData["recept_quantity"];
					if(not_quantity_data==0){
						pill.find("tr#" + IDS[i] + " td[aria\\-describedby='partialConfirmList_cb']").find("input").hide();
					}
					var bom_quantity = rowData["bom_quantity"];
					var code = rowData["code"];
					if(bom_quantity && bom_quantity > 0 && code.indexOf("*") < 0){
//						pill.jqGrid("setSelection", IDS[i]);
						pill.find("tr#" + IDS[i] + " td").css("background-color", "lightblue");
					}
				}
			}
		});
	
	}
	$("#partialConfirmList").jqGrid().clearGridData();
	$("#partialConfirmList").jqGrid('setGridParam',{data:resInfo.mpds}).trigger("reloadGrid", [{current:false}]);

	partial_closer = true;
	// TODO div infect
	$partialconfirmarea.dialog({
		modal : true,
		resizable:false,
		width : 'auto',
		title : "清点本工位使用零件",
		closeOnEscape: false,
		close: function(){
			if (partial_closer && !finish) window.location.href = "./panel.do?method=init";
		},
		buttons :{
			"退出工位":function() {
				$partialconfirmarea.dialog("close");
			},
			"报告线长":function() {
				reportAndBreak();
			},
			"确定":function() {
				commitPartialConfirm($partialconfirmarea, finish);
			}
		}
	});

	if (resInfo.notMatch) {
		if ($('div#errstring').length == 0) {
			$("body").append("<div id='errstring'/>");
		}
		$('div#errstring').show();
		$('div#errstring').html("<span class='errorarea'>您在本工位清点的零件情况与定位设定不符。<br>请确认您的点检结果，或者报告线长处理。</span>");
		$('div#errstring').dialog({
			dialogClass : 'ui-error-dialog',
			modal : true,
			resizable:false,
			width : 450,
			title : "提示信息",
			buttons :{
				"关闭":function(){
					$('div#errstring').dialog("close");
				}
			}
		});
	}
}

var doCallLeader_ajaxSuccess = function(xhrObj){
	partial_closer = false;
	$('#partialconfirmarea').dialog("close");
}

var reportAndBreak = function() {
	var postData = {};

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doCallLeaderOfPartialMismatch',
		cache : false,
		data : postData,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : doCallLeader_ajaxSuccess
	});
}

var commitPartialConfirm = function($target, finish) {
	var postData = {};

    var rows = $("#partialConfirmList").find("tr:has('input[type=checkbox][checked]:visible')");
    for(var i=0;i<rows.length;i++){
	    var rowData = $("#partialConfirmList").getRowData(rows[i].id);
	  	postData["keys.material_partial_detail_key[" + i + "]"] = rowData["material_partial_detail_key"];
	  	postData["keys.recept_quantity[" + i + "]"] = rowData["recept_quantity"];
    }
    postData.finish = finish;

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doPartialUse',
		cache : false,
		data : postData,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : doStart_ajaxSuccess
	});
}

var doStart=function(){
	var data = {
		material_id : $("#scanner_inputer").val()
	}
	$("#scanner_inputer").attr("value", "");

	// 小修理等待提醒
	var $lightBox_overtime = $(".lightBox.overtime");
	if ($lightBox_overtime.length > 0) {
		var hitOutDate = false;
		var $tdWait = $lightBox_overtime.find("td[material_id="+ data.material_id +"]");
		if ($tdWait.length > 0) {
			if ($tdWait.next().next().hasClass("overtime")) {
				hitOutDate = true;
			}
		}
		if (!hitOutDate) {
			warningConfirm("如果有条件，请选择等待作业相关中小修理中超时的维修对象进行作业。<BR>点击取消则继续当前维修。"
			, function(){$("#scanner_inputer").focus();}
			, function(){doStartForward(data);}
			);
		} else {
			doStartForward(data);
		} 
	} else {
		doStartForward(data);
	}

};

var doStartForward=function(data){

	if ($("#devicearea").length) {
		// 周边设备匹配扫描
		data.scan_part = "1";
	}

	if ($("#sendbutton").length > 0) {
		try {
			$("#pauseo_material_id").val(data.material_id);
			if (typeof(checkProcess) === "function") checkProcess($("#skip_position").val());
		}catch(e) {
			infoPop("don't care");
		}
	}

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
		complete : function(xhrobj, textStatus){
			doStart_ajaxSuccess(xhrobj, textStatus, data);
		}
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
				showPartialRecept(resInfo, 1);
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

				$("#working_detail").hide();

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
				if ($('#partialconfirmarea:visible').length > 0) {
					partial_closer = false;
					$('#partialconfirmarea').dialog("close");
				}
				if ($("#comments_dialog:visible").length > 0) {
					$("#comments_dialog textarea").val("");
					$("#comments_dialog").dialog("close");
				}
				if ($("#comments_sidebar:visible").length > 0) {
					$("#comments_sidebar .comments_area").val("");
					$("#comments_sidebar").hide();
				}

				hasPcs && pcsO.clearCache();

				if (resInfo.procedure_step_count_message) {
					infoPop(resInfo.procedure_step_count_message, null, "作业步骤计数");
				}
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
		var $invalidInputs = $("#pcs_contents input:text.invalid");
		if ($invalidInputs.length > 0) {
			errorPop("存在不符合输入范围的输入项，请检查改正后再完成本工位作业。", $invalidInputs.eq(0));
		} else {
			errorPop("请填写完所有的工程检查票选项后，再完成本工位作业。");
		}
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
	if (!iminute && iminute != 0) return "-";
	var hours = parseInt(iminute / 60);
	var minutes = iminute % 60;

	return fillZero(hours, 2) + ":" + fillZero(minutes, 2);
}

var convertMinute =function(sminute) {
	var hours = sminute.replace(/(.*):(.*)/, "$1");
	var minutes = sminute.replace(/(.*):(.*)/, "$2");

	return hours * 60 + parseInt(minutes);
}

var showMaterial = function(material_id) {
	$process_dialog = $("#process_dialog");
	if ($process_dialog.length == 0) return;
	$process_dialog.hide();
	// 导入编辑画面
	$process_dialog.load("widget.do?method=materialDetail&material_id=" + material_id,
		function(responseText, textStatus, XMLHttpRequest) {
			$.ajax({
			data:{
				"id": material_id // , occur_times: occur_times
			},
			url : "material.do?method=getDetial",
			type : "post",
			complete : function(xhrobj, textStatus){
				var resInfo = null;
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					setLabelText(resInfo.materialForm, resInfo.materialPartialFormList, resInfo.processForm, resInfo.timesOptions, material_id);
					if (resInfo.caseId == 3) {
						case3();
					} else {
						case0();
					}
					
				} catch (e) {
					alert("name: " + e.name + " message: " + e.message + " lineNumber: "
							+ e.lineNumber + " fileName: " + e.fileName);
				};
				
				$process_dialog.dialog({
					title : "维修对象详细信息",
					width : 800,
					show : "blind",
					height : 'auto' ,
					resizable : false,
					modal : true,
					minHeight : 200,
					buttons : {
						"关闭": function(){
							$process_dialog.dialog('close');
						}
					}
				});
				$process_dialog.show();
			}
		});
	});
};

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
							'<div class="tube-liquid' + expeditedColor(waiting.expedited, waiting.today)  + '">'
								+ getLevel(waiting.level) + "<span>"
								+ (waiting.sorc_no == null ? "" : waiting.sorc_no + ' | ') + waiting.category_name + ' | ' + waiting.model_name + ' | ' + waiting.serial_no
								+ (waiting.shelf_name ? (' | 存放于：' + waiting.shelf_name) : '') + "</span>"
								+ getFlags(waiting.expedited, waiting.direct_flg, waiting.light_fix, waiting.reworked)
								+ getBlock(waiting.block_status)
								+ getLineMinutes(waiting.line_minutes) +
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
//		try {
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
    			if (typeof(resetSnoutRefers) === "function") resetSnoutRefers();
    		}
	};  
	// 连接上时走这个方法  
	position_ws.onopen = function() {     
		position_ws.send("entach:" + g_pos_id + "#"+$("#op_id").val());
	}; 
//	} catch(e) {
//	}
	}
};


var pxChange = function() {
	if ($("#material_details").is(":visible")) {
		return;
	}
	$.ajax({
		data: null,
		url : servicePath + "?method=pxChange",
		type : "post",
		cache : false,
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError
	})
}

var checkSnoutPartial = function(callback) {
	var material_id = $("#pauseo_material_id").val();
	var occur_times = 1;
	var data = {
		material_id: material_id,
		occur_times: occur_times
	};
	callback();
//	$.ajax({
//		beforeSend : ajaxRequestType,
//		async : true,
//		url : 'materialPartial.do?method=getSnouts',
//		cache : false,
//		data : data,
//		type : "post",
//		dataType : "json",
//		success : ajaxSuccessCheck,
//		error : ajaxError,
//		complete : function(xhrObj) {
//			edit_snout_Complete(xhrObj, material_id, occur_times, $("#material_details > table td:eq(1)").text(), callback);
//		}
//	});
}

var edit_snout_Complete = function(xhrObj, material_id, occur_times, sorc_no, callback) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrObj.responseText);
		if (resInfo.errors && resInfo.errors.length > 0) {
			if (resInfo.never_order) {
				warningConfirm("尚未做成零件订单，目前无法替代。");
			} else {
				// 这里是无需替代零件的情况下(error是无可替代零件) 
				callback();
			}
			return;
		}
		setInsteadList(resInfo.Snouts_list);
		$("#consumables_dialog").dialog({
			title : "预制零件替代",
			width : 800,
			show : "blind",
			height : 'auto' ,
			resizable : false,
			modal : true,
			minHeight : 200,
			buttons : {
			"确定":function(){
					$("#consumables_dialog").dialog('close');

					var postData = {material_id:material_id, occur_times:occur_times,
					sorc_no:sorc_no};
					var iii = 0;
					$("#consumables_list").find("tr").each(function(idx, ele) {
						
						$input = $(ele).find("input[type=number]");
						if ($input && $input.val()) {
							var ival = $input.val();
							var ilimit = $input.attr("limit");
							var iTotal = $input.attr("total");
							if (ival.match(/^0*$/) == null && ival.match(/^[0-9]*$/) != null) {
								if (ival > ilimit) $input.val(ilimit);
								postData["exchange.quantity[" + iii + "]"] = $input.val();
								postData["exchange.total[" + iii + "]"] = iTotal;
								postData["exchange.material_partial_detail_key[" + iii + "]"] = $(ele).find("td[aria\\-describedby=consumables_list_material_partial_detail_key]").text();
								iii ++;
							}
						};
					});
					if (postData["exchange.material_partial_detail_key[0]"] != null) {
						$.ajax({
							beforeSend : ajaxRequestType,
							async : false,
							url : 'materialPartial.do?method=doUpdateSnouts',
							cache : false,
							data : postData,
							type : "post",
							dataType : "json",
							success : ajaxSuccessCheck,
							error : ajaxError,
							complete : function(){
								callback();
							}
						});
					} else {
						callback();
					}
				},
				"取消": function(){
					$("#consumables_dialog").dialog('close');
				}
			}
		});
		
		$("#consumables_dialog").show();
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName + " 1606");
	};
}

/*jqgrid表格*/
function setInsteadList(consumables_list){
	if ($("#gbox_consumables_list").length > 0) {
		$("#consumables_list").jqGrid().clearGridData();
		$("#consumables_list").jqGrid('setGridParam',{data:consumables_list}).trigger("reloadGrid", [{current:false}]);
	} else {
		$("#consumables_list").jqGrid({
			data:consumables_list,
			height: 461,
			width: 768,
			rowheight: 23,
			datatype: "local",
			colNames:['','零件编号','零件名称','可签收数量','工位','目前签收状态','签收日期','消耗品'],
			colModel:[
				{name:'material_partial_detail_key',index:'material_partial_detail_key', hidden:true},		   
				{name : 'code',index : 'code',width : 60,align : 'left'},
				{name : 'partial_name',index : 'partial_name',width : 300,align : 'left'},
				{name : 'waiting_quantity',index : 'waiting_quantity',width : 100,align : 'right',formatter:function(r,i,rowData){
					return '<input type="number" value="0" limit="'+r+'" total="'+rowData.quantity+'" > / ' + r;
				}},
				{name :'process_code',index:'process_code',width:60,align:'center' },
				{name :'status',index:'status',width:60,align:'center', formatter:'select', editoptions:{value:"1:未发放;2:已签收无BO;3:BO中;4:BO解决;5:消耗品签收"} },
				{name :'recent_receive_time',index:'recent_receive_time',width:60,align:'center',
						formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'m-d'}},
				{name:'append',index:'append', hidden:true}		   
			 ],
			rowNum: 100,
			toppager: false,
			pager: "#consumables_listpager",
			viewrecords: true,
			multiselect: true,
			hidegrid : false,
			gridview: true,
			pagerpos: 'right',
			pgbuttons: true,			
			pginput: false,
			recordpos: 'left',
			viewsortcols : [true,'vertical',true],
			onSelectRow : changevalue,
			onSelectAll : changevalue,
			gridComplete:function(){
				changevalue([] ,false);
			}
		});
	}
};

var changevalue = function(rowid,status,e){
	if (rowid instanceof Array) {
		// 全选
		var $inum = $("#consumables_list").find("input[type=number]");
		$inum.each(function(){
			if (status) {
				$(this).val($(this).attr("limit"));
			} else {
				$(this).val(0);
			}
		});
	} else {
		var $inum = $("#consumables_list").find("tr#"+rowid).find("input[type=number]");
		if (status) {
			$inum.val($inum.attr("limit"));
		} else {
			$inum.val(0);
		}
	}
}

var secondaryConfirm = function(postData, mform) {
	var $secondaryDialog = $("#secondary_scanner");
	if ($secondaryDialog.length == 0) {
		$("body").append("<div id='secondary_scanner'>" +
				"<div class='ui-state-default' style='padding:.5em;'>请再次扫描维修品实物附带小票上的条码确认。</div>" +
				"<div class='ui-widget-content' style='font-size:14px;padding-left: .5em;'></div>" +
				"<input type='text' title='扫描前请点入此处' class='scanner_inputer dwidth-half'></input><div style='text-align: center;'><img src='images/barcode.png' style='margin: auto; width: 150px; padding-top: 4px;'></div></div>");
		$secondaryDialog = $("#secondary_scanner");
	}

	var materialMessage = "修理单号：" + mform.sorc_no + "　型号：" + mform.model_name + "　机身号：" + mform.serial_no;
	if (mform.wip_location) {
		materialMessage += "　库位：" + mform.wip_location;
	}

	$secondaryDialog.children("div").eq(1).html(materialMessage);

	var $scanner_inputer = $secondaryDialog.find(".scanner_inputer");
	$scanner_inputer.val("");

	// 输入框触发，配合浏览器
	$scanner_inputer.unbind("keypress"); $scanner_inputer.unbind("keyup");
	$scanner_inputer.keypress(function(){
		if (this.value.length === 11) {
			if (this.value == mform.material_id) {
				secondaryScanPost(postData);
			} else {
				setTimeout(function(){
					errorPop("两次扫描的维修品编号不一致，请确认后重试。");
				}, 300);
			}
			$secondaryDialog.dialog("close");
		}
	});
	$scanner_inputer.keyup(function(){
		if (this.value.length >= 11) {
			if (this.value == mform.material_id) {
				secondaryScanPost(postData);
			} else {
				setTimeout(function(){
					errorPop("两次扫描的维修品编号不一致，请确认后重试。");
				}, 300);
			}
			$secondaryDialog.dialog("close");
		}
	});

	$secondaryDialog.dialog({
		title : "请操作第二次扫描",
		dialogClass : 'ui-warn-dialog',
		width : 640,
		height : 320,
		resizable : false,
		modal : true,
		minHeight : 220,
		open: function() {
//			$secondaryDialog.parent().find(".ui-dialog-titlebar-close").hide();
		}
	});
}

var secondaryScanPost = function(postData){
	postData.scan_part = 2;
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doscan',
		cache : false,
		data : postData,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus){
			doStart_ajaxSuccess(xhrobj, textStatus, postData);
		}
	});
}