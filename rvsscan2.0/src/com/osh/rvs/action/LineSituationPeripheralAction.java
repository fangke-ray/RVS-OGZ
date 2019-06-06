/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：周边修理工程展示<br>
 * @author 龚镭敏
 * @version 2.01
 */
package com.osh.rvs.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.service.AcceptFactService;
import com.osh.rvs.service.FinalCheckService;
import com.osh.rvs.service.LineLeaderService;
import com.osh.rvs.service.LineTimespaceService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;


public class LineSituationPeripheralAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	/**
	 * 周边修理工程展示画面初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("LineSituationPeripheralAction.init start" + req.getParameter("in"));

		String forward = FW_INIT;

		req.setAttribute("line_id", "00000000070");
		req.setAttribute("line_name", "周边维修工程");
		LineTimespaceService lineTimespaceService = new LineTimespaceService();
		req.setAttribute("standard_column", lineTimespaceService.getStandardColumn(null));

		// 迁移到页面
		actionForward = mapping.findForward(forward);

		log.info("LineSituationPeripheralAction.init end" + req.getParameter("in"));
	}

	/**
	 * 周边修理工程展示画面数据刷新处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void refresh(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("LineSituationPeripheralAction.refresh start");
		Map<String, Object> callback = new HashMap<String, Object>();

		LineLeaderService service = new LineLeaderService();

		// 取得等待投线
		AcceptFactService afService = new AcceptFactService();
		callback.put("waiting_cast", afService.getInlineWaitingForPeripheral(conn));

		// 取得今日计划暨作业对象一览
		service.getSituation("00000000001", "00000000070", callback, "without", conn);
		service.getSimpleContent("00000000001", "00000000070", callback, conn);
		callback.put("waiting_parts", service.getPeriWaitingPart(conn));

		// 取得品保完成信息
		FinalCheckService fcService = new FinalCheckService();
		fcService.getCounts(callback, conn);

		// 取得操作者作业
		LineTimespaceService ltService = new LineTimespaceService();
		
		String arrLineIds[] = {"00000000070"};
		callback.put("productionFeatures", ltService.getOperatorFeatures(arrLineIds, conn));

		// 检查发生错误时报告错误信息
		callback.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, callback);

		log.info("LineSituationPeripheralAction.refresh end");
	}
}
