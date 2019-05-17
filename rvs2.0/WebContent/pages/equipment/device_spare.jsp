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
<script type="text/javascript" src="js/equipment/device_spare.js"></script>

<style type="text/css">
order_no{
	cursor: pointer;
	color: blue;
	text-decoration: underline;
}
</style>

<title>设备工具备品管理</title>
</head>
<% 
	String privacy = (String) request.getAttribute("privacy");
	boolean isTechnology = ("technology").equals(privacy);
%>
<body class="outer" style="overflow: auto;" istechnology="<%= isTechnology%>">
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
			<div id="body-mdl" style="width: 994px; float: left;">
				<div id="search">
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
								   		<td class="ui-state-default td-title">品名</td>
								   		<td class="td-content">
											<input type="text" id="search_devices_type_name" readonly="readonly" class="ui-widget-content">
											<input type="hidden" id="search_device_type_id">
										</td>
										<td class="ui-state-default td-title">型号</td>
								   		<td class="td-content">
											<input id="search_model_name" type="text" class="ui-widget-content">
										</td>
										<td class="ui-state-default td-title">备品种类</td>
								   		<td class="td-content">
											<select id="search_device_spare_type">${goDeviceSpareType }</select>
										</td>
								   </tr>
								   <tr>
								   	    <td class="ui-state-default td-title">品牌</td>
								   		<td class="td-content">
											<input type="text" id="search_brand_name" readonly="readonly" class="ui-widget-content">
											<input type="hidden" id="search_brand_id">
										</td>
										<td class="ui-state-default td-title">需要订购</td>
								   		<td class="td-content" id="search_order_flg">
								   			<input type="radio" name="order_flg" id="search_order_flg_no" class="ui-widget-content" value="1" checked><label for="search_order_flg_no">否</label>
											<input type="radio" name="order_flg" id="search_order_flg_yes" class="ui-widget-content" value="2"><label for="search_order_flg_yes">是</label>
										</td>
										<td class="ui-state-default td-title">计算时期</td>
								   		<td class="td-content">
											<input type="text" class="ui-widget-content" id="search_adjust_time_start" readonly="readonly" value="${startDate}">起<br>
											<input type="text" class="ui-widget-content" id="search_adjust_time_end" readonly="readonly">止
										</td>
								   </tr>
								</tbody>
							</table>
							<div style="height:44px">
								<input type="button" class="ui-button" id="resetbutton" value="清除" style="float:right;right:2px">
								<input type="button" class="ui-button" id="searchbutton" value="检索" style="float:right;right:2px">					
							</div>
						</form>
					</div>
					
					<div class="areaencloser"></div>
					
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
						<span class="areatitle">设备工具备品总价</span>
					</div>
					<div class="ui-widget-content">
						<table class="condform">
							<tbody>
								<tr>
									<td class="ui-state-default td-title">设备工具备品合理金额</td>
									<td class="td-content">
										<span id="consumables_benchmark_price">${totalPrice.consumables_benchmark_price } RMB</span>
									</td>
									<td class="ui-state-default td-title">设备工具备品在库金额</td>
									<td class="td-content">
										<span id="consumables_inventory_price">${totalPrice.consumables_inventory_price } RMB</span>
									</td>
									<td class="ui-state-default td-title">备件在库金额</td>
									<td class="td-content">
										<span id="part_inventory_price">${totalPrice.part_inventory_price } RMB</span>
									</td>
								</tr>
							</tbody>
						</table>
					</div>
					
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
						<span class="areatitle">设备工具备品一览</span>
						<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
							<span class="ui-icon ui-icon-circle-triangle-n"></span>
						</a>
					</div>
				
					<table id="list"></table>
					<div id="listpager"></div>
					
