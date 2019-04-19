<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
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
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/jquery.select2buttons.js"></script>
<script type="text/javascript" src="js/jquery.mtz.monthpicker.min.js"></script>
<script type="text/javascript" src="js/equipment/device_jig_order.es5.js"></script>

<style type="text/css">
.text-center {
  text-align: center !important;
}

.text-right {
  text-align: right !important;
}

.referchooser {
	z-index:1050;
}
#order_detail table{
	border-collapse: collapse;
}

#order_detail tbody tr{
	height: 30px;
}

#order_detail tbody td{
	font-weight: normal;
}

#order_detail td.td-title{
	width:92px;
	padding:0px;
	text-align: center;
	border-color: #aaa;
}

#order_detail td.td-content{
	width:92px;
	border-color: #aaa;
}

#order_detail td.rowNum{
	width:30px;
	text-align: center;
}

#order_detail tr td:last-child{
	width: 50px;
}

#order_detail input[type='text']{
	width:85px;
}

#order_detail input[type='text'].nesssary_reason{
	width:160px;
}

#order_detail .ui-buttonset span{
	padding: 0.1em 0.3em !important;
}

#order_detail input.subtract,#add_row{
	padding: .1em 1em;
}

#quotationsend tbody tr{
	height: 30px;
}

td.text-right{
	padding-right:3px;
	padding-left:0;
}

</style>

<title>设备工具订购申请</title>
</head>
<% 
	String privacy = (String) request.getAttribute("privacy");
	boolean isTechnology = ("technology").equals(privacy);
	String role = (String) request.getAttribute("role");
	boolean isLine = ("line").equals(role);
	boolean isManager = ("manager").equals(role);
%>
<body class="outer" style="overflow: auto;" istechnology="<%= isTechnology%>">
	<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">
		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="2"/>
			</jsp:include>
		</div>
		
		<div class="ui-widget-panel ui-corner-all width-full" style="align: center; padding-top: 16px;" id="body-3">
			<div id="body-lft" style="width: 256px; float: left;">
				<jsp:include page="/appmenu.do?method=tinit" flush="true">
					<jsp:param name="linkto" value="设备工具•专用工具信息管理"/>
				</jsp:include>
			</div>
			
			<div id="body-mdl" style="width: 994px; float: left;">
				<div id="search">
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					   	 <span class="areatitle">检索条件</span>
					     <a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
						 	<span class="ui-icon ui-icon-circle-triangle-n"></span>
						 </a>
					</div>
					
					<div class="ui-widget-content">
						<form id="searchform" method="POST">
							<table class="condform">
								<tbody>
									<tr>
										<td class="ui-state-default td-title">报价单号</td>
								   		<td class="td-content">
											<input id="search_quotation_no" type="text" class="ui-widget-content">
										</td>
										<td class="ui-state-default td-title">订单号</td>
								   		<td class="td-content">
											<input id="search_order_no" type="text" class="ui-widget-content" value="${initOrderNo }">
										</td>
										<td class="ui-state-default td-title">订购品</td>
								   		<td class="td-content">
											<input id="search_model_name" type="text" class="ui-widget-content" value="${initModelName }">
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">申请者</td>
								   		<td class="td-content">
											<input type="text" id="search_applicator_name" readonly="readonly" class="ui-widget-content">
											<input type="hidden" id="search_applicator_id">
										</td>
										<td class="ui-state-default td-title">询价</td>
								   		<td class="td-content" id="order_invoice_flg">
								   			<input type="radio" name="order_invoice" id="search_order_invoice_all" class="ui-widget-content" value="" checked>
								   			<label for="search_order_invoice_all">(全部)</label>
											<input type="radio" name="order_invoice" id="search_order_invoice_yes" class="ui-widget-content" value="1">
											<label for="search_order_invoice_yes">未询价</label>
										</td>
										<td class="ui-state-default td-title">询价发送日期</td>
										<td class="td-content">
											<input type="text" class="ui-widget-content" id="search_send_date_start" readonly="readonly">起<br>
											<input type="text" class="ui-widget-content" id="search_send_date_end" readonly="readonly">止
										</td>
									</tr>
									<tr>
										<td class="ui-state-default td-title">预计纳期</td>
										<td class="td-content">
											<input type="text" class="ui-widget-content" id="search_scheduled_date_start" readonly="readonly">起<br>
											<input type="text" class="ui-widget-content" id="search_scheduled_date_end" readonly="readonly">止
										</td>
										<td class="ui-state-default td-title">收货时间</td>
										<td class="td-content">
											<input type="text" class="ui-widget-content" id="search_recept_date_start" readonly="readonly">起<br>
											<input type="text" class="ui-widget-content" id="search_recept_date_end" readonly="readonly">止
										</td>
										<td class="ui-state-default td-title">验收</td>
								   		<td class="td-content" id="inline_recept_flg">
								   			<input type="radio" name="inline_recept" id="search_inline_recept_all" class="ui-widget-content" value="">
								   			<label for="search_inline_recept_all">(全部)</label>
											<input type="radio" name="inline_recept" id="search_inline_recept_yes" class="ui-widget-content" value="1" checked>
											<label for="search_inline_recept_yes">未验收</label>
										</td>
									</tr>
								</tbody>
							</table>
							<div style="height:44px">
								<input type="button" class="ui-button" id="resetbutton" value="清除" style="float:right;right:2px">
								<input type="button" class="ui-button" id="searchbutton" value="检索" style="float:right;right:2px">					
							</div>
						</form>
					</div>
					
					<div class="areaencloser"></div>
					
					<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
						<span class="areatitle">设备工具订购申请一览</span>
						<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
							<span class="ui-icon ui-icon-circle-triangle-n"></span>
						</a>
					</div>
					
					<div>
						<div id="chooser" class="ui-widget-content" style="border-bottom: 0;">
							<div id="colchooser">
								<input type="checkbox" id="colchooser_buy"></input><label for="colchooser_buy">购买依赖</label>
								<input type="checkbox" id="colchooser_invoice"></input><label for="colchooser_invoice">询价</label>
								<input type="checkbox" id="colchooser_quotation"></input><label for="colchooser_quotation">报价发送</label>
								<input type="checkbox" id="colchooser_recept"></input><label for="colchooser_recept">受领确认</label>
							</div>
						</div>
						
						<table id="list"></table>
						<div id="listpager"></div>
