package com.osh.rvs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.MaterialEntity;

public interface MaterialMapper {
	public List<MaterialEntity> searchScheduled(@Param("scheduled_date") String scheduled_date);
}
