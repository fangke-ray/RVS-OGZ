package com.osh.rvs.bean.manage;

import java.io.Serializable;
import java.sql.Date;

public class ProcessInspectSearchEntity implements Serializable{

	private static final long serialVersionUID = -1;

	private String process_inspect_key;

	private String line_id;

	private String line_name;

	private String process_name;

	private String operator_id;

	private String operator_name;

	private String inspector_id;

	private String inspector_name;

	private Date inspect_date;

	private String inspect_date_from;

	private String inspect_date_to;

	private String model_id;

	private String model_name;

	private String serial_no;

	private Integer unqualified;

	private Integer file_type;

	private Integer perform_option;

	public String getProcess_inspect_key() {
		return process_inspect_key;
	}

	public void setProcess_inspect_key(String process_inspect_key) {
		this.process_inspect_key = process_inspect_key;
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

	public String getProcess_name() {
		return process_name;
	}

	public void setProcess_name(String process_name) {
		this.process_name = process_name;
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

	public Date getInspect_date() {
		return inspect_date;
	}

	public void setInspect_date(Date inspect_date) {
		this.inspect_date = inspect_date;
	}

	public String getInspect_date_from() {
		return inspect_date_from;
	}

	public void setInspect_date_from(String inspect_date_from) {
		this.inspect_date_from = inspect_date_from;
	}

	public String getInspect_date_to() {
		return inspect_date_to;
	}

	public void setInspect_date_to(String inspect_date_to) {
		this.inspect_date_to = inspect_date_to;
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

	public Integer getUnqualified() {
		return unqualified;
	}

	public void setUnqualified(Integer unqualified) {
		this.unqualified = unqualified;
	}

	public Integer getFile_type() {
		return file_type;
	}

	public void setFile_type(Integer file_type) {
		this.file_type = file_type;
	}

	public Integer getPerform_option() {
		return perform_option;
	}

	public void setPerform_option(Integer perform_option) {
		this.perform_option = perform_option;
	}

	public String getModel_id() {
		return model_id;
	}

	public void setModel_id(String model_id) {
		this.model_id = model_id;
	}


}
