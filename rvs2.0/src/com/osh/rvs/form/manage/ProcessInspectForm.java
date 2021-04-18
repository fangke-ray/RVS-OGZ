package com.osh.rvs.form.manage;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

public class ProcessInspectForm extends ActionForm {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7978201807580739478L;
	@BeanField(title = "Key", name = "process_inspect_key", primaryKey = true, length = 11)
	private String process_inspect_key;
	@BeanField(title = "实施选项", name = "perform_option", type=FieldType.Integer, notNull = true)
	private String perform_option;
	@BeanField(title = "归档日期", name = "filing_date", type=FieldType.Date)
	private String filing_date;
	@BeanField(title = "工程", name = "line_id", length = 11)
	private String line_id;
	@BeanField(title = "工程名", name = "line_name")
	private String line_name;
	@BeanField(title = "操作者", name = "operator_id", length = 11)
	private String operator_id;
	@BeanField(title = "操作者", name = "operator_name")
	private String operator_name;
	@BeanField(title = "监察者", name = "inspector_id", length = 11)
	private String inspector_id;
	@BeanField(title = "监察者", name = "inspector_name")
	private String inspector_name;
	@BeanField(title = "监察日", name = "inspect_date", type=FieldType.Date)
	private String inspect_date;
	@BeanField(title = "监察日起", name = "inspect_date_start", type=FieldType.Date)
	private String inspect_date_start;
	@BeanField(title = "监察日止", name = "inspect_date_end", type=FieldType.Date)
	private String inspect_date_end;
	@BeanField(title = "型号 ID", name = "model_id", length = 11)
	private String model_id;
	@BeanField(title = "型号名", name = "model_name")
	private String model_name;
	@BeanField(title = "机身号", name = "serial_no", length = 20)
	private String serial_no;
	@BeanField(title = "作业时间", name = "process_seconds", type=FieldType.UDouble, length = 4, scale = 1)
	private String process_seconds;
	@BeanField(title = "标准时间", name = "standard_seconds", type=FieldType.UDouble, length = 4, scale = 1)
	private String standard_seconds;

	@BeanField(title = "监查情况", name = "situation", length = 11)
	private String situation;
	@BeanField(title = "实施对策", name = "countermeasures", length = 11)
	private String countermeasures;
	@BeanField(title = "结果", name = "conclusion", length = 11)
	private String conclusion;
	public String getProcess_inspect_key() {
		return process_inspect_key;
	}
	public void setProcess_inspect_key(String process_inspect_key) {
		this.process_inspect_key = process_inspect_key;
	}
	public String getPerform_option() {
		return perform_option;
	}
	public void setPerform_option(String perform_option) {
		this.perform_option = perform_option;
	}
	public String getFiling_date() {
		return filing_date;
	}
	public void setFiling_date(String filing_date) {
		this.filing_date = filing_date;
	}
	public String getLine_id() {
		return line_id;
	}
	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}
	public String getLine_name() {
		return line_name;
	}
	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}
	public String getOperator_id() {
		return operator_id;
	}
	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}
	public String getOperator_name() {
		return operator_name;
	}
	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}
	public String getInspector_id() {
		return inspector_id;
	}
	public void setInspector_id(String inspector_id) {
		this.inspector_id = inspector_id;
	}
	public String getInspector_name() {
		return inspector_name;
	}
	public void setInspector_name(String inspector_name) {
		this.inspector_name = inspector_name;
	}
	public String getInspect_date() {
		return inspect_date;
	}
	public void setInspect_date(String inspect_date) {
		this.inspect_date = inspect_date;
	}
	public String getInspect_date_start() {
		return inspect_date_start;
	}
	public void setInspect_date_start(String inspect_date_start) {
		this.inspect_date_start = inspect_date_start;
	}
	public String getInspect_date_end() {
		return inspect_date_end;
	}
	public void setInspect_date_end(String inspect_date_end) {
		this.inspect_date_end = inspect_date_end;
	}
	public String getModel_id() {
		return model_id;
	}
	public void setModel_id(String model_id) {
		this.model_id = model_id;
	}
	public String getModel_name() {
		return model_name;
	}
	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}
	public String getSerial_no() {
		return serial_no;
	}
	public void setSerial_no(String serial_no) {
		this.serial_no = serial_no;
	}
	public String getProcess_seconds() {
		return process_seconds;
	}
	public void setProcess_seconds(String process_seconds) {
		this.process_seconds = process_seconds;
	}
	public String getStandard_seconds() {
		return standard_seconds;
	}
	public void setStandard_seconds(String standard_seconds) {
		this.standard_seconds = standard_seconds;
	}
	public String getSituation() {
		return situation;
	}
	public void setSituation(String situation) {
		this.situation = situation;
	}
	public String getCountermeasures() {
		return countermeasures;
	}
	public void setCountermeasures(String countermeasures) {
		this.countermeasures = countermeasures;
	}
	public String getConclusion() {
		return conclusion;
	}
	public void setConclusion(String conclusion) {
		this.conclusion = conclusion;
	}

}
