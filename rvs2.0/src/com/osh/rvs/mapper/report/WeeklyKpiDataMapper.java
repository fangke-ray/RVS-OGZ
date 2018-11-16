package com.osh.rvs.mapper.report;

import java.util.List;

import com.osh.rvs.bean.report.WeeklyKpiDataEntity;

public interface WeeklyKpiDataMapper {
	public List<WeeklyKpiDataEntity> searchAll();

	public List<WeeklyKpiDataEntity> searchDetails(WeeklyKpiDataEntity condition);

	public void update(WeeklyKpiDataEntity condition);
	
	public int checkCountDateEndExist(WeeklyKpiDataEntity condition);
}
