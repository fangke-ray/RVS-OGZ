package com.osh.rvs.form.partial;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

/**
 * 零件入库单
 *
 * @author liuxb
 *
 */
public class PartialWarehouseForm extends ActionForm {

	/**
	 *
	 */
	private static final long serialVersionUID = 8231985194725137030L;

	/**
	 * KEY
	 */
	@BeanField(title = "KEY", name = "key", length = 11, notNull = true, primaryKey = true)
	private String key;

	/**
	 * 日期
	 */
	@BeanField(title = "日期", name = "warehouse_date", type = FieldType.Date, notNull = true)
	private String warehouse_date;

	/**
	 * DN 编号
	 */
	@BeanField(title = "DN 编号", name = "dn_no", type = FieldType.String, length = 16, notNull = true)
	private String dn_no;

	/**
	 * 入库进展
	 */
	@BeanField(title = "入库进展", name = "step", type = FieldType.Integer, length = 1, notNull = true)
	private String step;

	/**
	 * 操作者 ID
	 */
	@BeanField(title = "操作者 ID", name = "operator_id", length = 11)
	private String operator_id;

	@BeanField(title = "开始日期", name = "warehouse_date_start", type = FieldType.Date)
	private String warehouse_date_start;

	@BeanField(title = "结束日期", name = "warehouse_date_end", type = FieldType.Date)
	private String warehouse_date_end;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getWarehouse_date() {
		return warehouse_date;
	}

	public void setWarehouse_date(String warehouse_date) {
		this.warehouse_date = warehouse_date;
	}

	public String getDn_no() {
		return dn_no;
	}

	public void setDn_no(String dn_no) {
		this.dn_no = dn_no;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public String getWarehouse_date_start() {
		return warehouse_date_start;
	}

	public void setWarehouse_date_start(String warehouse_date_start) {
		this.warehouse_date_start = warehouse_date_start;
	}

	public String getWarehouse_date_end() {
		return warehouse_date_end;
	}

	public void setWarehouse_date_end(String warehouse_date_end) {
		this.warehouse_date_end = warehouse_date_end;
	}

}
