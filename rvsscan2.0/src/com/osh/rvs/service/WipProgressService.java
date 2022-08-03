package com.osh.rvs.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.WipEntity;
import com.osh.rvs.mapper.WipProgressMapper;

public class WipProgressService {

	public List<WipEntity> getWip(SqlSession conn) {
		WipProgressMapper dao = conn.getMapper(WipProgressMapper.class);
		return dao.getWipMaterials();
	}

	public Object getWipCount(List<WipEntity> list, SqlSession conn) {
		Map<String, Integer> info = new HashMap<String, Integer>();
		int wipOvertimeCount = 0;

		if (list == null) {
			WipProgressMapper mapper = conn.getMapper(WipProgressMapper.class);
			list = mapper.countWipWaiting();
		}

		for (WipEntity wipCase : list) {
			if ("1".equals(wipCase.getWip_overceed())) {
				wipOvertimeCount++;
			}
		}
		info.put("wip_count", list.size());
		info.put("wip_overtime_count", wipOvertimeCount);

		return info;
	}
}
