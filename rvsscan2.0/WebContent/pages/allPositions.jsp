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
			<div class="line choosed" style="height:150px;width:144px;left: 10px;top: 10px;" id="line_qf" beforeOf="line_dec" group="line_qf">
				<div class="posi" id="posi_111" style="left: 32px; top: 10px;">111</div>

				<div class="posi" id="posi_121" style="left: 10px; top: 46px;">121</div>

				<div class="posi" id="posi_131" style="left: 60px; top: 46px;">131</div>

				<div class="posi" id="posi_151" style="left: 10px; top: 106px;">151</div>

				<div class="posi" id="posi_568" style="left: 60px; top: 106px;">568</div>

				<div class="posi" id="posi_181" style="left: 100px; top: 106px;">181</div>

				<div class="posi" id="posi_171" style="left: 110px; top: 36px;">171</div>

				<div class="banner" for="line_qf">受理报价</div>

			</div>

			<div class="line line-addin" style="height: 50px; left: 26px; width: 70px; top: 220px;" id="line2">
				<div class="banner" for="line2">WIP在库</div>
				<div class="banner" for="line2" style="text-decoration: none; top:26px;width: 58px;text-align:center;font-size: 12px;" id="wipsize">514台</div>
			</div>

			<div class="line line-addin" style="height: 50px; width: 70px; top: 180px; left: 110px;" id="line3">
				<div class="banner" for="line3">现品投线</div>
				<div class="banner" for="line3" style="text-decoration: none; top:26px;width: 58px;text-align:center;font-size: 12px;" id="inlinesize">20台</div>
			</div>

			<div class="line" style="height:146px;width:274px;left: 192px;top: 52px;" id="line_dec" beforeOf="line_ns" group="line_dec">
				<div class="posi" for="posi_211_1" style="top: 10px; left: 10px;">211</div>

				<div class="line line-addin" style="height: 50px; width: 70px; top: 80px; left: 30px;">
					<div class="banner" for="line4">分解库位</div>
					<div class="banner" for="line4" style="text-decoration: none; top:26px;width: 58px;text-align:center;font-size: 12px;" id="decsize">10台</div>
				</div>

				<div class="posi" for="posi_221_1" style="top: 64px; left: 108px;">221</div>
				<div class="posi" for="posi_222_1" style="top: 64px; left: 148px;">222</div>
				<div class="posi" for="posi_231_1" style="top: 106px; left: 108px;">231</div>
				<div class="posi" for="posi_241_1" style="top: 106px; left: 148px;">241</div>

				<div class="posi" for="posi_251_1" style="top: 10px; left: 122px;">251</div>
				<div class="posi" for="posi_252_1" style="top: 10px; left: 162px;">252</div>

				<div class="posi" for="posi_261_1" style="top: 50px; left: 202px;">261</div>
				<div class="posi" for="posi_262_1" style="top: 90px; left: 232px;">262</div>
				<div class="posi" for="posi_263_1" style="top: 130px; left: 262px;">263</div>

				<div class="banner" for="line_dec">分解</div>

			</div>

			<div class="line" style="height:168px;width:248px;left: 202px;top: 214px;" id="line_ns" beforeOf="line_com" group="line_ns">
				<div class="posi" for="posi_301_1" style="top: 130px; left: 10px;">301</div>
				<div class="posi" for="posi_302_1" style="top: 130px; left: 50px;">302</div>
				<div class="posi" for="posi_303_1" style="top: 130px; left: 90px;">303</div>
				<div class="posi" for="posi_304_1" style="top: 130px; left: 130px;">304</div>

				<div class="posi" for="posi_311_1" style="top: 28px; left: 10px;">311</div>
				<div class="posi" for="posi_312_1" style="top: 82px; left: 10px;">312</div>

				<div class="posi" for="posi_321_1" style="top: 54px; left: 50px;">321</div>

				<div class="posi" for="posi_331_1" style="top: 28px; left: 90px;">331</div>
				<div class="posi" for="posi_332_1" style="top: 82px; left: 90px;">332</div>

				<div class="posi" for="posi_333_1" style="top: 28px; left: 130px;">333</div>
				<div class="posi" for="posi_341_1" style="top: 82px; left: 130px;">341</div>

				<div class="posi" for="posi_351_1" style="top: 54px; left: 170px;">351</div>

				<div class="posi" for="posi_361_1" style="top: 28px; left: 210px;">361</div>
				<div class="posi" for="posi_362_1" style="top: 82px; left: 210px;">362</div>

				<div class="banner" for="line_ns">NS</div>

			</div>

			<div class="line" style="height:50px;width:284px;left: 492px;top: 170px;" id="line_com" beforeOf="line_endoeye" group="line_com">
				<div class="posi" for="posi_411_1" style="top: 10px; left: 10px;">411</div>

				<div class="posi" for="posi_421_1" style="top: 10px; left: 50px;">421</div>
				<div class="posi" for="posi_431_1" style="top: 10px; left: 90px;">431</div>
				<div class="posi" for="posi_451_1" style="top: 10px; left: 130px;">451</div>
				<div class="posi" for="posi_461_1" style="top: 10px; left: 170px;">461</div>
				<div class="posi" for="posi_471_1" style="top: 10px; left: 210px;">471</div>

				<div class="banner" for="line_com">总组</div>
			</div>

			<div class="line" style="height:50px;width:284px;left: 492px;top: 90px;" id="line_endoeye" beforeOf="line_fs_dec" group="line_endoeye">
				<div class="posi" for="posi_568_1" style="top: 10px; left: 10px;">568</div>

				<div class="posi" for="posi_569_1" style="top: 10px; left: 50px;">569</div>
				<div class="posi" for="posi_570_1" style="top: 10px; left: 90px;">570</div>
				<div class="posi" for="posi_571_1" style="top: 10px; left: 130px;">571</div>

				<div class="banner" for="line_endoeye">外科镜维修</div>
			</div>

			<div class="line" style="height:50px;width:284px;left: 492px;top: 330px;" id="line_fs_dec" beforeOf="line_fs_com" group="line_fs_dec">
				<div class="posi" for="posi_501_1" style="top: 10px; left: 10px;">501</div>

				<div class="posi" for="posi_502_1" style="top: 10px; left: 50px;">502</div>
				<div class="posi" for="posi_503_1" style="top: 10px; left: 90px;">503</div>
				<div class="posi" for="posi_504_1" style="top: 10px; left: 130px;">504</div>
				<div class="posi" for="posi_505_1" style="top: 10px; left: 170px;">505</div>

				<div class="banner" for="line_fs_dec">纤维镜分解</div>
			</div>

			<div class="line" style="height:50px;width:284px;left: 492px;top: 250px;" id="line_fs_com" beforeOf="line_lm" group="line_fs_com">
				<div class="posi" for="posi_511_1" style="top: 10px; left: 10px;">511</div>

				<div class="posi" for="posi_521_1" style="top: 10px; left: 50px;">521</div>
				<div class="posi" for="posi_531_1" style="top: 10px; left: 90px;">531</div>
				<div class="posi" for="posi_541_1" style="top: 10px; left: 130px;">541</div>

				<div class="banner" for="line_fs_com">纤维镜总组</div>
			</div>

			<div class="line" style="height:100px;width:104px;left: 56px;top: 282px;" id="line_lm" beforeOf="line_peri" group="line_lm">
				<div class="posi" for="posi_300_1" style="top: 10px; left: 10px;">300</div>

				<div class="posi" for="posi_400_1" style="top: 64px; left: 10px;">400</div>

				<div class="banner" for="line_lm">中小修</div>
			</div>

			<div class="line" style="height:50px;width:284px;left: 492px;top: 10px;" id="line_peri" beforeOf="line_qa" group="line_peri">
				<div class="posi" for="posi_181" style="top: 10px; left: 10px;">181</div>

				<div class="posi" for="posi_801_1" style="top: 10px; left: 50px;">801</div>

				<div class="posi" for="posi_802_1" style="top: 10px; left: 90px;">802</div>

				<div class="posi" for="posi_811_1" style="top: 10px; left: 130px;">811</div>

				<div class="banner" for="line_peri">周边维修</div>
			</div>

			<div class="line" style="height:192px;width:144px;left: 810px;top: 10px;" id="line_qa" beforeOf="line_qf" group="line_qa">
				<div class="posi" id="posi_613" style="left: 32px; top: 40px;">613</div>

				<div class="posi" id="posi_601" style="left: 84px; top: 76px;">601</div>

				<div class="posi" id="posi_611" style="left: 32px; top: 126px;">611</div>

				<div class="banner" for="line_qa">品保</div>

			</div>

			<div class="line" style="height: 48px; left: 948px; top: 224px; width: 61px;" id="line13">
				<div class="banner" for="line13" style="right: 14px;">出货</div>
				<div class="banner" for="line13" style="text-decoration: none; top:26px;width: 58px;text-align:center;font-size: 12px;" id="shippingsize">20台</div>
			</div>

			<div id="showline" class="line choosed" style="left: 10px; top: 410px; width: 998px; height: 242px;overflow:hidden;">
				<div class="scrollline" for="line_qf" style="position: relative;">
					<div class="position gridX4 gridY2" for="posi_111">111<br>受理</div>
					<div class="position-f gridX4 gridFY2" for="posi_111"></div>

					<div class="position gridX5 gridY2" for="posi_121">121<br>消毒</div>
					<div class="position-f gridX5 gridFY2" for="posi_121"></div>

					<div class="position gridX6 gridY2" for="posi_131">131<br>灭菌</div>
					<div class="position-f gridX6 gridFY2" for="posi_131"></div>

					<div class="position gridX7 gridY2" for="posi_151">151<br>报价确认</div>
					<div class="position-f gridX7 gridFY2" for="posi_151"></div>

					<div class="position gridX8 gridY2" for="posi_171">171<br>画像检查</div>
					<div class="position-f gridX8 gridFY2" for="posi_171"></div>

					<div class="banner" for="line_qf" style="font-size:18px;padding: 8px 10px;z-index:22;">受理报价状况</div>
				</div>
				<div class="scrollline" for="line_dec" style="position: relative;">
					<div class="position gridX1 gridY2" for="posi_211_1">211<br>内镜分解</div>
					<div class="position-f gridX1 gridFY2" for="posi_211_1"></div>

					<div class="line line-addin gridX2 gridY2" style="height: 50px; width: 70px;">
						<div class="banner" for="line4">分解库位</div>
						<div class="banner" for="line4" style="text-decoration: none; top:26px;width: 58px;text-align:center;font-size: 12px;" id="decsize">10台</div>
					</div>

					<div class="position gridX3 gridY1" for="posi_221_1">221<br>零件再生 1</div>
					<div class="position-f gridX3 gridFY1" for="posi_221_1"></div>

					<div class="position gridX3 gridY3" for="posi_222_1">222<br>零件再生 2</div>
					<div class="position-f gridX3 gridFY3" for="posi_222_1"></div>

					<div class="position gridX4 gridY1" for="posi_231_1">231<br>操作部分解</div>
					<div class="position-f gridX4 gridFY1" for="posi_231_1"></div>

					<div class="position gridX4 gridY3" for="posi_241_1">241<br>S 连接座<br>再生 1</div>
					<div class="position-f gridX4 gridFY3P" for="posi_241_1"></div>

					<div class="position gridX5 gridY2" for="posi_251_1">251<br>零件订购</div>
					<div class="position-f gridX5 gridFY2" for="posi_251_1"></div>

					<div class="position gridX6 gridY2" for="posi_252_1">252<br>零件签收</div>
					<div class="position-f gridX6 gridFY2" for="posi_252_1"></div>

					<div class="position gridX7 gridY2" for="posi_261_1">261<br>操作部再生</div>
					<div class="position-f gridX7 gridFY2" for="posi_261_1"></div>

					<div class="position gridX8 gridY2 gridWide" for="posi_262_1">262<br>S连接座再生+<br>操作部漏水检查</div>
					<div class="position-f gridX8 gridFY2P" for="posi_262_1"></div>

					<div class="position gridX10 gridY2" for="posi_263_1">263<br>配对成功</div>
					<div class="position-f gridX10 gridFY2" for="posi_263_1"></div>

					<div class="banner" for="line_dec" style="font-size:18px;padding: 8px 10px;z-index:22;">分解工程状况</div>
				</div>
				<div class="scrollline" for="line_ns" style="position: relative;">
					<div class="position gridX1 gridY1" for="posi_311_1">311<br>先端分解<br>再生</div>
					<div class="position-f gridX1 gridFY1P" for="posi_311_1"></div>

					<div class="position gridX1 gridY3" for="posi_312_1">312<br>先端分解<br>再生</div>
					<div class="position-f gridX1 gridFY3P" for="posi_312_1"></div>

					<div class="position gridX2 gridY2" for="posi_321_1">321<br>零件签收</div>
					<div class="position-f gridX2 gridFY2" for="posi_321_1"></div>

					<div class="position gridX3 gridY1" for="posi_331_1">331<br>先端 1 <br>工程</div>
					<div class="position-f gridX3 gridFY1P" for="posi_331_1"></div>

					<div class="position gridX3 gridY3" for="posi_333_1">333<br>先端 1-3<br>工程</div>
					<div class="position-f gridX3 gridFY3P" for="posi_333_1"></div>

					<div class="position gridX4 gridY1" for="posi_332_1">332<br>先端 1-2<br>工程</div>
					<div class="position-f gridX4 gridFY1P" for="posi_332_1"></div>

					<div class="position gridX4 gridY3" for="posi_341_1">341<br>先端 2 <br>工程</div>
					<div class="position-f gridX4 gridFY3P" for="posi_341_1"></div>

					<div class="position gridX5 gridY2" for="posi_351_1">351<br>NS 组件<br>穿束</div>
					<div class="position-f gridX5 gridFY2P" for="posi_351_1"></div>

					<div class="position gridX6 gridY2" for="posi_361_1">361<br>A 橡皮涂胶+一次涂胶</div>
					<div class="position-f gridX6 gridFY2P" for="posi_361_1"></div>

					<div class="position gridX7 gridY2 gridWide" for="posi_362_1">362<br>A 橡皮涂二次胶<br>+外观检查</div>
					<div class="position-f gridX7 gridFY2P" for="posi_362_1"></div>

					<div class="position gridX10 gridY1" for="posi_301_1">301<br>先端预制</div>
					<div class="position-f gridX10 gridFY1" for="posi_301_1"></div>

					<div class="position gridX10 gridY3" for="posi_302_1">302<br>CCD <br>盖玻璃更换</div>
					<div class="position-f gridX10 gridFY3P" for="posi_302_1"></div>

					<div class="position gridX11 gridY1" for="posi_303_1">303<br>LG <br>玻璃更换</div>
					<div class="position-f gridX11 gridFY1P" for="posi_303_1"></div>

					<div class="position gridX11 gridY3" for="posi_304_1">304<br>LG 线更换</div>
					<div class="position-f gridX11 gridFY3" for="posi_304_1"></div>

					<div class="banner" for="line_ns" style="font-size:18px;padding: 8px 10px;z-index:22;">NS 工程状况</div>
				</div>
				<div class="scrollline" for="line_com" style="position: relative;">
					<div class="position gridX3 gridY2" for="posi_411_1">411<br>内镜对接<br>+钢丝焊接</div>
					<div class="position-f gridX3 gridFY2P" for="posi_411_1"></div>

					<div class="position gridX4 gridY2" for="posi_421_1">421<br>软管对接<br>+LG穿束</div>
					<div class="position-f gridX4 gridFY2P" for="posi_421_1"></div>

					<div class="position gridX5 gridY2" for="posi_431_1">431<br>S 连接座<br>组装</div>
					<div class="position-f gridX5 gridFY2P" for="posi_431_1"></div>

					<div class="position gridX6 gridY2" for="posi_451_1">451<br>EL 焊接</div>
					<div class="position-f gridX6 gridFY2" for="posi_451_1"></div>

					<div class="position gridX7 gridY2" for="posi_461_1">461<br>画像检查<br>+护套安装</div>
					<div class="position-f gridX7 gridFY2P" for="posi_461_1"></div>

					<div class="position gridX8 gridY2 gridWide" for="posi_471_1">471<br>测漏，送气/送水量<br>最终检测</div>
					<div class="position-f gridX8 gridFY2P" for="posi_471_1"></div>

					<div class="banner" for="line_com" style="font-size:18px;padding: 8px 10px;z-index:22;">总组工程状况</div>
				</div>

				<div class="scrollline" for="line_endoeye" style="position: relative;">
					<div class="position gridX2 gridY2" for="posi_568_1">568<br>外科硬镜<br>报价</div>
					<div class="position-f gridX2 gridFY2P" for="posi_568_1"></div>

					<div class="position gridX4 gridY2" for="posi_569_1">569<br>零件订购</div>
					<div class="position-f gridX4 gridFY2" for="posi_569_1"></div>

					<div class="position gridX5 gridY2" for="posi_570_1">570<br>零件签收</div>
					<div class="position-f gridX5 gridFY2" for="posi_570_1"></div>

					<div class="position gridX7 gridY2" for="posi_571_1">571<br>外科硬镜<br>修理</div>
					<div class="position-f gridX7 gridFY2P" for="posi_571_1"></div>

					<div class="banner" for="line_endoeye" style="font-size:18px;padding: 8px 10px;z-index:22;">外科硬镜修理状况</div>
				</div>

				<div class="scrollline" for="line_fs_dec" style="position: relative;">
					<div class="position gridX2 gridY2" for="posi_501_1">501<br>内镜分解</div>
					<div class="position-f gridX2 gridFY2" for="posi_501_1"></div>

					<div class="position gridX3 gridY2" for="posi_502_1">502<br>零件再生</div>
					<div class="position-f gridX3 gridFY2" for="posi_502_1"></div>

					<div class="position gridX5 gridY2" for="posi_503_1">503<br>零件订购</div>
					<div class="position-f gridX5 gridFY2" for="posi_503_1"></div>

					<div class="position gridX6 gridY2" for="posi_504_1">504<br>零件签收</div>
					<div class="position-f gridX6 gridFY2" for="posi_504_1"></div>

					<div class="position gridX8 gridY2" for="posi_505_1">505<br>操作部组装</div>
					<div class="position-f gridX8 gridFY2" for="posi_505_1"></div>

					<div class="banner" for="line_fs_dec" style="font-size:18px;padding: 8px 10px;z-index:22;">纤维镜分解工程状况</div>
				</div>

				<div class="scrollline" for="line_fs_com" style="position: relative;">
					<div class="position gridX4 gridY2" for="posi_511_1">511<br>FS 对接</div>
					<div class="position-f gridX4 gridFY2" for="posi_511_1"></div>

					<div class="position gridX5 gridY2" for="posi_521_1">521<br>接眼组装</div>
					<div class="position-f gridX5 gridFY2" for="posi_521_1"></div>

					<div class="position gridX6 gridY2" for="posi_531_1">531<br>FS<br>LG 穿束</div>
					<div class="position-f gridX6 gridFY2P" for="posi_531_1"></div>

					<div class="position gridX7 gridY2" for="posi_541_1">541<br>FS<br>护套安装</div>
					<div class="position-f gridX7 gridFY2P" for="posi_541_1"></div>

					<div class="banner" for="line_fs_com" style="font-size:18px;padding: 8px 10px;z-index:22;">纤维镜总组工程状况</div>
				</div>

				<div class="scrollline" for="line_lm" style="position: relative;">
					<div class="position gridX5 gridY1" for="posi_300_1">300<br>NS<br>全岗位</div>
					<div class="position-f gridX5 gridFY1P" for="posi_300_1"></div>

					<div class="position gridX5 gridY3" for="posi_400_1">400<br>总组粗细镜<br>全岗位</div>
					<div class="position-f gridX5 gridFY3P" for="posi_400_1"></div>

					<div class="banner" for="line_lm" style="font-size:18px;padding: 8px 10px;z-index:22;">中小修状况</div>
				</div>

				<div class="scrollline" for="line_peri" style="position: relative;">
					<div class="position gridX2 gridY2" for="posi_181">181<br>周边报价</div>
					<div class="position-f gridX2 gridFY2" for="posi_181"></div>

					<div class="position gridX4 gridY2" for="posi_801_1">801<br>零件订购</div>
					<div class="position-f gridX4 gridFY2P" for="posi_801_1"></div>

					<div class="position gridX5 gridY2" for="posi_802_1">802<br>零件签收</div>
					<div class="position-f gridX5 gridFY2P" for="posi_802_1"></div>

					<div class="position gridX7 gridY2" for="posi_811_1">811<br>周边设备<br>维修</div>
					<div class="position-f gridX7 gridFY2P" for="posi_811_1"></div>

					<div class="banner" for="line_peri" style="font-size:18px;padding: 8px 10px;z-index:22;">周边维修状况</div>
				</div>

				<div class="scrollline" for="line_qa" style="position: relative;">
					<div class="position gridX2 gridY2" for="posi_601">601<br>返品解析</div>
					<div class="position-f gridX2 gridFY2" for="posi_601"></div>

					<div class="position gridX4 gridY2" for="posi_611">611<br>出检</div>
					<div class="position-f gridX4 gridFY2" for="posi_611"></div>

					<div class="position gridX6 gridY2" for="posi_613">613<br>周边检查</div>
					<div class="position-f gridX6 gridFY2" for="posi_613"></div>

					<div class="banner" for="line_qa" style="font-size:18px;padding: 8px 10px;z-index:22;">品保工程状况</div>
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

		$("#showline .scrollline:eq(0) .adit-c:eq(0)").text(resInfo.acceptCount + "台");
		$("#showline .scrollline:eq(0) .adit-c:eq(1)").text(resInfo.agreeCount + "台");
		$("#showline .scrollline:eq(0) .adit-c:eq(2)").text(resInfo.quotationCount + "台");
		$("#showline .scrollline:eq(0) .adit-c:eq(3)").text(resInfo.inlineCount + "台");

		$("#showline .scrollline:eq(1) .adit-c:eq(0)").text(resInfo.D1Count + "台");
		$("#showline .scrollline:eq(2) .adit-c:eq(0)").text(resInfo.N1Count + "台");
		$("#showline .scrollline:eq(3) .adit-c:eq(0)").text(resInfo.C1Count + "台 / " + resInfo.C1Plan + "台");
		$("#showline .scrollline:eq(4) .adit-c:eq(0)").text(resInfo.D2Count + "台");
		$("#showline .scrollline:eq(5) .adit-c:eq(0)").text(resInfo.N2Count + "台");
		$("#showline .scrollline:eq(6) .adit-c:eq(0)").text(resInfo.C2Count + "台 / " + resInfo.C2Plan + "台");
		$("#showline .scrollline:eq(7) .adit-c:eq(0)").text((resInfo.C1Count + resInfo.C2Count) + "台");
		$("#showline .scrollline:eq(7) .adit-c:eq(1)").text(resInfo.QOKCount + "台");
		$("#showline .scrollline:eq(7) .adit-c:eq(2)").text(resInfo.QNGCount + "台");
		$("#showline .scrollline:eq(8) .adit-c:eq(0)").text(resInfo.C2Count + "台 ");

		$("#showline .scrollline:eq(1) .adit-c:eq(1)").text(resInfo.boMaterialsD1 + "台");
		$("#showline .scrollline:eq(2) .adit-c:eq(1)").text(resInfo.boMaterialsN1 + "台");
		$("#showline .scrollline:eq(3) .adit-c:eq(1)").text(resInfo.boMaterialsC1 + "台");
		$("#showline .scrollline:eq(4) .adit-c:eq(1)").text(resInfo.boMaterialsD2 + "台");
		$("#showline .scrollline:eq(5) .adit-c:eq(1)").text(resInfo.boMaterialsN2 + "台");
		$("#showline .scrollline:eq(6) .adit-c:eq(1)").text(resInfo.boMaterialsC2 + "台");
		$("#showline .scrollline:eq(8) .adit-c:eq(1)").text(resInfo.boMaterialsC3 + "台");
		$("#boAllLine .adit-c").text(resInfo.boMaterialsAll + "台");
		$("#decsize").text(resInfo.decomStorageCount + "台");

		$("#line3").css("background", "linear-gradient(to right, rgba(255,179,0,1) 0%, rgba(188,194,10,1)" + resInfo.notInlineRate + "%, rgba(25,234,32,1) " + resInfo.notInlineRate + "%, rgba(128,247,17,1) 100%)");
		$("#line13").css("background", "linear-gradient(to right, rgba(255,179,0,1) 0%, rgba(188,194,10,1)" + (resInfo.notShipRate - 3) + "%, rgba(25,234,32,1) " + (resInfo.notShipRate + 3)  + "%, rgba(128,247,17,1) 100%)");
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