package com.osh.rvs.bean.report;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class WeeklyKpiDataEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6153881959601437794L;

	private Date count_date_start;// 统计日期.起

	private Date count_date_end;// 统计日期.止

	private Integer weekly_of_year;// 周数

	private Integer registration;// 到货受理数

	private Integer user_agreement;// 修理同意数

	private Integer return_to_osh;// 返回ＯＳＨ修理

	private Integer unrepair;// 未修理返回

	private Integer shipment;// 出货总数

	private Integer work_in_process;// WIP 在修数

	private Integer work_in_storage;// WIP 仕掛数

	private BigDecimal intime_complete_rate;// 预定纳期遵守比率

	private BigDecimal average_repair_lt;// 平均修理周期RLT

	private BigDecimal intime_work_out_rate;// 零件到达后4天内出货比率

	private BigDecimal average_work_lt;// 平均工作周期WLT

	private BigDecimal bo_rate;// 当天零件BO率

	private BigDecimal bo_3day_rate;// 三天零件BO率

	private BigDecimal inline_passthrough_rate;// 工程直行率

	private Integer final_check_pass_count;// 最终检查合格件数

	private Integer final_check_forbid_count;// 最终检查不合格件数

	private BigDecimal final_inspect_pass_rate;// 最终检查合格率

	private BigDecimal service_repair_back_rate;// 内镜保修期内返品率（含新品不良）

	private String comment;// 特记事项

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

	public Integer getWeekly_of_year() {
		return weekly_of_year;
	}

	public void setWeekly_of_year(Integer weekly_of_year) {
		this.weekly_of_year = weekly_of_year;
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

	public BigDecimal getIntime_complete_rate() {
		return intime_complete_rate;
	}

	public void setIntime_complete_rate(BigDecimal intime_complete_rate) {
		this.intime_complete_rate = intime_complete_rate;
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

	public BigDecimal getInline_passthrough_rate() {
		return inline_passthrough_rate;
	}

	public void setInline_passthrough_rate(BigDecimal inline_passthrough_rate) {
		this.inline_passthrough_rate = inline_passthrough_rate;
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

	public BigDecimal getFinal_inspect_pass_rate() {
		return final_inspect_pass_rate;
	}

	public void setFinal_inspect_pass_rate(BigDecimal final_inspect_pass_rate) {
		this.final_inspect_pass_rate = final_inspect_pass_rate;
	}

	public BigDecimal getService_repair_back_rate() {
		return service_repair_back_rate;
	}

	public void setService_repair_back_rate(BigDecimal service_repair_back_rate) {
		this.service_repair_back_rate = service_repair_back_rate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
