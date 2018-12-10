package com.osh.rvs.service.partial;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.form.partial.PartialWarehouseDetailForm;

/**
 * 零件核对
 *
 * @author liuxb
 *
 */
public class PartialCollationService {
	private final PartialWarehouseDetailService partialWarehouseDetailService = new PartialWarehouseDetailService();

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

}
