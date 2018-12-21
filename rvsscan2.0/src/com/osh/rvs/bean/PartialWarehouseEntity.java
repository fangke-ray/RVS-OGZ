package com.osh.rvs.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 *
 * @author liuxb
 *
 */
public class PartialWarehouseEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 29995007799752060L;

	/**
	 * 作业内容
	 */
	private Integer production_type;

	/**
	 * 作业内容名称
	 */
	private String production_type_name;

	private String material_id;

	/**
	 * 操作者 ID
	 */
	private String operator_id;
	/**
	 * 操作者名称
	 */
	private String operator_name;

	private Integer step;

	/**
	 * DN 编号
	 */
	private String dn_no;

	/**
	 * 零件入库单 KEY
	 */
	private String key;

	/**
	 * 规格种别
	 */
	private Integer spec_kind;

	/**
	 * 数量
	 */
	private Integer quantity;

	/**
	 * 核对数量
	 */
	private Integer collation_quantity;

	/**
	 * 收货
	 */
	private Integer recept;

	/**
	 * 核对
	 */
	private Integer collation;

	/**
	 * 核对+上架
	 */
	private Integer collation_shelf;
	/**
	 * 上架
	 */
	private Integer on_shelf;

	/**
	 * 分装
	 */
	private Integer unpack;

	/**
	 * 标准时间
	 */
	private Integer standardTime;

	/**
	 * 经过时间
	 */
	private Integer spentMins;

	/**
	 * NS 出库
	 */
	private Integer ns_outline;

	/**
	 * 分解出库
	 */
	private Integer dec_outline;

	private Integer isNow;

	private Integer accept_percent;

	private Integer collation_shelf_percent;

	private Integer collation_percent;

	private Integer unpack_percent;

	private Integer on_shelf_percent;

	private Integer ns_outline_percent;

	private Integer dec_outline_percent;

	private BigDecimal total_percent;

	private String omr_notifi_no;
	private String process_code;
	private Date action_time;

	private String content;

	public Integer getProduction_type() {
		return production_type;
	}

	public void setProduction_type(Integer production_type) {
		this.production_type = production_type;
	}

	public String getProduction_type_name() {
		return production_type_name;
	}

	public void setProduction_type_name(String production_type_name) {
		this.production_type_name = production_type_name;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public String getOperator_name() {
		return operator_name;
	}

	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}

	public String getDn_no() {
		return dn_no;
	}

	public void setDn_no(String dn_no) {
		this.dn_no = dn_no;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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

	public Integer getCollation_quantity() {
		return collation_quantity;
	}

	public void setCollation_quantity(Integer collation_quantity) {
		this.collation_quantity = collation_quantity;
	}

	public Integer getRecept() {
		return recept;
	}

	public void setRecept(Integer recept) {
		this.recept = recept;
	}

	public Integer getCollation() {
		return collation;
	}

	public void setCollation(Integer collation) {
		this.collation = collation;
	}

	public Integer getOn_shelf() {
		return on_shelf;
	}

	public void setOn_shelf(Integer on_shelf) {
		this.on_shelf = on_shelf;
	}

	public Integer getUnpack() {
		return unpack;
	}

	public void setUnpack(Integer unpack) {
		this.unpack = unpack;
	}

	public Integer getStandardTime() {
		return standardTime;
	}

	public void setStandardTime(Integer standardTime) {
		this.standardTime = standardTime;
	}

	public Integer getSpentMins() {
		return spentMins;
	}

	public void setSpentMins(Integer spentMins) {
		this.spentMins = spentMins;
	}

	public Integer getCollation_shelf() {
		return collation_shelf;
	}

	public void setCollation_shelf(Integer collation_shelf) {
		this.collation_shelf = collation_shelf;
	}

	public Integer getNs_outline() {
		return ns_outline;
	}

	public void setNs_outline(Integer ns_outline) {
		this.ns_outline = ns_outline;
	}

	public Integer getDec_outline() {
		return dec_outline;
	}

	public void setDec_outline(Integer dec_outline) {
		this.dec_outline = dec_outline;
	}

	public Integer getIsNow() {
		return isNow;
	}

	public void setIsNow(Integer isNow) {
		this.isNow = isNow;
	}

	public Integer getAccept_percent() {
		return accept_percent;
	}

	public void setAccept_percent(Integer accept_percent) {
		this.accept_percent = accept_percent;
	}

	public Integer getCollation_shelf_percent() {
		return collation_shelf_percent;
	}

	public void setCollation_shelf_percent(Integer collation_shelf_percent) {
		this.collation_shelf_percent = collation_shelf_percent;
	}

	public Integer getCollation_percent() {
		return collation_percent;
	}

	public void setCollation_percent(Integer collation_percent) {
		this.collation_percent = collation_percent;
	}

	public Integer getUnpack_percent() {
		return unpack_percent;
	}

	public void setUnpack_percent(Integer unpack_percent) {
		this.unpack_percent = unpack_percent;
	}

	public Integer getOn_shelf_percent() {
		return on_shelf_percent;
	}

	public void setOn_shelf_percent(Integer on_shelf_percent) {
		this.on_shelf_percent = on_shelf_percent;
	}

	public Integer getNs_outline_percent() {
		return ns_outline_percent;
	}

	public void setNs_outline_percent(Integer ns_outline_percent) {
		this.ns_outline_percent = ns_outline_percent;
	}

	public Integer getDec_outline_percent() {
		return dec_outline_percent;
	}

	public void setDec_outline_percent(Integer dec_outline_percent) {
		this.dec_outline_percent = dec_outline_percent;
	}

	public BigDecimal getTotal_percent() {
		return total_percent;
	}

	public void setTotal_percent(BigDecimal total_percent) {
		this.total_percent = total_percent;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	public String getMaterial_id() {
		return material_id;
	}

	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}

	public String getOmr_notifi_no() {
		return omr_notifi_no;
	}

	public void setOmr_notifi_no(String omr_notifi_no) {
		this.omr_notifi_no = omr_notifi_no;
	}

	public String getProcess_code() {
		return process_code;
	}

	public void setProcess_code(String process_code) {
		this.process_code = process_code;
	}

	public Date getAction_time() {
		return action_time;
	}

	public void setAction_time(Date action_time) {
		this.action_time = action_time;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
