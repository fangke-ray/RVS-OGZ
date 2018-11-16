<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
%>
<base href="<%=basePath%>">
<style>
#report_of_week td.td-content {
width:80px;
text-align:right;
}
#report_of_week input {
width:4em;
text-align:right;
}
.for_complete {
width:750px;
height:7em;
resize:none;
}
#capacity_of_upper_limit input{
 	ime-mode: disabled;
 }
</style>
	<form id="capacity_of_upper_limit">
		<table class="condform" style="width:100%">
			<thead>
				<tr>
					<td class="ui-state-default td-title"></td>
				</tr>
			</thead>
			<tbody>
				
			</tbody>
		</table>
	</form>
<script type="text/javascript">
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : 'scheduleProcessing.do?method=getUpperLimit',
			cache : false,
			data : null,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhrObj, textStatus) {
				try {
			 	var resInfo = $.parseJSON(xhrObj.responseText);
					if (resInfo.errors && resInfo.errors.length > 0) {
						treatBackMessages(null, resInfo.errors);
						return;
					}
					//课室显示
					var resultSectionNames = resInfo.resultSectionNames;
					var rsnLength = resultSectionNames.length;
					
					var $sec = $("#capacity_of_upper_limit >table > thead > tr td");
					if (resultSectionNames) {
						var secContent = "";
						for(var i = 0;i<rsnLength;i++){
							secContent +="<td class='ui-state-default td-title'>"+resultSectionNames[i].section_name+"</td>";
						}	
						$sec.after(secContent);
					}
					
					//维修对象+最大功能显示
					var resultBeans = resInfo.resultBeans;
					var rsLength = resultBeans.length;
					
					var $detail = $("#capacity_of_upper_limit >table > tbody");
					if (resultSectionNames) {
						var detailContent = "";
						var line_id = "";
						for(var j = 0;j<rsLength;j++){
							if (j == 0) {
								line_id = resultBeans[0].line_id;
							}
							detailContent += "<tr>"+
												"<td class='ui-state-default td-title'>"+resultBeans[j].category_name+"</td>";
							for(var i = 0;i<rsnLength;i++){
								detailContent += "<td section_id='"+resultSectionNames[i].section_id + "'><input type='number' value=''/></td>";
							}
							detailContent += "<input class ='category_id' type='hidden' value='"+resultBeans[j].category_id+"'/>"+
											"</tr>";
						}
						detailContent += "<input class ='line_id' type='hidden' value='"+line_id+"'/>";
						$detail.html(detailContent);

						for(var i = 0;i<rsLength;i++){
							if (resultBeans[i].upper_limit != undefined) {
								var str = resultBeans[i].upper_limit.split(";");
								for(var j = 0;j<str.length;j++){
									var temp = str[j].split(":");
									var section_id = temp[0];
									var upper_limit = temp[1];
									$detail.find("tr").eq(i).find("td[section_id="+section_id+"] input").val(upper_limit);
								}
							}
						}
					}

					//修改任一个input可输入数字之后，给其设置changed="true"
					//产能只能输入数字
					$("#capacity_of_upper_limit input[type='number']").change(function(){
						$(this).attr("changed", "true");
				        var ival = $(this).val();
				        if (ival.match(/^[0-9]*$/) == null) {
							var msgInfos=[];
							var msgInfo={};
							msgInfo.errmsg = "请输入0-9之间的数字";
							msgInfos.push(msgInfo);
							treatBackMessages("", msgInfos);
							$(this).val("");
				        }
					});
					
				} catch (e) {
					alert("name: " + e.name + " message: " + e.message + " lineNumber: "
							+ e.lineNumber + " fileName: " + e.fileName);
				};
			}
		});
</script>