var initials = [];

var tcCache = {};

var tcUpdate = function(){
	var postData = {};
	var $troAreaHtml = $("#trolley_area");

	var idx = 0;
	$troAreaHtml.find(".trolley_cart").each(function(i, ele) {
		var $cart = $(ele);
		var cartNo = $cart.attr("cart");
		$cart.children(".trolley_stock").each(function(j, elem) {
			var $stock = $(elem);
			postData["trolley_stack.trolley_code[" + idx + "]"] = cartNo;
			postData["trolley_stack.layer[" + idx + "]"] = $stock.attr("stock");
			postData["trolley_stack.material_id[" + idx + "]"] = $stock.children(".material").attr("material_id");
			idx++;
		});
	});

	$.ajax({
		data : postData,
		url: "turnover_case.do?method=doTrolleyUpdate",
		async: true, 
		beforeSend: ajaxRequestType, 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		type : "post",
		complete : null
	});
}

var tcLoadShow = function(xhrObj) {
	var resInfo = $.parseJSON(xhrObj.responseText);

	if (resInfo.errors && resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages(null, resInfo.errors);

		return;
	}

	var linkTrolley = "widgets/qf/turnover_case_to_trolley.jsp";
	var $to_trolley = $("#to_trolley");
	if ($to_trolley.length == 0) {
		$("body").append("<div id='to_trolley'></div>")
		$to_trolley = $("#to_trolley");
	}
	$to_trolley.hide();
	$to_trolley.load(linkTrolley, function(responseText, textStatus, XMLHttpRequest) {
		$to_trolley.find("button").button().trigger("blur").click(doAssignLocation);

		$to_trolley.find("button > span").removeClass();

		$("#tc2t_waitings")
			.on("dragstart", ".material", function(e){
				var $source = $(event.target);
				if ($source.hasClass("warehouse")) {
					dragMaterial = null;
					dragFrom = null;
					return false;
				}
				dragMaterial = event.target.getAttribute("material_id");
				dragFrom = "waiting";
				// event.dataTransfer.setData("Text", event.target.innerText);
			})
			.on("dblclick", ".material", function(evt){
				var $source = $(evt.target);
				if (evt.target.tagName == "SPAN") {
					$source = $source.parent();
				}
				if ($source.hasClass("warehouse")) return;
		
				var stock = null;
				var $trolley_cart = $("#trolley_area .trolley_cart.disp");
				$trolley_cart.children(".trolley_stock").each(function(idx, ele){
					if ($(ele).children(".material").length == 0) {
						stock = idx;
					}
				});
				if (stock == null) {
					errorPop("此推车已经放满，请使用其他推车。");
				} else {
					$trolley_cart.children(".trolley_stock").eq(stock)
						.append($source.clone());
		
					$source.addClass("warehouse");
		
					setTcLocations();
				}
			});

		tcReset($to_trolley, true, resInfo);

		$to_trolley.dialog({
			title : "通箱堆放移动设置",
			width : '88%',
			resizable : false,
			modal : true,
			minHeight : 200,
			close : tcUpdate,
			buttons : {
				"关闭" : function(){ $to_trolley.dialog("close"); }
			}
		});
	});
}

var tcReset = function($to_trolley, first, resInfo){
	$to_trolley.find("#tc2t_waitings").html(getIdleMaterialList(resInfo.idleMaterialList));

	tcCache.nextLocations = resInfo.nextLocations;
	tcCache.nextEndoeyeLocations = resInfo.nextEndoeyeLocations;

	setTrolleys($to_trolley.find("#tc2t_trolleys"), resInfo.trolleyStacks, first);
}

var dragMaterial = null;
var dragFrom = null;

var getIdleMaterialList = function(idleMaterialList){
	if (idleMaterialList.length == 0) {
		return "";
	}
//	var currModelName = "Not A Model";
	var retHtml = "";
	initials = ['尾数 0', '尾数 1', '尾数 2', '尾数 3', '尾数 4', '尾数 5', '尾数 6', '尾数 7', '尾数 8', '尾数 9'];
	var terminalArrays = {};

	for (var imIdx in idleMaterialList) {
		var idleMaterial = idleMaterialList[imIdx];
//		if (currModelName !== idleMaterial.model_name) {
//			if (currModelName !== "Not A Model") {
//				retHtml += "</div>";
//			}
//			currModelName = idleMaterial.model_name;

//			var begins = currModelName.charAt(0);
//			if (initials.indexOf(begins) < 0) {
//				initials.push(begins);
//			}
//			retHtml += "<div class='model_group'><span class='model_name'>" + currModelName + "</span>";
//		}

		var terminalChar = idleMaterial.serial_no.charAt(idleMaterial.serial_no.length - 1);
		if (terminalArrays[terminalChar] == null) {
			terminalArrays[terminalChar] = [];
		}
		terminalArrays[terminalChar].push(idleMaterial);
	}

	for (var i = 0; i < 10; i++) {
		if (terminalArrays[i] != null) {
			retHtml += "<div class='model_group'><span class='model_name'>尾数 " + i + "</span>";

			var terminalArr = terminalArrays[i];
			terminalArr.sort(function(a,b) {
				var aSer = a.serial_no.charAt(a.serial_no.length - 2);
				var bSer = b.serial_no.charAt(b.serial_no.length - 2);
				return aSer - bSer;
			});
			for(j = 0, len=terminalArr.length; j < len; j++) {
    			var idleMaterial = terminalArr[j];

				retHtml += "<div class='material' " + (idleMaterial.execute == 6 ? "model_kind='E' " : "")
					+ "material_id='" + idleMaterial.material_id + "' draggable=true>" 
					+ "<span class='serial_no'>" + idleMaterial.model_name + "</span><br>"
					+ (idleMaterial.omr_notifi_no || "-") 
					+ " <span class='serial_no'>" + idleMaterial.serial_no + "</span></div>";
    		}

			retHtml += "</div>";
		}
	}
			

	var iniHtml = "";
	for (var iIni in initials) {
		iniHtml += "<li class='initial'>&nbsp;" + initials[iIni] + " </li>"
	}
	$("#tc2t_initials").html(iniHtml)
		.find(".initial")
		.click(changeInitial);
	$("#tc2t_initials > .initial:eq(0)").addClass("checked");

	return retHtml;
}

