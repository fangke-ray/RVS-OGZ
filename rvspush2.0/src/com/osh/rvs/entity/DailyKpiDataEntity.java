package com.osh.rvs.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 日报KPI数据
 */
public class DailyKpiDataEntity implements Serializable {

	private static final long serialVersionUID = -5822192201410204094L;

	private Date count_date;
	/** 保修期内返品率 */
	private BigDecimal service_repair_back_rate;
	/** 最终检查合格率 */
	private BigDecimal final_inspect_pass_rate;
	/** 7天内纳期遵守比率 */
	private BigDecimal intime_complete_rate;
	private BigDecimal intime_complete_slt_rate;
	private BigDecimal intime_complete_medium_rate;
	private BigDecimal intime_complete_light_rate;
	/** 每日生产计划达成率 */
	private BigDecimal total_plan_processed_rate;
	/** 翻修1课每日生产计划达成率 */
	private BigDecimal section1_plan_processed_rate;
	/** 翻修2课每日生产计划达成率 */
	private BigDecimal section2_plan_processed_rate;
	/** NS再生率 */
	private BigDecimal ns_regenerate_rate;
	/** 工程直行率 */
	private BigDecimal inline_passthrough_rate;
	/** 报价周期LT达成率 */
	private BigDecimal quotation_lt_rate;
	/** 直送报价周期LT达成率 */
	private BigDecimal direct_quotation_lt_rate;
	/** 半期累计出货 */
	private Integer half_period_complete;
	/** 本月累计出货 */
	private Integer month_complete;
	private String comment;
	/** 返品分析LT达成率(24小时) **/
	private BigDecimal service_repair_analysis_lt24_rate;
	/** 返品分析LT达成率(48小时) **/
	private BigDecimal service_repair_analysis_lt48_rate;

	private Integer status;

	private Date count_date_start;

	private Date count_date_end;
	private Integer weekly_of_year;

	// 到货受理数
	private Integer registration;
	// 修理同意数
	private Integer user_agreement;
	// 返回ＯＳＨ修理
	private Integer return_to_osh;
	// 未修理返回
	private Integer unrepair;
	// 出货总数
	private Integer shipment;

	// WIP在修数 Endoeye系列镜子不计入
	private Integer work_in_process;
	// WIP在库数
	private Integer work_in_storage;

	
	// 平均修理周期RLT
	private BigDecimal average_repair_lt;
	// 零件到达后4天内出货比率
	private BigDecimal intime_work_out_rate;

	// 平均工作周期
	private BigDecimal average_work_lt;
	// 当天零件BO率
	private BigDecimal bo_rate;

	// 三天零件BO率
	private BigDecimal bo_3day_rate;
	// 工程内直行率 （无法算）

	private Integer final_check_pass_count;
	private Integer final_check_forbid_count;

	private Integer[] levels;

	public Date getCount_date() {
		return count_date;
	}

	public void setCount_date(Date count_date) {
		this.count_date = count_date;
	}

	public BigDecimal getService_repair_back_rate() {
		return service_repair_back_rate;
	}

	public void setService_repair_back_rate(BigDecimal service_repair_back_rate) {
		this.service_repair_back_rate = service_repair_back_rate;
	}

	public BigDecimal getFinal_inspect_pass_rate() {
		return final_inspect_pass_rate;
	}

	public void setFinal_inspect_pass_rate(BigDecimal final_inspect_pass_rate) {
		this.final_inspect_pass_rate = final_inspect_pass_rate;
	}

	public BigDecimal getIntime_complete_rate() {
		return intime_complete_rate;
	}

	public void setIntime_complete_rate(BigDecimal intime_complete_rate) {
		this.intime_complete_rate = intime_complete_rate;
	}

	public BigDecimal getTotal_plan_processed_rate() {
		return total_plan_processed_rate;
	}

	public void setTotal_plan_processed_rate(
			BigDecimal total_plan_processed_rate) {
		this.total_plan_processed_rate = total_plan_processed_rate;
	}

	public BigDecimal getSection1_plan_processed_rate() {
		return section1_plan_processed_rate;
	}

	public void setSection1_plan_processed_rate(
			BigDecimal section1_plan_processed_rate) {
		this.section1_plan_processed_rate = section1_plan_processed_rate;
	}

	public BigDecimal getSection2_plan_processed_rate() {
		return section2_plan_processed_rate;
	}

	public void setSection2_plan_processed_rate(
			BigDecimal section2_plan_processed_rate) {
		this.section2_plan_processed_rate = section2_plan_processed_rate;
	}

	public BigDecimal getNs_regenerate_rate() {
		return ns_regenerate_rate;
	}

	public void setNs_regenerate_rate(BigDecimal ns_regenerate_rate) {
		this.ns_regenerate_rate = ns_regenerate_rate;
	}

	public BigDecimal getInline_passthrough_rate() {
		return inline_passthrough_rate;
	}

	public void setInline_passthrough_rate(BigDecimal inline_passthrough_rate) {
		this.inline_passthrough_rate = inline_passthrough_rate;
	}

	public BigDecimal getQuotation_lt_rate() {
		return quotation_lt_rate;
	}

	public void setQuotation_lt_rate(BigDecimal quotation_lt_rate) {
		this.quotation_lt_rate = quotation_lt_rate;
	}

	public BigDecimal getDirect_quotation_lt_rate() {
		return direct_quotation_lt_rate;
	}

