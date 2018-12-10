package com.osh.rvs.mapper.partial;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.partial.PartialWarehouseEntity;

/**
 * 零件入库单
 *
 * @author liuxb
 *
 */
public interface PartialWarehouseMapper {

	public List<PartialWarehouseEntity> search(PartialWarehouseEntity entity);

	/**
	 * 新建零件入库单
	 *
	 * @param entity
	 */
	public void insert(PartialWarehouseEntity entity) throws Exception;

	/**
	 * 删除零件入库单
	 *
	 * @param entity
	 */
	public void delete(@Param("key") String key) throws Exception;

	/**
	 * 更新入库进展
	 *
	 * @param entity
	 */
	public void updateStep(PartialWarehouseEntity entity) throws Exception;

	/**
	 * 根据DN编号查询零件入库单信息
	 *
	 * @param no
	 * @return
	 */
	public PartialWarehouseEntity getByDnNo(@Param("dn_no") String no);

	/**
	 * 根据key查询零件入库单信息
	 *
	 * @param key
	 * @return
	 */
	public PartialWarehouseEntity getByKey(@Param("key") String key);

	/**
	 * 查询当前入库进展信息
	 *
	 * @return
	 */
	public List<PartialWarehouseEntity> searchStepPartialWarehouse(PartialWarehouseEntity entity);

}