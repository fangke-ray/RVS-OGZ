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
	 * 零件入库单号
	 */
	@BeanField(title = "零件入库单号", name = "warehouse_no", length = 10, notNull = true)
	private String warehouse_no;

	/**
	 * 入库进展
	 */
	@BeanField(title = "入库进展", name = "step", type = FieldType.Integer, length = 1, notNull = true)
	private String step;

	/**
	 * 日期
	 */
	@BeanField(title = "日期", name = "warehouse_date", type = FieldType.Date)
	private String warehouse_date;

	/**
	 * DN 编号
	 */
	@BeanField(title = "DN 编号", name = "dn_no", type = FieldType.String, length = 16)
	private String dn_no;

	/**
	 * 操作者 ID
	 */
	@BeanField(title = "操作者 ID", name = "operator_id", length = 11)
	private String operator_id;

	@BeanField(title = "入库单日期开始", name = "warehouse_date_start", type = FieldType.Date)
	private String warehouse_date_start;

	@BeanField(title = "入库单日期结束", name = "warehouse_date_end", type = FieldType.Date)
	private String warehouse_date_end;

	@BeanField(title = "完成上架日期开始", name = "finish_date_start", type = FieldType.Date)
	private String finish_date_start;

	@BeanField(title = "完成上架日期结束", name = "finish_date_end", type = FieldType.Date)
	private String finish_date_end;

	/**
	 * 数量
	 */
	@BeanField(title = "数量", name = "quantity", type = FieldType.Integer)
	private String quantity;

	/**
	 * 核对数量
	 */
	@BeanField(title = "核对数量", name = "collation_quantity", type = FieldType.Integer)
	private String collation_quantity;

	@BeanField(title = "核对一致", name = "match", type = FieldType.Integer)
	private String match;

	@BeanField(title = "作业内容", name = "production_type", type = FieldType.Integer, length = 2)
	private String production_type;

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

	public String getFinish_date_start() {
		return finish_date_start;
	}

	public void setFinish_date_start(String finish_date_start) {
		this.finish_date_start = finish_date_start;
	}

	public String getFinish_date_end() {
		return finish_date_end;
	}

	public void setFinish_date_end(String finish_date_end) {
		this.finish_date_end = finish_date_end;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getCollation_quantity() {
		return collation_quantity;
	}

	public void setCollation_quantity(String collation_quantity) {
		this.collation_quantity = collation_quantity;
	}

	public String getProduction_type() {
		return production_type;
	}

	public void setProduction_type(String production_type) {
		this.production_type = production_type;
	}

	public String getWarehouse_no() {
		return warehouse_no;
	}

	public void setWarehouse_no(String warehouse_no) {
		this.warehouse_no = warehouse_no;
	}

}
