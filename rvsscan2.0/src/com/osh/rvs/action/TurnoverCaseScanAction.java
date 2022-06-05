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

import com.osh.rvs.bean.WipEntity;
import com.osh.rvs.service.TurnoverCaseScanService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;


public class TurnoverCaseScanAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());
	private TurnoverCaseScanService service = new TurnoverCaseScanService();

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

		log.info("TurnoverCaseScanAction.init start");

		req.setAttribute("storageHtml", service.getLocationMap(conn));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("TurnoverCaseScanAction.init end");
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

		log.info("TurnoverCaseScanAction.refresh start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		List<WipEntity> list = service.getWip(conn);
		for (WipEntity store : list) {
			if (store.getExecute() == 2) {
				store.setBound_out_ocm(CodeListUtils.getValue("material_direct_ocm", store.getBound_out_ocm()));
			}
		}
		listResponse.put("list", list);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("TurnoverCaseScanAction.refresh end");
	}
}
