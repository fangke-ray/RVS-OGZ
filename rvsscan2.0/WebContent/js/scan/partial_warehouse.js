var servicePath = "partialWarehouse.scan";
var currentPos = 0;
var currentPos2 = 0;
$(function () {

	refresh();
	
	setInterval(refresh,60000);
});

var refresh = function () {
	var now = parseInt(((new Date().getTime() + 28800000) % 86400000) / 60000) - 480;
	var $standard_columns = $("#standard_column div").not(".position_intro, #now_period");
	if (now > 0 && now < 690) {
		$("#axis_base #now_period").css("height", now + "px");
	}
	if (now > 570) {
		$("#axis_base").addClass("overwork");
	}

	$standard_columns.each(function (idx, ele) {
		var bottom = parseInt(window.getComputedStyle(ele).bottom);
		if (bottom <= now)
			ele.className = "exceed";
	});

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true, // false
		url : servicePath + '?method=refresh',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : refreshSuccess
	});
};

var refreshSuccess = function(xhrObj) {
	var resInfo = $.parseJSON(xhrObj.responseText);
	if (resInfo.errors && resInfo.errors.length == 0) {
		// 仓管人员工作当前进度
		setProcess(resInfo.process);
		
		setChart(resInfo);
		
		// 仓管人员工作今日成果
		setCurrentResult(resInfo.resultList);
		
		setPercent(resInfo);
		
		var waitList = resInfo.waitList;
		var waitPartialWarehouseList = [];
		var waitMaterialList = [];
		
		for(var obj of waitList){
			if(obj.warehouse_no){
				waitPartialWarehouseList.push(obj);
			}else if(obj.omr_notifi_no){
				waitMaterialList.push(obj);
			}
		}
		
		setMaterialWait(waitMaterialList);
		
		setPartialWarehouseWait(waitPartialWarehouseList);
	}
};

function setChart(resInfo){
	// 仓管人员负荷率警报标志上线
	var strHighLever = resInfo.strHighLever;
	// 仓管人员负荷率警报标志下线
	var strLowLever = resInfo.strLowLever;
	
	var $y_columns = $(".y_columns .operator_flex").detach();
	$y_columns.html("");
	
	var pfs = resInfo.productionFeatures;
	var dirxTime = {};
	var elapse = parseInt(((new Date().getTime() + 28800000) % 86400000) / 60000) - 480;
	
	for (var ipf in pfs) {
		var pf = pfs[ipf];
		var $item = $('<div class="production_feature" d_type="' + pf.production_type + '" style="bottom:' + 
				pf.action_time + 'px;height:' + pf.spare_minutes + 'px;"' + '></div>');
		
		var $y_column = $y_columns.children(".y_column[for=" + pf.operatorId + "]");
		if ($y_column.length == 0) {
			$y_column = $("<div class=\"y_column\" " + "for=\"" + pf.operatorId + "\"><div class=\"position_intro\">" + pf.operatorName + "</div></div>");
			$y_columns.append($y_column);
		}
		
		$y_column.append($item);
		
		if (dirxTime[pf.operatorId] == null) {
			var dirxTimeOfJobNo = {};
			for (var ix = 1; ix <= elapse; ix++) {
				dirxTimeOfJobNo[ix] = 0;
			}
			
			if (elapse > 420) {
				for (var ix = 421; ix <= 430; ix++) {
					dirxTimeOfJobNo[ix] = null;
				}
			}
			
			if (elapse > 240) {
				for (var ix = 241; ix <= 300; ix++) {
					dirxTimeOfJobNo[ix] = null;
				}
			}

			if (elapse > 120) {
				for (var ix = 121; ix <= 130; ix++) {
					dirxTimeOfJobNo[ix] = null;
				}
			}
			
			dirxTime[pf.operatorId] = dirxTimeOfJobNo;
		}

		var dirxTimeOfJobNo = dirxTime[pf.operatorId];
		var endTime = parseInt(pf.action_time) + parseInt(pf.spare_minutes);
		
		for (var ix = parseInt(pf.action_time); ix <= endTime; ix++) {
			if (dirxTimeOfJobNo[ix] == 0) {
				dirxTimeOfJobNo[ix] = 1;
			}
			dirxTime[pf.operatorId] = dirxTimeOfJobNo;
		}
	}

	$y_columns.children().each(function(){
		var $y_column = $(this);
		var operatorId = $y_column.attr("for");
		var rate = checkDirxTime(dirxTime[operatorId]);
		operatorId = operatorId.padStart(11,"0");
		
		rate = rate * 1;
		var className = "";
		if(rate < strLowLever){
			className = "low";
		}else if(rate > strHighLever){
			className = "over";
		}
		
		$("#resultarea .result:last-child").find(".item[for=" + operatorId + "] .per:last-child")
		.removeClass("over low").text(rate).addClass(className);
	});
	
	$(".y_columns").append($y_columns);
};


