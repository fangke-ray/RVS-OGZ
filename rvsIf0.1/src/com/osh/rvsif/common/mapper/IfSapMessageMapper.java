package com.osh.rvsif.common.mapper;

import com.osh.rvsif.common.bean.IfSapMessageEntity;

public interface IfSapMessageMapper {

	/**
	 * 
	 * @param entity
	 * @return
	 */
	public void insert(IfSapMessageEntity entity) throws Exception;

	public void update(IfSapMessageEntity entity) throws Exception;
}
