var listdata = {};
var servicePath = "beforeLineLeader.do";
/** 医院autocomplete **/
var customers = {};
var opt_bound_out_ocm = {};

var lightRepairs=[];
var chosedPat = {};
var chosedPos = {};

var line_expedite = function() {

	var selectedId = $("#performance_list").getGridParam("selrow");
	var rowData = $("#performance_list").getRowData(selectedId);

	var data = {material_id : rowData["material_id"]};

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doexpedite',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus) {
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);

				// 紧急区分
				var expedited = rowData["expedited"];

				$("#expeditebutton").disable();
			
				findit();
			} catch (e) {
				alert("name: " + e.name + " message: " + e.message + " lineNumber: "
						+ e.lineNumber + " fileName: " + e.fileName);
			};
		}
	});
};

var treat_nogood = function() {
	$("#pop_treat").hide();
	// 导入不良处置画面
	$("#pop_treat").load("widget.do?method=nogoodedit", function(responseText, textStatus, XMLHttpRequest) {
		var selectedId = $("#performance_list").getGridParam("selrow");
		var rowData = $("#performance_list").getRowData(selectedId);
		var data = {
			material_id : rowData["material_id"],
			position_id : rowData["position_id"]
		};
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : servicePath + '?method=getwarning',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj, textStatus) {
				try {
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					$("#nogood_id").val(resInfo.warning.id);
					$("#nogood_occur_time").text(resInfo.warning.occur_time);
					$("#nogood_sorc_no").text(resInfo.warning.sorc_no);
					$("#nogood_model_name").text(resInfo.warning.model_name);
					$("#nogood_serial_no").text(resInfo.warning.serial_no);
					$("#nogood_line_name").text(resInfo.warning.line_name);
					$("#nogood_process_code").text(resInfo.warning.position_name);
					$("#nogood_reason").text(resInfo.warning.reason);
					$("#nogood_comment_other").text(resInfo.warning.comment);

					$("#nogoodfixbtn").hide();
					$("#nogoodwaitbtn").hide();

					$("#pop_treat").dialog({
						// position : [ 800, 20 ],
						title : "不良信息及处置",
						width : 468,
						show : "blind",
						height : 'auto', //450,
						resizable : false,
						modal : true,
						minHeight : 200,
						close : function() {
							if ($("#nogoodfixbtn").attr("checked") === "checked") {
								selectedMaterial.sorc_no = resInfo.warning.sorc_no;
								selectedMaterial.model_name = resInfo.warning.model_name;
								selectedMaterial.serial_no = resInfo.warning.serial_no;
								selectedMaterial.material_id = rowData["material_id"];
								selectedMaterial.position_id = rowData["position_id"];
								selectedMaterial.alarm_messsage_id = $("#nogood_id").val();
								selectedMaterial.append_parts = ($("#append_parts_y").attr("checked") ? 1 : 0);
								selectedMaterial.comment = $("#nogood_comment").val();
							}
							$("#pop_treat").html("");
						},
						buttons : {}
					});
					$("#pop_treat").show();
				} catch (e) {
					alert("name: " + e.name + " message: " + e.message + " lineNumber: "
							+ e.lineNumber + " fileName: " + e.fileName);
				};
			}
		});
	});
};

