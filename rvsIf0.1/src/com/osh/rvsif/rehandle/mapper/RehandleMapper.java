package com.osh.rvsif.rehandle.mapper;

import org.apache.ibatis.annotations.Param;

import com.osh.rvsif.rehandle.bean.RehandleEntity;

public interface RehandleMapper {

	/** search kind by key */
	public String searchKindByKey(String if_sap_message_key);

	/** search content by key */
	public RehandleEntity searchContentByKey(
			@Param("if_sap_message_key") String if_sap_message_key,
			@Param("seq") String seq);

	public void updateContent(RehandleEntity entity) throws Exception;

}
