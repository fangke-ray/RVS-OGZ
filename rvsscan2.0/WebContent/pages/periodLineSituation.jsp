<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<!DOCTYPE html><html>
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
<!--link rel="stylesheet" type="text/css" href="css/hexaflip.css"-->
<style>
.scan1024 .td-content {
	width : auto;
	padding: 1px;
	padding-left: 3px;
}

.scan1024 table {
	background-color : white;
}
span.areacount {
	float : right;
	font-size:12px;
	margin: .3em .4em .2em .3em;
	background-color : white;
	padding: .1em .3em .1em .3em;
}
td.low {
	color:#FF4F4F;
}
#sikake_in {
	font-size:12px;
}
#plan_complete div.marqueue {
	font-size: 16px;
}
#plan_complete div.marqueue .model {
	font-family: Georgia;
	float:left;
	margin-left: 0.2em;
	line-height: 1.6em;
}
#plan_complete div.marqueue .quantity {
	float:right;
	background-color: darkgray;
	color:white;
	background-color: gray;
	padding-right: 0.2em;
	padding-left:0.4em;
	border-top-left-radius:20px 12px;
	font-size:28px;
}
#plan_complete div.marqueue .quantity span {
	font-size:16px;
}
#plan_complete #plan_count div.marqueue .quantity {
	background-color: navy;
}
#plan_complete div.marqueue .quantity[inplan] {
	background-color: rgb(0,170,128);
}
#plan_complete div.marqueue .quantity[finish] {
	background-color: rgb(100,120,0);
}
#processing_container .highcharts-axis-labels > text > tspan:nth-child(2) {
	font-size : 12px;
}
span.out_major {
	background-color: rgb(0, 128, 192);
	padding: 0 4px;
}
span.out_minor {
	background-color: rgb(204, 118, 204);
	padding: 0 4px;
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
<script type="text/javascript" src="js/donuts.js"></script>
<script type="text/javascript" src="js/scan/line_situation3.js"></script>

<div class="width-full" style="align:center;margin:auto;margin-top:16px;">
<div id="basearea" class="dwidth-full" style="margin:auto;">
</div>
<script type="text/javascript">
	$("#basearea").load("widgets/header.jsp",
		function(responseText, textStatus, XMLHttpRequest) {
		$("#moduleName").text("工程展示");
	});
</script>

<div class="ui-widget-panel width-full" style="align:center;padding-top:16px;overflow-x: hidden;margin:auto;transform-origin:center top 0;transform:scaleY(.97);" id="body-3">
	
	<div class="ui-widget-header dwidth-full" style="align:center;padding-top:6px;padding-bottom:6px;margin-bottom:16px;text-align:center;">
		<span>${section_name} ${line_name}</span>
	</div>

<div style="width:1900px;">
<div id="workarea" class="dwidth-half" style="float:left;margin-left:14px;margin-bottom:16px;font-weight: bolder;">
	<div id="plan_complete">
		<div style="float:left">
			<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;margin-left : 16px;">
				<span class="areatitle">今日计划台数</span>
			</div>
			<div id="plan_count" style="width : 126px;height : 126px; background-color:white; border: 12px solid #92D050; margin-left : 16px;-webkit-box-shadow: 0 -1px 2px #292929;">
			</div>
		</div>
		<div style="float:left">
			<div class="donut donut-big" style="margin-top: 45px; margin-left : 12px;">
			    <div class="donut-arrow" data-percentage="0"></div>
			</div>
			<div id="completed_rate" style="
font-size: 350%;
margin: 16px 0 0 8px;
font-family: Georgia;
background: rgba(0, 0, 128, 0.4);
text-align: center;
width: 3em;
border: 4px solid rgb(255, 255, 255);
left: 10px;
border-radius: 14px;display:none;">0%</div>
			<div class="clear"></div>
		</div>
		<div style="float:left">
			<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;margin-left : 8px;">
				<span class="areatitle">今日产出台数</span>
			</div>
			<div id="plan_finish" style="height: 148px;transform-origin: top;">
				<div id="plan_finish_count" style="width : 126px;height : 126px; background-color:white; border: 12px solid #0080C0; margin-left : 8px;-webkit-box-shadow: 0 -1px 2px #292929;">
				</div>
				<div id="plan_finish_dm_count" style="display:none; width : 126px;height : 126px; background-color:white; border: 12px solid #cc76cc; margin-left : 8px;-webkit-box-shadow: 0 -1px 2px #292929;">
				</div>
			</div>
			<div class="clear"></div>
		</div>
		<div class="clear"></div>
	</div>
	


<div id="step_process" style="width:100%;height:240px;">
</div>


<div id="today_plan_outline">
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half" style="width : 450px;margin-left : 16px;margin-top : 8px;">
			<span class="areatitle">计划进展情况</span>
</div>
		


<table class="plan_process_grid" style="margin-left : 16px;width: 450px; height: 136px;border: 1px solid #808080;color: black;text-align:center;">

<thead>
<tr>

	<th class="ui-state-default" style="width:60px;">时段</th>
	<th class="ui-state-default" style="width:30px;">计划台数</th>

	<th class="ui-state-default" style="width:30px;">产出台数</th>

	<th class="ui-state-default" style="width:30px;">时段达成率</th>

	<th class="ui-state-default" style="width:30px;">累计达成率</th>

</tr>

</thead>
<tbody>
<tr>
	<td class="td-content"><label style="display: inline;">8:00~10:00</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content">
		<label style="display: inline;">-</label>
	</td>
</tr>

<tr style="background-color:#f0eaf0;">
	<td class="td-content"><label style="display: inline;">10:00~12:00</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>
	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>
		
	<td class="td-content">
		<label style="display: inline;">-</label>
	</td>
</tr>

<tr>
	<td class="td-content"><label style="display: inline;">13:00~15:00</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content">
		<label style="display: inline;">-</label>
	</td>
</tr>


<tr style="background-color:#f0eaf0;">
	<td class="td-content"><label style="display: inline;">15:00~17:15</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content"><label style="display: inline;">-</label></td>

	<td class="td-content">
		<label style="display: inline;">-</label>
	</td>
</tr>

</tbody>
</table>

</div>
	<div class="clear"><input type="hidden" id="page_line_id" value="${line_id}" /><input type="hidden" id="page_section_id" value="${section_id}" /></div>
</div>

<div id="storagearea" style="float:left;margin-left:16px;margin-bottom:16px;">
	<div>
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half">
			<span class="areatitle">${section_name} ${line_name} 当前仕挂台数</span>
			<input type="hidden" id="lm_tag" value="${lm_tag}"/>
		</div>
		<div class="ui-widget-content dwidth-half" style="text-align:center;padding-top:8px;padding-bottom:8px;cursor: pointer;">
			<span style="font-size:16px;"><label id="sikake"></label> 台<label id="sikake_in"></label></span>
		</div>
		<div class="clear areaencloser dwidth-half"></div>
	</div>

	<div>
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half">
			<span class="areatitle">${section_name} ${line_name} 当前仕挂分布</span>
		</div>
		<div class="ui-widget-content dwidth-half">
			<div id="processing_container"></div>
		</div>
		<div class="ui-state-default ui-corner-bottom areaencloser dwidth-half"></div>
		</div>
	</div>
	<div class="clear"/>
	</div>

</div>
</div>

</body></html>