<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
<meta name="apple-mobile-web-app-capable" content="yes" />

<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/scan/lineTimespace.css">
<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/highcharts.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/scan/partial_warehouse.js"></script>

<style type="text/css">
#axis_base {
	width: 90%;
}

#axis_base .y_column {
	width: 70px;
}

${standard_column.css}

#axis_base .y_columns {
	display: flex;
}

#axis_base .operator_flex {
	display: flex;
	justify-content: space-between;
	flex: 10;
	padding-left: 1em;
}

#axis_base .operator_flex .y_column {
	position: relative;
	flex: 1;
	margin: 0 2px 0 2px;
}

#axis_base #standard_column {
	position: relative;
	flex: 1;
}

#axis_base .operator_flex .production_feature {
	width: 30%;
	box-shadow: none;
	margin-left: 33%;
}

/* A、收货 */
#axis_base .operator_flex .production_feature[d_type="10"] {
	background-color: #00B0F0;
}
/* B1、核对+上架 */
#axis_base .operator_flex .production_feature[d_type="20"] {
	background-color: #FF7C80;
}
/* B2、核对 */
#axis_base .operator_flex .production_feature[d_type="21"] {
	background-color: #FF7C80;
}
/* C、分装 */
#axis_base .operator_flex .production_feature[d_type="30"] {
	background-color: #FFC000;
}

/* D、上架 */
#axis_base .operator_flex .production_feature[d_type="40"] {
	background-color: #FFFF00;
}

/* E1、NS 出库 */
#axis_base .operator_flex .production_feature[d_type="50"] {
	background-color: #00FF00;
}

/* E2、分解出库 */
#axis_base .operator_flex .production_feature[d_type="51"] {
	background-color: #00FF00;
}

/* O、其它 */
#axis_base .operator_flex .production_feature[d_type="99"] {
	background-color: #A162D0;
}

#axis_base .y_column:before{
	display: none;
}

#legend {
	top: 110px;
	left: 0;
	position: absolute;
	width: 200px;
	z-index: 2;
	display: none;
}

.left {
	float: left;
	box-sizing: border-box;
}

.right {
	float: right;
	box-sizing: border-box;
}

.progress {
	background-color: #DBEEF4;
}

.waiting {
	width: 360px;;
}

.flex-box {
	display: flex;
	height: 30px;
	line-height: 30px;
	font-size: 16px;
	border: 1px solid #78C1D4;
}

.flex-box .item {
	flex: 1;
	text-align: center;
	border-right: 1px solid #78C1D4;
	box-sizing: border-box;
}

.flex-box .item:last-child {
	border-right: none;
	text-align: right;
}

.notice {
	position: absolute;
	top: -8px;
	left: -5px;
	display: inline-block;
	width: 30px;
	height: 30px;
	line-height: 30px;
	border-radius: 50%;
	background-color: #96DEF9;
	color: #EEA569;
	text-align: center;
	font-size: 18px;
}

.time {
	color: #EEA569;
	padding-right: 2px;
	font-size: 15px;
}

.result {
	display: flex;
	font-size: 16px;
	border: 1px solid #FFF;
	height: 20px;
	line-height: 20px;
	font-size: 13px;
}

.result .item {
	flex: 1;
	border-right: 1px solid #FFF;
	box-sizing: border-box;
}

.result .item.low{
	color:red;
}

.result .item.over{
	color:#4ABD62;
}

.result .item:last-child {
	border-right: none;
}

.result:first-child {
	height: 34px;
	line-height: 34px;
	background-color: #000;
	color: #fff;
	text-align: center;
	font-size: 17px;
}

.result:nth-child(n+2) .item:nth-child(2),.item:nth-child(4) {
	background-color: #E9EDF4;
	text-align: center;
}

.result:nth-child(n+2) .item:nth-child(3),.item:nth-child(5) {
	background-color: #FADC90;
	text-align: center;
}

.result:last-child {
	height: 34px;
	line-height: 34px;
	color: #000;
	text-align: center;
	font-size: 13px;
}

.result .item .per {
	height: 50%;
	line-height: 17px;
}

.result .item .per.low {
	color:red;
}

.result .item .per.over {
	color:orange;
}

.wait .wait-box:nth-child(odd) {
	background-color: #CFD6E6;
}
.wait .wait-box:nth-child(even){
	background-color: #E9EDF4;
}
.wait-box{
	height: 34px;
	line-height:34px;
	display: flex;
	border-bottom: 2px solid #FFF;
	color:#000;
	font-size: 16px;
}
.wait-box:last-child{
	border-bottom: none;
}
.wait-box .item{
	box-sizing: border-box;
	flex:1;
	border-right: 2px solid #FFF;
}
.wait-box .item:last-child{
	border-right: none;
}

