var treat_nogood = function() {
	$("#nogood_treat").hide();
	// 导入不良处置画面
	$("#nogood_treat").load("widget.do?method=nogoodedit", function(responseText, textStatus, XMLHttpRequest) {
		var selectedId = $("#list").getGridParam("selrow");
		var rowData = $("#list").getRowData(selectedId);
		var data = {
			material_id : rowData["material_id"]
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
					getWarningComplete(xhrobj, rowData);
				} catch (e) {
					alert("name: " + e.name + " message: " + e.message + " lineNumber: "
							+ e.lineNumber + " fileName: " + e.fileName);
				};
			}
		});
	});
};

var redo_ccd = function() {
//	$("#nogood_treat").hide();
//	// 导入不良处置画面
//	$("#nogood_treat").load("widget.do?method=nogoodedit", function(responseText, textStatus, XMLHttpRequest) {
		var selectedId = $("#list").getGridParam("selrow");
		var rowData = $("#list").getRowData(selectedId);
		var data = {
			material_id : rowData["material_id"]
		};
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : 'material.do?method=doreccd',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj, textStatus) {
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					if (resInfo.errors.length == 0) {
						infoPop("已经可以开始CCD盖玻璃更换作业！");
					} else {
						treatBackMessages(null, resInfo.errors);
					}
				} catch (e) {
					alert("name: " + e.name + " message: " + e.message + " lineNumber: "
							+ e.lineNumber + " fileName: " + e.fileName);
				};
			}
		});
//	});
};

var show_daily_report = function() {
	
	var $jdialog = $("#daily_report");
	$jdialog.hide();

	// 导入每日KPI报告
	$jdialog.load("scheduleProcessing.do?method=daily_report", function(responseText, textStatus, XMLHttpRequest) {
		$jdialog.dialog({
			// position : [ 800, 20 ],
			title : "每日KPI信息",
			width : 780,
			show : "blind",
			height : 'auto', //450,
			resizable : false,
			modal : true,
			minHeight : 200,
			close : function() {
				$jdialog.html("");
			},
			buttons : {
				"修改" : function() {
					var postData = {};

					var $changed_object = $("#report_of_week input[changed=true]");
					var iV = 0;
					$changed_object.each(
					function(idx,ele) {
						var p_date =
							new Date(Date.parse($("#weekstart").val()) + ($(ele).parent().attr("weekday_index") - 1) * 86400000);
						var s_date = (p_date.getFullYear() + "/" + fillZero(p_date.getMonth()+1, 2) + "/" + fillZero(p_date.getDate(), 2));
						postData["update.count_date[" + iV + "]"] = s_date;
						postData["update.target[" + iV + "]"] = $(ele).parent().parent().attr("for");
						postData["update.val[" + iV + "]"] = ele.value;
						iV++;
					});
					$changed_object = $("#report_of_week textarea[changed=true]");
					$changed_object.each(
					function(idx,ele) {
						var p_date =
							new Date(Date.parse($("#weekstart").val()) + ($(ele).parent().attr("weekday_index") - 1) * 86400000);
						var s_date = (p_date.getFullYear() + "/" + fillZero(p_date.getMonth()+1, 2) + "/" + fillZero(p_date.getDate(), 2));
						postData["update.count_date[" + iV + "]"] = s_date;
						postData["update.target[" + iV + "]"] = "comment";
						postData["update.val[" + iV + "]"] = ele.value;
						iV++;
					});

					postData.weekstart = $("#weekstart").val();
					// postData.comment = $("#for_complete").val();

					// Ajax提交
					$.ajax({
						beforeSend : ajaxRequestType,
						async : false,
						url : 'scheduleProcessing.do?method=doUpdateDailyKpi',
						cache : false,
						data : postData,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : function(xhrobj, textStatus) {
							$jdialog.dialog("close");
						}
					});
				}, "关闭" : function() {
					$jdialog.dialog("close");
				}
			}
		});
	});
};

var clean_position = function() {
	$("#nogood_treat").hide();
	var selectedId = $("#list").getGridParam("selrow");
	var rowData = $("#list").getRowData(selectedId);
	var data = {
		material_id : rowData["material_id"],
		model_name : rowData["model_name"],
		serial_no : rowData["serial_no"]
	};
	// 导入不良处置画面
	$("#nogood_treat").load("widget.do?method=cleanPosition&material_id=" + rowData["material_id"], data, function(responseText, textStatus, XMLHttpRequest) {
		$("#clean_target").select2Buttons();
		$("#nogood_treat").dialog({
			// position : [ 800, 20 ],
			title : "不良信息及处置",
			width : 808,
			show : "blind",
			height : 'auto', //450,
			resizable : false,
			modal : true,
			minHeight : 200,
			close : function() {
				$("#nogood_treat").html("");
			},
			buttons : {
				"确认" : function() {
					// TODO check
					data.position_id = $("#clean_target").val();
					data.comment = $("#clean_comment").val();
					// Ajax提交
					$.ajax({
						beforeSend : ajaxRequestType,
						async : false,
						url : 'pcsFixOrder.do?method=doPcCreate',
						cache : false,
						data : data,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : function(xhrobj, textStatus) {
							try {
								// 以Object形式读取JSON
								eval('resInfo =' + xhrobj.responseText);
								$("#nogood_treat").dialog("close");
							} catch (e) {
								alert("name: " + e.name + " message: " + e.message + " lineNumber: "
										+ e.lineNumber + " fileName: " + e.fileName);
							};
						}
					});
				}
			}
		});
	});
};

