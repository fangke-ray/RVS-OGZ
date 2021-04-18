package com.osh.rvs.bean.manage;

import java.io.Serializable;
import java.util.Date;

public class ProcessInspectSummaryEntity implements Serializable{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7273655853183112197L;

	private String process_inspect_key;

	// 作业名
	private String process_name;

	// 项目行数
	private Integer line_seq;

	// 监查项
	private String inspect_item;

	// 本行检查
	private Integer need_check;

	// 监查内容
	private String inspect_content;

	// rowspan
	private Integer rowspan;

	// 不合格内容
	private String unqualified_content;

	// 不合格内容
	private String unqualified_treatment;

	// 不合格处理完成日
	private Date unqualified_treat_date;

	// 工程 ID
	private String line_id;

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
	public Date getUnqualified_treat_date() {
		return unqualified_treat_date;
	}
	public void setUnqualified_treat_date(Date unqualified_treat_date) {
		this.unqualified_treat_date = unqualified_treat_date;
	}
	public String getLine_id() {
		return line_id;
	}
	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}
}
