package com.osh.rvs.bean.equipment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 设备工具治具订单明细
 * 
 * @author liuxb
 * 
 */
public class DeviceJigOrderDetailEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1651341345956068574L;

	/**
	 * Key
	 */
	private String order_key;

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
	 * 系统编码
	 */
	private String system_code;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 受注方
	 */
	private Integer order_from;
	/**
	 * 数量
	 */

	private Integer quantity;

	/**
	 * 询价结果 ID
	 */
	private String order_invoice_id;

	/**
	 * 申请者
	 */
	private String applicator_id;

	/**
	 * 理由/必要性
	 */
	private String nesssary_reason;

	/**
	 * 申请日期
	 */
	private Date applicate_date;

	/**
	 * 报价 ID
	 */
	private String quotation_id;

	/**
	 * 重新订购纳期
	 */
	private Date reorder_scheduled_date;

	/**
	 * 收货时间
	 */
	private Date recept_date;

	/**
	 * 确认结果
	 */
	private Integer confirm_flg;

	/**
	 * 确认数量
	 */
	private Integer confirm_quantity;

	/**
	 * 验收日期
	 */
	private Date inline_recept_date;

	/**
	 * 验收人
	 */
	private String inline_receptor_id;

	/**
	 * 预算月
	 */
	private String budget_month;

	/**
	 * 预算说明
	 */
	private String budget_description;

	// 报价单号
	private String quotation_no;

	// 订单号
	private String order_no;

	// 询价
	private Integer order_invoice_flg;

	// 询价发送日期开始
	private Date send_date_start;

	// 询价发送日期结束
	private Date send_date_end;

	// 预计纳期开始
	private Date scheduled_date_start;

	// 预计纳期结束
	private Date scheduled_date_end;

	// 收货时间开始
	private Date recept_date_start;

	// 收货时间结束
	private Date recept_date_end;

	// 验收
	private Integer inline_recept_flg;

	// 委托单号
	private String entrust_no;

	// 询价
	private BigDecimal order_price;

	// 金额
	private BigDecimal total_order_price;

	// 原产单价
	private BigDecimal origin_price;

	// 差异
	private BigDecimal differ_price;

	// 申请者
	private String applicator_operator_name;

	// 委托发送日期
	private Date entrust_send_date;

	// 询价发送日期
	private Date send_date;

	// 确认接收日期
	private Date acquire_date;

	// 发送OSH日期
	private Date delivery_osh_date;

	// 纳期
	private Date scheduled_date;

	// 验收人
	private String inline_receptor_operator_name;

	// 设备名称
	private String device_type_name;

	// 现有备品数
	private Integer available_inventory;

	private String comment;

	public String getOrder_key() {
		return order_key;
	}

	public void setOrder_key(String order_key) {
		this.order_key = order_key;
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

	public String getSystem_code() {
		return system_code;
	}

	public void setSystem_code(String system_code) {
		this.system_code = system_code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getOrder_from() {
		return order_from;
	}

	public void setOrder_from(Integer order_from) {
		this.order_from = order_from;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getOrder_invoice_id() {
		return order_invoice_id;
	}

	public void setOrder_invoice_id(String order_invoice_id) {
		this.order_invoice_id = order_invoice_id;
	}

	public String getApplicator_id() {
		return applicator_id;
	}

	public void setApplicator_id(String applicator_id) {
		this.applicator_id = applicator_id;
	}

	public String getNesssary_reason() {
		return nesssary_reason;
	}

	public void setNesssary_reason(String nesssary_reason) {
		this.nesssary_reason = nesssary_reason;
	}

	public Date getApplicate_date() {
		return applicate_date;
	}

	public void setApplicate_date(Date applicate_date) {
		this.applicate_date = applicate_date;
	}

	public String getQuotation_id() {
		return quotation_id;
	}

	public void setQuotation_id(String quotation_id) {
		this.quotation_id = quotation_id;
	}

	public Date getReorder_scheduled_date() {
		return reorder_scheduled_date;
	}

	public void setReorder_scheduled_date(Date reorder_scheduled_date) {
		this.reorder_scheduled_date = reorder_scheduled_date;
	}

	public Date getRecept_date() {
		return recept_date;
	}

	public void setRecept_date(Date recept_date) {
		this.recept_date = recept_date;
	}

	public Integer getConfirm_flg() {
		return confirm_flg;
	}

	public void setConfirm_flg(Integer confirm_flg) {
		this.confirm_flg = confirm_flg;
	}

	public Date getInline_recept_date() {
		return inline_recept_date;
	}

	public void setInline_recept_date(Date inline_recept_date) {
		this.inline_recept_date = inline_recept_date;
	}

	public String getInline_receptor_id() {
		return inline_receptor_id;
	}

	public void setInline_receptor_id(String inline_receptor_id) {
		this.inline_receptor_id = inline_receptor_id;
	}

	public String getBudget_month() {
		return budget_month;
	}

	public void setBudget_month(String budget_month) {
		this.budget_month = budget_month;
	}

	public String getBudget_description() {
		return budget_description;
	}

	public void setBudget_description(String budget_description) {
		this.budget_description = budget_description;
	}

	public String getQuotation_no() {
		return quotation_no;
	}

	public void setQuotation_no(String quotation_no) {
		this.quotation_no = quotation_no;
	}

	public String getOrder_no() {
		return order_no;
	}

	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}

	public Integer getOrder_invoice_flg() {
		return order_invoice_flg;
	}

	public void setOrder_invoice_flg(Integer order_invoice_flg) {
		this.order_invoice_flg = order_invoice_flg;
	}

	public Date getSend_date_start() {
		return send_date_start;
	}

	public void setSend_date_start(Date send_date_start) {
		this.send_date_start = send_date_start;
	}

	public Date getSend_date_end() {
		return send_date_end;
	}

	public void setSend_date_end(Date send_date_end) {
		this.send_date_end = send_date_end;
	}

	public Date getScheduled_date_start() {
		return scheduled_date_start;
	}

	public void setScheduled_date_start(Date scheduled_date_start) {
		this.scheduled_date_start = scheduled_date_start;
	}

	public Date getScheduled_date_end() {
		return scheduled_date_end;
	}

	public void setScheduled_date_end(Date scheduled_date_end) {
		this.scheduled_date_end = scheduled_date_end;
	}

	public Date getRecept_date_start() {
		return recept_date_start;
	}

	public void setRecept_date_start(Date recept_date_start) {
		this.recept_date_start = recept_date_start;
	}

	public Date getRecept_date_end() {
		return recept_date_end;
	}

	public void setRecept_date_end(Date recept_date_end) {
		this.recept_date_end = recept_date_end;
	}

	public Integer getInline_recept_flg() {
		return inline_recept_flg;
	}

	public void setInline_recept_flg(Integer inline_recept_flg) {
		this.inline_recept_flg = inline_recept_flg;
	}

	public String getEntrust_no() {
		return entrust_no;
	}

	public void setEntrust_no(String entrust_no) {
		this.entrust_no = entrust_no;
	}

	public BigDecimal getOrder_price() {
		return order_price;
	}

	public void setOrder_price(BigDecimal order_price) {
		this.order_price = order_price;
	}

	public BigDecimal getTotal_order_price() {
		return total_order_price;
	}

	public void setTotal_order_price(BigDecimal total_order_price) {
		this.total_order_price = total_order_price;
	}

	public BigDecimal getOrigin_price() {
		return origin_price;
	}

	public void setOrigin_price(BigDecimal origin_price) {
		this.origin_price = origin_price;
	}

	public BigDecimal getDiffer_price() {
		return differ_price;
	}

	public void setDiffer_price(BigDecimal differ_price) {
		this.differ_price = differ_price;
	}

	public String getApplicator_operator_name() {
		return applicator_operator_name;
	}

	public void setApplicator_operator_name(String applicator_operator_name) {
		this.applicator_operator_name = applicator_operator_name;
	}

	public Date getEntrust_send_date() {
		return entrust_send_date;
	}

	public void setEntrust_send_date(Date entrust_send_date) {
		this.entrust_send_date = entrust_send_date;
	}

	public Date getSend_date() {
		return send_date;
	}

	public void setSend_date(Date send_date) {
		this.send_date = send_date;
	}

	public Date getAcquire_date() {
		return acquire_date;
	}

	public void setAcquire_date(Date acquire_date) {
		this.acquire_date = acquire_date;
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

	public String getInline_receptor_operator_name() {
		return inline_receptor_operator_name;
	}

	public void setInline_receptor_operator_name(String inline_receptor_operator_name) {
		this.inline_receptor_operator_name = inline_receptor_operator_name;
	}

	public String getDevice_type_name() {
		return device_type_name;
	}

	public void setDevice_type_name(String device_type_name) {
		this.device_type_name = device_type_name;
	}

	public Integer getAvailable_inventory() {
		return available_inventory;
	}

	public void setAvailable_inventory(Integer available_inventory) {
		this.available_inventory = available_inventory;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getConfirm_quantity() {
		return confirm_quantity;
	}

	public void setConfirm_quantity(Integer confirm_quantity) {
		this.confirm_quantity = confirm_quantity;
	}

}
