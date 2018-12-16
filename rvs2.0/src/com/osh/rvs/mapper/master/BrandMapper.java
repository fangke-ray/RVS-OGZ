package com.osh.rvs.mapper.master;

import java.util.List;

import com.osh.rvs.bean.master.BrandEntity;

public interface BrandMapper {

	public List<BrandEntity> searchBrand(BrandEntity brandEntity);

	public int insertBrand(BrandEntity brandEntity);

	public int deleteBrand(BrandEntity brandEntity);

	public int updateBrand(BrandEntity brandEntity);

}
