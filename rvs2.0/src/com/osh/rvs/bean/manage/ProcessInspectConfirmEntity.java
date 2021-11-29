package com.osh.rvs.bean.manage;

import java.io.Serializable;
import java.util.Date;

/**
 * 作业监察确认
 * 
 * @Description
 * @author liuxb
 * @date 2021-11-25 上午9:22:44
 */
public class ProcessInspectConfirmEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4397671939213359735L;

	// 作业监察确认KEY
	private String process_inspect_key;

	// 作业名
	private String process_name;

	// 经理印
	private String sign_manager_id;

	// 经理印日期
	private Date sign_manager_date;

	// 部长印
	private String sign_minister_id;

	// 部长印日期
	private Date sign_minister_date;

	// 经理工号
	private String manager_job_no;

	// 部长工号
	private String minister_job_no;

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

	public String getSign_manager_id() {
		return sign_manager_id;
	}

	public void setSign_manager_id(String sign_manager_id) {
		this.sign_manager_id = sign_manager_id;
	}

	public Date getSign_manager_date() {
		return sign_manager_date;
	}

	public void setSign_manager_date(Date sign_manager_date) {
		this.sign_manager_date = sign_manager_date;
	}

	public String getSign_minister_id() {
		return sign_minister_id;
	}

	public void setSign_minister_id(String sign_minister_id) {
		this.sign_minister_id = sign_minister_id;
	}

	public Date getSign_minister_date() {
		return sign_minister_date;
	}

	public void setSign_minister_date(Date sign_minister_date) {
		this.sign_minister_date = sign_minister_date;
	}

	public String getManager_job_no() {
		return manager_job_no;
	}

	public void setManager_job_no(String manager_job_no) {
		this.manager_job_no = manager_job_no;
	}

	public String getMinister_job_no() {
		return minister_job_no;
	}

	public void setMinister_job_no(String minister_job_no) {
		this.minister_job_no = minister_job_no;
	}

}