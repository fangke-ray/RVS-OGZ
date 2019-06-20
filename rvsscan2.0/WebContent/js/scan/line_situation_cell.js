$(function(){
	var servicePath = "lineSituationCell.scan";
	var chart = null;
	
	//中小修
	$("#plan_middle_light").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	$("#output_middle_light").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	
	//外科镜维修
	$("#plan_surgical").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	$("#output_surgical").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	
	//纤维镜分解
	$("#plan_fibrescope_dec").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	$("#output_fibrescope_dec").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	
	//纤维镜总组
	$("#plan_fibrescope_com").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	$("#output_fibrescope_com").flipCounter({numIntegralDigits:2,
		digitHeight:124,
		digitWidth:62,
		imagePath:"images/white_counter.png"
	});
	
	function refresh(){
		var data = {
			"section_id" : $("#hide_section_id").val()
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
	}

	var plan_value = [0, 0, 0, 0];
	var plan_complete_value = [0, 0, 0, 0];

	function jsinit_ajaxSuccess (xhrobj, textStatus) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				var linePlanList = resInfo.linePlanList;
				
				linePlanList.forEach(function(item,index){
					var line_id = item.line_id;
					
					var $cell = $(".cell[for=" + line_id + "]");
					$cell.find(".plan-num").flipCounter(
				   	"startAnimation",
				    {
				        number: plan_value[index],
				        end_number: item.plan,
				        duration: 1000
				    });
					plan_value[index] = item.plan;
					
					$cell.find(".output-num").flipCounter(
				   	"startAnimation",
				    {
				        number: plan_complete_value[index],
				        end_number: item.plan_complete,
				        duration: 1000
				    });
					plan_complete_value[index] = item.plan_complete;
					
					var com_rate = 0;
					if (plan_value[index] > 0) {
			        	com_rate = Math.floor(plan_complete_value[index] / plan_value[index] * 100);
			        }
					$cell.find('.donut-arrow').trigger('updatePercentage', com_rate);
					$cell.find(".rate").text(com_rate.toFixed(0) + "%");
				});
			
				
				showChart(resInfo);
			}
		} catch(e) {
		}
	}
	
	function showChart(resInfo){
		if (chart == null) {
			chart = new Highcharts.Chart({
				colors : ['#cc76cc', '#92D050', '#7faad4'],
				chart : {
					renderTo : 'processing_container',
					type : 'bar',
					marginRight : 10,
					backgroundColor : "white",
					height : 539,
					width : 585
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
			})
		}else {
			chart.xAxis[0].setCategories(resInfo.categories, false);
			chart.series[0].setData(resInfo.light_fix_counts, false);
			chart.series[1].setData(resInfo.counts, false);
			chart.series[2].setData(resInfo.overlines);
		}
	}
	
	refresh();
	
	setInterval(refresh, 20000);
});