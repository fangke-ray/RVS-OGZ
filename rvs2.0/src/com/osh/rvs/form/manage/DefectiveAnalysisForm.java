package com.osh.rvs.form.manage;

import java.io.Serializable;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

public class DefectiveAnalysisForm extends ActionForm implements Serializable {

	private static final long serialVersionUID = -6347780780457817033L;

	// 检索：维修单号
	private String omr_notifi_no;

	// 型号
	private String model_name;

	// 受理日
	@BeanField(title="工程ID", name="reception_time", type=FieldType.Date)
	private String reception_time;

	// 维修等级
	private String level;
	private String level_disp;

	private String serial_no;

	// 客户名称
	private String customer_name;

	// 检索：提出日开始
	private String sponsor_date_from;

	// 检索：提出日结束
	private String sponsor_date_to;

	// 检索：工程名
	private String line_name;

	// 检索：工程ID
	@BeanField(title="工程ID", name="line_id", type=FieldType.Integer, length = 11)
	private String line_id;

	// 检索：对策实施日开始
	private String cm_proc_confirmer_date_from;

	// 检索：对策实施日结束
	private String cm_proc_confirmer_date_to;

	@BeanField(title = "警报 ID", name = "alarm_message_id", primaryKey = true, length = 11)
	private String alarm_message_id;

	@BeanField(title = "不良分类", name = "defective_type", type = FieldType.Integer, length = 1)
	private String defective_type;

	private String defective_type_disp;

	@BeanField(title = "管理编号", name = "manage_code", type = FieldType.String, length = 45)
	private String manage_code;

	@BeanField(title = "对策进度", name = "step", type=FieldType.Integer, length = 1)
	private String step;

	private String step_disp;

	@BeanField(title = "提出者", name = "sponsor_operator_id", type=FieldType.Integer, length = 11)
	private String sponsor_operator_id;

	private String sponsor_operator_name;

	@BeanField(title = "提出者日期", name = "sponsor_date", type = FieldType.Date)
	private String sponsor_date;

	@BeanField(title = "不良现象", name = "defective_phenomenon", type = FieldType.String, length = 256)
	private String defective_phenomenon;

	@BeanField(title = "责任区分（生产线）", name = "responsibility_of_line", type = FieldType.Integer, length = 1)
	private String responsibility_of_line;

	private String responsibility_of_line_disp;

	@BeanField(title = "现象确认者 ID", name = "phenomenon_confirmer_id", type = FieldType.Integer, length = 11)
	private String phenomenon_confirmer_id;

	private String phenomenon_confirmer_name;

	@BeanField(title = "现象确认者日期", name = "phenomenon_confirmer_date", type = FieldType.Date)
	private String phenomenon_confirmer_date;

	@BeanField(title = "责任区分（技术）", name = "responsibility_of_ptl", type = FieldType.String, length = 2)
	private String responsibility_of_ptl;

	private String responsibility_of_ptl_disp;

	@BeanField(title = "原因分析", name = "cause_analysis", type = FieldType.String, length = 256)
	private String cause_analysis;

	@BeanField(title = "原因分析者 ID", name = "cause_analyst_id", type = FieldType.Integer, length = 11)
	private String cause_analyst_id;

	private String cause_analyst_name;

	@BeanField(title = "原因分析者日期", name = "cause_analyst_date", type = FieldType.Date)
	private String cause_analyst_date;

	@BeanField(title = "原因确认者 ID", name = "cause_confirmer_id", type = FieldType.Integer, length = 11)
	private String cause_confirmer_id;

	private String cause_confirmer_name;

	@BeanField(title = "原因确认者 日期", name = "cause_confirmer_date", type = FieldType.Date)
	private String cause_confirmer_date;

	@BeanField(title = "CAPA频度判断", name = "capa_frequency", type = FieldType.String, length = 1)
	private String capa_frequency;

	private String capa_frequency_disp;

	@BeanField(title = "CAPA重大度判断", name = "capa_major", type = FieldType.String, length = 1)
	private String capa_major;

	private String capa_major_disp;

	@BeanField(title = "风险大小等级", name = "capa_risk", type = FieldType.Integer, length = 1)
	private String capa_risk;

	private String capa_risk_disp;

