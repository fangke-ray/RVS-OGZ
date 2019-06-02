var servicePath="devices_manage.do";
var deliver_list={};
var manage_code_list={};
$(function(){
	$("#body-mdl span.ui-icon,#listarea span.ui-icon").bind("click",function() {
		$(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
		if ($(this).hasClass('ui-icon-circle-triangle-n')) {
			$(this).parent().parent().next().slideToggle("blind");
		} else {
			$(this).parent().parent().next().slideToggle("blind");
		}
	});

	/*按钮事件*/
	$("input.ui-button").button();
	
	$("#waste_old_products").buttonset();
	
	/*品名*/
	setReferChooser($("#hidden_search_name"),$("#name_referchooser"));
	setReferChooser($("#hidden_update_name"),$("#name_referchooser"));
    setReferChooser($("#hidden_add_name"),$("#name_referchooser"));
    
    /*管理员*/
    setReferChooser($("#hidden_search_manager_operator_id"),$("#operator_name_referchooser"));
    setReferChooser($("#hidden_add_manager_operator_id"),$("#operator_name_referchooser"));
    setReferChooser($("#hidden_update_manager_operator_id"),$("#operator_name_referchooser"));
	setReferChooser($("#hidden_deliver_manager_operator_id"),$("#operator_name_referchooser"));
	setReferChooser($("#hidden_to_manager_operator_id"),$("#operator_name_referchooser"));
    
    /*责任工位*/
    setReferChooser($("#hidden_search_position_id"),$("#position_name_referchooser"));
    setReferChooser($("#hidden_update_position_id"),$("#position_name_referchooser"));
    setReferChooser($("#hidden_add_position_id"),$("#position_name_referchooser"));
    setReferChooser($("#hidden_deliver_position_id"),$("#position_name_referchooser"));
    setReferChooser($("#hidden_to_position_id"),$("#position_name_referchooser"));
    
    /*责任人员*/
    setReferChooser($("#hidden_update_responsible_operator_id"),$("#responsible_operato_referchooser"));
    setReferChooser($("#hidden_add_responsible_operator_id"),$("#responsible_operato_referchooser"));

    setReferChooser($("#hidden_search_brand_id"),$("#brand_referchooser"));
    setReferChooser($("#hidden_add_brand_id"),$("#brand_referchooser"));
    setReferChooser($("#hidden_update_brand_id"),$("#brand_referchooser"));
    setReferChooser($("#hidden_replace_brand_id"),$("#brand_referchooser"));

    $("#add_import_date,#add_waste_date,#add_updated_time," +
	  "#update_import_date,#update_waste_date,#update_provide_date," +
	  "#replace_import_date,#replace_waste_date").datepicker({
		showButtonPanel : true,
		dateFormat : "yy/mm/dd",
		currentText : "今天"
	});

	/*检索*/
	$("#searchbutton").click(function(){
		findit();
	});

	$("#resetbutton").click(function(){
		reset();
	});

	$("#update_brand_detail_button").click(function(){
		if ($("#hidden_update_brand_id").val()) {
			showBrandDetail($("#hidden_update_brand_id").val());
		}
	});

    var section_id =$("#search_section_id").html();
    var line_id =$("#search_line_id").html();
    var manage_level =$("#add_manage_level").html();
    var status =$("#search_status").html();
    
    //状态默认是使用中和保管中 + 周边使用中
	$("#search_status option[value='1']").attr("selected","selected");
	$("#search_status option[value='4']").attr("selected","selected");
	$("#search_status option[value='5']").attr("selected","selected").trigger("change");

    $("#add_name_button").click(function(){
		$("#text_name").show();
		$("#add_name").hide();
	});

	$("#search_manage_level,#update_manage_rank,#add_manage_level,#deliver_section_name,#deliver_line_name,#to_section_name,#to_line_name").select2Buttons();
	 
    //修改画面select2Buttons
    $("#update_section_id").html(section_id);
    $("#update_line_id").html(line_id);
    //$("#update_manage_level").html(manage_level);
    //新建画面select2Buttons
    $("#add_section_id").html(section_id);
    $("#add_line_id").html(line_id);
    // $("#add_manage_level").html(manage_level);
    //替换新品
    $("#replace_section_id").html(section_id);
    $("#replace_line_id").html(line_id);
    $("#replace_manage_level").html(manage_level);
    
    $("#search_section_id,#search_line_id,#search_status,#add_section_id," +
      "#add_manage_level,#add_line_id,#add_status,#update_section_id," +
      "#update_line_id,#update_manage_level,#update_status,#replace_section_id," +
      "#replace_line_id,#replace_manage_level,#replace_status").select2Buttons();
    
    //新建课室选择事件
   /* $("#add_section_id").change(function(){
	    var data={
          "section_id":$(this).val()
        };        
       search_operator(data);
    });*/
    
     //修改课室选择事件
    /*$("#update_section_id").change(function(){
        var data={
          "section_id":$(this).val()
        };        
       search_operator(data);
    });*/
    //初始化
    findit(1);
    //移除(不选)选项
    $("#s2b_update_section_id").find("li:eq(0)").remove();
    $("#replacebutton").disable();
    $("#replacebutton").click(function(){
    	replace();
    });
    
    //批量交付
	$("#deliverbutton").click(function(){
		deliver();
	});
	
	//交付动作
	$("#more_update").click(more_update);

	// 备品加入管理
	$("#addsparebutton").click(getSpareList);
	// 订购品加入管理
	$("#addorderbutton").click(getOrderList);

	$("#add_order_filterbutton").click(filterOrderList);
	$("#add_order_clearbutton").click(function(){
		$("#add_order_dialog").find("#add_order_device_type_name").val("")
			.end().find("#add_order_model_name").val("");
		filterOrderList();
	});

});

var more_update = function(){
	var rowids = $('#deliver_list').jqGrid('getGridParam','selarrrow');
	var length = rowids.length;
	var data = {
		"section_id":$("#to_section_name").val(),
		"line_id":$("#to_line_name").val(),
		"position_id":$("#hidden_to_position_id").val(),
		"manager_operator_id":$("#hidden_to_manager_operator_id").val(),
		
		"compare_section_id":$("#deliver_section_name").data("post")==$("#to_section_name").val(),
		"compare_line_id":$("#deliver_line_name").data("post")==$("#to_line_name").val(),
		"compare_position_id":$("#hidden_deliver_position_id").data("post")==$("#hidden_to_position_id").val(),
		"compare_manager_operator_id":$("#hidden_deliver_manager_operator_id").data("post")==$("#hidden_to_manager_operator_id").val()
	};

	var sortManageIds = [];
	var sortManageCodes = [];
	//批量交付-全选
	if($("#cb_deliver_list").attr("checked")=="checked"){
		for(var i=0;i<deliver_list.length;i++){
			sortManageIds.push(deliver_list[i].devices_manage_id);
			sortManageCodes.push(deliver_list[i].manage_code);
		}
	}else{
		for (var i in rowids) {
			var rowData = $("#deliver_list").getRowData(rowids[i]);
			sortManageIds.push(rowData["devices_manage_id"]);
			sortManageCodes.push(rowData["manage_code"]);
		}
	}

	if (sortManageIds.length == 0) {
		return;
	}

	var $changeManageCode = $("#change_manage_code");
	if ($changeManageCode.length == 0) {
		$("body").append("<div id='change_manage_code'/>");
		$changeManageCode = $("#change_manage_code");
	}

	var tChangeManageCode = "<div style='max-height:480px;'><table class='condform'><tr><th>原管理编号</th><th>新管理编号(不变更无需填)</th></tr>";
	for (var i in sortManageIds) {
		tChangeManageCode += "<tr><td dm_id='" + sortManageIds[i] + "'>" + sortManageCodes[i] + "</td><td><input type='text' class='change_mc'></td></tr>"
	}
	tChangeManageCode += "</table></div>"
	$changeManageCode.html(tChangeManageCode);
	$changeManageCode.on("change", ".change_mc", function(){
		var thisValue = this.value;
		var orgValue = $(this).parent().prev().text();
		if (thisValue == orgValue) return;

		var dupli = false;
		for (var i in manage_code_list) {
			if (manage_code_list[i] == thisValue) {
				dupli = true;
				break;
			}
		}

		if (dupli) {
			$(this).attr("title", "与现有使用中重复");
		} else {
			$(this).removeAttr("title");
		}
	})

	$changeManageCode.dialog({
		position : 'center',
		title : "管理编号调整",
		width : 240,
		height : 'auto',
		resizable : false,
		modal : true,
		buttons : {
			"实行交付":function() {
				var i = 0;
				$changeManageCode.find("table td[dm_id]").each(function(){
					data["keys.devices_manage_id[" + i + "]"] = $(this).attr("dm_id");
					var new_manage_code = $(this).next().children("input").val();
					if (new_manage_code && new_manage_code != $(this).text()) {
						data["keys.manage_code[" + i + "]"] = new_manage_code;
					}
					i++;
				});
				$changeManageCode.dialog('close');
				dodeliver(data);
			},
			"关闭":function(){
				$changeManageCode.dialog('close');
			}
		}
	});
}

var dodeliver = function(data){

	$.ajax({
        beforeSend : ajaxRequestType,
        async : true,
        url : servicePath + '?method=dodeliver',
        cache : false,
        data : data,
        type : "post",
        dataType : "json",
        success : ajaxSuccessCheck,
        error : ajaxError,
        complete :deliver_update_Complete
    });
}

var deliver_update_Complete = function(xhrobj, textStatus){
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("#searcharea", resInfo.errors);
        } else {
			infoPop("交付已经完成。", null, "交付");
			deliver_findit();
        }
    }catch (e) {};
}

