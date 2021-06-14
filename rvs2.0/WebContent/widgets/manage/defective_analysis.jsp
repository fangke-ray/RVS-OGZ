<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<style>
#defective_analysis_detail_content textarea.mm_type {
	width:618px; height: 120px;
    resize: vertical;
}
#defective_analysis_detail_content div.imageLoader {
	width:618px; height: 128px;
    display:none;
}
#defective_analysis_detail_content div.typeSwitcher {
	height: 1.5em;
	padding: 0.2em;
	position: absolute;
	background-color:blue;
	right: 2em;
	cursor: pointer;
	color: white;
}
#defective_analysis_detail_content div.imageLoader > div {
	width: 180px;
	height: 120px;
	background-color: lightblue;
	border : 1px solid gray;
	float: left;
	overflow-y: hidden;
	position:relative;
}
#defective_analysis_detail_content div.imageLoader > div > img{
	width: 100%;
}
#defective_analysis_detail_content div.imageLoader > div > input {
	position:absolute;
	width: 100%;
	bottom:0;
	left:0;
	border: 0;
	box-sizing: border-box;
	background-color: rgba(255,255,255,0.8);
}
#defective_analysis_detail_content div.imageLoader > div > .rm_img {
	position:absolute;
	width: 1.44em;
	height: 1.44em;
	top:0;
	right:0;
	border: 0;
	background-color: darkblue;
	cursor:pointer;
	border-bottom-left-radius: 0.8em;
}
#defective_analysis_detail_content div.imageLoader > div.il_add {
	cursor: pointer;
	font-size: 50px;
	line-height: 110px;
	text-align:center;
}
#defective_analysis_detail_content div.typeSwitcher.typePicture {
	background-color:gold;
}
</style>

<script type="text/javascript">
var loadImageInputer = function(){
	$("#defective_analysis_detail_content textarea.mm_type").each(function(){
		$(this)
			.after("<div class='imageLoader'><div class='il_add'>+</div></div>")
			.before("<div class='typeSwitcher'>纯文字</div>");
	});
	var $imageLoader = $("#defective_analysis_detail_content div.imageLoader");
	$imageLoader.on("click", " > div > .rm_img", function(){
		$(this).closest("div[photo_uuid]").remove();
	}).on("click", " > div > img", function(){
		var $editTarget = $(this).closest("div[photo_uuid]");
		photo_editor_functions.editImgFor($editTarget, function(photo_uuid){
			if (photo_uuid) {
				var new_uuid = photo_editor_functions.getPathByPhotoUuid(photo_uuid);
				console.log(new_uuid);
				$editTarget.children("img").attr("src", new_uuid);
			}
		});
	});

	$("#defective_analysis_detail_content .typeSwitcher").click(function(){
		var $typeSwitcher = $(this);
		if ($typeSwitcher.hasClass("typePicture")) {
			$typeSwitcher.text("纯文字").removeClass("typePicture");
			$typeSwitcher.next().show()
						.next().hide();
		} else {
			$typeSwitcher.text("图文").addClass("typePicture");
			$typeSwitcher.next().hide()
						.next().show();
		}
	});
	$("#defective_analysis_detail_content .il_add").click(function(){
		var $addTarget = $("<div photo_uuid=''><img src=''></img><input type='text'><div class='rm_img ui-icon  ui-icon-close'></div></div>");
		$(this).before($addTarget);
		photo_editor_functions.editImgFor($addTarget, function(photo_uuid){
			if (photo_uuid) {
				$addTarget.attr("photo_uuid", photo_uuid);
				$addTarget.children("img").attr("src", photo_editor_functions.getPathByPhotoUuid(photo_uuid));
			} else {
				$addTarget.remove();
			}
		});
	});
}


