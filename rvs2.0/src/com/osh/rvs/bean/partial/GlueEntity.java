package com.osh.rvs.bean.partial;

import java.io.Serializable;
import java.util.Date;

/**
 * @Title: GlueEntity.java
 * @Package com.osh.rvs.bean.partial
 * @Description: 胶水
 * @author liuxb
 * @date 2017-12-15 下午2:00:04
 */
public class GlueEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4394468517283288615L;

	// 胶水 ID
	private String glue_id;

	// 乙材种类 ID
	private String partial_id;

	// 识别号
	private Integer fmg_in;

	// 保质期
	private Date expiration;

	// 开封日期
	private Date unseal_date;

	// 弃用日期
	private Date abandon_date;

	// 弃用操作者
	private Integer abandon_operator_id;

	// 位置
	private Integer location;

	// 领用课室
	private String section_id;

	// 领用工程
	private String line_id;

	// 批号
	private String lot_no;

	public String getGlue_id() {
		return glue_id;
	}

	public void setGlue_id(String glue_id) {
		this.glue_id = glue_id;
	}

	public String getPartial_id() {
		return partial_id;
	}

	public void setPartial_id(String partial_id) {
		this.partial_id = partial_id;
	}

	public Integer getFmg_in() {
		return fmg_in;
	}

	public void setFmg_in(Integer fmg_in) {
		this.fmg_in = fmg_in;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public Date getUnseal_date() {
		return unseal_date;
	}

	public void setUnseal_date(Date unseal_date) {
		this.unseal_date = unseal_date;
	}

	public Date getAbandon_date() {
		return abandon_date;
	}

	public void setAbandon_date(Date abandon_date) {
		this.abandon_date = abandon_date;
	}

	public Integer getAbandon_operator_id() {
		return abandon_operator_id;
	}

	public void setAbandon_operator_id(Integer abandon_operator_id) {
		this.abandon_operator_id = abandon_operator_id;
	}

	public Integer getLocation() {
		return location;
	}

	public void setLocation(Integer location) {
		this.location = location;
	}

	public String getSection_id() {
		return section_id;
	}

	public void setSection_id(String section_id) {
		this.section_id = section_id;
	}

	public String getLine_id() {
		return line_id;
	}

	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}

	public String getLot_no() {
		return lot_no;
	}

	public void setLot_no(String lot_no) {
		this.lot_no = lot_no;
	}

}
