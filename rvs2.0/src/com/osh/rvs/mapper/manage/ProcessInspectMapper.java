package com.osh.rvs.mapper.manage;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.manage.ProcessInspectAchievementEntity;
import com.osh.rvs.bean.manage.ProcessInspectSearchEntity;
import com.osh.rvs.bean.manage.ProcessInspectSummaryEntity;

public interface ProcessInspectMapper {

	List<ProcessInspectSearchEntity> search(ProcessInspectSearchEntity entity);

	Integer countAchievementType(String processInspectKey);

	ProcessInspectSummaryEntity findSummaryByKey(String processInspectKey);

	List<ProcessInspectAchievementEntity> findAchievementByKey(String processInspectKey);

	Integer insertSummary(ProcessInspectSummaryEntity entity);

	Integer insertAchievement(ProcessInspectAchievementEntity entity);

	Integer deleteSummary(String processInspectKey);

	Integer deleteAchievementByKey(String processInspectKey);

	Integer deleteAchievementByName(@Param("processInspectKey") String processInspectKey, @Param("processName") String processName);
}

