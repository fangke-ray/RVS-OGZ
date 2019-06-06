package com.osh.rvs.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.service.LineLeaderService;
import com.osh.rvs.service.LineSituationCellService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;

/**
 * 单元拉工程展示
 * 
 * @author liuxb
 * 
 */
public class LineSituationCellAction extends BaseAction {

	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("LineSituationCellAction.init start");

		req.setAttribute("line_name", "单元拉");
		req.setAttribute("section_name", "修理生产G");
		req.setAttribute("section_id", "00000000001");

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("LineSituationCellAction.init end");
	}

	@SuppressWarnings("unchecked")
	public void refresh(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("LineSituationCellAction.refresh start");

		Map<String, Object> callback = new HashMap<String, Object>();

		String section_id = req.getParameter("section_id");

		LineSituationCellService lineSituationCellService = new LineSituationCellService();
		LineLeaderService lineLeaderService = new LineLeaderService();

		// 计划台数,产出台数
		lineSituationCellService.getSituation(callback, section_id, conn);

		String[] arrLineIds = { "00000000054", "00000000050", "00000000060", "00000000061" };
		for (int i = 0; i < arrLineIds.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();

			boolean delLast = false;
			if (i == 3) {
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

		log.info("LineSituationCellAction.refresh end");
	}
}
