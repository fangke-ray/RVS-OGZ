<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>

<script type="text/javascript" src="js/utils.js"></script>

<div class="ui-widget-panel dwidth-full" style="align: center; margin: auto; padding: 16px;transform-origin:center top 0;transform:scaleY(.98);" id="body-3">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
		<span class="areatitle">全工程一览</span>
	</div>
	<div id="workarea" class="ui-widget-content">
		<div style="height:30px;font-size:15px;">
			<span class="areatitle">单位：台&nbsp;&nbsp;</span>
			<span class="areatitle"><hr width="50px" color="red" size="2"/></span>
			<span class="areatitle">粗细镜警戒线&nbsp;&nbsp;</span>
			<div style="float:left;">
				<span class="areatitle" style="margin-top:2px;"><hr width="20px" color="#83D1E4" size="12"/></span>
				<span class="areatitle">粗细镜（</span>
				<span class="areatitle" style="margin-left:-10px;margin-top:2px;"><hr width="20px" color="#EAC100" size="12"/></span>
				<span class="areatitle">超出警戒范围）</span>
				<span class="areatitle" style="margin-top:2px;"><hr width="20px" color="#4682B4" size="12"/></span>
				<span class="areatitle">纤维镜</span>
				<span class="areatitle" style="margin-top:2px;"><hr width="20px" color="#E377C2" size="12"/></span>
				<span class="areatitle">Endoeye</span>
				<span class="areatitle" style="margin-top:2px;"><hr width="20px" color="#00A843" size="12"/></span>
				<span class="areatitle">中小修理</span>
				<span class="areatitle" style="margin-top:2px;"><hr width="20px" color="#3333FF" size="12"/></span>
				<span class="areatitle">周边设备</span>
			</div>
		</div>
		<div class="clear"></div>
		<div style="height:250px;">
			<div id="performance_wipRepair_container" style="float:left;"></div>
			<div id="performance_wipOnline_container" style="float:right;"></div>
			<div class="clear"></div>
		</div>
		<div class="clear"></div>
		<div style="height:250px;">
			<div id="performance_today_container" style="float:left;"></div>
			<div id="shipping_container" style="float:right;"></div>
			<div class="clear"></div>
		</div>
	</div>
	<div class="ui-state-default ui-corner-bottom areaencloser"></div>
	<div class="clear"></div>
</div>

<script type="text/javascript">

// $(document).ready(

