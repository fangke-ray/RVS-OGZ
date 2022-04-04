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
${pocessCodeCss}

${standard_column.css}

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
					<div id="performance_container">
						<div id="axis_base">
							<div id="time_axis">
								<div class="time_axis_point" style="bottom: -2.5px;">&nbsp;8:00</div>
								<div class="time_axis_point" style="bottom: 122.5px;">10:00</div>
								<div class="time_axis_point" style="bottom: 242.5px;">12:00</div>
								<div class="time_axis_point" style="bottom: 422.5px;">15:00</div>
								<div class="time_axis_point" style="bottom: 542.5px;">17:00</div>
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
${pocessCodeHtml}
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
<input type="hidden" id="last_process_code" />
</body>
</html>