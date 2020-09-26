package com.osh.rvs.service;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.WipEntity;
import com.osh.rvs.mapper.TurnoverCaseMapper;

public class TurnoverCaseScanService {

	public List<WipEntity> getWip(SqlSession conn) {
		TurnoverCaseMapper dao = conn.getMapper(TurnoverCaseMapper.class);
		return dao.getTurnoverCase();
	}
}
