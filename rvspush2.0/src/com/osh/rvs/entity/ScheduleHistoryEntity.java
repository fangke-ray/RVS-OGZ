package com.osh.rvs.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 */
public class ScheduleHistoryEntity implements Serializable {

	private static final long serialVersionUID = 8873882909495347352L;
	// 计划投入日期
	private Date scheduled_date;
	// 维修对象ID
	private String material_id;
	// 投入类型
	private Integer in_schedule_means;
	// 移出标记
	private Integer remove_flg;
	// 登录者
	private String insert_operator_name;
	// 移出者
	private String remove_operator_name;
	// 排入时入库预定日
	private Date arrival_plan_date;
	// 排入时加急
	private Integer scheduled_expedited;
	// 发生故障编号
	private String alarm_messsage_id;

	// SORC No.
	private String sorc_no;
	// 机种
	private String category_name;

	// 型号
	private String model_name;
	// 机身号
	private String serial_no;

	// 维修课室
	private String section_name;
	// 等级
	private Integer level;
	// 委托处
	private Integer ocm;
	// 客户同意
	private Date agreed_date;
	// 8天纳期
	private Date scheduled_expire_date;

	// 零件入库 纳期
	private Date partial_expire_date;

	// 完成日期
	private Date outline_date;

	// 最新 计划安排日
	private Date new_scheduled_date;
	// 最新 进展工位
	private Date new_processing;
	// 故障理由
	private String alarm_messsage_content;
	// 最新 入库预定日
	private Date new_arrival_plan_date;
	// 最新 加急状态
	private Integer new_scheduled_expedited;

	private Integer plan_day_period;

	public Date getScheduled_date() {
		return scheduled_date;
	}
	public void setScheduled_date(Date scheduled_date) {
		this.scheduled_date = scheduled_date;
	}
	public String getMaterial_id() {
		return material_id;
	}
	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}
	public Integer getIn_schedule_means() {
		return in_schedule_means;
	}
	public void setIn_schedule_means(Integer in_schedule_means) {
		this.in_schedule_means = in_schedule_means;
	}
	public Integer getRemove_flg() {
		return remove_flg;
	}
	public void setRemove_flg(Integer remove_flg) {
		this.remove_flg = remove_flg;
	}
	public String getInsert_operator_name() {
		return insert_operator_name;
	}
	public void setInsert_operator_name(String insert_operator_name) {
		this.insert_operator_name = insert_operator_name;
	}
	public String getRemove_operator_name() {
		return remove_operator_name;
	}
	public void setRemove_operator_name(String remove_operator_name) {
		this.remove_operator_name = remove_operator_name;
	}
	public Date getArrival_plan_date() {
		return arrival_plan_date;
	}
	public void setArrival_plan_date(Date arrival_plan_date) {
		this.arrival_plan_date = arrival_plan_date;
	}
	public Integer getScheduled_expedited() {
		return scheduled_expedited;
	}
	public void setScheduled_expedited(Integer scheduled_expedited) {
		this.scheduled_expedited = scheduled_expedited;
	}
	public String getAlarm_messsage_id() {
		return alarm_messsage_id;
	}
	public void setAlarm_messsage_id(String alarm_messsage_id) {
		this.alarm_messsage_id = alarm_messsage_id;
	}
	public String getSorc_no() {
		return sorc_no;
	}
	public void setSorc_no(String sorc_no) {
		this.sorc_no = sorc_no;
	}
	public String getCategory_name() {
		return category_name;
	}
	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}
	public String getModel_name() {
		return model_name;
	}
	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}
	public String getSerial_no() {
		return serial_no;
	}
	public void setSerial_no(String serial_no) {
		this.serial_no = serial_no;
	}
	public String getSection_name() {
		return section_name;
	}
	public void setSection_name(String section_name) {
		this.section_name = section_name;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public Integer getOcm() {
		return ocm;
	}
	public void setOcm(Integer ocm) {
		this.ocm = ocm;
	}
	public Date getAgreed_date() {
		return agreed_date;
	}
	public void setAgreed_date(Date agreed_date) {
		this.agreed_date = agreed_date;
	}
	public Date getScheduled_expire_date() {
		return scheduled_expire_date;
	}
	public void setScheduled_expire_date(Date scheduled_expire_date) {
		this.scheduled_expire_date = scheduled_expire_date;
	}
	public Date getPartial_expire_date() {
		return partial_expire_date;
	}
	public void setPartial_expire_date(Date partial_expire_date) {
		this.partial_expire_date = partial_expire_date;
	}
	public Date getNew_scheduled_date() {
		return new_scheduled_date;
	}
	public void setNew_scheduled_date(Date new_scheduled_date) {
		this.new_scheduled_date = new_scheduled_date;
	}
	public Date getNew_processing() {
		return new_processing;
	}
	public void setNew_processing(Date new_processing) {
		this.new_processing = new_processing;
	}
	public String getAlarm_messsage_content() {
		return alarm_messsage_content;
	}
	public void setAlarm_messsage_content(String alarm_messsage_content) {
		this.alarm_messsage_content = alarm_messsage_content;
	}
	public Date getNew_arrival_plan_date() {
		return new_arrival_plan_date;
	}
	public void setNew_arrival_plan_date(Date new_arrival_plan_date) {
		this.new_arrival_plan_date = new_arrival_plan_date;
	}
	public Integer getNew_scheduled_expedited() {
		return new_scheduled_expedited;
	}
	public void setNew_scheduled_expedited(Integer new_scheduled_expedited) {
		this.new_scheduled_expedited = new_scheduled_expedited;
	}
	public Date getOutline_date() {
		return outline_date;
	}
	public void setOutline_date(Date outline_date) {
		this.outline_date = outline_date;
	}
	public Integer getPlan_day_period() {
		return plan_day_period;
	}
	public void setPlan_day_period(Integer plan_day_period) {
		this.plan_day_period = plan_day_period;
	}

	
}
