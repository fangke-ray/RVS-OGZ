package com.osh.rvs.mapper.inline;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.inline.GlueMixingProcessPaceEntity;

/**
 * 
 * @Title: GlueMixingProcessPaceMapper.java
 * @Package com.osh.rvs.mapper.inline
 * @Description: 胶水调制作业分段时间
 * @author liuxb
 * @date 2017-12-18 上午11:11:36
 */
public interface GlueMixingProcessPaceMapper {
	/**
	 * 新建胶水调制作业分段时间
	 * 
	 * @param entity
	 */
	public void insert(GlueMixingProcessPaceEntity entity);
	
	/**
	 * 根据胶水调制作业ID获取胶水调制作业时间最大分段
	 * @param glue_mixing_process_id
	 * @return
	 */
	public int getMaxPaceByGlueMixingProcessId(@Param("glue_mixing_process_id") String glue_mixing_process_id);
	
	/**
	 * 更新新建胶水调制作业分段时间
	 * @param entity
	 */
	public void update(GlueMixingProcessPaceEntity entity);
	
	/**
	 * 根据胶水调制作业ID获取胶水调制作业时间未完成信息
	 * @param glue_mixing_process_id
	 * @return
	 */
	public GlueMixingProcessPaceEntity getUnFinishById(@Param("glue_mixing_process_id") String glue_mixing_process_id);
}
