/**
 * 系统名：OSH-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：维修对象机种系统管理事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.WipEntity;
import com.osh.rvs.service.WipProgressService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;


public class WipProgressAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());
	private WipProgressService service = new WipProgressService();

	/**
	 * 菜单初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("WipProgressAction.init start");

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("WipProgressAction.init end");
	}

	/**
	 * 菜单初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void refresh(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("WipProgressAction.refresh start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		listResponse.put("list", service.getWip(conn));

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("WipProgressAction.refresh end");
	}

	/**
	 * WIP总数表示
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void total(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("WipProgressAction.total start");

		String callback = req.getParameter("callback");
		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

//		List<WipEntity> list = service.getWip(conn);

		listResponse.put("ogz", service.getWipCount(null, conn));

		// 返回Json格式响应信息
		if (callback != null) {
			returnJsonpResponse(res, listResponse);
		} else {
			returnJsonResponse(res, listResponse);
		}

		log.info("WipProgressAction.total end");
	}

	/**
	 * 写一个JSONP格式的反响
	 */
	protected void returnJsonpResponse(HttpServletResponse response, Object result) {
		PrintWriter out;
		try {
			response.setCharacterEncoding("UTF-8");
			out = response.getWriter();
			out.print("callback(" + json.format(result) + ")");
			out.flush();
		} catch (IOException e) {
		}
	}

}
