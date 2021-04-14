<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" 
import="java.util.List" 
isELIgnored="false"%>
<%@page import="com.osh.rvs.bean.LoginData"%>
<%@page import="com.osh.rvs.common.RvsConsts"%>
<%@page import="com.osh.rvs.bean.master.LineEntity"%>
<%@page import="com.osh.rvs.bean.master.SectionEntity"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Page-Exit"; content="blendTrans(Duration=1.0)">
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript" src="js/app/panel.js"></script>
<style>
	span.panel-message-title {
		font-size : 16px;
		padding-left : 4px;
	}
	#panelarea div.ui-widget-content {
		padding : 6px;
	}
	#panelarea > div.ui-widget-content:not(:first-child) {
		margin-top : 6px; 
	}
	#panelarea .ui-button-text-only .ui-button-text {
		padding: .4em 1em !important;
	}
	#panelarea .ui-buttonset .ui-button {
		margin-right: 0em;
	}
</style>

<title>设置面板</title>
</head>
<body class="outer" style="align: center;">


	<div class="width-full" style="align: center; margin: auto; margin-top: 16px;">
		<div id="basearea" class="dwidth-full" style="margin: auto;">
			<jsp:include page="/header.do" flush="true">
				<jsp:param name="part" value="3"/>
			</jsp:include>
		</div>

		<div class="ui-widget-panel ui-corner-all dwidth-full" style="align: center; padding-top: 16px; padding-bottom: 16px;" id="body-2">
			<div id="body-lft" style="width: 256px; float: left;">
				<jsp:include page="/appmenu.do" flush="true">
					<jsp:param name="linkto" value="panel"/>
				</jsp:include>
			</div>
			<div style="width: 798px; float: left;">
				<div id="body-mdl" class="dwidth-middle" style="margin: 0;">
<%
	String pwdDateMessage = (String)request.getAttribute("pwdDateMessage");
	if (pwdDateMessage!=null) {
%>
	<div id="pwdDateMessage" class="ui-state-active" style="font-size: 16px;padding:1em;cursor:pointer;">
		<%=pwdDateMessage%>
	</div>
<%
	}
%>
					<div id="panelarea" class="dwidth-middle">
<% 
	LoginData user = (LoginData) request.getSession().getAttribute(RvsConsts.SESSION_USER);
	String role_id = user.getRole_id();
%>
						<div class="ui-widget-content">
							<div class="ui-state-default">
								<span class="panel-message-title">请选择您当前的工作课室</span>
							</div>
<%
	String section_id = user.getSection_id();
%>
							<div class="ui-widget-content">
								<span id="sections">
<%
	if (!"00000000009".equals(role_id) && !"00000000012".equals(role_id)) { 
	List<SectionEntity> sections = user.getSections();
	for (SectionEntity section : sections) {
		if (section.getSection_id() != null) {
%>
									<input type="radio" name="section" id="section_<%=section.getSection_id()%>" value="<%=section.getSection_id()%>" <%=(section.getSection_id().equals(user.getSection_id())) ? "checked" : ""%>>
									<label for="section_<%=section.getSection_id()%>"><span><%=section.getName()%></span></label>
<%
		}
	}
	} else {
%>
<%
		if (user.getDepartment() == 1) {
%>
									<input type="radio" name="section" value="00000000001" id="section_0000000001" <% if ("00000000001".equals(section_id)) { %>checked<% } %>><label for="section_0000000001"><span>修理生产G</span></label>
									<input type="radio" name="section" value="00000000006" id="section_0000000006" <% if ("00000000006".equals(section_id)) { %>checked<% } %>><label for="section_0000000006"><span>修理支援G</span></label>
									<input type="radio" name="section" value="00000000007" id="section_0000000007" <% if ("00000000007".equals(section_id)) { %>checked<% } %>><label for="section_0000000007"><span>品保课</span></label>
<%
		} else if (user.getDepartment() == 2) {
%>
									<input type="radio" name="section" value="00000000009" id="section_0000000009" <% if ("00000000009".equals(section_id)) { %>checked<% } %>><label for="section_0000000009"><span>组立T</span></label>
									<input type="radio" name="section" value="00000000010" id="section_0000000010" <% if ("00000000010".equals(section_id)) { %>checked<% } %>><label for="section_0000000010"><span>品质技术T</span></label>
<%
		}
	}
