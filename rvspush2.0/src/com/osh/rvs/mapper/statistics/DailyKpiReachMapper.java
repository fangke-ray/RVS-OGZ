package com.osh.rvs.mapper.statistics;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.DailyKpiDataEntity;

/**
 * 
 * @Title DailyKipReachMapper.java
 * @Project rvsBoard
 * @Package com.osh.rvs.mapper.app
 * @ClassName: DailyKipReachMapper
 * @Description: 每日KPI指标达成情况Mapper
 * @author lxb
 * @date 2014-11-3 上午10:10:56
 */
public interface DailyKpiReachMapper {

	// 最终检查合格率
	public BigDecimal getFinalInspectPassRate();

	// 一周最终检查合格率
	public BigDecimal getFinalInspectPassWeekRate(DailyKpiDataEntity entity);

	// 纳期遵守比率
	public BigDecimal getIntimeCompleteRate(DailyKpiDataEntity entity);
	public BigDecimal getIntimeCompleteSltRate(DailyKpiDataEntity connd);

	// 一周纳期遵守比率
	public Map<String, BigDecimal> getIntimeCompleteWeekRate(DailyKpiDataEntity entity);

	// 每日生产计划达成率
	public BigDecimal getPlanProcessedCount(Date count_date);

	// 一周生产计划达成率
	public BigDecimal getPlanProcessedWeekRate(DailyKpiDataEntity entity);

	// 返品分析LT达成率
	public BigDecimal getServiceRepairAnalysislTRate(DailyKpiDataEntity entity);

	// 一周返品分析LT达成率
	public BigDecimal getServiceRepairAnalysisWeeklTRate(DailyKpiDataEntity entity);

	// 检查记录是否存在
	public int checkDailyKpiIsExist(Date count_date);

	// 新建KPI
	public void insertDailyKpi(DailyKpiDataEntity entity);

	// 更新KPI
	public void updateDailyKpi(DailyKpiDataEntity entity);
	
	public void insertWeeklyKpi(DailyKpiDataEntity entity);
	
	// 检查每周记录是否存在
	public int checkWeeklyKpiIsExist(DailyKpiDataEntity entity);
	
	//更新每周KPI
	public void updateWeeklyKpi(DailyKpiDataEntity entity);
	
	//删除每周KPI
	public void deleteWeeklyKpi(DailyKpiDataEntity entity);

	public Integer getRegistrationOfPeriod(DailyKpiDataEntity weekKPI);

	public Integer getuser_agreementOfPeriod(DailyKpiDataEntity weekKPI);

	public Map<String, BigDecimal> getFinishesOfPeriod(DailyKpiDataEntity weekKPI);

	public Map<String, BigDecimal> getWipsOfPeriod(DailyKpiDataEntity weekKPI);

	public Map<String, BigDecimal> getIntimeWorkWeekRate(DailyKpiDataEntity weekKPI);

	public Map<String, BigDecimal> getBo_rateOfPeriod(DailyKpiDataEntity weekKPI);

	public Integer getPeriodComplete(@Param("period_start_date") Date time, @Param("period_end_date") Date time2);

	public Map<String, BigDecimal> getFinalChecksOfPeriod(DailyKpiDataEntity weekKPI);

	public BigDecimal getInline_passthrough_rate(DailyKpiDataEntity weekKPI);

}
