/** 一览数据对象 */
var listdata = {};
var quotation_listdata = {};
/** 服务器处理路径 */
var servicePath = "quotation.do";
var hasPcs = null;

var wip_location = "";

// 取到的标准作业时间
var leagal_overline;

var lOptions = {};
var oOptions = {};
var dOptions = {};
var sOptions = {};
var tOptions = {};
var bOptions = {};
var pauseOptions = "";
var stepOptions = "";
var breakOptions = "";

/** 医院autocomplete **/
var customers = {};

var showWipEmpty=function() {
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : "wip.do" + '?method=getwipempty',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					pop_wip(doFinish, resInfo);
				}
			} catch (e) {
				alert("name: " + e.name + " message: " + e.message + " lineNumber: "
						+ e.lineNumber + " fileName: " + e.fileName);
			};
		}
	});
}

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
							var resInfo = $.parseJSON(xhr.responseText)
							treatPause(resInfo);
						}
					});
				}
			}, "关闭" : function(){ $(this).dialog("close"); }
		}
	});
}

var pop_wip = function(call_back, resInfo){
	var quotation_pop = $("#quotation_pop");
	quotation_pop.hide();
	quotation_pop.load("widgets/qf/wip_map.jsp", function(responseText, textStatus, XMLHttpRequest) {
		 //新增

		quotation_pop.dialog({
			position : [ 800, 20 ],
			title : "WIP 入库选择",
			width : 1000,
			show: "blind",
			height : 640,// 'auto' ,
			resizable : false,
			modal : true,
			minHeight : 200,
			buttons : {}
		});

		quotation_pop.find("td").addClass("wip-empty");
		for (var iheap in resInfo.heaps) {
			quotation_pop.find("td[wipid="+resInfo.heaps[iheap]+"]").removeClass("wip-empty").addClass("ui-storage-highlight wip-heaped");
		}

		//$("#quotation_pop").css("cursor", "pointer");
		quotation_pop.find(".ui-widget-content").click(function(e){
			if ("TD" == e.target.tagName) {
				if (!$(e.target).hasClass("wip-heaped")) {
					wip_location = $(e.target).attr("wipid");
					call_back();
					quotation_pop.dialog("close");
				}
			}
		});
		
		quotation_pop.find("div.cage").each(function(index,ele){
			var $tds = $(ele).find("td.ui-storage-highlight.wip-heaped");
			$(ele).find(".ui-widget-header").append("	:	" + $tds.length);
		});

		quotation_pop.show();

		if ($("#devicearea").length > 0) {
			setTimeout(function(){quotation_pop[0].scrollTop = 300}, 600);
		}
	});
}

