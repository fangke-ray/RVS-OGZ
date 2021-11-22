<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<script type="text/javascript">
var defectiveJs = function(){

	var popServicePath = "process_inspect.do";;

	{ // $(function()
		$("#nogoodform").validate({
			rules : {
				comment : {
					required : true
				}
			}
		});
		$("#downloadButton").click(function() {
			var url = servicePath + "?method=output&process_inspect_key=" + $("#header\\.process_inspect_key").val();
			if ($("iframe").length > 0) {
				$("iframe").attr("src", url);
			} else {
				var iframe = document.createElement("iframe");
		        iframe.src = url;
		        iframe.style.display = "none";
		        document.body.appendChild(iframe);
			}
		});
		$("#deleteButton").click(function() {
			var rowid = $("#list").jqGrid('getGridParam', 'selrow');
			if (rowid == null) {return;}

			var rowData = $("#list").jqGrid('getRowData', rowid);

			var data = {
				"process_inspect_key": $("#header\\.process_inspect_key").val()
			};

			// Ajax提交
			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url : servicePath + '?method=doRemove',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : function(xhrObj, textStatus) {
					var resInfo = $.parseJSON(xhrObj.responseText);
					try {

						if (resInfo.errors.length > 0) {
							// 共通出错信息框
							treatBackMessages("#searcharea", resInfo.errors);
						} else {
							$("#detail_dialog").dialog('close');

							if (typeof(refreshList) === "function") refreshList();
							if (typeof(findit) === "function") findit();
						}
					} catch (e) {
						alert("name: " + e.name + " message: " + e.message + " lineNumber: "
								+ e.lineNumber + " fileName: " + e.fileName);
					};
				}
			});
		});
		$(".deleteAchievementButton").click(function() {
			// TODO
		});

		$("#closeButton").click(function() {
			$("#detail_dialog").dialog("close");
		});

		var decBr = function(instr){
			if (instr) {
				return instr.replace(/＜br＞/gi,"<br>").replace(/\n/g,"<br>");
			}
			return instr;
		}

		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : false,
			url : popServicePath + '?method=detailInit',
			cache : false,
			data : {process_inspect_key : $("#header\\.process_inspect_key").val()},
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : function(xhjobj) {
				var resInfo = null;
				try {
					eval("resInfo=" + xhjobj.responseText);
					if (resInfo.header) {
						$("#header\\.file_type").text(resInfo.header.file_type);
						$("#header\\.perform_option_name").text(resInfo.header.perform_option_name);
						$("#header\\.filing_date").text(resInfo.header.filing_date);
						$("#header\\.line_name").text(resInfo.header.line_name);
						$("#header\\.operator_name").text(resInfo.header.operator_name);
						$("#header\\.inspector_name").text(resInfo.header.inspector_name);
						$("#header\\.inspect_date").text(resInfo.header.inspect_date);
						$("#header\\.model_name").text(resInfo.header.model_name);
						$("#header\\.serial_no").text(resInfo.header.serial_no);
						$("#header\\.process_seconds").text(resInfo.header.process_seconds);
						$("#header\\.standard_seconds").text(resInfo.header.standard_seconds);
						console.log(resInfo.header.situation);
						$("#header\\.situation").html(decBr(resInfo.header.situation));
						$("#header\\.countermeasures").html(decBr(resInfo.header.countermeasures));
						$("#header\\.conclusion").html(decBr(resInfo.header.conclusion));
					}

					var idx = 0;
					if (resInfo.details) {
						$.each(resInfo.details, function(key, val) {

							if ($("#process_inspect_detail_infoes_detail" + idx + "_label > span").length == 0) {
								$("#process_inspect_detail_infoes_detail" + idx + "_label").text(key);
							} else {
								$("#process_inspect_detail_infoes_detail" + idx + "_label > span").text(key);
							}

							var gridId = "process_inspect_detail_infoes_detail" + idx + "_list";
							$("#" + gridId).jqGrid({
									toppager : true,
									data : val,
									height : 400,
									width:"100%",
						            autowidth:true,
									rowheight : 23,
									datatype : "local",
									colNames : ['', '监查项目', '检查', '监查内容', '不合格内容', '不合格处理内容', '完成日'],
									colModel : [
										{name: 'rowspan', index: 'rowspan', hidden: true, sortable:false},
										{name:'inspect_item',index:'inspect_item', width:200, sortable:false},
										{name:'need_check',index:'need_check', width:40, align:'center',formatter:'select', editoptions:{value: "0:;1:√"}, sortable:false},
										{name:'inspect_content',index:'inspect_content', width:200,
											cellattr: function(rowId) {
		                                        return "id=\'" + gridId + "_inspectContent" + rowId + "\'";
		                                    }, sortable:false},
										{name:'unqualified_content',index:'unqualified_content', width:120, sortable:false},
										{name:'unqualified_treatment',index:'unqualified_treatment', width:120, sortable:false},
										{name:'unqualified_treat_date',index:'unqualified_treat_date', width:50, align:'center', formatter:'date', formatoptions:{srcformat:'Y/m/d',newformat:'m-d'}, sortable:false}
									],
									rowNum : 150,
									toppager : false,
									//pager : gridPagerId,
									viewrecords : true,
									caption : key + "一览",
									gridview : true,
									pagerpos : 'right',
									pgbuttons : true,
									pginput : false,
									recordpos : 'left',
									viewsortcols : [true, 'vertical', true],
									loadComplete: function() {
										var showIds = $("#" + gridId).getDataIDs();
										var length = showIds.length;

										for (var i = 0; i < length; i++) {
											var curr = $("#" + gridId).jqGrid('getRowData', showIds[i]);
											var rowSpanCount = 1;
											for (var j = i + 1; j <= length; j++) {
												var next = $("#" + gridId).jqGrid('getRowData', showIds[j]);

												if (next.rowspan == null || next.rowspan == '1') {
													rowSpanCount = 1;
													break;
												} else {
													rowSpanCount++;

													$("#" + gridId).setCell(showIds[j], 'inspect_content', '', { display: 'none' });
												}

												$("#" + gridId + "_inspectContent" + showIds[i] + "").attr("rowspan", rowSpanCount);
											}
										}
									}
								});
							idx++;
						})
					}
					$("#process_inspect_detail_content input.ui-button").button();
					$("#process_inspect_detail_infoes, #distributions").buttonset();
				} catch(e) {
					alert("name: " + e.name + "\n message: " + e.message + "\n lineNumber: "
							+ e.lineNumber + "\n fileName: " + e.fileName);
				}
			}
		});

		$("input.ui-button").button();
	}; // $(function()
} // defectiveJs

