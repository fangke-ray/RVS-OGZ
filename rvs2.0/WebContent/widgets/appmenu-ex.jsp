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

</script>
<body>
<% Map<String, Boolean> links = (Map<String, Boolean>) request.getAttribute("menuLinks"); %>

<% if(links.get("受理报价")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>受理报价</h3>
		<div class="ui-accordion-content ui-helper-reset ui-widget-content">
	<% if(links.get("acceptance")) { %>
		        ${beforePosition}
	<% } %>
	<% if(links.get("beforeline")) { %>
		        <a href="beforeLineLeader.do" title="受理报价线长界面">投线前维修对象一览</a><br>
	<% } %>
	<a href="materialFact.do">投线管理</a><br>
		</div>
</div>
<% } %>

<% if(links.get("在线作业")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>在线作业</h3>
		<div class="ui-accordion-content ui-helper-reset ui-widget-content">
	<% if(links.get("decomposeline")) { %>
		        <a href="lineLeader.do?line_no=12" title="分解工程线长界面">分解工程</a><br>
	<% } %>
	<% if(links.get("nsline")) { %>
		        <a href="lineLeader.do?line_no=13" title="NS工程线长界面">NS 工程</a><br>
	<% } %>
	<% if(links.get("composeline")) { %>
		        <a href="lineLeader.do?line_no=14" title="总组工程线长界面">总组工程</a><br>
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
</div>
<% } %>

<% if(links.get("品保作业")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>品保作业</h3>
		<div class="ui-accordion-content ui-helper-reset ui-widget-content">
	<% if(links.get("qa_work")) { %>
		${qaPosition}
	<% } %>
	<% if(links.get("qa_view")) { %>
		        <a href="qaResult.do">出检结果</a><br>
		        <a href="service_repair_manage.do">保修期内返品管理日程表</a><br>
	<% } %>
	</div>
</div>
<% } %>

<% if(links.get("现品管理")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>现品管理</h3>
		<div class="ui-accordion-content ui-helper-reset ui-widget-content">
	<% if(links.get("wip")) { %>
		        <a href="wip.do" title="返还，WIP出库">WIP管理</a><br>
	<% } %>
		        <a href="materialPartial.do" title="现品零件BO管理">零件订购信息</a><br>
    </div>
</div>
<% } %>

<% if(links.get("计划管理")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>计划管理</h3>
		<div class="ui-accordion-content ui-helper-reset ui-widget-content">
		        <a href="schedule.do">SA (Schedule Area) 管理</a><br>
	<% if(links.get("schedule_processing")) { %>
		        <a href="scheduleProcessing.do">RA (Racing Area) 管理</a><br>
		        <a href="forSolutionArea.do">PA (Pending Area) 管理</a><br>
	<% } %>
				<a href="operation_standard_doc.do">作业基准书管理</a><br>
    </div>
</div>
<% } %>

<% if(links.get("info")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>进度查询</h3>
		<div class="ui-accordion-content ui-helper-reset ui-widget-content">
		        <a href="material.do">维修对象</a><br>
		        <a href="positionProduction.do">工位工时</a><br>
		        <a href="operatorProduction.do">操作者工时</a><br>
		        <a href="alarmMessage.do">警报</a><br>
	<% if(links.get("nsline")) { %>
		        <a href="snouts.do">先端组件</a><br>
	<% } %>
		        <a href="materialPcs.do">工程检查票</a><br>
		    </div>
</div>
<% } %>

<% if(links.get("文档管理")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>文档管理</h3>
		<div class="ui-accordion-content ui-helper-reset ui-widget-content">
		        <a href="filing.do">维修作业归档</a><br>
		        <a href="operatorProduction.do?method=monthly">SORC 作业月报</a><br>
		        <a href="weekly_kpi_data.do">周报功能</a><br>
		        <a href="daily_work_sheet.do">工作记录表</a>
	</div>
</div>
<% } %>

<% if(links.get("viewer")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>展示一览</h3>
	<div class="ui-accordion-content ui-helper-reset ui-widget-content">
        <a target="_parent" href="show.do#globalProgress">全工程</a><br>
        <a target="_parent" href="show.do#acceptFact">受理报价展示</a><br>
        <a target="_parent" href="show.do#wipProgress">WIP库位</a><br>
        <a title="分解工程线长界面" target="_parent" href="show.do#lineSituationD1">分解</a>
        <a title="NS工程线长界面" target="_parent" href="show.do#lineSituationS1">NS</a>
        <a title="总组工程线长界面" target="_parent" href="show.do#lineSituationP1">总组</a>在线展示<br>
        <a title="" target="_parent" href="show.do#lineSituationPeripheral">周边维修在线展示</a><br>
        <a title="" target="_parent" href="show.do#allPositions">全工位状况展示</a><br>
        <a title="" target="_parent" href="show.do#lineTimeSpace-line_id-12">分解</a>
        <a title="" target="_parent" href="show.do#lineTimeSpace-line_id-13">NS</a>
        <a title="" target="_parent" href="show.do#lineTimeSpace">总组</a>进度管理板<br>
        <a title="" target="_parent" href="show.do#lineWorkDuration-line_id-12">分解</a>
        <a title="" target="_parent" href="show.do#lineWorkDuration-line_id-13">NS</a>
        <a title="" target="_parent" href="show.do#lineWorkDuration">总组</a>人员工时图<br>
        <a title="" target="_parent" href="show.do#service_repair_manage">保内返品分析展示</a><br>
        <a title="" target="_parent" href="show.do#finalCheck">最终检查展示</a><br>
        <a title="" target="_parent" href="show.do#partialWarehouse">仓管人员工时图</a><br>
	</div>
</div>
<% } %>

<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>资源功能</h3>
		<div class="ui-accordion-content ui-helper-reset ui-widget-content">
	<% if(links.get("admin")) { %>
		        <a href="adminmenu.do">系统信息管理</a><br>
	<% } %>
		        <a href="standard_work_time.do">标准工时参考</a><br>
		        <a href="pcsTemplate.do">工程检查票模板</a><br>
		        <a href="password.do">用户密码修改</a>
		    </div>
		</div>
</div>

</body></html>