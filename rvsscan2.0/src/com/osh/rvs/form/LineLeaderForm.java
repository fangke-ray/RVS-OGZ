package com.osh.rvs.form;

import java.io.Serializable;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

public class LineLeaderForm extends ActionForm implements Serializable {

	private static final long serialVersionUID = -7578466447353413939L;

	@BeanField(title = "同意日期", name = "agreed_date", type = FieldType.Date)
	private String agreed_date;
	@BeanField(title = "加急", name = "expedited", type = FieldType.Integer)
	private String expedited;
	private String sorc_no;
	private String esas_no;
	@BeanField(title = "零件订购日", name = "partical_order_date", type = FieldType.Date)
	private String partical_order_date;
	@BeanField(title = "BO", name = "partical_bo", type = FieldType.Integer)
	private String partical_bo;
	@BeanField(title = "入库预订日", name = "arrival_plan_date", type = FieldType.Date)
	private String arrival_plan_date;
	private String symbol;
	@BeanField(title = "等级", name = "level", type = FieldType.Integer)
	private String level;
	private String serial_no;
	private String category_name;
	private String model_name;
	@BeanField(title = "处理结果", name = "operate_result", type = FieldType.Integer)
	private String operate_result;
	private String process_code;
	@BeanField(title = "产出安排", name = "scheduled_date", type = FieldType.Date)
	private String scheduled_date;
	private String material_id;
	private String position_id;
	@BeanField(title = "返工中", name = "is_reworking", type = FieldType.Integer)
	private String is_reworking;
	@BeanField(title = "今日计划", name = "is_today", type = FieldType.Integer)
	private String is_today;

	public String getAgreed_date() {
		return agreed_date;
	}
	public void setAgreed_date(String agreed_date) {
		this.agreed_date = agreed_date;
	}
	public String getExpedited() {
		return expedited;
	}
	public void setExpedited(String expedited) {
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
	public String getPartical_order_date() {
		return partical_order_date;
	}
	public void setPartical_order_date(String partical_order_date) {
		this.partical_order_date = partical_order_date;
	}
	public String getPartical_bo() {
		return partical_bo;
	}
	public void setPartical_bo(String partical_bo) {
		this.partical_bo = partical_bo;
	}
	public String getArrival_plan_date() {
		return arrival_plan_date;
	}
	public void setArrival_plan_date(String arrival_plan_date) {
		this.arrival_plan_date = arrival_plan_date;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
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
	public String getOperate_result() {
		return operate_result;
	}
	public void setOperate_result(String operate_result) {
		this.operate_result = operate_result;
	}
	public String getProcess_code() {
		return process_code;
	}
	public void setProcess_code(String process_code) {
		this.process_code = process_code;
	}
	public String getScheduled_date() {
		return scheduled_date;
	}
	public void setScheduled_date(String scheduled_date) {
		this.scheduled_date = scheduled_date;
	}
	public String getOtherline_process_code() {
		return otherline_process_code;
	}
	public void setOtherline_process_code(String otherline_process_code) {
		this.otherline_process_code = otherline_process_code;
	}
	private String otherline_process_code;

	public String getMaterial_id() {
		return material_id;
	}
	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}
	public String getPosition_id() {
		return position_id;
	}
	public void setPosition_id(String position_id) {
		this.position_id = position_id;
	}
	public String getIs_reworking() {
		return is_reworking;
	}
	public void setIs_reworking(String is_reworking) {
		this.is_reworking = is_reworking;
	}
	public String getIs_today() {
		return is_today;
	}
	public void setIs_today(String is_today) {
		this.is_today = is_today;
	}
}