var jsinit_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			receivePos = resInfo.receivePos;
			// autocomplete
			if (resInfo.customers) customers = resInfo.customers;
			if (resInfo.opt_bound_out_ocm) opt_bound_out_ocm = resInfo.opt_bound_out_ocm;

			listdata = resInfo.performance;

			if ($("#gbox_performance_list").length > 0) {
				$("#performance_list").jqGrid().clearGridData();
				$("#performance_list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [{current : false}]);
			} else {
				$("#performance_list").jqGrid({
					data : listdata,
					height : 507,
					width : 1246,
					rowheight : 23,
					datatype : "local",
					colNames : ['修理单号', '型号', '机身号', '条码参照', '不良', '等级', '处理对策', '受理日期', '报价日期', '客户同意','agreed_date_org',
						'零件订单', '优先报价', '位置', '状态', '备注',
						'position', 'direct_flg', 'fix_type', 'service_repair_flg', 'model_id', 'selectable', 'ts', '', '', '', '', '' ,'','','vip','scheduled_expedited', '返送地区',''],
					colModel : [{
								name : 'sorc_no',
								index : 'sorc_no',
								width : 50
							},{
								name : 'model_name',
								index : 'model_name',
								width : 125
							},{
								name : 'serial_no',
								index : 'serial_no',
								width : 50
							}, {
								name : 'material_id', index : 'material_id', width : 70
							}, {
								name : 'symbol',
								index : 'symbol',
								formatter : function(value, options, rData){
									return rData['status'] || "";
				   				},
								width : 40,
								align : 'center'
							},{
								name : 'level',
								index : 'level',
								formatter: "select", editoptions:{value:resInfo.opt_level},
								width : 35,
								align : 'center'
							}, 
							{
								name : 'storager',
								index : 'storager',
								width : 50,
								align : 'left'
							}, {
								name : 'reception_time',
								index : 'reception_time',
								width : 50,
								align : 'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}
							}, {
								name : 'quotation_time',
								index : 'quotation_time',
								width : 50,
								align : 'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}
							}, {
								name : 'agreed_date',
								index : 'agreed_date',
								width : 50,
								align : 'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}
							}, 
							{name:'agreed_date_org',index:'agreed_date_org', formatter: function(value, options, rData){return rData["agreed_date"] || ""}, hidden:true},
							{
								name : 'partial_order_date',
								index : 'partial_order_date',
								width : 50,
								align : 'center', formatter: 'select', editoptions:{value: "-1:未下订单;0:无缺品;1:有缺品"}
							}, {
								name : 'quotation_first',
								index : 'quotation_first',
								width : 50, align:'center', formatter: function(value, options, rData){
									var val = ((value == 1)? "优先" : "")
									// 加急
									if (rData["scheduled_expedited"] == 1) {
										val += "<font style='color:red'>加急</font>"
									}
									return val;
								}
							}, {
								name : 'processing_position',
								index : 'processing_position',
								width : 50
							}, {
								name : 'operate_result',
								index : 'operate_result',
								formatter: "select", editoptions:{value:resInfo.opt_operate_result},
								width : 50
							}, {name:'comment',index:'comment', width:105, formatter:function(value, options, rData){
								var comment = " ";
								if (rData["direct_flg"] == "1") {
									comment += " 直送";
								}
								if (rData["service_repair_flg"] == "1") {
									comment += " 保内返修";
								} else if (rData["service_repair_flg"] == "2") {
									comment += " QIS";
								} else if (rData["service_repair_flg"] == "3") {
									comment += " 备品";
								}
								if (rData["fix_type"] == "1") {
									comment += " 流水线";
								} else if (rData["fix_type"] == "2") {
									comment += " 单元";
								}
								return comment.trim();
							}}, {
								name : 'position_id',
								index : 'position_id',
								formatter : function(value, options, rData){
									return rData['category_id'];
				   				},
								hidden : true
							},{
								name : 'direct_flg',
								index : 'direct_flg',
								hidden : true
							}, {
								name : 'fix_type',
								index : 'fix_type',
								hidden : true
							}, {
								name : 'service_repair_flg',
								index : 'service_repair_flg',
								hidden : true
							}, {
								name : 'model_id',
								index : 'model_id',
								hidden : true
							}, {
								name : 'selectable',
								index : 'selectable',
								hidden : true
							},
							{name:'ticket_flg',index:'ticket_flg', hidden:true},
							{name:'ocm',index:'ocm', hidden:true},
							{name:'ocm_rank',index:'ocm_rank', hidden:true},
							{name:'customer_name',index:'customer_name', hidden:true},
							{name:'ocm_deliver_date',index:'ocm_deliver_date', hidden:true},
							{name:'osh_deliver_date',index:'osh_deliver_date', hidden:true},
							{name:'material_id',index:'material_id', hidden:true},
							{name:'isHistory',index:'isHistory', hidden:true},
							{name:'vip',index:'vip', hidden:true},
							{name:'scheduled_expedited',index:'scheduled_expedited', hidden:true},
							{
								name : 'bound_out_ocm',
								index : 'bound_out_ocm',
								formatter: "select", editoptions:{value:opt_bound_out_ocm},
								width : 50,
								align : 'center'
							},
							{name:'area',index:'area', hidden:true}
						],
					rowNum : 50,
					rownumbers : true,
					toppager : false,
					viewrecords : true,
					caption : "",
					gridview : true, // Speed up
					pager:  "#performance_listpager",
					pagerpos : 'right',
					pgbuttons : true,
					pginput : false,
					recordpos : 'left',
					ondblClickRow : function(rid, iRow, iCol, e) {
						if ($("#isManager").val() != "false") {
							showDetail(rid, $("#isManager").val());
						}
						// TODO else
					},
					onSelectRow : function(id) {
						var rowdata = $(this).jqGrid('getRowData', id);
						
						var processing_position = rowdata["processing_position"];//位置
						var operate_result = rowdata["operate_result"];//状态
						$("#expeditebutton").enable();

						if(processing_position.indexOf("WIP") == 0 || operate_result == 2){
							$("#expeditebutton").disable();
						}

						// 紧急区分
						var expedited = rowdata["quotation_first"];
						if (expedited == "10" || expedited == "11") {
							$("#expeditebutton").disable();
						} else if (expedited == "1") {
							$("#expeditebutton").disable();
						}

						// 不良
						var symbol = rowdata["symbol"];
						if (symbol != "") {
							$("#nogoodbutton").enable();
						} else {
							$("#nogoodbutton").disable();
						}

						// 位置
						var processing_position = rowdata["processing_position"];
						var inQuote = processing_position.indexOf("报价");
						var inWip = processing_position.indexOf("WIP");
						if (inWip >= 0) {
							$("#movebutton").enable();
							$("#movebutton").val("移动库位");
						} else {
							// 同意日
							var agreed_date = rowdata["agreed_date"];
							var operate_result = rowdata["operate_result"];
							if (operate_result != "2" || inQuote < 0) {
								$("#movebutton").disable();
							} else if (agreed_date.trim() == "") {
								$("#movebutton").enable();
								$("#movebutton").val("入库");
							} else {
								$("#movebutton").enable();
								$("#movebutton").val("再入库");
							}
						}

						$("#sendbutton").enable();
						// 判定
						if (inWip >= 0 || inQuote >= 0) {
							$("#sendqabutton, #sendccdbutton").enable();
						} else {
							$("#sendqabutton, #sendccdbutton").disable();
						}

						$("#returnbutton").enable();
						$("#printbutton").enable().val("重新打印现品票" + $("#performance_list tr#" + id).find("td[aria\-describedby='performance_list_ticket_flg']").text()+"份");
						$("#printaddbutton").enable();
                        
                        if(rowdata.isHistory==1){
                            $("#modifybutton").enable();
                            $("#downloadbutton").enable();
                        }else{
                            $("#modifybutton").disable();
                            $("#downloadbutton").disable();
                        }
					},
					viewsortcols : [true, 'vertical', true],
					gridComplete : function() {
					}
				});
			}
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

$(document).ready(function() {
	$("div.ui-button").button();
	$("input.ui-button").button();
	$("#expeditebutton").disable();
	$("#nogoodbutton").disable();
	$("#returnbutton").disable();
	$("#printbutton").disable();
	$("#printaddbutton").disable();
	$("#movebutton").disable();
	$("#sendbutton").disable();
	$("#sendqabutton").disable();

	$("#expeditebutton").click(line_expedite);
	$("#nogoodbutton").click(treat_nogood);
	$("#returnRCbutton").click(function() {
		doStop("RC");
	});
	$("#returnOSHbutton").click(function() {
		doStop("OSH");
	});
	$("#printbutton").click(printTicket);
	$("#printaddbutton").click(function(){printTicket(1)});
	$("#movebutton").click(doMove);
	$("#sendqabutton").click(doJudge);
	$("#sendccdbutton").click(doCcdChange);
	$("#sendbutton, #returnbutton").hover(
		function(){$(this).find("div.ui-widget-content").show();},
		function(){$(this).find("div.ui-widget-content").hide();}
	);

	$("a.areacloser").hover(
		function (){$(this).addClass("ui-state-hover");}, 
		function (){$(this).removeClass("ui-state-hover");}
	);

	$("#search_agreed_set").buttonset();
	$("#search_level").select2Buttons();
	setReferChooser($("#search_model_id"), $("#model_refer1"));

	// 检索处理
	$("#searchbutton").click(function() {
		// 保存检索条件
		$("#search_sorc_no").data("post", $("#search_sorc_no").val());
		$("#search_serialno").data("post", $("#search_serialno").val());
		$("#search_model_id").data("post", $("#search_model_id").val());
		$("#search_level").data("post", $("#search_level").val());
		$("#search_agreed_set").data("post", $("#search_agreed_set input:checked").val());
		$("#search_wip_location").data("post", $("#search_wip_location").val());
		// 查询
		findits();
	});

	// 清空检索条件
	$("#resetbutton").click(function() {
		$("#search_sorc_no").data("post", "");
		$("#search_serialno").data("post", "");
		$("#search_model_id").data("post", "");
		$("#search_level").data("post", "");
		$("#search_agreed_set").data("post", "");
		$("#search_wip_location").data("post", "");

		$("#search_sorc_no").val("");
		$("#search_serialno").val("");
		$("#search_model_id").val("");
		$("#txt_modelname").val("");
		$("#search_level").val("").trigger("change");
		$("#search_agreed_set input").removeAttr("checked");
		$("#agreed_a").attr("checked", "checked").trigger("change");
		$("#search_wip_location").val("");
	});

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=jsinit',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : jsinit_ajaxSuccess
	});
    
    
    $("#modifybutton").click(function(){
        var selectedId = $("#performance_list").getGridParam("selrow");
        var rowData = $("#performance_list").getRowData(selectedId);
        edit_quotistion(rowData.material_id);
    });
    
    $("#downloadbutton").click(function(){
        var selectedId = $("#performance_list").getGridParam("selrow");
        var rowData = $("#performance_list").getRowData(selectedId);
        var data={
            "material_id":rowData.material_id
        }
        
        $.ajax({
            beforeSend : ajaxRequestType,
            async : true,
            url :'quotaion_prospectus.do?method=report',
            cache : false,
            data : data,
            type : "post",
            dataType : "json",
            success : ajaxSuccessCheck,
            error : ajaxError,
            complete : function(xhjObject) {
                var resInfo = null;
                eval("resInfo=" + xhjObject.responseText);
                if (resInfo && resInfo.fileName) {
                    if ($("iframe").length > 0) {
                        $("iframe").attr("src", "download.do" + "?method=output&filePath=" + resInfo.filePath+"&fileName="+resInfo.fileName);
                    } else {
                        var iframe = document.createElement("iframe");
                        iframe.src = "download.do" + "?method=output&filePath=" + resInfo.filePath+"&fileName="+resInfo.fileName;
                        iframe.style.display = "none";
                        document.body.appendChild(iframe);
                    }
                } else {
                    errorPop("文件导出失败！"); 
                }
            }
        });
        
        
        
    });
    
});

