package com.osh.rvs.action.manage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.manage.DefectiveAnalysisSearchEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.manage.DefectiveAnalysisForm;
import com.osh.rvs.service.LineService;
import com.osh.rvs.service.manage.DefectiveAnalysisService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

/**
 * 不良对策处理
 * @author
 *
 */
public class DefectiveAnalysisAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	private DefectiveAnalysisService service = new DefectiveAnalysisService();

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
	@Privacies(permit = { 111 })
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("DefectiveAnalysisAction.init start");

		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		req.setAttribute("defectiveTypeOptions", CodeListUtils.getSelectOptions("defective_type", null, ""));
		req.setAttribute("defectiveStepOptions", CodeListUtils.getSelectOptions("defective_step", null, ""));
		req.setAttribute("defectiveResponsibilityOfLineOptions", CodeListUtils.getSelectOptions("defective_responsibility_of_line", null, ""));
		req.setAttribute("defectiveResponsibilityOfPtlOptions", CodeListUtils.getSelectOptions("defective_responsibility_of_ptl", null, ""));
		req.setAttribute("defectiveCapaFrequencyOptions", CodeListUtils.getSelectOptions("defective_capa_frequency", null, ""));
		req.setAttribute("defectiveCapaMajorOptions", CodeListUtils.getSelectOptions("defective_capa_major", null, ""));
		req.setAttribute("defectiveCapaRiskOptions", CodeListUtils.getSelectOptions("defective_capa_risk", null, ""));
		// 返工应对
		req.setAttribute("defectiveReworkProceedOptions", CodeListUtils.getSelectOptions(service.setupReworkProceedList(), null, "", false));


		LineService lineService = new LineService();
		// 工程信息取得
		String lOptions = lineService.getOptions(user.getDepartment(), conn);
		// 工程信息设定
		req.setAttribute("lOptions", lOptions);

		Calendar today  =Calendar.getInstance();
		int hour = today.get(Calendar.HOUR_OF_DAY);

		if(hour < 10 ){
			today.add(Calendar.DAY_OF_MONTH, -1);
		}

		req.setAttribute("today", DateUtil.toString(today.getTime(), DateUtil.DATE_PATTERN));
		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("DefectiveAnalysisAction.init end");
	}

	/**
	 * 不良对策查询一览处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={111})
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("DefectiveAnalysisAction.search start");
		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

//		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行检索
			List<DefectiveAnalysisForm> amResultForm = service.search(form, conn, errors);

			// 查询结果放入Ajax响应对象
			listResponse.put("list", amResultForm);
			// 不良分类
			listResponse.put("typeOptions", CodeListUtils.getGridOptions("defective_type"));
			// 对策进度
			listResponse.put("stepOptions", CodeListUtils.getGridOptions("defective_step"));
			// 责任区分
			listResponse.put("ptlOptions", CodeListUtils.getGridOptions("defective_responsibility_of_ptl"));
			// 风险大小
			listResponse.put("riskOptions", CodeListUtils.getGridOptions("defective_capa_risk"));

		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DefectiveAnalysisAction.search end");
	}

	/**
	 * 警报查询详细处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit = { 111 })
	public void detail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {
		log.info("DefectiveAnalysisAction.detail start");

		String alarm_message_id = req.getParameter("alarm_message_id");
		String afterResolve = req.getParameter("afterResolve");

		if (CommonStringUtil.isEmpty(alarm_message_id)) {
			actionForward = mapping.findForward("error");
		} else {
			req.setAttribute("alarm_message_id", alarm_message_id);

			// 取得用户信息
			HttpSession session = req.getSession();
			LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

			DefectiveAnalysisSearchEntity entity = service.findEntityById(alarm_message_id, conn);

			if (entity.getDefective_type() == null) {
				if (RvsConsts.WARNING_REASON_QAFORBID ==  entity.getReason()) {
					entity.setDefective_type(DefectiveAnalysisService.DEFECTIVE_TYPE_QA);
				} else {
					entity.setDefective_type(service.getDefectiveTypeByBreakType(alarm_message_id, conn));
				}
			}
			Integer powerId = service.setupPowerId(user, afterResolve != null, alarm_message_id, entity);

			req.setAttribute("defectivePowerId", powerId);

			req.setAttribute("defectiveStep", entity.getStep());

			// ****************
			// 下拉框
			// ****************

			// 不良分类
			req.setAttribute("defectiveTypeOptions", setupTypeOptions(entity));

			req.setAttribute("defectiveTypeFlag", entity.getDefective_type());

			// 对策进度
			Map<String, String> defectiveStepOptions = new TreeMap<String, String>();

			Map<String, String> codeListStep = CodeListUtils.getList("defective_step");
			for (String key : codeListStep.keySet()) {
				if (key.contains(String.valueOf(entity.getStep()))) {
					defectiveStepOptions.put(key, codeListStep.get(key));
					break;
				}
			}
			req.setAttribute("defectiveStepOptions", CodeListUtils.getSelectOptions(defectiveStepOptions, "", null, true));

			// 责任区分（生产线）
			req.setAttribute("defectiveResponsibilityOfLineOptions", CodeListUtils.getSelectOptions("defective_responsibility_of_line", null, null));
			// 返工应对
			req.setAttribute("defectiveReworkProceedOptions", CodeListUtils.getSelectOptions(service.setupReworkProceedList(), "0", null, true));
			// 责任区分（技术）
			req.setAttribute("defectiveResponsibilityOfPtlOptions", CodeListUtils.getSelectOptions("defective_responsibility_of_ptl", null, null));
			// CAPA频度判断
			req.setAttribute("defectiveCapaFrequencyOptions", CodeListUtils.getSelectOptions("defective_capa_frequency", null, null));
			// CAPA重大度判断
			req.setAttribute("defectiveCapaMajorOptions", CodeListUtils.getSelectOptions("defective_capa_major", null, null));
			// 风险大小等级
			req.setAttribute("defectiveCapaRiskOptions", CodeListUtils.getSelectOptions("defective_capa_risk", null, null));
			// 入库零件不良处理
			req.setAttribute("defectiveStoredPartsResolveOptions", CodeListUtils.getSelectOptions("defective_stored_parts_resolve", null, null));
			// 波及性判断结果
			req.setAttribute("defectiveInvolvingOptions", CodeListUtils.getSelectOptions(service.setupInvolvingOptions(), "0", null, true));

			actionForward = mapping.findForward("detail");
		}

		log.info("DefectiveAnalysisAction.detail end");
	}

	/**
	 * 警报详细初始画面
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit = { 111 })
	public void detailInit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {
		log.info("DefectiveAnalysisAction.detailInit start");

		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		List<MsgInfo> infoes = new ArrayList<MsgInfo>();

		String alarm_message_id = req.getParameter("alarm_message_id");

		if (CommonStringUtil.isEmpty(alarm_message_id)) {
			MsgInfo e = new MsgInfo();
			e.setComponentid("alarm_message_id");
			e.setErrmsg("没有指定警告信息ID");
			infoes.add(e);
		} else {
			DefectiveAnalysisForm resform = service.findById(alarm_message_id, conn);

			callbackResponse.put("photo_list", service.getPhotoList(alarm_message_id, conn));
			callbackResponse.put("alarm", resform);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", infoes);

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		log.info("DefectiveAnalysisAction.detailInit end");
	}

	/**
	 * 提交处理
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit= {103, 104, 105, 106, 121})
	public void doCommit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {

		log.info("DefectiveAnalysisAction.doCommit start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		// 检查发生错误时报告错误信息
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		if (errors.size() == 0) {
			// 取得用户信息
			HttpSession session = req.getSession();
			LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

			String alarm_message_id = req.getParameter("alarm_message_id");

			DefectiveAnalysisSearchEntity entity = service.findEntityById(alarm_message_id, conn);
			if (entity.getDefective_type() == null) {
				if (RvsConsts.WARNING_REASON_QAFORBID ==  entity.getReason()) {
					entity.setDefective_type(DefectiveAnalysisService.DEFECTIVE_TYPE_QA);
				}
			}
			Integer powerId = service.setupPowerId(user, false, alarm_message_id, entity);

			if (powerId != DefectiveAnalysisService.POWERID_READONLY) {
				service.maintain(form, req.getParameterMap(), conn, powerId, Integer.valueOf(user.getOperator_id()), errors);
			} else {
				MsgInfo error = new MsgInfo();
				error.setErrcode("privacy.noPrivacy");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("privacy.noPrivacy"));
				errors.add(error);
			}
		}

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DefectiveAnalysisAction.doCommit end");
	}

	public void getAutoManageCode(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {
		log.info("DefectiveAnalysisAction.getAutoManageCode start");

		// 检查发生错误时报告错误信息
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.required("alarm_message_id");
		v.required("defective_type");
		List<MsgInfo> errors = v.validate();

		// 检查发生错误时报告错误信息
		Map<String, Object> cbResponse = new HashMap<String, Object>();
		cbResponse.put("errors", errors);

		if (errors.size() == 0) {
			String alarm_message_id = req.getParameter("alarm_message_id");
			String defective_type = req.getParameter("defective_type");

			int defectiveType = Integer.valueOf(defective_type);

			cbResponse.put("manage_code", service.getAutoManageCode(defectiveType, alarm_message_id, conn));
		}

		// 返回Json格式响应信息
		returnJsonResponse(res, cbResponse);

		log.info("DefectiveAnalysisAction.getAutoManageCode end");
	}

	/**
	 * 取得不良分类选项
	 * 
	 * @param reason
	 * @return
	 */
	private String setupTypeOptions(DefectiveAnalysisSearchEntity entity) {

		Map<String, String> defectiveTypeOptions = new TreeMap<String, String>();

		String nullOption = null;

		Map<String, String> codeListDefectiveType = CodeListUtils.getList("defective_type");
		for (String key : codeListDefectiveType.keySet()) {
			if (RvsConsts.WARNING_REASON_QAFORBID == entity.getReason()) {
				if (key.equals("" + DefectiveAnalysisService.DEFECTIVE_TYPE_QA)) {
					// 异常中断时
					// 对策 == 最终检查不良
					defectiveTypeOptions.put(key, codeListDefectiveType.get(key));
					entity.setDefective_type(DefectiveAnalysisService.DEFECTIVE_TYPE_QA);
					break;
				}
			} else {
				// 品保不合格时
				// 对策 = 最终检查不良 以外的情况
				if (key.equals("" + DefectiveAnalysisService.DEFECTIVE_TYPE_QA)) {
					continue;
				}
				defectiveTypeOptions.put(key, codeListDefectiveType.get(key));
			}
		}

		if (entity.getDefective_type() == null) nullOption = "";
		return CodeListUtils.getSelectOptions(defectiveTypeOptions, "", nullOption, true);
	}
}