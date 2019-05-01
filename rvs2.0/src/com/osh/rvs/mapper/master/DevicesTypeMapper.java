package com.osh.rvs.mapper.master;

import java.util.List;

import com.osh.rvs.bean.master.DeviceTypeEntity;

public interface DevicesTypeMapper {
	/*设备工具品名详细*/
	public List<DeviceTypeEntity> searchDeviceType(DeviceTypeEntity devicesTypeEntity);	
	
	/*新建设备工具品名*/
	public void insertDevicesType(DeviceTypeEntity devicesTypeEntity);
	
	/*删除设备工具品名*/
	public void deleteDevicesType(DeviceTypeEntity devicesTypeEntity);
	
	/*修改设备工具品名*/
	public void updateDevicesType(DeviceTypeEntity devicesTypeEntity);
	
	/*查询所有设备品名--referChooser*/
	public List<DeviceTypeEntity> getAllDeviceName();

	public void insertHazardousCaution(DeviceTypeEntity devicesTypeEntity);

	public void removeHazardousCautionById(String id);
}
