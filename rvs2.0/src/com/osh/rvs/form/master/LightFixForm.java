package com.osh.rvs.form.master;

import java.io.Serializable;
import java.util.List;

import org.apache.struts.action.ActionForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.AutofillArrayList;

/**
 * 
 * @Title LightFixForm.java
 * @Project rvs
 * @Package com.osh.rvs.form.master
 * @ClassName: CustomerForm
 * @Description: 中小修理标准管理Form
 * @author gonglm
 * @date 2022-6-21 上午11:32:20
 */
public class LightFixForm extends ActionForm implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3180566089037365399L;

	@BeanField(title = "中小修理标准编制 ID", name = "light_fix_id", type = FieldType.String, length = 11, notNull = true, primaryKey = true)
	private String light_fix_id;

	@BeanField(title = "修理代码", name = "activity_code", type = FieldType.String, length = 4, notNull = true)
	private String activity_code;

	@BeanField(title = "名称", name = "description", type = FieldType.String, length = 64, notNull = true)
	private String description;

	@BeanField(title = "修理等级", name = "rank", type = FieldType.String, length = 10)
	private String rank;

	@BeanField(title = "最后更新人", name = "updated_by", type = FieldType.String, length = 11)
	private String updated_by;

	@BeanField(title = "最后更新时间", name = "updated_time", type = FieldType.TimeStamp)
	private String updated_time;

	/** 机种分类 */
	private List<String> category_list = new AutofillArrayList<String>(String.class);
	/** 工位 ID */
	private List<String> position_list = new AutofillArrayList<String>(String.class);

	@BeanField(title = "维修对象 ID", name = "material_id", type = FieldType.String, length = 11)
	private String material_id;
	
	@BeanField(title = "工位 ID", name = "position_id", type = FieldType.String, length = 11)
	private String position_id;

	@BeanField(title = "关联参考流程", name = "correlated_pat_id", type = FieldType.String, length = 11)
	private String correlated_pat_id;

	@BeanField(title = "关联参考等级", name = "correlated_level", type = FieldType.Integer, length = 1)
	private String correlated_level;

	@BeanField(title = "机种", name = "category_id", type = FieldType.String, length = 11)
	private String category_id;

	@BeanField(title = "机种文字", name = "categories", type = FieldType.String)
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
	public String getUpdated_time() {
		return updated_time;
	}

	/**
	 * @param updated_time
	 *            the updated_time to set
	 */
	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
	}

	public String getMaterial_id() {
		return material_id;
	}

	public void setMaterial_id(String material_id) {
		this.material_id = material_id;
	}

	/**
	 * @return the position_list
	 */
	public List<String> getPosition_list() {
		return position_list;
	}

	/**
	 * @param position_list
	 *            the position_list to set
	 */
	public void setPosition_list(List<String> position_list) {
		this.position_list = position_list;
	}

	public String getPosition_id() {
		return position_id;
	}

	public void setPosition_id(String position_id) {
		this.position_id = position_id;
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

	public List<String> getCategory_list() {
		return category_list;
	}

	public void setCategory_list(List<String> category_list) {
		this.category_list = category_list;
	}

	public String getCorrelated_level() {
		return correlated_level;
	}

	public void setCorrelated_level(String correlated_level) {
		this.correlated_level = correlated_level;
	}

	public String getCategories() {
		return categories;
	}

	public void setCategories(String categories) {
		this.categories = categories;
	}
	
}
