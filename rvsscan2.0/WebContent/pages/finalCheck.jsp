<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
<meta name="apple-mobile-web-app-capable" content="yes" />
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">
<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/highcharts.js"></script>
<script type="text/javascript" src="js/jquery.flipCounter.1.2.pack.js"></script>
<script type="text/javascript" src="js/scan/final_check.js"></script>

<title>最终检查图表</title>
</head>
<body class="outer scan1024">
	
	    <div class="ui-widget-panel dwidth-full" style="align: center; margin: auto; padding: 16px;height:560px;" id="body-3">
	<!--柱状表格-->
			<div id="today_complete" style="float:left;" >						
							<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;">
								<span class="areatitle" style="font-size:13px;margin:7px 20px;">当前待检查件数</span>
							</div>
							<div id="today_quality" style="width : 126px;height : 126px; background-color:white; border: 12px solid #92d050; -webkit-box-shadow: 0 -1px 2px #292929;">
							</div>
							<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;margin-top: 182px;">
								<span class="areatitle" style="font-size:13px;margin:7px 20px;">当前待确认件数</span>
							</div>
							<div id="today_confirm_quality" style="width : 126px;height : 126px; background-color:white; border: 12px solid #48A068; -webkit-box-shadow: 0 -1px 2px #292929;">
							</div>
		   </div>
			<div id="workarea" style="width:690px;float:left">			
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full" style="width:820px;margin-left:1px;">
						<span class="areatitle">最终检查图表</span>
					</div>
					<div class="ui-widget-content dwidth-full" style="height:514px;width:100%;margin-left:1px;">
					      <div id="refix_1_container"></div>		           	      
                    </div>
	        </div>
    <!--右侧件数显示-->
			<div id="today_complete" style="float:left;" >						
							<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;">
								<span class="areatitle" style="font-size:13px;margin:7px 20px;">当日通过件数</span>
							</div>
							<div id="today_pass" style="width : 126px;height : 126px; background-color:white; border: 12px solid #0070c0; -webkit-box-shadow: 0 -1px 2px #292929;">
							</div>
							<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;margin-top: 182px;">
								<span class="areatitle" style="font-size:13px;margin:7px 20px;">当日不合格数</span>
							</div>
							<div id="today_unqualified" style="width : 126px;height : 126px; background-color:white; border: 12px solid #ff0000; -webkit-box-shadow: 0 -1px 2px #292929;">
							</div>						
		   </div>
	   <div class="clear"></div>
   </div>
</html>