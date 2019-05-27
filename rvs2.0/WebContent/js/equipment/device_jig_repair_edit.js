var djrEditServicePath = "device_jig_repair_record.do";

var showDjrEdit = function(device_jig_repair_record_key, check_unqualified_record_key){

	var detailMethod = "showDetail";
	if (!device_jig_repair_record_key) {
		detailMethod = "doCheckUnqualified"
	}

	var postData = {
		device_jig_repair_record_key : device_jig_repair_record_key,
		check_unqualified_record_key : check_unqualified_record_key
	}

	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : djrEditServicePath + '?method=' + detailMethod,
		cache : false,
		data : postData,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus) {
			var resInfo = $.parseJSON(xhrobj.responseText);
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				var retForm = resInfo.retForm;
				var $dialogDjrEdit = $("#device_jig_repair_edit");
				if ($dialogDjrEdit.length == 0) {
					$("body").append("<div id='device_jig_repair_edit'/>");
					$dialogDjrEdit = $("#device_jig_repair_edit");
				}

				$dialogDjrEdit.load("device_jig_repair_record.do?method=detail", function(responseText, textStatus, XMLHttpRequest) {
					var djrrKey = retForm.device_jig_repair_record_key;
					var objectType = retForm.object_type || 9;
					var repairCompleted = retForm.repair_complete_time;

					$dialogDjrEdit.find("#djr_line_name").text(retForm.line_name || "");
					$dialogDjrEdit.find("#djr_submitter_name").text(retForm.submitter_name);
					$dialogDjrEdit.find("#djr_submit_time").text(retForm.submit_time);
					$dialogDjrEdit.find("#djr_manage_code").text(retForm.manage_code || "");
					$dialogDjrEdit.find("#djr_object_name").text(retForm.object_name || "");
					$dialogDjrEdit.find("#djr_model_name").text(retForm.model_name || "");
					$dialogDjrEdit.find("#djr_phenomenon").val(retForm.phenomenon);
					$dialogDjrEdit.find("#djr_maintainer_name").text(retForm.maintainer_name)
						.attr("ids", retForm.maintainer_id || "");
					$dialogDjrEdit.find("#djr_fault_causes").val(retForm.fault_causes || "");
					$dialogDjrEdit.find("#djr_cause_type").val(retForm.cause_type).select2Buttons();
					$dialogDjrEdit.find("#djr_countermeasure").val(retForm.countermeasure || "");
					$dialogDjrEdit.find("#djr_comment").val(retForm.comment || "");
					$dialogDjrEdit.find("#djr_line_break").val(retForm.line_break || "");
					$dialogDjrEdit.find("#djr_spare_supplement").val(retForm.spare_supplement || "");
					$dialogDjrEdit.find("#djr_additional_infect_feature").val(retForm.additional_infect_feature || "");
					$dialogDjrEdit.find("#djr_latent_trouble").val(retForm.latent_trouble || "");
					if (repairCompleted) {
						$dialogDjrEdit.find("#djr_phenomenon, #djr_fault_causes").attr("readonly", true).disable();
						$("#djr_delphotobutton").hide();
					}

					$dialogDjrEdit.find("input.ui-button").button();

					$("#djr_cancelbutton").click(function(){
						$dialogDjrEdit.dialog("close");
					});

					$("#djr_submitbutton").click(function(){
						if (!repairCompleted) {
							warningConfirm("修理是否完成？", 
								function(){doDjrUpdate(djrrKey, true);},
								function(){doDjrUpdate(djrrKey, false);},
								null, "修理完成", "暂未完成"
								);
						} else {
							doDjrUpdate(djrrKey, false);
						}
					});

					if (objectType === "2") {
						$("#djr_costs td:eq(0)").text("治工具损耗&修理费用");
						$("#djr_costs table thead td:eq(5)").html("治工具新日本<br>订购报价");
					}
					if (resInfo.costs) {
						var costHtml = "";
						for (var iCost in resInfo.costs) {
							var cost = resInfo.costs[iCost];
							var totalPrice = (cost.price * cost.quantity) || 0;
							var outsourcingPrice = (cost.outsourcing_price || 0);
							costHtml += "<tr><td device_type_id=" + cost.device_type_id +">" + (cost.device_type_name || "-") 
								+ "</td><td>" + cost.model_name + "</td><td>" + (cost.quantity || "-") + "</td><td>" + (cost.price || "-") 
								+ "</td><td>" + totalPrice + "</td><td>" + outsourcingPrice
								+ "</td><td>" + (outsourcingPrice ? (outsourcingPrice - totalPrice) : "-") + "</td></tr>";
						}
						$("#djr_costs table tbody").html(costHtml);
					}

					$("#djr_photo").on("error", function(){
						$("#djr_photo").hide();
					});

					$("#djr_photo").show()
						.attr("src", "http://" + document.location.hostname + "/photos/dj_repair/" + djrrKey + "?_s=" + new Date().getTime());

					$dialogDjrEdit.dialog({
						title : "设备工具维修",
						width : 1080,
						height : 'auto',
						resizable : false,
						modal : true
					});
				});
			}
		}
	});
}

var doDjrUpdate = function(key, isRepairFinish) {

	var phenomenon = $("#djr_phenomenon").val();
	var fault_causes = $("#djr_fault_causes").val();
	var cause_type = $("#djr_cause_type").val();

	var postData = {device_jig_repair_record_key : key,
		maintainer_id : $("#djr_maintainer_name").attr("ids"),
		countermeasure : $("#djr_countermeasure").val(),
		comment : $("#djr_comment").val(),
		line_break : $("#djr_line_break").val(),
		spare_supplement : $("#djr_spare_supplement").val(),
		additional_infect_feature : $("#djr_additional_infect_feature").val(),
		latent_trouble : $("#djr_latent_trouble").val()
	}

	if (isRepairFinish) {
		if (!phenomenon) {
			errorPop("请输入现象描述。");
			return;
		}
		if (!fault_causes) {
			errorPop("请输入故障原因。");
			return;
		}
		if (!cause_type) {
			errorPop("请输入原因分类。");
			return;
		}
		postData.repair_complete_time = '2000-01-01 01:01:01';
	}

	if (!$("#djr_phenomenon").attr("disabled")) {
		postData.phenomenon = phenomenon;
	}
	if (!$("#djr_fault_causes").attr("disabled")) {
		postData.fault_causes = fault_causes;
	}
	if ($("#djr_cause_type").is(":visible")) {
		postData.cause_type = cause_type;
	}

	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doRepair',
		cache : false,
		data : postData,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus) {
			var resInfo = $.parseJSON(xhrobj.responseText);
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				if(typeof(findit) === "function") findit();
				$("#device_jig_repair_edit").dialog('close');
			}
		}
	})

}