<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>

<%
	String user_name = (String)request.getSession().getAttribute("user_name");
%>

<html>
<head>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
	function doAction(btn_flg) {
		if (btn_flg == "tc_storage") {
			window.location.href = "pda_turnover_case_storage.do?method=init";
		} else if (btn_flg == "tc_shipping") {
			window.location.href = "pda_turnover_case_shipping.do?method=init";
		} else if (btn_flg == "exit") {
			window.location.href = "login.do?method=pda";
		}
	};
	function chooseAction(btn_flg) {
		window.location.href = "appmenu.do?method=pdaMenu&flg=" + btn_flg;
	};
</script>
<%
	Boolean isFact = (Boolean) request.getAttribute("isFact");
	Boolean isRecept = (Boolean) request.getAttribute("isRecept");
%>
<title>菜单</title>
<link rel="stylesheet" type="text/css" href="css/pda/pda_custom.css">
</head>
 <body onload="history.forward();">
<%
	if (isRecept) {
%>
  <div class="width-full">
		<div class="header">
			<div style="margin-left:20px;margin-top:1px;color:#fff;">
				你好! <%=user_name %>
			</div>
			<div class="button" style="margin:30px auto;width:120px;" onclick="doAction('tc_storage')">通箱入库</div>
		</div>
		<div class="main">
		</div>
		<div class="bottom" style="position:relative;">
			<div class="button" style="position:absolute;margin:-20px auto 0 70px;width:120px;" onclick="doAction('tc_shipping')">通箱出库</div>
		</div>
	</div>
<%
	} else {
%>
  <div class="width-full">
		<div class="header">
			<div style="margin-left:20px;margin-top:1px;color:#fff;">
				你好! <%=user_name %> <input type="button" value="退出" onclick="doAction('exit')"/>
			</div>
		</div>
		<div class="main">
			<div>目前您在PDA界面没有可使用的功能。</div>
		</div>
		<div class="bottom" style="position:relative;">
		</div>
	</div>
<%
	}
%>
 </body>
</html>
