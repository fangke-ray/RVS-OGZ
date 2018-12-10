package com.osh.rvs.form.partial;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;

/**
 * 现品作业备注
 *
 * @author liuxb
 *
 */
public class FactProductionCommentForm extends ActionForm {

	/**
	 *
	 */
	private static final long serialVersionUID = -5660386032300600640L;

	/**
	 * KEY
	 */
	@BeanField(title = "KEY", name = "fact_pf_key", length = 11, primaryKey = true, notNull = true)
	private String fact_pf_key;

	/**
	 * 备注内容
	 */
	@BeanField(title = "备注内容", name = "comment", length = 250)
	private String comment;

	public String getFact_pf_key() {
		return fact_pf_key;
	}

	public void setFact_pf_key(String fact_pf_key) {
		this.fact_pf_key = fact_pf_key;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
