var chosedPos = {};

var popMessageDetail = function(message_id, is_modal){
	if (is_modal == null) is_modal = true;

	var this_dialog = $("#nogood_treat");
	if (this_dialog.length === 0) {
		$("body.outer").append("<div id='nogood_treat'/>");
		this_dialog = $("#nogood_treat");
	}

	this_dialog.html("");
	this_dialog.hide();
	// 导入详细画面
	this_dialog.load("alarmMessage.do?method=detail&alarm_messsage_id=" + message_id , function(responseText, textStatus, XMLHttpRequest) {
		this_dialog.dialog({
			position : [160, 20],
			title : "警报详细画面",
			width : 'auto',
			show : "",
			height :  'auto',
			resizable : false,
			modal : is_modal,
			buttons : null
		});
	});

	this_dialog.show();
}

var popPostDetail = function(message_id, is_modal){
	if (is_modal == null) is_modal = true;

	var this_dialog = $("#post_confirm");
	if (this_dialog.length === 0) {
		$("body.outer").append("<div id='post_confirm'/>");
		this_dialog = $("#post_confirm");
	}

	this_dialog.html("");
	this_dialog.hide();
	// 导入详细画面
	this_dialog.load("header.do?method=detailPost&post_message_id=" + message_id , function(responseText, textStatus, XMLHttpRequest) {
		this_dialog.dialog({
			position : [400, 20],
			title : "通知详细画面",
			width : 'auto',
			show : "",
			height :  'auto',
			resizable : false,
			modal : is_modal,
			buttons : null
		});
	});

	this_dialog.show();
}

var getWarningComplete = function(xhrobj, rowData, callback, break_message_level, is_modal) {

	var resInfo = null;
	// 以Object形式读取JSON
	eval('resInfo =' + xhrobj.responseText);

	if (!resInfo.warning.material_id) resInfo.warning.material_id = rowData["material_id"];
	if (!resInfo.warning.position_id) resInfo.warning.position_id = rowData["position_id"];

	$("#nogood_id").val(resInfo.warning.id);
	$("#nogood_occur_time").text(resInfo.warning.occur_time);
	$("#nogood_sorc_no").text(resInfo.warning.sorc_no);
	$("#nogood_model_name").text(resInfo.warning.model_name);
	$("#nogood_serial_no").text(resInfo.warning.serial_no);
	$("#nogood_line_name").text(resInfo.warning.line_name);
	$("#nogood_process_code").text(resInfo.warning.position_name);
	$("#nogood_reason").text(resInfo.warning.reason);
	$("#nogood_comment_other").text(resInfo.warning.comment);
	$("#nogood_comment").val(resInfo.warning.myComment);
	selectedMaterial.sorc_no = resInfo.warning.sorc_no;
	selectedMaterial.model_name = resInfo.warning.model_name;
	selectedMaterial.serial_no = resInfo.warning.serial_no;
	selectedMaterial.material_id = resInfo.warning.material_id;
	selectedMaterial.position_id = resInfo.warning.position_id;
	selectedMaterial.comment = $("#nogood_comment").val();
	selectedMaterial.alarm_messsage_id = $("#nogood_id").val();

	var $this_dialog = $("#nogood_treat");

	$this_dialog.dialog({
		// position : [ 800, 20 ],
		title : "不良信息及处置",
		width : 468,
		show : "blind",
		height : 'auto', //450,
		resizable : false,
		modal : true,
		minHeight : 200,
		close : function() {
			selectedMaterial.comment = $("#nogood_comment").val();
			selectedMaterial.append_parts = ($("#append_parts_y").attr("checked") ? 1 : 0);

			if ($("#nogoodclosebtn").attr("checked") === "checked") {
				if (callback) callback(resInfo.warning.material_id, break_message_level, is_modal);
			}
			$this_dialog.html("");
		},
		buttons : {}
	});

	defectNextEntach(resInfo.waiting_analysis);


	$this_dialog.show();
}