var show_capacity_setting = function(){
	var $jdialog = $("#capacity_setting");
	$jdialog.hide();
	
	$jdialog.load("scheduleProcessing.do?method=capacity_setting",function(responseText,textStatus,XMLHttpRequest){
		$jdialog.dialog({
			title : "产能设定",
			width : 600,
			show  : "blind",
			height: 'auto',
			resizable : false,//不可改变弹出框大小
			modal : true,
			minHeight:200,
			close :function(){
				$jdialog.html("");
			},
			buttons:{
				"修改" : function() {
					
					var postData = {};
					
					var $changed_object = $("#capacity_of_upper_limit input[changed=true]");

					$changed_object.each(function(idx,ele){						
						postData["update.line_id["+idx+"]"] = $(ele).parent().parent().parent().find(".line_id").val();
						postData["update.category_id["+idx+"]"]= $(ele).parent().parent().find(".category_id").val();
						postData["update.upper_limit["+idx+"]"] = ele.value;
						postData["update.section_id["+idx+"]"] = $(ele).parent().attr("section_id");
						
					});

					// Ajax提交
					$.ajax({
						beforeSend : ajaxRequestType,
						async : false,
						url : 'scheduleProcessing.do?method=doUpdateUpperLimit',
						cache : false,
						data : postData,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : function(xhrobj, textStatus) {
							$jdialog.dialog("close");
						}
					});
				}, "关闭" : function() {
					$jdialog.dialog("close");
				}
			}
		});
	});	
};

var getDailyPlans = function(){
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : 'scheduleProcessing.do?method=getManufactPlans',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrObj, textStatus) {
			var resInfo = $.parseJSON(xhrObj.responseText);
			if (resInfo.errors.length == 0) {
				showDailyPlans(resInfo.dailyPlans, resInfo.modelOptions);
			}
		}
	});
}

