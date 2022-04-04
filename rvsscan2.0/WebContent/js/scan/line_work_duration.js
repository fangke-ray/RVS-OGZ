"use strict";

var iamreadyLts = function() {

	var servicePath = "lineWorkDuration.scan";

	var lampAssign = {};
	var lampAssignNo = 1;
	var positionCountNo = {};

	$(".areatitle").hover(function(){
		$("#legend").show();
	}, function(){
		$("#legend").hide();
	});

	var refresh = function() {
		var now = parseInt(((new Date().getTime() + 28800000) % 86400000) / 60000) - 480;
		var $standard_columns = $("#standard_column div").not(".position_intro, #now_period");
		if (now > 0 && now < 690) {
			$("#axis_base #now_period").css("height", now + "px");
		}
		if (now > 570) {
			$("#axis_base").addClass("overwork");
		}

		$standard_columns.each(function(idx, ele) {
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
			data : {line_id : $("#line_id").val()},
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : refreshSuccess
		});

		setTimeout(refresh, 60000);
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

	var refreshSuccess = function(xhrObj) {
		var resInfo = $.parseJSON(xhrObj.responseText);
		if (resInfo.errors && resInfo.errors.length == 0) {
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

			if ($y_columns.children().length >= 12) {
				$y_columns.css({
					"transform": "scaleX(" + (12 / $y_columns.children().length) + ")",
					"marginLeft": "10px"
				});
			} else {
				$y_columns.css({
					"transform": "none",
					"marginLeft" : "0"
				});
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

	refresh();

};

if (typeof(jQuery) === "undefined") {
	loadCss("css/custom.css");
	loadCss("css/olympus/jquery-ui-1.9.1.custom.css", function(){
		loadJs("js/jquery-1.8.2.min.js", function(){
			$("body").addClass("scan1024");
			loadJs("js/jquery-ui-1.9.1.custom.min.js", function(){
				loadJs("js/jquery-plus.js", iamreadyLts);
			});
		});
	});
} else {
	iamreadyLts();
}