var changeInitial = function() {
	$("#tc2t_initials > .initial.checked").removeClass("checked");
	var initialVal = $(this).addClass("checked").text().trim();

	var $modelName = $("#tc2t_waitings .model_name");
	var nowScrollTop = $("#tc2t_waitings").scrollTop();

	var firstScrollTopTarget = null;
	for (var iMn in $modelName) {
		var modelSpan = $modelName[iMn];
		if (modelSpan.innerText && modelSpan.innerText === initialVal) {
			var positionTop = $modelName.eq(iMn).position().top;
			if (positionTop < 16) {
				if (firstScrollTopTarget == null) firstScrollTopTarget = nowScrollTop + positionTop;
				continue;
			}
			var scrollTopTarget = nowScrollTop + positionTop;
			$("#tc2t_waitings")[0].scroll({ top: scrollTopTarget, behavior: 'smooth' });
			firstScrollTopTarget = null;
			break;
		}
	}
	if (firstScrollTopTarget != null) {
		$("#tc2t_waitings")[0].scroll({ top: firstScrollTopTarget, behavior: 'smooth' });
	}
}

var setTrolleys = function($trolleys, trolleyStacks, first) {
	$trolleys.html("");

	if (first) {
	$trolleys.attr("draggable", false)
		.on("dragover", function(e){
			e.preventDefault();
		})
		.on("drop", function(e){
			if (e.target.className === "trolley_stock" || e.target.className === "material") {
				var $trolley_stock = $(e.target);
				if (e.target.className === "material") {
					$trolley_stock = $trolley_stock.parent();
				}
				var $trolley_cart = $(e.target).closest(".trolley_cart");

				if(dragMaterial) {
					if (dragFrom == "waiting") {
						var $source = $("#tc2t_waitings .material[material_id='" + dragMaterial + "']");
						var stock = null;
						$trolley_cart.children(".trolley_stock").each(function(idx, ele){
							if ($(ele).children(".material").length == 0) {
								stock = idx;
							}
						});
						if (stock == null) {
							errorPop("此推车已经放满，请使用其他推车。");
						} else {
							$trolley_cart.children(".trolley_stock").eq(stock)
								.append($source.clone());
	
							$source.addClass("warehouse");

							setTcLocations();
						}
					} else {

						if ($trolley_stock.children(".material").length > 0) {
							// 交换
							var $source = $trolleys.find(".material[material_id='" + dragMaterial + "']");
							var $sourceStock = $source.parent();
							if ($sourceStock.attr("stock") == $trolley_stock.attr("stock")) {
								return;
							}

							var $thisMaterial = $trolley_stock.children(".material").detach();

							$sourceStock.append($thisMaterial);

							$trolley_stock.append($source.detach());
						} else {
							var $source = $trolleys.find(".material[material_id='" + dragMaterial + "']");
							var $sourceStock = $source.parent();
							$source.detach();

							var $thisStock = $sourceStock;
							var $prevStock = $sourceStock.prev();
							while($prevStock.length > 0) {
								if ($prevStock.children(".material").length == 0) {
									$thisStock.append($source);
									break;
								}
								$thisStock.append($prevStock.children(".material").detach());
								$thisStock = $prevStock;
								$prevStock = $prevStock.prev();
							}
						}

						setTcLocations();
					}
				}
				e.preventDefault();
			}
		})
		.on("dragstart", ".material", function(){
			dragMaterial = event.target.getAttribute("material_id");
			dragFrom = "trolley";
		}).on("dblclick", ".material", function(evt){
			var $target = $(evt.target);
			if (evt.target.tagName == "SPAN") {
				$target = $target.parent();
			}
			var storedMaterial = $target.attr("material_id");
			$("#tc2t_waitings .material[material_id='" + storedMaterial + "']").removeClass("warehouse");

			var $thisStock = $target.parent();
			var $prevStock = $thisStock.prev();
			while($prevStock.length > 0) {
				if ($prevStock.children(".material").length == 0) {
					break;
				}
				$thisStock.append($prevStock.children(".material").detach());
				$thisStock = $prevStock;
				$prevStock = $prevStock.prev();
			}

			$target.remove();

			setTcLocations();
		});
	}

	var $troSelHtml = $("<div id='trolley_sel'><span>1</span><span>2</span><span>3</span><span>4</span><span>5</span></div>");
	$trolleys.append($troSelHtml);
	$troSelHtml.find("span").click(function(){
		var cart = this.innerText;
		$("#trolley_area .trolley_cart.disp").removeClass("disp");
		$("#trolley_area .trolley_cart[cart='" + cart + "']").addClass("disp");

		$troSelHtml.find("span").removeClass("selected");
		$(this).addClass("selected");

		setTcLocations();
	});

	var trolleyStackDict = [];
	for (var iTs in trolleyStacks) {
		var trolleyStack = trolleyStacks[iTs];
		if (trolleyStack.material_id) {
			trolleyStackDict[trolleyStack.trolley_code + "_" + trolleyStack.layer] = trolleyStack.material_id;
		}
	}

	var $troAreaHtmll = $("<div id='trolley_area'/>");
	var mostHeap = 0, mostHeapCnt = 0;

	for (var i0 = 0; i0 < 5; i0++) {
		var trolley = "<div class='trolley_cart' cart='" + (i0 + 1) + "'>";
		var heapCnt = 0;
		for (var i1 = 0; i1 < 10; i1++) {
			trolley += "<div class='trolley_stock' stock='" + (i1 + 1) + "'>";
			// trolleyStacks
			var storedMaterial = trolleyStackDict[(i0 + 1) + "_" + (i1 + 1)];
			if (storedMaterial) {
				var $source = $("#tc2t_waitings .material[material_id='" + storedMaterial + "']").addClass("warehouse");
				trolley += $source.prop("outerHTML"); 
				heapCnt++;
			}
			trolley += "</div>";
		}
		trolley += "</div>"; // trolley_cart
		$troAreaHtmll.append(trolley);

		if (heapCnt > mostHeapCnt) {
			mostHeapCnt = heapCnt;
			mostHeap = i0;
		}
	}

	$trolleys.append($troAreaHtmll);
	$troSelHtml.find("span:eq(" + mostHeap + ")").select().trigger("click");
}