/** 
 * 检索处理
 */
var findits = function() {

	// 读取已记录检索条件提交给后台
	var data = {
		"sorc_no" : $("#search_sorc_no").data("post"),
		"serial_no" : $("#search_serialno").data("post"),
		"model_id" : $("#search_model_id").data("post"),
		"level" : $("#search_level").data("post"),
		"ticket_flg" : $("#search_agreed_set").data("post"),
		"wip_location" : $("#search_wip_location").data("post")
	}

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=search',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : search_handleComplete
	});
};

var doJudge = function() {

	var rowid = $("#performance_list").jqGrid("getGridParam", "selrow");
	var rowdata = $("#performance_list").getRowData(rowid);

	var data = {material_id : rowdata.material_id};

	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doJudge',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					infoPop("已经发送至品保判定，请确认。");
				}
			} catch(e) {
				
			}
		}
	});	
};

var doCcdChange = function() {
	var rowid = $("#performance_list").jqGrid("getGridParam", "selrow");
	var rowdata = $("#performance_list").getRowData(rowid);
	var data = {
		material_id : rowdata["material_id"]
	};
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : 'material.do?method=doreccd',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj, textStatus) {
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length == 0) {
					infoPop("已经可以开始CCD盖玻璃更换作业！");
				} else {
					treatBackMessages(null, resInfo.errors);
				}
			} catch (e) {
				alert("name: " + e.name + " message: " + e.message + " lineNumber: "
						+ e.lineNumber + " fileName: " + e.fileName);
			};
		}
	});
};

