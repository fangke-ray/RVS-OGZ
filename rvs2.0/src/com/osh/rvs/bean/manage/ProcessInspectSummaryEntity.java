package com.osh.rvs.bean.manage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class ProcessInspectSummaryEntity implements Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4719031016541165805L;
	private String process_inspect_key;

	private Integer file_type;

	private Integer perform_option;
	// 归档日期
	private Date filing_date;
	// 工程
	private String line_id;
	// 工程名
	private String line_name;
	// 操作者 ID
	private String operator_id;
	// 操作者
	private String operator_name;
	// 监察者
	private String inspector_id;
	// 监察者
	private String inspector_name;
	// 监察日
	private Date inspect_date;
	// 监察日起
	private Date inspect_date_start;
	// 监察日止
	private Date inspect_date_end;
	// 型号 ID
	private String model_id;
	// 型号名
	private String model_name;
	// 机身号
	private String serial_no;
	// 作业时间
	private BigDecimal process_seconds;
	// 标准时间
	private BigDecimal standard_seconds;

	// 监查情况
	private String situation;
	// 实施对策
	private String countermeasures;
	// 结果
	private String conclusion;
	public String getProcess_inspect_key() {
		return process_inspect_key;
	}
	public void setProcess_inspect_key(String process_inspect_key) {
		this.process_inspect_key = process_inspect_key;
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
	public Date getFiling_date() {
		return filing_date;
	}
	public void setFiling_date(Date filing_date) {
		this.filing_date = filing_date;
	}
	public String getLine_id() {
		return line_id;
	}
	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}
	public String getOperator_id() {
		return operator_id;
	}
	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}
	public String getInspector_id() {
		return inspector_id;
	}
	public void setInspector_id(String inspector_id) {
		this.inspector_id = inspector_id;
	}
	public Date getInspect_date() {
		return inspect_date;
	}
	public void setInspect_date(Date inspect_date) {
		this.inspect_date = inspect_date;
	}
	public Date getInspect_date_start() {
		return inspect_date_start;
	}
	public void setInspect_date_start(Date inspect_date_start) {
		this.inspect_date_start = inspect_date_start;
	}
	public Date getInspect_date_end() {
		return inspect_date_end;
	}
	public void setInspect_date_end(Date inspect_date_end) {
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
	public BigDecimal getProcess_seconds() {
		return process_seconds;
	}
	public void setProcess_seconds(BigDecimal process_seconds) {
		this.process_seconds = process_seconds;
	}
	public BigDecimal getStandard_seconds() {
		return standard_seconds;
	}
	public void setStandard_seconds(BigDecimal standard_seconds) {
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

	public String getLine_name() {
		return line_name;
	}

	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}

	public String getOperator_name() {
		return operator_name;
	}

	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}

	public String getInspector_name() {
		return inspector_name;
	}

	public void setInspector_name(String inspector_name) {
		this.inspector_name = inspector_name;
	}
}