var setTcLocations = function() {
	// tcCache.nextLocations
	var $trolley_cart = $("#trolley_area .trolley_cart.disp");
	var tempIdx = 0, tempEndoeyeIdx = 0;

	$trolley_cart.children(".trolley_stock").each(function(idx, ele){
		var $material = $(ele).children(".material");
		var temp_stock = "";
		if ($material.length > 0) {
			if ($material.attr("model_kind")=="E") {
				temp_stock = tcCache.nextEndoeyeLocations[tempEndoeyeIdx++];
			} else {
				temp_stock = tcCache.nextLocations[tempIdx++]; 
			}
		}
		$("#tc2t_temp_location .location:eq(" + idx + ")").text(temp_stock);
	});
}

var doAssignLocation = function() {

	var $trolley_cart = $("#trolley_area .trolley_cart.disp");

	var postData = {};
	var postIdx = 0;

	$trolley_cart.children(".trolley_stock").each(function(idx, ele){
		var $material = $(ele).children(".material");

		if ($material.length > 0) {
			postData["assign_location.material_id[" + postIdx + "]"] = $material.attr("material_id");
			postData["assign_location.location[" + postIdx + "]"] = $("#tc2t_temp_location .location:eq(" + idx + ")").text();
			postIdx++;
		}
	});

	if (!postData["assign_location.material_id[0]"]) {
		errorPop("当前推车没有对方任何维修品。");
		return;
	}

	var $troAreaHtml = $("#trolley_area");

	var idx = 0;
	$troAreaHtml.find(".trolley_cart").each(function(i, ele) {
		var $cart = $(ele);
		var cartNo = $cart.attr("cart");
		$cart.children(".trolley_stock").each(function(j, elem) {
			var $stock = $(elem);
			postData["trolley_stack.trolley_code[" + idx + "]"] = cartNo;
			postData["trolley_stack.layer[" + idx + "]"] = $stock.attr("stock");
			postData["trolley_stack.material_id[" + idx + "]"] = $stock.children(".material").attr("material_id");
			idx++;
		});
	});

	$.ajax({
		data : postData,
		url: "turnover_case.do?method=doAssignLocation",
		async: true, 
		beforeSend: ajaxRequestType, 
		success: ajaxSuccessCheck, 
		error: ajaxError, 
		type : "post",
		complete : doAssignLocation_callback
	});
}

var doAssignLocation_callback = function(xhrObj) {
	var resInfo = $.parseJSON(xhrObj.responseText);

	if (resInfo.errors && resInfo.errors.length > 0) {
		// 共通出错信息框
		treatBackMessages(null, resInfo.errors);

		return;
	}

	if (resInfo.retMessage) infoPop(resInfo.retMessage);

	tcReset($("#to_trolley"), false, resInfo);
}