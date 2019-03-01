package com.osh.rvs.mapper.equipment;

import java.util.List;

import com.osh.rvs.bean.equipment.DeviceSpareAdjustEntity;

public interface DeviceSpareAdjustMapper {

	public void insert(DeviceSpareAdjustEntity entity);

	public List<DeviceSpareAdjustEntity> searchAdjustRecord(DeviceSpareAdjustEntity entity);

}