//批量交付
var deliver = function(){
	//左侧内容清空--start
	$("#deliver_section_name").val("").trigger("change");
	$("#deliver_line_name").val("").trigger("change");
	$("#deliver_position_id").val("");
	$("#hidden_deliver_position_id").val("");
	$("#deliver_manager_operator_id").val("");
	$("#hidden_deliver_manager_operator_id").val("");
	//左侧内容清空--end
	
	//右侧内容清空--start
	$("#to_section_name").val("").trigger("change");
	$("#to_line_name").val("").trigger("change");
	$("#to_position_id").val("");
	$("#hidden_to_position_id").val("");
	$("#to_manager_operator_id").val("");
	$("#hidden_to_manager_operator_id").val("");
	//右侧内容清空--end
	
	deliver_filed_list("");
	
    $("#deliver").dialog({
        position : 'center',
        title : "批量交付",
        width :1200,
        height : 'auto',
        resizable : false,
        modal : true,
        show : "blind",
        buttons : {
             "关闭":function(){
                 $("#deliver").dialog('close');
             }
        }
    });
    
    //批量交付--查询
    $("#searchDetail").click(function(){
    	//点击检索之后，将检索条件的值放在data("post")中
    	$("#deliver_section_name").data("post",$("#deliver_section_name").val());
    	$("#deliver_line_name").data("post",$("#deliver_line_name").val());
		$("#deliver_position_id").data("post",$("#deliver_position_id").val());
		$("#hidden_deliver_position_id").data("post",$("#hidden_deliver_position_id").val());
		$("#deliver_manager_operator_id").data("post",$("#deliver_manager_operator_id").val());
		$("#hidden_deliver_manager_operator_id").data("post",$("#hidden_deliver_manager_operator_id").val());
		
		//批量交付时，将左边的检索条件数据复制到右边--右部分设值--start
		$("#to_section_name").val($("#deliver_section_name").val()).trigger("change");
		$("#to_line_name").val($("#deliver_line_name").val()).trigger("change");
		$("#to_position_id").val($("#deliver_position_id").val());
		$("#hidden_to_position_id").val($("#hidden_deliver_position_id").val());
		$("#to_manager_operator_id").val($("#deliver_manager_operator_id").val());
		$("#hidden_to_manager_operator_id").val($("#hidden_deliver_manager_operator_id").val());
		//右部分设值--end
		deliver_findit();
    });
}
var deliver_findit = function(){
	var data={
            "section_id":$("#deliver_section_name").val(),
            "line_id":$("#deliver_line_name").val(),
            "position_id":$("#hidden_deliver_position_id").val(),
            "manager_operator_id":$("#hidden_deliver_manager_operator_id").val()
      };
      // Ajax提交
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
          complete : deliver_handleComplete
      });
}

var deliver_handleComplete = function(xhrobj, textStatus){
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("#searcharea", resInfo.errors);
        } else {
            var listdata = resInfo.devicesManageForms;
            deliver_filed_list(listdata);
        }
    }catch (e) {};
}

