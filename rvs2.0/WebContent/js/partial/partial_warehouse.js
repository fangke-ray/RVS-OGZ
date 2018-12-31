var servicePath = "partial_warehouse.do";

$(function(){
	$("input.ui-button").button();
	
	/*为每一个匹配的元素的特定事件绑定一个事件处理函数*/
    $("#body-mdl span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});
    
    $("#search_warehouse_date_start,#search_warehouse_date_end,#search_finish_date_start,#search_finish_date_end").datepicker({
		showButtonPanel:true,
		dateFormat: "yy/mm/dd",
		currentText: "今天"
	});
    
    $("#resetbutton").click(function(){
    	reset();
    });
    
    $("#searchbutton").click(function(){
    	findit();
    });
    
    $("#gobackbutton").click(function(){
    	$("#search").show();
    	$("#detail").hide();
    });
    
    findit();
});

function reset(){
	$("#search_dn_no,#search_warehouse_date_start,#search_warehouse_date_end,#search_finish_date_start,#search_finish_date_end").val("");
};

function findit(){
	var data = {
		"dn_no" : $("#search_dn_no").val(),
		"warehouse_date_start" : $("#search_warehouse_date_start").val(),
		"warehouse_date_end" : $("#search_warehouse_date_end").val(),
		"finish_date_start" : $("#search_finish_date_start").val(),
		"finish_date_end" : $("#search_finish_date_end").val()
	};
	
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
		complete : function(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					list(resInfo.finish);
				}
			}catch(e){}
		}
	});
};

function list(listdata){
	if ($("#gbox_list").length > 0) {
		$("#list").jqGrid().clearGridData();// 清除
		$("#list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
	} else {
		$("#list").jqGrid({
			data : listdata,// 数据
			height :461,// rowheight*rowNum+1
			width : 992,
			rowheight : 23,
			shrinkToFit:true,
			datatype : "local",
			colNames : ['','日期','DN 编号', '入库单总数量', '核对总数量', '入库进展', '核对一致'],
			colModel : [{name : 'key',index : 'key',hidden:true},
			            {name : 'warehouse_date',index : 'warehouse_date',width:30},
			            {name : 'dn_no',index : 'dn_no',width:50},
			            {name : 'quantity',index : 'quantity',align:'right', width:50, formatter:'integer', sorttype:'integer', formatoptions:{thousandsSeparator: ','}},
			            {name : 'collation_quantity',index : 'collation_quantity',align:'right', width:50, formatter:'integer', sorttype:'integer', formatoptions:{thousandsSeparator: ','}},
			            {name : 'step',index : 'step', align:'center', width:30, formatter:'select', editoptions:{value:$("#goStep").val()}},
			            {name : 'match',index : 'match', align:'center', formatter : function(value, options, rData){
							if(value == 0){
								return '一致';
							}else{
								return '差异';
							}
						}, width:30}
			],
			rowNum : 20,
			toppager : false,
			pager : "#listpager",
			viewrecords : true,
			caption : "",
			multiselect : false,
			gridview : true,
			pagerpos : 'right',
			pgbuttons : true, // 翻页按钮
			rownumbers : true,
			pginput : false,					
			recordpos : 'left',
			hidegrid : false,
			deselectAfterSort : false,
			onSelectRow : null,// 当选择行时触发此事件。
			ondblClickRow : function(rid, iRow, iCol, e) {
				showDetail();
			},
			viewsortcols : [ true, 'vertical', true ],
			gridComplete : function() {
			}
		});
	}
};

function showDetail(){
	var row = $("#list").jqGrid("getGridParam", "selrow");// 得到选中行的ID	
	var rowData = $("#list").getRowData(row);
	var data = {
		"key": rowData.key
	};
	
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
		complete : function(xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					$("#search").hide();
					$("#detail").show();
					detaillist(resInfo.list);
				}
			}catch(e){}
		}
	});
};

function detaillist(listdata){
	if ($("#gbox_detaillist").length > 0) {
		$("#detaillist").jqGrid().clearGridData();// 清除
		$("#detaillist").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
	} else {
		$("#detaillist").jqGrid({
			data : listdata,// 数据
			height :691,// rowheight*rowNum+1
			width : 992,
			rowheight : 23,
			shrinkToFit:true,
			datatype : "local",
			colNames : ['零件编号','零件名称','规格种别','数量','核对数量',''],
			colModel : [{name : 'code',index : 'code',width:50},
			            {name : 'partial_name',index : 'partial_name',width:200},
			            {name : 'spec_kind_name',index : 'spec_kind_name', align:'center',width:50},
			            {name : 'quantity',index : 'quantity',sorttype:'integer',width:50,align : 'right'},
			            {name : 'collation_quantity',index : 'collation_quantity',sorttype:'integer',width:50,align:'right',formatter : function(value, options, rData){
							if(value < 0){
								rData.flg = "0";
								return  value * -1;
							}else{
								return  value;
							}
						}},
						{name : 'flg',index : 'flg',hidden : true}
			],
			rowNum : 30,
			toppager : false,
			pager : "#detaillistpager",
			viewrecords : true,
			caption : "",
			multiselect : false,
			gridview : true,
			pagerpos : 'right',
			pgbuttons : true, // 翻页按钮
			rownumbers : true,
			pginput : false,					
			recordpos : 'left',
			hidegrid : false,
			deselectAfterSort : false,
			onSelectRow : null,// 当选择行时触发此事件。
			ondblClickRow : function(rid, iRow, iCol, e) {
				showDetail();
			},
			viewsortcols : [ true, 'vertical', true ],
			gridComplete : function() {
				var IDS = $("#detaillist").getDataIDs();
				// 当前显示多少条
				var length = IDS.length;
				var pill = $("#detaillist");
				
				for (var i = 0; i < length; i++) {
					var rowData = pill.jqGrid('getRowData', IDS[i]);
					var flg = rowData["flg"];
					if(flg == "0" || flg == "1"){
						pill.find("tr#" +IDS[i] +" td").css({"background-color":"#E48E38","color":"#fff"});
					}
				}
			}
		});
	}
};


