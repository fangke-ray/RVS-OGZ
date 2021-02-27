package com.osh.rvs.bean.master;

import java.io.Serializable;
import java.sql.Timestamp;

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
	private String category_name;

	private String client_address;

	private String material_id;
	private Integer rework;

	/** 最后更新人 */
	private String updated_by;
	/** 最后更新时间 */
	private Timestamp updated_time;

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
	public String getClient_address() {
		return client_address;
	}
	public void setClient_address(String client_address) {
		this.client_address = client_address;
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
	public String getCategory_name() {
		return category_name;
	}
	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}
	public String getMaterial_id() {
		return material_id;
	}
	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}
	public Integer getRework() {
		return rework;
	}
	public void setRework(Integer rework) {
		this.rework = rework;
	}

}
