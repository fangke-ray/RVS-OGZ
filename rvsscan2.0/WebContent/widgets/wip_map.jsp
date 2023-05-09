<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<style>
.cage {
	margin: 2px; margin-top: -6px; float: left; 
}
.cage-first {
	margin: 2px; margin-top: 16px; float: left;
}
.cage td[wipid],
.cage-first td[wipid]
{
font-size:15px;
}
#storages > .cage,
#storages > .cage-first,
#endoeyeWIP > .cage,
#not_endoeyeWIP > .cage{
transform: scaleY(.9);
transform-origin: top;
}
#storages {
	height:610px;
}
#not_endoeyeWIP{
right: 248px;
position: absolute;
top: 228px;
transform: scaleX(.9);
transform-origin: right;
}
#endoeyeWIP {
right: 16px;
position: absolute;
top: 228px;
transform: scaleX(.9);
transform-origin: right;
}
#periWIP {
right: 16px;
position: absolute;
top: 428px;
transform: scaleX(.9);
transform-origin: right;
}
</style>

		<div class="cage-first">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架０</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="0-A1">1</td>
<td wipid="0-A2">2</td>
<td wipid="0-A3">3</td>
<td wipid="0-A4">4</td>
<td wipid="0-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="0-B1">1</td>
<td wipid="0-B2">2</td>
<td wipid="0-B3">3</td>
<td wipid="0-B4">4</td>
<td wipid="0-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="0-C1">1</td>
<td wipid="0-C2">2</td>
<td wipid="0-C3">3</td>
<td wipid="0-C4">4</td>
<td wipid="0-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="0-D1">1</td>
<td wipid="0-D2">2</td>
<td wipid="0-D3">3</td>
<td wipid="0-D4">4</td>
<td wipid="0-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="0-E1">1</td>
<td wipid="0-E2">2</td>
<td wipid="0-E3">3</td>
<td wipid="0-E4">4</td>
<td wipid="0-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="0-F1">1</td>
<td wipid="0-F2">2</td>
<td wipid="0-F3">3</td>
<td wipid="0-F4">4</td>
<td wipid="0-F5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
<td wipid="0-G1">1</td>
<td wipid="0-G2">2</td>
<td wipid="0-G3">3</td>
<td wipid="0-G4">4</td>
<td wipid="0-G5">5</td>
</tr>
			</table>
		</div>
		<div class="cage-first">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架１</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
					<td wipid="1-A1">1</td>
					<td wipid="1-A2">2</td>
					<td wipid="1-A3">3</td>
					<td wipid="1-A4">4</td>
					<td wipid="1-A5">5</td>
				</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
					<td wipid="1-B1">1</td>
					<td wipid="1-B2">2</td>
					<td wipid="1-B3">3</td>
					<td wipid="1-B4">4</td>
					<td wipid="1-B5">5</td>
				</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
					<td wipid="1-C1">1</td>
					<td wipid="1-C2">2</td>
					<td wipid="1-C3">3</td>
					<td wipid="1-C4">4</td>
					<td wipid="1-C5">5</td>
				</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
					<td wipid="1-D1">1</td>
					<td wipid="1-D2">2</td>
					<td wipid="1-D3">3</td>
					<td wipid="1-D4">4</td>
					<td wipid="1-D5">5</td>
				</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
					<td wipid="1-E1">1</td>
					<td wipid="1-E2">2</td>
					<td wipid="1-E3">3</td>
					<td wipid="1-E4">4</td>
					<td wipid="1-E5">5</td>
				</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
					<td wipid="1-F1">1</td>
					<td wipid="1-F2">2</td>
					<td wipid="1-F3">3</td>
					<td wipid="1-F4">4</td>
					<td wipid="1-F5">5</td>
				</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="1-G1">1</td>
					<td wipid="1-G2">2</td>
					<td wipid="1-G3">3</td>
					<td wipid="1-G4">4</td>
					<td wipid="1-G5">5</td>
				</tr>
			</table>
		</div>
		<div class="cage-first">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架２</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="2-A1">1</td>
