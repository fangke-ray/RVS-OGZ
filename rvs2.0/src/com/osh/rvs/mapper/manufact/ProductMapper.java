package com.osh.rvs.mapper.manufact;

import java.util.List;

import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.inline.WaitingEntity;


public interface ProductMapper {

	public String getLastProductSerialNo(String prefix);

	public List<MaterialEntity> getProductsBySerials(List<String> serialNoList);

	public List<WaitingEntity> getWaitingStartOfSection(String section_id);

}
