package com.osh.rvs.mapper.master;

import java.util.List;

import com.osh.rvs.bean.master.ProcedureStepCountEntity;

public interface ProcedureStepCountMapper {

	public List<ProcedureStepCountEntity> searchProcedureStepCount(ProcedureStepCountEntity entity);

	public int insertProcedureStepCount(ProcedureStepCountEntity entity);

	public int updateProcedureStepCount(ProcedureStepCountEntity entity);

	public int updateClientAddress(ProcedureStepCountEntity entity);

	public int deleteProcedureStepCount(ProcedureStepCountEntity entity);

	public List<ProcedureStepCountEntity> searchProcedureStepOfModel(ProcedureStepCountEntity entity);

	public int insertProcedureStepOfModel(ProcedureStepCountEntity entity);

	public int updateProcedureStepOfModel(ProcedureStepCountEntity entity);

	public int deleteProcedureStepOfModel(ProcedureStepCountEntity entity);

	public List<ProcedureStepCountEntity> getMaterialCountedInPosition(
			ProcedureStepCountEntity pfEntity);

	public int insertProcedureStepRecord(ProcedureStepCountEntity entity);
}
