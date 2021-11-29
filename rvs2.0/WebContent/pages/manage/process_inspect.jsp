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
<script type="text/javascript" src="js/ajaxfileupload.js"></script>
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

<%
	Boolean enableEdit = (Boolean)request.getAttribute("enableEdit");
%>

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
							<select name="line_id" id="line_id" class="ui-widget-content">${lOptions}</select>
						</td>
						<td class="ui-state-default td-title">作业名</td>
						<td class="td-content">
							<input type="text" name="process_name" id="process_name" class="ui-widget-content">
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">监查日</td>
						<td class="td-content">
							<input type="text" name="inspect_date_from" id="inspect_date_from" class="ui-widget-content" readonly>起<br/>
							<input type="text" name="inspect_date_to" id="inspect_date_to" class="ui-widget-content" readonly>止
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">操作者</td>
						<td class="td-content">
							<input type="text" name="operator_name" id="operator_name" class="ui-widget-content" readonly>
							<input type="hidden" name="operator_id" id="operator_id">
						</td>
						<td class="ui-state-default td-title">监查者</td>
						<td class="td-content">
							<input type="text" name="inspector_name" id="inspector_name" class="ui-widget-content" readonly>
							<input type="hidden" name="inspector_id" id="inspector_id">
						</td>
						<td class="ui-state-default td-title">不合格</td>
						<td class="td-content" id="search_unqualified_set">
							<input type="radio" id="unqualified_a" name="unqualified" value="" checked><label for="unqualified_a">(全部)</label>
							<input type="radio" id="unqualified_y" name="unqualified" value="1"><label for="unqualified_y">发生</label>
							<input type="radio" id="unqualified_n" name="unqualified" value="0"><label for="unqualified_n">未发生</label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">型号</td>
						<td class="td-content">
							<input type="text" class="ui-widget-content" readonly="readonly" id="txt_modelname">
							<input type="hidden" name="model_id" id="model_id">
						</td>
						<td class="ui-state-default td-title">机身号</td>
						<td class="td-content">
							<input type="text" name="serial_no" id="serial_no" class="ui-widget-content">
						</td>
						<td class="ui-state-default td-title">文档</td>
						<td class="td-content" id="search_file_type_set">
							<input type="radio" id="file_type_all" name="file_type" value="" checked><label for="file_type_all">(全部)</label>
							<input type="radio" id="file_type_readable" name="file_type" value="1"><label for="file_type_readable">数据导入</label>
							<input type="radio" id="file_type_n" name="file_type" value="0"><label for="file_type_n">仅文件</label>
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

<input type="hidden" id="hide_sign_edit" value="${signEdit}">

<% if(enableEdit != null) { %>
<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase" style="padding-top:4px;">
		<%
			String privacy = (String) request.getAttribute("privacy");
			boolean isManager = ("manager").equals(privacy);
		%>
		<% if (isManager) { %>
			<!-- <input type="button" id="uploadSummaryButton" class="ui-button" value="上传历史数据" style="float:right;margin-right:10px;"/> -->
		<% } %>
			<input type="button" id="openAchievementButton" class="ui-button" value="上传作业监查实绩表" style="float:right;margin-right:10px;"/>
			<input type="button" id="openSummaryButton" class="ui-button" value="上传作业汇报" style="float:right;margin-right:10px;"/>

</div>
<% } %>

<div class="clear"></div>

<div id="upload_summary_dialog" style="display:none;">

	<div class="ui-widget-content">
		<table class="condform">
			<tbody>
				<tr>
					<td class="ui-state-default td-title">上传文件</td>
					<td class="td-content" colspan="3">
						<input type="file" name="uploadSummaryFile" id="uploadSummaryFile" class="ui-widget-content" />
						<input type="hidden" name="summary.file_type" id="summary.file_type" value="0">
						<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all"
							id="uploadSummaryButton" value="载入" role="button" aria-disabled="false" style="float: right; right: 2px">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">实施选项</td>
					<td class="td-content">
						<select name="summary.perform_option" id="summary.perform_option" class="ui-widget-content">${performOptions}</select>
					</td>
					<td class="ui-state-default td-title">归档日期</td>
					<td class="td-content">
						<input type="text" name="summary.filing_date" id="summary.filing_date" class="ui-widget-content">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">工程</td>
					<td class="td-content">
						<select name="summary.line_id" id="summary.line_id" class="ui-widget-content">${lOptions}</select>
					</td>
					<td class="ui-state-default td-title">操作者</td>
					<td class="td-content">
						<input type="text" name="summary.operator_name" id="summary.operator_name" class="ui-widget-content" readonly>
						<input type="hidden" name="summary.operator_id" id="summary.operator_id">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">监察者</td>
					<td class="td-content">
						<input type="text" name="summary.inspector_name" id="summary.inspector_name" class="ui-widget-content" readonly>
						<input type="hidden" name="summary.inspector_id" id="summary.inspector_id">
					</td>
					<td class="ui-state-default td-title">监察日</td>
					<td class="td-content">
						<input type="text" name="summary.inspect_date" id="summary.inspect_date" class="ui-widget-content" readonly>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">型号</td>
					<td class="td-content">
						<input type="text" class="ui-widget-content" readonly="readonly" id="summary.model_name">
						<input type="hidden" name="summary.model_id" id="summary.model_id">
					</td>
					<td class="ui-state-default td-title">机身号</td>
					<td class="td-content">
						<input type="text" name="summary.serial_no" id="summary.serial_no" class="ui-widget-content">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">作业时间</td>
					<td class="td-content">
						<input type="text" name="summary.process_seconds" id="summary.process_seconds" class="ui-widget-content">
						<span>分钟</span>
					</td>
					<td class="ui-state-default td-title">标准时间</td>
					<td class="td-content">
						<input type="text" name="summary.standard_seconds" id="summary.standard_seconds" class="ui-widget-content">
						<span>分钟</span>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">监察情况</td>
					<td class="td-content" colspan="3">
						<textarea name="summary.situation" alt="监察情况" id="summary.situation" style="width:618px; height: 64px;"></textarea>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">实施对策</td>
					<td class="td-content" colspan="3">
						<textarea name="summary.countermeasures" alt="实施对策" id="summary.countermeasures" style="width:618px; height: 64px;"></textarea>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">结果</td>
					<td class="td-content" colspan="3">
						<textarea name="summary.conclusion" alt="结果" id="summary.conclusion" style="width:618px; height: 64px;"></textarea>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
	<div style="text-align: right">
		<input type="button" id="saveSummaryButton" class="ui-button" value="确认" />
		<input type="button" id="closeSummaryButton" class="ui-button" value="关闭" />
	</div>
</div>

<div id="upload_achievement_dialog" style="display:none;">

	<div class="ui-widget-content">
		<table class="condform">
			<tbody>
				<tr>
					<td class="ui-state-default td-title">上传文件</td>
					<td class="td-content">
						<input type="file" name="uploadAchievementFile" id="uploadAchievementFile" class="ui-widget-content" />
						<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all"
							id="uploadAchievementButton" value="载入" role="button" aria-disabled="false" style="float: right; right: 2px">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">作业名</td>
					<td class="td-content">
						<input type="text" name="achievement.process_name" id="achievement.process_name" class="ui-widget-content">
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</div>

<div id="detail_dialog" style="display:none;">

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