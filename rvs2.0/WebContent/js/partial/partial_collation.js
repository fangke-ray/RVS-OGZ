var servicePath = "partial_collation.do";

// 作业完毕需要清除
var searchlist = [];
var allPartialMap = new Map();
var updateData = new Map();

$(function(){
	$("#partial_collation span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});
	
	$("#partial_collation .ui-button").button();
	
	// 开始
	$("#startbutton").click(doStart);

	$("#breakbutton").click(doBreak)

	$("#endbutton").click(function(){
		warningConfirm("是否完成核对？",function(){
			doEnd();
		},function(){});
	});
	
	// 输入框触发，配合浏览器
	$("#scanner_inputer").keypress(function(e){
		if(e.keyCode == 13){
			startScanner();
		}
	});
//	$("#scanner_inputer").keyup(function(e){
//		if(e.keyCode == 13){
//			startScanner();
//		}
//	});
	
	collationInit();
});

function startScanner(){
	var data = {
		"code" : $("#scanner_inputer").val().trim()
	};
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=checkScanner',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
					$("#scanner_inputer").val("");
				} else {
					$("#scanner_inputer").val("").focus();
					var warehouse_no = $("#hide_warehouse_no").val(),
					    partialForm = resInfo.partialForm;
					    code = partialForm.code;
					    partialBussinessStandardForm = resInfo.partialBussinessStandardForm,
					    onShelf = partialBussinessStandardForm.on_shelf;
					
					var type = "";
					if(onShelf < 0){
						type = "20";
					}else{
						type = "21";
					}
					
					if(allPartialMap.has(data.code)){
						var production_type = $("#hide_production_type").val();
						if(production_type != type){
							var errorData = `零件编号[${partialForm.code}]不适用于当前作业内容！`;
							errorPop(errorData);
						}else{
							// 更新一览数据
							updateList(partialForm.partial_id);
						}
					}else{
						var warnData = `零件编号[${partialForm.code}]在零件入库单[${warehouse_no}]中不存在，是否新建入库单！`;
						warningConfirm(warnData,function(){
							var production_type = $("#hide_production_type").val();
							if(production_type != type){
								var errorData = `零件编码[${partialForm.code}]不适用于当前作业内容！`;
								errorPop(errorData);
							}else{
								allPartialMap.set(code,code);
								var obj = {
										"key":$("#hide_key").val(),
										"partial_id":partialForm.partial_id,
										"seq":"0",
										"dn_no":warehouse_no + "E",
										"code":partialForm.code,
										"partial_name":partialForm.name,
										"quantity":"0",
										"collation_quantity":"",
										"flg":"1"
								};
								searchlist.push(obj);
								$("#collationlist").jqGrid('setGridParam', {data : searchlist}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
								updateList(partialForm.partial_id);
							}
						},function(){});
					}
				}
			}catch(e){}
		}
	});
};

