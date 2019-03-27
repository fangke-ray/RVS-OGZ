package com.osh.rvs.form.equipment;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

/**
 * 设备工具治具订购报价
 * 
 * @author liuxb
 * 
 */
public class DeviceJigQuotationForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7427040037046536353L;

	@BeanField(title = "Key", name = "quotation_id", type = FieldType.String, length = 11, notNull = true)
	private String quotation_id;

	@BeanField(title = "报价单号", name = "quotation_no", type = FieldType.String, length = 9, notNull = true)
	private String quotation_no;

	@BeanField(title = "确认接收日期", name = "acquire_date", type = FieldType.Date)
	private String acquire_date;

	@BeanField(title = "委托单号", name = "entrust_no", type = FieldType.String, length = 9)
	private String entrust_no;

	@BeanField(title = "委托发送日期", name = "entrust_send_date", type = FieldType.Date)
	private String entrust_send_date;

	@BeanField(title = "发送OSH日期", name = "delivery_osh_date", type = FieldType.Date)
	private String delivery_osh_date;

	@BeanField(title = "预计纳期", name = "scheduled_date", type = FieldType.Date)
	private String scheduled_date;

	@BeanField(title = "备注", name = "comment", type = FieldType.String, length = 256)
	private String comment;

	public String getQuotation_id() {
		return quotation_id;
	}

	public void setQuotation_id(String quotation_id) {
		this.quotation_id = quotation_id;
	}

	public String getQuotation_no() {
		return quotation_no;
	}

	public void setQuotation_no(String quotation_no) {
		this.quotation_no = quotation_no;
	}

	public String getAcquire_date() {
		return acquire_date;
	}

	public void setAcquire_date(String acquire_date) {
		this.acquire_date = acquire_date;
	}

	public String getEntrust_no() {
		return entrust_no;
	}

	public void setEntrust_no(String entrust_no) {
		this.entrust_no = entrust_no;
	}

	public String getEntrust_send_date() {
		return entrust_send_date;
	}

	public void setEntrust_send_date(String entrust_send_date) {
		this.entrust_send_date = entrust_send_date;
	}

	public String getDelivery_osh_date() {
		return delivery_osh_date;
	}

	public void setDelivery_osh_date(String delivery_osh_date) {
		this.delivery_osh_date = delivery_osh_date;
	}

	public String getScheduled_date() {
		return scheduled_date;
	}

	public void setScheduled_date(String scheduled_date) {
		this.scheduled_date = scheduled_date;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