var loadNogoodEditJqueryPlus = function(){
	if (typeof(warningConfirm) === "function") {
		defectiveJs();
	} else {
		loadJs("js/jquery-plus.js", defectiveJs);
	}
}

if (!$.validator) {
	loadJs("js/jquery.validate.min.js", loadNogoodEditJqueryPlus);
} else {
	loadNogoodEditJqueryPlus();
}
</script>

<form id="nogoodform">

<%
	Boolean enableEdit = (Boolean)request.getAttribute("enableEdit");
	Integer cnt = (Integer)request.getAttribute("achiCnt");
%>
<div id="process_inspect_detail_content" style="float:left;margin:auto;">
	<div style="height:44px;width:100%;" id="process_inspect_detail_infoes" class="dwidth-middle">

		<input type="hidden" id="header.process_inspect_key" name="header.process_inspect_key" value="${process_inspect_key}">

		<input type="radio" name="process_inspect_detail_infoes" class="ui-button ui-corner-up" id="process_inspect_detail_infoes_summary" role="button" checked>
		<label for="process_inspect_detail_infoes_summary" title="">汇总</label>
		<%
			for (int idx = 0; idx < cnt; idx++) {
		%>
			<input type="radio" name="process_inspect_detail_infoes" class="ui-button ui-corner-up" id="process_inspect_detail_infoes_detail<%=idx%>" role="button">
			<label for="process_inspect_detail_infoes_detail<%=idx%>" title="" id="process_inspect_detail_infoes_detail<%=idx %>_label"><%=idx %></label>
		<%
			}
		%>
	</div>

	<div class="ui-widget-content process_inspect_detail_tabcontent" for="process_inspect_detail_infoes_summary" style="width:100%;text-align:left;">
		<div id="process_inspect_detail_summary_area" style="margin-top:22px;margin-left:9px;">
			<div class="ui-widget-content dwidth-middle">
				<table class="condform">
					<tbody>
						<tr>
							<td class="ui-state-default td-title">实施选项</td>
							<td class="td-content">
								<span id="header.perform_option_name"></span>
							</td>
							<td class="ui-state-default td-title">归档日期</td>
							<td class="td-content">
								<span id="header.filing_date"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">工程</td>
							<td class="td-content">
								<span id="header.line_name"></span>
							</td>
							<td class="ui-state-default td-title">操作者</td>
							<td class="td-content">
								<span id="header.operator_name"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">监察者</td>
							<td class="td-content">
								<span id="header.inspector_name"></span>
							</td>
							<td class="ui-state-default td-title">监察日</td>
							<td class="td-content">
								<span id="header.inspect_date"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">型号</td>
							<td class="td-content">
								<span id="header.model_name"></span>
							</td>
							<td class="ui-state-default td-title">机身号</td>
							<td class="td-content">
								<span id="header.serial_no"></span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">作业时间</td>
							<td class="td-content">
								<span id="header.process_seconds"></span>
								<span>分钟</span>
							</td>
							<td class="ui-state-default td-title">标准时间</td>
							<td class="td-content">
								<span id="header.standard_seconds"></span>
								<span>分钟</span>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">监察情况</td>
							<td class="td-content" colspan="3">
								<div id="header.situation"></div>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">实施对策</td>
							<td class="td-content" colspan="3">
								<div id="header.countermeasures"></div>
							</td>
						</tr>
						<tr>
							<td class="ui-state-default td-title">结果</td>
							<td class="td-content" colspan="3">
								<div id="header.conclusion"></div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>

	<%
		for (int idx = 0; idx < cnt; idx++) {
	%>
	<div class="ui-widget-content process_inspect_detail_tabcontent" for="process_inspect_detail_infoes_detail<%=idx %>" style="width:100%;text-align:left;display:none;">
		<div id="process_inspect_detail_detail<%=idx %>_area" style="margin-top:22px;margin-left:9px;">
			<div class="ui-widget-content dwidth-middle">
				<table id="process_inspect_detail_infoes_detail<%=idx %>_list"></table>
			</div>
<% if(enableEdit != null) { %>
		<input type="button" class="deleteAchievementButton" class="ui-button" value="删除实绩表">
<% } %>
		</div>
	</div>
	<% } %>

	<div class="clear areaencloser"></div>
	<div style="text-align: right">
		<input type="button" id="downloadButton" class="ui-button" value="下载">
<% if(enableEdit != null) { %>
		<input type="button" id="deleteButton" class="ui-button" value="删除">
<% } %>
		<input type="button" id="closeButton" class="ui-button" value="关闭">
	</div>
</div>
<script type="text/javascript">
$(function() {
	$("#process_inspect_detail_content input.ui-button").button();
	$("#process_inspect_detail_infoes, #distributions").buttonset();
	$("#process_inspect_detail_infoes input:radio").click(function() {
		$("div.process_inspect_detail_tabcontent").hide();
		var tab = $("div.process_inspect_detail_tabcontent[for='"+this.id+"']");
		tab.show();
	});
});
</script>
</form>