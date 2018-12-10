package com.osh.rvs.mapper;

import com.osh.rvs.bean.ServiceRepairManageEntity;

public interface ServiceRepairManageMapper {

	public int searchServiceRepair(ServiceRepairManageEntity instance);

	// 查询分析进行中件数
	public int searchCurrentData();

	// 分析等待件数
	public int searchAnalyseWaitting();
}
