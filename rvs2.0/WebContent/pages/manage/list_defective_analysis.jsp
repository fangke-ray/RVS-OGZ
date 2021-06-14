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
<script type="text/javascript" src="js/manage/list_defective_analysis.js"></script>
<script type="text/javascript" src="js/common/defective_analysis_detail.js"></script>
<title>不良对策信息查询</title>
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
			<jsp:param name="linkto" value="进度查询"/>
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
						<td class="ui-state-default td-title">维修单号</td>
						<td class="td-content">
							<input type="text" id="omr_notifi_no" name="omr_notifi_no" maxlength="15" class="ui-widget-content">
						</td>
						<td class="ui-state-default td-title">不良提出日</td>
						<td class="td-content">
							<input type="text" name="sponsor_date_from" id="sponsor_date_from" class="ui-widget-content" value="${today}">起<br/>
							<input type="text" name="sponsor_date_to" id="sponsor_date_to" class="ui-widget-content">止
						</td>
						<td class="ui-state-default td-title">对策实施日</td>
						<td class="td-content">
							<input type="text" name="cm_proc_confirmer_date_from" id="cm_proc_confirmer_date_from" class="ui-widget-content">起<br/>
							<input type="text" name="cm_proc_confirmer_date_to" id="cm_proc_confirmer_date_to" class="ui-widget-content">止
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">不良分类</td>
						<td class="td-content" colspan="5">
							<select name="defective_type" id="defective_type" class="ui-widget-content">${defectiveTypeOptions}</select>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">对策进度</td>
						<td class="td-content" colspan="5">
							<select name="step" id="step" class="ui-widget-content">${defectiveStepOptions}</select>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">工程</td>
						<td class="td-content" colspan="5">
							<select name="line_id" id="line_id" class="ui-widget-content">${lOptions}</select>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">责任区分</td>
						<td class="td-content">
							<select name="responsibility_of_ptl" id="responsibility_of_ptl" class="ui-widget-content">${defectiveResponsibilityOfPtlOptions}</select>
						</td>
						<td class="ui-state-default td-title">风险大小等级</td>
						<td class="td-content">
							<select name="capa_risk" id="capa_risk" class="ui-widget-content">${defectiveCapaRiskOptions}</select>
						</td>
						<td class="ui-state-default td-title">返工对应</td>
						<td class="td-content">
							<select name="rework_proceed" id="rework_proceed" class="ui-widget-content">${defectiveReworkProceedOptions}</select>
						</td>

					</tr>
				</tbody>
			</table>
			<div style="height:44px">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="resetbutton" value="清除" role="button" aria-disabled="false" style="float:right;right:2px">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="searchbutton" value="检索" role="button" aria-disabled="false" style="float:right;right:2px">
				<input type="hidden" id="department" value="${userdata.department}">
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

</body>
</html>