%>
								</span>
							</div>
						</div>
<%
	if ("00000000005".equals(role_id) || "00000000012".equals(role_id) || "00000000009".equals(role_id)) { 
%>
						<div class="ui-widget-content">
							<div class="ui-state-default">
								<span class="panel-message-title">请选择您当前的工程</span>
							</div>
<%
	String line_id = user.getLine_id();
%>
							<div class="ui-widget-content">
								<span id="lines">
<%
		if (user.getDepartment() == 1) {
%>
									<input type="radio" name="line" value="00000000011" id="line_00000000011" <% if ("00000000011".equals(line_id)) { %>checked<% } %>><label for="line_00000000011"><span>受理报价</span></label>
									<input type="radio" name="line" value="00000000012" id="line_00000000012" <% if ("00000000012".equals(line_id)) { %>checked<% } %>><label for="line_00000000012"><span>分解</span></label>
									<input type="radio" name="line" value="00000000013" id="line_00000000013" <% if ("00000000013".equals(line_id)) { %>checked<% } %>><label for="line_00000000013"><span>ＮＳ</span></label>
									<input type="radio" name="line" value="00000000014" id="line_00000000014" <% if ("00000000014".equals(line_id)) { %>checked<% } %>><label for="line_00000000014"><span>总组</span></label>
									<input type="radio" name="line" value="00000000050" id="line_00000000050" <% if ("00000000050".equals(line_id)) { %>checked<% } %>><label for="line_00000000050"><span>外科硬镜修理</span></label>
									<input type="radio" name="line" value="00000000060" id="line_00000000060" <% if ("00000000060".equals(line_id)) { %>checked<% } %>><label for="line_00000000060"><span>纤维镜分解</span></label>
									<input type="radio" name="line" value="00000000061" id="line_00000000061" <% if ("00000000061".equals(line_id)) { %>checked<% } %>><label for="line_00000000061"><span>纤维镜总组</span></label>
									<input type="radio" name="line" value="00000000070" id="line_00000000070" <% if ("00000000070".equals(line_id)) { %>checked<% } %>><label for="line_00000000070"><span>周边设备修理</span></label>
									<input type="radio" name="line" value="00000000054" id="line_00000000054" <% if ("00000000054".equals(line_id)) { %>checked<% } %>><label for="line_00000000054"><span>中小修</span></label>
									<input type="radio" name="line" value="00000000015" id="line_00000000015" <% if ("00000000015".equals(line_id)) { %>checked<% } %>><label for="line_00000000015"><span>品保</span></label>
<%
		} else if (user.getDepartment() == 2) {
%>
									<input type="radio" name="line" value="00000000101" id="line_00000000101" <% if ("00000000101".equals(line_id)) { %>checked<% } %>><label for="line_00000000101"><span>组装</span></label>
									<input type="radio" name="line" value="00000000102" id="line_00000000102" <% if ("00000000102".equals(line_id)) { %>checked<% } %>><label for="line_00000000102"><span>检查</span></label>
									<input type="radio" name="line" value="00000000076" id="line_00000000076" <% if ("00000000076".equals(line_id)) { %>checked<% } %>><label for="line_00000000076"><span>最终检查</span></label>
									<input type="radio" name="line" value="00000000103" id="line_00000000103" <% if ("00000000103".equals(line_id)) { %>checked<% } %>><label for="line_00000000103"><span>包装</span></label>
<%
		}
%>
								</span>
							</div>
						</div>
<%
	} else if ("00000000006".equals(role_id) || "00000000001".equals(role_id) || "00000000002".equals(role_id) || "00000000007".equals(role_id) || "00000000008".equals(role_id)) { 
%>
						<div class="ui-widget-content">
							<div class="ui-state-default">
								<span class="panel-message-title">请选择您当前的工程</span>
							</div>
							<div class="ui-widget-content" style="min-height: 120px;">
								<span id="lines">
<%
//	List<PositionEntity> positions = user.getPositions();
//	for (PositionEntity position : positions) {
//		if (position.getPosition_id() != null) {
	List<LineEntity> lines = user.getLines();
	for (LineEntity line : lines) {
		if (line.getLine_id() != null) {
%>
									<input type="radio" name="line" id="line_<%=line.getLine_id()%>" value="<%=line.getLine_id()%>" <%=(line.getLine_id().equals(user.getLine_id())) ? "checked" : ""%>>
									<label for="line_<%=line.getLine_id()%>"><span><%=line.getName()%></span></label>
<%
		} 
	} 
%>
								</span>
							</div>
						</div>
<%
	} 