var deliver_filed_list = function(listdata){
    if($("#gbox_deliver_list").length > 0) {
        $("#deliver_list").jqGrid().clearGridData();
        $("#deliver_list").jqGrid('setGridParam',{data:listdata}).trigger("reloadGrid", [{current:false}]);
    }else{
        $("#deliver_list").jqGrid({
            data:listdata,
            height: 298,
            width:422,
            rowheight: 23,
            datatype: "local",
            colNames:['设备工具管理ID','设备工具品名ID','管理编号','品名','型号',
                      '管理员ID','管理员','管理<br>等级','状态','点检表管理号','对应类型','日常点检表<br>管理号','定期点检表<br>管理号',
                      '出厂编号','厂商','','备注','分发课室','责任工程','分发课室ID','责任工程ID','责任工位ID',
                      '责任工位','导入日期','发放日期','发放者','废弃日期','更新时间','最后更新人'],
			colModel:[
				{name:'devices_manage_id',index:'devices_manage_id',hidden:true},
				{name:'devices_type_id',index:'devices_type_id',hidden:true},
				{name:'manage_code',index:'manage_code',width:80},
				{name:'name',index:'name',width:120},
				{name:'model_name',index:'model_name',width:140},
                {name:'manager_operator_id',index:'manager_operator_id',width:100,align:'center',hidden:true},
				{name:'manager',index:'manager',width:60,align:'left',hidden:true},
				{name:'manage_level',index:'manage_level',width:50,align:'center',hidden:true,
	                formatter : 'select',
	                editoptions : {
	                    value : $("#hidden_goManage_level").val()
	                }
                },
                {name:'status',index:'status',width:60,align:'center',hidden:true,
                    formatter : 'select',
                    editoptions : {
                        value : $("#hidden_goStatus").val()
                    }
                },  
                {name:'check_manage_code',index:'check_manage_code',width:110,align:'center',hidden:true},
                {name:'access_place',index:'access_place',width:120,align:'center',hidden:true},
				{name:'daily_sheet_manage_no',index:'daily_sheet_manage_no',width:120,align:'center',hidden:true},
				{name:'regular_sheet_manage_no',index:'regular_sheet_manage_no',width:110,align:'center',hidden:true},
				{name:'products_code',index:'products_code',width:100,align:'center',hidden:true},
				{name:'brand',index:'brand',hidden:true},
				{name:'brand_id',brand_id:'brand_id',hidden:true},
				{name:'comment',index:'comment',width:100,align:'center',hidden:true},
				{name:'section_name',index:'section_name',width:100,align:'center',hidden:true},
				{name:'line_name',index:'responsible_line_name',width:85,align:'center',hidden:true},				
				{name:'section_id',index:'section_id',hidden:true},
				{name:'line_id',index:'line_id',hidden:true},
				{name:'position_id',index:'position_id',hidden:true},
				{name:'process_code',index:'process_code',width:85,align:'center',hidden:true},
                {name:'import_date',index:'import_date',width:85,align:'center',hidden:true},
                {name:'provide_date',index:'provide_date',width:85,align:'center',hidden:true},
                {name:'provider',index:'provider',width:85,align:'center',hidden:true,
	                formatter : function(value, options, rData) {
                        //当发放日期不为空时，发放者是当前更新人；如果为空时，发放者是空白
	                    if(rData.provide_date){
                            return rData.updated_by;
	                    }else{
	                        return "";
	                    }                           
	                }},
                {name:'waste_date',index:'waste_date',width:85,align:'center',hidden:true},
                {name:'updated_time',index:'updated_time',width:85,align:'center',hidden:true},
                {name:'updated_by',index:'updated_by',width:85,align:'center',hidden:true}
			],
            rownumbers:true,
            toppager : false,
            rowNum : 20,
            sortorder:"asc",
            sortname:"id",
            multiselect: true,
            pager : "#deliver_listpager",
            viewrecords : true,
            // ondblClickRow : showDetail,
            // onSelectRow:enableButton,
            gridview : true,
            pagerpos : 'right',
            pgbuttons : true,
            pginput : false,
            recordpos : 'left',
            viewsortcols : [true, 'vertical', true],
            gridComplete:function(){
            }
        });
    }
    deliver_list=listdata;
}

/*判断替换新品enable、disable(当选择了一行之后是enable；否则是disable)*/
var enableButton= function(){
	//选择行，并获取行数
	var row = $("#list").jqGrid("getGridParam", "selrow");
	
	if(row>0){
		$("#replacebutton").enable();
		$("#replacebutton").button();
	}else{
		$("#replacebutton").disable();
	}   
}

