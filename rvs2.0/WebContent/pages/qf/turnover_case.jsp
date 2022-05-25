<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/qf/turnover_case.js"></script>
<script type="text/javascript" src="js/qf/turnover_case_common.js"></script>
<title>通箱库位管理</title>
</head>
<body class="outer" style="align: center;">

<% 
	String editor = (String) request.getAttribute("editor");
	boolean isAdmin = ("admin").equals(editor);
	boolean isEditor = isAdmin || ("true").equals(editor);
%>
	<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">

		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="2"/>
			</jsp:include>
		</div>
		<div class="ui-widget-panel ui-corner-all width-full" style="align: center; padding-top: 16px;" id="body-2">
			<div id="body-lft" style="width: 256px; float: left;">
				<jsp:include page="/appmenu.do" flush="true">
					<jsp:param name="linkto" value="受理报价"/>
				</jsp:include>
			</div>
			<div id="body-mdl" style="width: 1022px; float: left;">

	<div style="margin-left: 8px;">
		<div id="searcharea" class="dwidth-middleright">
			<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
				<span class="areatitle">检索条件</span>
				<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
				<span class="ui-icon ui-icon-circle-triangle-n"></span>
				</a>
			</div>
			<div class="ui-widget-content dwidth-middleright">
				<form id="searchform" method="POST">
					<table class="condform">
						<tbody>
							<tr>
								<td class="ui-state-default td-title">修理单号</td>
								<td class="td-content"><input type="text" name="omr_notifi_no" id="cond_omr_notifi_no" maxlength="15" class="ui-widget-content"></td>
								<td class="ui-state-default td-title">维修对象型号</td>
								<td class="td-content">
									<input type="text" class="ui-widget-content" readonly="readonly" name="model_name" id="cond_model_name">
									<input type="hidden" name="model_id" id="cond_model_id">
								</td>
								<td class="ui-state-default td-title">机身号</td>
								<td class="td-content"><input type="text" name="serial_no" id="cond_serial_no" maxlength="20" class="ui-widget-content"></td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">通箱库位</td>
								<td class="td-content"><input type="text" name="location" id="cond_location" maxlength="5" class="ui-widget-content"></td>
								<td class="ui-state-default td-title">存入时间</td>
								<td class="td-content">
									<input type="text" name="storage_time_start" id="cond_storage_time_start" maxlength="10" class="ui-widget-content">起
									<br>
									<input type="text" name="storage_time_end" id="cond_storage_time_end" maxlength="10" class="ui-widget-content">止
								</td>
								<td class="ui-state-default td-title">直送</td>
								<td class="td-content" id="cond_direct_flg">
									<input type="radio" name="directflg" id="directflg_a" value="" class="ui-widget-content ui-helper-hidden-accessible" checked>
									<label for="directflg_a" aria-pressed="false">全部</label>
									<input type="radio" name="directflg" id="directflg_d" value="1" class="ui-widget-content ui-helper-hidden-accessible">
									<label for="directflg_d" aria-pressed="false">直送</label>
									<input type="radio" name="directflg" id="directflg_n" value="0" class="ui-widget-content ui-helper-hidden-accessible">
									<label for="directflg_n" aria-pressed="false">非直送</label>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">库位设置机种</td>
								<td class="td-content" colspan="3">
									<select id="cond_kind">
										${kindOptions}
									</select>
								</td>
								<td class="ui-state-default td-title">已同意/未同意<br>库位</td>
								<td class="td-content" id="cond_for_agreed">
									<input type="radio" name="for_agreed" id="cond_for_agreed_a" value="" class="ui-widget-content ui-helper-hidden-accessible" checked>
									<label for="cond_for_agreed_a" aria-pressed="false">全部</label>
									<input type="radio" name="for_agreed" id="cond_for_agreed_d" value="1" class="ui-widget-content ui-helper-hidden-accessible">
									<label for="cond_for_agreed_d" aria-pressed="false">已同意</label>
									<input type="radio" name="for_agreed" id="cond_for_agreed_n" value="0" class="ui-widget-content ui-helper-hidden-accessible">
									<label for="cond_for_agreed_n" aria-pressed="false">未同意</label>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">发送地</td>
								<td class="td-content" colspan="5">
									<select id="cond_bound_out_ocm">
										${boundOutOcmOptions}
									</select>
								</td>
							</tr>
						</tbody>
					</table>
					<div style="height: 44px">
						<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="resetbutton" value="清除" role="button" aria-disabled="false" style="float: right; right: 2px">
						<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="searchbutton" value="检索" role="button" aria-disabled="false" style="float: right; right: 2px">
						<input type="hidden" id="lOptions" value="${lOptions}">
						<input type="hidden" id="ocmOptions" value="${ocmOptions}">
					</div>
				</form>
			</div>
			<div class="clear areaencloser dwidth-middleright"></div>
		</div>


		<div id="listarea" class="dwidth-middleright">
			<table id="list"></table>
			<div id="listpager"></div>
			<div class="clear"></div>
		</div>

		<div id="functionarea" class="dwidth-middleright">
