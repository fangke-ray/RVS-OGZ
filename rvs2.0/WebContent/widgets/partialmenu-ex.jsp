<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" import="java.util.Map" isELIgnored="false"%>
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<body>
<% Map<String, Boolean> links = (Map<String, Boolean>) request.getAttribute("menuLinks"); %>
<div class="dwidth-left">
	<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0">
			<span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>零件基础数据管理</h3>
			<div class="ui-accordion-content ui-helper-reset ui-widget-content">
				<a target="_parent" href="partialManage.do">零件一览表</a><br>
				<!-- <a target="_parent" href="partial_position.do">零件定位信息管理</a><br> -->
<% if(links.get("partial_admin")){ %>
				<!-- <a target="_parent" href="partial_waste_modify_history.do">零件废改增履历</a> -->
<% } %>
		    </div>
</div>
<div class="dwidth-left">
	<h3 class="ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-accordion-icons" tabindex="0">
			<span class="ui-accordion-header-icon ui-icon ui-icon-triangle-1-s"></span>零件入出库</h3>
		    <div class="ui-accordion-content ui-helper-reset ui-widget-content">
	
	<% if(links.get("fact")){ %>
		        <a target="_parent" href="fact_production_feature.do" title="现品作业">现品作业</a><br>
	<% }%>
	 			<a target="_parent" href="partial_warehouse.do" title="零件入库单管理">零件入库单管理</a><br>
		        <a target="_parent" href="materialPartial.do">零件订购·签收管理</a>
		    </div>
</div>
</body>
</html>