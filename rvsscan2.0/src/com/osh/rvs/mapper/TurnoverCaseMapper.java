package com.osh.rvs.mapper;

import java.util.List;

import com.osh.rvs.bean.TurnoverCaseEntity;
import com.osh.rvs.bean.WipEntity;

public interface TurnoverCaseMapper {

	List<WipEntity> getTurnoverCase();

	List<TurnoverCaseEntity> getAllStorageMap();

}
