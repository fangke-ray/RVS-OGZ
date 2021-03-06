package com.osh.rvs.bean.master;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class DeviceCheckItemEntity implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 8055456003637076713L;

	/** 点检表管理ID */
	private String check_file_manage_id;
	/** 项目序号 */
	private String item_seq;
	/** 点检周期类别 */
	private Integer cycle_type;
	/** 触发限制条件 */
	private Integer trigger_state;

	private Date act_refer_time;

	private String model_relative;

	private String data_relative;
	/** 点检方式 */
	private Integer data_type;
	/** 数值点检上限 */
	private BigDecimal upper_limit;
	/** 数值点检下限 */
	private BigDecimal lower_limit;
	private String refer_upper_from;
	private String refer_lower_from;
	private Integer file_cycle_type;
	private String specified_model_name;

	private Integer tab = 0;
	private Integer gap = null;

	public String getCheck_file_manage_id() {
		return check_file_manage_id;
	}
	public void setCheck_file_manage_id(String check_file_manage_id) {
		this.check_file_manage_id = check_file_manage_id;
	}
	public String getItem_seq() {
		return item_seq;
	}
	public void setItem_seq(String seq_no) {
		this.item_seq = seq_no;
	}
	public Integer getCycle_type() {
		return cycle_type;
	}
	public void setCycle_type(Integer cycle_type) {
		this.cycle_type = cycle_type;
	}
	public Integer getTrigger_state() {
		return trigger_state;
	}
	public void setTrigger_state(Integer trigger_state) {
		this.trigger_state = trigger_state;
	}
	public Date getAct_refer_time() {
		return act_refer_time;
	}
	public void setAct_refer_time(Date act_refer_time) {
		this.act_refer_time = act_refer_time;
	}
	public String getModel_relative() {
		return model_relative;
	}
	public void setModel_relative(String model_relative) {
		this.model_relative = model_relative;
	}
	public Integer getData_type() {
		return data_type;
	}
	public void setData_type(Integer data_type) {
		this.data_type = data_type;
	}
	public BigDecimal getUpper_limit() {
		return upper_limit;
	}
	public void setUpper_limit(BigDecimal upper_limit) {
		this.upper_limit = upper_limit;
	}
	public BigDecimal getLower_limit() {
		return lower_limit;
	}
	public void setLower_limit(BigDecimal lower_limit) {
		this.lower_limit = lower_limit;
	}
	public Integer getFile_cycle_type() {
		return file_cycle_type;
	}
	public void setFile_cycle_type(Integer file_cycle_type) {
		this.file_cycle_type = file_cycle_type;
	}
	public String getData_relative() {
		return data_relative;
	}
	public void setData_relative(String data_relative) {
		this.data_relative = data_relative;
	}
	public Integer getTab() {
		return tab;
	}
	public void setTab(Integer tab) {
		this.tab = tab;
	}
	public String toXmlTag() {
		return "<point type='"+ (data_type == 1 ? "check" : "number") 
				+"' item_seq='"+ item_seq +"' cycle_type='"+ cycle_type 
				+ (gap == null ? "" : "' line='0' gap='" + gap) // line must be just after cycle_type, for regex
				+ (model_relative == null ? "" : "' model_relative='" + model_relative)
				+ (data_relative == null ? "" : "' data_relative='ref_" + data_relative)
				+ (upper_limit == null ? "" : "' upper_limit='" + upper_limit)
				+ (lower_limit == null ? "" : "' lower_limit='" + lower_limit)
				+ (refer_upper_from == null ? "" : "' refer_upper_from='" + refer_upper_from)
				+ (refer_lower_from == null ? "" : "' refer_lower_from='" + refer_lower_from)
				+"' tab='"+ tab + "' shift='1'" 
				+"/>";
	}
	public String toXmlButtonTag() {
		return "<confirm type='"+ (data_type == 1 ? "responser" : "leader") 
				+ "' cycle_type='"+ cycle_type 
				+ (gap == null ? "" : "' line='0' gap='" + gap) // line must be just after cycle_type, for regex
				+"' tab='"+ tab 
				+ (model_relative == null ? "" : "' model_relative='" + model_relative)
				+ "' shift='1'" + (file_cycle_type != null && file_cycle_type == -1 ? " st='vert'" : " st='hori'") 
				+ "/>";
	}
	public String toXmlDateTag() {
		return "<cdate type='"+ (trigger_state == 1 ? "responser" : "leader") 
				+ "' data_type='"+ data_type 
				+ (item_seq == null ? "" : "' item_seq='" + item_seq)
				+"' cycle_type='"+ cycle_type 
				+ (gap == null ? "" : "' line='0' gap='" + gap)
				+"' tab='"+ tab
				+ (model_relative == null ? "" : "' model_relative='" + model_relative)
				+ "' shift='1'" +"/>";
	}
	public String getRefer_upper_from() {
		return refer_upper_from;
	}
	public void setRefer_upper_from(String refer_upper_from) {
		this.refer_upper_from = refer_upper_from;
	}
	public String getRefer_lower_from() {
		return refer_lower_from;
	}
	public void setRefer_lower_from(String refer_lower_from) {
		this.refer_lower_from = refer_lower_from;
	}
	public String getSpecified_model_name() {
		return specified_model_name;
	}
	public void setSpecified_model_name(String specified_model_name) {
		this.specified_model_name = specified_model_name;
	}
	public Integer getGap() {
		return gap;
	}
	public void setGap(Integer gap) {
		this.gap = gap;
	}

}
