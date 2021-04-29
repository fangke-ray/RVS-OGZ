/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：维修对象机种系统管理事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;


public class BreakAction extends BaseAction {

	private final Logger log = Logger.getLogger(getClass());

	/**
	 * 菜单初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("BreakAction.init start");

		Map<String, Object> breakMessage = (Map<String, Object>) req.getSession().getAttribute("break_cb");

		List<MsgInfo> msgInfos = new ArrayList<MsgInfo>();
		String requestOccur = null;
		String requestMethod = null;
		String occurTime = null;

		if (breakMessage != null) {
			msgInfos = (List<MsgInfo>) breakMessage.get("errors");
			requestOccur = (String) breakMessage.get("request_occur");
			requestMethod = (String) breakMessage.get("request_method");
			if (breakMessage.get("occur_time") != null) {
				occurTime = breakMessage.get("occur_time").toString();
			}
//			req.getSession().removeAttribute("break_cb");
		}

		// 报告错误信息
		req.setAttribute("errors", msgInfos);
		req.setAttribute("request_occur", requestOccur);
		req.setAttribute("request_method", requestMethod);
		req.setAttribute("occur_time", occurTime);
		// 迁移到页面
		actionForward = mapping.findForward(FW_GLOBELBREAK);

		log.info("BreakAction.init end");
	}
}
