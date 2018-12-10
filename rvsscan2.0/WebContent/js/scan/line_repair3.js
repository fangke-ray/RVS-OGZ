var servicePath = "lineRepair3.scan";
var chart2 = null;
var endoEye_plan_value = 0;
var endoEye_plan_complete_value = 0;
var device_plan_value = 0;
var device_plan_complete_value = 0;

var getfree = true;

var jsinit_ajaxSuccess = function(xhrobj, textStatus){
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		} else {

			$("#waiting_cast").text(resInfo.waiting_cast);
			$("#waiting_repair").text(resInfo.waiting_repair);
			$("#waiting_parts").text(resInfo.waiting_parts);

			if (chart2 == null) {
				chart2 = new Highcharts.Chart({
					colors : ['#cc76cc', '#92D050', '#7faad4'],
					chart : {
						renderTo : 'processing_container',
						type : 'bar',
						marginRight : 10,
						backgroundColor : "white",
						height : 147,
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

		if ($("#endoEye_plan_count").length > 0) {
	 		$("#endoEye_plan_count").flipCounter(
		       	"startAnimation",
		        {
		            number: endoEye_plan_value,
		            end_number: resInfo.endoEye_plan,
		            duration: 1000
	        });
	
	        endoEye_plan_value = resInfo.endoEye_plan;
	
	      	$("#endoEye_plan_finish_count").flipCounter(
		       	"startAnimation",
		        {
		            number: endoEye_plan_complete_value,
		            end_number: resInfo.endoEye_plan_complete,
		            duration: 1000
	        });
	
		endoEye_plan_complete_value = resInfo.endoEye_plan_complete;

		var com_rate = 0;
	        if (endoEye_plan_value > 0) {
	        	com_rate = Math.floor(endoEye_plan_complete_value / endoEye_plan_value * 100);
	        }
			$('#endoEye_plan_complete .donut-arrow').trigger('updatePercentage', com_rate);
			$("#endoEye_completed_rate").text((endoEye_plan_value ? com_rate.toFixed(0) + "%" : "- %"));	        
		}

		if ($("#device_plan_count").length > 0) {
	 		$("#device_plan_count").flipCounter(
		       	"startAnimation",
		        {
		            number: device_plan_value,
		            end_number: resInfo.device_plan,
		            duration: 1000
	        });
	
	        device_plan_value = resInfo.device_plan;
	
	      	$("#device_plan_finish_count").flipCounter(
		       	"startAnimation",
		        {
		            number: device_plan_complete_value,
		            end_number: resInfo.device_plan_complete,
		            duration: 1000
	        });
	
	        device_plan_complete_value = resInfo.device_plan_complete;

			var com_rate = 0;
	        if (device_plan_value > 0) {
	        	com_rate = Math.floor(device_plan_complete_value / device_plan_value * 100);
	        }
			$('#device_plan_complete .donut-arrow').trigger('updatePercentage', com_rate);
			$("#device_completed_rate").text((device_plan_value ? com_rate.toFixed(0) + "%" : "- %"));
		}

	} catch (e) {
	};
	getfree = true;
};

$(document).ready(function() {
	var data = {
		line_id : $("#page_line_id").val(), section_id : $("#page_section_id").val()
	}

	if ($("#endoEye_plan_count").length > 0) {
		$("#endoEye_plan_count").flipCounter({
			numIntegralDigits:2,
			digitHeight:124,
			digitWidth:62,
			imagePath:"images/white_counter.png"
		});
		$("#endoEye_plan_finish_count").flipCounter({
			numIntegralDigits:2,
			digitHeight:124,
			digitWidth:62,
			imagePath:"images/white_counter.png"
		});
	}

	if ($("#device_plan_count").length > 0) {
		$("#device_plan_count").flipCounter({
			numIntegralDigits:2,
			digitHeight:124,
			digitWidth:62,
			imagePath:"images/white_counter.png"
		});
		$("#device_plan_finish_count").flipCounter({
			numIntegralDigits:2,
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
});

var refresh = function() {
	var data = {
		line_id : $("#page_line_id").val(),
		section_id : $("#page_section_id").val()
	}

	if (getfree) {
		getfree = false;

		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : servicePath + '?method=refresh',
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