<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/highcharts.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script src="js/jquery.flipCounter.1.2.pack.js" type="text/javascript"></script>
<script type="text/javascript" src="js/scan/service_repair_manage.js"></script>
<title>146P保修期内返品+QIS品分析</title>
</head>

<body class="outer scan1024">

	  <!--左侧-->
		<div class="ui-widget-panel dwidth-full" style="align: center; margin: auto; padding: 16px;height:560px;" id="body-3">
	 		<!--柱状表格-->
	   		<div id="workarea" style="width:840px;float:left">   
	     		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full" style="width:820px;margin-left:1px;">
	      			<span class="areatitle">保修期内返品统计表</span>
	     		</div>
	     		<div class="ui-widget-content dwidth-full" style="height:514px;width:820px;margin-left:1px;">
	          	 	<div id="performance_container"></div>                    
	            </div>
	 		</div>
      <!--右侧-->
		   	 <div id="today_complete" style="float:left;">      
			    
			       <div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;">
			        <span class="areatitle" style="font-size:13px;margin:7px 8px;">保内返品分析等待件数</span>
			       </div>
			       <div id="wait_complete" style="width : 126px;height : 126px; background-color:white; border: 12px solid #92d050; -webkit-box-shadow: 0 -1px 2px #292929;">
			       </div>
			       <div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;">
			       	<span class="areatitle" style="font-size:13px;margin:7px 28px;">分析进行中件数</span>
			       </div>
			       <div id="analyse_complete" style="width : 126px;height : 126px; background-color:white; border: 12px solid #FFD200; -webkit-box-shadow: 0 -1px 2px #292929;">
			       </div> 
			       <div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width:148px;">
			        <span class="areatitle" style="font-size:13px;margin:7px 35px;">分析完成件数</span>
			       </div>
			       <div id="service_complete" style="width : 126px;height : 126px; background-color:white; border: 12px solid #0070c0; -webkit-box-shadow: 0 -1px 2px #292929;">
			       </div>     
		     </div>
		    <div class="clear"></div>
		</div>   

</body>
</html>