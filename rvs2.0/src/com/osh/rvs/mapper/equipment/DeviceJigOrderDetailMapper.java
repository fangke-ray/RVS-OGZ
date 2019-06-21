package com.osh.rvs.mapper.equipment;

import java.util.List;

import com.osh.rvs.bean.equipment.DeviceJigOrderDetailEntity;

/**
 * 设备工具治具订单明细
 * 
 * @author liuxb
 * 
 */
public interface DeviceJigOrderDetailMapper {
	/**
	 * 检索
	 * 
	 * @param entity
	 * @return
	 */
	public List<DeviceJigOrderDetailEntity> search(DeviceJigOrderDetailEntity entity);

	/**
	 * 新建设备工具治具订单明细
	 * 
	 * @param entity
	 */
	public void insert(DeviceJigOrderDetailEntity entity);

	/**
	 * 删除明细
	 * 
	 * @param entity
	 */
	public void delete(DeviceJigOrderDetailEntity entity);

	/**
	 * 更新明细
	 * 
	 * @param entity
	 */
	public void update(DeviceJigOrderDetailEntity entity);
	
	/**
	 * 更新申请日期
	 * @param entity
	 */
	public void updateApplicateDate(DeviceJigOrderDetailEntity entity);

	public List<DeviceJigOrderDetailEntity> searchDetail(DeviceJigOrderDetailEntity entity);

	public List<DeviceJigOrderDetailEntity> searchInvoice();

	/**
	 * 更新询价ID
	 * 
	 * @param entity
	 */
	public void updateInvoiceId(DeviceJigOrderDetailEntity entity);

	/**
	 * 查询未报价订单明细
	 * 
	 * @return
	 */
	public List<DeviceJigOrderDetailEntity> searchUnQuotation();

	/**
	 * 更新报价ID
	 * 
	 * @param entity
	 */
	public void updateQuotationId(DeviceJigOrderDetailEntity entity);

	/**
	 * 更新确认结果
	 * 
	 * @param entity
	 */
	public void updateConfirm(DeviceJigOrderDetailEntity entity);

	/**
	 * 查询单条订购明细
	 * 
	 * @param entity
	 * @return
	 */
	public DeviceJigOrderDetailEntity getOrderDetail(DeviceJigOrderDetailEntity entity);

	/**
	 * 更新验收
	 * 
	 * @param entity
	 */
	public void updateInlineRecept(DeviceJigOrderDetailEntity entity);

	/**
	 * 更新预算
	 * 
	 * @param entity
	 */
	public void updateBudget(DeviceJigOrderDetailEntity entity);

	/**
	 * 更新确认数量
	 * 
	 * @param entity
	 */
	public void updateConfirmQuantity(DeviceJigOrderDetailEntity entity);

	/**
	 * 发票登记
	 * @param entity
	 */
	public void updateTicket(DeviceJigOrderDetailEntity entity);

	
	
	public List<DeviceJigOrderDetailEntity> searchInvoiceReferChooser();
	public List<DeviceJigOrderDetailEntity> searchOrderReferChooser();
	
	/**
	 * 导出询价明细
	 * @param entity
	 * @return
	 */
	public List<DeviceJigOrderDetailEntity> searchDetailUnQuotation(DeviceJigOrderDetailEntity entity);
	
	public List<DeviceJigOrderDetailEntity> searchOrderUnComfirm(DeviceJigOrderDetailEntity entity);


}
