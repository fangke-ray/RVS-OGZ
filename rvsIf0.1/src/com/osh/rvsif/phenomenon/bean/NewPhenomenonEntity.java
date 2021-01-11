package com.osh.rvsif.phenomenon.bean;

import java.io.Serializable;


public class NewPhenomenonEntity implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5200256589911210021L;

	private String omr_notifi_no; // SAPRepairNotificationNo
	private String material_id;
	private String key; // RVSDetailNo
	private String location_group_desc;
	private String location_desc;
	private String description;
	private String return_status; // DetermineTime
	private String last_sent_message_number;

	private String job_no; // DeterminePerson
	private String operator_id;
	public String getOmr_notifi_no() {
		return omr_notifi_no;
	}
	public void setOmr_notifi_no(String omr_notifi_no) {
		this.omr_notifi_no = omr_notifi_no;
	}
	public String getMaterial_id() {
		return material_id;
	}
	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getLocation_group_desc() {
		return location_group_desc;
	}
	public void setLocation_group_desc(String location_group_desc) {
		this.location_group_desc = location_group_desc;
	}
	public String getLocation_desc() {
		return location_desc;
	}
	public void setLocation_desc(String location_desc) {
		this.location_desc = location_desc;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getJob_no() {
		return job_no;
	}
	public void setJob_no(String job_no) {
		this.job_no = job_no;
	}
	public String getOperator_id() {
		return operator_id;
	}
	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}
	public String getReturn_status() {
		return return_status;
	}
	public void setReturn_status(String return_status) {
		this.return_status = return_status;
	}
	public String getLast_sent_message_number() {
		return last_sent_message_number;
	}
	public void setLast_sent_message_number(String last_sent_message_number) {
		this.last_sent_message_number = last_sent_message_number;
	}

}
