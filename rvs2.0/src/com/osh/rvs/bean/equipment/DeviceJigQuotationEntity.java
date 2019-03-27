package com.osh.rvs.bean.equipment;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备工具治具订购报价
 * 
 * @author liuxb
 * 
 */
public class DeviceJigQuotationEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3057948275361557786L;

	/**
	 * Key
	 */
	private String quotation_id;

	/**
	 * 报价单号
	 */
	private String quotation_no;

	/**
	 * 确认接收日期
	 */
	private Date acquire_date;

	/**
	 * 委托单号
	 */
	private String entrust_no;

	/**
	 * 委托发送日期
	 */
	private Date entrust_send_date;

	/**
	 * 发送OSH日期
	 */
	private Date delivery_osh_date;

	/**
	 * 预计纳期
	 */
	private Date scheduled_date;

	/**
	 * 备注
	 */
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

	public Date getAcquire_date() {
		return acquire_date;
	}

	public void setAcquire_date(Date acquire_date) {
		this.acquire_date = acquire_date;
	}

	public String getEntrust_no() {
		return entrust_no;
	}

	public void setEntrust_no(String entrust_no) {
		this.entrust_no = entrust_no;
	}

	public Date getEntrust_send_date() {
		return entrust_send_date;
	}

	public void setEntrust_send_date(Date entrust_send_date) {
		this.entrust_send_date = entrust_send_date;
	}

	public Date getDelivery_osh_date() {
		return delivery_osh_date;
	}

	public void setDelivery_osh_date(Date delivery_osh_date) {
		this.delivery_osh_date = delivery_osh_date;
	}

	public Date getScheduled_date() {
		return scheduled_date;
	}

	public void setScheduled_date(Date scheduled_date) {
		this.scheduled_date = scheduled_date;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
