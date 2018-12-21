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

import com.osh.rvs.service.LineTimespaceService;
import com.osh.rvs.service.PartialWarehouseService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;

public class PartialWarehouseAction extends BaseAction {
	private final Logger log = Logger.getLogger(getClass());
	private final LineTimespaceService lineTimespaceService = new LineTimespaceService();
	private final PartialWarehouseService partialWarehouseService = new PartialWarehouseService();

	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialWarehouseAction.init start");

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		req.setAttribute("standard_column", lineTimespaceService.getStandardColumn(null));

		log.info("PartialWarehouseAction.init end");

	}

	public void refresh(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialWarehouseAction.refresh start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();


		partialWarehouseService.searchProcess(listResponse, conn);


		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("PartialWarehouseAction.refresh end");
	}

}