//替换新品功能
var replace = function(){
	$("#waste_old_products_no").attr("checked","checked").trigger("change");
	
	var row = $("#list").jqGrid("getGridParam", "selrow");
	var rowData=$("#list").getRowData(row);
	
	//隐藏替换新品之前的治具的管理编号
    $("#hidden_old_manage_code").val(rowData.manage_code);
    //隐藏替换新新品之前的治具的tools_manage_id
    $("#hidden_old_devices_manage_id").val(rowData.devices_manage_id);
	
    //页面隐藏设备工具ID
    $("#hidden_devices_manage_id").val(rowData.devices_manage_id);
    //品名
	$("#replace_name").val(rowData.name); 
    //隐藏设备工具品名ID
    $("#hidden_replace_name").val(rowData.devices_type_id);
    //型号
    $("#replace_model_name").val(rowData.model_name); 
    //放置位置
	$("#replace_location").val(rowData.location);   
    //管理员
    $("#replace_manager").val(rowData.manager);  
    $("#hidden_replace_manager_operator_id").val(rowData.manager_operator_id);
    //管理等级
    $("#replace_manage_level").val(rowData.manage_level).trigger("change");
    //资产编号
    $("#replace_asset_no").val(rowData.asset_no);
     //管理内容
    $("#replace_manage_content").val(rowData.manage_content);
    //出厂编码
	$("#replace_products_code").val(rowData.products_code);
    //厂商
	if (rowData.brand) {
	    $("#replace_brand").val($(rowData.brand).text());
	} else {
	    $("#replace_brand").val("");
	}

    $("#hidden_replace_brand_id").val(rowData.brand_id); 
     //状态
    $("#replace_status").val(rowData.status).trigger("change"); 
    
    //课室ID
	$("#replace_section_id").val(rowData.section_id).trigger("change");
    //工程ID
    $("#replace_line_id").val(rowData.line_id).trigger("change"); 
    //工位ID
	$("#replace_position_id").val(rowData.process_code);  
    
    $("#hidden_replace_position_id").val(rowData.position_id);
    
    /**点击替换新品时，这几项内容为空**/
    //发放者
    $("#replace_provider").text("");    
    //发放日期
    $("#replace_provide_date").text(""); 
    //更新时间
	$("#replace_updated_time").text("");
    //导入日期
	$("#replace_import_date").val(""); 
    //废弃日期
	$("#replace_waste_date").val("");    
	
     //备注
    $("#replace_comment").val(rowData.comment); 

	var data = {
        "manage_code":rowData.manage_code
    };
    // Ajax提交
    $.ajax({
        beforeSend : ajaxRequestType,
        async : true,
        url : servicePath + '?method=searchMaxManageCode',
        cache : false,
        data : data,
        type : "post",
        dataType : "json",
        success : ajaxSuccessCheck,
        error : ajaxError,
        complete : searchMaxManageCode_handleComplete
    });
	$("#replace_confrim").dialog({
		width : 800,
		height : 660,
		resizable : false,
		show : "blind",
		modal : false,
		title : "替换新品",
		buttons : {
			"确认" : function() {
                 var data={
                	"compare_status":rowData.status==$("#replace_status").val(),
			        "manage_code": $("#replace_manage_code").val(),
			        "devices_type_id": $("#hidden_replace_name ").val(), 
			        "model_name":$("#replace_model_name ").val(),
			        "location":$("#replace_location").val(), 
			        "manager_operator_id": $("#hidden_replace_manager_operator_id").val(),
			        "manage_level":$("#replace_manage_level ").val(),
       				"asset_no":$("#replace_asset_no").val(),
			        "manage_content":$("#replace_manage_content").val(), 
			        "products_code": $("#replace_products_code").val(),
			       //"brand": $("#replace_brand ").val(), 
			        "brand_id" : $("#hidden_replace_brand_id").val(),
			        "section_id":$("#replace_section_id ").val(),
			        "line_id": $("#replace_line_id ").val(), 
			        "position_id": $("#hidden_replace_position_id").val(),
			        "responsible_operator_id": $("#hidden_replace_responsible_operator_id").val(),
			        "import_date": $("#replace_import_date").val(),
			        "waste_date":$("#replace_waste_date").val(),
			        "delete_flg":$("#replace_delete_flg").val(),
			        "updated_by":rowData.updated_by_id,
			        "updated_time":$("#replace_updated_time ").val(),
			        "status":$("#replace_status").val(), 
			        "comment": $("#replace_comment").val(),
			        
			        "waste_old_products":$("#waste_old_products input:checked").val(),//--同时废弃掉旧品,
                    "devices_manage_id":$("#hidden_old_devices_manage_id").val()
			     }
                 // Ajax提交
                 $.ajax({
                    beforeSend : ajaxRequestType,
                    async : true,
                    url : servicePath + '?method=doReplace',
                    cache : false,
                    data :data,
                    type : "post",
                    dataType : "json",
                    success : ajaxSuccessCheck,
                    error : ajaxError,
                    complete : function(xhrObj, textStatus){
						var resInfo = $.parseJSON(xhrObj.responseText);
       					if (resInfo.infoes && resInfo.infoes.length > 0) {
       						if (resInfo.infoes[0].errcode === "use_spare") {
								warningConfirm("此型号具有消耗备品，是否使用消耗备品来替换新品？", 
									function() {
										data["use_manage"] = 1;
										// Ajax提交
										$.ajax({
										    beforeSend : ajaxRequestType,
										    async : true,
										    url : servicePath + '?method=doReplace',
										    cache : false,
										    data :data,
										    type : "post",
										    dataType : "json",
										    success : ajaxSuccessCheck,
										    error : ajaxError,
										    complete : replace_handleComplete
										});
									}, function() {
										data["use_manage"] = -1;
										// Ajax提交
										$.ajax({
										    beforeSend : ajaxRequestType,
										    async : true,
										    url : servicePath + '?method=doReplace',
										    cache : false,
										    data :data,
										    type : "post",
										    dataType : "json",
										    success : ajaxSuccessCheck,
										    error : ajaxError,
										    complete : replace_handleComplete
										});
									}, "消耗备品替换"
								);
       						} else
      						if (resInfo.infoes[0].errcode === "use_order") {
      							if ($("#hitOrdersConfirm").length == 0) {
      								$("body").append("<div id='hitOrdersConfirm'></div>");
      							}
      							var hitOrdersHtml = "";
      							for (var i in resInfo.hitOrders) {
      								var hitOrder = resInfo.hitOrders[i];
      								hitOrdersHtml += "<tr><td><input type='radio' " + (i == 0 ? "checked" : "") +
      									"></td><td order_key=" + hitOrder.order_key + "_" + hitOrder.object_type + ">订购单 " + hitOrder.order_no + 
      									"</td><td applicator_id=" + hitOrder.applicator_id + ">" + hitOrder.operator_name + " 申请</td></tr>";
      							}
      							var $hitOrdersConfirm = $("#hitOrdersConfirm");
      							$hitOrdersConfirm.html("<span>此型号具有未登录的订购品，是否使用以下订购品来替换新品？<span><table id='hoTable'>"
      							+ hitOrdersHtml + "</table>"); 
								$hitOrdersConfirm.dialog({
									dialogClass : 'ui-warn-dialog',
									position : 'center',
									title : "选择从订购品登录",
									width : 660,
									height : 'auto',
									resizable : false,
									modal : true,
									buttons : {
										"选择":function() {
											var $selRow = $("#hoTable input:radio[checked]").closest("tr");
											if($selRow.length) {
												data["use_manage"] = 2;
												data["compare_manager_operator_id"] = $selRow.children("td:eq(2)").attr("applicator_id");
												data["order_key"] = $selRow.children("td:eq(1)").attr("order_key");
												// Ajax提交
												$.ajax({
												    beforeSend : ajaxRequestType,
												    async : true,
												    url : servicePath + '?method=doReplace',
												    cache : false,
												    data :data,
												    type : "post",
												    dataType : "json",
												    success : ajaxSuccessCheck,
												    error : ajaxError,
												    complete : replace_handleComplete
												});
												$hitOrdersConfirm.dialog("close");
											}
										},
										"不从订购品登录":function(){
											data["use_manage"] = -1;
											// Ajax提交
											$.ajax({
											    beforeSend : ajaxRequestType,
											    async : true,
											    url : servicePath + '?method=doReplace',
											    cache : false,
											    data :data,
											    type : "post",
											    dataType : "json",
											    success : ajaxSuccessCheck,
											    error : ajaxError,
											    complete : replace_handleComplete
											});
											$hitOrdersConfirm.dialog("close");
										}
									}
								});
      						}
						} else {
	                    	replace_handleComplete(xhrObj, textStatus);
						}
                    }
                 });
			},
			"取消" : function() {
				$(this).dialog("close");
			}
		}
	});
	
	setReferChooser($("#hidden_replace_tools_name"),$("#replace_name_referchooser"));
    setReferChooser($("#hidden_replace_position_id"),$("#replace_position_referchooser"));
    setReferChooser($("#hidden_replace_manager_operator_id"),$("#replace_operator_name_referchooser"));
}
var replace_handleComplete = function(xhrobj, textStatus) {
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("#editarea", resInfo.errors);
        } else {
        	infoPop("替换新品已经完成。", null, "替换新品");
		$("#replace_confrim").dialog("close");
       
            // 重新查询
            findit();
            // 切回一览画面
            showList();
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "
                + e.lineNumber + " fileName: " + e.fileName);
    };
}

