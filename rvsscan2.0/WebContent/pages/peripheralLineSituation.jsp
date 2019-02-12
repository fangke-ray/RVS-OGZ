<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache"> <meta http-equiv="Cache-Control" content="no-cache">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
<meta name="apple-mobile-web-app-capable" content="yes" />
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/donuts.css">
<link rel="stylesheet" type="text/css" href="css/scan/lineTimespace.css">
<style>
.left {
	float: left;
	box-sizing: border-box;
	padding-left: 14px;
}

.right {
	float: right;
	box-sizing: border-box;
}

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

.flex{
	display: flex;
}
.item{
	flex:1;
	text-align: center;
	font-size: 20px;
	font-weight: bold;
}
.item:nth-child(2){
	border-top: 1px solid #aaaaaa;
}
.item:nth-child(n+2){
	border-left: 1px solid #aaaaaa;
}
.flex.ui-widget-content{
	height: 36px;
	border-top:0;
}
.flex.ui-widget-content .item{
	line-height: 36px;
}

/* Unknown */
#axis_base .operator_flex .production_feature[d_type="9"] {
	background-color : #000000;
	width : 30%;
	box-shadow : none;
	margin-left : 33%;
}
/* Unknown */
#axis_base .operator_flex .production_feature[d_type="0"] { 
	background-color : #333399;
	width : 30%;
	box-shadow : none;
	margin-left : 33%;
}
/* 直接作业工时-特定维修对象 */
#axis_base .operator_flex .production_feature[d_type="1"] {
	background-color : #0066CC;
}
/* 直接作业工时-无特定维修对象 */
#axis_base .operator_flex .production_feature[d_type="2"] { 
	background-color : #333399;
}

/* 准备工作 */
#axis_base .operator_flex .production_feature[d_type="3"] {
	background-color : #91C678;
	width : 30%;
	box-shadow : none;
	margin-left : 33%;
}
/* 等待指示 */
#axis_base .operator_flex .production_feature[d_type="4"] {
	background-color : #998888;
	width : 30%;
	box-shadow : none;
	margin-left : 33%;
}
/* 休息 */
#axis_base .operator_flex .production_feature[d_type="5"] {
	background-color : #00ADED;
	width : 30%;
	box-shadow : none;
	margin-left : 33%;
}
/* 出勤变动 */
#axis_base .operator_flex .production_feature[d_type="6"] {
	background-color : #F3E665;
	width : 30%;
	box-shadow : none;
	margin-left : 33%;
}
/* 工作环境问题 */
#axis_base .operator_flex .production_feature[d_type="7"] {
	background-color : #F14818;
	width : 30%;
	box-shadow : none;
	margin-left : 33%;
}
</style>

<title>${section_name} ${line_name}</title>
</head>
<body class="outer scan1024">
<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/highcharts.js"></script>
<script type="text/javascript" src="js/jquery.flipCounter.1.2.pack.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/jquery.flipCounter.1.2.pack.js"></script>
<script type="text/javascript" src="js/donuts.js"></script>
<script type="text/javascript" src="js/scan/peripheral_line_situation.js"></script>

<div class="width-full" style="align:center;margin:auto;margin-top:16px;">
<div id="basearea" class="dwidth-full" style="margin:auto;">
</div>
<script type="text/javascript">
	$("#basearea").load("widgets/header.jsp",
		function(responseText, textStatus, XMLHttpRequest) {
		$("#moduleName").text("工程展示");
	});
</script>

<div class="ui-widget-panel width-full" style="align:center;padding-top:16px;overflow-x: hidden;margin:auto;transform-origin:center top 0;transform:scaleY(.98);" id="body-3">
	
	<div class="ui-widget-header dwidth-full" style="align:center;padding-top:6px;padding-bottom:6px;margin-bottom:16px;text-align:center;">
		<span>${section_name} ${line_name}</span>
	</div>

<div style="">
	<div class="left" style="width: 50%;margin-bottom : 16px;">
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
			<span class="areatitle">周边维修人员工时图</span>
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


	<div class="right" style="width: 48%;">
		<div id="storagearea" style="float:left;margin-left:16px;margin-bottom:16px;width: 90%;">
			<div class="flex ui-widget-content">
				<div class="item ui-widget-header">等待报价</div>
				<div class="item"><label id="waiting_quote"></label> 台</div>
			</div>
			<div class="flex ui-widget-content">
				<div class="item ui-widget-header">等待投线</div>
				<div class="item"><label id="waiting_inline"></label> 台</div>
			</div>
			<div class="flex ui-widget-content">
				<div class="item ui-widget-header">等待零件</div>
				<div class="item"><label id="waiting_parts"></label> 台</div>
			</div>
			<div class="flex ui-widget-content">
				<div class="item ui-widget-header">等待维修</div>
				<div class="item"><label id="waiting_repair"></label> 台</div>
			</div>
		</div>
		<div class="clear"></div>
		<div id="device_plan_complete">
			<div style="float:left">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;margin-left : 16px;">
					<span class="areatitle">今日计划台数</span>
				</div>
				<div id="device_plan_count" style="width : 126px;height : 126px; background-color:white; border: 12px solid #92D050; margin-left : 16px;-webkit-box-shadow: 0 -1px 2px #292929;">
				</div>
			</div>
			<div style="float:left">
				<div class="donut donut-big" style="margin-top: 45px; margin-left : 12px;">
				    <div class="donut-arrow" data-percentage="0"></div>
				</div>
				<div id="device_completed_rate" style="
					font-size: 350%;
					margin: 16px 0 0 8px;
					font-family: Georgia;
					background: rgba(0, 0, 128, 0.4);
					text-align: center;
					width: 3em;
					border: 4px solid rgb(255, 255, 255);
					left: 10px;
					border-radius: 14px">0%</div>
				<div class="clear"></div>
			</div>
			<div style="float:left">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;margin-left : 8px;">
					<span class="areatitle">今日产出台数</span>
				</div>
				<div id="device_plan_finish_count" style="width : 126px;height : 126px; background-color:white; border: 12px solid #0080C0; margin-left : 8px;-webkit-box-shadow: 0 -1px 2px #292929;">
				</div>
				<div class="clear"></div>
			</div>
			<div class="clear"></div>
		</div>
		<div class="clear">
		<div id="qawaitingarea" style="float:left;margin-top:16px;margin-left:16px;margin-bottom:16px;width: 90%;">
			<div class="flex ui-widget-content">
				<div class="item ui-widget-header">等待品保</div>
				<div class="item"><label id="waiting_qa"></label> 台</div>
			</div>
			<div class="flex ui-widget-content">
				<div class="item ui-widget-header">等待确认</div>
				<div class="item"><label id="waiting_qa_confirm"></label> 台</div>
			</div>
		</div>
		<div class="clear">
		<div id="today_complete" style="margin-top: 12px;margin-left : 16px;">						
			<div style="float:left">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;">
					<span class="areatitle" style="font-size:13px;margin:7px 20px;">当日通过件数</span>
				</div>
				<div id="today_pass" style="width : 126px;height : 126px; background-color:white; border: 12px solid #0070c0; -webkit-box-shadow: 0 -1px 2px #292929;">
				</div>
				<div class="clear"></div>
			</div>
			<div style="float:left;margin-left:148px;">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;">
					<span class="areatitle" style="font-size:13px;margin:7px 20px;">当日不合格数</span>
				</div>
				<div id="today_unqualified" style="width : 126px;height : 126px; background-color:white; border: 12px solid #ff0000; -webkit-box-shadow: 0 -1px 2px #292929;">
				</div>						
			</div>
	   </div>
	</div>

</div>

</div>
</body></html>