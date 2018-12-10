package com.osh.rvs.bean;

import framework.huiqing.bean.BaseUserBean;
import framework.huiqing.bean.annotation.BeanField;

public class LoginData extends BaseUserBean {

	private static final long serialVersionUID = 4302708914726757298L;

	private String operator_id;
	private String line_id;
	private String line_name;
	private String section_id;
	private String section_name;
	@BeanField(title = "工号", name = "job_no", notNull=true, length = 8)
	private String job_no;
	private String position_id;
	private String position_name;
	private String process_code;
	private String last_link;

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
	public String getSection_id() {
		return section_id;
	}
	public void setSection_id(String section_id) {
		this.section_id = section_id;
	}
	public String getSection_name() {
		return section_name;
	}
	public void setSection_name(String section_name) {
		this.section_name = section_name;
	}
	public String getJob_no() {
		return job_no;
	}
	public void setJob_no(String job_no) {
		this.job_no = job_no;
	}
	public String getOperator_id() {
		return operator_id;
	}
	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}
	public String getPosition_id() {
		return position_id;
	}
	public String getPosition_name() {
		return position_name;
	}
	public void setPosition_name(String position_name) {
		this.position_name = position_name;
	}
	public String getProcess_code() {
		return process_code;
	}
	public void setProcess_code(String process_code) {
		this.process_code = process_code;
	}
	public void setPosition_id(String position_id) {
		this.position_id = position_id;
	}
	public String getLast_link() {
		return last_link;
	}
	public void setLast_link(String last_link) {
		this.last_link = last_link;
	}
}
