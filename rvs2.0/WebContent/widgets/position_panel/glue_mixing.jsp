<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<script type="text/javascript" src="js/inline/glue_mixing.js?v=2"></script>
<div id="glue_area" style="display:none;">
	<table class="condform">
		<tbody>
			<tr>
				<td class="ui-state-default td-title">胶水原材料物料号</td>
				<td class="td-content">
					<input type="hidden" id="glue_partial_id">
					<label id="glue_partial_code"></label>
				</td>
			</tr>
			<tr>
				<td class="ui-state-default td-title">有效期</td>
				<td class="td-content">
					<input type="text" id="expiration_date" class="ui-widget-content">
					<label id="label_expiration_date"></label>
				</td>
			</tr>
			<tr class="glue_mixing" style="display: none;">
				<td class="ui-state-default td-title">LOT NO.</td>
				<td class="td-content">
					<input type="text" id="lot_no" class="ui-widget-content">
				</td>
			</tr>
			<tr class="glue_mixing" style="display: none;">
				<td class="ui-state-default td-title">调制品名</td>
				<td class="td-content">
					<input type="text" id="binder_name" class="ui-widget-content">
				</td>
			</tr>
		</tbody>
	</table>
	<input type="hidden" id="hid_glue_mixing_process_id">
	<div class="glue_mixing">
		<input id="glueFinishButton" class="ui-button" type="button" value="完成" style="float:right;right:2px">
		<input id="glueContinueButton" class="ui-button" type="button" value="重开" style="float:right;right:2px">
		<input id="gluePauseButton" class="ui-button" type="button" value="暂停" style="float:right;right:2px">
		<input id="glueStartButton" class="ui-button" type="button" value="开始" style="float:right;right:2px">
	</div>
</div>