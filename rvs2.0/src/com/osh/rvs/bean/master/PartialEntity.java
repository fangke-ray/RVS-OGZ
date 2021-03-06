package com.osh.rvs.bean.master;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class PartialEntity implements Serializable {

	/**
	 * 零件管理
	 */
	private static final long serialVersionUID = 8890673720525630453L;

	/* 最新价格 */
	private BigDecimal new_price;
	/* 通货 */
	private Integer value_currency;
	/* 价格 */
	private BigDecimal price;
	/* 零件 ID */
	private String partial_id;
	/* 零件编码 */
	private String code;
	/* 零件名称 */
	private String name;
	/* 最后更新人 */
	private String updated_by;
	/* 最后更新时间 */
	private Timestamp updated_time;

	/* 有效截止日期 */
	private Integer is_exists;

	/**
	 * 规格种别
	 */
	private Integer spec_kind;

	public Integer getIs_exists() {
		return is_exists;
	}

	public void setIs_exists(Integer is_exists) {
		this.is_exists = is_exists;
	}

	public BigDecimal getNew_price() {
		return new_price;
	}

	public void setNew_price(BigDecimal new_price) {
		this.new_price = new_price;
	}

	public String getPartial_id() {
		return partial_id;
	}

	public void setPartial_id(String partial_id) {
		this.partial_id = partial_id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUpdated_by() {
		return updated_by;
	}

	public void setUpdated_by(String updated_by) {
		this.updated_by = updated_by;
	}

	public Timestamp getUpdated_time() {
		return updated_time;
	}

	public void setUpdated_time(Timestamp updated_time) {
		this.updated_time = updated_time;
	}

	public Integer getValue_currency() {
		return value_currency;
	}

	public void setValue_currency(Integer value_currency) {
		this.value_currency = value_currency;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getSpec_kind() {
		return spec_kind;
	}

	public void setSpec_kind(Integer spec_kind) {
		this.spec_kind = spec_kind;
	}

}
