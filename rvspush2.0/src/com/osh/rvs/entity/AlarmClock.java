package com.osh.rvs.entity;

import com.osh.rvs.common.RvsConsts;

public class AlarmClock {

	private Long ring_time;

	private String mp_key;

	private AlarmMesssageEntity ring_message;

	private String line_id;

	public AlarmClock(Long ring_time, String material_id, String position_id, String line_id, String operator_id) {
		this.mp_key = material_id + "_" + position_id;
//		this.expert_time = expert_time;
		this.ring_time = ring_time;
		this.ring_message = new AlarmMesssageEntity();
		this.ring_message.setLevel(RvsConsts.WARNING_LEVEL_NORMAL); // 工程级别
		this.ring_message.setMaterial_id(material_id);
		this.ring_message.setReason(RvsConsts.WARNING_REASON_POSITION_OVERTIME); // 工位超时
		this.ring_message.setPosition_id(position_id);
		this.ring_message.setLine_id(line_id);
		this.ring_message.setSection_id("1");
		this.ring_message.setOperator_id(operator_id);
		this.line_id = line_id;

//		this.ring_message = "维修对象" + omr_notifi_no + "在" + process_code + "工位的标准作业时间设定为"
//				+ standard_worktime + "分钟。";
	}

	public Long getRing_time() {
		return ring_time;
	}

	public AlarmMesssageEntity getRing_message() {
		return ring_message;
	}

	public String getMp_key() {
		return mp_key;
	}

	public String getLine_id() {
		return line_id;
	}
}
