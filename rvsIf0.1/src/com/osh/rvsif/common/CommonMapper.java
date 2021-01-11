package com.osh.rvsif.common;

import java.util.Date;
import java.util.Map;


public interface CommonMapper {

	/** 取得本连接最后取得的自增ID */
	public String getLastInsertID();

	public Map<String, Object> selectMaterialByOmrNotifiNoForSchedule(String omr_notifi_no);
	
	/** 假日计算 */
	public Date addWorkdays(Map<String, Object> cond) throws Exception;

}
