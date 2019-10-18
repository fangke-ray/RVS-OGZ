var listdata = {};
var servicePath = "lineSituationP1.scan";
var position_counts = {};
var checked_position_id = "";
var chart2 = null;
var chart3 = null;
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

			position_counts = resInfo.counts;

			$("#sikake").text(resInfo.sikake); // TODO real sikake

			if (resInfo.sikake_in || resInfo.sikake_in == 0) {
				$("#sikake_in").text("（其中CCD盖玻璃更换与LG玻璃更换 " + resInfo.sikake_in + " 台)");
			}

			if (chart2 == null) {
			chart2 = new Highcharts.Chart({
				colors : ['#92D050', '#7faad4'],
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
							fontSize : '15px'
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
					name : '修理台数',
					data : resInfo.counts,
					zIndex : 2
				}, {
					type : 'area',
					name : '警戒线',
					data : resInfo.overlines
				}]
			});
			} else {
				chart2.xAxis[0].setCategories(resInfo.categories, false);
				// chart2.series[0].setData(resInfo.light_fix_counts, false);
				chart2.series[0].setData(resInfo.counts, false);
				chart2.series[1].setData(resInfo.overlines);
			}
		}

if (chart3== null) {
chart3 = new Highcharts.Chart({
colors : ['rgba(146, 208, 80,0.8)', 'rgba(0, 128, 192,0.65)' ],
chart : {
	renderTo : 'step_process',
	type : 'area',
	marginRight : 10,
	backgroundColor : {
		linearGradient : [ 0, 0, 0, 0 ],
		stops : [
				[ 0, 'rgba(0, 0, 0, 0)' ],
				[ 1, 'rgba(0, 0, 0, 0)' ] ]
		},
	events : {
		load : function() {
		}
	}
},
credits : {
	enabled : false
},
title : {
	text : ''
},
xAxis : {
	categories : [ '','','','08:00<br>~<br>10:00','','','','','','10:00<br>~<br>12:00','','','','','','13:00<br>~<br>15:00','','','','','','15:00<br>~<br>17:15','','',''
],
	labels : {
		style : {
			fontWeight : 'bold',
			fontSize : '14px',
			color : 'black'
		},
		y:26
	}
},
yAxis : {
	tickInterval:1,
	title : {
		text : '台数',
		style : {
			fontWeight : 'bold',
			fontSize : '16px'
		}
	}
},
tooltip : {
	animation : false,
	formatter : function() {
		return '<b>' + this.series.name
				+ '</b><br/>' + this.y
				+ '台<br/>';
	},
	style : {
		fontWeight : 'bold',
		fontSize : '12px'
	},
	labels : {
		style : {
			fontWeight : 'bold',
			fontSize : '16px'
		}
	}
},
plotOptions : {
	series : {
		animation: {
			duration: 300
		},
		cursor : 'pointer',
		point : {
			events : {
				click : function() {
				}
			}
		}
	},
	area : {
		borderWidth :0,
		marker : {
			enabled : false,
			states: {
				hover: {
					enabled: false
				}
			}
		}
	}
},
legend : {
	enabled : true,floating : true,
			align: 'left',
		valign:'top',	y : -166,
			x : 60

},
exporting : {
	enabled : false
},
series : [ {
	name : '计划台数',
	data : [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null]
},{
	name : '产出台数',
	data : [null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null]
} ]
			});
			} else {
				var plans = resInfo.plans;
				var outs = resInfo.outs;
				var now_period = resInfo.now_period;
				chart3.series[0].setData(plans, false);
				chart3.series[1].setData(outs);
				var tlt_plan = 0;
				var tlt_out = 0;

				var $today_plan_outline = $("#today_plan_outline tbody");
				var $today_plan_outline_tr = $today_plan_outline.find("tr:eq(0)");
				var plan = plans[2];
				tlt_plan += plan;
				var out = outs[2];
				tlt_out += out;
				if (plan>0) {
					$today_plan_outline_tr.find("td:eq(1)").text(plan);
					var rate = parseInt(out * 100 / plan);
					$today_plan_outline_tr.find("td:eq(3)").text(rate + " %");
					if (rate<100 && now_period > 1) $today_plan_outline_tr.find("td:eq(3)").addClass("low");
				}
				if (tlt_plan>0) {
					var rate = parseInt(tlt_out * 100 / tlt_plan);
					$today_plan_outline_tr.find("td:eq(4)").text(rate + " %");
					if (rate<100 && now_period > 1) $today_plan_outline_tr.find("td:eq(4)").addClass("low");
				}
				$today_plan_outline_tr.find("td:eq(2)").text(out);
				if (now_period > 1) $today_plan_outline_tr.css("background-color","lightgray");
				if (now_period == 1) $today_plan_outline_tr.css("background-color","lightblue");

				$today_plan_outline_tr = $today_plan_outline.find("tr:eq(1)");
				plan = plans[8];
				out = outs[8];
				tlt_plan += plan;
				tlt_out += out;
				if (plan>0) {
					$today_plan_outline_tr.find("td:eq(1)").text(plan);
					var rate = parseInt(out * 100 / plan);
					$today_plan_outline_tr.find("td:eq(3)").text(rate + " %");
					if (rate<100 && now_period > 2) $today_plan_outline_tr.find("td:eq(3)").addClass("low");
				}
				if (tlt_plan>0 && now_period >= 2) {
					var rate = parseInt(tlt_out * 100 / tlt_plan);
					$today_plan_outline_tr.find("td:eq(4)").text(rate + " %");
					if (rate<100 && now_period > 2) $today_plan_outline_tr.find("td:eq(4)").addClass("low");
				}
				$today_plan_outline_tr.find("td:eq(2)").text(out);
				if (now_period > 2) $today_plan_outline_tr.css("background-color","lightgray");
				if (now_period == 2) $today_plan_outline_tr.css("background-color","lightblue");

				$today_plan_outline_tr = $today_plan_outline.find("tr:eq(2)");
				plan = plans[14];
				out = outs[14];
				tlt_plan += plan;
				tlt_out += out;
				if (plan>0) {
					$today_plan_outline_tr.find("td:eq(1)").text(plan);
					var rate = parseInt(out * 100 / plan);
					$today_plan_outline_tr.find("td:eq(3)").text(rate + " %");
					if (rate<100 && now_period > 3) $today_plan_outline_tr.find("td:eq(3)").addClass("low");
				}
				if (tlt_plan>0 && now_period >= 3) {
					var rate = parseInt(tlt_out * 100 / tlt_plan);
					$today_plan_outline_tr.find("td:eq(4)").text(rate + " %");
					if (rate<100 && now_period > 3) $today_plan_outline_tr.find("td:eq(4)").addClass("low");
				}
				$today_plan_outline_tr.find("td:eq(2)").text(out);
				if (now_period > 3) $today_plan_outline_tr.css("background-color","lightgray");
				if (now_period == 3) $today_plan_outline_tr.css("background-color","lightblue");

				$today_plan_outline_tr = $today_plan_outline.find("tr:eq(3)");
				plan = plans[22];
				out = outs[22];
				tlt_plan += plan;
				tlt_out += out;
				if (plan>0) {
					$today_plan_outline_tr.find("td:eq(1)").text(plan);
					var rate = parseInt(out * 100 / plan);
					$today_plan_outline_tr.find("td:eq(3)").text(rate + " %");
				}
				if (tlt_plan>0 && now_period == 4) {
					$today_plan_outline_tr.find("td:eq(4)").text(parseInt(tlt_out * 100 / tlt_plan) + " %");
				}
				$today_plan_outline_tr.find("td:eq(2)").text(out);
				if (now_period == 4) $today_plan_outline_tr.css("background-color","lightblue");
			}

		$("tspan").hover(
			function() {
				var jthis = $(this);
				jthis.parent().css("color", "navy");
			}
		);


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

	} catch (e) {
	};
	getfree = true;
};

$(document).ready(function() {
	var data = {
		line_id : $("#page_line_id").val(), section_id : $("#page_section_id").val(), isPeriod:true
	}

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

	if ($("#page_line_id").val() == "00000000101") {
		servicePath = "lineSituationBX.scan";
	}

	// Ajax提交
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true, // false
		url : servicePath + '?method=refresh&w=' + (new Date()).getTime(),
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
		setInterval(movepiece, 30000); // 120000
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
		line_id : $("#page_line_id").val(), section_id : $("#page_section_id").val(), position_id : checked_position_id, isPeriod:true
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