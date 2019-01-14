package com.osh.rvs.mapper.push;

import java.util.List;
import java.util.Map;

import com.osh.rvs.entity.PeriodsEntity;

public interface InfectMapper {

	/**
	 * 查询当前条件下,应点检未点检的治具
	 * @return
	 */
	public List<Map<String,Object>> getExpiredTools(PeriodsEntity entity);

	public List<Map<String,Object>> getExpiredDevices(Map<String,Object> map);

	public List<Map<String, Object>> getExpiredExternals();

}
