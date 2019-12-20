package com.osh.rvs.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;


public interface AllPositionsMapper {

	public List<Map<String, Object>> getHeapAndWork();
	
	public List<Map<String, Object>> getOutcomeDivided();

	public Map<String, Object> getAlarmMessage(String alarm_id);

	public int getWipCount();

	public int getWaitInline();

	public int getInlineToday();

	public int getWaitShipping();

	public int getShippingToday();

	public int getAgreeCount();

	public int getLineOutcome(@Param("line_id") String line_id, @Param("section_id") String section_id);

	public int QaResult(int operateResult);

	public int getPlanToday(@Param("section_id") String section_id);

	public List<Map<String, Object>> getErrorAlarms(@Param("process_code") String process_code,@Param("section_id")  String section_id);

	public List<Map<String, Object>> getBoMaterialsOfSectionLine();

	public int getBoMaterialsAll();

	public int getDecomStorageCount();
}
