package com.osh.rvs.form.equipment;

import java.io.Serializable;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.CodeListUtils;

/**
 * 设备工具备品调整记录
 * 
 * @author liuxb
 * 
 */
public class DeviceSpareAdjustForm extends ActionForm implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6035106423547809464L;

	@BeanField(title = "设备品名 ID", name = "device_type_id", type = FieldType.String, length = 11, notNull = true)
	private String device_type_id;

	@BeanField(title = "型号", name = "model_name", type = FieldType.String, length = 32, notNull = true)
	private String model_name;

	@BeanField(title = "备品种类", name = "device_spare_type", type = FieldType.Integer, length = 1, notNull = true)
	private String device_spare_type;

	@BeanField(title = "调整日时", name = "adjust_time", type = FieldType.DateTime, notNull = true)
	private String adjust_time;

	@BeanField(title = "理由", name = "reason_type", type = FieldType.Integer, length = 2, notNull = true)
	private String reason_type;

	@BeanField(title = "调整量", name = "adjust_inventory", type = FieldType.Integer, length = 5, notNull = true)
	private String adjust_inventory;

	@BeanField(title = "调整负责人", name = "operator_id", type = FieldType.String, length = 11, notNull = true)
	private String operator_id;

	@BeanField(title = "调整备注", name = "comment", type = FieldType.String, length = 250)
	private String comment;

	@BeanField(title = "调整负责人名称", name = "operator_name", type = FieldType.String)
	private String operator_name;

	private String reason_type_name;

	public String getDevice_type_id() {
		return device_type_id;
	}

	public void setDevice_type_id(String device_type_id) {
		this.device_type_id = device_type_id;
	}

	public String getModel_name() {
		return model_name;
	}

	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}

	public String getDevice_spare_type() {
		return device_spare_type;
	}

	public void setDevice_spare_type(String device_spare_type) {
		this.device_spare_type = device_spare_type;
	}

	public String getAdjust_time() {
		return adjust_time;
	}

	public void setAdjust_time(String adjust_time) {
		this.adjust_time = adjust_time;
	}

	public String getReason_type() {
		return reason_type;
	}

	public void setReason_type(String reason_type) {
		if (reason_type.length() < 2) {
			this.reason_type = "0" + reason_type;
		} else {
			this.reason_type = reason_type;
		}
	}

	public String getAdjust_inventory() {
		return adjust_inventory;
	}

	public void setAdjust_inventory(String adjust_inventory) {
		this.adjust_inventory = adjust_inventory;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getOperator_name() {
		return operator_name;
	}

	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}

	public String getReason_type_name() {
		if (reason_type != null) {
			return CodeListUtils.getValue("device_spare_adjust_all_reason_type", reason_type);
		}

		return reason_type_name;
	}

	public void setReason_type_name(String reason_type_name) {
		this.reason_type_name = reason_type_name;
	}

}
