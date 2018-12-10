package com.osh.rvs.bean.partial;

import java.io.Serializable;

/**
 * 现品作业备注
 *
 * @author liuxb
 *
 */
public class FactProductionCommentEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2146110904373929261L;

	/**
	 * KEY
	 */
	private String fact_pf_key;

	/**
	 * 备注内容
	 */
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
