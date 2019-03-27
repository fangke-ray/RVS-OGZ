package com.osh.rvs.form.equipment;

import java.io.Serializable;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

/**
 * 设备工具治具订单
 * 
 * @author liuxb
 * 
 */
public class DeviceJigOrderForm extends ActionForm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1087027426214869844L;

	@BeanField(title = "Key", name = "order_key", type = FieldType.String, length = 11, notNull = true)
	private String order_key;

	@BeanField(title = "订单号", name = "order_no", type = FieldType.String, length = 9, notNull = true)
	private String order_no;

	public String getOrder_key() {
		return order_key;
	}

	public void setOrder_key(String order_key) {
		this.order_key = order_key;
	}

	public String getOrder_no() {
		return order_no;
	}

	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}

}
