<!DOCTYPE html>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" import="java.util.Map" isELIgnored="false"%>
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<script>
var getChangeSet = function(xhrobj){

	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		if (resInfo.errors.length > 0) {
			// 共通出错信息框
			treatBackMessages(null, resInfo.errors);

		} else {
			if (resInfo.position_link) {
				window.location.href = resInfo.position_link;
			}
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};	
};

var getPositionWork = function(position_id, px){
	var data = {
		position_id : position_id,
		px: px
	}
	$.ajax({
		beforeSend : ajaxRequestType,
		async : false,
		url : 'panel.do?method=changeposition',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : getChangeSet
	});
};

$(function() {
	$("a").attr("target", "_parent");

	var accordion_idx = $("#accordion h3").index($("#accordion h3:contains('" + $("#linkto").val() + "')"));
	if (accordion_idx < 0) accordion_idx = 0;
	$("#accordion" ).accordion({active: accordion_idx, autoHeight : false});
	$('#datepicker').datepicker({
		inline: true,
		width: 224
	});
	$('#datepicker div.ui-datepicker-inline').css("width","219px");

	$("#menucontainner").hide();
});

</script>
<body>
<% Map<String, Boolean> links = (Map<String, Boolean>) request.getAttribute("menuLinks"); %>
<div id="modelmenuarea">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-left">
		<span class="areatitle">功能菜单</span>
		<input type="hidden" id="linkto" value="${linkto}">
	</div>
	<div class="ui-widget-content dwidth-left">
		<div id="accordion">
<% if(links.get("受理报价")) { %>
			<h3 style="padding-left:30px;">受理报价</h3>
		<div>
	<% if(links.get("acceptance")) { %>
		        ${beforePosition}
	<% } %>
	<% if(links.get("beforeline")) { %>
		        <a href="beforeLineLeader.do" title="受理报价线长界面">投线前维修对象一览</a><br>
	<% } %>
	<a href="materialFact.do">投线管理</a><br>
		    </div>
<% } %>

<% if(links.get("在线作业")) { %>
		    <h3 style="padding-left:30px;">在线作业</h3>
		    <div>
	<% if(links.get("decomposeline")) { %>
		        <a href="lineLeader.do?line_no=12" title="分解工程线长界面">分解工程</a><br>
	<% } %>
	<% if(links.get("nsline")) { %>
		        <a href="lineLeader.do?line_no=13" title="NS工程线长界面">NS 工程</a><br>
	<% } %>
	<% if(links.get("composeline")) { %>
		        <a href="lineLeader.do?line_no=14" title="总组工程线长界面">总组工程</a><br>
	<% } %>
	<% if(links.get("spline")) { %>
		        <a href="lineLeader.do?line_no=50" title="外科硬镜修理工程线长界面">外科硬镜修理工程</a><br>
	<% } %>
	<% if(links.get("febdecomline")) { %>
		        <a href="lineLeader.do?line_no=61" title="纤维镜分解工程线长界面">分解工程</a><br>
	<% } %>
	<% if(links.get("febcomline")) { %>
		        <a href="lineLeader.do?line_no=62" title="纤维镜总组工程线长界面">总组工程</a><br>
	<% } %>
	<% if(links.get("periline")) { %>
		        <a href="lineLeader.do?line_no=70" title="周边设备修理线长界面">周边设备修理工程</a><br>
	<% } %>
	<% if(links.get("lmline")) { %>
		        <a href="lineLeader.do?line_no=54" title="中小修工程线长界面">中小修修理工程</a><br>
	<% } %>
	<% if(links.get("inlinePosition")) { %>
		        ${inlinePosition}
	<% } %>
	<% if(links.get("deposeStorage")) { %>
		        <a href="compose_storage.do">总组签收库位</a><br>
	<% } %>
	<% if(links.get("support")) { %>
		        <a href="support.do">辅助工作</a>
	<% } %>
		    </div>
<% } %>

<% if(links.get("品保作业")) { %>
	<h3 style="padding-left:30px;">品保作业</h3>
	<div>
	<% if(links.get("qa_work")) { %>
		${qaPosition}
	<% } %>
	<% if(links.get("qa_view")) { %>
		        <a href="qaResult.do">出检结果</a><br>
		        <a href="service_repair_manage.do">保修期内返品管理日程表</a><br>
	<% } %>
	</div>
<% } %>

<% if(links.get("现品管理")) { %>
    <h3 style="padding-left:30px;">现品管理</h3>
    <div>
	<% if(links.get("wip")) { %>
		        <a href="wip.do" title="返还，WIP出库">WIP管理</a><br>
	<% } %>
		        <a href="materialPartial.do" title="现品零件BO管理">零件订购信息</a><br>
    </div>
<% } %>

