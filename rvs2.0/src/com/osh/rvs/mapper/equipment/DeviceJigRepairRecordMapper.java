package com.osh.rvs.mapper.equipment;

import java.util.List;

import com.osh.rvs.bean.equipment.DeviceJigRepairRecordEntity;

public interface DeviceJigRepairRecordMapper {
	public List<DeviceJigRepairRecordEntity> search(DeviceJigRepairRecordEntity entity);

	public int insertRecord(DeviceJigRepairRecordEntity entity);

	public int updateRecord(DeviceJigRepairRecordEntity entity);

	public int insertSubmit(DeviceJigRepairRecordEntity entity);

	public DeviceJigRepairRecordEntity getDetailForRepair(String key);

	public int updateConfirm(DeviceJigRepairRecordEntity entity);

	public String checkWithCheckUnqualifiedRecordKey(String check_unqualified_record_key);

	public List<DeviceJigRepairRecordEntity> getConsumableByKey(String check_unqualified_record_key);

	public int updateConsumable(DeviceJigRepairRecordEntity entity);

	public int deleteConsumable(DeviceJigRepairRecordEntity entity);

	public int insertMaintainer(DeviceJigRepairRecordEntity entity);
}
