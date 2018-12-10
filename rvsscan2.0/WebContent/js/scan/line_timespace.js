"use strict";

var iamreadyLts = function() {

	var servicePath = "lineTimeSpace.scan";

	var lampAssign = {};
	var lampAssignNo = 1;
	var positionCountNo = {};
	var keyBound = {};
	var nowKey = null;
	var rkTo = null;

	var refresh = function() {
		var now = parseInt(((new Date().getTime() + 28800000) % 86400000) / 60000) - 480; // 美德UTC+8
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

	var resetPositionCountNo = function(){
		for (var process_code in positionCountNo) {
			positionCountNo[process_code] = 0;
		}
	}

	var getPositionCountNo = function(process_code){
		if (positionCountNo[process_code]) {
			return ++positionCountNo[process_code];
		} else {
			positionCountNo[process_code] = 1;
			return 1;
		}
	}

	var setAlert = function(key){
		if (typeof(key) != "string") {
			key = $(this).attr("key");
			nowKey = key;
			clearTimeout(rkTo);

			rkTo = setTimeout(refreshKey, 10000);
		}
		$(".production_feature.alerting").removeClass("alerting");
		$(".result_material.alerting").removeClass("alerting");
		$(".production_feature[key=" + key + "]").addClass("pre-alerting");
		$(".result_material[key=" + key + "]").addClass("alerting");
		setTimeout(function(){
			$(".production_feature[key=" + key + "]").removeClass("pre-alerting").addClass("alerting");
		}, 10);
	}

	var refreshSuccess = function(xhrObj) {
		var resInfo = $.parseJSON(xhrObj.responseText);
		if (resInfo.errors && resInfo.errors.length == 0) {
			var $y_columns = $(".y_columns").children().detach();
			$y_columns.not("#standard_column").children().not(".position_intro").remove();

			var pfs = resInfo.productionFeatures;

			for (var ipf in pfs) {
				var pf = pfs[ipf];
				if (pf.LB) {
					var lampKey = pf.material_id + "_" + pf.rework;

					keyBound[lampKey] = 1;

					if (lampAssign[lampKey] == null) {
						lampAssign[lampKey] = lampAssignNo;
						lampAssignNo ++;
						if (lampAssignNo == 8) lampAssignNo = 1;
					}
				}
			}
			resetPositionCountNo();
			var dirxTime = {};

			var elapse = parseInt(((new Date().getTime() + 28800000) % 86400000) / 60000) - 480;
//			if (elapse > 430) {
//				elapse -= 10;
//			} else if ((elapse > 420)){
//				elapse = 420;
//			}
//			
//			if (elapse > 300) {
//				elapse -= 60;
//			} else if ((elapse > 240)){
//				elapse = 240;
//			}
//
//			if (elapse > 130) {
//				elapse -= 10;
//			} else if ((elapse > 120)){
//				elapse = 120;
//			}

			for (var ipf in pfs) {
				var pf = pfs[ipf];

				var lampKey = pf.material_id + "_" + pf.rework;
				var lampNo = null;

				keyBound[lampKey] = 1;

				// if ("421")
				if (lampAssign[lampKey] == null) {
					lampAssign[lampKey] = lampAssignNo;
					lampAssignNo ++;
					if (lampAssignNo == 8) lampAssignNo = 1;
				}
				lampNo = lampAssign[lampKey];

				var $item = $('<div class="production_feature" title="' +
					pf.model_name + '" lamp_no=' + lampNo + ' style="bottom:' + 
					pf.action_time + 'px;height:' + pf.spare_minutes + 'px;"' +
						(pf.overtime ? ' overtime' : '') + " key='" + lampKey + "'" +
					'>' + 
					((pf.finish && pf.process_code == pf.o_process_code) ? "<div class='count_no'>" + getPositionCountNo(pf.process_code) + "</div>" : "") +
					'</div>');
				$y_columns.filter(".y_column[for=" + pf.process_code + "]").append($item);

				if (dirxTime[pf.process_code] == null) {
					var dirxTimeOfprocessCode = {};
					for (var ix = 1; ix <= elapse; ix++) {
						dirxTimeOfprocessCode[ix] = 0;
					}
					if (elapse > 420) {
						for (var ix = 421; ix <= 430; ix++) {
							dirxTimeOfprocessCode[ix] = null;
						}
					}
					
					if (elapse > 240) {
						for (var ix = 241; ix <= 300; ix++) {
							dirxTimeOfprocessCode[ix] = null;
						}
					}
		
					if (elapse > 120) {
						for (var ix = 121; ix <= 130; ix++) {
							dirxTimeOfprocessCode[ix] = null;
						}
					}
					dirxTime[pf.process_code] = dirxTimeOfprocessCode;
				}
				var dirxTimeOfprocessCode = dirxTime[pf.process_code];

				var endTime = parseInt(pf.action_time) + parseInt(pf.spare_minutes);

				for (var ix = parseInt(pf.action_time); ix <= endTime; ix++) {
					if (dirxTimeOfprocessCode[ix] == 0) {
						dirxTimeOfprocessCode[ix] = 1;
					}
					dirxTime[pf.process_code] = dirxTimeOfprocessCode;
				}

				if (pf.pauseFrom) {
					var $pitem = $('<div class="pause_feature" style="bottom:' + 
						pf.pauseFrom + 'px;height:' + pf.pauseTime + 'px;"></div>');
					$y_columns.filter(".y_column[for=" + pf.process_code + "]").append($pitem);
				}

				if (pf.LB) {
					var resBottom = parseInt(pf.action_time) + parseInt(pf.spare_minutes) - 24 - 2;

					var $result = $('<div class="result_material" style="bottom: '+ resBottom +'px;" key="' + lampKey + '"><div class="result_model" lamp_no=' +
							lampNo + '><nobr>' + pf.model_name + '</nobr></div><div class="result_summary" res="' + pf.LBST + '">LB: ' + pf.LB + '% </div><div class="result_summary" res="' + pf.WTRST + '">STR: ' + pf.WTR + '%</div></div>');
					$y_columns.filter(".y_result_column").append($result);
				}
			}

			$y_columns.each(function(){
				var $y_column = $(this);
				var position = $y_column.attr("for");
				var rate = checkDirxTime(dirxTime[position]);
				if ($y_column.is(".y_column_sync")) {
					$y_column.attr("work-rate", rate + "%"); // dirxTime[position]
				} else {
					$y_column.attr("work-rate", "WTR " + rate + "%"); // dirxTime[position]
				}
			});
			if ($.fn.hammer) {
				$y_columns.find(".production_feature").hammer().on("touch", setAlert);
			} else {
				$y_columns.find(".production_feature").click(setAlert);
			}

			$(".y_columns").html($y_columns);
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

	var refreshKey = function(){
		var hit = false;
		var ikey = null;
		for (ikey in keyBound) {
			if (nowKey == null) {
				nowKey = ikey;
				break;
			}
			if (hit) {
				nowKey = ikey;
				break;
			}
			if (nowKey == ikey) {
				hit = true;
				nowKey = null;
			}
		}
		if (nowKey == null) {
			for (ikey in keyBound) {
				nowKey = ikey;
				break;
			}
		}
		if (nowKey != null) {
			setAlert(nowKey);
		}

		rkTo = setTimeout(refreshKey, 2000);
	}

	rkTo = setTimeout(refreshKey, 2000);
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