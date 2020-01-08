<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
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
<link rel="stylesheet" type="text/css" href="css/ipad.css">
<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/iscroll-lite.js"></script>
<script type="text/javascript" src="js/scan/scheduled_material.js"></script>

<style type="text/css">
.table {
	margin:0;
	font-size: 20px;
	background-color: white;
	width:100%;
	border-collapse: collapse;
}
.table thead {
	text-align: center;
}
.table tbody tr {
	height: 28px;
	line-height: 28px;
}
.table tbody tr td {
	border: 1px solid lightgray;
	overflow: hidden;
	white-space: nowrap;
}
.table tbody tr td:first-child {
	border-left: 0;
}
.table tbody tr td:last-child {
	border-right: 0;
}
.table td.omr {
	background-color: #060b51;
	color: #fff;
	padding-left: .2em;
	width: 5em;
}
.table td.model {
	padding-left: .2em;
	background-color: #7FAAD4;
	color: #fff;
	width: 13em;
}
.table td.model[kind='3'] {
	background-color: #9933CC;
}
.table td.model[kind='6'] {
	background-color: #E377C2;
}
.table td.model[kind='7'] {
	background-color: #3366CC;
}
.table td.serial_no {
	padding-left: .2em;
	background-color: #92d050;
	color: #fff;
	width: 5em;
}
.table td.process_code {
	background-color: #93c3cd;
	text-align: center;
	width: 4em;
	color: #fff;
}
.table td.level{
	text-align: center;
	color:#fff;
	width: 3em;
}
.table td.level[level='1']{
	background-color: #B5B500;
}

.table td.level[level='2'],.table td.level[level='3']{
	background-color: #00B5B5;
}

.table td.level[level^='9']{
	background-color: #00A843;
}

.table td.level[level^='5']{
	background-color: #3333FF;
}

.table td.status {
	padding-left: .2em;
	width: 15em;
	/* background-color: #fecd7d; */
}
.box {
	position: relative;
}
#tip {
	font-size: 30px;
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
}
.label {
	display: inline;
	padding: .1em .3em .2em;
	font-size: 75%;
	font-weight: 700;
	line-height: 1;
	color: #fff;
	text-align: center;
	white-space: nowrap;
	vertical-align: baseline;
	border-radius: .25em;
}
.label-warning{
	background-color: #f0ad4e;
}
.label-info{
	background-color: #5bc0de;
}
</style>

<title>${today } 纳期维修品一览</title>
</head>
<body class="outer scan1024">
	<div class="dwidth-full" style="margin-top:16px;">
		<div id="basearea"></div>
		<script type="text/javascript">
			$("#basearea").load("widgets/header.jsp",function(responseText, textStatus, XMLHttpRequest) {});
		</script>

		<div class="ui-widget-panel dwidth-full" style="padding-top:6px;">
			<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
				<span class="areatitle" style="font-size: 20px;line-height: 1;" id="introduction">${today } 纳期维修品一览</span>
				<span class="areatitle" style="font-size: 20px;line-height: 1;float: right;" id="sum"></span>
			</div>

			<div class="ui-widget-content">
				<div class="box">
					<table id="title" class="table" style="margin-bottom: 0;margin-top:2px;">
						<thead>
							<tr class="ui-widget-header">
								<td>修理单号</td>
								<td>型号</td>
								<td>机身号</td>
								<td>等级</td>
								<td>当前工位</td>
								<td>状态</td>
							</tr>
						</thead>
					</table>
					<div style="border: 2px solid darkblue; border-top: 0; margin-top: 0;">
						<div style="height: 620px; overflow: hidden;" id="scroll">
							<table id="table" class="table" style="border: 0; margin: 0; width: 100%;">
								<tbody></tbody>
							</table>
						</div>
					</div>
					<div id="tip" style="display: none;">当天计划产出完成</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>