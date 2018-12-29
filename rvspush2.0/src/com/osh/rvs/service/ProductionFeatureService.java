package com.osh.rvs.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;

import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.mapper.push.MaterialMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;

public class ProductionFeatureService {
	public static Logger _log = Logger.getLogger("ProductionFeatureService");

	public MaterialEntity getMaterial(String material_id) {
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();

		SqlSession conn = factory.openSession(TransactionIsolationLevel.READ_COMMITTED);

		MaterialEntity ret = null;

		try {
			MaterialMapper dao = conn.getMapper(MaterialMapper.class);
			ret = dao.getMaterialEntityByKey(material_id);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		} finally {
			if (conn != null) {
				conn.close();
			}
			conn = null;
		}
		return ret;
	}

	public void makeQaOverTime(String string, String string2, String object) {
		// TODO Auto-generated method stub
		
	}

}
