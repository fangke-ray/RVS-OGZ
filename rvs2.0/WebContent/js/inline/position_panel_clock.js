"use strict"
var posClockObj = (function() {




// 已启动作业时间
var p_time = 0;
// 定时处理对象
var oInterval, ttInterval;
// 定时处理间隔（1分钟）
var iInterval = 60000;
// 取到的标准作业时间
var leagal_overline;
var t_operator_cost = 0;
var t_run_cost = 0;

var $material_detail_action = null;
var $material_detail_standard = null;
var $material_detail_spend = null;
var $material_detail_spend_lbl = null;
var $p_rate = null;

// 进行中效果
var _ctime=function(){
	p_time++;
	$material_detail_spend_lbl.text(_minuteFormat(p_time) + ":");

	var rate = parseInt((p_time + 1) / leagal_overline * 100);

	if (rate >= 100) rate = 99;

	t_operator_cost++;

	var $liquid = $p_rate.find("div");

	$(".roll_cell > .anim_act").removeClass("anim_act").addClass("anim_act");

	if (rate == 99) {
		$liquid.addClass("tube-orange");
		return;
	}

	$liquid.animate({width : rate + "%"}, iInterval, "linear");

	dyeLiquid(rate, $liquid);
};

var dyeLiquid = function(rate, $liquid) {
	if (rate > 80) {
		$liquid.removeClass("tube-green");
		if (rate > 95) {
			$liquid.removeClass("tube-yellow");
			$liquid.addClass("tube-orange");
		} else {
			$liquid.addClass("tube-yellow");
		}
	} else {
		$liquid.addClass("tube-green");
	}
}

// 进行中效果
var _ttime=function(){
	$("#p_run_cost").text(_minuteFormat(t_run_cost));
	t_run_cost++;
};

var _minuteFormat = function(iminute) {
	if (!iminute && iminute != 0) return "-";
	var hours = parseInt(iminute / 60);
	var minutes = iminute % 60;

	return fillZero(hours, 2) + ":" + fillZero(minutes, 2);
}

var _secondFormat = function(fminute) {
	if (!fminute && fminute != 0) return "-";
	var iminute = parseInt(fminute);
	var hours = parseInt(fminute / 60);
	var minutes = iminute % 60;
	var seconds = parseInt((fminute - iminute) * 60);

	return fillZero(hours, 2) + ":" + fillZero(minutes, 2) + ":" + fillZero(seconds, 2);
}

var _convertMinute =function(sminute) {
	var hours = sminute.replace(/(.*):(.*)/, "$1");
	var minutes = sminute.replace(/(.*):(.*)/, "$2");

	return hours * 60 + parseInt(minutes);
}


return {
	/** sample : posClockObj.init($("#material_details td:eq(7)"), $("#material_details td:eq(9)"), $("#dtl_process_time"), $("#p_rate")); */
	init : function($action_container, $standard_container, $spend_container, $rate_viewer) {
		$material_detail_action = $action_container;
		$material_detail_standard = $standard_container;
		$material_detail_spend = $spend_container;
		$material_detail_spend_lbl = $material_detail_spend.find("label");
		$p_rate = $rate_viewer;
	},
	setAction : function(action_time){
		if (action_time) {
			$material_detail_action.text(action_time);
		} else {
			var thistime=new Date();
			var hours=thistime.getHours();
			var minutes=thistime.getMinutes();
			var seconds=thistime.getSeconds();

			$material_detail_action.text(fillZero(hours, 2) + ":" + fillZero(minutes, 2) + ":" + fillZero(seconds, 2));
		}
	},
	setLeagalAndSpent : function(param_leagal_overline, spent_mins, spent_secs) {
		leagal_overline = param_leagal_overline;

		$material_detail_standard.text(_secondFormat(leagal_overline));

		if (!spent_mins) {
			spent_mins = _convertMinute($material_detail_spend_lbl.text()) || 0;
			spent_secs = spent_mins * 60;
		} else {
			$material_detail_spend_lbl.text(_minuteFormat(spent_mins) + ":");
			$(".roll_cell > *").addClass("anim_pause");
		}

		var frate = 0;
		if (spent_secs) {
			frate = parseInt(spent_secs / leagal_overline / 0.6); // /60 * 100
		} else {
			frate = parseInt(spent_mins / leagal_overline * 100);
		}

		if (frate > 99) {
			frate = 99;
		}
		$p_rate.html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
		dyeLiquid(frate, $p_rate.find("div"));
	},
	pauseClock : function(){
		clearInterval(oInterval);
		$(".roll_cell > *").addClass("anim_pause");
	},
	startClock : function(spent_mins, spent_secs){
		p_time = (spent_mins || 0);

		spent_secs = spent_secs % 60;
		var remain_secs = 0;
		if (spent_secs == 0) {
			p_time--;
		} else {
			remain_secs = 60 - spent_secs;
		}

		if (spent_secs == 0) {
			$(".roll_cell > *").removeClass("anim_pause").addClass("anim_act");
			_ctime();
			oInterval = setInterval(_ctime, iInterval);
		} else {
			setTimeout(function(){
				console.log("waiting " + remain_secs + " and run!");
				$(".roll_cell > *").removeClass("anim_pause").addClass("anim_act");
				_ctime();
				oInterval = setInterval(_ctime, iInterval);
			}, remain_secs * 1000);
		}
	},
	stopClock : function(){
		$material_detail_action.text("");
		$material_detail_spend_lbl.text("");
		$p_rate.html("");
		p_time = 0;
		clearInterval(oInterval);
		$(".roll_cell > *").removeClass("anim_pause").removeClass("anim_act");
	},
	initTopClock : function() {
		// 计算当前用时
		var p_operator_cost = $("#p_operator_cost").text();
		if (p_operator_cost.indexOf(':') < 0) {
			t_operator_cost = p_operator_cost;
			$("#p_operator_cost").text(_minuteFormat(t_operator_cost));
		}

		// 计算总用时
		var p_run_cost = $("#p_run_cost").text();
		if (p_run_cost.indexOf(':') < 0) {
			if (p_run_cost != "0" && p_run_cost != "") {
				t_run_cost = p_run_cost;
				$("#p_run_cost").text(_minuteFormat(t_run_cost));
				ttInterval = setInterval(_ttime, iInterval);
			}
		}
	},
	recountTopClock : function() {
		var p_operator_cost = $("#p_operator_cost").text();
		t_operator_cost = _convertMinute(p_operator_cost);// + spent_mins;
	}
}




/**************


var treatUsesnout = function(xhrobj) {
	if (resInfo.errors.length > 0) {
	} else {
			if (resInfo.leagal_overline) {

				posClockObj.setLeagalAndSpent(resInfo.leagal_overline);

//				leagal_overline = resInfo.leagal_overline;
//				$material_detail_standard.text(minuteFormat(leagal_overline)); //  + (leagal_overline ? ":00" : "")

//				var nspent_mins = _convertMinute($material_detail_spend_lbl.text());
//				var frate = parseInt(nspent_mins / leagal_overline * 100);
//				if (frate > 99) {
//					frate = 99;
//				}
//				$p_rate.html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
			}
	}
}

var treatPause = function(resInfo) {

	if (resInfo) {

		posClockObj.setAction(resInfo.action_time);

//		if (resInfo.action_time) {
//			$material_detail_action.text(resInfo.action_time);
//		} else {
//			var thistime=new Date();
//			var hours=thistime.getHours();
//			var minutes=thistime.getMinutes();

//			$material_detail_action.text(fillZero(hours, 2) + ":" + fillZero(minutes, 2));
//		}


		posClockObj.setLeagalAndSpent(resInfo.leagal_overline, resInfo.spent_mins);

//		$material_detail_standard.text(minuteFormat(resInfo.leagal_overline)); //  + (leagal_overline ? ":00" : "")
//		leagal_overline = resInfo.leagal_overline;

//		$material_detail_spend_lbl.text(minuteFormat(resInfo.spent_mins));
//		var frate = parseInt((resInfo.spent_mins) / leagal_overline * 100);
//		if (frate > 99) {
//			frate = 99;
//		}
//		$p_rate.html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");
	}

	posClockObj.pauseClock();
//	clearInterval(oInterval);
}

var treatStart = function(resInfo) {

//	if (resInfo.action_time) {
//		$material_detail_action.text(resInfo.action_time);
//	} else {
//		var thistime=new Date();
//		var hours=thistime.getHours();
//		var minutes=thistime.getMinutes();

//		$material_detail_action.text(fillZero(hours, 2) + ":" + fillZero(minutes, 2));
//	}

//	$material_detail_standard.text(minuteFormat(resInfo.leagal_overline)); //  + (leagal_overline ? ":00" : "")
//	leagal_overline = resInfo.leagal_overline;

//	$material_detail_spend_lbl.text(minuteFormat(resInfo.spent_mins));
//	var frate = parseInt((resInfo.spent_mins) / leagal_overline * 100);
//	if (frate > 99) {
//		frate = 99;
//	}
//	$p_rate.html("<div class='tube-liquid tube-green' style='width:"+ frate +"%;text-align:right;'></div>");


	posClockObj.startClock(resInfo.spent_mins);

//	p_time = resInfo.spent_mins - 1;
//	ctime();
//	oInterval = setInterval(ctime,iInterval);

	posClockObj.recountTopClock();

//	var p_operator_cost = $("#p_operator_cost").text();
//	t_operator_cost = convertMinute(p_operator_cost);// + resInfo.spent_mins;

};


var doInit_ajaxSuccess = function(xhrobj, textStatus){
		if (resInfo.errors.length > 0) {
		} else {

			posClockObj.initTopClock();


			// 计算当前用时
			var p_operator_cost = $("#p_operator_cost").text();
			if (p_operator_cost.indexOf(':') < 0) {
				t_operator_cost = p_operator_cost;
				$("#p_operator_cost").text(minuteFormat(t_operator_cost));
			}

			// 计算总用时
			var p_run_cost = $("#p_run_cost").text();
			if (p_run_cost.indexOf(':') < 0) {
				if (p_run_cost != "0" && p_run_cost != "") {
					t_run_cost = p_run_cost;
					$("#p_run_cost").text(minuteFormat(t_run_cost));
					ttInterval = setInterval(_ttime, iInterval);
				}
			}

		}
};

var doFinish_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
		} else {

				posClockObj.stopClock();

//				$material_detail_action.text("");
//				$material_detail_spend_lbl.text("");
//				$p_rate.html("");
//				p_time = 0;
//				clearInterval(oInterval);

			}
		}
	} catch (e) {
	};
}

*********/



})();