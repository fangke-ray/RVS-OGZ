package com.osh.rvs.bean.inline;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Title: GlueMixingProcessEntity.java
 * @Package com.osh.rvs.bean.inline
 * @Description: 胶水调制作业
 * @author liuxb
 * @date 2017-12-15 下午2:17:42
 */
public class GlueMixingProcessEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2095913453534575371L;

	// 胶水调制作业 ID
	private String glue_mixing_process_id;

	// 胶水调制种类 ID
	private String glue_mixing_type_id;

	// 调制原料胶水
	private String glue_id;

	// 调制者
	private String mixing_operator_id;

	// 调制开始时间
	private Date mixing_action_time;

	// 调制完了时间
	private Date mixing_finish_time;

	// 调制确认者
	private String mixing_confirmer_id;

	// 作业环境温度
	private BigDecimal env_temperature;

	// 作业环境湿度
	private BigDecimal env_humidity;

	// 硬度
	private Integer hsd;

	// 表面状态
	private Integer surface_texture;

	// 产出盒数
	private Integer output_amount;

	// 废弃时间
	private Date abandon_time;

	// 废弃担当者
	private String abandon_operator_id;

	// 废弃确认者
	private String abandon_confirmer_id;

	public String getGlue_mixing_process_id() {
		return glue_mixing_process_id;
	}

	public void setGlue_mixing_process_id(String glue_mixing_process_id) {
		this.glue_mixing_process_id = glue_mixing_process_id;
	}

	public String getGlue_mixing_type_id() {
		return glue_mixing_type_id;
	}

	public void setGlue_mixing_type_id(String glue_mixing_type_id) {
		this.glue_mixing_type_id = glue_mixing_type_id;
	}

	public String getGlue_id() {
		return glue_id;
	}

	public void setGlue_id(String glue_id) {
		this.glue_id = glue_id;
	}

	public String getMixing_operator_id() {
		return mixing_operator_id;
	}

	public void setMixing_operator_id(String mixing_operator_id) {
		this.mixing_operator_id = mixing_operator_id;
	}

	public Date getMixing_action_time() {
		return mixing_action_time;
	}

	public void setMixing_action_time(Date mixing_action_time) {
		this.mixing_action_time = mixing_action_time;
	}

	public Date getMixing_finish_time() {
		return mixing_finish_time;
	}

	public void setMixing_finish_time(Date mixing_finish_time) {
		this.mixing_finish_time = mixing_finish_time;
	}

	public String getMixing_confirmer_id() {
		return mixing_confirmer_id;
	}

	public void setMixing_confirmer_id(String mixing_confirmer_id) {
		this.mixing_confirmer_id = mixing_confirmer_id;
	}

	public BigDecimal getEnv_temperature() {
		return env_temperature;
	}

	public void setEnv_temperature(BigDecimal env_temperature) {
		this.env_temperature = env_temperature;
	}

	public BigDecimal getEnv_humidity() {
		return env_humidity;
	}

	public void setEnv_humidity(BigDecimal env_humidity) {
		this.env_humidity = env_humidity;
	}

	public Integer getHsd() {
		return hsd;
	}

	public void setHsd(Integer hsd) {
		this.hsd = hsd;
	}

	public Integer getSurface_texture() {
		return surface_texture;
	}

	public void setSurface_texture(Integer surface_texture) {
		this.surface_texture = surface_texture;
	}

	public Integer getOutput_amount() {
		return output_amount;
	}

	public void setOutput_amount(Integer output_amount) {
		this.output_amount = output_amount;
	}

	public Date getAbandon_time() {
		return abandon_time;
	}

	public void setAbandon_time(Date abandon_time) {
		this.abandon_time = abandon_time;
	}

	public String getAbandon_operator_id() {
		return abandon_operator_id;
	}

	public void setAbandon_operator_id(String abandon_operator_id) {
		this.abandon_operator_id = abandon_operator_id;
	}

	public String getAbandon_confirmer_id() {
		return abandon_confirmer_id;
	}

	public void setAbandon_confirmer_id(String abandon_confirmer_id) {
		this.abandon_confirmer_id = abandon_confirmer_id;
	}

}
