/**
 * 系统名：OSH-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：维修对象机种系统管理事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action;

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

import com.osh.rvs.service.AcceptFactService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;


public class AcceptFactAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());
	private AcceptFactService service = new AcceptFactService();

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

		log.info("AcceptFactAction.init start");

		Map<String, String> series = service.getSeries(conn);
		req.setAttribute("series1_1", series.get("series1_1"));
		req.setAttribute("series1_2", series.get("series1_2"));
		req.setAttribute("series1_3", series.get("series1_3"));
		req.setAttribute("series1_4", series.get("series1_4"));
		req.setAttribute("series2_1", series.get("series2_1"));
		req.setAttribute("series2_2", series.get("series2_2"));
		req.setAttribute("series2_3", series.get("series2_3"));
		req.setAttribute("series2_4", series.get("series2_4"));
		req.setAttribute("friday", series.get("friday"));
		req.setAttribute("series3_1", series.get("series3_1"));
		req.setAttribute("series3_2", series.get("series3_2"));
		req.setAttribute("series4_1", series.get("series4_1"));
		req.setAttribute("series4_2", series.get("series4_2"));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("AcceptFactAction.init end");
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

		log.info("AcceptFactAction.refresh start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		Map<String, String> series = service.getSeries(conn);
		listResponse.put("series1_1", series.get("series1_1"));
		listResponse.put("series1_2", series.get("series1_2"));
		listResponse.put("series1_3", series.get("series1_3"));
		listResponse.put("series1_4", series.get("series1_4"));
		listResponse.put("series2_1", series.get("series2_1"));
		listResponse.put("series2_2", series.get("series2_2"));
		listResponse.put("series2_3", series.get("series2_3"));
		listResponse.put("series2_4", series.get("series2_4"));
		listResponse.put("series3_1", series.get("series3_1"));
		listResponse.put("series3_2", series.get("series3_2"));
		listResponse.put("series4_1", series.get("series4_1"));
		listResponse.put("series4_2", series.get("series4_2"));

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("AcceptFactAction.refresh end");
	}
}