function setProcess(list){

	// 仓管人员工作当前进度
	var content = "";
	
	list.forEach(function(item,index){
		var production_type = item.production_type || "";
		var production_type_name = item.production_type_name || "一";
		var warehouse_no = item.warehouse_no || "一";
		var operator_name = item.operator_name;
		var standardTime = item.standardTime || "一";
		var spentMins =  item.spentMins;
		if(spentMins == undefined){
			spentMins = "一";
		}
		
		var progressBar = "";

		if(production_type){
			if(production_type == 99){
				progressBar = `<div class='tube-liquid tube-green' style='width:99%;text-align:right;'></div>`;
				spentMins = minuteFormat(spentMins);
			}else{
				var rate = parseInt(spentMins / standardTime * 100);
				if (rate > 99) {
					rate = 99;
				}
				
				var className = "";
				if (rate > 80) {
					if (rate > 95) {
						className = "tube-orange";
					} else {
						className = "tube-yellow";
					}
				} else {
					className = "tube-green";
				}
				
				progressBar = `<div class="tube-liquid ${className}" style="width:${rate}%;text-align:right;"></div>`;
				standardTime = minuteFormat(standardTime);
				spentMins = minuteFormat(spentMins);
			}	
		}
		
		content += `<div class="flex-box">
						<div class="item">${operator_name}</div>
						<div class="item" style="flex:2;">${production_type_name}</div>
						<div class="item">${warehouse_no}</div>
						<div class="item" style="position: relative;">
							<span class="notice">标</span><span class="time">${standardTime}</span>
						</div>
				    </div>
				    <div class="flex-box" style="background-color: #DBEEF4;">
						<div class="item" style="flex:4">
							<div class="waiting tube" id="p_rate" style="height: 20px; margin: auto; margin-top: 3px;">${progressBar}</div>
						</div>
						<div class="item">
							<span class="time">${spentMins}</span>
						</div>
					</div>`;
					
		$("#resultarea .result:eq(0)").find(".item").eq(index + 1).text(operator_name);
		$("#resultarea .result:gt(0)").each(function(){
			$(this).find(".item:nth-child(" + (2 * index +2) + ")").attr("for",item.operator_id)
		});
	});
	
	$("#current_process").html(content);
};

// 今日成果
function setCurrentResult(list){
	list.forEach(function(item,index){
		if(item.recept) $("#resultarea .result:eq(1)").find(".item[for=" + item.operator_id + "]").text(item.recept +" 箱");
		if(item.collation_shelf) $("#resultarea .result:eq(2)").find(".item[for=" + item.operator_id + "]").text(item.collation_shelf +" 个");
		if(item.collation) $("#resultarea .result:eq(3)").find(".item[for=" + item.operator_id + "]").text(item.collation +" 包");
		if(item.unpack) $("#resultarea .result:eq(4)").find(".item[for=" + item.operator_id + "]").text(item.unpack +" 袋");
		if(item.on_shelf) $("#resultarea .result:eq(5)").find(".item[for=" + item.operator_id + "]").text(item.on_shelf +" 包");
		if(item.ns_outline) $("#resultarea .result:eq(6)").find(".item[for=" + item.operator_id + "]").text(item.ns_outline +" 单");
		if(item.dec_outline) $("#resultarea .result:eq(7)").find(".item[for=" + item.operator_id + "]").text(item.dec_outline +" 单");
		if(item.spentMins) $("#resultarea .result:eq(8)").find(".item[for=" + item.operator_id + "]").text(item.spentMins +" 分钟").next().text("100 %");
	});
};

