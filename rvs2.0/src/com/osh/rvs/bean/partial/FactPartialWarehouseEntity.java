package com.osh.rvs.bean.partial;

import java.io.Serializable;

/**
 * 现品入库作业数
 *
 * @author liuxb
 *
 */
public class FactPartialWarehouseEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2698374280829717431L;

	/**
	 * KEY
	 */
	private String fact_pf_key;

	/**
	 * 规格种别
	 */
	private Integer spec_kind;

	/**
	 * 作业数量
	 */
	private Integer quantity;

	/**
	 * KEY
	 */
	private String key;

	/**
	 * 操作者 ID
	 */
	private String operator_id;

	/**
	 * 作业内容
	 */
	private Integer production_type;

	public String getFact_pf_key() {
		return fact_pf_key;
	}

	public void setFact_pf_key(String fact_pf_key) {
		this.fact_pf_key = fact_pf_key;
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

	public Integer getProduction_type() {
		return production_type;
	}

	public void setProduction_type(Integer production_type) {
		this.production_type = production_type;
	}

}
