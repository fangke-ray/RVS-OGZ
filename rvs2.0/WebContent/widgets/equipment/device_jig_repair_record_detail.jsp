<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<style>
.djr_comment {
	width: 50em;
	height: 6em;
}
#djr_costs table tbody tr td:nth-child(n+3){
	text-align: right;
}
#djr_costs table input[type="number"]{
	text-align: right;
	width:10em;
}
#djr_photo {
	max-width:320px;
	max-height:276px;
}
#djr_repairarea .ui-widget-content textarea.ui-state-disabled {
	opacity : 1;
}
</style>
<div id="djr_repairarea">
<%
	String privacy = (String) request.getAttribute("privacy");
	boolean isTech = ("technology").equals(privacy);
%>
	<div class="ui-widget-content">
		<form method="POST">
			<table class="condform">
				<tbody>
					<tr>
						<td class="ui-state-default td-title">责任工程</td>
				   		<td class="td-content">
							<label id="djr_line_name"></label>
						</td>
						<td class="ui-state-default td-title">修理依赖者</td>
				   		<td class="td-content">
							<label id="djr_submitter_name"></label>
						</td>
						<td class="ui-state-default td-title">报修时间</td>
				   		<td class="td-content">
							<label id="djr_submit_time"></label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">管理编号</td>
				   		<td class="td-content">
							<label id="djr_manage_code"/>
						</td>
						<td class="ui-state-default td-title">设备工具名</td>
				   		<td class="td-content">
							<label id="djr_object_name"></label>
						</td>
						<td class="ui-state-default td-title">型号</td>
						<td class="td-content">
							<label id="djr_model_name"/>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">故障现象</td>
				   		<td class="td-content" colspan="3">
							<textarea class="djr_comment" id="djr_phenomenon">
							</textarea>
						</td>
						<td class="ui-state-default td-title">修理担当</td>
						<td class="td-content">
							<label id="djr_maintainer_name"/>
						</td>
					</tr>
					<tr id="djr_costs">
						<td class="ui-state-default td-title">设备更换部件</td>
				   		<td class="td-content" colspan="5">
							<table class="condform">
								<thead>
								<tr>
									<td class="ui-state-default td-title">名称</td><td class="ui-state-default td-title">型号</td>
									<td class="ui-state-default td-title">数量</td><td class="ui-state-default td-title">单价</td>
									<td class="ui-state-default td-title">合计</td>
									<td class="ui-state-default td-title">设备委外修理报价</td><td class="ui-state-default td-title">节省金额</td>
								</tr>
								</thead>
								<tbody>
								</tbody>
							</table>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">故障和原因</td>
				   		<td class="td-content" colspan="3">
							<textarea class="djr_comment" id="djr_fault_causes">
							</textarea>
						</td>
						<td class="ui-state-default td-title">原因分类</td>
						<td class="td-content">
							<select id="djr_cause_type">
							${ctOptions}
							</select>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title" rowspan="2">修理对策和方向</td>
				   		<td class="td-content" colspan="3" rowspan="2">
							<textarea id="djr_countermeasure" class="djr_comment">
							</textarea>
						</td>
						<td class="ui-state-default td-title" colspan="2">
							故障相关照片
							<input type="button" class="ui-button" id="djr_uploadphotobutton" value="上传" style="padding:0 0.2em;">
							<input type="file" name="file" id="djr_update_photo" style="display:none;">
							<input type="button" class="ui-button" id="djr_delphotobutton" value="删除" style="padding:0 0.2em;">
						</td>
					</tr>
					<tr>
				   		<td class="td-content" colspan="2" rowspan="4">
							<img id="djr_photo"/>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">备注</td>
				   		<td class="td-content" colspan="3">
							<textarea id="djr_comment" class="djr_comment">
							</textarea>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">停线时间</td>
				   		<td class="td-content">
							<input type="number" id="djr_line_break" style="width:5em;"/> 分钟
						</td>
						<td class="ui-state-default td-title">是否需要备品</td>
				   		<td class="td-content">
							<input type="text" id="djr_spare_supplement"/>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">是否需要<br>追加点检项目</td>
				   		<td class="td-content">
							<input type="text" id="djr_additional_infect_feature"/>
						</td>
						<td class="ui-state-default td-title">此次发现其它隐患</td>
				   		<td class="td-content">
							<textarea id="djr_latent_trouble">
							</textarea>
						</td>
					</tr>
				</tbody>
			</table>
			<div style="height:44px">
				<input type="button" class="ui-button" id="djr_cancelbutton" value="取消" style="float:right;right:2px;">
<%
	if (isTech) {
%>
				<input type="button" class="ui-button" id="djr_submitbutton" value="提交" style="float:right;right:2px;">
<%
	}
%>
			</div>
		</form>
	</div>
</div>