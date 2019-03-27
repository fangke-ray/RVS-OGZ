package com.osh.rvs.form.equipment;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.CodeListUtils;

/**
 * 设备工具治具订购询价
 * 
 * @author liuxb
 * 
 */
public class DeviceJigInvoiceForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7573993343527258061L;

	@BeanField(title = "Key", name = "invoice_id", type = FieldType.String, length = 11, notNull = true)
	private String invoice_id;

	@BeanField(title = "对象类别", name = "object_type", type = FieldType.Integer, length = 1, notNull = true)
	private String object_type;

	@BeanField(title = "设备工具品名ID", name = "device_type_id", type = FieldType.String, length = 11, notNull = true)
	private String device_type_id;

	@BeanField(title = "型号/规格", name = "model_name", type = FieldType.String, length = 32, notNull = true)
	private String model_name;

	@BeanField(title = "询价发送日期", name = "send_date", type = FieldType.Date, notNull = true)
	private String send_date;

	@BeanField(title = "订购单价", name = "order_price", type = FieldType.UDouble, length = 7, scale = 0)
	private String order_price;

	@BeanField(title = "原产单价", name = "origin_price", type = FieldType.UDouble, length = 9, scale = 2)
	private String origin_price;

	@BeanField(title = "备注", name = "comment", type = FieldType.String, length = 256)
	private String comment;

	private String object_type_name;

	public String getInvoice_id() {
		return invoice_id;
	}

	public void setInvoice_id(String invoice_id) {
		this.invoice_id = invoice_id;
	}

	public String getObject_type() {
		return object_type;
	}

	public void setObject_type(String object_type) {
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

	public String getSend_date() {
		return send_date;
	}

	public void setSend_date(String send_date) {
		this.send_date = send_date;
	}

	public String getOrder_price() {
		return order_price;
	}

	public void setOrder_price(String order_price) {
		this.order_price = order_price;
	}

	public String getOrigin_price() {
		return origin_price;
	}

	public void setOrigin_price(String origin_price) {
		this.origin_price = origin_price;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getObject_type_name() {
		if (object_type != null) {
			return CodeListUtils.getValue("device_jig_object_type", object_type);
		}

		return object_type_name;
	}

	public void setObject_type_name(String object_type_name) {
		this.object_type_name = object_type_name;
	}

}
