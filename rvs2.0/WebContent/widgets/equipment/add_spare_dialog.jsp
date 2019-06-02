<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<script type="text/javascript">

$(function(){
	$("#add_spare_filterbutton").click(filterSpareList);
	$("#add_spare_clearbutton").click(function(){
		$("#add_spare_dialog").find("#add_spare_device_type_name").val("")
			.end().find("#add_spare_model_name").val("");
		filterSpareList();
	});

});

var showSparelist = function(listdata){
	if ($("#gbox_sp_list").length > 0) {
		$("#sp_list").jqGrid().clearGridData();// 清除
		$("#sp_list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
	} else {
		$("#sp_list").jqGrid({
			data : listdata,// 数据
			height :201,// rowheight*rowNum+1
			width : 640,
			rowheight : 10,
			shrinkToFit:true,
			datatype : "local",
			colNames : ['品名','型号','品牌','单价','Min<BR>Limit','Max<BR>Limit','当前<BR>有效库存','管理备注','device_type_id','brand_id'],
			colModel : [{name : 'device_type_name',index : 'device_type_name',width:100},
						{name : 'model_name',index : 'model_name',width:100},
						{name : 'brand_name',index : 'brand_name',width:100, formatter : function(value, options, rData) {
		                    if(rData.brand_name){
	                            return "<a href='javascript:showBrandDetail(\""+ rData.brand_id +"\")'>" + rData.brand_name + "</a>";
		                    }else{
		                        return "";
		                    }                           
		                }},
						{name : 'price',index : 'price',width:50,align:'right',sorttype:'currency',formatter:'currency',formatoptions:{thousandsSeparator:',',defaultValue: '',decimalPlaces:0}},
						{name : 'safety_lever',index : 'safety_lever',width:50,align:'right',sorttype:'number'},
						{name : 'benchmark',index : 'benchmark',width:50,align:'right',sorttype:'number'},
						{name : 'available_inventory',index : 'available_inventory',width:50,align:'right',sorttype:'number'},
						{name : 'comment',index : 'comment',width:100},
						{name : 'device_type_id',index : 'device_type_id',hidden:true},
						{name : 'brand_id',index : 'brand_id',hidden:true}
            ],
			rowNum : 20,
			toppager : false,
			pager : "#sp_listpager",
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
			viewsortcols : [ true, 'vertical', true ]
		});
	};
	var $add_spare_dialog = $("#add_spare_dialog");
	$add_spare_dialog.dialog({
		position : 'center',
		title : "选择备品品名型号",
		width : 660,
		height : 'auto',
		resizable : false,
		modal : true,
		buttons : {
			"选择":function() {
				var selRow = $("#sp_list").jqGrid("getGridParam", "selrow");
				if(selRow) {
					var rowData=$("#sp_list").getRowData(selRow);
					if (rowData.available_inventory <= 0) {
						errorPop("选择的品名型号的备品现有数量不足。");
					} else {
						if (typeof(showAdd) === "function") showAdd("spare", rowData);
						if (typeof(selectFromSpare) === "function") selectFromSpare(rowData);
						$add_spare_dialog.dialog('close');
					}
				}
			},
			"关闭":function(){
				$add_spare_dialog.dialog('close');
			}
		}
	});
}

var filterSpareList = function(){
	var spare_device_type_name = $("#add_spare_device_type_name").val();
	var spare_model_name = $("#add_spare_model_name").val();
	if (!spare_device_type_name && !spare_model_name) {
		showSparelist(localSpareList);
		return;
	}

	var filtedList = [];
	for (var iSl in localSpareList) {
		var localSpare = localSpareList[iSl];
		if (spare_device_type_name && localSpare["device_type_name"].indexOf(spare_device_type_name) < 0) {
			continue;
		}
		if (spare_model_name && localSpare["model_name"].indexOf(spare_model_name) < 0) {
			continue;
		}
		filtedList.push(localSpare);
	}
	showSparelist(filtedList);
}

</script>

<!-- 备品计入管理 -->
<div id="add_spare_dialog" style="display:none;">
	<table class="condform" style="border:1px solid #aaaaaa;margin-left:2px;">
		<tbody>	
		    <tr>
				<td class="ui-state-default td-title">品名</td>
				<td>
					<input type="text" id="add_spare_device_type_name" name="device_type_name" alt="品名" class="ui-widget-content">
				</td>
				<td class="ui-state-default td-title">型号</td>
				<td>
					<input type="text" id="add_spare_model_name" name="model_name" alt="型号"  class="ui-widget-content">
				</td>
				<td class="ui-state-default td-title" style="text-align:center;">
					<input type="button" class="ui-button" id="add_spare_filterbutton" value="过滤"/>
					<input type="button" class="ui-button" id="add_spare_clearbutton" value="清除"/>
				</td>
			</tr>
		</tbody>
	</table>	
	<table id="sp_list"></table>
	<div id="sp_listpager"></div>
</div>