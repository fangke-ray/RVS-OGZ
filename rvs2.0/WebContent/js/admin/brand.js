/** 模块名 */
var modelname = "厂商通讯录";

var servicePath="brand.do"

$(function(){
	$("input.ui-button").button();
	$("#search_business_relationship,#edit_business_relationship").select2Buttons();
	/*为每一个匹配的元素的特定事件绑定一个事件处理函数*/
	$("#searcharea span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	$("input.ui-button").button();

	//检索button
	$("#searchbutton").click(function(){
		findit();
	});

	//清除button
	$("#resetbutton").click(function(){
		$("#search_name").val("");
		$("#search_business_relationship").val("").trigger("change");
	});

	//新建---取消button
	$("#cancelbutton, #editarea span.ui-icon").click(function() {
		showList();
	});
	findit();
})

/**
 * 检索处理
 */
var keepSearchData;
var findit = function(data) {
	if (!data) {
		KeepSearchData = {
			"name":$("#search_name").val(),
			"business_relationship":$("#search_business_relationship").val()
		};
	} else {
		keepSearchData = data;
	}
	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath +'?method=search',
		cache : false,
		data : KeepSearchData,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : search_handleComplete
	});
};

/**
 * 检索Ajax通信成功时的处理
 */
var search_handleComplete = function(xhrObj, textStatus) {
	try {
		// 以Object形式读取JSON
		var resInfo = $.parseJSON(xhrObj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages("#searcharea", resInfo.errors);
		} else {
			var listdata = resInfo.devicesTypeForms;
			filed_list(listdata);
		}
	}catch (e) {console.log("81:" + e.message)};
}

/*jqgrid表格*/
function filed_list(finished){
	if ($("#gbox_list").length > 0) {
		$("#list").jqGrid().clearGridData();
		$("#list").jqGrid('setGridParam',{data:finished}).trigger("reloadGrid", [{current:false}]);
	} else {
		$("#list").jqGrid({
			data:finished,
			height: 461,
			width: 992,
			rowheight: 23,
			datatype: "local",
			colNames:['','厂商信息 ID','厂商名称','业务关系','地址','邮箱','联系电话','联系人','删除标记','最后更新人','最后更新时间'],
			colModel:[
				{name:'myac',fixed:true,width:40,sortable:false,resize:false,formatter:'actions',formatoptions:{keys:true, editbutton:false}},
				{name:'brand_id',index:'brand_id', hidden:true},
				{name:'name',index:'name',width : 150},
				{name:'business_relationship',index:'business_relationship',width : 35,formatter : 'select',
					editoptions : {
						value : $("#goBusinessRelationship").val()
					}
				},
				{name:'address',index:'address',width : 150},
				{name:'email',index:'email',width : 60},
				{name:'tel',index:'tel',width : 60},
				{name:'contacts',index:'contacts',width : 60},
				{name:'delete_flg',index:'delete_flg',hidden:true},
				{name:'updated_by',index:'updated_by',width : 35},
				{name:'updated_time',index:'updated_time',width : 50}
			],
			rowNum: 20,
			toppager : false,
			pager : "#list_pager",
			viewrecords : true,
			gridview : true,
			pagerpos : 'right',
			pgbuttons : true,
			pginput : false,
			recordpos : 'left',
			hidegrid : false,
			caption:' 厂商一览 ',
			deselectAfterSort : false,
			ondblClickRow : showEdit,
			viewsortcols : [true,'vertical',true]
		});
		$("#gbox_list .ui-jqgrid-hbox").before(
			'<div class="ui-widget-content" style="padding:4px;">' +
				'<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="addbutton" value="新建厂商通讯录" role="button" aria-disabled="false">' +
			'</div>'
		);
		$("input.ui-button").button();
		$("#addbutton").click(showAdd);
	}
};

