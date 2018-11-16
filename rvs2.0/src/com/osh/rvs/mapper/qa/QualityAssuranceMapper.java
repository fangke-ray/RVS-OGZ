package com.osh.rvs.mapper.qa;

import java.util.List;

import com.osh.rvs.bean.data.MaterialEntity;

public interface QualityAssuranceMapper {

	public List<MaterialEntity> getWaitings(String position_id);

	public List<MaterialEntity> getFinished(String position_id);

	public MaterialEntity getMaterialDetail(String material_id);

	public int updateMaterial(MaterialEntity entity) throws Exception;

	public int forbidMaterial(String material_id) throws Exception;

	public List<MaterialEntity> getWaitingsFiling(String position_id);

	public List<MaterialEntity> getFinishedFiling();

}