.item label{
	margin:0 0 0 4px;
}
</style>
<title>零件出入库工时展示</title>
</head>
<body class="outer scan1024">
	<div style="align: center; margin: auto;">
		<div class="ui-widget-panel dwidth-full" style="align: center; margin: auto; padding: 16px;" id="body-3">

			<div class="left" style="width: 39%;">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					<span class="areatitle">仓管人员工时图</span>
				</div>
				<div class="ui-widget-content" style="position: relative;">
					<div id="performance_container">
						<div id="axis_base">
							<div id="time_axis">
								<div class="time_axis_point" style="bottom: -2.5px;">&nbsp;8:00</div>
								<div class="time_axis_point" style="bottom: 122.5px;">10:00</div>
								<div class="time_axis_point" style="bottom: 242.5px;">12:00</div>
								<div class="time_axis_point" style="bottom: 302.5px;">13:00</div>
								<div class="time_axis_point" style="bottom: 422.5px;">15:00</div>
								<div class="time_axis_point" style="bottom: 542.5px;">17:00</div>
								<div class="time_axis_point overwork" style="bottom: 660px;">19:00</div>
							</div>

							<div class="y_columns" style="height: 100%">
								<div class="y_column" id="standard_column">
									<div class="position_intro">计划</div>
									<div>${standard_column.divHtml}</div>
									<div id="now_period"></div>
								</div>
								<div class="operator_flex"></div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<div class="right" style="width: 60%;">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					<span class="areatitle">仓管人员工作当前进度</span>
				</div>
				<div class="ui-widget-content" style="margin-bottom: 6px;" id="current_process">
					<div class="flex-box">
						<div class="item"></div>
						<div class="item" style="flex: 2;"></div>
						<div class="item"></div>
						<div class="item" style="position: relative;">
							<span class="notice"></span><span class="time"></span>
						</div>
					</div>
					<div class="flex-box" style="background-color: #DBEEF4;">
						<div class="item" style="flex: 4;">
							<div class="waiting tube" id="p_rate" style="height: 20px; margin: auto; margin-top: 3px;"></div>
						</div>
						<div class="item">
							<span class="time"></span>
						</div>
					</div>
					<div class="flex-box">
						<div class="item"></div>
						<div class="item" style="flex: 2;"></div>
						<div class="item"></div>
						<div class="item" style="position: relative;">
							<span class="notice"></span><span class="time"></span>
						</div>
					</div>
					<div class="flex-box" style="background-color: #DBEEF4;">
						<div class="item" style="flex: 4;">
							<div class="waiting tube" id="p_rate" style="height: 20px; margin: auto; margin-top: 3px;"></div>
						</div>
						<div class="item">
							<span class="time"></span>
						</div>
					</div>
				</div>
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					<span class="areatitle">仓管人员工作今日成果</span>
				</div>
				<div class="ui-widget-content" style="margin-bottom: 6px;" id="resultarea">
					<div class="result">
						<div class="item"></div>
						<div class="item" style="flex: 2;"></div>
						<div class="item" style="flex: 2;"></div>
					</div>
					<div class="result">
						<div class="item" style="background-color: #00B0F0;">A、收货</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
					</div>
					<div class="result">
						<div class="item" style="background-color: #FF7C80;">B1、核对+上架</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
					</div>
					<div class="result">
						<div class="item" style="background-color: #FF7C80;">B2、核对</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
					</div>
					<div class="result">
						<div class="item" style="background-color: #FFC000;">C、分装</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
					</div>
					<div class="result">
						<div class="item" style="background-color: #FFFF00;">D、上架</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
					</div>
					<div class="result">
						<div class="item" style="background-color: #00FF00;">E1、NS 出库</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
					</div>
					<div class="result">
						<div class="item" style="background-color: #00FF00;">E2、分解出库</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
					</div>
					<div class="result">
						<div class="item" style="background-color: #A162D0;">O、其它</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
						<div class="item">一</div>
					</div>
					<div class="result">
						<div class="item" style="background-color: #000; color: #fff;">合计</div>
						<div class="item" style="background-color: #0070C0;">
							<div class="per">负荷率：</div>
							<div class="per">一</div>
						</div>
						<div class="item">
							<div class="per">能率：</div>
							<div class="per">一</div>
						</div>
						<div class="item" style="background-color: #0070C0;">
							<div class="per">负荷率：</div>
							<div class="per">一</div>
						</div>
						<div class="item">
							<div class="per">能率：</div>
							<div class="per">一</div>
						</div>
					</div>
				</div>
				
				<div class="ui-widget-content">
					<div style="float:left;width: 49.9%;">
						<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
							<span class="areatitle">待处理入库单一览</span>
						</div>
						<div class="ui-widget-content wait" id="wait2">
							<div class="wait-box">
								<div class="item"><label></label></div>
								<div class="item" style="flex:2;"><label></label></div>
							</div>
							<div class="wait-box">
								<div class="item"><label></label></div>
								<div class="item" style="flex:2;"><label></label></div>
							</div>
							<div class="wait-box">
								<div class="item"><label></label></div>
								<div class="item" style="flex:2;"><label></label></div>
							</div>
							<div class="wait-box">
								<div class="item"><label></label></div>
								<div class="item" style="flex:2;"><label></label></div>
							</div>
						</div>
					</div>
					<div style="float:right;width: 49.9%;">
						<div class="ui-widget-content wait" id="wait">
							<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
								<span class="areatitle">待处理出库单一览</span>
							</div>
							<div class="wait-box">
								<div class="item"><label></label></div>
								<div class="item" style="flex:2;"><label></label></div>
							</div>
							<div class="wait-box">
								<div class="item"><label></label></div>
								<div class="item" style="flex:2;"><label></label></div>
							</div>
							<div class="wait-box">
								<div class="item"><label></label></div>
								<div class="item" style="flex:2;"><label></label></div>
							</div>
							<div class="wait-box">
								<div class="item"><label></label></div>
								<div class="item" style="flex:2;"><label></label></div>
							</div>
						</div>
					</div>
					<div class="clear"></div>
				</div>
			</div>
			<div class="clear"></div>
		</div>
	</div>
</body>
</html>