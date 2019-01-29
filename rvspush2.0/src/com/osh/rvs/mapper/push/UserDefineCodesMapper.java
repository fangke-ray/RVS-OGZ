package com.osh.rvs.mapper.push;

import org.apache.ibatis.annotations.Param;

/**
 * 
 * @author liuxb
 * 
 */
public interface UserDefineCodesMapper {
	public String getValue(@Param("code") String code);
}
