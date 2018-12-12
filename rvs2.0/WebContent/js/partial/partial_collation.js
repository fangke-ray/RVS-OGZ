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
	
	$("#endbutton").click(doEnd);
	
	// 输入框触发，配合浏览器
	$("#scanner_inputer").keypress(function(){
		if (this.value.length === 11) {
			startScanner();
		}
	});
	$("#scanner_inputer").keyup(function(){
		if (this.value.length >= 11) {
			startScanner();
		}
	});
	
	collationInit();
});

function startScanner(){
	var data = {
		"partial_id" : $("#scanner_inputer").val().trim()
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
					$("#scanner_inputer").val("");
					var dn_no = $("#label_dn_no").text(),
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
					
					if(allPartialMap.has(data.partial_id)){
						var production_type = $("#hide_production_type").val();
						if(production_type != type){
							var errorData = `零件编码[${partialForm.code}]不适用于当前作业内容！`;
							errorPop(errorData);
						}else{
							// 更新一览数据
							updateList(data.partial_id);
						}
					}else{
						var warnData = `零件编码[${partialForm.code}]在零件入库单DN编号为[${dn_no}]中不存在，是否加入此单中！`;
						warningConfirm(warnData,function(){
							var production_type = $("#hide_production_type").val();
							if(production_type != type){
								var errorData = `零件编码[${partialForm.code}]不适用于当前作业内容！`;
								errorPop(errorData);
							}else{
								allPartialMap.set(partialForm.partial_id,partialForm.partial_id);
								var obj = {
										"key":$("#hide_key").val(),
										"partial_id":partialForm.partial_id,
										"code":partialForm.code,
										"partial_name":partialForm.name,
										"quantity":"",
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
	let $grid = $("#collationlist");
	
	let map = new Map();// key=> partial_id,value=> rowid
	
	let IDS = $grid.getDataIDs();
	for(var id of IDS){
		let rowData = $grid.getRowData(id);
		map.set(rowData.partial_id,id);
	}
	
	// 行数据
	let rowData = $grid.getRowData(map.get(partial_id));
	
	var content=`<div class="ui-widget-content">
					<form id="updateForm">
						<table class="condform">
					 		<tbody>
					 			<tr>
					 				<td class="ui-state-default td-title">零件编码</td>
					 				<td class="td-content">${rowData.code}</td>
					 			</tr>
					 			<tr>
				 					<td class="ui-state-default td-title">零件名称</td>
				 					<td class="td-content">${rowData.partial_name}</td>
				 				</tr>
				 				<tr>
				 					<td class="ui-state-default td-title">数量</td>
				 					<td class="td-content">${rowData.quantity}</td>
				 				</tr>
				 				<tr>
				 					<td class="ui-state-default td-title">核对数量</td>
				 					<td class="td-content">
				 						<input type="text" class="ui-widget-content" name="collation_quantity" id="update_collation_quantity" alt="核对数量" value="${rowData.collation_quantity}">
				 					</td>
				 				</tr>
					 		</tbody>
						</table>
					</form>
				</div>`;
	
	var $dialog = $("#message_dialog");
	$dialog.html("").append(content);
	
	$("#updateForm").validate({
		rules : {
			collation_quantity : {
				digits:true,
				min:1,
				maxlength:5
			}
		}
	});
	
	$dialog.dialog({
		resizable : false,
		modal : true,
		title : "更新核对数量",
		width : 400,
		buttons : {
			"确定" : function(){
				if($("#updateForm").valid()){
					for(let i = 0;i < searchlist.length;i++){
						if(searchlist[i].partial_id == partial_id){
							searchlist[i].collation_quantity = $("#update_collation_quantity").val();
							break;
						}
					}
					$("#collationlist").jqGrid('setGridParam', {data : searchlist}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
					updateData.set(partial_id,{"quantity":rowData.quantity,"collation_quantity":$("#update_collation_quantity").val(),"flg":rowData.flg});
					$(this).dialog("close");
				}
			},
			"取消" : function() {
				$(this).dialog("close");
			} 
		}
	});
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
	var data = getUpdateData();
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=checkCollationFinish',
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
	var data = getUpdateData();
	
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : servicePath + '?method=doBreak',
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
};

function doFinish(){
	 var data = getUpdateData();
	 data["step"] = "2";
	 
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
		var partial_id = item[0];
		var collation_quantity = item[1].collation_quantity;
		var flg = item[1].flg;
		
		data["partial_warehouse_detail.partial_id[" + index + "]"] = partial_id;
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
	$("#label_warehouse_date,#label_dn_no,#label_production_type_name").text("");
	$("#scanner_container").hide();
	$("#scanner_inputer,#hide_key,#hide_production_type").val("");
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
						$("#scanner_inputer").val("");
						
						$("#label_warehouse_date").text(fact_production_feature.warehouse_date);
						$("#label_dn_no").text(fact_production_feature.dn_no);
						$("#label_production_type_name").text(fact_production_feature.production_type_name);
						$("#hide_key").val(fact_production_feature.partial_warehouse_key);
						$("#hide_production_type").val(fact_production_feature.production_type);
						
						enableMenu("collationbutton");
						
						allPartialMap.clear();
						var allPartialList = resInfo.allPartialList;
						allPartialList.forEach(function(item,index){
							allPartialMap.set(item.partial_id,item.partial_id);
						});
						
						// 零件入库明细
						var partialWarehouseDetailList = resInfo.partialWarehouseDetailList;
						list(partialWarehouseDetailList);
						setRate(fact_production_feature,resInfo.leagal_overline,resInfo.spent_mins);
					}else{
						reset();
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

function chooseKind(){
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
								$("#choose_spec_kind_dialog").dialog("close");
								$("#startbutton").disable().removeClass("ui-state-focus");
								$("#endbutton").enable().removeClass("ui-state-focus");
								enableMenu("collationbutton");
								
								collationInit();
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
									<th class="ui-state-default td-title">日期</th>
									<th class="ui-state-default td-title">DN 编号</th>
								</tr>
							</thead>
							<tbody>`;
	list.forEach(function(item,index){
		var key = item.key,
		warehouse_date = item.warehouse_date,
		dn_no = item.dn_no;
		content +=`<tr key="${key}">
			<td class="td-content"><input type="button" class="ui-button" value="选择"></td>
			<td class="td-content">${warehouse_date}</td>
			<td class="td-content">${dn_no}</td>
		</tr>`;
	});
	content += `</tbody></table></div>`;
	
	$dialog.html("").hide().append(content).find(".ui-button").button();
	
	$dialog.dialog({
		resizable : false,
		modal : true,
		title : "请选择零件入库单",
		width : 400,
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
			$dialog.dialog("close");
			chooseKind();
		});
	});
};

function list(listdata){
	searchlist = listdata;
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
			colNames : ['','','零件编码','零件名称','数量','核对数量',''],
			colModel : [{name : 'key',index : 'key',hidden : true},
			            {name : 'partial_id',index : 'partial_id',hidden : true},
			            {name : 'code',index : 'code',width:200},
			            {name : 'partial_name',index : 'partial_name',width:200},
			            {name : 'quantity',index : 'quantity',width:200,align:'right'},
			            {name : 'collation_quantity',index : 'collation_quantity',width:200,align:'right',formatter : function(value, options, rData){
							if(value < 0){
								rData.flg = "0";
								return  value * -1;
							}else{
								return  value;
							}
						}},
			            {name : 'flg',index : 'flg',hidden : true}
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