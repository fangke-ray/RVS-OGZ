﻿<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" isELIgnored="false"%>
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<link rel="stylesheet" type="text/css" href="css/custom.css">
<link rel="stylesheet" type="text/css" href="css/olympus/jquery-ui-1.9.1.custom.css">

<style>
	.loginput {
		/* Size and position */
		width: 300px;
		margin: auto;
		padding: 15px;
		position: relative;
	 display: block;
		/* Styles */
		background: #fffaf6;
		border-radius: 4px;
		color: #7e7975;
		box-shadow:
			0 2px 2px rgba(0,0,0,0.2),       
			0 1px 5px rgba(0,0,0,0.2),       
			0 0 0 12px rgba(255,255,255,0.4);
	}

.loginput h1 {
    font-size: 15px;
    font-weight: bold;
    color: #08107B;
    padding-bottom: 8px;
    border-bottom: 1px solid #EBE6E2;
    text-shadow: 0 2px 0 rgba(255,255,255,0.8);
    box-shadow: 0 1px 0 rgba(255,255,255,0.8);
}


.loginput .float {
    width: 47%;
    float: left;
    padding-top: 2px;
	margin: 2px;
    border-top: 1px solid rgba(255,255,255,1);
}
 
.loginput .float:first-of-type {
    padding-right: 5px;
}
 
.loginput .float:last-of-type {
    padding-left: 5px;
}
.loginput label {
    display: block;
    padding: 0 0 5px 2px;
    cursor: pointer;
    text-transform: uppercase;
    font-weight: 600;
    text-shadow: 0 1px 0 rgba(255,255,255,0.8);
    font-size: 11px;
    color: #08107b;
}

/*
.loginput label:first-letter {
    color: #ffb300;
    font-weight: 1200;
}
*/

.loginput label i {
    margin-right: 5px; /* Gap between icon and text */
    display: inline-block;
    width: 10px;
}

.loginput input[type=text],
.loginput input[type=password] {
    font-size: 13px;
    font-weight: 400;
    display: block;
    width: 90%;
    padding: 5px;
    margin-bottom: 5px;
    border: 3px solid #ebe6e2;
    border-radius: 5px;
    transition: all 0.3s ease-out;
}
.loginput input[type=text]:hover,
.loginput input[type=password]:hover {
    border-color: #CCC;
}
 
.loginput label:hover ~ input {
    border-color: #CCC;
}
 
.loginput input[type=text]:focus,
.loginput input[type=password]:focus {
    border-color: #ffb300;
    outline: none; /* Remove Chrome's outline */
}

#loginput input[type=button] {
    /* Size and position */
    width: 47%;
    height: 38px;
    float: left;
    position: relative;
 
    /* Styles */
    box-shadow: inset 0 1px rgba(255,255,255,0.3);
    border-radius: 3px;
    cursor: pointer;
 
    /* Font styles */
    font-size: 14px;
    line-height: 38px; /* Same as height */
    text-align: center;
    font-weight: bold;
}

#loginput input[type=reset] {
    /* Size and position */
    width: 47%;
    height: 38px;
    float: right;
    position: relative;
    margin-right : 6px;
 
    /* Styles */
    box-shadow: inset 0 1px rgba(255,255,255,0.3);
    border-radius: 3px;
    cursor: pointer;
 
    /* Font styles */
    font-size: 14px;
    line-height: 38px; /* Same as height */
    text-align: center;
    font-weight: bold;
}

#rolearea input[type=button] {
    width: 96%;
    height: 38px;
    margin-left: 6px;
 
    /* Styles */
    box-shadow: inset 0 1px rgba(255,255,255,0.3);
    border-radius: 3px;
    cursor: pointer;
 
    /* Font styles */
    font-size: 14px;
    line-height: 38px; /* Same as height */
    text-align: center;
    font-weight: bold;
}

