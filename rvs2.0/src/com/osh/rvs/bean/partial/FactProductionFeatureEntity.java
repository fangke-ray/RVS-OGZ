package com.osh.rvs.bean.partial;

import java.io.Serializable;
import java.util.Date;

/**
 * 现品作业信息
 *
 * @author liuxb
 *
 */
public class FactProductionFeatureEntity implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 29995007799752060L;

	/**
	 * KEY
	 */
	private String fact_pf_key;

	/**
	 * 作业内容
	 */
	private Integer production_type;

	/**
	 * 操作者 ID
	 */
	private String operator_id;

	/**
	 * 处理开始时间
	 */
	private Date action_time;

	/**
	 * 处理结束时间
	 */
	private Date finish_time;

	/**
	 * 零件入库单 KEY
	 */
	private String partial_warehouse_key;

	/**
	 * 维修对象 ID
	 */
	private String material_id;

	/**
	 * 订购次数
	 */
	private Integer occur_times;

	/**
	 * 日期
	 */
	private Date warehouse_date;

	/**
	 * DN 编号
	 */
	private String dn_no;

	public String getFact_pf_key() {
		return fact_pf_key;
	}

	public void setFact_pf_key(String fact_pf_key) {
		this.fact_pf_key = fact_pf_key;
	}

	public Integer getProduction_type() {
		return production_type;
	}

	public void setProduction_type(Integer production_type) {
		this.production_type = production_type;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public Date getAction_time() {
		return action_time;
	}

	public void setAction_time(Date action_time) {
		this.action_time = action_time;
	}

	public Date getFinish_time() {
		return finish_time;
	}

	public void setFinish_time(Date finish_time) {
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

	public Integer getOccur_times() {
		return occur_times;
	}

	public void setOccur_times(Integer occur_times) {
		this.occur_times = occur_times;
	}

	public Date getWarehouse_date() {
		return warehouse_date;
	}

	public void setWarehouse_date(Date warehouse_date) {
		this.warehouse_date = warehouse_date;
	}

	public String getDn_no() {
		return dn_no;
	}

	public void setDn_no(String dn_no) {
		this.dn_no = dn_no;
	}

}
