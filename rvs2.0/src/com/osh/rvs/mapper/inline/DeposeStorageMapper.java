package com.osh.rvs.mapper.inline;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface DeposeStorageMapper {

	public String getMaxStorage(@Param("case_code") String case_code);
	
	public String getNextEmptyStorage(@Param("case_code") String case_code);
	
	public int putIntoStorage(@Param("material_id") String material_id, @Param("case_code") String case_code);
	
	public int removeFromStorage(@Param("material_id") String material_id);
	
	public Map<String, String> getDeposeStorageByCode(@Param("case_code") String case_code);
	/**  */

	public int getDecomStorageCount();
	
}
