package com.osh.rvs.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public interface GlobalProgressMapper {
	// 到货台数
	public Integer getTodayRecieveResult();

	// 修理同意台数
	public Integer getTodaAgreedDateResult();

	// WIP在库台数
	public Map<String, BigDecimal> getTodayWipResult();
	public Map<String, BigDecimal> getTodayWipAgreedResult();

	// 投线台数
	public Integer getTodayInlineResult();

	// WIP在修
	public Map<String, BigDecimal> getTodayOnRepairWipResult();

	// 出货台数
	public Integer getTodayShippingResult();

	// 半出货累计（实绩）
	public Integer getShippingInPeriodResult(Date period_start);

	// 本月出货累计
	public Integer getShippingInMonthResult(String month_start);

	// 总在线台数
	public Map<String, BigDecimal> getInlineTotalResult();

	// 正常在线
	public Map<String, BigDecimal> getInlineReagalResult();

	// 不良
	public Map<String, BigDecimal>  getInlineFaultResult();

	// 延误
	public Map<String, BigDecimal> getInlineOvertimeResult();

	// 等待零件
	public Map<String, BigDecimal> getInlinePartialWaitingResult();

	// 半期计划出货总数
	public String getPlanAmountOfPeriod(String inStr);

}
