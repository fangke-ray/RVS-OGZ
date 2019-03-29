package com.osh.rvs.entity;

import java.io.Serializable;
import java.util.Date;

public class DeviceJigOrderEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2784418808048147071L;

	private String applicator_id;
	private Date scheduled_date;
	private Integer quantity;

	public String getApplicator_id() {
		return applicator_id;
	}

	public void setApplicator_id(String applicator_id) {
		this.applicator_id = applicator_id;
	}

	public Date getScheduled_date() {
		return scheduled_date;
	}

	public void setScheduled_date(Date scheduled_date) {
		this.scheduled_date = scheduled_date;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

}