	public void setDirect_quotation_lt_rate(BigDecimal direct_quotation_lt_rate) {
		this.direct_quotation_lt_rate = direct_quotation_lt_rate;
	}

	public Integer getHalf_period_complete() {
		return half_period_complete;
	}

	public void setHalf_period_complete(Integer half_period_complete) {
		this.half_period_complete = half_period_complete;
	}

	public Integer getMonth_complete() {
		return month_complete;
	}

	public void setMonth_complete(Integer month_complete) {
		this.month_complete = month_complete;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public BigDecimal getService_repair_analysis_lt24_rate() {
		return service_repair_analysis_lt24_rate;
	}

	public void setService_repair_analysis_lt24_rate(
			BigDecimal service_repair_analysis_lt24_rate) {
		this.service_repair_analysis_lt24_rate = service_repair_analysis_lt24_rate;
	}

	public BigDecimal getService_repair_analysis_lt48_rate() {
		return service_repair_analysis_lt48_rate;
	}

	public void setService_repair_analysis_lt48_rate(
			BigDecimal service_repair_analysis_lt48_rate) {
		this.service_repair_analysis_lt48_rate = service_repair_analysis_lt48_rate;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCount_date_start() {
		return count_date_start;
	}

	public void setCount_date_start(Date count_date_start) {
		this.count_date_start = count_date_start;
	}

	public Date getCount_date_end() {
		return count_date_end;
	}

	public void setCount_date_end(Date count_date_end) {
		this.count_date_end = count_date_end;
	}

	public Integer getRegistration() {
		return registration;
	}

	public void setRegistration(Integer registration) {
		this.registration = registration;
	}

	public Integer getUser_agreement() {
		return user_agreement;
	}

	public void setUser_agreement(Integer user_agreement) {
		this.user_agreement = user_agreement;
	}

	public Integer getReturn_to_osh() {
		return return_to_osh;
	}

	public void setReturn_to_osh(Integer return_to_osh) {
		this.return_to_osh = return_to_osh;
	}

	public Integer getUnrepair() {
		return unrepair;
	}

	public void setUnrepair(Integer unrepair) {
		this.unrepair = unrepair;
	}

	public Integer getShipment() {
		return shipment;
	}

	public void setShipment(Integer shipment) {
		this.shipment = shipment;
	}

	public Integer getWork_in_process() {
		return work_in_process;
	}

	public void setWork_in_process(Integer work_in_process) {
		this.work_in_process = work_in_process;
	}

	public Integer getWork_in_storage() {
		return work_in_storage;
	}

	public void setWork_in_storage(Integer work_in_storage) {
		this.work_in_storage = work_in_storage;
	}

	public BigDecimal getAverage_repair_lt() {
		return average_repair_lt;
	}

	public void setAverage_repair_lt(BigDecimal average_repair_lt) {
		this.average_repair_lt = average_repair_lt;
	}

	public BigDecimal getIntime_work_out_rate() {
		return intime_work_out_rate;
	}

	public void setIntime_work_out_rate(BigDecimal intime_work_out_rate) {
		this.intime_work_out_rate = intime_work_out_rate;
	}

	public BigDecimal getAverage_work_lt() {
		return average_work_lt;
	}

	public void setAverage_work_lt(BigDecimal average_work_lt) {
		this.average_work_lt = average_work_lt;
	}

	public BigDecimal getBo_rate() {
		return bo_rate;
	}

	public void setBo_rate(BigDecimal bo_rate) {
		this.bo_rate = bo_rate;
	}

	public BigDecimal getBo_3day_rate() {
		return bo_3day_rate;
	}

	public void setBo_3day_rate(BigDecimal bo_3day_rate) {
		this.bo_3day_rate = bo_3day_rate;
	}

	public Integer getWeekly_of_year() {
		return weekly_of_year;
	}

	public void setWeekly_of_year(Integer weekly_of_year) {
		this.weekly_of_year = weekly_of_year;
	}

	public Integer getFinal_check_pass_count() {
		return final_check_pass_count;
	}

	public void setFinal_check_pass_count(Integer final_check_pass_count) {
		this.final_check_pass_count = final_check_pass_count;
	}

	public Integer getFinal_check_forbid_count() {
		return final_check_forbid_count;
	}

	public void setFinal_check_forbid_count(Integer final_check_forbid_count) {
		this.final_check_forbid_count = final_check_forbid_count;
	}

	public BigDecimal getIntime_complete_slt_rate() {
		return intime_complete_slt_rate;
	}

	public void setIntime_complete_slt_rate(BigDecimal intime_complete_slt_rate) {
		this.intime_complete_slt_rate = intime_complete_slt_rate;
	}

	public BigDecimal getIntime_complete_medium_rate() {
		return intime_complete_medium_rate;
	}

	public void setIntime_complete_medium_rate(
			BigDecimal intime_complete_medium_rate) {
		this.intime_complete_medium_rate = intime_complete_medium_rate;
	}

	public BigDecimal getIntime_complete_light_rate() {
		return intime_complete_light_rate;
	}

	public void setIntime_complete_light_rate(BigDecimal intime_complete_light_rate) {
		this.intime_complete_light_rate = intime_complete_light_rate;
	}

	public Integer[] getLevels() {
		return levels;
	}

	public void setLevels(Integer[] levels) {
		this.levels = levels;
	}

}
