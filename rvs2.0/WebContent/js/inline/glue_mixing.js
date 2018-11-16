$(function() {
	/** 服务器处理路径 */
	var servicePath = "glue_fixing.do";
	
	// 初始化
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=init',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : initComplete
	});
	
	function initComplete(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
			if (resInfo.errors && resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				var resInfo = null;
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors && resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var state = resInfo.state;
					
					// 没有正在做的胶水
					if(state == "no"){
						return;
					}
					
					var $glue_dialog =  $("#glue_area");
					$glue_dialog.hide();
					
					$("#hid_glue_mixing_process_id").val(resInfo.glueMixingForm.glue_mixing_process_id);
					$("#glue_partial_id").val(resInfo.glueMixingForm.partial_id);
					$("#glue_partial_code").text(resInfo.glueMixingForm.code);
					$("#expiration_date").val(resInfo.glueMixingForm.expiration).hide();
					$("#label_expiration_date").text(resInfo.glueMixingForm.expiration);
					$("#lot_no").val(resInfo.glueMixingForm.lot_no);
					$("#binder_name").val(resInfo.glueMixingForm.binder_name);
					
					$glue_dialog.find("input[type='text']").attr("readonly",true);
					$glue_dialog.find(".glue_mixing").show();
					
					// 当前处于重开状态
					if(state == "continue"){
						$("#glueContinueButton").enable();
						$("#gluePauseButton,#glueFinishButton").disable().removeClass("ui-state-focus");
					}else if(state == "pause"){// 当前处于暂停状态
						$("#gluePauseButton,#glueFinishButton").enable();
						$("#glueContinueButton").disable().removeClass("ui-state-focus");
					}
					
					$("#glueStartButton").disable().removeClass("ui-state-focus");
					
					$glue_dialog.dialog({
						title : "调胶作业",
						width : 370,
						show : "blind",
						height : 'auto' ,
						resizable : false,
						modal : true,
						close :function(){
							window.location.href = "./panel.do?method=init";
						}
					});
					$glue_dialog.show();
				}
			}
		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		}
	}
	
	// 维修对象信息扫描录入区域
	$("#scanner_inputer").keydown(function(e){
		if(e.keyCode == 13){
			var code = this.value;
			
			if (code.substring(0,2)== "OT") {
				if(code.length == 7){
					code = code.trim();
				}
				
				if(code.length == 6){
					// Ajax提交
					$.ajax({
						beforeSend : ajaxRequestType,
						async : true,
						url : servicePath + '?method=checkCode',
						cache : false,
						data : {"code" : code},
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : checkCodeComplete
					});
				}
			}
		}
	});
	
	function checkCodeComplete(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
			if (resInfo.errors && resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				var $glue_dialog =  $("#glue_area");
				$glue_dialog.hide();
				
				// 数据赋值
				$("#scanner_inputer").val("");
				$("#glue_partial_id").val(resInfo.partial.partial_id);
				$("#glue_partial_code").text(resInfo.partial.code);
				$("#expiration_date").val("").focus().show();
				$("#label_expiration_date").text("");
				$("#lot_no").val("");
				$("#binder_name").val("");
				$("#hid_glue_mixing_process_id").val("");
				
				$glue_dialog.find(".glue_mixing").hide();
				$glue_dialog.find("input[type='text']").removeAttr("readonly");
				
				$glue_dialog.dialog({
					title : "调胶作业",
					width : 370,
					show : "blind",
					height : 'auto' ,
					resizable : false,
					modal : true,
					close :function(){
						var glue_mixing_process_id = $("#hid_glue_mixing_process_id").val();
						if(glue_mixing_process_id){
							window.location.href = "./panel.do?method=init";
						}else{
							$glue_dialog.dialog('close');
						}
					}
				});
				$glue_dialog.show();
			}
		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		};
	}
	
	// 有效期扫描
	$("#expiration_date").keydown(function(e){
		if(e.keyCode == 13){
			var expiration = this.value;
			// Ajax提交
			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url : servicePath + '?method=checkExpiration',
				cache : false,
				data : {"expiration" : expiration,"partial_id":$("#glue_partial_id").val()},
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : checkExpirationComplete
			});
		}
	});
	
	function checkExpirationComplete(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
			if (resInfo.errors && resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				// 有效期input变为label
				$("#expiration_date").hide();
				$("#label_expiration_date").text($("#expiration_date").val());
				$("#glue_area").find(".glue_mixing").show();
				$("#lot_no").focus();
				
				$("#binder_name").autocomplete({source : resInfo.autocomp.autoBinderNames, 
					minLength: 0, delay: 0,
					position: {
						my: "left bottom",
						at: "right bottom",
						collision: "none"
					},
					minChars: 0, max: 10, scrollHeight : 24, width: 200
				});

				$("#glueStartButton").enable();
				$("#glueContinueButton,#gluePauseButton,#glueFinishButton").disable().removeClass("ui-state-focus");
			}
		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		};
	}
	
	$("#lot_no").keydown(function(e){
		if(e.keyCode == 13){
			$("#binder_name").focus();
			var ev = jQuery.Event("keydown");
			ev.which = 8; //enter key
			$("#binder_name").trigger(ev);
		}
	});
	
	$("#binder_name").keydown(function(e){
		if(e.keyCode == 13){
			doStart();
		}
	});
	
	// 开始调胶
	$("#glueStartButton").click(function(){
		doStart();
	});
	
	// 暂停调胶
	$("#gluePauseButton").click(function(){
		makePause();
	});
	
	// 重开调胶
	$("#glueContinueButton").click(function(){
		doContinue();
	});
	
	// 完成调胶
	$("#glueFinishButton").click(function(){
		doFinish();
	});
	
	function doStart(){
		var data = {
			"code":$("#glue_partial_code").text(),
			"partial_id":$("#glue_partial_id").val(),
			"expiration":$("#expiration_date").val(),
			"lot_no":$("#lot_no").val(),
			"binder_name":$("#binder_name").val()
		}
		
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : servicePath + '?method=doStart',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : doStartComplete
		});
	};
	
	function doStartComplete(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
			if (resInfo.errors && resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				$("#glue_area input[type='text']").attr("readonly",true);
				$("#hid_glue_mixing_process_id").val(resInfo.glueMixingForm.glue_mixing_process_id);
				$("#glueStartButton,#glueContinueButton").disable().removeClass("ui-state-focus");
				$("#gluePauseButton,#glueFinishButton").enable();
			}
		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		};
	}
	
	function makePause(){
		var jBreakDialog = $("#break_dialog");
		if (jBreakDialog.length === 0) {
			$("body.outer").append("<div id='break_dialog'/>");
			jBreakDialog = $("#break_dialog");
		}
	
		jBreakDialog.hide();
		// 导入暂停画面
		jBreakDialog.load("widget.do?method=pauseoperator",
			function(responseText, textStatus, XMLHttpRequest) {
				// 设定暂停理由
				$("#pauseo_edit").show();
				$("#pauseo_show").hide();
				$("#pauseo_edit_pause_reason").html(pauseOptions);
				$("#pauseo_edit_pause_reason").select2Buttons();
	
				$("#pauseo_edit_pause_reason").val("49").trigger("change");
				$("#pauseo_edit").parent().validate({
					rules : {
						comments : {
							required : function() {
								return "49" === $("#pauseo_edit_pause_reason").val();
							}
						}
					}
				});
				makePauseDialog(jBreakDialog);
		});
	}
	
	/** 暂停信息弹出框 */
	function makePauseDialog(jBreakDialog) {
		jBreakDialog.dialog({
			title : "暂停信息编辑",
			width : 480,
			show: "blind",
			height : 'auto' ,
			resizable : false,
			modal : true,
			minHeight : 200,
			close : function(){
				jBreakDialog.html("");
			},
			buttons : {
				"确定":function(){
					if ($("#pauseo_edit").parent().valid()) {
						var data = {
							"glue_mixing_process_id":$("#hid_glue_mixing_process_id").val(),
							"reason" : $("#pauseo_edit_pause_reason").val(),
							"comments" : $("#pauseo_edit_comments").val()
						}
						
						$.ajax({
							beforeSend : ajaxRequestType,
							async : true,
							url : servicePath + '?method=doPause',
							cache : false,
							data : data,
							type : "post",
							dataType : "json",
							success : ajaxSuccessCheck,
							error : ajaxError,
							complete : function(xhrobj, textStatus){
								var resInfo = null;
								try {
									// 以Object形式读取JSON
									eval('resInfo =' + xhrobj.responseText);
									if (resInfo.errors && resInfo.errors.length > 0) {
										// 共通出错信息框
										treatBackMessages(null, resInfo.errors);
									} else {
										$("#glueStartButton,#gluePauseButton,#glueFinishButton").disable().removeClass("ui-state-focus");;
										$("#glueContinueButton").enable();
										jBreakDialog.dialog("close");
									}
								} catch (e) {
									alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
								}
							}
						});
						
					}
				}, "关闭" : function(){ $(this).dialog("close"); }
			}
		});
	}
	
	function doContinue(){
		var data = {
			"glue_mixing_process_id":$("#hid_glue_mixing_process_id").val()
		}
		
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : servicePath + '?method=doContinue',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : doContinueComplete
		});
	};
	
	function doContinueComplete(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
			if (resInfo.errors && resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				$("#glueStartButton,#glueContinueButton").disable().removeClass("ui-state-focus");
				$("#gluePauseButton,#glueFinishButton").enable();
			}
		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		};
	}
	
	function doFinish(){
		var data = {
			"glue_mixing_process_id":$("#hid_glue_mixing_process_id").val()
		}
		
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
			complete : doFinishComplete
		});
	};
	
	function doFinishComplete(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
			if (resInfo.errors && resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				$("#glue_area").dialog('destroy');
			}
		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		};
	}
	
});