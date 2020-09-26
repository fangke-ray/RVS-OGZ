// Your turst our mission

/** 模块名 */
var modelname = "通箱库存";
/** 一览数据对象 */
var listdata = {};
/** 服务器处理路径 */
var servicePath = "turnoverCase.scan";

var showlistsize = 0;
var currentPos = 0;

var showlistdata = [];
var prefer = "";
var iv = 1;
var stgHeight = 180;

$(function() {
	refresh();
	roll();
	rollStg();

	setInterval(refresh, 60000);
	setInterval(roll, 6000);
	setInterval(rollStg, 8000);
});

/*
 * Ajax通信成功的处理
 */
function search_handleComplete(xhrobj, textStatus) {

	listdata = [];
	var resInfo = {};
	try {
		eval("resInfo=" + xhrobj.responseText);
		listdata = resInfo.list;
	} catch(e) {
	}

	$("td.wip-heaped").removeClass("ui-storage-highlight").removeClass("ui-state-error")
		.removeClass("wip-storage").removeClass("wip-shipping").removeClass("wip-heaped");

	var stored_count = 0;
	var overceed_count = 0;
	var to_store_count = 0;
	var to_ship_count = 0;
	showlistdata = [];

	for (var line in listdata) {
		var material = listdata[line];
		stored_count++;

		var $tdLocation = $(".wip-table td[location='" + material["wip_location"]+ "']");
		if (material.wip_overceed == 1) {
			$tdLocation.addClass("wip-heaped").addClass("ui-state-error");
			overceed_count++;
		} else {
			$tdLocation.addClass("wip-heaped").addClass("ui-storage-highlight");
		}

		if (material.execute == 1) {
			$tdLocation.addClass("wip-storage");
			to_store_count++;
		} else if (material.execute == 2) {
			$tdLocation.addClass("wip-shipping");
			to_ship_count++;
			showlistdata.push(material);
		}
	}

	showlistsize = showlistdata.length;

	if (currentPos >= showlistsize) {
		currentPos = 0;
	}

	var all_count = $("#storages td").length + 100;

	var labels = $("#label_table label");
	
	$(labels[0]).html(all_count + " 位");
	$(labels[1]).html(stored_count + " 个");
	$(labels[2]).html(overceed_count + " 个");
	$(labels[3]).html(to_store_count + " 个");
	$(labels[4]).html(to_ship_count + " 个");
};

var refresh = function() {
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=refresh',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : search_handleComplete
	});
};

var roll = function () {
	if (showlistsize > 0) {
		$(".shipping-material").css("opacity", "0.2");

		setTimeout(function(){
	
			var showHtml = "";
			var mCnt = 0;
			for ( ; currentPos < showlistsize; currentPos++) {
				if (mCnt == 7) showHtml += "<div class=\"clear\" style='height:2em;'></div>";
				if (mCnt == 14) break;
				var material = showlistdata[currentPos];
				var model_name = material.model_name;
				var model_font_size = ""; 
				if (model_name.length > 16) {
					model_font_size = " longest_model";
				} else if (model_name.length > 10) {
					model_font_size = " longer_model";
				}
				showHtml += '<div class="shipping-material" style="opacity:.2;">' +
							'<div class="ui-widget-header">' + material.wip_location + '</div>' +
							'<div class="td-content' + model_font_size + '">' + model_name + '</div>' +
							'<div class="td-content">' + material.serial_no + '</div>' +
							'<div class="td-content">' + material.bound_out_ocm + '</div>' +
						'</div>';
				mCnt++;
			}
			$("#shipping_table td:eq(1)").html(showHtml);
	
			if (currentPos >= showlistsize) {
				currentPos = 0;
			}
	
			setTimeout(function(){
				$(".shipping-material").css("opacity", "1");
			}, 100);
		}, 400);
	}
}

var rollStg = function () {
	var n = $("#storages")[0].scrollTop;
	if (n == 0) n = stgHeight; else n = 0;
	$("#storages").animate({
        scrollTop:  n
    }, 800);
}
