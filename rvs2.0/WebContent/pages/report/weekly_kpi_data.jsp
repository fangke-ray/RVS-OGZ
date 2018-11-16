<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()	+ request.getContextPath() + "/";
%>
<base href="<%=basePath%>">
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
<script type="text/javascript" src="js/ajaxfileupload.js"></script>
<script type="text/javascript" src="js/report/weekly_kpi_data.js"></script>
<title>周报KPI</title>
</head>
<body class="outer" style="align: center;">


<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">
	<div id="basearea" class="dwidth-full" style="margin: auto;">
		<jsp:include page="/header.do" flush="true">
			<jsp:param name="part" value="3" />
		</jsp:include>
	</div>

	<div class="ui-widget-panel ui-corner-all width-full" style="align: center; padding-top: 16px; padding-bottom: 16px; width: 1266px;" id="body-2">
		<div id="body-lft" style="width: 256px; float: left;">
			<jsp:include page="/appmenu.do" flush="true">
				<jsp:param name="linkto" value="文档管理" />
			</jsp:include>
		</div>
		<div id="body-mdl" style="width: 994px; float: left;">
			<div id="listarea">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
					<span class="areatitle">KPI周报一览</span> 
				</div>
	
				<div class="dwidth-middleright">
					<table id="exd_list"></table>
					<div id="exd_listpager"></div>
					<div id="upload_file"></div>
					<div id="confirmmessage"></div>
				</div>
				
				<div class="ui-widget-header areabase dwidth-middleright" style="margin-top: 20px;">
					<div id="executes" style="margin-left: 4px; margin-top: 4px;">
						<input type="button" class="ui-button" value="周报计算" id="calculatebutton" style="float: right;"/>
					</div>
				</div>
				<div id="calculatedialog" style="display: none;">
					<form method="POST">
						<table class="condform">
							<tr>
								<td class="ui-state-default td-title">统计日期起</td>
								<td class="td-content">
									<input type="text" class="ui-widget-content" readonly="readonly" id="add_count_date_start">
								</td>
							</tr>
							<tr>
								<td class="ui-state-default td-title">统计日期止</td>
								<td class="td-content">
									<input type="text" class="ui-widget-content" readonly="readonly" id="add_count_date_end">
								</td>
							</tr>
						</table>
					</form>
				</div>
				
				<div class="clear dwidth-middle"></div>
				<input type="hidden" id="privacy" value="${privacy }">
			</div>
			<div id="editarea">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-middleright">
					<span class="areatitle">SORC-OGZ 周报数据</span>
					<span class="areatitle" id="title_header"></span>
					<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
						<span class="ui-icon ui-icon-circle-triangle-w"></span>
					</a>
				</div>
				<style>
#editarea table {
border-collapse: collapse;
margin :0;
width: 100%;
}
#editarea table th,
#editarea table td
{
	border : 1px solid black;
}
#editarea table th{
	background-color : #00B0F0;
}
#editarea table td{
	background-color : #FFFFFF;
	text-align : center;
}
#editarea table td.group {
	width: 3em;
	text-align :center;
}
#editarea table td.matter {
	width: 30em;
	text-align : left;
}
#editarea table tr.formula td.matter {
	text-align : right;
}
#editarea table td.matter english
{
	color : #0066CC;
}
#editarea table td.matter japanese
{
	color : #0066CC;
	font-family : 'MS PMincho';
}
#editarea table td.target {
	width: 3em;
	border-right:0;
	text-align:center;
}
#editarea table td.target + td {
	width: 1em;
}
#editarea table tr.formula td {
	background-color : #D9D9D9;
}
#editarea table tr.formula td[rowspan] {
	background-color : #FFFFFF;
}
#editarea table tr.formula td.target {
	text-align:left;
}
#editarea table tr.noset td.target {
	text-align:right;
	color: #BFBFBF;
}
#editarea table td.target + td {
	border-left:0;
}
#editarea table tr.noset td.target + td {
	color: #BFBFBF;
}
#editarea table td[status="normal"] {
	background-color : lime;
}
#editarea table td[status="over"] {
	background-color : red;
}
#editarea table td[status="warn"] {
	background-color : yellow;
}
#editarea table input {
	width : 3.2em;
	border : 0;
	border-bottom : 1px solid #00b0f0;
	text-align : center;
	font-size:12px;
	background-color : transparent;
}
				</style>
				<div class="ui-widget-content">
					<table>
						<thead>
							<tr>
								<th class="stable" rowspan="2">类别</th><th class="stable" rowspan="2">报告项目</th><th class="stable" rowspan="2" colspan="2">目標</th>
							</tr>
							<tr>
								<th>一周</th><th>一周</th><th>一周</th><th>一周</th><th>一周</th><th>一周</th><th>一周</th><th>本周</th><th>下周</th>
							</tr>
						</thead>
						<tbody>
							<tr class="formula" for="target">
								<td class="stable group" rowspan="10">W<br>I<br>P<br>
								和<br>修<br>理<br>业<br>绩</td>
								<td class="stable matter"></td>
								<td class="stable target">见后</td><td class="stable">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="noset" for="registration">
								<td class="stable matter"><chinese>到货受理数</chinese><english>Registration</english><japanese>着荷台数</japanese></td>
								<td class="stable noset target">---</td><td class="stable noset">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="formula">
								<td class="stable matter">受理数差异（（目标数+返回数）比）</td>
								<td class="stable target">≥0</td><td class="stable">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="noset" for="user_agreement">
								<td class="stable matter"><chinese>修理同意数</chinese><english>User Agreement</english></td>
								<td class="stable target">---</td><td class="stable">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="noset" for="return_to_osh">
								<td class="stable matter"><chinese>返回ＯＳＨ修理</chinese><english>Return to OSH to repair</english></td>
								<td class="stable target">---</td><td class="stable">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="noset" for="unrepair">
								<td class="stable matter"><chinese>未修理返回</chinese><english>Unrepair</english></td>
								<td class="stable target">---</td><td class="stable">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="noset" for="shipment">
								<td class="stable matter"><chinese>出货总数</chinese><english>Shipment</english><japanese>出荷総数</japanese></td>
								<td class="stable target">---</td><td class="stable">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="formula">
								<td class="stable matter">出货数差异（目标数比）</td>
								<td class="stable target">≥0</td><td class="stable">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="noset" for="work_in_process">
								<td class="stable matter"><chinese>WIP在修数</chinese><english>Work In Process</english><japanese>工程仕掛数</japanese></td>
								<td class="stable target" style="color:red;font-size:smaller;">90~110</td><td class="stable">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="noset" for="work_in_storage">
								<td class="stable matter"><chinese>WIP总数</chinese><english></english><japanese>総仕掛数</japanese></td>
								<td class="stable target" style="text-align:center;">250</td><td class="stable">条</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>

							<tr for="intime_complete_rate">
								<td class="stable group" rowspan="6">周<br>
