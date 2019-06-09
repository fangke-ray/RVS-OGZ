<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache"> <meta http-equiv="Cache-Control" content="no-cache">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
<meta name="apple-mobile-web-app-capable" content="yes" />
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/donuts.css">
<title>${section_name} ${line_name}</title>

<style type="text/css">
body{
	font-family: -apple-system, BlinkMacSystemFont, "PingFang SC","Helvetica Neue",STHeiti,"Microsoft Yahei",Tahoma,Simsun,sans-serif;
}
#num_area{
	transform:scale(.75);
	transform-origin:0 0;
}

.cell{
	margin: 10px 0;
}
.cell .title{
	float:left;
	font-size:26px;
	width:38px;
	background-color: #fff;
	writing-mode:vertical-lr;
	height: 182px;
	text-align: center;
	border : 1.5px solid #060b51;
}
.cell .ui-widget-header{
	width:148px;
}
.cell .areatitle{
	font-size: 20px;
}
.cell .plan-num{
	width:126px;
	height:126px;
	background-color:white;
	border:12px solid #92D050;
	box-shadow:0 -1px 2px #292929;
}
.cell .output-num{
	width : 126px;
	height : 126px;
	background-color:white;
	border: 12px solid #0080C0;
	box-shadow: 0 -1px 2px #292929;
}
.cell .rate{
	font-size: 350%;
	font-family: Georgia;
	background: rgba(0, 0, 128, 0.4);
	text-align: center;
	width: 3em;
	border: 4px solid rgb(255, 255, 255);
	left: 10px;
	border-radius: 14px;
	transform:translateY(20px);
}

.donut-big{
	margin-top: 45px;
}
</style>
</head>
<body class="outer scan1024">
<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/highcharts.js"></script>
<script type="text/javascript" src="js/jquery.flipCounter.1.2.pack.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/donuts.js"></script>
<script type="text/javascript" src="js/scan/line_situation_cell.js"></script>

	<div id="cell-body" class="dwidth-full" style="margin-top:16px;max-height:700px;">
		<div id="basearea"></div>
		<script type="text/javascript">
			$("#basearea").load("widgets/header.jsp",
				function(responseText, textStatus, XMLHttpRequest) {
				$("#moduleName").text("工程展示");
			});
		</script>
		
		<div class="ui-widget-panel dwidth-full" style="padding-top:8px;overflow: hidden;">
			<div class="ui-widget-header dwidth-full" style="align:center;padding:6px 0;text-align:center;">
				<span>${section_name} ${line_name}</span>
			</div>
			<input type="hidden" id="hide_section_id" value="${section_id }">
			<div class="dwidth-full">
				<div class="dwidth-half" style="float:left;margin-left:14px;font-weight: bolder;width:50%;height:600px;overflow: hidden;">
					<div id="num_area">
						<div class="cell" for="00000000054">
							<div class="title ui-state-default">中小修</div>
							<div style="float:left">
								<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
									<span class="areatitle">今日计划台数</span>
								</div>
								<div class="plan-num" id="plan_middle_light"></div>
							</div>
							<div style="float:left;margin-left:8px;">
								<div class="donut donut-big">
								    <div class="donut-arrow" data-percentage="0"></div>
								</div>
								<div class="rate">0%</div>
							</div>
							<div style="float:left;margin-left:8px;">
								<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
									<span class="areatitle">今日产出台数</span>
								</div>
								<div class="output-num" id="output_middle_light"></div>
							</div>
							<div class="clear"></div>
						</div>
						<div class="cell" for="00000000050">
							<div class="title ui-state-default">外科镜维修</div>
							<div style="float:left">
								<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
									<span class="areatitle">今日计划台数</span>
								</div>
								<div class="plan-num" id="plan_surgical"></div>
							</div>
							<div style="float:left;margin-left:8px;">
								<div class="donut donut-big">
								    <div class="donut-arrow" data-percentage="0"></div>
								</div>
								<div class="rate">0%</div>
							</div>
							<div style="float:left;margin-left:8px;">
								<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
									<span class="areatitle">今日产出台数</span>
								</div>
								<div class="output-num" id="output_surgical"></div>
							</div>
							<div class="clear"></div>
						</div>
						<div class="cell" for="00000000060">
							<div class="title ui-state-default">纤维镜分解</div>
							<div style="float:left">
								<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
									<span class="areatitle">今日计划台数</span>
								</div>
								<div class="plan-num" id="plan_fibrescope_dec"></div>
							</div>
							<div style="float:left;margin-left:8px;">
								<div class="donut donut-big">
								    <div class="donut-arrow" data-percentage="0"></div>
								</div>
								<div class="rate">0%</div>
							</div>
							<div style="float:left;margin-left:8px;">
								<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
									<span class="areatitle">今日产出台数</span>
								</div>
								<div class="output-num" id="output_fibrescope_dec"></div>
							</div>
							<div class="clear"></div>
						</div>
						<div class="cell" for="00000000061">
							<div class="title ui-state-default">纤维镜总组</div>
							<div style="float:left">
								<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
									<span class="areatitle">今日计划台数</span>
								</div>
								<div class="plan-num" id="plan_fibrescope_com"></div>
							</div>
							<div style="float:left;margin-left:8px;">
								<div class="donut donut-big">
								    <div class="donut-arrow" data-percentage="0"></div>
								</div>
								<div class="rate">0%</div>
							</div>
							<div style="float:left;margin-left:8px;">
								<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
									<span class="areatitle">今日产出台数</span>
								</div>
								<div class="output-num" id="output_fibrescope_com"></div>
							</div>
							<div class="clear"></div>
						</div>
					</div>
				</div>
				<div style="float:left;margin-left:-120px;margin-top:8px;width:59.5%;overflow: hidden;">
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
						<span class="areatitle">${section_name} ${line_name}当前仕挂分布</span>
					</div>
					<div class="ui-widget-content">
						<div id="processing_container"></div>
					</div>
				</div>
				<div class="clear"/></div>
			</div>
		</div>
	</div>
</body>
</html>