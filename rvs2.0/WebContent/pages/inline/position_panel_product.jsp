<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link rel="stylesheet" type="text/css" href="css/custom.css?version=418">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">

<style>
.working_status {
	background-color:white;
	padding-left:6px;
	padding-right:6px;
	text-align: center;
}
.anim_pause {
	animation-play-state: paused;
	-webkit-animation-play-state: paused;
	-moz-animation-play-state: paused;
}

@keyframes moveseconds {
	0% {top: 0;}
	100% {top: -160px;} 
}

@-webkit-keyframes moveseconds {
	0% {top: 0;}
	100% {top: -160px;} 
}

@-moz-keyframes moveseconds {
	0% {top: 0;}
	100% {top: -160px;} 
}

@keyframes movetenseconds {
	0% {top: 0;}
	100% {top: -96px;} 
}

@-webkit-keyframes movetenseconds {
	0% {top: 0;}
	100% {top: -96px;} 
}

@-moz-keyframes movetenseconds {
	0% {top: 0;}
	100% {top: -96px;} 
}

.roll_cell {
	top: 1px;
	height: 16px;
	overflow: hidden;
	position: relative;
	float: right;
}

.roll_seconds {
	line-height: 16px;
	width: 7px;
	text-align: center;
	position: absolute;
	top: 0;
	left: 0;
	-webkit-animation: moveseconds 10s steps(10, end) infinite;
	-moz-animation: moveseconds 10s steps(10, end) infinite;
	animation: moveseconds 10s steps(10, end) infinite;
}

.roll_tenseconds {
	line-height: 16px;
	width: 7px;
	text-align: center;
	position: absolute;
	top: 0;
	left: 0;
	-webkit-animation: movetenseconds 60s steps(6, end) infinite;
	-moz-animation: movetenseconds 60s steps(6, end) infinite;
	animation: movetenseconds 60s steps(6, end) infinite;
}

</style>
<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>

<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>

<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/common/pcs_editor.js"></script>
<script type="text/javascript" src="js/inline/position_panel_product.js"></script>
<script type="text/javascript" src="js/common/material_detail_ctrl.js"></script>

