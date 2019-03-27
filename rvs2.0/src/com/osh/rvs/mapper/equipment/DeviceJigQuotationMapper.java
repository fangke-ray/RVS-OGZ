package com.osh.rvs.mapper.equipment;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.equipment.DeviceJigQuotationEntity;

/**
 * 设备工具治具订购报价
 * 
 * @author liuxb
 * 
 */
public interface DeviceJigQuotationMapper {

	/**
	 * 查询全部报价
	 * 
	 * @return
	 */
	public List<DeviceJigQuotationEntity> searchAll();

	/**
	 * 新建设备工具治具订购报价
	 * 
	 * @param entity
	 */
	public void insert(DeviceJigQuotationEntity entity);

	/**
	 * 更新报价信息
	 * 
	 * @param entity
	 */
	public void update(DeviceJigQuotationEntity entity);

	/**
	 * 根据报价KEY查询报价信息
	 * 
	 * @param quotation_no
	 * @return
	 */
	public DeviceJigQuotationEntity getDeviceJigQuotationById(@Param("quotation_id") String quotationID);

	/**
	 * 根据报价单号查询报价信息
	 * 
	 * @param quotation_no
	 * @return
	 */
	public DeviceJigQuotationEntity getDeviceJigQuotationByQuotationNo(@Param("quotation_no") String quotation_no);

}