function setPercent(resInfo){
	// 仓管人员能率警报标志上线
	var efHighLever = resInfo.efHighLever;
	// 仓管人员能率警报标志下线
	var efLowLever = resInfo.efLowLever;
	
	resInfo.percentList.forEach(function(item,index){
		if(item.accept_percent){
			var accept_percent = item.accept_percent * 1;
			var className = "";
			if(accept_percent < efLowLever){
				className = "low";
			}else if(accept_percent > efHighLever){
				className = "over";
			}
			$("#resultarea .result:eq(1)").find(".item[for=" + item.operator_id + "]").next()
			.removeClass("over low").text(accept_percent +" %").addClass(className);
		}
		if(item.collation_shelf_percent) {
			var collation_shelf_percent = item.collation_shelf_percent * 1;
			var className = "";
			if(collation_shelf_percent < efLowLever){
				className = "low";
			}else if(collation_shelf_percent > efHighLever){
				className = "over";
			}
			$("#resultarea .result:eq(2)").find(".item[for=" + item.operator_id + "]").next()
			.removeClass("over low").text(collation_shelf_percent +" %").addClass(className);
		}
		if(item.collation_percent) {
			var collation_percent = item.collation_percent * 1;
			var className = "";
			if(collation_percent < efLowLever){
				className = "low";
			}else if(collation_percent > efHighLever){
				className = "over";
			}
			$("#resultarea .result:eq(3)").find(".item[for=" + item.operator_id + "]").next()
			.removeClass("over low").text(collation_percent +" %").addClass(className);
		}
		if(item.unpack_percent) {
			var unpack_percent = item.unpack_percent * 1;
			var className = "";
			if(unpack_percent < efLowLever){
				className = "low";
			}else if(unpack_percent > efHighLever){
				className = "over";
			}
			$("#resultarea .result:eq(4)").find(".item[for=" + item.operator_id + "]").next()
			.removeClass("over low").text(unpack_percent +" %").addClass(className);
		}
		if(item.on_shelf_percent) {
			var on_shelf_percent = item.on_shelf_percent * 1;
			var className = "";
			if(on_shelf_percent < efLowLever){
				className = "low";
			}else if(on_shelf_percent > efHighLever){
				className = "over";
			}
			$("#resultarea .result:eq(5)").find(".item[for=" + item.operator_id + "]").next()
			.removeClass("over low").text(on_shelf_percent +" %").addClass(className);
		}
		if(item.ns_outline_percent) {
			var ns_outline_percent = item.ns_outline_percent * 1;
			var className = "";
			if(ns_outline_percent < efLowLever){
				className = "low";
			}else if(ns_outline_percent > efHighLever){
				className = "over";
			}
			$("#resultarea .result:eq(6)").find(".item[for=" + item.operator_id + "]").next()
			.removeClass("over low").text(ns_outline_percent +" %").addClass(className);
		}
		if(item.dec_outline_percent) {
			var dec_outline_percent = item.dec_outline_percent * 1;
			var className = "";
			if(dec_outline_percent < efLowLever){
				className = "low";
			}else if(dec_outline_percent > efHighLever){
				className = "over";
			}
			$("#resultarea .result:eq(7)").find(".item[for=" + item.operator_id + "]").next()
			.removeClass("over low").text(dec_outline_percent +" %").addClass(className);
		}
		if(item.total_percent) {
			var total_percent = item.total_percent * 1;
			var color = "";
			if(total_percent < efLowLever){
				color = "red";
			}else if(total_percent > efHighLever){
				color = "#4ABD62";
			}
			$("#resultarea .result:eq(9)").find(".item[for=" + item.operator_id + "]").next()
			.find(".per").eq(1).text(total_percent +" %").css({"color":color});
		}
	});
};

