<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="js/partial/partial_other.js"></script>
<div id="partial_other">
	<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser">
		<span class="areatitle">作业信息</span>
	</div>
	<div class="ui-widget-content">
		<table class="condform">
			<tbody>
				<tr>
					<td class="ui-state-default td-title">备注内容:</td>
					<td class="td-content">
						<textarea id="comment" style="resize:none" rows="4" cols="130" class="ui-widget-content"></textarea>
					</td>
				</tr>
			</tbody>
		</table>
	</div>

	<div class="areaencloser"></div>

	<div class="ui-widget-header ui-corner-all ui-helper-clearfix areabase">
		<div style="margin-top: 6px;margin-left: 6px;">
			<input type="button" class="ui-button" id="startbutton" value="开始">
			<input type="button" class="ui-button" id="endbutton" value="结束">
		</div>
	</div>
	
</div>