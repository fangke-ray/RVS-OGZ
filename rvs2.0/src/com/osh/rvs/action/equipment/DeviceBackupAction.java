package com.osh.rvs.action.equipment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.equipment.DeviceBackupForm;
import com.osh.rvs.service.DownloadService;
import com.osh.rvs.service.LineService;
import com.osh.rvs.service.equipment.DeviceBackupService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.DateUtil;

public class DeviceBackupAction extends BaseAction {
	/**
	 * 初始化
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceBackupAction.init start");

		LineService lService = new LineService();
		String lOptions = lService.getAllOptions(conn, "", "(全部)");
		req.setAttribute("lOptions", lOptions);

		// 管理等级
		req.setAttribute("goManageLevel", CodeListUtils.getGridOptions("devices_manage_level"));

		// 状态
		req.setAttribute("goStatus",CodeListUtils.getGridOptions("devices_status"));

		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = user.getPrivacies();
		// 设备管理(设备管理画面)
		String privacy = "";
		if (privacies.contains(RvsConsts.PRIVACY_TECHNOLOGY)) {
			privacy = "technology";
		} 
		req.setAttribute("privacy", privacy);

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("DeviceBackupAction.init end");
	}

	/**
	 * 检索一览
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceBackupAction.search start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		DeviceBackupService service = new DeviceBackupService();
		// 设备工具替代管理详细数据
		List<DeviceBackupForm> listForms = service.search(form, conn, errors);

		listResponse.put("spareList", listForms);
		listResponse.put("errors", errors);

		returnJsonResponse(res, listResponse);
		log.info("DeviceBackupAction.search end");
	}

	/**
	 * 取得代替关系
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void getBackups(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceBackupAction.getBackups start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		DeviceBackupService service = new DeviceBackupService();

		// 取得同品类设备工具
		// 取得此管理编号代替关系
		service.getDetail(form, conn, listResponse, errors);

		listResponse.put("errors", errors);

		returnJsonResponse(res, listResponse);
		log.info("DeviceBackupAction.getBackups end");
	}

	/**
	 * 更新代替关系
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doSubmit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceBackupAction.doSubmit start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		DeviceBackupService service = new DeviceBackupService();

		// 取得同品类设备工具
		// 取得此管理编号代替关系
		service.submit(form, req, conn, errors);

		listResponse.put("errors", errors);

		returnJsonResponse(res, listResponse);
		log.info("DeviceBackupAction.doSubmit end");
	}

	/**
	 * 导出报表
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void makeReport(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceBackupAction.makeReport start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		DeviceBackupService service = new DeviceBackupService();

		// 导出报表
		String filePath = service.makeReport(conn);

		listResponse.put("errors", errors);
		listResponse.put("filePath", filePath);

		returnJsonResponse(res, listResponse);
		log.info("DeviceBackupAction.makeReport end");
	}

	public void export(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		String filePath = req.getParameter("filePath");
		Date today = new Date();
		filePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM") + "\\" + RvsUtils.charRecorgnize(filePath);
		String fileName = RvsUtils.charUrlEncode("设备代替一览表.xlsx");
		
		DownloadService dservice = new DownloadService();
		dservice.writeFile(res, DownloadService.CONTENT_TYPE_EXCEL_OPENXML, fileName, filePath);
	}

}
