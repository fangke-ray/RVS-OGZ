package com.osh.rvs.mapper.partial;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.partial.GlueEntity;

/**
 * 
 * @Title: GlueMapper.java
 * @Package com.osh.rvs.mapper.partial
 * @Description: 胶水
 * @author liuxb
 * @date 2017-12-15 下午2:52:09
 */
public interface GlueMapper {

	/**
	 * 获取胶水基本信息
	 * 
	 * @param entity
	 * @return
	 */
	public GlueEntity getGlueInfo(GlueEntity entity);

	/**
	 * 新建胶水
	 * 
	 * @param entity
	 */
	public void insert(GlueEntity entity);

	/**
	 * 根据胶水ID查询胶水信息
	 * 
	 * @param glue_id
	 * @return
	 */
	public GlueEntity getGlueByGlueId(@Param("glue_id") String glue_id);

}