<% if (isTechnology || isLine || isManager) { %>
						<div class="ui-widget-header areabase"style="padding-top:4px;">
						    <div id="executes" style="margin-left:4px;margin-top:4px;">
								<input type="button" id="applicationbutton" class="ui-button" value="申请">
<% if (isTechnology) { %>
								<input type="button" id="invoicebutton" class="ui-button" value="询价">
								<input type="button" id="quotationsendbutton" class="ui-button" value="报价发送">
								<input type="button" id="quotationtrackbutton" class="ui-button" value="报价追踪">
								<input type="button" id="budgetbutton" class="ui-button" value="预算">
<%} %>							
								<input type="button" id="inlinereceptbutton" class="ui-button" value="验收">
							</div>
						</div>
<%} %>
					</div>
				</div>
			</div>
			
			<div class="clear areaencloser"></div>
			
		</div>
		
		<div id="choose_order_no" style="display: none;">
			<div class="ui-widget-content">
				<table class="condform">
					<thead>
						<tr class="text-center">
							<td class="ui-state-default td-title">订单号</td>
							<td class="ui-state-default td-title"></td>
						</tr>
					</thead>
					<tbody></tbody>
				</table>
			</div>
			
			<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
				<span class="areatitle">新建订单号</span>
			</div>
			
			<div class="ui-widget-content">
				<table class="condform">
					<thead>
						<tr>
							<td class="ui-state-default td-title">订单号</td>
							<td class="td-content">
								<input type="text" id="add_order_no" class="ui-widget-content">
								<span></span>
							</td>
						</tr>
					</thead>
				</table>
			</div>
		</div>
		
		<div id="update_order_no_dialog" style="display: none;">
			<div class="ui-widget-content">
				<table class="condform">
					<thead>
						<tr>
							<td class="ui-state-default td-title">订单号</td>
							<td class="td-content">
								<input type="text" id="update_order_no" class="ui-widget-content">
							</td>
						</tr>
					</thead>
				</table>
			</div>
		</div>
		
		<div id="order_detail" style="display: none;">
			<div class="ui-widget-content">
				<table class="condform" style="width: 99.6%;">
					<thead>
						<tr>
							<td class="ui-state-default td-title" style="width:35px;">行号</td>
							<td class="ui-state-default td-title">对象类别</td>
							<td class="ui-state-default td-title" style="width: 140px;">品名</td>
							<td class="ui-state-default td-title">型号/规格</td>
							<td class="ui-state-default td-title">系统编码</td>
							<td class="ui-state-default td-title" style="width: 140px;">名称</td>
							<td class="ui-state-default td-title" style="width: 50px;">受注方</td>
							<td class="ui-state-default td-title" style="width: 60px;">数量</td>
							<td class="ui-state-default td-title" style="width: 180px;">理由/必要性</td>
							<td class="ui-state-default td-title" style="width: 60px;">申请人</td>
							<td class="ui-state-default td-title" style="width: 60px;">现有备品数</td>
							<td class="ui-state-default td-title confirm" style="width: 80px;">经理确认</td>
							<td class="td-content text-center">
								<input type="button" id="add_row" class="ui-button" value="+"/>
							</td>
						</tr>
					</thead>
					<tbody></tbody>
				</table>
			</div>
		</div>
		
		<div id="invoice" style="display: none;">
			<table id="invoicelist"></table>
			<div id="invoicelistpager"></div>
		</div>
		
		<div id="invoice_detail" style="display: none;">
			<div class="ui-widget-content">
				<table class="condform" style="width:99.6%;">
					<thead>
						<tr class="text-center">
							<td class="ui-state-default td-title">询价发送日期</td>
							<td class="ui-state-default td-title">订购单价</td>
							<td class="ui-state-default td-title">原产单价</td>
							<td class="td-content" style="width: 90px;">
								<input type="button" id="add_invoice_row" class="ui-button" value="新建询价" style="padding: .2em .6em;"/>
							</td>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
			</div>
		</div>
		
		<div id="applicat_list" style="display: none;">
			<div class="ui-widget-content">
				<table class="condform" style="width:99.6%;">
					<thead>
						<tr class="text-center">
							<td class="ui-state-default td-title"><input type="checkbox"></td>
							<td class="ui-state-default td-title">订单号</td>
							<td class="ui-state-default td-title">系统编码</td>
							<td class="ui-state-default td-title">名称</td>
							<td class="ui-state-default td-title">受注方</td>
							<td class="ui-state-default td-title">数量</td>
							<td class="ui-state-default td-title">申请者</td>
							<td class="ui-state-default td-title">申请日期</td>
						</tr>
					</thead>
					<tbody></tbody>
				</table>
			</div>
		</div>
		
		<div id="quotationsend" style="display: none;">
			<div class="ui-widget-content">
				<table class="condform" style="width:99.6%;">
					<thead>
						<tr class="text-center">
							<td class="ui-state-default td-title">订单号</td>
							<td class="ui-state-default td-title"></td>
							<td class="ui-state-default td-title">对象类别</td>
							<td class="ui-state-default td-title">型号/规格</td>
							<td class="ui-state-default td-title">系统编码</td>
							<td class="ui-state-default td-title">名称</td>
							<td class="ui-state-default td-title">受注方</td>
							<td class="ui-state-default td-title">数量</td>
							<td class="ui-state-default td-title">单价</td>
							<td class="ui-state-default td-title">金额</td>
							<td class="ui-state-default td-title">日本价格</td>
						</tr>
					</thead>
					<tbody></tbody>
				</table>
			</div>
		</div>
		
		<div id="quotation_dialog" style="display: none;">
			<div class="ui-widget-content">
				<table class="condform" style="width:99.6%;">
					<tr>
						<td class="ui-state-default td-title">报价单号</td>
						<td class="td-content">
							<input type="text" class="ui-widget-content" id="add_quotation_no">
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">备注</td>
						<td class="td-content">
							<textarea class="ui-widget-content" cols="50" style="resize: none;" id="add_quotation_comment"></textarea>
						</td>
					</tr>
				</table>
			</div>
		</div>
		
		<div id="quotationtrack_dialog" style="display: none;">
			<div style="box-sizing:border-box;float:left;">
				 <div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					<span class="areatitle">报价一览</span>
				 </div>
			 	<table id="quotationlist"></table>
			 	<div id="quotationlistpager"></div>
			</div>
			<div style="box-sizing:border-box;float:left;margin-left:2px;">
				 <div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					<span class="areatitle">报价对应订单明细</span>
				 </div>
				 <table id="order_quotationlist"></table>
				 <div id="order_quotationlistpager"></div>
			</div>
			<div class="clear"></div>
		</div>
		
		<div id="quotation_detail" style="display: none;">
			<div class="ui-widget-content" >
				<table class="condform" style="width:99.6%;">
					<tr>	
						<td class="ui-state-default td-title">报价单号</td>
						<td class="td-content"></td>
					</tr>
					<tr>	
						<td class="ui-state-default td-title">确认接收日期</td>
						<td class="td-content"></td>
					</tr>
					<tr>	
						<td class="ui-state-default td-title">发送OSH日期</td>
						<td class="td-content"></td>
					</tr>
					<tr>	
						<td class="ui-state-default td-title">预计纳期</td>
						<td class="td-content">
							<input type="text" class="ui-widget-content" id="update_scheduled_date" readonly="readonly">
						</td>
					</tr>
					<tr>	
						<td class="ui-state-default td-title">备注</td>
						<td class="td-content">
							<textarea class="ui-widget-content" cols="50" style="resize: none;" id="update_quotation_comment"></textarea>
						</td>
					</tr>
				</table>
			</div>
		</div>
		
		<div id="update_recept" style="display: none;">
			<div class="ui-widget-content" >
				<table class="condform" style="width:99.6%;">
					<tr>
						<td class="ui-state-default td-title">确认结果</td>
						<td class="td-content">
							<select id="recept_update_confirm_flg"></select>
						</td>
					</tr>
					<!--tr>
						<td class="ui-state-default td-title">收货时间</td>
						<td class="td-content">
							<input type="text" class="ui-widget-content" id="recept_update_recept_date" readonly="readonly">
						</td>
					</tr-->
					<tr style="display: none;">
						<td class="ui-state-default td-title">重新订购纳期</td>
						<td class="td-content">
							<input type="text" class="ui-widget-content" id="recept_update_reorder_scheduled_date" readonly="readonly">
						</td>
					</tr>
				</table>
			</div>
		</div>
		
		<div id="add_spare" style="display: none;">
			<div class="ui-widget-content">
				<table class="condform" style="width:99.6%;">
					<tr>
						<td class="ui-state-default td-title">确认数量</td>
						<td class="td-content">
							<input type="text" class="ui-widget-content" id="add_spare_confirm_quantity">
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">备品种类</td>
						<td class="td-content">
							<select id="add_device_spare_type">${goDeviceSpareType }</select>
						</td>
					</tr>
				</table>
			</div>
		</div>
		
		<div id="budget_dialog" style="display: none;">
			<div class="ui-widget-content">
				<table class="condform" style="width:99.6%;">
					<tr>
						<td class="ui-state-default td-title">型号/规格</td>
						<td class="td-content">
							<label id="budget_label_model_name"></label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">系统编码</td>
						<td class="td-content">
							<label id="budget_label_system_code"></label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">名称</td>
						<td class="td-content">
							<label id="budget_label_name"></label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">数量</td>
						<td class="td-content">
							<label id="budget_label_quantity"></label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">理由/必要性</td>
						<td class="td-content">
							<label id="budget_label_nesssary_reason"></label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">申请日期</td>
						<td class="td-content">
							<label id="budget_label_applicate_date"></label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">总价</td>
						<td class="td-content">
							<label id="budget_label_total_order_price"></label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">订单号</td>
						<td class="td-content">
							<label id="budget_label_order_no"></label>
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">预算月</td>
						<td class="td-content">
							<input type="text" class="ui-widget-content" id="budget_update_budget_month" readonly>
							<input type="button" class="ui-button" value="ｘ" id="budget_update_budget_month_clearer" style="padding:0;">
						</td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">预算说明</td>
						<td class="td-content">
							<textarea class="ui-widget-content" cols="35" style="resize: none;" id="budget_update_budget_description"></textarea>
						</td>
					</tr>
				</table>
			</div>
		</div>
		
		
		<div class="referchooser ui-widget-content" id="operator_id_referchooser" tabindex="-1">
			<table width="200px">
				<tr>
					<td></td>
					<td width="50%">过滤字:<input type="text"/></td>
					<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
				</tr>
			</table>
			<table  class="subform">${oReferChooser}</table>
		</div>
		
		<!-- 设备工具品名 -->
		<div class="referchooser ui-widget-content" id="device_type_id_referchooser" tabindex="-1">
			<table width="200px">
				<tr>
					<td></td>
					<td width="50%">过滤字:<input type="text"/></td>
					<td width="50%" align="right"><input type="button" class="ui-button" style="float:right;" value="清空"/></td>
				</tr>
			</table>
			<table  class="subform">${devicesTypeReferChooser}</table>
		</div>
		
		<input type="hidden" id="role" value="${role }">
		<input type="hidden" id="loginID" value="${loginID }">
		<input type="hidden" id="loginName" value="${loginName }">
		<select id="hide_order_from" style="display:none;">${sOrderFrom }</select>
		<select id="hide_object_type" style="display:none;">${sObjectType }</select>
		<select id="hide_confirm_flg" style="display:none;">${sConfirmFlg }</select>
		
	</div>
</body>
</html>