<% if(links.get("计划管理")) { %>
    <h3 style="padding-left:30px;">计划管理</h3>
    <div>
		        <a href="schedule.do">SA (Schedule Area) 管理</a><br>
	<% if(links.get("schedule_processing")) { %>
		        <a href="scheduleProcessing.do">RA (Racing Area) 管理</a><br>
		        <a href="forSolutionArea.do">PA (Pending Area) 管理</a><br>
	<% } %>
				<a href="operation_standard_doc.do">作业基准书管理</a><br>
    </div>
<% } %>

<% if(links.get("info")) { %>
		<h3 style="padding-left:30px;">进度查询</h3>
		    <div>
		        <a href="material.do">维修对象</a><br>
		        <a href="positionProduction.do">工位工时</a><br>
		        <a href="operatorProduction.do">操作者工时</a><br>
		        <a href="alarmMessage.do">警报</a><br>
	<% if(links.get("nsline")) { %>
		        <a href="snouts.do">先端组件</a><br>
	<% } %>
		        <a href="materialPcs.do">工程检查票</a><br>
		    </div>
<% } %>

<% if(links.get("文档管理")) { %>
    <h3 style="padding-left:30px;">文档管理</h3>
	<div>
		        <a href="filing.do">维修作业归档</a><br>
		        <a href="operatorProduction.do?method=monthly">SORC 作业月报</a><br>
		        <a href="weekly_kpi_data.do">周报功能</a><br>
		        <a href="daily_work_sheet.do">工作记录表</a>
	</div>
<% } %>

<% if(links.get("viewer")) { %>
<h3 style="padding-left:30px;" class="ui-accordion-header ui-helper-reset ui-state-default ui-accordion-icons ui-corner-top" role="tab" id="ui-accordion-accordion-header-2" aria-controls="ui-accordion-accordion-panel-2" aria-selected="true" tabindex="0"><span></span>展示一览</h3>
<div class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active" style="display: block;" id="ui-accordion-accordion-panel-2" aria-labelledby="ui-accordion-accordion-header-2" role="tabpanel" aria-expanded="true" aria-hidden="false">
	<a target="_parent" href="show.do#globalProgress">全工程</a><br>
	<a target="_parent" href="show.do#acceptFact">受理报价展示</a><br>
	<a target="_parent" href="show.do#wipProgress">WIP库位</a><br>
	<a title="分解工程线长界面" target="_parent" href="show.do#lineSituationD1">分解</a>
	<a title="NS工程线长界面" target="_parent" href="show.do#lineSituationS1">NS</a>
	<a title="总组工程线长界面" target="_parent" href="show.do#lineSituationP1">总组</a>在线展示<br>
	<a title="" target="_parent" href="show.do#lineSituationPeripheral">周边维修</a>
	<a title="" target="_parent" href="show.do#lineSituationCell">单元拉</a>在线展示<br>
	<a title="" target="_parent" href="show.do#allPositions">全工位状况展示</a><br>
	<a title="" target="_parent" href="show.do#lineTimeSpace-line_id-12">分解</a>
	<a title="" target="_parent" href="show.do#lineTimeSpace-line_id-13">NS</a>
	<a title="" target="_parent" href="show.do#lineTimeSpace-px-A">总组A</a>
	<a title="" target="_parent" href="show.do#lineTimeSpace-px-B">总组B</a>进度管理板<br>
	<a title="" target="_parent" href="show.do#lineWorkDuration-line_id-12">分解</a>
	<a title="" target="_parent" href="show.do#lineWorkDuration-line_id-13">NS</a>
	<a title="" target="_parent" href="show.do#lineWorkDuration-line_id-14">总组</a>
	<a title="" target="_parent" href="show.do#lineWorkDuration">单元拉</a>人员工时图<br>
	<a title="" target="_parent" href="show.do#service_repair_manage">保内返品分析展示</a><br>
	<a title="" target="_parent" href="show.do#finalCheck">最终检查展示</a><br>
	<a title="" target="_parent" href="show.do#partialWarehouse">仓管人员工时图</a><br>
</div>
<% } %>

		    <h3 style="padding-left:30px;">资源功能</h3>
		    <div>
	<% if(links.get("admin")) { %>
		        <a href="adminmenu.do">系统信息管理</a><br>
	<% } %>
		        <a href="standard_work_time.do">标准工时参考</a><br>
		        <a href="pcsTemplate.do">工程检查票模板</a><br>
		        <a href="password.do">用户密码修改</a>
		    </div>
		</div>
	</div>
	<div id="datepicker" class="dwidth-left"></div>
	<div class="clear areaencloser"></div>
</div>



</body></html>