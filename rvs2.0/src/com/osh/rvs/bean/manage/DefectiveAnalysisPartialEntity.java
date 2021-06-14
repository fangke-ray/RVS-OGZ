package com.osh.rvs.bean.manage;

import java.io.Serializable;
import java.util.Date;

public class DefectiveAnalysisPartialEntity implements Serializable{

	private static final long serialVersionUID = -7913620955362179253L;

	private String alarm_message_id;

	private String nongood_parts_situation;

	private Date receive_date;

	private String stored_parts;

	private Integer stored_parts_resolve;

	private Integer occur_times;

	public String getAlarm_message_id() {
		return alarm_message_id;
	}

	public void setAlarm_message_id(String alarm_message_id) {
		this.alarm_message_id = alarm_message_id;
	}

	public String getNongood_parts_situation() {
		return nongood_parts_situation;
	}

	public void setNongood_parts_situation(String nongood_parts_situation) {
		this.nongood_parts_situation = nongood_parts_situation;
	}

	public Date getReceive_date() {
		return receive_date;
	}

	public void setReceive_date(Date receive_date) {
		this.receive_date = receive_date;
	}

	public String getStored_parts() {
		return stored_parts;
	}

	public void setStored_parts(String stored_parts) {
		this.stored_parts = stored_parts;
	}

	public Integer getStored_parts_resolve() {
		return stored_parts_resolve;
	}

	public void setStored_parts_resolve(Integer stored_parts_resolve) {
		this.stored_parts_resolve = stored_parts_resolve;
	}

	public Integer getOccur_times() {
		return occur_times;
	}

	public void setOccur_times(Integer occur_times) {
		this.occur_times = occur_times;
	}


}
