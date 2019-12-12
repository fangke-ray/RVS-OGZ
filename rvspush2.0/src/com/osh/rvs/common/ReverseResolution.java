package com.osh.rvs.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;

import com.osh.rvs.mapper.master.ModelMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;

public class ReverseResolution {
	protected static final Logger logger = Logger.getLogger("Production");

	public static Map<String, String> modelRever = new HashMap<String, String>();
	public static Map<String, String> itemCodeRever = new HashMap<String, String>();

	public static String getModelByName(String model_name, SqlSession conn) {
		boolean ownConn = false;
		if (conn == null) {
			conn = getTempConn();
			ownConn = true;
		}
		if (!modelRever.containsKey(model_name)) {
			ModelMapper dao = conn.getMapper(ModelMapper.class);

			List<String> lResult = dao.checkModelByName(model_name, null);
			String model_id = null; 
			if (lResult.size() > 0) {
				model_id = lResult.get(0);
			} else {
				model_id = dao.getModelByName(RvsUtils.regfy(model_name));
			}

			if (model_id != null) {
				modelRever.put(model_name, model_id);
			}
		}
		if (ownConn) {
			logger.info("Connnection close");
			conn.close();
			conn = null;
		}
		if (modelRever.get(model_name) != null) {
			return modelRever.get(model_name);
		} else {
			return getModelByItemCode(model_name, conn);
		}
	}

	public static String getModelByItemCode(String model_name, SqlSession conn) {
		boolean ownConn = false;
		if (conn == null) {
			conn = getTempConn();
			ownConn = true;
		}
		if (!itemCodeRever.containsKey(model_name)) {
			ModelMapper dao = conn.getMapper(ModelMapper.class);
			String model_id = dao.getModelByItemCode(model_name);

			if (model_id != null) {
				itemCodeRever.put(model_name, model_id);
			}
		}
		if (ownConn) {
			logger.info("Connnection close");
			conn.close();
			conn = null;
		}
		return itemCodeRever.get(model_name);
	}

	public static SqlSession getTempConn() {
		logger.info("new Connnection");
		@SuppressWarnings("static-access")
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();
		return factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
	}
}
