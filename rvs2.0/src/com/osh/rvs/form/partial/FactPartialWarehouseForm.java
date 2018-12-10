package com.osh.rvs.form.partial;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

/**
 * 现品入库作业数
 *
 * @author liuxb
 *
 */
public class FactPartialWarehouseForm extends ActionForm {

	/**
	 *
	 */
	private static final long serialVersionUID = 3734706389406616400L;
	/**
	 * KEY
	 */
	@BeanField(title = "KEY", name = "fact_pf_key", length = 11, notNull = true, primaryKey = true)
	private String fact_pf_key;

	/**
	 * 规格种别
	 */
	@BeanField(title = "规格种别", name = "spec_kind", type = FieldType.Integer, length = 1, notNull = true, primaryKey = true)
	private String spec_kind;

	/**
	 * 作业数量
	 */
	@BeanField(title = "作业数量", name = "quantity", type = FieldType.Integer, length = 5, notNull = true)
	private String quantity;

	/**
	 * KEY
	 */
	@BeanField(title = "KEY", name = "key", length = 11)
	private String key;

	/**
	 * 操作者 ID
	 */
	@BeanField(title = "操作者 ID", name = "operator_id", length = 11)
	private String operator_id;

	/**
	 * 作业内容
	 */
	@BeanField(title = "作业内容", name = "production_type", type = FieldType.Integer, length = 2)
	private String production_type;

	/**
	 * 分装总数
	 */
	private String total_split_quantity;

	/**
	 * 分装数量
	 */
	private String split_quantity;

	public String getFact_pf_key() {
		return fact_pf_key;
	}

	public void setFact_pf_key(String fact_pf_key) {
		this.fact_pf_key = fact_pf_key;
	}

	public String getSpec_kind() {
		return spec_kind;
	}

	public void setSpec_kind(String spec_kind) {
		this.spec_kind = spec_kind;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public String getTotal_split_quantity() {
		return total_split_quantity;
	}

	public void setTotal_split_quantity(String total_split_quantity) {
		this.total_split_quantity = total_split_quantity;
	}

	public String getSplit_quantity() {
		return split_quantity;
	}

	public void setSplit_quantity(String split_quantity) {
		this.split_quantity = split_quantity;
	}

	public String getProduction_type() {
		return production_type;
	}

	public void setProduction_type(String production_type) {
		this.production_type = production_type;
	}

}
