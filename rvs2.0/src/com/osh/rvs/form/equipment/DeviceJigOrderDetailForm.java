package com.osh.rvs.form.equipment;

import java.io.Serializable;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.CodeListUtils;

/**
 * 设备工具治具订单明细
 * 
 * @author liuxb
 * 
 */
public class DeviceJigOrderDetailForm extends ActionForm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2684235510627835826L;

	@BeanField(title = "Key", name = "order_key", type = FieldType.String, length = 11, notNull = true)
	private String order_key;

	@BeanField(title = "对象类别", name = "object_type", type = FieldType.Integer, length = 1, notNull = true)
	private String object_type;

	@BeanField(title = "设备工具品名ID", name = "device_type_id", type = FieldType.String, length = 11, notNull = true)
	private String device_type_id;

	@BeanField(title = "型号/规格", name = "model_name", type = FieldType.String, length = 32, notNull = true)
	private String model_name;

	@BeanField(title = "系统编码", name = "system_code", type = FieldType.String, length = 32)
	private String system_code;

	@BeanField(title = "名称", name = "name", type = FieldType.String, length = 64, notNull = true)
	private String name;

	@BeanField(title = "受注方", name = "order_from", type = FieldType.Integer, length = 1)
	private String order_from;

	@BeanField(title = "数量", name = "quantity", type = FieldType.UInteger, length = 2, notNull = true)
	private String quantity;

	@BeanField(title = "询价结果 ID", name = "order_invoice_id", type = FieldType.String, length = 11)
	private String order_invoice_id;

	@BeanField(title = "申请者", name = "applicator_id", type = FieldType.String, length = 11, notNull = true)
	private String applicator_id;

	@BeanField(title = "理由/必要性", name = "nesssary_reason", type = FieldType.String, length = 256)
	private String nesssary_reason;

	@BeanField(title = "申请日期", name = "applicate_date", type = FieldType.Date)
	private String applicate_date;

	@BeanField(title = "报价 ID", name = "quotation_id", type = FieldType.String, length = 11)
	private String quotation_id;

	@BeanField(title = "重新订购纳期", name = "reorder_scheduled_date", type = FieldType.Date)
	private String reorder_scheduled_date;

	@BeanField(title = "收货时间", name = "recept_date", type = FieldType.Date)
	private String recept_date;

	@BeanField(title = "确认结果", name = "confirm_flg", type = FieldType.Integer, length = 1)
	private String confirm_flg;

	@BeanField(title = "确认数量", name = "confirm_quantity", type = FieldType.UInteger, length = 2)
	private String confirm_quantity;

	@BeanField(title = "验收日期", name = "inline_recept_date", type = FieldType.Date)
	private String inline_recept_date;

	@BeanField(title = "验收人", name = "inline_receptor_id", type = FieldType.String, length = 11)
	private String inline_receptor_id;

	@BeanField(title = "预算月", name = "budget_month", type = FieldType.String, length = 6)
	private String budget_month;

	@BeanField(title = "预算说明", name = "budget_description", type = FieldType.String, length = 256)
	private String budget_description;

	@BeanField(title = "发票号", name = "invoice_no", type = FieldType.String, length = 8)
	private String invoice_no;

	@BeanField(title = "发票收到日期", name = "invoice_date", type = FieldType.Date)
	private String invoice_date;

	@BeanField(title = "报价单号", name = "quotation_no", type = FieldType.String, length = 9)
	private String quotation_no;

	@BeanField(title = "订单号", name = "order_no", type = FieldType.String)
	private String order_no;

	@BeanField(title = "询价", name = "order_invoice_flg", type = FieldType.Integer)
	private String order_invoice_flg;

	@BeanField(title = "询价发送日期开始", name = "send_date_start", type = FieldType.Date)
	private String send_date_start;

	@BeanField(title = "询价发送日期结束", name = "send_date_end", type = FieldType.Date)
	private String send_date_end;

	@BeanField(title = "预计纳期开始", name = "scheduled_date_start", type = FieldType.Date)
	private String scheduled_date_start;

	@BeanField(title = "预计纳期结束", name = "scheduled_date_end", type = FieldType.Date)
	private String scheduled_date_end;

	@BeanField(title = "收货时间开始", name = "recept_date_start", type = FieldType.Date)
	private String recept_date_start;

	@BeanField(title = "收货时间结束", name = "recept_date_end", type = FieldType.Date)
	private String recept_date_end;

	@BeanField(title = "验收", name = "inline_recept_flg", type = FieldType.Integer)
	private String inline_recept_flg;

	@BeanField(title = "委托单号", name = "entrust_no", type = FieldType.String)
	private String entrust_no;

	@BeanField(title = "订购单价", name = "order_price", type = FieldType.UDouble, length = 7, scale = 0)
	private String order_price;

	@BeanField(title = "金额", name = "total_order_price", type = FieldType.Double)
	private String total_order_price;

	@BeanField(title = "原产单价", name = "origin_price", type = FieldType.UDouble, length = 9, scale = 2)
	private String origin_price;

	@BeanField(title = "差异", name = "differ_price", type = FieldType.Double)
	private String differ_price;

	@BeanField(title = "申请者", name = "applicator_operator_name", type = FieldType.String)
	private String applicator_operator_name;

	@BeanField(title = "委托发送日期", name = "entrust_send_date", type = FieldType.Date)
	private String entrust_send_date;

	@BeanField(title = "询价发送日期", name = "send_date", type = FieldType.Date)
	private String send_date;

	@BeanField(title = "确认接收日期", name = "acquire_date", type = FieldType.Date)
	private String acquire_date;

	@BeanField(title = "发送OSH日期", name = "delivery_osh_date", type = FieldType.Date)
	private String delivery_osh_date;

	@BeanField(title = "预计纳期", name = "scheduled_date", type = FieldType.Date)
	private String scheduled_date;

	@BeanField(title = "验收人", name = "inline_receptor_operator_name", type = FieldType.String)
	private String inline_receptor_operator_name;

	@BeanField(title = "设备名称", name = "device_type_name", type = FieldType.String)
	private String device_type_name;

	@BeanField(title = "现有备品数", name = "available_inventory", type = FieldType.Integer)
	private String available_inventory;

	@BeanField(title = "备品种类", name = "device_spare_type", type = FieldType.Integer, length = 1)
	private String device_spare_type;

	@BeanField(title = "询价结果 ID", name = "invoice_id", type = FieldType.String, length = 11)
	private String invoice_id;

	@BeanField(title = "备注", name = "comment", type = FieldType.String, length = 256)
	private String comment;

	private String object_type_name;

	private String order_from_name;

	private String confirm_flg_name;
	
	//经理确认标记
	private String manage_comfirm_flg;

	public String getOrder_key() {
		return order_key;
	}

	public void setOrder_key(String order_key) {
		this.order_key = order_key;
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

	public String getOrder_from() {
		return order_from;
	}

	public void setOrder_from(String order_from) {
		this.order_from = order_from;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
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

	public String getApplicate_date() {
		return applicate_date;
	}

	public void setApplicate_date(String applicate_date) {
		this.applicate_date = applicate_date;
	}

	public String getQuotation_id() {
		return quotation_id;
	}

	public void setQuotation_id(String quotation_id) {
		this.quotation_id = quotation_id;
	}

	public String getReorder_scheduled_date() {
		return reorder_scheduled_date;
	}

	public void setReorder_scheduled_date(String reorder_scheduled_date) {
		this.reorder_scheduled_date = reorder_scheduled_date;
	}

	public String getRecept_date() {
		return recept_date;
	}

	public void setRecept_date(String recept_date) {
		this.recept_date = recept_date;
	}

	public String getConfirm_flg() {
		return confirm_flg;
	}

	public void setConfirm_flg(String confirm_flg) {
		this.confirm_flg = confirm_flg;
	}

	public String getInline_recept_date() {
		return inline_recept_date;
	}

	public void setInline_recept_date(String inline_recept_date) {
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

	public String getInvoice_no() {
		return invoice_no;
	}

	public void setInvoice_no(String invoice_no) {
		this.invoice_no = invoice_no;
	}

	public String getInvoice_date() {
		return invoice_date;
	}

	public void setInvoice_date(String invoice_date) {
		this.invoice_date = invoice_date;
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

	public String getOrder_invoice_flg() {
		return order_invoice_flg;
	}

	public void setOrder_invoice_flg(String order_invoice_flg) {
		this.order_invoice_flg = order_invoice_flg;
	}

	public String getSend_date_start() {
		return send_date_start;
	}

	public void setSend_date_start(String send_date_start) {
		this.send_date_start = send_date_start;
	}

	public String getSend_date_end() {
		return send_date_end;
	}

	public void setSend_date_end(String send_date_end) {
		this.send_date_end = send_date_end;
	}

	public String getScheduled_date_start() {
		return scheduled_date_start;
	}

	public void setScheduled_date_start(String scheduled_date_start) {
		this.scheduled_date_start = scheduled_date_start;
	}

	public String getScheduled_date_end() {
		return scheduled_date_end;
	}

	public void setScheduled_date_end(String scheduled_date_end) {
		this.scheduled_date_end = scheduled_date_end;
	}

	public String getRecept_date_start() {
		return recept_date_start;
	}

	public void setRecept_date_start(String recept_date_start) {
		this.recept_date_start = recept_date_start;
	}

	public String getRecept_date_end() {
		return recept_date_end;
	}

	public void setRecept_date_end(String recept_date_end) {
		this.recept_date_end = recept_date_end;
	}

	public String getInline_recept_flg() {
		return inline_recept_flg;
	}

	public void setInline_recept_flg(String inline_recept_flg) {
		this.inline_recept_flg = inline_recept_flg;
	}

	public String getEntrust_no() {
		return entrust_no;
	}

	public void setEntrust_no(String entrust_no) {
		this.entrust_no = entrust_no;
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

	public String getOrder_from_name() {
		if (order_from != null) {
			return CodeListUtils.getValue("device_jig_order_from", order_from);
		}

		return order_from_name;
	}

	public void setOrder_from_name(String order_from_name) {
		this.order_from_name = order_from_name;
	}

	public String getOrder_price() {
		return order_price;
	}

	public void setOrder_price(String order_price) {
		this.order_price = order_price;
	}

	public String getTotal_order_price() {
		return total_order_price;
	}

	public void setTotal_order_price(String total_order_price) {
		this.total_order_price = total_order_price;
	}

	public String getOrigin_price() {
		return origin_price;
	}

	public void setOrigin_price(String origin_price) {
		this.origin_price = origin_price;
	}

	public String getDiffer_price() {
		return differ_price;
	}

	public void setDiffer_price(String differ_price) {
		this.differ_price = differ_price;
	}

	public String getApplicator_operator_name() {
		return applicator_operator_name;
	}

	public void setApplicator_operator_name(String applicator_operator_name) {
		this.applicator_operator_name = applicator_operator_name;
	}

	public String getEntrust_send_date() {
		return entrust_send_date;
	}

	public void setEntrust_send_date(String entrust_send_date) {
		this.entrust_send_date = entrust_send_date;
	}

	public String getSend_date() {
		return send_date;
	}

	public void setSend_date(String send_date) {
		this.send_date = send_date;
	}

	public String getAcquire_date() {
		return acquire_date;
	}

	public void setAcquire_date(String acquire_date) {
		this.acquire_date = acquire_date;
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

	public String getConfirm_flg_name() {
		if (confirm_flg != null) {
			return CodeListUtils.getValue("device_jig_confirm_flg", confirm_flg);
		}

		return confirm_flg_name;
	}

	public void setConfirm_flg_name(String confirm_flg_name) {
		this.confirm_flg_name = confirm_flg_name;
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

	public String getAvailable_inventory() {
		return available_inventory;
	}

	public void setAvailable_inventory(String available_inventory) {
		this.available_inventory = available_inventory;
	}

	public String getDevice_spare_type() {
		return device_spare_type;
	}

	public void setDevice_spare_type(String device_spare_type) {
		this.device_spare_type = device_spare_type;
	}

	public String getInvoice_id() {
		return invoice_id;
	}

	public void setInvoice_id(String invoice_id) {
		this.invoice_id = invoice_id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getConfirm_quantity() {
		return confirm_quantity;
	}

	public void setConfirm_quantity(String confirm_quantity) {
		this.confirm_quantity = confirm_quantity;
	}

	public String getManage_comfirm_flg() {
		return manage_comfirm_flg;
	}

	public void setManage_comfirm_flg(String manage_comfirm_flg) {
		this.manage_comfirm_flg = manage_comfirm_flg;
	}

}