function updateList(partial_id){
	//合并列数
	let colspan  = 0;
	let dnTD = "";
	let quantityTD = "";
	let collationQuantityTD = "";
	
	//零件信息
	let partial = "";
	
	//验证规则
	let rules= {};
	
	let rule = new Object();
	rule.digits = true;
	rule.min = 0;
	rule.maxlength = 5;
	
	for(let obj of searchlist){
		if(obj.partial_id == partial_id) {
			partial = obj;
			colspan++;
			dnTD += `<td class="td-content">${obj.dn_no}</td>`;
			quantityTD += `<td class="td-content">${obj.quantity}</td>`;
			
			let name = `collation_quantity${obj.seq}`;
			collationQuantityTD += `<td class="td-content">
										<input type="text" class="ui-widget-content" seq="${obj.seq}" name="${name}" alt="${obj.dn_no}核对数量" value="${obj.collation_quantity}" quant="${obj.quantity}">
										<input type="button" class="all_match" value="数量一致"/>
									</td>`;
			rules[name] = rule;
		}
	}
	var content=`<div class="ui-widget-content">
					<form id="updateForm" onsubmit="$('#message_dialog').next().find('button:eq(0)').trigger('click');return false;">
						<table class="condform" style="width:99%;">
					 		<tbody>
					 			<tr>
					 				<td class="ui-state-default td-title" style="min-width:100px;">零件编码</td>
					 				<td class="td-content" colspan="${colspan}">${partial.code}</td>
					 			</tr>
					 			<tr>
				 					<td class="ui-state-default td-title">零件名称</td>
				 					<td class="td-content" colspan="${colspan}">${partial.partial_name}</td>
				 				</tr>
				 				<tr>
				 					<td class="ui-state-default td-title">DN 编号</td>
				 					${dnTD}
				 				</tr>
				 				<tr>
				 					<td class="ui-state-default td-title">数量</td>
				 					${quantityTD}
				 				</tr>
				 				<tr>
				 					<td class="ui-state-default td-title">核对数量</td>
				 					${collationQuantityTD}
				 				</tr>
				 			</tbody>
				 		</table>
				 	</form>
				 </div>`;

	var $dialog = $("#message_dialog");
	var $content = $(content);
	$content.find(".all_match").button().click(function(){
		var $cq = $(this).prev();
		if ($cq.attr("value") != $cq.attr("quant")) {
			$cq.val($cq.attr("quant"))
				.attr("changed", true);
		}
		$(this).closest("#message_dialog").next().find("button:eq(0)").trigger("click");
	})
	.end().find("input[type='text']").change(function(){$(this).attr("changed", true)});
	$dialog.html("").append($content);
	
	$("#updateForm").validate({
		rules : rules
	});
	
	$dialog.dialog({
		resizable : false,
		modal : true,
		title : "更新核对数量",
		width : 400 + 60 * colspan,
		buttons : {
			"确定" : function(){
				if($("#updateForm").valid()){
					updateData.clear();
					$("#updateForm input[type='text'][changed]").each(function(){
						let seq = $(this).attr("seq");
						let value = $(this).val().trim();
						for(let i = 0;i < searchlist.length;i++){
							let partial = searchlist[i];
							
							if(partial.partial_id == partial_id && partial.seq == seq){
								let key = partial_id + "/" + seq;
								
								searchlist[i].collation_quantity = value;
								
								if(value && value != 0){
									updateData.set(key,{"quantity":partial.quantity,"collation_quantity":value,"flg":partial.flg});
								}else{
									if(updateData.has(key)){
										updateData.delete(key);
									}
								}
							}
						}
					});
					
					var data = getUpdateData();
					$.ajax({
						beforeSend : ajaxRequestType,
						async : true,
						url : servicePath + '?method=doUpdateQuantity',
						cache : false,
						data : data,
						type : "post",
						dataType : "json",
						success : ajaxSuccessCheck,
						error : ajaxError,
						complete : function(xhrobj,textStatus){
							var resInfo = null;
							try {
								// 以Object形式读取JSON
								eval('resInfo =' + xhrobj.responseText);
								if (resInfo.errors.length > 0) {
									// 共通出错信息框
									treatBackMessages(null, resInfo.errors);
								} else {
									collationInit();
									$dialog.dialog("close");
								}
							}catch(e){}
						}
					});
					
					//$("#collationlist").jqGrid('setGridParam', {data : searchlist}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
					//$(this).dialog("close");
				}
			},
			"取消" : function() {
				$(this).dialog("close");
			}
		},
		close :function(){
			$("#scanner_inputer").val("").focus();
		}
	});
	$("#updateForm input[type='text']:eq(0)").focus().select();
};

function doStart(){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=searchUnCollation',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					// 零件入库单信息
					var partialWarehouseList = resInfo.partialWarehouseList || [];
					var length = partialWarehouseList.length;
					if(length == 0){
						reset();
						infoPop("没有待核对零件入库单！");
						return;
					}
					
					if(length == 1){
						$("#hide_key").val(partialWarehouseList[0].key);
						chooseKind();
					}else{
						setPartialWarehouse(partialWarehouseList);
					}
				}
			}catch(e){}
		}
	});
};

function doEnd(){
	//var data = getUpdateData();
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=checkCollationFinish',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					// 核对数量是否一致标记
					var differ = resInfo.differ;
					if(differ){
						warningConfirm("此单中存在核对数量与数量不一致的零件，是否结束此单！",function(){
							doFinish();
						},function(){})
					}else{
						doFinish();
					}
				}
			}catch(e){}
		}
	});
};

function doBreak(){
	//var data = getUpdateData();
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doBreak',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					reset();
					enableMenu();
				}
			}catch(e){}
		}
	});
};

function doFinish(){
	 var data = getUpdateData();
	 
	 $.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : servicePath + '?method=doFinish',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					 reset();
					 enableMenu();
				}
			}catch(e){}
		}
	 });
}


function getUpdateData(){
	var data = {};
	
	var index = 0;
	for(var item of updateData){
		let arr = item[0].split("/");
		var partial_id = arr[0];
		var seq = arr[1];
		var collation_quantity = item[1].collation_quantity;
		var flg = item[1].flg;
		
		data["partial_warehouse_detail.partial_id[" + index + "]"] = partial_id;
		data["partial_warehouse_detail.seq[" + index + "]"] = seq;
		data["partial_warehouse_detail.collation_quantity[" + index + "]"] = collation_quantity;
		data["partial_warehouse_detail.flg[" + index + "]"] = flg;
		
		index++;
	}
	
	return data;
};

