var scrollInterval;
$(function () {
	var refresh = function() {
		// Ajax提交
		$.ajax({
			beforeSend : ajaxRequestType,
			async : true,
			url : 'scheduledMaterial.scan?method=refresh&s=' + new Date().getTime(),
			cache : false,
			data : null,
			type : "post",
			dataType : "json",
			success : ajaxSuccessCheck,
			error : ajaxError,
			complete : search_handleComplete
		});
	};
	
	document.addEventListener('touchmove', function (e) { e.preventDefault();}, {capture: false,passive: false});
	
	// 定义滚动容器
	var myScroll = new IScroll('#scroll',{});
	
	function search_handleComplete(xhrobj, textStatus) {
		clearInterval(scrollInterval);
		
		var resInfo = null;
		try {
			// 以Object形式读取JSON
			eval('resInfo =' + xhrobj.responseText);
			
			if (resInfo.errors.length > 0) {
				// 共通出错信息框
				treatBackMessages(null, resInfo.errors);
			} else {
				var $tbody = $("#table tbody");
				var list = resInfo.list;

				if (list.length == 0) {
					$("#tip").show().siblings().hide();
					$tbody.html("");
					return;
				} else {
					$("#tip").hide().siblings().show();
					
					var content = "";
					list.forEach(function(item,index,array){
						var process_code = item.process_code || '';
						var operate_result_name = item.operate_result_name || '';
						var break_off = '';
						var rework = '';
						if(process_code){
							if(item.break_off == 1){
								break_off = "有未处理中断";
							}
							if(item.rework == 1){
								rework = "反过工";
							}
						}

						content += '<tr>';
						content	+= '<td class="omr">' + item.omr_notifi_no + '</td>';
						content	+= '<td class="model" kind=' + item.kind + '>' + item.model_name + '</td>';
						content	+= '<td class="serial_no">' + item.serial_no + '</td>';
						content	+= '<td class="level" level=' + item.level + '>' + item.level_name+ '</td>';
						content	+= '<td class="process_code">' + process_code + '</td>';
						content	+= '<td class="status">' + operate_result_name;
						
						break_off && (content += ' <span class="label label-warning">' + break_off + '</span>');
						rework	&& (content += ' <span class="label label-info">' + rework + '</span>');
						
						content	+= '</td>';
						content += '</tr>';
					});
					
					$tbody.html(content);
					
					$("#table tbody tr:eq(0) > td").each(function(index,ele){
						$("#title thead tr:eq(0) > td").eq(index).css("width",$(ele).outerWidth()+"px");
					});
					
					//重新渲染滚动容器
					myScroll.refresh();
					//定位到顶部
					myScroll.scrollTo(0,0);
					
					var step = $("#table tbody tr:eq(0)").height();
					
					// 最大滚动距离(负数表示向下)
					var maxScrollY = Math.abs(myScroll.maxScrollY);
					var scrollY = 0;
					if(maxScrollY > 20){
						scrollInterval = setInterval(function(){
							if(scrollY >= maxScrollY){
								myScroll.scrollTo(0,0);
								scrollY = 0;
							}else{
								scrollY = scrollY + step;
								myScroll.scrollTo(0,-scrollY,2000,IScroll.utils.ease.circular);
							}
						},2500);
					}
				}
			}
		}catch(e){
		};
	}
	
	refresh();
	
	setInterval(refresh, 3*60*1000);
});