/*新建页面*/
var showAdd = function() {
	//默认画面变化
	top.document.title = "新建" + modelname;
	$("#searcharea,#searchform,#listarea,#editform tr:not(:has(input,textarea,select))").hide();
	$("#editform input:text, #editform input:hidden, #editform textarea").val("");
	$("#edit_business_relationship").val("").trigger("change");
	$("#editarea span.areatitle").html("新建" + modelname);
	$("#editarea").show();
	$("#editform input[type!='button']").val("");
	$("#editbutton").val("新建");
	$("#editbutton").enable();
	$(".errorarea-single").removeClass("errorarea-single");

	// 前台Validate验证
	$("#editform").validate({
		rules:{
			name:{
				required:true,
				maxlength:32
			}
		}
	});

	//新建治具点检种类--新建button
	$("#editbutton").unbind("click");
	$("#editbutton").click(function() {
		if ($("#editform").valid()) {
			$("#editbutton").disable();

			warningConfirm("确认要新建该厂商["+$("#edit_name").val()+"]记录吗？", function() {
				var data={
					"name":$("#edit_name").val(),
					"business_relationship":$("#edit_business_relationship").val(),
					"address":$("#edit_address").val(),
					"email":$("#edit_email").val(),
					"tel":$("#edit_tel").val(),
					"contacts":$("#edit_contacts").val()
				}
				// Ajax提交
				$.ajax({
					beforeSend : ajaxRequestType,
					async : true,
					url : servicePath + '?method=doinsert',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : update_handleComplete
				})
			}, function() {
				$("#editbutton").enable();
			}, "新建确认");
		};
	});
}

var insert_handleComplete = function(xhrObj, textStatus) {
	try {
		// 以Object形式读取JSON
		var resInfo = $.parseJSON(xhrObj.responseText);	
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages("#editarea", resInfo.errors);
		} else {
			infoPop("新建已经完成。");
			// 重新查询
			findit();
			// 切回一览画面
			showList();
		}
	}catch (e) {console.log("204:" + e.message)};
}

/*编辑页面*/
var showEdit = function() {
	$("#searcharea,#searchform,#listarea,#editform tr:not(:has(input,textarea,select))").show();
	$("#editbutton").val("修改");
	$("#editbutton").enable();
	$(".errorarea-single").removeClass("errorarea-single");
	$("#searcharea,#searchform,#listarea").hide();
	$("#editarea").show();

	var row = $("#list").jqGrid("getGridParam","selrow");//得到选中的行ID
	var rowData = $("#list").getRowData(row);

	$("#hidden_brand_id").val(rowData.brand_id);

	$("#editarea span.areatitle").html("修改" + modelname);
	// 默认画面变化 s
	top.document.title = modelname + "修改";

	$("#edit_name").val(rowData.name);
	$("#edit_specialized").val(rowData.specialized).trigger("change");
	// TODO edit_hazardous_cautions
	$("#label_updated_by").text(rowData.updated_by);
	$("#label_updated_time").text(rowData.updated_time);

	// 前台Validate验证
	$("#editform").validate({
		rules:{
			name:{
				required:true,
				maxlength:32
			}
		}
	});

	// 切换按钮效果
	$("#editbutton").unbind("click");
	$("#editbutton").click(function() {
		if ($("#editform").valid()) {
			$("#editbutton").disable();

			warningConfirm("确认要修改该厂商["+$("#edit_name").val()+"]记录吗？", 
				function() {
					var data={
					"name":$("#edit_name").val(),
					"specialized":$("#edit_specialized").val(),
					"brand_id":$("#hidden_brand_id").val()
				}
				// Ajax提交
				$.ajax({
					beforeSend : ajaxRequestType,
					async : true,
					url : servicePath + '?method=doupdate',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : update_handleComplete
				})
			}, function() {
				$("#editbutton").enable();
			}, "确定修改");
		};
	});
}

/**
 * 检索Ajax通信成功时的处理
 */
var update_handleComplete = function(xhrObj, textStatus) {
	try {
		// 以Object形式读取JSON
		var resInfo = $.parseJSON(xhrObj.responseText);	
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages("#searcharea", resInfo.errors);
		} else {
			infoPop("修改已经完成。");
			findit();
			//回到一览画面
			showList();
		}
	}catch (e) {console.log("290:" + e.message)};
}


/*删除*/
var showDelete = function(rid) {
	var rowData = $("#list").getRowData(rid);
	var data = {"brand_id" : rowData.brand_id}

	warningConfirm("删除不能恢复。确认要删除["+encodeText(rowData.name)+"]的记录吗？",
		function() {
			// Ajax提交
			$.ajax({
				beforeSend : ajaxRequestType,
				async : false,
				url : servicePath + '?method=dodelete',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : update_handleComplete
			});
		}, null, "删除确认"
	);
};

var delete_handleComplete = function(xhrObj, textStatus) {
	try {
		// 以Object形式读取JSON
		var resInfo = $.parseJSON(xhrObj.responseText);	
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages("#searcharea", resInfo.errors);
		} else {
			infoPop("删除已经完成。");
			findit();
			//回到一览画面
			showList();
		}
	}catch (e) {console.log("331:" + e.message)};
}
/*初始页面显示*/
var showList = function() {
	top.document.title = modelname + "一览";
	$("#searcharea,#searchform").show();
	$("#listarea").show();
	$("#editarea").hide();
}