function reset(){
	list([]);
	$("#startbutton").enable().removeClass("ui-state-focus");
	$("#breakbutton,#endbutton").disable().removeClass("ui-state-focus");
	$("#label_production_type_name").text("");
	$("#scanner_container").hide();
	$("#scanner_inputer,#hide_key,#hide_production_type").val("");
	$("#content tr:nth-child(n+3)").remove();
	allPartialMap.clear();
	updateData.clear();
	searchlist = [];
	
	$("#partial_details").hide();
	$("#partial_details td:eq(1),#partial_details td:eq(3)").text("");
	$("#dtl_process_time label").text("");
	clearInterval(oInterval);
	oInterval = null;
	$("#p_rate div:animated").stop();
	p_time = 0;
	leagal_overline = null;
};

function collationInit(){
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
		complete : function(xhrobj,textStatus){
			var resInfo = null;
			try {
				// 以Object形式读取JSON
				eval('resInfo =' + xhrobj.responseText);
				if (resInfo.errors.length > 0) {
					// 共通出错信息框
					treatBackMessages(null, resInfo.errors);
				} else {
					// 现品作业信息
					var fact_production_feature = resInfo.unfinish;

					// 存在正在进行中的作业信息
					if(fact_production_feature){
						$("#startbutton").disable().removeClass("ui-state-focus");
						$("#breakbutton,#endbutton").enable().removeClass("ui-state-focus");
						
						$("#scanner_container").show();
						$("#scanner_inputer").val("").focus();

						$("#label_production_type_name").text(fact_production_feature.production_type_name);
						$("#hide_key").val(fact_production_feature.partial_warehouse_key);
						$("#hide_production_type").val(fact_production_feature.production_type);
						$("#hide_warehouse_no").val(resInfo.partialWarehouse.warehouse_no);
						
						$("#content tr:nth-child(n+3)").remove();
						//零件入库DN编号
						var partialWarehouseDnList = resInfo.partialWarehouseDnList;
						
						if(partialWarehouseDnList && partialWarehouseDnList.length > 0){
							var content = "";
							
							partialWarehouseDnList.forEach(function(item,index){
								let dn_no = item.dn_no;
								let warehouse_date = item.warehouse_date;
								content += `<tr>
												<td class="td-content-text" style="text-align:left;">${warehouse_date}</td>
												<td class="td-content-text" style="text-align:left;">${dn_no}</td>
											</tr>`;
							});
							
							$("#content tbody").append(content);
						}
						
						enableMenu("collationbutton");
						
						allPartialMap.clear();
						var allPartialList = resInfo.allPartialList;
						allPartialList.forEach(function(item,index){
							allPartialMap.set(item.code,item.code);
						});
						
						// 零件入库明细
						var partialWarehouseDetailList = resInfo.partialWarehouseDetailList;
						list(partialWarehouseDetailList);
						setRate(fact_production_feature,resInfo.leagal_overline,resInfo.spent_mins);

						$("#scanner_inputer").val("").focus();
					}else{
						reset();
						enableMenu();
					}
				}
			}catch(e){}
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

function chooseKind(parentDialog){
	var content=`<div class="ui-widget-content">
					<table class="condform">
				 		<tbody>
				 			<tr>
				 				<td class="ui-state-default td-title">作业内容</td>
				 				<td class="td-content" id="production_type">
				 					<input type="radio" name="production_type" id="B1" value="20">
				 					<label for="B1">B1、核对+上架</label>
				 					<input type="radio" name="production_type" id="B2" value="21">
				 					<label for="B2">B2、核对</label>
				 				</td>
				 			</tr>
				 		</tbody>
				</table></div>`;
	$("#choose_spec_kind_dialog").html("").append(content);
	$("#production_type").buttonset();
	
	$("#choose_spec_kind_dialog").dialog({
		resizable : false,
		modal : true,
		title : "请选择作业内容",
		width : 400,
		buttons : {
			"确认" : function() {
				var productionType = $("input[name='production_type']:checked").val();
				if(!productionType){
					errorPop("请选择作业内容！");
					return;
				}
				
				var data = {
					"production_type" : productionType,
					"partial_warehouse_key" : $("#hide_key").val().trim()
				};
				
				$.ajax({
					beforeSend : ajaxRequestType,
					async : true,
					url : servicePath + '?method=checkUnMatch',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : function(xhrobj,textStatus){
						var resInfo = null;
						try {
							// 以Object形式读取JSON
							eval('resInfo =' + xhrobj.responseText);
							if (resInfo.errors.length > 0) {
								// 共通出错信息框
								treatBackMessages(null, resInfo.errors);
							} else {
								var matchFlg = resInfo.matchFlg;
								if(!matchFlg){
									var message = "作业内容:" + $("#production_type input[value='" + productionType + "']").next().html() + 
												   "，不适用于入库单" + $("#partial_warehouse_dialog tr[key='" + data.partial_warehouse_key + "'] td:eq(1)").text();
									infoPop(message);
								}else{
									$.ajax({
										beforeSend : ajaxRequestType,
										async : false,
										url : 'fact_production_feature.do?method=doStart',
										cache : false,
										data : data,
										type : "post",
										dataType : "json",
										success : ajaxSuccessCheck,
										error : ajaxError,
										complete : function(xhrobj,textStatus){
											var resInfo = null;
											try {
												// 以Object形式读取JSON
												eval('resInfo =' + xhrobj.responseText);
												if (resInfo.errors.length > 0) {
													// 共通出错信息框
													treatBackMessages(null, resInfo.errors);
												} else {
													if(parentDialog){
														parentDialog.dialog("close");
													}
													$("#choose_spec_kind_dialog").dialog("close");
													$("#startbutton").disable().removeClass("ui-state-focus");
													$("#endbutton").enable().removeClass("ui-state-focus");
													enableMenu("collationbutton");
													
													collationInit();
												}
											} catch (e) {}
										}
									});
								}
							}
						} catch (e) {}
					}
				});
			},
			"取消" : function() {
				$(this).dialog("close");

			}
		}
	});
	$("#production_type label").removeClass("ui-state-focus");
};

function setPartialWarehouse(list){
	var $dialog = $("#partial_warehouse_dialog");
	
	var content = `<div class="ui-widget-content">
						<table class="condform">
							<thead>
								<tr>
									<th class="ui-state-default td-title"></th>
									<th class="ui-state-default td-title">入库单号</th>
									<th class="ui-state-default td-title">DN 编号</th>
								</tr>
							</thead>
							<tbody>`;
	list.forEach(function(item,index){
		var key = item.key,
		warehouse_no = item.warehouse_no,
		dn_no = item.dn_no;
		content +=`<tr key="${key}">
			<td class="td-content"><input type="button" class="ui-button" value="选择"></td>
			<td class="td-content">${warehouse_no}</td>
			<td class="td-content">${dn_no}</td>
		</tr>`;
	});
	content += `</tbody></table></div>`;
	
	$dialog.html("").hide().append(content).find(".ui-button").button();
	
	$dialog.dialog({
		resizable : false,
		modal : true,
		title : "请选择零件入库单",
		width : 500,
		buttons : {
			"取消" : function() {
				$(this).dialog("close");
			} 
		}
	});
	
	$dialog.find(".ui-button").removeClass("ui-state-focus").each(function(){
		$(this).click(function(){
			var key = $(this).closest("tr").attr("key");
			$("#hide_key").val(key);
			chooseKind($dialog);
		});
	});
};

function list(listdata){
	searchlist = listdata;
	for(let i = 0;i < searchlist.length;i++){
		if(searchlist[i].seq == 0){
			searchlist[i].flg = "0";
		}
	}
	
	if ($("#gbox_collationlist").length > 0) {
		$("#collationlist").jqGrid().clearGridData();// 清除
		$("#collationlist").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
	} else {
		$("#collationlist").jqGrid({
			data : listdata,// 数据
			height :461,// rowheight*rowNum+1
			width : 1178,
			rowheight : 23,
			shrinkToFit:true,
			datatype : "local",
			colNames : ['','','seq','零件编码','零件名称','数量','已核对数量',''],
			colModel : [{name : 'key',index : 'key',hidden : true},
			            {name : 'partial_id',index : 'partial_id',hidden : true},
			            {name : 'seq',index : 'seq',hidden : true},
			            {name : 'code',index : 'code',width:50},
			            {name : 'partial_name',index : 'partial_name',width:200},
			            {name : 'quantity',index : 'quantity',width:50,align:'right'},
			            {name : 'collation_quantity',index : 'collation_quantity',width:50,align:'right'},
			            {name : 'flg',index : 'flg',hidden : true,formatter : function(value, options, rData){
			            	if(rData.seq == 0){
			            		return  "0";
			            	}else{
			            		return  "";
			            	}
			            }}
			],
			rowNum : 20,
			toppager : false,
			pager : "#collationlistpager",
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
			},
			viewsortcols : [ true, 'vertical', true ],
			gridComplete : function() {
				var IDS = $("#collationlist").getDataIDs();
				// 当前显示多少条
				var length = IDS.length;
				var pill = $("#collationlist");
				
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