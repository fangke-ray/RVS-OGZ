/** 模块名 */
var modelname = "作业步骤计次";

var servicePath="procedureStepCount.do"

$(function(){
	$("input.ui-button, a.ui-button").button();
	$("#cond_px, #input_px").select2Buttons();
	/*为每一个匹配的元素的特定事件绑定一个事件处理函数*/
	$("#searcharea span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	//检索button
	$("#searchbutton").click(function(){
		$("#cond_name").data("post", $("#cond_name").val());
		$("#cond_model_id").data("post", $("#cond_model_id").val());
		$("#cond_position_id").data("post", $("#cond_position_id").val());
		$("#cond_px").data("post", $("#cond_px").val());
		findit();
	});

	//清除button
	$("#resetbutton").click(function(){
		$("#cond_name").data("post", "").val("");
		$("#cond_model_name").val("");
		$("#cond_model_id").data("post", "").val("");
		$("#cond_process_code").val("");
		$("#cond_position_id").data("post", "").val("");
		$("#cond_px").data("post", "").val("").trigger("change");
	});

	//新建---取消button
	$("#cancelbutton, #editarea span.ui-icon").click(function() {
		showList();
	});

	setReferChooser($("#cond_position_id"), $("#position_refer"));
	setReferChooser($("#cond_model_id"), $("#model_refer"));
	setReferChooser($("#input_position_id"), $("#position_refer"));
	setReferChooser($("#input_model_id"), $("#model_refer"), null, function(tr){
		if (tr == null) return;

		$("#input_model_name").val("");

		var referId = $(tr).find(".referId").text().trim();
		if ($("#grid_edit_model tbody .referId:contains('"+referId+"')").length > 0) {
			return;
		}

		var htmlContent = "";
		htmlContent += '<tr>'
						+ $(tr).html()
						+ '<td><input type="number" class="step_times"></td>'
						+ '<td><span class="ui-icon ui-icon-trash"></span></td>'
						+ '</tr>';
		$("#grid_edit_model tbody").append(htmlContent);
	});

	$("#grid_edit_model").on("click", ".ui-icon-trash", function(){
	    $(this).parent().parent().remove();
	});

	findit();
})

/** 
 * 检索处理
 */
var findit = function() {
	// 读取已记录检索条件提交给后台
	var data = {
		"name" : $("#cond_name").data("post"),
		"relation_id" : $("#cond_model_id").data("post"),
		"position_id" : $("#cond_position_id").data("post"),
		"px" : $("#cond_px").data("post")
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

/**
 * 切到一览画面
 */
var showList = function() {
	top.document.title = modelname + "一览";
	$("#searcharea").show();
	$("#listarea").show();
	$("#editarea").hide();
	$("#detailarea").hide();
};

/**
 * 切到新建画面
 */
var showAdd = function() {
	// 默认画面变化 s
	top.document.title = "新建" + modelname;
	$("#searcharea").hide();
	$("#listarea").hide();
	$("#editarea span.areatitle").eq(0).html("新建" + modelname);
	$("#editarea").show();
	$("#editform tr:not(:has(input,textarea,select))").hide();
	$("#editform input[type!='button'][type!='radio'], #editform textarea").val("");
	$("#editform select").val("").trigger("change");
	$("#editform label").not("[for]").html("");
	$("#editbutton").val("新建");
	$("#editbutton").enable();
	$(".errorarea-single").removeClass("errorarea-single");
	$("#grid_edit_model tbody tr").remove();
	// 默认画面变化 e

	// 前台Validate设定
	$("#editform").validate({
		rules : {
			position_id : {
				required : true
			},
			name : {
				required : true
			}
		}
	});

	// 切换按钮效果
	$("#editbutton").unbind("click");
	$("#editbutton").click(function() {
		// 前台Validate
		if ($("#editform").valid()) {
			// 通过Validate,切到新建确认画面
			$("#editbutton").disable(
);
			// 新建画面输入项提交给后台
			var data = {
				"name" : $("#input_name").val(),
				"position_id" : $("#input_position_id").val(),
				"px" : $("#input_px").val()
			}

			var lostStepTimes = "";
			$("#grid_edit_model tbody tr").each(function(i,item){
				data["models.relation_id[" + i + "]"] = $(item).find(".referId").html();
				data["models.step_times[" + i + "]"] = $(item).find("input[type='number']").val();
				if (!data["models.step_times[" + i + "]"]) {
					lostStepTimes += "请输入型号" + $(item).find("td:eq(1)").text() + "的关联计数。<br>"; 
				}
			});
			
			if (lostStepTimes) {
				$("#editbutton").enable();
				errorPop(lostStepTimes);
				return;
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
			});
		};
	});
};

var search_handleComplete = function(xhrobj, textStatus) {
	var resInfo = $.parseJSON(xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages("#searcharea", resInfo.errors);
	} else {
		// 标题修改
		top.document.title = modelname + "一览";

		// 读取一览
		showGrid(resInfo.list);
	}
}

var showGrid = function(listdata) {
	// 读取一览
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
			colNames : ['', '', '作业步骤名称', '应用工位', '应用工位代码', '分线', '客户端地址', '最后更新人', '最后更新时间'],
			colModel : [
			{name:'myac', width:48, fixed:true, sortable:false, resize:false, formatter:'actions', formatoptions:{keys:true, editbutton:false}},
			{
				name : 'procedure_step_count_id',
				index : 'procedure_step_count_id',
				hidden : true
			}, {
				name : 'name',
				index : 'name',
				width : 110
			}, {
				name : 'position_name',
				index : 'position_name',
				width : 60
			}, {
				name : 'process_code',
				index : 'process_code',
				width : 40, align:'center'
			}, {
				name : 'px',
				index : 'px',
				width : 20, align:'center',
				formatter:'select',
				editoptions:{value:"0:;1:A;2:B"}
			}, {
				name : 'client_address',
				index : 'client_address',
				width : 100
			}, {
				name : 'updated_by',
				index : 'updated_by',
				width : 40
			}, {
				name : 'updated_time',
				index : 'updated_time',
				width : 60
			}],
			toppager : false,
			rowNum : 20,
			pager : "#listpager",
			viewrecords : true,
			caption : modelname + "一览",
			ondblClickRow : showEdit,
			gridview : true, // Speed up
			pagerpos : 'right',
			pgbuttons : true,
			pginput : false,
			recordpos : 'left',
			viewsortcols : [true, 'vertical', true]
		});
		$(".ui-jqgrid-hbox").before('<div class="ui-widget-content" style="padding:4px;">' +
				'<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="addbutton" value="新建'+ modelname +'" role="button" aria-disabled="false">' +
			'</div>');
		$("#addbutton").button();
		// 追加处理
		$("#addbutton").click(showAdd);
	}
}

