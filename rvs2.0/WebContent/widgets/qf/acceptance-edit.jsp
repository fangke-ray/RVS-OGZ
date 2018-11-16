<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="framework.huiqing.common.util.CodeListUtils"%>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>

<script type="text/javascript">
	$(function() {
	$("#set_normal").button();
	$("#set_normal").click(function(){
		$("#is_direct, #reason_type_warranty, #reason_type_new").attr("checked",false);
		$("#is_direct + label, #reason_type_warranty + label, #reason_type_new + label").removeClass("ui-state-active");
	});
	$("#is_direct, #reason_type_warranty, #reason_type_new").click(function(){
		$("#set_normal").attr("checked",false);
		$("#set_normal + label").removeClass("ui-state-active");
	});

//	$("#is_direct").button();
//	$("#reason_type_set").buttonset();
//	$("#handle_type_set").buttonset();

	});
</script>

	<form id="ins_material">
		<input type="hidden" id="material_id">
		<table class="condform">
			<tbody>
				<tr>
					<td class="ui-state-default td-title">修理单号</td>
					<td class="td-content" style="width: 240px;" colspan="3">
						<input type="text" class="ui-widget-content" id="edit_sorcno" name="sorcno" alt="修理单号" maxlength="15"></input>
					</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title">型号</td>
				<td class="td-content" colspan="3">
					<input type="text" class="ui-widget-content" readonly="readonly" id="inp_modelname" name="modelname" alt="型号" style="width:14em;">
					<input type="hidden" id="edit_modelname">
				</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title">机身号</td>
				<td class="td-content" colspan="3">
					<input type="text" class="ui-widget-content" id="edit_serialno" name="serialno" alt="机身号" maxlength="12"></input>
				</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title">RC</td>
				<td class="td-content" colspan="3">
					<select id="edit_ocm" class="ui-widget-content">
						<%=CodeListUtils.getSelectOptions("material_ocm", null, "", false) %>
					</select>
				</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title" rowspan="2">OCM 修理等级</td>
				<td class="td-content" rowspan="2">
					<select id="edit_ocm_rank" class="ui-widget-content">
						<%=CodeListUtils.getSelectOptions("material_ocm_direct_rank", null, "", false) %>
					</select>
				</td>
				<td class="ui-state-default td-title">OCM 配送日</td>
				<td class="td-content">
					<input type="text" id="edit_ocm_deliver_date" readonly></input>
				</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title">OSH 配送日</td>
				<td class="td-content">
					<input type="text" id="edit_osh_deliver_date" readonly></input>
				</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title">客户同意日</td>
				<td class="td-content" colspan="3">
					<input type="text" id="edit_agreed_date" readonly></input>
				</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title">修理等级</td>
				<td class="td-content" colspan="3">
					<select id="edit_level" class="ui-widget-content">
						<%=CodeListUtils.getSelectOptions("material_level", null, "", false) %>
					</select>
				</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title">顾客名</td>
				<td class="td-content" colspan="3">
					<input type="text" id="edit_customer_name" maxlength="100" style="width:40em;"></input>
				</td>
				</tr>
				<tr>
				<td rowspan="4" class="ui-state-default td-title">备注</td>
				<td class="td-content" colspan="3">
					<select id="edit_direct" class="ui-widget-content">
						<%=CodeListUtils.getSelectOptions("material_direct", null, "(普通)", false) %>
					</select>
				</td>
				</tr>
				<tr>
				<td class="td-content" colspan="3">
					<select id="edit_service_repair" class="ui-widget-content">
						<%=CodeListUtils.getSelectOptions("material_service_repair", null, "", false) %>
					</select>
				</td>
				</tr>
				<tr>
				<td class="td-content" colspan="3">
					<select id="edit_fix_type" class="ui-widget-content">
						<%=CodeListUtils.getSelectOptions("material_fix_type", null, null, false) %>
					</select>
				</td>
				</tr>
				<tr>
				<td class="td-content" colspan="3">
					<select id="edit_selectable" class="ui-widget-content">
						<option value="0">(普通)</option>
						<option value="1">选择式报价</option>
					</select>
				</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title">销售大区</td>
				<td class="td-content" colspan="3">
					<select id="edit_area" class="ui-widget-content">
						<%=CodeListUtils.getSelectOptions("material_large_area", null, "(无)", false) %>
					</select>
				</td>
				</tr>
				<tr>
				<td class="ui-state-default td-title">返送地区</td>
				<td class="td-content" colspan="3">
					<select id="edit_bound_out_ocm" class="ui-widget-content">
						<%=CodeListUtils.getSelectOptions("material_direct_area", null, "(无)", false) %>
					</select>
				</td>
				</tr>
			</tbody>
		</table>
	</form>
	

<div class="referchooser ui-widget-content" id="referchooser_edit" tabindex="-1">
	<table>
		<tr>
			<td width="50%">过滤字:<input type="text"/></td>	
			<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
		</tr>
	</table>
	
	<table class="subform"><%=session.getAttribute("mReferChooser")%></table>
<div>

