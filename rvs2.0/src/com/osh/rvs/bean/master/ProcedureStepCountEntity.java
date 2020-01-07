package com.osh.rvs.bean.master;

import java.io.Serializable;

public class ProcedureStepCountEntity implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 6513951781944836150L;

	private String procedure_step_count_id;
	private String name;
	private String position_id;
	private String process_code;
	private String position_name;
	private Integer px;

	private Integer relation_type;
	private String relation_id;
	private Integer step_times;
	private String model_name;
	public String getProcedure_step_count_id() {
		return procedure_step_count_id;
	}
	public void setProcedure_step_count_id(String procedure_step_count_id) {
		this.procedure_step_count_id = procedure_step_count_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPosition_id() {
		return position_id;
	}
	public void setPosition_id(String position_id) {
		this.position_id = position_id;
	}
	public String getProcess_code() {
		return process_code;
	}
	public void setProcess_code(String process_code) {
		this.process_code = process_code;
	}
	public String getPosition_name() {
		return position_name;
	}
	public void setPosition_name(String position_name) {
		this.position_name = position_name;
	}
	public Integer getPx() {
		return px;
	}
	public void setPx(Integer px) {
		this.px = px;
	}
	public Integer getRelation_type() {
		return relation_type;
	}
	public void setRelation_type(Integer relation_type) {
		this.relation_type = relation_type;
	}
	public String getRelation_id() {
		return relation_id;
	}
	public void setRelation_id(String relation_id) {
		this.relation_id = relation_id;
	}
	public Integer getStep_times() {
		return step_times;
	}
	public void setStep_times(Integer step_times) {
		this.step_times = step_times;
	}
	public String getModel_name() {
		return model_name;
	}
	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}

}
