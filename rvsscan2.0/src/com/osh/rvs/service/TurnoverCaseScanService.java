package com.osh.rvs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.TurnoverCaseEntity;
import com.osh.rvs.bean.WipEntity;
import com.osh.rvs.mapper.TurnoverCaseMapper;

import framework.huiqing.common.util.CommonStringUtil;

public class TurnoverCaseScanService {

	public List<WipEntity> getWip(SqlSession conn) {
		TurnoverCaseMapper dao = conn.getMapper(TurnoverCaseMapper.class);
		return dao.getTurnoverCase();
	}

	public String getLocationMap(SqlSession conn) {

		TurnoverCaseMapper mapper = conn.getMapper(TurnoverCaseMapper.class);

		StringBuffer retSb = new StringBuffer("<style>.storage-table td.storage-empty {cursor :pointer;}.storage-table td[f_ag]{border-radius:8px;}.storage-table td.wip-storage {outline: 2px solid green;box-shadow : 1px 1px 1px 1px green;}.storage-table td.wip-shipping {outline: 2px solid blue;box-shadow : 1px 1px 1px 1px blue;}</style>");
		retSb.append("<div class=\"ui-widget-content\" style=\"border:0;\">");

		List<TurnoverCaseEntity> storageMap = mapper.getAllStorageMap();

		Map<String, List<TurnoverCaseEntity>> storageByShelf = new TreeMap<String, List<TurnoverCaseEntity>>();

		for (TurnoverCaseEntity storage : storageMap) {
			if (!storageByShelf.containsKey(storage.getShelf())) {
				storageByShelf.put(storage.getShelf(), new ArrayList<TurnoverCaseEntity>());
			}
			storageByShelf.get(storage.getShelf()).add(storage);
		}

		for (String shelf : storageByShelf.keySet()) {
			int curLayer = -999;
			int maxItemCnt = 0;
			int lineCnt = 0;
			StringBuffer shelfSb = new StringBuffer("<tbody>");

			for (TurnoverCaseEntity storage : storageByShelf.get(shelf)) {
				if (storage.getLayer() != curLayer) {
					if (shelfSb.length() > 0) shelfSb.append("</tr>");
					shelfSb.append("<tr>");
					curLayer = storage.getLayer();
					if (lineCnt > maxItemCnt) {
						maxItemCnt = lineCnt;
					}
					lineCnt = 0;
				}
				shelfSb.append("<td class=\"");

				shelfSb.append("\" kind=\"");
				shelfSb.append(storage.getKind());
				if (storage.getFor_agreed() == 1) {
					shelfSb.append("\" f_ag");	
				} else {
					shelfSb.append("\"");
				}
				shelfSb.append(" location=\"");
				String slimLocation = slimLocation(shelf, storage.getLocation());
				shelfSb.append(storage.getLocation());
				if (slimLocation.length() > 2) {
					shelfSb.append("\" style=\"font-size:8px;");
				}
				shelfSb.append("\">");
				shelfSb.append(slimLocation);
				shelfSb.append("</td>");
				lineCnt++;
			}
			if (!storageByShelf.get(shelf).isEmpty()) {
				shelfSb.append("</tr>");
				if (lineCnt > maxItemCnt) {
					maxItemCnt = lineCnt;
				}
			}
			if (maxItemCnt == 0) {
				maxItemCnt = 3;
			}
			retSb.append("<div style=\"margin: 15px 0 0 15px; float: left;\"><div class=\"ui-widget-header\" style=\"width: " + (maxItemCnt * 20.4) + "px; text-align: center;\"> ");
			retSb.append(shelf);
			retSb.append(" 货架</div><table class=\"condform storage-table\" style=\"width: " + (maxItemCnt * 20) + "px;\">");
			retSb.append(shelfSb);
			retSb.append("</table></div>");
		}
		retSb.append("<div class=\"clear\"></div></div>");

		return retSb.toString();
	}

	private String slimLocation(String shelf, String location) {
		if (CommonStringUtil.isEmpty(location)) {
			return "-";
		}
		if (location.startsWith(shelf)) {
			location = location.substring(shelf.length()).trim();
		} else 	if (location.length() > 2) {
			location = location.substring(location.length() - 2);
		}
		return location;
	}
}