var doMove = function() {
	var this_dialog = $("#wip_pop");
	if (this_dialog.length === 0) {
		$("body.outer").append("<div id='wip_pop'/>");
		this_dialog = $("#wip_pop");
	}

	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'wip.do?method=getwipempty',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					this_dialog.hide();
					this_dialog.load("widgets/qf/wip_map.jsp", function(responseText, textStatus, XMLHttpRequest) {
					//新增
					this_dialog.dialog({
						title : "WIP 入库选择",
						width : 688,
						show: "blind",
						height : 'auto' ,
						resizable : false,
						modal : true,
						buttons : {}
					});
			
					this_dialog.find("td").addClass("wip-empty");
					for (var iheap in resInfo.heaps) {
						this_dialog.find("td[wipid="+resInfo.heaps[iheap]+"]").removeClass("wip-empty").addClass("ui-storage-highlight wip-heaped");
					}
			
					//this_dialog.css("cursor", "pointer");
					this_dialog.find(".ui-widget-content").click(function(e){
						if ("TD" == e.target.tagName) {
							if (!$(e.target).hasClass("wip-heaped")) {
								doChangeLocation($(e.target).attr("wipid"));
								this_dialog.dialog("close");
							}
						}
					});
					
					this_dialog.find("div.cage").each(function(index,ele){
						var $tds = $(ele).find("td.ui-storage-highlight.wip-heaped");
						$(ele).find(".ui-widget-header").append("	:	" + $tds.length);
					});
			
					this_dialog.show();
					});
				}
			} catch(e) {
				
			}
		}
	});
}

