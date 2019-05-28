package com.osh.rvs.mapper.push;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.AlarmMesssageEntity;
import com.osh.rvs.entity.AlarmMesssageSendationEntity;

public interface AlarmMesssageMapper {

	public void createAlarmMessage(AlarmMesssageEntity entity) throws Exception;

	public void createAlarmMessageSendation(AlarmMesssageSendationEntity sendation) throws Exception;
	
	public void insertAlarmMessageSendation(AlarmMesssageSendationEntity sendation)throws Exception;

	public AlarmMesssageEntity getBreakAlarmMessage(@Param("material_id") String material_id, @Param("position_id") String position_id);
	public AlarmMesssageEntity getBreakAlarmMessageByKey(@Param("alarm_messsage_id") String alarm_messsage_id);

	public List<AlarmMesssageEntity> getBreakAlarmMessages(@Param("material_id") String material_id);
	public List<AlarmMesssageSendationEntity> getBreakAlarmMessageSendation(@Param("alarm_messsage_id") String alarm_messsage_id);
	public AlarmMesssageSendationEntity getBreakAlarmMessageBySendation(@Param("alarm_messsage_id") String alarm_messsage_id, @Param("sendation_id") String sendation_id);

	public int updateAlarmMessageSendation(AlarmMesssageSendationEntity sendation) throws Exception;
	public int countAlarmMessageSendation(AlarmMesssageSendationEntity sendation);

	public int countAlarmMessageOfSendation(String operator_id);
	public List<AlarmMesssageEntity> getAlarmMessageBySendation(String operator_id);
	public String getBreakLevelByMaterialId(@Param("material_id") String material_id, @Param("position_id") String position_id);

	public void updateLevel(AlarmMesssageEntity entity) throws Exception;

	public AlarmMesssageEntity getBreakPushedAlarmMessage(String material_id);
	
	public int countBreakUnPushedAlarmMessage(String material_id);

	public int countOverflowUnresolvedAlarmMessage(@Param("section_id") String section_id, @Param("position_id") String position_id);

	public List<AlarmMesssageEntity> searchAlarmMessages(AlarmMesssageEntity entity);

	public boolean isFixed(String alarm_messsage_id);

	public int countAlarmMessageIntimeArea(@Param("reason") Integer warningReason, @Param("occur_time_start") Date occur_time_start, @Param("occur_time_end") Date occur_time_end);
	
	public List<AlarmMesssageEntity> searchAlarmMessageSend(@Param("reason") Integer reason,@Param("operator_id") String operator_id,@Param("occur_time_start") Date occur_time_start, @Param("occur_time_end") Date occur_time_end);
}
