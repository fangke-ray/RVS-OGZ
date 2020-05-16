<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">

<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">
<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">
<style>
#capacity_setting .dp_pofd {
	float:left;
	width:10em;
	height: 16.2em;
	border:1px solid black;
	overflow:auto;
}
#capacity_setting .dp_pofd > date {
	text-align : center;
	display: block;
	border-bottom:1px solid black;
}
#capacity_setting .dp_pofd > date.holiday {
	color: red;
	background-color: #FFFBD5;
}
#capacity_setting .dp_pofd > date.today {
	color: navy;
	background-color: lightskyblue;
}
#capacity_setting .dp_pofd > plan > div{
	border-bottom:1px solid black;
	clear: both;
}
#capacity_setting .dp_pofd > plan input[type=button]{
	background-color:#93C3CD;
	width: 2em;
	padding: 0;
}
#capacity_setting .dp_pofd > plan model{
	padding-left: 2px;
	float:left;
}
#capacity_setting .dp_pofd > plan input[type=number]{
	background-color:transparent;
	width: 3em;
	padding: 0;
	text-align: right;
	border: 1px solid lightgray;
	float:right;
}
#capacity_model_setting {
	display:none;
	position: fixed;
	background-color: white;
	z-index:2000;
	border: 1px solid lightgray;
	padding : 2px;
}
</style>
<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="js/common/material_detail_ctrl.js"></script>
<script type="text/javascript" src="js/inline/schedule_processing.js"></script>
<title>Racing Area</title>
</head>
<body class="outer">
	<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">

		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="1"/>
			</jsp:include>
		</div>

		<div class="ui-widget-panel ui-corner-all width-full" style="align: center; padding-top: 16px; padding-bottom: 16px;" id="body-1">
			<div id="searcharea" class="dwidth-full">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
					<span class="areatitle">检索条件</span> <a role="link" href="javascript:void(0)" class="HeaderButton areacloser"> <span class="ui-icon ui-icon-circle-triangle-n"></span>
					</a>
				</div>
				<% 
					String role = (String) request.getAttribute("role");
					boolean isManufactLeader = ("manufact_reader").equals(role);
				%>
				<div class="ui-widget-content dwidth-full">
					<form id="searchform" method="POST">
						<table class="condform">
							<tbody>
								<tr>
									<td class="ui-state-default td-title">制造对象机种</td>
									<td class="td-content" colspan="3">
										<select name="category_id" id="search_category_id" class="ui-widget-content">${cOptions}</select>	
									</td>
									<td class="ui-state-default td-title">机身号</td>
									<td class="td-content"><input type="text" id="search_serialno" maxlength="20" class="ui-widget-content"></td>
								</tr>
								<tr>

								</tr>
								<tr>
									<td class="ui-state-default td-title">加急</td>
									<td class="td-content">
										<div id="scheduled_expedited_set" class="ui-buttonset">
											<input type="radio" name="expedited" id="scheduled_expedited_a" class="ui-widget-content ui-helper-hidden-accessible" value="" checked="checked"><label for="scheduled_expedited_a" aria-pressed="false">全部</label>
											<input type="radio" name="expedited" id="scheduled_expedited_t" class="ui-widget-content ui-helper-hidden-accessible" value="1"><label for="scheduled_expedited_t" aria-pressed="false">有加急</label>
											<input type="radio" name="expedited" id="scheduled_expedited_f" class="ui-widget-content ui-helper-hidden-accessible" value="0"><label for="scheduled_expedited_f" aria-pressed="false">无加急</label>
											<input type="hidden" id="scheduled_expedited">
										</div>
									</td>
									<td class="ui-state-default td-title">课室</td>
									<td class="td-content">
										<select name="section" id="search_section" class="ui-widget-content">
											${sOptions}
										</select>
									</td>
									<td class="ui-state-default td-title">进展工位</td>
									<td class="td-content"><input type="button" class="ui-button" id="position_eval" value="通过">
										<input type="text" class="ui-widget-content" readonly="readonly" id="inp_position_id">
										<input type="hidden" id="search_position_id">
									</td>
								</tr>
							</tbody>
						</table>
						<div style="height: 44px">
							<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="resetbutton" value="清除" role="button" style="float: right; right: 12px">
							<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="searchbutton" value="检索" role="button" style="float: right; right: 2px">
							<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="searchpro2button" value="今日通过QA总数" role="button" style="float: right; right: 2px">
						</div>
					</form>
				</div>
				<div class="clear areaencloser dwidth-full"></div>
			</div>

			<div id="listarea" class="dwidth-full">
				<input type="hidden" value="<%=role%>">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
					<span class="areatitle">制品一览</span> 
					<a role="link" href="javascript:void(0)" class="HeaderButton areacloser"> <span class="ui-icon ui-icon-circle-triangle-n"></span>
					</a>
				</div>

				<table id="list"></table>
				<div id="listpager"></div>
				<div class="clear areaencloser"></div>
			</div>

			<div id="functionarea" class="dwidth-full" style="margin: auto;">
				<% if (isManufactLeader) { %>
				<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase">
<script type="text/javascript" src="js/inline/manager_processing.js"></script>
					<div id="executes" style="margin-left: 4px; margin-top: 4px;">
						<input id="nogoodbutton" class="ui-button" value="处置不良" type="button"/>
						<input type="button" class="ui-button" id="forbutton" value="移出流水线" />

						<input id="capacitybutton" class="ui-button" value="设定日生产计划" type="button"/>

						<input type="button" class="ui-button" id="ticketbutton" value="连续打印小票" style="float: right; margin-right: 4px" />
						<input type="button" class="ui-button" id="reacceptbutton" value="中途加入修理机 " style="float: right; margin-right: 4px" />
					</div>
						<div id="nogood_treat"></div>
				</div>
				<div class="clear areaencloser"></div>
				<% } %>
			</div>

<!--
			<div id="planned_listarea" class="dwidth-full">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
					<span class="areatitle" style="margin-right: 250px">当日进度一览</span>
				</div>
				<table id="planned_list"></table>
				<div id="planned_listpager"></div>
				<div class="clear areaencloser"></div>
			</div>
-->
			<div class="clear"></div>

<% if (isManufactLeader) { %>
	<input type="hidden" id="input_model_id">${modelOptions}</input>
	<%@include file="../../widgets/position_panel/product_serials.jsp"%>
	<%@include file="../../widgets/position_panel/unused_assemblies.jsp"%>
<% } %>

		</div>

		<div id="capacity_setting"></div>
		<div id="capacity_model_setting"></div>
		<div id="process_dialog"></div>
		<div id="confirmmessage"></div>
	</div>
	<div class="referchooser ui-widget-content" id="pReferChooser" tabindex="-1">
		<table>
			<tr>
				<td width="50%">过滤字:<input type="text"/></td>	
				<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
			</tr>
		</table>
		
		<table class="subform">${pReferChooser}</table>
	</div>
</body>
</html>
