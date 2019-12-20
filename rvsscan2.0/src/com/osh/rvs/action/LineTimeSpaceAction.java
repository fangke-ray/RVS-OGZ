/**
 * 系统名：OSH-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：维修对象机种系统管理事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.service.LineTimespaceService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;


public class LineTimeSpaceAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());
	private LineTimespaceService service = new LineTimespaceService();

	/**
	 * 菜单初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("LineTimeSpaceAction.init start");

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		String line_id = req.getParameter("line_id");

		String lineName = "总组";
		req.setAttribute("line_id", "00000000014");
		if ("12".equals(line_id)) {
			lineName = "分解";
			req.setAttribute("line_id", "00000000012");
			actionForward = mapping.findForward(FW_INIT + "_dec");
		}
		if ("13".equals(line_id)) {
			lineName = "NS ";
			req.setAttribute("line_id", "00000000013");
			actionForward = mapping.findForward(FW_INIT + "_ns");
		}
		if ("101".equals(line_id)) {
			lineName = "组装/检查";
			req.setAttribute("line_id", "00000000101");
			actionForward = mapping.findForward(FW_INIT + "_bx");
		}
		req.setAttribute("line_name", lineName);
		req.setAttribute("standard_column", service.getStandardColumn(lineName, conn));

		log.info("LineTimeSpaceAction.init end");
	}

	/**
	 * 菜单初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void refresh(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("LineTimeSpaceAction.refresh start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String line_id = req.getParameter("line_id");
		if (line_id == null) {
			line_id = "00000000014";
		}

		String px = req.getParameter("px");

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		listResponse.put("productionFeatures", service.getProductionFeatures(line_id, px, conn));

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("LineTimeSpaceAction.refresh end");
	}
}
