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

import com.osh.rvs.form.MaterialForm;
import com.osh.rvs.service.MaterialService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.DateUtil;

/**
 * 纳期维修品
 * 
 * @Description
 * @author dell
 * @date 2019-12-20 下午4:35:17
 */
public class ScheduledMaterialAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("ScheduledMaterialAction.init start");

		// 当天
		req.setAttribute("today", DateUtil.toString(Calendar.getInstance().getTime(), DateUtil.DATE_PATTERN));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("ScheduledMaterialAction.init end");
	}

	public void refresh(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("ScheduledMaterialAction.refresh start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String curDate = DateUtil.toString(Calendar.getInstance().getTime(), DateUtil.DATE_PATTERN);

		MaterialService service = new MaterialService();
		List<MaterialForm> list = service.searchScheduld(curDate, conn);
		listResponse.put("list", list);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);
		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("ScheduledMaterialAction.refresh end");
	}

}
