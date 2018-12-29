package com.osh.rvs.mapper.push;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.OperatorEntity;

public interface OperatorMapper {

	/** search all 
	 * @param monthStart */
	public List<OperatorEntity> getAllActivingOperator(Date monthStart);

	public OperatorEntity getOperatorByID(String operator_id);

	/** search
	 * @param privacy_id */
	public List<OperatorEntity> searchOperator(OperatorEntity operator);

	public List<Map<String, Object>> searchOperatorProcessInMonth(Date startDate);

	/** search all */
	public List<OperatorEntity> getCountWorkOperator();

	public List<OperatorEntity> getOperatorByPositionForLight(@Param("section_id") String section_id, @Param("position_id")  String position_id);
	public List<OperatorEntity> getLeadersByPosition(@Param("section_id") String section_id, @Param("position_id")  String position_id);
}
