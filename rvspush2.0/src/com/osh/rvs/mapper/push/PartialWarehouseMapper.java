package com.osh.rvs.mapper.push;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.entity.PartialWarehouseEntity;

public interface PartialWarehouseMapper {
	/**
	 * 每月工作记录
	 */
	public List<PartialWarehouseEntity> searchMonthWorkRecord(@Param("start_date") String start_date);

	/**
	 * 收货标准工时
	 */
	public BigDecimal countReceptStandardTime(@Param("key") String key);

	/**
	 * 拆盒标准工时
	 */
	public BigDecimal countCollectCaseStandardTime(@Param("key") String key);

	/**
	 * 核对+上架标准工时
	 */
	public BigDecimal countCollationAndOnShelfStandardTime(@Param("fact_pf_key") String fact_pf_key);

	/**
	 * 核对标准工时
	 */
	public BigDecimal countCollationStandardTime(@Param("fact_pf_key") String fact_pf_key);

	/**
	 * 分装准工时
	 */
	public BigDecimal countUnPackStandardTime(@Param("fact_pf_key") String fact_pf_key);

	/**
	 * 上架准工时
	 */
	public BigDecimal countOnShelfStandardTime(@Param("fact_pf_key") String fact_pf_key);

	/**
	 * 收货箱数
	 */
	public List<PartialWarehouseEntity> countReceptBox(@Param("key") String key);

	/**
	 * 核对+上架数量
	 */
	public List<PartialWarehouseEntity> countCollectAndOnShelfQuantity(@Param("fact_pf_key") String fact_pf_key);

	/**
	 * 核对数量
	 */
	public List<PartialWarehouseEntity> countCollectQuantity(@Param("fact_pf_key") String fact_pf_key);

	/**
	 * 分装数量、上架数量
	 */
	public List<PartialWarehouseEntity> countUnPackAndOnShelfQuantity(@Param("fact_pf_key") String fact_pf_key);

	/**
	 * 其他
	 */
	public String getComment(@Param("fact_pf_key") String fact_pf_key);

	/**
	 * 每天每种作业类型用时
	 */
	public List<PartialWarehouseEntity> countDailySpendTime(@Param("operatorId") String operatorId,@Param("start_date") String start_date);

	/**
	 * 每天工作记录
	 */
	public List<PartialWarehouseEntity> searchDailyWorkRecord(@Param("operatorId") String operatorId,@Param("production_type") String production_type, @Param("start_date") String start_date);


}
