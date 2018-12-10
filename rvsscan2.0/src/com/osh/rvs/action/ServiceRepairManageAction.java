package com.osh.rvs.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.service.ServiceRepairManageService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;

/**
 * 146P保修期内返品+QIS品分析
 * @author lxb
 *
 */
public class ServiceRepairManageAction extends BaseAction {
	private Logger log=Logger.getLogger(getClass());
	
	public void init(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSession conn)throws Exception {
		log.info("ServiceRepairManageAction.init start");
		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);
		
		log.info("ServiceRepairManageAction.init end");
	}
	
	public void search(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSession conn)throws Exception{
		log.info("ServiceRepairManageAction.search start");
		ServiceRepairManageService service=new ServiceRepairManageService();
		
		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> msgInfoes = new ArrayList<MsgInfo>();

		Calendar cal = Calendar.getInstance();

		Map<String, Object> retData = service.getData(cal, conn);
		
		Map<String, Object> currentData=service.getCurrentData(cal, conn);
		
		listResponse.put("retData", retData);
		listResponse.put("currentData", currentData);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", msgInfoes);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);
		
		log.info("ServiceRepairManageAction.search end");
	}
}
