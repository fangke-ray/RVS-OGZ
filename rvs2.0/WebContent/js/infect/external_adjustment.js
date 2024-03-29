var servicePath="external_adjustment.do";
var datalist=[];
var obj_effect_interval = {};

$(function(){
    $("#searcharea span.ui-icon,#listarea span.ui-icon").bind("click",function() {
        $(this).toggleClass('ui-icon-circle-triangle-n').toggleClass('ui-icon-circle-triangle-s');
        if ($(this).hasClass('ui-icon-circle-triangle-n')) {
            $(this).parent().parent().next().slideToggle("blind");
        } else {
            $(this).parent().parent().next().slideToggle("blind");
        }
    });
    
    /*按钮事件*/
    $("input.ui-button").button();
    $(".ui-buttonset").buttonset();
    
    //分发课室  责任工程 有效期  校验单位
    $("#search_section_id,#search_responsible_line_id,#search_effect_interval,#search_organization_type,#add_effect_interval,#add_organization_type,#update_organization_type").select2Buttons();
    
    //品名
    setReferChooser($("#hidden_devices_type_id"),$("#name_referchooser"));

    setReferChooser($("#hidden_search_brand_id"),$("#brand_referchooser"));
    setReferChooser($("#hidden_add_brand_id"),$("#brand_referchooser"));
    setReferChooser($("#hidden_update_brand_id"),$("#brand_referchooser"));

   	obj_effect_interval = selecOptions2Object($("#sEffectInterval").val());

     //校验日期  过期日期
    $("#search_checked_date_start,#search_checked_date_end,#search_available_end_date_start,#search_available_end_date_end,#add_checked_date,#update_checked_date").datepicker({
        showButtonPanel : true,
        dateFormat : "yy/mm/dd",
        currentText : "今天"
    });
    
    //检索
    $("#searchbutton").click(function(){
        $("#hidden_devices_type_id").data("post",$("#hidden_devices_type_id").val());//品名
        $("#search_brand").data("post",$("#search_brand").val());//厂商
        $("#search_model_name").data("post",$("#search_model_name").val());//型号
        $("#search_manage_code").data("post",$("#search_manage_code").val());//管理编号
        $("#search_measuring_instrument_manage_code").data("post",$("#search_measuring_instrument_manage_code").val());//管理编号
        $("#search_section_id").data("post",$("#search_section_id").val());//分发课室
        $("#search_responsible_line_id").data("post",$("#search_responsible_line_id").val());//责任工程
        $("#search_products_code").data("post",$("#search_products_code").val());//出厂编号
        $("#search_checked_date_start").data("post",$("#search_checked_date_start").val());//校验日期开始
        $("#search_checked_date_end").data("post",$("#search_checked_date_end").val());//校验日期结束
        $("#search_available_end_date_start").data("post",$("#search_available_end_date_start").val());//过期日期开始
        $("#search_available_end_date_end").data("post",$("#search_available_end_date_end").val());//过期日期结束
        $("#search_effect_interval").data("post",$("#search_effect_interval").val());//有效期
        $("#search_organization_type").data("post",$("#search_organization_type").val());//校验单位
        $("#search_institution_name").data("post",$("#search_institution_name").val());//校验机构名称
        $("#hidden_search_brand_id").data("post",$("#hidden_search_brand_id").val());//厂
        $("#search_idle_flg").data("post",$("#search_idle_flg > input:checked").val());//闲置状态
        findit();
        $("#tocheck").disable();
        $("#stop").disable();
    });
    
    $("#add_check_cost,#update_check_cost").keydown(function(evt){
        if (!((evt.keyCode >= 48 && evt.keyCode <= 57) ||  evt.keyCode==8 ||  evt.keyCode==46 || evt.keyCode==37 || evt.keyCode==39 || evt.keyCode==190)){
            return false;
        }
    });
    
    //清除
    $("#resetbutton").click(function(){
       reset();
       $("#tocheck").disable();
       $("#stop").disable();
    });
    
    //新建
    $("#addtbutton").click(function(){
         $.ajax({
	        beforeSend : ajaxRequestType,
	        async : true,
	        url : servicePath + '?method=manageCodeReferChooser',
	        cache : false,
	        data : null,
	        type : "post",
	        dataType : "json",
	        success : ajaxSuccessCheck,
	        error : ajaxError,
	        complete : manageCodeReferChooser_handleComplete
	    });
    });
    $("#addjbutton").click(function(){
         $.ajax({
	        beforeSend : ajaxRequestType,
	        async : true,
	        url : servicePath + '?method=manageCodeReferChooser',
	        cache : false,
	        data : null,
	        type : "post",
	        dataType : "json",
	        success : ajaxSuccessCheck,
	        error : ajaxError,
	        complete : manageCodeReferChooser_handleComplete
	    });
    });

	// 送检
	$("#tocheck").click(function() {
		var rowID = $("#list").jqGrid('getGridParam', 'selrow');
		if (!rowID) return;
		var rowData = $("#list").getRowData(rowID);

		warningConfirm("确认要送检管理编号为[" + rowData.manage_code + "]的记录吗？",
			function() {
				var data = {
					"devices_manage_id" : rowData.devices_manage_id,
					"object_type" : rowData.object_type
				}
				$.ajax({
					beforeSend : ajaxRequestType,
					async : true,
					url : servicePath + '?method=doChecking',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : doChecking_handleComplete
				});
			}, null, 
		"送检确认");
	});
    
    
    // 停止校验
	$("#stop").click(function() {
		var rowID = $("#list").jqGrid('getGridParam', 'selrow');
		var rowData = $("#list").getRowData(rowID);

		warningConfirm("确认要停止校验管理编号为[" + rowData.manage_code + "]的记录吗？",
			function() {
				var data = {
					"devices_manage_id" : rowData.devices_manage_id,
					"object_type" : rowData.object_type
				}
				$.ajax({
					beforeSend : ajaxRequestType,
					async : true,
					url : servicePath + '?method=doStop',
					cache : false,
					data : data,
					type : "post",
					dataType : "json",
					success : ajaxSuccessCheck,
					error : ajaxError,
					complete : doStop_handleComplete
				});
			}, null, 
		"停止校验确认");
	});

	$("#label_brand_add_button").click(function(){
		if ($("#hidden_add_brand_id").val()) {
			showBrandDetail($("#hidden_add_brand_id").val());
		}
	});
	$("#label_brand_update_button").click(function(){
		if ($("#hidden_update_brand_id").val()) {
			showBrandDetail($("#hidden_update_brand_id").val());
		}
	});

    reset();
    findit();
    
    $("#tocheck").disable();
    $("#stop").disable();
});

