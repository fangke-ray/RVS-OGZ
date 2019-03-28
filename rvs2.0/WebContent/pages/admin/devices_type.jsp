<%@page import="framework.huiqing.common.util.CodeListUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
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
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/ajaxfileupload.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>

<script type="text/javascript" src="js/admin/devices_type.js"></script>
<style>
	#show_photo {
		width:218px;
	}
</style>

<title>设备工具品名</title>
</head>
<body class="outer" style="overflow: auto;">
	<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">
		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="2"/>
			</jsp:include>
		</div>
		<div class="ui-widget-panel ui-corner-all width-full" style="align: center; padding-top: 16px;" id="body-3">
			<div id="body-lft" style="width: 256px; float: left;">
				<jsp:include page="/appmenu.do?method=tinit" flush="true">
					<jsp:param name="linkto" value="设备工具•专用工具信息管理"/>
				</jsp:include>
			</div>
			<div style="width: 1012px; float: left;">
				<div id="body-mdl" class="dwidth-middleright" style="margin: auto;">
					<div id="body-mdl" style="width: 994px; float: left;">
			<div id="searcharea" class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
				<span class="areatitle">设备・一般工具品名检索条件</span>
				<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
					<span class="ui-icon ui-icon-circle-triangle-n"></span>
				</a>
	        </div>
	        
	<div class="ui-widget-content dwidth-middleright">
	<!-- 检索条件 -->
		<form id="searchform" method="POST" onsubmit="return false;">
			<table class="condform">
				<tr>
					<td class="ui-state-default td-title">品名</td>
					<td class="td-content"><input type="text" name="name" alt="品名" id="search_name" class="ui-widget-content"/></td>							
					<td class="ui-state-default td-title">特定设备工具种类</td>
					<td class="td-content">
						<select id="search_specialized" class="ui-widget-content">${specializedDeviceType}</select>
					</td>
					<td class="ui-state-default td-title">代替使用方式</td>
					<td class="td-content" id="search_alter_flg" style="min-width:238px">
						<input type="radio" name="alter_flg" id="search_alter_flg_all" value="" class="ui-widget-content" checked>
						<label for="search_alter_flg_all" radio>(全部)</label>
						<input type="radio" name="alter_flg" alt="管理替代" id="search_alter_flg_yes" value="1" class="ui-widget-content">
						<label for="search_alter_flg_yes" radio>个别指定代替关系</label>
						<input type="radio" name="alter_flg" alt="开放替代" id="search_alter_flg_free" value="0" class="ui-widget-content">
						<label for="search_alter_flg_free" radio>开放同型号代替</label>
						<input type="radio" name="alter_flg" alt="不便替代" id="search_alter_flg_forbid" value="2" class="ui-widget-content">
						<label for="search_alter_flg_forbid" radio>不便互相代替</label>
					</td>
				</tr>
			</table>
			<div style="height:44px">
				<input id="resetbutton"  type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" style="float:right;right:2px" aria-disabled="false" role="button" value="清除">
				<input id="searchbutton" type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" value="检索" role="button" aria-disabled="false" style="float:right;right:2px">
			</div>
		</form>		
	</div> 
</div>
	<div id="editarea" style="display:none;">
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
			<span class="areatitle"></span>			
		</div>

		<div class="ui-widget-content dwidth-middleright">	
		<!-- 新建编辑页面 -->
			<form id="editform" method="POST" onsubmit="return false;">
			<table class="condform">
				<tr>
					<td class="ui-state-default td-title">品名</td>
					<td class="td-content"><input type="text" alt="品名" name="name" id="edit_name" class="ui-widget-content" style="width:215px;"/></td>
					<td class="td-content safety_guide" rowspan="7">
						<label id="show_no_photo">无安全操作手顺</label>
						<img id="show_photo"></img>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">特定设备工具种类</td>
					<td class="td-content">
						<select id="edit_specialized" class="ui-widget-content">${specializedDeviceType}</select>
					</td>
				</tr>						
				<tr>
					<td class="ui-state-default td-title">危险归类</td>
					<td class="td-content">
						<select id="edit_hazardous_cautions" multiple>${deviceHazardousClassification}</select>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">代替使用方式</td>
					<td class="td-content" id="edit_alter_flg" style="min-width:238px">
						<input type="radio" name="alter_flg" alt="管理替代" id="alter_flg_yes" value="1" class="ui-widget-content" checked>
						<label for="alter_flg_yes" radio>个别指定代替关系</label>
						<input type="radio" name="alter_flg" alt="开放替代" id="alter_flg_free" value="0" class="ui-widget-content">
						<label for="alter_flg_free" radio>开放同型号代替</label>
						<input type="radio" name="alter_flg" alt="不便替代" id="alter_flg_forbid" value="2" class="ui-widget-content">
						<label for="alter_flg_forbid" radio>不便互相代替</label>
					</td>
				</tr>
				<tr class="safety_guide">
					<td class="ui-state-default td-title">安全操作手顺上传</td>
					<td class="td-content">
						<input  type="file" name="file" id="update_photo" name="photo" value="导入图片" alt="安全操作手顺">	
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">最后更新人</td>
					<td class="td-content"><label id="label_updated_by"></label></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">最后更新时间</td>
					<td class="td-content"><label id="label_updated_time"></label></td>
				</tr>
				<input type="hidden" name="devices_type_id" id="hidden_devices_type_id">
			</table>

			<div style="height:44px;margin-top:5px;">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="editbutton" value="修改" role="button" aria-disabled="false" style="float:left;left:4px;">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="cancelbutton" value="取消" role="button" aria-disabled="false" style="float:left;left:4px;">
			</div>
		</form>	
		</div>
</div>

<div class="clear areaencloser"></div>
		<!--设备工具品名信息一览-->
		<div id="listarea" class="width-middleright">
			<table id="list"></table>
			<div id="list_pager"></div>			
		</div>
		<input type="hidden" id="gSpecializedDeviceType" value="${gSpecializedDeviceType }">
		<div id="confirmmessage"></div>
       </div>
     </div>
    <div class="clear areaencloser"></div>
</div>
</body>
</html>