var defectiveJs = function(){

	var $select2buttonGrp = $("#detail\\.defective_type, #detail\\.step, #detail\\.responsibility_of_line, "
		+ "#detail\\.responsibility_of_ptl, #detail\\.rework_proceed, #detail\\.involving,"
		+ "#detail\\.capa_frequency, #detail\\.capa_major, #detail\\.capa_risk, #detail\\.stored_parts_resolve");

	var popServicePath = "defectiveAnalysis.do";

	{ // $(function()
		$("#nogoodform").validate({
			rules : {
				comment : {
					required : true
				}
			}
		});
		$("input.ui-button").button();

		$select2buttonGrp.select2Buttons();

		$("#detail\\.receive_date").datepicker({
			showButtonPanel:true,
			currentText: "今天"
		});

		$("#okButton").click(function() {
			var data = {
				"alarm_message_id": $("#detail\\.alarm_message_id").val(),
				"defective_type": $("#detail\\.defective_type").val(),
				"manage_code": $("#detail\\.manage_code").val(),
				"step": $("#detail\\.step").val(),
				"responsibility_of_line": $("#detail\\.responsibility_of_line").val(),
				"responsibility_of_ptl": $("#detail\\.responsibility_of_ptl").val(),
				"cause_analysis": $("#detail\\.cause_analysis").val(),
				"capa_frequency": $("#detail\\.capa_frequency").val(),
				"capa_major": $("#detail\\.capa_major").val(),
				"capa_risk": $("#detail\\.capa_risk").val(),
				"append_part_order": $("#detail\\.append_part_order").val(),
				"rework_proceed": $("#detail\\.rework_proceed").val(),
				"countermeasures": $("#detail\\.countermeasures").val(),
				"countermeasure_effects": $("#detail\\.countermeasure_effects").val(),

				"nongood_parts_situation": $("#detail\\.nongood_parts_situation").val(),
				"receive_date": $("#detail\\.receive_date").val(),
				"stored_parts": $("#detail\\.stored_parts").val(),
				"stored_parts_resolve": $("#detail\\.stored_parts_resolve").val(),
				"occur_times": $("#detail\\.occur_times").val(),

				"defective_items": $("#detail\\.defective_items").val(),
				"involving": $("#detail\\.involving").val(),
				"involving_reason": $("#detail\\.involving_reason").val(),
				"closing_judgment": $("#detail\\.closing_judgment").val()
			};

			var photoIndex = 0;

			var $defective_phenomenon = $("#detail\\.defective_phenomenon");
			if ($defective_phenomenon.length > 0) {
				if ($defective_phenomenon.is(":visible")) {
					data["defective_phenomenon"] = $defective_phenomenon.val();
				} else {
					$defective_phenomenon.next(".imageLoader").children("div[photo_uuid]").each(function(iPh, ePh){
						data['photo[' + photoIndex + '].seq'] = iPh;
						data['photo[' + photoIndex + '].for_step'] = 0;
						data['photo[' + photoIndex + '].file_uuid'] = ePh.getAttribute("photo_uuid");
						photoIndex++;
					})
				}
			}

			var url = 'defectiveAnalysis.do?method=doCommit&alarm_message_id=' + $("#detail\\.alarm_message_id").val();
			// Ajax提交
			$.ajax({
				beforeSend : ajaxRequestType,
				async : false,
				url : url,
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
						$("#defective_analysis").dialog('close');

						$("#alarm_" + $("#nogood_id").val()).hide("slide");
						if (typeof(refreshList) === "function") refreshList();
						if (typeof(findit) === "function") findit();
					}
				}
			});
		});

		$("#cancelButton").click(function() {
			$(defective_analysis_detail_dlg).dialog("close");
		});
		$("#detail\\.defective_type").change(function() {

			var $trNewMaterialDismissId = $("#detail\\.tr_nongood_parts_situation_disp,"
					+"#detail\\.tr_receive_date, "
					+"#detail\\.tr_stored_parts, "
					+"#detail\\.tr_stored_parts_resolve, "
					+"#detial\\.tr_defective_items ");

			if ($(this).val() == 4) {

				$("#detail\\.responsibility_of_line").val("3");
				$("#detail\\.responsibility_of_line").select2Buttons();
				$("#s2b_detail\\.responsibility_of_line li").eq(0).hide();
				$("#s2b_detail\\.responsibility_of_line li").eq(1).hide();
			} else {

				$("#detail\\.responsibility_of_line").val("3");
				$("#detail\\.responsibility_of_line").select2Buttons();
				$("#s2b_detail\\.responsibility_of_line li").eq(0).show();
				$("#s2b_detail\\.responsibility_of_line li").eq(1).show();
			}

			if ($(this).val() == 2) {
				$trNewMaterialDismissId.show();
			} else {
				$trNewMaterialDismissId.hide();
			}
			//
		});

		$("#manage_code_button").click(function(){
			var postData = {
				"alarm_message_id": $("#detail\\.alarm_message_id").val(),
				"defective_type": $("#detail\\.defective_type").val()
			}
			// Ajax提交
			$.ajax({
				beforeSend : ajaxRequestType,
				async : false,
				url : 'defectiveAnalysis.do?method=getAutoManageCode',
				cache : false,
				data : postData,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : function(xhrObj, textStatus) {
					var resInfo = $.parseJSON(xhrObj.responseText);
					if (resInfo.errors && resInfo.errors.length > 0) {
						treatBackMessages(null, resInfo.errors);
					} else {
						$("#detail\\.manage_code").val(resInfo.manage_code);
					}
				}
			});
		});

		loadImageInputer();

		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : 'defectiveAnalysis.do?method=detailInit',
			cache : false,
			data : {alarm_message_id : $("#detail\\.alarm_message_id").val()},
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhjobj) {
				var resInfo = null;
				try {
					eval("resInfo=" + xhjobj.responseText);
					if (resInfo.alarm) {
						$("#detail\\.reception_time").text(resInfo.alarm.reception_time);
						$("#detail\\.omr_notifi_no").text(resInfo.alarm.omr_notifi_no);
						$("#detail\\.model_name").text(resInfo.alarm.model_name);
						$("#detail\\.serial_no").text(resInfo.alarm.serial_no);
						$("#detail\\.level_disp").text(resInfo.alarm.level_disp);
						$("#detail\\.customer_name").text(resInfo.alarm.customer_name);
						$("#detail\\.alarm_comments").text(resInfo.alarm.alarm_comments);

						$("#detail\\.alarm_message_id").val(resInfo.alarm.alarm_message_id);
						$("#detail\\.defective_type").val(resInfo.alarm.defective_type);
						$("#detail\\.defective_type_disp").text(resInfo.alarm.defective_type_disp);

						$("#detail\\.manage_code").val(resInfo.alarm.manage_code);
						$("#detail\\.manage_code_disp").text(resInfo.alarm.manage_code);

						$("#detail\\.step").val(resInfo.alarm.step);
						$("#detail\\.step_disp").text(resInfo.alarm.step_disp);

						$("#detail\\.defective_phenomenon").val(resInfo.alarm.defective_phenomenon);
						$("#detail\\.defective_phenomenon_disp").html(decodeText(resInfo.alarm.defective_phenomenon));

						$("#detail\\.responsibility_of_line").val(resInfo.alarm.responsibility_of_line);
						$("#detail\\.responsibility_of_line_disp").text(resInfo.alarm.responsibility_of_line_disp);

						$("#detail\\.responsibility_of_ptl").val(resInfo.alarm.responsibility_of_ptl);
						$("#detail\\.responsibility_of_ptl_disp").text(resInfo.alarm.responsibility_of_ptl_disp);

						$("#detail\\.cause_analysis").val(resInfo.alarm.cause_analysis);
						$("#detail\\.cause_analysis_disp").html(decodeText(resInfo.alarm.cause_analysis));

						$("#detail\\.capa_frequency").val(resInfo.alarm.capa_frequency);
						$("#detail\\.capa_frequency_disp").text(resInfo.alarm.capa_frequency_disp);

						$("#detail\\.capa_major").val(resInfo.alarm.capa_major);
						$("#detail\\.capa_major_disp").text(resInfo.alarm.capa_major_disp);

						$("#detail\\.capa_risk").val(resInfo.alarm.capa_risk);
						$("#detail\\.capa_risk_disp").text(resInfo.alarm.capa_risk_disp);

						$("#detail\\.append_part_order").val(resInfo.alarm.append_part_order);
						$("#detail\\.append_part_order_disp").text(resInfo.alarm.append_part_order);

						$("#detail\\.partial_applyier_name").text(resInfo.alarm.partial_applyier_name);

						$("#detail\\.rework_proceed").val(resInfo.alarm.rework_proceed);
						$("#detail\\.rework_proceed_disp").text(resInfo.alarm.rework_proceed_disp);

						$("#detail\\.countermeasures").val(resInfo.alarm.countermeasures);
						$("#detail\\.countermeasures_disp").html(decodeText(resInfo.alarm.countermeasures));

						$("#detail\\.countermeasure_effects").val(resInfo.alarm.countermeasure_effects);
						$("#detail\\.countermeasure_effects_disp").html(decodeText(resInfo.alarm.countermeasure_effects));

						$("#detail\\.nongood_parts_situation").val(resInfo.alarm.nongood_parts_situation);
						$("#detail\\.nongood_parts_situation_disp").html(decodeText(resInfo.alarm.nongood_parts_situation));

						$("#detail\\.receive_date").val(resInfo.alarm.receive_date);
						$("#detail\\.receive_date_disp").text(resInfo.alarm.receive_date);

						$("#detail\\.stored_parts").val(resInfo.alarm.stored_parts);
						$("#detail\\.stored_parts_disp").text(resInfo.alarm.stored_parts);

						$("#detail\\.stored_parts_resolve").val(resInfo.alarm.stored_parts_resolve);
						$("#detail\\.stored_parts_resolve_disp").text(resInfo.alarm.stored_parts_resolve_disp);

						$("#detail\\.occur_times").val(resInfo.alarm.occur_times);
						$("#detail\\.occur_times_disp").text(resInfo.alarm.occur_times);

						$("#detail\\.defective_items").val(resInfo.alarm.defective_items);

						$("#detail\\.involving").val(resInfo.alarm.involving);
						$("#detail\\.involving_disp").text(resInfo.alarm.involving_disp);

						$("#detail\\.involving_reason").val(resInfo.alarm.involving_reason);
						$("#detail\\.involving_reason_disp").text(resInfo.alarm.involving_reason);

						$("#detail\\.closing_judgment").val(resInfo.alarm.closing_judgment);
						$("#detail\\.closing_judgment_disp").html(decodeText(resInfo.alarm.closing_judgment));

						$("#detail\\.sponsor_operator_name").text(resInfo.alarm.sponsor_operator_name);
						$("#detail\\.sponsor_date").text(resInfo.alarm.sponsor_date);

						$("#detail\\.phenomenon_confirmer_name").text(resInfo.alarm.phenomenon_confirmer_name);
						$("#detail\\.phenomenon_confirmer_date").text(resInfo.alarm.phenomenon_confirmer_date);

						$("#detail\\.cause_analyst_name").text(resInfo.alarm.cause_analyst_name);
						$("#detail\\.cause_analyst_date").text(resInfo.alarm.cause_analyst_date);

						$("#detail\\.cause_confirmer_name").text(resInfo.alarm.cause_confirmer_name);
						$("#detail\\.cause_confirmer_date").text(resInfo.alarm.cause_confirmer_date);

						$("#detail\\.cm_filer_name").text(resInfo.alarm.cm_filer_name);
						$("#detail\\.cm_filer_date").text(resInfo.alarm.cm_filer_date);

						$("#detail\\.cm_confirmer_name").text(resInfo.alarm.cm_confirmer_name);
						$("#detail\\.cm_confirmer_date").text(resInfo.alarm.cm_confirmer_date);

						$("#detail\\.cm_processor_name").text(resInfo.alarm.cm_processor_name);
						$("#detail\\.cm_processor_date").text(resInfo.alarm.cm_processor_date);

						$("#detail\\.cm_proc_confirmer_name").text(resInfo.alarm.cm_proc_confirmer_name);
						$("#detail\\.cm_proc_confirmer_date").text(resInfo.alarm.cm_proc_confirmer_date);

						$("#detail\\.cm_effect_verifier_name").text(resInfo.alarm.cm_effect_verifier_name);
						$("#detail\\.cm_effect_verifier_date").text(resInfo.alarm.cm_effect_verifier_date);

						$("#detail\\.cm_effect_confirmer_name").text(resInfo.alarm.cm_effect_confirmer_name);
						$("#detail\\.cm_effect_confirmer_date").text(resInfo.alarm.cm_effect_confirmer_date);

						$("#detail\\.closing_judger_name").text(resInfo.alarm.closing_judger_name);
						$("#detail\\.closing_judger_date").text(resInfo.alarm.closing_judger_date);

						$("#detail\\.closing_confirmer_name").text(resInfo.alarm.closing_confirmer_name);
						$("#detail\\.closing_confirmer_date").text(resInfo.alarm.closing_confirmer_date);

						$("#detail\\.defective_type").trigger("change");

						$select2buttonGrp.select2Buttons();

						if (resInfo.photo_list && resInfo.photo_list.length) {
							showPhotoList(resInfo.photo_list);
						}
					}
				} catch(e) {

				}
			}
		});
	}; // $(function()
} // defectiveJs