var iamready = function() {

	var chart1;
//	var chart2;
	var chart3;
	var chart4;
	var chart5;
	
	chart1 = new Highcharts.Chart({
   		chart : {
			renderTo : 'performance_wipOnline_container',
			type : 'pie',
			marginRight : 20,
			height : 250,
			width : 520
		},
		title : {
			text : '等待同意'
		},
		legend : {
			enabled : false
		},
		plotOptions : {
			pie : {
				dataLabels: {
 					align : 'center',
					shadow: true,
					color : '#163c75',
					backgroundColor: 'rgba(252, 255, 197, 0.4)',
					borderRadius: 5,
					borderWidth: 1,
					borderColor: '#AAA',
					style : {
						fontWeight : 'bold',
						fontSize : '16px'
					},
					formatter: function() {
						return this.y == 0 ? null : this.point.name + this.y;
		           		 }
                		}
			}
		},
		credits : {
			enabled : false
		},
		exporting : {
			enabled : false
		},
 		tooltip:{
			enabled : false
		},
		series : [{
			data : ${serie11},
			size : '70%',
 			dataLabels: {
				distance: -20
			}
		},{
			data : ${serie101},
			size : '95%',
			innerSize : '70%',
 			dataLabels: {
				distance: 5
			}
		}]
	});

	chart3 = new Highcharts.Chart({
    	colors : [
    	   	{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#83D1E4'],[.7, '#83D1E4'],[1, '#83D1E4']]},
   			'#4682B4',
   			'#E377C2',
   			'#00A843',
   			'#3333FF'
   			],
   		chart : {
			renderTo : 'performance_wipRepair_container',
			type : 'column',
			marginLeft : 100,
			height : 250,
			width : 420,
//			events: {
//				load: function () {
//					var ren = this.renderer,
//						color = this.yAxis[0].plotLinesAndBands[0].options.color;
//					var legendOptions = this.legend.options;
//
//					ren.path(['M', 200, 10, 'L', 240, 10])
//						.attr({
//								'stroke-width': 2,
//								stroke: color
//						})
//						.add();

//					ren.label('粗细镜警戒线', 180, 15)
//						.attr({
//								fill: 'white',
//								stroke: legendOptions.borderColor,
//								'stroke-width': legendOptions.borderWidth,
//								padding: legendOptions.symbolPadding,
//								r: legendOptions.borderRadius
//						})
//						.css({
//								color: legendOptions.itemStyle.color
//						})
//						.add()
//						.shadow(true);
//				}
//			}
		},
		credits : {
			enabled : false
		},
		title : {
			text : '同意修理'
		},
		xAxis : {
			categories : [''],
			labels : {
				y : 20,
				style: {
 					fontWeight : 'bold',
                	color: '#0D869F',
					fontSize : '16px'
                }
			}	
		},
		yAxis : {
			title : {
				text : ''
			},
            min:0,
            minRange:100,
            tickInterval:20,
            plotLines:[{
            	id:'plot-line-1',
            	value:${wipOnlineRepairUp},
            	width:2,
            	color:'red',
            	label:{
            		align:'left',
            		textAlign:'right',
            		text: ${wipOnlineRepairUp} + ' 台',
            		style:{
            			fontWeight : 'bold',
            			fontSize : '12px'
            		},
            		x:40
            	}
            },{
            	id:'plot-line-2',
            	value:${wipOnlineRepairDown},
            	width:2,
            	color:'red',
            	label:{
            		align:'left',
            		textAlign:'right',
            		text:'<br/>' + ${wipOnlineRepairDown} + ' 台',
            		style:{
            			fontWeight : 'bold',
            			fontSize : '12px'
            		},
            		x:40
            	}
            }]
		},
		plotOptions : {
			series : {
				
			},
			column : {
				dataLabels : {
					align : 'center',
					enabled : true,
					shadow: true,
					color : '#163c75',
					backgroundColor: 'rgba(252, 255, 197, 0.4)',
					borderRadius: 5,
					borderWidth: 1,
					borderColor: '#AAA',
					style : {
						fontWeight : 'bold',
						fontSize : '16px'
					},
				  	formatter: function() {
						return this.y;
					},
					y:-4
				},
				pointPadding :0.15
			}
		},
		exporting : {
			enabled : false
		},
		legend : {
			enabled : false
		},
		tooltip:{
			enabled:false
		},
		series : [{
			name : '粗细镜',
			data : ${serie15}
		},{
			name : '纤维镜',
			data : ${serie14}
		},{
			name : 'Endoeye',
			data : ${serie13}
		},{
			name : '中小修理',
			data : ${serie17}
		},{
			name : '周边设备',
			data : ${serie16}
		}]
	});

	//线上内镜分布 修理生产
	chart4 = new Highcharts.Chart({
		colors : ['#3333FF', '#00A843', '#E377C2', '#4682B4','#83D1E4'],
		chart : {
			renderTo : 'performance_today_container',
			type : 'bar',
			marginLeft : 100,
			height : 250,
			width : 420
		},
		credits : {
			enabled : false
		},
		title : {
			text : '投线在修'
		},
		xAxis : {
			categories : ['零件齐备', '等待零件', '延误', '不良'],
			labels : {
				style : {
					fontWeight : 'bold',
					fontSize : '16px'
				}
			}
		},
		yAxis : {
			title : {
				text : '',
				style : {
					fontWeight : 'bold',
					fontSize : '16px'
				}
			},
			min : 0,
			labels : {
				enabled : false
			}
		},
	 	tooltip : {
	 		enabled : false
		},
		plotOptions : {
			series : {
				stacking : 'normal'
			},
			bar : {
 				dataLabels : {
					enabled : true,
					shadow: true,
					color : '#163c75',
					borderRadius: 5,
					backgroundColor: 'rgba(252, 255, 197, 0.4)',
					borderWidth: 1,
					borderColor: '#AAA',
					style: {
						fontWeight:'bold',
						fontSize : '16px'
					},
					overflow: 'justify',
					align: 'left',
					y : -15,
					formatter : function(){
						var x = this.point.x;
						var serie24 = this.series.chart.series[0];
						var serie25 = this.series.chart.series[1];
						var serie22 = this.series.chart.series[2];
						var serie23 = this.series.chart.series[3];
						var serie21 = this.series.chart.series[4];
						
						if (x==0) {//正常在线
							if (this.series.name=="粗细镜") {
								var label= serie21.processedYData[0] + "/" + serie23.processedYData[0] + "/" + serie22.processedYData[0] + "/" + serie25.processedYData[0] + "/" + serie24.processedYData[0];
								return label;
							}
						} else if (x==1) {//等待零件
							if (this.series.name=="粗细镜") {
								var label= serie21.processedYData[1] + "/" + serie23.processedYData[1] + "/" + serie22.processedYData[1] + "/" + serie25.processedYData[1] + "/" + serie24.processedYData[1];
								return label;
							}
						} else if (x==2) {//延误
							if (this.series.name=="粗细镜") {
								var label= serie21.processedYData[2] + "/" + serie23.processedYData[2] + "/" + serie22.processedYData[2] + "/" + serie25.processedYData[2] + "/" + serie24.processedYData[2];
								return label;
							}
						} else if (x==3) {//不良
							if (this.series.name=="粗细镜") {
								var label= serie21.processedYData[3] + "/" + serie23.processedYData[3] + "/" + serie22.processedYData[3] + "/" + serie25.processedYData[3] + "/" + serie24.processedYData[3];
								return label;
							}
//						} else if (x==4) {
//							if (this.series.name=="粗细镜") {
//								var label= serie21.processedYData[4] + "/" + serie23.processedYData[4] + "/" + serie22.processedYData[4];
//								return label;
//							}
						} else {
							return this.y;
						}
					}
				}
			}
		},
		legend : {
			enabled : false
		},
		series : [{
			name : '周边设备',
			data : ${serie24}
		},{
			name : '中小修理',
			data : ${serie25}
		},{
			name : 'Endoeye',
			data : ${serie22}
		},{
			name : '纤维镜',
			data : ${serie23}
		},{
			name : '粗细镜',
			data : ${serie21}
		}]
	});

	chart5 = new Highcharts.Chart({
		chart : {
			renderTo : 'shipping_container',
			type : 'bar',
			marginRight : 20,
			height : 250,
			width : 520
		},
		credits : {
			enabled : false
		},
		title : {
			text : '累计出货'
		},
		xAxis : {
			categories : ['本月出货累计','${period_name}出货累计（实绩）', '${period_name}累计（计划）'],
			labels : {
				style : {
					fontWeight : 'bold',
					fontSize : '16px'
				}
			}
		},
		yAxis : {
			title : {
				text : ''
			}
		},
		legend : {
			enabled : false
		},
		tooltip : {
		 	enabled : false
		},
		plotOptions : {
			bar : {
				stacking : 'normal',
				dataLabels : {
					enabled : true,
					shadow: true,
					color : '#163c75',
                    borderRadius: 5,
                    backgroundColor: 'rgba(255,255,255,0.3)',
                    borderWidth: 1,
                    borderColor: '#AAA',
                    style: {
                        fontWeight:'bold',
						fontSize : '16px'
                    },
                    overflow: 'justify',
                    align: 'left'
				},
				marker : {
					radius : 4,
					lineColor : '#666666',
					lineWidth : 1
				}
			}
		},
		exporting : {
			enabled : false
		},
		series : [{
			name : '累计出货',
			color:'#7FB418',
			data : ${serie31}
		}]
	});


	setInterval(refresh, 20000);

	function refresh() {
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : 'globalProgress.scan?method=refresh',
			cache : false,
			data : null,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrobj) {
				var resInfo = null;
				try {
					eval("resInfo=" + xhrobj.responseText);
					eval("chart1.series[0].setData(" + resInfo.serie11 + ", false)");
					eval("chart1.series[1].setData(" + resInfo.serie101 + ")");
					eval("chart3.series[0].setData(" + resInfo.serie15 + ", false)");
					eval("chart3.series[1].setData(" + resInfo.serie14 + ", false)");
					eval("chart3.series[2].setData(" + resInfo.serie13 + ", false)");
					eval("chart3.series[3].setData(" + resInfo.serie16 + ", false)");
					eval("chart3.series[4].setData(" + resInfo.serie17 + ")");
					eval("chart4.series[0].setData(" + resInfo.serie24 + ", false)");
					eval("chart4.series[1].setData(" + resInfo.serie25 + ", false)");
					eval("chart4.series[2].setData(" + resInfo.serie22 + ", false)");
					eval("chart4.series[3].setData(" + resInfo.serie23 + ", false)");
					eval("chart4.series[4].setData(" + resInfo.serie21 + ")");
					eval("chart5.series[0].setData(" + resInfo.serie31 + ")");
				} catch(e) {
				}
			}
		});
	}
	
	
}
// );

if (typeof(jQuery) === "undefined") {
	loadCss("css/custom.css");
	loadCss("css/olympus/jquery-ui-1.9.1.custom.css", function(){
		loadJs("js/jquery-1.8.2.min.js", function(){
			$("body").addClass("scan1024");
			loadJs("js/jquery-ui-1.9.1.custom.min.js", function(){
				loadJs("js/jquery-plus.js", function(){
					loadJs("js/highcharts.js", iamready);
				});
			});
		});
	});
} else {
	iamready();
}

</script>