var searchMaxManageCode_handleComplete=function(xhrobj, textStatus){
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("", resInfo.errors);
        } else {
        	//管理编号
            $("#replace_manage_code").val(resInfo.manageCode);
        }
    }catch (e) {};
}

/*var search_operator = function(data){
    // Ajax提交
    $.ajax({
        beforeSend : ajaxRequestType,
        async : true,
        url : servicePath + '?method=searchResponsibleOperator',
        cache : false,
        data : data,
        type : "post",
        dataType : "json",
        success : ajaxSuccessCheck,
        error : ajaxError,
        complete : searchOperator_handleComplete
    });
}
var searchOperator_handleComplete = function(xhrobj, textStatus) {
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("#searcharea", resInfo.errors);
        } else {
            var responseOperator = resInfo.responseOperator;
            $("#add_choose_operator").html(responseOperator);
            $("#update_choose_operator").html(responseOperator);
            setReferChooser($("#hidden_add_responsible_operator_id"),$("#add_responsible_operato_referchooser"));
            setReferChooser($("#hidden_update_responsible_operator_id"),$("#update_responsible_operato_referchooser"));
        }
    }catch (e) {};
}*/

var findit = function(arg) {
    var data = {
        "manage_code":$("#search_manage_code").val(),
        //"name":$("#search_name").val(),
        "devices_type_id":$("#hidden_search_name").val(),
        "model_name":$("#search_model_name").val(),
        "section_id":$("#search_section_id").val(),
        "line_id":$("#search_line_id").val(),            
        "manage_level":$("#search_manage_level").val(),
		"asset_no":$("#search_asset_no").val(),
        "manager_operator_id":$("#hidden_search_manager_operator_id").val(),
        "brand_id":$("#hidden_search_brand_id").val(),
        "status":$("#search_status").val() && $("#search_status").val().toString(),//默认是选择使用中和保管中
        "position_id":$("#hidden_search_position_id").val()
    };
    if (arg) {
    	data.access_place = 1;
    }
    // Ajax提交
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

var search_handleComplete = function(xhrobj, textStatus) {
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("#searcharea", resInfo.errors);
        } else {
        	 $("#replacebutton").disable();
        	 $("#hidden_import_date").val(resInfo.current_date);
            var listdata = resInfo.devicesManageForms;
            filed_list(listdata);
            if (resInfo.manageCodes) {
            	manage_code_list = resInfo.manageCodes;
            }
        }
    }catch (e) {};
}
//清除
var reset=function(){
	$("#search_name").data("post","").val("");
    $("#hidden_search_name").data("post","").val("");
	$("#search_brand").data("post","").val("");
	$("#search_model_name").val("");
	$("#search_manage_code").data("post","").val("");
	$("#search_asset_no").data("post","").val("");
	$("#search_section_id").data("post","").val("").trigger("change");
	$("#search_line_id").data("post","").val("").trigger("change");
	$("#search_position_id").val("");
    $("#hidden_search_position_id").data("post","").val("");
	$("#search_manager_operator_id").val("");
    $("#hidden_search_manager_operator_id").val("");

	$("#search_brand_id").val("");
    $("#hidden_search_brand_id").val("");

    $("#search_status").data("post","").val("").trigger("change");		
    $("#search_manage_level").data("post","").val("").trigger("change");
};

function filed_list(listdata){
	if($("#gbox_list").length > 0) {
		$("#list").jqGrid().clearGridData();
		$("#list").jqGrid('setGridParam',{data:listdata}).trigger("reloadGrid", [{current:false}]);
	}else{
		$("#list").jqGrid({
			data:listdata,
			height: 461,
			width: 992,
			rowheight: 23,
			datatype: "local",
			colNames:['设备工具管理ID','设备工具品名ID','管理编号','品名','型号','资产编号','放置位置',
                      '管理员ID','管理员','管理<br>等级','状态','点检表管理号','对应类型','日常点检表<br>管理号','定期点检表<br>管理号',
                      '出厂编号','厂商','','备注','分发课室','责任工程','分发课室ID','责任工程ID','责任工位ID',
                      '责任工位','导入日期','发放日期','发放者','废弃日期','更新时间','最后更新人'],
	        colModel:[
					{name:'devices_manage_id',index:'devices_manage_id',hidden:true},
					{name:'devices_type_id',index:'devices_type_id',hidden:true},
					{name:'manage_code',index:'manage_code',width:80},
					{name:'name',index:'name',width:120},
					{name:'model_name',index:'model_name',width:140},
					{name:'asset_no',index:'asset_no',width:70},
					{name:'location',index:'location',hidden:true},
	                {name:'manager_operator_id',index:'manager_operator_id',width:100,align:'center',hidden:true},
					{name:'manager',index:'manager',width:60,align:'left'},
					{name:'manage_level',index:'manage_level',width:50,align:'center',
		                formatter : 'select',
		                editoptions : {
		                    value : $("#hidden_goManage_level").val()
		                }
	                },
	                {name:'status',index:'status',width:60,align:'center',
	                    formatter : 'select',
	                    editoptions : {
	                        value : $("#hidden_goStatus").val()
	                    }
	                },  
	                {name:'check_manage_code',index:'check_manage_code',width:110,align:'center',hidden:true},
	                {name:'access_place',index:'access_place',width:120,align:'center',hidden:true},
					{name:'daily_sheet_manage_no',index:'daily_sheet_manage_no',width:120,align:'center'},
					{name:'regular_sheet_manage_no',index:'regular_sheet_manage_no',width:110,align:'center'},
					{name:'products_code',index:'products_code',width:100,align:'center'},
					{name:'brand',index:'brand',width:100,align:'center',
						formatter : function(value, options, rData) {
		                    if(rData.brand){
	                            return "<a href='javascript:showBrandDetail(\""+ rData.brand_id +"\")'>" + rData.brand + "</a>";
		                    }else{
		                        return "";
		                    }                           
		                }},
					{name:'brand_id',brand_id:'brand_id',hidden:true},
					{name:'comment',index:'comment',width:100,align:'center',hidden:true},
					{name:'section_name',index:'section_name',width:100,align:'center',hidden:false},
					{name:'line_name',index:'responsible_line_name',width:85,align:'center',hidden:false},				
					{name:'section_id',index:'section_id',hidden:true},
					{name:'line_id',index:'line_id',hidden:true},
					{name:'position_id',index:'position_id',hidden:true},
					//{name:'responsible_operator_id',index:'responsible_operator_id',hidden:true},
	                //{name:'responsible_operator',index:'responsible_operator',hidden:true},
					{name:'process_code',index:'process_code',width:85,align:'center'},
	                {name:'import_date',index:'import_date',width:85,align:'center',hidden:true},
	                {name:'provide_date',index:'provide_date',width:85,align:'center',hidden:true},
	                {name:'provider',index:'provider',width:85,align:'center',hidden:true,
		                formatter : function(value, options, rData) {
	                        //当发放日期不为空时，发放者是当前更新人；如果为空时，发放者是空白
		                    if(rData.provide_date){
	                            return rData.updated_by;
		                    }else{
		                        return "";
		                    }                           
		                }},
	                {name:'waste_date',index:'waste_date',width:85,align:'center',hidden:true},
	                {name:'updated_time',index:'updated_time',width:85,align:'center',hidden:true},
	                {name:'updated_by',index:'updated_by',width:85,align:'center',hidden:true}
				],
			rownumbers:true,
			toppager : false,
			rowNum : 20,
			pager : "#listpager",
			viewrecords : true,
			ondblClickRow : showEdit,
			onSelectRow :enableButton,
			gridview : true,
			pagerpos : 'right',
			pgbuttons : true,
			pginput : false,
			recordpos : 'left',
			viewsortcols : [true, 'vertical', true]
		});
		/*$(".ui-jqgrid-hbox").before('<div class="ui-widget-content" style="padding:4px;">' +
			'<input type="button" class="ui-button-primary ui-button ui-widget ui-state-default ui-corner-all" id="addbutton" value="新建设备工具'+'" role="button" aria-disabled="false">' +
		'</div>');
		$("#addbutton").button();*/
		$("#addbutton").click(showAdd);
	}	
};

