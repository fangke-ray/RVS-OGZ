package com.osh.rvs.bean.partial;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 零件入库明细
 *
 * @author liuxb
 *
 */
public class PartialWarehouseDetailEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1046986262646618507L;

	/**
	 * KEY
	 */
	private String key;

	/**
	 * 零件 ID
	 */
	private String partial_id;

	/**
	 * 数量
	 */
	private Integer quantity;

	/**
	 * 核对数量
	 */
	private Integer collation_quantity;

	/**
	 * 核对作业 KEY
	 */
	private String fact_pf_key;

	/**
	 * 零件名称
	 */
	private String partial_name;

	/**
	 * 零件编码
	 */
	private String code;

	/**
	 * 规格种别
	 */
	private Integer spec_kind;

	/**
	 * 作业内容
	 */
	private Integer production_type;

	/**
	 * 分装数量
	 */
	private Integer split_quantity;

	/**
	 * 分装总数
	 */
	private Integer total_split_quantity;

	/**
	 * 上架
	 */
	private BigDecimal on_shelf;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getPartial_id() {
		return partial_id;
	}

	public void setPartial_id(String partial_id) {
		this.partial_id = partial_id;
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

	public String getPartial_name() {
		return partial_name;
	}

	public void setPartial_name(String partial_name) {
		this.partial_name = partial_name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getSpec_kind() {
		return spec_kind;
	}

	public void setSpec_kind(Integer spec_kind) {
		this.spec_kind = spec_kind;
	}

	public String getFact_pf_key() {
		return fact_pf_key;
	}

	public void setFact_pf_key(String fact_pf_key) {
		this.fact_pf_key = fact_pf_key;
	}

	public Integer getProduction_type() {
		return production_type;
	}

	public void setProduction_type(Integer production_type) {
		this.production_type = production_type;
	}

	public Integer getSplit_quantity() {
		return split_quantity;
	}

	public void setSplit_quantity(Integer split_quantity) {
		this.split_quantity = split_quantity;
	}

	public Integer getTotal_split_quantity() {
		return total_split_quantity;
	}

	public void setTotal_split_quantity(Integer total_split_quantity) {
		this.total_split_quantity = total_split_quantity;
	}

	public BigDecimal getOn_shelf() {
		return on_shelf;
	}

	public void setOn_shelf(BigDecimal on_shelf) {
		this.on_shelf = on_shelf;
	}

}
