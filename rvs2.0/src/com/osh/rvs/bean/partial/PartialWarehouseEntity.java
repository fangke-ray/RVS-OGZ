package com.osh.rvs.bean.partial;

import java.io.Serializable;
import java.util.Date;

/**
 * 零件入库单
 *
 * @author liuxb
 *
 */
public class PartialWarehouseEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3155010979714435445L;

	/**
	 * KEY
	 */
	private String key;

	/**
	 * 日期
	 */
	private Date warehouse_date;

	/**
	 * DN 编号
	 */
	private String dn_no;

	/**
	 * 入库进展
	 */
	private Integer step;

	/**
	 * 操作者 ID
	 */
	private String operator_id;

	private Date warehouse_date_start;

	private Date warehouse_date_end;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getWarehouse_date() {
		return warehouse_date;
	}

	public void setWarehouse_date(Date warehouse_date) {
		this.warehouse_date = warehouse_date;
	}

	public String getDn_no() {
		return dn_no;
	}

	public void setDn_no(String dn_no) {
		this.dn_no = dn_no;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public Date getWarehouse_date_start() {
		return warehouse_date_start;
	}

	public void setWarehouse_date_start(Date warehouse_date_start) {
		this.warehouse_date_start = warehouse_date_start;
	}

	public Date getWarehouse_date_end() {
		return warehouse_date_end;
	}

	public void setWarehouse_date_end(Date warehouse_date_end) {
		this.warehouse_date_end = warehouse_date_end;
	}

}