/*新建*/
var showAdd = function(add_method, entity){
    //点击新建之前清空
	$("#add_manage_code").val("");
	$("#add_manage_level").val("").trigger("change");
	$("#add_asset_no").val("");
	$("#add_products_code").val("");
	$("#add_position_id").val("");
	$("#add_import_date").val("");
	$("#add_waste_date").val("").hide();
	$("#add_manage_content").val("");
    $("#add_status").val("").trigger("change");
    $("#add_section_id").val("").trigger("change");
    $("#add_line_id").val("").trigger("change");
	$("#add_responsible_operator_id").val("");
	$("#add_updated_time").val("");
	if (add_method == "spare") {
		$("#add_name").val(entity.device_type_name).disable();
		$("#hidden_add_name").val(entity.device_type_id);
		if (entity.brand_name) {
			var brand_name = $(entity.brand_name).text();
			$("#add_brand").val(brand_name).disable();
			$("#hidden_add_brand_id").val(entity.brand_id);
		} else {
			$("#add_brand").val("").enable();
			$("#hidden_add_brand_id").val("");
		}
		$("#add_comment").val("由备品转为编号管理。");
		$("#add_model_name").val(entity.model_name).disable();
		$("#add_manager").val("");  
		$("#hidden_add_manager_operator_id").val("");  

		$("#hidden_order_key").val("");  
		$("#hidden_applicator_id").val("");
	} else if (add_method == "order") {
		$("#hidden_add_name").val(entity.device_type_id);
		var device_type_name = $("#name_referchooser .subform tr").filter(function(){
			var $td = $(this).children("td:eq(0):contains(" + entity.device_type_id + ")");
			return $td.text() === entity.device_type_id;
		}).children("td:eq(1)").text();
		$("#add_name").val(device_type_name || entity.name).disable();
		$("#add_model_name").val(entity.model_name).disable();

		$("#add_manager").val(entity.applicator_operator_name);  
		$("#hidden_add_manager_operator_id").val(entity.applicator_id);  
		$("#add_comment").val("由订购单" + entity.order_no + "收货。" 
			+ (entity.nesssary_reason ? "\n" + entity.nesssary_reason : ""));

		$("#hidden_order_key").val(entity.order_key + "_" + entity.object_type);  
		$("#hidden_applicator_id").val(entity.applicator_id);
	} else {
		$("#add_name").val("").enable();
		$("#hidden_add_name").val("");
		$("#add_brand").val("").enable();
		$("#hidden_add_brand_id").val("");
		$("#add_comment").val("");
		$("#add_model_name").val("").enable();
		$("#add_manager").val("");  
		$("#hidden_add_manager_operator_id").val("");  

		$("#hidden_order_key").val("");  
		$("#hidden_applicator_id").val("");
	}

	//状态选择
    $("#add_status").bind("change", function() {
          //如果状态是遗失或者损坏--废弃日期可填
          if($(this).val()==2 || $(this).val()==3){
             $("#add_waste_date").show();
          }else{
             $("#add_waste_date").hide();
          }
    });
	
	$("#add_import_date").val($("#hidden_import_date").val());
	$("#body-mdl").hide();
	$("#body-detail").hide();
	$("#body-regist").show();
	$("#body-update").hide();	

	/*返回*/
	$("#goback").click(function(){
		$("#body-mdl").show();
		$("#body-detail").hide();
		$("#body-regist").hide();
	});
	
	$("#add_form").validate({
        rules:{
            manage_code:{
                required:true,
                maxlength : 9
            },
            devices_type_id:{
                required:true
            },
            model_name:{
                maxlength :32
            },
            location:{
                maxlength :10
            },
            manage_content:{
                maxlength:64
            },
            products_code:{
                maxlength:25
            },
            brand:{
                maxlength:32
            },
            section_id:{
                required:true
            },
            manage_level:{
                required:true
            },
            asset_no:{
                maxlength:8
            },
            section_id:{
                required:true
            },
            import_date:{
                required:true
            },
            status:{
                required:true
            }
        }
    });

    /*确认*/	
	$("#confirebutton").click(function(){
	  if ($("#add_form").valid()) {
		warningConfirm("是否新建管理编号为"+$("#add_manage_code").val()+", 品名为"+$("#add_name").val()+"的设备工具?", 
			function() {
	            var data={
			        "manage_code": $("#add_manage_code").val(),
			        "devices_type_id": $("#hidden_add_name").val(), 
			       //"name":$("#add_name").val(), 
			        "model_name":$("#add_model_name ").val(),
			        "manager_operator_id": $("#hidden_add_manager_operator_id").val(),
			        "compare_manager_operator_id": $("#hidden_applicator_id").val(), // 订购品收获时的申请者
			        "order_key": $("#hidden_order_key").val(), // 订购品收获时的设备工具治具订单 Key
			        "manage_level":$("#add_manage_level").val(),
			        "asset_no":$("#add_asset_no").val(),
			        "manage_content":$("#add_manage_content").val(), 
			        "products_code": $("#add_products_code").val(),
			       //"brand": $("#add_brand ").val(), 
			        "brand_id" : $("#hidden_add_brand_id").val(),
			        "section_id":$("#add_section_id ").val(),
			        "line_id": $("#add_line_id ").val(), 
			        "position_id": $("#hidden_add_position_id").val(),
			        "responsible_operator_id": $("#hidden_add_responsible_operator_id").val(),
			        "import_date": $("#add_import_date").val(),
			        "waste_date":$("#add_waste_date").val(),
			        "delete_flg":$("#add_delete_flg").val(),
			        "updated_by":$("#add_updated_by").val(),
			        "updated_time":$("#add_updated_time ").val(),
			        "status":$("#add_status").val(), 
			        "comment": $("#add_comment").val()  
			    }
			    if (typeof(add_method) === "string") {
			    	data["add_method"] = add_method;
			    }
	            // Ajax提交
	            $.ajax({
	                beforeSend : ajaxRequestType,
	                async : true,
	                url : servicePath + '?method=doinsert',
	                cache : false,
	                data :data,
	                type : "post",
	                dataType : "json",
	                success : ajaxSuccessCheck,
	                error : ajaxError,
	                complete : insert_handleComplete
	            });
			}, function() {
			// $("#editbutton").enable();
			}, "新建确认"
		)};
	});	
};
var insert_handleComplete = function(xhrobj, textStatus) {
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("#editarea", resInfo.errors);
        } else {
        	infoPop("新建已经完成。", null, "新建");
            // 重新查询
            findit();
            // 切回一览画面
            showList();
        }
    } catch (e) {
        console.log("name: " + e.name + " message: " + e.message + " lineNumber: "
                + e.lineNumber + " fileName: " + e.fileName);
    };
}
//双击一览详细画面
var showEdit = function(){

    //状态选择
    $("#update_status").bind("change", function() {
          //如果状态是遗失或者损坏--废弃日期可填
          if($(this).val()==2 || $(this).val()==3){
             $("#update_waste_date").show();
          }else{
             $("#update_waste_date").hide();
          }
    });
    //如果选中保管中--只能选择技术课
    /*if($("#update_status").val()=='3'){
       $("#update_section_id").disable(); 
       if($("#update_section_id").val()=='7'){
            $("#update_section_id").enable(); 
       };
    }*/
    $("#body-mdl").hide();
	$("#body-detail").show();

	var rowId=$("#list").jqGrid("getGridParam","selrow");//获取选中行的ID
	var rowData=$("#list").getRowData(rowId);	
    
    //页面隐藏设备工具ID
    $("#hidden_devices_manage_id").val(rowData.devices_manage_id);
    //管理编号
	$("#update_manage_code").val(rowData.manage_code);     
    //品名
	$("#update_name").val(rowData.name); 
    //隐藏设备工具品名ID
    $("#hidden_update_name").val(rowData.devices_type_id);
    //型号
    $("#update_model_name").val(rowData.model_name); 
   
    //管理员
    $("#update_manager").val(rowData.manager);  
    $("#hidden_update_manager_operator_id").val(rowData.manager_operator_id);
    //管理等级
    $("#update_manage_level").val(rowData.manage_level).trigger("change");
    //资产编号
    $("#update_asset_no").val(rowData.asset_no);
     //管理内容
    $("#update_manage_content").val(rowData.manage_content);
    //出厂编码
	$("#update_products_code").val(rowData.products_code);
    //厂商
	if (rowData.brand) {
	    $("#update_brand").val($(rowData.brand).text());
	} else {
	    $("#update_brand").val("");
	}
    $("#hidden_update_brand_id").val(rowData.brand_id || "");
     //状态
    $("#update_status").val(rowData.status).trigger("change"); 
    
    //课室ID
	$("#update_section_id").val(rowData.section_id).trigger("change");
    //工程ID
    $("#update_line_id").val(rowData.line_id).trigger("change"); 
    //工位ID
	$("#update_position_id").val(rowData.process_code);  
    
    $("#hidden_update_position_id").val(rowData.position_id);
    
    //发放者
    $("#update_provider").text(rowData.provider);
    
    //责任人
    //$("#update_responsible_operator_id").val(rowData.responsible_operator);     
    //$("#hidden_update_responsible_operator_id").val(rowData.responsible_operator_id);
    
    //导入日期
	$("#update_import_date").val(rowData.import_date);   
    //发放日期
    $("#update_provide_date").text(rowData.provide_date);  
    //废弃日期
	$("#update_waste_date").val(rowData.waste_date);    
    //更新时间
	$("#update_updated_time").text(rowData.updated_time);
     //备注
    $("#update_comment").val(rowData.comment);    

	/*返回*/
	$("#resetbutton3").click(function(){		
		$("#body-mdl").show();
		$("#body-detail").hide();
		$("#body-regist").hide();
	});
    
    $("#update_form").validate({
        rules:{
            manage_code:{
                required:true,
                maxlength : 9
            },
            manager_operator_id:{
                required:true
            },
            name:{
                required:true,
                maxlength :32
            },
            model_name:{
                maxlength :32
            },
            manage_content:{
                maxlength:64
            },
            products_code:{
                maxlength:25
            },
            brand:{
                maxlength:32
            },
            name:{
                required:true,
                maxlength :32
            },
            section_id:{
                required:true
            },
            manage_level:{
                required:true
            },
            section_id:{
                required:true
            },
            import_date:{
                required:true
            },
            status:{
                required:true
            }
        }
    });
	/*修改*/
	$("#updatebutton").click(function(){
	  if ($("#update_form").valid()) {
        $("#dialog_confrim").html("");
		warningConfirm("是否修改管理编号为"+$("#update_manage_code").val()+",品名为"+$("#update_name").val()+"的设备工具？", 
			function(){
				var data={
					"compare_status":rowData.status==$("#update_status").val(),
					"devices_manage_id": $("#hidden_devices_manage_id ").val(), 
					"manage_code": $("#update_manage_code").val(),
					"devices_type_id": $("#hidden_update_name").val(), 
					"model_name":$("#update_model_name ").val(),
					"manager_operator_id":$("#hidden_update_manager_operator_id").val(),
					"manage_level":$("#update_manage_level").val(),
					"asset_no":$("#update_asset_no").val(),
					"manage_content":$("#update_manage_content").val(), 
					"products_code": $("#update_products_code").val(),
			       //"brand": $("#update_brand ").val(), 
			        "brand_id" : $("#hidden_update_brand_id").val(),
					"section_id":$("#update_section_id ").val(),
					"line_id": $("#update_line_id ").val(), 
					"position_id": $("#hidden_update_position_id").val(),
					"responsible_operator_id":$("#hidden_update_responsible_operator_id").val(),
					"import_date":$("#update_import_date").val(),
					"provide_date":$("#update_provide_date ").text(),
					"waste_date":$("#update_waste_date ").val(),
					"delete_flg":$("#update_delete_flg ").val(),
					"updated_by":$("#update_updated_by ").val(),
					"status":$("#update_status").val(), 
					"comment": $("#update_comment").val()  
				}

	            // Ajax提交
			    $.ajax({
			        beforeSend : ajaxRequestType,
			        async : true,
			        url : servicePath + '?method=doupdate',
			        cache : false,
			        data :data,
			        type : "post",
			        dataType : "json",
			        success : ajaxSuccessCheck,
			        error : ajaxError,
			        complete : update_handleComplete
			    });
			}, 
	        null, 
	  		"修改确认");
    	}
	});	

	/*确认删除*/
	$("#delbutton").click(function(){
		warningConfirm("确认删除管理编号为"+$("#update_manage_code").val()+",品名为"+$("#update_name").val()+"的设备工具？", 
			function() {
                 var data={
                    "devices_manage_id": $("#hidden_devices_manage_id").val()
                 }
                  // Ajax提交
                 $.ajax({
                    beforeSend : ajaxRequestType,
                    async : true,
                    url : servicePath + '?method=dodelete',
                    cache : false,
                    data :data,
                    type : "post",
                    dataType : "json",
                    success : ajaxSuccessCheck,
                    error : ajaxError,
                    complete : delete_handleComplete
                 });
			},null,
			"删除确认"
		);
	});
};

