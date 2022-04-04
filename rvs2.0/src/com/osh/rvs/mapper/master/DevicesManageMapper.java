package com.osh.rvs.mapper.master;

import java.util.List;

import com.osh.rvs.bean.master.DeviceTypeEntity;
import com.osh.rvs.bean.master.DevicesManageEntity;
import com.osh.rvs.bean.master.PositionEntity;


public interface DevicesManageMapper {
	
	/*设备工具管理详细数据*/
   public List<DevicesManageEntity> searchDeviceManage(DevicesManageEntity devicesManageEntity);
   /* 设备工具分布一览 */
   public List<DevicesManageEntity> searchDistribute(DevicesManageEntity devicesManageEntity);
    /*修改设备工具管理详细*/
   public void updateDevicesManage(DevicesManageEntity devicesManageEntity);
   
   /*插入设备工具管理数据*/
   public void insertDevicesManage(DevicesManageEntity devicesManageEntity);
   
   /*删除设备工具管理*/
   public void deleteDevicesManage(DevicesManageEntity devicesManageEntity);
   
   /*查询所有的设备工具管理编号*/
   public List<String> searchManageCode(DevicesManageEntity devicesManageEntity);
   
   /*查询最大管理编号*/
   public List<String> searchMaxManageCode(DevicesManageEntity devicesManageEntity);
   
   /*替换新品*/
   public void replaceDevicesManage(DevicesManageEntity devicesManageEntity);

   /*批量交付*/
   public void deliverDevicesManage(DevicesManageEntity conditionEntity);
   
   /** 按主键查询所有的设备工具*/
   public DevicesManageEntity getByKey(String manage_id);

   /** 简单替换 */
   public int exchange(DevicesManageEntity dme);

   /** 简单废弃 */
   public int disband(DevicesManageEntity dme);

   public List<DevicesManageEntity> getManageCode(DevicesManageEntity devicesManageEntity);

   public List<DeviceTypeEntity> getDeviceTypeOfPosition(DevicesManageEntity dme);

	public List<DevicesManageEntity> getAllManageCode();

	public int insertDeviceManageRecord(DevicesManageEntity dme);

	public List<DevicesManageEntity> getDeviceManageRecordInPeriod(DevicesManageEntity devicesManageEntity);
	public List<PositionEntity> getRemainPosition();
}