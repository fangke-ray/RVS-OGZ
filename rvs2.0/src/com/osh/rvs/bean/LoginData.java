package com.osh.rvs.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.bean.master.SectionEntity;

import framework.huiqing.bean.BaseUserBean;
import framework.huiqing.bean.annotation.BeanField;

public class LoginData extends BaseUserBean {

//	private static final long serialVersionUID = 4302708914726757298L;
	private static final long serialVersionUID = -8853970306301323751L;

	private String operator_id;
	private String line_id;
	private String line_name;
	private String section_id;
	private String section_name;
	@BeanField(title = "工号", name = "job_no", notNull=true, length = 8)
	private String job_no;
	private String position_id;
	private String position_name;
	private String process_code;
	private List<PositionEntity> positions = new ArrayList<PositionEntity>();
	private List<SectionEntity> sections = new ArrayList<SectionEntity>();
	private List<LineEntity> lines = new ArrayList<LineEntity>();
	private String last_link;
	private String working_role_id;
	private Integer department;

	private Date pwd_date;

	// 流水线修理方式 全部=0 A线=1 B线=2
	private String px = "0";

	public String getLine_id() {
		return line_id;
	}
	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}
	public String getLine_name() {
		return line_name;
	}
	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}
	public String getSection_id() {
		return section_id;
	}
	public void setSection_id(String section_id) {
		this.section_id = section_id;
	}
	public String getSection_name() {
		return section_name;
	}
	public void setSection_name(String section_name) {
		this.section_name = section_name;
	}
	public String getJob_no() {
		return job_no;
	}
	public void setJob_no(String job_no) {
		this.job_no = job_no;
	}
	public String getOperator_id() {
		return operator_id;
	}
	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}
	public String getPosition_id() {
		return position_id;
	}
	public String getPosition_name() {
		return position_name;
	}
	public void setPosition_name(String position_name) {
		this.position_name = position_name;
	}
	public String getProcess_code() {
		return process_code;
	}
	public void setProcess_code(String process_code) {
		this.process_code = process_code;
	}
	public void setPosition_id(String position_id) {
		this.position_id = position_id;
	}
	public List<PositionEntity> getPositions() {
		return positions;
	}
	public void setPositions(List<PositionEntity> positions) {
		this.positions = positions;
	}
	public String getLast_link() {
		return last_link;
	}
	public void setLast_link(String last_link) {
		this.last_link = last_link;
	}
	public List<SectionEntity> getSections() {
		return sections;
	}
	public void setSections(List<SectionEntity> sections) {
		this.sections = sections;
	}
	public List<LineEntity> getLines() {
		return lines;
	}
	public void setLines(List<LineEntity> lines) {
		this.lines = lines;
	}

	public String toString() {
		return this.job_no + "  " + this.getName() + " " + this.getRole_name() + " " + this.department;
	}
	public String getWorking_role_id() {
		return working_role_id;
	}
	public void setWorking_role_id(String working_role_id) {
		this.working_role_id = working_role_id;
	}
	public String getPx() {
		return px;
	}
	public void setPx(String px) {
		this.px = px;
	}
	public Integer getDepartment() {
		return department;
	}
	public void setDepartment(Integer department) {
		this.department = department;
	}
	public Date getPwd_date() {
		return pwd_date;
	}
	public void setPwd_date(Date pwd_date) {
		this.pwd_date = pwd_date;
	}
}