	@BeanField(title = "更换零件对应", name = "append_part_order", type = FieldType.String, length = 256)
	private String append_part_order;

	@BeanField(title = "追加订购者 ID", name = "partial_applyier_id", type = FieldType.Integer, length = 11)
	private String partial_applyier_id;

	private String partial_applyier_name;

	@BeanField(title = "返工对应", name = "rework_proceed", type = FieldType.Integer, length = 1)
	private String rework_proceed;

	private String rework_proceed_disp;

	@BeanField(title = "对策", name = "countermeasures", type = FieldType.String, length = 512)
	private String countermeasures;

	@BeanField(title = "对策立案者 ID", name = "cm_filer_id", type = FieldType.Integer, length = 11)
	private String cm_filer_id;

	private String cm_filer_name;

	@BeanField(title = "对策立案日", name = "cm_filer_date", type = FieldType.Date)
	private String cm_filer_date;

	@BeanField(title = "对策确认者 ID", name = "cm_confirmer_id", type = FieldType.Integer, length = 11)
	private String cm_confirmer_id;

	private String cm_confirmer_name;

	@BeanField(title = "对策确认者 日期", name = "cm_confirmer_date", type = FieldType.Date)
	private String cm_confirmer_date;

	@BeanField(title = "对策实施者 ID", name = "cm_processor_id", type = FieldType.String, length = 11)
	private String cm_processor_id;

	private String cm_processor_name;

	@BeanField(title = "对策实施日", name = "cm_processor_date", type = FieldType.Date)
	private String cm_processor_date;

	@BeanField(title = "对策实施确认者 ID", name = "cm_proc_confirmer_id", type = FieldType.Integer, length = 11)
	private String cm_proc_confirmer_id;

	private String cm_proc_confirmer_name;

	@BeanField(title = "对策实施确认者 日期", name = "cm_proc_confirmer_date", type = FieldType.Date)
	private String cm_proc_confirmer_date;

	@BeanField(title = "对策效果", name = "countermeasure_effects", type = FieldType.String, length = 512)
	private String countermeasure_effects;

	@BeanField(title = "对策效果验证者 ID", name = "cm_effect_verifier_id", type = FieldType.Integer, length = 11)
	private String cm_effect_verifier_id;

	private String cm_effect_verifier_name;

	@BeanField(title = "对策效果验证者 日期", name = "cm_effect_verifier_date", type = FieldType.Date)
	private String cm_effect_verifier_date;

	@BeanField(title = "对策效果确认者 ID", name = "cm_effect_confirmer_id", type = FieldType.Integer, length = 11)
	private String cm_effect_confirmer_id;

	private String cm_effect_confirmer_name;

	@BeanField(title = "对策效果确认者 日期", name = "cm_effect_confirmer_date", type = FieldType.Date)
	private String cm_effect_confirmer_date;

	@BeanField(title = "不良零件情况", name = "nongood_parts_situation", type = FieldType.String, length = 256)
	private String nongood_parts_situation;

	@BeanField(title = "领取日期", name = "receive_date", type = FieldType.Date)
	private String receive_date;

	@BeanField(title = "入库部品", name = "stored_parts", type = FieldType.String, length = 256)
	private String stored_parts;

	@BeanField(title = "入库零件不良处理", name = "stored_parts_resolve", type = FieldType.String, length = 1)
	private String stored_parts_resolve;

	private String stored_parts_resolve_disp;

	@BeanField(title = "零件定单次数", name = "occur_times", type = FieldType.Integer, length = 2)
	private String occur_times;


	@BeanField(title = "委托名称", name = "defective_items", type = FieldType.String, length = 45)
	private String defective_items;

	@BeanField(title = "波及性判断结果", name = "involving", type = FieldType.String, length = 1)
	private String involving;

	private String involving_disp;

	@BeanField(title = "波及性判断理由", name = "involving_reason", type = FieldType.String, length = 45)
	private String involving_reason;

	@BeanField(title = "委托关闭判断", name = "closing_judgment", type = FieldType.String, length = 256)
	private String closing_judgment;

	@BeanField(title = "关闭判断者 ID", name = "closing_judger_id", type = FieldType.Integer, length = 11)
	private String closing_judger_id;

	private String closing_judger_name;

	@BeanField(title = "关闭判断日", name = "closing_judger_date", type = FieldType.Date)
	private String closing_judger_date;