/**
 * 切到修改画面
 */
var showEdit = function(rid) {
	// 读取修改行
	var rowData = $("#list").getRowData(rid);
	var data = {
		"procedure_step_count_id" : rowData.procedure_step_count_id
	}

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
		complete : showEdit_handleComplete
	});
};

/**
 * 读取修改详细信息Ajax通信成功时的处理
 */
var showEdit_handleComplete = function(xhrobj, textStatus) {
	// 以Object形式读取JSON
	var resInfo = $.parseJSON(xhrobj.responseText);

	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages(null, resInfo.errors);
	} else {

		// 默认画面变化 s
		top.document.title = modelname + "修改";
		$("#searcharea").hide();
		$("#listarea").hide();
		$("#editarea span.areatitle").eq(0).html(modelname + "修改");
		$("#editarea").show();
		$("#editform tr").show();
		$("#editbutton").val("修改");
		$("#editbutton").enable();
		$(".errorarea-single").removeClass("errorarea-single");
		// 默认画面变化 e

		var detailForm = resInfo.detailForm;
		// 详细数据
		$("#edit_procedure_step_count_id").val(detailForm.procedure_step_count_id);
		$("#input_name").val(detailForm.name);
		$("#input_position_name").val(detailForm.position_name);
		$("#input_position_id").val(detailForm.position_id);
		$("#input_px").val(detailForm.px).trigger("change");
		$("#label_edit_updated_by").text(detailForm.updated_by);
		$("#label_edit_updated_time").text(detailForm.updated_time);

		// 维修对象型号
		var modelBeans = resInfo.modelList;
		$("#grid_edit_model tbody tr").remove();
		var htmlContent = "";
		for (var jj in modelBeans) {
			htmlContent += '<tr>'
						+ '<td class="referId" style="display:none">' + modelBeans[jj].relation_id + '</td>'
						+ '<td><nobr>' + modelBeans[jj].model_name + '</nobr></td>'
						+ '<td>' + modelBeans[jj].category_name + '</td>'
						+ '<td><input type="number" value="' + modelBeans[jj].step_times + '"></td>'
						+ '<td><span class="ui-icon ui-icon-trash"></span></td>'
						+ '</tr>';
		}
		$("#grid_edit_model tbody").append(htmlContent);

		// 图片显示
		$("#editform").validate({
			rules : {
				position_name : {
					required : true
				}
			}
		});

		// 切换按钮效果
		$("#editbutton").unbind("click");
		$("#editbutton").click(function() {
			// 前台Validate
			if ($("#editform").valid()) {
				// 通过Validate,切到修改确认画面
				$("#editbutton").disable();

				var data = {
					"procedure_step_count_id" : $("#edit_procedure_step_count_id").val(),
					"name" : $("#input_name").val(),
					"position_id" : $("#input_position_id").val(),
					"px" : $("#input_px").val()
				}

				var lostStepTimes = "";
				$("#grid_edit_model tbody tr").each(function(i,item){
					data["models.relation_id[" + i + "]"] = $(item).find(".referId").html();
					data["models.step_times[" + i + "]"] = $(item).find("input[type='number']").val();
					if (!data["models.step_times[" + i + "]"]) {
						lostStepTimes += "请输入型号" + $(item).find("td:eq(1)").text() + "的关联计数。<br>"; 
					}
				});
				
				if (lostStepTimes) {
					$("#editbutton").enable();
					errorPop(lostStepTimes);
					return;
				}

				warningConfirm("确认要修改记录吗？", 
					function() {

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
					}, 
					function(){
						$("#editbutton").enable();
					}, "修改确认"
				);
			};
		});
	}
}

/**
 * 更新完成Ajax通信成功时的处理
 */
var update_handleComplete = function(xhrobj, textStatus) {
	var resInfo = $.parseJSON(xhrobj.responseText);
	if (resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages("#editarea", resInfo.errors);
		// 编辑确认按钮重新有效
		$("#editbutton").enable();
	} else {
		// 重新查询
		findit();
		// 切回一览画面
		showList();
	}
}

var showDelete = function(rid){
	// 读取修改行
	var rowData = $("#list").getRowData(rid);
	var data = {
		"procedure_step_count_id" : rowData.procedure_step_count_id
	}

	warningConfirm("确认要删除此条作业步骤计次[" + rowData.name  +"]吗？", 
		function() {

			// Ajax提交
			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url : servicePath + '?method=dodelete',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : update_handleComplete
			});
		}, 
		function(){
			$("#editbutton").enable();
		}, "删除确认"
	);
}