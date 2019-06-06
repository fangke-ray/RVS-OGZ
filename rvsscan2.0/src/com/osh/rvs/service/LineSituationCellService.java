package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.mapper.LineLeaderMapper;

/**
 * 单元拉
 * 
 * @author liuxb
 * 
 */
public class LineSituationCellService {
	public void getSituation(Map<String, Object> responseMap, String sectionId, SqlSession conn) {
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("00000000054", "中小修工程");
		map.put("00000000050", "外科硬镜修理工程");
		map.put("00000000060", "纤维镜分解工程");
		map.put("00000000061", "纤维镜总组工程");

		for (String lineId : map.keySet()) {
			Map<String, Object> childMap = new HashMap<String, Object>();
			String lineName = map.get(lineId);

			childMap.put("line_id", lineId);
			// 今日计划台数
			childMap.put("plan", PathConsts.SCHEDULE_SETTINGS.get("daily.schedule." + lineName));
			// 今日产出台数
			childMap.put("plan_complete", dao.getProduceActualOfLine(sectionId, lineId));

			list.add(childMap);
		}

		responseMap.put("linePlanList", list);
	}
}
