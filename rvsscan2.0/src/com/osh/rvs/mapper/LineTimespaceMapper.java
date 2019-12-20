package com.osh.rvs.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface LineTimespaceMapper {

	public List<Map<String, Object>> getProductionFeatures(@Param("line_id")String line_id, @Param("division") String px);

	public List<Map<String, Object>> getLineBalancing(@Param("material_id") String material_id, @Param("line_id")String line_id);

	public BigDecimal getWorkingStandingRate(@Param("material_id")String material_id, @Param("line_id")String line_id
			, @Param("drying_time")String drying_time);

	public List<Map<String, Object>> getOperatorFeatures(@Param("line_ids") String[] line_ids);

	public boolean checkNoDrying(@Param("material_id")String material_id, @Param("line_id") String line_id);

	public BigDecimal getDecWorkingStandingRate(@Param("material_id")String material_id, @Param("line_id")String line_id
			, @Param("drying_time")String drying_time);

	public List<Map<String, String>> getLevels();

	public String getTodayManufatorModelName();

}
