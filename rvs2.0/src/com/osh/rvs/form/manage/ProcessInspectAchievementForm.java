package com.osh.rvs.form.manage;

import org.apache.struts.action.ActionForm;

public class ProcessInspectAchievementForm extends ActionForm {

	private String process_inspect_key;

	private String process_name;

	private Integer line_seq;

	private String inspect_item;

	private Integer need_check;

	private String inspect_content;

	private Integer rowspan;

	private String unqualified_content;

	private String unqualified_treatment;

	private String unqualified_treat_date;

	public String getProcess_inspect_key() {
		return process_inspect_key;
	}

	public void setProcess_inspect_key(String process_inspect_key) {
		this.process_inspect_key = process_inspect_key;
	}

	public String getProcess_name() {
		return process_name;
	}

	public void setProcess_name(String process_name) {
		this.process_name = process_name;
	}

	public Integer getLine_seq() {
		return line_seq;
	}

	public void setLine_seq(Integer line_seq) {
		this.line_seq = line_seq;
	}

	public String getInspect_item() {
		return inspect_item;
	}

	public void setInspect_item(String inspect_item) {
		this.inspect_item = inspect_item;
	}

	public Integer getNeed_check() {
		return need_check;
	}

	public void setNeed_check(Integer need_check) {
		this.need_check = need_check;
	}

	public String getInspect_content() {
		return inspect_content;
	}

	public void setInspect_content(String inspect_content) {
		this.inspect_content = inspect_content;
	}

	public Integer getRowspan() {
		return rowspan;
	}

	public void setRowspan(Integer rowspan) {
		this.rowspan = rowspan;
	}

	public String getUnqualified_content() {
		return unqualified_content;
	}

	public void setUnqualified_content(String unqualified_content) {
		this.unqualified_content = unqualified_content;
	}

	public String getUnqualified_treatment() {
		return unqualified_treatment;
	}

	public void setUnqualified_treatment(String unqualified_treatment) {
		this.unqualified_treatment = unqualified_treatment;
	}

	public String getUnqualified_treat_date() {
		return unqualified_treat_date;
	}

	public void setUnqualified_treat_date(String unqualified_treat_date) {
		this.unqualified_treat_date = unqualified_treat_date;
	}

}
