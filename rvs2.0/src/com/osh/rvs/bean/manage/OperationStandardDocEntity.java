package com.osh.rvs.bean.manage;

import java.io.Serializable;

/**
 * 作业基准书
 * 
 * @author liuxb
 * 
 */
public class OperationStandardDocEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2474658780846486861L;

	/**
	 * 维修型号 ID
	 */
	private String model_id;

	/**
	 * 工位 ID
	 */
	private String position_id;

	/**
	 * 文档序列
	 */
	private Integer doc_seq;

	/**
	 * 文档 URL
	 */
	private String doc_url;

	/**
	 * 页码数
	 */
	private Integer page_no;

	/**
	 * 机种 ID
	 */
	private String category_id;

	/**
	 * 机种名称
	 */
	private String category_name;

	/**
	 * 型号名称
	 */
	private String model_name;

	/**
	 * 工程 ID
	 */
	private String line_id;

	/**
	 * 工程名称
	 */
	private String line_name;

	/**
	 * 工位代码
	 */
	private String process_code;

	/**
	 * 工位名称
	 */
	private String position_name;

	/**
	 * 配置文档数
	 */
	private Integer total_doc;

	private String copy_model_id;

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

	public Integer getDoc_seq() {
		return doc_seq;
	}

	public void setDoc_seq(Integer doc_seq) {
		this.doc_seq = doc_seq;
	}

	public String getDoc_url() {
		return doc_url;
	}

	public void setDoc_url(String doc_url) {
		this.doc_url = doc_url;
	}

	public Integer getPage_no() {
		return page_no;
	}

	public void setPage_no(Integer page_no) {
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

	public Integer getTotal_doc() {
		return total_doc;
	}

	public void setTotal_doc(Integer total_doc) {
		this.total_doc = total_doc;
	}

	public String getCopy_model_id() {
		return copy_model_id;
	}

	public void setCopy_model_id(String copy_model_id) {
		this.copy_model_id = copy_model_id;
	}

}
