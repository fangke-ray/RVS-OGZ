package com.osh.rvs.form.inline;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

/**
 * 胶水制作
 * 
 * @Title: GlueMixingForm.java
 * @Package com.osh.rvs.form.inline
 * @Description: TODO
 * @author liuxb
 * @date 2017-12-14 下午4:53:08
 */
public class GlueMixingForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1493064725289381713L;

	@BeanField(title = "胶水原料物料号", name = "code", type = FieldType.String, length = 6)
	private String code;

	@BeanField(title = "乙材种类 ID", name = "partial_id", type = FieldType.String, length = 11, notNull = true)
	private String partial_id;

	@BeanField(title = "保质期", name = "expiration", type = FieldType.Date, notNull = true)
	private String expiration;

	@BeanField(title = "LOT NO.", name = "lot_no", type = FieldType.String, length = 7, notNull = true)
	private String lot_no;

	@BeanField(title = "调制品名", name = "binder_name", type = FieldType.String, length = 12, notNull = true)
	private String binder_name;

	@BeanField(title = "胶水 ID", name = "glue_id", type = FieldType.String, length = 11, notNull = true)
	private String glue_id;

	@BeanField(title = "胶水调制种类 ID", name = "glue_mixing_type", type = FieldType.String, length = 11, notNull = true)
	private String glue_mixing_type;

	@BeanField(title = "胶水调制作业 ID", name = "glue_mixing_process_id", type = FieldType.String, length = 11, notNull = true)
	private String glue_mixing_process_id;

	@BeanField(title = "暂停原因", name = "reason", type = FieldType.Integer, length = 2, notNull = true)
	private String reason;

	@BeanField(title = "备注信息", name = "comments", type = FieldType.String, length = 100)
	private String comments;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	public String getPartial_id() {
		return partial_id;
	}

	public void setPartial_id(String partial_id) {
		this.partial_id = partial_id;
	}

	public String getLot_no() {
		return lot_no;
	}

	public void setLot_no(String lot_no) {
		this.lot_no = lot_no;
	}

	public String getBinder_name() {
		return binder_name;
	}

	public void setBinder_name(String binder_name) {
		this.binder_name = binder_name;
	}

	public String getGlue_id() {
		return glue_id;
	}

	public void setGlue_id(String glue_id) {
		this.glue_id = glue_id;
	}

	public String getGlue_mixing_type() {
		return glue_mixing_type;
	}

	public void setGlue_mixing_type(String glue_mixing_type) {
		this.glue_mixing_type = glue_mixing_type;
	}

	public String getGlue_mixing_process_id() {
		return glue_mixing_process_id;
	}

	public void setGlue_mixing_process_id(String glue_mixing_process_id) {
		this.glue_mixing_process_id = glue_mixing_process_id;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
