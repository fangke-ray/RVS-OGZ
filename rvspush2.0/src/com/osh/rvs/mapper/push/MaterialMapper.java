package com.osh.rvs.mapper.push;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.entity.MaterialRemainTimeEntity;

public interface MaterialMapper {

	/**
	 * 维修对象详细信息，工位等待区用
	 * @param ids 符合所有查询条件的ID集合
	 * @return
	 */
	public MaterialEntity getMaterialDetail(String material_id);

	/**
	 * 查询修理完成工程超期（包括今天）的维修对象
	 * @return
	 */
	public List<MaterialEntity> getTodayDelayMaterials();

	/**
	 * 查询修理完成工程接近超期（包括明天）的维修对象
	 * @return
	 */
	public List<MaterialEntity> getCloserDelayMaterials();

	public MaterialEntity getMaterialEntityByKey(String material_id);
	public MaterialEntity getMaterialNamedEntityByKey(String material_id);

	public int countWipAgreed(@Param("start_date") String start_date, @Param("end_date") String end_date,
			@Param("section_id") String section_id);

	/**
	 * 同意两天内未投线的维修对象
	 * @return
	 */
	public List<MaterialEntity> getMaterialInlineLater(Integer kind);

	public BigDecimal getCountRecieptInPeriod(@Param("start_date") String start_date, @Param("end_date") String end_date,  @Param("isDirect") String isDirect);

	public List<MaterialEntity> getCloserDelayByPartialMaterials();

	/**
	 * 当日未完成计划自动顺延
	 * @param nextDay 
	 * @param today 
	 * @return
	 */
	public int setSchedulePostpone(@Param("today") Date today, @Param("nextDay") Date nextDay);

	/**
	 * 取得次日到达的零件
	 * @return
	 */
	public List<MaterialEntity> getPartialReachNextDay();

	/***/
	public MaterialEntity getMaterialEntityByModelAndSerial(@Param("model_name") String model, @Param("serial_no") String serial, 
			@Param("sorc_no") String sorc_no, @Param("rc_mailsend_date") String rc_mailsend_date);
	
	/**
	 * 某个工位的指定日期完成维修对象的
	 * @return 
	 */
	public List<MaterialEntity> searchShipping(@Param("finish_time") String finish_time);
	public List<MaterialEntity> searchInlineMaterial(@Param("inline_time")String inlinedate);
	public String getTwoDaysOfLines(String material_id);	

	public int clearCompletedMaterialRemainTime();

	public void deleteRemainTime();

	public List<MaterialEntity> getInlineMaterials();

	public void insertRemainTime(MaterialRemainTimeEntity mrtEntity);
	
	/**
	 * 修理翻修品清点状况记录
	 * @return
	 */
	public List<MaterialEntity> unRepairAdjust();

	public void sendToBLine(String material_id);
	
	public Date getMinFinishTime(String date);
	
	//先端回收记录
	public List<MaterialEntity> searchAdvancedRecovery(String date);
	/**
	 * 待投线的维修对象
	 * @return
	 */
	public List<MaterialEntity> getInlinePlan();
	public int deleteInlinePlan();
	public int createInlinePlan(MaterialEntity entity);

	//受理消毒灭菌
	public List<MaterialEntity> searchAcceptDisinfectSterilize(@Param("finish_time") String finish_time);
}
