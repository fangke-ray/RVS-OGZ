package com.osh.rvs.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class DeviceTypeEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3239198003185511177L;
	/* 设备工具品名ID */
	private String device_type_id;
	/* 品名 */
	private String name;
	/* 删除标记 */
	private Integer delete_flg;
	/* 最后更新人 */
	private String updated_by;
	/* 最后更新时间 */
	private Timestamp updated_time;
	// 特定设备工具种类
	private Integer specialized;

	public String getDevice_type_id() {
		return device_type_id;
	}

	public void setDevice_type_id(String device_type_id) {
		this.device_type_id = device_type_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getDelete_flg() {
		return delete_flg;
	}

	public void setDelete_flg(Integer delete_flg) {
		this.delete_flg = delete_flg;
	}

	public String getUpdated_by() {
		return updated_by;
	}

	public void setUpdated_by(String updated_by) {
		this.updated_by = updated_by;
	}

	public Timestamp getUpdated_time() {
		return updated_time;
	}

	public void setUpdated_time(Timestamp updated_time) {
		this.updated_time = updated_time;
	}

	public Integer getSpecialized() {
		return specialized;
	}

	public void setSpecialized(Integer specialized) {
		this.specialized = specialized;
	}

}
