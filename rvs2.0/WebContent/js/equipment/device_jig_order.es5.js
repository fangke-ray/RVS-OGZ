"use strict";

var _templateObject = _taggedTemplateLiteral(["<input type=\"text\" class=\"ui-widget-content quantity\" value=\"", "\">"], ["<input type=\"text\" class=\"ui-widget-content quantity\" value=\"", "\">"]),
    _templateObject2 = _taggedTemplateLiteral(["<td class=\"td-content text-center\" applicate_date=", " style=\"width:90px;\">OK</td>"], ["<td class=\"td-content text-center\" applicate_date=", " style=\"width:90px;\">OK</td>"]),
    _templateObject3 = _taggedTemplateLiteral(["<td class=\"td-content text-center manager_confirm\" style=\"width:90px;\">\n\t\t\t\t\t\t \t\t\t\t<input type=\"radio\" name=\"manager_confirm_", "\" id=\"manager_confirm_", "_ok\" class=\"ui-widget-content\" value=\"1\">\n\t\t\t\t\t\t \t\t\t\t<label for=\"manager_confirm_", "_ok\">OK</label>\n\t\t\t\t\t\t \t\t\t\t<input type=\"radio\" name=\"manager_confirm_", "\" id=\"manager_confirm_", "_ng\" class=\"ui-widget-content\" value=\"0\">\n\t\t\t\t\t\t \t\t\t\t<label for=\"manager_confirm_", "_ng\">NG</label>\n\t\t\t\t\t\t \t\t   </td>"], ["<td class=\"td-content text-center manager_confirm\" style=\"width:90px;\">\n\t\t\t\t\t\t \t\t\t\t<input type=\"radio\" name=\"manager_confirm_", "\" id=\"manager_confirm_", "_ok\" class=\"ui-widget-content\" value=\"1\">\n\t\t\t\t\t\t \t\t\t\t<label for=\"manager_confirm_", "_ok\">OK</label>\n\t\t\t\t\t\t \t\t\t\t<input type=\"radio\" name=\"manager_confirm_", "\" id=\"manager_confirm_", "_ng\" class=\"ui-widget-content\" value=\"0\">\n\t\t\t\t\t\t \t\t\t\t<label for=\"manager_confirm_", "_ng\">NG</label>\n\t\t\t\t\t\t \t\t   </td>"]),
    _templateObject4 = _taggedTemplateLiteral(["<td class=\"td-content text-center manager_confirm\" manager_confirm=\"1\" style=\"width:90px;\">OK</td>"], ["<td class=\"td-content text-center manager_confirm\" manager_confirm=\"1\" style=\"width:90px;\">OK</td>"]),
    _templateObject5 = _taggedTemplateLiteral(["<td class=\"td-content\"style=\"width:90px;\"></td>"], ["<td class=\"td-content\"style=\"width:90px;\"></td>"]);

function _taggedTemplateLiteral(strings, raw) { return Object.freeze(Object.defineProperties(strings, { raw: { value: Object.freeze(raw) } })); }

var servicePath = "device_jig_order.do";

$(function () {
	$("input.ui-button").button();
	/* 为每一个匹配的元素的特定事件绑定一个事件处理函数 */
	$("#search span.ui-icon").bind("click", function () {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});
	// 申请者
	setReferChooser($("#search_applicator_id"), $("#operator_id_referchooser"));
	// 询价,验收
	$("#order_invoice_flg,#inline_recept_flg,#colchooser").buttonset();
	// 日期
	$("#search_send_date_start,#search_send_date_end,#search_scheduled_date_start,#search_scheduled_date_end,#search_recept_date_start,#search_recept_date_end,#recept_update_reorder_scheduled_date").datepicker({
		showButtonPanel: true,
		dateFormat: "yy/mm/dd",
		currentText: "今天"
	});

	$("#add_device_spare_type").select2Buttons();

	// 清除
	$("#resetbutton").click(reset);

	// 检索
	$("#searchbutton").click(findit);

	$("#colchooser input:checkbox").click(function () {
		var colid = $(this).attr("id");
		var colchk = $(this).attr("checked");
		var colsname = void 0;
		switch (colid) {
			case "colchooser_quotation":
				colsname = ['acquire_date', 'delivery_osh_date'];
				break;
			case "colchooser_buy":
				colsname = ['system_code', 'name', 'order_from_name', 'nesssary_reason'];
				break;
			case "colchooser_invoice":
				colsname = ['order_price', 'differ_price', 'send_date'];
				break;
			case "colchooser_recept":
				colsname = ['recept_date', 'inline_receptor_operator_name', 'budget_month'];
				break;
			default:
				return;
		}
		if ("checked" === colchk) {
			$("#list").jqGrid('showCol', colsname);
		} else {
			$("#list").jqGrid('hideCol', colsname);
		}
		$("#list").jqGrid('setGridWidth', '992');
	});

	$("#colchooser input").attr("checked", false).trigger("change");
	// 申请
	$("#applicationbutton").click(application);
	// 对象类别选择事件
	$(document).on("change", "#order_detail tbody select.object_type", objectTypeChange);
	// 型号/规格回车事件
	$(document).on("keypress", "#order_detail tbody input.model_name", modelNameKeypress);
	// 型号/规格change事件
	$(document).on("change", "#order_detail tbody input.model_name", modelNameChange);
	// 删除事件
	$(document).on("click", "#order_detail tbody input.subtract", subClick);
	// 判断数据是否修改过
	$(document).on("change", "#order_detail tr.unchanged input,#order_detail tr.unchanged select,#order_detail tr.change input,#order_detail tr.change select", function () {
		var $tr = $(this).closest("tr");
		var iDiff = 0;
		$tr.find("td[value]").each(function (index, item) {
			var originalValue = $(item).attr("value");
			var value = $(item).find("input,select").val();
			originalValue != value && iDiff++;
		});

		iDiff == 0 ? $tr.removeClass().addClass("unchanged") : $tr.removeClass().addClass("change");
	});

	$(document).on("change", "#order_detail input.device_type_id", deviceTypeIdChange);

	// 询价
	$("#invoicebutton").click(invoice);
	// 应用
	$(document).on("click", "#invoice_detail tbody input.applicate", applicateDetail);

	$("#applicat_list thead input[type='checkbox']").click(function () {
		$("#applicat_list tbody input[type='checkbox']").prop("checked", this.checked);
	});

	$(document).on("click", "#applicat_list tbody input[type='checkbox']", function () {
		var arr = $("#applicat_list tbody input[type='checkbox']").toArray();
		var checkAll = arr.every(function (item) {
			return item.checked == true;
		});
		$("#applicat_list thead input[type='checkbox']").prop("checked", checkAll);
	});
	//报价发送
	$("#quotationsendbutton").click(quotationSend);

	$("#quotationsend tbody").on("click", "input[type='checkbox'][order_no]", function () {
		var order_no = $(this).attr("order_no");
		$("#quotationsend tbody input[type='checkbox'][for='" + order_no + "']").prop("checked", this.checked);
	});

	// 报价追踪
	$("#quotationtrackbutton").click(quotationTrack);
	// 验收
	$("#inlinereceptbutton").disable().click(inlineRecept);
	// 预算
	$("#budgetbutton").disable().click(budget);

	var today = new Date();

	$("#budget_update_budget_month").monthpicker({
		pattern: "yyyymm",
		startYear: 2018,
		finalYear: today.getFullYear(),
		selectedMonth: today.getMonth() + 1,
		monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
	});

	$("#budget_update_budget_month_clearer").click(function (evt) {
		$("#budget_update_budget_month").val("");
	});

	findit();
});
function reset() {
	$("#searchform input[type='text']").val("");
	$("#searchform input[type='hidden']").val("");
	$("#search_order_invoice_all").attr("checked", true).trigger("change");
	$("#search_inline_recept_yes").attr("checked", true).trigger("change");
};
function findit() {
	var data = {
		"quotation_no": $("#search_quotation_no").val(), // 报价单号
		"order_no": $("#search_order_no").val(), // 订单号
		"model_name": $("#search_model_name").val(), // 订购品
		"applicator_id": $("#search_applicator_id").val(), // 申请者
		"order_invoice_flg": $("#order_invoice_flg input:checked").val(), // 询价
		"send_date_start": $("#search_send_date_start").val(), // 询价发送开始日期
		"send_date_end": $("#search_send_date_end").val(), // 询价发送结束日期
		"scheduled_date_start": $("#search_scheduled_date_start").val(), // 预计开始纳期
		"scheduled_date_end": $("#search_scheduled_date_end").val(), // 预计结束纳期
		"recept_date_start": $("#search_recept_date_start").val(), // 收货开始时间
		"recept_date_end": $("#search_recept_date_end").val(), // 收货结束时间
		"inline_recept_flg": $("#inline_recept_flg input:checked").val() // 验收
	};
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=search',
		cache: false,
		data: data,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					list(resInfo.orderList);
				}
			} catch (e) {}
		}
	});
};
//验收
function inlineRecept() {
	var rowId = $("#list").jqGrid("getGridParam", "selrow");
	var rowData = $("#list").getRowData(rowId);

	var postData = {
		"order_key": rowData.order_key, // 订购单KEY
		"object_type": rowData.object_type, // 对象类别
		"device_type_id": rowData.device_type_id, //设备ID
		"model_name": rowData.model_name, // 型号/规格
		"applicator_id": rowData.applicator_id // 申请人
	};

	var loginID = $("#loginID").val().trim();

	//如果applicator_id不是登录者
	if (rowData.applicator_id != loginID) {
		var warningMessage = "\u8FD9\u662F" + rowData.applicator_operator_name + "\u7533\u8BF7\u7684\uFF0C\u662F\u5426\u7531\u60A8\u6765\u9A8C\u6536\u3002";
		warningConfirm(warningMessage, function () {
			doUpdateRecept(postData);
		}, function () {});
	} else {
		doUpdateRecept(postData);
	}

	function doUpdateRecept(data) {
		$.ajax({
			beforeSend: ajaxRequestType,
			async: true,
			url: servicePath + '?method=doUpdateRecept',
			cache: false,
			data: data,
			type: "post",
			dataType: "json",
			success: ajaxSuccessCheck,
			error: ajaxError,
			complete: function complete(xhrobj, textStatus) {
				var resInfo = null;
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					if (resInfo.errors.length > 0) {
						// 共通出错信息框
						treatBackMessages(null, resInfo.errors);
					} else {
						findit();
					}
				} catch (e) {}
			}
		});
	}
};
function budget() {
	var rowId = $("#list").jqGrid("getGridParam", "selrow");
	var rowData = $("#list").getRowData(rowId);

	//型号/规格
	$("#budget_label_model_name").text(rowData.model_name);
	//系统编码
	$("#budget_label_system_code").text(rowData.system_code);
	//名称
	$("#budget_label_name").text(rowData.name);
	//数量
	$("#budget_label_quantity").text(rowData.quantity);
	//理由/必要性
	$("#budget_label_nesssary_reason").text(rowData.nesssary_reason);
	//申请日期
	$("#budget_label_applicate_date").text(rowData.applicate_date);
	//总价
	$("#budget_label_total_order_price").text(rowData.total_order_price);
	//订单号
	$("#budget_label_order_no").text(rowData.order_no);
	//预算月
	$("#budget_update_budget_month").val(rowData.budget_month);
	//预算说明
	$("#budget_update_budget_description").val(rowData.budget_description);

	var $dialog = $("#budget_dialog").dialog({
		title: "预算",
		width: 350,
		height: 'auto',
		resizable: false,
		modal: true,
		buttons: {
			"确定": function _() {
				var postData = {
					"order_key": rowData.order_key, // 订购单KEY
					"object_type": rowData.object_type, // 对象类别
					"device_type_id": rowData.device_type_id, //设备ID
					"model_name": rowData.model_name, // 型号/规格
					"applicator_id": rowData.applicator_id, // 申请人
					"budget_month": $("#budget_update_budget_month").val(),
					"budget_description": $("#budget_update_budget_description").val()
				};

				$.ajax({
					beforeSend: ajaxRequestType,
					async: true,
					url: servicePath + '?method=doUpdateBudget',
					cache: false,
					data: postData,
					type: "post",
					dataType: "json",
					success: ajaxSuccessCheck,
					error: ajaxError,
					complete: function complete(xhrobj, textStatus) {
						var resInfo = null;
						try {
							// 以Object形式读取JSON
							eval('resInfo =' + xhrobj.responseText);
							if (resInfo.errors.length > 0) {
								// 共通出错信息框
								treatBackMessages(null, resInfo.errors);
							} else {
								$dialog.dialog('close');
								findit();
							}
						} catch (e) {}
					}
				});
			},
			"关闭": function _() {
				$(this).dialog('close');
			}
		}
	});
};
//报价追踪
function quotationTrack() {
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: 'device_jig_quotation.do?method=searchAll',
		cache: false,
		data: null,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					quotationTrackList(resInfo.allQuotationList);
				}
			} catch (e) {}
		}
	});
};
function quotationTrackList(listdata) {
	if ($("#gbox_quotationlist").length > 0) {
		$("#quotationlist").jqGrid().clearGridData(); // 清除
		$("#quotationlist").jqGrid('setGridParam', { data: listdata }).trigger("reloadGrid", [{ current: false }]); // 刷新列表
	} else {
		$("#quotationlist").jqGrid({
			data: listdata, // 数据
			height: 461, // rowheight*rowNum+1
			width: 450,
			rowheight: 23,
			shrinkToFit: true,
			datatype: "local",
			colNames: ['报价单号', '确认<br>接收日期', '委托<br>单号', '委托<br>发送日期', '发送<br>OSH日期', '预计纳期', '有无<br>备注', 'comment', 'quotation_id'],
			colModel: [{ name: 'quotation_no', index: 'quotation_no', width: 100 }, { name: 'acquire_date', index: 'acquire_date', width: 70, align: 'center', sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'entrust_no', index: 'entrust_no', width: 80, hidden: true }, { name: 'entrust_send_date', index: 'entrust_send_date', width: 70, align: 'center', sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' }, hidden: true }, { name: 'delivery_osh_date', index: 'delivery_osh_date', width: 70, align: 'center', sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'scheduled_date', index: 'scheduled_date', width: 70, align: 'center', sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'comment_flg', index: 'comment_flg', width: 40, align: 'center', formatter: function formatter(value, option, rData) {
					if (rData.comment) {
						return "有";
					}
					return "无";
				} }, { name: 'comment', index: 'comment', hidden: true }, { name: 'quotation_id', index: 'quotation_id', hidden: true }],
			rowNum: 30,
			toppager: false,
			pager: "#quotationlistpager",
			viewrecords: true,
			caption: "",
			multiselect: false,
			gridview: true,
			pagerpos: 'right',
			pgbuttons: true, // 翻页按钮
			rownumbers: true,
			pginput: false,
			recordpos: 'left',
			hidegrid: false,
			deselectAfterSort: false,
			onSelectRow: function onSelectRow(rowid, statue) {

				//编辑报价按钮可用
				$("#quotationtrack_dialog").next().find("div.ui-dialog-buttonset button:eq(0)").enable();

				searchOrderDetailInQuotationList(rowid);
			},
			ondblClickRow: null,
			viewsortcols: [true, 'vertical', true],
			gridComplete: function gridComplete() {
				$("#quotationtrack_dialog").next().find("div.ui-dialog-buttonset button:eq(0)").disable();
				orderQuotationList([]);
			}
		});
	}

	orderQuotationList([]);

	var $parentDialog = $("#quotationtrack_dialog");
	if ($parentDialog.length == 0 || !$parentDialog.is(":visible")) {
		$parentDialog.dialog({
			title: "报价追踪",
			width: 1200,
			height: 'auto',
			resizable: false,
			modal: true,
			show: "blind",
			buttons: {
				"编辑报价": function _() {
					showQuotation($parentDialog);
				},
				"确认收货": function _() {
					confirmRecept($parentDialog);
				},
				"关闭": function _() {
					$parentDialog.dialog('close');
				}
			}
		});
	}

	$("#quotationtrack_dialog").next().find("div.ui-dialog-buttonset button:nth-last-child(n+2)").disable();
};

