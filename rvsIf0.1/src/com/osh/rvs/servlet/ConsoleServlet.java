package com.osh.rvs.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;

import com.osh.rvsif.common.CommonMapper;

import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;

public class ConsoleServlet extends HttpServlet {

	private static final long serialVersionUID = -8368010250271594625L;
	public Logger logger = Logger.getLogger("ConsoleServlet");

	protected static JSON json = new JSON();
	static {
		json.setSuppressNull(true);
	}

	public static String nowWarning = new String();

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		Map<String, String> resObj = new HashMap<String, String>();

		String first = req.getParameter("getDb");
		if (first != null) {
			try {
				SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();

				SqlSession conn = factory.openSession(TransactionIsolationLevel.READ_COMMITTED);
				CommonMapper mapper = conn.getMapper(CommonMapper.class);
				mapper.getLastInsertID();
				
				resObj.put("getDb", "OK");
			} catch (Exception e) {
				resObj.put("getDb", "error");
			}
		}
		

		resObj.put("nowWarning", nowWarning);
		
		PrintWriter out;
		try {
			res.setCharacterEncoding("UTF-8");
			out = res.getWriter();
			out.print(json.format(resObj));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}