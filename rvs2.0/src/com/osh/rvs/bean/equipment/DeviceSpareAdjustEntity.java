package com.osh.rvs.bean.equipment;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备工具备品调整记录
 * 
 * @author liuxb
 * 
 */
public class DeviceSpareAdjustEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7505857660550144212L;

	/**
	 * 设备品名 ID
	 */
	private String device_type_id;

	/**
	 * 型号
	 */
	private String model_name;

	/**
	 * 备品种类
	 */
	private Integer device_spare_type;

	/**
	 * 调整日时
	 */
	private Date adjust_time;

	/**
	 * 理由
	 */
	private Integer reason_type;

	/**
	 * 调整量
	 */
	private Integer adjust_inventory;

	/**
	 * 调整负责人
	 */
	private String operator_id;

	/**
	 * 调整备注
	 */
	private String comment;

	// 调整负责人名称
	private String operator_name;

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

	public Integer getDevice_spare_type() {
		return device_spare_type;
	}

	public void setDevice_spare_type(Integer device_spare_type) {
		this.device_spare_type = device_spare_type;
	}

	public Date getAdjust_time() {
		return adjust_time;
	}

	public void setAdjust_time(Date adjust_time) {
		this.adjust_time = adjust_time;
	}

	public Integer getReason_type() {
		return reason_type;
	}

	public void setReason_type(Integer reason_type) {
		this.reason_type = reason_type;
	}

	public Integer getAdjust_inventory() {
		return adjust_inventory;
	}

	public void setAdjust_inventory(Integer adjust_inventory) {
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

}
