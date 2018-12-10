var listdata = {};
var servicePath = "lineSituation.scan";
var checked_position_id = "";
var chart2 = null;
var hexaPlan;
var hexaPlanFinish;
var plan_value = 0;
var plan_complete_value = 0;

var now_nogood_listsize = 0;
var now_nogood_currentPos = 0;
var now_nogood_showlistdata = {};

var now_expedited_listsize = 0;
var now_expedited_currentPos = 0;
var now_expedited_showlistdata = {};

var today_plan_outline_listsize = 0;
var today_plan_outline_currentPos = 0;
var today_plan_outline_showlistdata = {};

var other_line_closer_listsize = 0;
var other_line_closer_currentPos = 0;
var other_line_closer_showlistdata = {};

var getfree = true;

var time_archer = (new Date()).getTime();

var jsinit_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {

			$("#sikake").text(resInfo.sikake); // TODO real sikake

			if (chart2 == null) {
			chart2 = new Highcharts.Chart({
				colors : ['#cc76cc', '#92D050', '#7faad4'],
				chart : {
					renderTo : 'processing_container',
					type : 'bar',
					marginRight : 10,
					backgroundColor : "white",
					height : 427,
					width : 486
				},
				credits : {
					enabled : false
				},
				title : {
					text : ''
				},
				xAxis : {
					categories : resInfo.categories,
					labels : {
						style : {
							fontWeight : 'bold',
							fontSize : '13px'
						}
					}
				},
				yAxis : {
					title : {
						text : '台数',
						style : {
							fontWeight : 'bold',
							fontSize : '16px'
						}
					},
					allowDecimals: false
				},
				tooltip : {
					animation : false,
					formatter : function() {
						return '<b>' + this.series.name + '</b><br/>' + this.y + '台<br/>';
					},
					style : {
						fontWeight : 'bold',
						fontSize : '12px'
		
					},
					labels : {
						style : {
							fontWeight : 'bold',
							fontSize : '18px'
						}
					}
				},
				plotOptions : {
					series : {
						point : {
							events : {
							}
						}
					},
					area : {
						lineWidth : 0,
						marker : {
							enabled : false
						},
						dataLabels : {
							enabled: true,
							color : 'black',
							formatter : function() {
								if ((this.x - .25) === parseInt(this.x)) {
									return this.y;
								} else {
									return '';
								}
							},
							y : 10
						},
						pointInterval : .25,
						pointStart : -.25
					},
					bar : {
						dataLabels : {
							enabled: true,
							color : 'green'
						},
						pointWidth : 8,
						stacking : 'normal'
					}
				},
				legend : {
					enabled : true,
					floating : true,
					x : -110,
					y : 10
				},
				exporting : {
					enabled : false
				},
				series : [{
					type : 'bar',
					name : '小修理台数',
					data : resInfo.light_fix_counts,
					zIndex : 2
				},{
					type : 'bar',
					name : '大修理台数',
					data : resInfo.counts,
					zIndex : 2
				},{
					type : 'area',
					name : '警戒线',
					data : resInfo.overlines
				}]
			});
			} else {
				chart2.xAxis[0].setCategories(resInfo.categories, false);
				chart2.series[0].setData(resInfo.light_fix_counts, false);
				chart2.series[1].setData(resInfo.counts, false);
				chart2.series[2].setData(resInfo.overlines);
			}
		}

		$("tspan").hover(
			function() {
				var jthis = $(this);
				jthis.parent().css("color", "navy");
			}
		);

		now_expedited_showlistdata = resInfo.now_expedited;
		now_expedited_listsize = now_expedited_showlistdata.length;
		$("#now_expedited .areacount").text("总数"+now_expedited_listsize+"台");

		if (now_expedited_currentPos >= now_expedited_listsize) {
			now_expedited_currentPos = 0;
		}
	
		if (now_expedited_listsize > 0) {
			var now_expedited_show = now_expedited_showlistdata[now_expedited_currentPos];
			$("#now_expedited table td:eq(0) label").hide("fade", function(){
				var level = now_expedited_show.level;
				$(this).html("<span class='sorc_no'>" + now_expedited_show.sorc_no + "</span>  " 
					+ (now_expedited_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
					+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : ""));
			});
			$("#now_expedited table td:eq(1) label").hide("fade", function(){$(this).text(now_expedited_show.model_name)});
			$("#now_expedited table td:eq(2) label").hide("fade", function(){$(this).text(now_expedited_show.serial_no);
				$("#now_expedited table td:eq(0) label").show("fade");
				$("#now_expedited table td:eq(1) label").show("fade");
				$("#now_expedited table td:eq(2) label").show("fade");

				var tr2_pos = now_expedited_currentPos;
				if (tr2_pos >= now_expedited_listsize) tr2_pos -= now_expedited_listsize;
				if (now_expedited_listsize == 1) {$("#now_expedited table td:gt(2) label").text(""); return};
				now_expedited_show = now_expedited_showlistdata[tr2_pos];
				$("#now_expedited table td:eq(3) label").hide("fade", function(){
					var level = now_expedited_show.level;
					$(this).html("<span class='sorc_no'>" + now_expedited_show.sorc_no + "</span>  " 
						+ (now_expedited_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
						+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : ""));
				});
				$("#now_expedited table td:eq(4) label").hide("fade", function(){$(this).text(now_expedited_show.model_name)});
				$("#now_expedited table td:eq(5) label").hide("fade", function(){$(this).text(now_expedited_show.serial_no);
					$("#now_expedited table td:eq(3) label").show("fade");
					$("#now_expedited table td:eq(4) label").show("fade");
					$("#now_expedited table td:eq(5) label").show("fade");
					var tr3_pos = now_expedited_currentPos + 1;
					if (tr3_pos >= now_expedited_listsize) tr3_pos -= now_expedited_listsize;
					if (now_expedited_listsize == 2) {$("#now_expedited table td:gt(5) label").text(""); return};
					now_expedited_show = now_expedited_showlistdata[tr3_pos];
					$("#now_expedited table td:eq(6) label").hide("fade", function(){
						var level = now_expedited_show.level;
						$(this).html("<span class='sorc_no'>" + now_expedited_show.sorc_no + "</span>  " 
							+ (now_expedited_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
							+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : "")).show("fade");
					});
					$("#now_expedited table td:eq(7) label").hide("fade", function(){$(this).text(now_expedited_show.model_name).show("fade")});
					$("#now_expedited table td:eq(8) label").hide("fade", function(){$(this).text(now_expedited_show.serial_no).show("fade")});
				})})
			now_expedited_currentPos++;
		} else {
			$("#now_expedited table label").hide();
		}

		now_nogood_showlistdata = resInfo.now_nogood;
		now_nogood_listsize = now_nogood_showlistdata.length;
		$("#now_nogood .areacount").text("总数"+now_nogood_listsize+"台");

		if (now_nogood_currentPos >= now_nogood_listsize) {
			now_nogood_currentPos = 0;
		}
	
		if (now_nogood_listsize > 0) {
			var now_nogood_show = now_nogood_showlistdata[now_nogood_currentPos];
			$("#now_nogood table td:eq(0) label").hide("fade", function(){
				var level = now_nogood_show.level;
				$(this).html("<span class='sorc_no'>" + now_nogood_show.sorc_no + "</span>  " 
					+ (now_nogood_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
					+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : ""));
			});
			$("#now_nogood table td:eq(1) label").hide("fade", function(){$(this).text(now_nogood_show.model_name)});
			$("#now_nogood table td:eq(2) label").hide("fade", function(){$(this).text(now_nogood_show.serial_no);
				$("#now_nogood table td:eq(0) label").show("fade");
				$("#now_nogood table td:eq(1) label").show("fade");
				$("#now_nogood table td:eq(2) label").show("fade");

				if (now_nogood_listsize == 1) {$("#now_nogood table td:gt(2) label").text(""); return};
				var tr2_pos = now_nogood_currentPos;
				if (tr2_pos >= now_nogood_listsize) tr2_pos -= now_nogood_listsize;

				now_nogood_show = now_nogood_showlistdata[tr2_pos];
				$("#now_nogood table td:eq(3) label").hide("fade", function(){
					var level = now_nogood_show.level;
					$(this).html("<span class='sorc_no'>" + now_nogood_show.sorc_no + "</span>  " 
						+ (now_nogood_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
						+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : ""));
				});
				$("#now_nogood table td:eq(4) label").hide("fade", function(){$(this).text(now_nogood_show.model_name)});
				$("#now_nogood table td:eq(5) label").hide("fade", function(){$(this).text(now_nogood_show.serial_no);
					$("#now_nogood table td:eq(3) label").show("fade");
					$("#now_nogood table td:eq(4) label").show("fade");
					$("#now_nogood table td:eq(5) label").show("fade");

					if (now_nogood_listsize == 2) {$("#now_nogood table td:gt(5) label").text(""); return};
					var tr3_pos = now_nogood_currentPos + 1;
					if (tr3_pos >= now_nogood_listsize) tr3_pos -= now_nogood_listsize;
					now_nogood_show = now_nogood_showlistdata[tr3_pos];
					$("#now_nogood table td:eq(6) label").hide("fade", function(){
						var level = now_nogood_show.level;
						$(this).html("<span class='sorc_no'>" + now_nogood_show.sorc_no + "</span>  " 
							+ (now_nogood_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
							+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : "")).show("fade");
					});
					$("#now_nogood table td:eq(7) label").hide("fade", function(){$(this).text(now_nogood_show.model_name).show("fade")});
					$("#now_nogood table td:eq(8) label").hide("fade", function(){$(this).text(now_nogood_show.serial_no).show("fade")});
				})})
			now_nogood_currentPos++;
		} else {
			$("#now_nogood table label").hide();
		}

		today_plan_outline_showlistdata = resInfo.today_plan_outline;
		today_plan_outline_listsize = today_plan_outline_showlistdata.length;
		$("#today_plan_outline .areacount").text("总数"+today_plan_outline_listsize+"台");

		if (today_plan_outline_currentPos >= today_plan_outline_listsize) {
			today_plan_outline_currentPos = 0;
		}
	
		if (today_plan_outline_listsize > 0) {
			var today_plan_outline_show = today_plan_outline_showlistdata[today_plan_outline_currentPos];
			$("#today_plan_outline table td:eq(0) label").hide("fade", function(){
				var level = today_plan_outline_show.level;
				$(this).html("<span class='sorc_no'>" + today_plan_outline_show.sorc_no + "</span>  " 
					+ (today_plan_outline_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
					+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : ""));
			});
			$("#today_plan_outline table td:eq(1) label").hide("fade", function(){$(this).text(today_plan_outline_show.model_name)});
			$("#today_plan_outline table td:eq(2) label").hide("fade", function(){$(this).text(today_plan_outline_show.serial_no);
				$("#today_plan_outline table td:eq(0) label").show("fade");
				$("#today_plan_outline table td:eq(1) label").show("fade");
				$("#today_plan_outline table td:eq(2) label").show("fade");

				if (today_plan_outline_listsize == 1) {$("#today_plan_outline table td:gt(2) label").text(""); return};
				var tr2_pos = today_plan_outline_currentPos;
				if (tr2_pos >= today_plan_outline_listsize) tr2_pos -= today_plan_outline_listsize;

				today_plan_outline_show = today_plan_outline_showlistdata[tr2_pos];
				$("#today_plan_outline table td:eq(3) label").hide("fade", function(){
					var level = today_plan_outline_show.level;
					$(this).html("<span class='sorc_no'>" + today_plan_outline_show.sorc_no + "</span>  " 
						+ (today_plan_outline_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
						+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : ""));
				});
				$("#today_plan_outline table td:eq(4) label").hide("fade", function(){$(this).text(today_plan_outline_show.model_name)});
				$("#today_plan_outline table td:eq(5) label").hide("fade", function(){$(this).text(today_plan_outline_show.serial_no);
					$("#today_plan_outline table td:eq(3) label").show("fade");
					$("#today_plan_outline table td:eq(4) label").show("fade");
					$("#today_plan_outline table td:eq(5) label").show("fade");

					if (today_plan_outline_listsize == 2) {$("#today_plan_outline table td:gt(5) label").text(""); return};
					var tr3_pos = today_plan_outline_currentPos + 1;
					if (tr3_pos >= today_plan_outline_listsize) tr3_pos -= today_plan_outline_listsize;

					today_plan_outline_show = today_plan_outline_showlistdata[tr3_pos];
					$("#today_plan_outline table td:eq(6) label").hide("fade", function(){
						var level = today_plan_outline_show.level;
						$(this).html("<span class='sorc_no'>" + today_plan_outline_show.sorc_no + "</span>  " 
							+ (today_plan_outline_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
							+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : "")).show("fade");
					});
					$("#today_plan_outline table td:eq(7) label").hide("fade", function(){$(this).text(today_plan_outline_show.model_name).show("fade")});
					$("#today_plan_outline table td:eq(8) label").hide("fade", function(){$(this).text(today_plan_outline_show.serial_no).show("fade")});
				})})
			today_plan_outline_currentPos++;
		} else {
			$("#today_plan_outline table label").hide();
		}

		other_line_closer_showlistdata = resInfo.other_line_closer;
		if (other_line_closer_showlistdata) {
			other_line_closer_listsize = other_line_closer_showlistdata.length;
			$("#other_line_closer .areacount").text("总数"+other_line_closer_listsize+"台");
	
			if (other_line_closer_currentPos >= other_line_closer_listsize) {
				other_line_closer_currentPos = 0;
			}
		
			if (other_line_closer_listsize > 0) {
				var other_line_closer_show = other_line_closer_showlistdata[other_line_closer_currentPos];
				$("#other_line_closer table td:eq(0) label").hide("fade", function(){
					var level = other_line_closer_show.level;
					$(this).html("<span class='sorc_no'>" + other_line_closer_show.sorc_no + "</span>  " 
						+ (other_line_closer_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
						+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : ""));
				});
				$("#other_line_closer table td:eq(1) label").hide("fade", function(){$(this).text(other_line_closer_show.model_name)});
				$("#other_line_closer table td:eq(2) label").hide("fade", function(){$(this).text(other_line_closer_show.serial_no);
					$("#other_line_closer table td:eq(0) label").show("fade");
					$("#other_line_closer table td:eq(1) label").show("fade");
					$("#other_line_closer table td:eq(2) label").show("fade");
	
					if (other_line_closer_listsize == 1) {$("#other_line_closer table td:gt(2) label").text(""); return};
					var tr2_pos = other_line_closer_currentPos;
					if (tr2_pos >= other_line_closer_listsize) tr2_pos -= other_line_closer_listsize;

					other_line_closer_show = other_line_closer_showlistdata[tr2_pos];
					$("#other_line_closer table td:eq(3) label").hide("fade", function(){
						var level = other_line_closer_show.level;
						$(this).html("<span class='sorc_no'>" + other_line_closer_show.sorc_no + "</span>  " 
							+ (other_line_closer_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
							+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : ""));
					});
					$("#other_line_closer table td:eq(4) label").hide("fade", function(){$(this).text(other_line_closer_show.model_name)});
					$("#other_line_closer table td:eq(5) label").hide("fade", function(){$(this).text(other_line_closer_show.serial_no);
						$("#other_line_closer table td:eq(3) label").show("fade");
						$("#other_line_closer table td:eq(4) label").show("fade");
						$("#other_line_closer table td:eq(5) label").show("fade");

						if (other_line_closer_listsize == 2) return;
						var tr3_pos = other_line_closer_currentPos + 1;
						if (tr3_pos >= other_line_closer_listsize) tr3_pos -= other_line_closer_listsize;
						if (tr3_pos >= other_line_closer_listsize - 1) {$("#other_line_closer table td:gt(5) label").text(""); return};
						other_line_closer_show = other_line_closer_showlistdata[tr3_pos];
						$("#other_line_closer table td:eq(6) label").hide("fade", function(){
							var level = other_line_closer_show.level;
							$(this).html("<span class='sorc_no'>" + other_line_closer_show.sorc_no + "</span>  " 
								+ (other_line_closer_show.direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
								+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : "")).show("fade");
						});
						$("#other_line_closer table td:eq(7) label").hide("fade", function(){$(this).text(other_line_closer_show.model_name).show("fade")});
						$("#other_line_closer table td:eq(8) label").hide("fade", function(){$(this).text(other_line_closer_show.serial_no).show("fade")});
					})})
				other_line_closer_currentPos++;
			} else {
				$("#other_line_closer table label").hide();
			}
		}

		if ($(".storagepiece").length > 0) {
			var matchTableCells = $(".storagepiece table:eq(1) td");
			matchTableCells.text("　");
			for (var ii = 0 ;ii < resInfo.com_ns_matches.length;ii++) {
				var level = resInfo.com_ns_matches[ii].level;
				matchTableCells.eq(ii * 3).html("<span class='sorc_no'>" + resInfo.com_ns_matches[ii].sorc_no + "</span>  " 
					+ (resInfo.com_ns_matches[ii].direct_flg == 1 ? " <span class='direct_flg'>直</span> " : "")
					+ (level == 9 || level == 91 || level == 92 || level == 93 ? " <span class='small_flg'>小</span> " : ""));
				matchTableCells.eq(ii * 3 + 1).text(resInfo.com_ns_matches[ii].model_name);
				matchTableCells.eq(ii * 3 + 2).text(resInfo.com_ns_matches[ii].process_code);
			}
		}

		if ($("#plan_count").length > 0) {
 		$("#plan_count").flipCounter(
       	"startAnimation", // scroll counter from the current number to the specified number
        {
            number: plan_value, // the number we want to scroll from
            end_number: resInfo.plan, // the number we want the counter to scroll to
            duration: 1000 // number of ms animation should take to complete
        });

        plan_value = resInfo.plan;

      	$("#plan_finish_count").flipCounter(
       	"startAnimation", // scroll counter from the current number to the specified number
        {
            number: plan_complete_value, // the number we want to scroll from
            end_number: resInfo.plan_complete, // the number we want the counter to scroll to
            duration: 1000 // number of ms animation should take to complete
        });

        plan_complete_value = resInfo.plan_complete;

        var com_rate = 0;
        if (plan_value > 0) {
        	com_rate = Math.floor(plan_complete_value / plan_value * 100);
        }
		$('.donut-arrow').trigger('updatePercentage', com_rate);
		$("#completed_rate").text(com_rate.toFixed(0) + "%");
		}

