package com.osh.rvs.mapper;


public interface CommonMapper {

	/** 取得本连接最后取得的自增ID */
	public String getLastInsertID();

	public String findCommonMemoByKey(String memo_key);

	public int addCommonMemo(String memo_content);

}