	@BeanField(title = "关闭确认者 ID", name = "closing_confirmer_id", type = FieldType.Integer, length = 11)
	private String closing_confirmer_id;

	private String closing_confirmer_name;

	@BeanField(title = "关闭确认者 日期", name = "closing_confirmer_date", type = FieldType.Date)
	private String closing_confirmer_date;

	private String alarm_comments;

	public String getAlarm_message_id() {
		return alarm_message_id;
	}

	public void setAlarm_message_id(String alarm_message_id) {
		this.alarm_message_id = alarm_message_id;
	}

	public String getDefective_type() {
		return defective_type;
	}

	public void setDefective_type(String defective_type) {
		this.defective_type = defective_type;
	}

	public String getManage_code() {
		return manage_code;
	}

	public void setManage_code(String manage_code) {
		this.manage_code = manage_code;
	}

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getSponsor_operator_id() {
		return sponsor_operator_id;
	}

	public void setSponsor_operator_id(String sponsor_operator_id) {
		this.sponsor_operator_id = sponsor_operator_id;
	}

	public String getSponsor_date() {
		return sponsor_date;
	}

	public void setSponsor_date(String sponsor_date) {
		this.sponsor_date = sponsor_date;
	}

	public String getDefective_phenomenon() {
		return defective_phenomenon;
	}

	public void setDefective_phenomenon(String defective_phenomenon) {
		this.defective_phenomenon = defective_phenomenon;
	}

	public String getResponsibility_of_line() {
		return responsibility_of_line;
	}

	public void setResponsibility_of_line(String responsibility_of_line) {
		this.responsibility_of_line = responsibility_of_line;
	}

	public String getPhenomenon_confirmer_id() {
		return phenomenon_confirmer_id;
	}

	public void setPhenomenon_confirmer_id(String phenomenon_confirmer_id) {
		this.phenomenon_confirmer_id = phenomenon_confirmer_id;
	}

	public String getPhenomenon_confirmer_date() {
		return phenomenon_confirmer_date;
	}

	public void setPhenomenon_confirmer_date(String phenomenon_confirmer_date) {
		this.phenomenon_confirmer_date = phenomenon_confirmer_date;
	}

	public String getResponsibility_of_ptl() {
		return responsibility_of_ptl;
	}

	public void setResponsibility_of_ptl(String responsibility_of_ptl) {
		this.responsibility_of_ptl = responsibility_of_ptl;
	}

	public String getCause_analysis() {
		return cause_analysis;
	}

	public void setCause_analysis(String cause_analysis) {
		this.cause_analysis = cause_analysis;
	}

	public String getCause_analyst_id() {
		return cause_analyst_id;
	}

	public void setCause_analyst_id(String cause_analyst_id) {
		this.cause_analyst_id = cause_analyst_id;
	}

	public String getCause_analyst_date() {
		return cause_analyst_date;
	}

	public void setCause_analyst_date(String cause_analyst_date) {
		this.cause_analyst_date = cause_analyst_date;
	}

	public String getCause_confirmer_id() {
		return cause_confirmer_id;
	}

	public void setCause_confirmer_id(String cause_confirmer_id) {
		this.cause_confirmer_id = cause_confirmer_id;
	}

	public String getCause_confirmer_date() {
		return cause_confirmer_date;
	}

	public void setCause_confirmer_date(String cause_confirmer_date) {
		this.cause_confirmer_date = cause_confirmer_date;
	}

	public String getCapa_frequency() {
		return capa_frequency;
	}

	public void setCapa_frequency(String capa_frequency) {
		this.capa_frequency = capa_frequency;
	}

	public String getCapa_major() {
		return capa_major;
	}

	public void setCapa_major(String capa_major) {
		this.capa_major = capa_major;
	}

	public String getCapa_risk() {
		return capa_risk;
	}

	public void setCapa_risk(String capa_risk) {
		this.capa_risk = capa_risk;
	}

	public String getAppend_part_order() {
		return append_part_order;
	}

	public void setAppend_part_order(String append_part_order) {
		this.append_part_order = append_part_order;
	}

	public String getPartial_applyier_id() {
		return partial_applyier_id;
	}