var doChangeLocation = function(wip_location) {
	var rowid = $("#performance_list").jqGrid("getGridParam", "selrow");
	var rowdata = $("#performance_list").getRowData(rowid);

	var data = {material_id : rowdata.material_id ,wip_location : wip_location};

	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : 'wip.do?method=doChangelocation',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : findit
	});
}


/**
 * 检索Ajax通信成功时的处理
 */
var search_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages("#searcharea", resInfo.errors);
		} else {
			// 读取一览
			listdata = resInfo.list;

			// jqGrid已构建的情况下,重载数据并刷新
			$("#performance_list").jqGrid().clearGridData();
			$("#performance_list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [{current : false}]);
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var findit = function(xhrobj, textStatus) {
	try {
	$("#pop_treat").dialog('close');	
	} catch (e) {
		//TODO 
	}
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=refresh',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : jsinit_ajaxSuccess
	});
}

var doStop=function(return_ocm) {

	var rowid = $("#performance_list").jqGrid("getGridParam", "selrow");
	var rowdata = $("#performance_list").getRowData(rowid);

	$("#confirmmessage").text("未修理返还后，维修对象["+encodeText(rowdata.sorc_no)+"]将会退出RVS系统中的显示，" + ( rowdata.processing_position.indexOf("WIP:") >= 0 ? "自动从WIP出库，在图象检查后" : "" )+ "直接出货，确认操作吗？");
	$("#confirmmessage").dialog({
		resizable : false,
		modal : true,
		title : "返还操作确认",
		buttons : {
			"确认" : function() {
				$(this).dialog("close");
				var data = {
					material_id : rowdata["material_id"],
					return_ocm : return_ocm
				};
			
				$.ajax({
					beforeSend : ajaxRequestType,
					async : false,
					url : 'wip.do?method=dostop',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : function(xhrobj, textStatus){
						// 以Object形式读取JSON
						eval('resInfo =' + xhrobj.responseText);
				
						if (resInfo.errors.length > 0) {
							// 共通出错信息框
							treatBackMessages(null, resInfo.errors);
						} else {
							findit();
						}
					}
				});	
			},
			"取消" : function() {
				$(this).dialog("close");
			}
		}
	});
}

var printTicket=function(addan) {

	var rowid = $("#performance_list").jqGrid("getGridParam","selrow");
	var rowdata = $("#performance_list").getRowData(rowid);

	var data = {
		material_id : rowdata["material_id"]
	}
	if (addan != 1) {
		data.quotator = 1;
	}
	// Ajax提交
	$.ajax({
		beforeSend: ajaxRequestType, 
		async: false, 
		url: 'material.do?method=printTicket', 
		cache: false, 
		data: data, 
		type: "post", 
		dataType: "json", 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		complete:  function(xhrobj, textStatus){
			var resInfo = null;

			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
			
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					if ($("iframe").length > 0) {
						$("iframe").attr("src", "download.do"+"?method=output&fileName="+ rowdata["model_name"] + "-" + rowdata["serial_no"] +"-ticket.pdf&filePath=" + resInfo.tempFile);
					} else {
						var iframe = document.createElement("iframe");
			            iframe.src = "download.do"+"?method=output&fileName="+ rowdata["model_name"] + "-" + rowdata["serial_no"] +"-ticket.pdf&filePath=" + resInfo.tempFile;
			            iframe.style.display = "none";
			            document.body.appendChild(iframe);
					}
				}
			} catch(e) {
			}
		}
	});
};

var doInit = function() {
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=jsinit',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : jsinit_ajaxSuccess
	});
}

