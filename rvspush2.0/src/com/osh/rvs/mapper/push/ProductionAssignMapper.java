package com.osh.rvs.mapper.push;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.ProductionAssignEntity;


/**
 * 工位作业指定
 * @author Gong
 *
 */
public interface ProductionAssignMapper {

	public List<ProductionAssignEntity> getProductionAssignByOperator(@Param("operator_id") String operator_id);

	public List<ProductionAssignEntity> getProductionAssignByLine(@Param("section_id") String section_id, @Param("line_id") String line_id);

	public int leaderAssign(ProductionAssignEntity entity);

	public int create(ProductionAssignEntity entity);
}