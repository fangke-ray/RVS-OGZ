package com.osh.rvs.mapper.infect;

import java.util.List;

import com.osh.rvs.bean.infect.JigCheckResultEntity;


public interface JigCheckResultMapper {
	
	/*治具点检结果前半部分详细数据*/
	public List<JigCheckResultEntity> searchToolsCheckResult(JigCheckResultEntity toolsCheckResultEntity);
	
	/*治具点检结果=课室+工程+工位*/
	public JigCheckResultEntity searchSectionLinePosition(JigCheckResultEntity toolsCheckResultEntity);
	
	/*治具点检记录--查询当前月的所有详细*/
	public List<JigCheckResultEntity> searchCheckResult(JigCheckResultEntity toolsCheckResultEntity);
}
