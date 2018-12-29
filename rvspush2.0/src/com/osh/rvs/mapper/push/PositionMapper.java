package com.osh.rvs.mapper.push;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.PositionEntity;

public interface PositionMapper {

	/** search all */
	public List<PositionEntity> getAllPosition();

	public PositionEntity getPositionByID(String position_id);

	/** search */
	public List<PositionEntity> searchPosition(PositionEntity position);

	public int getPositionHeap(@Param(value = "section_id") String section_id, @Param(value = "position_id") String position_id, @Param(value = "px") String px);

	public String getPositionWithSectionByID(@Param(value = "section_id") String section_id, @Param(value = "position_id") String position_id);

	public String getLineWithSectionByID(@Param(value = "section_id") String section_id, @Param(value = "line_id") String line_id);

}