var showDailyPlans = function(dailyPlans, modelOptions){
	var dataLength = dailyPlans.length;
	var hitdateIdx = 0;

	var $jdialog = $("#capacity_setting");
	$jdialog.hide();

	var ressignArrows = function($target) {
		$target.find("input:button[value='↑']").css("visibility", "visible")
			.eq(0).css("visibility", "hidden");
		$target.find("input:button[value='↓']").css("visibility", "visible")
			.eq(-1).css("visibility", "hidden");
	}

	var $setting = $("#capacity_model_setting");
	if (!$setting.html()) {
		$setting.html("<select id='capacity_model_selector'>" + modelOptions + "</select>")
			.find("select")
			.change(function(){

				if(!$setting.is(":visible")) {
					return;
				}

				var $addTarget = $("#manufact_plans .dp_pofd.setting");
				if ($addTarget.length > 0) {
					var $selector = $(this);

					var selModelId = $selector.val();
					if ($addTarget.find("model[model_id='" + selModelId + "']").length > 0) {
						errorPop("当天已经设置过此制品型号的计划。");
					} else {
						var selModelName = $selector.find("option:selected").text();
						var planText = "<div><input type='button' value='-'><input type='button' value='↑'><input type='button' value='↓'><br>" +
							"<model model_id='" + selModelId + "'>" + selModelName + "</model><input type='number' value='0'/><br></div>";
						$addTarget.children("plan").children("div").eq(-1).before(planText);
						$addTarget.children("plan").addClass("changed");
						ressignArrows($addTarget);
					}
				}

				$setting.hide();
			})
			.select2Buttons();
	}

	var $settingHtml = $("<div id='manufact_plans'/>");
	var today = new Date();
	for (var i = 0; i < 14; i++) {
		var aDay = new Date(today.getTime() + i * 24 * 60 * 60000);
		var dateString = aDay.dateFormat('Y-m-d');
		var wdString = aDay.dateFormat('D');
		var $ofDate = $("<div class='dp_pofd' dt='" + dateString + "'><date>" + dateString + " " + wdString + "</date><plan></plan></div>");
		var bWeekend = (wdString==='Sat' || wdString==='Sun');
		if (i == 0) {
			$ofDate.find("date").addClass("today");
		} else 	if (bWeekend) {
			$ofDate.find("date").addClass("holiday");
		}

		var planText = "";
		if (dataLength > 0) {
			for (var ii = hitdateIdx; ii < dataLength; ii++) {
				var plan = dailyPlans[ii];
				var planDateString = new Date(plan.plan_date).dateFormat('Y-m-d');
				if (planDateString === dateString) {
					hitdateIdx = ii;
					planText += "<div><input type='button' value='-'><input type='button' value='↑'><input type='button' value='↓'><br>" +
							"<model model_id='" + plan.model_id + "'>" + plan.model_name + "</model><input type='number' value='" + plan.quantity + "'/><br></div>";
				}
			}
		}

		planText += "<div><input type='button' value='+'></div>";
		var $plan = $ofDate.find("plan");
		$plan.html(planText);

		ressignArrows($plan);

		$settingHtml.append($ofDate);
	}

	$settingHtml.find("input:button[value='+']").click(function(evt){
		$("#capacity_model_setting").css("top", 0).hide()
			.find("select").val("").trigger("change")
			.end()
			.show();

		$("#manufact_plans .dp_pofd.setting").removeClass("setting");
		$(this).closest(".dp_pofd").addClass("setting");

		var px = evt.pageX - ($("#capacity_model_setting").width() / 2) + "px";
		var py = evt.pageY + "px";

		$("#capacity_model_setting").css({"top": py, "left" : px});
	});

	$settingHtml.on("click", "input:button[value='-']", function() {
		
		var $divTarget = $(this).parent();
		var $planTarget = $divTarget.parent();
		$divTarget.remove();

		$planTarget.addClass("changed");
		ressignArrows($planTarget);
	});

	$settingHtml.on("click", "input:button[value='↑']", function() {
		
		var $divTarget = $(this).parent();
		var $planTarget = $divTarget.parent();
		$divTarget.prev().before($divTarget.detach());

		$planTarget.addClass("changed");
		ressignArrows($planTarget);
	});

	$settingHtml.on("click", "input:button[value='↓']", function() {
		
		var $divTarget = $(this).parent();
		var $planTarget = $divTarget.parent();
		$divTarget.next().after($divTarget.detach());

		$planTarget.addClass("changed");
		ressignArrows($planTarget);
	});

	$settingHtml.on("change", "input[type='number']", function() {

		var $planTarget = $(this).closest("plan");
		$planTarget.addClass("changed");

	});

	$jdialog.html($settingHtml);

	$jdialog.dialog({
		title : "每日计划设定",
		width : 880,
		show  : "blind",
		height: 'auto',
		resizable : false,//不可改变弹出框大小
		modal : true,
		minHeight:200,
		close :function(){
			$jdialog.html("");
			$("#capacity_model_setting").hide();
		},
		buttons:{
			"提交" : function() {
				var $changedPlans = $jdialog.find("plan.changed");
				if ($changedPlans.length > 0) {
					var iPl = 0, iDt = 0;
					var postData = {};

					$changedPlans.each(function(){
						var plan_date = $(this).parent().attr("dt");
						postData["changed[" + iDt + "].plan_date"] = plan_date;
						iDt++;

						$(this).children("div").each(function(idx, ele){
							var $ele = $(ele);
							postData["plan[" + iPl + "].model_id"] = $ele.children("model").attr("model_id");
							if (!postData["plan[" + iPl + "].model_id"]) {
								return;
							}
							postData["plan[" + iPl + "].quantity"] = $ele.children("input[type='number']").val();
							postData["plan[" + iPl + "].seq"] = idx;
							postData["plan[" + iPl + "].plan_date"] = plan_date;
							iPl++;
						});
					});

					// Ajax提交
					$.ajax({
						beforeSend : ajaxRequestType,
						async : false,
						url : 'scheduleProcessing.do?method=doUpdateManufactPlans',
						cache : false,
						data : postData,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : function(xhrObj, textStatus) {
							var resInfo = $.parseJSON(xhrObj.responseText);
							if (resInfo.errors.length == 0) {
								infoPop("计划设定完成。");
								$jdialog.dialog("close");
							} else {
								treatBackMessages(null, resInfo.errors);
							}
						}
					});
				}

			},
			"关闭" : function() {
				$jdialog.dialog("close");
			}
		}
	})
}

$(document).ready(function() {
	$("#nogoodbutton").disable();
	$("#nogoodbutton").click(function() {
		treat_nogood();
	});
	$("#reccdbutton").disable();
	$("#reccdbutton").click(function() {
		redo_ccd();
	});
	$("#cleanbutton").disable();
	$("#cleanbutton").click(function() {
		clean_position();
	});
	
	//产能设定
	$("#capacity_setting_button").click(function(){
		show_capacity_setting();
	});
	
	$("#daily_report_button").click(function() {
		show_daily_report();
	});

	$("#capacitybutton").click(function(){
		getDailyPlans();
	})
});
