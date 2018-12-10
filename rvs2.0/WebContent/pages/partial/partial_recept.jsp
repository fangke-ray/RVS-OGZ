<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="js/partial/partial_recept.js"></script>
<div id="partial_recept">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">零件收货</span>
	</div>
	<div class="ui-widget-content">
		<form id="uploadform" method="POST">
			<table class="condform">
				<tbody>
					<tr>
						<td class="ui-state-default td-title">上传文件</td>
						<td class="td-content"><input type="file" name="file" id="file" class="ui-widget-content" accept=".xlsx"/></td>
					</tr>
				</tbody>
			</table>
			<div style="height: 44px">
				<input type="button" class="ui-button" id="uploadbutton" value="载入" style="float: right; right: 2px">
			</div>
		</form>
	</div>
	
	<div class="areaencloser"></div>
	
	<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase">
		<div style="margin-top: 6px;margin-left: 6px;">
			<input type="button" class="ui-button" id="startbutton" value="开始">
			<input type="button" class="ui-button" id="endbutton" value="结束">
			<input type="button" class="ui-button" id="restartbutton" value="重新导入">
		</div>
	</div>
	
	<div class="areaencloser"></div>
	
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">货物到达验收确认单信息</span>
	</div>
	
	<div class="ui-widget-content">
		<table class="condform" id="content">
			<tbody>
				<tr>
					<td class="ui-state-default td-title">日期:</td>
					<td class="td-content"><span id="label_warehouse_date"></span></td>
					<td class="ui-state-default td-title">DN 编号:</td>
					<td class="td-content"><span id="label_dn_no"></span></td>
				</tr>
			</tbody>
		</table>
	</div>
	
	<div class="areaencloser"></div>
	
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">货物到达验收确认单信息一览</span>
		<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
			<span class="ui-icon ui-icon-circle-triangle-n"></span>
		</a>
	</div>
	<table id="receptlist" ></table>
	<div id="receptlistpager"></div>

</div>