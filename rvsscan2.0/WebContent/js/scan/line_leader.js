var listdata = {};
var servicePath = "lineLeader.scan";
var position_counts = {};
var checked_position_id = "";
var chart2;

var jsinit_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {

			listdata = resInfo.performance;

			if ($("#gbox_performance_list").length > 0) {
				$("#performance_list").jqGrid().clearGridData();
				$("#performance_list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [{current : false}]);
			} else {
				$("#performance_list").jqGrid({
					toppager : true,
					data : listdata,
					height : 507,
					width : 485,
					rowheight : 23,
					datatype : "local",
					colNames : ['同意日', '加急', '修理单号', '零件订购日','零件BO','入库预定日','ESAS No.', '不良', '等级', '机身号', '机种', '型号', '状态', '位置', '产出安排' ,'本工程外位置',
							'material_id', 'position_id', 'is_reworking', 'is_today'],
					colModel : [{
								name : 'agreed_date',
								index : 'agreed_date',
								width : 50,
								align : 'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}
							},
							{
								name : 'expedited',
								index : 'expedited',
								hidden : true
							}, {
								name : 'sorc_no',
								index : 'sorc_no',
								width : 110
							}, {
								name : 'partical_order_date',
								index : 'partical_order_date',
								width : 85,
								align:'center',
								hidden : true, formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}
							}, {
								name : 'partical_bo',
								index : 'partical_bo',
								formatter: "select", editoptions:{value:"0:;1:BO"},
								align:'center',
								width : 65,
								hidden : true},
						{name:'arrival_plan_date',index:'arrival_plan_date', width:85, align:'center', hidden : true,
						formatter:function(a,b,row) {
							if ("9999/12/31" == a) {
								return "未定";
							}
							
							if (a) {
								var d = new Date(a);
								return (d.getMonth()+1)+"-"+d.getDate();
							}
							
							return "";
						}},
						{name:'esas_no',index:'esas_no', width:55, hidden:true},
						{
								name : 'symbol',
								index : 'symbol',
								width : 35,
								align : 'center'
							}, {
								name : 'level',
								index : 'level',
								formatter: "select", editoptions:{value:resInfo.opt_level},
								width : 35,
								align : 'center'
							}, {
								name : 'serial_no',
								index : 'serial_no',
								width : 55
							},
						{name:'category_name',index:'category_name', width:50, hidden : true},
							 {
								name : 'model_name',
								index : 'model_name',
								width : 125
							},
							{
								name : 'operate_result',
								index : 'operate_result',
								width : 50,
								formatter: "select", editoptions:{value:resInfo.opt_operate_result},
								hidden : true
							}, {
								name : 'process_code',
								index : 'process_code',
								width : 50,
								align : 'center'
							},
						{name:'scheduled_date',index:'scheduled_date', width:60, align:'center', hidden:true, formatter:'date', formatoptions:{srcformat:'Y/m/d', newformat:'m-d'}},
						{name:'otherline_process_code',index:'otherline_process_code', width:65, align:'center', hidden:true},
						{name : 'material_id', index : 'material_id', hidden : true},
						{name : 'position_id', index : 'position_id', hidden : true},
						{name : 'is_reworking', index : 'is_reworking', hidden : true},
						{name : 'is_today', index : 'is_today', hidden : true}
							],
					rowNum : 50,
					scrollOffset : 0,
					toppager : false,
					viewrecords : true,
					caption : "",
					gridview : true, // Speed up
					pagerpos : 'right',
					pgbuttons : true,
					pginput : false,
					recordpos : 'left',
					viewsortcols : [true, 'vertical', true],
					gridComplete : function() {
						// ②在gridComplete调用合并方法
						var gridName = "performance_list";
					 	Merger(gridName, 'agreed_date');
					}
				});
			}

			position_counts = resInfo.counts;

			$("#sikake").text(listdata.length); // TODO real sikake

			chart2 = new Highcharts.Chart({
				colors : ['#92D050', '#7faad4'],
				chart : {
					renderTo : 'processing_container',
					type : 'bar',
					marginRight : 10,
					backgroundColor : "white",
					height : 427,
					width : 486
				},
				credits : {
					enabled : false
				},
				title : {
					text : ''
				},
				xAxis : {
					categories : resInfo.categories,
					labels : {
						style : {
							fontWeight : 'bold',
							fontSize : '15px'
						}
					}
				},
				yAxis : {
					title : {
						text : '台数',
						style : {
							fontWeight : 'bold',
							fontSize : '16px'
						}
					},
					allowDecimals: false
				},
				tooltip : {
					animation : false,
					formatter : function() {
						return '<b>' + this.series.name + '</b><br/>' + this.y + '台<br/>';
					},
					style : {
						fontWeight : 'bold',
						fontSize : '12px'
		
					},
					labels : {
						style : {
							fontWeight : 'bold',
							fontSize : '18px'
						}
					}
				},
				plotOptions : {
					series : {
						point : {
							events : {
							}
						}
					},
					area : {
						lineWidth : 0,
						marker : {
							enabled : false,
		                    states: {
		                        hover: {
		                            enabled: false
		                        }
		                    }
                    	},
						dataLabels : {
							enabled: true,
							color : 'black',
							formatter : function() {
								if ((this.x - .25) === parseInt(this.x)) {
									return this.y;
								} else {
									return '';
								}
							},
							y : 10
						},
						pointInterval : .25,
						pointStart : -.25
					},
					bar : {
						dataLabels : {
							enabled: true,
							color : 'green'
						}
					}
				},
				legend : {
					enabled : false
				},
				exporting : {
					enabled : false
				},
		
				series : [{
					type : 'bar',
					name : '台数',
					data : position_counts,
					zIndex : 2
				}, {
					type : 'area',
					name : '警戒线',
					data : resInfo.overlines
				}]
			});
		}

		$("tspan").hover(
			function() {
				var jthis = $(this);
				jthis.parent().css("color", "navy");
			}
		);
	} catch (e) {
	};
};

