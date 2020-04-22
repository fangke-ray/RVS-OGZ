package com.osh.rvs.mapper.manufact;

import java.util.Date;
import java.util.List;

import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.inline.WaitingEntity;
import com.osh.rvs.bean.manage.DailyProductPlanEntity;


public interface ProductMapper {

	public String getLastProductSerialNo(String prefix);
	public String getLastProductSerialNoWhenClear(String prefix);

	public List<MaterialEntity> getProductsBySerials(List<String> serialNoList);

	public List<WaitingEntity> getWaitingStartOfSection(String section_id);

	public List<DailyProductPlanEntity> getDailyProductPlans();

	public int insertPlan(DailyProductPlanEntity entity);

	public int deletePlanOfDate(Date plan_date);

}
