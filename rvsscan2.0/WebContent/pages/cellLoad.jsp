<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
<meta name="apple-mobile-web-app-capable" content="yes" />
<title></title>

<style type="text/css">
	*{
		-webkit-user-select: none; /*禁止选择文本*/
				user-select: none; 
		-webkit-appearance: none; /*移除系统默认样式*/
				appearance: none;
		-webkit-touch-action: none; /*禁止触发默认的手势操作*/
				touch-action: none;
		-webkit-touch-callout: none; /*禁止系统默认菜单*/
				touch-callout: none;
		-webkit-text-size-adjust: 100%; /*文字大小100%缩放*/
				text-size-adjust: 100%;
	}
	body{
		overflow-y:hidden;
		font-family: -apple-system, BlinkMacSystemFont, "PingFang SC","Helvetica Neue",STHeiti,"Microsoft Yahei",Tahoma,Simsun,sans-serif;
	}
	.chart_page {
		position : absolute;
		/* transform:scale(0.97, 1); transform-origin: 10% top 0; */
	}
	.pt-page-current{
	}
	.pt-page-scaleDown {
		-webkit-animation: scaleDown .7s ease both;
		-moz-animation: scaleDown .7s ease both;
		animation: scaleDown .7s ease both;
	}
	
	.pt-page-moveFromBottom {
		-webkit-animation: moveFromBottom 1s ease both;
		-moz-animation: moveFromBottom 1s ease both;
		animation: moveFromBottom 1s ease both;
	}
	
	@-webkit-keyframes scaleDown {
		to { opacity: 0; -webkit-transform: scale(.8); }
	}
	@-moz-keyframes scaleDown {
		to { opacity: 0; -moz-transform: scale(.8); }
	}
	@keyframes scaleDown {
		to { opacity: 0; transform: scale(.8); }
	}
	
	@-webkit-keyframes moveFromBottom {
		from { -webkit-transform: translateY(100%); }
	}
	@-moz-keyframes moveFromBottom {
		from { -moz-transform: translateY(100%); }
	}
	@keyframes moveFromBottom {
		from { transform: translateY(100%); }
	}
</style>

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>

<script type="text/javascript">
//body--之外不可选择文字
$('*').disableSelection();

	var time_archer = (new Date()).getTime();

	var pages=["lineSituationCell","lineWorkDuration"];
	var titles=["单元拉展示","单元拉人员工时图"];
	$(function(){
		$(".chart_page").hide();
		
		$("#lineSituationCell").load("lineSituationCell.scan",function(responseText, textStatus, XMLHttpRequest) {
			$("#cell-body").css({"margin-top":"0"});
		});
		$("#lineWorkDuration").load("lineWorkDuration.scan",function(responseText, textStatus, XMLHttpRequest) {
			$(this).find(".width-full").addClass("dwidth-full").removeClass("width-full");
			$(this).find("#body-3").css({"width":"990px","box-sizing":"border-box","padding":"8px"});
			$(this).find("#axis_base").css({"width":"932px"});
		});

		refresh();
		setInterval(refresh,18000);
	});
	var irefresh = 0;

	var refresh=function(){
		if ((new Date()).getTime() - time_archer > 28800000) {
			return window.location.reload();
		}

		irefresh++;
		if(irefresh>=pages.length){
			irefresh=0;
		}
		//导入页面 
		// 页面向上滚动 
		$(".chart_page").removeClass("pt-page-moveFromBottom");
		$("#" + pages[irefresh]).show().addClass("pt-page-moveFromBottom").addClass("pt-page-current");
		// 变更模块名 
		$("#moduleName").text(titles[irefresh]);
		
		// 退出去 
		$(".pt-page-current").addClass("pt-page-scaleDown");
		
	}
	
	document.addEventListener('touchmove', function (e) {e.preventDefault();}, {capture: false,passive: false});
</script>

</head>
<body class="outer scan1024" id="roller">
	<div style="align: center; margin: auto; margin-top: 16px; ">
		<div id="basearea" class="dwidth-full" style="margin: auto;"></div>
		<script type="text/javascript">
			$("#basearea").load("widgets/header.jsp",
				function(responseText, textStatus, XMLHttpRequest) {
				$("#moduleName").text("单元拉展示");
			});
		</script>
	</div>
	<div class="dwidth-full">
		<div style="display: block;" id="lineSituationCell" class="chart_page pt-page-current pt-page-scaleDown "></div>
		<div style="display: block;" id="lineWorkDuration" class="chart_page pt-page-current pt-page-scaleDown pt-page-moveFromBottom"></div>
	</div>
</body>
</html>