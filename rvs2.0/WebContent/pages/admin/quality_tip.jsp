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
<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">
<style>
.navli {
	background-color:gray;
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
<script type="text/javascript" src="js/ajaxfileupload.js"></script>
<script type="text/javascript" src="js/admin/quality_tip.js"></script>
<title>质量提示管理</title>
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
					<td class="ui-state-default td-title">标题</td>
					<td class="td-content"><input type="text" id="cond_title" maxlength="11" class="ui-widget-content"></td>
					<td class="ui-state-default td-title">工位名称</td>
					<td class="td-content">
						<input type="text" id="cond_position_name" readonly="readonly" class="ui-widget-content">
						<input type="hidden" id="cond_position_id">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">维修对象机种</td>
					<td class="td-content" colspan="3">
						<select id="cond_category_id" class="ui-widget-content">
							${cOptions}
						</select>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">维修对象型号</td>
					<td class="td-content" colspan="3">
						<input type="text" id="cond_model_name" readonly="readonly" class="ui-widget-content">
						<input type="hidden" id="cond_model_id">
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

<div id="editarea" style="display:none;">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
		<span class="areatitle"></span>
		<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
			<span class="ui-icon ui-icon-circle-triangle-w"></span>
		</a>
	</div>
	<div class="ui-widget-content dwidth-middleright">
		<form id="editform" method="POST" onsubmit="return false;">
			<input type="hidden" id="edit_quality_tip_id" value=""/>
			<div>
				<div style="float:left;">
					<table class="condform">
						<tr>
							<td class="ui-state-default td-title">标题</td>
							<td class="td-content"><input type="text" id="input_title" maxlength="15" class="ui-widget-content"></td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">工位名称</td>
							<td class="td-content">
								<input type="text" id="input_position_name" name="position_name" alt="工位名称" readonly="readonly" class="ui-widget-content">
								<input type="hidden" id="input_position_id">
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
						<tr>
							<td class="ui-state-default td-title">图片上传</td>
							<td class="td-content" align="right">
								<input type="file" name="file" id="photo_file" style="width:245px;"></input>					
								<br><input type="button" id="photo_upload_button" class="ui-button" value="上传"></input>
								<input type="hidden" id="photo_file_name" value=""></input>
							</td>
						</tr>
					</table>
				</div>
				<div style="float:left;width:600px;" class="ui-widget-content">
					<div class="ui-widget-header areaencloser">
						<span class="areatitle">选择维修对象机种或者型号</span>
					</div>
					<div style="float:left;">
						<table class="subform" id="grid_edit_category">
							<tr>
								<th class="ui-state-default td-title" colspan="3" style="min-width:220px;">维修对象机种</th>
							</tr>
							<%=request.getAttribute("cReferChooser")%>
						</table>
					</div>
					<div style="float:left;">
						<table class="subform" id="grid_edit_model">
							<thead>
							<tr>
								<th class="ui-state-default td-title">维修对象型号</th>
								<th class="td-content">
									<input type="text" id="input_model_name" name="model_name" alt="维修对象型号" readonly="readonly" class="ui-widget-content">
									<input type="hidden" id="input_model_id">
								</th>
							</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
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

<div id="confirmmessage"></div>
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