var showPhotoList = function(photo_list){
	var kinshasa = {};

	for (var photo_idx in photo_list) {
		var photo_entity = photo_list[photo_idx];
		if (kinshasa[photo_entity.for_step] == null) {
			kinshasa[photo_entity.for_step] = [];
		}
		kinshasa[photo_entity.for_step].push(photo_entity.file_uuid);
	}
	if (kinshasa[0] && kinshasa[0].length) {
		setPhotoStepList("defective_phenomenon", kinshasa[0]);
	}
}

var setPhotoStepList = function(position, photo_list) {
	var $input = $("#detail\\." + position);
	var $disp = $("#detail\\." + position + "_disp");
	if ($disp.length) {
		var comment = $disp.text();
		var commentTarget = {};
		if (comment) {
			try {
				commentTarget = $.parseJSON(comment);
			} catch(e) {
			}
		}

		var photoHtml = '<div class="imageLoader" style="display: block;">';
		for (var i in photo_list) {
			photoHtml += '<div><img src="' + photo_editor_functions.getPathByPhotoUuid(photo_list[i]) + '">' + 
				'<input type="text" ' + (commentTarget[i] || "") + ' disabled></div>';
		}
		photoHtml += '</div>';

		$disp.html(photoHtml);
	} else if ($input.length) {
		var $typeSwitcher = $input.prev();
		$typeSwitcher.text("图文").addClass("typePicture");
		$typeSwitcher.next().hide()
						.next().show();

		var $addCom = $input.next(".imageLoader").children(".il_add");
		console.log($addCom);
		for (var i in photo_list) {
			var $addTarget = $("<div photo_uuid=''><img src=''></img><input type='text'><div class='rm_img ui-icon ui-icon-close'></div></div>");
			$addCom.before($addTarget);
			$addTarget.attr("photo_uuid", photo_list[i]);
			$addTarget.children("img").attr("src", photo_editor_functions.getPathByPhotoUuid(photo_list[i]));
			console.log($addTarget);
		}
	}
}

