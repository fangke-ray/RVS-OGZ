package com.osh.rvs.bean.manage;

import java.io.Serializable;
import java.util.Date;

public class DefectiveAnalysisEntity implements Serializable{

	private static final long serialVersionUID = -7913620955362179253L;

	private String alarm_message_id;

	private Integer defective_type;

	private String manage_code;

	private Integer step;

	private Integer sponsor_operator_id;

	private Date sponsor_date;

	private String defective_phenomenon;

	private Integer responsibility_of_line;

	private Integer phenomenon_confirmer_id;

	private Date phenomenon_confirmer_date;

	private String responsibility_of_ptl;

	private String cause_analysis;

	private Integer cause_analyst_id;

	private Date cause_analyst_date;

	private Integer cause_confirmer_id;

	private Date cause_confirmer_date;

	private Integer capa_frequency;

	private Integer capa_major;

	private Integer capa_risk;

	private String append_part_order;

	private Integer partial_applyier_id;

	private Integer rework_proceed;

	private String countermeasures;

	private Integer cm_filer_id;

	private Date cm_filer_date;

	private Integer cm_confirmer_id;

	private Date cm_confirmer_date;

	private Integer cm_processor_id;

	private Date cm_processor_date;

	private Integer cm_proc_confirmer_id;

	private Date cm_proc_confirmer_date;

	private String countermeasure_effects;

	private Integer cm_effect_verifier_id;

	private Date cm_effect_verifier_date;

	private Integer cm_effect_confirmer_id;

	private Date cm_effect_confirmer_date;

	public String getAlarm_message_id() {
		return alarm_message_id;
	}

	public void setAlarm_message_id(String alarm_message_id) {
		this.alarm_message_id = alarm_message_id;
	}

	public Integer getDefective_type() {
		return defective_type;
	}

	public void setDefective_type(Integer defective_type) {
		this.defective_type = defective_type;
	}

	public String getManage_code() {
		return manage_code;
	}

