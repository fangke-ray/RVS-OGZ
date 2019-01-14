<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<style>
#brand-detail {display:none;}
#brand-detail label{display: contents;}
</style>
<script type="text/javascript">
var showBrandDetail = function(brand_id) {
	$.ajax({
		data:{
			"brand_id": brand_id
		},
		url : "brand.do?method=search",
		type : "post",
		complete : function(xhrobj, textStatus){
			// 读取JSON
			var resInfo = $.parseJSON(xhrobj.responseText);

			if (resInfo.brandForms && resInfo.brandForms.length > 0) {
				var brandForm = resInfo.brandForms[0];
				var $brand_detail = $("#brand-detail");
				$brand_detail.find("#brand_detail_name").text(brandForm.name);
				$brand_detail.find("#brand_detail_business_relationship").text(brandForm.business_relationship_text);
				$brand_detail.find("#brand_detail_address").val(brandForm.address || "");
				$brand_detail.find("#brand_detail_email").text(brandForm.email || "");
				$brand_detail.find("#brand_detail_tel").text(brandForm.tel || "");
				$brand_detail.find("#brand_detail_contacts").text(brandForm.contacts || "");
				$brand_detail.dialog({
					title : "厂商通讯信息",
					width : 340,
					show : "blind",
					height : 'auto' ,
					resizable : false,
					modal : true,
					minHeight : 200,
					buttons : {
						"关闭": function(){
							$brand_detail.dialog('close');
						}
					}
				});
				$brand_detail.show();
			} else {
				errorPop("没有此厂商记录。");
			}
		}
	});
}
</script>
		<div id="brand-detail">
			<table class="condform">
				<tr>
					<td class="ui-state-default td-title">厂商名称</td>
					<td class="td-content"><label alt="厂商名称" id="brand_detail_name" class="ui-widget-content"/></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">业务关系</td>
					<td class="td-content">
						<label alt="业务关系" id="brand_detail_business_relationship" class="ui-widget-content"/>
					</td>
				</tr>						
				<tr>
					<td class="ui-state-default td-title">地址</td>
					<td class="td-content">
						<textarea name="address" id="brand_detail_address" readonly disabled></textarea>
					</td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">邮箱</td>
					<td class="td-content"><label alt="邮箱" id="brand_detail_email" class="ui-widget-content"/></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">联系电话</td>
					<td class="td-content"><label alt="联系电话" id="brand_detail_tel" class="ui-widget-content"/></td>
				</tr>
				<tr>
					<td class="ui-state-default td-title">联系人</td>
					<td class="td-content"><label alt="联系人" id="brand_detail_contacts" class="ui-widget-content"/></td>
				</tr>
			</table>