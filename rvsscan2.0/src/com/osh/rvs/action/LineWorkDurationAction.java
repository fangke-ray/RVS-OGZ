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


public class LineWorkDurationAction extends BaseAction {

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

		log.info("LineWorkDurationAction.init start");

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		String line_id = req.getParameter("line_id");

		String lineName = "单元拉";
		req.setAttribute("line_id", "00000000050,00000000054,00000000060,00000000061");
		if ("12".equals(line_id)) {
			lineName = "分解";
			req.setAttribute("line_id", "00000000012");
		} else if ("13".equals(line_id)) {
			lineName = "NS ";
			req.setAttribute("line_id", "00000000013");
		} else if ("14".equals(line_id)){
			lineName = "总组";
			req.setAttribute("line_id", "00000000014");
		} else if ("201".equals(line_id)) {
			lineName = "290 拉 ";
			req.setAttribute("line_id", "00000000201");
		} else if ("202".equals(line_id)) {
			lineName = "260 拉 ";
			req.setAttribute("line_id", "00000000202");
		} else if ("203".equals(line_id)) {
			lineName = "细镜拉 ";
			req.setAttribute("line_id", "00000000203");
		}
		req.setAttribute("line_name", lineName);
		req.setAttribute("standard_column", service.getStandardColumn(null, conn));

		log.info("LineWorkDurationAction.init end");
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

		log.info("LineWorkDurationAction.refresh start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String line_id = req.getParameter("line_id");
		String [] arrLineIds = line_id.split(",");

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		listResponse.put("productionFeatures", service.getOperatorFeatures(arrLineIds, conn));

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("LineWorkDurationAction.refresh end");
	}
}
