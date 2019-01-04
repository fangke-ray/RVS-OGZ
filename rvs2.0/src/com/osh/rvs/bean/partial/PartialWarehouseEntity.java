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

	/**
	 * 完成上架日期
	 */
	private Date finish_date_start;
	private Date finish_date_end;

	private Integer quantity;
	private Integer collation_quantity;
	private Integer match;

	private String code;
	private String partial_name;
	private String operator_name;

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

	public Date getFinish_date_start() {
		return finish_date_start;
	}

	public void setFinish_date_start(Date finish_date_start) {
		this.finish_date_start = finish_date_start;
	}

	public Date getFinish_date_end() {
		return finish_date_end;
	}

	public void setFinish_date_end(Date finish_date_end) {
		this.finish_date_end = finish_date_end;
	}

	public Integer getMatch() {
		return match;
	}

	public void setMatch(Integer match) {
		this.match = match;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getCollation_quantity() {
		return collation_quantity;
	}

	public void setCollation_quantity(Integer collation_quantity) {
		this.collation_quantity = collation_quantity;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPartial_name() {
		return partial_name;
	}

	public void setPartial_name(String partial_name) {
		this.partial_name = partial_name;
	}

	public String getOperator_name() {
		return operator_name;
	}

	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}

}