	public void setPartial_applyier_id(String partial_applyier_id) {
		this.partial_applyier_id = partial_applyier_id;
	}

	public String getRework_proceed() {
		return rework_proceed;
	}

	public void setRework_proceed(String rework_proceed) {
		this.rework_proceed = rework_proceed;
	}

	public String getCountermeasures() {
		return countermeasures;
	}

	public void setCountermeasures(String countermeasures) {
		this.countermeasures = countermeasures;
	}

	public String getCm_filer_id() {
		return cm_filer_id;
	}

	public void setCm_filer_id(String cm_filer_id) {
		this.cm_filer_id = cm_filer_id;
	}

	public String getCm_filer_date() {
		return cm_filer_date;
	}

	public void setCm_filer_date(String cm_filer_date) {
		this.cm_filer_date = cm_filer_date;
	}

	public String getCm_confirmer_id() {
		return cm_confirmer_id;
	}

	public void setCm_confirmer_id(String cm_confirmer_id) {
		this.cm_confirmer_id = cm_confirmer_id;
	}

	public String getCm_confirmer_date() {
		return cm_confirmer_date;
	}

	public void setCm_confirmer_date(String cm_confirmer_date) {
		this.cm_confirmer_date = cm_confirmer_date;
	}

	public String getCm_processor_id() {
		return cm_processor_id;
	}

	public void setCm_processor_id(String cm_processor_id) {
		this.cm_processor_id = cm_processor_id;
	}

	public String getCm_processor_date() {
		return cm_processor_date;
	}

	public void setCm_processor_date(String cm_processor_date) {
		this.cm_processor_date = cm_processor_date;
	}

	public String getCm_proc_confirmer_id() {
		return cm_proc_confirmer_id;
	}

	public void setCm_proc_confirmer_id(String cm_proc_confirmer_id) {
		this.cm_proc_confirmer_id = cm_proc_confirmer_id;
	}

	public String getCm_proc_confirmer_date() {
		return cm_proc_confirmer_date;
	}

	public void setCm_proc_confirmer_date(String cm_proc_confirmer_date) {
		this.cm_proc_confirmer_date = cm_proc_confirmer_date;
	}

	public String getCountermeasure_effects() {
		return countermeasure_effects;
	}

	public void setCountermeasure_effects(String countermeasure_effects) {
		this.countermeasure_effects = countermeasure_effects;
	}

	public String getCm_effect_verifier_id() {
		return cm_effect_verifier_id;
	}

	public void setCm_effect_verifier_id(String cm_effect_verifier_id) {
		this.cm_effect_verifier_id = cm_effect_verifier_id;
	}

	public String getCm_effect_verifier_date() {
		return cm_effect_verifier_date;
	}

	public void setCm_effect_verifier_date(String cm_effect_verifier_date) {
		this.cm_effect_verifier_date = cm_effect_verifier_date;
	}

	public String getCm_effect_confirmer_id() {
		return cm_effect_confirmer_id;
	}

	public void setCm_effect_confirmer_id(String cm_effect_confirmer_id) {
		this.cm_effect_confirmer_id = cm_effect_confirmer_id;
	}

	public String getCm_effect_confirmer_date() {
		return cm_effect_confirmer_date;
	}

	public void setCm_effect_confirmer_date(String cm_effect_confirmer_date) {
		this.cm_effect_confirmer_date = cm_effect_confirmer_date;
	}

	public String getOmr_notifi_no() {
		return omr_notifi_no;
	}

	public void setOmr_notifi_no(String omr_notifi_no) {
		this.omr_notifi_no = omr_notifi_no;
	}

