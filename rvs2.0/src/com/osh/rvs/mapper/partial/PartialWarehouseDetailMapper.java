package com.osh.rvs.mapper.partial;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.partial.PartialWarehouseDetailEntity;

/**
 * 零件入库明细
 *
 * @author liuxb
 *
 */
public interface PartialWarehouseDetailMapper {
	/**
	 * 新建零件入库明细
	 *
	 * @param entity
	 */
	public void insert(PartialWarehouseDetailEntity entity) throws Exception;

	/**
	 * 根据零件入库单KEY查询零件入库明细
	 *
	 * @param key
	 * @return
	 */
	public List<PartialWarehouseDetailEntity> searchByKey(@Param("key") String key);

	/**
	 * 删除零件入库明细
	 *
	 * @param entity
	 */
	public void delete(@Param("key") String key) throws Exception;

	/**
	 * 更新零件入库明细
	 *
	 */
	public void update(PartialWarehouseDetailEntity entity) throws Exception;

	/**
	 * 统计各个规格种别总数量
	 *
	 * @param entity
	 * @return
	 */
	public List<PartialWarehouseDetailEntity> countQuantityOfSpecKind(@Param("key") String key);

	/**
	 * 查询需要分装的零件入库明细
	 *
	 * @param entity
	 * @return
	 */
	public List<PartialWarehouseDetailEntity> searchUnpackByKey(@Param("key") String key);

	/**
	 * 根据零件入库单KEY，统计不同规格种别分装总数
	 *
	 * @param entity
	 * @return
	 */
	public List<PartialWarehouseDetailEntity> countUnpackOfSpecKindByKey(@Param("key") String key);

}