<% if (isAdmin) { %>
			<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase">
				<div id="admin_executes" style="margin-left: 4px; margin-top: 4px;">
					<input type="button" id="createStorageButton" class="ui-button ui-widget ui-state-default ui-corner-all" value="建立库位" role="button">
					<input type="button" id="changeStorageButton" class="ui-button ui-widget ui-state-default ui-corner-all" value="调整库位" role="button">
					<input type="button" id="removeStorageButton" class="ui-button ui-widget ui-state-default ui-corner-all" value="删除库位" role="button">
				</div>
			</div>
			<div class="clear"></div>
<% } %>
<% if (isEditor) { %>
			<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase">
				<div id="executes" style="margin-left: 4px; margin-top: 4px;">
					<input type="button" class="ui-button" value="计划入库" id="stoargeButton" />
					<input type="button" class="ui-button" value="计划出库" id="warehousingButton" />
					<input type="button" class="ui-button" value="调整入库" id="placeButton" />
					<input type="button" class="ui-button" value="调整出库" id="removeButton" />
					<input type="button" class="ui-button" value="移动库位" id="moveButton" />
					<span style="margin-right: 10px; float: right; height: 44px; line-height: 36px; display:none;">通箱库位积荷率：<label id="heaprate">16</label>%</span>
				</div>
			</div>
			<div class="clear areaencloser"></div>
<% } %>
		</div>

		<div id="wiparea" class="wiparea dwidth-middleright">
		</div>

	</div>
			</div>
	<div id="wip_pop"></div>
	<div class="referchooser ui-widget-content" id="model_refer" tabindex="-1" style="z-index:1003;">
	<table>
		<tr>
			<td width="50%">过滤字:<input type="text"/></td>	
			<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
		</tr>
	</table>
	
	<table class="subform">${mReferChooser}</table>

	</div>
	<div class="referchooser ui-widget-content" id="model_refer1" tabindex="-1" style="z-index:1013;">
	<table>
		<tr>
			<td width="50%">过滤字:<input type="text"/></td>	
			<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
		</tr>
	</table>
	
	<table class="subform">${mReferChooser}</table>

	</div>
		<div class="clear"></div>
		</div>
	</div>
	<div id="confirmmessage"></div>

<% if (isAdmin) { %>
<div class="ui-widget-content" id="editarea" style="display:none;">
	<form id="editform" method="POST">
		<table class="condform">
		<tbody>
			<tr>
				<td class="ui-state-default td-title">货架</td>
				<td class="td-content">
					<input type="text" name="shelf" id="edit_shelf" maxlength="2" class="ui-widget-content">
				</td>
			</tr>
			<tr>
				<td class="ui-state-default td-title">层数</td>
				<td class="td-content">
					<input type="text" name="layer" id="edit_layer" maxlength="1" class="ui-widget-content">
				</td>
			</tr>
			<tr>
				<td class="ui-state-default td-title">库位代码</td>
				<td class="td-content">
					<input type="hidden" name="key" id="edit_key">
					<input type="text" name="location" id="edit_location" maxlength="4" class="ui-widget-content">
				</td>
			</tr>
			<tr>
				<td class="ui-state-default td-title">库位设置机种</td>
				<td class="td-content"><select name="kind" id="edit_kind">${kindOptions}</select>
				</td>
			</tr>
			<tr>
				<td class="ui-state-default td-title">已同意/未同意<br>库位</td>
				<td class="td-content" id="edit_for_agreed">
					<input type="radio" name="auto_arrange" id="edit_for_agreed_y" value="1" class="ui-widget-content"><label for="edit_for_agreed_y">已同意</label>
					<input type="radio" name="auto_arrange" id="edit_for_agreed_n" value="0" class="ui-widget-content"><label for="edit_for_agreed_n">未同意</label>
				</td>
			</tr>
		</tbody>
		</table>
	</form>
</div>

<% } %>
</body>
</html>