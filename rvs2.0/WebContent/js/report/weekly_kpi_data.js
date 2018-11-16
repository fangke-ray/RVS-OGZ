var servicePath = "weekly_kpi_data.do";

var cur_count_date_start;
var cur_count_date_end;
var cur_weekly_of_year;

$(function(){
	$("input.ui-button").button();
	$("#editarea").hide();

	$("#cancelbutton, #editarea span.ui-icon").click(showList);
	$("#updatebutton").click(update);
	$("#createreportbutton").click(createReport);
	$("#calculatebutton").click(calculate);
	
	$("#add_count_date_start,#add_count_date_end").datepicker({
		showButtonPanel:true,
		dateFormat: "yy/mm/dd",
		currentText: "今天"
	});
	
	
	findit();
	
});

var findit  = function(data){
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: true, 
		url: servicePath + '?method=search', 
		cache: false, 
		data: null, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError,
		complete: search_handleComplete
	});
};

var search_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages("", resInfo.errors);
		} else {
			var listdata = resInfo.fileNameList;
			if ($("#gbox_exd_list").length > 0) {
				$("#exd_list").jqGrid().clearGridData();
				$("#exd_list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [{current : false}]);
			} else {
				$("#exd_list").jqGrid({
					data : listdata,
					height : 461,
					width : 992,
					rowheight : 23,
					datatype : "local",
					colNames : ['', '', '','', '统计日期起~止', '周数', '系统生成', '确认完成', '上传'],
					colModel : [{
							name : 'myac',
							width : 40,
							fixed : true,
							sortable : false,
							resize : false,
							formatter : 'actions',
							formatoptions : {
								keys : true,
								editbutton : false
							}
						}, {
							name : 'count_date_start',
							index : 'count_date_start',
							hidden : true
						}, {
							name : 'count_date_end',
							index : 'count_date_end',
							hidden : true
						}, {
							name : 'weekly_of_year',
							index : 'weekly_of_year',
							hidden : true
						}, {
							name : 'fileDayTime',
							index : 'fileDayTime',
							formatter : function(value, options, rData) {
								return rData.count_date_start + "~" + rData.count_date_end
							},
							width : 60
						}, {
							name : 'weekly_of_year',
							index : 'weekly_of_year',
							width : 20,
							align : 'right'
						}, {
							name : 'fileName',
							index : 'fileName',
							width : 150,
							align : 'center',
							formatter : function(value, options, rData) {
								if (value) {
									return "<a href='javascript:downExcel(\"" + rData['fileName'] + "\");' >"
											+ rData['fileName'] + "</a>";
								} else {
									return "";
								}
	
							}
						}, {
							name : 'confirmfilename',
							index : 'confirmfilename',
							width : 150,
							align : 'center',
							formatter : function(value, options, rData) {
								if (value) {
									return "<a href='javascript:downConfirmExcel(\"" + rData['confirmfilename'] + "\");' >"
											+ rData['confirmfilename'] + "</a>";
								} else {
									return "";
								}
	
							}
						}, {
							name : 'upload',
							index : 'upload',
							width : 30,
							align : 'center',
							formatter : function(value, options, rData) {
								if($("#privacy").val().trim() == "line"){
									var count_date_start = rData.count_date_start;
									var count_date_end = rData.count_date_end;
		
									var arr_count_date_start = count_date_start.split("/");
									var arr_count_date_end = count_date_end.split("/");
		
									var filename = arr_count_date_start[1] + "月" + arr_count_date_start[2] + "日~" 
										+ arr_count_date_end[1] + "月" + arr_count_date_end[2] + "日";
		
									return "<a style='text-decoration:none;' href='javascript:import_upload_file(\""+ filename + "\"," + "\""
											+ rData.weekly_of_year + "\");' ><input type='button'value='上传'class='upload-button ui-state-default '></input></a>";
								}else{
									return "";
								}
							}
						}],
					rowNum : 20,
					toppager : false,
					pager : "#exd_listpager",
					viewrecords : true,
					multiselect : false,
					gridview : true,
					pagerpos : 'right',
					pgbuttons : true,
					rownumbers : true,
					pginput : false,
					recordpos : 'left',
					hidegrid : false,
					deselectAfterSort : false,
					ondblClickRow : showEdit,
					viewsortcols : [true, 'vertical', true],
					gridComplete : function() {
					}
				});
			}
		}
						
	}catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
	};
}

/*下载*/
var downExcel = function(fileName) {
	if ($("iframe").length > 0) {
		$("iframe").attr("src", "download.do"+"?method=output&fileName="+ fileName +"&from=report_weeks");
	} else {
		var iframe = document.createElement("iframe");
				iframe.src = "download.do"+"?method=output&fileName="+ fileName +"&from=report_weeks";
				iframe.style.display = "none";
				document.body.appendChild(iframe);
	}
};

