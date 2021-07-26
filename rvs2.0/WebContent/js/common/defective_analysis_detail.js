var defective_analysis_detail_dlg;
var afterResolve = "false";
var popDefectiveAnalysis = function(message_id, is_modal, close_function, open_function){
	if (is_modal == null) is_modal = true;

	var this_dialog = $("#defective_analysis");

	if (this_dialog.length === 0) {
		$("body.outer").append("<div id='defective_analysis'/>");
		this_dialog = $("#defective_analysis");
	}

	this_dialog.html("");
	this_dialog.hide();
	// 导入详细画面
	this_dialog.load("defectiveAnalysis.do?method=detail&afterResolve="+ afterResolve +"&alarm_message_id=" + message_id , function(responseText, textStatus, XMLHttpRequest) {

		this_dialog.dialog({
//			position : [400, 20],
			title : "不良对策详细画面",
			width : 'auto',
			show : null,
			height :  'auto',
			resizable : false,
			modal : is_modal,
			buttons : null
		}).off( "dialogbeforeclose" ).on( "dialogbeforeclose", close_function);

		if (typeof open_function === "function") {
			open_function();
		}
	});
	defective_analysis_detail_dlg = this_dialog;

	this_dialog.show();
}
