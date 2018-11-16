package com.osh.rvs.bean.master;

import java.io.Serializable;
import java.sql.Timestamp;

public class QualityTipEntity implements Serializable {

	private static final long serialVersionUID = 5905460644296104860L;

	/** 质量提示 ID */
	private String quality_tip_id;
	/** 标题 */
	private String title;
	/** 工位ID */
	private String position_id;
	/** 工位名称 */
	private String position_name;
	/** 绑定方式 */
	private Integer bind_type;
	/** 绑定对象 */
	private String bind_id;
	/** 绑定对象 */
	private String bind_name;
	/** 维修对象机种 ID */
	private String category_id;
	/** 维修对象机种名称 */
	private String category_name;
	/** 维修对象型号 ID */
	private String model_id;
	/** 最后更新人 */
	private String updated_by;
	/** 最后更新时间 */
	private Timestamp updated_time;

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
	public Integer getBind_type() {
		return bind_type;
	}
	public void setBind_type(Integer bind_type) {
		this.bind_type = bind_type;
	}
	public String getBind_id() {
		return bind_id;
	}
	public void setBind_id(String bind_id) {
		this.bind_id = bind_id;
	}
	public String getBind_name() {
		return bind_name;
	}
	public void setBind_name(String bind_name) {
		this.bind_name = bind_name;
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
	public Timestamp getUpdated_time() {
		return updated_time;
	}
	public void setUpdated_time(Timestamp updated_time) {
		this.updated_time = updated_time;
	}
}
