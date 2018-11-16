<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" import="java.util.Map" isELIgnored="false"%>
<%@ taglib prefix="logic"  uri="http://struts.apache.org/tags-logic"%>
<%@ taglib prefix="bean"  uri="http://struts.apache.org/tags-bean"%>
<%
	String status = (String)request.getAttribute("status");
	String privacy = (String) request.getAttribute("privacy"); 
	boolean isCreate = "create".equals(status);
	boolean isFact = "fact".equals(privacy);
%>
<div id="material_detail_paticalarea">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">维修对象零件订购信息</span>
	</div>
	<div class="ui-widget-content">
		<input type="hidden" id="hidden_partialcode_autocomplete" value='${partialCodeList}'/>
		<input type="hidden" id="hidden_isFact" value='<%=isFact%>'/>
		<table class="condform">
			<tbody>
<%
	if(isFact) {//现品人员 
%>
<%
		if(isCreate){//新建维修对象预提订购单
%>
				<tr>
					<td class="ui-state-default td-title">订单次数</td>
					<td class="td-content">1次</td>
					<td class="ui-state-default td-title">缺品发生工位名称</td>
					<td class="td-content">
						<input type="text" id="insert_bo_position" class="ui-widget-content">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">订单状态</td>
					<td class="td-content">预提</td>
					<td class="ui-state-default td-title">零件订购日</td>
					<td class="td-content"></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">零件到货日</td>
					<td class="td-content"></td>
					<td class="ui-state-default td-title">入库预定日</td>
					<td class="td-content">
						<div id="insert_div_no_arrival_plan_date" style="float: left;">
							<input type="checkbox" id="insert_no_arrival_plan_date" class="ui-widget-content">
							<label for="insert_no_arrival_plan_date">未定</label>
						</div>
						<input type="text" id="insert_arrival_plan_date" class="ui-widget-content" style="margin-top:5px;">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">全局缺品零件</td>
					<td class="td-content" colspan="3" style="width: 640px;position: relative;">
						<textarea class="ui-widget-content" line_id="00000000000" style="width:520px;height:80px;resize:none;"></textarea>
						<input type="text" class="ui-widget-content partial_code" style="width: 100px;position: absolute;right: 0;top:35px;">
					</td>
				</tr>
<%
		}else{
%>
				<tr>
					<td class="ui-state-default td-title">订单次数</td>
					<td class="td-content">
						<select	id="edit_occur_times">
						</select>
					</td>
					<td class="ui-state-default td-title">缺品发生工位名称</td>
					<td class="td-content">
						<input type="text" id="edit_bo_position" class="ui-widget-content">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">订单状态</td>
					<td class="td-content">
						<select id="edit_bo_flg" class="ui-widget-content">${sBoflg}</select>
						<select id="hidden_bo_flg" style="display:none;">${sBoflg}</select>
					</td>
					<td class="ui-state-default td-title">零件订购日</td>
					<td class="td-content">
						<input type="text" id="edit_order_date" class="ui-widget-content">
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">零件到货日</td>
					<td class="td-content">
						<input type="text" id="edit_arrival_date" class="ui-widget-content">
					</td>
					<td class="ui-state-default td-title">入库预定日</td>
					<td class="td-content">
						<div id="div_no_arrival_plan_date" style="float: left;">
							<input type="checkbox" id="edit_no_arrival_plan_date" class="ui-widget-content">
							<label for="edit_no_arrival_plan_date">未定</label>
						</div>
						<input type="text" id="edit_arrival_plan_date" class="ui-widget-content" style="margin-top:5px;">
					</td>
				</tr>
				<logic:iterate id="inline" collection="${inlineList}">
					<tr>
						<td class="ui-state-default td-title"><bean:write name="inline" property="name"/>缺品零件</td>
						<td class="td-content" colspan="3" style="width: 640px;position: relative;">
							<textarea class="ui-widget-content" line_id='<bean:write name="inline" property="id"/>' style="width:520px;height:80px;resize:none;"></textarea>
							<input type="text" class="ui-widget-content partial_code" style="width: 100px;position: absolute;right: 0;top:35px;">
						</td>
					</tr>
				</logic:iterate>
<% 
		}
%>
<% 
	}else{//非现品人员
%>
				<tr>
					<td class="ui-state-default td-title">订单次数</td>
					<td class="td-content">
						<select	id="edit_occur_times">
						</select>
					</td>
					<td class="ui-state-default td-title">缺品发生工位名称</td>
					<td class="td-content">
						<label id="label_bo_position"></label>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">订单状态</td>
					<td class="td-content">
						<label id="label_bo_flg"></label>
					</td>
					<td class="ui-state-default td-title">零件订购日</td>
					<td class="td-content">
						<label id="label_order_date"></label>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">零件到货日</td>
					<td class="td-content">
						<label id="label_arrival_date"></label>
					</td>
					<td class="ui-state-default td-title">入库预定日</td>
					<td class="td-content">
						<label id="label_arrival_plan_date"></label>
					</td>
				</tr>
				<logic:iterate id="inline" collection="${inlineList}">
					<tr>
						<td class="ui-state-default td-title"><bean:write name="inline" property="name"/>缺品零件</td>
						<td class="td-content" colspan="3">
							<textarea class="ui-widget-content" line_id='<bean:write name="inline" property="id"/>' style="width:640px;height:80px;resize:none;" disabled="disabled"></textarea>
						</td>
					</tr>
				</logic:iterate>
<%
	}
%>
			</tbody>
		</table>
	</div>
</div>
