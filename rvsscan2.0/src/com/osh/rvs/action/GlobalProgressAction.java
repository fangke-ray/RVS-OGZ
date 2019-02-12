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

import com.osh.rvs.service.GlobalProgressService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;


public class GlobalProgressAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());
	private GlobalProgressService service = new GlobalProgressService();

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
		log.info("GlobalProgressAction.init start");

		Map<String, String> series = service.getSeries(conn);
		req.setAttribute("wipOnlineRepairUp", series.get("wipOnlineRepairUp"));
		req.setAttribute("wipOnlineRepairDown", series.get("wipOnlineRepairDown"));
		req.setAttribute("serie11", series.get("serie11"));
		req.setAttribute("serie101", series.get("serie101"));
//		req.setAttribute("serie12", series.get("serie12"));
		req.setAttribute("serie13", series.get("serie13"));
		req.setAttribute("serie14", series.get("serie14"));
		req.setAttribute("serie15", series.get("serie15"));
		req.setAttribute("serie16", series.get("serie16"));
		req.setAttribute("serie17", series.get("serie17"));
		req.setAttribute("serie21", series.get("serie21"));
		req.setAttribute("serie22", series.get("serie22"));
		req.setAttribute("serie23", series.get("serie23"));
		req.setAttribute("serie24", series.get("serie24"));
		req.setAttribute("serie25", series.get("serie25"));
		req.setAttribute("period_name", series.get("period_name"));
		req.setAttribute("serie31", series.get("serie31"));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("GlobalProgressAction.init end");
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

		log.info("GlobalProgressAction.refresh start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		Map<String, String> series = service.getSeries(conn);
		listResponse.put("wipOnlineRepairUp", series.get("wipOnlineRepairUp"));
		listResponse.put("wipOnlineRepairDown", series.get("wipOnlineRepairDown"));
		listResponse.put("serie11", series.get("serie11"));
		listResponse.put("serie101", series.get("serie101"));
//		listResponse.put("serie12", series.get("serie12"));
		listResponse.put("serie13", series.get("serie13"));
		listResponse.put("serie14", series.get("serie14"));
		listResponse.put("serie15", series.get("serie15"));
		listResponse.put("serie16", series.get("serie16"));
		listResponse.put("serie17", series.get("serie17"));
		listResponse.put("serie21", series.get("serie21"));
		listResponse.put("serie22", series.get("serie22"));
		listResponse.put("serie23", series.get("serie23"));
		listResponse.put("serie24", series.get("serie24"));
		listResponse.put("serie25", series.get("serie25"));
		listResponse.put("period_name", series.get("period_name"));
		listResponse.put("serie31", series.get("serie31"));

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("GlobalProgressAction.refresh end");
	}
}