var showDetail=function(rid, isManager) {
	var jthis = $("#pop_treat");
	jthis.hide();
	// 导入编辑画面
	jthis.load("widgets/qf/acceptance-edit.jsp", function(responseText, textStatus, XMLHttpRequest) {
		$("#edit_ocm").parent().parent().hide();

		// 读取修改行
		var rowData = $("#performance_list").getRowData(rid);

		// 数据取得
		$("#material_id").val(rowData.material_id);
		$("#edit_sorcno").val(rowData.sorc_no);
		$("#inp_modelname").val(rowData.model_name);
		$("#edit_modelname").val(rowData.model_id);
		$("#edit_serialno").val(rowData.serial_no);
		$("#edit_level").val(rowData.level); 
		$("#edit_direct").val(rowData.direct_flg);
		$("#edit_service_repair").val(rowData.service_repair_flg);
		$("#edit_fix_type").val(rowData.fix_type);
		$("#edit_selectable").val(rowData.selectable);
		$("#edit_customer_name").val(rowData.customer_name); 
		$("#edit_ocm").val(rowData.ocm); 
		$("#edit_ocm_rank").val(rowData.ocm_rank); 
		$("#edit_ocm_deliver_date").val(rowData.ocm_deliver_date); 
		$("#edit_osh_deliver_date").val(rowData.osh_deliver_date); 
		$("#edit_agreed_date").val(rowData.agreed_date_org); 
		if (isManager != "manager") {
			$("#edit_agreed_date").disable();
		}
		$("#edit_bound_out_ocm").val(rowData.bound_out_ocm);
		$("#edit_area").val(rowData.area);

		$("#edit_ocm_deliver_date, #edit_osh_deliver_date, #edit_agreed_date").datepicker({
			showButtonPanel : true,
			dateFormat : "yy/mm/dd",
			currentText : "今天"
	    });

		$("#edit_direct,#edit_service_repair,#edit_fix_type,#edit_selectable,#edit_ocm,#edit_level,#edit_ocm_rank,#edit_area,#edit_bound_out_ocm").select2Buttons();

		$("#edit_customer_name").autocomplete({
			source : customers,
			minLength :2,
			delay : 100
		});

		$("#edit_level").change(function(){
			if (f_isLightFix(this.value) || (this.value >=6 && this.value <= 8)) {
				$("#edit_fix_type").val("2").trigger("change");
			} else {
				$("#edit_fix_type").val("1").trigger("change");
			}
		});

		$("#edit_sorcno,#inp_modelname,#edit_serialno").change(function(){
			treatBackMessages(null, [{errmsg:"修理单号,型号,机身号为重要信息，请慎重修改!"}]);
		});

		setReferChooser($("#edit_modelname"), $("#referchooser_edit"));
		$(".ui-button[value='清空']").button();
		jthis.dialog({
			position : 'auto',
			title : "维修对象信息编辑",
			width : 'auto',
			show: "blind",
			height : 'auto' ,
			resizable : false,
			modal : true,
			minHeight : 200,
			buttons : {
				"确定":function(){
					var data = {
						"material_id":$("#material_id").val(),
						"sorc_no":$("#edit_sorcno").val(),
						"model_id":$("#edit_modelname").val(),
						"serial_no":$("#edit_serialno").val(),
						"customer_name":$("#edit_customer_name").val(),
						"ocm":$("#edit_ocm").val(),
						"ocm_rank":$("#edit_ocm_rank").val(),
						"ocm_deliver_date":$("#edit_ocm_deliver_date").val(),
						"osh_deliver_date":$("#edit_osh_deliver_date").val(),
						"agreed_date": ($("#edit_agreed_date").val() || "9999/12/31"),
						"level":$("#edit_level").val(),
						"direct_flg":$("#edit_direct").val() == "" ? "0" : $("#edit_direct").val(),
						"service_repair_flg":$("#edit_service_repair").val(),
						"fix_type":$("#edit_fix_type").val(),
						"selectable":$("#edit_selectable").val(),
						"bound_out_ocm":$("#edit_bound_out_ocm").val(),
						"area":$("#edit_area").val()
					}
					if ($("#edit_agreed_date").val() && !data.level) {
						errorPop("维修对象同意修理时，需要设定修理等级。", $("#edit_level"));
						return;
					}

					$.ajax({
						beforeSend : ajaxRequestType,
						async : false,
						url : servicePath + '?method=doupdate',
						cache : false,
						data : data,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : function(xhrobj) {
							
							var resInfo = null;
				
							try {
								// 以Object形式读取JSON
								eval('resInfo =' + xhrobj.responseText);
							
								if (resInfo.errors.length > 0) {
									// 共通出错信息框
									treatBackMessages(null, resInfo.errors);
								} else {
									jthis.dialog("close");
									findits();
								}
							} catch(e) {
									alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
							}
						}
					});
				}, "关闭" : function(){ jthis.dialog("close"); }
			}
		});
		jthis.show();
	});
};

