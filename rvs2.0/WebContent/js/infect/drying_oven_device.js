var current_tab = "page_drying_oven_device_tab";
var showDelete = function(rid){
	drying_oven_device_action.del(rid);
};

/**
 * 初始话页面元素
 */
function init(){
	$("input.ui-button").button();
	$("div.ui-buttonset").buttonset();
	
	//默认选中烘箱管理
	$("#page_drying_oven_device_tab").attr("checked","checked").trigger("change");
	dryingOvenDeviceManage();
	
//	$("#tabs input:radio").click(function(e){
//		var target =  e.target.id;
//		current_tab = target;
//		if(target == "page_drying_oven_device_tab"){
//			$("#page_drying_oven_device").show();
//			$("#page_drying_job").hide();
//			$("title").html("烘干设备管理");
//			
//			$("#drying_oven_device_init").show();
//			$("#drying_oven_device_add,#drying_oven_device_update").hide();
//		}else if(target == "page_drying_job_tab"){
//			$("#page_drying_oven_device").hide();
//			$("#page_drying_job").show();
//			$("title").html("烘干作业");
//			
//			$("#page_drying_job_init").show();
//			$("#page_drying_job_add,#page_drying_job_update").hide();
//		}
//	});
};

var drying_oven_device_action = {
	servicePath : "drying_oven_device.do",
	
	reset:function(){
		$("#search_m_manage_code,#hidden_search_m_device_manage_id").val("");
		$("#search_m_setting_temperature").val("").trigger("change");
	},
	
	findit : function() {
		var searchData = {
			"device_manage_id" : $("#hidden_search_m_device_manage_id").val(),
			"setting_temperature" : $("#search_m_setting_temperature").val()
		};

		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : drying_oven_device_action.servicePath + '?method=search',
			cache : false,
			data : searchData,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : drying_oven_device_action.doInit_ajaxSuccess
		});
	},
	
	doInit_ajaxSuccess : function(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
	
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				drying_oven_device_action.show_list(resInfo.finished);
			}
		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		};
	},
	
	show_list : function(list) {
		if ($("#gbox_drying_oven_device_list").length > 0) {
			// jqGrid已构建的情况下,重载数据并刷新
			$("#drying_oven_device_list").jqGrid().clearGridData();
			$("#drying_oven_device_list").jqGrid('setGridParam', {data : list}).trigger("reloadGrid", [{current : false}]);
		} else {
			$("#drying_oven_device_list").jqGrid({
				data : list,
				height : 461,
				width : 992,
				rowheight : 23,
				datatype : "local",
				colNames : ['','管理编号ID', '管理编号', '型号名称','设定温度', '实测温度下限', '实测温度上限','库位数'],
				colModel : [
					{name:'myac', width:30, fixed:true, sortable:false, resize:false,formatter:'actions',formatoptions:{keys:true, editbutton:false}},
					{name:'device_manage_id',index :'device_manage_id',hidden:true},
					{name:'manage_code',index :'manage_code',width :160},
					{name:'model_name',index :'model_name',width :160},
					{name:'setting_temperature',index : 'setting_temperature',width:160,formatter:'select', editoptions:{value:$("#hidden_sDryingOvenSettingTemperature").val()}},
					{name:'lower_limit',index :'lower_limit',align:'right'},
				    {name:'upper_limit',index :'upper_limit',align:'right'}, 
				    {name:'slot',index:'slot',align:'right'}
				],
				rowNum : 35,
				rownumbers:true,
				toppager : false,
				pager : "#drying_oven_device_list_pager",
				viewrecords : true,
				gridview : true,
				pagerpos : 'right',
				pgbuttons : true,
				pginput : false,
				recordpos : 'left',
				hidegrid : false,
				deselectAfterSort : false,
				ondblClickRow : drying_oven_device_action.showDryingOvenDeviceEdit,
				viewsortcols : [true, 'vertical', true]
			});
			$("#gbox_drying_oven_device_list .ui-jqgrid-hbox").before('<div class="ui-widget-content" style="padding:4px;">' +
				'<input type="button" class="ui-button" id="add_drying_oven_device_button" value="新建烘干设备管理" aria-disabled="false">' +
			'</div>');
			$("#add_drying_oven_device_button").button();
			
			//新建烘箱
			$("#add_drying_oven_device_button").unbind("click");
			$("#add_drying_oven_device_button").click(function(){
				$("#add_m_form input,#add_m_form select").removeClass("valid errorarea-single");
				$("#add_m_manage_code,#hidden_add_m_device_manage_id,#add_m_slot").val("");
				$("#add_m_setting_temperature").val("").trigger("change");
				
				$("#drying_oven_device_add").show();
				$("#drying_oven_device_init").hide();
			});
		}
	},
	
	add : function(){
		$("#add_m_form").validate({
			rules:{	
				device_manage_id:{
				   required:true
			   },
			   setting_temperature:{
				   required:true,
				   digits:true
			   },
			   slot:{
			   	   required:true,
				   digits:true,
				   range:[1,99]
			   }
			}
	     });
		
		if( $("#add_m_form").valid()) {
			var data = {
				"device_manage_id" : $("#hidden_add_m_device_manage_id").val(),
				"manage_code" : $("#add_m_manage_code").val(),
				"setting_temperature" : $("#add_m_setting_temperature").val(),
				"slot" : $("#add_m_slot").val()
			};
			
			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url : drying_oven_device_action.servicePath + '?method=doInsert',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : drying_oven_device_action.doInsert_ajaxSuccess
			});
		}
	},
	
	doInsert_ajaxSuccess : function(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
	
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				infoPop("新建已经完成。", null, "新建");

               drying_oven_device_action.findit();
               $("#drying_oven_device_init").show();
			   $("#drying_oven_device_add").hide();
			}

		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		};
	},
	
	showDryingOvenDeviceEdit : function(){
		var rowID = $("#drying_oven_device_list").jqGrid("getGridParam", "selrow");// 得到选中行的ID
		var rowData = $("#drying_oven_device_list").getRowData(rowID);
		
		$("#update_m_form input,#update_m_form select").removeClass("valid errorarea-single");
		
		$("#label_m_manage_code").text(rowData.manage_code);//管理编号
		$("#label_m_model_name").text(rowData.model_name);//型号名称
		$("#update_m_setting_temperature").val(rowData.setting_temperature).trigger("change");//设定温度
		$("#update_m_slot").val(rowData.slot);//库位数
		
		$("#drying_oven_device_update").show();
		$("#drying_oven_device_init").hide();
	},
	
	update:function(){
		var rowID = $("#drying_oven_device_list").jqGrid("getGridParam", "selrow");// 得到选中行的ID
		var rowData = $("#drying_oven_device_list").getRowData(rowID);
		
		$("#update_m_form").validate({
			rules:{	
			   setting_temperature:{
				   required:true,
				   digits:true
			   },
			   slot:{
			   	   required:true,
				   digits:true,
				   range:[1,99]
			   }
			}
	     });
		
		if( $("#update_m_form").valid()) {
			var data = {
				"device_manage_id" : rowData.device_manage_id,
				"setting_temperature" : $("#update_m_setting_temperature").val(),
				"slot" : $("#update_m_slot").val()
			};
			
			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url : drying_oven_device_action.servicePath + '?method=doUpdate',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : drying_oven_device_action.doUpdate_ajaxSuccess
			});
		}
	},
	
	doUpdate_ajaxSuccess : function(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
	
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				infoPop("更新已经完成。", null, "更新");

               drying_oven_device_action.findit();
               $("#drying_oven_device_init").show();
			   $("#drying_oven_device_update").hide();
			}
		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		};
	},
	
	del : function(rid){
		var rowData = $("#drying_oven_device_list").getRowData(rid);
		var data = {
			"device_manage_id" : rowData.device_manage_id
	    };

		warningConfirm("删除不能恢复。确认要删除设备管理编号:"+encodeText(rowData.manage_code)+"的记录吗？", function() {

			// Ajax提交
			$.ajax({
				beforeSend : ajaxRequestType,
				async : false,
				url : drying_oven_device_action.servicePath + '?method=doDelete',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : drying_oven_device_action.delete_handleComplete
			});
		}, null, "删除确认");
	},
	
	delete_handleComplete : function(xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
	
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				infoPop("删除已经完成。", null, "删除");
	            drying_oven_device_action.findit();
			}
		} catch (e) {
			alert("name: " + e.name + " message: " + e.message + " lineNumber: " + e.lineNumber + " fileName: " + e.fileName);
		};
	}
};

