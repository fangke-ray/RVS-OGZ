package com.osh.rvs.action.manage;

import static com.osh.rvs.service.UploadService.toXls2003;
import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.manage.ProcessInspectAchievementEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.common.ZipUtility;
import com.osh.rvs.form.manage.ProcessInspectAchievementForm;
import com.osh.rvs.form.manage.ProcessInspectConfirmForm;
import com.osh.rvs.form.manage.ProcessInspectForm;
import com.osh.rvs.service.DownloadService;
import com.osh.rvs.service.LineService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.OperatorService;
import com.osh.rvs.service.manage.ProcessInspectConfirmService;
import com.osh.rvs.service.manage.ProcessInspectService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

public class ProcessInspectAction extends BaseAction {

	private Logger _log = Logger.getLogger(getClass());

	private ProcessInspectService service = new ProcessInspectService();
	private ProcessInspectConfirmService confirmService = new ProcessInspectConfirmService();

	/**
	 * 作业监察 页面初始化
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		_log.info("ProcessInspectAction.init start");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		if (user.getPrivacies().contains(RvsConsts.PRIVACY_LINE)) {
			req.setAttribute("enableEdit", true);
		}
		
		if(user.getRole_id().equals(RvsConsts.ROLE_MANAGER)){
			req.setAttribute("signEdit", true);
		}

		LineService lineService = new LineService();
		// 工程信息取得
		String lOptions = lineService.getOptions(RvsConsts.DEPART_REPAIR, conn);
		// 工程信息设定
		req.setAttribute("lOptions", lOptions);

		ModelService modelService = new ModelService();
		req.setAttribute("mReferChooser", modelService.getOptions(user.getDepartment(), conn));

		OperatorService oService = new OperatorService();
		if (user.getDepartment() != null) {
			req.setAttribute("oReferChooser", oService.getAllOperatorName(user.getDepartment(), conn));
		} else {
			req.setAttribute("oReferChooser", oService.getAllOperatorName(conn));
		}

		req.setAttribute("iReferChooser", service.getInspectors(user.getDepartment(), conn));

		// 实施选项
		req.setAttribute("performOptions", CodeListUtils.getSelectOptions("inspect_perform_option", null, ""));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		_log.info("ProcessInspectAction.init end");
	}

	/**
	 * 作业监察 页面初始化 查询一览处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
//	@Privacies(permit={111})
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		_log.info("ProcessInspectAction.search start");
		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行检索
			List<ProcessInspectForm> resultForm = service.search(form, conn, errors);

			// 查询结果放入Ajax响应对象
			listResponse.put("list", resultForm);
			// 实施选项
			listResponse.put("performOptions", CodeListUtils.getGridOptions("inspect_perform_option"));
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		_log.info("ProcessInspectAction.search end");
	}

	/**
	 * 获取详细信息
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void detail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		_log.info("ProcessInspectAction.detail start");

		String process_inspect_key = req.getParameter("process_inspect_key");

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		if (CommonStringUtil.isEmpty(process_inspect_key)) {
			actionForward = mapping.findForward("error");
		} else {
			Integer cnt = service.countAchievementType(process_inspect_key, conn, errors);

			req.setAttribute("process_inspect_key", process_inspect_key);
			req.setAttribute("achiCnt", cnt);

			// 取得用户信息
			HttpSession session = req.getSession();
			LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
			req.setAttribute("jobNo", user.getJob_no());

			if (user.getPrivacies().contains(RvsConsts.PRIVACY_LINE)) {
				req.setAttribute("enableEdit", true);
			}
			
			if(user.getRole_id().equals(RvsConsts.ROLE_MANAGER)){
				req.setAttribute("signEdit", true);
			}

			actionForward = mapping.findForward("detail");
		}

		_log.info("ProcessInspectAction.detail end");
	}

	/**
	 * 获取详细信息
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void detailInit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		_log.info("ProcessInspectAction.detailInit start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行检索
			ProcessInspectForm data = (ProcessInspectForm) form;

			ProcessInspectForm summary = service.findSummaryByKey(data.getProcess_inspect_key(), conn, errors);

			Map<String, List<ProcessInspectAchievementForm>> details = service.findAchievementByKey(data.getProcess_inspect_key(), conn, errors);
			
			// 作业监察确认
			List<ProcessInspectConfirmForm> confirmList = confirmService.searchAll(data.getProcess_inspect_key(), conn);

			// 查询结果放入Ajax响应对象
			listResponse.put("header", summary);
			listResponse.put("details", details);
			listResponse.put("confirmList", confirmList);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		_log.info("ProcessInspectAction.detailInit end");
	}

	/**
	 * 汇总上传 execute
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void uploadSummaryFile(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {
		_log.info("ProcessInspectAction.uploadSummarFile start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String tempfilename = service.saveSummaryFile2Local(form, errors, true);
		// 转换2003格式
		if (tempfilename.endsWith(".xlsx")) {
			tempfilename = toXls2003(tempfilename);
		}
		if (errors.size() == 0) {
			ProcessInspectForm data = new ProcessInspectForm();
			if (tempfilename.endsWith(".xls")) {
				data = service.readSummaryFile(tempfilename, conn, errors);

				data.setFile_type("1");
			} else {
				data.setFile_type("0");
			}

			listResponse.put("data", data);

			if (res == null && errors.size() == 0) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("file.invalidFormat");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidFormat"));
				errors.add(error);
				listResponse.put("errors", errors);
			}

		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		_log.info("ProcessInspectAction.uploadSummarFile end");
	}

	/**
	 * 汇总上传保存 execute
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void doCreateSummary(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {
		_log.info("ProcessInspectAction.doCreateSummary start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		ProcessInspectForm data = (ProcessInspectForm) form;

		// 存储数据
		String processInspectKey = service.createSummary(data, conn, errors);

		if (processInspectKey != null) {
			data.setProcess_inspect_key(processInspectKey);

			// 存储文件
			String tempfilename = service.saveSummaryFile2Local(data, errors, false);
			// 转换2003格式
			if (tempfilename.endsWith(".xlsx")) {
				tempfilename = toXls2003(tempfilename);
			}

			if (res == null && errors.size() == 0) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("file.invalidFormat");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidFormat"));
				errors.add(error);
				listResponse.put("errors", errors);
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		_log.info("ProcessInspectAction.doCreateSummary end");
	}

	/**
	 * 作业监查实绩上传保存 execute
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void doCreateAchievement(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {
		_log.info("ProcessInspectAction.doCreateAchievement start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		ProcessInspectForm data = (ProcessInspectForm) form;

		// ***********************
		// 保存临时文件
		// ***********************
		String tempfilename = service.saveAchievementFile2Local(data, errors, true);
		// 转换2003格式
		if (tempfilename.endsWith(".xlsx")) {
			tempfilename = toXls2003(tempfilename);
		} else if (!tempfilename.endsWith(".xls") && isEmpty(data.getProcess_name())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("process_name");
			error.setErrcode("validator.required.singledetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "作业名"));
			errors.add(error);
		}

		// ***********************
		// 插入
		// ***********************
		if (errors.size() == 0) {
			if (tempfilename.endsWith(".xls")) {
				// 读取分析Excel文件
				List<ProcessInspectAchievementEntity> entitys = service.readAchievementFile(tempfilename, data.getProcess_name(), data, conn, errors);
				if (entitys == null && errors.size() == 0) {
					MsgInfo error = new MsgInfo();
					error.setErrcode("file.invalidFormat");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidFormat"));
					errors.add(error);
					listResponse.put("errors", errors);
				} else if (entitys != null && errors.size() == 0) {
					// 插入实绩表
					service.createAchievement(entitys, conn, errors);
				}
			} else {
				// 插入实绩表
				service.createAchievement(data.getProcess_inspect_key(), data.getProcess_name(), conn, errors);
			}

			if (res == null && errors.size() == 0) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("file.invalidFormat");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidFormat"));
				errors.add(error);
				listResponse.put("errors", errors);
			}

		}

		// ***********************
		// 保存文件
		// ***********************
		if (errors.size() == 0) {
			tempfilename = service.saveAchievementFile2Local(data, errors, false);
			// 转换2003格式
			if (tempfilename.endsWith(".xlsx")) {
				tempfilename = toXls2003(tempfilename);
			} else if (isEmpty(data.getProcess_name())) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("process_name");
				error.setErrcode("validator.required.singledetail");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "作业名"));
				errors.add(error);
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		_log.info("ProcessInspectAction.doCreateAchievement end");
	}

	/**
	 * 作业完成
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void output(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("ProcessInspectAction.output start");

		String process_inspect_key = req.getParameter("process_inspect_key");

     	String folderPath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\process_inspect\\" + process_inspect_key;

     	String filePath = folderPath + ".zip";
     	String fileName = process_inspect_key + ".zip";
		// 打包
		ZipUtility.zipper(folderPath, filePath, "UTF-8");

		service.outputFile(res,
				DownloadService.CONTENT_TYPE_ZIP,
				RvsUtils.charUrlEncode(fileName),
				filePath);

		log.info("ProcessInspectAction.output end");
	}

	/**
	 * 删除 execute
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void doRemove(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {
		_log.info("ProcessInspectAction.doRemove start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		ProcessInspectForm data = (ProcessInspectForm) form;

		if (data == null || isEmpty(data.getProcess_inspect_key())) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("process_inspect_key");
			error.setErrcode("validator.required.singledetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.singledetail", "Key"));
			errors.add(error);
		} else {
			service.removeAll(data.getProcess_inspect_key(), conn, errors);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		_log.info("ProcessInspectAction.doRemove end");
	}
	
	/**
	 * 删除作业监察实绩
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doDeleteAchievement(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSessionManager conn) throws Exception {
		_log.info("ProcessInspectAction.doDeleteAchievement start");
		
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("process_inspect_key", v.required("Key"));
		v.add("process_name", v.required("作业名"));
		
		List<MsgInfo> errors = v.validate();
		if(errors.size() == 0){
			service.deleteAchievement(form, conn);
		}
		
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		_log.info("ProcessInspectAction.doDeleteAchievement end");
	}
}