$(document).ready(function() {

	var data = {
		line_id : $("#page_line_id").val(), section_id : $("#page_section_id").val()
	}

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=refresh',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : jsinit_ajaxSuccess
	});

	setInterval(refresh, 20000);
});

// 公共调用方法
function Merger(gridName, CellName) {
	var rheight = $("div.ui-jqgrid-bdiv").css("overflow-y","hidden").attr("height");

	// 得到显示到界面的id集合
	var mya = $("#" + gridName + "").getDataIDs();
	// 当前显示多少条
	var length = mya.length;

	var pill = $("#" + gridName + "");

	for (var i = 0; i < length; i++) {
		// 从上到下获取一条信息
		var before = pill.jqGrid('getRowData', mya[i]);
		// 定义合并行数
		var rowSpanTaxCount = 1;
		// 操作中区分
		var status = before["operate_result"];
		if (status == "0" || status == "4") {
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_process_code']").css("color", "#0080FF");
		} else if (status == "1") {
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_process_code']").css("color", "#58b848");
		} else if (status == "3") {
			// 不良
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_process_code']").css("color", "red");
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_process_code']").css("font-weight", "bolder");
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_symbol']").css("background-color", "yellow").css("font-weight", "bolder"); // .text("※")
		}
		// 当日计划
		var today = before["is_today"];
		if (today == "1") {
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_sorc_no']").css("color", "green");
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_sorc_no']").css("font-weight", "bolder");
		}
		// 紧急区分
		var expedited = before["expedited"];
		if (expedited == "1") {
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_sorc_no']").css("color", "blue");
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_sorc_no']").css("font-weight", "bolder");
		} else
		if (expedited == "10" || expedited == "11") {
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_sorc_no']").css("color", "#F0C800;");
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_sorc_no']").css("font-weight", "bolder");
		}
		// 返工中
		var reworking = before["is_reworking"];
		if (reworking == "1") {
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_process_code']").css("color", "pink");
		}
						// 入库预定日
//						if (before["arrival_plan_date"] != "")
//							alert(before["arrival_plan_date"]);

		for (var j = i + 1; j <= length; j++) {
			// 和上边的信息对比 如果值一样就合并行数+1 然后设置rowspan 让当前单元格隐藏
			var end = pill.jqGrid('getRowData', mya[j]);
			if (before[CellName] == end[CellName]) {
				rowSpanTaxCount++;
				pill.setCell(mya[j], CellName, '', {display : 'none'});
			} else {
				rowSpanTaxCount = 1;
				break;
			}
			pill.find("tr#" + mya[i] + " td[aria\\-describedby='" + gridName + "_"
					+ CellName + "']").attr("rowspan", rowSpanTaxCount);
		}
	}
}

var refresh = function() {
	var data = {
		line_id : $("#page_line_id").val(), section_id : $("#page_section_id").val(), position_id : checked_position_id
	}

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=refresh',
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

				listdata = resInfo.performance;
	
				$("#performance_list").jqGrid().clearGridData();
				$("#performance_list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [{current : false}]);

				chart2.series[0].setData(resInfo.counts);
			} catch (e) {
			};
		}
	});
}

