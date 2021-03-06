var panelOverlay = 0;

var gridWidthMiddleRight = 992;

/**
 * 输入项内容变成标签文本
 */
var toLabelValue = function(jobj) {
	if (jobj.is("input:radio")) {
		return jobj.val();
	} else if (jobj.is("input:checkbox")) {
		
	} else if (jobj.is("input")) {
		return jobj.val();
	} else if (jobj.is("select")) {
		return jobj.find("option:selected").text();
	} else if (jobj.is("textarea")) {
		return jobj.val();
	} else if (jobj.is("label")) {
		return jobj.text();
	}
}

/*
 * AJAX给予filter标记,供session超时时判断动作
 */
var ajaxRequestType = function(XMLHttpRequest){
    XMLHttpRequest.setRequestHeader("RequestType", "ajax");
}

/*
 * Ajax通信成功时的处理
 * 页面跳转(可能)
 */
var ajaxSuccessCheck = function(data, textStatus) {

	if (data.redirect) {
		window.location.href = data.redirect;
	}
}

var errorPop = function(errorData, closeToFocus) {
	if ($('div#popuperrstring').length > 0) {
		$('div#popuperrstring').html("<span class='errorarea'>" + errorData + "</span>");
		$('div#popuperrstring').show();
	} else {
		if ($('div#errstring').length == 0) {
			$("body").append("<div id='errstring'/>");
		}
		$('div#errstring').show();
		$('div#errstring').dialog({dialogClass : 'ui-error-dialog', modal : true, width : 450, title : "提示信息", 
			buttons : {"确认" : function() { $(this).dialog("close"); }}, close : function() {if (closeToFocus) closeToFocus.focus(); else $("#scanner_inputer").focus()}});
		$('div#errstring').html("<span class='errorarea'>" + errorData + "</span>");
	}
}

/*
 * Ajax通信失敗時の処理
 */
var ajaxError = function(xhrobj, status, e) {

//	if ($('#pagecontent').length > 0) {
//		window.location.hash = "#error";
//	} else {
//		console.debug("name: " + e.name + " message: " + e.message + " lineNumber: "
//				+ e.lineNumber + " fileName: " + e.fileName + " stack: "
//				+ e.stack);
//		window.location.href = "/panel/error.html";
//	}
};

$(document).ajaxSend(function(evt, request, settings) {
	if(!settings.async) {
		if (panelOverlay == 0) {
			makeWindowOverlay();
		}
	}
	panelOverlay++;
});

$(document).ajaxComplete(function(evt, request, settings) {
	if(panelOverlay > 0) {
		panelOverlay--;
	}

	if(panelOverlay == 0) {
		killWindowOverlay();
	}
});

function makeWindowOverlay(){
	if ($("#woverlay").length == 0) {
		$(window).overlay();
		$("div.overlay").attr("id", "woverlay");
		setTimeout(killWindowOverlay, 5000);
	}
}

function killWindowOverlay(){
	if($("#woverlay").length > 0) {
		$("#woverlay").remove();
		$("body").css('overflow', 'auto');
		panelOverlay = 0;
	}
}

/*
 * jQuery Simple Overlay
 * A jQuery Plugin for creating a simple, customizable overlay. Supports multiple instances,
 * custom callbacks, hide on click, glossy effect, and more.
 *
 * Copyright 2011 Tom McFarlin, http://tommcfarlin.com, @moretom
 * Released under the MIT License
 *
 * http://moreco.de/simple-overlay
 */
