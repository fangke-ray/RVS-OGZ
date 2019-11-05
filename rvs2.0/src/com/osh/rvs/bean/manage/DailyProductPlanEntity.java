package com.osh.rvs.bean.manage;

import java.io.Serializable;
import java.util.Date;

public class DailyProductPlanEntity implements Serializable{

	private static final long serialVersionUID = -7913620955362179253L;

	private Date plan_date;
	private String model_id;
	private String model_name;
	private Integer seq;
	private Integer quantity;
	public Date getPlan_date() {
		return plan_date;
	}
	public void setPlan_date(Date plan_date) {
		this.plan_date = plan_date;
	}
	public String getModel_id() {
		return model_id;
	}
	public void setModel_id(String model_id) {
		this.model_id = model_id;
	}
	public Integer getSeq() {
		return seq;
	}
	public void setSeq(Integer seq) {
		this.seq = seq;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getModel_name() {
		return model_name;
	}
	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}
}
