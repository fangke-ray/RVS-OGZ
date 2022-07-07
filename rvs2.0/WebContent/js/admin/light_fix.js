/** 模块名 */
var modelname = "中小修理标准编制";
/** 一览数据对象 */
var listdata = {};
/** 服务器处理路径 */
var servicePath = "light_fix.do";

/**
 * 页面加载处理
 */
$(function() {
	// 适用jquery按钮
	$("input.ui-button").button();
	$("#cond_category_id, #add_correlated_pat_id, #edit_correlated_pat_id").select2Buttons();

	setReferChooser($("#cond_position_id"), $("#position_refer"));

	// 右上图标效果
	$("a.areacloser").hover(function() { $(this).addClass("ui-state-hover");},
		function() {$(this).removeClass("ui-state-hover");});
	$("#searcharea span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	findit();
	showList();

	// 检索处理
	$("#searchbutton").click(findit);
	$("#addbutton").click(doAdd);
	$("#editbutton").click(doEdit);
	$("#addcancelbutton, #editcancelbutton, #editarea span.ui-icon").click(showList);

	// 清空检索条件
	$("#resetbutton").click(function() {
		$("#cond_light_fix_id").val("");
		$("#cond_description").val("");
		$("#cond_activity_code").val("");
		$("#cond_rank").val("");
		$("#cond_category_id").val("").trigger("change");
		$("#cond_position_name").val("");
		$("#cond_position_id").val("");
	});

	// 编辑机种,工位
	$("#grid_add_categories tr:not(:first)").click(function(){
		$(this).toggleClass("ui-state-active");
	});
	$("#grid_add_positions tr:not(:first)").click(function(){
		$(this).toggleClass("ui-state-active");
	});
	$("#grid_edit_categories tr:not(:first)").click(function(){
		$(this).toggleClass("ui-state-active");
	});
	$("#grid_edit_positions tr:not(:first)").click(function(){
		$(this).toggleClass("ui-state-active");
	});

	$("#add_correlated_level, #edit_correlated_level").click(function(){
		switch (this.value) {
		case "1" : this.value = "2"; break;	
		case "2" : this.value = "3"; break;
		case "3" : this.value = "1"; break;
		}
	}).on("mouseout", function(){
		$(this).addClass("ui-state-active").removeClass("ui-state-focus");
	})
});

/**
 * 切到一览画面
 */
var showList = function() {
	top.document.title = modelname + "一览";
	$("#searcharea").show();
	$("#listarea").show();
	$("#addarea").hide();
	$("#editarea").hide();
};

/** 
 * 检索处理
 */
var findit = function() {
	// 读取已记录检索条件提交给后台
	var data = {
		"light_fix_id" : $("#cond_light_fix_id").val(),
		"description" : $("#cond_description").val(),
		"activity_code" : $("#cond_activity_code").val(),
		"rank" : $("#cond_rank").val(),
		"category_id" : $("#cond_category_id").val(),
		"position_id" : $("#cond_position_id").val()
	};

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

/**
 * 检索Ajax通信成功时的处理
 */
var search_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;

	// 以Object形式读取JSON
	resInfo = $.parseJSON(xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages("#searcharea", resInfo.errors);
	} else {
		// 标题修改
		top.document.title = modelname + "一览";

		// 读取一览
		listdata = resInfo.list;
		if ($("#gbox_list").length > 0) {
			// jqGrid已构建的情况下,重载数据并刷新
			$("#list").jqGrid().clearGridData();
			$("#list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [{current : false}]);
		} else {
			// 构建jqGrid对象
			$("#list").jqGrid({
				data : listdata,
				height : 461,
				width : gridWidthMiddleRight,
				rowheight : 23,
				datatype : "local",
				colNames : ['', '', '修理代码', '名称', '修理等级', '机种', '最后更新人', '最后更新时间'],
				colModel : [
				{name:'myac', width:40, fixed:true, sortable:false, resize:false, formatter:'actions', formatoptions:{keys:true, editbutton:false}},
				{	name : 'light_fix_id',
					index : 'light_fix_id',
					hidden : true
				}, {
					name : 'activity_code',
					index : 'activity_code',
					width : 40
				}, {
					name : 'description',
					index : 'description',
					width : 100
				}, {
					name : 'rank',
					index : 'rank',
					width : 40
				}, {
					name : 'categories',
					index : 'categories',
					width : 150
				}, {
					name : 'updated_by',
					index : 'updated_by',
					hidden : true
				}, {
					name : 'updated_time',
					index : 'updated_time',
					width : 80
				}],
				toppager : false,
				rowNum : 20,
				pager : "#listpager",
				viewrecords : true,
				caption : modelname + "一览",
				ondblClickRow : showEdit,
				gridview : true,
				pagerpos : 'right',
				pgbuttons : true,
				pginput : false,
				recordpos : 'left',
				viewsortcols : [true, 'vertical', true]
			});
			$(".ui-jqgrid-hbox").before('<div class="ui-widget-content" style="padding:4px;">' +
					'<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="addbutton" value="新建'+ '" role="button" aria-disabled="false">' +
				'</div>');
			$("#addbutton").button();
			// 追加处理
			$("#addbutton").click(showAdd);
		}

	}

};

var showAdd = function() {
	top.document.title = "新建" + modelname;
	$("#searcharea").hide();
	$("#listarea").hide();
	$("#addarea span.areatitle").html("新建" + modelname);
	$("#addarea").show();
	$("#addform input[type='text']").val("");
	$("#add_correlated_pat_id").val("").trigger("change");
	$("#add_correlated_level").val("1");

	$(".errorarea-single").removeClass("errorarea-single");

	$("#grid_add_categories").find("tr").removeClass("ui-state-active");
	$("#grid_add_positions").find("tr").removeClass("ui-state-active");
};

var doAdd = function() {
	// 前台Validate设定
	$("#addform").validate({
		rules : {
			add_activity_code : {
				required : true
			},
			add_description : {
				required : true
			}
		}
	});

	// 前台Validate
	if ($("#addform").valid()) {
		// 新建画面输入项提交给后台
		var data = {
			"activity_code" : $("#add_activity_code").val(),
			"description" : $("#add_description").val(),
			"rank" : $("#add_rank").val(),
			"correlated_pat_id" : $("#add_correlated_pat_id").val()
		};

		if (data.correlated_pat_id) {
			data.correlated_level = $("#add_correlated_level").val();
		}

		$("#grid_add_categories tr.ui-state-active").each(function(i,item){
			data["category_list[" + i + "]"] = $(item).find(".referId").html();
		});

		$("#grid_add_positions tr.ui-state-active").each(function(i,item){
			data["position_list[" + i + "]"] = $(item).find(".referId").html();
		});

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
		});
	};
};

