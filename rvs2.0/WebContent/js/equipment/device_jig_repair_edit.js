var djrEditServicePath = "device_jig_repair_record.do";

var consumHtml = "<input type=\"button\" value=\"选择备件\" class=\"djr_consum_button\">";

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
					$dialogDjrEdit.find("#djr_manage_code").text(retForm.manage_code || "").attr("key", djrrKey);
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
						$dialogDjrEdit.find("#djr_cause_type").disable();
						$("#djr_delphotobutton").hide();
					} else {
						$("#djr_delphotobutton").click(function(){
							var postData = {device_jig_repair_record_key : djrrKey};
	
							$("#djr_photo").hide();
							$.ajax({
								beforeSend : ajaxRequestType,
								async : false,
								url : djrEditServicePath + '?method=delImage',
								cache : false,
								data : postData,
								type : "post",
								dataType : "json",
								success : ajaxSuccessCheck,
								error : ajaxError
							})
						});
					}

					$dialogDjrEdit.find("input.ui-button").button();

					$("#djr_cancelbutton").click(function(){
						$dialogDjrEdit.dialog("close");
					});

					$('#djr_uploadphotobutton').click(function(){
						$('#djr_update_photo').click();
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
					if (resInfo.costs && resInfo.costs.length > 0) {
						var costHtml = "";
						for (var iCost in resInfo.costs) {
							var cost = resInfo.costs[iCost];
							var totalPrice = (cost.price * cost.quantity) || 0;
							var outsourcingPrice = (cost.outsourcing_price || 0);
							costHtml += "<tr><td device_type_id=" + cost.device_type_id +">" + (cost.device_type_name || "-") 
									+ "</td><td>" + cost.model_name + "</td>";
							if (repairCompleted) {
								costHtml += "<td>" + (cost.quantity || "-") + "</td><td>" + (cost.price || "-") 
									+ "</td><td>";
							} else {
								costHtml += "<td><input type='number' class='djr_cost_quantity' value='" + (cost.quantity || 0) + "'></td><td>" 
									+ "<input type='number' class='djr_cost_price' value='" + (cost.price || 0) 
									+ "'></td><td>";
							}
							costHtml += totalPrice + "</td><td><input type='number' class='djr_cost_outsourcing_price' value='" + outsourcingPrice + "'>"
									+ "</td><td>" + (outsourcingPrice ? (outsourcingPrice - totalPrice) : "-") + "</td></tr>";
						}
						$("#djr_costs table tbody").html(costHtml);
					} else {
						var costHtml = "";

						if (repairCompleted) {
							costHtml += "<tr><td>-</td><td>-</td><td>-</td>"
								+ "<td>-</td><td>-</td><td><input type='number' class='djr_cost_outsourcing_price'>"
								+ "</td><td></td></tr>";
						} else {
							costHtml += "<tr><td></td><td></td><td><input type='number' class='djr_cost_quantity'></td>"
								+ "<td><input type='number' class='djr_cost_price'></td><td></td><td><input type='number' class='djr_cost_outsourcing_price'>"
								+ "</td><td></td></tr>";
						}

						$("#djr_costs table tbody").html(costHtml);
					}
					if (!repairCompleted) {
						$("#djr_costs tbody").find("tr > td:eq(0), tr > td:eq(1)").click(selectCost);
					}

					$("#djr_costs").find(".djr_cost_quantity, .djr_cost_price, .djr_cost_outsourcing_price").change(calcCost);

					$("#djr_photo").on("error", function(){
						$("#djr_photo").hide();
					});

					$("#djr_photo").show()
						.attr("src", "http://" + document.location.hostname + "/photos/dj_repair/" + djrrKey + "?_s=" + new Date().getTime());
					if (typeof(showPhoto)==="function") {
						$("#djr_photo").css("cursor", "pointer").click(showPhoto);
					}

					$("#djr_update_photo").parent().on("change", "#djr_update_photo", callDjrUploadPhoto);

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
		object_name : $("#djr_object_name").text(),
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

	var $trTodo = $("#djr_costs tbody").find("tr");// TODO
	var $deviceTypeTd = $trTodo.children(":eq(0)");

	postData.device_type_id = $deviceTypeTd.attr("device_type_id");
	postData.device_type_name = $deviceTypeTd.text();
	postData.model_name = $trTodo.children(":eq(1)").text();

	var $tdQuantity = $trTodo.children().eq(2);
	var $tdPrice = $trTodo.children().eq(3);
	var quantity = parseInt($tdQuantity.children("input").val() || $tdQuantity.text() || 0);
	var price = parseFloat($tdPrice.children("input").val() || $tdPrice.text() || 0);

	if (price) postData.price = price;
	if (quantity) postData.quantity = quantity;
	postData.outsourcing_price = $trTodo.find(".djr_cost_outsourcing_price").val();

	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : djrEditServicePath + '?method=doRepair',
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

var callDjrUploadPhoto = function(){
	if(typeof($.ajaxFileUpload)==="undefined") {
		loadJs(
			"js/ajaxfileupload.js",
			djrUploadPhoto
		);
	} else {
		djrUploadPhoto();
	}
}

var djrUploadPhoto = function(){

	var djrr_key = $("#djr_manage_code").attr("key");
	$.ajaxFileUpload({
		url : djrEditServicePath + "?method=sourceImage", // 需要链接到服务器地址
		secureuri : false,
		data: {device_jig_repair_record_key : djrr_key},
		fileElementId : 'djr_update_photo', // 文件选择框的id属性
		dataType : 'json', // 服务器返回的格式
		success : function(responseText, textStatus) {
			var resInfo = $.parseJSON(responseText);	

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				$("#djr_update_photo").val("");
				
				$("#djr_photo").show()
					.attr("src", "http://" + document.location.hostname + "/photos/dj_repair/" + djrr_key + "?_s=" + new Date().getTime()).show();
			}
		}
	});
}

var selectCost = function(){
	var $tr = $(this).closest("tr");
	var device_type_id = $tr.children("td:eq(0)")
	warningConfirm("是否使用备件仓库更换部件？", getSpareList, inputTypeModel, null, "使用备件", "使用采购或其他来源");
}

var inputTypeModel = function() {
	var $dialogConsumInput = $("#device_jig_repair_consum_input");
	if ($dialogConsumInput.length == 0) {
		$("body").append("<div id='device_jig_repair_consum_input'/>");
		$dialogConsumInput = $("#device_jig_repair_consum_input");
	}

	$dialogConsumInput.html("<tr><td class='ui-state-default td-title'>名称</td><td><input type='text'></td><td class='ui-state-default td-title'>型号</td><td><input type='text'></td></tr>");

	$dialogConsumInput.dialog({
		title : "输入更换/替换/修理品",
		width : 'auto',
		height : 'auto',
		resizable : false,
		modal : true,
		buttons : {"确认":function(){
			var $trTodo = $("#djr_costs tbody").find("tr");// TODO
			var $deviceTypeTd = $trTodo.children(":eq(0)");
			$deviceTypeTd.attr("device_type_id", "00000000000");
			$deviceTypeTd.text($dialogConsumInput.find("td:eq(1) input").val());
			$trTodo.children(":eq(1)").text($dialogConsumInput.find("td:eq(3) input").val());
			$dialogConsumInput.dialog('close');
		},"关闭" : function(){$dialogConsumInput.dialog('close');}}
	});
}

var calcCost = function(){
	var $tr = $(this).closest("tr");
	var outsourcingPrice = parseFloat($tr.find(".djr_cost_outsourcing_price").val(), 0);
	var $tdQuantity = $tr.children().eq(2);
	var $tdPrice = $tr.children().eq(3);
	var quantity = parseInt($tdQuantity.children("input").val() || $tdQuantity.text() || 0);
	var price = parseFloat($tdPrice.children("input").val() || $tdPrice.text() || 0);

	var totalPrice = quantity * price;
	if (totalPrice > 0) {
		$tr.children().eq(4).text(totalPrice.toFixed(2));
	} else {
		$tr.children().eq(4).text("-");
		totalPrice = 0;
	}

	if (outsourcingPrice > 0) {
		$tr.children().eq(6).text((outsourcingPrice - totalPrice).toFixed(2));
	} else {
		$tr.children().eq(6).text("-");
	}
}

var localSpareList = {};

var getSpareList = function(){
	var data ={
		"device_spare_type":"2"
	};
	var spareServicePath = "device_spare.do";
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : spareServicePath + '?method=search',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrObj, textStatus) {
			var resInfo = $.parseJSON(xhrObj.responseText);
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				localSpareList = resInfo.spareList;
				$("#add_spare_device_type_name").val("");
				$("#add_spare_model_name").val("");
				showSparelist(localSpareList);
			}
		}
	});
}

var selectFromSpare = function(rowData) {
	var $trTodo = $("#djr_costs tbody").find("tr");// TODO
	var $deviceTypeTd = $trTodo.children(":eq(0)");
	$deviceTypeTd.attr("device_type_id", rowData.device_type_id);
	$deviceTypeTd.text(rowData.device_type_name);
	$trTodo.children(":eq(1)").text(rowData.model_name || "");
	$trTodo.children(":eq(3)").children("input").val(rowData.price || "").trigger("change");
}