<td wipid="2-A2">2</td>
<td wipid="2-A3">3</td>
<td wipid="2-A4">4</td>
<td wipid="2-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="2-B1">1</td>
<td wipid="2-B2">2</td>
<td wipid="2-B3">3</td>
<td wipid="2-B4">4</td>
<td wipid="2-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="2-C1">1</td>
<td wipid="2-C2">2</td>
<td wipid="2-C3">3</td>
<td wipid="2-C4">4</td>
<td wipid="2-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="2-D1">1</td>
<td wipid="2-D2">2</td>
<td wipid="2-D3">3</td>
<td wipid="2-D4">4</td>
<td wipid="2-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="2-E1">1</td>
<td wipid="2-E2">2</td>
<td wipid="2-E3">3</td>
<td wipid="2-E4">4</td>
<td wipid="2-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="2-F1">1</td>
<td wipid="2-F2">2</td>
<td wipid="2-F3">3</td>
<td wipid="2-F4">4</td>
<td wipid="2-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="2-G1">1</td>
					<td wipid="2-G2">2</td>
					<td wipid="2-G3">3</td>
					<td wipid="2-G4">4</td>
					<td wipid="2-G5">5</td>
				</tr>
			</table>
		</div>
		<div class="cage-first">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架３</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="3-A1">1</td>
<td wipid="3-A2">2</td>
<td wipid="3-A3">3</td>
<td wipid="3-A4">4</td>
<td wipid="3-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="3-B1">1</td>
<td wipid="3-B2">2</td>
<td wipid="3-B3">3</td>
<td wipid="3-B4">4</td>
<td wipid="3-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="3-C1">1</td>
<td wipid="3-C2">2</td>
<td wipid="3-C3">3</td>
<td wipid="3-C4">4</td>
<td wipid="3-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="3-D1">1</td>
<td wipid="3-D2">2</td>
<td wipid="3-D3">3</td>
<td wipid="3-D4">4</td>
<td wipid="3-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="3-E1">1</td>
<td wipid="3-E2">2</td>
<td wipid="3-E3">3</td>
<td wipid="3-E4">4</td>
<td wipid="3-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="3-F1">1</td>
<td wipid="3-F2">2</td>
<td wipid="3-F3">3</td>
<td wipid="3-F4">4</td>
<td wipid="3-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="3-G1">1</td>
					<td wipid="3-G2">2</td>
					<td wipid="3-G3">3</td>
					<td wipid="3-G4">4</td>
					<td wipid="3-G5">5</td>
				</tr>
			</table>
		</div>
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架４</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="4-A1">1</td>
<td wipid="4-A2">2</td>
<td wipid="4-A3">3</td>
<td wipid="4-A4">4</td>
<td wipid="4-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="4-B1">1</td>
<td wipid="4-B2">2</td>
<td wipid="4-B3">3</td>
<td wipid="4-B4">4</td>
<td wipid="4-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="4-C1">1</td>
<td wipid="4-C2">2</td>
<td wipid="4-C3">3</td>
<td wipid="4-C4">4</td>
<td wipid="4-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="4-D1">1</td>
<td wipid="4-D2">2</td>
<td wipid="4-D3">3</td>
<td wipid="4-D4">4</td>
<td wipid="4-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="4-E1">1</td>
<td wipid="4-E2">2</td>
<td wipid="4-E3">3</td>
<td wipid="4-E4">4</td>
<td wipid="4-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="4-F1">1</td>
<td wipid="4-F2">2</td>
<td wipid="4-F3">3</td>
<td wipid="4-F4">4</td>
<td wipid="4-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="4-G1">1</td>
					<td wipid="4-G2">2</td>
					<td wipid="4-G3">3</td>
					<td wipid="4-G4">4</td>
					<td wipid="4-G5">5</td>
				</tr>

		</table>
		</div>

		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架５</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="5-A1">1</td>
