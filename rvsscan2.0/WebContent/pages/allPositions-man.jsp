<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no" name="viewport">
<meta name="apple-mobile-web-app-capable" content="yes" />
<link rel="stylesheet" type="text/css" href="css/scan/allPositions.css">

<script type="text/javascript" src="js/utils.js"></script>

<!--script type="text/javascript" src="js/scan/all_positions.js"></script-->

<title>全工位展示</title>
</head>
<body class="outer scan1024" style="color: black;">
	<div class="width-full" style="align: center; margin: 0 auto; margin-top: 4px;">

		<div id="wiparea" class="ui-widget-panel" style="width:100%;height:670px; position: relative;">
			<div class="line choosed" style="height:150px;width:320px;left: 300px;top: 50px;" id="line_bx" beforeOf="line_dec" group="line_bx">
				<div class="posi" id="posi_001" style="left: 10px; top: 80px;">001</div>

				<div class="posi" id="posi_002" style="left: 10px; top: 40px;">002</div>

				<div class="posi" id="posi_003" style="left: 50px; top: 40px;">003</div>

				<div class="posi" id="posi_004" style="left: 90px; top: 40px;">004</div>

				<div class="posi" id="posi_005" style="left: 130px; top: 40px;">005</div>

				<div class="posi" id="posi_006" style="left: 170px; top: 40px;">006</div>

				<div class="posi" id="posi_007" style="left: 210px; top: 40px;">007</div>

				<div class="posi" id="posi_008" style="left: 250px; top: 40px;">008</div>

				<div class="posi" id="posi_009" style="left: 250px; top: 80px;">009</div>

				<div class="banner" for="line_bx">BX 本体</div>

			</div>

			<div id="showline" class="line choosed" style="left: 10px; top: 410px; width: 998px; height: 242px;overflow:hidden;">

				<div class="scrollline" for="line_bx" style="position: relative;">
					<div class="position gridX3 gridY3" for="posi_001">001<br>部组</div>
					<div class="position-f gridX3 gridFY3" for="posi_001"></div>

					<div class="position gridX3 gridY1" for="posi_002">002<br>总组1</div>
					<div class="position-f gridX3 gridFY1" for="posi_002"></div>

					<div class="position gridX4 gridY1" for="posi_003">003<br>总组2</div>
					<div class="position-f gridX4 gridFY1" for="posi_003"></div>

					<div class="position gridX5 gridY1" for="posi_004">004<br>总组3</div>
					<div class="position-f gridX5 gridFY1" for="posi_004"></div>

					<div class="position gridX6 gridY1" for="posi_005">005<br>总组4</div>
					<div class="position-f gridX6 gridFY1" for="posi_005"></div>

					<div class="position gridX7 gridY1" for="posi_006">006<br>总组5</div>
					<div class="position-f gridX7 gridFY1" for="posi_006"></div>

					<div class="position gridX8 gridY1" for="posi_007">007<br>QC</div>
					<div class="position-f gridX8 gridFY1" for="posi_007"></div>

					<div class="position gridX9 gridY1" for="posi_008">008<br>QA</div>
					<div class="position-f gridX9 gridFY1" for="posi_008"></div>

					<div class="position gridX9 gridY3" for="posi_009">009<br>捆包</div>
					<div class="position-f gridX9 gridFY3" for="posi_009"></div>

					<div class="banner" for="line_bx" style="font-size:18px;padding: 8px 10px;z-index:22;">BX 本体流水线状况</div>
				</div>

			</div>

<marquee scrollamount="2" scrollspeed="10" class="posi" style="-webkit-animation: none;animation: none; height: 26px; overflow-x: hidden; position: absolute; 
	bottom: -18px; left: 10px; width: 998px; border-radius:0;font-size :15px;" bgcolor="#EFEFEF">
</marquee>

			<div class="line-addin" style="height: 96px; width: 128px; left: 808px; top: 284px;" id="tuli">
				<div class="adit" style="position: relative;height: 72px;width: 22px;padding-left:4px;padding-top: 24px;">图例</div>
				<div class="position posi-free" style="animation: none; top: 2px; left: 35px;width: 32px;">现无仕挂</div>
				<div class="position posi-noml" style="animation: none; top: 2px; left: 85px;width: 32px;">正常进行</div>
				<div class="position posi-over" style="animation: none; top: 50px; left: 35px;width: 32px;">仕挂超限</div>
				<div class="position posi-erro" style="animation: none; top: 50px; left: 85px;width: 32px;">发生不良</div>
			</div>

		</div>
		<div class="clear areacloser"></div>
	</div>
</body>
<div id="alert_float" style="position:fixed;"></div>
<script type="text/javascript">

