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
	<div class="ui-widget-content dwidth-left">
		<div id="accordion">
<% if(links.get("在线作业")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>在线作业</h3>
		<div class="ui-accordion-content ui-helper-reset ui-widget-content">
	<% if(links.get("manufactline")) { %>
		        <a href="lineLeader.do?line_no=12" title="${userdata.line_name}线长界面">${userdata.line_name}</a><br>
	<% } %>
	<% if(links.get("inlinePosition")) { %>
		        ${inlinePosition}
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
	<% } %>
	</div>
</div>
<% } %>

<% if(links.get("info")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>进度查询</h3>
	<div class="ui-accordion-content ui-helper-reset ui-widget-content">
		        <a href="material.do">产品</a><br>
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
		        <a href="filing.do">检查票归档</a><br>
		        <a href="operatorProduction.do?method=monthly">SORC 作业月报</a><br>
	</div>
</div>
<% } %>

<% if(links.get("viewer")) { %>
<div class="dwidth-left">
<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0"><span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>展示一览</h3>
	<div class="ui-accordion-content ui-helper-reset ui-widget-content">
	<a title="BX本体生产线" target="_parent" href="show.do#lineSituationBX">BX本体生产线</a><br>
	<a title="" target="_parent" href="show.do#allPositions-man">流水线状况展示</a><br>
	<a title="" target="_parent" href="show.do#lineWorkDuration-man">BX本体生产线人员工时图</a><br>
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
	</div>
</div>



</body></html>