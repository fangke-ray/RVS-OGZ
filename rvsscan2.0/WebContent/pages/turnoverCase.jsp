<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">
<link rel="stylesheet" type="text/css" href="css/ui.jqgrid.css">

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>

<script type="text/javascript" src="js/scan/turnover_case.js"></script>

<title>通箱库存展示</title>
</head>
<body class="outer scan1024" style="overflow:hidden;">
	<div class="width-full" style="align: center; margin : auto; margin-top: 24px;">
		<div id="basearea" class="dwidth-full" style="margin: auto; margin-bottom: 12px;"></div>
		<script type="text/javascript">
			$("#basearea").load("widgets/header.jsp",
				function(responseText, textStatus, XMLHttpRequest) {
				$("#moduleName").text("通箱库存展示");
			});
		</script>

		<div id="wiparea" style="width:100%;margin-left:4px;margin-right:24px;overflow:hidden;transform-origin: center top 0;">
			<!--div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
				<span class="areatitle">WIP区域一览</span>
			</div-->

			<div class="ui-widget-content" style="position:relative;height:740px;">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">通箱库位区域一览</span>
	</div>
				<div style="float: left; width: 112%; margin-top: 297px; height: 390px; transform-origin: 0 0; transform: scale(0.9); overflow-y: hidden;" id="storages">
					${storageHtml}
				</div>
				<script type="text/javascript">
//					$("#storages").load("widgets/turnover_case_storage_map.jsp",
//						function(responseText, textStatus, XMLHttpRequest) {
						setTimeout(function(){stgHeight = $("#storages > div:eq(0)").height()
								- $("#storages").height();}, 10);
//					});
				</script>
				<style>
				#listarea tr td:nth-child(1) {width:60px;}
				#listarea tr td:nth-child(2) {width:6px;}
				#listarea tr td:nth-child(3) {width:90px;}
				#listarea tr td:nth-child(4) {width:56px;}
				</style>
<style>
	.shipping-material {
		border :1px solid blue;
		font-weight:normal;
		text-align:center;
		width:90px;
		background-color:#e6dc84;
		float:left;
		margin-right:.6em;
		transition: .4s;
	}
	
	.shipping-material:first-child, .shipping-material:nth-child(9) {
		margin-left:.6em;
	}
	.shipping-material div:nth-child(2) {
		border-bottom: 1px dashed gray;
	}
	.shipping-material div.longer_model {
		font-size:12px;
	}
	.shipping-material div.longest_model {
		font-size:10px;
	}
	.shipping-material div:nth-child(3) {
		border-bottom: 1px dashed gray;
	}
	.shipping-material div:last-child > span{
		position: absolute;
		color: red;
		font-size:12px;
		border: 1px solid red;
		transform: rotate(35deg);
		transform-origin: right middle;
	}
</style>
				<div id="listarea" style="position:absolute;top: 26px;right:5px;">
					<table class="condform" id="label_table" style="margin-top: 15px; margin-bottom: 15px;font-size:15px;">
						<tr>
							<td class="ui-widget-header" style="text-align:center;">图例</td>
							<td style="border:none;"></td>
							<td class="ui-state-default td-title">总库位数</td>
							<td class="td-content"><label /></td>
						</tr>
						<tr>
							<td class="ui-storage-highlight" style="text-align:center;font-size:10px;">占用<br>两个月以内</td>
							<td style="border:none;"></td>
							<td class="ui-state-default td-title">当前在库台数</td>
							<td class="td-content"><label /></td>
						</tr>
						<tr>
							<td class="ui-state-error" style="text-align:center;font-size:10px;">占用<br>两个月以上</td>
							<td style="border:none;"></td>
							<td class="ui-state-default td-title">超期台数</td>
							<td class="td-content"><label/></td>
						</tr>
						<tr>
							<td style="text-align:center;font-size:10px;outline: 2px solid green;">等待入库</td>
							<td style="border:none;"></td>
							<td class="ui-state-default td-title">等待入库</td>
							<td class="td-content"><label /></td>
						</tr>
						<tr>
							<td style="text-align:center;font-size:10px;outline: 2px solid blue;">等待出库</td>
							<td style="border:none;"></td>
							<td class="ui-state-default td-title">等待出库</td>
							<td class="td-content"><label /></td>
						</tr>
					</table>
				</div>
				<div class="clear"></div>
				<div id="shippingarea" style="position: absolute; left: 10px; border: 1px solid black; top: 44px; width: 745px; height: 273px;">
					<table class="condform" id="shipping_table" style="font-size:15px;width:100%;height:100%;margin:0;">
						<tr>
							<td class="ui-widget-header" style="text-align:center;width:1em;">待出库一览</td>
							<td class="ui-state-default">
							</td>
						</tr>
					</table>
				</div>
			</div>
		</div>
	</div>
</body>
</html>