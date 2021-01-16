<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">
<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">
<style>
pil_tag {
	display: inline-block;
	padding-top:1.33em;
	border: 1px dashed navy;
	background-color: lightskyblue;
	position: relative;
}
pil_tag:before{
	content: attr(key);
	top:0em;
	position: absolute;
}
pil_tag > input{
	width: 4em;
	border-color: transparent;
	text-align:center;
}
pil_tag > input:invalid {
	border-color: transparent;
	box-shadow: none;
}
.pil_lower:valid {
	background-color: greenyellow;
}
.pil_upper:valid {
	background-color: violet;
}
pil_tag > checkbox {
	width:10px;
	height:10px;
	border: 1px solid navy;
	background-color: darkblue;
	position: absolute;
	right:2px;
	top:2px;
	cursor:pointer;
	font-size: 8px;
}
pil_tag > checkbox[value="0"] {
	background-color: yellow;
}
</style>
<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/admin/pcs_input_limit.js"></script>
<title>工程检查票输入值标准设定</title>
</head>
<body class="outer">
<div class="width-middleright" style="align:center;margin:auto;margin-top:16px;">


<div id="searcharea">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
		<span class="areatitle">检索条件</span>
		<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
			<span class="ui-icon ui-icon-circle-triangle-n"></span>
		</a>
	</div>
	<div class="ui-widget-content dwidth-middleright">
		<form id="searchform" method="POST" onsubmit="return false;">
			<table class="condform">
				<tr>
					<td class="ui-state-default td-title" rowspan="2">工程检查票类型</td>
					<td class="td-content" rowspan="2" style="width: 300px;">
						<select name="section" id="search_line_type" class="ui-widget-content" style="display: none;">
						${lLineType}
						</select>
					</td>
					<td class="ui-state-default td-title">模板文档名</td>
					<td class="td-content">
						<input type="text" id="search_file_name" maxlength="100" class="ui-widget-content">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">针对型号</td>
					<td class="td-content">
						<input type="text" id="search_target_model" maxlength="100" class="ui-widget-content">
						<input type="hidden" id="search_target_model_id">
					</td>
				</tr>
			</table>
			<div style="height:44px">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="resetbutton" value="清除" role="button" aria-disabled="false" style="float:right;right:2px">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="searchbutton" value="检索" role="button" aria-disabled="false" style="float:right;right:2px">
			</div>
		</form>
	</div>
	<div class="clear dwidth-middleright"></div>
</div>

<div id="listarea" class="width-middleright">
	<table id="list"></table>
	<div id="listpager"></div>
</div>


</div>

<div id="pcs_content_container">
	<div id="pcs_models"></div>
	<div id="pcs_content"></div>
</div>

	<div class="referchooser ui-widget-content" id="model_refer" tabindex="-1" style="z-index:1100;">
	<table>
		<tr>
			<td width="50%">过滤字:<input type="text"/></td>	
			<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
		</tr>
	</table>

	<table class="subform">${mReferChooser}</table>
	</div>
</body></html>