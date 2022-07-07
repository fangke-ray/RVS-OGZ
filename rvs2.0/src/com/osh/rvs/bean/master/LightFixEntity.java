package com.osh.rvs.bean.master;

import java.io.Serializable;
import java.sql.Timestamp;

public class LightFixEntity implements Serializable {

	private static final long serialVersionUID = -7010444264736469308L;

	/** 中小修理标准编制 ID */
	private String light_fix_id;
	/** 修理代码 */
	private String activity_code;
	/** 名称 */
	private String description;
	/** 修理等级 */
	private String rank;
	/** 机种 ID */
	private String category_id;
	/** 工位 ID */
	private String position_id;
	/** 最后更新人 */
	private String updated_by;
	/** 最后更新时间 */
	private Timestamp updated_time;

	private String material_id;

	private String correlated_pat_id;

	private Integer correlated_level;

	private String categories;

	/**
	 * @return the light_fix_id
	 */
	public String getLight_fix_id() {
		return light_fix_id;
	}

	/**
	 * @param light_fix_id
	 *            the light_fix_id to set
	 */
	public void setLight_fix_id(String light_fix_id) {
		this.light_fix_id = light_fix_id;
	}

	/**
	 * @return the activity_code
	 */
	public String getActivity_code() {
		return activity_code;
	}

	/**
	 * @param activity_code
	 *            the activity_code to set
	 */
	public void setActivity_code(String activity_code) {
		this.activity_code = activity_code;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the rank
	 */
	public String getRank() {
		return rank;
	}

	/**
	 * @param rank
	 *            the rank to set
	 */
	public void setRank(String rank) {
		this.rank = rank;
	}

	/**
	 * @return the position_id
	 */
	public String getPosition_id() {
		return position_id;
	}

	/**
	 * @param position_id
	 *            the position_id to set
	 */
	public void setPosition_id(String position_id) {
		this.position_id = position_id;
	}

	/**
	 * @return the updated_by
	 */
	public String getUpdated_by() {
		return updated_by;
	}

	/**
	 * @param updated_by
	 *            the updated_by to set
	 */
	public void setUpdated_by(String updated_by) {
		this.updated_by = updated_by;
	}

	/**
	 * @return the updated_time
	 */
	public Timestamp getUpdated_time() {
		return updated_time;
	}

	/**
	 * @param updated_time
	 *            the updated_time to set
	 */
	public void setUpdated_time(Timestamp updated_time) {
		this.updated_time = updated_time;
	}

	public String getMaterial_id() {
		return material_id;
	}

	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}

	public String getCorrelated_pat_id() {
		return correlated_pat_id;
	}

	public void setCorrelated_pat_id(String correlated_pat_id) {
		this.correlated_pat_id = correlated_pat_id;
	}

	public String getCategory_id() {
		return category_id;
	}

	public void setCategory_id(String category_id) {
		this.category_id = category_id;
	}

	public Integer getCorrelated_level() {
		return correlated_level;
	}

	public void setCorrelated_level(Integer correlated_level) {
		this.correlated_level = correlated_level;
	}

	public String getCategories() {
		return categories;
	}

	public void setCategories(String categories) {
		this.categories = categories;
	}

}
