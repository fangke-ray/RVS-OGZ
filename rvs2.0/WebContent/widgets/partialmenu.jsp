<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" import="java.util.Map" isELIgnored="false"%>
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<script>
$(function() {
	$("a").attr("target", "_parent");
	var accordion_idx = $("#accordion h3").index($("#accordion h3:contains('" + $("#linkto").val() + "')"));
	if (accordion_idx < 0) accordion_idx = 0;
	$("#accordion" ).accordion({active: accordion_idx});
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
			<h3 style="padding-left:30px;">零件基础数据管理</h3>
			<div>
				<a target="_parent" href="partialManage.do">零件一览表</a><br>
				<!-- <a target="_parent" href="partial_position.do">零件定位信息管理</a><br> -->
<% if(links.get("partial_admin")){ %>
				<!-- <a target="_parent" href="partial_waste_modify_history.do">零件废改增履历</a><br> -->
				<!-- <a target="_parent" href="premake_partial.do">预制零件设定</a><br> -->
<% } %>
		    </div>
		    <h3 style="padding-left:30px;">零件入出库</h3>
		    <div>
	<%if(links.get("fact")){ %>
		        <a target="_parent" href="fact_production_feature.do" title="现品作业信息">现品作业信息</a><br>
	<% } %>
		        <a target="_parent" href="partial_warehouse.do" title="零件入库单管理">零件入库单管理</a><br>
		        <a target="_parent" href="materialPartial.do">零件订购·签收管理</a>
		    </div>
		</div>
	</div>
	<div id="datepicker" class="dwidth-left"></div>
	<div class="clear areaencloser"></div>
</div>



</body></html>