package com.osh.rvs.mapper.master;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface ModelMapper {

	public String getModelByName(String model_name); 
	public String getModelByItemCode(String item_code);

	public List<String> checkModelByName(@Param("model_name") String model_name, @Param("model_id") String model_id);
}