<% if (isTechnology) { %>
					<div class="ui-widget-header areabase"style="padding-top:4px;">
					    <div id="executes" style="margin-left:4px;margin-top:4px;">
							<input type="button" id="cancelbutton" class="ui-button" value="取消管理">
							<input type="button" id="inventorybutton" class="ui-button" value="盘点">
						</div>
					</div>
<%} %>	
				</div>
				
				<div id="add" style="display: none;">
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					   	 <span class="areatitle">新建设备工具备品</span>
					     <a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
						 	<span class="ui-icon ui-icon-circle-triangle-w"></span>
						 </a>
					</div>
					<div class="ui-widget-content">
						<form id="addform" method="POST">
							<table class="condform">
								<tbody>
									<tr>
										<td class="ui-state-default td-title">品名</td>
								   		<td class="td-content">
											<input type="text" id="add_devices_type_name" readonly="readonly" class="ui-widget-content">
											<input type="hidden" id="add_device_type_id">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">型号</td>
										<td class="td-content">
											<input id="add_model_name" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">备品种类</td>
										<td class="td-content">
											<select id="add_device_spare_type">${goDeviceSpareType }</select>
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">订货天数（工作日）</td>
										<td class="td-content">
											<input id="add_order_cycle" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">品牌</td>
										<td class="td-content">
											<input type="text" id="add_brand_name" readonly="readonly" class="ui-widget-content">
											<input type="hidden" id="add_brand_id">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">单价</td>
										<td class="td-content">
											<input id="add_price" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">Min-Limit</td>
										<td class="td-content">
											<input id="add_safety_lever" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">Max-Limit</td>
										<td class="td-content">
											<input id="add_benchmark" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">当前有效库存</td>
										<td class="td-content">
											<input id="add_available_inventory" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">放置位置</td>
										<td class="td-content">
											<input id="add_location" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">管理备注</td>
										<td class="td-content">
											<textarea id="add_comment" class="ui-widget-content" style="resize: none;" cols="50"></textarea>				
										</td>
									</tr>
								</tbody>
							</table>
							<div style="height:44px">
								<input type="button" class="ui-button" id="gobackbutton" value="取消" style="float:right;right:2px">
								<input type="button" class="ui-button" id="newbutton" value="新建" style="float:right;right:2px">					
							</div>
						</form>
					</div>
				</div>
				
				<div id="update" style="display: none;">
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					   	 <span class="areatitle">更新设备工具备品</span>
					     <a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
						 	<span class="ui-icon ui-icon-circle-triangle-w"></span>
						 </a>
					</div>
					
					<div class="ui-widget-content">
						<form id="updateform" method="POST">
							<table class="condform" style="width: 600px;">
								<tbody>
									<tr>
										<td class="ui-state-default td-title">品名</td>
								   		<td class="td-content"><span id="update_devices_type_name"></span></td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">型号</td>
										<td class="td-content"><span id="update_model_name"></span></td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">备品种类</td>
										<td class="td-content"><span id="update_device_spare_type_name"></span></td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">订货天数（工作日）</td>
										<td class="td-content">
											<input id="update_order_cycle" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">品牌</td>
										<td class="td-content">
											<input type="text" id="update_brand_name" readonly="readonly" class="ui-widget-content">
											<input type="hidden" id="update_brand_id">
											<input type="button" id="update_brand_detail_button" class="ui-button" value="…" style="padding: 0 3px;">						
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">单价</td>
										<td class="td-content">
											<input id="update_price" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">Min-Limit</td>
										<td class="td-content">
											<input id="update_safety_lever" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">Max-Limit</td>
										<td class="td-content">
											<input id="update_benchmark" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">当前有效库存</td>
										<td class="td-content">
											<span id="update_available_inventory"></span>
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">合理总额</td>
										<td class="td-content">
											<span id="update_total_benchmark_price"></span>
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">在库总额</td>
										<td class="td-content">
											<span id="update_total_available_price"></span>
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">放置位置</td>
										<td class="td-content">
											<input id="update_location" type="text" class="ui-widget-content">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">管理备注</td>
										<td class="td-content">
											<textarea id="update_comment" class="ui-widget-content" style="resize: none;" cols="55"></textarea>				
										</td>
									</tr>
								</tbody>
							</table>
							<div style="height:44px">
								<input type="button" class="ui-button" id="goback2button" value="取消" style="float:right;right:2px">
								<input type="button" class="ui-button" id="managebutton" value="管理" style="float:right;right:2px">
								<input type="button" class="ui-button" id="updatebutton" value="修改" style="float:right;right:2px">					
							</div>
						</form>
					</div>
					
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					   	 <span class="areatitle">设备工具备品调整记录一览</span>
					</div>
					
					<table id="adjustlist"></table>
					<div id="adjustlistpager"></div>
					
				</div>
				
				
				<!-- 设备工具品名 -->
				<div class="referchooser ui-widget-content" id="device_type_id_referchooser" tabindex="-1">
					<table width="200px">
						<tr>
							<td></td>
							<td width="50%">过滤字:<input type="text"/></td>
							<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
						</tr>
					</table>
					<table  class="subform">${nReferChooser}</table>
				</div>
				
				<!-- 品牌 -->
				<div class="referchooser ui-widget-content" id="device_brand_id_referchooser" tabindex="-1">
					<table width="200px">
						<tr>
							<td></td>
							<td width="50%">过滤字:<input type="text"/></td>
							<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
						</tr>
					</table>
					<table  class="subform">${brandNameReferChooser}</table>
				</div>
				
				<select id="hidden_manage_reason_type" style="display: none;">${goManageReasonType}</select>
				
			</div>
			
			<div class="clear areaencloser"></div>
		</div>
	</div>

	<%@include file="../../widgets/infect/brand-detail.jsp"%>
</body>
</html>