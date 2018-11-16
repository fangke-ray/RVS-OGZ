package com.osh.rvs.mapper.partial;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.partial.MaterialPartialEntity;

public interface MaterialPartialMapper {
	public MaterialPartialEntity loadMaterialPartial(MaterialPartialEntity entity);
	public MaterialPartialEntity loadMaterialPartialGroup(MaterialPartialEntity entity);
	
	public void updateMaterialPartial(MaterialPartialEntity entity) throws Exception;
	public void updateMaterialPartialFromFile(MaterialPartialEntity entity) throws Exception;
	
	public void createMaterialPartialAtOrderPosition(MaterialPartialEntity entity) throws Exception;

	public List<MaterialPartialEntity> searchMaterial(MaterialPartialEntity entity);
	public List<MaterialPartialEntity> searchMaterialReport(MaterialPartialEntity entity);

	public List<MaterialPartialEntity> searchMaterialByKey(String id);
	
	public MaterialPartialEntity getMaterialByKey(@Param("material_id") String id, @Param("occur_times") String occur_times);

	public MaterialPartialEntity getMaterialByMaterialId(@Param("material_id") String id);

	public List<String> getOccurTimesById(String id);

	public void updateReachDateBySorc(Map<String, Object> paramMap) throws Exception;
	
	public Integer getTotalBo();
	public Double getTodayBoRate(@Param("from") Date from, @Param("to") Date to);
	public Double get3daysBoRate(@Param("from") Date from, @Param("to") Date to);
	
	/**
	 * 更新零件BO
	 * @param entity
	 */
	public void updateBoFlg(MaterialPartialEntity entity);
	
	/**
	 * 更新零件BO和零件订购日期
	 * @param entity
	 */
	public void updateOrderDate(MaterialPartialEntity entity);

	/**
	 * 更新零件BO和零件订购日期
	 * @param entity
	 */
	public void updateBoFlgAndOrderDate(MaterialPartialEntity entity);
	
	/**
	 * 零件签收对象一览
	 * @param entity
	 * @return
	 */
	public List<MaterialPartialEntity> searchMaterialPartialRecept(MaterialPartialEntity entity);
	
	/**
	 *  取得所有没有投线，也没有建立过维修对象零件订购单的维修对象
	 * @return
	 */
	public List<MaterialPartialEntity> searchNotOrderMaterail();
	
	/**
	 * 新建维修对象订购单
	 * @param entity
	 */
	public void insertMaterialPartial(MaterialPartialEntity entity);
	
	/**
	 * 根据维修对象ID查询维修对象所有订购单
	 * @param material_id
	 * @return
	 */
	public List<MaterialPartialEntity> searchMaterialPartialById(String material_id);
	public List<MaterialPartialEntity> getAllWorkingMaterialPartail();

}
