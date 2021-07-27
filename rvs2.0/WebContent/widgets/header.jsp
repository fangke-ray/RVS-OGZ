<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/lte-style.css">
<script type="text/javascript" src="js/common/message_detail.js?version=110"></script>

<style>
.menulink {
	font-size: 16px;
	color: white;
	float: right;
	right: 4px;
	margin: 4px;
}

.menulink:hover {
	color: #FFB300;
	cursor: pointer;
}

.menulink.icon-calendar-2 {
	position:relative;
}

.menulink.icon-calendar-2 .expland-menu {
	position:absolute;
background-color: #93c3cd;
/*background-image: linear-gradient(45deg, transparent 50%, rgba(255,255,255,.5) 50%);
background-size: 10px 60px;*/
	z-index: 100;
}

.menulink.icon-calendar-2 .expland-menu > div {
	float: left;
}

.littleball {
	font-size: 10px;
	-moz-border-radius-topleft: 8px;
	-webkit-border-top-left-radius: 8px;
	-khtml-border-top-left-radius: 8px;
	border-top-left-radius: 8px;
	-moz-border-radius-bottomleft: 8px;
	-webkit-border-bottom-left-radius: 8px;
	-khtml-border-bottom-left-radius: 8px;
	border-bottom-left-radius: 8px;
	-moz-border-radius-topright: 8px;
	-webkit-border-top-right-radius: 8px;
	-khtml-border-top-right-radius: 8px;
	border-top-right-radius: 8px;
	-moz-border-radius-bottomright: 8px;
	-webkit-border-bottom-right-radius: 8px;
	-khtml-border-bottom-right-radius: 8px;
	border-bottom-right-radius: 8px; //
	width: 180px;
	height: 18px;
	text-align: center; //
	background-color: green;
	padding: 1px;
}

.littleball.blinking {
	background-color: #FFB300;
	display: inline-block;
	width:1.4em;
	height:1.4em;
}

</style>

	<div class="areabase">
		<img src="images/logo-rvs.png?v=2021" style="margin-top: 7px; float: left"></img>
		<div>
			<div class="menulink icon-switch">退出</div>
			<div class="menulink icon-home">首页
			<input type="hidden" id="op_id" value="${userdata.operator_id}"/>
			<input type="hidden" id="submenu" value="${retSub}"/>
			</div>
			<div class="menulink icon-help">查询</div>
			${(department eq 1) ? '<div class="menulink icon-list">点检</div>' : ''}
			${(retPartialLink eq "1" and department eq 1) ? '<div class="menulink icon-cog">零件</div>' : ''}
			${(needMenu eq "1") ? '<div class="menulink icon-calendar-2">菜单<div class="expland-menu ui-accordion" id="accordion"></div></div>' : ''}
			<div style="height:29px;line-height:29px;float:right;margin-right: 16px;">
				<span style="color:white;font-size:14px;">您好! ${userdata.name}${userdata.role_name}。</span>
				<span style="color:white;font-size:14px;cursor:pointer;" id="userPosition">${userPosition}</span>
			</div>
			${(needMessageBox eq "1") ? '<div class="clear menulink icon-bell">提示信息<span class="littleball">0</span>条</div>' : ''}

		</div>
	</div>
	<div class="clear" style="height: 10px;"></div>

	<div class="hidemessage" id="hidemessage">
		<div id="messagecontainner" style="float: left;display: none;">
			<div id="messagearea">
				<div class="ui-widget-header ui-corner-top ui-helper-clearfix areaencloser dwidth-right">
					<span class="areatitle">消息一览</span>
				</div>
				<div class="ui-state-default dwidth-right" id="message_contents">
				</div>
				<div class="clear"></div>
			</div>
		</div>
	</div>
	<div id="process_resign"/>
<script type="text/javascript">
var headerServicePath = "header.do";
var selectedMaterial = {};
var header_belongs = {};
var header_holidays = ${header_holidays};
var header_today_holiday = ${today_holiday};
var header_getInholidays = function(month){
	if (month)
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : 'holiday.do?method=getHolidays',
			cache : false,
			data : {month : month},
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhjObj) {
				var resInfo = $.parseJSON(xhjObj.responseText);
				header_holidays[month] = resInfo.signed;
			}
		});
};

var closePostMessage = function(post_message_id){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : headerServicePath + '?method=doReadPostMessage',
		cache : false,
		data : {post_message_id : post_message_id.replace("p_", "")},
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : getMessage_complete
	});
};

$("#message_contents").on("mouseenter", "a.areacloser", 
	function() {$(this).addClass("ui-state-hover");}
);
$("#message_contents").on("mouseleave", "a.areacloser", 
	function() {$(this).removeClass("ui-state-hover");}
);
$("#message_contents").on("click", "a.areacloser", 
	function() {
		var alarm_messsage_id = $(this).attr("refid");
		if (alarm_messsage_id.indexOf("p") == 0) {
			closePostMessage(alarm_messsage_id);
		} else {
			popMessageDetail(alarm_messsage_id, true);
		}
	}
);
$("#message_contents").on("mouseenter", "div > .m_title", function(){
	$(this).next().show("blind");
});

$("#message_contents").mouseleave(function(){
	$("#message_contents > div > .m_content").hide("blind");
});