/*下载确认*/
var downConfirmExcel = function(fileName) {
	if ($("iframe").length > 0) {
		$("iframe").attr("src", "download.do"+"?method=output&fileName="+ fileName +"&from=report_weeks_confirm");
	} else {
		var iframe = document.createElement("iframe");
				iframe.src = "download.do"+"?method=output&fileName="+ fileName +"&from=report_weeks_confirm";
				iframe.style.display = "none";
				document.body.appendChild(iframe);
	}
};

/*上传文件*/ 
var import_upload_file = function(date,weekly_of_year) {
	$("#upload_file").hide();
	$("#upload_file").html("<input name='file' id='make_upload_file' type='file'/>");		
	$("#upload_file").dialog({
		title : "选择上传文件",
		width : 280,
		show: "blind",
		height : 180,
		resizable : false,
		modal : true,
		minHeight : 200,
		close : function(){
			$("#upload_file").html("");
		},
		buttons : {
			"上传":function(){
					uploadfile(date,weekly_of_year);
			}, "关闭" : function(){ $(this).dialog("close"); }
		}
		
	});
	$("#upload_file").show();	
};

var uploadfile = function(date,weekly_of_year) {
	// 覆盖层
	panelOverlay++;
	makeWindowOverlay();

	$.ajaxFileUpload({
		url : servicePath+ '?method=confirm&date=' + date + '&weekly_of_year=' +weekly_of_year, // 需要链接到服务器地址
		secureuri : false,
		fileElementId : 'make_upload_file', // 文件选择框的id属性
		dataType : 'json', // 服务器返回的格式
		success : function(responseText, textStatus) {
			panelOverlay--;
			killWindowOverlay();
	
			var resInfo = null;
	
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					$("#upload_file").dialog('close');
					$("#upload_file").text("上传文件完成。");
					$("#upload_file").dialog({
						resizable : false,
						modal : true,
						title : "导入文件确认",
						buttons : {
							"确认" : function() {
								$(this).dialog("close");
							}
						}
					});
					findit();
				}
			} catch(e) {
				
			}
		}
	});
};

var showDelete = function(rid) {

	// 读取删除行
	var rowData = $("#exd_list").getRowData(rid);
	var data = {
		"count_date_start" : rowData.count_date_start.replace(/\//g,""),
		"count_date_end" : rowData.count_date_end.replace(/\//g,"")
	}

	warningConfirm("删除不能恢复。确认要删除["+rowData.count_date_start +"~" + rowData.count_date_end +"]的记录吗？",
	function() {
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : servicePath + '?method=delete',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : update_handleComplete
		});
	});
};

var showList = function() {
	$("#listarea").show();
	$("#editarea").hide();
}

var update_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			showList();
			findit();
		}
	} catch (e) {
		console.log("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var showEdit = function(rid) {
	// 读取修改行
	var rowData = $("#exd_list").getRowData(rid);
	var data = {
		"count_date_start" : rowData.count_date_start,
		"count_date_end" : rowData.count_date_end
	}

	cur_count_date_start = rowData.count_date_start;
	cur_count_date_end =  rowData.count_date_end;
	cur_weekly_of_year =  rowData.weekly_of_year;
	
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=detail',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : showdetail_handleComplete
	});
};

