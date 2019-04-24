var servicePath = "partial_out_storage.do";

$(function () {
	$("#partial_out_storage span.ui-icon").bind("click", function () {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	$("#partial_out_storage .ui-button").button();

	// 结束
	$("#endbutton").click(function(){
		warningConfirm("是否结束出库作业？",function(){
			doEnd();
		},function(){});
	});

	// 输入框触发，配合浏览器
	$("#scanner_inputer").keypress(function () {
		if (this.value.length === 11) {
			startScanner();
		}
	});
	$("#scanner_inputer").keyup(function () {
		if (this.value.length === 11) {
			startScanner();
		}
	});

	outInit();
});

function startScanner () {
	var id = $("#scanner_inputer").val().trim();
	$("#scanner_inputer").val("");
	if (!id) {
		return;
	}
	var data = {
		"material_id" : id
	};

	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doScan',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function (xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
					$("#scanner_inputer").val("");
				} else {
					outInit();
				}
			} catch (e) {
			}
		}
	});
};

function doEnd(){
	var data = {
		"fact_pf_key" : $("#hide_fact_pf_key").val().trim()
	};
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doFinish',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function (xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					outInit();
				}
			}catch(e){}
		}
	});
};


function outInit () {
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=jsinit',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function (xhrobj, textStatus) {
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					$("#scanner_inputer").val("");
					
					// 现品作业信息
					var fact_production_feature = resInfo.unfinish;

					if(fact_production_feature){//进行中作业
						list(resInfo.materialList, 10);

						$("#scanner_container").hide();
						$("#result").show();
						
						var materialPartial = resInfo.materialPartial;
						$("#label_omr_notifi_no").text(materialPartial.omr_notifi_no);
						$("#label_line_name").text(materialPartial.line_name);
						$("#label_level_name").text(materialPartial.level_name);
						$("#label_order_date").text(materialPartial.order_date);
						$("#label_bo_flg_name").text(materialPartial.bo_flg_name);
						
						var bo_contents = materialPartial.bo_contents;
						if (bo_contents) {
							var content = "";
							var jsonObj = JSON.parse(bo_contents);
							for ( var i in jsonObj) {
								if (content) content += ",";
								content += jsonObj[i];
							}
							$("#label_bo_contents").text(content);
						} else {
							$("#label_bo_contents").text("");
						}
						
						setRate(fact_production_feature,resInfo.leagal_overline,resInfo.spent_mins);
						
						enableMenu("outstoragebutton");
						
						$("#hide_fact_pf_key").val(fact_production_feature.fact_pf_key);

					}else{
						list(resInfo.materialList, 20);

						$("#scanner_container").show();
						$("#scanner_inputer").focus();
						$("#result").hide();
						enableMenu("");
						
						clearInterval(oInterval);
						oInterval = null;
						$("#p_rate div:animated").stop();
						p_time = 0;
						leagal_overline = null;
					}
				}
			} catch (e) {
			}
		}
	});
};

function setRate(factProductionFeature,leagalOverline,spent_mins){
	$("#partial_details").show();
	//开始时间
	$("#partial_details td:eq(1)").text(factProductionFeature.action_time);
	leagal_overline = leagalOverline;
	
	var frate = parseInt(spent_mins / leagal_overline * 100);
	if (frate > 99) {
		frate = 99;
	}
	$("#p_rate").html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
	
	p_time = spent_mins;
	
	//作业标准时间
	$("#partial_details td:eq(3)").text(minuteFormat(leagalOverline));
	ctime();
	clearInterval(oInterval);
	oInterval = null;
	oInterval = setInterval(ctime,iInterval);
};

function list (listdata, rowNum) {
	if ($("#gbox_outlist").length > 0) {
		$("#outlist").jqGrid().clearGridData();// 清除
		$("#outlist").jqGrid('setGridParam', {data : listdata, 'rowNum': rowNum})
			.trigger("reloadGrid", [ {current : false} ]);// 刷新列表
		$("#gbox_outlist .ui-jqgrid-bdiv").css({"height" : 23 * rowNum + 1});
	} else {
		$("#outlist").jqGrid({
			data : listdata,// 数据
			height : 23 * rowNum + 1,// rowheight*rowNum+1
			width : 1178,
			rowheight : 23,
			shrinkToFit : true,
			datatype : "local",
			colNames : [ '', '修理单号', '工程', '等级', '订购日期', '零件BO', '零件缺品备注' ],
			colModel : [ {name : 'material_id',index : 'material_id',hidden : true}, 
			             {name : 'omr_notifi_no',index : 'omr_notifi_no',width : 100}, 
			             {name : 'line_name',index : 'line_name',width : 100}, 
			             {name : 'level_name',index : 'level_name',width : 100,align : 'center'},
			             {name : 'order_date',index : 'order_date',width : 100,align : 'center'},
			             {name : 'bo_flg_name',index : 'bo_flg_name',width : 100,align : 'center'}, 
			             {name : 'bo_contents',index : 'bo_contents',width : 400,formatter : function (value, options, rData) {
							var content = "";
							if (value) {
								var jsonObj = JSON.parse(value);
								for ( var i in jsonObj) {
									if (content)
										content += ",";
									content += jsonObj[i];
								}
							}
							return content;
						}}],
			rowNum : rowNum,
			toppager : false,
			pager : "#outlistpager",
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
			ondblClickRow : function (rid, iRow, iCol, e) {
			},
			viewsortcols : [ true, 'vertical', true ],
			gridComplete : function () {
			}
		});
	}
};