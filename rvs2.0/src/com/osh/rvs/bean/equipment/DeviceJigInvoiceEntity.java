package com.osh.rvs.bean.equipment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 设备工具治具订购询价
 * 
 * @author liuxb
 * 
 */
public class DeviceJigInvoiceEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5187619179015023626L;

	/**
	 * Key
	 */
	private String invoice_id;

	/**
	 * 对象类别
	 */
	private Integer object_type;

	/**
	 * 设备工具品名ID
	 */
	private String device_type_id;

	/**
	 * 型号/规格
	 */
	private String model_name;

	/**
	 * 询价发送日期
	 */
	private Date send_date;

	/**
	 * 订购单价
	 */
	private BigDecimal order_price;

	/**
	 * 原产单价
	 */
	private BigDecimal origin_price;

	/**
	 * 备注
	 */
	private String comment;

	public String getInvoice_id() {
		return invoice_id;
	}

	public void setInvoice_id(String invoice_id) {
		this.invoice_id = invoice_id;
	}

	public Integer getObject_type() {
		return object_type;
	}

	public void setObject_type(Integer object_type) {
		this.object_type = object_type;
	}

	public String getDevice_type_id() {
		return device_type_id;
	}

	public void setDevice_type_id(String device_type_id) {
		this.device_type_id = device_type_id;
	}

	public String getModel_name() {
		return model_name;
	}

	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}

	public Date getSend_date() {
		return send_date;
	}

	public void setSend_date(Date send_date) {
		this.send_date = send_date;
	}

	public BigDecimal getOrder_price() {
		return order_price;
	}

	public void setOrder_price(BigDecimal order_price) {
		this.order_price = order_price;
	}

	public BigDecimal getOrigin_price() {
		return origin_price;
	}

	public void setOrigin_price(BigDecimal origin_price) {
		this.origin_price = origin_price;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