var showedit_handleComplete=function(xhrobj){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			$("#pa_main").flowchart("fill", resInfo.processAssigns);
			//对象机型
			if (resInfo.isCcdModel)
				// 增加302工位选择  
				$("#pa_main").prepend('<div class="edgeposition"><div class="just"><div code="25" posid="25" nextcode="9999999" prevcode="0" class="pos"><span>302 CCD 盖玻璃更换</span></div></div>');
			if (resInfo.isLgModel)
				// 增加303工位选择  
				$("#pa_main").prepend('<div class="edgeposition"><div class="just"><div code="60" posid="60" nextcode="0" prevcode="0" class="pos"><span>303 LG 玻璃更换</span></div></div>');

			$("#light_repair_process .subform tbody tr").each(function(index,ele){
				var $tr = $(ele);
				var pat_id = $tr.attr("pat_id");
				
				$tr.removeClass("ui-state-active");
				$tr.addClass("unact");
			});
			
			chosedPat = {};
 			chosedPos = {};

			$(".pos[posid] span").on("click",function(){
				$span = $(this);
				if ($span.hasClass("suceed")) {
					return;
				} else {
					var pos_id = $span.parent().attr("code");
					if ($span.hasClass("point")) {
						$span.removeClass("point");
						chosedPos[pos_id] = 0;
					} else {
						$span.addClass("point");
						chosedPos[pos_id] = 1;
					}
				}
			});
			showResult(3);

			$("#pa_main").parent().trigger("scroll").scrollTop(0);
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
	};
};

var getDetail_ajaxSuccess=function(xhrobj){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {
			
			$("#ref_template").html($("#paOptions").val());
			$("#ref_template").select2Buttons();
			
			categoryKindMap = resInfo.categoryKindMap;
			
			lightRepairs = resInfo.lightFixs;//小修理标准编制
			var processAssigns = resInfo.processAssigns;//维修对象独有修理流程
			var materialForm = resInfo.materialForm;
			
			//工位
			$("#pa_main").html("");
			$("#pa_main").flowchart({},{editable:false, selections: resInfo.list});
			
			if(materialForm.pat_id!=null && materialForm.pat_id!=''){
				$("#ref_template").val(materialForm.pat_id).trigger("change");
			}else if(lightRepairs.length>0){
				var kind = lightRepairs[0].kind;
				$("#s2b_ref_template a:contains('"+categoryKindMap[kind]+"'):first").trigger("click");
				$("#s2b_ref_template a:contains('"+categoryKindMap[kind]+"小修理'):first").trigger("click");
			}else{
				$("#ref_template").val("").trigger("change");
			}
			
			//生产小修理流程工位内容
			var patContent = "";
			for (var index in lightRepairs) {
				var lightRepair = lightRepairs[index];
				patContent += "<tr pat_id='"+ lightRepair.light_fix_id +"' class='unact'>" 
								+ "<td>" + lightRepair.activity_code + "</td>"
								+ "<td>" + lightRepair.description + "</td>"
						   + "</tr>";
			};
			
			$("#light_repair_process .subform tbody").html(patContent);
			
			
			
			setTimeout(function(){
				var materialLightFixs = resInfo.materialLightFixs;//维修对象选用小修理
				$("#light_repair_process .subform tbody tr").removeClass("ui-state-active").addClass("unact");
				for(var index in materialLightFixs){
					var materialLightFix = materialLightFixs[index];
					var pat_id = materialLightFix.light_fix_id;
					chosedPat[pat_id] = 1;
					$("#light_repair_process .subform tbody tr").each(function(){
						var $tr = $(this);
						if(pat_id==$tr.attr("pat_id")) $tr.addClass("ui-state-active").removeClass("unact");
					});
				}
				
				changeFlow();
				$("#pa_main div.pos").each(function(index,ele){
					var $div = $(ele);
					var code = $div.attr("code");
					var $span = $div.find("span");
					for(var i=0;i<processAssigns.length;i++){
						var obj = processAssigns[i];
						if(code == obj.position_id && !$span.hasClass("suceed")){
							$span.addClass("point");
							chosedPos[code]=1;
						}
					}
				});

 				showResult(4);

			},300);
			
			//小修理流程工位TR单击事件
			$("#light_repair_process .subform tbody tr").click(function(){
				var $tr = $(this);
				var pat_id = $tr.attr("pat_id");
				
				if ($tr.hasClass("ui-state-active")) {
					$tr.removeClass("ui-state-active");
					$tr.addClass("unact");
					chosedPat[pat_id] = 0;
				}else{
					$tr.addClass("ui-state-active");
					$tr.removeClass("unact");
					chosedPat[pat_id] = 1;
				}
				changeFlow();
		 		showResult(5);
			});
						
			$("#light_fix_dialog").dialog({
				title : "中小修理维修内容流程设定",
				modal : true,
				width: 980,
				height : 660,
				resizable: false,
				buttons:{
					"确定":function(){
						update_material_process_assign();
					},
					"取消":function(){
						$(this).dialog("close");
					}
				}
			});
			
			$("#light_fix_dialog").show();
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
	};
};

