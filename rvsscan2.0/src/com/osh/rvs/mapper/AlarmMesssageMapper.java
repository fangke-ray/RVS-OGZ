package com.osh.rvs.mapper;

import org.apache.ibatis.annotations.Param;

public interface AlarmMesssageMapper {

	public String getBreakLevelByMaterialId(@Param("material_id") String material_id, @Param("position_id") String position_id);
}