<td wipid="5-A2">2</td>
<td wipid="5-A3">3</td>
<td wipid="5-A4">4</td>
<td wipid="5-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="5-B1">1</td>
<td wipid="5-B2">2</td>
<td wipid="5-B3">3</td>
<td wipid="5-B4">4</td>
<td wipid="5-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="5-C1">1</td>
<td wipid="5-C2">2</td>
<td wipid="5-C3">3</td>
<td wipid="5-C4">4</td>
<td wipid="5-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="5-D1">1</td>
<td wipid="5-D2">2</td>
<td wipid="5-D3">3</td>
<td wipid="5-D4">4</td>
<td wipid="5-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="5-E1">1</td>
<td wipid="5-E2">2</td>
<td wipid="5-E3">3</td>
<td wipid="5-E4">4</td>
<td wipid="5-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="5-F1">1</td>
<td wipid="5-F2">2</td>
<td wipid="5-F3">3</td>
<td wipid="5-F4">4</td>
<td wipid="5-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="5-G1">1</td>
					<td wipid="5-G2">2</td>
					<td wipid="5-G3">3</td>
					<td wipid="5-G4">4</td>
					<td wipid="5-G5">5</td>
				</tr>
			</table>
		</div>
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架６</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="6-A1">1</td>
<td wipid="6-A2">2</td>
<td wipid="6-A3">3</td>
<td wipid="6-A4">4</td>
<td wipid="6-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="6-B1">1</td>
<td wipid="6-B2">2</td>
<td wipid="6-B3">3</td>
<td wipid="6-B4">4</td>
<td wipid="6-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="6-C1">1</td>
<td wipid="6-C2">2</td>
<td wipid="6-C3">3</td>
<td wipid="6-C4">4</td>
<td wipid="6-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="6-D1">1</td>
<td wipid="6-D2">2</td>
<td wipid="6-D3">3</td>
<td wipid="6-D4">4</td>
<td wipid="6-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="6-E1">1</td>
<td wipid="6-E2">2</td>
<td wipid="6-E3">3</td>
<td wipid="6-E4">4</td>
<td wipid="6-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="6-F1">1</td>
<td wipid="6-F2">2</td>
<td wipid="6-F3">3</td>
<td wipid="6-F4">4</td>
<td wipid="6-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="6-G1">1</td>
					<td wipid="6-G2">2</td>
					<td wipid="6-G3">3</td>
					<td wipid="6-G4">4</td>
					<td wipid="6-G5">5</td>
				</tr>
			</table>
		</div>
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架７</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="7-A1">1</td>
<td wipid="7-A2">2</td>
<td wipid="7-A3">3</td>
<td wipid="7-A4">4</td>
<td wipid="7-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="7-B1">1</td>
<td wipid="7-B2">2</td>
<td wipid="7-B3">3</td>
<td wipid="7-B4">4</td>
<td wipid="7-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="7-C1">1</td>
<td wipid="7-C2">2</td>
<td wipid="7-C3">3</td>
<td wipid="7-C4">4</td>
<td wipid="7-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="7-D1">1</td>
<td wipid="7-D2">2</td>
<td wipid="7-D3">3</td>
<td wipid="7-D4">4</td>
<td wipid="7-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="7-E1">1</td>
<td wipid="7-E2">2</td>
<td wipid="7-E3">3</td>
<td wipid="7-E4">4</td>
<td wipid="7-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="7-F1">1</td>
<td wipid="7-F2">2</td>
<td wipid="7-F3">3</td>
<td wipid="7-F4">4</td>
<td wipid="7-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="7-G1">1</td>
					<td wipid="7-G2">2</td>
					<td wipid="7-G3">3</td>
					<td wipid="7-G4">4</td>
					<td wipid="7-G5">5</td>
				</tr>
