package com.osh.rvs.mapper;

import org.apache.ibatis.annotations.Param;

public interface LineRepairMapper {

	// 取得工程内全部件数
	public long getWaitingRepairMaterialCounts();

	// 取得工程内全部件数
	public long getWaitingPartsMaterialCounts(@Param("section_id") String section_id);
}
