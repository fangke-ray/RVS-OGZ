package com.osh.rvs.bean.inline;

import java.io.Serializable;
import java.util.Date;

/**
 * @Title: GlueMixingProcessPaceEntity.java
 * @Package com.osh.rvs.bean.inline
 * @Description: 胶水调制作业分段时间
 * @author liuxb
 * @date 2017-12-15 下午2:47:10
 */
public class GlueMixingProcessPaceEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5851737932072100905L;

	// 胶水调制作业 ID
	private String glue_mixing_process_id;

	// 分段时间
	private Integer pace;

	// 调制者
	private String mixing_operator_id;

	// 调制开始时间
	private Date mixing_action_time;

	// 调制完了时间
	private Date mixing_finish_time;

	public String getGlue_mixing_process_id() {
		return glue_mixing_process_id;
	}

	public void setGlue_mixing_process_id(String glue_mixing_process_id) {
		this.glue_mixing_process_id = glue_mixing_process_id;
	}

	public Integer getPace() {
		return pace;
	}

	public void setPace(Integer pace) {
		this.pace = pace;
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

}
