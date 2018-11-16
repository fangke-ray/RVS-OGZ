package com.osh.rvs.bean.inline;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author fxc
 * PS: String 在Mapper.XML中IF判断条件 !=NULL and !='';
 * 		其他  !=NULL
 */
public class MaterialProcessEntity  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 420070483135911357L;


	private String material_id;
	

	private String line_id;
	private Date finish_date;
	private Date schedule_date;
	private Date schedule_assign_date;

	private Date dec_plan_date;
	private Date dec_finish_date;
	private Date ns_plan_date;
	private Date ns_finish_date;
	private Date com_plan_date;
	private Date com_finish_date;
	
	
	public Date getSchedule_date() {
		return schedule_date;
	}
	public void setSchedule_date(Date schedule_date) {
		this.schedule_date = schedule_date;
	}
	public String getLine_id() {
		return line_id;
	}
	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}
	public Date getFinish_date() {
		return finish_date;
	}
	public void setFinish_date(Date finish_date) {
		this.finish_date = finish_date;
	}
	public String getMaterial_id() {
		return material_id;
	}
	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}
	public Date getDec_plan_date() {
		return dec_plan_date;
	}
	public void setDec_plan_date(Date dec_plan_date) {
		this.dec_plan_date = dec_plan_date;
	}
	public Date getDec_finish_date() {
		return dec_finish_date;
	}
	public void setDec_finish_date(Date dec_finish_date) {
		this.dec_finish_date = dec_finish_date;
	}
	public Date getNs_plan_date() {
		return ns_plan_date;
	}
	public void setNs_plan_date(Date ns_plan_date) {
		this.ns_plan_date = ns_plan_date;
	}
	public Date getNs_finish_date() {
		return ns_finish_date;
	}
	public void setNs_finish_date(Date ns_finish_date) {
		this.ns_finish_date = ns_finish_date;
	}
	public Date getCom_plan_date() {
		return com_plan_date;
	}
	public void setCom_plan_date(Date com_plan_date) {
		this.com_plan_date = com_plan_date;
	}
	public Date getCom_finish_date() {
		return com_finish_date;
	}
	public void setCom_finish_date(Date com_finish_date) {
		this.com_finish_date = com_finish_date;
	}
	public Date getSchedule_assign_date() {
		return schedule_assign_date;
	}
	public void setSchedule_assign_date(Date schedule_assign_date) {
		this.schedule_assign_date = schedule_assign_date;
	}
	
	
	
	
}
