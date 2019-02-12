// Your turst our mission

/** 模块名 */
var modelname = "WIP";
/** 一览数据对象 */
var listdata = {};
/** 服务器处理路径 */
var servicePath = "wipProcess.do";

var listsize = 0;
var gridsize = 7;
var currentPos = 0;

var showlistdata = {};
var prefer = "";

var checkedShowTo = 0;

/** 选择触发联动 */
var mappingwip = function() {
	var jthis = $(this);
	if (jthis.hasClass("wip-heaped-checked")) {
		jthis.removeClass("wip-heaped-checked");
	} else {
		$("td.wip-heaped-checked").removeClass("wip-heaped-checked");
		jthis.addClass("wip-heaped-checked");
	}

	var rowids = $("#list").jqGrid("getGridParam", "selarrrow");

	var gridlistsize = listdata.length;
	for (var igridlist = 0; igridlist < gridlistsize; igridlist++) {
		var gridvalue = listdata[igridlist];
		if (gridvalue.wip_location == jthis.attr("wipid")) {
			$("#checkedShow > table > tbody").html("<td>" + gridvalue.sorc_no + "</td><td>" + gridvalue.model_name + "</td><td>" + gridvalue.serial_no + "</td><td>" + gridvalue.wip_location + "</td>");
			$("#checkedShow").css("opacity", "1");
			clearTimeout(checkedShowTo);
			checkedShowTo = setTimeout(function(){
				$("#checkedShow").css("opacity", "0");
			} , 3000);
			break;
		}
	}
	
};

$(function() {
	$("input.ui-button").button();

	$("#storages").on("click", "td.wip-heaped", mappingwip);
});

var mapLoaded = function() {
	
	/** WIP 区联动 */
	$(".wip-heaped").hover(function() {
		var wip_location = $(this).attr("wipid");
		$("tr.jqgrow:has(td:[aria\-describedby='list_wip_location'][title='" + wip_location + "'])").addClass("wip-heaped-hover");
	}, function() {
		$("tr.jqgrow").removeClass("wip-heaped-hover");
	});

	refresh();

	setInterval(refresh, 60000);
	setInterval(roll, 1000);

}

var refreshing = false;

/*
 * Ajax通信成功的处理
 */
