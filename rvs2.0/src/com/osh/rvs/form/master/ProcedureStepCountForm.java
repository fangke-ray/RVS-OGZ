package com.osh.rvs.form.master;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

public class ProcedureStepCountForm extends ActionForm {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -5188181855910245982L;

	@BeanField(title = "作业步骤计数 ID", name = "procedure_step_count_id", primaryKey = true, length = 11)
	private String procedure_step_count_id;
	@BeanField(title = "作业步骤名称", name = "name", notNull = true, length = 45)
	private String name;
	@BeanField(title = "应用工位", name = "position_id", notNull = true, length = 11)
	private String position_id;
	@BeanField(title = "应用工位代码", name = "process_code")
	private String process_code;
	@BeanField(title = "应用工位名称", name = "position_name")
	private String position_name;
	@BeanField(title = "应用工位分线", name = "px", type = FieldType.UInteger)
	private String px;

	@BeanField(title = "关联类别", name = "relation_type", type = FieldType.UInteger)
	private String relation_type;
	@BeanField(title = "关联机种/型号 ID", name = "relation_id", length = 11)
	private String relation_id;
	@BeanField(title = "次数", name = "step_times", type = FieldType.UInteger)
	private String step_times;
	@BeanField(title = "型号名", name = "model_name")
	private String model_name;
	@BeanField(title = "机种名", name = "category_name")
	private String category_name;

	@BeanField(title = "客户端地址", name = "client_address", length = 15)
	private String client_address;

	/** 最后更新人 */
	@BeanField(title = "更新者", name = "updated_by")
	private String updated_by;
	/** 最后更新时间 */
	@BeanField(title = "更新时间", name = "updated_time", type = FieldType.TimeStamp)
	private String updated_time;

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

	public String getPx() {
		return px;
	}

	public void setPx(String px) {
		this.px = px;
	}

	public String getRelation_type() {
		return relation_type;
	}

	public void setRelation_type(String relation_type) {
		this.relation_type = relation_type;
	}

	public String getRelation_id() {
		return relation_id;
	}

	public void setRelation_id(String relation_id) {
		this.relation_id = relation_id;
	}

	public String getStep_times() {
		return step_times;
	}

	public void setStep_times(String step_times) {
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

	public String getUpdated_time() {
		return updated_time;
	}

	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
	}

	public String getCategory_name() {
		return category_name;
	}

	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}

}
