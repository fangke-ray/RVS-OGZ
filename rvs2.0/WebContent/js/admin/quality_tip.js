/** 模块名 */
var modelname = "质量提示";
/** 一览数据对象 */
var listdata = {};
/** 服务器处理路径 */
var servicePath = "quality_tip.do";

/**
 * 页面加载处理
 */
$(function() {
	// 适用jquery按钮
	$("input.ui-button").button();

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

	setReferChooser($("#cond_position_id"), $("#position_refer"));
	setReferChooser($("#input_position_id"), $("#position_refer"));
	setReferChooser($("#cond_model_id"), $("#model_refer"));
	setReferChooser($("#input_model_id"), $("#model_refer"), null, function(tr){
		if (tr == null) return;
		var referId = $(tr).find(".referId").text().trim();
		if ($("#grid_edit_model tbody .referId:contains('"+referId+"')").length > 0) {
			return;
		}

		var htmlContent = "";
		htmlContent += '<tr>'
						+ $(tr).html()
						+ '<td><span class="ui-icon ui-icon-trash"></span></td>'
						+ '</tr>';
		$("#grid_edit_model tbody").append(htmlContent);
		$("#grid_edit_model .ui-icon-trash").unbind("click");
		$("#grid_edit_model .ui-icon-trash").click(function(){
		    $(this).parent().parent().remove();
		});
	});

	$("#cond_category_id").select2Buttons();

	$("#cancelbutton, #editarea span.ui-icon").click(function() {
		showList();
	});

	// 初始状态隐藏编辑
	$("#editarea").hide();

	// 检索处理
	$("#searchbutton").click(function() {
		findit();
	});

	// 清空检索条件
	$("#resetbutton").click(function() {		
		resetCondition();
	});

	$("#grid_edit_category tr:not(:first)").click(function(){
		$(this).toggleClass("ui-state-active");
	});

	$("#photo_upload_button").click(function(){
		uploadPhoto();
	});

	resetCondition();
	findit();
});

