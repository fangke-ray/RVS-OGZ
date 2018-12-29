package com.osh.rvs.mapper.push;

import java.util.Date;
import java.util.List;

import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.entity.ServiceRepairManageEntity;

public interface ServiceRepairManageMapper {

	/** 确认保内QIS管理件数 **/
	public long checkExistsByKey(ServiceRepairManageEntity condition);

	/** 新建保内QIS数据 **/
	public Integer insertServiceRepairManage(ServiceRepairManageEntity insertance)throws Exception;

	/** 新建保内QIS-旧维修对象数据 **/
	public Integer insertServiceRepairPastMaterial(ServiceRepairManageEntity insertance) throws Exception;

	/** 保内QIS型号名字集合 **/
	public List<String> getModelNameAutoCompletes();
	
	/**无QA判定日的数据中最早的一个QA受理日**/
	public Date getMinReceptionTime();
	
	/**保内QIS管理一览**/
	public List<ServiceRepairManageEntity> searchServiceRepair(ServiceRepairManageEntity instance);
	
	/**保内QIS管理等级集合**/
	public List<String> getRankAutoCompletes();
	
	/**维修对象对象集合，集合长度=0 or >1返回null,集合长度=1返回一个对象**/
	public List<MaterialEntity> getRecept(ServiceRepairManageEntity instance);
	
	/**查询住键是否存在**/
	public List<ServiceRepairManageEntity> getPrimaryKey(ServiceRepairManageEntity instance);
	
	public List<MaterialEntity> getMaterialIds(ServiceRepairManageEntity instance);
	
	/**更新service_repair_manage表**/
	public void updateServiceRepairManage(ServiceRepairManageEntity entity);
	
	/**删除QIS请款信息**/
	public void deleteQisPayout(ServiceRepairManageEntity entity);
	
	/**更新QIS请款信息**/
	public void updateQisPayout(ServiceRepairManageEntity entity);
	
	/**获取service_repair_manage表中最大material_id**/
	public String getMaxMaterialId(String typeChar);
}
