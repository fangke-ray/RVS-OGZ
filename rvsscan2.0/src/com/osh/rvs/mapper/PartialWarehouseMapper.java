package com.osh.rvs.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.osh.rvs.bean.PartialWarehouseEntity;

public interface PartialWarehouseMapper {
	public List<Map<String, Object>> getOperatorFeatures(@Param("operator_id") String operator_id);

	/**
	 * 查询正在进行中的作业
	 *
	 * @return
	 */
	public PartialWarehouseEntity searchUnFinish(@Param("operator_id") String operator_id);

	/**
	 * 零件出入库收货工时标准
	 *
	 * @return
	 */
	public BigDecimal searchReceptStandardTime(PartialWarehouseEntity entity);

	/**
	 * 零件出入库工时标准
	 *
	 * @return
	 */
	public BigDecimal searchStandardTime(PartialWarehouseEntity entity);

	/**
	 * 统计作业时间(分钟)(isNow=1:当前正在做的总计用时，isNow=2:当日已经完成作业用时，isNow=3:当前正在作业用时)
	 *
	 * @return
	 */
	public Integer searchSpentMins(PartialWarehouseEntity entity);

	/**
	 * 当日收货数量
	 *
	 * @param entity
	 * @return
	 */
	public Integer searchCurrentReceptQuantity(@Param("operator_id") String operator_id);

	/**
	 * 当日核对数量
	 *
	 * @param entity
	 * @return
	 */
	public Integer searchCurrentCollationQuantity(PartialWarehouseEntity entity);

	/**
	 * 当日分装/上架/出库数量
	 *
	 * @param entity
	 * @return
	 */
	public Integer searchQuantity(PartialWarehouseEntity entity);

	/**
	 * 待处理单
	 *
	 * @return
	 */
	public List<PartialWarehouseEntity> waittingProcess();

	/**
	 * 出库数量
	 *
	 * @param entity
	 * @return
	 */
	public Integer countOutLineQuantity(PartialWarehouseEntity entity);

	/**
	 * 出库待处理单
	 *
	 * @return
	 */
	public List<PartialWarehouseEntity> waittingOutLine();

}
