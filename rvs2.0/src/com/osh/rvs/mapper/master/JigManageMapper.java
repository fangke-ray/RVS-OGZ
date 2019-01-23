package com.osh.rvs.mapper.master;

import java.util.Date;
import java.util.List;

import com.osh.rvs.bean.master.JigManageEntity;

public interface JigManageMapper {

	/* 治具管理详细数据 */
	public List<JigManageEntity> searchJigManage(
			JigManageEntity toolsManageEntity);

	/*治具分布详细数据*/
	public List<JigManageEntity> searchJigDistribute(JigManageEntity toolsDistributeEntity);

	/* 修改治具管理详细 */
	public void updateJigManage(JigManageEntity toolsManageEntity);

	/* 插入治具管理数据 */
	public void insertJigManage(JigManageEntity toolsManageEntity);

	/* 删除治具管理 */
	public void deleteJigManage(JigManageEntity toolsManageEntity);

	/* 查询所有的管理编号 */
	public List<String> searchManageCode(JigManageEntity toolsManageEntity);

	/* 查询最大管理编号 */
	public List<String> searchMaxManageCode(JigManageEntity toolsManageEntity);

	public void replace(JigManageEntity toolsManageEntity);

	public JigManageEntity getByKey(String manage_id);

	/* 批量交付 */
	public void deliverJigManage(JigManageEntity toolsManageEntity);

	/** 确认区间内是否发生交付 */
	public Date checkProvideInPeriod(JigManageEntity toolsManageEntity);

	/** 确认区间内是否发生废弃 */
	public Date checkWasteInPeriod(JigManageEntity toolsManageEntity);
	
	public void disband(JigManageEntity toolsManageEntity);

}
