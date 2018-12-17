package com.osh.rvs.bean.master;

import java.io.Serializable;
import java.sql.Timestamp;

public class BrandEntity implements Serializable{

	private static final long serialVersionUID = 6313881495565715875L;

	private String brand_id;

	private String name;
	// 业务关系
	private Integer business_relationship;

	// 地址
	private String address;
	// 邮箱
	private String email;
	// 联系电话
	private String tel;
	// 联系人
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
