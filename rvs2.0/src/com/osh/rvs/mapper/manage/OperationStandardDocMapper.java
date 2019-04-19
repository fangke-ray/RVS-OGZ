package com.osh.rvs.mapper.manage;

import java.util.List;

import com.osh.rvs.bean.manage.OperationStandardDocEntity;

public interface OperationStandardDocMapper {
	public List<OperationStandardDocEntity> search(OperationStandardDocEntity entity);

	public void insert(OperationStandardDocEntity entity);

	public void delete(OperationStandardDocEntity entity);

	public List<OperationStandardDocEntity> searchAllModel();

	/**
	 * 作业基准书明细
	 * 
	 * @param entity
	 * @return
	 */
	public List<OperationStandardDocEntity> searchDetail(OperationStandardDocEntity entity);
}
