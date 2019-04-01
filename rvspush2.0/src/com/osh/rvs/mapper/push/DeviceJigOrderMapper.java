package com.osh.rvs.mapper.push;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.DeviceJigOrderEntity;

/**
 * 设备工具订购申请
 * 
 * @author liuxb
 * 
 */
public interface DeviceJigOrderMapper {
	/**
	 * 统计未询价且申请日期早于当天5个工作日申请数
	 * 
	 * @param workdays 工作日
	 * @return
	 */
	public Integer countUnInvoice();

	/**
	 * 统计未报价，且询价发送日期早于离当前日期最近的25日，询价日期较旧的申请数
	 * 
	 * @param send_date 询价发送日期
	 * @return
	 */
	public Integer countOldInvoice(@Param("send_date") Date send_date);

	/**
	 * 临近纳期的报价
	 * 
	 * @return
	 */
	public List<DeviceJigOrderEntity> searchNearScheduledQuotation();

	/**
	 * 超过纳期还没有收货
	 * 
	 * @return
	 */
	public List<DeviceJigOrderEntity> searchOverScheduledAndUnRecept();

	/**
	 * 查询收货后还没有验收订购单明细
	 * 
	 * @return
	 */
	public List<DeviceJigOrderEntity> searchUnInlineRecept();

}