var iamreadyAp = function() {

var rollTimer = null;

var servicePath = "allPositions.scan";

var scrollWidth = 998;

var refresh_position = function(xhrObj) {
	var resInfo = null;

	try {
		eval("resInfo=" + xhrObj.responseText);

		$("div.posi").removeAttr("info");
		$("div.posi").removeAttr("alarm");

		for (var position in resInfo.positions) {
			var thisPosition = resInfo.positions[position];
			var status = thisPosition.status;
			$("#posi_" + position).attr("info", status);
			if (thisPosition.alarm) {
				$("#posi_" + position).attr("alarm", thisPosition.alarm);
			}

			$("div[for=posi_" + position + "]").attr("info", status);

			$(".position-f[for=posi_" + position + "]").html(getHeaps(thisPosition) + "<br>今日: "
				+thisPosition.today_work+"台<br>Avg: "+thisPosition.avg_cost+"mins");

			$("#posi_" + position).find(".position_bo").text(thisPosition.countm == null ? "" : thisPosition.countm);
			$(".position[for=posi_" + position + "]").find(".position_bo").text(thisPosition.countm == null ? "" : thisPosition.countm);
			if (thisPosition.alarm) {
				$(".position[for=posi_" + position + "]").attr("alarm", thisPosition.alarm);
			}
		}

		$("div.posi:not([info])").each(function() {
			$(this).attr("info", "free");
			$("div[for=" + this.id + "]").attr("info", "free");
		});

		$("#wipsize").text(resInfo.wipsize);
		$("#inlinesize").text(resInfo.inlinesize);
		$("#shippingsize").text(resInfo.shippingsize);
	} catch(e) {
	}
}

var getHeaps = function(thisPosition){
	return "仕挂: "+thisPosition.heaps+"/"+(thisPosition.overline==0 ? "-" : thisPosition.overline);
}

var refresh = function(){
	// Ajax提交
	$.ajax({
	async : true,
		url : servicePath + '?method=refresh',
		cache : false,
		data : null,
		type : "post",
		dataType : "json",
		complete : refresh_position
	});	
}

var showMessage = function() {
	var allmessage = "";

	var choosedFor = $(".line.choosed").attr("id");
	console.log("choosedFor" + choosedFor);
	$(".scrollline[for=" + choosedFor + "] div[alarm]").each(function() {
		allmessage += $(this).attr("alarm") + "\t\t";
	});
	if(allmessage.length > 0) {
		allmessage = "发生中警报一览：" + allmessage;
		$("marquee").text(allmessage).addClass("posi-erro")
		.css("text-shadow", "none").css("font-weight", "bold").show();
	} else {
		$("marquee").hide();
//		$("marquee").text("欢迎使用RVS系统，目前本工程无异常。").removeClass("posi-erro")
//		.css("color", "black").css("font-weight", "normal");
	}
}
var roll = function(animtime){
	$(".scrollline").animate({
		'left' : '-=' + scrollWidth
	}, animtime, function() {
		$(".scrollline").each(function(idx) {
			var leftpx = parseInt($(this).css("left").replace("px", "") ,10);
			if (leftpx <= -(totalWidth - scrollWidth)) {
				$(this).css("left", scrollWidth + "px");
			}
		});
	});
	$("#alert_float").hide("fade");
	clearTimeout(fadeTO);

}
var autorolling = function(){
	roll(1000);

	var beforeOf = $(".choosed[group]").removeClass("choosed").attr("beforeOf");
	$(".line[group='"+beforeOf+"']").addClass("choosed");

	showMessage();

	var choosedID = $(".choosed")[0].id;
	if(choosedID == "line10" || choosedID == "line11"){
		rollTimer = setTimeout(autorolling, 45000);
	} else {
		rollTimer = setTimeout(autorolling, 30000);
	}
}

var getMinutes = function(seconds) {
	if (seconds) {
		var minutes = parseInt(seconds / 60);
		var hours = parseInt(minutes / 60);
		minutes -= hours * 60;
		var ret = "";
		if (hours > 0) {
			ret += (hours + "时");
		}
		if (minutes > 0) {
			ret += (minutes + "分");
		}
		return ret;
	}
	return "";
}

var fadeTO = null;
var setAlarmDetail = function(xhrObj, p_left, p_top){
	var resInfo = null;

	try {
		eval("resInfo=" + xhrObj.responseText);
	} catch(e) {
		console.log("setAlarmDetail Error:" + e.message);
	}

	if (resInfo == null) return;

	clearTimeout(fadeTO);

	// html
	var poHtml = "<table id='alert_box'>";
	if (resInfo.balarms) {
		for (var ii=0; ii < resInfo.balarms.length;ii++) {
			var alarm = resInfo.balarms[ii];
			poHtml += "<tr class='af_bo'><td>" + alarm.order_date + "</td>";
			poHtml += "<td>" + alarm.sorc_no + "</td></tr>";
		}
	}
	if (resInfo.alarms) {
		for (var ii=0; ii < resInfo.alarms.length;ii++) {
			var alarm = resInfo.alarms[ii];
			poHtml += "<tr class='af_erro'><td>" + alarm.start_time + "(" + getMinutes(alarm.seconds) + ")" + "</td>";
			poHtml += "<td>" + alarm.material_name + "</td></tr>";
		}
	}
	poHtml += "</table>";

	$("#alert_float").html(poHtml);

	$("#alert_float").css({"top" : (p_top + 100) + "px", "left" : (p_left + 100) + "px"}).show("fade");

	fadeTO = setTimeout(function(){$("#alert_float").hide("fade");} ,5000);
};

var totalWidth = 0;

var initScrollPosition = function(){

	var $scrolllines = $(".scrollline");
	var slLength = $scrolllines.length;
	totalWidth = scrollWidth * slLength;
	for (var i = 0; i < slLength; i++ ) {
		$scrolllines.eq(i).css("left", scrollWidth * i);
	}

		
}


$(function () {
$(".adit-c").text("");

initScrollPosition();

refresh();
// rollTimer = setTimeout(autorolling, 30000);

showMessage();

setInterval(refresh, 20000);

var lineClick = function(evt){
	evt.cancelBubble = true;
	var jthis = $(this);
	if(jthis.is(".choosed")) return;
	var group = jthis.attr("group");
	if (group == null) return;
	if ($(".scrollline:animated").length > 0) return;

	clearTimeout(rollTimer);
	$("#showline .scrollline > .banner").removeClass("locked");

	var toscroll = $(".scrollline[for="+ group +"]");
	var toidx = ((parseInt(toscroll.css("left")) + totalWidth) % totalWidth) / scrollWidth; // $(".scrollline").index(toscroll);
	if (toidx) {
		var animtime = 600 / toidx;
		for(var ii=0;ii < toidx;ii++) {
			roll(animtime);
		}
	
		$(".choosed[group]").removeClass("choosed");
		$(".line[group='"+jthis.attr("group")+"']").addClass("choosed");
	
		showMessage();
	
		rollTimer = setTimeout(autorolling, 60000);
	}
}

var posClick = function(){

	var $positionCase = $(this);
	var $positionBo = $positionCase.children(".position_bo");
	if ($positionBo.length == 0 && $positionCase.attr("info") != "erro") {
		return;
	}

	var fs = $positionCase.attr("for");
	var fa = fs.split("_");
	var data = null;
	if (fa.length > 1) { 
		data = {process_code:fa[1], line_id :fa[2], hasError:($positionCase.attr("info") == "erro"), hasBo:(!$positionBo.is(":empty"))};
	} else {
		data = {process_code:fs, hasError:($positionCase.attr("info") == "erro"), hasBo:(!$positionBo.is(":empty"))};
	}

	var position = $positionCase.position();
	var parentPosition = $positionCase.parent().position();

	var p_left = position.left + parentPosition.left + parseFloat($positionCase.css("width")) * 2;
	var p_top = position.top + parentPosition.top +  parseFloat($positionCase.css("height")) * 2;
	$("#alert_float").hide();
	$.ajax({
		async : true,
		url : servicePath + '?method=getAlarmsTime',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		complete : function(xhrObj){
			setAlarmDetail(xhrObj, p_left, p_top);
		}
	});
}

var bannerClick = function(){
	var $banner = $(this);
	if ($banner.hasClass("locked")) {
		$banner.removeClass("locked");
		autorolling();
	} else {
		$banner.addClass("locked");
		clearTimeout(rollTimer);
	}
};

if ($.fn.hammer) {
	$(".line").hammer().on("tap", lineClick);
	$(".position").hammer().on("tap", posClick);
	$("#showline .scrollline > .banner").hammer().on("tap", bannerClick);
} else {
	$(".line").click(lineClick);
	$(".position").click(posClick);
	$("#showline .scrollline > .banner").click(bannerClick);
}

});

} // function iamreadyAp

if (typeof(jQuery) === "undefined") {
	loadCss("css/custom.css");
	loadCss("css/olympus/jquery-ui-1.9.1.custom.css", function(){
		loadJs("js/jquery-1.8.2.min.js", function(){
			$("body").addClass("scan1024");
			loadJs("js/jquery-ui-1.9.1.custom.min.js", function(){
				loadJs("js/jquery-plus.js", iamreadyAp);
			});
		});
	});
} else {
	iamreadyAp();
}
</script>
</html>