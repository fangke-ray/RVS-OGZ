package com.osh.rvsif.common.bean;

import java.io.Serializable;

public class IfSapMessageEntity implements Serializable {

	private static final long serialVersionUID = 5290575289129634728L;

	private Integer if_sap_message_key;

	// 1:收到 2:发出
	private Integer forward;

	private String kind;

	private String response_message;

	// 1:正常2:错误
	private Integer check_status;

	public Integer getIf_sap_message_key() {
		return if_sap_message_key;
	}

	public void setIf_sap_message_key(Integer if_sap_message_key) {
		this.if_sap_message_key = if_sap_message_key;
	}

	public Integer getForward() {
		return forward;
	}

	public void setForward(Integer forward) {
		this.forward = forward;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getResponse_message() {
		return response_message;
	}

	public void setResponse_message(String response_message) {
		this.response_message = response_message;
	}

	public Integer getCheck_status() {
		return check_status;
	}

	public void setCheck_status(Integer check_status) {
		this.check_status = check_status;
	}
}
