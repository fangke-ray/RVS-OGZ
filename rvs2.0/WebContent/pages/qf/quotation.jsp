<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">
<link rel="stylesheet" type="text/css" href="css/flowchart.css">
<style>
.width-quotation {
	width: 604px;
	margin:auto;
}
.dwidth-quotation {
	width: 603px;
	margin:auto;
}
.qa_info {
	display : none;
}
</style>

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/jquery.flowchart.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/qf/quotation.js?version=446"></script>
<script type="text/javascript" src="js/qf/set_material_process_assign.js"></script>

<title>报价</title>
</head>
<body class="outer">
<%
Boolean peripheral = (Boolean) request.getAttribute("peripheral");
%>
	<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">

		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="1"/>
			</jsp:include>
		</div>

		<div class="ui-widget-panel ui-corner-all width-full" style="align: center; padding-top: 16px; padding-bottom: 16px;" id="body-pos">
			<div id="body-mdl">
				<div class="dwidth-full" style="margin-left: 8px;">
					<div id="uld_listarea">
						<table id="uld_list"></table>
						<div id="uld_listpager"></div>
						<div id="uld_listedit"></div>
					</div>

					<div>
						<div id="waitarea" class="ui-widget-content" style="float: left;min-height: 250px;">
							<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half">
								<span class="areatitle">暂停/中断区域</span>
							</div>
							<div id="wtg_list" style="overflow-y: auto;max-height:760px;"></div>
						</div>
						<div id="executearea" class="dwidth-half" style="float: right;">
							<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half">
								<span class="areatitle">报价处理</span>
							</div>
							<div class="ui-widget-content dwidth-half">
								<div class="ui-widget-content dwidth-half" id="scanner_container">
									<div class="ui-state-default td-title">扫描录入区域</div>
									<input type="text" id="scanner_inputer" title="扫描前请点入此处" class="scanner_inputer" style="width: 597px;"></input>
									<div style="text-align: center;">
										<img src="images/barcode.png" style="margin: auto; width: 150px; padding-top: 4px;">
									</div>
								</div>
								<div class="ui-widget-content dwidth-half" id="material_details" style="display: none;">
									<form id="editform" method="POST">
										<table class="condform">
											<tbody>
												<tr>
													<td class="ui-state-default td-title">型号</td>
													<td class="td-content"></td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">机身号</td>
													<td class="td-content"></td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">修理单号</td>
													<td class="td-content"></td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">RC</td>
													<td class="td-content">
														<select name="edit_ocm" alt="RC" id="edit_ocm" class="ui-widget-content">
															${edit_ocm}
														</select>
													</td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">客户同意日</td>
													<td class="td-content">
														<input id="edit_agreed_date" alt="客户同意日" maxlength="10" class="ui-widget-content" readonly/>
													</td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">OCM 修理等级</td>
													<td class="td-content">
														<select name="edit_ocm_rank" alt="OCM 修理等级" id="edit_ocm_rank" class="ui-widget-content">
															${options_ocm_rank}
														</select>
													</td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">OCM 配送日</td>
													<td class="td-content">
														<input alt="OCM 配送日" id="edit_ocm_deliver_date" readonly></input>
													</td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">OSH 配送日</td>
													<td class="td-content">
														<input alt="OSH 配送日" id="edit_osh_deliver_date" readonly></input>
													</td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">等级</td>
													<td class="td-content">
														<select name="edit_level" alt="等级" id="edit_level" class="ui-widget-content">
															${edit_level}
														</select>
													</td>
												</tr>
												<tr style="display:none;">
													<td class="ui-state-default td-title">中小修理维修内容流程</td>
													<td class="td-content">
														<input alt="中小修理维修流程" type="button" id="light_pat_button" class="ui-button" value="设定">
													</td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">顾客名</td>
													<td class="td-content">
														<input alt="顾客名" id="edit_customer_name" maxlength="100" style="width:32em;"></input>
													</td>
												</tr>
												<tr class="qa_info">
													<td class="ui-state-default td-title">品保判定等级</td>
													<td class="td-content">
														<label id="edit_qa_level" class="ui-widget-content">
														</label>
													</td>
												</tr>
												<tr class="qa_info">
													<td class="ui-state-default td-title">品保判定有无偿</td>
													<td class="td-content">
														<label id="edit_service_free" class="ui-widget-content">
														</label>
													</td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">备注</td>
													<td class="td-content">
														<label id="edit_direct_flg" style="margin-right:1em;"></label>
														<select name="fix_type" alt="流水线分类" id="edit_fix_type" class="ui-widget-content">
															${edit_fix_type}
														</select>
														<textarea name="comment" id="edit_comment" alt="备注信息" maxlength="225" class="ui-widget-content" rows="5" style="width: 444px;"></textarea>
														<textarea name="comment_other" id="edit_comment_other" alt="澶囨敞淇信息" maxlength="125" class="ui-widget-content" rows="5" style="width: 444px;" disabled readonly></textarea>
														<!--select name="service_repair_flg" alt="返修标记" id="edit_service_repair_flg" class="ui-widget-content">
															${edit_service_repair_flg}
														</select-->
													</td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">返送地区</td>
													<td class="td-content">
														<select id="edit_bound_out_ocm" class="ui-widget-content">
															${edit_material_direct_area}
														</select>
													</td>
												</tr>
												<tr>
													<td class="ui-state-default td-title">销售大区</td>
													<td class="td-content">
														<select id="edit_area" class="ui-widget-content">
															${edit_material_large_area}
														</select>
													</td>
												</tr>

												<tr <%=(peripheral!=null && peripheral) ? "style='display:none;'" : ""%>>
													<td class="ui-state-default td-title">库位</td>
													<td class="td-content">
														<input type="text" readonly style="width:4em;text-align: center;" name="wip_location" id="edit_wip_location"/>
														<input type="button" value="更改" id="edit_wip_location_button" class="ui-button"/>
													</td>
												</tr>
											</tbody>
										</table>
										<div style="height: 44px">
