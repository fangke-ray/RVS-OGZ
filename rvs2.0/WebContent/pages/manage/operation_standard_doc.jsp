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
<script type="text/javascript" src="js/manage/operation_standard_doc.js"></script>

<style type="text/css">
.referchooser {
	z-index:1050;
}

#updateform thead{
	text-align: center;
}

#updateform tr > td:nth-child(1){
	width: 16px;
	text-align: center;
	font-weight: normal;
}

#updateform tr > td:nth-child(2){
	width: 600px;
}

#updateform tr > td:nth-child(2) > input[type='text']{
	width: 84%;
}

#updateform tr > td:nth-child(2) > input[type='button']{
	margin-left: 5px;
}

#updateform tr > td:nth-child(3){
	width: 60px;
}

#updateform tr > td:nth-child(3) > input{
	width: 90%;
}

#updateform tr > td:nth-child(4){
	width: 50px;
	text-align: center;
}

#updateform  table input.ui-button{
	padding: .2em 1em;
}

</style>

<title>作业基准书管理</title>
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
				<jsp:include page="/appmenu.do?method=init" flush="true">
					<jsp:param name="linkto" value="计划管理"/>
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
								   		<td class="ui-state-default td-title">机种</td>
								   		<td class="td-content" colspan="5">
								   			<select id="search_category_id">${categoryOptions}</select>
										</td>
								   </tr>
								   <tr>
								   		<td class="ui-state-default td-title">工程</td>
								   		<td class="td-content"  colspan="5">
								   			<select id="search_line_id">${lineOptions}</select>
										</td>
								   </tr>
								   <tr>
								   		<td class="ui-state-default td-title">型号</td>
								   		<td class="td-content">
								   			<input type="text" id="search_model_name" readonly="readonly" class="ui-widget-content">
											<input type="hidden" id="search_model_id">
								   		</td>
										<td class="ui-state-default td-title">工位代码</td>
										<td class="td-content">
											<input type="text" id="search_process_code" readonly="readonly" class="ui-widget-content">
											<input type="hidden" id="search_position_id">
										</td>
										<td class="ui-state-default td-title"></td>
										<td class="td-content"></td>
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
						<span class="areatitle">配置作业基准书</span>
					</div>
					
					<div class="ui-widget-content">
						<form id="configureform">
							<table class="condform">
								<tr>
									<td class="ui-state-default td-title">型号</td>
									<td class="td-content">
							   			<input type="text" id="configure_model_name" readonly="readonly" class="ui-widget-content">
										<input type="hidden" id="configure_model_id">
							   		</td>
							   		<td class="ui-state-default td-title">工位</td>
									<td class="td-content">
										<input type="text" id="configure_process_code" readonly="readonly" class="ui-widget-content">
										<input type="hidden" id="configure_position_id">
									</td>
								</tr>
							</table>
							<div style="height:44px">
								<input type="button" class="ui-button" id="copybutton" value="复制于" style="float:right;right:2px">
								<input type="button" class="ui-button" id="configurebutton" value="配置" style="float:right;right:2px">
							</div>
						</form>
					</div>
					
					<div class="areaencloser"></div>
					
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
						<span class="areatitle">作业基准书一览</span>
						<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
							<span class="ui-icon ui-icon-circle-triangle-n"></span>
						</a>
					</div>
				
					<table id="list"></table>
					<div id="listpager"></div>
					
					<div class="ui-widget-header areabase"style="padding-top:4px;">
					    <div id="executes" style="margin-left:4px;margin-top:4px;">
					    	<input type="button" class="ui-button" value="删除型号配置" id="delModelButton">
						</div>
					</div>
					
				</div>
				
				<div id="update" style="display: none;">
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					   	 <span class="areatitle">作业基准书明细 型号【<span></span>】，工位【<span></span>】</span>
					     <a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
						 	<span class="ui-icon ui-icon-circle-triangle-w"></span>
						 </a>
					</div>
					
					<div class="ui-widget-content">
						<form id="updateform" method="POST">
							<table class="condform">
								<thead>
									<tr>
										<td class="ui-state-default td-title"></td>
										<td class="ui-state-default td-title">文档 URL</td>
										<td class="ui-state-default td-title">页码数</td>
										<td class="td-content">
											<input type="button" id="add_row" class="ui-button" value="+"/>
										</td>
									</tr>
								</thead>
								<tbody></tbody>
							</table>
							<div style="height:44px">
								<input type="button" class="ui-button" id="gobackbutton" value="取消" style="float:right;right:2px">
								<input type="button" class="ui-button" id="updatebutton" value="更新" style="float:right;right:2px">
								<input type="button" class="ui-button" id="delModelAndPositionButton" value="删除型号工位配置" style="float:right;right:2px">
							</div>
						</form>
					</div>					
				</div>
				
				<div id="copyModelDialog" style="display: none">
					<div class="ui-widget-content">
						<table class="condform">
							<tr>
								<td class="ui-state-default td-title">型号</td>
								<td class="td-content">
						   			<input type="text" id="copy_model_name" readonly="readonly" class="ui-widget-content">
									<input type="hidden" id="copy_model_id">
						   		</td>
							</tr>
						</table>
					</div>
				</div>
				
				<!-- 型号 -->
				<div class="referchooser ui-widget-content" id="model_id_referchooser" tabindex="-1">
					<table width="200px">
						<tr>
							<td></td>
							<td width="50%">过滤字:<input type="text"/></td>
							<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
						</tr>
					</table>
					<table  class="subform">${mReferChooser}</table>
				</div>
				
				<div class="referchooser ui-widget-content" id="conf_model_id_referchooser" tabindex="-1">
					<table width="200px">
						<tr>
							<td></td>
							<td width="50%">过滤字:<input type="text"/></td>
							<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
						</tr>
					</table>
					<table  class="subform">${mReferChooser}</table>
				</div>
				
				<div class="referchooser ui-widget-content" id="copy_model_id_referchooser" tabindex="-1">
					<table width="200px">
						<tr>
							<td></td>
							<td width="50%">过滤字:<input type="text"/></td>
							<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
						</tr>
					</table>
					<table  class="subform"></table>
				</div>
				
				<!-- 工位代码 -->
				<div class="referchooser ui-widget-content" id="process_code_referchooser" tabindex="-1">
					<table width="200px">
						<tr>
							<td></td>
							<td width="50%">过滤字:<input type="text"/></td>
							<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
						</tr>
					</table>
					<table  class="subform">${pReferChooser}</table>
				</div>
				
				<div class="referchooser ui-widget-content" id="conf_process_code_referchooser" tabindex="-1">
					<table width="200px">
						<tr>
							<td></td>
							<td width="50%">过滤字:<input type="text"/></td>
							<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
						</tr>
					</table>
					<table  class="subform">${pReferChooser}</table>
				</div>
			</div>
			
			<div class="clear areaencloser"></div>
		</div>
	</div>

</body>
</html>