//	    var settings = {
//	        size: 150,
//	        margin: 'auto',
//	        fontSize: 100,
//	        perspective: 450,
//	        backgroundColor: '#e67e22'
//	    }
//		if (plan_value < resInfo.plan) {
//			for (var ii=plan_value ; ii <= resInfo.plan; ii++) {
//				range.push(""+ii);
//			}
//			$("#plan_count").html("");
//			hexaPlan = new HexaFlip($("#plan_count")[0], {sett: range}, settings);
//			plan_value = resInfo.plan;
//			hexaPlan.flip();
//			hexaPlan.setValue({sett: ""+plan_value});
//			
//		} else if (plan_value > resInfo.plan) {
//			for (var ii=plan_value ; ii >= resInfo.plan; ii--) {
//				range.push(""+ii);
//			}
//			$("#plan_count").html("");
//			hexaPlan = new HexaFlip($("#plan_count")[0], {sett: range}, settings);
//			plan_value = resInfo.plan;
//			hexaPlan.flip();
//			hexaPlan.setValue({sett: ""+plan_value});
//		}
//
//		range = [];
//		if (plan_complete_value < resInfo.plan_complete) {
//			for (var ii=plan_complete_value ; ii <= resInfo.plan_complete; ii++) {
//				range.push(""+ii);
//			}
//			$("#plan_finish_count").html("");
//			hexaPlanFinish = new HexaFlip($("#plan_finish_count")[0], {sed: range}, settings);
//			plan_complete_value = resInfo.plan_complete;
//			hexaPlanFinish.setValue(range);
//		} else if (plan_complete_value > resInfo.plan_complete) {
//			for (var ii=plan_complete_value ; ii >= resInfo.plan_complete; ii--) {
//				range.push(""+ii);
//			}
//			$("#plan_finish_count").html("");
//			hexaPlanFinish = new HexaFlip($("#plan_finish_count")[0], {sed: range}, settings);
//			plan_complete_value = resInfo.plan_complete;
//			hexaPlanFinish.setValue(range);
//		}
	} catch (e) {
	};
	getfree = true;
};

