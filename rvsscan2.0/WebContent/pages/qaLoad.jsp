<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8" isELIgnored="false" %>
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
<title></title>

<style type="text/css">
	body{overflow-y:hidden;}
	.chart_page {
		position : absolute;
		/* transform:scale(0.96, 1); transform-origin: 20% top 0; */
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

#roller .areaencloser.dwidth-full{
	text-align:center;
}

#roller .areaencloser.dwidth-full span{
	float:none;
	font-size: 22px;
}

</style>

<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">


<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/highcharts.js"></script>
<script type="text/javascript" src="js/jquery.flipCounter.1.2.pack.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>

<script type="text/javascript" src="js/scan/service_repair_manage.js"></script>
<script type="text/javascript" src="js/scan/final_check.js"></script>

<script type="text/javascript">
//body--之外不可选择文字
$('*').disableSelection();

	var time_archer = (new Date()).getTime();

	var pages=["service_repair_manageIn","finalCheckIn"];
	var titles=["保修期内返品统计表","最终检查图表"];
	$(function(){
		$(".chart_page").hide();

		setInterval(refresh,18000);
	});
	var irefresh=0;

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
	var loadOver = function() {
	}

</script>
</head>
<body class="outer scan1024" id="roller">
	<div style="align: center; margin: auto; margin-top: 16px; ">
		<div id="basearea" class="dwidth-full" style="margin: auto;"></div>
			<script type="text/javascript">
				$("#basearea").load("widgets/header.jsp",
					function(responseText, textStatus, XMLHttpRequest) {
					$("#moduleName").text("保修期内返品统计表");
				});
			</script>
		</div>
		<div class="dwidth-full" style="margin: auto;">
			<div style="display: block;" id="service_repair_manageIn" class="chart_page pt-page-current pt-page-scaleDown">
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
			</div>	


			<div style="display: block;" id="finalCheckIn" class="chart_page pt-page-current pt-page-scaleDown pt-page-moveFromBottom">
	    <div class="ui-widget-panel dwidth-full" style="align: center; margin: auto; padding: 16px;height:560px;" id="body-3">
	<!--柱状表格-->
			<div id="workarea" style="width:840px;float:left">			
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full" style="width:820px;margin-left:1px;">
						<span class="areatitle">最终检查图表</span>
					</div>
					<div class="ui-widget-content dwidth-full" style="height:514px;width:820px;margin-left:1px;">
					      <div id="refix_1_container"></div>		           	      
                    </div>
	        </div>
    <!--右侧件数显示-->
			<div id="today_complete" style="float:left;" >						
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;">
								<span class="areatitle" style="font-size:13px;margin:7px 20px;">当前待品保件数</span>
					</div>
							<div id="today_quality" style="width : 126px;height : 126px; background-color:white; border: 12px solid #92d050; -webkit-box-shadow: 0 -1px 2px #292929;">
					</div>
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;">
						<span class="areatitle" style="font-size:13px;margin:7px 20px;">当日通过件数</span>
					</div>
							<div id="today_pass" style="width : 126px;height : 126px; background-color:white; border: 12px solid #0070c0; -webkit-box-shadow: 0 -1px 2px #292929;">
					</div>
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser" style="width : 148px;">
						<span class="areatitle" style="font-size:13px;margin:7px 20px;">当日不合格数</span>
					</div>
					<div id="today_unqualified" style="width : 126px;height : 126px; background-color:white; border: 12px solid #ff0000; -webkit-box-shadow: 0 -1px 2px #292929;">
					</div>						
		   </div>
	   <div class="clear"></div>
   </div>
			</div>	

		</div>
	</div>

</body></html>