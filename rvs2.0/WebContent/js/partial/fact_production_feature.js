$(function () {
	let servicePaths = {
		"receptbutton" : "partial_recept.do",
		"collationbutton" : "partial_collation.do",
		"unpackbutton" : "partial_unpack.do",
		"onshelfbutton" : "partial_on_shelf.do",
		"otherbutton" : "partial_other.do"
	};

	$("#workflow ul").buttonset();
	$("#workflow input[type='radio'").each(function () {
		$(this).click(function () {
			if (!$(this).hasClass("active")) {
				$("#workflow input[type='radio'").removeClass("active").next("label").css("cursor", "pointer");
				$(this).addClass("active").next("label").css("cursor", "default");
				loadPage(this.id, servicePaths[this.id]);
			}
		});
	});

	init();
});

function enableMenu (id) {
	if (id) {
		$("#workflow input[type='radio'").each(function () {
			let _id = this.id;
			if (id == _id) {
				$("#" + id).enable().next().css({"pointer-events" : "auto"});
			} else {
				$(this).disable().next().css({"pointer-events" : "none"});
			}
		});
	} else {
		$("#workflow input[type='radio'").each(function () {
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
			// 现品作业信息
			let factProductionFeature = resInfo.unfinish;
			if (factProductionFeature) {
				// 作业内容
				let production_type = factProductionFeature.production_type;

				// 处理没结束
				step(production_type);
			} else {
				// 默认收货
				$("#receptbutton").trigger("click");
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
	case "99":
		$("#otherbutton").trigger("click");
		break;
	default:
		break;
	}
};