var loadPhotoEditor = function(){
	if (typeof(photo_editor_functions) === "object") {
		defectiveJs();
	} else {
		loadJs("js/jquery.Jcrop.js");
		loadJs("js/common/photo_editor.js", defectiveJs);
	}
}

var loadNogoodEditJqueryPlus = function(){
	if (typeof(warningConfirm) === "function") {
		loadPhotoEditor();
	} else {
		loadJs("js/jquery-plus.js", loadPhotoEditor);
	}
}

if (!$.validator) {
	loadJs("js/jquery.validate.min.js", loadNogoodEditJqueryPlus);
} else {
	loadNogoodEditJqueryPlus();
}
</script>

<form id="nogoodform">
<%
	Integer powerId = (Integer)request.getAttribute("defectivePowerId");
	Integer typeFlag = (Integer)request.getAttribute("defectiveTypeFlag");
	if (typeFlag == null) typeFlag = -1;
	Integer step = (Integer)request.getAttribute("defectiveStep");
%>
<div id="defective_analysis_detail_base_area" class="dwidth-left" style="float:left;margin-right:8px;">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-left">
		<span class="areatitle">维修品信息</span>
		<input type="hidden" id="detail.alarm_message_id" name="detail.alarm_message_id" value="${alarm_message_id}">
	</div>
	<div class="ui-widget-content dwidth-left">
		<table class="condform">
			<tbody>
				<tr>
					<td class="ui-state-default td-title">受理日</td>
					<td class="td-content">
						<span id="detail.reception_time"></span>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">修理NO.</td>
					<td class="td-content" colspan="7">
						<span id="detail.omr_notifi_no"></span>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">产品名称</td>
					<td class="td-content">
						<span id="detail.model_name"></span>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">BodyNo.</td>
					<td class="td-content">
						<span id="detail.serial_no"></span>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">修理等级</td>
					<td class="td-content">
						<span id="detail.level_disp"></span>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">客户名称</td>
					<td class="td-content">
						<span id="detail.customer_name"></span>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">警报描述</td>
					<td class="td-content">
						<span id="detail.alarm_comments"></span>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</div>
