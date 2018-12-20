<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="js/partial/partial_out_storage.js"></script>
<div id="partial_out_storage">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">零件待出库一览</span>
		<a role="link" href="javascript:void(0)" class="HeaderButton areacloser">
			<span class="ui-icon ui-icon-circle-triangle-n"></span>
		</a>
	</div>
	<table id="outlist" ></table>
	<div id="outlistpager"></div>
	
	<div class="areaencloser"></div>
	
	<div class="ui-widget-content" id="scanner_container" style="min-height: 215px;" >
		<div class="ui-state-default td-title">扫描录入区域</div>
		<input type="text" id="scanner_inputer" title="扫描前请点入此处" class="scanner_inputer" style="width: 99.5%"></input>
		<div style="text-align: center;">
			<img src="images/barcode.png" style="margin: auto; width: 150px; padding-top: 4px;">
		</div>
	</div>
	
	<div id="result" style="display: none;">
		<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
			<span class="areatitle">作业信息</span>
		</div>
		
		<div class="ui-widget-content">
			<table class="condform">
				<tbody>
					<tr>
						<td class="ui-state-default td-title">修理单号</td>
						<td class="td-content"><span id="label_omr_notifi_no"></span></td>
						<td class="ui-state-default td-title">工程</td>
						<td class="td-content"><span id="label_line_name"></span></td>
						<td class="ui-state-default td-title">等级</td>
						<td class="td-content"><span id="label_level_name"></span></td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">订购日期</td>
						<td class="td-content"><span id="label_order_date"></span></td>
						<td class="ui-state-default td-title">零件BO</td>
						<td class="td-content"><span id="label_bo_flg_name"></span></td>
						<td class="ui-state-default td-title">零件缺品备注</td>
						<td class="td-content"><span id="label_bo_contents"></span></td>
					</tr>
				</tbody>
			</table>
		</div>
		
		<div class="areaencloser"></div>
		
		<div class="ui-widget-content" id="partial_details">
			<table class="condform">
				<tbody>
					<tr>
						<td class="ui-state-default td-title">开始时间</td>
						<td class="td-content"></td>
						<td class="ui-state-default td-title">作业标准时间</td>
						<td class="td-content"></td>
						<td class="ui-state-default td-title">作业经过时间</td>
						<td class="td-content" id="dtl_process_time"><label style="float:right;"></label></td>
					</tr>
					<tr>
						<td class="ui-state-default td-title">完成度</td>
						<td colspan="5" class="td-content slim">
							<div class="waiting tube" id="p_rate" style="height: 20px; margin: auto;"></div>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
		
		<div class="areaencloser"></div>
		
		<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase">
			<div style="margin-top: 6px;margin-left: 6px;">
				<input type="button" class="ui-button" id="endbutton" value="结束">
			</div>
		</div>
	</div>
	

		
	<div class="areaencloser"></div>
	
	<input type="hidden" id="hide_fact_pf_key">
	
</div>