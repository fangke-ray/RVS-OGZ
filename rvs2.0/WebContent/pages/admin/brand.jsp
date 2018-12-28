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
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>

<script type="text/javascript" src="js/admin/brand.js"></script>

<title>厂商通讯录</title>
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
					<jsp:param name="linkto" value=设备工具•专用工具信息管理"/>
				</jsp:include>
			</div>
			<div style="width: 1012px; float: left;">
				<div id="body-mdl" class="dwidth-middleright" style="margin: auto;">
					<div id="body-mdl" style="width: 994px; float: left;">
			<div id="searcharea" class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
				<span class="areatitle">检索条件</span>
				<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
					<span class="ui-icon ui-icon-circle-triangle-n"></span>
				</a>
	        </div>
	        
	<div class="ui-widget-content dwidth-middleright">
	<!-- 检索条件 -->
		<form id="searchform" method="POST" onsubmit="return false;">
			<table class="condform">
				<tr>
					<td class="ui-state-default td-title">厂商名称</td>
					<td class="td-content"><input type="text" name="name" alt="厂商名称" id="search_name" class="ui-widget-content"/></td>							
					<td class="ui-state-default td-title">业务关系</td>
					<td class="td-content">
						<select id="search_business_relationship" class="ui-widget-content">${businessRelationship}</select>
					</td>
				</tr>
			</table>
			<div style="height:44px">
				<input id="resetbutton"  type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" style="float:right;right:2px" aria-disabled="false" role="button" value="清除">
				<input id="searchbutton" type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" value="检索" role="button" aria-disabled="false" style="float:right;right:2px">
				<input type="hidden" id="goBusinessRelationship" value="${goBusinessRelationship}">
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
					<td class="ui-state-default td-title">厂商名称</td>
					<td class="td-content"><input type="text" alt="厂商名称" name="name" id="edit_name" class="ui-widget-content" style="width:215px;"/></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">业务关系</td>
					<td class="td-content">
						<select name="business_relationship" id="edit_business_relationship" alt="业务关系" class="ui-widget-content">${businessRelationship}</select>
					</td>
				</tr>						
				<tr>
					<td class="ui-state-default td-title">地址</td>
					<td class="td-content">
						<textarea name="address" id="edit_address"></textarea>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">邮箱</td>
					<td class="td-content"><input type="text" alt="邮箱" name="email" id="edit_email" class="ui-widget-content"/></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">联系电话</td>
					<td class="td-content"><input type="text" alt="联系电话" name="tel" id="edit_tel" class="ui-widget-content"/></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">联系人</td>
					<td class="td-content"><input type="text" alt="联系人" name="contacts" id="edit_contacts" class="ui-widget-content" style="width:215px;"/></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">最后更新人</td>
					<td class="td-content"><label id="label_updated_by"></label></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">最后更新时间</td>
					<td class="td-content"><label id="label_updated_time"></label></td>
				</tr>
				<input type="hidden" name="brand_id" id="hidden_brand_id">
			</table>

			<div style="height:44px;margin-top:5px;">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="editbutton" value="修改" role="button" aria-disabled="false" style="float:left;left:4px;">
				<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="cancelbutton" value="取消" role="button" aria-disabled="false" style="float:left;left:4px;">
			</div>
		</form>	
	</div>
</div>

<div class="clear areaencloser"></div>
		<!-- 厂商一览-->
		<div id="listarea" class="width-middleright">
			<table id="list"></table>
			<div id="list_pager"></div>			
		</div>
       </div>
     </div>
    <div class="clear areaencloser"></div>
</div>

</body>
</html>