function searchOrderDetailInQuotationList(rowId) {
	if (!rowId) return;

	var rowData = $("#quotationlist").getRowData(rowId);
	var postData = {
		"quotation_id": rowData.quotation_id
	};

	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=searchOrderDetail',
		cache: false,
		data: postData,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					orderQuotationList(resInfo.detailList);
				}
			} catch (e) {}
		}
	});
}

function orderQuotationList(listdata) {
	if ($("#gbox_order_quotationlist").length > 0) {
		$("#order_quotationlist").jqGrid().clearGridData(); // 清除
		$("#order_quotationlist").jqGrid('setGridParam', { data: listdata }).trigger("reloadGrid", [{ current: false }]); // 刷新列表
	} else {
		$("#order_quotationlist").jqGrid({
			data: listdata, // 数据
			height: 461, // rowheight*rowNum+1
			width: 720,
			rowheight: 23,
			shrinkToFit: true,
			datatype: "local",
			colNames: ['对象<br>类别', '品名', '型号/规格', '系统<br>编码', '名称', '受注方', '数量', '申请人', '确认结果', '确认<br>数量', 'confirm_flg', 'order_key', 'object_type', 'device_type_id', 'applicator_id', 'scheduled_date', 'reorder_scheduled_date', 'recept_date', 'order_no'],
			colModel: [{ name: 'object_type_name', index: 'object_type_name', width: 100 }, { name: 'device_type_name', index: 'device_type_name', width: 100 }, { name: 'model_name', index: 'model_name', width: 100 }, { name: 'system_code', index: 'system_code', width: 100 }, { name: 'name', index: 'name', width: 100 }, { name: 'order_from_name', index: 'order_from_name', width: 100 }, { name: 'quantity', index: 'quantity', width: 100, align: 'right', sorttype: 'number' }, { name: 'applicator_operator_name', index: 'applicator_operator_name', width: 100 }, { name: 'confirm_flg_name', index: 'confirm_flg_name', width: 100, align: 'center' }, { name: 'confirm_quantity', index: 'confirm_quantity', width: 100, align: 'right', sorttype: 'number' }, { name: 'confirm_flg', index: 'confirm_flg', hidden: true }, { name: 'order_key', index: 'order_key', hidden: true }, { name: 'object_type', index: 'object_type', hidden: true }, { name: 'device_type_id', index: 'device_type_id', hidden: true }, { name: 'applicator_id', index: 'applicator_id', hidden: true }, { name: 'scheduled_date', index: 'scheduled_date', hidden: true }, { name: 'reorder_scheduled_date', index: 'reorder_scheduled_date', hidden: true }, { name: 'recept_date', index: 'recept_date', hidden: true }, { name: 'order_no', index: 'order_no', hidden: true }],
			rowNum: 30,
			toppager: false,
			pager: "#order_quotationlistpager",
			viewrecords: true,
			caption: "",
			multiselect: false,
			gridview: true,
			pagerpos: 'right',
			pgbuttons: true, // 翻页按钮
			rownumbers: true,
			pginput: false,
			recordpos: 'left',
			hidegrid: false,
			deselectAfterSort: false,
			onSelectRow: function onSelectRow(rowid, statue) {
				var rowData = $("#order_quotationlist").getRowData(rowid);
				// 确认结果
				var confirm_flg = rowData.confirm_flg.trim();
				// 预计纳期
				var scheduled_date = rowData.scheduled_date.trim();
				//确认收货按钮
				var $confirmReceptBtn = $("#quotationtrack_dialog").next().find("div.ui-dialog-buttonset button:eq(1)");

				//存在预计纳期
				if (scheduled_date) {
					//确认结果OK
					if (confirm_flg == 1) {
						$confirmReceptBtn.disable();
					} else {
						$confirmReceptBtn.enable();
					}
				} else {
					$confirmReceptBtn.disable();
				}
			},
			ondblClickRow: null,
			viewsortcols: [true, 'vertical', true],
			gridComplete: function gridComplete() {
				$("#quotationtrack_dialog").next().find("div.ui-dialog-buttonset button:eq(1)").disable();
			}
		});
	}

	$("#quotationtrack_dialog").next().find("div.ui-dialog-buttonset button:eq(1)").disable();
};
function showQuotation($parentDialog) {
	var rowId = $("#quotationlist").jqGrid("getGridParam", "selrow");
	if (!rowId) {
		errorPop("请选中一条报价记录。");
		return;
	}

	var rowData = $("#quotationlist").getRowData(rowId);
	var postData = {
		"quotation_id": rowData.quotation_id
	};

	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: 'device_jig_quotation.do?method=searchQuotationById',
		cache: false,
		data: postData,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var quotationForm = resInfo.quotationForm,
					    $table = $("#quotation_detail table");

					$table.find("tr:eq(0) td:eq(1)").text(quotationForm.quotation_no);
					// 确认接收日期
					if (quotationForm.acquire_date) {
						$table.find("tr:eq(1) td:eq(1)").text(quotationForm.acquire_date);
					} else {
						$table.find("tr:eq(1) td:eq(1)").html("<input type=\"text\" class=\"ui-widget-content acquire_date\" readonly=\"readonly\"></td>");
					}
					// 发送OSH日期
					if (quotationForm.delivery_osh_date) {
						$table.find("tr:eq(2) td:eq(1)").text(quotationForm.delivery_osh_date);
					} else {
						$table.find("tr:eq(2) td:eq(1)").html("<input type=\"text\" class=\"ui-widget-content delivery_osh_date\" readonly=\"readonly\"></td>");
					}
					// 预计纳期
					$("#update_scheduled_date").val(quotationForm.scheduled_date);
					// 备注
					$("#update_quotation_comment").val(quotationForm.comment);

					$("#quotation_detail table input[readonly]").datepicker({
						showButtonPanel: true,
						dateFormat: "yy/mm/dd",
						currentText: "今天"
					});

					var $childDialog = $("#quotation_detail").dialog({
						title: "报价编辑",
						width: 450,
						height: 320,
						resizable: false,
						modal: true,
						buttons: {
							"确定": function _() {
								var postData = {
									"quotation_id": rowData.quotation_id, // 报价单KEY
									"scheduled_date": $("#update_scheduled_date").val().trim(), // 预计纳期
									"comment": $("#update_quotation_comment").val().trim() // 备注

									// 确认接收日期
								};if ($table.find("input.acquire_date").length == 1) {
									postData["acquire_date"] = $table.find("input.acquire_date").val().trim();
								}
								// 发送OSH日期
								if ($table.find("input.delivery_osh_date").length == 1) {
									postData["delivery_osh_date"] = $table.find("input.delivery_osh_date").val().trim();
								}

								$.ajax({
									beforeSend: ajaxRequestType,
									async: true,
									url: 'device_jig_quotation.do?method=doUpdate',
									cache: false,
									data: postData,
									type: "post",
									dataType: "json",
									success: ajaxSuccessCheck,
									error: ajaxError,
									complete: function complete(xhrobj, textStatus) {
										var resInfo = null;
										try {
											// 以Object形式读取JSON
											eval('resInfo =' + xhrobj.responseText);
											if (resInfo.errors.length > 0) {
												// 共通出错信息框
												treatBackMessages(null, resInfo.errors);
											} else {
												$childDialog.dialog('close');
												quotationTrack();
												findit();
											}
										} catch (e) {}
									}
								});
							},
							"关闭": function _() {
								$(this).dialog('close');
							}
						}
					});
				}
			} catch (e) {}
		}
	});
};
//确认收货
function confirmRecept($parentDialog) {
	var rowId = $("#order_quotationlist").jqGrid("getGridParam", "selrow");
	var rowData = $("#order_quotationlist").getRowData(rowId);
	// 确认结果
	var confirm_flg = rowData.confirm_flg.trim();
	// 重新订购纳期
	var reorder_scheduled_date = rowData.reorder_scheduled_date.trim();
	// 收货时间
	var recept_date = rowData.recept_date.trim();

	$("#recept_update_confirm_flg").html($("#hide_confirm_flg").html());

	//已经选择过确认结果，移除不选项
	confirm_flg && $("#recept_update_confirm_flg option:eq(0)").remove();

	$("#recept_update_confirm_flg").unbind("change").bind("change", function () {
		var value = this.value;
		var $tr = $("#recept_update_reorder_scheduled_date").closest("tr");

		value == 2 ? $tr.show() : $tr.hide();
	}).select2Buttons().val(confirm_flg).trigger("change");

	$("#recept_update_reorder_scheduled_date").val(reorder_scheduled_date);
	//	$("#recept_update_recept_date").val(recept_date);

	var buttons = {};
	if (rowData.object_type == 1 || rowData.object_type == 3) {
		//设备/一般工具
		buttons["收货到备品库"] = function () {
			$childDialog.dialog('close');
			addSpare(rowData);
		};
	}
	buttons["确定"] = function () {
		updateConfirm(rowData, $childDialog);
	};
	buttons["关闭"] = function () {
		$(this).dialog('close');
	};

	var $childDialog = $("#update_recept").dialog({
		title: "收货确认",
		width: 350,
		height: 240,
		resizable: false,
		modal: true,
		buttons: buttons
	});
};
//更新收货确认
function updateConfirm(rowData) {
	for (var _len = arguments.length, arrDialog = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
		arrDialog[_key - 1] = arguments[_key];
	}

	var postData = {
		"order_key": rowData.order_key, // 订购单KEY
		"object_type": rowData.object_type, // 对象类别
		"device_type_id": rowData.device_type_id, //设备ID
		"model_name": rowData.model_name, // 型号/规格
		"applicator_id": rowData.applicator_id, // 申请人
		"confirm_flg": $("#recept_update_confirm_flg").val(), // 确认结果
		//		"recept_date" : $("#recept_update_recept_date").val().trim(),// 收货时间
		"reorder_scheduled_date": $("#recept_update_reorder_scheduled_date").val().trim() // 重新订购纳期
	};

	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=doUpdateConfirm',
		cache: false,
		data: postData,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					arrDialog.forEach(function (item) {
						return item.dialog('close');
					});
					searchOrderDetailInQuotationList($("#quotationlist").getGridParam("selrow"));
					findit();
				}
			} catch (e) {}
		}
	});
};
//加入备品
function addSpare(rowData) {
	for (var _len2 = arguments.length, arrDialog = Array(_len2 > 1 ? _len2 - 1 : 0), _key2 = 1; _key2 < _len2; _key2++) {
		arrDialog[_key2 - 1] = arguments[_key2];
	}

	$("#add_spare_confirm_quantity").val("");
	$("#add_device_spare_type").val("").trigger("change");

	var $childDialog = $("#add_spare").dialog({
		title: "作爲备品收貨",
		width: 350,
		height: 200,
		resizable: false,
		modal: true,
		buttons: {
			"确定": function _() {
				var postData = {
					"order_key": rowData.order_key, // 订购单KEY
					"object_type": rowData.object_type, // 对象类别
					"device_type_id": rowData.device_type_id, // 品名
					"model_name": rowData.model_name, // 型号/规格
					"applicator_id": rowData.applicator_id, // 申请者
					"device_type_name": rowData.device_type_name,
					"order_no": rowData.order_no,
					"confirm_quantity": $("#add_spare_confirm_quantity").val().trim(), // 确认数量
					"device_spare_type": $("#add_device_spare_type").val() // 备品种类
				};

				$.ajax({
					beforeSend: ajaxRequestType,
					async: true,
					url: servicePath + '?method=doAddSpare',
					cache: false,
					data: postData,
					type: "post",
					dataType: "json",
					success: ajaxSuccessCheck,
					error: ajaxError,
					complete: function complete(xhrobj, textStatus) {
						var resInfo = null;
						try {
							// 以Object形式读取JSON
							eval('resInfo =' + xhrobj.responseText);
							if (resInfo.errors.length > 0) {
								// 共通出错信息框
								treatBackMessages(null, resInfo.errors);
							} else {
								$childDialog.dialog('close');
								arrDialog.forEach(function (item) {
									return item.dialog('close');
								});
								searchOrderDetailInQuotationList($("#quotationlist").getGridParam("selrow"));
								findit();
							}
						} catch (e) {}
					}
				});
			},
			"关闭": function _() {
				$(this).dialog('close');
			}
		}
	});
};
//报价发送
function quotationSend() {
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=searchQuotationSend',
		cache: false,
		data: null,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var unQuotationList = resInfo.unQuotationList;
					var map = new Map();
					unQuotationList.forEach(function (item) {
						var order_no = item.order_no;
						if (map.has(order_no)) {
							var arr = map.get(order_no);
							arr.push(item);
							map.set(order_no, arr);
						} else {
							map.set(order_no, Array.of(item));
						}
					});

					var content = "";
					var _iteratorNormalCompletion = true;
					var _didIteratorError = false;
					var _iteratorError = undefined;

					try {
						var _loop = function _loop() {
							var item = _step.value;

							var arr = item[1];
							var rowspan = arr.length;

							arr.forEach(function (obj, index) {
								// 订单号
								var order_no = obj.order_no;
								// 订单KEY
								var order_key = obj.order_key;
								// 设备ID
								var device_type_id = obj.device_type_id;
								// 申请人ID
								var applicator_id = obj.applicator_id;
								// 申请日期
								var applicate_date = obj.applicate_date || '';
								// 对象类别
								var object_type = obj.object_type;
								// 对象类别名称
								var object_type_name = obj.object_type_name;
								// 型号/规格
								var model_name = obj.model_name;
								// 系统编码
								var system_code = obj.system_code || '';
								// 名称
								var name = obj.name;
								// 受注方名称
								var order_from_name = obj.order_from_name || '';
								// 数量
								var quantity = obj.quantity;
								// 价格
								var order_price = obj.order_price || '';
								// 总价
								var total_order_price = obj.total_order_price || '';
								// 日本价格
								var origin_price = obj.origin_price || '';

								var checkbox = "";
								if (applicate_date && order_price) {
									checkbox = "<input type=\"checkbox\" for=\"" + order_no + "\">";
								}

								if (index == 0) {
									//每组第一行
									content += "<tr order_key=\"" + order_key + "\" object_type=\"" + object_type + "\" device_type_id=\"" + device_type_id + "\" model_name=\"" + model_name + "\" applicator_id=\"" + applicator_id + "\">\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\" rowspan=\"" + rowspan + "\">\n\t\t\t\t\t\t\t\t\t\t\t\t\t<input type=\"checkbox\" order_no=\"" + order_no + "\"><br>" + order_no + "\n\t\t\t\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\">" + checkbox + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + object_type_name + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + model_name + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + system_code + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + name + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + order_from_name + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + quantity + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + order_price + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + total_order_price + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + origin_price + "</td>\n\t\t\t\t\t\t\t\t\t\t   </tr>";
								} else {
									content += "<tr order_key=\"" + order_key + "\" object_type=\"" + object_type + "\" device_type_id=\"" + device_type_id + "\" model_name=\"" + model_name + "\" applicator_id=\"" + applicator_id + "\">\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\">" + checkbox + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + object_type_name + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + model_name + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + system_code + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + name + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + order_from_name + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + quantity + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + order_price + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + total_order_price + "</td>\n\t\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + origin_price + "</td>\n\t\t\t\t\t\t\t\t\t\t\t</tr>";
								}
							});
						};

						for (var _iterator = map[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
							_loop();
						}
					} catch (err) {
						_didIteratorError = true;
						_iteratorError = err;
					} finally {
						try {
							if (!_iteratorNormalCompletion && _iterator.return) {
								_iterator.return();
							}
						} finally {
							if (_didIteratorError) {
								throw _iteratorError;
							}
						}
					}

					$("#quotationsend tbody").html(content);

					var $parentDialog = $("#quotationsend").dialog({
						title: "报价发送",
						width: 1000,
						height: 600,
						resizable: false,
						modal: true,
						show: 'blind',
						buttons: {
							"确定": function _() {
								var checked = $("#quotationsend tbody input[type='checkbox'][for]").toArray().some(function (item) {
									return item.checked == true;
								});
								if (!checked) {
									errorPop("请至少选择一条记录。");
									return;
								}

								var postData = {};
								var ii = 0;
								$("#quotationsend tbody tr").each(function (index, tr) {
									var $tr = $(tr);
									var checked = $tr.find("input[type='checkbox'][for]").prop("checked");
									if (checked) {
										postData["device_jig_order_detail.order_key[" + ii + "]"] = $tr.attr("order_key");
										postData["device_jig_order_detail.object_type[" + ii + "]"] = $tr.attr("object_type");
										postData["device_jig_order_detail.device_type_id[" + ii + "]"] = $tr.attr("device_type_id");
										postData["device_jig_order_detail.model_name[" + ii + "]"] = $tr.attr("model_name");
										postData["device_jig_order_detail.applicator_id[" + ii + "]"] = $tr.attr("applicator_id");
										ii++;
									}
								});

								$("#add_quotation_no").val("");
								$("#add_quotation_comment").val("");

								var $childDialog = $("#quotation_dialog").dialog({
									title: "订购报价",
									width: 500,
									height: 220,
									resizable: false,
									modal: true,
									buttons: {
										"确定": function _() {
											postData["quotation_no"] = $("#add_quotation_no").val().trim();
											postData["comment"] = $("#add_quotation_comment").val().trim();

											$.ajax({
												beforeSend: ajaxRequestType,
												async: true,
												url: servicePath + '?method=doUpdateQuotation',
												cache: false,
												data: postData,
												type: "post",
												dataType: "json",
												success: ajaxSuccessCheck,
												error: ajaxError,
												complete: function complete(xhrobj, textStatus) {
													var resInfo = null;
													try {
														// 以Object形式读取JSON
														eval('resInfo =' + xhrobj.responseText);
														if (resInfo.errors.length > 0) {
															// 共通出错信息框
															treatBackMessages(null, resInfo.errors);
														} else {
															$childDialog.dialog('close');
															$parentDialog.dialog('close');
															findit();
														}
													} catch (e) {}
												}
											});
										},
										"关闭": function _() {
											$(this).dialog('close');
										}
									}
								});
							},
							"关闭": function _() {
								$(this).dialog('close');
							}
						}
					});
				}
			} catch (e) {}
		}
	});
};
//应用
function applicateDetail() {
	var $tr = $(this).closest("tr");
	var data = {
		"confirm_flg_name": $tr.attr("class"),
		"object_type": $tr.attr("object_type"),
		"device_type_id": $tr.attr("device_type_id"),
		"model_name": $tr.attr("model_name"),
		"order_invoice_id": $tr.attr("invoice_id"),
		"send_date": $tr.attr("send_date") || $tr.find("input.send_date").val().trim(),
		"order_price": $tr.find("input.order_price").val().trim(),
		"origin_price": $tr.find("input.origin_price").val().trim()
	};

	$tr.prev().length == 1 ? data["old_send_date"] = $tr.prev().attr("send_date") : data["old_send_date"] = "";

	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=searchApplicate',
		cache: false,
		data: data,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var detailList = resInfo.detailList;
					applicateList(detailList, data);
				}
			} catch (e) {}
		}
	});
};
function applicateList(list, postData) {
	var content = '';
	list.forEach(function (item) {
		// 订单号
		var order_no = item.order_no;
		// 系统编码
		var system_code = item.system_code || '';
		// 名称
		var name = item.name;
		// 受注方
		var order_from_name = item.order_from_name || '';
		// 数量
		var quantity = item.quantity;
		// 申请人ID
		var applicator_id = item.applicator_id;
		// 申请人名称
		var applicator_operator_name = item.applicator_operator_name;
		// 申请日期
		var applicate_date = item.applicate_date || '等待确认申请';

		content += "<tr order_key=\"" + item.order_key + "\" object_type=\"" + item.object_type + "\" device_type_id=\"" + item.device_type_id + "\" model_name=\"" + item.model_name + "\" applicator_id=\"" + item.applicator_id + "\">\n\t\t\t\t\t\t<td class=\"td-content text-center\"><input type=\"checkbox\"></td>\n\t\t\t\t\t\t<td class=\"td-content\">" + order_no + "</td>\n\t\t\t\t\t\t<td class=\"td-content\">" + system_code + "</td>\n\t\t\t\t\t\t<td class=\"td-content\">" + name + "</td>\n\t\t\t\t\t\t<td class=\"td-content\">" + order_from_name + "</td>\n\t\t\t\t\t\t<td class=\"td-content text-right\">" + quantity + "</td>\n\t\t\t\t\t\t<td class=\"td-content\">" + applicator_operator_name + "</td>\n\t\t\t\t\t\t<td class=\"td-content text-center\">" + applicate_date + "</td>\n\t\t\t\t\t</tr>";
	});

	$("#applicat_list tbody").html(content);
	$("#applicat_list thead input[type='checkbox']").prop("checked", false);

	var thisDialog = $("#applicat_list").dialog({
		title: '订购明细',
		width: 800,
		height: 400,
		resizable: false,
		modal: true,
		buttons: {
			"确定": function _() {
				var data = {
					"confirm_flg_name": postData.confirm_flg_name,
					"object_type": postData.object_type,
					"device_type_id": postData.device_type_id,
					"model_name": postData.model_name,
					"order_invoice_id": postData.order_invoice_id,
					"send_date": postData.send_date,
					"old_send_date": postData.old_send_date,
					"order_price": postData.order_price,
					"origin_price": postData.origin_price
				};

				var ii = -1;
				$("#applicat_list tbody tr").each(function (index, tr) {
					var $tr = $(tr);
					var checked = $tr.find("input[type='checkbox']").prop("checked");
					if (checked) {
						ii++;
						data["device_jig_order_detail.order_key[" + ii + "]"] = $tr.attr("order_key");
						data["device_jig_order_detail.object_type[" + ii + "]"] = $tr.attr("object_type");
						data["device_jig_order_detail.device_type_id[" + ii + "]"] = $tr.attr("device_type_id");
						data["device_jig_order_detail.model_name[" + ii + "]"] = $tr.attr("model_name");
						data["device_jig_order_detail.applicator_id[" + ii + "]"] = $tr.attr("applicator_id");
					}
				});

				if (ii == -1) {
					errorPop("请至少选择一条记录。");
					return;
				}

				$.ajax({
					beforeSend: ajaxRequestType,
					async: true,
					url: servicePath + '?method=doUpdateInvoice',
					cache: false,
					data: data,
					type: "post",
					dataType: "json",
					success: ajaxSuccessCheck,
					error: ajaxError,
					complete: function complete(xhrobj, textStatus) {
						var resInfo = null;
						try {
							// 以Object形式读取JSON
							eval('resInfo =' + xhrobj.responseText);
							if (resInfo.errors.length > 0) {
								// 共通出错信息框
								treatBackMessages(null, resInfo.errors);
							} else {
								thisDialog.dialog('close');invoice;
								$("#invoice_detail").dialog("close");
								$("#invoice").dialog("close");
								findit();
							}
						} catch (e) {}
					}
				});
			},
			"关闭": function _() {
				$(this).dialog('close');
			}
		}
	});
};
// 询价
function invoice() {
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=searchInvoice',
		cache: false,
		data: null,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					invoiceList(resInfo.invoiceList);
				}
			} catch (e) {}
		}
	});
};
function invoiceList(listdata) {
	if ($("#gbox_invoicelist").length > 0) {
		$("#invoicelist").jqGrid().clearGridData(); // 清除
		$("#invoicelist").jqGrid('setGridParam', { data: listdata }).trigger("reloadGrid", [{ current: false }]); // 刷新列表
	} else {
		$("#invoicelist").jqGrid({
			data: listdata, // 数据
			height: 461, // rowheight*rowNum+1
			width: 600,
			rowheight: 23,
			shrinkToFit: true,
			datatype: "local",
			colNames: ['对象类别', '品名', '型号/规格', '最近询价发送', 'object_type', 'device_type_id'],
			colModel: [{ name: 'object_type_name', index: 'object_type_name', width: 100 }, { name: 'device_type_name', index: 'device_type_name', width: 100, formatter: function formatter(value, options, rData) {
					if (!value) {
						return '-';
					}
					return value;
				} }, { name: 'model_name', index: 'model_name', width: 100 }, { name: 'send_date', index: 'send_date', width: 100, align: 'center', sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'object_type', index: 'object_type', hidden: true }, { name: 'device_type_id', index: 'device_type_id', hidden: true }],
			rowNum: 20,
			toppager: false,
			pager: "#invoicelistpager",
			viewrecords: true,
			caption: "",
			multiselect: false,
			gridview: true,
			pagerpos: 'right',
			pgbuttons: true, // 翻页按钮
			rownumbers: true,
			pginput: false,
			recordpos: 'left',
			hidegrid: false,
			deselectAfterSort: false,
			onSelectRow: function onSelectRow(id) {
				$("#invoice").next().find("div.ui-dialog-buttonset button:eq(0)").enable();
			},
			ondblClickRow: function ondblClickRow(rid, iRow, iCol, e) {},
			viewsortcols: [true, 'vertical', true],
			gridComplete: function gridComplete() {
				$("#invoice").next().find("div.ui-dialog-buttonset button:eq(0)").disable();
			}
		});
	}

	var $thisDialog = $("#invoice").dialog({
		title: "询价",
		width: 'auto',
		height: 'auto',
		resizable: false,
		modal: true,
		show: 'blind',
		buttons: {
			"编辑询价": function _() {
				var rowId = $("#invoicelist").jqGrid("getGridParam", "selrow");
				if (!rowId) {
					errorPop("请选中一条记录。");
					return;
				}
				var rowData = $("#invoicelist").getRowData(rowId);
				var data = {
					"object_type": rowData.object_type,
					"device_type_id": rowData.device_type_id,
					"model_name": rowData.model_name,
					"object_type_name": rowData.object_type_name,
					"device_type_name": rowData.device_type_name
				};
				invoiceDetail(data, $thisDialog);
			},
			"关闭": function _() {
				$(this).dialog('close');
			}
		}
	});

	$("#invoice").next().find("div.ui-dialog-buttonset button:eq(0)").disable();
};
function invoiceDetail(data, $parentDialog) {
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=searchInvoiceResult',
		cache: false,
		data: data,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					// 询价结果
					var deviceJigInvoiceForm = resInfo.deviceJigInvoiceForm;

					if (!deviceJigInvoiceForm) {
						$("#invoice_detail table tbody").html("");
					} else {
						var invoice_id = deviceJigInvoiceForm.invoice_id;
						var send_date = deviceJigInvoiceForm.send_date;
						var order_price = deviceJigInvoiceForm.order_price || '';
						var origin_price = deviceJigInvoiceForm.origin_price || '';
						var content = "";

						content = "<tr invoice_id=\"" + invoice_id + "\" send_date=\"" + send_date + "\" object_type=\"" + data.object_type + "\" device_type_id=\"" + data.device_type_id + "\" model_name=\"" + data.model_name + "\">\n\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\">" + send_date + "</td>\n\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\"><input type=\"text\" class=\"ui-widget-content order_price\" value=\"" + order_price + "\"></td>\n\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\"><input type=\"text\" class=\"ui-widget-content origin_price\" value=\"" + origin_price + "\"></td>\n\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\"><input type=\"button\" class=\"ui-button applicate\" value=\"\u5E94\u7528\"></td>\n\t\t\t\t\t\t\t\t   </tr>";

						$("#invoice_detail table tbody").html(content);
					}

					var dialogTitle = "\u5BF9\u8C61\u7C7B\u522B:[" + data.object_type_name + "],\u54C1\u540D:[" + data.device_type_name + "],\u578B\u53F7/\u89C4\u683C:[" + data.model_name + "]\u8BE2\u4EF7";

					var $thisDialog = $("#invoice_detail").dialog({
						title: dialogTitle,
						width: 600,
						height: 260,
						resizable: false,
						modal: true,
						buttons: {
							"确定": function _() {
								var len = $("#invoice_detail tbody tr").length;
								if (len == 0) {
									$thisDialog.dialog('close');
									$parentDialog.dialog('close');
								} else {
									var postData = {};
									var url = "";
									var $tr = "";

									if (len == 1) {
										$tr = $("#invoice_detail tbody tr:eq(0)");
									} else if (len == 2) {
										$tr = $("#invoice_detail tbody tr:eq(1)");
									}
									if ($tr.attr("invoice_id")) {
										url = "device_jig_invoice.do?method=doUpdatePrice";
										postData["invoice_id"] = $tr.attr("invoice_id");
									} else {
										url = "device_jig_invoice.do?method=doInsert";
										postData["object_type"] = data.object_type;
										postData["device_type_id"] = data.device_type_id;
										postData["model_name"] = data.model_name;
										postData["send_date"] = $tr.find("input.send_date").val().trim();
										if ($tr.prev()) {
											postData["old_send_date"] = $tr.prev().attr("send_date");
										}
									}
									postData["order_price"] = $tr.find("input.order_price").val().trim();
									postData["origin_price"] = $tr.find("input.origin_price").val().trim();

									$.ajax({
										beforeSend: ajaxRequestType,
										async: true,
										url: url,
										cache: false,
										data: postData,
										type: "post",
										dataType: "json",
										success: ajaxSuccessCheck,
										error: ajaxError,
										complete: function complete(xhrobj, textStatus) {
											var resInfo = null;
											try {
												// 以Object形式读取JSON
												eval('resInfo =' + xhrobj.responseText);
												if (resInfo.errors.length > 0) {
													// 共通出错信息框
													treatBackMessages(null, resInfo.errors);
												} else {
													$thisDialog.dialog('close');
													$parentDialog.dialog('close');
													findit();
												}
											} catch (e) {}
										}
									});
								}
							},
							"关闭": function _() {
								$(this).dialog('close');
							}
						}
					});

					$("#invoice_detail tbody input.ui-button").button();

					$("#add_invoice_row").enable().unbind("click").click(function () {
						$("#invoice_detail tbody tr:eq(0) input").disable().css({ "pointer-events": "none" });
						$("#invoice_detail tbody tr:eq(0) td:eq(0)").css({
							"text-decoration": "red line-through"
						});
						addInvoiceRow(data);
						$(this).disable().removeClass("ui-state-focus");
					});
				}
			} catch (e) {}
		}
	});
};
function addInvoiceRow(data) {
	var id = Date.now();
	var content = "<tr id=\"" + id + "\" class=\"add\" object_type=\"" + data.object_type + "\" device_type_id=\"" + data.device_type_id + "\" model_name=\"" + data.model_name + "\">\n\t\t\t\t\t\t<td class=\"td-content\"><input type=\"text\" class=\"ui-widget-content send_date\" readonly=\"readonly\"></td>\n\t\t\t\t\t\t<td class=\"td-content\"><input type=\"text\" class=\"ui-widget-content order_price\"></td>\n\t\t\t\t\t\t<td class=\"td-content\"><input type=\"text\" class=\"ui-widget-content origin_price\"></td>\n\t\t\t\t\t\t<td class=\"td-content text-center\"><input type=\"button\" class=\"ui-button applicate\" value=\"\u5E94\u7528\"></td>\n\t\t\t\t  </tr>";
	$("#invoice_detail table:last-child tbody").append(content);

	var $newRow = $("#invoice_detail table:last-child tbody tr#" + id);
	$newRow.find("input.ui-button").button();
	$newRow.find("input.send_date").datepicker({
		showButtonPanel: true,
		dateFormat: "yy/mm/dd",
		currentText: "今天"
	});
};
function resetRowNum() {
	$("#order_detail tbody tr").each(function (index, tr) {
		var $tr = $(tr);
		$tr.find("td.rowNum").text(index + 1);
	});
};
function subClick() {
	$(this).removeClass("ui-state-focus");
	var $tr = $(this).closest("tr");
	var className = $tr.attr("class");
	if (className == "unchanged" || className == "change") {
		$tr.css({ "background-color": "#CCC" }).removeClass("unchanged change").addClass("remove");
		$tr.find("input,select").disable().prop("readonly", true);
	} else if (className == "add") {
		$tr.remove();
		resetRowNum();
	}
};
function objectTypeChange() {
	var $tr = $(this).closest("tr");
	var value = this.value;
	if (value == 2) {
		// 治具
		// "品名”列不显示。
		$tr.find("input.device_type_name").hide().val("").next().val("");
		// “现有备品数” 当“对象类别”选择为“治具”时显示空白。
		$tr.find("td.available_inventory").text("");
	} else {
		// "品名”列显示可选。
		$tr.find("input.device_type_name").show();
	}
};
function modelNameKeypress(e) {
	// "型号/规格”：填写完成这个值后按回车键，跳到"名称"列
	var $tr = $(this).closest("tr");
	if (e.keyCode == 13) {
		// 回车
		if (this.value) {
			// 名称
			var name = $tr.find("input.name").focus().val().trim();
			// 当“对象类别”选择为“治具”并且"名称"当时为空时
			var objectType = $tr.find("select.object_type").val().trim();
			if (objectType == 2 && !name) {
				// jig_manage表里按jig_no找出jig_name（delete_flg = 0）
				var data = {
					"jig_no": this.value
				};
				searchJigName(data, $tr);
			}
		}
	}
};
function searchJigName(data, $tr) {
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=searchJigName',
		cache: false,
		data: data,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var jigManageList = resInfo.jigManageList;
					var len = jigManageList.length;
					var $input = $tr.find("input.name");
					// 找到一个，将找到的jig_name设给"名称"列。
					if (len == 1) {
						$input.val(jigManageList[0].tools_name).unbind("click");
					} else if (len > 1) {
						var arr = [];
						var _iteratorNormalCompletion2 = true;
						var _didIteratorError2 = false;
						var _iteratorError2 = undefined;

						try {
							for (var _iterator2 = jigManageList[Symbol.iterator](), _step2; !(_iteratorNormalCompletion2 = (_step2 = _iterator2.next()).done); _iteratorNormalCompletion2 = true) {
								var item = _step2.value;

								arr.push(item.tools_name);
							}
						} catch (err) {
							_didIteratorError2 = true;
							_iteratorError2 = err;
						} finally {
							try {
								if (!_iteratorNormalCompletion2 && _iterator2.return) {
									_iterator2.return();
								}
							} finally {
								if (_didIteratorError2) {
									throw _iteratorError2;
								}
							}
						}

						$input.datalist({
							source: arr
						});
					}
				}
			} catch (e) {
				console.log("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
			}
		}
	});
};
(function ($) {
	$.fn.datalist = function (options) {
		var default_options = {
			width: "100px",
			source: []
		};
		return this.each(function () {
			var _this = $(this);
			var opts = $.extend({}, default_options, options);

			if (!(opts.source instanceof Array)) {
				throw new Error("parameter source is not Array");
				return;
			}

			var id = _this.data("id") || Date.now(),
			    to = void 0,
			    items = '';

			var $ul = $("#" + id);
			if ($ul.length === 0) {
				$ul = $('<ul class="ui-menu ui-widget-content ui-corner-all"></ul>');
				$ul.attr("id", id).css({ "position": "absolute", "z-index": "1007", "width": opts.width }).hide();
				$("body").append($ul);

				_this.data("id", id);
				$ul = $("#" + id);
			}

			opts.source.forEach(function (item) {
				return items += "<li class=\"ui-menu-item\"><a class=\"ui-corner-all\">" + item + "</a></li>";
			});

			$ul.html("").append(items).find("li").hover(function () {
				$(this).addClass('ui-state-hover').css({ "cursor": "pointer" });
			}, function () {
				$(this).removeClass('ui-state-hover');
			}).click(function () {
				clearTimeout(to);
				$ul.hide();
				_this.val($(this).find("a").text());
			});

			_this.click(function () {
				var top = _this.offset().top,
				    left = _this.offset().left,
				    height = _this.outerHeight();

				top += height;
				$ul.css({
					"top": top + "px",
					"left": left + "px"
				}).show("fast");
			}).blur(function () {
				return to = setTimeout(function () {
					return $ul.hide('fade');
				}, 200);
			}).focus(function () {
				return clearTimeout(to);
			});
		});
	};
})(jQuery);
function deviceTypeIdChange() {
	// “对象类别”选择为“设备”或“一般工具”时，一旦"品名”或者"型号/规格”被改动且都有值后
	// 到device_spare表里取一下device_spare_type固定为1，相应的device_type_id和model_name对应记录的available_inventory。
	// 取到的话显示到“现有备品数” ，取不到显示为“-”。
	var $tr = $(this).closest("tr");
	var objectType = $tr.find("select.object_type").val().trim();
	if (objectType == 1 || objectType == 3) {
		var device_type_id = this.value;
		var model_name = $tr.find("input.model_name").val().trim();
		if (model_name && device_type_id) {
			// 查询现有备品数
			var data = {
				"device_type_id": device_type_id,
				"model_name": model_name,
				"device_spare_type": '1'
			};
			getAvailableInventory(data, $tr);
		}
	}
};
function modelNameChange() {
	// “对象类别”选择为“设备”或“一般工具”时，一旦"品名”或者"型号/规格”被改动且都有值后
	// 到device_spare表里取一下device_spare_type固定为1，相应的device_type_id和model_name对应记录的available_inventory。
	// 取到的话显示到“现有备品数” ，取不到显示为“-”。
	var $tr = $(this).closest("tr");
	var objectType = $tr.find("select.object_type").val().trim();
	if (objectType == 1 || objectType == 3) {
		var model_name = this.value;
		var device_type_id = $tr.find("input.device_type_id").val().trim();
		if (model_name && device_type_id) {
			// 查询现有备品数
			var data = {
				"device_type_id": device_type_id,
				"model_name": model_name,
				"device_spare_type": '1'
			};
			getAvailableInventory(data, $tr);
		}
	}
};
function getAvailableInventory(data, $tr) {
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=searchAvailableInventory',
		cache: false,
		data: data,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var deviceSpareForm = resInfo.deviceSpareForm;
					// 现有备品数
					if (deviceSpareForm) {
						$tr.find("td.available_inventory").text(deviceSpareForm.available_inventory);
					} else {
						$tr.find("td.available_inventory").text('－');
					}
				}
			} catch (e) {}
		}
	});
};
function application() {
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=searchUnProvide',
		cache: false,
		data: null,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var _list = resInfo.list;
					var content = '';

					// 设备管理员
					var isTechnology = $("body").attr("istechnology") === "true" ? true : false;

					if (isTechnology) {
						_list.forEach(function (item) {
							content += "<tr order_key=\"" + item.order_key + "\" order_no=\"" + item.order_no + "\">\n\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">\n\t\t\t\t\t\t\t\t\t\t\t\t<span style=\"display:inline-block;box-sizing:border-box;width:49%;\">" + item.order_no + "</span>\n\t\t\t\t\t\t\t\t\t\t\t\t<span style=\"display:inline-block;box-sizing:border-box;width:49%;\">\n\t\t\t\t\t\t\t\t\t\t\t\t\t<input type=\"button\" class=\"ui-button order_no\" value=\"\u4FEE\u6539\u8BA2\u5355\u53F7\">\n\t\t\t\t\t\t\t\t\t\t\t\t</span>\n\t\t\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\"><input type=\"button\" class=\"ui-button choose\" value=\"\u9009\u62E9\"></td>\n\t\t\t\t\t\t\t\t\t\t</tr>";
						});
					} else {
						_list.forEach(function (item) {
							content += "<tr order_key=\"" + item.order_key + "\" order_no=\"" + item.order_no + "\">\n\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + item.order_no + "</td>\n\t\t\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\"><input type=\"button\" class=\"ui-button choose\" value=\"\u9009\u62E9\"></td>\n\t\t\t\t\t\t\t\t\t\t</tr>";
						});
					}

					$("#choose_order_no tbody").html(content);

					if (isTechnology) {
						$("#add_order_no").focus().val("").next().text("");
					} else {
						var tempOrderNo = resInfo.tempOrderNo;
						if (!tempOrderNo) {
							tempOrderNo = "L001";
						} else {
							tempOrderNo = tempOrderNo.substring(1);
							tempOrderNo = tempOrderNo * 1;
							tempOrderNo++;
							if (tempOrderNo < 10) {
								tempOrderNo = "L00" + tempOrderNo;
							} else if (tempOrderNo < 100) {
								tempOrderNo = "L0" + tempOrderNo;
							} else {
								tempOrderNo = "L" + tempOrderNo;
							}
						}

						$("#add_order_no").val(tempOrderNo).hide().next().text(tempOrderNo);
					}

					if (_list.length == 0) {
						$("#choose_order_no table:eq(0)").hide();
					} else {
						$("#choose_order_no table:eq(0)").show();
					}

					var $thisDialog = $("#choose_order_no").dialog({
						title: "申请",
						width: 500,
						height: 'auto',
						resizable: false,
						modal: true,
						show: "blind",
						buttons: {
							"确认": function _() {
								var data = {
									"order_no": $("#add_order_no").val().trim()
								};
								$.ajax({
									beforeSend: ajaxRequestType,
									async: true,
									url: servicePath + '?method=doInsertOrder',
									cache: false,
									data: data,
									type: "post",
									dataType: "json",
									success: ajaxSuccessCheck,
									error: ajaxError,
									complete: function complete(xhrobj, textStatus) {
										var resInfo = null;
										try {
											// 以Object形式读取JSON
											eval('resInfo =' + xhrobj.responseText);
											if (resInfo.errors.length > 0) {
												// 共通出错信息框
												treatBackMessages(null, resInfo.errors);
											} else {
												var order_key = resInfo.order_key;
												$thisDialog.dialog('close');
												showDetailDialog(order_key, data.order_no, null);
											}
										} catch (e) {}
									}
								});
							},
							"关闭": function _() {
								$(this).dialog('close');
							}
						}
					});

					$("#choose_order_no tbody input[type='button']").button();
					// 选择事件
					$("#choose_order_no tbody input.choose").click(function () {
						var $tr = $(this).closest("tr");
						showDetail($tr.attr("order_key"), $tr.attr("order_no"), $thisDialog);
					});

					// 修改订单号
					$("#choose_order_no tbody input.order_no").click(function () {
						var $tr = $(this).closest("tr");
						var order_no = $tr.attr("order_no");
						var order_key = $tr.attr("order_key");

						$("#update_order_no").val(order_no);

						var $childDialog = $("#update_order_no_dialog").dialog({
							title: "修改订单号",
							width: 400,
							height: 'auto',
							resizable: false,
							modal: true,
							buttons: {
								"确定": function _() {
									var postData = {
										"order_key": order_key,
										"order_no": $("#update_order_no").val().trim()
									};

									$.ajax({
										beforeSend: ajaxRequestType,
										async: true,
										url: servicePath + '?method=doUpdateOrderNo',
										cache: false,
										data: postData,
										type: "post",
										dataType: "json",
										success: ajaxSuccessCheck,
										error: ajaxError,
										complete: function complete(xhrobj, textStatus) {
											var resInfo = null;
											try {
												// 以Object形式读取JSON
												eval('resInfo =' + xhrobj.responseText);
												if (resInfo.errors.length > 0) {
													// 共通出错信息框
													treatBackMessages(null, resInfo.errors);
												} else {
													var _$tr = $("#choose_order_no tr[order_key=" + order_key + "]");
													_$tr.attr("order_no", postData.order_no).find("span:eq(0)").text(postData.order_no);
													$childDialog.dialog('close');
												}
											} catch (e) {}
										}
									});
								},
								"关闭": function _() {
									$(this).dialog('close');
								}
							}
						});
					});
				}
			} catch (e) {}
		}
	});
};
function showDetail(order_key, order_no, $parentDialog) {
	var data = {
		"order_key": order_key
	};
	$.ajax({
		beforeSend: ajaxRequestType,
		async: true,
		url: servicePath + '?method=searchOrderDetail',
		cache: false,
		data: data,
		type: "post",
		dataType: "json",
		success: ajaxSuccessCheck,
		error: ajaxError,
		complete: function complete(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var detailList = resInfo.detailList;
					showDetailDialog(order_key, order_no, detailList, $parentDialog);
				}
			} catch (e) {}
		}
	});
};
/**
 * 
 * @param order_key
 *            订单KEY
 * @param list
 *            订单明细
 */