var doStop_handleComplete=function(xhrobj, textStatus){
	var resInfo = null;
    try {
    	// 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
    	if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("", resInfo.errors);
        } else {
        	findit();
        }
    }catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
    };
}

var manageCodeReferChooser_handleComplete=function(xhrobj, textStatus){
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("", resInfo.errors);
        } else {
            $("#managecode_referchooser table:nth-child(2)").html(resInfo.finished);

            setReferChooser($("#hidden_devices_manage_id2"),$("#managecode_referchooser"),null,searchBaseInfo);

            $("#add_manage_code").val("");//管理编号
	        $("#hidden_devices_manage_id").val("");
	        $("#label_name").text("");//品名
	        $("#label_brand").text("");//厂商
            $("#text_add_brand").val("");
            $("#hidden_add_brand_id").val("");//厂商
	        $("#label_model_name").text("");//型号
	        $("#label_products_code").text("");//出厂编号
	        $("#label_section_name").text("");//分发课室
	        $("#label_responsible_line_name").text("");//责任工程
	        $("#add_checked_date").val("").removeClass("errorarea-single");//校验日期
	        $("#add_effect_interval").val("").trigger("change");//有效期
	        $("#add_check_cost").val("");//校验费用
	        $("#add_organization_type").val("").trigger("change");//校验单位
	        $("#add_institution_name").val("");//校验机构名称
	        $("#add_idle_flg input:eq(0)").attr("checked", "checked").trigger("change"); // 闲置
	        $("#add_comment").val("");//备注
            
	        $("#add_form").validate({
	            rules:{
	                devices_manage_id:{
	                    required:true
	                },
	                checked_date:{
	                    required:true
	                },
	                effect_interval:{
	                    required:true
	                },
	                organization_type:{
	                    required:true
	                }
	            },
	            ignore:''
	        });
        
             $("#add").dialog({
	            position : 'center',
	            title : "新建检查机器校正",
	            width : 640,
	            height : 640,
	            resizable : false,
	            modal : true,
	            buttons : {
	                "新建":function(){
	                     if($("#add_form").valid()){
	                        var data={
	                            "devices_manage_id":$("#hidden_devices_manage_id2").val(),
	                            "brand_id":$("#hidden_add_brand_id").val(),
	                            "checked_date":$("#add_checked_date").val(),
	                            "effect_interval":$("#add_effect_interval").val(),
                                "check_cost":$("#add_check_cost").val(),
	                            "organization_type":$("#add_organization_type").val(),
                                "institution_name": $("#add_institution_name").val(),
                                "idle_flg" : $("#add_idle_flg input:checked").val(),
                                "comment": $("#add_comment").val()
	                        };
	                        $.ajax({
	                            beforeSend : ajaxRequestType,
	                            async : true,
	                            url : servicePath + '?method=doInsert',
	                            cache : false,
	                            data : data,
	                            type : "post",
	                            dataType : "json",
	                            success : ajaxSuccessCheck,
	                            error : ajaxError,
	                            complete : doInsert_handleComplete
	                        });
	                        
	                     }
	                },
	                "取消":function(){
	                    $("#add").dialog('close');
	                }
	            }
	       });        
            
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
    };
}

