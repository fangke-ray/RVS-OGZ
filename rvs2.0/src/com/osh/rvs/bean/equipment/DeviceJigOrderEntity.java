package com.osh.rvs.bean.equipment;

import java.io.Serializable;

/**
 * 设备工具治具订单
 * 
 * @author liuxb
 * 
 */
public class DeviceJigOrderEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6185088275334032835L;

	/**
	 * Key
	 */
	private String order_key;

	/**
	 * 订单号
	 */
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
