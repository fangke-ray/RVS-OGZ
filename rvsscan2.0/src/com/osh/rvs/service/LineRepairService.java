package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.mapper.LineLeaderMapper;
import com.osh.rvs.mapper.LineRepairMapper;

public class LineRepairService {

	/**
	 * 取得等待投线，维修，零件的台数
	 * @param section_id
	 * @param line_id
	 * @param conn
	 * @param listResponse
	 */
	public void getSituation(String section_id, String line_id, Map<String, Object> responseMap, SqlSession conn) {
		getSituation(section_id, line_id, responseMap, null, conn);
	}
	public void getSituation(String section_id, String line_id, Map<String, Object> responseMap, String isPeriod, SqlSession conn) {
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		LineRepairMapper repairDao = conn.getMapper(LineRepairMapper.class);

		// 等待投线
		responseMap.put("waiting_cast", repairDao.getWaitingRepairMaterialCounts());
		// 等待维修
		responseMap.put("waiting_repair", dao.getWorkingMaterialCounts(section_id, null)); 
		// 等待零件
		responseMap.put("waiting_parts", repairDao.getWaitingPartsMaterialCounts(section_id));

		// 取得今日计划件数
		responseMap.put("endoEye_plan", dao.getTodayPlanMaterialCounts(section_id, line_id, "06"));
		responseMap.put("device_plan", dao.getTodayPlanMaterialCounts(section_id, line_id, "07"));

		// 取得今日计划内完成件数
		responseMap.put("endoEye_plan_complete", dao.getTodayCompleteMaterialCounts(section_id, line_id, "06"));
		responseMap.put("device_plan_complete", dao.getTodayCompleteMaterialCounts(section_id, line_id, "07"));
	}

