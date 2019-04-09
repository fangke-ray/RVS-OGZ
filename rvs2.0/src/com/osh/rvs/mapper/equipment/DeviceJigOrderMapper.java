package com.osh.rvs.mapper.equipment;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.equipment.DeviceJigOrderEntity;

/**
 * 设备工具治具订单
 * 
 * @author liuxb
 * 
 */
public interface DeviceJigOrderMapper {
	/**
	 * 新建设备工具治具订单
	 * 
	 * @param entity
	 */
	public void insert(DeviceJigOrderEntity entity);
	
	/**
	 * 更新设备工具治具订单
	 * @param entity
	 */
	public void update(DeviceJigOrderEntity entity);

	/**
	 * 查询未发放申请单号
	 * 
	 * @return
	 */
	public List<DeviceJigOrderEntity> searchUnQuotation();

	/**
	 * 根据订单号查询设备工具治具订单信息
	 * 
	 * @param entity
	 * @return
	 */
	public DeviceJigOrderEntity getDeviceJigOrderByOrderNo(@Param("order_no") String order_no);
}
