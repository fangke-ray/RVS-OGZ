package com.osh.rvs.mapper.statistics;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.DailyKpiDataEntity;

/**
 * KPI日报信息取得
 * 
 * @author gonglm
 * 
 */
public interface DailyKpiMapper {

	public BigDecimal getFinalInspectPassRate(Date count_date);

	public BigDecimal getIntimeCompleteRate(Date count_date);

	public BigDecimal getPlanProcessedRate(Map<String, Object> condi);

	public BigDecimal getQuotationLtRate(Date count_date);

	public BigDecimal getDirectQuotationLtRate(Date count_date);

	public DailyKpiDataEntity getByDate(Date count_date);

	public Integer getOutCount(@Param("start_date") Date periodStart, @Param("end_date") Date count_date);

	public Integer getShippingPlan(@Param("planYear") String planYear, @Param("planMonth") String planMonth);

	public String getPlanAmountOfPeriod(String inStr);

}