(function($) {
	var opts;
	var overlay;
	var containerof;
	var triggerThis;

	$.fn.overlay = function(options) {
		triggerThis = $(this);
		if (options === "close") {
			close(overlay, opts);
		} else {

		    opts = $.extend({}, $.fn.overlay.defaults, options);
				return this.each(function(evt) {
		      if(!triggerThis.hasClass('overlay-trigger')) {
		        show(create(triggerThis, opts), opts);
		      }
			});
		}
    
	}; // end overlay

  /*--------------------------------------------------*
   * helper functions
   *--------------------------------------------------*/
  
  /**
   * Creates the overlay element, applies the styles as specified in the 
   * options, and sets up the event handlers for closing the overlay.
   *
   * opts The plugin's array options.
   */
  function create($src, opts) {
  
    // prevents adding multiple overlays to a container
    $src.addClass('overlay-trigger');
  
    // create the overlay and add it to the dom
    var iTop = 0;
    if($.browser.mozilla && opts.container.toString() === 'body') { 
      iTop = $('html').scrollTop();
      opts.container = $(opts.container)
    } else {
      iTop = $(opts.container).scrollTop();
    } // end if/else

    containerof = opts.container.css("overflow");
    var ajust = (opts.container.css("position") == "relative");
    overlay = $('<div></div>')
    .addClass(opts.styleclass)
    .css({
        background: opts.color,
        opacity: opts.opacity,
        top: opts.container.toString() === 'body' ? iTop : (ajust ? 0 : $(opts.container).offset().top),
        left: ajust ? 0 : $(opts.container).offset().left,
        width: opts.container === 'body' ? '100%' : $(opts.container).width(),
        height: opts.container === 'body' ? '100%' : $(opts.container).height(),
        position: 'absolute',
        zIndex: 1000,
        display: 'none',
        overflow: 'hidden'
      });

    // if specified, apply the gloss
    if(opts.glossy) {
      applyGloss(opts, overlay);     
    } // end if
    
    // setup the event handlers for closing the overlay
    if(opts.closeOnClick) {
      $(overlay).click(function() {
        close(overlay, opts);
        $src.removeClass('overlay-trigger');
      });
    } // end if
    
    // finally add the overlay
    $(opts.container).append(overlay);
   
    return overlay;
    
  } // end createOverlay
  
  /**
   * Displays the overlay using the effect specified in the options. Optionally
   * triggers the onShow callback function.
   *
   * opts The plugin's array options.
   */
  function show(overlay, opts) {
    
    switch(opts.effect.toString().toLowerCase()) {
    
      case 'fade':
        $(overlay).fadeIn('fast', opts.onShow);
        break;
      
      case 'slide':
        $(overlay).slideDown('fast', opts.onShow);
        break;
        
      default:
        $(overlay).show(opts.onShow);
        break;
    
    } // end switch/case
    
    $(opts.container).css('overflow', 'hidden');
    
  } // end show
  
  /**
   * Hides the overlay using the effect specified in the options. Optionally
   * triggers the onHide callback function.
   *
   * opts The plugin's array options.
   */
  function close(overlay, opts) {
    
    switch(opts.effect.toString().toLowerCase()) {
        
      case 'fade':
        $(overlay).fadeOut('fast', function() {
          if(opts.onHide) {
            opts.onHide();
          }
          $(this).remove();
        });
        break;
            
      case 'slide':
        $(overlay).slideUp('fast', function() {
          if(opts.onHide) {
            opts.onHide();
          }
          $(this).remove();
        });
        break;
            
      default:
        $(overlay).hide();
        if(opts.onHide) {
          opts.onHide();
        }
        $(overlay).remove();
        break;
            
    } // end switch/case
    
    $(opts.container).css('overflow', containerof);
    triggerThis.removeClass('overlay-trigger');
    
  } // end close
  
  /*--------------------------------------------------*
   * default settings
   *--------------------------------------------------*/
   
	$.fn.overlay.defaults = {
    color: '#000',
    opacity: 0.5,
    effect: 'none',
    onShow: null,
    onHide: null,
    closeOnClick: false,
    glossy: false,
    container: 'body',
    styleclass : 'overlay'
	}; // end defaults

})(jQuery);

(function($) {
	if ($.fn.fmatter) {
		$.fn.fmatter.rowactions = function(rid, gid, act, pos) {

			switch (act) {
			case 'edit':
				showEdit(rid);
				break;
			case 'del':
				showDelete(rid);
				break;
			}
		};
	}
})(jQuery);


