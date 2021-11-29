package com.osh.rvs.form.manage;

import java.io.Serializable;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

/**
 * 作业监察确认
 * 
 * @Description
 * @author liuxb
 * @date 2021-11-25 上午10:14:58
 */
public class ProcessInspectConfirmForm extends ActionForm implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3530026741070849178L;

	@BeanField(name = "process_inspect_key", title = "作业监察确认KEY", type = FieldType.String, primaryKey = true, length = 11)
	private String process_inspect_key;

	@BeanField(name = "process_name", title = "作业名", type = FieldType.String, primaryKey = true, length = 32)
	private String process_name;

	@BeanField(name = "sign_manager_id", title = "经理印", type = FieldType.String, length = 11)
	private String sign_manager_id;

	@BeanField(name = "sign_manager_date", title = "经理印日期", type = FieldType.Date)
	private String sign_manager_date;

	@BeanField(name = "sign_minister_id", title = "部长印", type = FieldType.String, length = 11)
	private String sign_minister_id;

	@BeanField(name = "sign_minister_date", title = "部长印日期", type = FieldType.Date)
	private String sign_minister_date;

	@BeanField(title = "经理工号", name = "manager_job_no", type = FieldType.String, length = 8)
	private String manager_job_no;

	@BeanField(title = "部长工号", name = "minister_job_no", type = FieldType.String, length = 8)
	private String minister_job_no;
	
	// 盖章标记("1":"经理印","2":"部长印")
	private String process_flg;

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

	public String getSign_manager_date() {
		return sign_manager_date;
	}

	public void setSign_manager_date(String sign_manager_date) {
		this.sign_manager_date = sign_manager_date;
	}

	public String getSign_minister_id() {
		return sign_minister_id;
	}

	public void setSign_minister_id(String sign_minister_id) {
		this.sign_minister_id = sign_minister_id;
	}

	public String getSign_minister_date() {
		return sign_minister_date;
	}

	public void setSign_minister_date(String sign_minister_date) {
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

	public String getProcess_flg() {
		return process_flg;
	}

	public void setProcess_flg(String process_flg) {
		this.process_flg = process_flg;
	}
	
}