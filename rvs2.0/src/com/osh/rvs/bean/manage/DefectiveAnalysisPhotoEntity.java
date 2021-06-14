package com.osh.rvs.bean.manage;

import java.io.Serializable;

public class DefectiveAnalysisPhotoEntity implements Serializable{

	private static final long serialVersionUID = -7913620955362179253L;

	private String alarm_message_id;

	private Integer seq;

	private Integer for_step;

	private String file_uuid;

	public String getAlarm_message_id() {
		return alarm_message_id;
	}

	public void setAlarm_message_id(String alarm_message_id) {
		this.alarm_message_id = alarm_message_id;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Integer getFor_step() {
		return for_step;
	}

	public void setFor_step(Integer for_step) {
		this.for_step = for_step;
	}

	public String getFile_uuid() {
		return file_uuid;
	}

	public void setFile_uuid(String file_uuid) {
		this.file_uuid = file_uuid;
	}

}
