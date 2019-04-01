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
<script type="text/javascript" src="js/equipment/device_backup.js"></script>

<title>设备工具替代管理</title>
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
			<div id="body-mdl" style="width: 994px; float: left;">
				<div id="searcharea">
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
										<td class="ui-state-default td-title">管理编号</td>
								   		<td class="td-content">
											<input id="search_manage_code" type="text" class="ui-widget-content">
										</td>
								   </tr>
								   <tr>
								   	    <td class="ui-state-default td-title">责任工程</td>
								   		<td class="td-content" colspan="3">
											<select id="search_line_id">${lOptions}</select>
										</td>
										<td class="ui-state-default td-title">责任工位</td>
								   		<td class="td-content">
											<input type="text" class="ui-widget-content" readonly="readonly" id="text_position_id">
											<input type="hidden" id="search_position_id">
										</td>
								   </tr>
								</tbody>
							</table>
							<div style="height:44px">
								<input type="button" class="ui-button" id="resetbutton" value="清除" style="float:right;right:2px">
								<input type="button" class="ui-button" id="searchbutton" value="检索" style="float:right;right:2px">	
								<input type="hidden" id="hidden_goManage_level" value="${goManageLevel}">	
								<input type="hidden" id="hidden_goStatus" value="${goStatus}">	
							</div>
						</form>
					</div>
					
					<div class="areaencloser"></div>
					
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
						<span class="areatitle">设备代替一览表</span>
						<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
							<span class="ui-icon ui-icon-circle-triangle-n"></span>
						</a>
					</div>
					<div class="ui-widget-content">
						<span style="padding-left:2em;">评价说明：◎=本工程有替代品    ○=他工程有替代品    △=临时共用    ×=无替代品重点管理对象</span>
					</div>
		
					<table id="list"></table>
					<div id="listpager"></div>
					<div class="ui-widget-header areabase"style="padding-top:4px;">
					    <div id="executes" style="margin-left:4px;margin-top:4px;">
							<input type="button" id="setbutton" class="ui-button" value="设置代替关系">
							<input type="button" id="reportbutton" class="ui-button" value="导出设备代替一览表" style="float:right;right:2px;">
						</div>
					</div>
				</div>
				
				<div id="setarea" style="display: none;">
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
										<td class="ui-state-default td-title">管理编号</td>
								   		<td class="td-content">
											<label id="set_manage_code"></label>
											<input type="hidden" id="set_manage_id"/>
										</td>
										<td class="ui-state-default td-title">管理等级/状态</td>
								   		<td class="td-content">
											<label id="set_manage_level"></label>
											<label id="set_status"></label>
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">品名</td>
								   		<td class="td-content">
											<label id="set_device_type_name"/>
										</td>
										<td class="ui-state-default td-title">型号</td>
										<td class="td-content">
											<label id="set_model_name"/>
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">责任工程</td>
								   		<td class="td-content">
											<label id="set_line_name"/>
										</td>
										<td class="ui-state-default td-title">当前评价</td>
										<td class="td-content">
											<label id="set_evaluation"/>
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">对应</td>
										<td class="td-content" colspan="3">
											<textarea id="set_corresponding" class="ui-widget-content" style="width:90%;">
											</textarea>
										</td>
									</tr>
								</tbody>
							</table>
							<div style="height:44px">
								<input type="button" class="ui-button" id="gobackbutton" value="取消" style="float:right;right:2px">
								<input type="button" class="ui-button" id="submitbutton" value="提交更新" style="float:right;right:2px">					
							</div>
							<style>
								#set_evaluation{font-size: large;} 
								#pires{width:710px;}
								#pires th,#pires td {padding: 0 2px;}
								#pires button{background-color:white;border-color:black;width:72px;padding:0 0;}
								#pires button[usage="1"]{background-color:lightgreen;}
								#pires button[usage="0"]{background-color:yellow;}
							</style>
							<table id="pires" class="condform">
								<thead>
									<th class="ui-state-default">管理编号</th><th class="ui-state-default">管理等级/状态</th><th class="ui-state-default">型号</th><th class="ui-state-default">责任工程</th><th class="ui-state-default">替换关系</th><th class="ui-state-default">反向替换关系</th>
								</thead>
								<tbody>
								</tbody>
							</table>
						</form>
					</div>
				</div>			
		</div>
		<div class="clear areaencloser"></div>
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

<!-- 工位 -->
<div class="referchooser ui-widget-content" id="position_id_referchooser" tabindex="-1">
	<table width="200px">
		<tr>
			<td></td>
			<td width="50%">过滤字:<input type="text"/></td>
			<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
		</tr>
	</table>
	<table  class="subform">${pReferChooser}</table>
</div>

</body>
</html>