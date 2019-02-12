var qa_servicePath = "finalCheck.scan";

/*当日通过总数*/
var currentPassCount =0;

/*当日不合格数*/
var currentUnqualifiedCount =0;

/*当日品保件数*/
var currentCount=currentPassCount + currentUnqualifiedCount;
var chartFinalCheck = new Object();

/*取出数据*/
var showStatictisData = function(retData) {	

	/*今年 X轴坐标月份+当前月份周*/
	var mouthAndCurrentMouthWeek  = new Array();
	
	/*检查总数*/
	var inspectTotal = new Array();　
	
	/*检查合格率*/
	var percentOfPass = new Array();　
	
	/*目标99.8%*/
	var target = new Array();　
	for (var idx in retData.tLines) {
		var tLine = retData.tLines[idx];
		var axisText = tLine.axisText;	
		mouthAndCurrentMouthWeek.push(axisText);
		inspectTotal.push((tLine.process_count==0)? null:tLine.process_count);		;
		percentOfPass.push((tLine.process_count > 0) ? parseFloat(((1 - (tLine.fail_count / tLine.process_count)) * 100).toFixed(2) + "%") : null);
		target.push(parseFloat(tLine.targetRate));		
	}	
	
	/*上期检查总数*/
	var lastInspectTotal = new Array();
	
	/*上期检查合格率*/　
	var lastPercentOfPass = new Array();
	for (var idx in retData.lastYearInspectLines) {
		var lastYearInspectLine = retData.lastYearInspectLines[idx];
		lastInspectTotal.push(lastYearInspectLine.process_count || null);
		lastPercentOfPass.push((lastYearInspectLine.process_count > 0) ? 
		parseFloat(((1 - (lastYearInspectLine.fail_count / lastYearInspectLine.process_count)) * 100).toFixed(2)) : null);
	}

/**
 * highcharts start
 */
	//if(chartFinalCheck==null){	
	chartFinalCheck = new Highcharts.Chart({
		colors :['#CCCCCC', '#a0dbea'],
		chart:{
			renderTo: 'refix_1_container',//柱状表格显示
			alignTicks:false,
			marginTop:44,
			height:514,
//			width:820,
			events: {
				load : function(){
					try { loadOver();} catch(e){};
				}
			}
		},
		title:{
			text:null
		},
		xAxis:{
			categories:mouthAndCurrentMouthWeek,
			labels:{
				style:{
					fontSize:'12px',
					color:'black'
				}
			},
			lineWidth:1,
			lineColor:'#000000',
			gridLineWidth:1,
			tickColor:'#000000',
			tickPosition:'inside'
		},
		yAxis: [{
			title:{
				text:null
			},
			max:+1000,
			labels:{
				style:{
					fontSize:'12px',
					color:'black'
				}
			},
			tickPosition:'inside',
			lineWidth:1,
			lineColor:'#000000',
			gridLineWidth:0,
			tickWidth:1,
			tickLength:5,
			tickColor:'#000000'
		}, {
			min:95,
			max:100,
			tickInterval:1,
			title:{
				text:null
			},
			labels:{
				style:{
					fontSize:'12px',
					color:'black'
				},
				formatter:function(){return this.value+'%';}
			},
			tickPosition:'inside',
			lineWidth:1,
			gridLineWidth:0,
			lineStyle:'dashed',
			lineColor:'#000000',
			tickWidth:1,
			tickLength:5,
			tickColor:'#000000',
			opposite:true
		}],
		tooltip:{
			enabled: true,labels:{
				style:{
					fontSize:'12px',
					color:'black'
				}
			}
		},
		dataLabels: {
				color:'black'},
		plotOptions:{
			column:{				
				dataLabels: {
					enabled: true,//是否显示柱状头数据
					style: {
						fontSize:'12px',
						color:'black'
					},
					y: -12
				},
				animation:false
			},
			line:{
				connectNulls:true,
				animation:false
			}
		},
		legend:{
			borderWidth:0
		},
		credits:{
			enabled:false
		},
		series:[{
			name:'上期检查总数',
			type:'column',
			data:lastInspectTotal
		}, {
			name:'检查总数',
			type:'column',
			data:inspectTotal
		},  {
			name:'上期检查合格率',
			type:'line',
			color:'#9999FF',
			yAxis:1,
			dataLabels: {
				formatter: function(){return this.y + '%'},
				enabled: false
			},
			marker:{
				symbol: 'diamond'
			},
			data:lastPercentOfPass
		},  {
			name:'检查合格率',
			type:'line',
			color:'#0000FF',
			yAxis:1,
			dataLabels: {
				color:'#0000FF',
				formatter: function(){return this.y + '%'},
				enabled: true,
				style: {
					fontSize:'12px',
					class:'textSign'
				}
			},
			marker:{
				symbol: 'diamond'
			},
			data:percentOfPass
		}, {
			name:'目标99.6%',
			type:'line',
			dashStyle: 'Dash',
			marker:{
				symbol:'square'
			},
			color:'#800000',
			yAxis:1,
			data:target
		}]
	});	
	//}else{		
	//}
}

