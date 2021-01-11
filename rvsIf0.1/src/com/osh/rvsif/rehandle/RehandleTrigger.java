package com.osh.rvsif.rehandle;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;

import com.osh.rvsif.rehandle.bean.RehandleEntity;
import com.osh.rvsif.rehandle.mapper.RehandleMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;

public class RehandleTrigger extends HttpServlet {

	private static final long serialVersionUID = 5399315020296629748L;
	Logger _logger = Logger.getLogger(getClass());

	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		String uri = req.getRequestURI();
		uri = uri.replaceFirst(req.getContextPath(), "");
		uri = uri.replaceFirst(req.getServletPath(), "");
		String addr = req.getRemoteAddr();
		_logger.info("Get finger on :" + uri + " from " + addr);

		// 只有本机可以访问
		if (!"0:0:0:0:0:0:0:1".equals(addr) && !"127.0.0.1".equals(addr)) {
			_logger.warn("推送只限服务器本机触发");
			return;
		}

		String[] parameters = uri.split("\\/");
		if (parameters.length > 1) {
			String if_sap_message_key = parameters[1];
			String seq = parameters[2];

			@SuppressWarnings("static-access")
			SqlSessionFactory factory = SqlSessionFactorySingletonHolder
					.getInstance().getFactory();
			SqlSessionManager conn = SqlSessionManager.newInstance(factory);
			conn.startManagedSession(ExecutorType.BATCH,
					TransactionIsolationLevel.REPEATABLE_READ);

			try {
				RehandleMapper mapper = conn.getMapper(RehandleMapper.class);
				RehandleEntity entity = mapper.searchContentByKey(if_sap_message_key, seq);
				if (entity == null || entity.getIf_sap_message_key() == null) {
					_logger.warn("Content not set for " + if_sap_message_key);
					return;
				}

				// 画面区分取得
				String kind = mapper.searchKindByKey(if_sap_message_key);
				if ("recept".equals(kind)) {
					
				} else if ("approve".equals(kind)) {
					
				} else if ("part_order".equals(kind)) {	
					
				}

				if (conn != null && conn.isManagedSessionStarted()) {
					conn.commit();
				}

			} catch (Exception e) {
				e.printStackTrace();
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.rollback();
				}
				_logger.error(this.getClass().getName() + " Exception:", e);
			} finally {
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.close();
				}
				conn = null;
			}
		}
	}
}
