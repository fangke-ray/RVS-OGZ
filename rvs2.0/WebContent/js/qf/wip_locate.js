var showLocate=function(location) {

	if (location == null || location.trim() == "") {
		return;
	}
	var jthis = $("#wip_pop");
	jthis.hide();
	jthis.load("widgets/qf/wip_map.jsp", function(responseText, textStatus, XMLHttpRequest) {
		 //新增

		jthis.dialog({
			title : "WIP 位置标示",
			width : 688,
			show: "blind",
			height : 'auto' ,
			resizable : false,
			modal : true,
			buttons : {"关闭" : function() {
				jthis.dialog("close");
			}}
		});

		jthis.find("td[wipid="+location+"]").removeClass("wip-empty").addClass("ui-storage-highlight");

		jthis.show();
	});
}