<div id="defective_analysis_detail_content" style="float:left;margin:auto;">
	<div style="height:44px;width:100%;" id="defective_analysis_detail_infoes" class="dwidth-middle">

		<input type="radio" name="defective_analysis_detail_infoes" class="ui-button ui-corner-up" id="defective_analysis_detail_infoes_sponsor" role="button" checked><label for="defective_analysis_detail_infoes_sponsor" title="">不良提出</label>
<% if (step >= 1) {%>
		<input type="radio" name="defective_analysis_detail_infoes" class="ui-button ui-corner-up" id="defective_analysis_detail_infoes_cause" role="button"><label for="defective_analysis_detail_infoes_cause" title="">原因分析</label>
<% } %>
<% if (step >= 2) {%>
		<input type="radio" name="defective_analysis_detail_infoes" class="ui-button ui-corner-up" id="defective_analysis_detail_infoes_filer" role="button"><label for="defective_analysis_detail_infoes_filer">对策立案</label>
<% } %>
<% if (step >= 3) {%>
		<input type="radio" name="defective_analysis_detail_infoes" class="ui-button ui-corner-up" id="defective_analysis_detail_infoes_processor" role="button"><label for="defective_analysis_detail_infoes_processor">对策实施</label>
<% } %>
<% if (step >= 4) {%>
		<input type="radio" name="defective_analysis_detail_infoes" class="ui-button ui-corner-up" id="defective_analysis_detail_infoes_processor_confirm" role="button"><label for="defective_analysis_detail_infoes_processor_confirm">对策实施确认</label>
<% } %>
<% if (typeFlag == 1 && step >= 5) {%>
		<input type="radio" name="defective_analysis_detail_infoes" class="ui-button ui-corner-up" id="defective_analysis_detail_infoes_effect" role="button"><label for="defective_analysis_detail_infoes_effect">委托关闭判断</label>
<% } %>
	</div>

	<div class="ui-widget-content defective_analysis_detail_tabcontent" for="defective_analysis_detail_infoes_sponsor" style="width:100%;text-align:left;">
		<div id="defective_analysis_detail_sponsor_area" style="margin-top:22px;margin-left:9px;">
			<div class="ui-widget-content dwidth-middle">
				<table class="condform">
					<tbody>
						<tr>
							<td class="ui-state-default td-title">不良分类</td>
							<td class="td-content" colspan="3">
<% if (step == 0 && (powerId == 1 || powerId == 3)) {%>
								<select name="detail.defective_type" id="detail.defective_type" class="ui-widget-content">${defectiveTypeOptions}</select>
<% } else { %>
								<span id="detail.defective_type_disp"></span>
								<input id="detail.defective_type" name="detail.defective_type" type="hidden">
<% } %>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">对策进度</td>
							<td class="td-content">
<% if (step == 0 && (powerId == 1 || powerId == 3)) {%>
								<select name="detail.step" id="detail.step" class="ui-widget-content">${defectiveStepOptions}</select>
<% } else { %>
								<span id="detail.step_disp"></span>
<% } %>
							</td>
							<td class="ui-state-default td-title">管理编号</td>
							<td class="td-content">
