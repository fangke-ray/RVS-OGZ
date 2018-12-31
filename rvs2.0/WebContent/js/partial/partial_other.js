var servicePath = "partial_other.do";

$(function(){
	$("#partial_other span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});
	
	$("#partial_other .ui-button").button();
	$("#comment_keyboard").on("click", "input:button", function(){
		var v = this.value;
		if (v === "换行") v = "\n";
		var dComment = $("#comment").focus()[0];
		if(dComment.selectionStart) {
			var start = dComment.selectionStart;
			$("#comment").val(dComment.value.substring(0, dComment.selectionStart) + v 
				+ dComment.value.substring(dComment.selectionEnd, dComment.value.length));
			dComment.selectionEnd = dComment.selectionStart = start + v.length;
		} else {
			$("#comment").val(dComment.value + v);
		}
	}).children("input:button").button();

	$("#startbutton").click(doStart);
	$("#endbutton").click(doEnd);
	reset();
	otherInit();
});

function otherInit(){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=jsinit',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					//现品作业信息
					var fact_production_feature = resInfo.unfinish;
					//存在正在进行中的作业信息
					if(fact_production_feature){
						$("#startbutton").disable().removeClass("ui-state-focus");
						$("#endbutton").enable().removeClass("ui-state-focus");
						$("#comment").val("").enable();
						$("#comment_keyboard input").enable();
					
						enableMenu("otherbutton");
					}else{
						reset();
					}
				}
			}catch(e){}
		}
	});
};

function reset(){
	$("#startbutton").enable().removeClass("ui-state-focus");
	$("#endbutton").disable().removeClass("ui-state-focus");
	$("#comment").val("").disable();
	$("#comment_keyboard input").disable();
	enableMenu("");
};

function doStart(){
	var data = {
		"production_type" : "99"//作业内容 O：其它
	};
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'fact_production_feature.do?method=doStart',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					otherInit();
				}
			} catch (e) {}
		}
	});
};

function doEnd(){

	if (!$("#comment").val()) {
		errorPop("请填写作业备注内容", $("#comment"));
		return;
	}

	var data = {
		"comment" : $("#comment").val()
	};
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doFinish',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					otherInit();
				}
			} catch (e) {}
		}
	});
};