//jquery 添加扩展方法：
(function($) {
	$.fn.disable = function(){
	  return this.each(function(){
	    if(typeof this.disabled!="undefined") {
	    	this.disabled=true;
	    	var jthis = $(this)
		    jthis.attr("aria-disabled", true);
		    jthis.addClass("ui-state-disabled");
	    }
	  });
	}

	$.fn.enable = function(){
	  return this.each(function(){
	    if(typeof this.disabled!="undefined") {
	    	this.disabled=false;
	    	var jthis = $(this)
		    jthis.attr("aria-disabled", false);
		    jthis.removeClass("ui-state-disabled");
		    jthis.removeClass("ui-button-disabled");
	    }
	  });
	}
})(jQuery);

/**
 * jquery validate 提交时弹出窗口
 * @param {} e
 * @param {} v
 */
var jInvalidPop = function(e, v) {
	var sErrormsg = "";
	var firstErrorComponent = null;
	for (var iError in v.errorList){
		var errorline = v.errorList[iError];
		sErrormsg += errorline.message.replace(/\{alt\}/, $(errorline.element).attr("alt")) + "<br>";
		if (iError == 0) {
			firstErrorComponent = errorline.element;
		}
	}
	errorPop(sErrormsg, firstErrorComponent);
}

if ($.validator) {
	$.extend($.validator.defaults, {errorClass:"errorarea-single"});
	$.extend($.validator.messages, {
		required: "请输入{alt}的值",
		remote: "请修正该字段",
		email: "请为{alt}输入一个符合格式的邮箱地址。",
		url: "请输入合法的网址",
		date: "请为{alt}输入一个符合yyyy-mm-dd的形式的日期。",
		dateISO: "请输入合法的日期 (ISO).",
		number: "请为{alt}输入一个数值。",
		digits: "请为{alt}输入一个整数。",
		creditcard: "请为{alt}输入合法的信用卡号",
		equalTo: "请再次输入相同的值",
		accept: "请输入拥有合法后缀名的字符串",
		maxlength: $.validator.format("请为{alt}输入一个长度最多是 {0} 的字符串"),
		minlength: $.validator.format("请为{alt}输入一个长度最少是 {0} 的字符串"),
		rangelength: $.validator.format("请为{alt}输入一个长度介于 {0} 和 {1} 之间的字符串"),
		range: $.validator.format("请为{alt}输入一个介于 {0} 和 {1} 之间的值"),
		max: $.validator.format("请为{alt}输入一个最大为 {0} 的值"),
		min: $.validator.format("请为{alt}输入一个最小为 {0} 的值")
	});
	$.validator.setDefaults(
		//不报label错误信息
		{showErrors: function(){
			var i, elements;
			for ( i = 0; this.errorList[i]; i++ ) {
				var error = this.errorList[i];
				if ( this.settings.highlight ) {
					this.settings.highlight.call( this, error.element, this.settings.errorClass, this.settings.validClass );
				}
				//this.showLabel( error.element, error.message );
			}
			if( this.errorList.length ) {
				this.toShow = this.toShow.add( this.containers );
			}
			if (this.settings.success) {
				for ( i = 0; this.successList[i]; i++ ) {
					this.showLabel( this.successList[i] );
				}
			}
			if (this.settings.unhighlight) {
				for ( i = 0, elements = this.validElements(); elements[i]; i++ ) {
					this.settings.unhighlight.call( this, elements[i], this.settings.errorClass, this.settings.validClass );
				}
			}
			this.toHide = this.toHide.not( this.toShow );
			this.hideErrors();
			this.addWrapper( this.toShow ).show();
    	},
    	// 错误对象效果
		highlight : function(element, errorClass) {
			$(element).addClass(errorClass);
			blink($(element), "errorarea-blink", 6);
		},
		// 弹出框形式报错
		invalidHandler : jInvalidPop,
		// 隐藏的下拉框予以检查
		ignore : "input[type='text']:hidden"
	});
    
}
var datepicker_CurrentInput;
if ($.datepicker) {
	$.datepicker.setDefaults({
			dayNamesMin: ['<span style="color:red;">日</span>','一','二','三','四','五','<span style="color:red;">六</span>'],
			monthNames: ['一月','二月','三月','四月','五月','六月','七月','八月','九月','十月','十一月','十二月'],
			showMonthAfterYear: true,
			yearSuffix: '<span>年</span>',
			closeText:'清空',
			dateFormat: 'yy/mm/dd',
			beforeShow: function (input, inst) { datepicker_CurrentInput = input; }
	});
	$(".ui-datepicker-close").live("click", function (){
			datepicker_CurrentInput.value = "";
	});
}
/**
 * 后台传来的信息在画面上表示
 * @param String range
 * @param {} msgInfos
 */
