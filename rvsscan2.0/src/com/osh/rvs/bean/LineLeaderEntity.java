package com.osh.rvs.bean;

import java.io.Serializable;
import java.util.Date;

public class LineLeaderEntity implements Serializable {

	private static final long serialVersionUID = -1677198428012226361L;

	private Date agreed_date;
	private Integer expedited;
	private String sorc_no;
	private String esas_no;
	private Date partical_order_date;
	private Integer partical_bo;
	private Date arrival_plan_date;
	private String symbol;
	private Integer level;
	private String serial_no;
	private String category_name;
	private String model_name;
	private Integer operate_result;
	private Date scheduled_date;
	private String process_code;
	private String otherline_process_code;
	private String material_id;
	private String position_id;
	private Integer is_reworking;
	private Integer is_today;
	private Integer direct_flg;

	public Date getAgreed_date() {
		return agreed_date;
	}
	public void setAgreed_date(Date agreed_date) {
		this.agreed_date = agreed_date;
	}
	public Integer getExpedited() {
		return expedited;
	}
	public void setExpedited(Integer expedited) {
		this.expedited = expedited;
	}
	public String getSorc_no() {
		return sorc_no;
	}
	public void setSorc_no(String sorc_no) {
		this.sorc_no = sorc_no;
	}
	public String getEsas_no() {
		return esas_no;
	}
	public void setEsas_no(String esas_no) {
		this.esas_no = esas_no;
	}
	public Date getPartical_order_date() {
		return partical_order_date;
	}
	public void setPartical_order_date(Date partical_order_date) {
		this.partical_order_date = partical_order_date;
	}
	public Integer getPartical_bo() {
		return partical_bo;
	}
	public void setPartical_bo(Integer partical_bo) {
		this.partical_bo = partical_bo;
	}
	public Date getArrival_plan_date() {
		return arrival_plan_date;
	}
	public void setArrival_plan_date(Date arrival_plan_date) {
		this.arrival_plan_date = arrival_plan_date;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public String getSerial_no() {
		return serial_no;
	}
	public void setSerial_no(String serial_no) {
		this.serial_no = serial_no;
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
	public Integer getOperate_result() {
		return operate_result;
	}
	public void setOperate_result(Integer operate_result) {
		this.operate_result = operate_result;
	}
	public Date getScheduled_date() {
		return scheduled_date;
	}
	public void setScheduled_date(Date scheduled_date) {
		this.scheduled_date = scheduled_date;
	}
	public String getOtherline_process_code() {
		return otherline_process_code;
	}
	public void setOtherline_process_code(String otherline_process_code) {
		this.otherline_process_code = otherline_process_code;
	}
	/**
	 * @return the process_code
	 */
	public String getProcess_code() {
		return process_code;
	}
	/**
	 * @param process_code the process_code to set
	 */
	public void setProcess_code(String process_code) {
		this.process_code = process_code;
	}
	public String getPosition_id() {
		return position_id;
	}
	public void setPosition_id(String position_id) {
		this.position_id = position_id;
	}
	/**
	 * @return the material_id
	 */
	public String getMaterial_id() {
		return material_id;
	}
	/**
	 * @param material_id the material_id to set
	 */
	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}
	/**
	 * @return the is_reworking
	 */
	public Integer getIs_reworking() {
		return is_reworking;
	}
	/**
	 * @param is_reworking the is_reworking to set
	 */
	public void setIs_reworking(Integer is_reworking) {
		this.is_reworking = is_reworking;
	}
	/**
	 * @return the is_today
	 */
	public Integer getIs_today() {
		return is_today;
	}
	/**
	 * @param is_today the is_today to set
	 */
	public void setIs_today(Integer is_today) {
		this.is_today = is_today;
	}
	public Integer getDirect_flg() {
		return direct_flg;
	}
	public void setDirect_flg(Integer direct_flg) {
		this.direct_flg = direct_flg;
	}
}
