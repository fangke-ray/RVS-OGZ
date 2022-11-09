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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.service.LineLeaderService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;


public class LineSituationAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	/**
	 * 受理画面初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("LineSituationAction.init start" + req.getParameter("in"));
		String path = req.getRequestURI();
		String subpath = path.replaceAll(".*lineSituation(.*)\\.scan", "$1");
		
		log.info(subpath);

		String forward = FW_INIT;
		req.setAttribute("plan_name", "出货安排");
		if (subpath.toLowerCase().startsWith("d")) {
			req.setAttribute("line_id", "00000000012");
			req.setAttribute("line_name", "分解工程");
			req.setAttribute("plan_name", "再生计划");
			forward = "period";
		} else if (subpath.toLowerCase().startsWith("n")) {
			req.setAttribute("line_id", "00000000013");
			req.setAttribute("line_name", "ＮＳ工程");
			req.setAttribute("plan_name", "再生计划");
			forward = "ns";
		} else if (subpath.toLowerCase().startsWith("a")) {
			req.setAttribute("line_id", "00000000201");
			req.setAttribute("line_name", "290 拉");
			req.setAttribute("lm_tag", "2");
			forward = "period";
		} else if (subpath.toLowerCase().startsWith("b")) {
			req.setAttribute("line_id", "00000000202");
			req.setAttribute("line_name", "260 拉");
			req.setAttribute("lm_tag", "2");
			forward = "period";
		} else if (subpath.toLowerCase().startsWith("c")) {
			req.setAttribute("line_id", "00000000203");
			req.setAttribute("line_name", "细镜拉");
			req.setAttribute("lm_tag", "2");
			forward = "period";
		} else { //  if (subpath.startsWith("c"))
			req.setAttribute("line_id", "00000000014");
			req.setAttribute("line_name", "总组工程");
			req.setAttribute("plan_name", "出货安排");
		}

		if (subpath.endsWith("1")) {
			req.setAttribute("section_id", "00000000001");
			req.setAttribute("section_name", "修理生产G");
		} else	if (subpath.endsWith("0")) {
			req.setAttribute("section_id", "10000000001");
			req.setAttribute("section_name", "修理生产G NS 再生");
		} else {
			req.setAttribute("section_id", "00000000003");
			req.setAttribute("section_name", "维修２课");
		}
		// 迁移到页面
		actionForward = mapping.findForward(forward);

		log.info("LineSituationAction.init end" + req.getParameter("in"));
	}

	/**
	 * 工位画面初始取值处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void refresh(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("LineSituationAction.refresh start");
		Map<String, Object> callback = new HashMap<String, Object>();

		String section_id = req.getParameter("section_id");
		String isPeriod = req.getParameter("isPeriod");
		String s1pass = null;
		if (section_id != null && section_id.startsWith("1")) {
			section_id = "00000000001";
			s1pass = "pass";
		}
		String line_id = req.getParameter("line_id");

		LineLeaderService service = new LineLeaderService();

		// 取得今日计划暨作业对象一览
		service.getSituation(section_id, line_id, callback, isPeriod, s1pass != null, conn);

		// 取得工位仕挂一览
		if ("00000000201".equals(line_id)) {
			service.getChartContent(section_id, line_id, callback, isPeriod, s1pass, conn);
		} else {
			service.getChartContent(section_id, line_id, s1pass, conn, callback);
		}

		// 取得分解～NS
//		if ("00000000012".equals(line_id) || "00000000013".equals(line_id)) {
//			service.getComAndNsMatch(section_id, line_id, conn, callback);
//		}

		callback.put("opt_level", CodeListUtils.getGridOptions("material_level"));

		// 检查发生错误时报告错误信息
		callback.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, callback);

		log.info("LineSituationAction.refresh end");
	}
}
