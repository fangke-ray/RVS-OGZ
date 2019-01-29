package com.osh.rvs.entity;

import java.io.Serializable;
import java.util.Date;

public class PartialWarehouseEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6273759415725809048L;

	private String key;
	private Date action_time;
	private Date finish_time;
	private Date start_date;
	private String operator_name;
	private String operator_id;
	private String job_no;
	private String dn_no;
	private Integer production_type;
	private Integer spec_kind;
	private Integer quantity;
	private String fact_pf_key;
	private String warehouse_no;
	private Integer spendTime;
	private Integer overtime_flg;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getAction_time() {
		return action_time;
	}

	public void setAction_time(Date action_time) {
		this.action_time = action_time;
	}

	public Date getFinish_time() {
		return finish_time;
	}

	public void setFinish_time(Date finish_time) {
		this.finish_time = finish_time;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public String getOperator_name() {
		return operator_name;
	}

	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public String getJob_no() {
		return job_no;
	}

	public void setJob_no(String job_no) {
		this.job_no = job_no;
	}

	public String getDn_no() {
		return dn_no;
	}

	public void setDn_no(String dn_no) {
		this.dn_no = dn_no;
	}

	public Integer getProduction_type() {
		return production_type;
	}

	public void setProduction_type(Integer production_type) {
		this.production_type = production_type;
	}

	public Integer getSpec_kind() {
		return spec_kind;
	}

	public void setSpec_kind(Integer spec_kind) {
		this.spec_kind = spec_kind;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getFact_pf_key() {
		return fact_pf_key;
	}

	public void setFact_pf_key(String fact_pf_key) {
		this.fact_pf_key = fact_pf_key;
	}

	public String getWarehouse_no() {
		return warehouse_no;
	}

	public void setWarehouse_no(String warehouse_no) {
		this.warehouse_no = warehouse_no;
	}

	public Integer getSpendTime() {
		return spendTime;
	}

	public void setSpendTime(Integer spendTime) {
		this.spendTime = spendTime;
	}

	public Integer getOvertime_flg() {
		return overtime_flg;
	}

	public void setOvertime_flg(Integer overtime_flg) {
		this.overtime_flg = overtime_flg;
	}

}
