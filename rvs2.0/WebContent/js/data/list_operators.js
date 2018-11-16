$(function() {
		// Your turst our mission
		var modelname = "作业担当人";
		var servicePath = "operatorProduction.do";
		/** 一览数据对象 */
		var listdata = {};

		var initSearch = function() {
			var data = {
				"action_time_start": $("#search_action_time_start").val()
			};
			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url : servicePath + '?method=initSearch',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : function(xhrobj) {
					var resInfo = null;
		
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
		
					if (resInfo.errors.length > 0) {
						// 共通出错信息框
						treatBackMessages("#searcharea", resInfo.errors);
					} else {
						if (resInfo.section_id) {
							$("#search_section_id").val(resInfo.section_id).trigger("change");
						}
						if (resInfo.line_id) {
							$("#search_line_id").val(resInfo.line_id).trigger("change");
						}
						findit();
					}
				}
			});
		}
		var findit = function() {
			var data = {
				"job_no" : $("#search_job_no").val(),
				"name" : $("#search_name").val(),
				"section_id" : $("#search_section_id").val(),
				"line_id" : $("#search_line_id").val(),
				"delete_flg" : $("#search_deleted").val(),
				"action_time_start": $("#search_action_time_start").val(),
				"action_time_end": $("#search_action_time_end").val()
			};

			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url : servicePath + '?method=search',
				cache : false,
				data : data,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : search_handleComplete
			});
		};

		function initGrid() {
			$("#list").jqGrid({
				toppager : true,
				data : [],
				height : 461,
				width : 992,
				rowheight : 23,
				datatype : "local",
				colNames : ['作业时间', '','工号', '姓名', '', '担当主要工位', '实际工时',
						'主要技能'],
				colModel : [{
							name : 'action_time',
							index : 'action_time',
							width : '85',
							formatter : 'date',
							formatoptions : {
								srcformat : 'Y/m/d H:i:s',
								newformat : 'm-d'
							}
						},{
							name: 'action_time_hidden',
							hidden: true,
							formatter:function(a,b,c){return c.action_time}
						}, {
							name : 'job_no',
							index : 'job_no',
							width : 85
						}, {
							name : 'name',
							index : 'name',
							width : 100
						}, {
							name : 'operator_id',
							index : 'operator_id',
							hidden : true
						}, {
							name : 'position_name',
							index : 'position_name',
							width : 100
						}, {
							name : 'worktime',
							index : 'worktime',
							width : 65,
							align : 'center'
						}, {
							name : 'main_ability',
							index : 'main_ability',
							width : 80
						}],
				rowNum : 50,
				toppager : false,
				pager : "#listpager",
				viewrecords : true,
				caption : modelname + "一览",
				ondblClickRow : function(rid, iRow, iCol, e) {
					showDetail(rid);
				},
				// multiselect : true,
				gridview : true, // Speed up
				pagerpos : 'right',
				pgbuttons : true,
				pginput : false,
				recordpos : 'left',
				viewsortcols : [true, 'vertical', true]
			});
		}

		var showDetail = function(rid) {

			var data = $("#list").getRowData(rid);
			var operator_id = data["operator_id"];
			var action_time = data["action_time_hidden"];

			var this_dialog = $("#od_detail_dialog");
			if (this_dialog.length === 0) {
				$("body.outer").append("<div id='od_detail_dialog'/>");
				this_dialog = $("#od_detail_dialog");
			}
			this_dialog.hide();
			this_dialog.load("widgets/operator-detail.jsp", function(
							responseText, textStatus, XMLHttpRequest) {
				this_dialog.dialog({
					position : [400, 20],
					title : action_time + "作业信息",
					width : 800,
					show : "",
					height : 'auto',
					resizable : false,
					modal : true,
					buttons : {
						"确定":function(){
							doOk(operator_id, action_time, this_dialog);
						},
						"取消":function(){
							this_dialog.dialog('close');
						}
					}
				});
				this_dialog.show();
				
				initDetailView();
				opd_finddetail(operator_id, action_time)
			});
		};

		function doOk(operator_id, action_time, this_dialog) {
			var rowData = $("#operator_detail_list").getRowData();
			var last = rowData[rowData.length-1];
			
			if (last.leak === 'true' && last.reason != "") { //最后一条记录是虚拟记录 并且填写了理由
				
				if(confirm("您是否要结束当日的工作?")){
					for (var i in rowData) {
						if (rowData[i].leak === 'true') {
							if (!rowData[i].reason) {
								treatBackMessages("#searcharea", [{errmsg:"如果完成当日日常点检，请补充全部的暂停理由。当前第"+(i+1)+"行未补充。"}]);
								return;
							}
						}
					}
				} else {
					return ;
				}
			}
			
			var edit = $("#can_edit_overtime").val();

			if (edit === 'true') {
				saveOverwork(operator_id, action_time, function(){
					for (var i in rowData) {
						if (rowData[i].leak === 'true' && (rowData[i].reason || rowData[i].comments)) {
							saveLeakData(rowData[i], operator_id);
						}
					}
					this_dialog.dialog('close');
				});
			} else {
				this_dialog.dialog('close');
			}
		}

		function saveOverwork(operator_id, action_time, callback) {
			var over_start = $("#edit_overtime_start").val();
			var over_end = $("#edit_overtime_end").val();
			var start = over_start ? action_time+" "+over_start+":00" : "";
			var end = over_end ? action_time+" "+over_end+':00' : "";
			
			var data = {
				"operator_id" : operator_id,
				"action_time" : action_time,
				"pause_start_time": start,
				"pause_finish_time": end,
				"overwork_reason" : $("#edit_overtime_reason").val(),
				"comments" : $("#edit_overtime_comment").val()
			}
			
			$.ajax({
				beforeSend : ajaxRequestType,
				async : false,
				data : data,
				cache : false,
				url : "operatorProduction.do?method=dosaveoverwork",
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : function(xhrobj, textStatus) {
					var resInfo = null;
					// 以Object形式读取JSON
					eval('resInfo =' + xhrobj.responseText);
					if (resInfo.errors.length > 0) {
						// 共通出错信息框
						treatBackMessages("#searcharea", resInfo.errors);
					} else {
						callback();
					}
				}
			});
		}
		
		function saveLeakData(rowData, operator_id) {
			var data = {
				"operator_id" : operator_id,
				"reason" : rowData["reason"],
				"comments" : rowData["comments"],
				"pause_start_time" : rowData["pause_start_time_hidden"],
				"pause_finish_time" : rowData["pause_finish_time_hidden"]
			}
			
			$.ajax({
				beforeSend : ajaxRequestType,
				async : false,
				data : data,
				cache : false,
				url : "operatorProduction.do?method=dosavepause",
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : function(xhrobj, textStatus) {
					
				}
			});
		}
		
		/*function deletePause(rowData, operator_id) {
			var data = {
				"operator_id" : operator_id,
				"pause_start_time" : rowData["pause_start_time_hidden"]
			}
			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				data : data,
				cache : false,
				url : "operatorProduction.do?method=dodeletepause",
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : function(xhrobj, textStatus) {
					
				}
			});
		}*/

		/*
		 * Ajax通信成功的处理
		 */
		function search_handleComplete(xhrobj, textStatus) {

			var resInfo = null;

			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);

			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages("#searcharea", resInfo.errors);
			} else {
				loadData(resInfo.list);
			}
		};

		function loadData(data) {
			$("#list").jqGrid().clearGridData();
			$("#list").jqGrid('setGridParam', {
						data : data
					}).trigger("reloadGrid", [{
								current : false
							}]);
		}
		function new_handleComplete(xhrobj, textStatus) {

			showList();
		}

		var showList = function() {
			$(document)[0].title = modelname + "一览";
			$("#searcharea").show();
			$("#listarea").show();
			$("#editarea").hide();
			$("#detailarea").hide();
		};

		var download = function(method) {
			$.ajax({
				beforeSend : ajaxRequestType,
				async : true,
				url : servicePath + '?method='+method,
				cache : false,
				data : null,
				type : "post",
				dataType : "json",
				success : ajaxSuccessCheck,
				error : ajaxError,
				complete : function(xhjObject) {
					var resInfo = null;
					eval("resInfo=" + xhjObject.responseText);
					if (resInfo && resInfo.fileName) {
						if ($("iframe").length > 0) {
							$("iframe").attr("src", "download.do"+"?method=output&from=reportm&fileName=" + resInfo.fileName);
						} else {
							var iframe = document.createElement("iframe");
				            iframe.src = "download.do"+"?method=output&from=reportm&fileName=" + resInfo.fileName;
				            iframe.style.display = "none";
				            document.body.appendChild(iframe);
						}
					} else {
						errorPop("上月文件不存在！"); // TODO dialog
					}
				}
			});
		};

		function initView() {
			$("input.ui-button").button();
			$("#deleted_set").buttonset();

			var dToday = new Date();
			$("#search_action_time_start").val(dToday.getFullYear() + "/" + (dToday.getMonth()+1) + "/"  + dToday.getDate());

			$("#searchbutton").addClass("ui-button-primary");
			$("#searchbutton").click(function() {
				findit();
			});
			$("#reportbutton").click(function() {
				download("getLastMonth");
			});

			$("#searcharea span.ui-icon,#wiparea span.ui-icon").bind(
					"click", function() {
						$(this).toggleClass('ui-icon-circle-triangle-n')
								.toggleClass('ui-icon-circle-triangle-s');
						if ($(this).hasClass('ui-icon-circle-triangle-n')) {
							$(this).parent().parent().next().show("blind");
						} else {
							$(this).parent().parent().next().hide("blind");
						}
					});
			$("input[name=deleted]").bind('click', function() {
						$("#search_deleted").val($(this).val());
					});

			$("#resetbutton").click(function() {
				$("#search_job_no").val("");
				$("#search_name").val("");
				$("#search_section_id").val("").trigger("change");
				$("#search_line_id").val("").trigger("change");
				$("#search_deleted").val("");
				$("#deleted_n").attr("checked", "checked").trigger("change");
				$("#search_action_time_start").val(dToday.getFullYear() + "/" + (dToday.getMonth()+1) + "/"  + dToday.getDate());
				$("#search_action_time_end").val("");
			});
			
			$("#search_section_id, #search_line_id").select2Buttons();
			$("#search_action_time_start, #search_action_time_end").datepicker({
				showButtonPanel : true,
				currentText : "今天",
				maxDate : 0
			});

			initGrid();
			initSearch();
		}

		initView();
});