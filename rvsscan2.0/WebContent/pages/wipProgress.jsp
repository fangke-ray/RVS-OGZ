<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
<meta name="apple-mobile-web-app-capable" content="yes" />
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">
<style>
	.wip-heaped-blink {
		border-width: 2px;
		font-weight: bold;
		-webkit-animation: blink 1s steps(8, end) infinite;
		-moz-animation: blink 1s steps(8, end) infinite;
		animation: blink 1s steps(8, end) infinite;
	}

#label_table td.td-content {
	width:70px;
}
	@keyframes blink {
		0% {border-color : white !important;}
		100% {border-color : red !important;} 
	}
	
	@-webkit-keyframes blink {
		0% {padding-left: 0px; padding-top: 1px;} 
		50% {padding-left: 7px; padding-top: 0px;} 
		100% {padding-left: 0px; padding-top: 1px;} 
	}
	
	@-moz-keyframes blink {
		0% {background-color : white !important; border-color : white !important;color : yellow;} 
		50% {background-color : red !important;border-color : red !important;color : red;} 
		100% {background-color : white !important;border-color : white !important;{color : yellow;} 
	}

</style>

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>

<script type="text/javascript" src="js/scan/wip_progress.js"></script>

<title>WIP库存展示</title>
</head>
<body class="outer scan1024">
	<div class="width-full" style="align: center; margin: auto; margin-top: 24px;">
		<div id="basearea" class="dwidth-full" style="margin: auto; margin-bottom: 12px;"></div>
		<script type="text/javascript">
			$("#basearea").load("widgets/header.jsp",
				function(responseText, textStatus, XMLHttpRequest) {
				$("#moduleName").text("WIP库位展示");
				mapLoaded();
			});
		</script>

		<div id="wiparea" style="width:100%;margin-left:16px;margin-right:24px;">
			<!--div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
				<span class="areatitle">WIP区域一览</span>
			</div-->

			<div class="ui-widget-content" style="position: relative;">
				<div style="width: 524px; float: left; padding-left:16px;"id="storages">
					<div class="clear areacloser"></div>
				</div>
				<script type="text/javascript">
					$("#storages").load("widgets/wip_map.jsp",
						function(responseText, textStatus, XMLHttpRequest) {
					});
				</script>
				<div id="legendarea" style="position: absolute;bottom: 190px;left: 145px;">
   					<table class="condform" id="label_table" style="margin-top: 15px; margin-bottom: 15px;font-size:15px;">
						<tr>
							<td class="ui-widget-header" style="width:80px;text-align:center;">图例</td>
							<td class="ui-storage-highlight" style="width:80px;text-align:center;font-size:10px;">占用<br>两个月以内</td>
							<td class="ui-state-error" style="width:80px;text-align:center;font-size:10px;">占用<br>两个月以上</td>
							<td class="" style="width:80px;text-align:center;font-size:10px;">空闲<br>可存放</td>
						</tr>
					</table>
				</div>
				<div id="sumarea" style="position: absolute; bottom: 0px; left: 146px;">
   					<table class="condform" id="label_table" style="margin-top: 15px; margin-bottom: 15px;font-size:15px;">
						<tr>
							<td class="ui-state-default td-title">总库位数</td>
							<td class="ui-state-default" rowspan="4" style="">普通<br/>内镜</td>
							<td class="td-content"><label /></td>
							<td class="ui-state-default" rowspan="4" style="">Endo<br/>-eye</td>
							<td class="td-content"><label>250 台</label></td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">当前在库台数</td>
							<td class="td-content"><label /></td>
							<td class="td-content"><label /></td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">超期台数</td>
							<td class="td-content"><label/></td>
							<td class="td-content"><label /></td>
						</tr>
						<!--tr>
							<td class="ui-state-default" style="width:80px;text-align:center;"></td>
							<td class="ui-state-default td-title">积荷率</td>
							<td class="td-content"><label /></td>
						</tr-->
						<tr>
							<td class="ui-state-default td-title">超期率</td>
							<td class="td-content"><label /></td>
							<td class="td-content"><label /></td>
						</tr>
					</table>
				</div>
				<div id="listarea" style="float: left; padding-top: 16px;">
					<table id="list"></table>
					<div id="listpager" style="display: none"></div>
					<div class="clear" style="height:4px;"></div>
				</div>
				<div id="checkedShow" style="float: left; position: absolute; bottom: 162px; left: 140px; opacity:0; transition: .5s">
					<table class="subform">
						<thead><th role="columnheader" class="ui-state-default ui-th-column ui-th-ltr" style="width: 64px;"><div id="jqgh_list_sorc_no" class="ui-jqgrid-sortable">修理单号</div></th><th id="list_model_name" role="columnheader" class="ui-state-default ui-th-column ui-th-ltr" style="width: 190px;"><div id="jqgh_list_model_name" class="ui-jqgrid-sortable">型号</div></th><th id="list_serial_no" role="columnheader" class="ui-state-default ui-th-column ui-th-ltr" style="width: 53px;"><div id="jqgh_list_serial_no" class="ui-jqgrid-sortable">机身号</div></th><th id="list_wip_location" role="columnheader" class="ui-state-default ui-th-column ui-th-ltr" style="width: 63px;"><div id="jqgh_list_wip_location" class="ui-jqgrid-sortable">WIP货架位置</div></th></thead>
						<tbody></tbody>
					</table>
				</div>
				<div class="clear"></div>
			</div>
		</div>
	</div>
</body>
</html>