var showdetail_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			if (resInfo.details && resInfo.details.length > 0) {
				// 清空动态部分
				var $table = $("#editarea table");
				$table.find("th:not(.stable)").text("一周");
				$table.find("td:not(.stable)").text("-");
				$table.find("tr:not(.noset)").find("td:not(.stable)").attr("status", "normal");

				for (var iDtl in resInfo.details) {
					var detail = resInfo.details[iDtl];
					if (iDtl == 9) break;
					// 目標
					var revIdx = parseInt(iDtl) + 1 
					// TODO 月合并
					$table.find("thead tr:eq(1) th:eq(-" + revIdx + ")").text(detail.weekly_of_year + "W");
					$table.find("tr[for=target] td:eq(-" + revIdx + ")").text(detail.target);
					if (iDtl == 0) continue;

					if(iDtl == 1){
						$table.find("tr[for=registration] td:eq(-" + revIdx + ")").html("<input value='" + (detail.registration || "") + "'>");
						$table.find("tr[for=user_agreement] td:eq(-" + revIdx + ")").html("<input value='" + (detail.user_agreement || "") + "'>");
						$table.find("tr[for=return_to_osh] td:eq(-" + revIdx + ")").html("<input value='" + (detail.return_to_osh || "") + "'>");
						$table.find("tr[for=unrepair] td:eq(-" + revIdx + ")").html("<input value='" + (detail.unrepair || "") + "'>");
						$table.find("tr[for=shipment] td:eq(-" + revIdx + ")").html("<input value='" + (detail.shipment || "") + "'>");
						$table.find("tr[for=work_in_process] td:eq(-" + revIdx + ")").html("<input value='" + (detail.work_in_process || "") + "'>");
						$table.find("tr[for=work_in_storage] td:eq(-" + revIdx + ")").html("<input value='" + (detail.work_in_storage || "") + "'>");
					}else{
						$table.find("tr[for=registration] td:eq(-" + revIdx + ")").text(detail.registration);
						$table.find("tr[for=user_agreement] td:eq(-" + revIdx + ")").text(detail.user_agreement);
						$table.find("tr[for=return_to_osh] td:eq(-" + revIdx + ")").text(detail.return_to_osh);
						$table.find("tr[for=unrepair] td:eq(-" + revIdx + ")").text(detail.unrepair);
						$table.find("tr[for=shipment] td:eq(-" + revIdx + ")").text(detail.shipment);
						$table.find("tr[for=work_in_process] td:eq(-" + revIdx + ")").text(detail.work_in_process);
						$table.find("tr[for=work_in_storage] td:eq(-" + revIdx + ")").text(detail.work_in_storage);
					}
					

					setEvalValue(iDtl, $table.find("tr[for=intime_complete_rate] td:eq(-" + revIdx + ")"), detail.intime_complete_rate);
					setEvalValue(iDtl, $table.find("tr[for=average_repair_lt] td:eq(-" + revIdx + ")"), detail.average_repair_lt);
					setEvalValue(iDtl, $table.find("tr[for=average_work_lt] td:eq(-" + revIdx + ")"), detail.average_work_lt);
					setEvalValue(iDtl, $table.find("tr[for=intime_work_out_rate] td:eq(-" + revIdx + ")"), detail.intime_work_out_rate);
					setEvalValue(iDtl, $table.find("tr[for=bo_rate] td:eq(-" + revIdx + ")"), detail.bo_rate);
					setEvalValue(iDtl, $table.find("tr[for=bo_3day_rate] td:eq(-" + revIdx + ")"), detail.bo_3day_rate);
					setEvalValue(iDtl, $table.find("tr[for=inline_passthrough_rate] td:eq(-" + revIdx + ")"), detail.inline_passthrough_rate);

					if(iDtl == 1){
						$table.find("tr[for=final_check_pass_count] td:eq(-" + revIdx + ")").html("<input value='" + (detail.final_check_pass_count || "") + "'>");
					}else{
						$table.find("tr[for=final_check_pass_count] td:eq(-" + revIdx + ")").text(detail.final_check_pass_count);
					}
					
					setEvalValue(iDtl, $table.find("tr[for=final_check_forbid_count] td:eq(-" + revIdx + ")"), detail.final_check_forbid_count);
					
					setEvalValue(iDtl, $table.find("tr[for=final_inspect_pass_rate] td:eq(-" + revIdx + ")"), detail.final_inspect_pass_rate);
					$table.find("tr[for=final_inspect_pass_rate] td:eq(-" + revIdx + ")").find("input").attr("readonly",true);
					
					setEvalValue(iDtl, $table.find("tr[for=service_repair_back_rate] td:eq(-" + revIdx + ")"), detail.service_repair_back_rate);
				}
				$("#listarea").hide();
				$("#editarea").show();
				
				if($("#privacy").val().trim() == ""){
					$("#editarea table").find("input").attr("disabled",true);
				}
				
				var regexp = new RegExp("^[0-9]*$");
				
				$("#editarea tr[for='final_check_pass_count'] input").keyup(function(evt){
					var final_check_pass_count = $(this).val().trim();
					if(final_check_pass_count == "") {
						$("#editarea tr[for='final_inspect_pass_rate'] input").val("");
						return;
					}
					
					if(regexp.test(final_check_pass_count)){
						if($("#editarea tr[for='final_check_forbid_count'] input").val()){
							var final_check_forbid_count = $("#editarea tr[for='final_check_forbid_count'] input").val().trim();
							if(final_check_forbid_count == "") return;
							
							var total = parseInt(final_check_pass_count) + parseInt(final_check_forbid_count);
							if(total == 0) return;
							
							var rate = (final_check_pass_count / total *100).toFixed(2);
							$("#editarea tr[for='final_inspect_pass_rate'] input").val(rate);
						}
							
					}
				});
				
				$("#editarea tr[for='final_check_forbid_count'] input").keyup(function(evt){
					var final_check_forbid_count = $(this).val().trim();
					if(final_check_forbid_count == ""){
						$("#editarea tr[for='final_inspect_pass_rate'] input").val("");
						return;
					}
					if(regexp.test(final_check_forbid_count)){
						if($("#editarea tr[for='final_check_pass_count'] input").val()){
							var final_check_pass_count = $("#editarea tr[for='final_check_pass_count'] input").val().trim();
							if(final_check_pass_count == "") return;
							
							var total = parseInt(final_check_pass_count) + parseInt(final_check_forbid_count);
							if(total == 0) return;
							
							var rate = (final_check_pass_count / total *100).toFixed(2);
							$("#editarea tr[for='final_inspect_pass_rate'] input").val(rate);
						}
							
					}
				});
				
				$("#editarea tr:nth-child(n+11) input").each(function(index,ele){
					var $input = $(ele);
					var $item = $input.parent();//td
					var $tr =  $item.parent();
					
					if($tr.attr("for") == "final_check_pass_count"){
						return;
					}
					
					var $target = $item.parent().children(".target");
					if ($target.length > 0) {
						$input.change(function(){
							alert()
							checkTarget($item, $target, this.value);
						});
					}
				});
				
			}
		}
	} catch (e) {
		console.log("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
	};
}