var reset=function(){
    $("#hidden_devices_type_id").data("post","").val("");//品名
    $("#search_name").val("");
    $("#search_brand").data("post","").val("");//厂商
    $("#search_model_name").data("post","").val("");//型号
    $("#search_manage_code").data("post","").val("");//管理编号
    $("#search_measuring_instrument_manage_code").data("post", "").val("");//管理编号
    $("#search_section_id").data("post","").val("").trigger("change");//分发课室
    $("#search_responsible_line_id").data("post","").val("").trigger("change");//责任工程
    $("#search_products_code").data("post","").val("");//出厂编号
    $("#search_checked_date_start").data("post","").val("");//校验日期开始
    $("#search_checked_date_end").data("post","").val("");//校验日期结束
    $("#search_available_end_date_start").data("post","").val("");//过期日期开始
    $("#search_available_end_date_end").data("post","").val("");//过期日期结束
    $("#search_effect_interval").data("post","").val("").trigger("change");//有效期
    $("#search_organization_type").data("post","").val("").trigger("change");//校验单位
    $("#search_institution_name").data("post","").val("");//校验机构名称
	$("#search_brand_id").val("");
    $("#hidden_search_brand_id").data("post","").val("");//厂
    $("#search_idle_flg").data("post","").children().eq(0).attr("check", true).trigger("change");// 闲置状态
}

