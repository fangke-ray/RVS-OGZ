package com.osh.rvs.form.master;

import java.sql.Timestamp;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;

public class BrandForm extends ActionForm{

	private static final long serialVersionUID = -1407422751137288929L;

	@BeanField(title = "厂商信息 ID", name = "brand_id", primaryKey = true, length = 11)
	private String brand_id;

	@BeanField(title = "厂商名称", name = "name", length = 100, type = FieldType.String)
	private String name;

	@BeanField(title = "业务关系", name = "business_relationship", type = FieldType.Integer, length = 1)
	private Integer business_relationship;

	@BeanField(title = "地址", name = "address", length = 150, type = FieldType.String)
	private String address;

	@BeanField(title = "邮箱", name = "email", length = 45, type = FieldType.String)
	private String email;

	@BeanField(title = "联系电话", name = "tel", length = 21, type = FieldType.String)
	private String tel;

	@BeanField(title = "联系人", name = "contacts", length = 10, type = FieldType.String)
	private String contacts;

	/** 删除标记 */
	private boolean delete_flg = false;
	/** 最后更新人 */
	private String updated_by;
	/** 最后更新时间 */
	private Timestamp updated_time;
	public String getBrand_id() {
		return brand_id;
	}
	public void setBrand_id(String brand_id) {
		this.brand_id = brand_id;
	}
	public Integer getBusiness_relationship() {
		return business_relationship;
	}
	public void setBusiness_relationship(Integer business_relationship) {
		this.business_relationship = business_relationship;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getContacts() {
		return contacts;
	}
	public void setContacts(String contacts) {
		this.contacts = contacts;
	}
	public boolean isDelete_flg() {
		return delete_flg;
	}
	public void setDelete_flg(boolean delete_flg) {
		this.delete_flg = delete_flg;
	}
	public String getUpdated_by() {
		return updated_by;
	}
	public void setUpdated_by(String updated_by) {
		this.updated_by = updated_by;
	}
	public Timestamp getUpdated_time() {
		return updated_time;
	}
	public void setUpdated_time(Timestamp updated_time) {
		this.updated_time = updated_time;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
