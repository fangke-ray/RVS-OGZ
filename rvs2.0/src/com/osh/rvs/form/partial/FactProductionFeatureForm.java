package com.osh.rvs.form.partial;

import com.osh.rvs.form.UploadForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.CodeListUtils;

/**
 * 现品作业信息
 *
 * @author liuxb
 *
 */
public class FactProductionFeatureForm extends UploadForm {

	/**
	 *
	 */
	private static final long serialVersionUID = -6591012227794797960L;

	/**
	 * KEY
	 */
	@BeanField(title = "KEY", name = "fact_pf_key", length = 11, primaryKey = true, notNull = true)
	private String fact_pf_key;

	/**
	 * 作业内容
	 */
	@BeanField(title = "作业内容", name = "production_type", type = FieldType.Integer, length = 2, notNull = true)
	private String production_type;

	/**
	 * 操作者 ID
	 */
	@BeanField(title = "操作者 ID", name = "operator_id", length = 11, notNull = true)
	private String operator_id;

	/**
	 * 处理开始时间
	 */
	@BeanField(title = "处理开始时间", name = "action_time", type = FieldType.DateTime, notNull = true)
	private String action_time;

	/**
	 * 处理结束时间
	 */
	@BeanField(title = "处理结束时间", name = "finish_time", type = FieldType.DateTime)
	private String finish_time;

	/**
	 * 零件入库单 KEY
	 */
	@BeanField(title = "零件入库单 KEY", name = "partial_warehouse_key", length = 11)
	private String partial_warehouse_key;

	/**
	 * 维修对象 ID
	 */
	@BeanField(title = "维修对象 ID", name = "material_id", length = 11)
	private String material_id;

	/**
	 * 订购次数
	 */
	@BeanField(title = "订购次数", name = "occur_times", type = FieldType.Integer, length = 2)
	private String occur_times;

	/**
	 * 日期
	 */
	@BeanField(title = "日期", name = "warehouse_date", type = FieldType.Date)
	private String warehouse_date;

	/**
	 * DN 编号
	 */
	@BeanField(title = "DN 编号", name = "dn_no", type = FieldType.String, length = 16)
	private String dn_no;

	/**
	 * 入库进展
	 */
	private String step;

	/**
	 * 作业内容名称
	 */
	private String production_type_name;

	public String getFact_pf_key() {
		return fact_pf_key;
	}

	public void setFact_pf_key(String fact_pf_key) {
		this.fact_pf_key = fact_pf_key;
	}

	public String getProduction_type() {
		return production_type;
	}

	public void setProduction_type(String production_type) {
		this.production_type = production_type;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public String getAction_time() {
		return action_time;
	}

	public void setAction_time(String action_time) {
		this.action_time = action_time;
	}

	public String getFinish_time() {
		return finish_time;
	}

	public void setFinish_time(String finish_time) {
		this.finish_time = finish_time;
	}

	public String getPartial_warehouse_key() {
		return partial_warehouse_key;
	}

	public void setPartial_warehouse_key(String partial_warehouse_key) {
		this.partial_warehouse_key = partial_warehouse_key;
	}

	public String getMaterial_id() {
		return material_id;
	}

	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}

	public String getOccur_times() {
		return occur_times;
	}

	public void setOccur_times(String occur_times) {
		this.occur_times = occur_times;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getProduction_type_name() {
		if (production_type != null) {
			return CodeListUtils.getValue("fact_production_type", production_type);
		}

		return production_type_name;
	}

	public void setProduction_type_name(String production_type_name) {
		this.production_type_name = production_type_name;
	}

	public String getWarehouse_date() {
		return warehouse_date;
	}

	public void setWarehouse_date(String warehouse_date) {
		this.warehouse_date = warehouse_date;
	}

	public String getDn_no() {
		return dn_no;
	}

	public void setDn_no(String dn_no) {
		this.dn_no = dn_no;
	}

}
