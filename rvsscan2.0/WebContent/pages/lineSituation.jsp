<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<!DOCTYPE html>
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache"> <meta http-equiv="Cache-Control" content="no-cache">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
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
<script type="text/javascript" src="js/scan/line_situation.js"></script>

<div class="width-full" style="align:center;margin:auto;margin-top:16px;">
<div id="basearea" class="dwidth-full" style="margin:auto;">
</div>
<script type="text/javascript">
	$("#basearea").load("widgets/header.jsp",
		function(responseText, textStatus, XMLHttpRequest) {
		$("#moduleName").text("工程展示");
	});
</script>

<div class="ui-widget-panel width-full" style="align:center;padding-top:16px;overflow-x: hidden;margin:auto;" id="body-3">
	
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
border-radius: 14px">0%</div>
			<div class="clear"></div>
		</div>
		<div style="float:left">
			<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;margin-left : 8px;">
				<span class="areatitle">今日产出台数</span>
			</div>
			<div id="plan_finish_count" style="width : 126px;height : 126px; background-color:white; border: 12px solid #0080C0; margin-left : 8px;-webkit-box-shadow: 0 -1px 2px #292929;">
			</div>
			<div class="clear"></div>
		</div>
		<div class="clear"></div>
	</div>
	<div id="now_nogood">
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaenclose dwidth-halfr" style="width : 450px;margin-left : 16px;margin-top : 8px;">
			<span class="areatitle">中断维修对象</span>
			<span class="areacount" style="color:red;">总数0台</span>
		</div>
		<table class="condform leader_grid" style="margin-left : 16px;width: 450px; height: 78px;border: 1px solid #808080;color:red;">
			<tr style="height:20px;">
				<th class="ui-state-default" style="width:128px;">修理单号</th>
				<th class="ui-state-default">型号</th>
				<th class="ui-state-default" style="width:60px;">机身号</th>
			</tr>
			<tr style="height:20px;">
				<td class="td-content"><label></label></td>
				<td class="td-content"><label></label></td>
				<td class="td-content"><label></label></td>
			</tr>
		</table>
	</div>
	<div id="now_expedited">
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half" style="width : 450px;margin-left : 16px;margin-top : 8px;">
			<span class="areatitle">加急维修对象</span>
			<span class="areacount" style="color:blue;">总数0台</span>
		</div>
		<table class="condform leader_grid" style="margin-left : 16px;width: 450px; height: 76px;border: 1px solid #808080;color:blue;">
			<tr style="height:20px;">
				<th class="ui-state-default" style="width:128px;">修理单号</th>
				<th class="ui-state-default">型号</th>
				<th class="ui-state-default" style="width:60px;">机身号</th>
			</tr>
			<tr style="height:20px;">
				<td class="td-content"><label></label></td>
				<td class="td-content"><label></label></td>
				<td class="td-content"><label></label></td>
			</tr>
		</table>
	</div>
	<div id="today_plan_outline">
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half" style="width : 450px;margin-left : 16px;margin-top : 8px;">
			<span class="areatitle">纳期为今日或之前的维修对象</span>
			<span class="areacount" style="color:#292929;">总数0台</span>
		</div>
		<table class="condform leader_grid" style="margin-left : 16px;width: 450px; height: 136px;border: 1px solid #808080;color: #2A600B;">
			<tr>
				<th class="ui-state-default" style="width:128px;">修理单号</th>
				<th class="ui-state-default">型号</th>
				<th class="ui-state-default" style="width:60px;">机身号</th>
			</tr>
			<tr>
				<td class="td-content"><label></label></td>
				<td class="td-content"><label></label></td>
				<td class="td-content"><label></label></td>
			</tr>
			<tr>
				<td class="td-content" style="width:128px;"><label></label></td>
				<td class="td-content"><label></label></td>
				<td class="td-content" style="width:60px;"><label></label></td>
			</tr>
			<tr>
				<td class="td-content" style="width:128px;"><label></label></td>
				<td class="td-content"><label></label></td>
				<td class="td-content" style="width:60px;"><label></label></td>
			</tr>
		</table>
	</div>
	<div class="clear"><input type="hidden" id="page_line_id" value="${line_id}" /><input type="hidden" id="page_section_id" value="${section_id}" /></div>
</div>

<div id="storagearea" style="float:left;margin-left:16px;margin-bottom:16px;">
	<div>
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half">
			<span class="areatitle">${section_name} ${line_name}当前仕挂台数</span>
		</div>
		<div class="ui-widget-content dwidth-half" style="text-align:center;padding-top:8px;padding-bottom:8px;cursor: pointer;">
			<span style="font-size:16px;"><label id="sikake"></label> 台</span>
		</div>
		<div class="clear areaencloser dwidth-half"></div>
	</div>

	<div>
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half">
			<span class="areatitle">${section_name} ${line_name}当前仕挂分布</span>
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