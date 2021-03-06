<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/ajaxfileupload.js"></script>
<script type="text/javascript" src="js/qf/acceptance.js?version=215"></script>
<script type="text/javascript" src="js/qf/acceptance_turnover_case.js"></script>
<style>
.waitTicket {
	background-color : #0070C0;
	color :white;
}
.ui-state-hover .waitTicket {
	color :#F8BB14;
}
</style>
<title>受理</title>
</head>
<body class="outer" style="align: center;">

<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">
		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="2"/>
			</jsp:include>
		</div>
		<div class="ui-widget-panel ui-corner-all width-full" style="align: center; padding-top: 16px;" id="body-2">
			<div id="body-lft" style="width: 256px; float: left;">
				<jsp:include page="/appmenu.do" flush="true">
					<jsp:param name="linkto" value="受理报价"/>
				</jsp:include>
			</div>
			<div id="body-mdl" style="width: 1022px; float: left;">
			<div id="searcharea">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
					<span class="areatitle">受理导入</span>
				</div>
				<div class="ui-widget-content dwidth-middleright">
					<form id="uploadform" method="POST">
						<table class="condform">
							<tbody>
								<tr>
									<td class="ui-state-default td-title">上传文件</td>
									<td class="td-content"><input type="file" name="file" id="file" class="ui-widget-content" /></td>
								</tr>
							</tbody>
						</table>
						<div style="height: 44px">
							<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="uploadbutton" value="载入" role="button" aria-disabled="false" style="float: right; right: 2px">
						</div>
					</form>
				</div>
				<div class="clear dwidth-middleright"></div>
			</div>
			<div id="uld_listarea" class="width-middleright">
				<table id="uld_list"></table>
				<div id="uld_listpager"></div>
				<div id="uld_listedit"></div>
				<div class="ui-widget-content areabase dwidth-middleright">
					<div id="executes" style="margin-left: 4px; margin-top: 4px;">
						<input type="button" class="ui-button" value="导入" id="importbutton" />
						<input type="button" class="ui-button" value="手工添加" id="manualbutton" />
						<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="resetbutton" value="清除" role="button" aria-disabled="false" style="float: right; right: 2px">
					</div>
				</div>
			</div>
			<div id="imp_listarea" class="width-middleright">
				<table id="imp_list"></table>
				<div id="imp_listpager"></div>
				<div id="imp_listedit"></div>
				<div class="ui-widget-content areabase dwidth-middleright">
					<div id="executes" style="margin-left: 4px; margin-top: 4px;">
						<input type="button" class="ui-button" id="printbutton" value="打印现品票" />
						<input type="button" class="ui-button" id="acceptancebutton" value="实物受理" />
						<div class="ui-button" id="returnbutton" style="font-size:14px;">
							不修理发还
							<div class="ui-widget-content" style="z-index:2;position:absolute;display:none;top:-200%;">
								<input type="button" class="ui-button" value="发还给RC" id="returnRCbutton" />
								<input type="button" class="ui-button" value="发还给OSH" id="returnOSHbutton" />
							</div>
						</div>
						<input type="button" class="ui-button" id="disinfectionbutton" value="进行消毒" />
						<input type="button" class="ui-button" id="sterilizationbutton" value="进行灭菌" />
						<input type="button" class="ui-button" id="tcbutton" value="通箱入库准备" style="float: right; right: 2px">
						<input type="button" class="ui-button-primary ui-button" id="outbutton" value="临时生成当日报表" role="button" aria-disabled="false" style="float: right; right: 2px">
					</div>
				</div>
			</div>
			</div>
			<div class="clear areaencloser dwidth-middle"></div>
			<div id="confirmmessage"></div>

			<div class="if_message-dialog ui-warn-dialog" style="margin-left:-250px; width:500px;">
  				<div class="ui-dialog-titlebar ui-widget-header" style="height:24px;"><span>接口处理同步</span></div>
				<div style="margin-top: 1em;margin-bottom: 1em;">其他系统中的操作已经改变了受理品的状态，请刷新一览表。</div>
				<input type="button" class="ui-button" value="忽略"></input>
				<input type="button" class="ui-button" value="刷新"></input>
			</div>
		</div>
</div>
<div id="pop_detail"></div>
<input type="hidden" id="g_bound_out_ocm" value='${g_bound_out_ocm }'>
<input type="hidden" id="g_area" value='${g_area }'>
</body>
</html>