package com.osh.rvs.form.manage;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

public class OperationStandardDocForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -450110006690723706L;

	@BeanField(title = "维修型号 ID", name = "model_id", type = FieldType.String, length = 11, notNull = true)
	private String model_id;

	@BeanField(title = "工位 ID", name = "position_id", type = FieldType.String, length = 11, notNull = true)
	private String position_id;

	@BeanField(title = "文档序列", name = "doc_seq", type = FieldType.Integer, length = 2, notNull = true)
	private String doc_seq;

	@BeanField(title = "文档 URL", name = "doc_url", type = FieldType.String, length = 512, notNull = true)
	private String doc_url;

	@BeanField(title = "页码数", name = "page_no", type = FieldType.UInteger, length = 3, notNull = true)
	private String page_no;

	@BeanField(title = "机种 ID", name = "category_id", type = FieldType.String, length = 11)
	private String category_id;

	@BeanField(title = "机种名称", name = "category_name", type = FieldType.String)
	private String category_name;

	@BeanField(title = "型号名称", name = "model_name", type = FieldType.String)
	private String model_name;

	@BeanField(title = "工程 ID", name = "line_id", type = FieldType.String, length = 11)
	private String line_id;

	@BeanField(title = "工程名称", name = "line_name", type = FieldType.String)
	private String line_name;

	@BeanField(title = "工位代码", name = "process_code", type = FieldType.String)
	private String process_code;

	@BeanField(title = "工位名称", name = "position_name", type = FieldType.String)
	private String position_name;

	@BeanField(title = "配置文档数", name = "total_doc", type = FieldType.Integer)
	private String total_doc;

	@BeanField(title = "维修型号 ID", name = "copy_model_id", type = FieldType.String, length = 11)
	private String copy_model_id;

	private String copy_model_name;

	private String flg;

	public String getModel_id() {
		return model_id;
	}

	public void setModel_id(String model_id) {
		this.model_id = model_id;
	}

	public String getPosition_id() {
		return position_id;
	}

	public void setPosition_id(String position_id) {
		this.position_id = position_id;
	}

	public String getDoc_seq() {
		return doc_seq;
	}

	public void setDoc_seq(String doc_seq) {
		this.doc_seq = doc_seq;
	}

	public String getDoc_url() {
		return doc_url;
	}

	public void setDoc_url(String doc_url) {
		this.doc_url = doc_url;
	}

	public String getPage_no() {
		return page_no;
	}

	public void setPage_no(String page_no) {
		this.page_no = page_no;
	}

	public String getCategory_id() {
		return category_id;
	}

	public void setCategory_id(String category_id) {
		this.category_id = category_id;
	}

	public String getCategory_name() {
		return category_name;
	}

	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}

	public String getModel_name() {
		return model_name;
	}

	public void setModel_name(String model_name) {
		this.model_name = model_name;
	}

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

	public String getProcess_code() {
		return process_code;
	}

	public void setProcess_code(String process_code) {
		this.process_code = process_code;
	}

	public String getPosition_name() {
		return position_name;
	}

	public void setPosition_name(String position_name) {
		this.position_name = position_name;
	}

	public String getTotal_doc() {
		return total_doc;
	}

	public void setTotal_doc(String total_doc) {
		this.total_doc = total_doc;
	}

	public String getCopy_model_id() {
		return copy_model_id;
	}

	public void setCopy_model_id(String copy_model_id) {
		this.copy_model_id = copy_model_id;
	}

	public String getCopy_model_name() {
		return copy_model_name;
	}

	public void setCopy_model_name(String copy_model_name) {
		this.copy_model_name = copy_model_name;
	}

	public String getFlg() {
		return flg;
	}

	public void setFlg(String flg) {
		this.flg = flg;
	}

}