</table>
		</div>
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架８</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="8-A1">1</td>
<td wipid="8-A2">2</td>
<td wipid="8-A3">3</td>
<td wipid="8-A4">4</td>
<td wipid="8-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="8-B1">1</td>
<td wipid="8-B2">2</td>
<td wipid="8-B3">3</td>
<td wipid="8-B4">4</td>
<td wipid="8-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="8-C1">1</td>
<td wipid="8-C2">2</td>
<td wipid="8-C3">3</td>
<td wipid="8-C4">4</td>
<td wipid="8-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="8-D1">1</td>
<td wipid="8-D2">2</td>
<td wipid="8-D3">3</td>
<td wipid="8-D4">4</td>
<td wipid="8-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="8-E1">1</td>
<td wipid="8-E2">2</td>
<td wipid="8-E3">3</td>
<td wipid="8-E4">4</td>
<td wipid="8-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="8-F1">1</td>
<td wipid="8-F2">2</td>
<td wipid="8-F3">3</td>
<td wipid="8-F4">4</td>
<td wipid="8-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="8-G1">1</td>
					<td wipid="8-G2">2</td>
					<td wipid="8-G3">3</td>
					<td wipid="8-G4">4</td>
					<td wipid="8-G5">5</td>
				</tr>
			</table>
		</div>

<div id="not_endoeyeWIP">
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架９</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="9-A1">1</td>
<td wipid="9-A2">2</td>
<td wipid="9-A3">3</td>
<td wipid="9-A4">4</td>
<td wipid="9-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="9-B1">1</td>
<td wipid="9-B2">2</td>
<td wipid="9-B3">3</td>
<td wipid="9-B4">4</td>
<td wipid="9-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="9-C1">1</td>
<td wipid="9-C2">2</td>
<td wipid="9-C3">3</td>
<td wipid="9-C4">4</td>
<td wipid="9-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="9-D1">1</td>
<td wipid="9-D2">2</td>
<td wipid="9-D3">3</td>
<td wipid="9-D4">4</td>
<td wipid="9-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="9-E1">1</td>
<td wipid="9-E2">2</td>
<td wipid="9-E3">3</td>
<td wipid="9-E4">4</td>
<td wipid="9-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="9-F1">1</td>
<td wipid="9-F2">2</td>
<td wipid="9-F3">3</td>
<td wipid="9-F4">4</td>
<td wipid="9-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="9-G1">1</td>
					<td wipid="9-G2">2</td>
					<td wipid="9-G3">3</td>
					<td wipid="9-G4">4</td>
					<td wipid="9-G5">5</td>
				</tr>
			</table>
		</div>
 
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架10</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="10-A1">1</td>
<td wipid="10-A2">2</td>
<td wipid="10-A3">3</td>
<td wipid="10-A4">4</td>
<td wipid="10-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="10-B1">1</td>
<td wipid="10-B2">2</td>
<td wipid="10-B3">3</td>
<td wipid="10-B4">4</td>
<td wipid="10-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="10-C1">1</td>
<td wipid="10-C2">2</td>
<td wipid="10-C3">3</td>
<td wipid="10-C4">4</td>
<td wipid="10-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="10-D1">1</td>
<td wipid="10-D2">2</td>
<td wipid="10-D3">3</td>
<td wipid="10-D4">4</td>
<td wipid="10-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="10-E1">1</td>
<td wipid="10-E2">2</td>
<td wipid="10-E3">3</td>
<td wipid="10-E4">4</td>
<td wipid="10-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="10-F1">1</td>
<td wipid="10-F2">2</td>
<td wipid="10-F3">3</td>
<td wipid="10-F4">4</td>
<td wipid="10-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="10-G1">1</td>
					<td wipid="10-G2">2</td>
					<td wipid="10-G3">3</td>
					<td wipid="10-G4">4</td>
					<td wipid="10-G5">5</td>
				</tr>
			</table>
		</div>

