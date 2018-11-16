<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="framework.huiqing.common.util.CodeListUtils"%>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">

	<form id="ins_serviceRepairManage">
		<input type="hidden" id="material_id">
		<table class="condform">
			<tbody>
				<tr>
					<td class="ui-state-default td-title">型号</td>
					<td class="td-content">
						<label id="add_model_name" name="model_name" alt="型号" ></label>
					</td>
					<td class="ui-state-default td-title">机身号</td>
					<td class="td-content">
						<label  id="add_serial_no" name="serial_no"alt="机身号"  ></label>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">修理编号</td>
					<td class="td-content">
						<label  id="add_sorc_no" name="sorc_no" alt="修理编号" ></label>
					</td>
					<td class="ui-state-default td-title">类别</td>
					<td class="td-content">
						<select id="add_service_repair_flg" name="service_repair_flg" alt="类别"class="ui-widget-content">${sQaMaterialServiceRepair}</select>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">RC邮件发送日</td>
					<td class="td-content">
						<label id="add_rc_mailsend_date"name="rc_mailsend_date" alt="RC邮件发送日"   ></label>
					</td>
					<td class="ui-state-default td-title">RC出货指示日</td>
					<td class="td-content">
						<label   id="add_rc_ship_assign_date"  alt="RC出货指示日" ></label>						
					</td>	
					
				</tr>
				<tr>
					<td class="ui-state-default td-title">QA受理日</td>
					<td class="td-content">
						<label id="add_qa_reception_time"name="qa_reception_time" alt="QA受理日"   ></label>
					</td>
					<td class="ui-state-default td-title">QA判定日</td>
					<td class="td-content">
						<label   id="add_qa_referee_time" name="qa_referee_time"  alt="QA判定日" ></label>
						<label id="add_answer_in_deadline" alt="答复时限"></label>
					</td>	
					
				</tr>
				<tr>					
					 <td class="ui-state-default td-title">等级</td>
				    <td class="td-content">
						<input name="rank" alt="等级" style="border:none;" id="add_rank" type="text" class="ui-widget-content" disabled></input>
					</td>
					 <td class="ui-state-default td-title">有无偿</td>
				    <td class="td-content">
				    	<select id="add_search_service_free_flg"  alt="有无偿" class="ui-widget-content"></select>
				    </td>
				</tr>				
				<tr>				    
				   <td class="ui-state-default td-title">本次编缉作为二次判定</td>
				    <td class="td-content" id="add_twojudge">
						<input type="radio" name="qa_secondary_referee_date" id="add_twojudge_yes" class="ui-widget-content ui-helper-hidden-accessible"  value="1"><label for="add_twojudge_yes" aria-pressed="false">是</label>
						<input type="radio" name="qa_secondary_referee_date" id="add_twojudge_no" class="ui-widget-content ui-helper-hidden-accessible" value="0" checked="checked"><label for="add_twojudge_no" aria-pressed="false">否</label>
					</td>	
				    <td class="ui-state-default td-title">维修站</td>
				    <td class="td-content">
						<select id="add_workshop"  alt="维修站"class="ui-widget-content"></select>
					</td>
				</tr>	
				<tr>
					<td class="ui-state-default td-title">处理对策</td>
				    <td class="td-content">
						<textarea id="add_countermeasures" name="countermeasures" alt="处理对策"   cols=35 rows=2 class="ui-widget-content"></textarea>
					</td>
					<td class="ui-state-default td-title">备注</td>
				    <td class="td-content">
						<textarea id="add_comment" name="comment"  alt="备注"  cols=35 rows=2 class="ui-widget-content"></textarea>
					</td>
				</tr>
			</tbody>
		</table>		
	</form>