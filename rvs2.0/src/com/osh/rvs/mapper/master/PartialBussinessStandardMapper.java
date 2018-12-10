package com.osh.rvs.mapper.master;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.master.PartialBussinessStandardEntity;

/**
 * 零件出入库工时标准
 *
 * @author liuxb
 *
 */
public interface PartialBussinessStandardMapper {
	public List<PartialBussinessStandardEntity> search();

	public void update(PartialBussinessStandardEntity entity);

	public PartialBussinessStandardEntity getPartialBussinessStandardBySpecKind(@Param("spec_kind") String spec_kind);

}
