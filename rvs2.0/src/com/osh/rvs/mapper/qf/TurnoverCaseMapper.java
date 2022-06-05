package com.osh.rvs.mapper.qf;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.qf.TurnoverCaseEntity;

public interface TurnoverCaseMapper {

	public List<TurnoverCaseEntity> searchTurnoverCase(TurnoverCaseEntity condition);

	public List<String> getStorageHeaped(@Param("kind")String kind);

	public void putin(TurnoverCaseEntity condition);

	public String getNextEmptyLocation(@Param("location")String location);

	public void checkStorage(@Param("location")String location);

	public void warehousing(@Param("location")String location);

	public List<TurnoverCaseEntity> getStoragePlan();

	public List<TurnoverCaseEntity> getWarehousingPlan();

	public List<TurnoverCaseEntity> getListOnShelf(String shelf);

	public List<String> getAllShelf();

	public TurnoverCaseEntity getEntityByLocation(String location);

	public TurnoverCaseEntity getEntityByLocationForStorage(String location);

	public TurnoverCaseEntity getEntityByLocationForShipping(String location);

	public List<TurnoverCaseEntity> getIdleMaterialList();

	public TurnoverCaseEntity checkEmpty(String location);

	public List<TurnoverCaseEntity> getTrolleyStacks();

	public int removeTrolleyStacks();

	public int insertTrolleyStacks(List<TurnoverCaseEntity> list);

	public void clearTrolleyStacks(String material_id);

	public List<TurnoverCaseEntity> countNowStorageEmpty();

	public List<String> getSpaceInShelf(String shelf);

	public TurnoverCaseEntity getEntityByKey(String key);

	public int create(TurnoverCaseEntity entity);

	public void changeSetting(TurnoverCaseEntity entity);

	public int remove(TurnoverCaseEntity entity);

	public List<TurnoverCaseEntity> getStorageMap(TurnoverCaseEntity entity);

	public List<TurnoverCaseEntity> getStartLocationsOnKindForAgreed();

	public List<String> getNextLocationsOnKindForAgreed(TurnoverCaseEntity entity);

	public String getStorageKindByMaterial(String material_id);
}
