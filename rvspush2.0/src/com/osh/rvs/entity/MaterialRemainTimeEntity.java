package com.osh.rvs.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 维修对象预计时间
 */
public class MaterialRemainTimeEntity implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8638271903985662081L;
	
	private String material_id;
	private Integer remain_before_com_minutes;
	private Integer remain_minutes;
	private Date expected_finish_time;

	public String getMaterial_id() {
		return material_id;
	}
	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}
	public Integer getRemain_before_com_minutes() {
		return remain_before_com_minutes;
	}
	public void setRemain_before_com_minutes(Integer remain_before_com_minutes) {
		this.remain_before_com_minutes = remain_before_com_minutes;
	}
	public Integer getRemain_minutes() {
		return remain_minutes;
	}
	public void setRemain_minutes(Integer remain_minutes) {
		this.remain_minutes = remain_minutes;
	}
	public Date getExpected_finish_time() {
		return expected_finish_time;
	}
	public void setExpected_finish_time(Date expected_finish_time) {
		this.expected_finish_time = expected_finish_time;
	}
	
}