var treatBackMessages = function(range, msgInfos) {
	if (!msgInfos) return;
	var sErrormsg = "";
	var firstErrorComponent = null;
	for (var ierror in msgInfos){
		var msgInfo = msgInfos[ierror];
		sErrormsg += decodeText(msgInfo.errmsg) + "<br>";
		if (range != null) {
			// 指定项目范围的时候，范围内对应项目标示错误
			var componentid = msgInfo.componentid;
			var component = $(range + " *[id$='_"+componentid+"']");
			if (ierror == 0) {
				firstErrorComponent = component[0];
			}
			component.addClass("errorarea-single");
			if (component[0] && "TABLE" === component[0].tagName) {
				blink(component.find("tr:first"), "errorarea-blink", 6);				
			} else {
				blink(component, "errorarea-blink", 6);
			}
		}
	}
	// 共通出错信息框
	errorPop(sErrormsg, firstErrorComponent);
}

var setReferChooser = function(target, jthis, jfather) {
	if (jthis == null || jthis.length == 0) {
		jthis = $(".referchooser");
	}
	
	if (jfather) { // modify with fengxc
		jfather.change(function(){
			var text = $(this).find("option:selected").text();
			var value = $(this).val();
			if (value === "") {
				refTr.show();
			} else {
				refTr.hide();
				refTr.filter(":has(td:not('.referId'):contains(" + text + "))").show();
			}
		});
	}

	var shower = target.prev("input:text");
	var filter = jthis.find("input:eq(0)");
	var clearer = jthis.find("input:eq(1)");
	var refTr = jthis.find(".subform").find("tr");

	var to;

	shower.click(function(e){
		jthis.css({"top" : shower.position().top + shower.height() - 5, "left" : shower.position().left}).show("fast");
		filter.val("").trigger("change");
		jthis[0].scrollTop = 0;
		jthis.show("fade", function() {
			jthis.focus();
		});
	})
	jthis.blur(function(){
		if(!jthis.is(":hidden")) {
			to = setTimeout(function() {jthis.hide('fade');}, 200);
		}
	})
	filter.focus(function(){
		clearTimeout(to);
	});
	filter.blur(function(){
		jthis.focus();
	});
//	shower.focus(function(){
//		clearTimeout(to);
//	});
//	shower.blur(function(){
//		jthis.focus();
//	});

	filter.change(function() {
		if (this.value === "") {
			if (jfather) {  // modify with fengxc
				jfather.change();//如果过滤是空,关联父类
			} else {
				refTr.show();
			}
		} else {
			refTr.hide();
			refTr.filter(":has(td:not('.referId'):contains(" + this.value + "))").show();
		}
	});
	clearer.click(function() {
		clearTimeout(to);
		jthis.hide();
		shower.val("");
		target.val("");
	});
	refTr.click(function() {
		clearTimeout(to);
		jthis.hide();
		shower.val($(this).find("td:eq(1)").text());
		target.val($(this).find("td:eq(0)").text());
	});
	refTr.hover(function() {$(this).addClass('ui-state-hover');}, function() {$(this).removeClass('ui-state-hover');});
}

$(".ui-widget-overlay").on("click", function(){alert (1);});
//
//$(window).unload(function(e){
//  alert("Goodbye!"+e);
//});