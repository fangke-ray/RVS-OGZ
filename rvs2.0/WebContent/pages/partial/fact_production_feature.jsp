<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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

<link rel="stylesheet" type="text/css" href="css/olympus/select2Buttons.css">
<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="js/i18n/grid.locale-cn.js"></script>
<script type="text/javascript" src="js/ajaxfileupload.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/partial/fact_production_feature.js"></script>

<style type="text/css">
.left{
	float:left;
}
.right{
	float:right;
}
ul{
	list-style: none;
	padding: 0;
	margin: 0;
}

/* tabs标签外形-自定义 */
.ui-corner-left + label {
	-moz-border-radius-topleft: 6px;
	-webkit-border-top-left-radius: 6px;
	-khtml-border-top-left-radius: 6px;
	border-top-left-radius: 6px;
	-moz-border-radius-bottomleft: 6px;
	-webkit-border-bottom-left-radius: 6px;
	-khtml-border-bottom-left-radius: 6px;
	border-bottom-left-radius: 6px;
	height:42px;
	margin-right:4px;
}

/* tabs标签外形-自定义 */
.ui-corner-left + label span {
	font-size:16px;
	margin-top:8px;
}

#workflow ul.ui-buttonset{
	margin-right: 3px;
}
</style>

<title>现品作业信息</title>
</head>
<body class="outer">
	<div class="width-full" style="align:center;margin:auto;margin-top:16px;">
		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="1"/>
				<jsp:param name="sub" value="p"/>
			</jsp:include>
		</div>
	
		<div class="ui-widget-panel ui-corner-all width-full" style="padding-top:16px;padding-bottom:16px;">
			<div class="dwidth-full">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-full">
					<span class="areatitle">零件仓作业</span>
				</div>
				
				<div class="dwidth-full" style="padding-top:4px;">
					<div id="workflow" class="ui-widget-content left">
						<ul>
							<li>
								<input type="radio" name="works" class="ui-button ui-corner-left" id="receptbutton">
								<label for="receptbutton">收货</label>
							</li>
							<li>
								<input type="radio" name="works" class="ui-button ui-corner-left" id="collationbutton">
								<label for="collationbutton">核对</label>
							</li>
							<li>
								<input type="radio" name="works" class="ui-button ui-corner-left" id="unpackbutton">
								<label for="unpackbutton">分装</label>
							</li>
							<li>
								<input type="radio" name="works" class="ui-button ui-corner-left" id="onshelfbutton">
								<label for="onshelfbutton">上架</label>
							</li>
							<li>
								<input type="radio" name="works" class="ui-button ui-corner-left" id="outstoragebutton">
								<label for="outstoragebutton">出库</label>
							</li>
							<li>
								<input type="radio" name="works" class="ui-button ui-corner-left" id="otherbutton">
								<label for="otherbutton">其它</label>
							</li>
						</ul>
					</div>
					<div id="workflow_content" class="right" style="width:1180px;"></div>
					<div class="clear areaencloser"></div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
