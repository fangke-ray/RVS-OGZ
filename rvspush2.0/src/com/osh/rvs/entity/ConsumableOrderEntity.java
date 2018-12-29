package com.osh.rvs.entity;

import java.io.Serializable;

/**
 * 
 * @Title ConsumableOrderEntity.java
 * @Project rvspush
 * @Package com.osh.rvs.entity
 * @ClassName: ConsumableOrderEntity
 * @Description: TODO
 * @author lxb
 * @date 2015-5-13 下午5:31:45
 */
public class ConsumableOrderEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3538706994713809393L;
	/*** 零件ID */
	private String partial_id;

	/** 补充量 **/
	private Integer supply_quantity;

	/** 补充量 **/
	private Integer supply_day;

	/** 消耗品订购单编号 **/
	private String order_no;

	/** 消耗品申请单Key **/
	private String consumable_order_key;

	/** 订购数量 **/
	private Integer order_quantity;

	public String getPartial_id() {
		return partial_id;
	}

	public void setPartial_id(String partial_id) {
		this.partial_id = partial_id;
	}

	public Integer getSupply_quantity() {
		return supply_quantity;
	}

	public void setSupply_quantity(Integer supply_quantity) {
		this.supply_quantity = supply_quantity;
	}

	public Integer getSupply_day() {
		return supply_day;
	}

	public void setSupply_day(Integer supply_day) {
		this.supply_day = supply_day;
	}

	public String getOrder_no() {
		return order_no;
	}

	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}

	public String getConsumable_order_key() {
		return consumable_order_key;
	}

	public void setConsumable_order_key(String consumable_order_key) {
		this.consumable_order_key = consumable_order_key;
	}

	public Integer getOrder_quantity() {
		return order_quantity;
	}

	public void setOrder_quantity(Integer order_quantity) {
		this.order_quantity = order_quantity;
	}

}