	/**
	 * 仕挂分布图表
	 * @param section_id
	 * @param line_id
	 * @param conn
	 * @param listResponse
	 */
	public void getChartContent(String section_id, String line_id, Map<String, Object> responseMap, SqlSession conn) {
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		List<Map<String, String>> workingOfPositions = dao.getWorkingOfPositions(section_id, null);

		// 数据整合
		List<Map<String, String>> newWorkingOfPositions = new ArrayList<Map<String, String>>();

		for (int i=0; i < workingOfPositions.size(); i+=2){
			Map<String, String> workingOfPositionH = workingOfPositions.get(i);
			Map<String, String> workingOfPositionL = workingOfPositions.get(i+1);
			String light_division_flg = workingOfPositionH.get("LIGHT_DIVISION_FLG");
			String process_code = workingOfPositionH.get("PROCESS_CODE");

			boolean depar = false;
			if ("1".equals(light_division_flg)) { // 分线
				depar = true;
				if ("0".equals(workingOfPositionL.get("material_count"))
						&& "0".equals(workingOfPositionL.get("light_fix_count"))) { // B线无仕挂
					depar = false;
				}
			}

			if (depar) { // 分线
				workingOfPositionH.put("PROCESS_CODE", process_code + "A");
				workingOfPositionL.put("PROCESS_CODE", process_code + "B");
				newWorkingOfPositions.add(workingOfPositionH);
				newWorkingOfPositions.add(workingOfPositionL);
			} else { // 不分
				Float fCount = 0f;
				Float light_fix_count = 0f;
				try {
					fCount = Float.parseFloat(workingOfPositionH.get("material_count"))
							+ Float.parseFloat(workingOfPositionL.get("material_count"));
					light_fix_count = Float.parseFloat(workingOfPositionH.get("light_fix_count"))
							+ Float.parseFloat(workingOfPositionL.get("light_fix_count"));
				} catch (NumberFormatException e) {
				}
				workingOfPositionH.put("material_count", String.valueOf(fCount));
				workingOfPositionH.put("light_fix_count", String.valueOf(light_fix_count));
				newWorkingOfPositions.add(workingOfPositionH);
			}
		}

		List<String> positions = new ArrayList<String>();
		List<Integer> overlines = new ArrayList<Integer>();
		List<Object> counts = new ArrayList<Object>();
		List<Object> light_fix_counts = new ArrayList<Object>();

		for (Map<String, String> workingOfPosition : newWorkingOfPositions){			
			String process_code = workingOfPosition.get("PROCESS_CODE");
			if (process_code.endsWith("A") || process_code.endsWith("B")) {
				process_code = process_code.substring(0, process_code.length() - 1);
			}
//			if ("400".equals(process_code)) {
//				positions.add("" + workingOfPosition.get("PROCESS_CODE") + " " + workingOfPosition.get("NAME") + "\n(x 10)");
//			} else {
				positions.add("<a href=\"javaScript:positionFilter('"+workingOfPosition.get("POSITION_ID")+"')\">" + workingOfPosition.get("PROCESS_CODE") + " " + workingOfPosition.get("NAME") + "</a>");
//			}
			
			String sWaitingflow = RvsUtils.getWaitingflow(section_id, null, process_code);

			Integer iWaitingflow = null;
			if (sWaitingflow != null) {
				try {
					iWaitingflow = Integer.parseInt(sWaitingflow);
					if (workingOfPosition.get("PROCESS_CODE").endsWith("B")) {
						iWaitingflow = 6;
					}
				} catch (NumberFormatException e) {
				}
			}
			overlines.add(iWaitingflow);
			overlines.add(iWaitingflow);
			overlines.add(iWaitingflow);
			overlines.add(null);

			// 大修理数据
			Float fCount = 0f;
			try {
				fCount = Float.parseFloat(workingOfPosition.get("material_count"));
				if ("400".equals(process_code)) {
					fCount /= 10;
				}
			} catch (NumberFormatException e) {
			}

			// 小修理数据
			Float light_fix_count = 0f;
			try {
				light_fix_count = Float.parseFloat(workingOfPosition.get("light_fix_count"));
			} catch (NumberFormatException e) {
			}
			// 合计
			Float total = fCount + light_fix_count;

			Map<String, Object> series = new HashMap<String, Object>();
			Map<String, Object> color = new HashMap<String, Object>();
			Map<String, Object> linearGradient = new HashMap<String, Object>();
			linearGradient.put("x1", "0");
			linearGradient.put("x2", "0");
			linearGradient.put("y1", "1");
			linearGradient.put("y2", "0");
			color.put("linearGradient", linearGradient);
			List<ArrayList<Object>> stops = new ArrayList<ArrayList<Object>>();
			ArrayList<Object> stop = new ArrayList<Object>();
			if (iWaitingflow != null && iWaitingflow != 0 && fCount > iWaitingflow && light_fix_count == 0){
				// 超过等待去上限的{color : '#f04e08', y : 47}
				stop = new ArrayList<Object>();
				stop.add("0.7");
				stop.add("#92D050");
				stops.add(stop);
				stop = new ArrayList<Object>();
				stop.add("0.8");
				stop.add("#FFC000");
				stops.add(stop);
				color.put("stops", stops);
				series.put("color", color);
				series.put("y", fCount);
				counts.add(series);
			} else {
				counts.add(fCount);
			}

			if (iWaitingflow != null && iWaitingflow != 0
					&& (light_fix_count > iWaitingflow || (total > iWaitingflow && light_fix_count > 0))) {
				stop = new ArrayList<Object>();
				stop.add("0.7");
				stop.add("#cc76cc");
				stops.add(stop);
				stop = new ArrayList<Object>();
				stop.add("0.8");
				stop.add("#FFC000");
				stops.add(stop);
				color.put("stops", stops);
				series.put("color", color);
				series.put("y", light_fix_count);
				light_fix_counts.add(series);
			} else {
				if (light_fix_count == 0) {
					light_fix_counts.add(null);
				} else {
					light_fix_counts.add(light_fix_count);
				}
			}
		}

		if (overlines.size() > 0)
			overlines.remove(overlines.size() - 1);

		responseMap.put("categories", positions);
		responseMap.put("overlines", overlines);
		responseMap.put("counts", counts);
		responseMap.put("light_fix_counts", light_fix_counts);

		return;
	}
}