//烘箱管理
function dryingOvenDeviceManage(){
	$("#search_m_setting_temperature,#add_m_setting_temperature,#update_m_setting_temperature").select2Buttons();

	/*管理编号*/
	setReferChooser($("#hidden_search_m_device_manage_id"),$("#search_m_device_manage_id_referchooser"));
	setReferChooser($("#hidden_add_m_device_manage_id"),$("#add_m_device_manage_id_referchooser"));
	
	/*清除*/
	$("#reset_m_button").click(function() {
		drying_oven_device_action.reset();
	});
	
	//检索
	$("#search_m_button").click(function() {
		drying_oven_device_action.findit();
	});
	
	//新建取消
	$("#add_m_resetbutton").click(function(){
		$("#drying_oven_device_init").show();
		$("#drying_oven_device_add").hide();
	});
	
	//新建确认
	$("#add_m_comfirmbutton").click(function(){
		drying_oven_device_action.add();
	});
	
	//更新取消
	$("#update_m_resetbutton").click(function(){
		$("#drying_oven_device_init").show();
		$("#drying_oven_device_update").hide();
	});
	
	//更新确认
	$("#update_m_comfirmbutton").click(function(){
		drying_oven_device_action.update();
	});
	
	//$("#search_m_button").trigger("click");
	drying_oven_device_action.findit();
};

$(function() {
	init();
});