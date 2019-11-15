<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">

<link rel="stylesheet" type="text/css" href="css/scan/lineTimespace.css">
<style>
#axis_base .y_column {
	width : 80px;
}
#axis_base .y_column:nth-child(1) {
	left:4px;
}
#axis_base .y_column:nth-child(2) {
	left:110px;
}
#axis_base .y_column:nth-child(3) {
	left:206px;
}
#axis_base .y_column:nth-child(4) {
	left:302px;
}
#axis_base .y_column:nth-child(5) {
	left:398px;
}
#axis_base .y_column:nth-child(6) {
	left:494px;
}
#axis_base .y_column:nth-child(7) {
	left:590px;
}
#axis_base .y_column:nth-child(8) {
	left:686px;
}

${standard_column.css}

/**
#axis_base .y_result_column {
	width: 15em;
}
*/

#standard_column > div > div[model_name]:before {
	position : absolute;
	right: 2px;
	content : attr(model_name);
	background-color: rgba(255,255,0,0.85);
	height: 1.4em;
	padding-left:0.2em;
	border-radius: .5em;
}
</style>

<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/scan/line_timespace.js"></script>

<title>${line_name}工程进度管理板</title>
</head>
<body class="outer scan1024">

	<div style="align: center; margin: auto; /* margin-top: 16px; */">

		<div class="ui-widget-panel dwidth-full" style="align: center; margin: auto; padding: 16px;" id="body-3">
			<div id="workarea">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					<span class="areatitle">${line_name}进度管理板</span>
				</div>
				<div class="ui-widget-content" style="position:relative;">
<input type="hidden" id="h_factor" value=3>
					<div id="performance_container">
						<div id="axis_base">
							<div id="time_axis">
								<div class="time_axis_point" style="bottom: -2.5px;">&nbsp;8:00</div>
								<div class="time_axis_point" style="bottom: 62.5px;">&nbsp;9:00</div>
								<div class="time_axis_point" style="bottom: 122.5px;">10:00</div>
								<div class="time_axis_point" style="bottom: 182.5px;">11:00</div>
								<div class="time_axis_point" style="bottom: 242.5px;">12:00</div>
								<div class="time_axis_point" style="bottom: 302.5px;">13:00</div>
								<div class="time_axis_point" style="bottom: 362.5px;">14:00</div>
								<div class="time_axis_point" style="bottom: 422.5px;">15:00</div>
								<div class="time_axis_point" style="bottom: 482.5px;">16:00</div>
								<div class="time_axis_point" style="bottom: 542.5px;">17:00</div>
								<div class="time_axis_point overwork" style="bottom: 602.5px;">18:00</div>
								<div class="time_axis_point overwork" style="bottom: 660px;">19:00</div>
							</div>
							<!--div id="pre_work"></div>
							<div class="rest_work" style="height:10px;bottom:125px;"></div>
							<div class="rest_work" style="height:60px;bottom:245px;"></div>
							<div class="rest_work" style="height:10px;bottom:425px;"></div>
							<div id="suf_work"></div-->
							<div class="y_columns">
								<div class="y_column" id="standard_column">
									<div class="position_intro">计划</div>
									<div>
${standard_column.divHtml}
									</div> 
									<div id="now_period"></div>
								</div>
								<div class="y_column" for="001">
									<div class="position_intro">001</div>
								</div>
								<div class="y_column" for="002">
									<div class="position_intro">002</div>
								</div>
								<div class="y_column" for="003">
									<div class="position_intro">003</div>
								</div>
								<div class="y_column" for="004">
									<div class="position_intro">004</div>
								</div>
								<div class="y_column" for="005">
									<div class="position_intro">005</div>
								</div>
								<div class="y_column" for="006">
									<div class="position_intro">006</div>
								</div>
								<div class="y_result_column">
									<div class="position_intro">结果</div>
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
<input type="hidden" id="line_id" value="${line_id}" />
</body>
</html>
