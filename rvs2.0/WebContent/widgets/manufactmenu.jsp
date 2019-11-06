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
<% if(links.get("在线作业")) { %>
		    <h3 style="padding-left:30px;">在线作业</h3>
		    <div>
	<% if(links.get("manufactline")) { %>
		        <a href="lineLeader.do?line_no=12" title="${userdata.line_name}线长界面">${userdata.line_name}</a><br>
	<% } %>
	<% if(links.get("inlinePosition")) { %>
		        ${inlinePosition}
	<% } %>
	<% if(links.get("manufactline")) { %>
		        <a href="scheduleProcessing.do">${userdata.line_name}流水线在线管理</a><br>
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
	<% } %>
	</div>
<% } %>

<% if(links.get("info")) { %>
		<h3 style="padding-left:30px;">进度查询</h3>
		    <div>
		        <a href="material.do">产品</a><br>
		        <a href="positionProduction.do">工位工时</a><br>
		        <a href="operatorProduction.do">操作者工时</a><br>
		        <a href="alarmMessage.do">警报</a><br>
		        <a href="materialPcs.do">工程检查票</a><br>
		    </div>
<% } %>

<% if(links.get("文档管理")) { %>
    <h3 style="padding-left:30px;">文档管理</h3>
	<div>
		        <a href="filing.do">检查票归档</a><br>
		        <a href="operatorProduction.do?method=monthly">SORC 作业月报</a><br>
	</div>
<% } %>

<% if(links.get("viewer")) { %>
<h3 style="padding-left:30px;" class="ui-accordion-header ui-helper-reset ui-state-default ui-accordion-icons ui-corner-top" role="tab" id="ui-accordion-accordion-header-2" aria-controls="ui-accordion-accordion-panel-2" aria-selected="true" tabindex="0"><span></span>展示一览</h3>
<div class="ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active" style="display: block;" id="ui-accordion-accordion-panel-2" aria-labelledby="ui-accordion-accordion-header-2" role="tabpanel" aria-expanded="true" aria-hidden="false">
	<a title="BX本体生产线" target="_parent" href="show.do#lineSituationBX">BX本体生产线</a><br>
	<a title="" target="_parent" href="show.do#allPositions-display-man">流水线状况展示</a><br>
	<a title="" target="_parent" href="show.do#lineTimeSpace-line_id-101">BX本体生产线人员工时图</a><br>
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