	public String getModel_name() {
		return model_name;
	}

	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}

	public String getSponsor_date_from() {
		return sponsor_date_from;
	}

	public void setSponsor_date_from(String sponsor_date_from) {
		this.sponsor_date_from = sponsor_date_from;
	}

	public String getSponsor_date_to() {
		return sponsor_date_to;
	}

	public void setSponsor_date_to(String sponsor_date_to) {
		this.sponsor_date_to = sponsor_date_to;
	}

	public String getLine_name() {
		return line_name;
	}

	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}

	public String getCm_proc_confirmer_date_from() {
		return cm_proc_confirmer_date_from;
	}

	public void setCm_proc_confirmer_date_from(String cm_proc_confirmer_date_from) {
		this.cm_proc_confirmer_date_from = cm_proc_confirmer_date_from;
	}

	public String getCm_proc_confirmer_date_to() {
		return cm_proc_confirmer_date_to;
	}

	public void setCm_proc_confirmer_date_to(String cm_proc_confirmer_date_to) {
		this.cm_proc_confirmer_date_to = cm_proc_confirmer_date_to;
	}

	public String getNongood_parts_situation() {
		return nongood_parts_situation;
	}

	public void setNongood_parts_situation(String nongood_parts_situation) {
		this.nongood_parts_situation = nongood_parts_situation;
	}

	public String getReceive_date() {
		return receive_date;
	}

	public void setReceive_date(String receive_date) {
		this.receive_date = receive_date;
	}

	public String getStored_parts() {
		return stored_parts;
	}

	public void setStored_parts(String stored_parts) {
		this.stored_parts = stored_parts;
	}

	public String getStored_parts_resolve() {
		return stored_parts_resolve;
	}

	public void setStored_parts_resolve(String stored_parts_resolve) {
		this.stored_parts_resolve = stored_parts_resolve;
	}

	public String getOccur_times() {
		return occur_times;
	}

	public void setOccur_times(String occur_times) {
		this.occur_times = occur_times;
	}

	public String getDefective_items() {
		return defective_items;
	}

	public void setDefective_items(String defective_items) {
		this.defective_items = defective_items;
	}

	public String getInvolving() {
		return involving;
	}

	public void setInvolving(String involving) {
		this.involving = involving;
	}

	public String getInvolving_reason() {
		return involving_reason;
	}

	public void setInvolving_reason(String involving_reason) {
		this.involving_reason = involving_reason;
	}

	public String getClosing_judgment() {
		return closing_judgment;
	}

	public void setClosing_judgment(String closing_judgment) {
		this.closing_judgment = closing_judgment;
	}

	public String getClosing_judger_id() {
		return closing_judger_id;
	}

	public void setClosing_judger_id(String closing_judger_id) {
		this.closing_judger_id = closing_judger_id;
	}

	public String getClosing_judger_date() {
		return closing_judger_date;
	}

	public void setClosing_judger_date(String closing_judger_date) {
		this.closing_judger_date = closing_judger_date;
	}

	public String getClosing_confirmer_id() {
		return closing_confirmer_id;
	}

	public void setClosing_confirmer_id(String closing_confirmer_id) {
		this.closing_confirmer_id = closing_confirmer_id;
	}

	public String getClosing_confirmer_date() {
		return closing_confirmer_date;
	}

	public void setClosing_confirmer_date(String closing_confirmer_date) {
		this.closing_confirmer_date = closing_confirmer_date;
	}

	public String getLine_id() {
		return line_id;
	}

	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}

	public String getSponsor_operator_name() {
		return sponsor_operator_name;
	}

	public void setSponsor_operator_name(String sponsor_operator_name) {
		this.sponsor_operator_name = sponsor_operator_name;
	}

	public String getPhenomenon_confirmer_name() {
		return phenomenon_confirmer_name;
	}

	public void setPhenomenon_confirmer_name(String phenomenon_confirmer_name) {
		this.phenomenon_confirmer_name = phenomenon_confirmer_name;
	}

	public String getCause_analyst_name() {
		return cause_analyst_name;
	}

	public void setCause_analyst_name(String cause_analyst_name) {
		this.cause_analyst_name = cause_analyst_name;
	}

	public String getCause_confirmer_name() {
		return cause_confirmer_name;
	}

	public void setCause_confirmer_name(String cause_confirmer_name) {
		this.cause_confirmer_name = cause_confirmer_name;
	}

	public String getPartial_applyier_name() {
		return partial_applyier_name;
	}

	public void setPartial_applyier_name(String partial_applyier_name) {
		this.partial_applyier_name = partial_applyier_name;
	}

	public String getCm_filer_name() {
		return cm_filer_name;
	}

	public void setCm_filer_name(String cm_filer_name) {
		this.cm_filer_name = cm_filer_name;
	}

	public String getCm_confirmer_name() {
		return cm_confirmer_name;
	}

	public void setCm_confirmer_name(String cm_confirmer_name) {
		this.cm_confirmer_name = cm_confirmer_name;
	}

	public String getCm_processor_name() {
		return cm_processor_name;
	}

	public void setCm_processor_name(String cm_processor_name) {
		this.cm_processor_name = cm_processor_name;
	}

	public String getCm_proc_confirmer_name() {
		return cm_proc_confirmer_name;
	}

	public void setCm_proc_confirmer_name(String cm_proc_confirmer_name) {
		this.cm_proc_confirmer_name = cm_proc_confirmer_name;
	}

	public String getCm_effect_verifier_name() {
		return cm_effect_verifier_name;
	}

	public void setCm_effect_verifier_name(String cm_effect_verifier_name) {
		this.cm_effect_verifier_name = cm_effect_verifier_name;
	}

	public String getCm_effect_confirmer_name() {
		return cm_effect_confirmer_name;
	}

	public void setCm_effect_confirmer_name(String cm_effect_confirmer_name) {
		this.cm_effect_confirmer_name = cm_effect_confirmer_name;
	}

	public String getClosing_judger_name() {
		return closing_judger_name;
	}

	public void setClosing_judger_name(String closing_judger_name) {
		this.closing_judger_name = closing_judger_name;
	}

	public String getClosing_confirmer_name() {
		return closing_confirmer_name;
	}

	public void setClosing_confirmer_name(String closing_confirmer_name) {
		this.closing_confirmer_name = closing_confirmer_name;
	}

	public String getDefective_type_disp() {
		return defective_type_disp;
	}

	public void setDefective_type_disp(String defective_type_disp) {
		this.defective_type_disp = defective_type_disp;
	}

	public String getStep_disp() {
		return step_disp;
	}

	public void setStep_disp(String step_disp) {
		this.step_disp = step_disp;
	}

	public String getResponsibility_of_line_disp() {
		return responsibility_of_line_disp;
	}

	public void setResponsibility_of_line_disp(String responsibility_of_line_disp) {
		this.responsibility_of_line_disp = responsibility_of_line_disp;
	}

	public String getResponsibility_of_ptl_disp() {
		return responsibility_of_ptl_disp;
	}

	public void setResponsibility_of_ptl_disp(String responsibility_of_ptl_disp) {
		this.responsibility_of_ptl_disp = responsibility_of_ptl_disp;
	}

	public String getCapa_frequency_disp() {
		return capa_frequency_disp;
	}

	public void setCapa_frequency_disp(String capa_frequency_disp) {
		this.capa_frequency_disp = capa_frequency_disp;
	}

	public String getCapa_major_disp() {
		return capa_major_disp;
	}

	public void setCapa_major_disp(String capa_major_disp) {
		this.capa_major_disp = capa_major_disp;
	}

	public String getCapa_risk_disp() {
		return capa_risk_disp;
	}

	public void setCapa_risk_disp(String capa_risk_disp) {
		this.capa_risk_disp = capa_risk_disp;
	}

	public String getRework_proceed_disp() {
		return rework_proceed_disp;
	}

	public void setRework_proceed_disp(String rework_proceed_disp) {
		this.rework_proceed_disp = rework_proceed_disp;
	}

	public String getStored_parts_resolve_disp() {
		return stored_parts_resolve_disp;
	}

	public void setStored_parts_resolve_disp(String stored_parts_resolve_disp) {
		this.stored_parts_resolve_disp = stored_parts_resolve_disp;
	}

	public String getCustomer_name() {
		return customer_name;
	}

	public void setCustomer_name(String customer_name) {
		this.customer_name = customer_name;
	}

	public String getSerial_no() {
		return serial_no;
	}

	public void setSerial_no(String serial_no) {
		this.serial_no = serial_no;
	}

	public String getReception_time() {
		return reception_time;
	}

	public void setReception_time(String reception_time) {
		this.reception_time = reception_time;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLevel_disp() {
		return level_disp;
	}

	public void setLevel_disp(String level_disp) {
		this.level_disp = level_disp;
	}

	public String getInvolving_disp() {
		return involving_disp;
	}

	public void setInvolving_disp(String involving_disp) {
		this.involving_disp = involving_disp;
	}

	public String getAlarm_comments() {
		return alarm_comments;
	}

	public void setAlarm_comments(String alarm_comments) {
		this.alarm_comments = alarm_comments;
	}

}
