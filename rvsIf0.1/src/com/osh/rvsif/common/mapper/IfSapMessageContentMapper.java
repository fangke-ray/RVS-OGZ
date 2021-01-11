package com.osh.rvsif.common.mapper;

import com.osh.rvsif.common.bean.IfSapMessageContentEntity;

public interface IfSapMessageContentMapper {

	/**
	 * 
	 * @param entity
	 * @return
	 */
	public void insert(IfSapMessageContentEntity entity) throws Exception;

	/**
	 * 
	 * @param entity
	 * @return
	 */
	public void update(IfSapMessageContentEntity entity) throws Exception;
}
