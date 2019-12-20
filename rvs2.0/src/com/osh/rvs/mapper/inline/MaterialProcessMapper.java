package com.osh.rvs.mapper.inline;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.inline.MaterialProcessEntity;

public interface MaterialProcessMapper {

	public MaterialProcessEntity loadMaterialProcess(String id);
	
	public void updateMaterialProcess(MaterialProcessEntity entity) throws Exception;
	public void finishMaterialProcess(MaterialProcessEntity entity) throws Exception;
	
	public void insertMaterialProcess(MaterialProcessEntity entity) throws Exception;

	public void undoLineComplete(MaterialProcessEntity entity) throws Exception;

	public void removeByBreak(String material_id);

	public MaterialProcessEntity loadMaterialProcessOfLine(@Param("material_id")String material_id, @Param("line_id")String line_id);
	
	//查询维修对象进展工程
	public List<String> searchMaterialProcessLineByMaterialId(String material_id);

	public void assignReworkTrigger(Map<String, Object> cond);

	/**
	 * 切换平行线位
	 * @param material_id
	 */
	public int updatePx(@Param("material_id") String material_id,@Param("line_id") String line_id);

	public List<MaterialProcessEntity> loadMaterialProcessLines(String id);

	public String getMaterialProcessLine(@Param("material_id") String material_id, @Param("in_advance") String in_advance);

	public void removeMaterialProcessLine(@Param("material_id")String material_id, @Param("line_id")String line_id);
}
