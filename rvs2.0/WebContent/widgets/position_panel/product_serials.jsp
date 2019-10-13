<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<div id="serials_area">
<style>
#serials_area #products_area {
	height: 105px;
}
#serials_area #products_area > div{
	float: left;
	width: 80px;
	padding-top: 10px;
	margin-left:40px;
	text-align:center;
	position: relative;
}
#serials_area #products_area > div > * {
	display: block;
	width: 100%;
}
#serials_area #products_area > div > model {
	box-sizing: border-box;
	border: 1px solid gray;
}
#serials_area #products_area > div > serial {
	background : gray;
	color: white;
}
#serials_area #products_area > div > printer {
	position: absolute;
	width: 20px;
	height: 20px;
	line-height: 20px;
	top: 2px;
	right: -20px;
	background-color: yellow;
	cursor: zoom-in;
	font-family: 'icomoon';
	content: "\47";
}
#serials_area #products_area > div[ticket="printed"] > printer {
	background-color: lightgreen;
}
</style>
<script type="text/javascript">
var showSerialNos = function(serialNos) {
	var $products_area = $("#products_area");
	var productsAreaHtml = "";
	for(var i in serialNos) {
		var serialItem = serialNos[i];
		productsAreaHtml += 
			"<div" + (serialItem.material_id ? (" material_id='" + serialItem.material_id + "'") : "")
			+ (serialItem.ticket_flg ? (" ticket='printed'") : "")
			+ (serialItem.inline_time ? "inline" : "") + ">"
			+ "<model>" + (serialItem.model_name || "-") + "</model><serial>" + serialItem.serial_no + "</serial><printer>G</printer></div>";
	}
	$products_area.html(productsAreaHtml);
}

$(function() {
	$("#products_area").on("click", "printer", function(){
		var postData = {};
		var $thisParent = $(this).parent();
		postData["materials.material_id[0]"] = $thisParent.attr("material_id");
		postData["materials.serial_no[0]"] = $thisParent.children("serial").text();
		postData["materials.model_name[0]"] = $thisParent.children("model").text();
		if (!postData["materials.model_name[0]"] || postData["materials.model_name[0]"] === "-") {
			errorPop("请设定产品的型号");
			return;
		}

		doPrintProductTickets(postData, $thisParent);
	});

var printGroup = function() {
	var postData = {};
	var ii = 0;
	var break_flg = false;

	$("#products_area > div").each(function(idx, ele){
		var $ele = $(ele);
		if (!break_flg && !$ele.attr("inline")) {
			
			postData["materials.material_id["+ ii +"]"] = $ele.attr("material_id");
			postData["materials.serial_no["+ ii +"]"] = $ele.children("serial").text();
			var thisModel = $ele.children("model").text();
			if (!thisModel || thisModel === "-") {
				errorPop("请设定产品的型号");
				break_flg = true;
			}
			postData["materials.model_name["+ ii +"]"] = thisModel;

			ii ++;
		}
	});

	if (!break_flg) {
		doPrintProductTickets(postData);
	}
}
var setNewProductModel = function() {
	var $setDialog = $("#ps_set_model");
	if ($setDialog.length == 0) {
		$("body").append("<div id='ps_set_model'></div>");
		$setDialog = $("#ps_set_model");
	}

	$setDialog.html("<select>"+ $("#input_model_id").html() + "</select>");
	var $selector = $setDialog.children("select");
	$selector.children().eq(0).attr("selected", true);
	$selector.select2Buttons();

	$setDialog.dialog({
		modal : true,
		resizable:false,
		width : 'auto',
		title : "选择产品型号",
		closeOnEscape: false,
		buttons : {
		"确认" : function(){
			var selectModel = $setDialog.children("select").val();
			if (selectModel) {
				var postData = {model_id : selectModel};

				// Ajax提交
				$.ajax({
					beforeSend: ajaxRequestType, 
					async: false, 
					url: 'material.do?method=doSetNewProductModel', 
					cache: false, 
					data: postData, 
					type: "post", 
					dataType: "json", 
					success: ajaxSuccessCheck, 
					error: ajaxError, 
					complete:  function(xhrobj, textStatus){
						// 读取JSON
						var resInfo = $.parseJSON(xhrobj.responseText);

						if (resInfo.errors.length > 0) {
							// 共通出错信息框
							treatBackMessages(null, resInfo.errors);
						} else {
							var selectModelName = $selector.children("option:selected").text();

							$("#products_area div").each(function(){
								var $model = $(this).children("model")
								if (!$model.text() || $model.text() === "-") {
									$model.text(selectModelName);
								}
							});
							$setDialog.dialog("close");
						}
					}
				});
			}
		}, "取消": function(){
			$setDialog.dialog("close");
		}}
	});
};

var doPrintProductTickets = function(postData, $ele){
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: false, 
		url: 'material.do?method=doPrintProductTickets', 
		cache: false, 
		data: postData, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete:  function(xhrobj, textStatus){
			// 读取JSON
			var resInfo = $.parseJSON(xhrobj.responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				if ($("iframe").length > 0) {
					$("iframe").attr("src", "download.do"+"?method=output&fileName=tickets.pdf&filePath=" + resInfo.tempFile);
				} else {
					var iframe = document.createElement("iframe");
		            iframe.src = "download.do"+"?method=output&fileName=tickets.pdf&filePath=" + resInfo.tempFile;
		            iframe.style.display = "none";
		            document.body.appendChild(iframe);
				}

				if($ele == null) {
					var hitSerialNo = postData["materials.serial_no[0]"];
					$ele = $("#products_area").find("serial").filter(function(){return $(this).text() === hitSerialNo}).parent();
				}
				$ele.attr("ticket", "printed");

				// 取得id（如果有）
				if (resInfo.id) {
					$ele.attr("material_id", resInfo.id);
				} else {
					// 如果没有（批量）就刷新
					refreshSerialNos()
				}
			}
		}
	});
};

var refreshSerialNos = function(){
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: 'material.do?method=refreshSerialNos', 
		cache: false, 
		data: null, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete:  function(xhrobj, textStatus){
			// 读取JSON
			var resInfo = $.parseJSON(xhrobj.responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				showSerialNos(resInfo.serialNos);
			}
		}
	});
}

	$("#ps_model_button").click(setNewProductModel);
	$("#ps_print_button").click(printGroup);
	$("#refresh_button").click(refreshSerialNos);

});
</script>
<div class="ui-widget-content dwidth-full" id="material_details" style="margin-bottom: 16px; display: block;">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
		<span class="areatitle">产品信息</span>
		<input type="button" class="ui-button" id="ps_model_button" style="float:right;margin-left:4px;" value="设定未开始产品型号"></input>
		<input type="button" class="ui-button" id="ps_print_button" style="float:right;margin-left:4px;" value="打印未开始产品小票"></input>
		<input type="button" class="ui-button" id="refresh_button" style="float:right;margin-left:4px;" value="刷新"></input>
	</div>
	<div id="products_area">
	</div>
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
		<span class="areatitle">完成部组信息</span>
	</div>
</div>
</div>