package com.osh.rvs.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;

import org.apache.ibatis.session.SqlSessionManager;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.entity.DailyKpiDataEntity;
import com.osh.rvs.mapper.statistics.DailyKpiReachMapper;

import framework.huiqing.common.util.CommonStringUtil;

/**
 * @Title DailyKipReachService.java
 * @Project rvsBoard
 * @Package com.osh.rvs.service
 * @ClassName: DailyKipReachService
 * @Description: 每日KPI指标达成情况Service
 * @author lxb
 * @date 2014-11-3 上午9:38:52
 */
public class DailyKpiReachService {
	private static final BigDecimal HUNDRED = new BigDecimal(100);

	/**
	
	/**
	 * 当天KPI获取
	 * @param todayKPI
	 * @param curTime 当前时间
	 * @param dao
	 */
	private void getTodayKPI(DailyKpiDataEntity todayKPI, Calendar curTime, DailyKpiReachMapper dao){
	
		// 检索条件
		DailyKpiDataEntity connd = new DailyKpiDataEntity();
		connd.setCount_date(curTime.getTime());

		// 半年开始时间
		Calendar periodStart = Calendar.getInstance();
		periodStart.setTime(curTime.getTime());

		// 半期完成数量
		Integer half_period_complete = dao.getPeriodComplete(RvsUtils.getBussinessHalfStartDate(periodStart), curTime.getTime());
		todayKPI.setHalf_period_complete(half_period_complete);

		// 月完成数量
		Integer month_complete = dao.getPeriodComplete(RvsUtils.getMonthStartDate(curTime), curTime.getTime());
		todayKPI.setMonth_complete(month_complete);

		// 最终检查合格率
		BigDecimal final_inspect_pass_rate = dao.getFinalInspectPassRate();
		todayKPI.setFinal_inspect_pass_rate(final_inspect_pass_rate);
		
		// 大修理5天内纳期遵守比率
		Integer heavy_fix[] = new Integer[]{1,2,3};
		connd.setLevels(heavy_fix);
		BigDecimal intime_complete_rate = dao.getIntimeCompleteRate(connd);
		todayKPI.setIntime_complete_rate(intime_complete_rate);

		// 大修理6天内纳期遵守比率
		connd.setLevels(heavy_fix);
		BigDecimal intime_complete_slt_rate = dao.getIntimeCompleteSltRate(connd);
		todayKPI.setIntime_complete_slt_rate(intime_complete_slt_rate);

		// 截至今天中修理2天内纳期遵守比率
		Integer medium_fix[] = new Integer[]{96,97,98};
		connd.setLevels(medium_fix);
		BigDecimal intime_complete_medium_rate = dao.getIntimeCompleteRate(connd);
		todayKPI.setIntime_complete_medium_rate(intime_complete_medium_rate);

		// 截至今天小修理2天内纳期遵守比率
		Integer light_fix[] = new Integer[]{9,91,92,93};
		connd.setLevels(light_fix);
		BigDecimal intime_complete_light_rate = dao.getIntimeCompleteRate(connd);
		todayKPI.setIntime_complete_light_rate(intime_complete_light_rate);

		// 每日生产计划达成率
		BigDecimal total_plan_processed_rate = dao.getPlanProcessedCount(curTime.getTime());
		// 取得生产计划数
		String sDailySchedule  = PathConsts.SCHEDULE_SETTINGS.getProperty("daily.schedule.总组工程");
		if (CommonStringUtil.isEmpty(sDailySchedule)) {
			sDailySchedule = "1";
		}

		todayKPI.setTotal_plan_processed_rate(total_plan_processed_rate.divide(
				new BigDecimal(sDailySchedule), 1, BigDecimal.ROUND_HALF_UP));

		// 工程内直行率 
		todayKPI.setCount_date_start(curTime.getTime());
		todayKPI.setCount_date_end(curTime.getTime());
		BigDecimal inline_passthrough_rate = dao.getInline_passthrough_rate(todayKPI);
		todayKPI.setInline_passthrough_rate(inline_passthrough_rate);

		//返品分析LT达成率(24小时)
		connd.setStatus(24);
		BigDecimal service_repair_analysis_lt24_rate = dao.getServiceRepairAnalysislTRate(connd);
		todayKPI.setService_repair_analysis_lt24_rate(service_repair_analysis_lt24_rate);
		
		//返品分析LT达成率(48小时)
		connd.setStatus(48);
		BigDecimal service_repair_analysis_lt48_rate = dao.getServiceRepairAnalysislTRate(connd);
		todayKPI.setService_repair_analysis_lt48_rate(service_repair_analysis_lt48_rate);
		
		//周天
		if(curTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){ 
			curTime.add(Calendar.DATE, -1);
		}
		
		todayKPI.setCount_date(curTime.getTime());
	}
	
	/**
	 * 新建KPI
	 * 
	 */
	public void insert(SqlSessionManager conn, Calendar curTime){
		DailyKpiReachMapper dao = conn.getMapper(DailyKpiReachMapper.class);
		
		// 当天为周日
		if(curTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
			curTime.add(Calendar.DATE, -1);// 周日日期变为周六日期
		}
		
		// 获取当天KPI数据
		DailyKpiDataEntity todayKPI = new DailyKpiDataEntity();
		getTodayKPI(todayKPI, curTime, dao);
		
		// 检查记录是否存在
		int exist = dao.checkDailyKpiIsExist(curTime.getTime());
		if(exist == 0){// 不存在
			dao.insertDailyKpi(todayKPI);
		}else{// 存在
			dao.updateDailyKpi(todayKPI);
		}
	}
	
