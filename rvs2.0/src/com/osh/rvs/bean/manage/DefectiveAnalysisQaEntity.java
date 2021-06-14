package com.osh.rvs.bean.manage;

import java.io.Serializable;
import java.util.Date;

public class DefectiveAnalysisQaEntity implements Serializable{

	private static final long serialVersionUID = -7913620955362179253L;

	private String alarm_message_id;

	private String defective_items;

	private Integer involving;

	private String involving_reason;

	private String closing_judgment;

	private Integer closing_judger_id;

	private Date closing_judger_date;

	private Integer closing_confirmer_id;

	private Date closing_confirmer_date;

	public String getAlarm_message_id() {
		return alarm_message_id;
	}

	public void setAlarm_message_id(String alarm_message_id) {
		this.alarm_message_id = alarm_message_id;
	}

	public String getDefective_items() {
		return defective_items;
	}

	public void setDefective_items(String defective_items) {
		this.defective_items = defective_items;
	}

	public Integer getInvolving() {
		return involving;
	}

	public void setInvolving(Integer involving) {
		this.involving = involving;
	}

	public String getInvolving_reason() {
		return involving_reason;
	}

	public void setInvolving_reason(String involving_reason) {
		this.involving_reason = involving_reason;
	}

	public String getClosing_judgment() {
		return closing_judgment;
	}

	public void setClosing_judgment(String closing_judgment) {
		this.closing_judgment = closing_judgment;
	}

	public Integer getClosing_judger_id() {
		return closing_judger_id;
	}

	public void setClosing_judger_id(Integer closing_judger_id) {
		this.closing_judger_id = closing_judger_id;
	}

	public Date getClosing_judger_date() {
		return closing_judger_date;
	}

	public void setClosing_judger_date(Date closing_judger_date) {
		this.closing_judger_date = closing_judger_date;
	}

	public Integer getClosing_confirmer_id() {
		return closing_confirmer_id;
	}

	public void setClosing_confirmer_id(Integer closing_confirmer_id) {
		this.closing_confirmer_id = closing_confirmer_id;
	}

	public Date getClosing_confirmer_date() {
		return closing_confirmer_date;
	}

	public void setClosing_confirmer_date(Date closing_confirmer_date) {
		this.closing_confirmer_date = closing_confirmer_date;
	}

}
