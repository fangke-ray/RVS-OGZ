<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/admin/position.js"></script>
<title>工位检索</title>
</head>
<body>

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
			<tbody>
				<tr>
					<td class="ui-state-default td-title">工位 ID</td>
					<td class="td-content"><input type="text" name="id" id="cond_id" maxlength="11" class="ui-widget-content"></td>
					<td class="ui-state-default td-title">工位名称</td>
					<td class="td-content"><input type="text" name="name" id="cond_name" maxlength="15" class="ui-widget-content"></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">工位代码</td>
					<td class="td-content"><input type="text" name="process_code" id="cond_process_code" maxlength="3" class="ui-widget-content"></td>
					<td class="ui-state-default td-title">所属工程</td>
					<td class="td-content">
						<select name="line_id" id="cond_line_id" class="ui-widget-content">
							<%=request.getAttribute("lOptions")%>
						</select>
					</td>
				</tr>
			</tbody>
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

<div id="editarea" style="display:none;">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
		<span class="areatitle"></span>
		<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
			<span class="ui-icon ui-icon-circle-triangle-w"></span>
		</a>
	</div>
	<div class="ui-widget-content dwidth-middleright">
		<form id="editform" method="POST" onsubmit="return false;">
			<table class="condform">
				<tr>
					<td class="ui-state-default td-title" rowspan=2>所属工程</td>
					<td class="td-content" rowspan=2>
						<select name="line_id" alt="所属工程" id="input_line_id" class="ui-widget-content">
							<%=request.getAttribute("lOptions")%>
						</select>
					</td>
					<td class="ui-state-default td-title">工位 ID</td>
					<td class="td-content"><label id="label_edit_id"> - </label></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">工位代码</td>
					<td class="td-content">
						<input type="text" alt="工位代码" name="process_code" id="input_process_code" maxlength="3" class="ui-widget-content">
						<label id="input_label_process_code" style="display:none"></label>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">工位名称</td>
					<td class="td-content">
						<input type="text" alt="工位名称" name="name" id="input_name" maxlength="20" class="ui-widget-content">
					</td>
					<td class="ui-state-default td-title">进行分线的工位</td>
					<td class="td-content" id="input_light_division_flg"></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">特殊工位页面</td>
					<td class="td-content" colspan=3>
						<select name="special_page" alt="特殊工位页面" id="input_special_page" class="ui-widget-content">
							<%=request.getAttribute("spOptions")%>
						</select>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">线内划分</td>
					<td class="td-content">
						<select name="kind" alt="线内划分" id="input_kind" class="ui-widget-content">
							<%=request.getAttribute("ikOptions")%>
						</select>
					</td>
					<td class="ui-state-default td-title">最后更新时间</td>
					<td class="td-content"><label id="label_edit_updated_time"></label></td>
				</tr>
			</table>
			<div style="height:44px">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="editbutton" value="新建" role="button" aria-disabled="false" style="float:left;left:4px;">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="cancelbutton" value="取消" role="button" aria-disabled="false" style="float:left;left:4px;">
			</div>
		</form>
	</div>
	<div class="ui-state-default ui-corner-bottom areaencloser dwidth-middleright"></div>
</div>
<div id="confirmmessage"></div>
</body>
</html>
