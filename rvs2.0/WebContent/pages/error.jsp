<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<%@ page import="java.util.List" session="false" %>
<%@ page import="org.apache.catalina.util.RequestUtil" session="false"
         trimDirectiveWhitespaces="true" %>
<%@ page import="framework.huiqing.bean.message.MsgInfo" session="false" %>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<base href="<%=basePath%>">
		<link rel="stylesheet" type="text/css" href="css/custom.css">
		<style>
#error_content input[type=button]{
	background-color:#FFB300;
	font-size:16px;
	border: 1px solid darkblue;
	cursor:pointer;
}
		</style>
	</head>
	<body class="outer error_body">
		<div id="error_content" style="width:800px;margin: 0 auto 0.4em;min-height:480px;font-size: 16px;">
<%


String errorUrl = (String) request.getAttribute("request_occur");
if (errorUrl == null) {
	errorUrl = (String) request.getAttribute("javax.servlet.error.request_uri");
}
if (errorUrl == null) {
	errorUrl = (String) request.getAttribute("javax.servlet.forward.request_uri");
}
if (errorUrl == null) {
	errorUrl = (String) request.getAttribute("javax.servlet.include.request_uri");
}
if (errorUrl == null) {
	errorUrl = request.getRequestURL().toString();
}
if (errorUrl != null) {
	String[] subPart = errorUrl.split("/");
	if (subPart.length > 1) {
		errorUrl = subPart[subPart.length - 1].replaceAll(".do", "");
	}
}
String strMethod = (String) request.getAttribute("request_method");
if (strMethod == null) {
	strMethod = request.getParameter("method");
}
Throwable exception = (Throwable) request.getAttribute("exception");
if (exception == null ) {
	exception = (Throwable) request.getAttribute( "java.lang.Exception" );
}

List<MsgInfo> actionErrors = (List) request.getAttribute("errors");
String sOccurTime = (String) request.getAttribute("occur_time");
%>
			<pre style="background-color:#FFFFFF;padding:.5em 1em;">
RVS 服务端发现了错误。<br>本次操作不可继续。<br>您可以选择将下面显示的错误相关信息发送给系统管理员或开发人员。<br>
按下列任意按钮：
<input type="button" value="返回上页" onclick="window.history.back()"> <input type="button" value="返回主页" onclick="window.location.href = 'panel.do'"> <input type="button" value="重新登录" onclick="window.location.href = 'login.do'">
继续您的使用。</pre>
			<div style="background-color:#FFB300;padding :0 1em;">
			<h2>发生时间</h2>
			</div>
			<pre style="background-color:#FFFFFF;padding:.5em 1em;">
<%=sOccurTime%>
			</pre>
			<div style="background-color:#FFB300;padding :0 1em;">
			<h2>事件来源</h2>
			</div>
			<pre style="background-color:#FFFFFF;padding:.5em 1em;">
<%=errorUrl%>?<%=strMethod%>
			</pre>
			<div style="background-color:#FFB300;padding :0 1em;">
			<h2>错误信息</h2>
			</div>

			<div style="background-color:#FFFFFF;padding:.5em 1em;">
<%
if (exception != null ) {
	String exMessage = exception.getMessage();
	if (exMessage == null || "".equals(exMessage)) {
		exMessage = "(无异常相关的情报，可能为空指针)";
		out.write(exMessage);
		out.write("\r\n");
	}
} else if (actionErrors.size() > 0) {
	for (MsgInfo msgInfo : actionErrors) {
		if (msgInfo.getErrmsg() == null) {
			out.write("(无异常相关的情报，可能为空指针)");
		} else {
			out.write(msgInfo.getErrmsg());
		}
		out.write("\r\n");
	}
}
%>
			</div>
		</div>
	</body>
</html>
<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript">
	var bodyClass = $("#error_content").closest("body")[0].className;

	if (!bodyClass || bodyClass.indexOf("error_body") < 0) {
		window.location.href = "break.do";
	}
</script>