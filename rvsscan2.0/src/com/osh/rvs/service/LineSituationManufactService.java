package com.osh.rvs.service;

import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.mapper.LineLeaderMapper;

/**
 * 制造流水线
 * 
 * @author gonglm
 * 
 */
public class LineSituationManufactService {
	public void getSituation(Map<String, Object> responseMap, String sectionId, SqlSession conn) {
		LineLeaderMapper mapper = conn.getMapper(LineLeaderMapper.class);

		// 工程仕挂总数
		responseMap.put("sikake", mapper.getWorkingMaterialCounts(sectionId, "101", null, null, null));

		responseMap.put("linePlanList", mapper.getTodayProductPlan(sectionId)); // TODO sectionId

		responseMap.put("linePlanCompleteList", mapper.getTodayCompleteMaterialCountByModels(sectionId, "00000000101"));
	}
}
