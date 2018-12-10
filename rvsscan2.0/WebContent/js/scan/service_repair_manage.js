var sr_servicePath="service_repair_manage.scan";

var sr_chart = new Object();

$(function(){
	$.ajax({
		beforeSend:ajaxRequestType,
		async:true,
		url:sr_servicePath+"?method=search",
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : sr_jsinit_ajaxSuccess
	});

});

var sr_refresh_ajaxSuccess=function(xhrobj,textStatus){
	var resInfo = null;
//	try{
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
//	} catch(e) {
		
//	}

	sr_chart.series[0].setData(resInfo.retData.tLines.list_last);
	sr_chart.series[1].setData(resInfo.retData.tLines.list_cuttent);
	sr_chart.series[2].setData(resInfo.retData.tLines.one_day_rate);
	sr_chart.series[3].setData(resInfo.retData.tLines.last_one_day_rate);
	sr_chart.series[4].setData(resInfo.retData.tLines.two_day_rate);
	sr_chart.series[5].setData(resInfo.retData.tLines.last_two_day_rate);
	sr_chart.series[6].setData(resInfo.retData.tLines.target_oneday_rateList);
	sr_chart.series[7].setData(resInfo.retData.tLines.target_twoday_rateList);

	$("#service_complete").flipCounter(
	"startAnimation", // scroll counter from the current number to the specified number
	{
	    end_number: resInfo.currentData.cuttent_count, // the number we want the counter to scroll to
	    duration: 1000 // number of ms animation should take to complete
	});
	
	$("#wait_complete").flipCounter(
	"startAnimation", // scroll counter from the current number to the specified number
	{
	    end_number: resInfo.currentData.wait_count, // the number we want the counter to scroll to
	    duration: 1000 // number of ms animation should take to complete
	});
	
	$("#analyse_complete").flipCounter(
	"startAnimation", // scroll counter from the current number to the specified number
	{
	    end_number: resInfo.currentData.analyse_count, // the number we want the counter to scroll to
	    duration: 1000 // number of ms animation should take to complete
	});
	
}

