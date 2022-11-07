var setMpaObj = (function() {


var lightRepairs=[];
var lockPat = false;
var chosedPat = {};
var chosedPos = {};
var lf_material_id = null;
var isLightHandled = false;
var correlated_level = 9;

var showeditLightHandleComplete = function(xhrobj){
	var resInfo = $.parseJSON(xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages(null, resInfo.errors);
	} else {
		$("#pa_main").flowchart("fill", resInfo.processAssigns);

		_addCellPos($("#pa_main"), resInfo);

		correlated_level = 9;
		$("#light_repair_process .subform tbody tr.ui-state-active").each(function(index,ele){
			var $tr = $(ele);

			if (!lockPat) {
				$tr.removeClass("ui-state-active");
				$tr.addClass("unact");
			}
			if ($tr.attr("level")) {
				var c_level = parseInt($tr.attr("level"));
				if (correlated_level > c_level) {
					correlated_level = c_level;
				}
			}
		});

		if (lockPat) {
			lockPat = false;
			changeFlow();
			showResult(95);
		} else {
			chosedPat = {};
			chosedPos = {};
		}

		$("#pa_main .pos[posid] span").on("click",function(){
			var $span = $(this);
			if ($span.hasClass("suceed")) {
				return;
			} else {
				var pos_id = $span.parent().attr("code");
				if ($span.hasClass("point")) {
					$span.removeClass("point");
					chosedPos[pos_id] = 0;
				} else {
					$span.addClass("point");
					chosedPos[pos_id] = 1;
				}
			}
			showResult(3.3);
		});

		showResult(3);
		isLightHandled = true;

		$("#pa_main").parent().trigger("scroll").scrollTop(0);
	}

};

var getDetail_ajaxSuccess=function(xhrobj){
	var resInfo = $.parseJSON(xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages(null, resInfo.errors);
	} else {

		$("#ref_template").html($("#paOptions").val());
		$("#ref_template").select2Buttons();

		lightRepairs = resInfo.lightFixs;//小修理标准编制
		var processAssigns = resInfo.processAssigns;//维修对象独有修理流程
		var materialForm = resInfo.materialForm;

		//工位
		$("#pa_main").html("");
		$("#pa_main").flowchart({},{editable:false, selections: resInfo.list});

		if(materialForm.pat_id!=null && materialForm.pat_id!=''){
			$("#ref_template").val(materialForm.pat_id).trigger("change");
		}else{
			$("#ref_template").val("").trigger("change");
		}

		//生产小修理流程工位内容
		var patContent = "";
		for (var index in lightRepairs) {
			var lightRepair = lightRepairs[index];
			patContent += "<tr lf_id='"+ lightRepair.light_fix_id
					+ (lightRepair.correlated_pat_id ? 
						("' pat_id='" + lightRepair.correlated_pat_id + "' level='" + lightRepair.correlated_level) 
						: "") +"' class='unact'>"
					+ "<td>" + lightRepair.activity_code + "</td>"
					+ "<td>" + lightRepair.description + "</td>"
					+ "</tr>";
		};

		$("#light_repair_process .subform tbody").html(patContent);

		var timeoutFunc = function(){

			if (!isLightHandled) {
				setTimeout(timeoutFunc, 310);
				return;
			}
			var materialLightFixs = resInfo.materialLightFixs;//维修对象选用小修理
			$("#light_repair_process .subform tbody tr").removeClass("ui-state-active").addClass("unact");
			for(var index in materialLightFixs){
				var materialLightFix = materialLightFixs[index];
				var lf_id = materialLightFix.light_fix_id;
				chosedPat[lf_id] = 1;
				$("#light_repair_process .subform tbody tr").each(function(){
					var $tr = $(this);
					if(lf_id==$tr.attr("lf_id")) $tr.addClass("ui-state-active").removeClass("unact");
				});
			}

			changeFlow();
			$("#pa_main div.pos").each(function(index,ele){
				var $div = $(ele);
				var code = $div.attr("code");
				var $span = $div.find("span");
				for(var i=0;i<processAssigns.length;i++){
					var obj = processAssigns[i];
					if(code == obj.position_id && !$span.hasClass("suceed")){
						$span.addClass("point");
						chosedPos[code]=1;
						break;
					}
				}
			});

			showResult(4);
		}

		if (isLightHandled) {
			timeoutFunc();
		} else {
			setTimeout(timeoutFunc, 310);
		}

		//小修理流程工位TR单击事件
		$("#light_repair_process .subform tbody tr").click(function(){
			var $tr = $(this);
			var lf_id = $tr.attr("lf_id");

			if ($tr.hasClass("ui-state-active")) {
				$tr.removeClass("ui-state-active");
				$tr.addClass("unact");
				chosedPat[lf_id] = 0;
				changeFlow();
				showResult(5.0);
			}else{
				$tr.addClass("ui-state-active");
				$tr.removeClass("unact");
				chosedPat[lf_id] = 1;

				var correlated_pat_id = $tr.attr("pat_id");
				if (correlated_pat_id) {
					var $patTr = $("#ref_template option[value='" + correlated_pat_id + "']");

					var c_level = parseInt($tr.attr("level"));

					if ($patTr.length > 0 && !$patTr.attr("selected")
						&& c_level < correlated_level) {
						var curRefText = $("#ref_template option:selected").text();
						if (curRefText) {
							curRefText = "维修项目：" + $tr.children("td:eq(0)").text() + "具有关联的参考流程，是否将流程切换到【" + $patTr.text() + "】？"
								+ "<br>（现有流程【" + curRefText + "】）";
							warningConfirm(curRefText,
								function() {
									lockPat = true;
									$("#ref_template").val(correlated_pat_id).trigger("change");
									if (correlated_level > c_level) correlated_level = c_level;
								},
								function() {
									changeFlow();
									showResult(5.3);
								}, "是否切换流程", "切换后重新选择", "保持现有流程继续"
							)
						} else {
							$("#ref_template").val(correlated_pat_id).trigger("change");
							if (correlated_level > c_level) correlated_level = c_level;
						}

					} else {
						if (correlated_level > c_level) correlated_level = c_level;

						changeFlow();
						showResult(5.2);
					}
				} else {
					changeFlow();
					showResult(5.1);
				}
			}
		});

		$("#light_fix_dialog").dialog({
			title : "中小修理维修内容流程设定",
			modal : true,
			width: 1240,
			height : 660,
			resizable: false,
			buttons:{
				"确定":function(){
					update_material_process_assign();
				},
				"取消":function(){
					$(this).dialog("close");
				}
			}
		});

		$("#light_fix_dialog").show();
	}

};

var _addCellPos = function($pa, resInfo){
	//对象机型
	var $firstpos = $pa.find(".pos:first");
	if (resInfo.isLgModel) {
		var $303Pos = $pa.find(".pos[code='60']");
		if ($firstpos.length && !$303Pos.length) {
			var $realFirstpos = $firstpos;
			if ($firstpos.is(".chartarea")) {
				$realFirstpos = $firstpos.find(".pos:first");
			}
			// 增加303工位选择
			$303Pos = $('<div code="60" class="pos"><span>303\nLG 玻璃更换</span></div>');
			$303Pos.attr({
				"posid" : $realFirstpos.attr("posid"),
				"prevcode" : $realFirstpos.attr("prevcode"),
				"nextcode" : $realFirstpos.attr("nextcode")
			});
			$realFirstpos.after($303Pos);
			if (!$realFirstpos.parent().hasClass("just-multi")) {
				$realFirstpos.parent().addClass("just-multi");
			}
		}
	}
	if (resInfo.isCcdModel) {
		var $302Pos = $pa.find(".pos[code='25']");
		if (!$302Pos.length) {
			$302Pos = $('<div class="edgeposition"><div class="just"><div code="25" posid="25" prevcode="0" class="pos"><span>302\nCCD 盖玻璃更换</span></div></div>');
			var nextcode="9999999";
			if ($firstpos.length) {
				nextcode = $firstpos.attr("code");
			}
			$302Pos.find(".pos").attr("nextcode", nextcode);

			$pa.find(".pos[posid='" + $firstpos.attr("code") + "']")
				.attr("prevcode", "25");
			// 增加302工位选择
			$pa.prepend($302Pos);
		}
	}
	if (resInfo.isCcdLineModel) {
		var $304Pos = $pa.find(".pos[code='66']");
		var $331Pos = $pa.find(".pos[code='28']");
		if ($331Pos.length && !$304Pos.length) {
			// 增加304工位选择
			$304Pos = $('<div code="66" class="pos"><span>304\nCCD 线更换</span></div>');
			$304Pos.attr({
				"posid" : $331Pos.attr("posid"),
				"prevcode" : $331Pos.attr("prevcode"),
				"nextcode" : $331Pos.attr("nextcode")
			});
			$331Pos.after($304Pos);
			if (!$331Pos.parent().hasClass("just-multi")) {
				$331Pos.parent().addClass("just-multi");
			}
		}
	}
}

var changeFlow = function(){
	var resultPos = {};
	for (var lf_id in chosedPat) {
		if (chosedPat[lf_id] == 1) {
			for (var iLightRepair in lightRepairs) {
				var lightRepair = lightRepairs[iLightRepair];
				if (lf_id == lightRepair.light_fix_id) {
					for (var iprocesses in lightRepair.position_list) {
						var process = lightRepair.position_list[iprocesses];
						resultPos[process] = 1;
					}
				}
			}
		}
	};
	$(".pos[posid]").find("span").removeClass("suceed").removeClass("point");//清楚所有样式

	for (var process in resultPos) {
		$(".pos[code="+ process +"]").find("span").addClass("suceed");
	};

	for (var process in chosedPos) {
		if (chosedPos[process] == 1)
		$(".pos[code="+ process +"]").find("span:not('.suceed')").addClass("point");
	}
};

var update_material_process_assign=function(){
	var data={
		"material_id": lf_material_id,
		"pad_id":$("#ref_template").val()
	};

	var i=0;
	for (var lf_id in chosedPat) {
		if (chosedPat[lf_id] == 1) {
			data["material_light_fix.light_fix_id[" + i + "]"] = lf_id;
			i++;
		}
	}

	//工位
	var count=0;
	var positionPost = [];
	getProcessPost($("#pa_main > .edgeposition"), positionPost);

	var line_id = 9000000;

	getProcessRequest(positionPost, data, line_id, count);

	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'material_process_assign.do?method=doUpdateMaterialProcessAssign',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus){
			var resInfo = $.parseJSON(xhrobj.responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				$("#light_fix_dialog").dialog("close");
			}
		}
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

var showResult= function(intt) {

	var $showTarget = $("#light_repair_record > div");
	var processText = "";
	$("#light_repair_process .ui-state-active").each(function(idx, ele){
		processText += "; " + $(ele).find("td:eq(1)").text();
	});

	var positionText = getProcessText($("#pa_main > .edgeposition"));

	var showText = "";
	if (processText.length) {
		showText += "所选择的中小修理项目为：" + processText.substring(2);
		if (positionText.length) {
			showText += "<BR>作业流程为：" + positionText;
		}
	} else {
		if (positionText.length) {
			showText += "作业流程为：" + positionText;
		}
	}
	$showTarget.html(showText);
}

var getProcessText = function($edgeposition, allItem) {
	var positionText = "";
	$edgeposition.each(function(idx, ele){
		var $pos = $(ele).children(".just").children(".pos");
		var eleText = null;
		if ($pos.length == 1) {
			eleText = getProcessCodeFromPos($pos, allItem);
		} else if ($pos.length > 1) {
			eleText = "";
			$pos.each(function(ipos, epos){
				var $epos = $(epos);
				if ($epos.is(".chartarea")) {
					var processCode = getProcessText($epos.children(".edgeposition"), allItem);
					if (processCode) eleText += "=" + processCode;
				} else {
					var processCode = getProcessCodeFromPos($epos, allItem);
					if (processCode) eleText += "=" + processCode;
				}
			});

			if (eleText) {
				if (eleText.length == 4) {
					eleText = eleText.substring(1);
				} else {
					eleText = "[" + eleText.substring(1) + "]";
				}
			}
		}
		if (eleText) {
			positionText += "->" +eleText;
		}
	});
	if (positionText) {
		if (positionText.indexOf("->") < 0 && positionText.indexOf("[") == 0) {
			positionText = positionText.substring(1, positionText.length - 1);
		} else if (positionText.indexOf("->") == 0) {
			positionText = positionText.substring(2);
		}
	}

	return positionText;
}

var getProcessPost = function($edgeposition, positionPost, rework) {

	$edgeposition.each(function(idx, ele){
		var $pos = $(ele).children(".just").children(".pos");

		if ($pos.length == 1) {
			if (!rework && $pos.children(".point,.suceed").length == 0) return;
			if (rework && $pos.children(".point,.suceed,.rework").length == 0) return;
			positionPost.push($pos);
		} else if ($pos.length > 1) {
			var multiPositionPost = [];
			$pos.each(function(ipos, epos){
				var $epos = $(epos);
				if ($epos.is(".chartarea")) {
					var subPositionPost = [];
					getProcessPost($epos.children(".edgeposition"), subPositionPost, rework);
					if (subPositionPost.length == 1) {
						multiPositionPost.push(subPositionPost[0]);
					} else if (subPositionPost.length > 1) {
						var subposkey = $epos.attr("code");
						multiPositionPost.push({subposkey : subposkey, arr : subPositionPost});
					}
				} else {
					if (!rework && $epos.children(".point,.suceed").length == 0) return;
					if (rework && $epos.children(".point,.suceed,.rework").length == 0) return;
					multiPositionPost.push($epos);
				}
			});
			if (multiPositionPost.length == 1) {
				positionPost.push(multiPositionPost[0]);
			} else if (multiPositionPost.length > 1) {
				positionPost.push(multiPositionPost);
			}
		}
	});

	return positionPost;
}

var getProcessRequest = function(positionPost, data, line_id, count) {
	var rcd = {
		sign_position_id : null,
		prev_position_id : null
	}

	for (var il in positionPost) {
		var layer = positionPost[il];
		if (Array.isArray(layer)) {
			for (var ia in layer) {
				var sub_layer = layer[ia];
				if (Array.isArray(sub_layer)) {
					for (var isa in sub_layer) {
						if (sub_layer[isa] instanceof $) {
							setMpaData(sub_layer[isa].attr("code"),
								line_id, count, rcd, true, data);
							count++;
						}
					}
				} else if (sub_layer.subposkey) {
					setMpaData(sub_layer.subposkey,
						line_id, count, rcd, true, data);
					count++;
					count = getProcessRequest(sub_layer.arr, data, sub_layer.subposkey, count);
				} else if (sub_layer instanceof $) {
					setMpaData(sub_layer.attr("code"),
						line_id, count, rcd, true, data);
					count++;
				}
			}
			rcd.prev_position_id = rcd.sign_position_id;
			rcd.sign_position_id = null;
		} else if (layer.subposkey){ // 第一层单支线不需要记录支线
			for (var il in layer.arr) {
				var item = layer.arr[il];
				if (Array.isArray(item)) {
					for (var isa in item) {
						if (item[isa] instanceof $) {
							setMpaData(item[isa].attr("code"),
								line_id, count, rcd, true, data);
							count++;
						}
					}
					rcd.prev_position_id = rcd.sign_position_id;
					rcd.sign_position_id = null;
				} else if (item instanceof $) {
					setMpaData(item.attr("code"),
						line_id, count, rcd, false, data);
					rcd.prev_position_id = code;
					count++;
				}
			}
		} else if (layer instanceof $) {
			code = layer.attr("code");
			setMpaData(layer.attr("code"),
				line_id, count, rcd, false, data);
			rcd.prev_position_id = code;
			count++;
		}
	}
	return count;
}

var setMpaData = function(code, line_id, count, rcd, checkSign, data) {
	data["material_process_assign.line_id[" + count + "]"] = line_id;
	data["material_process_assign.position_id[" + count + "]"] = code;
	data["material_process_assign.sign_position_id[" + count + "]"] = (rcd.sign_position_id || code);
	data["material_process_assign.prev_position_id[" + count + "]"] = (rcd.prev_position_id || 0);

	if (checkSign) {
		if (rcd.sign_position_id == null) {
			rcd.sign_position_id = code;
			if (rcd.prev_position_id) {
				for (var ip = 0; ip < count; ip++) {
					if (data["material_process_assign.sign_position_id[" + ip + "]"] == rcd.prev_position_id) {
						data["material_process_assign.next_position_id[" + ip + "]"] = code;
					}
				}
			}
		}
	} else {
		if (rcd.prev_position_id) {	
			for (var ip = 0; ip < count; ip++) {
				if (data["material_process_assign.sign_position_id[" + ip + "]"] == rcd.prev_position_id) {
					data["material_process_assign.next_position_id[" + ip + "]"] = code;
				}
			}
		}
	}
}

var getProcessCodeFromPos = function($pos, allItem) {
	if (!allItem &&
		$pos.children(".point,.suceed,.rework").length == 0) return "";

	var posText = $pos.text();
	if (posText.indexOf("\n") >= 0) posText = posText.split("\n")[0];
	return posText;
}

return {
	initDialog : function($light_fix_dialog, arr_material_id, arr_level, arr_model_id, allcheckable){
		isLightHandled = false;

		lf_material_id = arr_material_id;

		$light_fix_dialog.load("widgets/light_fix.jsp", function(responseText, textStatus, XMLHttpRequest) {

			$(this).hide();

			if (allcheckable) {
				var $allpos = $("<tr><td colspan='2'><input type='button' type='ui-button' id='ref_allpos' value='选择全部工位'></td></tr>");
				$allpos.find("#ref_allpos").button().click(function(){
					$(".pos span").each(function(idx, ele){
						var $span = $(ele);
						if ($span.hasClass("suceed")) {
							return;
						} else {
							var pos_id = $span.parent().attr("code");
							if ($span.hasClass("point")) {
							} else {
								$span.addClass("point");
								chosedPos[pos_id] = 1;
							}
						}
					});

					showResult(1);
				});
				$("#ref_template").parents("table").eq(0).append($allpos);
			}

			lightRepairs=[];
			chosedPat = {};
			chosedPos = {};
			var data={
				"material_id": lf_material_id,
				"level" : arr_level
			}

			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url :'material_process_assign.do?method=getDetail',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : getDetail_ajaxSuccess
			});

			//设定维修流程
			$("#ref_template").change(function(){
				if (this.value === "") {
					$("#pa_main").html("");
					$("#light_repair_process .subform tbody tr").each(function(index,ele){
						var $tr = $(ele);

						$tr.removeClass("ui-state-active");
						$tr.addClass("unact");
					});
					chosedPat = {};
					chosedPos = {};
					correlated_level = 9;
					showResult(1.1);
				}else {
					var data = {
						"id" : this.value,
						lf_model_id : arr_model_id
					};

					// Ajax提交
					$.ajax({
						beforeSend : ajaxRequestType,
						async : true,
						url : 'materialFact.do?method=getPa',
						cache : false,
						data : data,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : showeditLightHandleComplete
					});
				}
			});
	
		});
	}
	,logger : function(){
		console.log(lightRepairs);
		console.log(chosedPat);
		console.log(chosedPos);
		console.log(lf_material_id);
		console.log(isLightHandled);
		console.log(correlated_level);
	}
	,postData : function($pa_main, data, rework){
		var count=0;
		var positionPost = [];
		getProcessPost($pa_main.children(".edgeposition"), positionPost, rework);
	
		var line_id = 9000000;
	
		getProcessRequest(positionPost, data, line_id, count);
	}
	,postText : function($pa_main){
		return getProcessText($pa_main.children(".edgeposition"), true);
	}
	,postSelText : function($pa_main){
		return getProcessText($pa_main.children(".edgeposition"), false);
	}
	,addCellPos : _addCellPos 
}
})();
