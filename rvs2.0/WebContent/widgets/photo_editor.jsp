<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
</head>
	<div id="photo_edit_area" class="d-cropper bg-content bg-content-border-h" style="background-color:white;width:100%;min-height:640px;z-index: 200;">
		<div style="float:left;position:relative;">
			<section class="detail-buttons" style="margin:1em;width:800px;border:1px solid black;text-align:left;
				left:30em;top:0;padding-left:1em;">
				<span style="position: absolute;top: 0;left: 90%;">${no_image}</span>
			<div style="float:left;width:50%;">
			<form id="photo_upload_form" method="POST">
				
				<!--如果是单个文件上传的话name使用file -->
				<input type="file" name="file" id="photo_file"  value="上传"></input>
				<!--如果是多个文件上传的话name使用files(注：多个上传功能也支持单个文件上传)-->
				<!--<input type="file" name="file" id="photo_file" value="上传"></input>-->			

				<input type="button" id="photo_upload_button" class="ui-button" value="上传"></input>
				<input type="hidden" id="photo_uuid" value="${photo_uuid}"></input>
			</form>
			</div>

			<div style="float:left;width:50%;">
				<input type="button" id="photo_reset_button" class="ui-button" value="改回原图"></input>
			</div>
			<div class="clear"/>
			</section>
			<section class="detail-buttons" style="margin:1em;width:800px;border:1px solid black;text-align:left;
				left:30em;top:0;padding-left:1em;">
			<div style="float:left;width:50%;">
				请在图片上划定显示区域，并点击：
				<input type="button" id="image_crop_button" class="ui-button" value="选定"></input>
			</div>
			<div style="float:left;width:50%;">
				<input type="button" id="unclockwise_button" value="旋转(逆)"></input>
				<input type="button" id="clockwise_button" value="旋转(顺)"></input>
				<input type="button" id="photo_mark_button" value="标记" disabled></input>
			</div>
			<div class="clear"/>
			</section>
			<div class="samp_pic" style="top: 1em;position: relative;margin:1em;width:800px;min-height:480px;padding:10px;">
				<img id="editted_image"></img>
			</div>
		</div>
		<style>
		.drFg_section,
		.drIn_section {
			background-image: linear-gradient(rgba(255, 255, 255, .3) 1px, rgba(0, 0, 0, 0) 1px), 
						linear-gradient(90deg, rgba(255, 255, 255, .3) 1px, rgba(0, 0, 0, 0) 1px);
			background-color: lightblue;
			background-size: 16px 16px, 16px 16px;
			background-position: -1px -1px, -1px -1px;
		}

		.drFg_section > div {
			height:30px;
			clear: both;
		}
		.drFg_section > span {
			float: left;
			clear: both;
		}
		.drFg_section > div > hr {
			clear: both;
			border-color: transparent;
			margin: 2px 0;
		}
		.drFg_section > div > div,
		.drIn_section > div > div {
			display:block; float: left; margin-right:4px; cursor: pointer;
			width:40px; height:30px; line-height:30px;
			text-align: center;
		}
		</style>
		<div style="float:left;position:relative;width:200px;">
			<section class="drFg_section" style="margin:1em 0;width:200px;border:1px solid black;text-align:left;
				padding-left:1em;">
				<span>描绘图形</span>
				<div id="drFg_Sharp">
					<div style="border:1px solid black; border-radius: none;">
					</div>
					<div style="border:1px solid black; border-radius: 6px;">
					</div>
					<div style="border:1px solid black; border-radius: 50% / 50%;">
					</div>
				</div>
				<span>线框颜色</span>
				<div id="drFg_Border">
					<div style="border:1px dashed transparent;">
						无
					</div>
					<div style="border:1px solid black;">
					</div>
					<div style="border:1px solid white;">
					</div>
					<hr>
					<div style="border:1px solid red;">
					</div>
					<div style="border:1px solid green;">
					</div>
					<div style="border:1px solid blue;">
					</div>
				</div>
				<span>填充颜色</span>
				<div id="drFg_Backcolor">
					<div style="border:1px solid black;background-color:transparent;">
						无
					</div>
					<div style="border:1px solid black;background-color:rgba(255,255,255,.5);">
					</div>
					<div style="border:1px solid black;background-color:rgba(127,127,127,.5);">
					</div>
					<div style="border:1px solid black;background-color:white;">
					</div>
				</div>
				<span>文字颜色</span>
				<div id="drFg_Forecolor">
					<div style="border:1px solid black;color:black;">
						黑字
					</div>
					<div style="border:1px solid black;color:white;">
						白字
					</div>
					<hr>
					<div style="border:1px solid black;color:red;">
						红字
					</div>
					<div style="border:1px solid black;color:green;">
						绿字
					</div>
					<div style="border:1px solid black;color:blue;">
						蓝字
					</div>
				</div>
				<div style="clear:both;height:4px;"></div>
			</section>
			<section class="drIn_section" style="margin:1em 0;width:200px;border:1px solid black;text-align:left;
				padding: 1em 0 1em 1em;">
				<div>
					<div id="drIn_target" style="border:1px solid black;color:black;">
						对象
					</div>
				</div>
				<div style="clear:both;height:4px;"></div>
			</section>
			<input type="button" id="sharp_insert_button" class="ui-button" value="新建插入" disabled></input>
		</div>
	</div>
