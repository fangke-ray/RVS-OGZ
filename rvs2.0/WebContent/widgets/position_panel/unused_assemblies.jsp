<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<div id="unused_assemblies_area">
<style>

</style>
<script type="text/javascript">

$(function() {

	var refreshUnusedAssemblies = function() {
		var postData = {
			position_id : 101, // 001岗位
			status : 4 // 未使用状态
		};

		// Ajax提交
		$.ajax({
			beforeSend: ajaxRequestType, 
			async: false, 
			url: 'snouts.do?method=search', 
			cache: false, 
			data: postData, 
			type: "post", 
			dataType: "json", 
			success: ajaxSuccessCheck, 
			error: ajaxError, 
			complete:  function(xhrobj, textStatus){
				// 读取JSON
				var resInfo = $.parseJSON(xhrobj.responseText);
	
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					var listdata = resInfo.list;
					
					if ($("#gbox_unused_assemblies_list").length > 0) {
						$("#unused_assemblies_list").jqGrid().clearGridData();
						$("#unused_assemblies_list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [{current : false}]);
					} else {
						$("#unused_assemblies_list").jqGrid({
							toppager : true,
							data : listdata,
							height : 231,
							width : 1246,
							rowheight : 23,
							datatype : "local",
							colNames : ['model_id', '部组型号', '部组序列号', '完成时间', '制造人'],
							colModel : [
								{name:'model_id',index:'model_id', hidden:true},
								{name:'model_name',index:'model_id', width:75},
								{name:'serial_no',index:'serial_no', width:75},
								{name:'finish_time',index:'finish_time', width:50, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d H:i:s',newformat:'Y-m-d H:i'}},
								{name:'operator_name',index:'operator_name', width:60}
							],
							rowNum : 50,
							toppager : false,
							pager : "#unused_assemblies_listpager",
							viewrecords : true,
							caption : null,
							gridview : true, // Speed up
							pagerpos : 'right',
							pgbuttons : true,
							pginput : false,
							recordpos : 'left',
							viewsortcols : [true, 'vertical', true]
						});
					}
				}
			}
		});
	}

	var deleteUnusedAssemblies = function() {
		var rowid = $("#unused_assemblies_list").jqGrid("getGridParam", "selrow");
		if (rowid) {
			var rowdata = $("#unused_assemblies_list").getRowData(rowid);
			warningConfirm("该操作不可恢复，是否真的要废弃？", function () {
				// Ajax提交
				$.ajax({
					beforeSend : ajaxRequestType,
					async : true,
					url : 'snouts.do?method=doDelete',
					cache : false,
					data : {position_id: "00000000101", serial_no: rowdata["serial_no"], model_id : rowdata["model_id"]},
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : function(xhrObj) {
						refreshUnusedAssemblies();
					}
				});
			})
		} else {
			errorPop("请选择要删除的部组件。");
		}
	}

	refreshUnusedAssemblies();

	$("#unused_assemblies_delete_button").click(deleteUnusedAssemblies);
	$("#unused_assemblies_refresh_button").click(refreshUnusedAssemblies);

});
</script>
<div class="ui-widget-content dwidth-full" id="material_details" style="margin-bottom: 16px; display: block;">

	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
		<span class="areatitle">完成可使用部组信息</span>
	</div>
	<table id="unused_assemblies_list"></table>
	<div id="unused_assemblies_listpager"></div>

	<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase">
		<div id="unused_assemblies_executes" style="margin-left: 4px; margin-top: 4px;">
			<input id="unused_assemblies_delete_button" class="ui-button" value="废弃" type="button"/>
			<input id="unused_assemblies_refresh_button" class="ui-button" value="刷新" type="button"/>
		</div>
	</div>

</div>
</div>