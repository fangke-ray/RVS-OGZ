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

		responseMap.put("linePlanList", mapper.getTodayProductPlan(sectionId)); // TODO sectionId

		responseMap.put("linePlanCompleteList", mapper.getTodayCompleteMaterialCountByModels(sectionId, "00000000101"));
	}
}