var sr_jsinit_ajaxSuccess=function(xhrobj,textStatus){
	var resInfo=null;
//	try{
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);

		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);
		}else{
			sr_chart = new Highcharts.Chart({
				chart:{
					alignTicks:true, //多轴对齐刻度
					renderTo : 'performance_container',
					borderRadius:0,//边框圆角
			height:500,
			width:820,
					events: {
						load : function(){
							try { loadOver();} catch(e){};
						}
					},
					spacingTop : 20
				},
				credits:{
				    position: {              
					    align: 'left',       
			            x: -100                           
			        }
				},
				title:{
						text:null, // '146P保修期内返品+QIS品分析(统计表)'
						margin:28
				},
				xAxis:{
					categories:resInfo.retData.tLines.axisTextList,//坐标值
					labels:{//标签
						style:{
							fontSize:'12px',
							color:'black'
						},
						y:19
					},
					lineColor:'#CCCCCC',//轴线颜色
					tickLength:8,//刻度线长度
					gridLineColor:'#99CCFF',//纵向格线的颜色
					gridLineWidth:1
				},
				yAxis:[{
					min:0,
					max:70,
					tickInterval:14,
					title:{
						text:null
					},
					labels:{
						style:{
							fontSize:'12px',
							color:'black'
						}
					},
					lineWidth:1,//Y轴宽度
					tickWidth:1,//刻度尺宽度
					tickLength:5,//刻度尺长度
					gridLineWidth: 0//无网格
				}, {
					min:40,
					max:100,
					tickInterval:10,
					title:{
						text:null
					},
					labels:{
						style:{
							fontSize:'12px',
							color:'black'
						},
						formatter:function(){
							return this.value+"%"
						}
					},
					tickPosition:'outside',
					lineWidth:1,
					lineColor:'#CCCCCC',
					tickWidth:1,
					tickLength:5,
					tickColor:'#CCCCCC',
					opposite:true,
					gridLineWidth:0//无网格
				}],
				legend:{//图例
					borderWidth:0,
					itemWidth:170,
					itemStyle:{//图例文字样式
						fontSize:'12px',
						color:'#000000'
					}
				},
				plotOptions:{//绘图
	  				column:{
	  					pointPadding:0.1,//图形间的间距
	  					borderWidth:0,//图形边框宽度
	  					animation:false
	  				},
	  				line:{
	  					connectNulls:true,
	  					animation:false
	  				}
	  			},
				series:[{
					type:'column',
					name:'上期保修期内返品',
					data:resInfo.retData.tLines.list_last,
					color:'#CCCCCC',
					dataLabels: {
					    formatter: function(){return this.y},
					    enabled:true,
					    color:'#3399FF'
					}
				},{
					type:'column',
					name:'保修期内返品',
					data:resInfo.retData.tLines.list_cuttent,
					color:'#99CCFF',
					dataLabels: {
					    formatter: function(){return this.y},
					    enabled:true,
					    color:'#0066CC'
					}
				},{
					type:'line',
					yAxis:1,
					name:'答复一日达成率',
					data:resInfo.retData.tLines.one_day_rate,
					color:'#003399',
					marker:{
						symbol:'triangle'
					},
					dataLabels: {
					    formatter: function(){return this.y + '%'},
					    enabled:true,
					    color:'#003399',
					    style:{
					    	fontSize:'12px'
					    }
					}			
				},{
					type:'line',
					yAxis:1,
					name:'上期答复一日达成率',
					data:resInfo.retData.tLines.last_one_day_rate,
					color:'#9999CC',
					marker:{
						symbol:'triangle'
					},
					dataLabels: {
					   formatter: function(){return this.y + '%'},
					    enabled:true,
					    color:'#9999CC',
					    style:{
					    	fontSize:'12px'
					    }
					}
				},{
					type:'line',
					yAxis:1,
					name:'答复两日达成率',
					data:resInfo.retData.tLines.two_day_rate,
					color:'#92d050',
					marker:{
						symbol:'circle'
					},
					dataLabels: {
					    formatter: function(){return this.y + '%'},
					    enabled:true,
					    color:'#92d050',
					    style:{
					    	fontSize:'12px'
					    }
					}
				},{
					type:'line',
					yAxis:1,
					name:'上期答复两日达成率',
					data:resInfo.retData.tLines.last_two_day_rate,
					color:'#ff7900',
					marker:{
						symbol:'circle'
					},
					dataLabels: {
					     formatter: function(){return this.y + '%'},
					    enabled:true,
					    color:'#ff7900',
					    style:{
					    	fontSize:'12px'
					    }
					}
				},{
					type:'line',
					yAxis:1,
					dashStyle: 'Dash',
					name:'答复一日达成目标90%',
					data:resInfo.retData.tLines.target_oneday_rateList,
					color:'#990092',
					marker:{
						symbol:'square'
					}
				},{
					type:'line',
					yAxis:1,
					dashStyle: 'Dash',
					name:'答复两日达成目标95%',
					data:resInfo.retData.tLines.target_twoday_rateList,
					color:'#996666',
					marker:{
						symbol:'square'
					}
				}]
			});
	
			if (resInfo.currentData) {

			 if ($("#service_complete").length > 0) {
				 $("#service_complete").flipCounter(
			        {
						numIntegralDigits:2,
						digitHeight:124,
						digitWidth:62,
						imagePath:"images/white_counter.png",
						number: resInfo.currentData.cuttent_count // the number we want to scroll from
				 });
			 }
			 
			 if ($("#wait_complete").length > 0) {
				 $("#wait_complete").flipCounter(
			        {
						numIntegralDigits:2,
						digitHeight:124,
						digitWidth:62,
						imagePath:"images/white_counter.png",
						number: resInfo.currentData.wait_count // the number we want to scroll from
			        });
			 }
			 

			 if ($("#analyse_complete").length > 0) {
				 $("#analyse_complete").flipCounter(
			        {
						numIntegralDigits:2,
						digitHeight:124,
						digitWidth:62,
						imagePath:"images/white_counter.png",
						number: resInfo.currentData.analyse_count // the number we want to scroll from
			        });
			 }
			}
			setInterval(function(){
				$.ajax({
					beforeSend:ajaxRequestType,
					async:true,
					url:sr_servicePath+"?method=search",
					cache : false,
					data : null,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : sr_refresh_ajaxSuccess
				});
			}, 50000);
		}
//	}catch(e){};
	
}