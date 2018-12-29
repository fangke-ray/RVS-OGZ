package com.osh.rvs.mapper.statistics;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.entity.ScheduleHistoryEntity;

public interface ScheduleHistoryMapper {

	/**
	 * 建立最初的计划记录
	 */
	public void setFirstSchedule(Date schedule_date);
	/**
	 * 建立顺延的计划记录
	 */
	public void setPostponeSchedule(Date schedule_date);

	/**
	 * 查询为追朔完毕的日期
	 * @return
	 */
	public List<Date> getUnfinishedDates(Date today);

	/**
	 * 查询为追朔完毕的日期
	 * @return
	 */
	public List<ScheduleHistoryEntity> getScheduleHistory(Date schedule_date);
	public Integer getAtline(Date treatDate);
	public Integer getAgreed(Date treatDate);
	public Integer getInline(Date treatDate);
	public Integer getDelay(Date treatDate);
	public Integer getBo(Date treatDate);
	public Double getLt(Date treatDate);
	public MaterialEntity getNewStatusOfMaterial(String material_id);
	public List<Map<String, Object>> getScheduleSort(Date today);
	public void removeCompleteNow(Date nextDay);

	public List<Map<String, Object>> getCapacity();

	public void updateScheduleSort(ScheduleHistoryEntity entity);
}