<% if (step == 0 && (powerId == 1 || powerId == 3)) {%>
								<input type="text" id="detail.manage_code" name="detail.manage_code" style="width:12em;" class="ui-widget-content">
								<input type="button" id="manage_code_button" value="自动取号" class="ui-button" style="font-size:12px;">
<% } else { %>
								<span id="detail.manage_code_disp"></span>
								<input type="hidden" id="detail.manage_code" name="detail.manage_code" class="ui-widget-content">
<% } %>
							</td>
						</tr>
						<tr <% if (typeFlag == 1 && step == 0 && powerId == 3) {%> style="display: none;" <% } %>>
							<td class="ui-state-default td-title">责任区分（生产线）</td>
							<td class="td-content" colspan="3">
<% if (step <= 2 && (powerId == 1)) {%>
								<select name="detail.responsibility_of_line" id="detail.responsibility_of_line" class="ui-widget-content">${defectiveResponsibilityOfLineOptions}</select>
<% } else { %>
								<span id="detail.responsibility_of_line_disp"></span>
<% } %>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">不良现象</td>
							<td class="td-content" colspan="3">
<% if (step == 0 && (powerId == 1 || powerId == 3)) {%>
								<textarea name="detail.defective_phenomenon" alt="不良现象" class="mm_type" photo="1" id="detail.defective_phenomenon"></textarea>
<% } else { %>
								<div id="detail.defective_phenomenon_disp"></div>
<% } %>
							</td>
						</tr>
						<tr <% if (typeFlag == 1 && step == 0 && powerId == 3) {%> style="display: none;" <% } %>>
							<td class="ui-state-default td-title">返工对应</td>
							<td class="td-content" colspan="3">
<% if (step <= 2 && powerId == 1) { %>
								<select name="detail.rework_proceed" id="detail.rework_proceed" class="ui-widget-content">${defectiveReworkProceedOptions}</select>
<% } else { %>
								<span id="detail.rework_proceed_disp"></span>
<% } %>
							</td>
						</tr>
						<tr <% if (typeFlag == 1 && step == 0 && powerId == 3) {%> style="display: none;" <% } %>>
							<td class="ui-state-default td-title">更换零件对应</td>
							<td class="td-content" colspan="3">
<% if (step <= 2 && powerId == 1) {%>
								<input type="text" name="detail.append_part_order" id="detail.append_part_order" class="ui-widget-content" style="width:618px;">
<% } else { %>
								<span id="detail.append_part_order_disp"></span>
<% } %>
							</td>
						</tr>
						<tr id="detail.tr_nongood_parts_situation_disp" <% if (typeFlag == 1 && step == 0 && powerId == 3) {%> style="display: none;" <% } %>>
							<td class="ui-state-default td-title">不良零件情况</td>
							<td class="td-content" colspan="3">
<% if (step <= 2 && powerId == 1) {%>
								<textarea name="detail.nongood_parts_situation" alt="不良零件情况" id="detail.nongood_parts_situation"></textarea>
<% } else { %>
								<div id="detail.nongood_parts_situation_disp"></div>
<% } %>
							</td>
						</tr>

						<tr id="detail.tr_receive_date" <% if (typeFlag == 1 && step == 0 && powerId == 3) {%> style="display: none;" <% } %>>
							<td class="ui-state-default td-title">领取日期</td>
							<td class="td-content" colspan="3">
<% if (step <= 2 && powerId == 1) {%>
								<input type="text" name="detail.receive_date" id="detail.receive_date" class="ui-widget-content" value="${today}">
<% } else { %>
								<span id="detail.receive_date_disp"></span>
<% } %>
							</td>
						</tr>
						<tr id="detail.tr_stored_parts" <% if (typeFlag == 1 && step == 0 && powerId == 3) {%> style="display: none;" <% } %>>
							<td class="ui-state-default td-title">入库部品</td>
							<td class="td-content" colspan="3">
<% if (step <= 2 && powerId == 1) {%>
								<input type="text" name="detail.stored_parts" id="detail.stored_parts" class="ui-widget-content" style="width:618px;">
<% } else { %>
								<span id="detail.stored_parts_disp"></span>
<% } %>
							</td>
						</tr>
						<tr id="detail.tr_stored_parts_resolve" <% if (typeFlag == 1 && step == 0 && powerId == 3) {%> style="display: none;" <% } %>>
							<td class="ui-state-default td-title">入库零件不良处理</td>
							<td class="td-content" colspan="3">
<% if (step <= 2 && powerId == 1) {%>
								<select name="detail.stored_parts_resolve" id="detail.stored_parts_resolve" class="ui-widget-content">${defectiveStoredPartsResolveOptions}</select>
<% } else { %>
								<span id="detail.stored_parts_resolve_disp"></span>
<% } %>
							</td>
						</tr>

						<tr id="detial.tr_defective_items" <% if (typeFlag != 1) { %>style="display: none;" <%} %>>
							<td class="ui-state-default td-title">委托名称</td>
							<td class="td-content" colspan="3">
