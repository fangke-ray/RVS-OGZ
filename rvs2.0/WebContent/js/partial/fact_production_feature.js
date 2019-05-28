// 已启动作业时间
var p_time = 0;
// 定时处理对象
var oInterval, ttInterval;
// 定时处理间隔（1分钟）
var iInterval = 60000;
// 取到的标准作业时间
var leagal_overline;

$(function () {
	let servicePaths = {
		"receptbutton" : "partial_recept.do",
		"collationbutton" : "partial_collation.do",
		"unpackbutton" : "partial_unpack.do",
		"onshelfbutton" : "partial_on_shelf.do",
		"outstoragebutton" : "partial_out_storage.do",
		"otherbutton" : "partial_other.do"
	};

	$("#workflow ul").buttonset();
	$("#workflow input[type='radio']").each(function () {
		$(this).click(function () {
			if (!$(this).hasClass("active")) {
				$("#workflow input[type='radio']").removeClass("active").next("label").css("cursor", "pointer");
				$(this).addClass("active").next("label").css("cursor", "default");
				loadPage(this.id, servicePaths[this.id]);
			}
		});
	});

	init();
});

function enableMenu (id) {
	if (id) {
		$("#workflow input[type='radio']").each(function () {
			let _id = this.id;
			if (id == _id) {
				$("#" + id).enable().next().css({"pointer-events" : "auto"});
			} else {
				$(this).disable().next().css({"pointer-events" : "none"});
			}
		});
	} else {
		$("#workflow input[type='radio']").each(function () {
			$(this).enable().next().css({"pointer-events" : "auto"});
		});
	}
};

function init () {
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'fact_production_feature.do?method=jsinit',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : jsinit_complete
	});
};

function jsinit_complete (xhrobj, textStatus) {
	let	resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			let alarmList = resInfo.alarmList;
			
			if(alarmList){
				warningMessage = "";
				alarmList.forEach(function(item,index){
					let myComment = item.myComment;
					let occur_time = item.occur_time.substring(0,10);
					warningMessage += occur_time + " " + myComment + "<br>";
				});
				
				var this_dialog = $("#error_pop");
				if (this_dialog.length === 0) {
					$("body.outer").append("<div id='error_pop'/>");
					this_dialog = $("#error_pop");
				}
				
				this_dialog.html(warningMessage);
				
				this_dialog.dialog({
					dialogClass : 'ui-error-dialog',
					title : "提示信息",
					width : 300,
					height : 'auto',
					resizable : false,
					modal : true,
					buttons : {
						"警告信息处理":function(){
							window.location.href = "./alarmMessage.do?method=init";
						}
					},
					close : function() {
						window.location.href = "./panel.do?method=init";
					}
				});
			} else {
				// 现品作业信息
				let factProductionFeature = resInfo.unfinish;
				if (factProductionFeature) {
					// 作业内容
					let production_type = factProductionFeature.production_type;
					if(production_type == 11){
						warningConfirm("",function(){
							window.location.href = "./partial_warehouse.do?method=init";
						},function(){
							window.location.href = "./panel.do?method=init";
						}, "入库单补充中", "返回零件入库单管理", "返回首页");
					}else{
						// 处理没结束
						step(production_type);
					}
				} else {
					// 默认收货
					$("#receptbutton").trigger("click");
				}
			}
		}
	} catch (e) {
	}
};

/**
 * 
 * @param id
 *            单选按钮ID
 * @param path
 *            倒入文件路径
 */
function loadPage (id, path) {
	$("#workflow_content").load(path, function (responseText, textStatus, XMLHttpRequest) {	});
	$("#" + id).prop("checked", true).trigger("change");
};

function step (production_type) {
	switch (production_type) {
	case "10":
		$("#receptbutton").trigger("click");
		break;
	case "20":
	case "21":
		$("#collationbutton").trigger("click");
		break;
	case "30":
		$("#unpackbutton").trigger("click");
		break;
	case "40":
		$("#onshelfbutton").trigger("click");
		break;
	case "50":
	case "51":
	case "52":
		$("#outstoragebutton").trigger("click");
		break;
	case "99":
		$("#otherbutton").trigger("click");
		break;
	default:
		break;
	}
};

//进行中效果
var ctime=function(){
	p_time++;
	var spendTime = p_time - 1;
	if(spendTime<0){
		spendTime = 0
	}
	
	$("#dtl_process_time label").text(minuteFormat(spendTime));

	var rate = parseInt((p_time + 1) / leagal_overline * 100);
	if (rate == 99) return;
	if (rate >= 100) rate = 99;
	var liquid = $("#p_rate div");
	liquid.animate({width : rate + "%"}, iInterval, "linear");
	if (rate > 80) {
		liquid.removeClass("tube-green");
		if (rate > 95) {
			liquid.removeClass("tube-yellow");
			liquid.addClass("tube-orange");
		} else {
			liquid.addClass("tube-yellow");
		}
	} else {
		liquid.addClass("tube-green");
	}
};

var minuteFormat =function(iminute) {
	if (!iminute) return "-";
	var hours = parseInt(iminute / 60);
	var minutes = iminute % 60;

	return fillZero(hours, 2) + ":" + fillZero(minutes, 2);
}