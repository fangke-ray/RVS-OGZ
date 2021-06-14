package com.osh.rvs.mapper.manage;

import java.util.List;

import com.osh.rvs.bean.manage.DefectiveAnalysisEntity;
import com.osh.rvs.bean.manage.DefectiveAnalysisPartialEntity;
import com.osh.rvs.bean.manage.DefectiveAnalysisPhotoEntity;
import com.osh.rvs.bean.manage.DefectiveAnalysisQaEntity;
import com.osh.rvs.bean.manage.DefectiveAnalysisSearchEntity;

public interface DefectiveAnalysisMapper {

	List<DefectiveAnalysisSearchEntity> search(DefectiveAnalysisSearchEntity entity);

	DefectiveAnalysisSearchEntity findOneById(String alarmMessageId);

	List<DefectiveAnalysisPhotoEntity> findPhotoById(String alarmMessageId);

	Integer insert(DefectiveAnalysisEntity entity);

	Integer insertPartial(DefectiveAnalysisPartialEntity entity);

	Integer insertQa(DefectiveAnalysisQaEntity entity);

	Integer insertPhoto(DefectiveAnalysisPhotoEntity entity);

	Integer update(DefectiveAnalysisEntity entity);

	Integer updatePartial(DefectiveAnalysisPartialEntity entity);

	Integer updateQa(DefectiveAnalysisQaEntity entity);

	int deletePhoto (DefectiveAnalysisPhotoEntity entity);

	Integer updatePhoto(DefectiveAnalysisPhotoEntity entity);

	Integer count(String alarmMessageId);

	/** 更新不良提出 */
	Integer updateSponsor(DefectiveAnalysisEntity entity);

	/** 更新不良提出确认 */
	Integer updatePhenomenonConfirmer(DefectiveAnalysisEntity entity);

	/** 更新原因分析 */
	Integer updateCauseAnalysis(DefectiveAnalysisEntity entity);

	/** 更新原因分析确认 */
	Integer updateCauseConfirmer(DefectiveAnalysisEntity entity);

	/** 更新对策立案 */
	Integer updateCmFiler(DefectiveAnalysisEntity entity);

	/** 更新对策立案确认 */
	Integer updateCmConfirmer(DefectiveAnalysisEntity entity);

	/** 更新对策实施 */
	Integer updateCmProcessor(DefectiveAnalysisEntity entity);

	/** 更新对策实施确认 */
	Integer updateCmProcConfirmer(DefectiveAnalysisEntity entity);

	/** 更新对策效果 */
	Integer updateCmEffectVerifier(DefectiveAnalysisEntity entity);

	/** 更新对策效果确认 */
	Integer updateCmEffectConfirmer(DefectiveAnalysisEntity entity);

	/** 更新委托关闭判断 */
	Integer updateClosingJudger(DefectiveAnalysisQaEntity entity);

	/** 更新委托关闭判断确认 */
	Integer updateClosingConfirmer(DefectiveAnalysisQaEntity entity);

	/** g更新 对策进度 */
	Integer updateStep(DefectiveAnalysisEntity entity);

	public String getMaxManageCode(DefectiveAnalysisEntity entity);

	public String checkDuplicateManageCode(String manage_code);
}

