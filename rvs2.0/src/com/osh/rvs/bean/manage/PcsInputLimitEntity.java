package com.osh.rvs.bean.manage;

import java.io.Serializable;
import java.math.BigDecimal;

public class PcsInputLimitEntity implements Serializable {

	private static final long serialVersionUID = 3196791584515042173L;

	private String type_code;
	private String packed_file_name;
	private String tag_code;

	private BigDecimal lower_limit;
	private BigDecimal upper_limit;
	private Boolean allow_pass;

	private Integer cnt;

	public String getType_code() {
		return type_code;
	}
	public void setType_code(String type_code) {
		this.type_code = type_code;
	}
	public String getPacked_file_name() {
		return packed_file_name;
	}
	public void setPacked_file_name(String packed_file_name) {
		this.packed_file_name = packed_file_name;
	}
	public String getTag_code() {
		return tag_code;
	}
	public void setTag_code(String tag_code) {
		this.tag_code = tag_code;
	}
	public BigDecimal getLower_limit() {
		return lower_limit;
	}
	public void setLower_limit(BigDecimal lower_limit) {
		this.lower_limit = lower_limit;
	}
	public BigDecimal getUpper_limit() {
		return upper_limit;
	}
	public void setUpper_limit(BigDecimal upper_limit) {
		this.upper_limit = upper_limit;
	}
	public Boolean getAllow_pass() {
		return allow_pass;
	}
	public void setAllow_pass(Boolean allow_pass) {
		this.allow_pass = allow_pass;
	}
	public Integer getCnt() {
		return cnt;
	}
	public void setCnt(Integer cnt) {
		this.cnt = cnt;
	}

}
