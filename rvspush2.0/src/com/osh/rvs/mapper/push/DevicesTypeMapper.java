package com.osh.rvs.mapper.push;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.DeviceTypeEntity;

public interface DevicesTypeMapper {
	public DeviceTypeEntity getDeviceTypeByID(@Param("device_type_id") String device_type_id);
}