var setEvalValue = function(idx, $item, value){
	if (idx == 1) { // 本周
		var $target = $item.parent().children(".target");
		if ($target.length > 0) {
			checkTarget($item, $target, value);
		}
		$item.html("<input value='" + (value || "") + "'>");
	} else {
		if (idx != -1) { // 目标比较
			var $target = $item.parent().children(".target");
			if ($target.length > 0) {
				checkTarget($item, $target, value);
			}
		}
		$item.text(value);
	}
}

var checkTarget = function($item, $target, thisVal) {
	if (isNaN(thisVal)) return;

	var upper = $target.attr("upper");
	if (upper) {
		var nUpper = parseFloat(upper);
		var nUpperClose = nUpper * 1.1;
		if (thisVal > nUpperClose) {
			$item.attr("status", "over");
		} else if (thisVal > nUpper) {
			$item.attr("status", "warn");
		}else{
			$item.attr("status", "normal");
		}
	}

	var lower = $target.attr("lower");
	if (lower) {
		var nLower = parseFloat(lower);
		var nLowerClose = nLower * 0.9;
		if (thisVal < nLowerClose) {
			$item.attr("status", "over");
		} else if (thisVal < nLower) {
			$item.attr("status", "warn");
		}else{
			$item.attr("status", "normal");
		}
	}
}

var update = function(){
	var data = {
		"count_date_start" : cur_count_date_start,
		"count_date_end" : cur_count_date_end
	};
	
	$("#editarea tbody tr:not(.formula)").each(function(index,ele){
		var $tr = $(ele);
		var field = $tr.attr("for");
		var value = $tr.find("input").val();
		if(value!=null && value!=""){
			data[field] = $tr.find("input").val().trim();
		}else{
			data[field] = null;
		}
	});
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doUpdate',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : update_handleComplete
	});
}

var createReport = function(){
	var data = {
		"count_date_start" : cur_count_date_start,
		"count_date_end" : cur_count_date_end,
		"weekly_of_year" : cur_weekly_of_year
	};
	
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=createReport',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : create_report_handleComplete
	});
}


var create_report_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			$("#confirmmessage").text("生成成功");
			$("#confirmmessage").dialog({
				resizable : false,
				modal : true,
				title : "",
				buttons : {
					"确认" : function() {
						$(this).dialog("close");
						findit();
					}
				}
			});
		}
	} catch (e) {
		console.log("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var calculate = function(){
	$("#add_count_date_start,#add_count_date_end").val("");
	$("#calculatedialog").dialog({
		resizable : false,
		width : 310,
		show : "blind",
		height : 'auto',
		modal : true,
		title : "周报计算",
		buttons : {
			"确认" : function() {
				var data = {
					"count_date_start" : $("#add_count_date_start").val(),
					"count_date_end" : $("#add_count_date_end").val()
				};
				
				$.ajax({
					beforeSend : ajaxRequestType,
					async : false,
					url : servicePath + '?method=calculate',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : calculate_handleComplete
				});
			},
			"取消" : function() {
				$(this).dialog("close");
			}
		}
	});
};

var calculate_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			$("#calculatedialog").dialog("close");
			findit();
		}
	} catch (e) {
		console.log("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
	};
};