<% if (powerId == 3 && step == 0) { %>
								<input type="text" id="detail.defective_items" class="ui-widget-content"style=" width:618px;">
<% } else { %>
								<span id="detail.defective_items"></span>
<% } %>
							</td>
						</tr>

						<tr>
							<td class="ui-state-default td-title">提出者</td>
							<td class="td-content">
								<span id="detail.sponsor_operator_name"></span>
								<span id="detail.sponsor_date"></span>
							</td>
							<td class="ui-state-default td-title">现象确认者</td>
							<td class="td-content">
								<span id="detail.phenomenon_confirmer_name"></span>
								<span id="detail.phenomenon_confirmer_date"></span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>

	<div class="ui-widget-content defective_analysis_detail_tabcontent" for="defective_analysis_detail_infoes_cause" style="width:100%;text-align:left;display:none;">
		<div id="defective_analysis_detail_cause_area" style="margin-top:22px;margin-left:9px;">
			<div class="ui-widget-content dwidth-middle">
				<table class="condform">
					<tbody>
						<tr>
							<td class="ui-state-default td-title">责任区分（技术）</td>
							<td class="td-content" colspan="3">
<% if (step == 1 && powerId == 5) { %>
								<select id="detail.responsibility_of_ptl" id="detail.responsibility_of_ptl" class="ui-widget-content">${defectiveResponsibilityOfPtlOptions}</select>
<% } else { %>
								<span id="detail.responsibility_of_ptl_disp"></span>
<% } %>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">原因分析</td>
							<td class="td-content" colspan="3">
<% if (step == 1 && powerId == 5) { %>
								<textarea name="detail.cause_analysis" class="mm_type" photo="6" id="detail.cause_analysis"></textarea>
<% } else { %>
								<div id="detail.cause_analysis_disp"></div>
<% } %>
							</td>
						</tr>

						<tr>
							<td class="ui-state-default td-title">CAPA频度判断</td>
							<td class="td-content" colspan="3">
<% if (step == 1 && powerId == 5) { %>
								<select name="detail.capa_frequency" id="detail.capa_frequency" class="ui-widget-content">${defectiveCapaFrequencyOptions}</select>
<% } else { %>
								<span id="detail.capa_frequency_disp"></span>
<% } %>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">CAPA重大度判断</td>
							<td class="td-content">
<% if (step == 1 && powerId == 5) { %>
								<select name="detail.capa_major" id="detail.capa_major" class="ui-widget-content">${defectiveCapaMajorOptions}</select>
<% } else { %>
								<span id="detail.capa_major_disp"></span>
<% } %>
							</td>
							<td class="ui-state-default td-title">风险大小等级</td>
							<td class="td-content">
<% if (step == 1 && powerId == 5) { %>
								<select name="detail.capa_risk" id="detail.capa_risk" class="ui-widget-content">${defectiveCapaRiskOptions}</select>
<% } else { %>
								<span id="detail.capa_risk_disp"></span>
<% } %>
							</td>

						</tr>

						<tr <% if (typeFlag != 1) {%>style="display:none;"<%} %> >
							<td class="ui-state-default td-title">波及性判断结果</td>
							<td class="td-content">
<% if (step == 1 && powerId == 5) { %>
								<select name="detail.involving" id="detail.involving" class="ui-widget-content">${defectiveInvolvingOptions}</select>
<% } else { %>
								<span id="detail.involving_disp"></span>
<% } %>
							</td>
							<td class="ui-state-default td-title">波及性判断理由</td>
							<td class="td-content">
<% if (step == 1 && powerId == 5) { %>
								<input type="text" id="detail.involving_reason" name="detail.involving_reason" class="ui-widget-content">
<% } else { %>
								<span id="detail.involving_reason_disp"></span>
<% } %>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">零件定单次数</td>
							<td class="td-content">
<% if (powerId ==7) { %>
								<input type="text" id="detail.occur_times" name="detail.occur_times" class="ui-widget-content">
<% } else { %>
								<span id="detail.occur_times_disp"></span>
<% } %>
							</td>
							<td class="ui-state-default td-title">追加订购者</td>
							<td class="td-content">
								<span id="detail.partial_applyier_name"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">原因分析者</td>
							<td class="td-content">
								<span id="detail.cause_analyst_name"></span>
								<span id="detail.cause_analyst_date"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">原因确认者</td>
							<td class="td-content">
								<span id="detail.cause_confirmer_name"></span>
								<span id="detail.cause_confirmer_date"></span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>

	<div class="ui-widget-content defective_analysis_detail_tabcontent" for="defective_analysis_detail_infoes_filer" style="width:100%;text-align:left;display:none;">
		<div id="defective_analysis_detail_filer_area" style="margin-top:22px;margin-left:9px;">
			<div class="ui-widget-content dwidth-middle">
				<table class="condform">
					<tbody>
						<tr>
							<td class="ui-state-default td-title">对策</td>
							<td class="td-content" colspan="3">
