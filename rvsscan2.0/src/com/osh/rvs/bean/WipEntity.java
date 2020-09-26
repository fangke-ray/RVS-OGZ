package com.osh.rvs.bean;

import java.io.Serializable;

public class WipEntity implements Serializable {

	private static final long serialVersionUID = -7361517716705531343L;

	private String sorc_no;
	private String model_name;
	private String serial_no;
	private String wip_location;
	private String wip_overceed;

	private String bound_out_ocm;
	private Integer execute;
	private Integer kind;

	public String getSorc_no() {
		return sorc_no;
	}
	public void setSorc_no(String sorc_no) {
		this.sorc_no = sorc_no;
	}
	public String getModel_name() {
		return model_name;
	}
	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}
	public String getSerial_no() {
		return serial_no;
	}
	public void setSerial_no(String serial_no) {
		this.serial_no = serial_no;
	}
	public String getWip_location() {
		return wip_location;
	}
	public void setWip_location(String wip_location) {
		this.wip_location = wip_location;
	}
	public String getWip_overceed() {
		return wip_overceed;
	}
	public void setWip_overceed(String wip_overceed) {
		this.wip_overceed = wip_overceed;
	}
	public String getBound_out_ocm() {
		return bound_out_ocm;
	}
	public void setBound_out_ocm(String bound_out_ocm) {
		this.bound_out_ocm = bound_out_ocm;
	}
	public Integer getExecute() {
		return execute;
	}
	public void setExecute(Integer execute) {
		this.execute = execute;
	}
	public Integer getKind() {
		return kind;
	}
	public void setKind(Integer kind) {
		this.kind = kind;
	}

}
