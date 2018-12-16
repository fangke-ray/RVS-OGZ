package com.osh.rvs.mapper.partial;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.partial.FactProductionFeatureEntity;

/**
 * 现品作业信息
 *
 * @author liuxb
 *
 */
public interface FactProductionFeatureMapper {
	/**
	 * 查询作业记录
	 *
	 * @param entity
	 * @return
	 */
	public List<FactProductionFeatureEntity> searchWorkRecord(FactProductionFeatureEntity entity);

	/**
	 * 新建现品作业信息
	 *
	 * @param entity
	 */
	public void insert(FactProductionFeatureEntity entity) throws Exception;

	/**
	 * 删除现品作业信息
	 *
	 * @param entity
	 */
	public void delete(@Param("fact_pf_key") String key) throws Exception;

	/**
	 * 更新处理结束时间
	 *
	 * @param entity
	 */
	public void updateFinishTime(FactProductionFeatureEntity entity) throws Exception;

	/**
	 * 查询未结束作业的记录
	 *
	 * @param entity
	 * @return
	 */
	public FactProductionFeatureEntity searchUnFinishedProduction(FactProductionFeatureEntity entity);

	/**
	 * 更新零件入库单 KEY
	 *
	 * @param entity
	 */
	public void updateKey(FactProductionFeatureEntity entity) throws Exception;

}
