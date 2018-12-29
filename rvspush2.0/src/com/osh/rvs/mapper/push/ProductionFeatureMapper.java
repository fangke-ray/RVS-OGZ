package com.osh.rvs.mapper.push;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import com.osh.rvs.entity.PositionEntity;

public interface ProductionFeatureMapper {

	public List<Map<String, String>> getWorkings();

	public List<PositionEntity> getNonfinishedPositions(@Param("material_id") String material_id, @Param("line_id") String line_id);
}