<%
if (peripheral!=null && peripheral) {
%>
											<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="wipconfirmbutton" value="放入WIP" role="button" aria-disabled="false" style="float: right; right: 2px">
<%
}
%>
											<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="confirmbutton" value="修理同意" role="button" aria-disabled="false" style="float: right; right: 2px">
											<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="breakbutton" value="异常中断" role="button" aria-disabled="false" style="float: right; right: 2px">
											<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="stepbutton" value="正常中断" role="button" aria-disabled="false" style="float: right; right: 2px">
											<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="pausebutton" value="暂停" role="button" aria-disabled="false" style="float: right; right: 2px">
											<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="continuebutton" value="重开" role="button" aria-disabled="false" style="float: right; right: 2px;">
										</div>
									</form>
								</div>
							</div>
							<div class="clear"></div>
						</div>
						<div class="clear"></div>
					</div>

<%
if (peripheral!=null && peripheral) {
%>
<%@include file="/widgets/position_panel/device_infect.jsp"%>
<script type="text/javascript" src="js/common/pcs_editor.js?v=446"></script>
<div id="manualdetailarea" style="margin-bottom: 16px;">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
		<span class="areatitle">工程检查票</span>
	</div>
	<div class="ui-widget-content dwidth-full">
		<div id="pcs_pages">
		</div>
		<div id="pcs_contents">
		</div>
	</div>
	<div class="ui-state-default ui-corner-bottom areaencloser dwidth-full"></div>
</div>
<%
}
%>

					<div id="exd_listarea">
						<table id="exd_list"></table>
						<div id="exd_listpager"></div>
						<div id="exd_listedit"></div>
						<div class="ui-widget-content areabase dwidth-full">
							<div id="executes" style="margin-left: 4px; margin-top: 4px;">
								<input type="button" class="ui-button" id="printbutton" value="重新打印现品票" />
								<input type="button" class="ui-button" id="printaddbutton" value="补充打印现品票" />
								<!-- <input type="button" class="ui-button" id="modifybutton" value="报价说明书修改" />
								<input type="button" class="ui-button" id="downloadbutton" value="下载报价说明书" /> -->
								<input type="hidden" id="hide_material_id" value="" />
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="clear areaencloser"></div>
		</div>
		<div id="quotation_pop"></div>
		<div id="break_dialog"></div>

	</div>
	
	<div id="comments_dialog" style="display:none;width:576px;">
		<textarea style="width:90%;height:6em;resize:none;" disabled readonly>
		</textarea>
	</div>
	<div id="light_fix_dialog"></div>
	<input type="hidden" id="paOptions" value='${paOptions }'>
	
</body>
</html>