</div>
<div id="endoeyeWIP">
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架11</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="11-A1">1</td>
<td wipid="11-A2">2</td>
<td wipid="11-A3">3</td>
<td wipid="11-A4">4</td>
<td wipid="11-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="11-B1">1</td>
<td wipid="11-B2">2</td>
<td wipid="11-B3">3</td>
<td wipid="11-B4">4</td>
<td wipid="11-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="11-C1">1</td>
<td wipid="11-C2">2</td>
<td wipid="11-C3">3</td>
<td wipid="11-C4">4</td>
<td wipid="11-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="11-D1">1</td>
<td wipid="11-D2">2</td>
<td wipid="11-D3">3</td>
<td wipid="11-D4">4</td>
<td wipid="11-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="11-E1">1</td>
<td wipid="11-E2">2</td>
<td wipid="11-E3">3</td>
<td wipid="11-E4">4</td>
<td wipid="11-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="11-F1">1</td>
<td wipid="11-F2">2</td>
<td wipid="11-F3">3</td>
<td wipid="11-F4">4</td>
<td wipid="11-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="11-G1">1</td>
					<td wipid="11-G2">2</td>
					<td wipid="11-G3">3</td>
					<td wipid="11-G4">4</td>
					<td wipid="11-G5">5</td>
				</tr>
			</table>
		</div>
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架12</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="12-A1">1</td>
<td wipid="12-A2">2</td>
<td wipid="12-A3">3</td>
<td wipid="12-A4">4</td>
<td wipid="12-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="12-B1">1</td>
<td wipid="12-B2">2</td>
<td wipid="12-B3">3</td>
<td wipid="12-B4">4</td>
<td wipid="12-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="12-C1">1</td>
<td wipid="12-C2">2</td>
<td wipid="12-C3">3</td>
<td wipid="12-C4">4</td>
<td wipid="12-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="12-D1">1</td>
<td wipid="12-D2">2</td>
<td wipid="12-D3">3</td>
<td wipid="12-D4">4</td>
<td wipid="12-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="12-E1">1</td>
<td wipid="12-E2">2</td>
<td wipid="12-E3">3</td>
<td wipid="12-E4">4</td>
<td wipid="12-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="12-F1">1</td>
<td wipid="12-F2">2</td>
<td wipid="12-F3">3</td>
<td wipid="12-F4">4</td>
<td wipid="12-F5">5</td>
</tr>
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ｇ</th>
					<td wipid="12-G1">1</td>
					<td wipid="12-G2">2</td>
					<td wipid="12-G3">3</td>
					<td wipid="12-G4">4</td>
					<td wipid="12-G5">5</td>
				</tr>
			</table>
		</div>
	</div>