var delete_handleComplete = function(xhrobj, textStatus) {
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("#editarea", resInfo.errors);
        } else {
			infoPop("删除已经完成。", null, "删除");
			// 重新查询
			findit(); 
			// 切回一览画面
			showList();
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "
                + e.lineNumber + " fileName: " + e.fileName);
    };
}

var update_handleComplete = function(xhrobj, textStatus) {
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("#editarea", resInfo.errors);
        } else {
			infoPop("修改已经完成。", null, "修改");
			// 重新查询
			findit(); 
			// 切回一览画面
			showList();
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "
                + e.lineNumber + " fileName: " + e.fileName);
    };
}

var showList = function(){
        $("#body-mdl").show();
        $("#body-detail").hide();
        $("#body-regist").hide();
}

var localSpareList = {};

var getSpareList = function(){
	var data ={
		"device_spare_type":"1"
	};
	var spareServicePath = "device_spare.do";
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : spareServicePath + '?method=search',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrObj, textStatus) {
			var resInfo = $.parseJSON(xhrObj.responseText);
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				localSpareList = resInfo.spareList;
				$("#add_spare_device_type_name").val("");
				$("#add_spare_model_name").val("");
				if (typeof(showSparelist) === "function") showSparelist(localSpareList);
			}
		}
	});
}

