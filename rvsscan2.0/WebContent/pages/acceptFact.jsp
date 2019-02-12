<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>

<script type="text/javascript" src="js/utils.js"></script>

<div class="ui-widget-panel dwidth-full" style="align: center; margin: auto; padding: 16px;transform-origin:center top 0;transform:scaleY(.9);" id="body-3">
	<div id="leftarea" style="width:640px;float:left;">
		<div id="workarea">
			<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
				<span class="areatitle">现品管理/受理报价-实绩</span>
			</div>
			<div class="ui-widget-content" style="position:relative;">
				<div id="performance_container"></div>
	
				<div id="direct_legend" style="border: 2px solid #ADD8E6; border-radius: 4px; height: 51px; left: 12px; position: absolute; top: 188px; width: 36px;">
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:27px;left:2px;
						background: #2125BE;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #AABEE4 0%, #2125BE 100%);
						background: -webkit-linear-gradient(top,  #AABEE4 0%,#2125BE 100%);
					">分室</div>
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:2px;left:2px;
						background: #449E2D;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #B6E0AA 0%, #449E2D 100%);
						background: -webkit-linear-gradient(top,  #B6E0AA 0%,#449E2D 100%);
					">直送</div>
				</div>
	
				<div id="sterilize_legend" style="border: 2px solid #ADD8E6; border-radius: 4px; height: 26px; left: 294px; position: absolute; top: 30px; width: 49px;">
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:2px;left:2px;
						background: #2125BE;color:maroon;
						text-shadow: 1px 1px white;
						background: -webkit-linear-gradient(top,  #FFFF33 0%, #F0F000 100%);
						background: -moz-linear-gradient(top,  #FFFF33 0%, #F0F000 100%);
					">进行中</div>
				</div>
	
				<div id="inlined_legend" style="border: 2px solid #ADD8E6; border-radius: 4px; height: 97px; left: 582px; position: absolute; top: 142px; width: 49px;">
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:2px;left:13px;
						background: #B5B500;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #A0A000 0%, #B5B500 100%);
						background: -webkit-linear-gradient(top,  #A0A000 0%,#B5B500 100%);
					">S1</div>
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:26px;left:2px;
						background: #00B5B5;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #00A0A0 0%, #00B5B5 100%);
						background: -webkit-linear-gradient(top,  #00A0A0 0%,#00B5B5 100%);
					">S2+S3</div>
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:50px;left:6px;
						background: #00A843;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #00A0A0 0%, #00A843 100%);
						background: -webkit-linear-gradient(top,  #00A0A0 0%,#00A843 100%);
					">D+M</div>
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:74px;left:16px;
						background: #3333FF;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #3399FF 0%, #3333FF 100%);
						background: -webkit-linear-gradient(top,  #3399FF 0%,#3333FF 100%);
					">E</div>
				</div>
			</div>
			<div class="ui-state-default ui-corner-bottom areaencloser"></div>
			<div class="clear" style="height:16px;"></div>
		</div>

		<div id="storagearea">
			<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
				<span class="areatitle">现品管理/受理报价-待处理品</span>
			</div>
			<div class="ui-widget-content" style="position:relative;">
				<div id="tobe_inline_legend" style="border: 2px solid #ADD8E6; border-radius: 4px; height: 96px; left: 582px; position: absolute; top: 142px; width: 49px;z-index:10;">
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:2px;left:13px;
						background: #B5B500;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #A0A000 0%, #B5B500 100%);
						background: -webkit-linear-gradient(top,  #A0A000 0%,#B5B500 100%);
					">S1</div>
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:26px;left:2px;
						background: #00B5B5;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #00A0A0 0%, #00B5B5 100%);
						background: -webkit-linear-gradient(top,  #00A0A0 0%,#00B5B5 100%);
					">S2+S3</div>
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:50px;left:6px;
						background: #00A843;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #00A0A0 0%, #00A843 100%);
						background: -webkit-linear-gradient(top,  #00A0A0 0%,#00A843 100%);
					">D+M</div>
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:74px;left:16px;
						background: #3333FF;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #3399FF 0%, #3333FF 100%);
						background: -webkit-linear-gradient(top,  #3399FF 0%,#3333FF 100%);
					">E</div>
				</div>
				<div id="waiting_container"></div>
			</div>
			<div class="ui-state-default ui-corner-bottom areaencloser"></div>
			<div class="clear"></div>
		</div>
	</div>

	<div id="rightarea" style="width:336px;float:left;margin-left: 16px;">
		<div id="weekarea">
			<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
				<span class="areatitle">一周实绩累计（${friday}～）</span>
			</div>
			<div class="ui-widget-content" style="position:relative;">
				<div id="week_recept_container"></div>
				<div id="week_shipping_container" style="margin-top:42px;margin-bottom:2px;"></div>
				<div id="week_recept_dir_container" style="position:absolute;top:154px;right:-6px;"></div>
				<div id="week_shipping_dir_container" style="position:absolute;top:490px;right:-6px;"></div>
				<div style="position:absolute;top:278px;left:10px;border-radius:4px;border:2px solid lightblue;height:50px;width:132px">
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:2px;left:2px;
						background: #7FAAD4;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #6699CC 0%, #7FAAD4 100%);
						background: -webkit-linear-gradient(top,  #6699CC 0%,#7FAAD4 100%);
					">大修理</div>
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:2px;left:50px;
						background: #993999;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #cc76cc 0%, #993999 100%);
						background: -webkit-linear-gradient(top,  #cc76cc 0%,#993999 100%);
					">中小修理</div>
					<div style="position:absolute;padding:2px 4px;border-radius:2px;top:2px;left:97px;
						background: #91D580;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #6DCC66 0%, #91D580 100%);
						background: -webkit-linear-gradient(top,  #6DCC66 0%,#91D580 100%);
					">单元</div>
					<div style="position:absolute;padding:2px 7px;border-radius:2px;top:26px;left:2px;
						background: #D19961;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #D5BA5E 0%, #F33A0C 100%);
						background: -webkit-linear-gradient(top,  #D5BA5E 0%,#F33A0C 100%);
					">EndoEye</div>
					<div style="position:absolute;padding:2px 7px;border-radius:2px;top:26px;left:67px;
						background: #3333FF;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #3399FF 0%, #3333FF 100%);
						background: -webkit-linear-gradient(top,  #3399FF 0%,#3333FF 100%);
					">周边设备</div>
				</div>
				<div id="week_legend" style="position:absolute;top:298px;left:150px;border-radius:4px;border:2px solid lightblue;height:50px;width:177px">
					<div style="position:absolute;padding:2px 8px;border-radius:2px;top:2px;left:2px;
						background: #FFBF00;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #DADA7A 0%, #FFBF00 100%);
						background: -webkit-linear-gradient(top,  #DADA7A 0%,#FFBF00 100%);
					">OCM-GZ RC</div>
					<div style="position:absolute;padding:2px 8px;border-radius:2px;top:2px;left:92px;
						background: #FF4A4A;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #FF9B9B 0%, #FF4A4A 100%);
						background: -webkit-linear-gradient(top,  #FF9B9B 0%,#FF4A4A 100%);
					">OCM-BJ RC</div>
					<div style="position:absolute;padding:2px 8px;border-radius:2px;top:26px;left:2px;
						background: #336633;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #99CC66 0%, #336633 100%);
						background: -webkit-linear-gradient(top,  #99CC66 0%,#336633 100%);
					">OCM-SH RC</div>
					<div style="position:absolute;padding:2px 8px;border-radius:2px;top:26px;left:92px;
						background: #009999;color:white;
						text-shadow: 1px 1px maroon;
						background: -moz-linear-gradient(top,  #66CCCC 0%, #009999 100%);
						background: -webkit-linear-gradient(top,  #66CCCC 0%,#009999 100%);
					">OCM-SY RC</div>
				</div>
				</div>
			<div class="ui-state-default ui-corner-bottom areaencloser"></div>
			<div class="clear"></div>
		</div>
	</div>

	<div class="clear"></div>
</div>

<script type="text/javascript">
var iamreadyAf = function() {
	var chart1;
	var chart2;
	var chartW1;
	var chartW2;
	var chartW1_1;
	var chartW2_1;
	
	chartW1 = new Highcharts.Chart({
    	colors: [{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#6699CC'],[1, '#7FAAD4']]},
    	        {linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#cc76cc'],[1, '#993999']]},
    			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#D5BA5E'],[1, '#F33A0C']]},
    			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#6DCC66'],[1, '#91D580']]},
    			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#3399FF'],[1, '#3333FF']]}],
        chart: {
			style : {
				fontFamily : '"Microsoft YaHei", SimHei, STHeiti, "Lucida Grande", "Lucida Sans Unicode", Verdana, Arial, Helvetica, sans-serif', // default font
				fontSize : '12px'
			},
			renderTo : 'week_recept_container',
 			height : 300,
			width : 300,
	        type: 'pie',
			events : {
				load : function() {
				}
			}
        },
		title : {
			text : '本周到货登录实绩'
		},
 		legend : {
			enabled : true,
			borderWidth:0,
			floating : true,
			align :'right'
		},
		plotOptions: {
            pie: {
                dataLabels: {
                    distance: -26,
                      style: {
                        fontWeight: 'bold',
                        fontSize : '20px',
                        color: '#343264'
                    },
	                formatter: function() {
	                	return this.y + " 台";
	                },
 	                backgroundColor : 'rgba(255,255,255,0.8)',
	                borderColor : '#7096C5',
	                borderRadius : 4,
	                borderWidth :2
                },
	  			center : ['32%','40%'],
				size : "80%"
				// ,showInLegend: true
	        }
        },
		credits : {
			enabled : false
		},
 		exporting : {
			enabled : false
		},       
        series: [{
			name : '台数',
            data: ${series3_1}
        }]
    });
 
    chartW2 = new Highcharts.Chart({
    	colors: [{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#6699CC'],[1, '#7FAAD4']]},
    	     	{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#cc76cc'],[1, '#993999']]},
    			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#D5BA5E'],[1, '#F33A0C']]},
    			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#6DCC66'],[1, '#91D580']]},
    			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 1 },stops: [[0, '#3399FF'],[1, '#3333FF']]}],
        chart: {
			style : {
				fontFamily : '"Microsoft YaHei", SimHei, STHeiti, "Lucida Grande", "Lucida Sans Unicode", Verdana, Arial, Helvetica, sans-serif', // default font
				fontSize : '12px'
			},
			renderTo : 'week_shipping_container',
 			height : 300,
			width : 300,
	        type: 'pie',
			events : {
				load : function() {
				}
			}
        },
		title : {
			text : '本周完成出货实绩'
		},
 		legend : {
			enabled : true,
			borderWidth:0,
			floating : true,
			align :'right'
		},
		plotOptions: {
            pie: {
                dataLabels: {
                    distance: -26,
                      style: {
                        fontWeight: 'bold',
                        fontSize : '20px',
                        color: '#343264'
                    },
	                formatter: function() {
	                	return this.y + " 台";
	                },
 	                backgroundColor : 'rgba(255,255,255,0.8)',
	                borderColor : '#7096C5',
	                borderRadius : 4,
	                borderWidth :2
                },
	  			center : ['32%','40%'],
				size : "80%"
	        }
        },
		credits : {
			enabled : false
		},
 		exporting : {
			enabled : false
		},       
        series: [{
			name : '台数',
            data: ${series4_1}
        }]
    });
 
    chartW1_1 = new Highcharts.Chart({
    	colors: [{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#DADA7A'],[1, '#FFBF00']]},
    			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#FF9B9B'],[1, '#FF4A4A']]},
    			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#99CC66'],[1, '#336633']]},
    			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#00CCCC'],[1, '#009999']]}],
        chart: {
			style : {
				fontFamily : '"Microsoft YaHei", SimHei, STHeiti, "Lucida Grande", "Lucida Sans Unicode", Verdana, Arial, Helvetica, sans-serif', // default font
				fontSize : '12px'
			},
			renderTo : 'week_recept_dir_container',
 			height : 160,
			width : 160,
	        type: 'pie',
	        backgroundColor: 'transparent',
			events : {
				load : function() {
				}
			}
        },
		title : {
			text : 'RC分布实绩',
			style : {
				fontSize : '14px'
			}		
		},
 		legend : {
			enabled : true,
			borderWidth:0,
			floating : true,
			align :'right'
		},
		plotOptions: {
            pie: {
                dataLabels: {
                    distance: -16,
                      style: {
                        fontWeight: 'bold',
                        fontSize : '16px',
                        color: '#343264'
                    },
	                formatter: function() {
	                	return this.y + " 台";
	                },
 	                backgroundColor : 'rgba(255,255,255,0.8)',
	                borderColor : '#7096C5',
	                borderRadius : 4,
	                borderWidth :2
                },
	  			center : ['50%','50%'],
				size : "90%"
	        }
        },
		credits : {
			enabled : false
		},
 		exporting : {
			enabled : false
		},       
        series: [{
			name : '台数',
            data: ${series3_2}
        }]
    });

    chartW2_1 = new Highcharts.Chart({
    	colors: [{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#DADA7A'],[1, '#FFBF00']]},
     			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#FF9B9B'],[1, '#FF4A4A']]},
     			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#99CC66'],[1, '#336633']]},
     			{linearGradient: { x1: 0, x2: 1, y1: 0, y2: 0 },stops: [[0, '#00CCCC'],[1, '#009999']]}],
        chart: {
			style : {
				fontFamily : '"Microsoft YaHei", SimHei, STHeiti, "Lucida Grande", "Lucida Sans Unicode", Verdana, Arial, Helvetica, sans-serif', // default font
				fontSize : '12px'
			},
			renderTo : 'week_shipping_dir_container',
 			height : 160,
			width : 160,
	        type: 'pie',
	        backgroundColor: 'transparent'
        },
		title : {
			text : 'RC分布实绩',
			style : {
				fontSize : '14px'
			}			
		},
 		legend : {
			enabled : true,
			borderWidth:0,
			floating : true,
			align :'right'
		},
		plotOptions: {
            pie: {
                dataLabels: {
                    distance: -16,
                      style: {
                        fontWeight: 'bold',
                        fontSize : '16px',
                        color: '#343264'
                    },
	                formatter: function() {
	                	return this.y + " 台";
	                },
 	                backgroundColor : 'rgba(255,255,255,0.8)',
	                borderColor : '#7096C5',
	                borderRadius : 4,
	                borderWidth :2
                },
	  			center : ['50%','50%'],
				size : "90%"
	        }
        },
		credits : {
			enabled : false
		},
 		exporting : {
			enabled : false
		},       
        series: [{
			name : '台数',
            data: ${series4_2}
        }]
    });

    var xCategories1 = [ '受理', '消毒','灭菌','报价完成', '投线' ];
	chart1 = new Highcharts.Chart({
		colors : [ '#D0D0D0', '#D0D0D0', '#D0D0D0' ],
		chart : {
			style : {
				fontFamily : '"Microsoft YaHei", SimHei, STHeiti, "Lucida Grande", "Lucida Sans Unicode", Verdana, Arial, Helvetica, sans-serif', // default font
				fontSize : '12px'
			},
			renderTo : 'performance_container',
			type : 'column',
			backgroundColor :'#fff',
			height : 280,
			width : 600,
			marginLeft : 50,
			marginRight : 40
		},
		labels : {
			style : {
				fontWeight : 'bold',
				fontSize : '20px',
				color : 'black'
			}
		},
		credits : {
			enabled : false
		},
		title : {
			text : '现品管理/受理报价-实绩'
		},
		xAxis : {
			categories : xCategories1,
			labels : {
				style : {
					fontWeight : 'bold',
					fontSize : '18px',
					color : 'black'
				},
				y:26
			}
//		},
//			plotBands : [ { // 121 - 141
//				from : 1.5,
//				to : 4.5,
//				color : 'rgba(68, 170, 213, .2)'
//			} ]
		},
		yAxis : {
			title : {
				text : '台数',
	
				style : {
					fontWeight : 'bold',
					fontSize : '18px'
				}
			}
		},
		tooltip : {
			animation : true,
			crosshairs : true,
			shared : true,
			formatter : function() {
				if (this.points[0].y > 0) {
					if (this.points[2] != null){
						return '<b>'
								+ this.points[2].series.name
								+ '</b><br/>'
								+ (this.points[0].y + this.points[1].y + this.points[2].y)
								+ '台<br/>'
								+ '<b>其中<br/>S1</b>'
								+ this.points[0].y + '台<br/>'
								+ '<b>S2+S3</b>'
								+ this.points[1].y + '台<br/>'
								+ '<b>D+M</b>'
								+ this.points[2].y + '台<br/>'
								+ '<b>E</b>'
								+ this.points[3].y + '台';
					} else if (this.points[1] != null){
						var setsmei = "其中直送";
						if(xCategories1.indexOf(this.x) == 0){
							setsmei = "其中直送";
						} else if(xCategories1.indexOf(this.x) == 2){
							setsmei = "其中进行中";
						} else if(xCategories1.indexOf(this.x) == 5){
							setsmei = "其中S1";
						}
						return '<b>'
								+ this.points[1].series.name
								+ '</b><br/>'
								+ (this.points[0].y + this.points[1].y)
								+ '台<br/>'
								+ '<b>' + setsmei + '</b><br/>'
								+ this.points[0].y
								+ '台';
					}  else {
						return '<b>'
								+ this.points[0].series.name
								+ '</b><br/>'
								+ (this.points[0].y)
								+ '台';
					}
				} else if(this.points[1]) {
					return '<b>'
							+ this.points[1].series.name
							+ '</b><br/>'
							+ this.points[1].y
							+ '台<br/>';
				}
			},
			style : {
				fontWeight : 'bold',
				fontSize : '12px'
	
			},
			labels : {
				style : {
					fontWeight : 'bold',
					fontSize : '20px'
	
				}
			}
		},
		plotOptions : {
			series : {
				cursor : 'pointer',
                dataLabels: {
                    enabled: true,
                    style: {
                        fontWeight: 'bold',
                        fontSize : '20px',
                        color: '#343264'
                    }
                },
				point : {
					events : {
						click : function() {
						}
					}
				},
				marker : {
					lineWidth : 1
				},
				stacking : 'normal',
				animation : {
                    duration: 2000,
                    easing: 'easeOutBounce'
                }
	
			},
			column : {
				dataLabels : {
					enabled : true,
					formatter : function() {
						return (this.y == 0) ? ''
								: this.y;
					}
				},
				marker : {
					radius : 4,
					lineColor : '#666666',
					lineWidth : 1
				}
			}
		},
		legend : {
			enabled : false
		},
		exporting : {
			enabled : false
		},
		series : [ {
			name : '直送',
			data : ${series1_2}
		}, {
			name : '台数',
			data : ${series1_1}
		},{
			name : '台数',
			data : ${series1_3}
		},{
			name : '台数',
			data : ${series1_4}
		}]
	});

	chart2 = new Highcharts.Chart({
		colors : [ {linearGradient: { x1: 0, x2: 0, y1: 0, y1: 1 }, stops: [[0, '#cc76cc'], [1, '#993999']]} ,
		{linearGradient: { x1: 0, x2: 0, y1: 0, y1: 1 }, stops: [[0, '#cc76cc'], [1, '#993999']]},
		{linearGradient: { x1: 0, x2: 0, y1: 0, y1: 1 }, stops: [[0, '#cc76cc'], [1, '#993999']]},
		{linearGradient: { x1: 0, x2: 0, y1: 0, y1: 1 }, stops: [[0, '#3399FF'], [1, '#3333FF']]}],
			chart : {
				renderTo : 'waiting_container',
				type : 'column',
				backgroundColor : '#fff',
				height : 280,
				width : 600,
				marginLeft : 50,
				marginRight : 40
			},
			credits : {
				enabled : false
			},
			title : {
				text : '现品管理/受理报价-待处理品'
			},
			xAxis : {
				categories : [ '在途','等待<br>消毒','等待<br>灭菌','等待<br>报价','品保分析', '临时放入<br>WIP', '等待<br>投线' ],
				labels : {
					style : {
						fontWeight : 'bold',
						fontSize : '16px',
						color : 'black'
					},
					y:26
				}
			},
			yAxis : {
				title : {
					text : '台数',
					style : {
						fontWeight : 'bold',
						fontSize : '18px'
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
						fontSize : '18px'
					}
				}
			},
			plotOptions : {
				series : {
	                animation: {
	                    duration: 400
	                },
	                dataLabels: {
	                    enabled: true,
	                    style: {
	                        fontWeight: 'bold',
	                        fontSize : '20px',
	                        color: '#400040'
	                    }
 	                },
                	stacking : 'normal',
					cursor : 'pointer',
					borderRadius: 5 
				},
				column : {
					dataLabels : {
						enabled : true
					},
					borderWidth :4,
					borderColor : '#FFFFC0',
					// shadow：2,
					marker : {
						radius : 4,
						lineColor : '#666666',
						lineWidth : 1
					}
				}
			},
			legend : {
				enabled : false
			},
			exporting : {
				enabled : false
			},
			series : [ {
				name : 'S2',
				data : ${series2_2}
			}, {
				name : '台数',
				data : ${series2_1}
			},{
				name : 'D+M',
				data : ${series2_3}
			},{
				name : 'E',
				data : ${series2_4}
			} ]
		});

		setInterval(refresh, 20000);
		
		function refresh() {
			// Ajax提交
			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url : 'acceptFact.scan?method=refresh',
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
						eval("chart1.series[0].setData(" + resInfo.series1_2 + ", false)");
						eval("chart1.series[1].setData(" + resInfo.series1_1 + ")");
						eval("chart1.series[2].setData(" + resInfo.series1_3 + ")");
						eval("chart1.series[3].setData(" + resInfo.series1_4 + ")");
						eval("chart2.series[0].setData(" + resInfo.series2_2 + ", false)");
						eval("chart2.series[1].setData(" + resInfo.series2_1 + ")");
						eval("chart2.series[2].setData(" + resInfo.series2_3 + ")");
						eval("chart2.series[3].setData(" + resInfo.series2_4 + ")");

						eval("chartW1.series[0].setData(" + resInfo.series3_1 + ")");
						eval("chartW2.series[0].setData(" + resInfo.series4_1 + ")");
						eval("chartW1_1.series[0].setData(" + resInfo.series3_2 + ")");
						eval("chartW2_1.series[0].setData(" + resInfo.series4_2 + ")");

						if (resInfo.countFrailized > 0) {
							$("#sterilize_legend > div").css({"background": "-moz-linear-gradient(top,  #AABEE4 0%, #2125BE 100%)", "color": "midnightblue"});
						} else {
							$("#sterilize_legend > div").css({"background": "-moz-linear-gradient(top,  #FFFF33 0%, #F0F000 100%)", "color": "maroon"});
						}

					} catch(e) {
						// alert('c');
					}
				}
			});
		}
	}

if (typeof(jQuery) === "undefined") {
	loadCss("css/custom.css");
	loadCss("css/olympus/jquery-ui-1.9.1.custom.css", function(){
		loadJs("js/jquery-1.8.2.min.js", function(){
			$("body").addClass("scan1024");
			loadJs("js/jquery-ui-1.9.1.custom.min.js", function(){
				loadJs("js/jquery-plus.js", function(){
					loadJs("js/highcharts.js", iamreadyAf);
				});
			});
		});
	});
} else {
	iamreadyAf();
}
</script>