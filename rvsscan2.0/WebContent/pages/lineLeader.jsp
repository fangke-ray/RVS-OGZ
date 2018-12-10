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

<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/highcharts.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/scan/line_leader.js"></script>

<title>${section_name} ${line_name}</title>
</head>
<body class="outer scan1024">
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
<div id="workarea" class="dwidth-half" style="float:left;margin-left:14px;margin-bottom:16px;">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">今日${section_name} ${line_name} ${plan_name}</span>
	</div>
	<div class="ui-widget-content">
		<table id="performance_list"></table>
	</div>
	<div class="ui-state-default ui-corner-bottom areaencloser"></div>
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