var localOrderList = [];

var getOrderList = function(){
	var data ={
		"object_type":"1",
		"inline_recept_flg" : 1
	};
	var orderServicePath = "device_jig_order.do";
	$.ajax({
		beforeSend : ajaxRequestType,
		async : true,
		url : orderServicePath + '?method=search',
		cache : false,
		data : data,
		type : "post",
		dataType : "json",
		success : ajaxSuccessCheck,
		error : ajaxError,
		complete : function(xhrObj, textStatus) {
			var resInfo = $.parseJSON(xhrObj.responseText);
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				var remoteOrderList = resInfo.orderList;
				localOrderList = [];
				for (var iRol in remoteOrderList) {
					var remoteOrder = remoteOrderList[iRol];
					remoteOrder.quantity = remoteOrder.quantity - (remoteOrder.confirm_quantity || 0);
					if (0 < remoteOrder.quantity) {
						localOrderList.push(remoteOrder);
					}
				}
				$("#add_order_device_type_name").val("");
				$("#add_order_model_name").val("");
				showOrderlist(localOrderList);
			}
		}
	});
}

var showOrderlist = function(listdata){
	if ($("#gbox_ord_list").length > 0) {
		$("#ord_list").jqGrid().clearGridData();// 清除
		$("#ord_list").jqGrid('setGridParam', {data : listdata}).trigger("reloadGrid", [ {current : false} ]);// 刷新列表
	} else {
		$("#ord_list").jqGrid({
			data : listdata,// 数据
			height :201,// rowheight*rowNum+1
			width : 640,
			rowheight : 10,
			shrinkToFit:true,
			datatype : "local",
			colNames : ['品名','型号','申请者','数量','理由/必要性','device_type_id','applicator_id','order_no','order_key','object_type'],
			colModel : [{name : 'name',index : 'name',width:80},
						{name : 'model_name',index : 'model_name',width:110},
						{name : 'applicator_operator_name',index : 'applicator_operator_name',width:60},
						{name : 'quantity',index : 'quantity',width:50,align:'right',sorttype:'integer',formatter:'integer'},
						{name : 'nesssary_reason',index : 'nesssary_reason',width:110},
						{name : 'device_type_id',index : 'device_type_id',hidden:true},
						{name : 'applicator_id',index : 'applicator_id',hidden:true},
						{name : 'order_no',index : 'order_no',hidden:true},
						{name : 'order_key',index : 'order_key',hidden:true},
						{name : 'object_type',index : 'object_type',hidden:true}
            ],
			rowNum : 20,
			toppager : false,
			pager : "#ord_listpager",
			viewrecords : true,
			caption : "",
			multiselect : false,
			gridview : true,
			pagerpos : 'right',
			pgbuttons : true, // 翻页按钮
			rownumbers : true,
			pginput : false,					
			recordpos : 'left',
			hidegrid : false,
			deselectAfterSort : false,
			viewsortcols : [ true, 'vertical', true ]
		});
	};
	var $add_order_dialog = $("#add_order_dialog");
	$add_order_dialog.dialog({
		position : 'center',
		title : "选择订购品",
		width : 660,
		height : 'auto',
		resizable : false,
		modal : true,
		buttons : {
			"选择":function() {
				var selRow = $("#ord_list").jqGrid("getGridParam", "selrow");
				if(selRow) {
					var rowData=$("#ord_list").getRowData(selRow);
					showAdd("order", rowData);
					$add_order_dialog.dialog('close');
				}
			},
			"关闭":function(){
				$add_order_dialog.dialog('close');
			}
		}
	});
}

var filterOrderList = function(){
	var order_device_type_name = $("#add_order_device_type_name").val();
	var order_model_name = $("#add_order_model_name").val();
	if (!order_device_type_name && !order_model_name) {
		showOrderlist(localOrderList);
		return;
	}

	var filtedList = [];
	for (var iSl in localOrderList) {
		var localOrder = localOrderList[iSl];
		if (order_device_type_name && localOrder["name"].indexOf(order_device_type_name) < 0) {
			continue;
		}
		if (order_model_name && localOrder["model_name"].indexOf(order_model_name) < 0) {
			continue;
		}
		filtedList.push(localOrder);
	}
	showOrderlist(filtedList);
}