var changeFlow = function(){
	var resultPos = {};
	for (var pat_id in chosedPat) {
		if (chosedPat[pat_id] == 1) {
			for (var iLightRepair in lightRepairs) {
				var lightRepair = lightRepairs[iLightRepair];
				if (pat_id == lightRepair.light_fix_id) {
					for (var iprocesses in lightRepair.position_list) {
						var process = lightRepair.position_list[iprocesses];
						resultPos[process] = 1;
					}
				}
			}
		}
	};
	$(".pos[posid]").find("span").removeClass("suceed").removeClass("point");//清楚所有样式
	
	for (var process in resultPos) {
		$(".pos[code="+ process +"]").find("span").addClass("suceed");
	};
	
	for (var process in chosedPos) {
		if (chosedPos[process] == 1)
		$(".pos[code="+ process +"]").find("span:not('.suceed')").addClass("point");
	}
};

var update_material_process_assign=function(){
	var data={
		"material_id":$("#material_id").val(),
		"pad_id":$("#ref_template").val()
	};
	
	var i=0;
	for (var pat_id in chosedPat) {
		if (chosedPat[pat_id] == 1) {
			data["material_light_fix.light_fix_id[" + i + "]"] = pat_id;
			i++;
		}
	}
	
	//工位
	var count=0;
	var chainHead = null;
	var chain = {};
	var total = $("#pa_main div.pos span").length;
	$("#pa_main div.pos span").each(function(index,ele){
		var $span = $(ele);
		if($span.hasClass("point") || $span.hasClass("suceed")){
			var $posData = $span.parent();
			var code = $posData.attr("code");
			if (code != "25") {
				var item = {code: code};
				if ($posData.attr("prevcode") == "0" && $posData.parents(".just-multi").length == 0) {
					chainHead = item;
				}
				item["prev"] = getPrevPos($posData);
				chain[code] = item;
			}
			data["material_process_assign.position_id[" + count + "]"] = code;
			if (count > 0) {
				data["material_process_assign.next_position_id[" + (count-1) + "]"] = code;
			}
			if (count < (total - 1)) {
				data["material_process_assign.prev_position_id[" + (count+1) + "]"] = code;
			}
			count++;
		}
	});

	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'material_process_assign.do?method=doUpdateMaterialProcessAssign',
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
			
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					$("#light_fix_dialog").dialog("close");
				}
			} catch(e) {
				alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
			}
		}
	});	
};

function getPrevPos($posData){
	var posid = $posData.attr("posid");
	var code = $posData.attr("code");
	var prevcode = $posData.attr("prevcode");
	var nextcode = $posData.attr("nextcode");
	if (code == null) return null;

	if (posid == code) {
		if (prevcode == "0") {
			var $justMulti = $posData.parents(".just-multi:first");
			if ($justMulti.length == 0) {
				return "0";
			} else {
				return getPrevPos($justMulti.parent().parent());
			}
		}
		var $thePrev = $("#pa_main").find(".div[code="+code+"]");
		if ($thePrev.children("span").hasClass("point") || $thePrev.children("span").hasClass("suceed")) {
			return $thePrev.attr("code");
		} else {
			return getPrevPos($thePrev);
		}
	}
	return null;
}

var showResult= function(intt) {
//	var $showTargets = $("#light_repair_record > span");
//	if(intt) $showTargets.text($showTargets.text() + intt);

	var $showTarget = $("#light_repair_record > div");
	var processText = "";
	$("#light_repair_process .ui-state-active").each(function(idx, ele){
		processText += "; " + $(ele).find("td:eq(1)").text();
	});
	var positionText = "";
	$("#pa_main .suceed,#pa_main .point").each(function(idx, ele){
		positionText += "->" + $(ele).text();
	});

	var showText = "";
	if (processText.length) {
		showText += "所选择的中小修理流程为：" + processText.substring(2);
		if (positionText.length) {
			showText += "<BR>作业流程为：" + positionText.substring(2);
		}
	} else {
		if (positionText.length) {
			showText += "作业流程为：" + positionText.substring(2);
		}
	}
	$showTarget.html(showText);
}