/** 中断信息弹出框 */
var makeBreakDialog = function(jBreakDialog) {
	var b_request = {
		ocm : $("#edit_ocm").val(),
		customer_name : $("#edit_customer_name").val(),
		ocm_rank : $("#edit_ocm_rank").val(),
		ocm_deliver_date : $("#edit_ocm_deliver_date").val(),
		osh_deliver_date : $("#edit_osh_deliver_date").val(),
		agreed_date : $("#edit_agreed_date").val(),
		level : $("#edit_level").val(),
		fix_type : $("#edit_fix_type").val(),
		service_repair_flg : $("#edit_service_repair_flg").val(),
		comment : $("#edit_comment").val(),
		selectable : ($("#partake").attr("checked") ? "1" : "0"),
		bound_out_ocm : $("#edit_bound_out_ocm option:selected").val(),
		area : $("#edit_area option:selected").val()
	}

	var submitBreak = function(){
		b_request.wip_location = wip_location;

		var invalid = false;
		if (hasPcs) {
			invalid = pcsO.valuePcs(b_request, true);
		}
		if (!$("#light_pat_button").is(":visible")) {
			b_request.pat_id = "00000000000";
		}
		if (invalid) {
			var $invalidInputs = $("#pcs_contents input:text.invalid");
			if ($invalidInputs.length > 0) {
				jBreakDialog.dialog("close");
				errorPop("存在不符合输入范围的输入项，请检查改正或暂时删除后再实行中断。", $invalidInputs.eq(0));
				return;
			}
		}

		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : servicePath + '?method=dobreak',
			cache : false,
			data : b_request,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj, textStatus) {
				doFinish_ajaxSuccess(xhrobj);
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					if (resInfo.errors.length === 0) {
						jBreakDialog.dialog("close");
					}
				} catch (e) {
					alert("name: " + e.name + " message: " + e.message + " lineNumber: "
							+ e.lineNumber + " fileName: " + e.fileName);
				};
			}
		});
	};

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
					b_request.reason = $("#break_reason").val();
					b_request.comments = $("#edit_comments").val();

					if (parseInt($("#break_reason").val()) > 70) {
						if ($("#break_reason").val() == 73) {
							if ($("#wipconfirmbutton").length == 0) {
								submitBreak();
							} else {
								if($("#edit_wip_location").val() != null && $("#edit_wip_location").val() != "") {
									errorPop("已经放入WIP"+$("#edit_wip_location").val()+"了");
									$(this).dialog("close");
									return;
								}
								$.ajax({
									beforeSend : ajaxRequestType,
									async : true,
									url : "wip.do" + '?method=getwipempty',
									cache : false,
									data : null,
									type : "post",
									dataType : "json",
									success : ajaxSuccessCheck,
									error : ajaxError,
									complete : function(xhrobj) {
										var resInfo = null;
										try {
											// 以Object形式读取JSON
											eval('resInfo =' + xhrobj.responseText);
											if (resInfo.errors.length > 0) {
												// 共通出错信息框
												treatBackMessages(null, resInfo.errors);
											} else {
												pop_wip(submitBreak, resInfo);
											}
										} catch (e) {
											alert("name: " + e.name + " message: " + e.message + " lineNumber: "
													+ e.lineNumber + " fileName: " + e.fileName);
										};
									}
								});
							}
						} else {
							submitBreak();
						}
					} else {
						submitBreak();
					}
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

/** 正常中断信息 */
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

var paused_list = function(paused) {
	// 建立等待区一览
	var reason = "";
	var waiting_html = "";
	var subCount = 0;
	for (var iwaiting = 0; iwaiting < paused.length; iwaiting++) {
		var waiting = paused[iwaiting];
		if (reason != waiting.operate_result) {
			reason = waiting.operate_result;
			waiting_html = waiting_html.replace('#count#', subCount);
			subCount = 0;
			waiting_html += '<div class="ui-state-default w_group" style="margin-top: 12px; margin-bottom: 8px; padding: 2px;">'+ reason +' #count#件:</div>'
		}
		waiting_html += '<div class="waiting tube" id="w_' + waiting.material_id + '">' +
							'<div class="tube-liquid  ' +
							(waiting.agreed_date ? 'tube-green' : 'tube-gray') +
							'">' +
								(waiting.sorc_no == null ? "" : waiting.sorc_no + ' | ') + waiting.model_name + ' | ' + waiting.serial_no +
								(waiting.wip_location ? (' | 存放于：' + waiting.wip_location) : '') + 
								getFlags(waiting.quotation_first, waiting.direct_flg, waiting.light_fix, waiting.service_repair_flg) +
							'</div>' +
						'</div>';
		subCount++;
	}
	waiting_html = waiting_html.replace('#count#', subCount);

	$("#wtg_list").html(waiting_html);
}

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

var doInit_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
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

			lOptions = resInfo.lOptions;
			oOptions = resInfo.oOptions;
			dOptions = resInfo.dOptions;
			sOptions = resInfo.sOptions;
			tOptions = resInfo.tOptions;
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

			load_list(resInfo.waitings);
			paused_list(resInfo.paused);
			acceptted_list(resInfo.finished);

// 			$("#hide_material_id").val(resInfo.mform.material_id);
			// 存在进行中作业的时候
			if(resInfo.workstauts == 1 || resInfo.workstauts == 4) {
				getMaterialInfo(resInfo);
				treatStart(resInfo);

				hasPcs && pcsO.loadCache();
			} else if (resInfo.workstauts == 2 || resInfo.workstauts == 5) {
				getMaterialInfo(resInfo);
				treatPause(resInfo);
				hasPcs && pcsO.loadCache();
			}

			// autocomplete
			if (resInfo.customers) {
				customers = resInfo.customers;
				$("#edit_customer_name").autocomplete({
					source : customers,
					minLength :2,
					delay : 100
				});
			}
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

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