期<br>
(LT)<br>
纳<br>
期</td>
								<td class="stable matter"><chinese>大修理LT</chinese><english>Major repair LT ratio</english><br><japanese>スコープ重修理LT</japanese></td>
								<td class="stable target" lower="90">≥90</td><td class="stable">%</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr for="average_repair_lt">
								<td class="stable matter"><chinese>平均修理周期RLT</chinese><english>Average Repair LT (days)</english><br><japanese>平均修理LT</japanese></td>
								<td class="stable target" upper="6">≤6</td><td class="stable">天</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr for="intime_work_out_rate">
								<td class="stable matter"><chinese>零件到达后4天内出货比率</chinese><english>WLT within 4 days</english><br><japanese>部品納品後4日以内出荷率</japanese></td>
								<td class="stable target" lower="95">≥95</td><td class="stable">%</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr for="average_work_lt">
								<td class="stable matter"><chinese>平均工作周期</chinese><english>WLTAverage Work LT (days)</english><br><japanese>平均WLT</japanese></td>
								<td class="stable target" upper="4">≤4</td><td class="stable">天</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr for="bo_rate">
								<td class="stable matter"><chinese>当天零件BO率</chinese><english>Daily BO ratio</english><br><japanese>当日部品BO率</japanese></td>
								<td class="stable target" upper="8">≤8</td><td class="stable">%</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr for="bo_3day_rate">
								<td class="stable matter"><chinese>三天零件BO率</chinese><english>3-day BO ratio</english><br><japanese>3日間部品BO率</japanese></td>
								<td class="stable target" upper="4">≤4</td><td class="stable">%</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>

							<tr for="inline_passthrough_rate">
								<td class="stable group" rowspan="5">品<br>
質<br>
状<br>
况</td>
								<td class="stable matter"><chinese>工程内直行率</chinese><english>Going straight rate of  repair line</english><japanese></japanese></td>
								<td class="stable target" lower="94">≥94</td><td class="stable">%</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr class="noset" for="final_check_pass_count">
								<td class="stable matter"><chinese>最终检查合格件数</chinese><english></english><japanese></japanese></td>
								<td class="stable target" upper="0">---</td><td class="stable">件</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr for="final_check_forbid_count">
								<td class="stable matter"><chinese>最终检查不合格件数</chinese><english></english><japanese></japanese></td>
								<td class="stable target" upper="0">0</td><td class="stable">件</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr for="final_inspect_pass_rate">
								<td class="stable matter"><chinese>最终检查合格率</chinese><english>Final inspection pass ratio</english><japanese></japanese></td>
								<td class="stable target" lower="99.6">≥99.6</td><td class="stable">%</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>
							<tr for="service_repair_back_rate">
								<td class="stable matter"><chinese>内镜保修期内返品率（含新品不良）</chinese><br><english>Re-repair ratio within 6 months (including new product NG)</english><br><japanese>内視鏡保証期間内返品率(新品不良含む)</japanese></td>
								<td class="stable target" upper="0.9">≤0.9</td><td class="stable">%</td>

								<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td></td>
							</tr>

						</tbody>
					</table>
					<div style="height: 44px;margin-top:10px;padding-top:5px;">
						<input type="button" class="ui-button" id="cancelbutton" value="取消" style="float:right;right:2px">
						<input type="button" class="ui-button" id="updatebutton" value="保存" style="float:right;right:2px">
						<input type="button" class="ui-button" id="createreportbutton" value="生成周报" style="float:right;right:2px">
					</div>
				</div>
			</div>
		</div>
		<div class="clear"></div>
	</div>
</div>
</body>
</html>