<% if (step == 2 && powerId == 5) { %>
								<textarea name="detail.countermeasures" class="mm_type" photo="3" id="detail.countermeasures"></textarea>
<% } else { %>
								<div id="detail.countermeasures_disp"></div>
<% } %>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">对策立案者</td>
							<td class="td-content">
								<span id="detail.cm_filer_name"></span>
							</td>
							<td class="ui-state-default td-title">对策立案日</td>
							<td class="td-content">
								<span id="detail.cm_filer_date"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">对策确认者</td>
							<td class="td-content">
								<span id="detail.cm_confirmer_name"></span>
							</td>
							<td class="ui-state-default td-title">对策确认日</td>
							<td class="td-content">
								<span id="detail.cm_confirmer_date"></span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>

	<div class="ui-widget-content defective_analysis_detail_tabcontent" for="defective_analysis_detail_infoes_processor" style="width:100%;text-align:left;display:none;">
		<div id="defective_analysis_detail_processor_area" style="margin-top:22px;margin-left:9px;">
			<div class="ui-widget-content dwidth-middle">
				<table class="condform">
					<tbody>
						<tr>
							<td class="ui-state-default td-title">对策实施者</td>
							<td class="td-content">
								<span id="detail.cm_processor_name"></span>
							</td>
							<td class="ui-state-default td-title">对策实施日</td>
							<td class="td-content">
								<span id="detail.cm_processor_date"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">对策实施确认者</td>
							<td class="td-content">
								<span id="detail.cm_proc_confirmer_name"></span>
							</td>
							<td class="ui-state-default td-title">对策实施确认日</td>
							<td class="td-content">
								<span id="detail.cm_proc_confirmer_date"></span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>

	<div class="ui-widget-content defective_analysis_detail_tabcontent" for="defective_analysis_detail_infoes_processor_confirm" style="width:100%;text-align:left;display:none;">
		<div id="defective_analysis_detail_processor_confirm_area" style="margin-top:22px;margin-left:9px;">

			<div class="ui-widget-content dwidth-middle">
				<table class="condform">
					<tbody>
						<tr>
							<td class="ui-state-default td-title">对策效果</td>
							<td class="td-content" colspan="3">
<% if (step == 4 && powerId == 1) { %>
								<textarea name="detail.countermeasure_effects" class="mm_type" photo="3" id="detail.countermeasure_effects"></textarea>
<% } else { %>
								<div id="detail.countermeasure_effects_disp"></div>
<% } %>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">对策效果验证者</td>
							<td class="td-content">
								<span id="detail.cm_effect_verifier_name"></span>
							</td>
							<td class="ui-state-default td-title">对策效果验证日</td>
							<td class="td-content">
								<span id="detail.cm_effect_verifier_date"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">对策效果确认者</td>
							<td class="td-content">
								<span id="detail.cm_effect_confirmer_name"></span>
							</td>
							<td class="ui-state-default td-title">对策效果确认日</td>
							<td class="td-content">
								<span id="detail.cm_effect_confirmer_date"></span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>

	<div class="ui-widget-content defective_analysis_detail_tabcontent" for="defective_analysis_detail_infoes_effect" style="width:100%;text-align:left;display:none;">
		<div id="defective_analysis_detail_effect_area" style="margin-top:22px;margin-left:9px;">
			<div class="ui-widget-content dwidth-middle">
				<table class="condform">
					<tbody>
						<tr>
							<td class="ui-state-default td-title">委托关闭判断</td>
							<td class="td-content" colspan="3">
<% if (step == 5 && (powerId == 2 || powerId == 4)) { %>
								<textarea name="detail.closing_judgment" id="detail.closing_judgment"></textarea>
<% } else { %>
								<div id="detail.closing_judgment_disp"></div>
<% } %>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">关闭判断者</td>
							<td class="td-content">
								<span id="detail.closing_judger_name"></span>
								<span id="detail.closing_judger_date"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">关闭确认者</td>
							<td class="td-content">
								<span id="detail.closing_confirmer_name"></span>
								<span id="detail.closing_confirmer_date"></span>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
	<div class="clear areaencloser"></div>
	<div style="text-align: right">
		<% if (powerId != 0) { %>
		<input type="button" id="okButton" class="ui-button" value="确认">
		<% } %>
		<input type="button" id="cancelButton" class="ui-button" value="取消">
	</div>
</div>
<script type="text/javascript">
$(function() {
	$("#defective_analysis_detail_content input.ui-button").button();
	$("#defective_analysis_detail_infoes, #distributions").buttonset();
	$("#defective_analysis_detail_infoes input:radio").click(function() {
		$("div.defective_analysis_detail_tabcontent").hide();
		var tab = $("div.defective_analysis_detail_tabcontent[for='"+this.id+"']");
		tab.show();
	});
});
</script>
</form>