package com.osh.rvs.mapper.push;

import java.util.Date;

public interface PartialMapper {

	public int setBoOfMaterialPartialDetail() throws Exception;
	public int setBoOfMaterialPartial(Date date) throws Exception;
	public int setBoResolvedOfMaterialPartial(Date date) throws Exception;
	public int setNoBoOfMaterialPartial(Date date) throws Exception;
}