var defectNextEntach = function(waiting_analysis){
	var $titlebar = $("#nogood_treat").prev(".ui-dialog-titlebar");
	if ($titlebar.length == 0) {
		if ($("#nogood_treat").length > 0) {
			setTimeout(function(){defectNextEntach(waiting_analysis)}, 300);
		}
		return;
	}
	if (waiting_analysis) {
		if ($titlebar.children("#defective_next").length == 0) {
			$titlebar.append('<div id="defective_next" style="padding-top: 5px;margin-left:12em;"><input id="defective_next_check" checked type="checkbox"><label for="defective_next_check">处理完成后开启不良对策</label></div>');
		} else {
			$("#defective_next_check").attr("checked", true).trigger("change");
		}
	} else {
		$("#defective_next").remove();
	}
}

/*
* 取得流程信息返回
*/
var getFlowchart_handleComplete = function(xhrobj, textStatus, callback) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {

			chosedPos = {};

			if ($("#rework_pat_id").attr("neo")) {
				$("#hdn_pat_id").val(resInfo.pat_id);
				$("#rework_pat_id").val(resInfo.pat_id).trigger("change");
			}

			$("#pa_red").html("");
			$("#pa_red").flowchart({},{editable:false, selections: resInfo.positions});
			$("#pa_red").flowchart("fill", resInfo.processAssigns);

			// 小修理工位
			if (resInfo.isLightFix) {
				if (typeof  setMpaObj == "undefined" ) {
					loadJs("js/qf/set_material_process_assign.js?version=605", function(){
						lightFixFlow(resInfo);
					});
				} else {
					lightFixFlow(resInfo);
				}
			} else {
				$("#light_fix_content").text("").parent().hide();

				$("#pa_red span:empty").closest(".edgeposition").each(function(){
					$(this).hide();
					if ($(this).parent().hasClass("pos")) {
						$(this).parent().hide();
					}
				});
	
				signProductionFeature(resInfo);

				$("#pa_red span.suceed").click(function() {
					var mespan = $(this);
	
					if (mespan.hasClass("rework")) {
						mespan.removeClass("rework");
					} else {
						mespan.addClass("rework");
					}
				});
			}

			if (callback) callback();
		}

		$("#process_resign").dialog({
			// position : [ 800, 20 ],
			title : "不良信息及处置",
			width : 'auto' ,
			show : "blind",
			height : 620,// 'auto' ,
			resizable : false,
			modal : true,
			minHeight : 200,
			close : function() {
				$("#process_resign").html("");
			},
			buttons : {
				"确定" : function() {
					var data = {
						material_id : selectedMaterial.material_id,
						position_id : selectedMaterial.position_id,
						alarm_messsage_id : selectedMaterial.alarm_messsage_id,
						comment : selectedMaterial.comment,
						append_parts : $("#flowcase input[name='append_parts']:checked").val() || selectedMaterial.append_parts
					};

					var reworkPositions = $("#pa_red span.rework").parent();
					if (reworkPositions.length > 0) {
					for (var iReworkPositions = 0; iReworkPositions<reworkPositions.length; iReworkPositions++) {
						var $reworkPosition = $(reworkPositions[iReworkPositions]);
						data["rework.positions["+iReworkPositions+"]"] = $reworkPosition.attr("code");
					}
					}

					// 同时修改维修路径
					if ($("#rework_pat_id").length > 0) {
						if ($("#hdn_pat_id").val() != $("#rework_pat_id").val()) {
							data.pat_id = $("#rework_pat_id").val();
						}
					} else {
						// 线长线内必返工位
						if (!data["rework.positions[0]"]) {
							data["rework.positions[0]"] = $("#pa_red span.nogood").parent().attr("code");
						}
					}

					// 记载到工程检查票
					if ($("#flowcase input[name=pcs_signed]").length > 0) {
						if ($("#pcs_signed_y").attr("checked")) {
							data["pcs_signed"] = "true";
						}
					}

					// 小修理修改过流程
					if (data.pat_id || // 只是改过参考流程也需要重排
						($("#flowcase div.pos span.point").length > 0
						&& $("#flowcase div.pos span.rework").not(".point").length > 0)) {

						setMpaObj.postData($("#pa_red"), data, true);
						data["flow_str"] = setMpaObj.postSelText($("#pa_red"));
					}

					// Ajax提交
					$.ajax({
						beforeSend : ajaxRequestType,
						async : false,
						url : 'alarmMessage.do?method=dorework',
						cache : false,
						data : data,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : function(xhrObj, textStatus) {
							var resInfo = $.parseJSON(xhrObj.responseText);
							if (resInfo.errors && resInfo.errors.length > 0) {
								treatBackMessages(null, resInfo.errors);
							} else {
								$("#process_resign").dialog('close');
								$("#alarm_" + $("#nogood_id").val()).hide("slide");
								try {
								if (refreshList) refreshList();
								}catch(e){
								}
							}
						}
					});
				},
				"重置" : function() {
					$("#process_resign .rework").removeClass("rework");
					$("#flowcase input[name=reworked]").enable().trigger("change");
				}, "关闭" : function(){ $(this).dialog("close"); }
			}
		});
		$("#process_resign").show();

	} catch (e) {
		console.log("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
}

var signProductionFeature = function(resInfo){
	var resultlen = resInfo.result.length;
	for (var iresult = 0 ; iresult < resultlen ; iresult++) {
		var productionFeature = resInfo.result[iresult];
		$("#pa_red div.pos[code="+ parseInt(productionFeature.position_id, 10) +"]").find("span").addClass("suceed")
			.after("<div class=\"feature_result\">"+productionFeature.operator_name+"<br>"+productionFeature.finish_time+"</div>");
	}
	$("#pa_red div.pos[code="+ parseInt(selectedMaterial.position_id, 10) +"]").find("span").addClass("nogood");
}

var lightFixFlow = function(resInfo){

	$("#light_fix_content").text(resInfo.light_fix_content).parent().show();

	setMpaObj.addCellPos($("#pa_red"), resInfo);

	$("#pa_red div.pos").each(function(index,ele){
		var $div = $(ele);
		var code = $div.attr("code");
		var $span = $div.find("span");

		for(var i=0;i<resInfo.mProcessAssigns.length;i++){
			var obj = resInfo.mProcessAssigns[i];

			if(code == obj.position_id){ //  && !$span.hasClass("suceed")
				$span.addClass("point");
				chosedPos[code]=1;
				break;
			}
		}
	});

	$("#ref_allpos").button().click(function(){
		$(".pos span").each(function(idx, ele){
			$span = $(ele);
			var pos_id = $span.parent().attr("code");
			if ($span.hasClass("rework")) {
			} else {
				$span.addClass("rework");
				chosedPos[pos_id] = 1;
			}
		});
	});

	signProductionFeature(resInfo);

	$("#pa_red span").click(function() {
		var mespan = $(this);

		if (mespan.hasClass("rework")) {
			mespan.removeClass("rework");
		} else {
			mespan.addClass("rework");
		}
	});
}

var process_resign = function() {
	$("#process_resign").hide();
	// µ¼Èë·µ¹¤»­Ãæ
	$("#process_resign").load("widget.do?method=rework", function(responseText, textStatus, XMLHttpRequest) {

		$("#rework_sorc_no").text(selectedMaterial.sorc_no);
		$("#rework_model_name").text(selectedMaterial.model_name);
		$("#rework_serial_no").text(selectedMaterial.serial_no);
		$("#rework_line_name").text(selectedMaterial.line_name);

		$("#flowcase input[name='append_parts'][value=" + (selectedMaterial.append_parts || 0) + "]")
			.attr("checked", true).trigger("change");

		// AjaxÌá½»
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : 'material.do?method=getFlowchart',
			cache : false,
			data : {material_id : selectedMaterial.material_id},
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : getFlowchart_handleComplete
		});
	});
};

function getPrevPos($posData){
	var posid = $posData.attr("posid");
	var code = $posData.attr("code");
	var prevcode = $posData.attr("prevcode");
	var nextcode = $posData.attr("nextcode");
	if (code == null) return null;

	if (posid == code) {
		if (prevcode == "0") {
			var $justMulti = $posData.parents(".just-multi:first");
			if ($justMulti.length == 0) {
				return "0";
			} else {
				return getPrevPos($justMulti.parent().parent());
			}
		}
		var $thePrev = $("#pa_main").find(".div[code="+code+"]");
		if ($thePrev.children("span").hasClass("point") || $thePrev.children("span").hasClass("suceed")) {
			return $thePrev.attr("code");
		} else {
			return getPrevPos($thePrev);
		}
	}
	return null;
}