//检索函数
var findit=function(){
    var data={
        "devices_type_id":$("#hidden_devices_type_id").data("post"),
        "brand":$("#search_brand").data("post"),
        "model_name":$("#search_model_name").data("post"),
        "manage_code":$("#search_manage_code").data("post"),
        "measuring_instrument_manage_code":$("#search_measuring_instrument_manage_code").data("post"),
        "products_code": $("#search_products_code").data("post"),
        "section_id":$("#search_section_id").data("post"),
        "line_id":$("#search_responsible_line_id").data("post"),
        "checked_date_start": $("#search_checked_date_start").data("post"),
        "checked_date_end":  $("#search_checked_date_end").data("post"),
        "available_end_date_start":$("#search_available_end_date_start").data("post"),
        "available_end_date_end":$("#search_available_end_date_end").data("post"),
        "effect_interval":$("#search_effect_interval").data("post"),
        "organization_type":$("#search_organization_type").data("post"),
        "institution_name":$("#search_institution_name").data("post"),
        "brand_id" : $("#hidden_search_brand_id").data("post"),
        "idle_flg" : $("#search_idle_flg").data("post")
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

//检索完成函数
var search_handleComplete=function(xhrobj, textStatus){
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("", resInfo.errors);
        } else {
            list(resInfo.finished);
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
    };
};

//一览
var list=function(datalist){
    if($("#gbox_list").length>0){
        $("#list").jqGrid().clearGridData();
        $("#list").jqGrid("setGridParam",{data:datalist}).trigger("reloadGrid",[{current:false}]);
    }else{
       $("#list").jqGrid({
            data:datalist,
            height:461,
            width: 992,
            rowheight: 23,
            datatype: "local",
            colNames:['','管理编号','计量器具管理编号','品名','厂商','厂商','型号','出厂编号','分发课室','责任工程','校验日期','过期日期','有效期','校验费用','检验单位','校验机构名称','','','','',''],
            colModel:[
                {name:'devices_manage_id',index:'devices_manage_id',hidden:true},
                {name:'manage_code',index:'manage_code',width:70},
 				{name:'measuring_instrument_manage_code',index:'measuring_instrument_manage_code',width:70},
                {name:'name',index:'name',width:140},
                {name:'brand_id',index:'brand_id',hidden:true},
				{name:'brand',index:'brand',width:100,align:'left',
					formatter : function(value, options, rData) {
	                    //当发放日期不为空时，发放者是当前更新人；如果为空时，发放者是空白
	                    if(rData.brand){
	                        return "<a href='javascript:showBrandDetail(\""+ rData.brand_id +"\")'>" + rData.brand + "</a>";
	                    }else{
	                        return "";
	                    }                           
	                }},
                {name:'model_name',index:'model_name',width:150},
                {name:'products_code',index:'products_code',width:100},
                {name:'section_name',index:'section_name',width:100},
                {name:'line_name',index:'line_name',width:100},
                {name:'checked_date',index:'checked_date',width:100,align:'center',sorttype:'date',formatter:'date',
                    formatoptions:{
                        srcformat:'Y/m/d',
                        newformat:'Y年m月'
                    }
                },
                {name:'available_end_date',index:'available_end_date',width:100,align:'center',sorttype:'date',formatter:'date',
                    formatoptions:{
                        srcformat:'Y/m/d',
                        newformat:'Y年m月'
                    }
                },
                {name:'effect_interval',index:'effect_interval',width:60,formatter:'select',align:'center',
                    editoptions:{
                        value:$("#sEffectInterval").val()
                    }
                },
                {name:'check_cost',index:'check_cost',width:100,align:'right'},
                {name:'organization_type',index:'organization_type',width:100,formatter:'select',
                    editoptions:{
                        value:$("#sOrganizationType").val()
                    }
                },
                {name:'institution_name',index:'institution_name',width:140},
                {name:'isover',index:'isover',hidden:true},
                {name:'checking_flg',index:'checking_flg',hidden:true},
                {name:'manage_level',index:'manage_level',hidden:true},
                {name:'idle_flg',index:'idle_flg',hidden:true},
                {name:'object_type',index:'object_type',hidden:true}
            ],
            rownumbers:true,
            toppager : false,
            rowNum : 20,
            pager : "#listpager",
            viewrecords : true,
            gridview : true,
            pagerpos : 'right',
            pgbuttons : true,
            pginput : false,
            onSelectRow:enableButton,
            recordpos : 'left',
            ondblClickRow:showDetail,
            viewsortcols : [true, 'vertical', true],
            gridComplete:function(){
                 // 得到显示到界面的id集合
                var IDS=$("#list").getDataIDs();
                //显示当前多少条记录
                var length=IDS.length;
                var pill = $("#list");
                for(var i=0;i<length;i++){
                      var rowData=$("#list").getRowData(IDS[i]);
                      var isover=rowData.isover;
                      var checking_flg=rowData.checking_flg;
                      var idle_flg=rowData.idle_flg;
                      if(idle_flg==1){//闲置
                            pill.find("tr#" +IDS[i] +" td").filter(function(idx,elm){return idx <= 3 || idx > 10}).addClass("spare");
                      } else if(checking_flg==1){//校验中
                            pill.find("tr#" +IDS[i] +" td").filter(function(idx,elm){return idx <= 3 || idx > 10}).addClass("wait");
                      }else{
                           if(isover==1){//过期需校验
                                pill.find("tr#" +IDS[i] +" td").filter(function(idx,elm){return idx <= 3 || idx > 10}).addClass("require_check");
                           }else if(isover==2){
                           		pill.find("tr#" +IDS[i] +" td").filter(function(idx,elm){return idx <= 3 || idx > 10}).addClass("close_to_check");
                           }
                      }
                }
                enableButton();
            }
       });
       $("#gbox_list .ui-jqgrid-hbox").before(
            '<div class="ui-widget-content" style="padding:4px;font-size:13px;">' +
                '备注：<div class="require_check" style="display:inline-block;height:28px;width:60px;text-align:center;line-height:28px;">红色</div>&nbsp;&nbsp;过期需校验'+
                '<div class="close_to_check" style="margin-left:20px;display:inline-block;height:28px;width:60px;text-align:center;line-height:28px;">橙色</div>&nbsp;&nbsp;临近到期需校验'+
                '<div class="wait" style="margin-left:20px;display:inline-block;height:28px;width:60px;text-align:center;line-height:28px;">灰色</div>&nbsp;&nbsp;校验中/等待报告'+
                '<div class="spare" style="margin-left:20px;display:inline-block;height:28px;width:60px;text-align:center;line-height:28px;">茶色</div>&nbsp;&nbsp;闲置'+
            '</div>'
       );
    }
};

/*管理编号chang事件*/
var searchBaseInfo=function(){
    var data={
		"devices_manage_id":$("#hidden_devices_manage_id2").val()
    };
    
    $.ajax({
        beforeSend : ajaxRequestType,
        async : true,
        url : servicePath + '?method=searchBaseInfo',
        cache : false,
        data : data,
        type : "post",
        dataType : "json",
        success : ajaxSuccessCheck,
        error : ajaxError,
        complete : searchBaseInfo_handleComplete
    });
}

var searchBaseInfo_handleComplete=function(xhrobj, textStatus){
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("", resInfo.errors);
        } else {
            if(resInfo.finished!=null){
            	$("div [id^='label']").text("");
            	
            	var object_type=$("#hidden_devices_manage_id2").val().split("-")[1];
            	if(object_type==1){
            		$("#label_brand").show();
            		$("#text_add_brand").hide();
            		$("#label_products_code").closest("tr").show();
            	}else{
            		$("#label_brand").hide();
            		$("#text_add_brand").show();
            		$("#label_products_code").closest("tr").hide();
            	}
            	
                $("#label_name").text(resInfo.finished.name);//品名
	            $("#label_brand").text(resInfo.finished.brand==null ? "(未指定)" :resInfo.finished.brand );//厂商
	            $("#text_add_brand").val(resInfo.finished.brand);
	            $("#hidden_add_brand_id").val(resInfo.finished.brand_id);//厂商
	            $("#label_model_name").text(resInfo.finished.model_name);//型号
	            $("#label_products_code").text(resInfo.finished.products_code==null ? "" : resInfo.finished.products_code);//出厂编号
	            $("#label_section_name").text(resInfo.finished.section_name);//分发课室
	            $("#label_responsible_line_name").text(resInfo.finished.line_name);//责任工程
            }else{
            	var object_type=$("#hidden_devices_manage_id2").val().split("-")[1];
            	if(object_type==1){
            		$("#label_brand").show();
            		$("#text_add_brand").hide();
            		$("#label_products_code").closest("tr").show();
            	}else{
            		$("#label_brand").hide();
            		$("#text_add_brand").show();
            		$("#label_products_code").closest("tr").hide();
            	}
            	
                $("#label_name").text("");//品名
		        $("#label_brand").text("");//厂商
	            $("#text_add_brand").val("");
	            $("#hidden_add_brand_id").val("");//厂商
		        $("#label_model_name").text("");//型号
		        $("#label_products_code").text("");//出厂编号
		        $("#label_section_name").text("");//分发课室
		        $("#label_responsible_line_name").text("");//责任工程
            }
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
    };
}

