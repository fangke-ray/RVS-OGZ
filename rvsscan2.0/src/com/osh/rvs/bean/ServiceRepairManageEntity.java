package com.osh.rvs.bean;

import java.io.Serializable;
import java.util.Date;

public class ServiceRepairManageEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1717898670489095598L;
	
	private String material_id;
	private String model_name;
	private String serial_no;
	private String sorc_no;
	private Integer service_repair_flg;
	private Date rc_mailsend_date;
	private Date rc_ship_assign_date;
	private Date qa_reception_time;
	private Date qa_reception_time_end;
	private Date qa_referee_time_end;
	private Date qa_referee_time;
	private Integer answer_in_deadline;
	private Integer service_free_flg;
	private Date qa_secondary_referee_date;
	private String rank;
	private Integer workshop;
	private String countermeasures;
	private String comment;
	private String mention;

	private Date reception_date;
	private Date quotation_date;
	private Date agreed_date;
	private Date inline_date;
	private Date outline_date;
	private Integer unfix_back_flg;
	
	private Date qa_reception_time_start;
	private Date qa_referee_time_start;
	
	private String include_month;
	private Double charge_amount;
	
	public String getMaterial_id() {
		return material_id;
	}
	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
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
	public String getSorc_no() {
		return sorc_no;
	}
	public void setSorc_no(String sorc_no) {
		this.sorc_no = sorc_no;
	}
	public Integer getService_repair_flg() {
		return service_repair_flg;
	}
	public void setService_repair_flg(Integer service_repair_flg) {
		this.service_repair_flg = service_repair_flg;
	}
	public Date getRc_mailsend_date() {
		return rc_mailsend_date;
	}
	public void setRc_mailsend_date(Date rc_mailsend_date) {
		this.rc_mailsend_date = rc_mailsend_date;
	}
	public Date getRc_ship_assign_date() {
		return rc_ship_assign_date;
	}
	public void setRc_ship_assign_date(Date rc_ship_assign_date) {
		this.rc_ship_assign_date = rc_ship_assign_date;
	}
	public Date getQa_reception_time() {
		return qa_reception_time;
	}
	public void setQa_reception_time(Date qa_reception_time) {
		this.qa_reception_time = qa_reception_time;
	}
	public Date getQa_referee_time() {
		return qa_referee_time;
	}
	public void setQa_referee_time(Date qa_referee_time) {
		this.qa_referee_time = qa_referee_time;
	}
	public Integer getAnswer_in_deadline() {
		return answer_in_deadline;
	}
	public void setAnswer_in_deadline(Integer answer_in_deadline) {
		this.answer_in_deadline = answer_in_deadline;
	}
	public Integer getService_free_flg() {
		return service_free_flg;
	}
	public void setService_free_flg(Integer service_free_flg) {
		this.service_free_flg = service_free_flg;
	}
	public Date getQa_secondary_referee_date() {
		return qa_secondary_referee_date;
	}
	public void setQa_secondary_referee_date(Date qa_secondary_referee_date) {
		this.qa_secondary_referee_date = qa_secondary_referee_date;
	}
	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}
	public Integer getWorkshop() {
		return workshop;
	}
	public void setWorkshop(Integer workshop) {
		this.workshop = workshop;
	}
	public String getCountermeasures() {
		return countermeasures;
	}
	public void setCountermeasures(String countermeasures) {
		this.countermeasures = countermeasures;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getMention() {
		return mention;
	}
	public void setMention(String mention) {
		this.mention = mention;
	}
	public Date getReception_date() {
		return reception_date;
	}
	public void setReception_date(Date reception_date) {
		this.reception_date = reception_date;
	}
	public Date getQuotation_date() {
		return quotation_date;
	}
	public void setQuotation_date(Date quotation_date) {
		this.quotation_date = quotation_date;
	}
	public Date getAgreed_date() {
		return agreed_date;
	}
	public void setAgreed_date(Date agreed_date) {
		this.agreed_date = agreed_date;
	}
	public Date getInline_date() {
		return inline_date;
	}
	public void setInline_date(Date inline_date) {
		this.inline_date = inline_date;
	}
	public Date getOutline_date() {
		return outline_date;
	}
	public void setOutline_date(Date outline_date) {
		this.outline_date = outline_date;
	}
	public Integer getUnfix_back_flg() {
		return unfix_back_flg;
	}
	public void setUnfix_back_flg(Integer unfix_back_flg) {
		this.unfix_back_flg = unfix_back_flg;
	}
	public Date getQa_reception_time_end() {
		return qa_reception_time_end;
	}
	public void setQa_reception_time_end(Date qa_reception_time_end) {
		this.qa_reception_time_end = qa_reception_time_end;
	}
	public Date getQa_referee_time_end() {
		return qa_referee_time_end;
	}
	public void setQa_referee_time_end(Date qa_referee_time_end) {
		this.qa_referee_time_end = qa_referee_time_end;
	}
	public Date getQa_reception_time_start() {
		return qa_reception_time_start;
	}
	public void setQa_reception_time_start(Date qa_reception_time_start) {
		this.qa_reception_time_start = qa_reception_time_start;
	}
	public Date getQa_referee_time_start() {
		return qa_referee_time_start;
	}
	public void setQa_referee_time_start(Date qa_referee_time_start) {
		this.qa_referee_time_start = qa_referee_time_start;
	}
	public String getInclude_month() {
		return include_month;
	}
	public void setInclude_month(String include_month) {
		this.include_month = include_month;
	}
	public Double getCharge_amount() {
		return charge_amount;
	}
	public void setCharge_amount(Double charge_amount) {
		this.charge_amount = charge_amount;
	}
	
}
