package com.osh.rvs.service.partial;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.partial.FactProductionFeatureEntity;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.mapper.partial.FactProductionFeatureMapper;
import com.osh.rvs.service.PartialBussinessStandardService;

import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 零件核对
 *
 * @author liuxb
 *
 */
public class PartialCollationService {
	private final PartialWarehouseDetailService partialWarehouseDetailService = new PartialWarehouseDetailService();
	private final PartialBussinessStandardService partialBussinessStandardService = new PartialBussinessStandardService();

	/**
	 * 过滤核对的数据
	 *
	 * @param key 入库单KEY
	 * @param productionType 作业类型
	 * @param conn
	 * @return
	 */
	public List<PartialWarehouseDetailForm> filterCollation(String key, String productionType, SqlSession conn) {
		// 当前作业单中所有零件
		List<PartialWarehouseDetailForm> list = partialWarehouseDetailService.searchByKey(key, conn);

		List<PartialWarehouseDetailForm> respList = filterCollation(list, productionType);

		return respList;
	}

	/**
	 * 过滤核对的数据
	 *
	 * @param list 数据集
	 * @param productionType 作业类型
	 * @return
	 */
	public List<PartialWarehouseDetailForm> filterCollation(List<PartialWarehouseDetailForm> list, String productionType) {
		List<PartialWarehouseDetailForm> respList = new ArrayList<PartialWarehouseDetailForm>();
		// 过滤核对的数据
		for (PartialWarehouseDetailForm form : list) {
			// 上架
			Integer onShelf = Integer.valueOf(form.getOn_shelf());

			// 【B1：核对+上架】
			if ("20".equals(productionType)) {
				if (onShelf < 0)
					respList.add(form);
			} else if ("21".equals(productionType)) {// 【B2：核对】
				if (onShelf > 0)
					respList.add(form);
			}
		}

		return respList;
	}

	/**
	 * 作业标准时间
	 *
	 * @param list
	 * @param conn
	 * @return
	 */
	public String getStandardTime(List<PartialWarehouseDetailForm> list, SqlSession conn) {
		Map<Integer, BigDecimal> map = partialBussinessStandardService.getCollationStandardTime(conn);

		// 总时间
		BigDecimal totalTime = new BigDecimal("0");

		for (PartialWarehouseDetailForm form : list) {
			Integer specKind = Integer.valueOf(form.getSpec_kind());

			// 标准工时
			BigDecimal time = map.get(specKind);

			totalTime = totalTime.add(time);
		}

		// 向上取整
		totalTime = totalTime.setScale(0, RoundingMode.UP);
		return totalTime.toString();
	}

	/**
	 * 作业经过时间
	 *
	 * @param time
	 * @return
	 */
	public String getSpentTimes(FactProductionFeatureForm form, SqlSession conn) {
		FactProductionFeatureMapper factProductionFeatureMapper = conn.getMapper(FactProductionFeatureMapper.class);
		FactProductionFeatureEntity entity = new FactProductionFeatureEntity();

		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		entity.setProduction_type(null);
		entity.setProduction_types(new Integer[] { 20, 21 });
		// 相差毫秒数
		long millisecond = 0;

		List<FactProductionFeatureEntity> list = factProductionFeatureMapper.searchWorkRecord(entity);

		for (int i = 0; i < list.size(); i++) {
			entity = list.get(i);

			if (entity.getFinish_time() == null) {
				Calendar cal = Calendar.getInstance();
				millisecond += cal.getTimeInMillis() - entity.getAction_time().getTime();
			} else {
				millisecond += entity.getFinish_time().getTime() - entity.getAction_time().getTime();
			}
		}

		BigDecimal diff = new BigDecimal(millisecond);
		// 1分钟
		BigDecimal oneMinute = new BigDecimal(60000);

		BigDecimal spent = diff.divide(oneMinute, RoundingMode.UP);

		return spent.toString();

	}
}