function showDetailDialog(order_key, order_no, list, $parentDialog) {
	$("#order_detail tbody").html("");
	if (list) {
		// 设备管理员
		var isTechnology = $("body").attr("istechnology") === "true" ? true : false;
		// 角色
		var role = $("#role").val().trim();

		var loginID = $("#loginID").val().trim();
		var orderFromOptions = $("#hide_order_from").html();
		var content = '';

		list.forEach(function (item, index) {
			var rowNum = index + 1;
			var order_key = item.order_key;
			var object_type = item.object_type;
			var object_type_name = item.object_type_name;
			var device_type_id = item.device_type_id;
			var device_type_name = item.device_type_name || '';
			var model_name = item.model_name;
			var applicator_id = item.applicator_id;
			var applicate_date = item.applicate_date || '';
			var applicator_operator_name = item.applicator_operator_name;
			var system_code = item.system_code || '';
			var quantity = item.quantity || '';
			var nesssary_reason = item.nesssary_reason || '';
			var available_inventory = item.available_inventory || '-';
			var order_from = item.order_from || '';
			var order_from_name = item.order_from_name || '';
			var name = item.name;

			// 经理角色
			if (role === "manager") {
				//登录的数据是当前登录者自己的
				if (applicator_id == loginID) {
					content += "<tr class=\"unchanged\" order_key=\"" + order_key + "\" object_type=\"" + object_type + "\" device_type_id=\"" + device_type_id + "\" model_name=\"" + model_name + "\" applicator_id=\"" + applicator_id + "\">\n\t\t\t\t\t\t\t\t\t<td class=\"ui-state-default td-title rowNum\">" + rowNum + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + object_type_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + device_type_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + model_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + system_code + "\">\n\t\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content system_code\" value=\"" + system_code + "\">\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + name + "\">\n\t\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content name\" value=\"" + name + "\">\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + order_from + "\">\n\t\t\t\t\t\t\t\t\t\t<select class=\"ui-widget-content order_from\" order_from=\"" + order_from + "\">" + orderFromOptions + "</select>\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\" value=\"" + quantity + "\">\n\t\t\t\t\t" + (applicate_date != '' ? quantity : String.raw(_templateObject, quantity)) + "\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + nesssary_reason + "\">\n\t\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content nesssary_reason\" value=\"" + nesssary_reason + "\">\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + applicator_operator_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + available_inventory + "</td>\n\t\t\t\t\t" + (applicate_date != '' ? String.raw(_templateObject2, applicate_date) : String.raw(_templateObject3, index, index, index, index, index, index)) + "\n\t\t\t\t\t\t\t\t   <td class=\"td-content text-center\"><input type=\"button\" class=\"ui-button subtract\" value=\"-\"/></td>\n\t\t\t\t\t\t\t\t</tr>";
				} else {
					//不是自己的数据
					content += "<tr order_key= \"" + order_key + "\" object_type=\"" + object_type + "\" device_type_id=\"" + device_type_id + "\" model_name=\"" + model_name + "\" applicator_id=\"" + applicator_id + "\">\n\t\t\t\t\t\t\t\t\t<td class=\"ui-state-default td-title rowNum\">" + rowNum + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + object_type_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + device_type_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + model_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + system_code + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + order_from_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + quantity + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + nesssary_reason + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + applicator_operator_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + available_inventory + "</td>\n\t\t\t\t" + (applicate_date != '' ? String.raw(_templateObject2, applicate_date) : String.raw(_templateObject3, index, index, index, index, index, index)) + "\n\t\t\t\t \t\t\t\t   <td class=\"td-content\"></td>\n\t\t\t\t \t\t\t\t</tr>";
				}
			} else if (role === "line") {
				//线长角色
				//登录的数据是当前登录者自己的
				if (applicator_id == loginID) {
					content += "<tr class=\"unchanged\" order_key=\"" + order_key + "\" object_type=\"" + object_type + "\" device_type_id=\"" + device_type_id + "\" model_name=\"" + model_name + "\" applicator_id=\"" + applicator_id + "\">\n\t\t\t\t\t\t\t\t\t<td class=\"ui-state-default td-title rowNum\">" + rowNum + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + object_type_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + device_type_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + model_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + system_code + "\">\n\t\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content system_code\" value=\"" + system_code + "\">\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + name + "\">\n\t\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content name\" value=\"" + name + "\">\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + order_from + "\">\n\t\t\t\t\t\t\t\t\t\t<select class=\"ui-widget-content order_from\" order_from=\"" + order_from + "\">" + orderFromOptions + "</select>\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\" value=\"" + quantity + "\">\n\t\t\t\t" + (applicate_date != '' ? quantity : String.raw(_templateObject, quantity)) + "\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + nesssary_reason + "\">\n\t\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content nesssary_reason\" value=\"" + nesssary_reason + "\">\n\t\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">\t" + applicator_operator_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + available_inventory + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\" style=\"width:90px;\" applicate_date=\"" + applicate_date + "\">" + (applicate_date != '' ? applicate_date : '') + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\"><input type=\"button\" class=\"ui-button subtract\" value=\"-\"/></td>\n\t\t\t\t\t\t\t\t</tr>";
				} else {
					//不是自己的数据,数据只度
					content += "<tr order_key= \"" + order_key + "\" object_type=\"" + object_type + "\" device_type_id=\"" + device_type_id + "\" model_name=\"" + model_name + "\" applicator_id=\"" + applicator_id + "\">\n\t\t\t\t\t\t\t\t\t<td class=\"ui-state-default td-title rowNum\">" + rowNum + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + object_type_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + device_type_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + model_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + system_code + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + order_from_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + quantity + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + nesssary_reason + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content\">" + applicator_operator_name + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + available_inventory + "</td>\n\t\t\t\t\t\t\t\t\t<td class=\"td-content text-center\" style=\"width:90px;\" applicate_date=\"" + applicate_date + "\">" + (applicate_date != '' ? applicate_date : '') + "</td>\n\t\t\t\t\t\t\t\t \t<td class=\"td-content\"></td>\n\t\t\t\t\t\t\t\t</tr>";
				}
			} else if (isTechnology) {
				//设备管理权限
				content += "<tr class=\"unchanged\" order_key= \"" + order_key + "\" object_type=\"" + object_type + "\" device_type_id=\"" + device_type_id + "\" model_name=\"" + model_name + "\" applicator_id=\"" + applicator_id + "\">\n\t\t\t\t\t\t\t\t<td class=\"ui-state-default td-title rowNum\">" + rowNum + "</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content\">" + object_type_name + "</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content\">" + device_type_name + "</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content\">" + model_name + "</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + system_code + "\">\n\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content system_code\" value=\"" + system_code + "\">\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + name + "\">\n\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content name\" value=\"" + name + "\">\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + order_from + "\">\n\t\t\t\t\t\t\t\t\t<select class=\"ui-widget-content order_from\" order_from=\"" + order_from + "\">" + orderFromOptions + "</select>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content text-right\" value=\"" + quantity + "\">\n\t\t\t\t" + (applicate_date != '' ? quantity : String.raw(_templateObject, quantity)) + "\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content\" value=\"" + nesssary_reason + "\">\n\t\t\t\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content nesssary_reason\" value=\"" + nesssary_reason + "\">\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content\">" + applicator_operator_name + "</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content text-right\">" + available_inventory + "</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content text-center\" style=\"width:90px;\" applicate_date=\"" + applicate_date + "\">" + (applicate_date != '' ? applicate_date : '') + "</td>\n\t\t\t\t\t\t\t\t<td class=\"td-content text-center\"><input type=\"button\" class=\"ui-button subtract\" value=\"-\"/></td>\n\t\t\t\t\t\t\t</tr>";
			}
		});

		var $table = $("#order_detail table");
		$table.find("tbody").html(content);
		$table.find("tbody input.ui-button").button();

		$table.find("td.manager_confirm").buttonset();
		// 已有的数据完成赋值
		$table.find("select.order_from").each(function (index, select) {
			return $(select).val($(select).attr("order_from"));
		});
	}

	$("#add_row").unbind("click").click(function () {
		return addRow(order_key);
	});

	var $childDialg = $("#order_detail").dialog({
		title: "设备/专用工具/一般工具订单明细",
		width: 1250,
		height: 500,
		resizable: false,
		modal: true,
		buttons: {
			"确认": function _() {
				var data = {};
				$("#order_detail tbody tr").each(function (index, tr) {
					var $tr = $(tr);
					var className = $tr.attr("class") || '';

					// 数据区分标记[add，change，unchanged，remove]
					data["device_jig_order_detail.confirm_flg_name[" + index + "]"] = className;
					// 订购单KEY
					data["device_jig_order_detail.order_key[" + index + "]"] = $tr.attr("order_key");
					// 对象类别
					data["device_jig_order_detail.object_type[" + index + "]"] = $tr.attr("object_type") || $tr.find("select.object_type").val();
					// 品名
					data["device_jig_order_detail.device_type_id[" + index + "]"] = $tr.attr("device_type_id") || $tr.find("input.device_type_id").val();
					// 型号/规格
					data["device_jig_order_detail.model_name[" + index + "]"] = $tr.attr("model_name") || $tr.find("input.model_name").val();
					// 系统编码
					data["device_jig_order_detail.system_code[" + index + "]"] = $tr.find("input.system_code").val();
					// 名称
					data["device_jig_order_detail.name[" + index + "]"] = $tr.find("input.name").val();
					// 受注方
					data["device_jig_order_detail.order_from[" + index + "]"] = $tr.find("select.order_from").val();
					// 数量
					data["device_jig_order_detail.quantity[" + index + "]"] = $tr.find("input.quantity").val();
					// 理由/必要性
					data["device_jig_order_detail.nesssary_reason[" + index + "]"] = $tr.find("input.nesssary_reason").val();
					// 申请人
					data["device_jig_order_detail.applicator_id[" + index + "]"] = $tr.attr("applicator_id");
					//经理确认
					data["device_jig_order_detail.manage_comfirm_flg[" + index + "]"] = $tr.find("td.manager_confirm").attr("manager_confirm") || $tr.find("td.manager_confirm input[type='radio']:checked").val() || '';
					data["device_jig_order_detail.applicate_date[" + index + "]"] = $tr.find("td[applicate_date]").attr("applicate_date") || '';
				});

				data["order_no"] = order_no;
				$.ajax({
					beforeSend: ajaxRequestType,
					async: true,
					url: servicePath + '?method=doUpdate',
					cache: false,
					data: data,
					type: "post",
					dataType: "json",
					success: ajaxSuccessCheck,
					error: ajaxError,
					complete: function complete(xhrobj, textStatus) {
						var resInfo = null;
						try {
							// 以Object形式读取JSON
							eval('resInfo =' + xhrobj.responseText);
							if (resInfo.errors.length > 0) {
								// 共通出错信息框
								treatBackMessages(null, resInfo.errors);
							} else {
								$childDialg.dialog('close');
								if ($parentDialog) {
									$parentDialog.dialog('close');
								}
								findit();
							}
						} catch (e) {}
					}
				});
			},
			"关闭": function _() {
				$(this).dialog('close');
			}
		}
	});
};
function addRow(orderKey) {
	// 角色
	var role = $("#role").val().trim();

	var loginID = $("#loginID").val().trim();
	var loginName = $("#loginName").val().trim();
	var orderFromOptions = $("#hide_order_from").html();
	var objectTypeOptions = $("#hide_object_type").html();
	var id = Date.now();

	var content = "<tr class=\"add\" id=\"" + id + "\" order_key= \"" + orderKey + "\" applicator_id=\"" + loginID + "\">\n\t\t\t\t\t<td class=\"ui-state-default td-title rowNum\"></td>\n\t\t\t\t\t<td class=\"td-content\">\n\t\t\t\t\t\t<select class=\"ui-widget-content object_type\">" + objectTypeOptions + "</select>\n\t\t\t\t\t</td>\n\t\t\t\t\t<td class=\"td-content\">\n\t\t\t\t\t\t<input type=\"text\" readonly=\"readonly\" class=\"ui-widget-content device_type_name\">\n\t\t\t\t\t\t<input type=\"hidden\" class=\"device_type_id\">\n\t\t\t\t\t</td>\n\t\t\t\t\t<td class=\"td-content\">\n\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content model_name\">\n\t\t\t\t\t</td>\n\t\t\t\t\t<td class=\"td-content\">\n\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content system_code\">\n\t\t\t\t\t</td>\n\t\t\t\t\t<td class=\"td-content\">\n\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content name\">\n\t\t\t\t\t</td>\n\t\t\t\t\t<td class=\"td-content\">\n\t\t\t\t\t\t<select class=\"ui-widget-content order_from\">" + orderFromOptions + "</select>\n\t\t\t\t\t</td>\n\t\t\t\t\t<td class=\"td-content\">\n\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content quantity\">\n\t\t\t\t\t</td>\n\t\t\t\t\t<td class=\"td-content\">\n\t\t\t\t\t\t<input type=\"text\" class=\"ui-widget-content nesssary_reason\">\n\t\t\t\t\t</td>\n\t\t\t\t\t<td class=\"td-content\">\t" + loginName + "</td>\n\t\t\t\t\t<td class=\"td-content available_inventory text-right\"></td>\n\t" + (role === "manager" ? String.raw(_templateObject4) : String.raw(_templateObject5)) + "\n\t\t \t\t\t<td class=\"td-content text-center\" style=\"width:50px;\">\n\t\t\t\t\t\t<input type=\"button\" class=\"ui-button subtract\" value=\"-\"/>\n\t\t\t\t\t</td>\n\t\t\t\t</tr>";
	$("#order_detail tbody").append(content);

	var $newRow = $("#order_detail tbody tr#" + id);
	$newRow.find("input.subtract").button();

	// "品名”列参照选择全部设备类别。当选择后，如果"名称"当时为空，则"名称"也设成品名的文字。
	setReferChooser($newRow.find("input.device_type_id"), $("#device_type_id_referchooser"), null, function (TR) {
		if (TR == null) return;
		// 当前选择的ID
		var id = $(TR).find("td.referId").text().trim();
		var value = $(TR).find("nobr").text().trim();

		var $tr = $("#order_detail input.device_type_id.referchooser_target").closest("tr");
		var $typeIdInput = $tr.find("input.device_type_id");
		// 第一次选择
		if (!$typeIdInput.attr("prev-value")) {
			$typeIdInput.attr("prev-value", id).change();
		} else {
			// 取出上次选择的ID
			var preValue = $typeIdInput.attr("prev-value");
			// 选择不一致
			if (preValue != id) {
				// 触发chnag事件
				$typeIdInput.attr("prev-value", id).change();
			}
		}
		// 如果"名称"当时为空，则"名称"也设成品名的文字。
		var name = $tr.find("input.name").val();
		if (!name) {
			$tr.find("input.name").val(value);
		}
	});
	resetRowNum();
};
/**
 * 一览
 * 
 * @param listdata
 */