var resetCondition = function() {		
	$("#cond_title").val("");
	$("#cond_position_name").val("");
	$("#cond_position_id").val("");
	$("#cond_category_id").val("").trigger("change");
	$("#cond_model_name").val("");
	$("#cond_model_id").val("");

	$("#cond_title").data("post", "");
	$("#cond_position_name").data("post", "");
	$("#cond_position_id").data("post", "");
	$("#cond_category_id").data("post", "");
	$("#cond_model_name").data("post", "");
	$("#cond_model_id").data("post", "");	
};

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
					colNames : ['', '', '标题', '工位名称', '绑定对象', '最后更新人', '最后更新时间'],
					colModel : [
					{name:'myac', width:48, fixed:true, sortable:false, resize:false, formatter:'actions', formatoptions:{keys:true, editbutton:false}},
					{
						name : 'quality_tip_id',
						index : 'quality_tip_id',
						hidden : true
					}, {
						name : 'title',
						index : 'title',
						width : 50
					}, {
						name : 'position_name',
						index : 'position_name',
						width : 60
					}, {
						name : 'bind_name',
						index : 'bind_name',
						width : 100
					}, {
						name : 'updated_by',
						index : 'updated_by',
						width : 80
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
					gridview : true, // Speed up
					pagerpos : 'right',
					pgbuttons : true,
					pginput : false,
					recordpos : 'left',
					viewsortcols : [true, 'vertical', true]
				});
				$(".ui-jqgrid-hbox").before('<div class="ui-widget-content" style="padding:4px;">' +
						'<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="addbutton" value="新建'+'" role="button" aria-disabled="false">' +
					'</div>');
				$("#addbutton").button();
				// 追加处理
				$("#addbutton").click(showAdd);
			}

		}
	} catch (e) {
		console.log("160 name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

/**
 * 更新完成Ajax通信成功时的处理
 */
var update_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages("#editarea", resInfo.errors);
			// 编辑确认按钮重新有效
			$("#editbutton").enable();
		} else {
			resetCondition();
			// 重新查询
			findit();
			// 切回一览画面
			showList();
		}
	} catch (e) {
		console.log("186 name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
}
	
/**
 * 读取修改详细信息Ajax通信成功时的处理
 */
var showedit_handleComplete = function(xhrobj, textStatus) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
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
			$("#photo_edit_area").show();
			$("#editform tr").show();
			$("#editbutton").val("修改");
			$("#editbutton").enable();
			$(".errorarea-single").removeClass("errorarea-single");
			// 默认画面变化 e

			var detailForm = resInfo.detailForm;
			// 详细数据
			$("#edit_quality_tip_id").val(detailForm.quality_tip_id);
			$("#input_title").val(detailForm.title);
			$("#input_position_name").val(detailForm.position_name);
			$("#input_position_id").val(detailForm.position_id);
			$("#label_edit_updated_by").text(detailForm.updated_by);
			$("#label_edit_updated_time").text(detailForm.updated_time);

			// 维修对象机种
			var categorys = detailForm.categorys;
			$("#grid_edit_category").find("tr").removeClass("ui-state-active");
			for (var ii in categorys) {
				$("#grid_edit_category").find("tr:has(.referId:contains('"+categorys[ii]+"'))")
					.addClass("ui-state-active");
			}
			// 维修对象型号
			var modelBeans = detailForm.modelBeans;
			$("#grid_edit_model tbody tr").remove();
			var htmlContent = "";
			for (var jj in modelBeans) {
				htmlContent += '<tr>'
							+ '<td class="referId" style="display:none">' + modelBeans[jj].id + '</td>'
							+ '<td><nobr>' + modelBeans[jj].name + '</nobr></td>'
							+ '<td>' + modelBeans[jj].category_name + '</td>'
							+ '<td><span class="ui-icon ui-icon-trash"></span></td>'
							+ '</tr>';
			}
			$("#grid_edit_model tbody").append(htmlContent);

			$("#grid_edit_model .ui-icon-trash").unbind("click");
			$("#grid_edit_model .ui-icon-trash").click(function(){
			    $(this).parent().parent().remove();
			});

			// 图片显示
			$("#photo_file").val("");
			$("#editted_image").attr("src", "http://" + document.location.hostname + "/photos/quality_tip/" + detailForm.quality_tip_id + "?_s=" + new Date().getTime());

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

					$("#confirmmessage").text("确认要修改记录吗？");
				 	$("#confirmmessage").dialog({
						resizable : false,
						modal : true,
						title : "修改确认",
						close: function() {
							$("#editbutton").enable();
						},
						buttons : {
							"确认" : function() {
								var data = {
									"quality_tip_id" : $("#edit_quality_tip_id").val(),
									"title" : $("#input_title").val(),
									"position_id" : $("#input_position_id").val(),
									"photo_file_name" : $("#photo_file_name").val()
								}

								$("#grid_edit_category tr.ui-state-active").each(function(i,item){
									data["categorys[" + i + "]"] = $(item).find(".referId").html();
								});
					
								$("#grid_edit_model tbody tr").each(function(i,item){
									data["models[" + i + "]"] = $(item).find(".referId").html();
								});

								$(this).dialog("close");

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
							"取消" : function() {
								$(this).dialog("close");
							}
						}
					});
				};
			});
		}
	} catch (e) {
		console.log("298 name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
}

/** 
 * 检索处理
 */
var findit = function() {
	$("#cond_title").data("post", $("#cond_title").val());
	$("#cond_position_name").data("post", $("#cond_position_name").val());
	$("#cond_position_id").data("post", $("#cond_position_id").val());
	$("#cond_category_id").data("post", $("#cond_category_id").val());
	$("#cond_model_name").data("post", $("#cond_model_name").val());
	$("#cond_model_id").data("post", $("#cond_model_id").val());

	// 读取已记录检索条件提交给后台
	var data = {
		"title" : $("#cond_title").data("post"),
		"position_id" : $("#cond_position_id").data("post"),
		"category_id" : $("#cond_category_id").data("post"),
		"model_id" : $("#cond_model_id").data("post")
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
};

/**
 * 切到新建画面
 */
var showAdd = function() {
	// 默认画面变化 s
	top.document.title = modelname + "新建";
	$("#searcharea").hide();
	$("#listarea").hide();
	$("#editarea span.areatitle").eq(0).html(modelname + "新建");
	$("#editarea").show();
	$("#photo_edit_area").hide();
	$("#photo_file").val("");
	$("#editted_image").attr("src", "");
	$("#editarea tr:has(#label_edit_updated_by)").hide();
	$("#editarea tr:has(#label_edit_updated_time)").hide();
	$("#editform input[type!='button'], #editform select").val("");
	$("#editform label").html("");
	$("#editbutton").val("新建");
	$("#editbutton").enable();
	$("#grid_edit_category tr").removeClass("ui-state-active");
	$("#grid_edit_model tbody tr").remove();
	$(".errorarea-single").removeClass("errorarea-single");
	// 默认画面变化 e

	// 前台Validate设定
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
			// 通过Validate,切到新建确认画面
			$("#editbutton").disable();
			// 新建画面输入项提交给后台
			var data = {
				"title" : $("#input_title").val(),
				"position_id" : $("#input_position_id").val(),
				"photo_file_name" : $("#photo_file_name").val()
			}

			$("#grid_edit_category tr.ui-state-active").each(function(i,item){
				data["categorys[" + i + "]"] = $(item).find(".referId").html();
			});

			$("#grid_edit_model tbody tr").each(function(i,item){
				data["models[" + i + "]"] = $(item).find(".referId").html();
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
	});
};

/**
 * 切到修改画面
 */
var showEdit = function(rid) {
	// 读取修改行
	var rowData = $("#list").getRowData(rid);
	var data = {
		"quality_tip_id" : rowData.quality_tip_id
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
		complete : showedit_handleComplete
	});
};

/**
 * 切到删除画面
 */
var showDelete = function(rid) {
	// 读取删除行
	var rowData = $("#list").getRowData(rid);
	var data = {
		"quality_tip_id" : rowData.quality_tip_id
	}

	warningConfirm("删除不能恢复。确认要删除["+encodeText(rowData.title)+"]的记录吗？",
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

var uploadPhoto = function() {
    $.ajaxFileUpload({
        url : servicePath + "?method=sourceImage", // 需要链接到服务器地址
        secureuri : false,
        data:null,
        fileElementId : 'photo_file', // 文件选择框的id属性
        dataType : 'json', // 服务器返回的格式
		success : function(responseText, textStatus) {
			eval('resInfo =' + responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				infoPop("上传完毕！");
				$("#photo_file_name").val(resInfo.photo_file_name);
			}
		}
     });
};
