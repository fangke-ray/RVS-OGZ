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
<script type="text/javascript" src="js/admin/procedure_step_count.js"></script>
<title>作业步骤计数</title>
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
				<tr>
					<td class="ui-state-default td-title">作业步骤名称</td>
					<td class="td-content"><input type="text" name="name" id="cond_name" maxlength="50" class="ui-widget-content"></td>
					<td class="ui-state-default td-title">应用机型</td>
					<td class="td-content">
						<input type="text" class="ui-widget-content" readonly="readonly" id="cond_model_name">
						<input type="hidden" name="model_id" id="cond_model_id">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">应用工位</td>
					<td class="td-content">
						<input type="text" class="ui-widget-content" readonly="readonly" id="cond_process_code">
						<input type="hidden" name="position_id" id="cond_position_id">
					</td>
					<td class="ui-state-default td-title">应用工位分线</td>
					<td class="td-content">
						<select name="px" id="cond_px">
							<option value="0">(任何)</option>
							<option value="1">A</option>
							<option value="2">B</option>
						</select>
					</td>
				</tr>

			</table>
			<div style="height:44px">
				<a href="/docs/ProcedureStepCounter.exe" class="ui-button" style="float:right;right:2px;font-size: 14px;">下载客户端(ver.2)</a>
				<input type="button" class="ui-button ui-widget ui-state-default ui-corner-all" id="resetbutton" value="取消" role="button" aria-disabled="false" style="float:right;right:2px">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="searchbutton" value="检索" role="button" aria-disabled="false" style="float:right;right:2px">
			</div>
		</form>
	</div>
	<div class="clear dwidth-middleright"></div>

	<div id="listarea" class="width-middleright">
		<table id="list"></table>
		<div id="listpager"></div>
	</div>
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
			<input type="hidden" id="edit_procedure_step_count_id" value=""/>
			<div>
				<div style="float:left;">
					<table class="condform">
						<tr>
							<td class="ui-state-default td-title">作业步骤名称</td>
							<td class="td-content"><input type="text" id="input_name" maxlength="45" class="ui-widget-content"></td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">应用工位</td>
							<td class="td-content">
								<input type="text" id="input_position_name" name="position_name" alt="应用工位" readonly="readonly" class="ui-widget-content">
								<input type="hidden" id="input_position_id">
								<select name="px" id="input_px">
									<option value="0">(任何)</option>
									<option value="1">A</option>
									<option value="2">B</option>
								</select>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">最后更新人</td>
							<td class="td-content"><label id="label_edit_updated_by"></label></td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">最后更新时间</td>
							<td class="td-content"><label id="label_edit_updated_time"></label></td>
						</tr>
					</table>
				</div>
				<div style="float:left;width:600px;" class="ui-widget-content">
					<div style="float:left;">
						<table class="subform" id="grid_edit_model_header">
							<thead>
							<tr>
								<th class="ui-state-default td-title" style="width:150px;">维修对象型号</th>
								<th class="td-content">
									<input type="text" id="input_model_name" name="model_name" alt="维修对象型号" readonly="readonly" class="ui-widget-content">
									<input type="hidden" id="input_model_id">
								</th>
								<th class="ui-state-default td-title" style="width:150px;">机型关联计数</th>
							</tr>
							</thead>
						</table>
						<div style="max-height:480px;overflow-y: auto;width: 508px;">
						<style>
						#grid_edit_model td:nth-child(2),#grid_edit_model td:nth-child(4) {width: 150px;max-width: 150px;}
						#grid_edit_model input[type='number']{text-align:right;width:5em;}
						</style>
						<table class="subform" id="grid_edit_model" style="width: 482px;">
							<tbody>
							</tbody>
						</table>
						</div>
					</div>
				</div>
			</div>
			<div class="clear"></div>
			<div id="photo_edit_area" style="margin:.3em .3em .3em .3em;border:1px solid #aaaaaa;display:none;">
				<img id="editted_image" style="max-width:600px;max-height:380px;"></img>
			</div>
			<div class="clear" style="height:44px">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="editbutton" value="新建" role="button" aria-disabled="false" style="float:left;left:4px;">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="cancelbutton" value="取消" role="button" aria-disabled="false" style="float:left;left:4px;">
			</div>
		</form>
	</div>
	<div class="ui-state-default ui-corner-bottom areaencloser dwidth-middleright"></div>

</div>

<div class="referchooser ui-widget-content" id="position_refer" tabindex="-1">
	<table>
		<tr>
			<td width="50%">过滤字:<input type="text" tabindex="-1"/></td>	
			<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
		</tr>
	</table>
	
	<table class="subform"><%=request.getAttribute("pReferChooser")%></table>
</div>
<div class="referchooser ui-widget-content" id="model_refer" tabindex="-1">
	<table>
		<tr>
			<td width="50%">过滤字:<input type="text" tabindex="-1"/></td>	
			<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
		</tr>
	</table>
	
	<table class="subform"><%=request.getAttribute("mReferChooser")%></table>
</div>

</body>
</html>
<script type="text/javascript">
$("#testbutton").click(function(){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : 'procedureStepCount.do?method=test',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrobj){

		}
	});
});
</script>