var treatPause = function(resInfo) {
	getPeripharalInfo(resInfo);
	$("#editform table tbody").find("input,select,textarea").disable();
	$("#continuebutton").show();
	$("#pausebutton").hide();
	$("#breakbutton, #stepbutton").disable();
	$("#confirmbutton, #wipconfirmbutton").disable();
}

var treatStart = function(resInfo) {
	getPeripharalInfo(resInfo);
	$("#editform table tbody").find("input,select,textarea").enable();
	$("#continuebutton").hide();
	$("#pausebutton").show();
	$("#breakbutton, #stepbutton").enable();

	if (resInfo.workstauts == "4") {
		$("#confirmbutton, #wipconfirmbutton").disable();
	} else {
		$("#confirmbutton, #wipconfirmbutton").enable();
	}

	if (resInfo.quality_tip) {
		showTips(resInfo.quality_tip);
	}
}

var getMaterialInfo = function(resInfo) {
	$("#hide_material_id").val(resInfo.mform.material_id);
	$("#scanner_inputer").attr("value", "");
	$("#scanner_container").hide();
	$("#material_details").show();

	if (!resInfo.finish_check) {
		$("#material_details td:eq(1)").text(resInfo.mform.model_name).attr("model_id", resInfo.mform.model_id);
		$("#material_details td:eq(3)").text(resInfo.mform.serial_no);
		$("#material_details td:eq(5)").text(resInfo.mform.sorc_no);
		$("#edit_ocm").val("").val(resInfo.mform.ocm).trigger("change");

		$("#edit_ocm_rank").val("").val(resInfo.mform.ocm_rank).trigger("change");
		$("#edit_ocm_deliver_date").val(resInfo.mform.ocm_deliver_date);
		$("#edit_osh_deliver_date").val(resInfo.mform.osh_deliver_date);
		$("#edit_agreed_date").val(resInfo.mform.agreed_date);
		$("#edit_level").val("").val(resInfo.mform.level).trigger("change");
		$("#edit_customer_name").val(resInfo.mform.customer_name);
		$("#edit_fix_type").val("").val(resInfo.mform.fix_type).trigger("change");
		$("#edit_service_repair_flg").val("").val(resInfo.mform.service_repair_flg).trigger("change");
		$("#edit_comment").val(resInfo.mform.comment);
		if (resInfo.mform.scheduled_manager_comment) {
			$("#edit_comment_other").show().val(resInfo.mform.scheduled_manager_comment);
		} else {
			$("#edit_comment_other").hide().val("");
		}
		$("#edit_wip_location").val(resInfo.mform.wip_location);

		$("#edit_bound_out_ocm").val("").val(resInfo.mform.bound_out_ocm).trigger("change");
		$("#edit_area").val("").val(resInfo.mform.area).trigger("change");

		wip_location = resInfo.mform.wip_location;

		if (resInfo.mform.direct_flg != 1) {
			$("#edit_direct_flg").text("").removeClass("fit2rapid");
		} else {
			$("#edit_direct_flg").text("直送").addClass("fit2rapid");
		}

		leagal_overline = resInfo.leagal_overline;

		if ($("#wipconfirmbutton").length > 0) {
			if (resInfo.mform.wip_location != null) {
				$("#wipconfirmbutton").val("放回WIP");
			} else {
				$("#wipconfirmbutton").val("放入WIP");
			}
		}

		if (resInfo.qa_rank || resInfo.qa_service_free) {
			$("#editform .qa_info").show();
			$("#edit_qa_level").text(resInfo.qa_rank);
			$("#edit_service_free").text(resInfo.qa_service_free);
		} else {
			$("#editform .qa_info").hide();
		}

	}

	if (resInfo.workstauts == 4 || resInfo.workstauts == 5) {
		$("#confirmbutton, #wipconfirmbutton").disable();
	} else {
		$("#confirmbutton, #wipconfirmbutton").enable();
	}

}
var getPeripharalInfo = function(resInfo) {
	if (resInfo.peripheralData && resInfo.peripheralData.length > 0) {
		showPeripheral(resInfo);
	}

	if (resInfo.workstauts == 1 || resInfo.workstauts == 2) {
		$("#device_details table tbody").find(".manageCode").disable();
		$("#device_details table tbody").find("input[type=button]").disable();
		$("#finishcheckbutton").disable();
	} else {
		$("#device_details table tbody").find(".manageCode").enable();
		$("#device_details table tbody").find(".manageCode").trigger("change");
	}

	if (resInfo.workstauts == 4 || resInfo.workstauts == 5) {
		hasPcs && pcsO.clear();
	} else {
		// 工程检查票
		if (resInfo.pcses && resInfo.pcses.length > 0 && hasPcs) {
			pcsO.generate(resInfo.pcses, true, false, resInfo.pcs_limits);
		}
	};
}
var doStart_ajaxSuccess=function(xhrobj){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			getMaterialInfo(resInfo);
			treatStart(resInfo);
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var doStart=function(){
	wip_location = "";

	var data = {
		material_id : $("#scanner_inputer").val()
	}

	$("#scanner_inputer").attr("value", "");

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

/** 暂停重开 */
var endPause = function() {
	// 重新读入刚才暂停的维修对象
	var data = {
		material_id : $("#hide_material_id").val()
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

var doFinish_ajaxSuccess=function(xhrobj){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			$("#scanner_inputer").attr("value", "");
			$("#material_details").hide();
			$("#scanner_container").show();
			$("#devicearea").hide();
			$("#manualdetailarea").hide();

			load_list(resInfo.waitings);
			paused_list(resInfo.paused);
			acceptted_list(resInfo.finished);

			if ($("#break_dialog").length > 0 && $("#break_dialog").html()) {
				//$("#pauseo_edit").hide;
				//$("#pauseo_edit").hide;
				$("#break_dialog").dialog("close");
			}

			hasPcs && pcsO.clearCache();
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var doFinish=function(){
	$("#editform").validate({
		rules : {
			sorc_no : {
				required : true
			}
		}
	});

	if($("#editform").valid()) {

		var data = {
			ocm : $("#edit_ocm").val(),
			customer_name : $("#edit_customer_name").val(),
			ocm_rank : $("#edit_ocm_rank").val(),
			ocm_deliver_date : $("#edit_ocm_deliver_date").val(),
			osh_deliver_date : $("#edit_osh_deliver_date").val(),
			agreed_date : $("#edit_agreed_date").val(),
			level : $("#edit_level").val(),
			fix_type : $("#edit_fix_type").val(),
			service_repair_flg : $("#edit_service_repair_flg").val(),
			wip_location : wip_location,
			comment : $("#edit_comment").val(),
//			selectable : ($("#partake").attr("checked") ? "1" : "0"),
			material_id :$("#hide_material_id").val(),
			bound_out_ocm : $("#edit_bound_out_ocm option:selected").val(),
			area : $("#edit_area option:selected").val()
		}

		if (!$("#light_pat_button").is(":visible")) {
			data.pat_id = "00000000000";
		}

		if (hasPcs) {
			var empty = pcsO.valuePcs(data);

			if (empty) {
				var $invalidInputs = $("#pcs_contents input:text.invalid");
				if ($invalidInputs.length > 0) {
					errorPop("存在不符合输入范围的输入项，请检查改正后再完成本工位作业。", $invalidInputs.eq(0));
				} else {
					errorPop("请填写完所有的工程检查票选项后，再完成本工位作业。");
				}
			}
		}

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

function acceptted_list(quotation_listdata){
	if ($("#gbox_exd_list").length > 0) {
		$("#exd_list").jqGrid().clearGridData();
		$("#exd_list").jqGrid('setGridParam',{data:quotation_listdata}).trigger("reloadGrid", [{current:false}]);
	} else {
		$("#exd_list").jqGrid({
			data:quotation_listdata,
			//height: 461,
			width: 1248,
			rowheight: 23,
			datatype: "local",
			colNames:['material_id','受理时间','报价时间', '修理单号', '型号 ID', '型号' , '机身号','RC','同意日期', '等级', '备注', '结果', 'ts'],
			colModel:[
				{name:'material_id',index:'material_id', hidden:true},
				{name:'reception_time',index:'reception_time', width:70, align:'center',
					sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'm-d'}},
				{name:'quotation_time',index:'quotation_time', width:70, align:'center',
					sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'm-d H:i'}},
				{name:'sorc_no',index:'sorc_no', width:105},
				{name:'model_id',index:'model_id', hidden:true},
				{name:'model_name',index:'model_id', width:125},
				{name:'serial_no',index:'serial_no', width:50, align:'center'},
				{name:'ocm',index:'ocm', width:65, formatter: 'select', editoptions:{value: oOptions}},
				{name:'agreed_date',index:'agreed_date', width:50, align:'center',
					sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d', newformat: 'm-d'}},
				{name:'level',index:'level', width:35, align:'center', formatter: 'select', editoptions:{value: lOptions}},
				{name:'fix_type',index:'fix_type', width:100, formatter : function(value, options, rData){
					return rData['remark'];
				}},
				{name:'wip_location',index:'wip_location', width:60},
				{name:'quotation_first',index:'quotation_first', hidden:true}
			],
			rowNum: 20,
			pager: "#exd_listpager",
			viewrecords: true,
			caption: "报价成果一览",
			gridview: true, // Speed up
			onSelectRow : enablebuttons2,
//			ondblClickRow : function(rid, iRow, iCol, e) {
//				popMaterialDetail(rid, true);
//			},
			hidegrid: false, 
			pagerpos: 'right',
			pgbuttons: true,
			pginput: false,
			recordpos: 'left',
			viewsortcols : [true,'vertical',true],
			gridComplete: function(){
			}
		});
	}
};

var enablebuttons2 = function(idx) {
	$("#printbutton").enable().val("重新打印现品票" + $("#exd_list tr#" + idx).find("td[aria\-describedby='exd_list_quotation_first']").text()+"份");
	$("#printaddbutton").enable();


	var rowID = $("#exd_list").jqGrid("getGridParam","selrow");
	var rowData = $("#exd_list").getRowData(rowID);
}

/**
 * 小票打印
 */
var printTicket=function(addan) {

	var rowid = $("#exd_list").jqGrid("getGridParam","selrow");
	if (rowid == null || rowid=="") return;

	var rowdata = $("#exd_list").getRowData(rowid);

	var data = {
		material_id : rowdata["material_id"]
	}
	if (addan != 1) {
		data.quotator = 1;
	}
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType,
		async: false,
		url: 'material.do?method=printTicket',
		cache: false,
		data: data,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete:  function(xhrobj, textStatus){
			var resInfo = null;

			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);

				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					if ($("iframe").length > 0) {
						$("iframe").attr("src", "download.do"+"?method=output&fileName="+ rowdata["model_name"] + "-" + rowdata["serial_no"] +"-ticket.pdf&filePath=" + resInfo.tempFile);
					} else {
						var iframe = document.createElement("iframe");
						iframe.src = "download.do"+"?method=output&fileName="+ rowdata["model_name"] + "-" + rowdata["serial_no"] +"-ticket.pdf&filePath=" + resInfo.tempFile;
						iframe.style.display = "none";
						document.body.appendChild(iframe);
					}
				}
			} catch(e) {

			}
		}
	});
};


$(function() {
	$("input.ui-button").button();
	$("a.areacloser").hover(
		function (){$(this).addClass("ui-state-hover");},
		function (){$(this).removeClass("ui-state-hover");}
	);

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

	$("#continuebutton").hide();
	$("#pausebutton").show();
	$("#confirmbutton").click(doFinish);
	$("#pausebutton").click(makePause);
	$("#continuebutton").click(endPause);
	$("#breakbutton").click(makeBreak);
	$("#stepbutton").click(makeStep);
	$("#wipconfirmbutton").click(function(){
		if (this.value == '放入WIP')
			showWipEmpty();
		else {
			wip_location = $("#edit_wip_location").val();
			doFinish();
		}
	});
	$("#edit_wip_location_button").click(doMoveLocation);

	$("#body-mdl select").select2Buttons();
	$("#edit_agreed_date, #edit_ocm_deliver_date, #edit_osh_deliver_date").datepicker({
		showButtonPanel: true,
		maxDate: 0,
		currentText: "今天"
	});
//	$("#partake").button().click(function(){
//		if (this.checked) {
//			$(this).next().next().text("选择式报价");
//		} else {
//			$(this).next().next().text("非选择式报价");
//		}
//	});
	$("#printbutton").disable();
	$("#printbutton").click(printTicket);
	$("#printaddbutton").disable();
	$("#printaddbutton").click(function(){printTicket(1)});

	$("#edit_customer_name").autocomplete({
		source : customers,
		minLength :2,
		delay : 100
	});

	$("#edit_level").change(function(){
		if (this.value >=6 && this.value <= 8) {
			$("#edit_fix_type").val("2").trigger("change");
			$("#light_pat_button").closest("tr").hide();
		} else {
			$("#edit_fix_type").val("1").trigger("change");


			if (f_isLightFix(this.value) && $("#edit_fix_type").val()==2) {
				$("#light_pat_button").parents("tr").hide();
			}else if (f_isLightFix(this.value)) {
				$("#light_pat_button").parents("tr").show();
			}else {
				$("#light_pat_button").parents("tr").hide();
			}
		}
	});
	$("#edit_fix_type").change(function(){
		var edit_level = $("#edit_level")[0].value;
		if (this.value==1) {
			if (f_isLightFix(edit_level)) {
				$("#light_pat_button").closest("tr").show();
				return;
			}
		}

		$("#light_pat_button").parents("tr").hide();

	});

	$("#modifybutton").disable();
	$("#downloadbutton").disable();

	$("#makebutton").click(function(){
		var material_id=$("#hide_material_id").val();
		edit_quotistion(material_id);
	});

	$("#modifybutton").click(function(){
		var rowID = $("#exd_list").jqGrid("getGridParam","selrow");
		var rowData = $("#exd_list").getRowData(rowID);
		edit_quotistion(rowData.material_id);
	});

	$("#downloadbutton").click(function(){
		var rowID = $("#exd_list").jqGrid("getGridParam","selrow");
		var rowData = $("#exd_list").getRowData(rowID);
		var data={
			"material_id":rowData.material_id
		}

		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url :'quotaion_prospectus.do?method=report',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhjObject) {
				var resInfo = null;
				eval("resInfo=" + xhjObject.responseText);
				if (resInfo && resInfo.fileName) {
					if ($("iframe").length > 0) {
						$("iframe").attr("src", "download.do" + "?method=output&filePath=" + resInfo.filePath+"&fileName="+resInfo.fileName);
					} else {
						var iframe = document.createElement("iframe");
						iframe.src = "download.do" + "?method=output&filePath=" + resInfo.filePath+"&fileName="+resInfo.fileName;
						iframe.style.display = "none";
						document.body.appendChild(iframe);
					}
				} else {
					errorPop("文件导出失败！");
				}
			}
		});
	});

	hasPcs = (typeof pcsO === "object");

	if (hasPcs) {
		$("#manualdetailarea").hide();
		pcsO.init($("#manualdetailarea"), false);
	}

	doInit();

	//设定
	$("#light_pat_button").click(function(){
		setMpaObj.initDialog($("#light_fix_dialog"), $("#hide_material_id").val(), $("#edit_level").val(), 
			$("#material_details td:eq(1)").attr("model_id"), false);
	});
});