%>						<div class="ui-widget-content" id="oldbrowser" style="display: none;">
							<div class="ui-state-default">
								<span class="panel-message-title">浏览器版本提示</span>
							</div>
							<div class="ui-widget-content">
								<p>你正在使用的浏览器版本过低、这可能导致:</p>
								<p>1.画面流畅度变低</p>
								<p>2.一部分页面效果无法展示</p>
								<p>3.工作页面需要手动刷新以显示最新状况</p>
								<p>4.展示界面无法做到即时反映</p>
								<p>请升级到以下版本的浏览器使用本系统.</p>
								<p>Chrome 4+ Firefox 4+ Internet Explorer 10+ Opera 10+ Safari 5+</p>
							</div>
						</div>
						<div class="ui-widget-content" id="system_verison" style="">
							<div class="ui-widget-content">
								<p>系统版本:</p>
								<p id="nee">2.9.540</p>
								<p>&nbsp;</p>
								<p>发布时间:</p>
								<p>2021年3月2日</p>
							</div>
						</div>
<%
	if ("00000000012".equals(role_id)) { 
%>
						<div class="ui-widget-content" id="system_verison_content">
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>21/3/2 2.9.540 更新</p>
								<p>在线作业/管理：工作步骤计次工具正式版上线。</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>21/1/22 2.8.536 更新</p>
								<p>受理/物料/出货：通箱出入库管理上线。</p>
								<p>物料：零件出货（其它）工时设置。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>21/1/16 2.8.535 更新</p>
								<p>在线作业/管理：工程检查表输入项目设置与提示。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>20/3/2 2.5.519 更新</p>
								<p>在线作业/管理：工作步骤计次工具测试版第二版。</p>
								<p>设备・专用工具管理：计量器具管理编号记载。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>20/1/7 2.5.517 更新</p>
								<p>在线作业/管理：工作步骤计次工具测试版。</p>
								<p>展示：纳期维修品一览展示。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/12/20 2.5.513 更新</p>
								<p>在线作业/管理：总组流水线工程分线。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/11/15 2.5.510 更新</p>
								<p>显微镜制作：展示画面相应(标准工时/计划排定/流水线变更)调整</p>
								<p>显微镜制作：增加计划排定等管理功能</p>
								<p>显微镜制作：标准时间允许小数化</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/10/14 2.5.500 更新</p>
								<p>显微镜制作：BX流水线导入</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/8/1 2.4.451 更新</p>
								<p>设备・专用工具管理：同工位点检表单批量进行点检</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/6/26 2.4.446 更新</p>
								<p>设备・专用工具管理：设备工具订购申请导出询价单，导出订单功能</p>
								<p>在线作业：工程检查票客户端保存</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/6/17 2.4.440 更新</p>
								<p>受理报价：备品和RC品受理消毒灭菌。</p>
								<p>展示：单元拉展示。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/4/27 2.3.435 更新</p>
								<p>设备・专用工具管理：设备工具维修记录一览表上线</p>
								<p>物料作业：零件出入库工时未达成警报</p>
								<p>物料作业：零件出入库工时月报表显示未达成</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/4/17 2.3.417 更新</p>
								<p>设备・专用工具管理：设备工具订购申请推送方式修改。</p>
								<p>物料作业：其他维修零件出库。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/3/28 2.3.410 更新</p>
								<p>设备・专用工具管理：设备工具备品管理。</p>
								<p>设备・专用工具管理：点检归档查询。</p>
								<p>生产线线管理：重要工程日次点检。</p>
								<p>设备・专用工具管理：设备工具替代管理。</p>
								<p>设备・专用工具管理：设备工具订购申请。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/2/22 2.1.389 更新</p>
								<p>展示：周边修理展示。</p>
								<p>设备・专用工具点检：线长确认判定查询与判定。</p>
								<p>文档：出入库月报下载。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/1/31 2.1.379 更新</p>
								<p>在线作业/管理：周边修理功能上线。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/1/21 2.0.372 更新</p>
								<p>设备・专用工具点检：特殊点检表点检操作功能上线。</p>
								<p>设备・专用工具点检：周边修理点检关系功能上线。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>19/1/17 2.0.360 更新</p>
								<p>物料作业：零件入出库工时记录功能上线。</p>
								<p>物料作业：零件入出库展示功能上线。</p>
								<p>设备・专用工具管理：基本功能上线。</p>
								<p>设备・专用工具管理：安全手顺功能上线。</p>
								<p>设备・专用工具点检：点检表管理功能上线。</p>
								<p>设备・专用工具点检：点检操作功能上线。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>18/11/12 1.9.298 更新</p>
								<p>在线作业/管理：流水线工程重排。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>18/04/09 1.8.294 更新</p>
								<p>在线作业/管理：151P 维修流程与KPI数据对应。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>18/02/27 1.7.287 更新</p>
								<p>在线作业：CCD 线更换支持。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/12/28 1.7.267 更新</p>
								<p>在线作业：中小修理采用流水线中的单元维修。</p>
								<p>在线作业：调胶作业。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/7/19 1.6.248 更新</p>
								<p>展示：全局展示画面布局和规则修改。</p>
								<p>进度查询：增加维修对象各工位工时统计报表功能。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/6/20 1.6.234 更新</p>
								<p>展示：分解工程仕挂显示修改。</p>
								<p>展示：STR计算方式修正。</p>
								<p>通知：每日KPI统计方式修正。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/5/31 1.6.230 更新</p>
								<p>展示：刷新时间间隔修改。显示项目修改。</p>
								<p>通知：每日KPI统计方式修正。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/5/13 1.5.220 更新</p>
								<p>受理报价：投线时不受预提零件发放影响。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/4/17 1.5.219 更新</p>
								<p>进度一览：维修对象工时统计。</p>
								<p>展示：进度管理板三色管理。</p>
								<p>通知：邮件设定完成。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/4/17 1.4.216 更新</p>
								<p>受理报价：保内返品报价不受顺序制约。</p>
								<p>投线：WIP在库超时品投线前强制进行图像检查。</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/3/30 1.4.215 更新</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/1/13 1.3.198 更新</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/1/10 1.2.196 更新</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>17/1/7 1.2.188 更新</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>16/12/6 1.1.141 更新</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>16/11/21 1.1.119 更新</p>
								<p>在线维修：返工过维修对象标示</p>
								<p>在线维修：全机种质量提示在一段时间内不重复显示</p>
								<p>--------------------------------------</p>
							</div>
							<div class="ui-widget-content" style="max-height: 560px;overflow: auto;">
								<p>--------------------------------------</p>
								<p>16/10/19 1.0.23 更新</p>
								<p>全局：主程序现场上线、测试</p>
								<p>--------------------------------------</p>
							</div>
						</div>
<%
	} 
%>
						<div class="clear"></div>
					</div>
				</div>
			</div>
			<div style="width: 224px; float: left;">
				<div id="body-rgt" class="dwidth-right" style="margin-left: 8px;"></div>
			</div>
			<div class="clear areaencloser dwidth-middle"></div>
		</div>
	</div>

</body>
</html>