package com.osh.rvs.mapper.data;

import java.util.List;

import com.osh.rvs.bean.data.OperatorProductionEntity;


public interface OperatorProductionMapper {

	public List<OperatorProductionEntity> getProductionFeatureByCondition(OperatorProductionEntity entity);
	public List<OperatorProductionEntity> getProductionFeatureByConditionOfDay(OperatorProductionEntity entity);
	
	public OperatorProductionEntity getDetail(OperatorProductionEntity entity);
	
	public List<OperatorProductionEntity> getProductionFeatureByKey(OperatorProductionEntity entity);
	
	public void savePause(OperatorProductionEntity entity);
	
	public void deletePause(OperatorProductionEntity entity);
	
	public String existPause(OperatorProductionEntity entity);
	
	public void updatePause(OperatorProductionEntity entity);
	
	public OperatorProductionEntity getPauseOvertime(OperatorProductionEntity entity);
	
	public void deletePauseOvertime(OperatorProductionEntity entity);
	
	public void updatePauseOvertime(OperatorProductionEntity entity);
}
