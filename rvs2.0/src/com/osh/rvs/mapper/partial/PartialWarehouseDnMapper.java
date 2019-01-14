package com.osh.rvs.mapper.partial;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.partial.PartialWarehouseDnEntity;

public interface PartialWarehouseDnMapper {

	/**
	 * 新建零件入库DN编号
	 *
	 * @param entity
	 */
	public void insert(PartialWarehouseDnEntity entity) throws Exception;

	/**
	 * 根据入库单KEY删除入库DN编号
	 *
	 * @param key
	 */
	public void delete(@Param("key") String key) throws Exception;

	/**
	 * 根据DN编号查询零件入库单信息
	 *
	 * @param no
	 * @return
	 */
	public PartialWarehouseDnEntity getByDnNo(@Param("dn_no") String no);

	/**
	 * 根据入库KEY查询零件入库单DN编号信息
	 *
	 * @param key
	 * @return
	 */
	public List<PartialWarehouseDnEntity> getByKey(@Param("key") String key);

}
