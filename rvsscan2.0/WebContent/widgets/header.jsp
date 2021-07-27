<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style type="text/css">
.watch {
text-shadow: 0 0 5px #FFFFFF;color:white;font-size: 24px;font-family: Georgia;
}
@-webkit-keyframes blink{
0%{opacity : 1}
50%{opacity : 0.2}
100%{opacity : 1}
}
@-moz-keyframes blink{
0%{opacity : 1}
10%{opacity : 1}
50%{opacity : 0.2}
90%{opacity : 1}
100%{opacity : 1}
}
</style>
	<div class="areabase">
		<img src="images/logo-rvs.png?v=2021" style="margin-top: 7px; float: left"></img> <!-- -->
		<div>
			<div style="height:29px;line-height:42px;float:right;margin-right: 8px;">
				<span style="color:white;font-size:18px;" id="moduleName"></span>
				<span class="watch" style="padding-left:20px;">13</span>
				<span class="watch" style="-webkit-animation:blink 1s 1s linear infinite both; -moz-animation:blink  1s 1s linear infinite both;">
				:
				</span>
				<span class="watch">24</span>
			</div>
		</div>
	</div>
	<div class="clear" style="height: 10px;"></div>
<script type="text/javascript">
var twochar = function(num) {
	var ret = num.toString();
	for (var ii =ret.length; ii < 2 ;ii++) {
		ret = "0" + ret;
	}
	return ret;
}

var clock = function() {
	var now = new Date();
	$(".watch:eq(0)").text(twochar(now.getHours()));
	$(".watch:eq(2)").text(twochar(now.getMinutes()));
	var rseconds = 60 - now.getSeconds();
	setTimeout(clock , rseconds * 1000);
}

clock();
</script>