package com.osh.rvs.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;

public class TriggerServlet extends HttpServlet {

	private static final long serialVersionUID = -3163557381361759907L;

	Logger log = Logger.getLogger("TriggerServlet");

	/** 更新配置文件 **/
	private static final String METHOD_UPDATE_PROPERTIES = "prop";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse arg1) throws ServletException, IOException {
		String uri = req.getRequestURI();
		uri = uri.replaceFirst(req.getContextPath(), "");
		uri = uri.replaceFirst(req.getServletPath(), "");
		String addr = req.getRemoteAddr();
		log.info("Get finger on :" + uri + " from " + addr);

		// 只有本机可以访问
		if (!"0:0:0:0:0:0:0:1".equals(addr) && !"127.0.0.1".equals(addr)) {
			log.warn("推送只限服务器本机触发");
			return;
		}
		String[] parameters = uri.split("\\/");
		if (parameters.length > 1) {
			String method = parameters[1];
//			String target = "";
//			String object = "";
//			if (parameters.length > 2) {
//				target = parameters[2];
//				if (parameters.length > 3) {
//					object = parameters[3];
//				}
//			}

			if (METHOD_UPDATE_PROPERTIES.equals(method)){
				PathConsts.load();
				RvsUtils.initAll();
			}
		}
	}
}