	public void setManage_code(String manage_code) {
		this.manage_code = manage_code;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	public Integer getSponsor_operator_id() {
		return sponsor_operator_id;
	}

	public void setSponsor_operator_id(Integer sponsor_operator_id) {
		this.sponsor_operator_id = sponsor_operator_id;
	}

	public Date getSponsor_date() {
		return sponsor_date;
	}

	public void setSponsor_date(Date sponsor_date) {
		this.sponsor_date = sponsor_date;
	}

	public String getDefective_phenomenon() {
		return defective_phenomenon;
	}

	public void setDefective_phenomenon(String defective_phenomenon) {
		this.defective_phenomenon = defective_phenomenon;
	}

	public Integer getResponsibility_of_line() {
		return responsibility_of_line;
	}

	public void setResponsibility_of_line(Integer responsibility_of_line) {
		this.responsibility_of_line = responsibility_of_line;
	}

	public Integer getPhenomenon_confirmer_id() {
		return phenomenon_confirmer_id;
	}

	public void setPhenomenon_confirmer_id(Integer phenomenon_confirmer_id) {
		this.phenomenon_confirmer_id = phenomenon_confirmer_id;
	}

	public Date getPhenomenon_confirmer_date() {
		return phenomenon_confirmer_date;
	}

	public void setPhenomenon_confirmer_date(Date phenomenon_confirmer_date) {
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

	public Integer getCause_analyst_id() {
		return cause_analyst_id;
	}

	public void setCause_analyst_id(Integer cause_analyst_id) {
		this.cause_analyst_id = cause_analyst_id;
	}

	public Date getCause_analyst_date() {
		return cause_analyst_date;
	}

	public void setCause_analyst_date(Date cause_analyst_date) {
		this.cause_analyst_date = cause_analyst_date;
	}

	public Integer getCause_confirmer_id() {
		return cause_confirmer_id;
	}

	public void setCause_confirmer_id(Integer cause_confirmer_id) {
		this.cause_confirmer_id = cause_confirmer_id;
	}

	public Date getCause_confirmer_date() {
		return cause_confirmer_date;
	}

	public void setCause_confirmer_date(Date cause_confirmer_date) {
		this.cause_confirmer_date = cause_confirmer_date;
	}

	public Integer getCapa_frequency() {
		return capa_frequency;
	}

	public void setCapa_frequency(Integer capa_frequency) {
		this.capa_frequency = capa_frequency;
	}

	public Integer getCapa_major() {
		return capa_major;
	}

	public void setCapa_major(Integer capa_major) {
		this.capa_major = capa_major;
	}

	public Integer getCapa_risk() {
		return capa_risk;
	}

	public void setCapa_risk(Integer capa_risk) {
		this.capa_risk = capa_risk;
	}

	public String getAppend_part_order() {
		return append_part_order;
	}

	public void setAppend_part_order(String append_part_order) {
		this.append_part_order = append_part_order;
	}

	public Integer getPartial_applyier_id() {
		return partial_applyier_id;
	}

	public void setPartial_applyier_id(Integer partial_applyier_id) {
		this.partial_applyier_id = partial_applyier_id;
	}

	public Integer getRework_proceed() {
		return rework_proceed;
	}

	public void setRework_proceed(Integer rework_proceed) {
		this.rework_proceed = rework_proceed;
	}

	public String getCountermeasures() {
		return countermeasures;
	}

	public void setCountermeasures(String countermeasures) {
		this.countermeasures = countermeasures;
	}

	public Integer getCm_filer_id() {
		return cm_filer_id;
	}

	public void setCm_filer_id(Integer cm_filer_id) {
		this.cm_filer_id = cm_filer_id;
	}

	public Date getCm_filer_date() {
		return cm_filer_date;
	}

	public void setCm_filer_date(Date cm_filer_date) {
		this.cm_filer_date = cm_filer_date;
	}

	public Integer getCm_confirmer_id() {
		return cm_confirmer_id;
	}

	public void setCm_confirmer_id(Integer cm_confirmer_id) {
		this.cm_confirmer_id = cm_confirmer_id;
	}

	public Date getCm_confirmer_date() {
		return cm_confirmer_date;
	}

	public void setCm_confirmer_date(Date cm_confirmer_date) {
		this.cm_confirmer_date = cm_confirmer_date;
	}

	public Integer getCm_processor_id() {
		return cm_processor_id;
	}

	public void setCm_processor_id(Integer cm_processor_id) {
		this.cm_processor_id = cm_processor_id;
	}

	public Date getCm_processor_date() {
		return cm_processor_date;
	}

	public void setCm_processor_date(Date cm_processor_date) {
		this.cm_processor_date = cm_processor_date;
	}

	public Integer getCm_proc_confirmer_id() {
		return cm_proc_confirmer_id;
	}

	public void setCm_proc_confirmer_id(Integer cm_proc_confirmer_id) {
		this.cm_proc_confirmer_id = cm_proc_confirmer_id;
	}

	public Date getCm_proc_confirmer_date() {
		return cm_proc_confirmer_date;
	}

	public void setCm_proc_confirmer_date(Date cm_proc_confirmer_date) {
		this.cm_proc_confirmer_date = cm_proc_confirmer_date;
	}

	public String getCountermeasure_effects() {
		return countermeasure_effects;
	}

	public void setCountermeasure_effects(String countermeasure_effects) {
		this.countermeasure_effects = countermeasure_effects;
	}

	public Integer getCm_effect_verifier_id() {
		return cm_effect_verifier_id;
	}

	public void setCm_effect_verifier_id(Integer cm_effect_verifier_id) {
		this.cm_effect_verifier_id = cm_effect_verifier_id;
	}

	public Date getCm_effect_verifier_date() {
		return cm_effect_verifier_date;
	}

	public void setCm_effect_verifier_date(Date cm_effect_verifier_date) {
		this.cm_effect_verifier_date = cm_effect_verifier_date;
	}

	public Integer getCm_effect_confirmer_id() {
		return cm_effect_confirmer_id;
	}

	public void setCm_effect_confirmer_id(Integer cm_effect_confirmer_id) {
		this.cm_effect_confirmer_id = cm_effect_confirmer_id;
	}

	public Date getCm_effect_confirmer_date() {
		return cm_effect_confirmer_date;
	}

	public void setCm_effect_confirmer_date(Date cm_effect_confirmer_date) {
		this.cm_effect_confirmer_date = cm_effect_confirmer_date;
	}


}