//新建完成函数
var doInsert_handleComplete=function(xhrobj, textStatus){
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("#add_form", resInfo.errors);
        } else {
            $("#add").dialog('close');
            $("#tocheck").disable();
            findit();
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
    };
};


var enableButton=function(){
    var rowID= $("#list").jqGrid('getGridParam','selrow');
    if (!rowID) {
    	$("#tocheck, #stop").disable();
    	return;
    }
    var rowData=$("#list").getRowData(rowID);
    var checking_flg=rowData.checking_flg;//是否是校验中
    if(checking_flg==1){//校验中不能送检
        $("#tocheck").disable();
    }else{
        $("#tocheck").enable();
    }
    
    $("#stop").enable();
}

//送检完成函数
var doChecking_handleComplete=function(xhrobj, textStatus){
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("", resInfo.errors);
        } else {
            findit();
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
    };
}

var showDetail=function(){
      var rowID= $("#list").jqGrid('getGridParam','selrow');
      var rowData=$("#list").getRowData(rowID);
      var data={
            "devices_manage_id":rowData.devices_manage_id,
            "object_type":rowData.object_type
      }
      $.ajax({
            beforeSend : ajaxRequestType,
            async : true,
            url : servicePath + '?method=searchDetailById',
            cache : false,
            data : data,
            type : "post",
            dataType : "json",
            success : ajaxSuccessCheck,
            error : ajaxError,
            complete : searchDetailById_handleComplete
      });
};

