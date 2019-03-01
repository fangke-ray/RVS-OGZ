package com.osh.rvs.bean.equipment;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备工具备品
 * 
 * @author liuxb
 * 
 */
public class DeviceSpareEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -179230348977512038L;

	/**
	 * 设备品名 ID
	 */
	private String device_type_id;

	/**
	 * 型号
	 */
	private String model_name;

	/**
	 * 备品种类
	 */
	private Integer device_spare_type;

	/**
	 * 订货天数（工作日）
	 */
	private Integer order_cycle;

	/**
	 * 品牌
	 */
	private String brand_id;

	/**
	 * 单价
	 */
	private Integer price;

	/**
	 * Min-Limit
	 */
	private Integer safety_lever;

	/**
	 * Max-Limit
	 */
	private Integer benchmark;

	/**
	 * 当前有效库存
	 */
	private Integer available_inventory;

	/**
	 * 放置位置
	 */
	private String location;

	/**
	 * 管理备注
	 */
	private String comment;

	// 需要订购
	private Integer order_flg;

	// 品名
	private String device_type_name;

	// 品牌名称
	private String brand_name;

	// 合理总额
	private Integer total_benchmark_price;

	// 在库总额
	private Integer total_available_price;

	// 调整量
	private Integer adjust_inventory;

	// 理由
	private Integer reason_type;

	// 计算开始时期
	private Date adjust_time_start;

	// 计算结束时期
	private Date adjust_time_end;

	// 期间消耗量
	private Integer consumable;

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

	public Integer getDevice_spare_type() {
		return device_spare_type;
	}

	public void setDevice_spare_type(Integer device_spare_type) {
		this.device_spare_type = device_spare_type;
	}

	public Integer getOrder_cycle() {
		return order_cycle;
	}

	public void setOrder_cycle(Integer order_cycle) {
		this.order_cycle = order_cycle;
	}

	public String getBrand_id() {
		return brand_id;
	}

	public void setBrand_id(String brand_id) {
		this.brand_id = brand_id;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Integer getSafety_lever() {
		return safety_lever;
	}

	public void setSafety_lever(Integer safety_lever) {
		this.safety_lever = safety_lever;
	}

	public Integer getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(Integer benchmark) {
		this.benchmark = benchmark;
	}

	public Integer getAvailable_inventory() {
		return available_inventory;
	}

	public void setAvailable_inventory(Integer available_inventory) {
		this.available_inventory = available_inventory;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getOrder_flg() {
		return order_flg;
	}

	public void setOrder_flg(Integer order_flg) {
		this.order_flg = order_flg;
	}

	public String getDevice_type_name() {
		return device_type_name;
	}

	public void setDevice_type_name(String device_type_name) {
		this.device_type_name = device_type_name;
	}

	public String getBrand_name() {
		return brand_name;
	}

	public void setBrand_name(String brand_name) {
		this.brand_name = brand_name;
	}

	public Integer getTotal_benchmark_price() {
		return total_benchmark_price;
	}

	public void setTotal_benchmark_price(Integer total_benchmark_price) {
		this.total_benchmark_price = total_benchmark_price;
	}

	public Integer getTotal_available_price() {
		return total_available_price;
	}

	public void setTotal_available_price(Integer total_available_price) {
		this.total_available_price = total_available_price;
	}

	public Integer getAdjust_inventory() {
		return adjust_inventory;
	}

	public void setAdjust_inventory(Integer adjust_inventory) {
		this.adjust_inventory = adjust_inventory;
	}

	public Integer getReason_type() {
		return reason_type;
	}

	public void setReason_type(Integer reason_type) {
		this.reason_type = reason_type;
	}

	public Date getAdjust_time_start() {
		return adjust_time_start;
	}

	public void setAdjust_time_start(Date adjust_time_start) {
		this.adjust_time_start = adjust_time_start;
	}

	public Date getAdjust_time_end() {
		return adjust_time_end;
	}

	public void setAdjust_time_end(Date adjust_time_end) {
		this.adjust_time_end = adjust_time_end;
	}

	public Integer getConsumable() {
		return consumable;
	}

	public void setConsumable(Integer consumable) {
		this.consumable = consumable;
	}

}