var refresh_ajaxSuccess = function(xhrobj,textStatus){
	var resInfo = null;
	try{
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
	} catch(e) {
		
	}

	var retData = resInfo.retData;

	/*今年 X轴坐标月份+当前月份周*/
	var mouthAndCurrentMouthWeek  = new Array();
	
	/*检查总数*/
	var inspectTotal = new Array();　
	
	/*检查合格率*/
	var percentOfPass = new Array();　
	
	/*目标99.8%*/
	var target = new Array();　
	for (var idx in retData.tLines) {
		var tLine = retData.tLines[idx];
		var axisText = tLine.axisText;	
		mouthAndCurrentMouthWeek.push(axisText);
		inspectTotal.push((tLine.process_count==0)? null:tLine.process_count);		;
		percentOfPass.push((tLine.process_count > 0) ? parseFloat(((1 - (tLine.fail_count / tLine.process_count)) * 100).toFixed(2) + "%") : null);
		target.push(parseFloat(tLine.targetRate));		
	}	
	
	/*上期检查总数*/
	var lastInspectTotal = new Array();
	
	/*上期检查合格率*/　
	var lastPercentOfPass = new Array();
	for (var idx in retData.lastYearInspectLines) {
		var lastYearInspectLine = retData.lastYearInspectLines[idx];
		lastInspectTotal.push(lastYearInspectLine.process_count || null);
		lastPercentOfPass.push((lastYearInspectLine.process_count > 0) ? 
		parseFloat(((1 - (lastYearInspectLine.fail_count / lastYearInspectLine.process_count)) * 100).toFixed(2)) : null);
	}

	chartFinalCheck.series[0].setData(lastInspectTotal, false);
	chartFinalCheck.series[1].setData(inspectTotal);
	chartFinalCheck.series[2].setData(lastPercentOfPass, false);
	chartFinalCheck.series[3].setData(percentOfPass, false);
	chartFinalCheck.series[4].setData(target);

	$("#today_quality").flipCounter(
	"startAnimation", // scroll counter from the current number to the specified number
	{
	    end_number: resInfo.currentWaitingCount, // the number we want the counter to scroll to
	    duration: 1000 // number of ms animation should take to complete
	});

	$("#today_confirm_quality").flipCounter(
	"startAnimation", // scroll counter from the current number to the specified number
	{
	    end_number: resInfo.currentWaitingConfirmCount, // the number we want the counter to scroll to
	    duration: 1000 // number of ms animation should take to complete
	});

	$("#today_pass").flipCounter(
	"startAnimation", // scroll counter from the current number to the specified number
	{
	    end_number: resInfo.currentPassCount, // the number we want the counter to scroll to
	    duration: 1000 // number of ms animation should take to complete
	});
	
	$("#today_unqualified").flipCounter(
	"startAnimation", // scroll counter from the current number to the specified number
	{
	    end_number: resInfo.currentUnqualifiedCount, // the number we want the counter to scroll to
	    duration: 1000 // number of ms animation should take to complete
	});

	$("#today_quality_peri").html("&nbsp;含周边设备：" + (resInfo.currentWaitingCountP || 0));
	$("#today_confirm_quality_peri").html("&nbsp;含周边设备：" + (resInfo.currentWaitingConfirmCountP || 0));
	$("#today_pass_peri").html("&nbsp;含周边设备：" + (resInfo.currentPassCountP || 0));
	$("#today_unqualified_peri").html("&nbsp;含周边设备：" + (resInfo.currentUnqualifiedCountP || 0));
	
}

$(function(){

	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : qa_servicePath + '?method=search',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj) {
			var resInfo;
//			try{
				eval("resInfo=" + xhrobj.responseText);
				showStatictisData(resInfo.retData);
            
				if ($("#today_quality").length > 0) {
					$("#today_quality").flipCounter({numIntegralDigits:2,
						digitHeight:124,
						digitWidth:62,
						imagePath:"images/white_counter.png",
						number : resInfo.currentWaitingCount
					});
					$("#today_confirm_quality").flipCounter({numIntegralDigits:2,
						digitHeight:124,
						digitWidth:62,
						imagePath:"images/white_counter.png",
						number : resInfo.currentWaitingConfirmCount
					});
					$("#today_pass").flipCounter({numIntegralDigits:2,
						digitHeight:124,
						digitWidth:62,
						imagePath:"images/white_counter.png",
						number : resInfo.currentPassCount
					});
					$("#today_unqualified").flipCounter({numIntegralDigits:2,
						digitHeight:124,
						digitWidth:62,
						imagePath:"images/white_counter.png",
						number : resInfo.currentUnqualifiedCount
					});

					$("#today_quality_peri").html("&nbsp;含周边设备：" + (resInfo.currentWaitingCountP || 0));
					$("#today_confirm_quality_peri").html("&nbsp;含周边设备：" + (resInfo.currentWaitingConfirmCountP || 0));
					$("#today_pass_peri").html("&nbsp;含周边设备：" + (resInfo.currentPassCountP || 0));
					$("#today_unqualified_peri").html("&nbsp;含周边设备：" + (resInfo.currentUnqualifiedCountP || 0));
				}
//				if ($("#today_quality").length > 0) {
//					$("#today_quality").flipCounter("startAnimation",
//					{
//						number : resInfo.currentUnqualifiedCount+resInfo.currentPassCount, 
//						end_number : resInfo.currentUnqualifiedCount+resInfo.currentPassCount,
//						duration : 100
//					});
//					currentCount = resInfo.currentUnqualifiedCount+resInfo.currentPassCount;
//					
//					$("#today_pass").flipCounter("startAnimation", 																	
//					{
//						number : resInfo.currentPassCount, 
//						end_number : resInfo.currentPassCount, 
//						duration : 100
//					});
//					currentPassCount = resInfo.currentPassCount;
//					
//					$("#today_unqualified").flipCounter("startAnimation",
//					{
//						number : currentUnqualifiedCount, 
//						end_number : resInfo.currentUnqualifiedCount, 
//						duration : 100
//					});
//					currentUnqualifiedCount = resInfo.currentUnqualifiedCount;
//					
//										
//				}
//			} catch(e) {
//				
//			}

			setInterval(function(){
				$.ajax({
					beforeSend:ajaxRequestType,
					async:true,
					url:qa_servicePath+"?method=search",
					cache : false,
					data : null,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : refresh_ajaxSuccess
				});
			}, 60000);
		}
	});
});
