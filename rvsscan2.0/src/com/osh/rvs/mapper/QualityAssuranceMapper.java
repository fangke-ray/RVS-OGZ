package com.osh.rvs.mapper;

import java.util.Date;
import java.util.HashMap;

import org.apache.ibatis.annotations.Param;
public interface QualityAssuranceMapper {
	//当日通过数量
	public int getCurrentPassCount();
	//当日不合格数量
	public int getCurrentUnqualifiedCount();
	//取得品保完成件数和不合格的件数
	public HashMap<String, Object> getWorkresult(@Param("start_Date") Date start_Date, @Param("end_Date") Date end_Date);
	// 当前等待处理数量
	public int getCurrentWaitingCount(boolean checked);
}
