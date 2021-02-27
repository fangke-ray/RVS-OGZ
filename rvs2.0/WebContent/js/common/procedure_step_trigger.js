$(function(){
	var manualStart = function(){
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : 'procedureStepCount.do?method=manualStart',
			cache : false,
			data : null,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj, textStatus) {
				// 读取JSON
				var resInfo = $.parseJSON(xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				}
			}
		})
	}
	$("#cntbutton").click(manualStart);
})