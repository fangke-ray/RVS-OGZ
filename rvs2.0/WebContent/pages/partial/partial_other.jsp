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
				<tr>
					<td class="ui-state-default td-title">常用作业:</td>
					<td class="td-content" id="comment_keyboard">
<input type="button" value="零件出库：">
<input type="button" value="中小修">
<input type="button" value="周边设备工程">
<input type="button" value="消耗品">
<input type="button" value="EndoEye">
<input type="button" value="总组NS出库">
<input type="button" value="其它">
<hr>
<input type="button" value="入库管理：">
<input type="button" value="乙材入库">
<input type="button" value="EndoEye入库">
<hr>
<input type="button" value="辅助业务：">
<input type="button" value="收缩管切割">
<input type="button" value="零件清洗">
<input type="button" value="四楼仓库溶液领取">
<input type="button" value="废弃零件处理">
<input type="button" value="点检+镜头纸库存补充">
<input type="button" value="BO零件对应">
<input type="button" value="《零件出库指示单》作成">
<input type="button" value="纸盒类零件拆包">
<input type="button" value="螺丝分装">
<input type="button" value="零件订购">
<hr>
<input type="button" value="零件管理：">
<input type="button" value="盘点">
<input type="button" value="新制品对应">
<input type="button" value="有效日期确认">
<hr>
<input type="button" value="5S管理：">
<input type="button" value="废弃纸皮处理">
<input type="button" value="其它">
<hr>
<input type="button" value="乙材">
<input type="button" value="螺丝">
<input type="button" value="胶水">
<input type="button" value="消耗品">
<input type="button" value="订购牌">
<input type="button" value="实物">
<input type="button" value="冰箱">
<input type="button" value="干燥箱">
<input type="button" value="温湿度">
<input type="button" value="镜头纸">
<input type="button" value="溶液">
<input type="button" value="酒精">
<input type="button" value="阿波罗液">
<input type="button" value="台车">
<input type="button" value="空瓶">
<input type="button" value="收货">
<input type="button" value="搬运">
<input type="button" value="开箱">
<input type="button" value="核对">
<input type="button" value="上架">
<input type="button" value="拿取">
<input type="button" value="移动">
<input type="button" value="信息记录">
<input type="button" value="捆绑">
<input type="button" value="点检">
<input type="button" value="推动">
<input type="button" value="收集">
<input type="button" value="补充">
<input type="button" value="放置">
<br>
<input type="button" value="到实物">
<input type="button" value="消耗品区域">
<input type="button" value=" " style="width: 436px;">
<input type="button" value="换行" style="color: darkgray;">
<input type="button" value="总务仓库">
<input type="button" value="到指定位置">
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