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
<style type="text/css">
.require_check{
	background-color:red;
	color:white;
}
.close_to_check{
	background-color:#FFC000;
}
.wait{
	background-color:#a6a6a6;
}
.spare{
	background-color:#fabf8f;
}

#update_check_cost,#add_check_cost{
	ime-mode: disabled;
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
<script type="text/javascript" src="js/infect/external_adjustment.js"></script>

<title>检查机器校正</title>
</head>
<body class="outer" style="align: center;">
	<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">
		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="3" />
			</jsp:include>
		</div>
		
		<div class="ui-widget-panel ui-corner-all width-full" style="align: center; padding-top: 16px; padding-bottom: 16px; width: 1266px;" id="body-2">
			<div id="body-lft" style="width: 256px; float: left;">
				<jsp:include page="/appmenu.do?method=tinit" flush="true">
					<jsp:param name="linkto" value="设备工具•专用工具点检" />
				</jsp:include>
			</div>
			
			<div id="body-mdl" style="width: 994px; float: left;">
				<div id="searcharea" class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					<span class="areatitle">检索条件</span>
					<a target="_parent" role="link" href="javascript:void(0)" class="HeaderButton areacloser">
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
										<input id="search_name" class="ui-widget-content" type="text" readonly="readonly">
										<input type="hidden" id="hidden_devices_type_id">
									</td>
									<td class="ui-state-default td-title">厂商</td>
									<td class="td-content">
										<input type="text" id="search_brand_id" readonly="readonly" class="ui-widget-content">
										<input type="hidden" id="hidden_search_brand_id">
									</td>
									<td class="ui-state-default td-title">型号</td>
									<td class="td-content">
										<input type="text" id="search_model_name" class="ui-widget-content">
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">管理编号</td>
									<td class="td-content" >
										<input type="text" id="search_manage_code" class="ui-widget-content">
									</td>
									<td class="ui-state-default td-title">分发课室</td>
									<td class="td-content" colspan="3">
										<select id="search_section_id">${sectionOptions}</select>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">计量器具<br>管理编号</td>
									<td class="td-content" >
										<input type="text" id="search_measuring_instrument_manage_code" class="ui-widget-content">
									</td>
									<td class="ui-state-default td-title" rowspan="2">责任工程</td>
									<td class="td-content" rowspan="2" colspan="3">
										<select id="search_responsible_line_id">${lineOptions }</select>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">出厂编号</td>
									<td class="td-content">
										<input type="text" id="search_products_code" class="ui-widget-content">
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">校验日期</td>
									<td>
										<input type="text" class="ui-widget-content" id="search_checked_date_start"readonly="readonly">起
										<input type="text" class="ui-widget-content" id="search_checked_date_end"readonly="readonly">止
									</td>
									<td class="ui-state-default td-title">过期日期</td>
									<td>
										<input type="text" class="ui-widget-content" id="search_available_end_date_start"readonly="readonly">起
										<input type="text" class="ui-widget-content" id="search_available_end_date_end"readonly="readonly">止
									</td>
									<td class="ui-state-default td-title">有效期</td>
									<td class="td-content">
										<select id="search_effect_interval">${goEffectInterval }</select>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">校验单位</td>
									<td class="td-content">
										<select id="search_organization_type">${goOrganizationType }</select>
									</td>
									<td class="ui-state-default td-title">校验机构名称</td>
									<td class="td-content">
										<input type="text" id="search_institution_name" class="ui-widget-content">
									</td>
									<td class="ui-state-default td-title">闲置状态</td>
									<td class="td-content">
										<div id="search_idle_flg" class="ui-buttonset">
											<input type="radio" name="idle_flg" id="search_idle_flg_all" class="ui-widget-content ui-helper-hidden-accessible" value="0" checked><label for="search_idle_flg_all" aria-pressed="false">(全部)</label>
											<input type="radio" name="idle_flg" id="search_idle_flg_y" class="ui-widget-content ui-helper-hidden-accessible" value="-1"><label for="search_idle_flg_y" aria-pressed="false">非闲置</label>
											<input type="radio" name="idle_flg" id="search_idle_flg_n" class="ui-widget-content ui-helper-hidden-accessible" value="1"><label for="search_idle_flg_n" aria-pressed="false">闲置</label>
										</div>
									</td>
								</tr>	
							</tbody>
						</table>
						<div style="height:44px">
							<input id="resetbutton" type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all"  style="float:right;right:2px" aria-disabled="false" role="button" value="清除">
							<input id="searchbutton" type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all"  value="检索" role="button" aria-disabled="false" style="float:right;right:2px">
							<input type="hidden" id="sEffectInterval" value="${sEffectInterval }">
							<input type="hidden" id="sOrganizationType" value="${sOrganizationType }">
						</div>
					</form>
				</div>
				
				<div class="clear areaencloser"></div>
				
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
					<span class="areatitle">校验费用总计</span>
				</div>
				<div class="ui-widget-content">
					<table class="condform">
						<tr>
							<td class="ui-state-default td-title">校验机构</td>
							<td class="td-content">
								<label>${institution }</label>&nbsp;&nbsp;RMB
							</td>
							<td class="ui-state-default td-title">国内厂商校验</td>
							<td class="td-content">
								<label>${domestic }</label>&nbsp;&nbsp;RMB
							</td>
							<td class="ui-state-default td-title">国外厂商校验</td>
							<td class="td-content">
								<label>${abroad }</label>&nbsp;&nbsp;USD
							</td>
						</tr>
					</table>
				</div>
				
				<div id="listarea" class="">
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
						<span class="areatitle">检查机器校验一览</span>
						<a target="_parent" role="link" href="javascript:void(0)" class="HeaderButton areacloser">
							<span class="ui-icon ui-icon-circle-triangle-n"></span>
						</a>
					</div>
					<table id="list"></table>
					<div id="listpager"></div>
				</div>
				<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase" style="padding-top:4px;margin-top:24px;">
					<input id="addtbutton" type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all"  style="float:right;right:2px" aria-disabled="false" role="button" value="设定校验对象">
					<input id="stop" type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all"  style="float:right;right:2px" aria-disabled="false" role="button" value="停止校验">
					<input id="tocheck" type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all"  style="float:right;right:2px" aria-disabled="false" role="button" value="送检">
				</div>
			</div>
			
			<div class="clear"></div>
			
			
			<!-- 新建开始 -->
			<div id="add" style="display:none">
				<form id="add_form" method="POST">
					<table class="condform">
						<tbody>
							<tr>
								<td class="ui-state-default td-title">管理编号</td>
									<td class="td-content" >
										<input type="text" id="add_manage_code" class="ui-widget-content" readonly="readonly">
										<input type="hidden" id="hidden_devices_manage_id2" name="devices_manage_id" alt="管理编号">
									</td>
								</tr>
							<tr>
								<td class="ui-state-default td-title">品名</td>
								<td class="td-content">
									<label id="label_name"></label>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">厂商</td>
								<td class="td-content">
									<label id="label_brand"></label>
									<input type="text" id="text_add_brand" class="ui-widget-content"></input>
									<input type="hidden" id="hidden_add_brand_id" name="brand_id">						
									<input type="button" id="label_brand_add_button" class="ui-button" value="…" style="padding: 0 3px;">						
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">型号</td>
								<td class="td-content">
									<label id="label_model_name"></label>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">出厂编号</td>
								<td class="td-content">
									<label id="label_products_code"></label>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">分发课室</td>
								<td class="td-content">
									<label id="label_section_name"></label>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">责任工程</td>
								<td class="td-content">
									<label id="label_responsible_line_name"></label>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">校验日期</td>
								<td>
									<input type="text" class="ui-widget-content" id="add_checked_date" name="checked_date" readonly="readonly" alt="校验日期">
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">有效期</td>
								<td class="td-content">
									<select id="add_effect_interval" name="effect_interval" alt="有效期">${goEffectInterval }</select>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">校验费用</td>
								<td>
									<input type="text" class="ui-widget-content" id="add_check_cost">
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">校验单位</td>
								<td class="td-content">
									<select id="add_organization_type" name="organization_type" alt="校验单位">${goOrganizationType }</select>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">校验机构名称</td>
								<td class="td-content">
									<input type="text" id="add_institution_name" class="ui-widget-content">
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">闲置状态</td>
								<td  class="td-content">
									<div id="add_idle_flg" class="ui-buttonset">
										<input type="radio" name="idle_flg" id="idle_flg_y" class="ui-widget-content ui-helper-hidden-accessible" value="0" checked="checked"><label for="idle_flg_y" aria-pressed="false">非闲置</label>
										<input type="radio" name="idle_flg" id="idle_flg_n" class="ui-widget-content ui-helper-hidden-accessible" value="1"><label for="idle_flg_n" aria-pressed="false">闲置</label>
									</div>
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">备注</td>
								<td  class="td-content">
									<textarea id="add_comment" style="resize:none;" rows="3" cols="35" class="ui-widget-content"></textarea>
								</td>
							</tr>
						</tbody>
					</table>
				</form>
			</div>
			<!-- 新建结束 -->
			
			<!-- 详细信息开始 -->
			<div id="detail" style="display:none">
				<form id="updateForm">
					<table class="condform">
						<tbody>
							<tr>
								<td class="ui-state-default td-title">管理编号</td>
									<td class="td-content" >
										<label id="detail_label_manage_code"></label>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">品名</td>
									<td class="td-content">
										<label id="detail_label_name"></label>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">厂商</td>
									<td class="td-content">
										<label id="detail_label_brand"></label>
										<input type="text" id="text_update_brand" class="ui-widget-content"></input>
										<input type="hidden" id="hidden_update_brand_id" name="brand_id">						
										<input type="button" id="label_brand_update_button" class="ui-button" value="…" style="padding: 0 3px;">						
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">型号</td>
									<td class="td-content">
										<label id="detail_label_model_name"></label>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">出厂编号</td>
									<td class="td-content">
										<label id="detail_label_products_code"></label>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">分发课室</td>
									<td class="td-content">
										<label id="detail_label_section_name"></label>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">责任工程</td>
									<td class="td-content">
										<label id="detail_label_responsible_line_name"></label>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">校验日期</td>
									<td class="td-content">
										<input type="text" id="update_checked_date" name="checked_date" alt="校验日期" class="ui-widget-content" readonly="readonly"></input>
										<label id="detail_label_checked_date"></label>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">过期日期</td>
									<td class="td-content">
										<label id="detail_label_available_end_date"></label>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">有效期</td>
									<td class="td-content">
										<label id="detail_label_effect_interval"></label>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">校验费用</td>
									<td>
										<input type="text" id="update_check_cost" name="check_cost" alt="校验费用" class="ui-widget-content" ></input>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">校验单位</td>
									<td class="td-content">
										<select id="update_organization_type" name="organization_type" alt="校验单位">${goOrganizationType2 }</select>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">校验机构名称</td>
									<td class="td-content">
										<input type="text" id="update_institution_name"  class="ui-widget-content" size="30"></input>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">闲置状态</td>
									<td  class="td-content">
										<div id="update_idle_flg" class="ui-buttonset">
											<input type="radio" name="idle_flg" id="update_idle_flg_y" class="ui-widget-content ui-helper-hidden-accessible" value="0"><label for="update_idle_flg_y" aria-pressed="false">非闲置</label>
											<input type="radio" name="idle_flg" id="update_idle_flg_n" class="ui-widget-content ui-helper-hidden-accessible" value="1"><label for="update_idle_flg_n" aria-pressed="false">闲置</label>
										</div>
									</td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">备注</td>
									<td  class="td-content">
										<textarea id="update_comment" style="resize:none" rows="3" cols="35"></textarea>
									</td>
								</tr>
						</tbody>
					</table>
					<input type="hidden" id="hidden_effect_interval">
					<input type="hidden" id="hidden_devices_manage_id">
					<input type="hidden" id="hidden_checked_date">
				</form>
			</div>
			<!-- 详细信息结束 -->
			<div id="managecode_referchooser" class="referchooser ui-widget-content" tabindex="-1" style="z-index:10000">
				 <table>
					<tbody>
					   <tr>
							<td width="50%">过滤字:<input type="text"></td>	
							<td align="right" width="50%"><input aria-disabled="false" role="button" class="ui-button ui-widget ui-state-default ui-corner-all" style="float:right;" value="清空" type="button"></td>
					   </tr>
				   </tbody>
			  	 </table>
			  	 <table class="subform"></table>
			</div>

<div class="referchooser ui-widget-content" id="brand_referchooser" tabindex="-1" style="z-index:1024;">
	<table width="200px">
		<tr>
			<td></td>
			<td width="50%">过滤字:<input type="text"/></td>
			<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
		</tr>
	</table>
	<table  class="subform">${bReferChooser}</table>
</div>
			
			<div id="name_referchooser" class="referchooser ui-widget-content" tabindex="-1" style="z-index:10000">
				 <table>
					<tbody>
					   <tr>
							<td width="50%">过滤字:<input type="text"></td>	
							<td align="right" width="50%"><input aria-disabled="false" role="button" class="ui-button ui-widget ui-state-default ui-corner-all" style="float:right;" value="清空" type="button"></td>
					   </tr>
				   </tbody>
			  	 </table>
			  	 <table class="subform">${nReferChooser}</table>
			</div>
			
		</div>
	</div>


	<%@include file="../../widgets/infect/brand-detail.jsp"%>

</body>
</html>