<div id="periWIP">
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架13</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="13-A1">1</td>
<td wipid="13-A2">2</td>
<td wipid="13-A3">3</td>
<td wipid="13-A4">4</td>
<td wipid="13-A5">5</td>
<td wipid="13-A6">6</td>
<td wipid="13-A7">7</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="13-B1">1</td>
<td wipid="13-B2">2</td>
<td wipid="13-B3">3</td>
<td wipid="13-B4">4</td>
<td wipid="13-B5">5</td>
<td wipid="13-B6">6</td>
<td wipid="13-B7">7</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="13-C1">1</td>
<td wipid="13-C2">2</td>
<td wipid="13-C3">3</td>
<td wipid="13-C4">4</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="13-D1">1</td>
<td wipid="13-D2">2</td>
<td wipid="13-D3">3</td>
<td wipid="13-D4">4</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="13-E1">1</td>
<td wipid="13-E2">2</td>
<td wipid="13-E3">3</td>
<td wipid="13-E4">4</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="13-F1">1</td>
<td wipid="13-F2">2</td>
<td wipid="13-F3">3</td>
<td wipid="13-F4">4</td>
</tr>
			</table>
		</div>
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架14</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="14-A1">1</td>
<td wipid="14-A2">2</td>
<td wipid="14-A3">3</td>
<td wipid="14-A4">4</td>
<td wipid="14-A5">5</td>
<td wipid="14-A6">6</td>
<td wipid="14-A7">7</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="14-B1">1</td>
<td wipid="14-B2">2</td>
<td wipid="14-B3">3</td>
<td wipid="14-B4">4</td>
<td wipid="14-B5">5</td>
<td wipid="14-B6">6</td>
<td wipid="14-B7">7</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="14-C1">1</td>
<td wipid="14-C2">2</td>
<td wipid="14-C3">3</td>
<td wipid="14-C4">4</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="14-D1">1</td>
<td wipid="14-D2">2</td>
<td wipid="14-D3">3</td>
<td wipid="14-D4">4</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="14-E1">1</td>
<td wipid="14-E2">2</td>
<td wipid="14-E3">3</td>
<td wipid="14-E4">4</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="14-F1">1</td>
<td wipid="14-F2">2</td>
<td wipid="14-F3">3</td>
<td wipid="14-F4">4</td>
<td wipid="14-F5">5</td>
</tr>
			</table>
		</div>
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架15</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="15-A1">1</td>
<td wipid="15-A2">2</td>
<td wipid="15-A3">3</td>
<td wipid="15-A4">4</td>
<td wipid="15-A5">5</td>
<td wipid="15-A6">6</td>
<td wipid="15-A7">7</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="15-B1">1</td>
<td wipid="15-B2">2</td>
<td wipid="15-B3">3</td>
<td wipid="15-B4">4</td>
<td wipid="15-B5">5</td>
<td wipid="15-B6">6</td>
<td wipid="15-B7">7</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="15-C1">1</td>
<td wipid="15-C2">2</td>
<td wipid="15-C3">3</td>
<td wipid="15-C4">4</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="15-D1">1</td>
<td wipid="15-D2">2</td>
<td wipid="15-D3">3</td>
<td wipid="15-D4">4</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="15-E1">1</td>
<td wipid="15-E2">2</td>
<td wipid="15-E3">3</td>
<td wipid="15-E4">4</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="15-F1">1</td>
<td wipid="15-F2">2</td>
<td wipid="15-F3">3</td>
<td wipid="15-F4">4</td>
</tr>
			</table>
		</div>
		<div class="cage">
			<div class="ui-widget-header" style="width: 120px; text-align: center;">货架16</div>
			<table class="condform wip-table" style="width: 120px;">
				<tr>
					<th class="ui-state-default" style="width: 14px;">Ａ</th>
<td wipid="16-A1">1</td>
<td wipid="16-A2">2</td>
<td wipid="16-A3">3</td>
<td wipid="16-A4">4</td>
<td wipid="16-A5">5</td>
				</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｂ</th>
<td wipid="16-B1">1</td>
<td wipid="16-B2">2</td>
<td wipid="16-B3">3</td>
<td wipid="16-B4">4</td>
<td wipid="16-B5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｃ</th>
<td wipid="16-C1">1</td>
<td wipid="16-C2">2</td>
<td wipid="16-C3">3</td>
<td wipid="16-C4">4</td>
<td wipid="16-C5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｄ</th>
<td wipid="16-D1">1</td>
<td wipid="16-D2">2</td>
<td wipid="16-D3">3</td>
<td wipid="16-D4">4</td>
<td wipid="16-D5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｅ</th>
<td wipid="16-E1">1</td>
<td wipid="16-E2">2</td>
<td wipid="16-E3">3</td>
<td wipid="16-E4">4</td>
<td wipid="16-E5">5</td>
</tr>
<tr>
					<th class="ui-state-default" style="width: 14px;">Ｆ</th>
<td wipid="16-F1">1</td>
<td wipid="16-F2">2</td>
<td wipid="16-F3">3</td>
<td wipid="16-F4">4</td>
<td wipid="16-F5">5</td>
</tr>
			</table>
		</div>
</div>

		<div class="clear"></div>

	<!--div class="clear areaencloser"></div-->