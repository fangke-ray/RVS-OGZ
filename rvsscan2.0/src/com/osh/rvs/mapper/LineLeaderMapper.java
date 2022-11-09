package com.osh.rvs.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.LineLeaderEntity;

public interface LineLeaderMapper {

	// 取得工程内各工位，及各工位的仕掛
	public List<Map<String, String>> getWorkingOfPositions(@Param("section_id") String section_id, @Param("line_id") String line_id);
	public List<Map<String, String>> getWorkingOfPositionsS1passed(@Param("section_id") String section_id, @Param("line_id") String line_id);

	// 取得工程内全部
	public List<LineLeaderEntity> getWorkingMaterials(@Param("section_id") String section_id, @Param("line_id") String line_id, @Param("position_id") String position_id);

	// 取得工程内全部件数
	public long getWorkingMaterialCounts(@Param("section_id") String section_id, @Param("line_id") String line_id, @Param("rank") String rank, @Param("cell") String cell, @Param("s1_pass") String s1_pass);

	// 当日计划件数
	public long getTodayCompleteMaterialCounts(@Param("section_id") String section_id, @Param("line_id") String line_id, @Param("rank") String rank, @Param("kind") String kind);
	public List<LineLeaderEntity> getBreakingMaterials(@Param("section_id") String section_id, @Param("line_id") String line_id);
	public List<LineLeaderEntity> getExpeditingMaterials(@Param("section_id") String section_id, @Param("line_id") String line_id);
	public List<LineLeaderEntity> getPlanOutlineMaterials(@Param("section_id") String section_id, @Param("line_id") String line_id);
	public List<LineLeaderEntity> getOtherLineFinishMaterials(@Param("section_id") String section_id, @Param("line_id") String line_id);
	public List<Map<String, String>> getComAndNsMatch(@Param("section_id") String section_id);
	public List<Map<String, String>> getNsAndComMatch(@Param("section_id") String section_id);

	public Integer getOutPeriod(@Param("out_period") String out_period, @Param("section_id") String section_id, @Param("line_id") String line_id, @Param("rank") String rank);

//	public Integer getPlanPeriod(@Param("out_period") String out_period, @Param("section_id") String section_id);

	public Integer getProduceActualOfLine(@Param("section_id") String section_id,@Param("line_id") String line_id);
	public Integer getProduceActualOfNsByBoard(@Param("section_id") String section_id);

	public Integer getComninedCount(@Param("section_id") String section_id, @Param("process_code_set") Set<String> processCodeSet);

	public Integer getPeriWaitingPart();

	public List<Map<String, Object>> getTodayProductPlan(@Param("section_id") String section_id);

	public List<Map<String, Object>> getTodayCompleteMaterialCountByModels(@Param("section_id") String section_id,@Param("line_id") String line_id);
}
