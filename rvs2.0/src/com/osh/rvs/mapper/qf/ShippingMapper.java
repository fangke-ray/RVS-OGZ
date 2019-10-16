package com.osh.rvs.mapper.qf;

import java.util.List;

import com.osh.rvs.bean.data.MaterialEntity;

public interface ShippingMapper {

	public List<MaterialEntity> getWaitings(String shipping_position_id);

	public List<MaterialEntity> getFinished(String shipping_position_id);

	public MaterialEntity getMaterialDetail(String material_id);

}