function load_list(listdata){

	if ($("#gbox_uld_list").length > 0) {
		$("#uld_list").jqGrid().clearGridData();
		$("#uld_list").jqGrid('setGridParam',{data:listdata}).trigger("reloadGrid", [{current:false}]);
	} else {
		$("#uld_list").jqGrid({
			data:listdata,
			//height: 461,
			width: 1248,
			rowheight: 23,
			datatype: "local",
			colNames:['受理时间', '修理单号', '型号 ID', '型号' , '机身号','RC','同意日期', '优先报价','预定纳期', '等级', '库位', '备注'],
			colModel:[
				{name:'reception_time',index:'reception_time', width:50, align:'center', 
					sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d H:i:s', newformat: 'm-d'}},
				{name:'sorc_no',index:'sorc_no', width:95},
				{name:'model_id',index:'model_id', hidden:true},
				{name:'model_name',index:'model_id', width:105},
				{name:'serial_no',index:'serial_no', width:60, align:'center'},
				{name:'ocm',index:'ocm', width:65, formatter: 'select', editoptions:{value: oOptions}},
				{name:'agreed_date',index:'agreed_date', width:50, align:'center',
					sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d', newformat: 'm-d'}},
				{
					name : 'quotation_expedited',
					index : 'quotation_first',
					width : 50, align:'center', formatter: function(value, options, rData){
						var val = ((value == 1)? "优先" : "")
						// 加急
						if (rData["scheduled_expedited"] == 1) {
							val += "<font style='color:red'>加急</font>"
						}
						return val;
					}
				},
				{name:'scheduled_date',index:'scheduled_date', width:50, align:'center',
					sorttype: 'date', formatter: 'date', formatoptions: {srcformat: 'Y/m/d', newformat: 'm-d'}},
				{name:'level',index:'level', width:35, align:'center', formatter: 'select', editoptions:{value: lOptions}},
				{name:'wip_location',index:'wip_location', width:40},
				{name:'fix_type',index:'fix_type', width:120, formatter : function(value, options, rData){
					return rData['remark'];
				}}//formatter: 'select', editoptions:{value: tOptions}}
			],
			rowNum: 20,
			rownumbers : true,
			toppager: false,
			pager: "#uld_listpager",
			viewrecords: true,
			caption: "待报价维修品一览",
			gridview: true, // Speed up
			pagerpos: 'right',
			pgbuttons: true,
			pginput: false,
			// ondblClickRow : popMaterialDetail,
			recordpos: 'left',
			viewsortcols : [true,'vertical',true]
			// gridComplete: function(){
		});
	}

};

var getFlags = function(over_time, direct_flg, light_fix, service_repair_flg) {
	if (over_time || direct_flg || light_fix) {
		var retDiv = "<div class='material_flags'>";
		if (service_repair_flg == 1) {
			retDiv += "<div class='service_repair_flg'>返</div>";
		} else if (service_repair_flg == 2) {
			retDiv += "<div class='service_repair_flg'>Ｑ</div>";
		}
		if (direct_flg == 1) {
			retDiv += "<div class='direct_flg'>直</div>";
		}
		if (light_fix == 1) {
			retDiv += "<div class='light_fix'>小</div>";
		}
		if (over_time == 1) {
			retDiv += "<div class='over_time'>超</div>";
		}
		
		retDiv += "</div>";
		return retDiv;
	} else {
		return "";
	}
};

var showTips = function(quality_tip, material_comment) {
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
		title : "质量提示",
		closeOnEscape: false
	});
}


