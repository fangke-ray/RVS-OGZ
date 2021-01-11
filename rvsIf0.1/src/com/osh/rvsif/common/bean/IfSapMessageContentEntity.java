package com.osh.rvsif.common.bean;

import java.io.Serializable;

public class IfSapMessageContentEntity implements Serializable {

	private static final long serialVersionUID = 5290575289129634728L;

	private Integer if_sap_message_key;

	private Integer seq;

	private String content;

	private Integer resolved;
	
	private String invalid_message;

	public Integer getIf_sap_message_key() {
		return if_sap_message_key;
	}

	public void setIf_sap_message_key(Integer if_sap_message_key) {
		this.if_sap_message_key = if_sap_message_key;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getResolved() {
		return resolved;
	}

	public void setResolved(Integer resolved) {
		this.resolved = resolved;
	}

	public String getInvalid_message() {
		return invalid_message;
	}

	public void setInvalid_message(String invalid_message) {
		this.invalid_message = invalid_message;
	}
	
	
}
