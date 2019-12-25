package com.osh.rvs.form;

import java.io.Serializable;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.CodeListUtils;

public class MaterialForm extends ActionForm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5854897951389999002L;

	@BeanField(title = "修理单号", name = "omr_notifi_no", type = FieldType.String)
	private String omr_notifi_no;

	@BeanField(title = "机身号", name = "serial_no", type = FieldType.String)
	private String serial_no;

	@BeanField(title = "等级", name = "level", type = FieldType.Integer)
	private String level;

	@BeanField(title = "型号", name = "model_name", type = FieldType.String)
	private String model_name;

	@BeanField(title = "工位代码", name = "process_code", type = FieldType.String)
	private String process_code;

	@BeanField(title = "处理结果", name = "operate_result", type = FieldType.Integer)
	private String operate_result;

	@BeanField(title = "中断标记", name = "break_off", type = FieldType.Integer)
	private String break_off;

	@BeanField(title = "返工标记", name = "rework", type = FieldType.Integer)
	private String rework;

	@BeanField(title = "机种", name = "kind", type = FieldType.Integer)
	private String kind;

	private String level_name;
	private String operate_result_name;

	public String getOmr_notifi_no() {
		return omr_notifi_no;
	}

	public void setOmr_notifi_no(String omr_notifi_no) {
		this.omr_notifi_no = omr_notifi_no;
	}

	public String getSerial_no() {
		return serial_no;
	}

	public void setSerial_no(String serial_no) {
		this.serial_no = serial_no;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLevel_name() {
		if (level != null) {
			return CodeListUtils.getValue("material_level", level);
		}
		return level_name;
	}

	public void setLevel_name(String level_name) {
		this.level_name = level_name;
	}

	public String getModel_name() {
		return model_name;
	}

	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}

	public String getProcess_code() {
		return process_code;
	}

	public void setProcess_code(String process_code) {
		this.process_code = process_code;
	}

	public String getOperate_result() {
		return operate_result;
	}

	public void setOperate_result(String operate_result) {
		this.operate_result = operate_result;
	}

	public String getOperate_result_name() {
		if (operate_result != null) {
			return CodeListUtils.getValue("material_operate_result", operate_result);
		}
		return operate_result_name;
	}

	public void setOperate_result_name(String operate_result_name) {
		this.operate_result_name = operate_result_name;
	}

	public String getBreak_off() {
		return break_off;
	}

	public void setBreak_off(String break_off) {
		this.break_off = break_off;
	}

	public String getRework() {
		return rework;
	}

	public void setRework(String rework) {
		this.rework = rework;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

}