<title>欢迎登录RVS系统</title>
</head>
<body class="outer">

	<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">

		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="1"/>
			</jsp:include>
		</div>

		<div class="ui-widget-panel width-full" style="align: center; padding-top: 16px;" id="body-pos">

			<div id="workarea">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
					<span id="position_name" class="areatitle">工作信息</span> <span id="position_status" class="areatitle working_status">${position.status}</span>
				</div>
				<div class="ui-widget-content dwidth-full">
					<table class="condform">
						<tbody>
							<tr>
								<td class="ui-state-default td-title">生产课室</td>
								<td class="td-content-text">${userdata.section_name}</td>
								<td class="ui-state-default td-title">工程名</td>
								<td class="td-content-text">${userdata.line_name}</td>
								<td class="ui-state-default td-title">工位号<input type="hidden" id="g_pos_id" value="${userdata.section_id}#${userdata.position_id}"/></td>
								<td class="td-content-text">${userdata.process_code}</td>
								<td class="ui-state-default td-title">工位名称</td>
								<td class="td-content-text">${userdata.position_name}</td>
							</tr>

							<tr style="display: table-row;">
								<td class="ui-state-default td-title">操作人员</td>
								<td class="td-content-text">${userdata.name}</td>
								<td class="ui-state-default td-title">工作时间</td>
								<td class="td-content-text" id="p_run_cost">${position.run_cost}</td>
								<td class="ui-state-default td-title">操作时间</td>
								<td class="td-content-text" id="p_operator_cost">${position.operator_cost}</td>
								<td class="ui-state-default td-title">完成件数</td>
								<td class="td-content-text" id="p_finish_count">${position.finish_count}台</td>
							</tr>
							<tr id="toInfect" style="display:none;">
								<td class="ui-state-default td-title" style="background:#f9ec54;">待处理点检项目</td>
								<td class="td-content-text" colspan="7" id="infecttext" style="text-align: left;"></td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="clear areaencloser"></div>
			</div>

			<div class="dwidth-full">
				<div id="storagearea" style="float: left;">
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half">
						<span class="areatitle">等待区信息</span>
					</div>
					<div class="ui-widget-content dwidth-half" style="height: 215px; overflow-y: auto; overflow-x: hidden;">
						<div id="waitings" style="margin: 20px;">
						</div>
					</div>
				</div>

				<div id="manualarea" style="float: right;">
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-half">
						<span class="areatitle">作业产品信息</span>
					</div>

					<div class="ui-widget-content dwidth-half" id="scanner_container" style="min-height: 215px;">
						<div class="ui-state-default td-title">扫描录入区域</div>
						<input type="text" id="scanner_inputer" title="扫描前请点入此处" class="scanner_inputer dwidth-half"></input>
						<div style="text-align: center;">
							<img src="images/barcode.png" style="margin: auto; width: 150px; padding-top: 4px;">
						</div>
					</div>
					<div class="ui-widget-content dwidth-half" id="material_details" style="min-height: 215px;">
						<table class="condform">
							<tbody>
								<tr>
									<td class="ui-state-default td-title">机种</td>
									<td class="td-content-text"></td>
									<td class="ui-state-default td-title">型号<input type="hidden" id="pauseo_material_id"></td>
									<td class="td-content-text"></td>
									<td class="ui-state-default td-title">序列号</td>
									<td class="td-content-text"></td>
								</tr>
								<tr style="display: table-row;">
									<td class="ui-state-default td-title">开始时间</td>
									<td class="td-content-text"></td>
									<td class="ui-state-default td-title">作业标准时间</td>
									<td class="td-content-text"></td>
									<td class="ui-state-default td-title">作业经过时间</td>
									<td class="td-content-text" id="dtl_process_time"><div class="roll_cell"><div class="roll_seconds">0 1 2 3 4 5 6 7 8 9</div></div><div class="roll_cell"><div class="roll_tenseconds">0 1 2 3 4 5 6</div></div><label style="float:right;"></label></td>
								</tr>
								<tr>
									<td class="ui-state-default td-title">标准进行度</td>
									<td colspan="5" class="td-content-text slim">
										<div class="waiting tube" id="p_rate" style="height: 20px; margin: auto;"></div>
									</td>
								</tr>
								<tr>
								</tr>
							</tbody>
						</table>
						<div style="height: 44px">
							<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="finishbutton" value="完成" role="button" aria-disabled="false" style="float: right; right: 2px;">
							<input type="button" class="ui-button ui-widget ui-state-default ui-corner-all" id="breakbutton" value="异常中断" role="button" aria-disabled="false" style="float: right; right: 2px;">
							<input type="button" class="ui-button ui-widget ui-state-default ui-corner-all" id="stepbutton" value="正常中断" role="button" aria-disabled="false" style="float: right; right: 2px;">
							<input type="button" class="ui-button ui-widget ui-state-default ui-corner-all" id="pausebutton" value="暂停" role="button" aria-disabled="false" style="float: right; right: 2px;">
							<input type="button" class="ui-button ui-widget ui-state-default ui-corner-all" id="continuebutton" value="重开" role="button" aria-disabled="false" style="float: right; right: 2px;">
						</div>
					</div>

				</div>

				<div class="clear areaencloser"></div>
			</div>

<%
	Boolean use_snout = (Boolean) request.getAttribute("use_snout");
	if (use_snout) {
%>
			<div id="usesnoutarea" style="margin-bottom: 16px;display:none;">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
					<span class="areatitle">使用完成部组</span>
				</div>
				<div class="ui-widget-content dwidth-full" id="select_snout">
					<table class="condform" id="snoutpane">
						<tbody>
							<tr>
								<td class="ui-state-default td-title">当前组装型号</td>
								<td class="td-content-text"></td>
								<td class="ui-state-default td-title">可使用部组</td>
								<td class="td-content-text"><input type="text" readonly></input><input type="hidden" name="privacy" id="input_snout"></input></td>
								<td class="ui-state-default td-title">已使用部组</td>
								<td class="td-content-text"><label type="text" id="used_snouts" /></td>
							</tr>
						</tbody>
					</table>
					<div style="height: 44px">
						<input type="button" class="ui-button ui-widget ui-state-default ui-corner-all" id="unusesnoutbutton" value="取消使用部组" role="button" aria-disabled="false" style="float: right; right: 2px;">
					</div>
				</div>
				<div class="ui-state-default ui-corner-bottom areaencloser dwidth-full"></div>
				<div class="referchooser ui-widget-content" tabindex="-1" style="z-index:80;">
					<table class="subform" id="snouts">
					<thead><th class="ui-state-default" style="padding: 0 0.5em;">部组序列号</th></thead>
					<tbody></tbody></table>
				</div>
			</div>
<%
	}
%>

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

		</div>
	</div>
	<div id="process_dialog"></div>
	<div id="break_dialog"></div>
	<div id="comments_dialog" style="display:none;width:576px;">
		<textarea style="width:90%;height:6em;resize:none;" disabled readonly>
		</textarea>
	</div>
	<div id="comments_sidebar" style="display:none;">
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix">
			<span class="areatitle icon-enter-2">提示相关信息</span>
		</div>
		<div class="comments_area">
		</div>
	</div>
	<input type="hidden" id="hidden_workstauts" value=""/>
</body>
</html>