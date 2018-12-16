<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="js/partial/partial_on_shelf.js"></script>
<div id="partial_on_shelf">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">零件上架一览</span>
		<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
			<span class="ui-icon ui-icon-circle-triangle-n"></span>
		</a>
	</div>
	<table id="onshelflist" ></table>
	<div id="onshelflistpager"></div>
	
	<div class="areaencloser"></div>
	
	<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase">
		<div style="margin-top: 6px;margin-left: 6px;">
			<input type="button" class="ui-button" id="startbutton" value="开始">
			<input type="button" class="ui-button" id="breakbutton" value="中断">
			<input type="button" class="ui-button" id="endbutton" value="结束">
		</div>
	</div>
	<div class="ui-widget-content" id="partial_details" style="display: none;">
		<table class="condform">
			<tr style="display: table-row;">
				<td class="ui-state-default td-title">开始时间</td>
				<td class="td-content-text"></td>
				<td class="ui-state-default td-title">作业标准时间</td>
				<td class="td-content-text"></td>
				<td class="ui-state-default td-title">作业经过时间</td>
				<td class="td-content-text" id="dtl_process_time"><label style="float:right;"></label></td>
			</tr>
			<tr>
				<td class="ui-state-default td-title">完成度</td>
				<td colspan="5" class="td-content-text slim">
					<div class="waiting tube" id="p_rate" style="height: 20px; margin: auto;"></div>
				</td>
			</tr>
		</table>
	</div>
	
	<div class="areaencloser"></div>
	
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">作业信息</span>
	</div>
	<div class="ui-widget-content" id="kind_quantity">
		<table class="condform">
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
	
	<div id="partial_warehouse_dialog" style="display: none;"></div>
	<input type="hidden" id="hide_key">
	
</div>