var doMoveLocation = function() {
	var this_dialog = $("#wip_pop");
	if (this_dialog.length === 0) {
		$("body.outer").append("<div id='wip_pop'/>");
		this_dialog = $("#wip_pop");
	}

	var hide_material_id = $("#hide_material_id").val();

	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'turnover_case.do?method=getStoargeEmpty',
		cache : false,
		data : {material_id : hide_material_id},
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj) {
			// 以Object形式读取JSON
			var resInfo = $.parseJSON(xhrobj.responseText);
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				this_dialog.hide();
				//新增
				this_dialog.dialog({
					position : [ 800, 0 ],
					title : "通箱库位选择",
					width : 1200,
					show: "blind",
					resizable : false,
					modal : true,
					minHeight : 240,
					buttons : {}
				});

				this_dialog.html(resInfo.storageHtml);

				this_dialog.find(".ui-widget-content").click(function(e){
					if ("TD" == e.target.tagName) {
						if (!$(e.target).hasClass("storage-heaped")) {
							doChangeLocation(hide_material_id, $(e.target).attr("location"));
							this_dialog.dialog("close");
						}
					}
				});
		
				this_dialog.show();
			}
		}
	});
}

var doChangeLocation = function(material_id, location) {

	var data = {material_id : material_id ,location : location};

	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : 'turnover_case.do?method=doChangeLocation',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(){
			$("#edit_wip_location").val(location);
		}
	});
}
