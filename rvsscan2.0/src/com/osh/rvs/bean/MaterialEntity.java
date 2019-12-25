package com.osh.rvs.bean;

import java.io.Serializable;

/**
 * 维修品
 * 
 * @Description
 * @author dell
 * @date 2019-12-20 下午4:41:36
 */
public class MaterialEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6073819282835988826L;

	/**
	 * 修理单号
	 */
	private String omr_notifi_no;

	/**
	 * 机身号
	 */
	private String serial_no;

	/**
	 * 等级
	 */
	private Integer level;

	/**
	 * 型号
	 */
	private String model_name;

	/**
	 * 工位代码
	 */
	private String process_code;

	/**
	 * 处理结果
	 */
	private Integer operate_result;

	/**
	 * 中断标记
	 */
	private Integer break_off;

	/**
	 * 返工标记
	 */
	private Integer rework;

	/**
	 * 机种
	 */
	private Integer kind;

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

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
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

	public Integer getOperate_result() {
		return operate_result;
	}

	public void setOperate_result(Integer operate_result) {
		this.operate_result = operate_result;
	}

	public Integer getBreak_off() {
		return break_off;
	}

	public void setBreak_off(Integer break_off) {
		this.break_off = break_off;
	}

	public Integer getRework() {
		return rework;
	}

	public void setRework(Integer rework) {
		this.rework = rework;
	}

	public Integer getKind() {
		return kind;
	}

	public void setKind(Integer kind) {
		this.kind = kind;
	}

}
