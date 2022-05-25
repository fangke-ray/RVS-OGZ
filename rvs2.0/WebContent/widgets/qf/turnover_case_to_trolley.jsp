<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<style>
#tc2t_initials .initial{
	margin-right: 1em;
	padding: 0 0.1em;
	cursor:pointer;
	user-select:none;
	display:inline;
}
#tc2t_initials .initial.checked {
	background-color:darkblue;
	color:white;
	border-radius: 0.4em;
}
#tc2t_waitings, #tc2t_trolleys, #tc2t_temp_location {
	float : left;
}
#tc2t_waitings {
	height : 560px;
	overflow-y : scroll;
	width : 60%;
	position:relative;
	padding-left: 2px;
}
#tc2t_waitings .model_group {
	clear: both;
}
#tc2t_waitings .model_group > span.model_name {
	float: none;
	display: block;
}
#turnover_case_to_trolley .material {
	border: 1px;
	background-color:lightcyan;
	width : 12.5em;
	height : 3em;
	margin-right: 0.8em;
	margin-bottom: 0.2em;
	text-align:center;
	overflow-x: clip;
}
#tc2t_waitings .material {
	float:left;
}
#turnover_case_to_trolley .material[model_kind="1"] {
	background-color:lightgreen;
}
#turnover_case_to_trolley .material[model_kind="2"] {
	background-color:#FFD280;
}
#turnover_case_to_trolley .material[model_kind="4"] {
	background-color:#FFFF80;
}
#turnover_case_to_trolley .material[model_kind="6"] {
	background-color:lightcyan;
}
#tc2t_waitings .material.warehouse {
	background-color:gainsboro;
}
#turnover_case_to_trolley .material[agreed_date] {
	outline : 2px solid darksalmon;
}

#turnover_case_to_trolley .material span {
	background-color:darkcyan;
	color:white;
	padding: 0 0.5em;
	white-space: nowrap;
}
#tc2t_waitings .material.warehouse span {
	background-color: #2E5C5C;
}
#tc2t_trolleys {
	border : 2px solid black;
	height : 560px;
	width : 20%;
}

#tc2t_trolleys .trolley_cart {
	display: none;
	border-left: 4px solid black;
	margin-left: 1.2em;
	border-bottom: 4px solid black;
}
#tc2t_trolleys .trolley_cart.disp {
	display : block;
}
#tc2t_trolleys #trolley_sel > span {
	background: lightblue;
	width: 4.2em;
	display: block;
	float: left;
	text-align: center;
	border-top-left-radius: .5em;
	border-top-right-radius: .5em;
	cursor:pointer;
}
#tc2t_trolleys #trolley_sel > span[kind="1"] {
	background-color:lightgreen;
}
#tc2t_trolleys #trolley_sel > span[kind="2"] {
	background-color:#FFD280;
}
#tc2t_trolleys #trolley_sel > span[kind="4"] {
	background-color:#FFFF80;
}
#tc2t_trolleys #trolley_sel > span[kind="6"] {
	background-color:lightcyan;
}
#tc2t_trolleys #trolley_sel > .selected {
	font-weight: bolder;
	color:brown;
}
#tc2t_trolleys #trolley_sel > .selected:before {
	content: '★';
	top:0;
}
#tc2t_trolleys #trolley_area {
	clear:both;
}
#tc2t_trolleys .trolley_stock {
	background: lightgray;
	width: 80%;
	height: 4em;
	margin-left: 20px;
	margin-bottom: 0.5em;
	position: relative;
}
#tc2t_trolleys .trolley_stock:before {
	width: 100%;
	height: 100%;
	content: attr(stock);
	display: block;
	text-align: center;
	line-height: 1.5em;
	font-size: 2.5em;
	text-align: left;
	left: 1em;
	padding-left: .5em;
}
#tc2t_trolleys .trolley_cart > .trolley_stock:nth-child(n+10):before {
	padding-left: 0;
}
#tc2t_trolleys .material {
	position:absolute;
	top: 6px;
	right: 0;
}
#tc2t_temp_location {
	border : 2px solid black;
	height : 560px;
	width : 18%;
}
#tc2t_temp_location .location {
    background: lightgray;
    width: 80%;
    height: 1em;
    margin-left: 20px;
    margin-bottom: 0.2em;
    position: relative;
    text-align: center;
    font-size: 2.5em;
    padding-bottom: 0.45em;
    padding-top: 0.15em;
}
#tc2t_temp_location button {
	width: 100px;
	height: 20px;
	left: 48px;
}

</style>
<div id="turnover_case_to_trolley">
	<div id="tc2t_initials">
	</div>
	<div id="tc2t_waitings">
	</div>
	<div id="tc2t_trolleys">
	</div>
	<div id="tc2t_temp_location">
		<button class="ui-button" value="分配库位">分配库位</button>
		<div class="location" stock="1"></div>
		<div class="location" stock="2"></div>
		<div class="location" stock="3"></div>
		<div class="location" stock="4"></div>
		<div class="location" stock="5"></div>
		<div class="location" stock="6"></div>
		<div class="location" stock="7"></div>
		<div class="location" stock="8"></div>
		<div class="location" stock="9"></div>
		<div class="location" stock="10"></div>
	</div>
</div>