$(document).ready(function() {
	if ($("#page_line_id").val() == "00000000012") servicePath = "lineSituationC1.scan";
	if ($("#page_line_id").val() == "00000000013") servicePath = "lineSituationN1.scan"; // TODO

	var data = {
		line_id : $("#page_line_id").val(), section_id : $("#page_section_id").val()
	}

//    var settings = {
//        size: 150,
//        margin: 'auto',
//        fontSize: 100,
//        perspective: 450,
//        backgroundColor: '#e67e22'
//    }

	if ($("#plan_count").length > 0) {
	$("#plan_count").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	$("#plan_finish_count").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	}

    // Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true, // false
		url : servicePath + '?method=refresh',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : jsinit_ajaxSuccess
	});

	setInterval(refresh, 20000);

	if ($(".storagepiece").length > 0) {
		// setInterval(movepiece, 30000); // 120000
	}

});

var movepiece = function() {
	if ("-604px" == $(".storagepiece").css("top"))
		$(".storagepiece").animate({top : 0},750);
	else
		$(".storagepiece").animate({top : -604},750);
}

var refresh = function() {
	var data = {
		line_id : $("#page_line_id").val(), section_id : $("#page_section_id").val(), position_id : checked_position_id
	}

	if ((new Date()).getTime() - time_archer > 28800000) {
		window.location.reload();
		return;
	}

	if (getfree) {
		getfree = false;

		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : servicePath + '?method=refresh&w=' + (new Date()).getTime(),
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : jsinit_ajaxSuccess
		});
	}
}