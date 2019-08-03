package com.osh.rvs.mapper.statistics;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.CheckedFileStorageEntity;

public interface InfectMapper {

	/**
	 * 查询单独归档的月设备
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getSingleOfMonth(
			@Param("monthStart") Date monthStart,
			@Param("monthEnd") Date monthEnd);

	/**
	 * 查询按工位归档的月设备
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getOnPositionOfMonth(
			@Param("monthStart") Date monthStart,
			@Param("monthEnd") Date monthEnd);

	/**
	 * 查询按工程归档的月设备
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getOnLineOfMonth(
			@Param("monthStart") Date monthStart,
			@Param("monthEnd") Date monthEnd);

	public List<Map<String, Object>> getExpiredExternals();

	public int recordFileData(CheckedFileStorageEntity checked_file_storage);

	/**
	 * 查询单独归档的月设备
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getSingleOfPeriod(
			@Param("periodStart") Date periodStart,
			@Param("periodEnd") Date periodEnd);

	/**
	 * 查询按工位归档的月设备
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getOnPositionOfPeriod(
			@Param("periodStart") Date periodStart,
			@Param("periodEnd") Date periodEnd);

	/**
	 * 查询按工程归档的月设备
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getOnLineOfPeriod(
			@Param("periodStart") Date periodStart,
			@Param("periodEnd") Date periodEnd);	

	/**
	 * 查询归档的治具
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getJig(
			@Param("periodStart") Date periodStart,
			@Param("periodEnd") Date periodEnd);

	public void removeCheckStatusWait();
}
