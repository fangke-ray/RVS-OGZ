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

import com.osh.rvs.service.AllPositionsService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;


public class AllPositionsAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

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

		log.info("AllPositionsAction.init start");

		String display = req.getParameter("display");
		if ("man".equals(display)) {
			// 迁移到页面
			actionForward = mapping.findForward("man");
		} else {
			// 迁移到页面
			actionForward = mapping.findForward(FW_INIT);
		}

		log.info("AllPositionsAction.init end");
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

		log.info("AllPositionsAction.refresh start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		AllPositionsService service = new AllPositionsService();
		
		// 取得各工位等待/产出/平均工时
		List<Map<String, Object>> objs = service.getPositions(conn);

		// 取得各工位上限状态/中断信息
		Map<String, Map<String, String>> onjs = service.makeOnjs(listResponse, objs, conn);
		listResponse.put("positions", onjs);

		// 取得受理/报价/同意/投线数/WIP
		// 取得当日计划
		// 取得各工程产出
		// 取得品保通过/不通过/出货
		service.getBannerCounts(listResponse, conn);

		// 取得各工程当前BO
		service.getBannerBoCounts(listResponse, conn);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("AllPositionsAction.refresh end");
	}

	/**
	 * 中断/BO时间详细
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void getAlarmsTime(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("AllPositionsAction.getAlarmsTime start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		AllPositionsService service = new AllPositionsService();

		if ("true".equals(req.getParameter("hasError"))) {
			// 取得各工位故障信息
			List<Map<String, Object>> objs = service.getErrorAlarms(req, conn);

			listResponse.put("alarms", objs);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("AllPositionsAction.getAlarmsTime end");
	}
}
