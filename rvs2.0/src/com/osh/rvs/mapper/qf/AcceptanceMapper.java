package com.osh.rvs.mapper.qf;

import java.util.List;
import java.util.Map;

import com.osh.rvs.bean.data.MaterialEntity;

public interface AcceptanceMapper {

	public void insertMaterial(MaterialEntity entity);
	
	public void updateMaterial(MaterialEntity entity);
	
	public String checkSorcNo(MaterialEntity entity);
	
	public String checkModelSerialNo(MaterialEntity entity);

	public List<MaterialEntity> getTodayMaterialDetail();

	public void updateFormalReception(String[] material_ids) throws Exception;

	public void updatePastOgzShipped(Map<String, String> map) throws Exception;

	public void updateOcmShippingBySorc(MaterialEntity cond) throws Exception;

	public void updateOcmShippingByID(MaterialEntity cond) throws Exception;

	public List<MaterialEntity> getAllAgreedDate();

	public void updateAgreedDate(MaterialEntity entity) throws Exception;

	public void changeSorc(List<String> ids) throws Exception;
}