var alarm_counts = 0; // -1
var getMessage_complete = function(xhrobj, textStatus) {
	var resInfo = null;
	try {
		// 以Object形式读取JSON
		eval('resInfo =' + xhrobj.responseText);
		$(".littleball").text(resInfo.alarm_counts);
		var new_alarm_counts = parseInt(resInfo.alarm_counts)
		if (new_alarm_counts > alarm_counts) {
			// if (alarm_counts >= 0 || new_alarm_counts > 8)
				$(".littleball").addClass("blinking");
		}
		alarm_counts = new_alarm_counts;

		var alarm_show_counts = resInfo.alarms.length;

		if (alarm_show_counts === 0) {
			$("#message_contents").html("<span>没有未处理的警告。</span>");
		} else {
			$("#message_contents").html(resInfo.alarms);
			$("#message_contents > div > .m_content").hide();
		}
	} catch (e) {
		alert("name: " + e.name + " message: " + e.message + " lineNumber: "
				+ e.lineNumber + " fileName: " + e.fileName);
	};
};

var refreshMes = function(){
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : headerServicePath + '?method=getMessage',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : getMessage_complete
	});
}

$(function() {
	$(".areabase .icon-switch").click(function() {
		window.location.href = "login.do?method=logoff";
	});
	$(".areabase .icon-home").click(function() {
		window.location.href = "panel.do";
	});
	$(".areabase .icon-help").click(function() {
		window.location.href = "material.do";
	});
	if ($(".areabase .icon-calendar-2").length > 0) {
		$(".areabase .icon-calendar-2").click(function() {
			if (!$(".expland-menu").html()) {
				var $explandMenu = $(".expland-menu");
				$explandMenu.load("appmenu.do?method=" + ($("#submenu").val() || "") + "init&ex=true", function(){
					var $explandMenuItem = $explandMenu.children("div");
					var menu_count = $explandMenuItem.length;

					if (menu_count >= 4){
						$explandMenu.css({
						left: "-560px",
						width: "896px"
						})
					} else if (menu_count == 3){
						$explandMenu.css({
						left: "-336px",
						width: "672px"
						})
					} else if (menu_count == 2){
						$explandMenu.css({
						left: "-112px",
						width: "448px"
						})
					}
					
					var maxHeight = 0;
					for (var iM = 0;iM < menu_count;iM++ ) {
						var tHeight = $explandMenuItem.eq(iM).children("div").height();
						if (tHeight > maxHeight) {
							maxHeight = tHeight;
						}
						if (iM % 4 == 3 || iM == menu_count - 1) {
							var iN = iM - iM % 4;
							while (iN <= iM) {
								$explandMenuItem.eq(iN).children("div").css("height", maxHeight);
								iN++;
							}
							maxHeight = 0;
						}
					};
					$explandMenu.css("display", "none");
					$explandMenu.toggle("fold");
				});
			} else {
				$(".expland-menu").toggle("fold");
			}
		});
	}
	$(".areabase .icon-bell, #messagearea div:first").click(function() {
		$(".littleball").removeClass("blinking");
		$("#messagecontainner").toggle("fold");
	});
	$(".areabase .icon-cog").click(function() {
		window.location.href = "header.do?method=pinit";
	});
	$(".areabase .icon-list").click(function() {
		window.location.href = "header.do?method=iinit";
	});

	$("#userPosition").click(function(){
		var txt = $(this).text();
		if (txt && txt.indexOf("工位") >= 0) {
			window.location.href = "position_panel.do";
		}
	});

	refreshMes();
});
</script>



<script type="text/javascript">
var operator_ws = null;

$(function() {
	if (typeof (WebSocket) == "undefined") {
		return;
	}

	try {

	// 创建WebSocket  
	operator_ws = new WebSocket(wsPath + "/operator");
	// 收到消息时在消息框内显示  
	operator_ws.onmessage = function(evt) {
    	var resInfo = {};
    	try {
    		eval("resInfo=" + evt.data);
    		if ("conrrupted" == resInfo.method) {
    			var allowed = false;
    			var errorData = "RVS系统不支持单个用户在多窗口工作，请保持只有一个窗口登录。";
				if ($('div#errstring').length == 0) {
					$("body").append("<div id='errstring'/>");
				}
				$('div#errstring').show();
				$('div#errstring').dialog({dialogClass : 'ui-error-dialog', modal : true, width : 450, title : "在线冲突", 
					buttons : {"重新连接" : function() { allowed = true; $(this).dialog("close"); operator_ws.send("entach:"+$("#op_id").val() + ($("#ro_mt_id").val() ? ("+" + $("#ro_mt_id").val()) : ""));},
						"退出登录" : function() { $(this).dialog("close"); }},
					close : function() { if(!allowed) window.location.href = "login.do?method=logoff" }});
				$('div#errstring').html("<span class='errorarea'>" + errorData + "</span>");
    		} else if ("connectted" == resInfo.method) {
    			if (resInfo.belongs) {
    				header_belongs = resInfo.belongs;
    			}
    		} else if ("ping" == resInfo.method) {
    			operator_ws.send("pong:"+resInfo.id + "+" + operator_ws.readyState);
    		} else if ("message" == resInfo.method) {
    			refreshMes();
    		}
    	} catch(e) {
    	}
        // $('#msgBox').append(evt.data);  
        // $('#msgBox').append('</br>');  
	};  
	// 断开时会走这个方法  
	operator_ws.onclose = function() {   
	};  
	// 连接上时走这个方法  
	operator_ws.onopen = function() {     
		operator_ws.send("entach:"+$("#op_id").val() + ($("#ro_mt_id").val() ? ("+" + $("#ro_mt_id").val()) : ""));
	}; 

	} catch(e) {
	}
});
</script>

	</div>


