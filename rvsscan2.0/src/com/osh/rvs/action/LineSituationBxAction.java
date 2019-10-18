/**
 * 系统名：OGZ-RVS<br>
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

import com.osh.rvs.service.LineLeaderService;
import com.osh.rvs.service.LineSituationCellService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;


public class LineSituationBxAction extends BaseAction {

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

		log.info("LineSituationBxAction.init start" + req.getParameter("in"));
		String path = req.getRequestURI();
		String subpath = path.replaceAll(".*lineSituation(.*)\\.scan", "$1");
		
		log.info(subpath);

		String forward = FW_INIT;
		req.setAttribute("line_id", "00000000101");
		req.setAttribute("line_name", "组装/检查工程");
		req.setAttribute("plan_name", "组装计划");

		req.setAttribute("section_id", "00000000009");
		req.setAttribute("section_name", "组立T");

		// 迁移到页面
		actionForward = mapping.findForward(forward);

		log.info("LineSituationBxAction.init end" + req.getParameter("in"));
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

		log.info("LineSituationBxAction.refresh start");
		Map<String, Object> callback = new HashMap<String, Object>();

		String section_id = req.getParameter("section_id");

		LineSituationCellService lineSituationCellService = new LineSituationCellService();
		LineLeaderService lineLeaderService = new LineLeaderService();

		// 计划台数,产出台数
		lineSituationCellService.getSituation(callback, section_id, conn);

		String[] arrLineIds = { "00000000101", "00000000102"};
		for (int i = 0; i < arrLineIds.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();

			boolean delLast = false;
			if (i == 1) {
				delLast = true;
			}

			lineLeaderService.getChartContent(section_id, arrLineIds[i], map, null, conn, delLast);

			if (!callback.containsKey("categories")) {
				callback.putAll(map);
			} else {
				List<String> allPositions = (List<String>) callback.get("categories");
				List<Object> allCounts = (List<Object>) callback.get("counts");
				List<Integer> allOverlines = (List<Integer>) callback.get("overlines");
				List<Object> allLightFixCounts = (List<Object>) callback.get("light_fix_counts");

				List<String> positions = (List<String>) map.get("categories");
				List<Object> counts = (List<Object>) map.get("counts");
				List<Integer> overlines = (List<Integer>) map.get("overlines");
				List<Object> lightFixCounts = (List<Object>) map.get("light_fix_counts");

				allPositions.addAll(positions);
				allCounts.addAll(counts);
				allOverlines.addAll(overlines);
				allLightFixCounts.addAll(lightFixCounts);

				callback.put("categories", allPositions);
				callback.put("counts", allCounts);
				callback.put("overlines", allOverlines);
				callback.put("light_fix_counts", allLightFixCounts);
			}
		}

		// 检查发生错误时报告错误信息
		callback.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, callback);

		log.info("LineSituationBxAction.refresh end");
	}
}