function setMaterialWait(list){
	var listsize = list.length;

	if (currentPos >= listsize) {
		currentPos = 0;
	}

	if (listsize > 0) {
		var partial_warehouse = list[currentPos];
		
		$("#wait .item:eq(0) label").hide("fade", function(){$(this).text(partial_warehouse.omr_notifi_no);});
		$("#wait .item:eq(1) label").hide("fade", function(){
			$("#wait .item:eq(0) label").show("fade");
			$("#wait .item:eq(1) label").show("fade");
			$(this).text(judgeStatus(partial_warehouse));
			
			if (listsize == 1) {$("#wait .item:gt(1) label").text(""); return};

			var tr2_pos = currentPos;
			if (tr2_pos >= listsize) tr2_pos -= listsize;
			
			partial_warehouse = list[tr2_pos];
			$("#wait .item:eq(2) label").hide("fade", function(){$(this).text(partial_warehouse.omr_notifi_no);});
			$("#wait .item:eq(3) label").hide("fade", function(){
				$("#wait .item:eq(2) label").show("fade");
				$("#wait .item:eq(3) label").show("fade");
				$(this).text(judgeStatus(partial_warehouse));
				
				if (listsize == 2) {$("#wait .item:gt(3) label").text(""); return};
				
				var tr3_pos = currentPos + 1;
				if (tr3_pos >= listsize) tr3_pos -= listsize;
				
				partial_warehouse = list[tr3_pos];
				$("#wait .item:eq(4) label").hide("fade", function(){$(this).text(partial_warehouse.omr_notifi_no);});
				$("#wait .item:eq(5) label").hide("fade", function(){
					$("#wait .item:eq(4) label").show("fade");
					$("#wait .item:eq(5) label").show("fade");
					$(this).text(judgeStatus(partial_warehouse));
					
					if (listsize == 3) {$("#wait .item:gt(5) label").text(""); return};
					
					var tr4_pos = currentPos + 2;
					if (tr4_pos >= listsize) tr4_pos -= listsize;
					
					partial_warehouse = list[tr4_pos];
					$("#wait .item:eq(6) label").hide("fade", function(){$(this).text(partial_warehouse.omr_notifi_no);});
					$("#wait .item:eq(7) label").hide("fade", function(){
						$("#wait .item:eq(6) label").show("fade");
						$("#wait .item:eq(7) label").show("fade");
						$(this).text(judgeStatus(partial_warehouse));
					});
				});
			});
		});
		currentPos ++;
	}else {
		$("#wait label").hide();
	}
};

