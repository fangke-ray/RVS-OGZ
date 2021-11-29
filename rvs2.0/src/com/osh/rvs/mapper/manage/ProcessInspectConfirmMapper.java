package com.osh.rvs.mapper.manage;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.manage.ProcessInspectConfirmEntity;

/**
 * 作业监察确认
 * 
 * @Description
 * @author liuxb
 * @date 2021-11-25 上午9:40:34
 */
public interface ProcessInspectConfirmMapper {
	/**
	 * 查询所有作业监察确认
	 * 
	 * @param process_inspect_key 作业监察KEY
	 * @return
	 * @throws Exception
	 */
	public List<ProcessInspectConfirmEntity> searchAll(@Param("process_inspect_key") String process_inspect_key) throws Exception;

	/**
	 * 查询单个作业监察确认
	 * 
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public ProcessInspectConfirmEntity getProcessInspectConfirmByKey(@Param("process_inspect_key") String process_inspect_key,@Param("process_name") String process_name) throws Exception;

	/**
	 * 新建作业监察确认（经理/部长盖章）
	 * 
	 * @param entity
	 * @throws Exception
	 */
	public void insert(ProcessInspectConfirmEntity entity) throws Exception;

	/**
	 * 更新作业监察确认（经理/部长盖章）
	 * 
	 * @param entity
	 * @throws Exception
	 */
	public void update(ProcessInspectConfirmEntity entity) throws Exception;

	/**
	 * 删除所有作业监察确认
	 * @param process_inspect_key 作业监察KEY
	 * @throws Exception
	 */
	public void deleteConfirmByKey(@Param("process_inspect_key") String process_inspect_key) throws Exception;
	
	/**
	 * 删除单个作业监察确认
	 * 
	 * @param entity
	 * @throws Exception
	 */
	public void deleteConfirmByName(@Param("process_inspect_key") String process_inspect_key,@Param("process_name") String process_name) throws Exception;
}