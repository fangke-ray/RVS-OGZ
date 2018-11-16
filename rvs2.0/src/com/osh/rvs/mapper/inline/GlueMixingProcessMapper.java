package com.osh.rvs.mapper.inline;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.inline.GlueMixingProcessEntity;

/**
 * 
 * @Title: GlueMixingProcessMapper.java
 * @Package com.osh.rvs.mapper.inline
 * @Description: 胶水调制作业
 * @author liuxb
 * @date 2017-12-18 上午10:11:13
 */
public interface GlueMixingProcessMapper {

	/**
	 * 新建胶水调制作业
	 * 
	 * @param entity
	 */
	public void insert(GlueMixingProcessEntity entity);

	/**
	 * 更新胶水调制作业
	 * 
	 * @param entity
	 */
	public void update(GlueMixingProcessEntity entity);

	/**
	 * 获取登录者未调制完成的胶水
	 * 
	 * @return
	 */
	public GlueMixingProcessEntity getUnFinishGlueMixing(@Param("mixing_operator_id") String mixing_operator_id);
}
