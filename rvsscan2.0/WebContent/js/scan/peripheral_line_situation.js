var lineP_servicePath = "lineSituationPeripheral.scan";

var device_plan_value = 0;
var device_plan_complete_value = 0;

var refresh_ajaxSuccess = function(xhrobj,textStatus){

	var resInfo = $.parseJSON(xhrobj.responseText);

	$("#waiting_quote").text(resInfo.waiting_quote);
	$("#waiting_inline").text(resInfo.waiting_cast);
	$("#waiting_repair").text(resInfo.waiting_repair);
	$("#waiting_parts").text("-"); // resInfo.waiting_parts

	if ($("#device_plan_count").length > 0) {
		$("#device_plan_count").flipCounter({numIntegralDigits:2,
			digitHeight:124,
			digitWidth:62,
			imagePath:"images/white_counter.png",
			number : resInfo.plan
		});
		$("#device_plan_finish_count").flipCounter({numIntegralDigits:2,
			digitHeight:124,
			digitWidth:62,
			imagePath:"images/white_counter.png",
			number : resInfo.plan_complete
		});

        device_plan_value = resInfo.plan;

        device_plan_complete_value = resInfo.plan_complete;

		var com_rate = 0;
        if (device_plan_value > 0) {
        	com_rate = Math.floor(device_plan_complete_value / device_plan_value * 100);
        }
		$('#device_plan_complete .donut-arrow').trigger('updatePercentage', com_rate);
		$("#device_completed_rate").text((device_plan_value ? com_rate.toFixed(0) + "%" : "- %"));
	}

	$("#waiting_qa").text((resInfo.currentWaitingCountP || 0));
	$("#waiting_qa_confirm").text((resInfo.currentWaitingConfirmCountP || 0));

	$("#today_pass").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png",
		number : (resInfo.currentPassCountP || 0)
	});
	$("#today_unqualified").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png",
		number : (resInfo.currentUnqualifiedCountP || 0)
	});

	
	var $y_columns = $(".y_columns .operator_flex").detach();
	$y_columns.html("");

	var pfs = resInfo.productionFeatures;

	var dirxTime = {};

	var elapse = parseInt(((new Date().getTime() + 28800000) % 86400000) / 60000) - 480;

	for (var ipf in pfs) {
		var pf = pfs[ipf];

		var $item = $('<div class="production_feature" d_type="' + pf.d_type + '" style="bottom:' + 
			pf.action_time + 'px;height:' + pf.spare_minutes + 'px;"' + '>' + 
			(pf.finish ? "<div class='count_no'>" + getPositionCountNo(pf.job_no) + "</div>" : "") +
			'</div>');
		var $y_column = $y_columns.children(".y_column[for=" + pf.job_no + "]");
		if ($y_column.length == 0) {
			$y_column = $("<div class=\"y_column\" " + getWorkCountAs(pf.work_count_flg) +
					"for=\"" + pf.job_no + "\"><div class=\"position_intro\">" + pf.operator_name + "</div></div>");
			$y_columns.append($y_column);
		}
		$y_column.append($item);

		if (dirxTime[pf.job_no] == null) {
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
			dirxTime[pf.job_no] = dirxTimeOfJobNo;
		}
		var dirxTimeOfJobNo = dirxTime[pf.job_no];

		var endTime = parseInt(pf.action_time) + parseInt(pf.spare_minutes);

		for (var ix = parseInt(pf.action_time); ix <= endTime; ix++) {
			if (dirxTimeOfJobNo[ix] == 0) {
				dirxTimeOfJobNo[ix] = 1;
			}
			dirxTime[pf.job_no] = dirxTimeOfJobNo;
		}

		if (pf.unknownFrom) {
			var $pitem = $('<div class="production_feature" d_type="0" style="bottom:' + 
				pf.unknownFrom + 'px;height:' + pf.unknownTime + 'px;"></div>');
			$y_column.append($pitem);
		}

	}

	$y_columns.children().each(function(){
		var $y_column = $(this);
		var job_no = $y_column.attr("for");
		var rate = checkDirxTime(dirxTime[job_no]);
		if ($y_column.is("[leader]")) {
			$y_column.attr("work-rate", "管理人员");
		} else if ($y_column.is("[super]")) {
			$y_column.attr("work-rate", "超级员工");
		} else if ($y_column.is(".y_column_sync")) {
			$y_column.attr("work-rate", rate + "%"); // dirxTime[position]
		} else {
			$y_column.attr("work-rate", "WTR " + rate + "%"); // dirxTime[position]
		}
	});

	$(".y_columns").append($y_columns);
		
}

var getWorkCountAs = function(work_count_flg) {
	if (work_count_flg == null || work_count_flg == "null") {
		return "";
	}
	if (work_count_flg == 0 || work_count_flg == 2) {
		return "leader ";
	}
	if (work_count_flg == 4) {
		return "super ";
	}
	return "";
}

var checkDirxTime = function(dirxTime){
	if (!dirxTime) return 0;
	var cnt = 0;
	var length = 0;
	for (var item in dirxTime) {
		if (dirxTime[item] == 0) cnt++;
		if (dirxTime[item] != null) length++;
	}
	return (cnt / length * 100).toFixed(1);
}

var refresh_pls = function(){
	$.ajax({
		beforeSend:ajaxRequestType,
		async:true,
		url:lineP_servicePath+"?method=refresh",
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : refresh_ajaxSuccess
	});
}

$(function(){

	refresh_pls();

	setInterval(refresh_pls, 60000);

});

