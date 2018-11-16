package com.osh.rvs.bean.master;

import java.io.Serializable;
import java.util.Date;

/**
 * @Title: GlueMixingTypeEntity.java
 * @Package com.osh.rvs.bean.master
 * @Description: 胶水调制种类
 * @author liuxb
 * @date 2017-12-15 下午2:08:02
 */
public class GlueMixingTypeEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6217912329393371841L;

	// 胶水调制种类 ID
	private String glue_mixing_type_id;

	// 粘结剂品名
	private String binder_name;

	// 原料胶水 ID
	private String partial_id;

	// 作业环境温度下限
	private Integer env_t_lower_limit;

	// 作业环境温度上限
	private Integer env_t_upper_limit;

	// 作业环境湿度下限
	private Integer env_h_lower_limit;

	// 作业环境湿度上限
	private Integer env_h_upper_limit;

	// 合格硬度下限
	private Integer hsd_lower_limit;

	// 合格硬度上限
	private Integer hsd_upper_limit;

	// 保质时间
	private Integer guarantee;

	// 删除标记
	private Integer delete_flg;

	// 更新者
	private String update_by;

	// 更新时间
	private Date update_time;

	public String getGlue_mixing_type_id() {
		return glue_mixing_type_id;
	}

	public void setGlue_mixing_type_id(String glue_mixing_type_id) {
		this.glue_mixing_type_id = glue_mixing_type_id;
	}

	public String getBinder_name() {
		return binder_name;
	}

	public void setBinder_name(String binder_name) {
		this.binder_name = binder_name;
	}

	public String getPartial_id() {
		return partial_id;
	}

	public void setPartial_id(String partial_id) {
		this.partial_id = partial_id;
	}

	public Integer getEnv_t_lower_limit() {
		return env_t_lower_limit;
	}

	public void setEnv_t_lower_limit(Integer env_t_lower_limit) {
		this.env_t_lower_limit = env_t_lower_limit;
	}

	public Integer getEnv_t_upper_limit() {
		return env_t_upper_limit;
	}

	public void setEnv_t_upper_limit(Integer env_t_upper_limit) {
		this.env_t_upper_limit = env_t_upper_limit;
	}

	public Integer getEnv_h_lower_limit() {
		return env_h_lower_limit;
	}

	public void setEnv_h_lower_limit(Integer env_h_lower_limit) {
		this.env_h_lower_limit = env_h_lower_limit;
	}

	public Integer getEnv_h_upper_limit() {
		return env_h_upper_limit;
	}

	public void setEnv_h_upper_limit(Integer env_h_upper_limit) {
		this.env_h_upper_limit = env_h_upper_limit;
	}

	public Integer getHsd_lower_limit() {
		return hsd_lower_limit;
	}

	public void setHsd_lower_limit(Integer hsd_lower_limit) {
		this.hsd_lower_limit = hsd_lower_limit;
	}

	public Integer getHsd_upper_limit() {
		return hsd_upper_limit;
	}

	public void setHsd_upper_limit(Integer hsd_upper_limit) {
		this.hsd_upper_limit = hsd_upper_limit;
	}

	public Integer getGuarantee() {
		return guarantee;
	}

	public void setGuarantee(Integer guarantee) {
		this.guarantee = guarantee;
	}

	public Integer getDelete_flg() {
		return delete_flg;
	}

	public void setDelete_flg(Integer delete_flg) {
		this.delete_flg = delete_flg;
	}

	public String getUpdate_by() {
		return update_by;
	}

	public void setUpdate_by(String update_by) {
		this.update_by = update_by;
	}

	public Date getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(Date update_time) {
		this.update_time = update_time;
	}

}
