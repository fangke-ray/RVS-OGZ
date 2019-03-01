package com.osh.rvs.form.equipment;

import java.io.Serializable;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.CodeListUtils;

/**
 * 设备工具备品
 * 
 * @author liuxb
 * 
 */
public class DeviceSpareForm extends ActionForm implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5678496147905455420L;

	@BeanField(title = "品名", name = "device_type_id", type = FieldType.String, length = 11, notNull = true)
	private String device_type_id;

	@BeanField(title = "型号", name = "model_name", type = FieldType.String, length = 32, notNull = true)
	private String model_name;

	@BeanField(title = "备品种类", name = "device_spare_type", type = FieldType.Integer, length = 1, notNull = true)
	private String device_spare_type;

	@BeanField(title = "订货天数（工作日）", name = "order_cycle", type = FieldType.UInteger, length = 2, notNull = true)
	private String order_cycle;

	@BeanField(title = "品牌", name = "brand_id", type = FieldType.String, length = 11)
	private String brand_id;

	@BeanField(title = "单价", name = "price", type = FieldType.UInteger, length = 5, notNull = true)
	private String price;

	@BeanField(title = "Min-Limit", name = "safety_lever", type = FieldType.UInteger, length = 5, notNull = true)
	private String safety_lever;

	@BeanField(title = "Max-Limit", name = "benchmark", type = FieldType.UInteger, length = 5, notNull = true)
	private String benchmark;

	@BeanField(title = "当前有效库存", name = "available_inventory", type = FieldType.UInteger, length = 5, notNull = true)
	private String available_inventory;

	@BeanField(title = "放置位置", name = "location", type = FieldType.String, length = 10)
	private String location;

	@BeanField(title = "管理备注", name = "comment", type = FieldType.String, length = 250)
	private String comment;

	@BeanField(title = "品名", name = "device_type_name", type = FieldType.String)
	private String device_type_name;

	@BeanField(title = "品牌名称", name = "brand_name", type = FieldType.String)
	private String brand_name;

	@BeanField(title = "需要订购", name = "order_flg", type = FieldType.Integer, length = 1)
	private String order_flg;

	@BeanField(title = "合理总额", name = "total_benchmark_price", type = FieldType.Integer)
	private String total_benchmark_price;

	@BeanField(title = "在库总额", name = "total_available_price", type = FieldType.Integer)
	private String total_available_price;

	@BeanField(title = "调整量", name = "adjust_inventory", type = FieldType.UInteger, length = 5)
	private String adjust_inventory;

	@BeanField(title = "理由", name = "reason_type", type = FieldType.Integer, length = 2)
	private String reason_type;

	@BeanField(title = "计算开始时期", name = "adjust_time_start", type = FieldType.Date)
	private String adjust_time_start;

	@BeanField(title = "计算结束时期", name = "adjust_time_end", type = FieldType.Date)
	private String adjust_time_end;

	@BeanField(title = "期间消耗量", name = "consumable", type = FieldType.Integer)
	private String consumable;

	// 备品种类名称
	private String device_spare_type_name;

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

	public String getDevice_spare_type() {
		return device_spare_type;
	}

	public void setDevice_spare_type(String device_spare_type) {
		this.device_spare_type = device_spare_type;
	}

	public String getOrder_cycle() {
		return order_cycle;
	}

	public void setOrder_cycle(String order_cycle) {
		this.order_cycle = order_cycle;
	}

	public String getBrand_id() {
		return brand_id;
	}

	public void setBrand_id(String brand_id) {
		this.brand_id = brand_id;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getSafety_lever() {
		return safety_lever;
	}

	public void setSafety_lever(String safety_lever) {
		this.safety_lever = safety_lever;
	}

	public String getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(String benchmark) {
		this.benchmark = benchmark;
	}

	public String getAvailable_inventory() {
		return available_inventory;
	}

	public void setAvailable_inventory(String available_inventory) {
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

	public String getOrder_flg() {
		return order_flg;
	}

	public void setOrder_flg(String order_flg) {
		this.order_flg = order_flg;
	}

	public String getTotal_benchmark_price() {
		return total_benchmark_price;
	}

	public void setTotal_benchmark_price(String total_benchmark_price) {
		this.total_benchmark_price = total_benchmark_price;
	}

	public String getTotal_available_price() {
		return total_available_price;
	}

	public void setTotal_available_price(String total_available_price) {
		this.total_available_price = total_available_price;
	}

	public String getDevice_spare_type_name() {
		if (device_spare_type != null) {
			return CodeListUtils.getValue("device_spare_type", device_spare_type);
		}

		return device_spare_type_name;
	}

	public void setDevice_spare_type_name(String device_spare_type_name) {
		this.device_spare_type_name = device_spare_type_name;
	}

	public String getAdjust_inventory() {
		return adjust_inventory;
	}

	public void setAdjust_inventory(String adjust_inventory) {
		this.adjust_inventory = adjust_inventory;
	}

	public String getReason_type() {
		return reason_type;
	}

	public void setReason_type(String reason_type) {
		this.reason_type = reason_type;
	}

	public String getAdjust_time_start() {
		return adjust_time_start;
	}

	public void setAdjust_time_start(String adjust_time_start) {
		this.adjust_time_start = adjust_time_start;
	}

	public String getAdjust_time_end() {
		return adjust_time_end;
	}

	public void setAdjust_time_end(String adjust_time_end) {
		this.adjust_time_end = adjust_time_end;
	}

	public String getConsumable() {
		return consumable;
	}

	public void setConsumable(String consumable) {
		this.consumable = consumable;
	}

}
