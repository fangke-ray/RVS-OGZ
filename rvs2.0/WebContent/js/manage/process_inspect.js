



$(function() {

	$("input.ui-button").button();

	$("#searchbutton").addClass("ui-button-primary");

	$("#searcharea span.ui-icon,#wiparea span.ui-icon").bind("click", function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().show("blind");
		} else {
			$(this).parent().parent().next().hide("blind");
		}
	});

	$("#search_line_id").select2Buttons();
	$("#search_unqualified_set, #search_history_set").buttonset();

	$("#search_inspector_date_from, #search_inspector_date_to").datepicker({
		showButtonPanel:true,
		currentText: "今天"
	});

	setReferChooser($("#search_model_id"), $("#model_referchooser"));
	setReferChooser($("#search_operator_id"), $("#operator_referchooser"));
	setReferChooser($("#search_inspector_id"), $("#inspector_referchooser"));
});