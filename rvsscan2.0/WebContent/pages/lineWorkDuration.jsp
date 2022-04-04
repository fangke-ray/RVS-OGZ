<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<!DOCTYPE html>
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link rel="stylesheet" type="text/css" href="css/scan/lineTimespace.css">
<style>
#axis_base .y_column {
	width : 70px;
}

${standard_column.css}
#axis_base .y_columns{
	display: flex;
}

#axis_base .operator_flex {
	display: flex;
	justify-content : space-between;
	flex:10;
	padding-left: 1em;
	height:100%;
	transform-origin: left;
}

#axis_base .operator_flex .y_column{
	position:relative;
	flex:1;
	margin: 0 2px 0 2px;
}

#axis_base #standard_column {
	position:relative;
	flex : 1;
	min-width : 36px;
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
	background-color : #aaa;
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
#legend {
    top: 110px;
    left: 0;
    position: absolute;
    width: 200px;
    z-index: 2;
    display:none;
}
</style>

<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/scan/line_work_duration.js?version=189"></script>

<title>${line_name}工程人员工时图</title>
</head>
<body class="outer scan1024">

	<div style="align: center; margin: auto; /* margin-top: 16px; */">

		<div class="ui-widget-panel dwidth-full" style="align: center; margin: auto; padding: 16px;" id="body-3">
			<div id="workarea">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					<span class="areatitle">${line_name}人员工时图</span>
				</div>
				<div class="ui-widget-content" style="position:relative;">
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

							<div class="y_columns" style="height:100%">
								<div class="y_column" id="standard_column">
									<div class="position_intro">计划</div>
									<div>
${standard_column.divHtml}
									</div> 
									<div id="now_period"></div>
								</div>
								<div class="operator_flex">

								</div>
							</div>
						</div>
					</div>
				</div>
				<div class="clear"></div>
			</div>
			<div class="clear"></div>
		</div>
	</div>

	<div id="legend">
		<div style="background-color: #000000; color:white;">Unknown</div>
		<div style="background-color: #0066CC; color:white;">直接作业工时-特定维修对象</div>
		<div style="background-color: #333399; color:white;">直接作业工时-无特定维修对象</div>
		<div style="background-color: #91C678;">准备工作（5S、会议等）</div>
		<div style="background-color: #998888;">等待指示</div>
		<div style="background-color: #00ADED;">休息</div>
		<div style="background-color: #F3E665;">出勤变动（休假、迟到等）</div>
		<div style="background-color: #F14818;">工作环境问题（断电、RVS故障等）</div>
	</div>

<input type="hidden" id="line_id" value="${line_id}" />
</body>
</html>