.loginput-button {
    margin-left: 1%;
	background: #ffd575;
	background: -moz-linear-gradient(top, #ffd575 0%, #ffb300 100%);
	background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#ffd575), color-stop(100%,#ffb300));
	background: -webkit-linear-gradient(top, #ffd575 0%,#ffb300 100%);
	background: -o-linear-gradient(top, #ffd575 0%,#ffb300 100%);
	background: -ms-linear-gradient(top, #ffd575 0%,#ffb300 100%);
	background: linear-gradient(to bottom, #ffd575 0%,#ffb300 100%);
	filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ffd575', endColorstr='#ffb300',GradientType=0 );
    border: 1px solid #f4ab4c;
    color: #996319;
    text-shadow: 0 1px rgba(255,255,255,0.3);
}

.loginput-reset {
	background: #34a5cf;
	background: -moz-linear-gradient(top, #34a5cf 0%, #08107b 100%);
	background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#34a5cf), color-stop(100%,#08107b));
	background: -webkit-linear-gradient(top, #34a5cf 0%,#08107b 100%);
	background: -o-linear-gradient(top, #34a5cf 0%,#08107b 100%);
	background: -ms-linear-gradient(top, #34a5cf 0%,#08107b 100%);
	background: linear-gradient(to bottom, #34a5cf 0%,#08107b 100%);
	filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#34a5cf', endColorstr='#08107b',GradientType=0 );    border: 1px solid #2B8BC7;
    color: #FFFFFF;
    margin-right: 2%;
    text-decoration: none;
    text-shadow: 0 -1px rgba(0, 0, 0, 0.3);
}

.loginput-role-button-default {
	background: #ffd575;
	background: -moz-linear-gradient(top, #ffd575 0%, #ffb300 100%);
	background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#ffd575), color-stop(100%,#ffb300));
	background: -webkit-linear-gradient(top, #ffd575 0%,#ffb300 100%);
	background: -o-linear-gradient(top, #ffd575 0%,#ffb300 100%);
	background: -ms-linear-gradient(top, #ffd575 0%,#ffb300 100%);
	background: linear-gradient(to bottom, #ffd575 0%,#ffb300 100%);
	filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ffd575', endColorstr='#ffb300',GradientType=0 );
    border: 1px solid #f4ab4c;
    color: #996319;
    text-shadow: 0 1px rgba(255,255,255,0.3);
}

.loginput-role-button {
	background: #34a5cf;
	background: -moz-linear-gradient(top, #34a5cf 0%, #08107b 100%);
	background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#34a5cf), color-stop(100%,#08107b));
	background: -webkit-linear-gradient(top, #34a5cf 0%,#08107b 100%);
	background: -o-linear-gradient(top, #34a5cf 0%,#08107b 100%);
	background: -ms-linear-gradient(top, #34a5cf 0%,#08107b 100%);
	background: linear-gradient(to bottom, #34a5cf 0%,#08107b 100%);
	filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#34a5cf', endColorstr='#08107b',GradientType=0 );    border: 1px solid #2B8BC7;
    color: #FFFFFF;
    margin-right: 2%;
    text-decoration: none;
    text-shadow: 0 -1px rgba(0, 0, 0, 0.3);
}
 
.loginput input[type=button]:hover,
.loginput input[type=reset]:hover {
 	box-shadow: 0 1px rgba(255,255, 255, 0.3) inset , 0 20px 40px rgba(255,255,255, 0.15) inset;
}


</style>

<script type="text/javascript" src="js/jquery-1.8.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.9.1.custom.min.js"></script>
<script type="text/javascript" src="js/jquery.dialog.js"></script>
<script type="text/javascript" src="js/utils.js"></script>
<script type="text/javascript" src="js/jquery-plus.js"></script>
<script type="text/javascript">
$(function() {
	var jobno = $("#loginput input:text:eq(0)");
	var jpwd = $('<input type="password" name="password" value="" placeholder="" autocomplete="new-password">');
	$("#loginarea label[for=password]").after(jpwd);

	var handleComplete = function(xhrobj) {
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
	
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages("#searcharea", resInfo.errors);
			} else if (resInfo.roles) {
				var roleHtml = "<h1>选择登录角色</h1>";
				for (var role_id in resInfo.roles) {
					roleHtml += '<p style="height:30px;"><input type="button" name="submit" id="'+ role_id 
					+ '" class="loginput-role-button' + ("main" == role_id ? "-default" : "") + '" value="' + resInfo.roles[role_id] +'"></p>';
				}
				$("#rolearea").html(roleHtml);
				$("#loginarea").hide();
				$("#rolearea").parent().show("fade", function(){
					$("#rolearea input.loginput-role-button-default").focus();
					$("#rolearea input:button").click(function() {
						var data = {
							role_id : this.id,
							role_name : this.value
						}
						// Ajax提交
						$.ajax({
							beforeSend : ajaxRequestType,
							async : true,
							url : 'login.do?method=selectrole',
							cache : false,
							data : data,
							type : "post",
							dataType : "json",
							success : ajaxSuccessCheck,
							error : ajaxError,
							complete : function(){document.location.href = "panel.do?method=dispatch";} // TODO根据权限特殊画面
						});
					});
				});
			} else {
				document.location.href = "panel.do?method=dispatch"; // adminmenu.do
			}
		}
		catch (e) {
		}
	};

	$("#submitbutton").click(function() {
		var data = {
			job_no : jobno.val(),
			pwd : _enc(jpwd.val())
		}

		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : 'login.do?method=login',
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : handleComplete
		});
	});
	jobno.keydown(function(e){
		if(e.keyCode === 13) {
			jpwd[0].focus();
		}
	});
	jpwd.keydown(function(e){
		if(e.keyCode === 13) {
			$("#submitbutton").trigger("click");
		}
	});
	jobno[0].focus();

	loadJs("js/frontEnc.js");
});

</script>
<title>欢迎登录RVS系统</title>
</head>
<body class="outer">

<%
	String dpt = (String) request.getAttribute("department");
	if ("1".equals(dpt)) {
%>
<div class="ui-widget-panel width-full" style="background-image:url('images/login-r.png');height:673px;margin:auto;" id="body-1">
<%
	} else {
%>
<div class="ui-widget-panel width-full" style="background-image:url('images/login.png?year=2019');height:673px;margin:auto;" id="body-1">
<%
	}
%>
<div id="loginarea" style="position:relative;left: 155px;top: 375px;">
	<formOff class="loginput" id="loginput" autocomplete="off">
	    <h1>登录系统</h1>
	    <p class="float">
	        <label for="login">工号</label>
	        <input type="text" name="login" value="" placeholder="" autocomplete="off">
	    </p>
	    <p class="float">
	        <label for="password">密码</label>
	    </p>
	    <p class="clear" style="height:30px;">
	        <input type="button" class="loginput-button" name="submit" id="submitbutton" value="登录">
	        <input type="reset" class="loginput-reset" name="clear" value="取消">
	    </p>      
	</formOff>
</div>
<div style="color: white; position: absolute; left: 1014px; top: 327px;">
${version}
</div>
<div style="position:relative;left: 155px;top: 375px;display:none;">
	<form class="loginput" id="rolearea">
	    <h1>选择登录角色</h1>
	</form>
</div>
</div>

</body></html>