var update_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;

	// 以Object形式读取JSON
	resInfo = $.parseJSON(xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages(null, resInfo.errors);
	} else {
		// 重新查询
		findit();
		// 切回一览画面
		showList();
	}
};

var showEdit = function() {
	// 读取修改行
	var row = $("#list").jqGrid("getGridParam", "selrow");
	var rowData = $("#list").getRowData(row);
	var data = {
		"light_fix_id" : rowData.light_fix_id
	};

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
		complete : function(xhrobj, textStatus) {
			var resInfo = null;
			// 以Object形式读取JSON
			resInfo = $.parseJSON(xhrobj.responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				top.document.title = "修改" + modelname;
				$("#searcharea").hide();
				$("#listarea").hide();
				$("#editarea span.areatitle").html("修改" + modelname);
				$("#editarea").show();
				$(".errorarea-single").removeClass("errorarea-single");

				// 详细数据
				var detail = resInfo.detail;
				$("#edit_light_fix_id").val(detail.light_fix_id);
				$("#edit_activity_code").val(detail.activity_code);
				$("#edit_description").val(detail.description);
				$("#edit_rank").val(detail.rank);
				$("#edit_correlated_pat_id").val(detail.correlated_pat_id).trigger("change");
				$("#edit_correlated_level").val(detail.correlated_level || "1");

				$("#label_edit_updated_by").text(detail.updated_by);
				$("#label_edit_updated_time").text(detail.updated_time);

				$("#grid_edit_categories").find("tr").removeClass("ui-state-active");
				var category_list = detail.category_list;
				for (var ii in category_list) {
					$("#grid_edit_categories").find("tr:has(.referId:contains('" + category_list[ii] + "'))")
						.addClass("ui-state-active");
				}

				$("#grid_edit_positions").find("tr").removeClass("ui-state-active");
				var position_list = detail.position_list;
				for (var ii in position_list) {
					$("#grid_edit_positions").find("tr:has(.referId:contains('" + position_list[ii] + "'))")
						.addClass("ui-state-active");
				}
			}
		}
	});
};

var doEdit = function() {
	// 前台Validate设定
	$("#editform").validate({
		rules : {
			edit_activity_code : {
				required : true
			},
			edit_description : {
				required : true
			}
		}
	});

	warningConfirm("确认要修改记录吗？", 
		function() {
			// 前台Validate
			if ($("#editform").valid()) {
				// 新建画面输入项提交给后台
				var data = {
					"light_fix_id" : $("#edit_light_fix_id").val(),
					"activity_code" : $("#edit_activity_code").val(),
					"description" : $("#edit_description").val(),
					"rank" : $("#edit_rank").val(),
					"correlated_pat_id" : $("#edit_correlated_pat_id").val()
				};

				if (data.correlated_pat_id) {
					data.correlated_level = $("#edit_correlated_level").val();
				}

				$("#grid_edit_categories tr.ui-state-active").each(function(i,item){
					data["category_list[" + i + "]"] = $(item).find(".referId").html();
				});
		
				$("#grid_edit_positions tr.ui-state-active").each(function(i,item){
					data["position_list[" + i + "]"] = $(item).find(".referId").html();
				});
		
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
				});
			};
		}, null, "修改确认");
};

/**
 * 切到删除画面
 */
var showDelete = function(rid) {
	// 读取删除行
	var rowData = $("#list").getRowData(rid);
	var data = {
		"light_fix_id" : rowData.light_fix_id
	};

	warningConfirm("删除不能恢复。确认要删除["+encodeText(rowData.activity_code)+"]的记录吗？", 
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
	}, null, "删除确认");
};