function setPartialWarehouseWait(list){
	var listsize = list.length;

	if (currentPos2 >= listsize) {
		currentPos2 = 0;
	}

	if (listsize > 0) {
		var partial_warehouse = list[currentPos2];
		
		$("#wait2 .item:eq(0) label").hide("fade", function(){$(this).text(partial_warehouse.warehouse_no);});
		$("#wait2 .item:eq(1) label").hide("fade", function(){
			$("#wait2 .item:eq(0) label").show("fade");
			$("#wait2 .item:eq(1) label").show("fade");
			$(this).text(judgeStatus(partial_warehouse));
			
			if (listsize == 1) {$("#wait .item:gt(1) label").text(""); return};

			var tr2_pos = currentPos2;
			if (tr2_pos >= listsize) tr2_pos -= listsize;
			
			partial_warehouse = list[tr2_pos];
			$("#wait2 .item:eq(2) label").hide("fade", function(){$(this).text(partial_warehouse.warehouse_no);});
			$("#wait2 .item:eq(3) label").hide("fade", function(){
				$("#wait2 .item:eq(2) label").show("fade");
				$("#wait2 .item:eq(3) label").show("fade");
				$(this).text(judgeStatus(partial_warehouse));
				
				if (listsize == 2) {$("#wait .item:gt(3) label").text(""); return};
				
				var tr3_pos = currentPos2 + 1;
				if (tr3_pos >= listsize) tr3_pos -= listsize;
				
				partial_warehouse = list[tr3_pos];
				$("#wait2 .item:eq(4) label").hide("fade", function(){$(this).text(partial_warehouse.warehouse_no);});
				$("#wait2 .item:eq(5) label").hide("fade", function(){
					$("#wait2 .item:eq(4) label").show("fade");
					$("#wait2 .item:eq(5) label").show("fade");
					$(this).text(judgeStatus(partial_warehouse));
					
					if (listsize == 3) {$("#wait .item:gt(5) label").text(""); return};
					
					var tr4_pos = currentPos2 + 2;
					if (tr4_pos >= listsize) tr4_pos -= listsize;
					
					partial_warehouse = list[tr4_pos];
					$("#wait2 .item:eq(6) label").hide("fade", function(){$(this).text(partial_warehouse.warehouse_no);});
					$("#wait2 .item:eq(7) label").hide("fade", function(){
						$("#wait .item:eq(6) label").show("fade");
						$("#wait .item:eq(7) label").show("fade");
						$(this).text(judgeStatus(partial_warehouse));
					});
				});
			});
		});
		currentPos2 ++;
	}else {
		$("#wait2 label").hide();
	}
}

function checkDirxTime(dirxTime){
	if (!dirxTime) return 0;
	var cnt = 0;
	var length = 0;
	for (var item in dirxTime) {
		if (dirxTime[item] == 1) cnt++;
		if (dirxTime[item] != null) length++;
	}
	return (cnt / length * 100).toFixed(1);
};

function minuteFormat(iminute) {
	if (!iminute) return "-";
	var hours = parseInt(iminute / 60);
	var minutes = iminute % 60;

	return fillZero(hours, 2) + "小时" + fillZero(minutes, 2) + "分钟";
};

function judgeStatus(obj){
	var content = "";
	var omr_notifi_no = obj.omr_notifi_no;
	if(omr_notifi_no){
		var arr = obj.content.split(",");
		if(arr.length == 1){
			//工位代码
			var process_code = arr[0].split(":")[0];
			//开始时间
			var time = arr[0].split(":")[1];
			if(time){
				if(process_code == 252 || process_code == 504){
					content ="分解出库中";
				}else{
					content ="NS出库中";
				}
			}else{
				if(process_code == 252 || process_code == 504){
					content ="等待分解出库";
				}else{
					content ="等待NS出库";
				}
			}
		}else{
			var nsFlg = 0;
			var decFlg = 0;
			for(var i in arr){
				var process_code = arr[i].split(":")[0];
				var time = arr[i].split(":")[1];
				if(process_code == 252){
					if(time){
						decFlg++;
					}
				}
				
				if(process_code == 321){
					if(time){
						nsFlg++;
					}
				}
			}
			
			if(nsFlg == 0 && decFlg == 0){
				content ="等待NS/分解出库";
			}else if(nsFlg == 1 && decFlg == 0){
				content ="NS出库中,等待分解出库";
			}else if(nsFlg == 0 && decFlg == 1){
				content ="分解出库中,等待NS出库";
			}else{
				content ="NS/分解出库中";
			}
		}
	}else{
		var step = obj.step;
		
		if(step == 0){
			content = "收货中";
		}else if(step == 1){
			var collation = obj.collation;
			if(collation == 1){
				content = "核对中";
			}else{
				content = "等待核对";
			}
		}else if(step == 2){
			var unpack = obj.unpack;
			var on_shelf = obj.on_shelf;
			
			if(unpack == 0 && on_shelf == 0){
				content = "等待分装/上架中";
			}else if(unpack == 1 && on_shelf == 0){
				content = "分装中";
			}else if(unpack == 0 && on_shelf == 1){
				content = "上架中";
			}else{
				content = "分装/上架中";
			}
		}
	}
	
	return content;
};