var searchDetailById_handleComplete=function(xhrobj, textStatus){
    var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("", resInfo.errors);
        } else {
        	$("#updateForm label").not("[for]").text("");
        	
            $("#detail_label_manage_code").text(resInfo.finished.manage_code);//管理编号
            $("#detail_label_name").text(resInfo.finished.name);//品名
            $("#detail_label_brand").text(resInfo.finished.brand);//厂商
            $("#text_update_brand").val(resInfo.finished.brand);//厂商
            var brand_id = resInfo.finished.brand_id || "";
            $("#hidden_update_brand_id").val(brand_id);//厂商
            if (resInfo.finished.object_type == 1 || checking_flg==1) {
	            $("#detail_label_brand").show();//厂商
	            $("#text_update_brand, #hidden_update_brand_id").hide();//厂商
            } else {
	            $("#detail_label_brand").hide();//厂商
	            $("#text_update_brand, #hidden_update_brand_id").show();//厂商
            }
            if (brand_id) {
            	$("#label_brand_update_button").show();
            } else {
            	$("#label_brand_update_button").hide();
            }
            $("#detail_label_model_name").text(resInfo.finished.model_name);//型号
            $("#detail_label_products_code").text(resInfo.finished.products_code);//出厂编号
            $("#detail_label_section_name").text(resInfo.finished.section_name);//分发课室
            $("#detail_label_responsible_line_name").text(resInfo.finished.line_name);//责任工程
            
            var checking_flg=resInfo.finished.checking_flg;//校验状态
            if(checking_flg==1){//校验中
            	$("#update_checked_date").show();
            	$("#update_checked_date").val(resInfo.finished.checked_date);//校验日期
            	$("#detail_label_checked_date").hide();
            }else{
            	$("#update_checked_date").hide();
            	$("#detail_label_checked_date").show();
            	$("#detail_label_checked_date").text(resInfo.finished.checked_date);//校验日期
            }
          
            $("#detail_label_available_end_date").text(resInfo.finished.available_end_date);//过期日期
            
            var effect_interval=resInfo.finished.effect_interval;//有效期
            $("#detail_label_effect_interval").text(obj_effect_interval[resInfo.finished.effect_interval]);

            $("#hidden_effect_interval").val(effect_interval);
            $("#hidden_devices_manage_id").val(resInfo.finished.devices_manage_id);
            $("#hidden_checked_date").val(resInfo.finished.checked_date);//

            $("#update_organization_type").val(resInfo.finished.organization_type).trigger("change");//校验单位

            $("#update_check_cost").val(resInfo.finished.check_cost);//校验费用

            $("#update_idle_flg input[value=" + resInfo.finished.idle_flg + "]").attr("checked", true).trigger("change"); // 闲置

            $("#update_institution_name").val(resInfo.finished.institution_name);//校验机构名称
            
            $("#update_comment").val(resInfo.finished.comment);//备注
            

            $("#updateForm").validate({
	            rules:{
	                checked_date:{
	                    required:true
	                },
	                check_cost:{
	                	number:true
	                }
	            }
	        });
            
            $("#detail").dialog({
		        position : 'center',
		        title : "检查机器校正详细信息",
		        width : 400,
		        height : 660,
		        resizable : false,
		        modal : true,
		        buttons : {
		            "更新":function(){
		            	if($("#updateForm").valid()){
		            		var data="";
		            		if(checking_flg==1){//校验中
		            			data={
			            			"devices_manage_id": $("#hidden_devices_manage_id").val(),
			            			"checked_date": $("#update_checked_date").val(),
			            			"checked_date_start":$("#hidden_checked_date").val(),//用于比较
			            			"effect_interval":$("#hidden_effect_interval").val(),//有效期
			            			"check_cost": $("#update_check_cost").val(),//校验费用
			            			"organization_type":$("#update_organization_type").val(),//校验单位
			            			"institution_name": $("#update_institution_name").val(),//校验机构名称
			            			"comment":$("#update_comment").val(),//备注
			            			"checking_flg":checking_flg,//校验状态
	                                "idle_flg" : $("#update_idle_flg input:checked").val(),
			            			"object_type":resInfo.finished.object_type
				            	 };
		            		}else{
		            			data={
			            			"devices_manage_id": $("#hidden_devices_manage_id").val(),
			            			"brand_id": $("#hidden_update_brand_id").val(),
			            			"check_cost": $("#update_check_cost").val(),
			            			"organization_type":$("#update_organization_type").val(),
			            			"institution_name": $("#update_institution_name").val(),
			            			"comment":$("#update_comment").val(),
			            			"checking_flg":checking_flg,
	                                "idle_flg" : $("#update_idle_flg input:checked").val(),
			            			"object_type":resInfo.finished.object_type
				            	 };
		            		}
		            		
		            	    $.ajax({
	            	            beforeSend : ajaxRequestType,
	            	            async : true,
	            	            url : servicePath + '?method=doUpdate',
	            	            cache : false,
	            	            data : data,
	            	            type : "post",
	            	            dataType : "json",
	            	            success : ajaxSuccessCheck,
	            	            error : ajaxError,
	            	            complete : doUpdate_handleComplete
		            	    });
		            	}
		            },
		            "取消":function(){
		                $("#detail").dialog('close');
		            }
		        }
		   });        
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
    };
};

var doUpdate_handleComplete=function(xhrobj, textStatus){
 	var resInfo = null;
    try {
        // 以Object形式读取JSON
        eval('resInfo =' + xhrobj.responseText);
        if (resInfo.errors.length > 0) {
            // 共通出错信息框
            treatBackMessages("", resInfo.errors);
        } else {
        	$("#detail").dialog('close');
            findit();
        }
    } catch (e) {
        alert("name: " + e.name + " message: " + e.message + " lineNumber: "+ e.lineNumber + " fileName: " + e.fileName);
    };
}
