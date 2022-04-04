<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no," name="viewport">
<meta name="apple-mobile-web-app-capable" content="yes" />
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">

<style type="text/css">
	.chart_page {
		position : absolute;
	}
	.pt-page-current{
	}
	.pt-page-rotateSlideOutL {
		-webkit-animation: rotateSlideOutL .7s ease both;
		-moz-animation: rotateSlideOutL .7s ease both;
		animation: rotateSlideOutL .7s ease both;
	}
	
	.pt-page-rotateSlideInL {
		-webkit-animation: rotateSlideInL 1s ease both;
		-moz-animation: rotateSlideInL 1s ease both;
		animation: rotateSlideInL 1s ease both;
	}

	.pt-page-rotateSlideOutR {
		-webkit-animation: rotateSlideOutR .7s ease both;
		-moz-animation: rotateSlideOutR .7s ease both;
		animation: rotateSlideOutR .7s ease both;
	}
	
	.pt-page-rotateSlideInR {
		-webkit-animation: rotateSlideInR 1s ease both;
		-moz-animation: rotateSlideInR 1s ease both;
		animation: rotateSlideInR 1s ease both;
	}
	.iframe-slider {
		position : absolute;
		width : 100%;
		height : 100%;
		z-index : 5;
		top : 0;
		background-color : transparent;
	}
/* slide */

@-webkit-keyframes rotateSlideOutL {
	25% { opacity: .5; -webkit-transform: translateZ(-500px) scale(.8);}
	75% { opacity: .5; -webkit-transform: translateZ(-500px) translateX(-200%) scale(.6); }
	100% { opacity: .5; -webkit-transform: translateZ(-500px) translateX(-200%) scale(.6); }
}
@-moz-keyframes rotateSlideOutL {
	25% { opacity: .5; -moz-transform: translateZ(-500px) scale(.8);}
	75% { opacity: .5; -moz-transform: translateZ(-500px) translateX(-200%) scale(.6); }
	100% { opacity: .5; -moz-transform: translateZ(-500px) translateX(-200%) scale(.6); }
}
@keyframes rotateSlideOutL {
	25% { opacity: .5; transform: translateZ(-500px) scale(.8);}
	75% { opacity: .5; transform: translateZ(-500px) translateX(-200%) scale(.6); }
	100% { opacity: .5; transform: translateZ(-500px) translateX(-200%) scale(.6); }
}

@-webkit-keyframes rotateSlideInL {
	0%, 25% { opacity: .5; -webkit-transform: translateZ(-500px) translateX(200%) scale(.6); }
	75% { opacity: .5; -webkit-transform: translateZ(-500px);  scale(.8)}
	100% { opacity: 1; -webkit-transform: translateZ(0) translateX(0); }
}
@-moz-keyframes rotateSlideInL {
	0%, 25% { opacity: .5; -moz-transform: translateZ(-500px) translateX(200%) scale(.6); }
	75% { opacity: .5; -moz-transform: translateZ(-500px) scale(.8); }
	100% { opacity: 1; -moz-transform: translateZ(0) translateX(0); }
}
@keyframes rotateSlideInL {
	0%, 25% { opacity: .5; transform: translateZ(-500px) translateX(200%) scale(.6); }
	75% { opacity: .5; transform: translateZ(-500px) scale(.8); }
	100% { opacity: 1; transform: translateZ(0) translateX(0); }
}

@-webkit-keyframes rotateSlideOutR {
	25% { opacity: .5; -webkit-transform: translateZ(500px) scale(.8);}
	75% { opacity: .5; -webkit-transform: translateZ(500px) translateX(200%) scale(.6); }
	100% { opacity: .5; -webkit-transform: translateZ(500px) translateX(200%) scale(.6); }
}
@-moz-keyframes rotateSlideOutR {
	25% { opacity: .5; -moz-transform: translateZ(500px) scale(.8);}
	75% { opacity: .5; -moz-transform: translateZ(500px) translateX(200%) scale(.6); }
	100% { opacity: .5; -moz-transform: translateZ(500px) translateX(200%) scale(.6); }
}
@keyframes rotateSlideOutR {
	25% { opacity: .5; transform: translateZ(500px) scale(.8);}
	75% { opacity: .5; transform: translateZ(500px) translateX(200%) scale(.6); }
	100% { opacity: .5; transform: translateZ(500px) translateX(200%) scale(.6); }
}

@-webkit-keyframes rotateSlideInR {
	0%, 25% { opacity: .5; -webkit-transform: translateZ(500px) translateX(-200%) scale(.6); }
	75% { opacity: .5; -webkit-transform: translateZ(500px);  scale(.8)}
	100% { opacity: 1; -webkit-transform: translateZ(0) translateX(0); }
}
@-moz-keyframes rotateSlideInR {
	0%, 25% { opacity: .5; -moz-transform: translateZ(500px) translateX(-200%) scale(.6); }
	75% { opacity: .5; -moz-transform: translateZ(500px) scale(.8); }
	100% { opacity: 1; -moz-transform: translateZ(0) translateX(0); }
}
@keyframes rotateSlideInR {
	0%, 25% { opacity: .5; transform: translateZ(500px) translateX(-200%) scale(.6); }
	75% { opacity: .5; transform: translateZ(500px) scale(.8); }
	100% { opacity: 1; transform: translateZ(0) translateX(0); }
}
</style>

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/highcharts.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/jquery.hammer.js"></script>