function list(listdata) {
	if ($("#gbox_list").length > 0) {
		$("#list").jqGrid().clearGridData(); // 清除
		$("#list").jqGrid('setGridParam', { data: listdata }).trigger("reloadGrid", [{ current: false }]); // 刷新列表
	} else {
		$("#list").jqGrid({
			data: listdata, // 数据
			height: 461, // rowheight*rowNum+1
			width: 992,
			rowheight: 23,
			shrinkToFit: true,
			datatype: "local",
			colNames: ['委托单号', '报价单号', '订单号', '分类', '型号/规格', '系统编码', '名称', '受注方', '数量', '单价', '金额', '日本价格', '差异', '申请者', '理由/必要性', '申请日期', '委托发送日期', '询价发送日期', '确认接收日期', '发送OSH日期', '预计纳期', '收货时间', '确认结果', '验收日期', '验收人', '预算月', 'order_key', 'object_type', 'device_type_id', 'applicator_id', 'confirm_flg', 'confirm_quantity', 'quotation_id', 'budget_description'],
			colModel: [{ name: 'entrust_no', index: 'entrust_no', width: 100, hidden: true }, { name: 'quotation_no', index: 'quotation_no', width: 100 }, { name: 'order_no', index: 'order_no', width: 100 }, { name: 'object_type_name', index: 'object_type_name', width: 100 }, { name: 'model_name', index: 'model_name', width: 100 }, { name: 'system_code', index: 'system_code', width: 100, hidden: true }, { name: 'name', index: 'name', width: 100, hidden: true }, { name: 'order_from_name', index: 'order_from_name', width: 100, hidden: true }, { name: 'quantity', index: 'quantity', width: 100, align: 'right', sorttype: 'number' }, { name: 'order_price', index: 'order_price', width: 100, align: 'right', hidden: true, formatter: 'currency', sorttype: 'currency', formatoptions: { thousandsSeparator: ',', decimalPlaces: 0, defaultValue: '-' } }, { name: 'total_order_price', index: 'total_order_price', width: 100, align: 'right', formatter: 'currency', sorttype: 'currency', formatoptions: { thousandsSeparator: ',', decimalPlaces: 0, defaultValue: '-' } }, { name: 'origin_price', index: 'origin_price', width: 100, align: 'right', formatter: 'currency', sorttype: 'currency', formatoptions: { thousandsSeparator: ',', decimalPlaces: 2, defaultValue: '-' } }, { name: 'differ_price', index: 'differ_price', width: 100, align: 'right', hidden: true, formatter: 'currency', sorttype: 'currency', formatoptions: { thousandsSeparator: ',', decimalPlaces: 0, defaultValue: '-' } }, { name: 'applicator_operator_name', index: 'applicator_operator_name', width: 100 }, { name: 'nesssary_reason', index: 'nesssary_reason', width: 100, hidden: true }, { name: 'applicate_date', index: 'applicate_date', width: 100, align: 'center', sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'entrust_send_date', index: 'entrust_send_date', width: 100, align: 'center', hidden: true, sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'y/m/d', newformat: 'y-m-d' } }, { name: 'send_date', index: 'send_date', width: 100, align: 'center', hidden: true, sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'acquire_date', index: 'acquire_date', width: 100, align: 'center', hidden: true, sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'delivery_osh_date', index: 'delivery_osh_date', width: 100, align: 'center', hidden: true, sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'scheduled_date', index: 'scheduled_date', width: 100, align: 'center', sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'recept_date', index: 'recept_date', width: 100, align: 'center', hidden: true, sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'confirm_flg_name', index: 'confirm_flg_name', width: 100, align: 'center' }, { name: 'inline_recept_date', index: 'inline_recept_date', width: 100, align: 'center', sorttype: 'date', formatter: 'date', formatoptions: { srcformat: 'Y/m/d', newformat: 'y-m-d' } }, { name: 'inline_receptor_operator_name', index: 'inline_receptor_operator_name', width: 100, hidden: true }, { name: 'budget_month', index: 'budget_month', width: 100, align: 'center', hidden: true }, { name: 'order_key', index: 'order_key', hidden: true }, { name: 'object_type', index: 'object_type', hidden: true }, { name: 'device_type_id', index: 'device_type_id', hidden: true }, { name: 'applicator_id', index: 'applicator_id', hidden: true }, { name: 'confirm_flg', index: 'confirm_flg', hidden: true }, { name: 'confirm_quantity', index: 'confirm_quantity', hidden: true }, { name: 'quotation_id', index: 'quotation_id', hidden: true }, { name: 'budget_description', index: 'budget_description', hidden: true }],
			rowNum: 20,
			toppager: false,
			pager: "#listpager",
			viewrecords: true,
			caption: "",
			multiselect: false,
			gridview: true,
			pagerpos: 'right',
			pgbuttons: true, // 翻页按钮
			rownumbers: true,
			pginput: false,
			recordpos: 'left',
			hidegrid: false,
			deselectAfterSort: false,
			onSelectRow: function onSelectRow(rowId) {
				var rowData = $("#list").jqGrid('getRowData', rowId);
				// 确认结果
				var confirm_flg = rowData.confirm_flg.trim();
				// 确认数量
				var confirm_quantity = rowData.confirm_quantity.trim();
				if (!confirm_quantity) {
					confirm_quantity = 0;
				} else {
					confirm_quantity *= 1;
				}
				// 验收日期
				var inline_recept_date = rowData.inline_recept_date.trim();

				var quotation_id = rowData.quotation_id.trim();

				//如果confirm_flg是OK并且confirm_quantity大于0。而且inline_recept_date为空，则验收按钮可以使用
				if (confirm_flg == 1 && confirm_quantity > 0 && !inline_recept_date) {
					$("#inlinereceptbutton").enable();
				} else {
					$("#inlinereceptbutton").disable();
				}

				//一览中选中的行只要`quotation_id`字段有值预算按钮就可以操作
				if (quotation_id) {
					$("#budgetbutton").enable();
				} else {
					$("#budgetbutton").disable();
				}
			},
			ondblClickRow: function ondblClickRow(rid, iRow, iCol, e) {},
			viewsortcols: [true, 'vertical', true],
			gridComplete: function gridComplete() {
				var IDS = $("#list").getDataIDs(),

				// 当前显示多少条
				length = IDS.length,
				    pill = $("#list");

				// 当前日期
				var now = new Date();
				now.setUTCHours(0, 0, 0, 0);

				// 一天毫秒数
				var oneDay = 24 * 60 * 60 * 1000;

				for (var i = 0; i < length; i++) {
					var rowData = pill.jqGrid('getRowData', IDS[i]);
					// 报价单号
					var quotation_no = rowData.quotation_no;
					// 申请日期
					var applicate_date = rowData.applicate_date.trim();
					if (applicate_date.length == 8) {
						applicate_date = "20" + applicate_date;
					}
					// 确认接收日期
					var acquire_date = rowData.acquire_date.trim();
					// 询价发送日期
					var send_date = rowData.send_date.trim();
					// 收货时间
					var recept_date = rowData.recept_date.trim();
					// 预计纳期
					var scheduled_date = rowData.scheduled_date.trim();
					// 确认结果
					var confirm_flg = rowData.confirm_flg.trim();
					// 验收日期
					var inline_recept_date = rowData.inline_recept_date.trim();
					// 间隔天数
					var differDays = "";
					// 确认数量
					var confirm_quantity = rowData.confirm_quantity.trim();
					// “报价单号”为空，并且“申请日期”早于当前日期一个自然月前
					if (!quotation_no && applicate_date) {
						differDays = (now - new Date(applicate_date)) / oneDay;
						if (differDays > 30) {
							// “订单号”，“分类”，“型号/规格”，“申请日期”单元格底色橙色
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_order_no']").css({ "background-color": "orange", "color": "#fff" });
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_object_type_name']").css({ "background-color": "orange", "color": "#fff" });
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_model_name']").css({ "background-color": "orange", "color": "#fff" });
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_applicate_date']").css({ "background-color": "orange", "color": "#fff" });
						}
					}
					// “确认接收日期”为空，并且“询价发送日期”早于离当前日往前最早的25日
					// “离当前日往前最早的25日”，就是说本月还没到25日就是上月25日，本月过了25日就是本月25日。
					if (!acquire_date && send_date) {
						// 当前日往前最早的25日
						var tempNow = new Date();
						tempNow.setTime(now.getTime());

						if (tempNow.getDate() >= 25) {
							tempNow.setDate(25); // 本月25日
						} else {
							if (tempNow.getMonth() == 0) {
								// 跨年取月份
								tempNow.setFullYear(tempNow.getFullYear() - 1, 11, 25);
							} else {
								tempNow.setMonth(tempNow.getMonth() - 1, 25);
							}
						}

						differDays = (tempNow - new Date(send_date)) / oneDay;
						if (differDays > 0) {
							// “订单号”，“分 类”，“型号/规格”，“询价发送日期”单元格底色橙色
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_order_no']").css({ "background-color": "orange", "color": "#fff" });
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_object_type_name']").css({ "background-color": "orange", "color": "#fff" });
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_model_name']").css({ "background-color": "orange", "color": "#fff" });
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_send_date']").css({ "background-color": "orange", "color": "#fff" });
						}
					}
					// 收货时间”为空，但是“预计纳期”存在并且已经等于或小于当前日期
					if (!recept_date && scheduled_date) {
						differDays = (now - new Date(scheduled_date)) / oneDay;
						if (differDays >= 0) {
							// “预计纳期”底色橙色
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_scheduled_date']").css({ "background-color": "orange", "color": "#fff" });
						}
					}
					// “收货时间”存在并且“确认结果”是“OK”，但是“验收日期”是空
					if (recept_date && confirm_flg == 1) {
						if (confirm_quantity == 0) {
							// “确认结果”底色橙色
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_confirm_flg_name']").css({ "background-color": "orange", "color": "#fff" });
						} else if (!inline_recept_date) {
							// “验收日期”，“验收人”底色橙色
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_inline_recept_date']").css({ "background-color": "orange", "color": "#fff" });
							$("#list tr#" + IDS[i] + " td[aria\\-describedby='list_inline_receptor_operator_name']").css({ "background-color": "orange", "color": "#fff" });
						}
					}
				}
				$("#inlinereceptbutton,#budgetbutton").disable();
			}
		});
	}
	$("#inlinereceptbutton,#budgetbutton").disable();
};