package com.osh.rvs.mapper.master;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.master.GlueMixingTypeEntity;

/**
 * 
 * @Title: GlueMixingTypeMapper.java
 * @Package com.osh.rvs.mapper.master
 * @Description:胶水调制种类
 * @author liuxb
 * @date 2017-12-15 下午4:08:44
 */
public interface GlueMixingTypeMapper {
	/**
	 * 获取胶水调制种类基本信息
	 * 
	 * @param entity
	 * @return
	 */
	public GlueMixingTypeEntity getGlueMixingTypeInfo(GlueMixingTypeEntity entity);

	/**
	 * 新建胶水调制种类
	 * 
	 * @param entity
	 */
	public void insert(GlueMixingTypeEntity entity);

	/**
	 * 
	 * @return
	 */
	public String[] getBinderNameAutoCompletes(@Param("partial_id") String partial_id);
	
	/**
	 * 根据胶水调制种类ID查询胶水调制种类信息
	 * @param glue_mixing_type_id
	 * @return
	 */
	public GlueMixingTypeEntity getGlueMixingTypeById(@Param("glue_mixing_type_id") String glue_mixing_type_id);
}