<script type="text/javascript">
//body--之外不可选择文字
$('*').disableSelection();
$(function(){
	var time_archer = (new Date()).getTime();

	var pages = ["allPositions","lineTimeSpaceA","lineTimeSpaceB","lineTimeSpaceC"];
	var moduleNames = ["全工程展示","290 拉进度管理板","260 拉进度管理板","细镜拉进度管理板"]
	var irefresh = 0;
	$(".chart_page:gt(0)").hide();
	
	$("#allPositions").load("allPositions.scan",function(responseText, textStatus, XMLHttpRequest) {
		var chart_page_width = $("#allPositions").width();
		var chart_page_height = $("#allPositions").height();
		$(".chart_page").each(function(idx, ele){
			if (idx > 0) {
				$(ele).children("iframe").width(chart_page_width);
				$(ele).children("iframe").height(chart_page_height);
			}
		});
	});
//	$("#lineTimeSpaceA").load("lineTimeSpace.scan?line_id=201",function(responseText, textStatus, XMLHttpRequest) {
//	});
//	$("#lineTimeSpaceB").load("lineTimeSpace.scan?line_id=202",function(responseText, textStatus, XMLHttpRequest) {
//	});
//	$("#lineTimeSpaceC").load("lineTimeSpace.scan?line_id=203",function(responseText, textStatus, XMLHttpRequest) {
//	});
	
	$("#roller").hammer().on("swipeleft", goLeft).on("swiperight", goRight);
	
	function goLeft(){
		if ((new Date()).getTime() - time_archer > 28800000) {
			return window.location.reload();
		}

		irefresh++;
		if(irefresh >= pages.length){
			irefresh = 0;
		}
		//导入页面 
		// 页面滚动 
		$(".chart_page").removeClass("pt-page-rotateSlideInL").removeClass("pt-page-rotateSlideInR");
		// 退出去 
		$(".pt-page-current").addClass("pt-page-rotateSlideOutL");

		$("#" + pages[irefresh]).show().removeClass("pt-page-rotateSlideOutL").removeClass("pt-page-rotateSlideOutR")
			.addClass("pt-page-rotateSlideInL").addClass("pt-page-current");

		setTimeout(function(){
			$(".chart_page").not(".pt-page-current").hide();
		} , 700);
		$("body.outer").css("overflow", "hidden");
		$("#moduleName").text("");
	};
	
	function goRight(){
		if ((new Date()).getTime() - time_archer > 28800000) {
			return window.location.reload();
		}

		irefresh--;
		if(irefresh<0){
			irefresh=pages.length-1;
		}
		//导入页面 
		// 页面滚动 
		$(".chart_page").removeClass("pt-page-rotateSlideInL").removeClass("pt-page-rotateSlideInR");
		// 退出去 
		$(".pt-page-current").addClass("pt-page-rotateSlideOutR");
		$("#" + pages[irefresh]).show().removeClass("pt-page-rotateSlideOutL").removeClass("pt-page-rotateSlideOutR")
			.addClass("pt-page-rotateSlideInR").addClass("pt-page-current");
		
		setTimeout(function(){
			$(".chart_page").not(".pt-page-current").hide();
		} , 700);
		$("body.outer").css("overflow", "hidden");
		$("#moduleName").text(moduleNames[irefresh]);
 
	};
});
</script>

<body class="outer scan1024" style="overflow:hidden;">
	<div class="width-full" style="margin:0 auto;margin-top:16px;">
		<div id="basearea" class="dwidth-full" style="position: absolute; bottom: 0px; right: 2em;"></div>
		<script type="text/javascript">
			$("#basearea").load("widgets/header.jsp",
				function(responseText, textStatus, XMLHttpRequest) {
				$("#basearea img").remove();
				$("#moduleName").text("");
			});
		</script>
		
		<div id="roller" class="width-full" style="overflow:hidden;min-height:500px;">
			<div id="allPositions" class="chart_page pt-page-current" style="display: block;"></div>
			<div id="lineTimeSpaceA" class="chart_page" style="display: none;"><iframe src="lineTimeSpace.scan?line_id=201" frameborder=0 scrolling=no></iframe><div class="iframe-slider"></div></div>
			<div id="lineTimeSpaceB" class="chart_page" style="display: none;"><iframe src="lineTimeSpace.scan?line_id=202" frameborder=0 scrolling=no></iframe><div class="iframe-slider"></div></div>
			<div id="lineTimeSpaceC" class="chart_page" style="display: none;"><iframe src="lineTimeSpace.scan?line_id=203" frameborder=0 scrolling=no></iframe><div class="iframe-slider"></div></div>
		</div>
	</div>
</body>
</html>
