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

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/manage/process_inspect.js"></script>
<title>作业监察查询</title>
</head>
<body class="outer" style="align: center;">


	<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">
<div id="basearea" class="dwidth-full" style="margin: auto;">
	<jsp:include page="/header.do" flush="true">
		<jsp:param name="part" value="2"/>
	</jsp:include>
</div>
		<div class="ui-widget-panel ui-corner-all width-full" style="align: center; padding-top: 16px;" id="body-3">
	<div id="body-lft" style="width:256px;float:left;">
		<jsp:include page="/appmenu.do" flush="true">
			<jsp:param name="linkto" value="文档管理"/>
		</jsp:include>
	</div>
			<div style="width: 1012px; float: left;">
				<div id="body-mdl" class="dwidth-middleright" style="margin: auto;">
	<div style="margin:auto;">

<div id="searcharea" class="dwidth-middleright">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">检索条件</span>
		<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
			<span class="ui-icon ui-icon-circle-triangle-n"></span>
		</a>
	</div>
	<div class="ui-widget-content">
		<form id="searchform" method="POST">
			<table class="condform">
				<tbody>
					<tr>
						<td class="ui-state-default td-title" rowspan="2">工程</td>
						<td class="td-content" rowspan="2" colspan="3">
							<select name="line_id" id="search_line_id" class="ui-widget-content">${lOptions}</select>
						</td>
						<td class="ui-state-default td-title">作业名</td>
						<td class="td-content">
							<input type="text" name="process_name" id="search_process_name" class="ui-widget-content">
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">监查日</td>
						<td class="td-content">
							<input type="text" name="inspector_date_from" id="search_inspector_date_from" class="ui-widget-content" readonly>起<br/>
							<input type="text" name="inspector_date_to" id="search_inspector_date_to" class="ui-widget-content" readonly>止
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">操作者</td>
						<td class="td-content">
							<input type="text" name="operator_name" id="search_operator_name" class="ui-widget-content" readonly>
							<input type="hidden" name="operator_id" id="search_operator_id">
						</td>
						<td class="ui-state-default td-title">监查者</td>
						<td class="td-content">
							<input type="text" name="inspector_name" id="search_inspector_name" class="ui-widget-content" readonly>
							<input type="hidden" name="inspector_id" id="search_inspector_id">
						</td>
						<td class="ui-state-default td-title">不合格</td>
						<td class="td-content" id="search_unqualified_set">
							<input type="radio" id="unqualified_a" name="unqualified" checked><label for="unqualified_a">(全部)</label>
							<input type="radio" id="unqualified_y" name="unqualified"><label for="unqualified_y">发生</label>
							<input type="radio" id="unqualified_n" name="unqualified"><label for="unqualified_n">未发生</label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">型号</td>
						<td class="td-content">
							<input type="text" class="ui-widget-content" readonly="readonly" id="txt_modelname">
							<input type="hidden" name="model_id" id="search_model_id">
						</td>
						<td class="ui-state-default td-title">机身号</td>
						<td class="td-content">
							<input type="text" name="serial_no" id="search_serial_no" class="ui-widget-content">
						</td>
						<td class="ui-state-default td-title">文档</td>
						<td class="td-content" id="search_history_set">
							<input type="radio" id="file_type_all" name="file_type" checked><label for="file_type_all">(全部)</label>
							<input type="radio" id="file_type_readable" name="file_type"><label for="file_type_readable">数据导入</label>
							<input type="radio" id="file_type_n" name="file_type"><label for="file_type_n">仅文件</label>
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
	<div class="clear areaencloser"></div>
</div>


<div id="listarea" class="dwidth-middleright">
	<table id="list"></table>
	<div id="listpager"></div>
</div>

<div id="detail_dialog">
</div>

<div class="clear areacloser"></div>
</div>
				</div>
			</div>

			<div class="clear areaencloser dwidth-middleright"></div>
		</div>
	</div>

	<!-- 型号 -->
	<div class="referchooser ui-widget-content" id="model_referchooser" tabindex="-1">
			<table width="200px">
				<tr>
					<td></td>
					<td width="50%">过滤字:<input type="text"/></td>
					<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
				</tr>
			</table>
			<table  class="subform">${mReferChooser}</table>
	</div>
	
	<!--作业员 -->
	<div class="referchooser ui-widget-content" id="operator_referchooser" tabindex="-1">
			<table width="200px">
				<tr>
					<td></td>
					<td width="50%">过滤字:<input type="text"/></td>
					<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
				</tr>
			</table>
			<table  class="subform">${oReferChooser}</table>
	</div>

	<!-- 监察员 -->
	<div class="referchooser ui-widget-content" id="inspector_referchooser" tabindex="-1">
		<table width="200px">
			<tr>
				<td></td>
				<td width="50%">过滤字:<input type="text"/></td>
				<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
			</tr>
		</table>
		<table  class="subform">${iReferChooser}</table>
	</div>
</body>
</html>