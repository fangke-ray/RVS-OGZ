package com.osh.rvs.mapper.equipment;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.equipment.DeviceJigInvoiceEntity;

/**
 * 设备工具治具订购询价
 * 
 * @author liuxb
 * 
 */
public interface DeviceJigInvoiceMapper {
	/**
	 * 新建设备工具治具订购询价
	 * 
	 * @param entity
	 */
	public void insert(DeviceJigInvoiceEntity entity);

	public void updatePrice(DeviceJigInvoiceEntity entity);

	/**
	 * 根据询价ID查询订购询价信息
	 * 
	 * @param invoice_id
	 * @return
	 */
	public DeviceJigInvoiceEntity getDeviceJigInvoiceById(@Param("invoice_id") String invoice_id);

	/**
	 * 查询最后一次询价
	 * 
	 * @param entity
	 * @return
	 */
	public DeviceJigInvoiceEntity getLastTimeInvoice(DeviceJigInvoiceEntity entity);
}
