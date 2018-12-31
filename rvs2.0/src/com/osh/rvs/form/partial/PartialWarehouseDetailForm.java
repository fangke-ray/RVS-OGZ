package com.osh.rvs.form.partial;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.CodeListUtils;

/**
 * 零件入库明细
 *
 * @author liuxb
 *
 */
public class PartialWarehouseDetailForm extends ActionForm {

	/**
	 *
	 */
	private static final long serialVersionUID = -8612709913848931983L;

	/**
	 * KEY
	 */
	@BeanField(title = "KEY", name = "key", length = 11, notNull = true, primaryKey = true)
	private String key;

	/**
	 * 零件 ID
	 */
	@BeanField(title = "零件 ID", name = "partial_id", length = 11, notNull = true, primaryKey = true)
	private String partial_id;

	/**
	 * 数量
	 */
	@BeanField(title = "数量", name = "quantity", type = FieldType.Integer, length = 5, notNull = true)
	private String quantity;

	/**
	 * 核对作业 KEY
	 */
	@BeanField(title = "核对作业 KEY", name = "fact_pf_key", length = 11)
	private String fact_pf_key;

	/**
	 * 核对数量
	 */
	@BeanField(title = "核对数量", name = "collation_quantity", type = FieldType.Integer, length = 5, notNull = true)
	private String collation_quantity;

	/**
	 * 零件名称
	 */
	@BeanField(title = "零件名称", name = "partial_name")
	private String partial_name;

	/**
	 * 零件编码
	 */
	@BeanField(title = "零件编码", name = "code")
	private String code;

	/**
	 * 规格种别
	 */
	@BeanField(title = "规格种别", name = "spec_kind", type = FieldType.Integer, length = 1)
	private String spec_kind;

	/**
	 * 规格种别名称
	 *
	 */
	@BeanField(title = "规格种别名称", name = "spec_kind_name")
	private String spec_kind_name;

	/**
	 * 作业内容
	 */
	@BeanField(title = "作业内容", name = "production_type", type = FieldType.Integer, length = 2)
	private String production_type;

	/**
	 * 分装数量
	 */
	@BeanField(title = "分装数量", name = "split_quantity", type = FieldType.Integer, length = 3)
	private String split_quantity;

	/**
	 * 分装总数
	 */
	@BeanField(title = "分装总数", name = "total_split_quantity", type = FieldType.Integer)
	private String total_split_quantity;

	/**
	 * 上架
	 */
	@BeanField(title = "上架", name = "on_shelf", type = FieldType.Double)
	private String on_shelf;

	private String flg;

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

	public String getSpec_kind() {
		return spec_kind;
	}

	public void setSpec_kind(String spec_kind) {
		this.spec_kind = spec_kind;
	}

	public String getSpec_kind_name() {
		if (spec_kind != null) {
			return CodeListUtils.getValue("partial_spec_kind", spec_kind);
		}

		return spec_kind_name;
	}

	public void setSpec_kind_name(String spec_kind_name) {
		this.spec_kind_name = spec_kind_name;
	}

	public String getFact_pf_key() {
		return fact_pf_key;
	}

	public void setFact_pf_key(String fact_pf_key) {
		this.fact_pf_key = fact_pf_key;
	}

	public String getProduction_type() {
		return production_type;
	}

	public void setProduction_type(String production_type) {
		this.production_type = production_type;
	}

	public String getFlg() {
		return flg;
	}

	public void setFlg(String flg) {
		this.flg = flg;
	}

	public String getSplit_quantity() {
		return split_quantity;
	}

	public void setSplit_quantity(String split_quantity) {
		this.split_quantity = split_quantity;
	}

	public String getTotal_split_quantity() {
		return total_split_quantity;
	}

	public void setTotal_split_quantity(String total_split_quantity) {
		this.total_split_quantity = total_split_quantity;
	}

	public String getOn_shelf() {
		return on_shelf;
	}

	public void setOn_shelf(String on_shelf) {
		this.on_shelf = on_shelf;
	}

}
