package com.osh.rvs.mapper.master;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.master.QualityTipEntity;

public interface QualityTipMapper {
	/** search*/
	public List<QualityTipEntity> search(QualityTipEntity entity);

	/** insert single */
	public void insertQualityTip(QualityTipEntity entity) throws Exception;

	public QualityTipEntity getQualityTipByID(String quality_tip_id);

	public List<QualityTipEntity> getQualityTipBindByID(String quality_tip_id);

	public void updateQualityTip(QualityTipEntity entity) throws Exception;

	public void deleteQualityTip(String quality_tip_id) throws Exception;

	public void insertQualityTipBind(QualityTipEntity entity) throws Exception;

	public void deleteQualityTipBind(String quality_tip_id) throws Exception;

	public QualityTipEntity getQualityTipOfMaterialAtPosition(@Param("material_id") String material_id, @Param("position_id") String position_id);
}
