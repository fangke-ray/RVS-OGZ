package com.osh.rvs.action.data;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.master.ModelEntity;
import com.osh.rvs.bean.master.ProcessAssignEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.form.data.MonthFilesDownloadForm;
import com.osh.rvs.form.data.ProductionFeatureForm;
import com.osh.rvs.form.inline.MaterialProcessAssignForm;
import com.osh.rvs.form.inline.MaterialProcessForm;
import com.osh.rvs.form.master.ModelForm;
import com.osh.rvs.form.master.ProcessAssignForm;
import com.osh.rvs.form.partial.MaterialPartialForm;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.master.ProcessAssignMapper;
import com.osh.rvs.service.CategoryService;
import com.osh.rvs.service.DownloadService;
import com.osh.rvs.service.MaterialProcessAssignService;
import com.osh.rvs.service.MaterialProcessService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.ProcessAssignService;
import com.osh.rvs.service.ProductionFeatureService;
import com.osh.rvs.service.SectionService;
import com.osh.rvs.service.inline.PositionPanelService;
import com.osh.rvs.service.partial.MaterialPartialService;
import com.osh.rvs.service.product.ProductService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

/**
 * 维修对象信息管理
 * @author Gong
 *
 */
public class MaterialAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	private CategoryService categoryService = new CategoryService();
	private ModelService modelService = new ModelService();
	private SectionService sectionService = new SectionService();
	private MaterialService materialService = new MaterialService();
	private MaterialPartialService materialPartialService = new MaterialPartialService();
	private MaterialProcessService materialProcessService = new MaterialProcessService();
	private ProductionFeatureService featureService = new ProductionFeatureService();

	/**
	 * 一览画面初始化
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("MaterialAction.init start");

		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		String cOptions = categoryService.getOptions(user.getDepartment(), conn);

		req.setAttribute("cOptions", cOptions);

		String mReferChooser = modelService.getOptions(user.getDepartment(), conn);

		req.setAttribute("mReferChooser", mReferChooser);

		boolean onlyManufactor = (user.getDepartment() != null && user.getDepartment() == RvsConsts.DEPART_MANUFACT);

		if (!onlyManufactor) {
			// OCM取得
			req.setAttribute("oOptions", CodeListUtils.getSelectOptions("material_ocm", null, ""));

			// level取得
			req.setAttribute("lOptions",CodeListUtils.getSelectOptions("material_level", null, "", false));
			req.setAttribute("lGos",CodeListUtils.getGridOptions("material_level"));

			Date cal = new Date();
			req.setAttribute("today_date", DateUtil.toString(cal, DateUtil.DATE_PATTERN));
			req.setAttribute("past_4_date", DateUtil.toString(RvsUtils.switchWorkDate(cal, -4, conn), DateUtil.DATE_PATTERN));

		}

		String sOptions = sectionService.getOptions(user.getDepartment(), conn, "(全部)");
		req.setAttribute("sOptions", sOptions);

		String privacy="";
		//进度操作+计划员+经理以上人员+系统管理员
		if (user.getPrivacies().contains(RvsConsts.PRIVACY_PROCESSING)
				|| user.getPrivacies().contains(RvsConsts.PRIVACY_SCHEDULE)
				|| user.getPrivacies().contains(RvsConsts.PRIVACY_ADMIN)) {
			privacy = "isPrivacy";
		}
		req.setAttribute("privacy", privacy);

		// 迁移到页面
		if (onlyManufactor) {
			actionForward = mapping.findForward("init-man");
		} else {
			actionForward = mapping.findForward(FW_INIT);
		}

		log.info("MaterialAction.init end");
	}

	/**
	 * 维修对象条件查询
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("MaterialAction.search start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);

		List<MsgInfo> errors = v.validate();

		// 历史标记
		String completed = req.getParameter("completed");
		if (completed == null) {
			completed = "1";
		}

		if (errors.size() == 0) {
			//检查用户权限, 如果是系统管理员
			List<Integer> privacies = getPrivacies(req.getSession());
			boolean contains = privacies.contains(RvsConsts.PRIVACY_OVEREDIT);
			if (contains) {
				// ((MaterialForm)form).setFix_type("999"); not TODO
			}

			// 执行检索
			List<MaterialForm> lResultForm = materialService.searchMaterial(form, completed, conn, errors);

			// 检测角色
			materialService.calcOverTime(lResultForm);

			// 查询结果放入Ajax响应对象
			listResponse.put("list", lResultForm);

			req.getSession().setAttribute(RvsConsts.SEARCH_RESULT, lResultForm);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.search end");
	}

	/**
	 * 维修对象详细
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 */
	public void getDetial(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) {
		log.info("MaterialAction.getDetial start");
		// Ajax响应对象
		Map<String, Object> detailResponse = new HashMap<String, Object>();

		String id = req.getParameter("id");
		String occur_times = req.getParameter("occur_times");
		MaterialForm materialForm = new MaterialForm();
//		MaterialPartialForm partialForm = new MaterialPartialForm();
		MaterialProcessForm processForm = new MaterialProcessForm();
		List<String> occutTimes = new ArrayList<String>();

		List<MaterialPartialForm> materialPartialFormList = new ArrayList<MaterialPartialForm>();
		int caseId = 0;
		HttpSession session = req.getSession();
		if (id != null && !"".equals(id)) {

			materialForm = materialService.loadMaterialDetail(conn, id);

			String location = materialForm.getWip_location();
//			if (location != null && !"".equals(location) && location.startsWith("WIP:")) { //location不为空，设置当前状态WIP
//				materialForm.setStatus("WIP");
//				materialForm.setProcessing_position(location);
//			} else { //否则，根据Operate_result查找状态
				String position = materialForm.getProcessing_position();
				materialForm.setWip_location(position);

				List<Map<String, String>> positions = materialService.getLastPositionAndStatus(id, conn);
				int positionSize = positions.size();
				String[] locations = new String[positionSize];
				String[] statuses = new String[positionSize];
				for (int i =0 ; i < positionSize ; i++) {
					Map<String, String> positionMap = positions.get(i);
					locations[i] = "" + positionMap.get("process_code") + " " + positionMap.get("position_name");
					statuses[i] = CodeListUtils.getValue("material_operate_result", positionMap.get("operate_result")) + " (" + positionMap.get("process_code") + " " + positionMap.get("position_name") + ")";
				}
				materialForm.setProcessing_position(CommonStringUtil.joinBy("\n", locations));
				materialForm.setStatus(CommonStringUtil.joinBy("\n", statuses));

				if (location != null && !"".equals(location)) { //location不为空，设置当前状态WIP
					materialForm.setProcessing_position("库位：" + location + "\n" + materialForm.getProcessing_position());
				}

//				String operateResult = materialForm.getOperate_result();
//				if (operateResult != null) {
//					String value = CodeListUtils.getValue("material_operate_result", operateResult);
//					materialForm.setStatus(value);
//				}
//			}

			@SuppressWarnings("unused")
			Integer iOccur_times = null;
			if (occur_times != null) {
				iOccur_times = Integer.parseInt(occur_times);
			}

			//根据维修对象ID查询维修对象所有订购单
			materialPartialFormList = materialPartialService.searchMaterialPartailById(id,conn);

			processForm = materialProcessService.loadMaterialProcess(conn, id);
			// TODO What
			// occutTimes = materialPartialService.getOccurTimes(conn, id);
			caseId = chooseCase(session, materialPartialFormList.size() != 0 , processForm != null);
		}

		detailResponse.put("materialForm", materialForm);
//		detailResponse.put("partialForm", partialForm);
		detailResponse.put("processForm", processForm);
		detailResponse.put("timesOptions", occutTimes);
		detailResponse.put("caseId", caseId);
		detailResponse.put("materialPartialFormList", materialPartialFormList);


		session.setAttribute("caseId", Integer.valueOf(caseId).toString());
//		String sOptions = sectionService.getOptions(conn, null);
//		session.setAttribute("sOptions", sOptions);
//		String mReferChooser = modelService.getOptions(conn);
//		session.setAttribute("mReferChooser", mReferChooser);

		returnJsonResponse(res, detailResponse);

		log.info("MaterialAction.getDetial end");
	}

	/**
	 * 维修对象详细
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 */
	public void getPcsDetail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) {
		log.info("MaterialAction.getPcsDetail start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String material_id = req.getParameter("material_id");
		String getHistory = req.getParameter("get_history");

		// 取得维修对象信息
		MaterialForm mform = materialService.loadSimpleMaterialDetail(conn, material_id);
		listResponse.put("mform", mform);

		String sLine_id = user.getLine_id();

		//String role = user.getRole_id();

		// 经理以上/计划全览工程
		List<Integer> privacies = user.getPrivacies();

		if (!privacies.contains(RvsConsts.PRIVACY_LINE)
				&& !privacies.contains(RvsConsts.PRIVACY_POSITION)) {
			sLine_id = "00000000015";
		} if (privacies.contains(RvsConsts.PRIVACY_PROCESSING)) {
			sLine_id = "00000000015";
		}

		boolean isLeader = (privacies.contains(RvsConsts.PRIVACY_LINE)); //  && !privacies.contains(RvsConsts.PRIVACY_PROCESSING)
		boolean isManuf = RvsConsts.DEPART_MANUFACT.equals(user.getDepartment());

		if (("00000000015".equals(sLine_id) || isLeader) && isManuf) {
			sLine_id = "00000000076";
		}

		if (privacies.contains(RvsConsts.PRIVACY_ADMIN)) {
			isLeader = true;
		} else {
			String outlineTime = mform.getOutline_time();
			if (outlineTime != null) {
				// 如果完成的话，只有系统管理员能修改
				if (isManuf) {
					// 完成两天内
					Date bf = RvsUtils.switchWorkDate(new Date(), -2, conn);
					String sBf = DateUtil.toString(bf, DateUtil.DATE_PATTERN); 
					if (sBf.compareTo(outlineTime) > 0) {
						isLeader = false;
					}
				} else {
					// 完成就没法改
					isLeader = false;
				}
			}
		}

		// 取得工程检查票
		materialService.getPcses(listResponse, mform, material_id, sLine_id, isLeader, getHistory, conn); // 线长编辑

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.getPcsDetail end");
	}

	/**
	 * 维修对象工程检查票详细修正
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 */
	@Privacies(permit={106})
	public void getPcsDetailFixer(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) {
		log.info("MaterialAction.getPcsDetailFixer start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String material_id = req.getParameter("material_id");
		String line_id = req.getParameter("line_id");

		// 取得维修对象信息
		MaterialForm mform = materialService.loadSimpleMaterialDetail(conn, material_id);
		listResponse.put("mform", mform);

		List<Integer> privacies = user.getPrivacies();

		String backDoor = req.getParameter("back_door");

		// 经理以上/计划全览工程
		if (privacies.contains(RvsConsts.PRIVACY_SA)
				|| (backDoor != null && privacies.contains(RvsConsts.PRIVACY_ADMIN) )) {
			// 取得工程检查票
			materialService.getPcses4Fix(listResponse, mform, material_id, conn); // 管理员编辑
		}
		else if (privacies.contains(RvsConsts.PRIVACY_ADMIN) || privacies.contains(RvsConsts.PRIVACY_PROCESSING)) {
			// 取得工程检查票
			materialService.getPcses(listResponse, mform, material_id, line_id, true, "manager", conn); // 管理员编辑 仅备注
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.getPcsDetailFixer end");
	}

	/**
	 * 维修对象修改操作
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("MaterialAction.doUpdate start");
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		String caseId = (String) req.getSession().getAttribute("caseId");
		List<String> triggerList = new ArrayList<String>();

		MaterialForm newForm = (MaterialForm) form;
//		if ("2".equals(caseId)){
//			v.add("", v.required());
//		}
		List<MsgInfo> errors = v != null ? v.validate(): new ArrayList<MsgInfo>();
		int check = -1;
		if (errors.size() == 0) {
			String material_id = ((MaterialForm)form).getMaterial_id();
			boolean level_c = false, section_c = false, pat_c = false;

//			// 旧流程有NS，新流程没有时
//			boolean nsClose = false;

			if ("2".equals(caseId)){//计划权限，修改等级或课时，进行判断有无operate_result=1的作业报告
				MaterialForm oldForm = materialService.loadSimpleMaterialDetail(conn, material_id);
				check = checkToUpdate(oldForm, newForm, material_id, conn);

				if (!isEmpty(oldForm.getInline_time())) { // 投线后
					pat_c = oldForm.getPat_id() != null && oldForm.getPat_id().equals(newForm.getPat_id());
					level_c = oldForm.getLevel() != null && oldForm.getLevel().equals(newForm.getLevel());
					section_c = oldForm.getSection_id() != null && oldForm.getSection_id().equals(newForm.getSection_id());

//					ProcessAssignService pas = new ProcessAssignService();
//					boolean oldHasNs = pas.checkPatHasNs(oldForm.getPat_id(), conn);
//					boolean newHasNs = pas.checkPatHasNs(newForm.getPat_id(), conn);
//					if (oldHasNs && !newHasNs) {
//						// 旧流程有NS，新流程没有时
//						nsClose = true;
//					}
				}

			} else {
				check = 0;
			}

			if (check == 0) {
				// 更新维修对象自身
				materialService.update(form, conn);
				// 更改等级 TODO // 更改维修流程 TODO
				if ("2".equals(caseId) && (!level_c || !pat_c)) {
					ProductionFeatureMapper ppDao = conn.getMapper(ProductionFeatureMapper.class);
					// 本工位作业状态等待的记录中取得Rework
					int rework = 0;
					rework = ppDao.getReworkCount(material_id);
					// 删除目前的等待作业
					featureService.removeWorking(material_id, null, conn);

//					// 单追组件后，NS工程算当时结束 TODO 维修对象进度表的终了时间，要根据实际工位需要状况，改动
//					if (nsClose) {
//						MaterialProcessService mpService = new MaterialProcessService();
//						mpService.finishMaterialProcess(material_id, "00000000013", triggerList, conn);
//					}

					// 修改流程重排工程计划
					if (!pat_c) {
						MaterialProcessService mpService = new MaterialProcessService();
						List<String> oldHasLines = mpService.loadMaterialProcessLineIds(material_id, conn); // 取得已存在工程
						if (oldHasLines != null && oldHasLines.size() > 0) {
							ProcessAssignService pas = new ProcessAssignService();
							List<String> newHasLines = pas.checkPatHasLine(newForm.getPat_id(), newForm.getLevel(), conn);
							mpService.resignMaterialProcess(material_id, oldHasLines, newHasLines, conn);
						}
					}
					// 按新流程重新指派
					featureService.reprocess(material_id, rework, ppDao, triggerList, conn);
				}
				// 更改课室
				if (!section_c && !CommonStringUtil.isEmpty(newForm.getSection_id())) {
					featureService.changeSection(newForm.getMaterial_id(), newForm.getSection_id(), conn);
				}
			} else if (check == 1) {
				MsgInfo info = new MsgInfo();
				info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.modify.working")); // 无法修改当前等级或课时
				errors.add(info);
			} else if (check == 2) {
				MsgInfo info = new MsgInfo();
				info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.modify.otherbreak")); // 当前维修对象尚有未通知到您处的不良中断存在，请等待这些中断被处理。
				errors.add(info);
			}
		}

		if (triggerList.size() > 0 && errors.size() == 0) {
			conn.commit();
			RvsUtils.sendTrigger(triggerList);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("MaterialAction.doUpdate end");
	}

	public void doInsert(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) {
		log.info("MaterialAction.doInsert start");
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v != null ? v.validate(): new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			materialService.insert(form, conn);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("MaterialAction.doInsert end");
	}

	/**
	 * 等级/课室/工艺 变更时，确认是否存在进行中的工位
	 * @param oldForm
	 * @param newForm
	 * @param id
	 * @param conn
	 * @return
	 */
	private int checkToUpdate(MaterialForm oldForm, MaterialForm newForm, String id, SqlSessionManager conn){
		String oldSectionId = oldForm.getSection_id();
		String newSectionId = newForm.getSection_id();
		String oldLevel = oldForm.getLevel();
		String newLevel = newForm.getLevel();
		String oldPatID = oldForm.getPat_id();
		String newPatID = newForm.getPat_id();

		if (!newSectionId.equals(oldSectionId) || !newLevel.equals(oldLevel) || !newPatID.equals(oldPatID)) {
			log.info("OLD LEVEL :" + oldLevel + " NEW LEVEL :" + newLevel);
			log.info("OLD SECTION :" + oldSectionId + " NEW SECTION :" + newSectionId);
			log.info("OLD PAT :" + oldPatID + " NEW PAT :" + newPatID);
			int result = featureService.checkOperateResult(id, conn);

			if (result != 0) {
				return 1;
			}

			result = featureService.checkOtherBreak(id, conn);

			if (result != 0) {
				return 2;
			}
		}

		return 0;
	}

	private int chooseCase(HttpSession session, boolean partial, boolean process) {
		List<Integer> privacies = getPrivacies(session);

		if (privacies.contains(RvsConsts.PRIVACY_OVEREDIT) && privacies.contains(1)) {
			return 4; //汇总操作，系统管理员
		} else if (privacies.contains(RvsConsts.PRIVACY_SCHEDULE) && process) {
			return 2; //计划操作
		} else if (privacies.contains(RvsConsts.PRIVACY_FACT_MATERIAL) && partial) {
			return 3; //现品操作
		} else if (privacies.contains(RvsConsts.PRIVACY_OVEREDIT)) {
			return 1; //汇总操作
		} else {
			return 0; //详细状态
		}
	}

	private List<Integer> getPrivacies(HttpSession session) {
		LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = loginData.getPrivacies();

		return privacies;
	}

	/**
	 * 小票打印
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void printTicket(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("MaterialAction.printTicket start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		List<MsgInfo> infoes = new ArrayList<MsgInfo>();

		// 取得所选维修对象信息
		MaterialService mService = new MaterialService();
		List<String> ids = new ArrayList<String>();
		ids.add(req.getParameter("material_id"));
		List<MaterialEntity> mBeans = mService.loadMaterialDetailBeans(ids, conn);

		int operator = req.getParameter("quotator") == null ? RvsConsts.TICKET_ADDENDA : RvsConsts.TICKET_QUTOTAOR;

		if (mBeans.size() > 0) {
			DownloadService dService = new DownloadService();
			String filename = dService.printTicket(mBeans.get(0), conn, operator);
			callbackResponse.put("tempFile", filename);
		} else {
			MsgInfo info = new MsgInfo();
			info.setErrcode("");
			info.setErrmsg("");
			infoes.add(info);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", infoes);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("MaterialAction.printTicket end");
	}

	/**
	 * 线长填写工程检查票
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={106})
	public void dowritepcs(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("MaterialAction.dowritepcs start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		materialService.saveLeaderInput(req, user, conn);
		// service.switchLeaderExpedite(material_id, line_id, conn);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.dowritepcs end");
	}

	/**
	 * 管理员修改工程检查票
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1})
	public void doFixPcs(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("MaterialAction.doFixPcs start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		materialService.fixInput(req, user, conn);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.doFixPcs end");
	}

	public void getFlowchart(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("MaterialAction.getFlowchart start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		// 维修对象
		String material_id = req.getParameter("material_id");
		String pat_id = req.getParameter("pat_id");

		ProcessAssignMapper pamDao = conn.getMapper(ProcessAssignMapper.class);

		MaterialMapper mDao = conn.getMapper(MaterialMapper.class);
		MaterialEntity mEntity = mDao.getMaterialEntityByKey(material_id);

		if (pat_id == null) {
			pat_id = mEntity.getPat_id();
		}

		boolean isLightFix = RvsUtils.isLightFix(mEntity.getLevel());

		// 取得流程-全在线
		List<ProcessAssignEntity> seqlist = pamDao.getProcessAssignByTemplateID(pat_id);
		List<ProcessAssignForm> palist = new ArrayList<ProcessAssignForm>();
		BeanUtil.copyToFormList(seqlist, palist, null, ProcessAssignForm.class);
		listResponse.put("processAssigns", seqlist);

		List<ProductionFeatureEntity> lEntities = null;

		List<Integer> privacies = user.getPrivacies();

		if (privacies.contains(RvsConsts.PRIVACY_PROCESSING)) {
			// 取得在先工程内工位-用于流程图
			List<Map<String, String>> pMaps = pamDao.getInlinePositions();
			listResponse.put("positions", pMaps);

			// 取得作业情报-工程部分
			lEntities = pamDao.getFinishedPositionsInline(material_id);

			for (Map<String, String> pMap : pMaps) {
				pMap.put("text", pMap.get("process_code") + " " + pMap.get("text"));
			}

		} else if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
			String line_id = user.getLine_id();

			// 取得工程内工位-用于流程图
			List<Map<String, String>> pMaps = pamDao.getPositionsOfLine(line_id);
			listResponse.put("positions", pMaps);

			// 取得作业情报-工程部分
			lEntities = pamDao.getFinishedPositionsByLine(material_id, line_id);
		}

		CopyOptions cos = new CopyOptions();
		cos.dateConverter("MM-dd HH:mm", "finish_time");
		cos.include("position_id", "operator_name", "finish_time");

		List<ProductionFeatureForm> lcf = new ArrayList<ProductionFeatureForm>();
		BeanUtil.copyToFormList(lEntities, lcf, cos, ProductionFeatureForm.class);
		listResponse.put("result", lcf);
		listResponse.put("pat_id", pat_id);

		if (isLightFix) {
			
			listResponse.put("isLightFix", isLightFix);

			MaterialProcessAssignService mpas = new MaterialProcessAssignService();
			String lightFixStr = mpas.getLightFixesByMaterial(material_id, null, conn);
			if (!isEmpty(lightFixStr)) {
				String light_fix_content = "中小修理的修理内容是：" + lightFixStr;
				listResponse.put("light_fix_content", light_fix_content);
			}

			List<String> light_positions = mpas.getLightPositionsByMaterial(material_id, conn);
			listResponse.put("light_positions", light_positions);

			Set<String> ccdModels = RvsUtils.getCcdModels(conn);
			if (ccdModels != null && ccdModels.contains(mEntity.getModel_id())) {
				listResponse.put("isCcdModel", true);
			}

			// LG 目镜对应机型
			ModelService ms = new ModelService();
			ModelForm model = ms.getDetail(mEntity.getModel_id(), conn);
			if (model.getKind().equals("01")) {
				listResponse.put("isLgModel", true);
			}
			ccdModels = RvsUtils.getCcdLineModels(conn);
			if (ccdModels != null && ccdModels.contains(mEntity.getModel_id())) {
				listResponse.put("isCcdLineModel", true);
			}

			List<MaterialProcessAssignForm> processAssigns = mpas.searchMaterialProcessAssign(form, conn);
			listResponse.put("mProcessAssigns", processAssigns);
		}

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.getFlowchart end");
	}

	/**
	 * 查询结果导出
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void export(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("MaterialAction.export start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		MaterialService materialService = new MaterialService();

		@SuppressWarnings("unchecked")
		List<MaterialForm> lResultForm = (ArrayList<MaterialForm>) session.getAttribute(RvsConsts.SEARCH_RESULT);
		Date today = new Date();
		String filePath = "Chaxunjieguo" + user.getJob_no() + today.getTime() + ".xls";
		String fileFullPath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM") + "\\" + filePath;

		materialService.createReport(fileFullPath, lResultForm);

		listResponse.put("filePath", filePath);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.export end");
	}

	/**
	 * 月档案详细一览
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param request 页面请求
	 * @param response 页面响应
	 * @param conn 数据库会话
	 * @throws Exception Exception
	 */
	public void searchMonthFiles(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSession conn) throws Exception{
		log.info("MaterialAction.searchMonthFiles start");

		Map<String, Object> listResponse = new HashMap<String, Object>();


		List<MonthFilesDownloadForm> filesList = materialService.getMonthFiles();

		listResponse.put("filesList", filesList);

		//返回Json格式响应信息
		returnJsonResponse(response, listResponse);

		log.info("MaterialAction.searchMonthFiles end");
	}

	/**
     * 月档案点击下载
     * @param mapping ActionMapping
     * @param form 表单
     * @param req 页面请求
     * @param res 页面响应
     * @param conn 数据库会话
     * @return
     * @throws Exception
     */
	public ActionForward output(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res, SqlSession conn) throws Exception {
    	log.info("MaterialAction.output start");

		String fileName =req.getParameter("fileName");

		String contentType = DownloadService.CONTENT_TYPE_ZIP;
		if (CommonStringUtil.isEmpty(fileName)) {
			fileName = RvsUtils.charRecorgnize(req.getParameter("filePath")); // new String(fileName.getBytes("iso-8859-1"),"UTF-8");
		}else{
			fileName = RvsUtils.charRecorgnize(fileName);
		}

		String filePath = "";
		filePath = PathConsts.BASE_PATH + PathConsts.PCS+"\\_monthly\\"+fileName;

		res.setHeader( "Content-Disposition", "attachment;filename=" + RvsUtils.charUrlEncode(fileName)); // 
		res.setContentType(contentType);
		File file = new File(filePath);
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		int avail = is.available();
		byte[] buffer = null;
		if (avail <= 268435456) { // 268435456 = 256 m
			buffer = new byte[avail];
			is.read(buffer);
			is.close();

			OutputStream os = new BufferedOutputStream(res.getOutputStream());
			os.write(buffer);
			os.flush();
			os.close();

		} else {

		    buffer = new byte[40960];
		    int n = 0;
			OutputStream os = new BufferedOutputStream(res.getOutputStream());
		    while (-1 != (n = is.read(buffer))) {
				os.write(buffer, 0, n);
		    }
			is.close();

			os.flush();
			os.close();
		}

		log.info("MaterialAction.output end");
		return null;
	}

	public void getMaterialComment(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("MaterialAction.getMaterialComment start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String material_id = req.getParameter("material_id");
		String write = req.getParameter("write");
		String operator_id = user.getOperator_id();

		materialService.getMaterialCommentEdit(material_id, operator_id, listResponse, !"0".equals(write), conn);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.getMaterialComment end");
	}

	/**
	 * 维修对象备注
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={104,105,106})
	public void doUpdateMaterialComment(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("MaterialAction.doUpdateMaterialComment start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String material_id = req.getParameter("material_id");
		String operator_id = user.getOperator_id();
		String comment = req.getParameter("comment");

		materialService.updateMaterialComment(material_id, operator_id, comment, conn);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.doUpdateMaterialComment end");
	}

	/**
	 * 分线切换
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={104,105,106})
	public void doPxExchange(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("MaterialAction.doPxExchange start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		String material_id = req.getParameter("material_id");
		String position_id = req.getParameter("position_id");
		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		String line_id = req.getParameter("line_id");
		if (line_id == null) {
			line_id = user.getLine_id();
		}

		MaterialProcessService mpService = new MaterialProcessService();
		mpService.pxExchange(material_id, line_id, conn);

		if (position_id != null) {

			String section_id = user.getSection_id();

			PositionPanelService ppService = new PositionPanelService();
			ppService.notifyPosition(section_id, position_id, material_id);
		}

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.doPxExchange end");
	}

	/**
	 * 指定CDD盖玻璃工作
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={105})
	public void doreccd(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{

		log.info("MAterialAction.doreccd start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<String> triggerList = new ArrayList<String>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ONLYKEY);
		List<MsgInfo> errors = v.validate();

		String material_id = req.getParameter("material_id");

		ProductionFeatureService service = new ProductionFeatureService();

		ProductionFeatureEntity workingPf = new ProductionFeatureEntity();
		workingPf.setPosition_id("00000000025");
		workingPf.setSection_id("00000000001"); // 暂时固定 TODO
		workingPf.setRework(service.getReworkCountWithLine(material_id, null, conn));

		// 检查本次返工中有没有做过CCD盖玻璃更换
		if (service.checkPositionDid(material_id, "00000000025", null, "" + workingPf.getRework(), conn)) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("material_id");
			error.setErrcode("info.linework.accessedCcdInRework");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.accessedCcdInRework"));
			errors.add(error);
		} else {
			service.removeWorking(material_id, "00000000025", conn);
			// 重做
			service.fingerSpecifyPosition(material_id, true, workingPf, triggerList, conn);

			if (triggerList.size() > 0 && errors.size() == 0) {
				conn.commit();
				RvsUtils.sendTrigger(triggerList);
			}
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MAterialAction.doreccd end");
	}

	/**
	 * 工时统计
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void worktimeExport(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("MaterialAction.worktimeExport start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		MaterialService materialService = new MaterialService();
		String fileName ="工时统计.xls";
		String filePath = materialService.worktimeExport(req,conn,errors);

		listResponse.put("fileName", fileName);
		listResponse.put("filePath", filePath);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialAction.worktimeExport end");
	}

	/**
	 * 小票打印
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={100})
	public void doPrintProductTickets(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{ 
		log.info("MaterialAction.printProductionTicket start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 补打
		String addition = req.getParameter("addition"); 

		// 取得所选维修对象信息
		ProductService pService = new ProductService();
		List<MaterialEntity> mBeans = pService.getModelSerials(req.getParameterMap());

		String model_id = null;

		if (mBeans.size() > 0) {
			List<String> ids = new ArrayList<String>();
			String id = null;

			LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

			for (MaterialEntity mBean : mBeans) {
				if (addition == null) {
					id = mBean.getMaterial_id();	
				} else {
					if (model_id == null) {
						model_id = ReverseResolution.getModelByName(mBean.getModel_name(), conn);
					}
					id = pService.getIdBySerialWithModelCheck(model_id, mBean.getSerial_no(), 
							addition.equals("change"), conn, errors);
				}

				if (id == null && errors.size() == 0) {
					mBean.setFix_type(RvsConsts.PROCESS_TYPE_MANUFACT_LINE);
					id = pService.insertProduct(mBean, user.getSection_id(), conn);
					if (mBeans.size() == 1) {
						callbackResponse.put("id", id);
					}
				}

				ids.add(id);
			}

			if (errors.size() == 0) {
				DownloadService dService = new DownloadService();
				String filename = dService.printSerialTickets(mBeans, conn);
				callbackResponse.put("tempFile", filename);

				// 更新维修对象小票打印标记
				MaterialMapper mdao = conn.getMapper(MaterialMapper.class);
				mdao.updateMaterialTicket(ids);
			} else {
				conn.rollback();
			}

		} else {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setErrcode("info.product.noAccessObject");
			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.noAccessObject"));
			errors.add(msgInfo);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("MaterialAction.printProductionTicket end");
	}


	/**
	 * 没开始的产品型号改动
	 * 
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={106,107})
	public void doSetNewProductModel(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{ 
		log.info("MaterialAction.doSetNewProductModel start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		ProductService pService = new ProductService();

		pService.setNewProductModel(req.getParameter("model_id"), conn);

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("MaterialAction.doSetNewProductModel end");
	}

	public void refreshSerialNos(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{ 
		log.info("MaterialAction.refreshSerialNos start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		ProductService pService = new ProductService();

		callbackResponse.put("serialNos", pService.getSerialNos(5, 15, conn));

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("MaterialAction.refreshSerialNos end");
	}


	/**
	 * 取得制品型号信息
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void getForProductSerial(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("MaterialAction.getForSerial start");

		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", new ArrayList<MsgInfo>());

		ProductService pService = new ProductService();
		callbackResponse.put("serial_no", pService.getSerialMonthlyPrefix());

		// 部组，取得可制作的型号
		ModelService mdlService = new ModelService();
		ModelEntity mdlEntity = new ModelEntity();
		mdlEntity.setKind("11");
		callbackResponse.put("modelOptions", mdlService.searchToSelectOptions(mdlEntity, conn));

		// 取得作业类别
		callbackResponse.put("fixTypeOptions", CodeListUtils.getSelectOptions("material_fix_type_manufact"));


		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		log.info("MaterialAction.getForSerial end");
	}

	public void doReaccpect(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{ 
		log.info("MaterialAction.doReaccpect start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		MaterialService mService = new MaterialService();
		ProductService pService = new ProductService();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		String section_id = user.getSection_id();
		if (section_id == null) {
			MsgInfo info = new MsgInfo();
			info.setErrcode("info.product.withoutSection");
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.withoutSection"));
			errors.add(info);
		}

		MaterialForm createForm = (MaterialForm) form;

		String existId = mService.checkModelSerialNo(createForm, conn); // 规则有待确定（不同型号可否用相同序列号？TODO）

		if (existId != null) {
			String productName = CodeListUtils.getValue("material_fix_type_manufact", createForm.getFix_type());

			MsgInfo info = new MsgInfo();
			info.setErrcode("dbaccess.columnNotUnique");
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "机身号", 
					createForm.getSerial_no(), productName));
			errors.add(info);
		}

		if (errors.size() == 0) {
			pService.reaccpect(form, section_id, conn);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("MaterialAction.doReaccpect end");
	}
}
