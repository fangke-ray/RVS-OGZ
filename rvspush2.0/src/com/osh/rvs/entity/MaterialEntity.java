package com.osh.rvs.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author fxc PS: String 在Mapper.XML中IF判断条件 !=NULL and !=''; 其他 !=NULL
 */
public class MaterialEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7498550404024660749L;

	private String material_id;
	private String sorc_no;
	private String esas_no;
	private String model_id;
	private String serial_no;
	private Integer ocm;
	private Integer level;
	private String package_no;
	private String storager;
	private Integer direct_flg;
	private Integer service_repair_flg;
	private Date reception_time;
	private Integer fix_type;
	private String operator_id;
	private Date inline_time;
	private String section_id;
	private String wip_location;
	private Integer am_pm;
	private Integer scheduled_expedited;
	private String scheduled_manager_comment;
	private Date outline_time;
	private Integer break_back_flg;
	private Date agreed_date;
	private String model_name;
	private String operator_name;
	private Integer sterilized;
	private String pat_id;
	private Date qa_check_time;
	private Date filing_time;
	private Integer ticket_flg;
	private Date wip_date;
	private Integer unrepair_flg;
	private Integer selectable;
	private Integer quotation_first;
	private Integer symbol1;
	private String category_id;
	private String category_name;
	private String reception_time_start;
	private String reception_time_end;
	private String inline_time_start;
	private String inline_time_end;
	private String scheduled_date_start;
	private String scheduled_date_end;
	private String processing_position;
	private String processing_position2;
	private String section_name;
	private Date scheduled_date;
	private Date action_time;
	private Date finish_time;
	private Date quotation_time;
	private Integer operate_result;
	private Date arrival_plan_date;
	private Date scheduled_assign_date;
	// 作业等待类型
	private Integer now_operate_result;
	// 暂停理由
	private Integer now_pause_reason;

	private String pcs_inputs;
	private String job_no;// 工号
	private Integer bound_out_ocm;
	private String bound_out_package_no;
	private String repair_notifi_no;
	private String manage_serial_no;
	private String omr_notifi_no;
	private Integer kind;
	private Date accept_finish_time;
	private String accept_job_no;
	private Date disinfect_finish_time;
	private String disinfect_job_no;
	private Integer area;

	private String customer_name;
	private String line_id;

	public String getCategory_id() {
		return category_id;
	}

	public void setCategory_id(String category_id) {
		this.category_id = category_id;
	}

	public String getReception_time_start() {
		return reception_time_start;
	}

	public void setReception_time_start(String reception_time_start) {
		this.reception_time_start = reception_time_start;
	}

	public String getReception_time_end() {
		return reception_time_end;
	}

	public void setReception_time_end(String reception_time_end) {
		this.reception_time_end = reception_time_end;
	}

	public String getInline_time_start() {
		return inline_time_start;
	}

	public void setInline_time_start(String inline_time_start) {
		this.inline_time_start = inline_time_start;
	}

	public String getInline_time_end() {
		return inline_time_end;
	}

	public void setInline_time_end(String inline_time_end) {
		this.inline_time_end = inline_time_end;
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

	public Integer getOperate_result() {
		return operate_result;
	}

	public void setOperate_result(Integer operate_result) {
		this.operate_result = operate_result;
	}

	public Date getFinish_time() {
		return finish_time;
	}

	public void setFinish_time(Date finish_time) {
		this.finish_time = finish_time;
	}

	public Date getScheduled_date() {
		return scheduled_date;
	}

	public void setScheduled_date(Date scheduled_date) {
		this.scheduled_date = scheduled_date;
	}

	public String getSection_name() {
		return section_name;
	}

	public void setSection_name(String section_name) {
		this.section_name = section_name;
	}

	public String getProcessing_position() {
		return processing_position;
	}

	public void setProcessing_position(String processing_position) {
		this.processing_position = processing_position;
	}

	public String getOperator_name() {
		return operator_name;
	}

	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}

	public Integer getSterilized() {
		return sterilized;
	}

	public void setSterilized(Integer sterilized) {
		this.sterilized = sterilized;
	}

	public Date getInline_time() {
		return inline_time;
	}

	public void setInline_time(Date inline_time) {
		this.inline_time = inline_time;
	}

	public String getSection_id() {
		return section_id;
	}

	public void setSection_id(String section_id) {
		this.section_id = section_id;
	}

	public String getWip_location() {
		return wip_location;
	}

	public void setWip_location(String wip_location) {
		this.wip_location = wip_location;
	}

	public Integer getAm_pm() {
		return am_pm;
	}

	public void setAm_pm(Integer am_pm) {
		this.am_pm = am_pm;
	}

	public Integer getScheduled_expedited() {
		return scheduled_expedited;
	}

	public void setScheduled_expedited(Integer scheduled_expedited) {
		this.scheduled_expedited = scheduled_expedited;
	}

	public String getScheduled_manager_comment() {
		return scheduled_manager_comment;
	}

	public void setScheduled_manager_comment(String scheduled_manager_comment) {
		this.scheduled_manager_comment = scheduled_manager_comment;
	}

	public Date getOutline_time() {
		return outline_time;
	}

	public void setOutline_time(Date outline_time) {
		this.outline_time = outline_time;
	}

	public Integer getBreak_back_flg() {
		return break_back_flg;
	}

	public void setBreak_back_flg(Integer break_back_flg) {
		this.break_back_flg = break_back_flg;
	}

	public String getModel_name() {
		return model_name;
	}

	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}

	public Date getAgreed_date() {
		return agreed_date;
	}

	public void setAgreed_date(Date agreed_date) {
		this.agreed_date = agreed_date;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public Integer getService_repair_flg() {
		return service_repair_flg;
	}

	public void setService_repair_flg(Integer service_repair_flg) {
		this.service_repair_flg = service_repair_flg;
	}

	public Integer getFix_type() {
		return fix_type;
	}

	public void setFix_type(Integer fix_type) {
		this.fix_type = fix_type;
	}

	public String getMaterial_id() {
		return material_id;
	}

	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}

	public String getSorc_no() {
		return sorc_no;
	}

	public void setSorc_no(String sorc_no) {
		this.sorc_no = sorc_no;
	}

	public String getEsas_no() {
		return esas_no;
	}

	public void setEsas_no(String esas_no) {
		this.esas_no = esas_no;
	}

	public String getModel_id() {
		return model_id;
	}

	public void setModel_id(String model_id) {
		this.model_id = model_id;
	}

	public String getSerial_no() {
		return serial_no;
	}

	public void setSerial_no(String serial_no) {
		this.serial_no = serial_no;
	}

	public Integer getOcm() {
		return ocm;
	}

	public void setOcm(Integer ocm) {
		this.ocm = ocm;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getPackage_no() {
		return package_no;
	}

	public void setPackage_no(String package_no) {
		this.package_no = package_no;
	}

	public String getStorager() {
		return storager;
	}

	public void setStorager(String storager) {
		this.storager = storager;
	}

	public Integer getDirect_flg() {
		return direct_flg;
	}

	public void setDirect_flg(Integer direct_flg) {
		this.direct_flg = direct_flg;
	}

	public Date getReception_time() {
		return reception_time;
	}

	public void setReception_time(Date reception_time) {
		this.reception_time = reception_time;
	}

	public String getPat_id() {
		return pat_id;
	}

	public void setPat_id(String pat_id) {
		this.pat_id = pat_id;
	}

	public Integer getNow_operate_result() {
		return now_operate_result;
	}

	public void setNow_operate_result(Integer now_operate_result) {
		this.now_operate_result = now_operate_result;
	}

	public Integer getNow_pause_reason() {
		return now_pause_reason;
	}

	public void setNow_pause_reason(Integer now_pause_reason) {
		this.now_pause_reason = now_pause_reason;
	}

	public Date getQuotation_time() {
		return quotation_time;
	}

	public void setQuotation_time(Date quotation_time) {
		this.quotation_time = quotation_time;
	}

	public Date getQa_check_time() {
		return qa_check_time;
	}

	public void setQa_check_time(Date qa_check_time) {
		this.qa_check_time = qa_check_time;
	}

	public Date getFiling_time() {
		return filing_time;
	}

	public void setFiling_time(Date filing_time) {
		this.filing_time = filing_time;
	}

	public Integer getTicket_flg() {
		return ticket_flg;
	}

	public void setTicket_flg(Integer ticket_flg) {
		this.ticket_flg = ticket_flg;
	}

	public Date getWip_date() {
		return wip_date;
	}

	public void setWip_date(Date wip_date) {
		this.wip_date = wip_date;
	}

	/**
	 * @return the category_name
	 */
	public String getCategory_name() {
		return category_name;
	}

	/**
	 * @param category_name
	 *            the category_name to set
	 */
	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}

	/**
	 * @return the unrepair_flg
	 */
	public Integer getUnrepair_flg() {
		return unrepair_flg;
	}

	/**
	 * @param unrepair_flg
	 *            the unrepair_flg to set
	 */
	public void setUnrepair_flg(Integer unrepair_flg) {
		this.unrepair_flg = unrepair_flg;
	}

	public Integer getSelectable() {
		return selectable;
	}

	public void setSelectable(Integer selectable) {
		this.selectable = selectable;
	}

	public Integer getQuotation_first() {
		return quotation_first;
	}

	public void setQuotation_first(Integer quotation_first) {
		this.quotation_first = quotation_first;
	}

	public Integer getSymbol1() {
		return symbol1;
	}

	public void setSymbol1(Integer symbol1) {
		this.symbol1 = symbol1;
	}

	/**
	 * @return the processing_position2
	 */
	public String getProcessing_position2() {
		return processing_position2;
	}

	/**
	 * @param processing_position2
	 *            the processing_position2 to set
	 */
	public void setProcessing_position2(String processing_position2) {
		this.processing_position2 = processing_position2;
	}

	public Date getArrival_plan_date() {
		return arrival_plan_date;
	}

	public void setArrival_plan_date(Date arrival_plan_date) {
		this.arrival_plan_date = arrival_plan_date;
	}

	public Date getScheduled_assign_date() {
		return scheduled_assign_date;
	}

	public void setScheduled_assign_date(Date scheduled_assign_date) {
		this.scheduled_assign_date = scheduled_assign_date;
	}

	public String getPcs_inputs() {
		return pcs_inputs;
	}

	public void setPcs_inputs(String pcs_inputs) {
		this.pcs_inputs = pcs_inputs;
	}

	public String getJob_no() {
		return job_no;
	}

	public void setJob_no(String job_no) {
		this.job_no = job_no;
	}

	public Integer getBound_out_ocm() {
		return bound_out_ocm;
	}

	public void setBound_out_ocm(Integer bound_out_ocm) {
		this.bound_out_ocm = bound_out_ocm;
	}

	public String getBound_out_package_no() {
		return bound_out_package_no;
	}

	public void setBound_out_package_no(String bound_out_package_no) {
		this.bound_out_package_no = bound_out_package_no;
	}

	public Date getAction_time() {
		return action_time;
	}

	public void setAction_time(Date action_time) {
		this.action_time = action_time;
	}

	public String getRepair_notifi_no() {
		return repair_notifi_no;
	}

	public void setRepair_notifi_no(String repair_notifi_no) {
		this.repair_notifi_no = repair_notifi_no;
	}

	public String getManage_serial_no() {
		return manage_serial_no;
	}

	public void setManage_serial_no(String manage_serial_no) {
		this.manage_serial_no = manage_serial_no;
	}

	public String getOmr_notifi_no() {
		return omr_notifi_no;
	}

	public void setOmr_notifi_no(String omr_notifi_no) {
		this.omr_notifi_no = omr_notifi_no;
	}

	public Integer getKind() {
		return kind;
	}

	public void setKind(Integer kind) {
		this.kind = kind;
	}

	public Date getAccept_finish_time() {
		return accept_finish_time;
	}

	public void setAccept_finish_time(Date accept_finish_time) {
		this.accept_finish_time = accept_finish_time;
	}

	public String getAccept_job_no() {
		return accept_job_no;
	}

	public void setAccept_job_no(String accept_job_no) {
		this.accept_job_no = accept_job_no;
	}

	public Date getDisinfect_finish_time() {
		return disinfect_finish_time;
	}

	public void setDisinfect_finish_time(Date disinfect_finish_time) {
		this.disinfect_finish_time = disinfect_finish_time;
	}

	public String getDisinfect_job_no() {
		return disinfect_job_no;
	}

	public void setDisinfect_job_no(String disinfect_job_no) {
		this.disinfect_job_no = disinfect_job_no;
	}

	public String getCustomer_name() {
		return customer_name;
	}

	public void setCustomer_name(String customer_name) {
		this.customer_name = customer_name;
	}

	public Integer getArea() {
		return area;
	}

	public void setArea(Integer area) {
		this.area = area;
	}

	public String getLine_id() {
		return line_id;
	}

	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}

}