	public void insertWeekKPI(DailyKpiDataEntity weekKPI,SqlSessionManager conn){
		DailyKpiReachMapper dao = conn.getMapper(DailyKpiReachMapper.class);
		getweekKPI(weekKPI, dao);
		
		// 检查记录是否存在
		int exist = dao.checkWeeklyKpiIsExist(weekKPI);		
		if(exist == 0){// 不存在
			dao.insertWeeklyKpi(weekKPI);
		}else{
			dao.updateWeeklyKpi(weekKPI);
		}
	}

	public void deletetWeekKPI(DailyKpiDataEntity weekKPI,SqlSessionManager conn){
		DailyKpiReachMapper dao = conn.getMapper(DailyKpiReachMapper.class);
		dao.deleteWeeklyKpi(weekKPI);
	}
	
	/**
	 * 一周KPI获取
	 * @param todayKPI
	 * @param dao
	 */
	private void  getweekKPI(DailyKpiDataEntity weekKPI,DailyKpiReachMapper dao){
		// 到货受理数
		Integer registration = dao.getRegistrationOfPeriod(weekKPI);
		weekKPI.setRegistration(registration);
		// 修理同意数
		Integer user_agreement = dao.getuser_agreementOfPeriod(weekKPI);
		weekKPI.setUser_agreement(user_agreement);

		Map<String, BigDecimal> finishes = dao.getFinishesOfPeriod(weekKPI);
		// 返回ＯＳＨ修理
		weekKPI.setReturn_to_osh(finishes.get("return_to_osh").intValue());
		// 未修理返回
		weekKPI.setUnrepair(finishes.get("unrepair").intValue());
		// 出货总数
		weekKPI.setShipment(finishes.get("shipment").intValue());

		// WIP在修数 Endoeye系列镜子不计入
		Map<String, BigDecimal> wips = dao.getWipsOfPeriod(weekKPI);
		weekKPI.setWork_in_process(wips.get("work_in_process").intValue());
		// WIP在库数
		weekKPI.setWork_in_storage(wips.get("work_in_all").intValue());

		// 大修理6天内纳期遵守比率
		Map<String, BigDecimal> intime_complete_rates = dao.getIntimeCompleteWeekRate(weekKPI);
		weekKPI.setIntime_complete_rate(intime_complete_rates.get("intime_complete_rate"));

		// 平均修理周期RLT
		weekKPI.setAverage_repair_lt(intime_complete_rates.get("average_repair_lt"));

		// 零件到达后4天内出货比率
		Map<String, BigDecimal> intime_work_out_rates = dao.getIntimeWorkWeekRate(weekKPI);
		weekKPI.setIntime_work_out_rate(intime_work_out_rates.get("intime_work_out_rate"));

		// 平均工作周期
		weekKPI.setAverage_work_lt(intime_work_out_rates.get("average_work_lt"));

		// 当天零件BO率
		// 三天零件BO率
		Map<String, BigDecimal> bo_rates = dao.getBo_rateOfPeriod(weekKPI);
		weekKPI.setBo_rate(bo_rates.get("bo_rate"));
		weekKPI.setBo_3day_rate(bo_rates.get("bo_3day_rate"));

		// 工程内直行率 
		BigDecimal inline_passthrough_rate = dao.getInline_passthrough_rate(weekKPI);
		weekKPI.setInline_passthrough_rate(inline_passthrough_rate);

		// 最终检查合格件数
		Map<String, BigDecimal> final_checks = dao.getFinalChecksOfPeriod(weekKPI);
		Integer final_check_pass_count = final_checks.get("final_check_pass_count").intValue();
		weekKPI.setFinal_check_pass_count(final_check_pass_count);
		// 最终检查不合格件数
		Integer final_check_forbid_count = final_checks.get("final_check_forbid_count").intValue();
		weekKPI.setFinal_check_forbid_count(final_check_forbid_count);

		// 最终检查合格率
		if ((final_check_pass_count != null && final_check_pass_count > 0)
			|| (final_check_forbid_count != null && final_check_forbid_count > 0)) {
			BigDecimal final_inspect_pass_rate = new BigDecimal(final_check_pass_count)
				.divide(new BigDecimal(final_check_pass_count + final_check_forbid_count), 3, BigDecimal.ROUND_HALF_UP)
				.multiply(HUNDRED);
			weekKPI.setFinal_inspect_pass_rate(final_inspect_pass_rate);
		}
		// 内镜保修期内返品率（含新品不良） （无法算）

		// 最终检查合格率
//		BigDecimal final_inspect_pass_rate = dao.getFinalInspectPassWeekRate(weekKPI);
//		weekKPI.setFinal_inspect_pass_rate(final_inspect_pass_rate);
		
		// 每日生产计划达成率
		BigDecimal total_plan_processed_rate = dao.getPlanProcessedWeekRate(weekKPI);
		weekKPI.setTotal_plan_processed_rate(total_plan_processed_rate);

		//返品分析LT达成率(24小时)
		weekKPI.setStatus(24);
		BigDecimal service_repair_analysis_lt24_rate = dao.getServiceRepairAnalysisWeeklTRate(weekKPI);
		weekKPI.setService_repair_analysis_lt24_rate(service_repair_analysis_lt24_rate);
		
		//返品分析LT达成率(48小时)
		weekKPI.setStatus(48);
		BigDecimal service_repair_analysis_lt48_rate = dao.getServiceRepairAnalysisWeeklTRate(weekKPI);
		weekKPI.setService_repair_analysis_lt48_rate(service_repair_analysis_lt48_rate);

		// 周数
		Calendar cal = Calendar.getInstance();
		cal.setTime(weekKPI.getCount_date_end());
		weekKPI.setWeekly_of_year(cal.get(Calendar.WEEK_OF_YEAR));
	}
	
	
}
