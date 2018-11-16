package com.osh.rvs.mapper.inline;

import org.apache.ibatis.annotations.Param;


/**
 * 维修计划
 * @author Gong
 *
 */
public interface RepairPlanMapper {

	public Integer getShippingPlan(@Param("planYear") String planYear, @Param("planMonth") String planMonth);

}