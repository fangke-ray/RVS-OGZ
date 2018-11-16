package com.osh.rvs.form.report;

import java.io.Serializable;

import com.osh.rvs.form.UploadForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

public class WeeklyKpiDataForm extends UploadForm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8574381203847719963L;

	@BeanField(title = "统计日期起", name = "count_date_start", type = FieldType.Date, notNull = true)
	private String count_date_start;

	@BeanField(title = "统计日期止", name = "count_date_end", type = FieldType.Date, notNull = true)
	private String count_date_end;

	@BeanField(title = "周数", name = "weekly_of_year", type = FieldType.Integer, length = 2)
	private String weekly_of_year;

	@BeanField(title = "到货受理数", name = "registration", type = FieldType.Integer, length = 3)
	private String registration;

	@BeanField(title = "修理同意数", name = "user_agreement", type = FieldType.Integer, length = 3)
	private String user_agreement;

	@BeanField(title = "返回ＯＳＨ修理", name = "return_to_osh", type = FieldType.Integer, length = 3)
	private String return_to_osh;

	@BeanField(title = "未修理返回", name = "unrepair", type = FieldType.Integer, length = 3)
	private String unrepair;

	@BeanField(title = "出货总数", name = "shipment", type = FieldType.Integer, length = 3)
	private String shipment;

	@BeanField(title = "WIP 在修数", name = "work_in_process", type = FieldType.Integer, length = 3)
	private String work_in_process;

	@BeanField(title = "WIP 仕掛数", name = "work_in_storage", type = FieldType.Integer, length = 3)
	private String work_in_storage;

	@BeanField(title = "大修理LT", name = "intime_complete_rate", type = FieldType.Double, length = 5, scale = 2)
	private String intime_complete_rate;

	@BeanField(title = "平均修理周期RLT", name = "average_repair_lt", type = FieldType.Double, length = 4, scale = 2)
	private String average_repair_lt;

	@BeanField(title = "零件到达后4天内出货比率", name = "intime_work_out_rate", type = FieldType.Double, length = 5, scale = 2)
	private String intime_work_out_rate;

	@BeanField(title = "平均工作周期WLT", name = "average_work_lt", type = FieldType.Double, length = 4, scale = 2)
	private String average_work_lt;

	@BeanField(title = "当天零件BO率", name = "bo_rate", type = FieldType.Double, length = 5, scale = 2)
	private String bo_rate;

	@BeanField(title = "三天零件BO率", name = "bo_3day_rate", type = FieldType.Double, length = 5, scale = 2)
	private String bo_3day_rate;

	@BeanField(title = "工程直行率", name = "inline_passthrough_rate", type = FieldType.Double, length = 5, scale = 2)
	private String inline_passthrough_rate;

	@BeanField(title = "最终检查合格件数", name = "final_check_pass_count", type = FieldType.Integer, length = 3)
	private String final_check_pass_count;

	@BeanField(title = "最终检查不合格件数", name = "final_check_forbid_count", type = FieldType.Integer, length = 3)
	private String final_check_forbid_count;

	@BeanField(title = "最终检查合格率", name = "final_inspect_pass_rate", type = FieldType.Double, length = 7, scale = 3)
	private String final_inspect_pass_rate;

	@BeanField(title = "内镜保修期内返品率（含新品不良）", name = "service_repair_back_rate", type = FieldType.Double, length = 7, scale = 3)
	private String service_repair_back_rate;

	@BeanField(title = " 特记事项", name = "comment", type = FieldType.String, length = 2000)
	private String comment;

	private String fileName;

	private String confirmfilename;

	private String target;// 目標

	public String getCount_date_start() {
		return count_date_start;
	}

	public void setCount_date_start(String count_date_start) {
		this.count_date_start = count_date_start;
	}

	public String getCount_date_end() {
		return count_date_end;
	}

	public void setCount_date_end(String count_date_end) {
		this.count_date_end = count_date_end;
	}

	public String getWeekly_of_year() {
		return weekly_of_year;
	}

	public void setWeekly_of_year(String weekly_of_year) {
		this.weekly_of_year = weekly_of_year;
	}

	public String getRegistration() {
		return registration;
	}

	public void setRegistration(String registration) {
		this.registration = registration;
	}

	public String getUser_agreement() {
		return user_agreement;
	}

	public void setUser_agreement(String user_agreement) {
		this.user_agreement = user_agreement;
	}

	public String getReturn_to_osh() {
		return return_to_osh;
	}

	public void setReturn_to_osh(String return_to_osh) {
		this.return_to_osh = return_to_osh;
	}

	public String getUnrepair() {
		return unrepair;
	}

	public void setUnrepair(String unrepair) {
		this.unrepair = unrepair;
	}

	public String getShipment() {
		return shipment;
	}

	public void setShipment(String shipment) {
		this.shipment = shipment;
	}

	public String getWork_in_process() {
		return work_in_process;
	}

	public void setWork_in_process(String work_in_process) {
		this.work_in_process = work_in_process;
	}

	public String getWork_in_storage() {
		return work_in_storage;
	}

	public void setWork_in_storage(String work_in_storage) {
		this.work_in_storage = work_in_storage;
	}

	public String getIntime_complete_rate() {
		return intime_complete_rate;
	}

	public void setIntime_complete_rate(String intime_complete_rate) {
		this.intime_complete_rate = intime_complete_rate;
	}

	public String getAverage_repair_lt() {
		return average_repair_lt;
	}

	public void setAverage_repair_lt(String average_repair_lt) {
		this.average_repair_lt = average_repair_lt;
	}

	public String getIntime_work_out_rate() {
		return intime_work_out_rate;
	}

	public void setIntime_work_out_rate(String intime_work_out_rate) {
		this.intime_work_out_rate = intime_work_out_rate;
	}

	public String getAverage_work_lt() {
		return average_work_lt;
	}

	public void setAverage_work_lt(String average_work_lt) {
		this.average_work_lt = average_work_lt;
	}

	public String getBo_rate() {
		return bo_rate;
	}

	public void setBo_rate(String bo_rate) {
		this.bo_rate = bo_rate;
	}

	public String getBo_3day_rate() {
		return bo_3day_rate;
	}

	public void setBo_3day_rate(String bo_3day_rate) {
		this.bo_3day_rate = bo_3day_rate;
	}

	public String getInline_passthrough_rate() {
		return inline_passthrough_rate;
	}

	public void setInline_passthrough_rate(String inline_passthrough_rate) {
		this.inline_passthrough_rate = inline_passthrough_rate;
	}

	public String getFinal_check_pass_count() {
		return final_check_pass_count;
	}

	public void setFinal_check_pass_count(String final_check_pass_count) {
		this.final_check_pass_count = final_check_pass_count;
	}

	public String getFinal_check_forbid_count() {
		return final_check_forbid_count;
	}

	public void setFinal_check_forbid_count(String final_check_forbid_count) {
		this.final_check_forbid_count = final_check_forbid_count;
	}

	public String getFinal_inspect_pass_rate() {
		return final_inspect_pass_rate;
	}

	public void setFinal_inspect_pass_rate(String final_inspect_pass_rate) {
		this.final_inspect_pass_rate = final_inspect_pass_rate;
	}

	public String getService_repair_back_rate() {
		return service_repair_back_rate;
	}

	public void setService_repair_back_rate(String service_repair_back_rate) {
		this.service_repair_back_rate = service_repair_back_rate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getConfirmfilename() {
		return confirmfilename;
	}

	public void setConfirmfilename(String confirmfilename) {
		this.confirmfilename = confirmfilename;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

}
