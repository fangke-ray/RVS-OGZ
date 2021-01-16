/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：工程检查票改废订画面<br>
 * @author 龚镭敏
 * @version 2.6
 */
package com.osh.rvs.action.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.common.PcsUtils;
import com.osh.rvs.form.master.PcsRequestForm;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.manage.PcsRequestService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;

public class PcsInputLimitAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	/**
	 * 工程检查票改废订画面初始表示处理
	 * 
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit = { 2, 0 })
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {

		log.info("PcsInputLimitAction.init start");

		// 取得工程检查票类型
		Map<String, String> folderTypes = PcsUtils.getFolderTypes();
		req.setAttribute("lLineType", CodeListUtils.getSelectOptions(folderTypes, null, "", false));

		ModelService mService = new ModelService();
		req.setAttribute("mReferChooser", mService.getAllOptions(conn));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("PcsInputLimitAction.init end");
	}

	/**
	 * 工程检查票文档检索
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PcsInputLimitAction.search start");
		Map<String, Object> lResponseResult = new HashMap<String, Object>();
		List<MsgInfo> msgInfos = new ArrayList<MsgInfo>();

		PcsRequestService service = new PcsRequestService();
		List<PcsRequestForm> lForms = service.getFileList(form, conn);

		lResponseResult.put("lForms", lForms);
		lResponseResult.put("errors", msgInfos);
		returnJsonResponse(res, lResponseResult);
		log.info("PcsInputLimitAction.search end");
	}

	public void getPcsInputs(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PcsInputLimitAction.getPcsInputs start");
		Map<String, Object> lResponseResult = new HashMap<String, Object>();
		List<MsgInfo> msgInfos = new ArrayList<MsgInfo>();

		PcsRequestService service = new PcsRequestService();
		String fileHtml = service.getPcsInputs(form, lResponseResult, conn);

		lResponseResult.put("fileHtml", fileHtml);
		lResponseResult.put("errors", msgInfos);
		returnJsonResponse(res, lResponseResult);
		log.info("PcsInputLimitAction.getPcsInputs end");
	}

	public void doSetLimits(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PcsInputLimitAction.doSetLimits start");
		Map<String, Object> lResponseResult = new HashMap<String, Object>();
		List<MsgInfo> msgInfos = new ArrayList<MsgInfo>();

		PcsRequestService service = new PcsRequestService();
		service.setPcsInputLimits(form, req.getParameterMap(), msgInfos, conn);

		lResponseResult.put("errors", msgInfos);
		returnJsonResponse(res, lResponseResult);
		log.info("PcsInputLimitAction.doSetLimits end");
	}
	
}