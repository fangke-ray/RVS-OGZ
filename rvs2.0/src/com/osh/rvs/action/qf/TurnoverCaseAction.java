package com.osh.rvs.action.qf;

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

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.qf.TurnoverCaseEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.qf.TurnoverCaseForm;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.qf.TurnoverCaseService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;

public class TurnoverCaseAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	private ModelService modelService = new ModelService();

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

		log.info("TurnoverCaseAction.init start");

		String mReferChooser = modelService.getOptions(RvsConsts.DEPART_REPAIR, conn);
		req.setAttribute("mReferChooser", mReferChooser);
		req.setAttribute("boundOutOcmOptions", CodeListUtils.getSelectOptions("material_direct_ocm", null, "(全部)"));
		// 设定等级文字
		req.setAttribute("lOptions", CodeListUtils.getGridOptions("material_level"));
		// 设定发送地文字
		req.setAttribute("ocmOptions", CodeListUtils.getGridOptions("material_direct_ocm"));
		req.setAttribute("kindOptions", CodeListUtils.getSelectOptions("turnover_case_kind", null, "(全部)"));

		// 取得登录用户权限
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = user.getPrivacies();

		boolean isRditor = false;
		if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {  // quotationer

			List<LineEntity> lines = user.getLines();
			for (LineEntity line : lines) {
				if ("00000000011".equals(line.getLine_id())) {
					isRditor = true;
					break;
				}
			}			
		}

		req.setAttribute("editor", "" + isRditor);
		if (privacies.contains(RvsConsts.PRIVACY_ADMIN)) {  // quotationer
			req.setAttribute("editor", "admin");
		}

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("TurnoverCaseAction.init end");
	}

	/**
	 * 通箱库位条件查询
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 111 })
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("TurnoverCaseAction.search start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);

		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			TurnoverCaseService service = new TurnoverCaseService();
			// 执行检索
			List<TurnoverCaseForm> lResultForm = service.searchTurnoverCase(form, conn);
			// 查询结果放入Ajax响应对象
			listResponse.put("list", lResultForm);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("TurnoverCaseAction.search end");
	}

	@Privacies(permit = { 111 })
	public void getStoargeEmpty(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("TurnoverCaseAction.getStoargeEmpty start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		String kind = null;
		if (req.getParameter("material_id") != null) {
			kind = service.getStorageKindByMaterial(req.getParameter("material_id"), conn);
		}

		List<String> wipHeaped = service.getStorageHeaped(kind, conn);

		// 查询结果放入Ajax响应对象
		calbackResponse.put("heaps", wipHeaped);

		service.getLocationMap(kind, null, calbackResponse, conn);

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.getStoargeEmpty end");
	}

	@Privacies(permit = { 103, 107 })
	public void doChangeLocation(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doChangeLocation start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		service.changelocation(conn, req.getParameter("material_id"), req.getParameter("location"));

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doChangeLocation end");
	}

	@Privacies(permit = { 103, 107 })
	public void doWarehousing(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doWarehousing start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		String location = req.getParameter("location");
		service.warehousing(conn, location);

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doWarehousing end");
	}

	@Privacies(permit = { 103, 107 })
	public void doWarehousingPlanned(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doWarehousingPlanned start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		service.warehousing(conn, req.getParameterMap());

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doWarehousingPlanned end");
	}

	@Privacies(permit = { 103, 107 })
	public void getWarehousingPlanList(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("TurnoverCaseAction.getWarehousingPlanList start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		List<TurnoverCaseForm> warehousingPlanList = service.getWarehousingPlanList(conn);

		// 查询结果放入Ajax响应对象
		calbackResponse.put("warehousingPlanList", warehousingPlanList);

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.getWarehousingPlanList end");
	}

	@Privacies(permit = { 103, 107 })
	public void doStorage(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doStorage start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		String location = req.getParameter("location");
		service.checkStorage(conn, location);

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doStorage end");
	}

	@Privacies(permit = { 103, 107 })
	public void doStoragePlanned(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doStoragePlanned start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		service.checkStorage(conn, req.getParameterMap());

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doStoragePlanned end");
	}

	public void getStoragePlanList(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("TurnoverCaseAction.getStoragePlanList start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		List<TurnoverCaseForm> storagePlanList = service.getStoragePlanList(conn);

		// 查询结果放入Ajax响应对象
		calbackResponse.put("storagePlanList", storagePlanList);

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.getStoragePlanList end");
	}

	public void getIdleMaterialList(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("TurnoverCaseAction.getIdleMaterialList start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		List<TurnoverCaseForm> idleMaterialList = service.getIdleMaterialList(conn);

		// 查询结果放入Ajax响应对象
		calbackResponse.put("idleMaterialList", idleMaterialList);

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.getIdleMaterialList end");
	}

	@Privacies(permit = { 103, 107 })
	public void doPutin(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doPutin start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 手动放入
		TurnoverCaseService service = new TurnoverCaseService();
		service.putinManual(req.getParameter("location"), req.getParameter("material_id"), conn);

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doPutin end");
	}

	@Privacies(permit = { 103, 107 })
	public void doTrolleyUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doTrolleyUpdate start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 推车位置
		TurnoverCaseService service = new TurnoverCaseService();
		service.trolleyUpdate(req.getParameterMap(), conn);

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doTrolleyUpdate end");
	}

	@Privacies(permit = { 103, 107 })
	public void doAssignLocation(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doAssignLocation start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 推车位置
		TurnoverCaseService service = new TurnoverCaseService();
		service.trolleyUpdate(req.getParameterMap(), conn);

		// 定位
		String retMessage = service.assignLocation(req.getParameterMap(), conn);

		calbackResponse.put("retMessage", retMessage);

		List<TurnoverCaseForm> idleMaterialList = service.getIdleMaterialList(conn);

		// 刷新等待维修品以及推车信息
		// 查询预计的入库位信息
		try {
//			List<String> nextLocations = new ArrayList<String>();
//			List<String> nextEndoeyeLocations = new ArrayList<String>();
//
//			service.getEmptyLocations("0", nextLocations, 10, false, conn, false);
//			List<String> nextEndoeyeLocations = service.getEmptyLocations("06", 10, false, conn);

//			calbackResponse.put("nextLocations", nextLocations);
//			calbackResponse.put("nextEndoeyeLocations", nextEndoeyeLocations);

			calbackResponse.put("nextLocations", 
					service.getKindAgreeEmptyLocations(10, conn));
		} catch (Exception e) {
			// if (e.getMessage().equals("递归安排也无法找到库位！")) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("location");
				error.setErrmsg("剩余空闲的通箱库位已经不足。请先处理出库。");
				errors.add(error);
			// }
		}

		if (errors.size() == 0) {
			// 查询入库待处理维修品放入Ajax响应对象
			calbackResponse.put("idleMaterialList", idleMaterialList);

			// 查询推车信息
			List<TurnoverCaseEntity> trolleyStacks = service.getTrolleyStacks(conn);
			calbackResponse.put("trolleyStacks", trolleyStacks);
		}

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doAssignLocation end");
	}
	
	@Privacies(permit = { 1 })
	public void getStoargeByKey(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("TurnoverCaseAction.getStoargeByKey start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();

		// 查询结果放入Ajax响应对象
		calbackResponse.put("ret", service.getStoargeByKey(req.getParameter("key"), conn));

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.getStoargeByKey end");
	}

	@Privacies(permit = { 1 })
	public void doCreate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doCreate start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);

		List<MsgInfo> errorMsgs = v.validate();

		if (errorMsgs.size() == 0) {
			TurnoverCaseService service = new TurnoverCaseService();
			service.create(form, errorMsgs, conn);
		}

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", errorMsgs);

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doCreate end");
	}

	@Privacies(permit = { 1 })
	public void doChange(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doChange start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);

		List<MsgInfo> errorMsgs = v.validate();

		if (errorMsgs.size() == 0) {
			TurnoverCaseService service = new TurnoverCaseService();
			service.changeSetting(form, errorMsgs, conn);
		}

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", errorMsgs);

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doChange end");
	}

	@Privacies(permit = { 1 })
	public void doRemove(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {

		log.info("TurnoverCaseAction.doRemove start");
		// Ajax回馈对象
		Map<String, Object> calbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errorMsgs = new ArrayList<MsgInfo>();

		// 执行检索
		TurnoverCaseService service = new TurnoverCaseService();
		service.remove(form, errorMsgs, conn);

		// 检查发生错误时报告错误信息
		calbackResponse.put("errors", errorMsgs);

		// 返回Json格式响应信息
		returnJsonResponse(res, calbackResponse);

		log.info("TurnoverCaseAction.doRemove end");
	}

}
