package com.osh.rvs.mapper.equipment;

import java.util.List;
import java.util.Map;

import com.osh.rvs.bean.equipment.DeviceSpareEntity;

public interface DeviceSpareMapper {
	/**
	 * 检索
	 * 
	 * @param entity
	 * @return
	 */
	public List<DeviceSpareEntity> search(DeviceSpareEntity entity);

	/**
	 * 新建
	 * 
	 * @param entity
	 */
	public void insert(DeviceSpareEntity entity);

	/**
	 * 删除
	 * 
	 * @param entity
	 */
	public void delete(DeviceSpareEntity entity);

	/**
	 * 更新
	 * 
	 * @param entity
	 */
	public void update(DeviceSpareEntity entity);

	/**
	 * 查询设备工具备品信息
	 * 
	 * @param entity
	 * @return
	 */
	public DeviceSpareEntity getDeviceSpare(DeviceSpareEntity entity);

	/**
	 * 更新当前有效库存
	 * 
	 * @param entity
	 */
	public void updateAvailableInventory(DeviceSpareEntity entity);

	public Map<String, Integer> calculatePrice();
}
