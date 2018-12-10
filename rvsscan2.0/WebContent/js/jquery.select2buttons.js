/**
 * .select2Buttons - Convert standard html select into button like elements
 *
 * Version: 1.0.1
 * Updated: 2011-04-14
 *
 *  Provides an alternative look and feel for HTML select buttons, inspired by threadless.com
 *
 * Author: Sam Cavenagh (cavenaghweb@hotmail.com)
 * Doco and Source: https://github.com/o-sam-o/jquery.select2Buttons
 *
 * Licensed under the MIT
 **/
jQuery.fn.select2Buttons = function(options) {
return this.each(function(){
	var $ = jQuery;
	var select = $(this);
	var multiselect = select.attr('multiple');
	select.hide();

	var groupsHtml = select.next(".select2Groups");
	var buttonsHtml;
	if (groupsHtml != null) {
		buttonsHtml = groupsHtml.next(".select2Buttons");
		groupsHtml.remove();
	} else {
        buttonsHtml = select.next(".select2Buttons");
    }
    if (buttonsHtml.length > 0) {
    	buttonsHtml.html("");
    } else {
    	buttonsHtml = $('<div class="select2Buttons" id="s2b_' + select.attr("id") + '"></div>');
    	select.after(buttonsHtml);
    }
    var selectIndex = 0;
	var addOptGroup = function(optGroup) {
		var ulHtml = $('<ul class="select-buttons">');
		if (optGroup.attr('label')) {
			select.next(".select2Groups").find(".select-buttons").append('<li><a>' + optGroup.attr('label') + '<span class="ui-icon ui-icon-circle-triangle-s"></span></a></li>');
			ulHtml = $('<ul class="select-buttons" uid="'+ optGroup.attr('label') +'">');
		}

		var optionsize = optGroup.children('option').length;
		optGroup.children('option').each(function(inde) {
			var liHtml = $('<li></li>');

			var liText = $(this).html();
			if ("" == liText.trim()) {
				liText = "(不选)";
				if(multiselect){
					$(this).removeAttr("selected");
				}
			}
			if ($(this).attr('disabled') || select.attr('disabled')) {
				liHtml.addClass('disabled');
				liHtml.append('<span>' + liText + '</span>');
			} else {
				liHtml.append('<a href="#" data-select-index="' + selectIndex + '">' + liText + '</a>');
				if (inde === 0) {
					liHtml.find("a").addClass("ui-corner-left");
				}
				if (inde === (optionsize - 1)) {
					liHtml.find("a").addClass("ui-corner-right");
				}
			}

			// Mark current selection as "picked"
			if ((!options || !options.noDefault) && $(this).attr('selected')) {
				liHtml.children('a, span').addClass('picked');
			}
			ulHtml.append(liHtml);
			selectIndex++;
		});
		buttonsHtml.append(ulHtml);
	}

	var optGroups = select.children('optgroup');
	if (optGroups.length == 0) {
		addOptGroup(select);
	} else {
		buttonsHtml.before("<div class='select2Groups'><ul class='select-buttons'></ul></div>");
		optGroups.each(function() {
			addOptGroup($(this));
		});
		var groups = select.next(".select2Groups").find(".select-buttons li");
		var groupsize = groups.length;
		groups.each(function(inde,item) {
			if (inde === 0) {
				$(item).find("a").addClass("ui-corner-tl");
			}
			if (inde === (groupsize - 1)) {
				$(item).find("a").addClass("ui-corner-tr");
			}
			$(item).mouseover(function() {
				$("ul[uid]").hide();
				$("ul[uid='"+$(item).text()+"']").show();
			});
		});
	};

	buttonsHtml.find('a').click(function(e){
      e.preventDefault();
      var allOptions = $(select.find('option'));
      //var clickedOption = allOptions.filter("[data-select-index]");
      var clickedOption = $(select.find('option')[$(this).attr('data-select-index')]);
      if(multiselect){
        if(clickedOption.attr('selected')){
          $(this).removeClass('picked');
          clickedOption.removeAttr('selected');
        }else{
          $(this).addClass('picked');
          clickedOption.attr('selected', 'selected');
        }
      }else{
      	allOptions.removeAttr('selected');
        buttonsHtml.find('a, span').removeClass('picked');
        $(this).addClass('picked');
        clickedOption.attr('selected', 'selected');
      }
      select.trigger('change');
    });


    select.change(function(e){
      
 		buttonsHtml.find('a').removeClass('picked');
		if (multiselect) {
			var selectvals = select.val();
			for (var selectedindex in selectvals) {
				var selectval = selectvals[selectedindex];
				if (selectval == "") {
					select.val("");
					select.find("option[value=]").removeAttr("selected");
					buttonsHtml.find('a').removeClass('picked');
					break;
				}
				var sindex = select.find("option[value="+selectval+"]")[0].index;
				buttonsHtml.find('a[data-select-index="'+ sindex+ '"]').addClass('picked');
			}
		} else {
			var sindex = select[0].selectedIndex;
			buttonsHtml.find('a[data-select-index="'+ sindex+ '"]').addClass('picked');
		}
    });
  });
};