function search_handleComplete(xhrobj, textStatus) {
	if (refreshing) return;
	refreshing = true;

	listdata = [];
	var resInfo = {};
	try {
		eval("resInfo=" + xhrobj.responseText);
		listdata = resInfo.list;
	} catch(e) {
	}

	$("td.wip-heaped").removeClass("ui-storage-highlight").removeClass("ui-state-error");

	for (var line in listdata) {
		var material = listdata[line];
		if (material.wip_overceed == 1) {
			$("td[wipid='" + material["wip_location"]+ "']").addClass("wip-heaped").addClass("ui-state-error");
		} else {
			$("td[wipid='" + material["wip_location"]+ "']").addClass("wip-heaped").addClass("ui-storage-highlight");
		}
	}

	listsize = listdata.length;

	if (currentPos >= listsize) {
		currentPos = 0;
	}

	if (currentPos > 0) {
		listdata = listdata.slice(currentPos, listsize).concat(listdata.slice(0, currentPos));
	}

	if (listsize > gridsize) showlistdata = listdata.slice(0, gridsize);

	if ($("#gbox_list").length > 0) {
		$("#list").jqGrid().clearGridData();
		$("#list").jqGrid('setGridParam', {data : showlistdata}).trigger("reloadGrid", [{current : false}]);
	} else {
		$("#list").jqGrid({
			toppager : true,
			data : showlistdata,
			height : gridsize * 23 + 1, // 461
			width : 458,
			rowheight : 23,
			datatype : "local",
			colNames : ['修理单号', '型号', '机身号', 'WIP货架位置'],
			colModel : [{
						name : 'sorc_no',
						index : 'sorc_no',
						sortable : false,
						width : 80
					}, {
						name : 'model_name',
						index : 'model_id',
						sortable : false,
						width : 180
					}, {
						name : 'serial_no',
						index : 'serial_no',
						sortable : false,
						width : 50
					}, {
						name : 'wip_location',
						index : 'wip_location',
						sortable : false,
						align: 'center',
						width : 60
					}
			],
			rowNum : gridsize, // 20
			rownumbers : true,
			toppager : false,
			pager : "#listpager",
			viewrecords : true,
			caption : "",
			gridview : true, // Speed up
			pagerpos : 'right',
			hidegrid : false,
			pgbuttons : true,
			pginput : false,
			recordpos : 'left',
			viewsortcols : [true, 'vertical', true],
			gridComplete : function() {
				blinkfirstLocation();
			}
		});
	}

	var all_count = $("#storages td").length;
	var stored_count = $("#storages td.wip-heaped").length;
	var overceed_count = $("#storages td.ui-state-error").length;

	var all_e_count = $("#endoeyeWIP td").length;
	var stored_e_count = $("#endoeyeWIP td.wip-heaped").length;
	var overceed_e_count = $("#endoeyeWIP td.ui-state-error").length;

	var all_p_count = $("#periWIP td").length;
	var stored_p_count = $("#periWIP td.wip-heaped").length;
	var overceed_p_count = $("#periWIP td.ui-state-error").length;

	var all_i_count = all_count - all_e_count - all_p_count;
	var stored_i_count = stored_count - stored_e_count - stored_p_count;
	var overceed_i_count = overceed_count - overceed_e_count - overceed_p_count;

	var labels = $("#label_table label");

	$(labels[0]).html(all_i_count + "位");
	$(labels[3]).html(stored_i_count + "台");
	$(labels[6]).html(overceed_i_count + "台");
	$(labels[9]).html(calcRate(overceed_i_count, stored_i_count));

	$(labels[1]).html(all_e_count + "位");
	$(labels[4]).html(stored_e_count + "台");
	$(labels[7]).html(overceed_e_count + "台");
	$(labels[10]).html(calcRate(overceed_e_count, stored_e_count));
	$(labels[2]).html(all_p_count + "位");
	$(labels[5]).html(stored_p_count + "台");
	$(labels[8]).html(overceed_p_count + "台");
	$(labels[11]).html(calcRate(overceed_p_count, stored_p_count));

	$("#list tr.jqgrow").unbind("hover").hover(function() {
		var wip_location = $(this)
			.find("td[aria\\-describedby='list_wip_location']").attr("title");

		$("td[wipid='" + wip_location +"']").addClass("wip-heaped-hover");
	}, function() {
		$(".wip-heaped-hover").removeClass("wip-heaped-hover");
	});

	refreshing = false;
};

var blinkfirstLocation = function() {
	var firstLocation = $("#list tr:eq(1) td")
		.parent().find("td[aria\\-describedby='list_wip_location']").attr("title");
	if (firstLocation != null && firstLocation != "") {
		$("td.wip-heaped-blink").addClass(prefer).removeClass("wip-heaped-blink");
		$("td[wipid='" + firstLocation + "']").addClass("wip-heaped-blink");
		if ($("td[wipid='" + firstLocation + "']").hasClass("ui-storage-highlight")) {
			prefer = "ui-storage-highlight";
		} else if ($("td[wipid='" + firstLocation + "']").hasClass("ui-state-error")) {
			prefer = "ui-state-error";
		}
		$("td[wipid='" + firstLocation + "']").removeClass(prefer);
	}
	$("#list tr:eq(1) td").addClass(prefer);
}

var refresh = function() {
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'wipProgress.scan?method=refresh',
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
	if (listsize > 1) {
		listdata.push(listdata.shift());
		if (listsize > gridsize) showlistdata = listdata.slice(0, gridsize);

		$("#list").jqGrid().clearGridData();
		$("#list").jqGrid('setGridParam', {data : showlistdata}).trigger("reloadGrid", [{current : false}]);

		blinkfirstLocation();
		if (currentPos < listsize) {
			currentPos++;
		} else {
			currentPos = 0;
		}
	}
}

var calcRate = function(numerator, denominator) {
	return (denominator > 0 ? (Math.round(numerator / denominator * 100) + "%") : "--");
}
