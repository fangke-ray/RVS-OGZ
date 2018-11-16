package com.osh.rvs.form.master;

import java.util.HashMap;
import java.util.List;

import com.osh.rvs.form.UploadForm;

import framework.huiqing.bean.annotation.BeanField;
import framework.huiqing.bean.annotation.FieldType;
import framework.huiqing.common.util.AutofillArrayList;

public class QualityTipForm extends UploadForm {

	private static final long serialVersionUID = 2413471756814818982L;

	/** 质量提示 ID */
	@BeanField(title = "质量提示 ID", name = "quality_tip_id", primaryKey = true, length = 11, notNull = true)
	private String quality_tip_id;
	/** 标题 */
	@BeanField(title = "标题", name = "title", type = FieldType.String, length = 45)
	private String title;
	/** 绑定对象 */
	@BeanField(title = "绑定对象", name = "bind_name", type = FieldType.String)
	private String bind_name;
	/** 工位 ID */
	@BeanField(title = "工位 ID", name = "position_id", type = FieldType.String, length = 11)
	private String position_id;
	/** 工位名称 */
	@BeanField(title = "工位名称", name = "position_name", type = FieldType.String, length = 15)
	private String position_name;
	/** 维修对象机种  */
	@BeanField(title = "维修对象机种", name = "category_id", type = FieldType.String, length = 11)
	private String category_id;
	/** 维修对象型号  */
	@BeanField(title = "维修对象型号", name = "model_id", type = FieldType.String, length = 11)
	private String model_id;
	/** 最后更新人 */
	@BeanField(title = "更新者", name = "updated_by", type = FieldType.String)
	private String updated_by;
	/** 最后更新时间 */
	@BeanField(title = "更新时间", name = "updated_time", type = FieldType.TimeStamp)
	private String updated_time;

	private String bind_type;

	private List<String> categorys = new AutofillArrayList<String>(String.class);
	private List<String> models = new AutofillArrayList<String>(String.class);
	private List<HashMap> modelBeans = new AutofillArrayList<HashMap>(HashMap.class);

	private String photo_file_name;

	public String getQuality_tip_id() {
		return quality_tip_id;
	}
	public void setQuality_tip_id(String quality_tip_id) {
		this.quality_tip_id = quality_tip_id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBind_name() {
		return bind_name;
	}
	public void setBind_name(String bind_name) {
		this.bind_name = bind_name;
	}
	public String getPosition_id() {
		return position_id;
	}
	public void setPosition_id(String position_id) {
		this.position_id = position_id;
	}
	public String getPosition_name() {
		return position_name;
	}
	public void setPosition_name(String position_name) {
		this.position_name = position_name;
	}
	public String getCategory_id() {
		return category_id;
	}
	public void setCategory_id(String category_id) {
		this.category_id = category_id;
	}
	public String getModel_id() {
		return model_id;
	}
	public void setModel_id(String model_id) {
		this.model_id = model_id;
	}
	public String getUpdated_by() {
		return updated_by;
	}
	public void setUpdated_by(String updated_by) {
		this.updated_by = updated_by;
	}
	public String getUpdated_time() {
		return updated_time;
	}
	public void setUpdated_time(String updated_time) {
		this.updated_time = updated_time;
	}
	public List<String> getCategorys() {
		return categorys;
	}
	public void setCategorys(List<String> categorys) {
		this.categorys = categorys;
	}
	public List<String> getModels() {
		return models;
	}
	public void setModels(List<String> models) {
		this.models = models;
	}
	public List<HashMap> getModelBeans() {
		return modelBeans;
	}
	public void setModelBeans(List<HashMap> modelBeans) {
		this.modelBeans = modelBeans;
	}
	public String getPhoto_file_name() {
		return photo_file_name;
	}
	public void setPhoto_file_name(String photo_file_name) {
		this.photo_file_name = photo_file_name;
	}
	public String getBind_type() {
		return bind_type;
	}
	public void setBind_type(String bind_type) {
		this.bind_type = bind_type;
	}
}
