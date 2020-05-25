/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：工位平台事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action.inline;

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
import com.osh.rvs.bean.data.AlarmMesssageEntity;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.inline.SoloProductionFeatureEntity;
import com.osh.rvs.bean.master.ModelEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.PcsUtils;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.form.inline.SnoutForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.inline.SoloProductionFeatureMapper;
import com.osh.rvs.service.AlarmMesssageService;
import com.osh.rvs.service.CheckResultPageService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.PauseFeatureService;
import com.osh.rvs.service.inline.PositionPanelService;
import com.osh.rvs.service.inline.SoloSnoutService;
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


public class PositionPanelSnoutAction extends BaseAction {

	private static final String SNOUT_MODEL_EXCLUDED = "0";
	private static final String SNOUT_MODEL_INCLUDED = "1";
	private static final String SNOUT_MODEL_INCLUDED_AND_REWORKED = "2";
	private static final String WORK_STATUS_FORBIDDEN = "-1";

	private Logger log = Logger.getLogger(getClass());

	private PositionPanelService service = new PositionPanelService();
	private SoloSnoutService sservice = new SoloSnoutService();
	private PauseFeatureService bfService = new PauseFeatureService();

	/**
	 * 判断是否能进入的处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void entrance(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("PositionPanelSnoutAction.entrance start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		String req_line_no = req.getParameter("line_id");
		// 取得处理状态
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		if (CommonStringUtil.isEmpty(user.getLine_id())) {
			// 选择的不是本工程的作业
		} else if (req_line_no == null || !req_line_no.equals(user.getLine_id())) {
			// 进入的页面不是选择的工程
		}

		// String position_id = user.getPosition_id();

		listResponse.put("checkToken", errors); //TODO random

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.entrance end");
	}

	/**
	 * 工位画面初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("PositionPanelSnoutAction.init start");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String section_id = user.getSection_id();
		String position_id = user.getPosition_id();
		String level = user.getPx();

		// 取得工位信息
		req.setAttribute("position", service.getPositionMap(section_id, position_id, level, conn));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("PositionPanelSnoutAction.init end");
	}

	/**
	 * 工位画面初始取值处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void jsinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("PositionPanelSnoutAction.jsinit start");
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		List<MsgInfo> infoes = new ArrayList<MsgInfo>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String section_id = user.getSection_id();
		String process_code = user.getProcess_code();
		String position_id = user.getPosition_id();
		String line_id = user.getLine_id();

		CheckResultPageService crService = new CheckResultPageService();
		crService.checkForPosition(section_id, position_id, line_id, conn);

		PositionPanelService ppservice = new PositionPanelService();
		String infectString = ppservice.getInfectMessageByPosition(section_id,
				position_id, line_id, conn);

		callbackResponse.put("infectString", infectString);

		if (infectString.indexOf("限制工作") >= 0) {
			callbackResponse.put("workstauts", WORK_STATUS_FORBIDDEN);
		} else {

			String stepOptions = "";
			// 设定正常中断选项
			String steps = PathConsts.POSITION_SETTINGS.getProperty("steps."+process_code);
			if (steps != null) {
				String[] steparray = steps.split(",");
				for (String step : steparray) {
					step = step.trim();
					String stepname = PathConsts.POSITION_SETTINGS.getProperty("step." + process_code + "." + step);
					stepOptions += "<option value=\"" + step + "\">" + stepname + "</option>";
				}
			}
			callbackResponse.put("stepOptions", stepOptions);

			String breakOptions = "";

			// 设定异常中断选项
			steps = PathConsts.POSITION_SETTINGS.getProperty("break."+process_code);
			if (steps != null) {
				String[] steparray = steps.split(",");
				for (String step : steparray) {
					step = step.trim();
					String stepname = PathConsts.POSITION_SETTINGS.getProperty("break." + process_code + "." + step);
					breakOptions += "<option value=\"" + step + "\">" + stepname + "</option>";
				}
			}
			// 设定一般中断选项
			breakOptions += CodeListUtils.getSelectOptions("break_reason", null);

			callbackResponse.put("breakOptions", breakOptions);

			// 设定暂停选项
			String pauseOptions = "";

			pauseOptions += PauseFeatureService.getPauseReasonSelectOptions();
			callbackResponse.put("pauseOptions", pauseOptions);

			// 取得等待区一览
			callbackResponse.put("waitings", sservice.getWaitingMaterial(section_id, user.getPosition_id(),
					user.getOperator_id(), process_code, conn));

			if ("301".equals(process_code)) {
				// 先端预制，取得可制作的型号
				callbackResponse.put("modelOptions", CodeListUtils.getSelectOptions(RvsUtils.getSnoutModels(conn), "", "(未选择)", false));
			} else if ("001".equals(process_code)) {
				// 部组，取得可制作的型号
				ModelService mdlService = new ModelService();
				ModelEntity mdlEntity = new ModelEntity();
				mdlEntity.setKind("11");
				callbackResponse.put("modelOptions", mdlService.searchToSelectOptions(mdlEntity, conn));

				ProductService pService = new ProductService();
				callbackResponse.put("serialNos", pService.getSerialNos(5, 15, conn));

				callbackResponse.put("todayAccessaries", sservice.searchTodayAccessaries(conn));
			} else {
				// 设备附件，取得可制作的型号
				callbackResponse.put("modelOptions", CodeListUtils.getSelectOptions(RvsUtils.getAccessoriesModels(conn), "", "(未选择)", false));
			}

			SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);
			SoloProductionFeatureEntity pfBean = new SoloProductionFeatureEntity();
			pfBean.setOperator_id(user.getOperator_id());
			pfBean.setPosition_id(user.getPosition_id());
			pfBean.setAction_time_null(0);
			pfBean.setFinish_time_null(1);

			// 判断是否有在进行中的维修对象
			List<SoloProductionFeatureEntity> workingPfs = dao.searchSoloProductionFeature(pfBean);

			// 进行中的话
			if (workingPfs.size() > 0) {
				SoloProductionFeatureEntity workingPf = workingPfs.get(0);
				if (RvsConsts.OPERATE_RESULT_SUPPORT == workingPf.getOperate_result()) {
					MsgInfo msginfo = new MsgInfo();
					msginfo.setErrcode("info.linework.supportingRemain");
					msginfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.supportingRemain"));
					infoes.add(msginfo);
					callbackResponse.put("redirect", "support.do");
				} else if (RvsConsts.OPERATE_RESULT_WORKING == workingPf.getOperate_result()) {
					// 取得作业信息
					// getProccessingData(listResponse, workingPf.getMaterial_id(), workingPf, user, conn);

					// 取得工程检查票
					getPcses(callbackResponse, workingPf, user.getLine_id(), conn);

					// 取得本先端头第一次作业 的开始时间 TODO
					callbackResponse.put("action_time", DateUtil.toString(sservice.getFirstPaceOnRework(workingPf, conn), "HH:mm:ss"));

					// 取得标准时间和作业经过时间
					sservice.getProccessingDataSolo(callbackResponse, workingPf, user, conn);

					callbackResponse.put("snout_origin", sservice.getSnoutOriginNoBySerialNo(workingPf.getSerial_no() ,conn));
					callbackResponse.put("model_name", workingPf.getModel_name());
					callbackResponse.put("serial_no", workingPf.getSerial_no());

					// 页面设定为编辑模式
					callbackResponse.put("workstauts", "1");

				} else if (RvsConsts.OPERATE_RESULT_PAUSE == workingPf.getOperate_result()) {
					// 暂停中的话
	//				// 取得作业信息
	//				getProccessingData(listResponse, pauseingPf.getMaterial_id(), pauseingPf, user, conn);

					// 取得工程检查票
					getPcses(callbackResponse, workingPf, user.getLine_id(), conn);

					//spent_mins
					callbackResponse.put("action_time", DateUtil.toString(workingPf.getAction_time(), "HH:mm:ss"));

					// 取得标准时间和作业经过时间
					pfBean.setModel_name(workingPf.getModel_name());
					sservice.getProccessingDataSolo(callbackResponse, workingPf, user, conn);

					callbackResponse.put("snout_origin", sservice.getSnoutOriginNoBySerialNo(workingPf.getSerial_no() ,conn));
					callbackResponse.put("model_name", workingPf.getModel_name());
					callbackResponse.put("serial_no", workingPf.getSerial_no());

					// 页面设定为编辑模式
					callbackResponse.put("workstauts", "2");

				}
			}
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", infoes);

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		log.info("PositionPanelSnoutAction.jsinit end");
	}

	@Privacies(permit={0})
	public void dostart(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("PositionPanelSnoutAction.start start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String model_id = req.getParameter("model_id");
		String material_id = req.getParameter("material_id");
		String model_name = req.getParameter("model_name").replace(' ', (char)0x20); // TODO 20 -> 3F???
		String serial_no = req.getParameter("serial_no");

		listResponse.put("model_name", model_name);
		listResponse.put("serial_no", serial_no);

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		SoloProductionFeatureMapper spfDao = conn.getMapper(SoloProductionFeatureMapper.class);

		String process_code = user.getProcess_code();
		String object_name = "", module_name = "";
		String kind = "";

		if ("301".equals(process_code)) {
			object_name = "先端头"; module_name = "先端预制";
		} else if ("001".equals(process_code)) {
			object_name = "部组"; module_name = "部组组装"; kind = "11";
		} else {
			object_name = "设备附件"; module_name = "周边设备附件修理"; kind = "07";
		}

		int pace = -1;
		if (CommonStringUtil.isEmpty(model_id)) {
			MsgInfo msg = new MsgInfo();
			msg.setComponentid("model_id");
			msg.setErrcode("validator.required");
			msg.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", object_name + "型号"));
			errors.add(msg);
		}
		if (CommonStringUtil.isEmpty(serial_no)) {
			if ("301".equals(process_code)) {
				// 先端预制必须输入
				MsgInfo msg = new MsgInfo();
				msg.setComponentid("snout_no");
				msg.setErrcode("validator.required");
				msg.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", object_name + "序列号"));
				errors.add(msg);
			} else {
				// 先端预制以外取得序列号
				List<MaterialEntity> snoutsByMonth = new ArrayList<MaterialEntity>();
				serial_no = sservice.loadSnoutsByMonth(snoutsByMonth, kind, conn);
				if (serial_no == null) {
					// 取不到，请输入
					MsgInfo msg = new MsgInfo();
					msg.setComponentid("snout_no");
					msg.setErrcode("validator.required");
					msg.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", object_name + "序列号"));
					errors.add(msg);
				} else {
					listResponse.put("serial_no", serial_no);
				}
			}
		} else {
			if (serial_no.length() != 7) {
				MsgInfo msg = new MsgInfo();
				msg.setComponentid("snout_no");
				msg.setErrcode("validator.invalidParam.invalidMaxLengthValue");
				msg.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidJustLengthValue", object_name + "序列号", "7"));
				errors.add(msg);
			}
			SoloProductionFeatureEntity pfBean = new SoloProductionFeatureEntity();
			pfBean.setSerial_no(serial_no);
			pfBean.setPosition_id(user.getPosition_id());
			List<SoloProductionFeatureEntity> resultBeans = spfDao.searchSoloProductionFeature(pfBean);
			for (SoloProductionFeatureEntity resultBean : resultBeans) {
				if (resultBean.getOperate_result() == 2 || !model_name.equals(resultBean.getModel_name())) {
					MsgInfo msg = new MsgInfo();
					msg.setComponentid("snout_no");
					msg.setErrcode("dbaccess.columnNotUnique");
					msg.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", object_name + "序列号", serial_no, module_name + "作业"));
					errors.add(msg);
					break;
				}
				if (resultBean.getPace() > pace) pace = resultBean.getPace();
			}
		}

		if (errors.size() == 0) {
//			getProccessingData(listResponse, material_id, waitingPf, user, conn);
//
			SoloProductionFeatureEntity pfBean = new SoloProductionFeatureEntity();
			pfBean.setPosition_id(user.getPosition_id()); // 301
			pfBean.setModel_id(model_id);
			pfBean.setOperator_id(user.getOperator_id());
			pfBean.setSerial_no(serial_no);

			if (pace == -1) {
				// 登录先端头来源
				if (material_id != null) {
					sservice.registSnoutOrigin(material_id, serial_no, conn);
					listResponse.put("spent_mins", "0");
					listResponse.put("snout_origin", sservice.getSnoutOriginNoBySerialNo(serial_no ,conn));
				} else {
					pfBean.setModel_name(model_name);
					pfBean.setUse_seconds(0);
					sservice.getProccessingDataSolo(listResponse, pfBean, user, conn);
				}

				// 建立独立作业记录，作业中
				pfBean.setModel_name(model_name);
				pfBean.setPace(0);
				pfBean.setOperate_result(RvsConsts.OPERATE_RESULT_WORKING);
				pfBean.setSection_id(user.getSection_id());

				spfDao.insert(pfBean);
			} else {
				// 中断后继续
				pfBean.setPace(pace);
				sservice.pauseToResume(pfBean, conn);
				pfBean.setModel_name(model_name);
				pfBean.setSection_id(user.getSection_id());

				sservice.getProccessingDataSolo(listResponse, pfBean, user, conn);

				listResponse.put("snout_origin", sservice.getSnoutOriginNoBySerialNo(serial_no ,conn));
			}

			// 如果等待中信息是暂停中，则结束掉暂停记录(有可能已经被结束)
			// 只要开始做，就结束掉本人所有的暂停信息。
			bfService.finishPauseFeature(null, user.getSection_id(), user.getPosition_id(), user.getOperator_id(), serial_no, conn);

			// 取得工程检查票
			getPcses(listResponse, pfBean, user.getLine_id(), conn);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.start end");
	}

	@Privacies(permit={0})
	/**
	 * 扫描件（先端来源/周边本体）
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void checkScan(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("PositionPanelSnoutAction.checkScan start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String material_id = req.getParameter("material_id");
		MaterialForm mForm = null;
		
		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		switch(user.getProcess_code().charAt(0)) {
		case '3' : {
			mForm = sservice.checkOrigin(material_id, conn, errors); 
			if (errors.size() == 0) {
				List<MaterialEntity> snoutsByMonth = new ArrayList<MaterialEntity>();

				// 取得本月先端管理列表
				if (mForm.getSerial_no() == null) {
					String manage_serial_no = sservice.loadSnoutsByMonth(snoutsByMonth, "01", conn);
					mForm.setSerial_no(manage_serial_no);
				} else {
					listResponse.put("Continue", true);
				}

				// 返回
				listResponse.put("snoutsByMonth", snoutsByMonth);
			}
			break;
		}
		case '8' : {
			mForm = sservice.checkBody(material_id, conn, errors); 
			Map<String, List<String>> accessoriesModelAccessories = RvsUtils.getAccessoriesModelAccessories(conn);
			listResponse.put("accessoriesModels", accessoriesModelAccessories.get(mForm.getModel_id()));
			break;
		}
		}

		if (errors.size() == 0) {
			listResponse.put("mForm", mForm);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.checkScan end");
	}

	/**
	 * 取得工程检查票
	 * @param listResponse
	 * @param pf
	 * @param user
	 * @param conn
	 */
	public static void getPcses(Map<String, Object> listResponse, SoloProductionFeatureEntity pf, String sline_id,
			SqlSession conn) {
		List<Map<String, String>> pcses = new ArrayList<Map<String, String>>();

		String[] showLines = new String[1];
		String processCode = null;
		if ("00000000013".equals(sline_id)) {
			showLines[0] = "NS 工程";
			processCode = "301";
		} else {
			showLines[0] = "检查卡";
			processCode = "812";
		}

		for (String showLine : showLines) {
			Map<String, String> fileTempl = PcsUtils.getXmlContents(showLine, pf.getModel_name(), null, conn);

			Map<String, String> fileTemplSolo = new HashMap<String, String>();
			for (String key : fileTempl.keySet()) {
				if (key.contains("先端预制") || key.contains("检查卡")) {
					fileTemplSolo.put(key, fileTempl.get(key));
					break;
				}
			}

			Map<String, String> fileHtml = PcsUtils.toHtmlSnout(fileTemplSolo,
					pf.getModel_name(), pf.getSerial_no(), processCode, null, conn);

			fileHtml = RvsUtils.reverseLinkedMap(fileHtml);
			pcses.add(fileHtml);
		}

		listResponse.put("pcses", pcses);
	}

	/**
	 * 作业暂停
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void dopause(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("PositionPanelSnoutAction.dopause start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		// 取得当前作业中作业信息
		SoloProductionFeatureEntity condition = new SoloProductionFeatureEntity();
		condition.setOperator_id(user.getOperator_id());
		condition.setPosition_id(user.getPosition_id());
		condition.setAction_time_null(0);
		condition.setFinish_time_null(1);
		condition.setUsed(0);

		SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);
		List<SoloProductionFeatureEntity> workings = dao.searchSoloProductionFeature(condition);

		if (workings.size() > 0) {
			// 取得当前作业中作业信息
			SoloProductionFeatureEntity sworkingPf = workings.get(0);

			// 作业信息状态改为，暂停
			sworkingPf.setOperate_result(RvsConsts.OPERATE_RESULT_PAUSE);
			sworkingPf.setUse_seconds(null);
			dao.finish(sworkingPf);

			// 制作暂停信息
			bfService.createPauseFeature(sworkingPf, req.getParameter("reason"), req.getParameter("comments"), user.getSection_id(), null, conn);

			// 根据作业信息生成新的等待作业信息－－有开始时间（仅作标记用，重开时需要覆盖掉），说明是操作者原因暂停，将由本人重开。
			sservice.pauseToSelf(sworkingPf, conn);

			listResponse.put("action_time", DateUtil.toString(sworkingPf.getAction_time(), "HH:mm:ss"));

//			sservice.getProccessingDataSolo(listResponse, sworkingPf, user, conn);

			listResponse.put("workstauts", "2");
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.dopause end");
	}

	@Privacies(permit={0})
	public void doendpause(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("PositionPanelSnoutAction.doendpause start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String serial_no = req.getParameter("serial_no");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		// 得到暂停的维修对象，返回这一条作业信息
		SoloProductionFeatureEntity workwaitingPf = service.checkPausingSerialNo(serial_no, user, errors, conn);

		if (errors.size() == 0) {
			// getProccessingData(listResponse, material_id, workwaitingPf, user, conn);
			// 只要开始做，就结束掉本人所有的暂停信息。
			bfService.finishPauseFeature(null, user.getSection_id(), user.getPosition_id(), user.getOperator_id(), 
					serial_no, conn);

			// 作业信息状态改为，作业中
			SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);
			workwaitingPf.setOperate_result(RvsConsts.OPERATE_RESULT_WORKING);
			dao.pauseWaitProductionFeature(workwaitingPf);

			sservice.getProccessingDataSolo(listResponse, workwaitingPf, user, conn);

			listResponse.put("action_time", DateUtil.toString(workwaitingPf.getAction_time(), "HH:mm:ss"));
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.doendpause end");
	}

	/**
	 * 作业中断
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void dobreak(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("PositionPanelSnoutAction.dobreak start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String sReason = req.getParameter("reason");
		log.info("REASON:" + sReason);
		Integer iReason = null;

		try {
			iReason = Integer.parseInt(sReason.trim());
		} catch (Exception e) {
			// 维修对象不在用户所在等待区
			log.error("ERROR:" + e.getMessage());
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("reason");
			msgInfo.setErrcode("validator.invalidParam.invalidIntegerValue");
			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidIntegerValue", "中断代码"));
			errors.add(msgInfo);
		}

		// 正常中断判断工程检查票
		if (iReason > 70) {
			checkSnoutPcs(req.getParameter("serial_no"), req.getParameter("pcs_inputs"), errors);
		}

		if (errors.size() == 0) {
			// 取得当前作业中作业信息
			SoloProductionFeatureEntity condition = new SoloProductionFeatureEntity();
			condition.setOperator_id(user.getOperator_id());
			condition.setPosition_id(user.getPosition_id());
			condition.setAction_time_null(0);
			condition.setFinish_time_null(1);
			condition.setUsed(0);
			SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);
			List<SoloProductionFeatureEntity> workings = dao.searchSoloProductionFeature(condition);

			if (workings.size() > 0) {
				ProductionFeatureEntity workingPf = new ProductionFeatureEntity();
				SoloProductionFeatureEntity soloWorkingPf = workings.get(0);
				workingPf.setLine_id(user.getLine_id());
				workingPf.setSection_id(user.getSection_id());
				workingPf.setOperator_id(user.getOperator_id());
				workingPf.setPosition_id(user.getPosition_id());
				workingPf.setMaterial_id(soloWorkingPf.getSerial_no());

				String alarm_messsage_id = null;

				if (iReason <= 30) {
					// 制作中断警报
					AlarmMesssageService amservice = new AlarmMesssageService();
					AlarmMesssageEntity amEntity = amservice.createSoloBreakAlarmMessage(workingPf);
					amservice.createAlarmMessage(amEntity, user.getDepartment(), conn, false, null);

					// 取得插入的中断警报序号
					CommonMapper cDao = conn.getMapper(CommonMapper.class);
					alarm_messsage_id = cDao.getLastInsertID();
				}


				// 制作暂停信息
				bfService.createPauseFeature(workingPf, req.getParameter("reason"), req.getParameter("comments"), alarm_messsage_id, conn
						, soloWorkingPf.getSerial_no());

				if (iReason > 70) { // 业务流程-非直接工步操作

					// 作业信息状态改为，中断
					soloWorkingPf.setOperate_result(RvsConsts.OPERATE_RESULT_BREAK);
					soloWorkingPf.setUse_seconds(null);
					soloWorkingPf.setPcs_inputs(req.getParameter("pcs_inputs"));
					soloWorkingPf.setPcs_comments(req.getParameter("pcs_comments"));
					soloWorkingPf.setOperator_id(user.getOperator_id());
					soloWorkingPf.setSection_id(user.getSection_id());

					dao.breakWork(soloWorkingPf);

					// 根据作业信息生成新的等待作业信息－－无开始时间，说明进行非直接工步操作，回到等待区，可由他人接手
					sservice.breakToNext(soloWorkingPf, conn);

					// 通知 TODO
				} else if (iReason <= 30) { // 不良中断
					// 作业信息状态改为，中断
					soloWorkingPf.setOperate_result(RvsConsts.OPERATE_RESULT_BREAK);
					soloWorkingPf.setUse_seconds(null);
					soloWorkingPf.setOperator_id(user.getOperator_id());
					dao.breakWork(soloWorkingPf);
					soloWorkingPf.setSection_id(user.getSection_id());

					// 根据作业信息生成新的中断作业信息
					sservice.breakToNext(soloWorkingPf, conn);
				}
			}

			// 取得等待区一览
			listResponse.put("waitings", sservice.getWaitingMaterial(user.getSection_id(), user.getPosition_id(),
					user.getOperator_id(), user.getProcess_code(), conn));

		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.dobreak end");
	}

	private void checkSnoutPcs(String serial_no, String pcs_inputs, List<MsgInfo> errors) {
		if (pcs_inputs.contains("\"EI30162\"") && !pcs_inputs.contains(serial_no)) {
			MsgInfo msg = new MsgInfo();
			msg.setErrcode("info.scout.notWroteInOcs");
			msg.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.scout.notWroteInOcs"));
			errors.add(msg);
		}
	}

	@Privacies(permit={0})
	public void dofinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("PositionPanelSnoutAction.dofinish start");
		Map<String, Object> callBackResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String from_process_code = user.getProcess_code();

		// 取得当前作业中作业信息
		SoloProductionFeatureEntity condition = new SoloProductionFeatureEntity();
		condition.setOperator_id(user.getOperator_id());
		condition.setPosition_id(user.getPosition_id());
		condition.setAction_time_null(0);
		condition.setFinish_time_null(1);
		condition.setUsed(0);

		SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);
		List<SoloProductionFeatureEntity> workings = dao.searchSoloProductionFeature(condition);
		if (workings.size() > 0) {
			SoloProductionFeatureEntity workingPf = workings.get(0);

//			Integer use_seconds = service.getTotalTimeByRework(workingPf, conn);
			String pcs_inputs = req.getParameter("pcs_inputs");
			checkSnoutPcs(workingPf.getSerial_no(), pcs_inputs, errors);

			if (errors.size() == 0) {
				// 作业信息状态改为，作业完成
				workingPf.setOperate_result(RvsConsts.OPERATE_RESULT_FINISH);
				workingPf.setPcs_inputs(pcs_inputs);
				workingPf.setPcs_comments(req.getParameter("pcs_comments"));
				dao.finish(workingPf);
			}
		} else {
			log.error("作业已经被结束。");
		}

		// 计算一下总工时：
		// 取得本次工时
//		Integer use_seconds = workingPf.getUse_seconds();

//		/// 加上本次返工内本工位所用全部时间
//		use_seconds += service.getTotalTimeByRework(workingPf, conn);

		// 取得等待区一览
		callBackResponse.put("waitings", sservice.getWaitingMaterial(user.getSection_id(), user.getPosition_id(),
				user.getOperator_id(), from_process_code, conn));

		if (errors.size() == 0) {
			List<String> target_position_ids = new ArrayList<String>();
			if ("301".equals(from_process_code)) {
				target_position_ids.add("00000000028"); // 331
				target_position_ids.add("00000000081"); // 333
			} else if ("001".equals(from_process_code)) {
				target_position_ids.add("00000000102"); // 002
			}

			if (target_position_ids.size() > 0) {
				conn.commit();
				for (String target_position_id : target_position_ids) {
					service.notifyPosition(user.getSection_id(), target_position_id, null);
				}
			}
		}

		// 检查发生错误时报告错误信息
		callBackResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, callBackResponse);

		log.info("PositionPanelSnoutAction.dofinish end");
	}

	/**
	 * 取得维修对象可使用先端头信息
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void getMaterialUse(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("PositionPanelSnoutAction.getMaterialUse start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());
		String material_id = req.getParameter("material_id");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		String process_code = user.getProcess_code();

		// 取得维修对象信息
		MaterialService mservice = new MaterialService();
		MaterialEntity mBean = mservice.loadMaterialDetailBean(conn, material_id);
		String model_id = mBean.getModel_id();

		boolean included = false;
		String from_position_id = "";

		if ("801".equals(process_code)) {
			// 设备附件，取得可制作的型号
			included = RvsUtils.getUseAccessoriesModels(conn).containsKey(model_id);
		} else if ("002".equals(process_code)) {
			included = true;
			from_position_id = "00000000101";
		} else {
			included = RvsUtils.getSnoutModels(conn).containsKey(model_id);
			from_position_id = "00000000024";
		}

		// 判断维修对象型号是否可做先端预制
		if (included) {

			// 取得工作中维修对象
			ProductionFeatureEntity workingPf = service.getWorkingOrSupportingPf(user, conn);

			// 取得当前进行中Rework
			int rework = 0;
			if (workingPf != null)
				rework = workingPf.getRework();
			else {
				ProductionFeatureEntity pausingPf = service.getPausingPf(user, conn);
				if (pausingPf != null) {
					rework = pausingPf.getRework();
				}
			}

			listResponse.put("snout_model", SNOUT_MODEL_INCLUDED);
			SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);
			// 寻找维修对象已使用先端头
			List<ProductionFeatureEntity> used_snouts = dao.findUsedSnoutsByMaterial(material_id, from_position_id);

			boolean chosedInRework = false;
			List<String> serialNos = new ArrayList<String>();
			for (ProductionFeatureEntity used_snout : used_snouts) {
				if (used_snout.getRework() == rework) {
					chosedInRework = true;
				}
				serialNos.add(used_snout.getSerial_no());
			}
			String sUsedSnout = CommonStringUtil.joinBy("，", serialNos.toArray(new String[serialNos.size()]));

			if (!chosedInRework)  {
				if ("801".equals(process_code)) {
					// 设备附件，取得可制作的型号
					included = RvsUtils.getUseAccessoriesModels(conn).containsKey(model_id);
				} else if ("002".equals(process_code)) {
					// 得到refer
					ProductService psservice = new ProductService();
					String refer = psservice.getRefers(model_id, conn);
					listResponse.put("sReferChooser", refer);
				} else {
					// 得到refer
					String refer = sservice.getRefers(model_id, conn);
					listResponse.put("sReferChooser", refer);
				}
				listResponse.put("snout_model", SNOUT_MODEL_INCLUDED_AND_REWORKED);
			}
			listResponse.put("used_snout", sUsedSnout);

		} else {
			listResponse.put("snout_model", SNOUT_MODEL_EXCLUDED);
		}

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.getMaterialUse end");
	}

	/**
	 * 使用先端组件
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void dousesnout(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("PositionPanelSnoutAction.dousesnout start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		String serial_no = req.getParameter("serial_no");
		String from_process_code = req.getParameter("process_code");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		String this_process_code = user.getProcess_code();

		List<MsgInfo> infoes = new ArrayList<MsgInfo>();
		if (CommonStringUtil.isEmpty(serial_no)) {
			MsgInfo msginfo = new MsgInfo();
			msginfo.setErrcode("validator.required");
			msginfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "先端头序号"));
			infoes.add(msginfo);
		}

		String from_position_id = ReverseResolution.getPositionByProcessCode(from_process_code, conn);

		// 先端头线长确认
//		ProductionFeatureService pfService = new ProductionFeatureService();
//		String sGot = pfService.checkLpi(serial_no, "301", conn);
//		if (sGot == null) {
//			MsgInfo error = new MsgInfo();
//			error.setComponentid("serial_no");
//			error.setErrcode("info.linework.uncheckedSnout");
//			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.uncheckedSnout"));
//			infoes.add(error);
//		}

		// 确认先端头是否被使用 2016/4/22
		SnoutForm sForm = sservice.getDetail(serial_no, from_position_id, conn);
		if ("1".equals(sForm.getStatus())) { // 已被用掉，（竞合）
			// 取得工作中维修对象
			ProductionFeatureEntity workingPf = service.getWorkingOrSupportingPf(user, conn);
			MaterialService ms = new MaterialService();
			MaterialForm material = ms.loadSimpleMaterialDetail(conn, workingPf.getMaterial_id());

			// 得到refer
			if ("801".equals(this_process_code)) {
			} else if ("002".equals(this_process_code)) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("serial_no");
				error.setErrcode("info.product.usedAccessory");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.product.usedAccessory"));
				infoes.add(error);

				// 得到refer
				ProductService psservice = new ProductService();
				String refer = psservice.getRefers(material.getModel_id(), conn);
				listResponse.put("sReferChooser", refer);
			} else {
				MsgInfo error = new MsgInfo();
				error.setComponentid("serial_no");
				error.setErrcode("info.linework.usedSnout");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.usedSnout"));
				infoes.add(error);

				// 得到refer
				String refer = sservice.getRefers(material.getModel_id(), conn);
				listResponse.put("sReferChooser", refer);
			}
		}

		if (infoes.size() == 0) {
			// 取得工作中维修对象
			ProductionFeatureEntity workingPf = service.getWorkingOrSupportingPf(user, conn);
			if (workingPf == null) workingPf = service.getPausingPf(user, conn);
			String material_id = workingPf.getMaterial_id();

			SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);
			// 标准作业时间

			ProductionFeatureEntity pfBean = new ProductionFeatureEntity();
			pfBean.setSerial_no(serial_no);
			pfBean.setMaterial_id(material_id);
			pfBean.setRework(workingPf.getRework());
			pfBean.setSection_id(workingPf.getSection_id());
			pfBean.setPosition_id(workingPf.getPosition_id());

			dao.forbid(pfBean);

			SoloProductionFeatureEntity spfBean = new SoloProductionFeatureEntity();
			spfBean.setPosition_id(from_position_id);
			spfBean.setSerial_no(serial_no);
			Integer use_seconds = sservice.getTotalTime(spfBean, conn);
			pfBean.setUse_seconds(use_seconds);

			pfBean.setPosition_id(from_position_id);
			dao.use(pfBean);
			dao.useto(pfBean);
			dao.leaderuseto(pfBean);

			// 重新取得工程检查票
			MaterialMapper mdao = conn.getMapper(MaterialMapper.class);
			MaterialForm mform = new MaterialForm();
			MaterialEntity mbean = mdao.loadMaterialDetail(workingPf.getMaterial_id());
			BeanUtil.copyToForm(mbean, mform, CopyOptions.COPYOPTIONS_NOEMPTY);
			listResponse.put("mform", mform);

			if ("301".equals(from_process_code)) {
				PositionPanelService.getPcses(listResponse, workingPf, user.getLine_id(), conn);
			}

			listResponse.put("snout_model" , SNOUT_MODEL_INCLUDED);
			List<ProductionFeatureEntity> used_snouts = dao.findUsedSnoutsByMaterial(material_id, from_position_id);

			List<String> serialNos = new ArrayList<String>();
			for (ProductionFeatureEntity used_snout : used_snouts) {
				serialNos.add(used_snout.getSerial_no());
			}
			String sUsedSnout = CommonStringUtil.joinBy("，", serialNos.toArray(new String[serialNos.size()]));
			listResponse.put("used_snout" , sUsedSnout);

			// 取得维修对象的作业标准时间。
			String leagal_overline = RvsUtils.getZeroOverLine(mform.getModel_name(), mform.getCategory_name(), user, null);
			if ("301".equals(from_process_code)) {
				leagal_overline = "" + (Integer.parseInt(leagal_overline) - 15);
			}
			listResponse.put("leagal_overline", leagal_overline);

		} else {
			listResponse.put("snout_model" , SNOUT_MODEL_INCLUDED);
			listResponse.put("used_snout" , "");
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", infoes);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.dousesnout end");
	}

	/**
	 * 取消使用先端组件
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void dounusesnout(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("PositionPanelSnoutAction.dounusesnout start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String serial_no = req.getParameter("serial_no");
		String from_process_code = req.getParameter("process_code");
		String from_position_id = ReverseResolution.getPositionByProcessCode(from_process_code, conn);

		if (errors.size() == 0) {

			// 取得用户信息
			HttpSession session = req.getSession();
			LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
			String this_process_code = user.getProcess_code();
	
			// 取得工作中维修对象
			ProductionFeatureEntity workingPf = service.getWorkingOrSupportingPf(user, conn);
			String material_id = workingPf.getMaterial_id();
	
			// 取得当前进行中Rework
			int rework = workingPf.getRework();

			SoloProductionFeatureMapper dao = conn.getMapper(SoloProductionFeatureMapper.class);

			// 寻找维修对象已使用先端头
			List<ProductionFeatureEntity> used_snouts = dao.findUsedSnoutsByMaterial(material_id, from_position_id);
			for (ProductionFeatureEntity used_snout : used_snouts) {
				if (used_snout.getRework() == rework) {
					serial_no = used_snout.getSerial_no();
				}
			}

			dao.unuse(serial_no, from_position_id);
			dao.unuseto(workingPf.getMaterial_id(), "" + workingPf.getRework(), from_position_id);
	
			ProductionFeatureEntity pfBean = new ProductionFeatureEntity();
			pfBean.setSerial_no(serial_no);
			dao.leaderuseto(pfBean);
	
			// 重新取得工程检查票
			MaterialMapper mdao = conn.getMapper(MaterialMapper.class);
			MaterialForm mform = new MaterialForm();
			MaterialEntity mbean = mdao.loadMaterialDetail(material_id);
			BeanUtil.copyToForm(mbean, mform, CopyOptions.COPYOPTIONS_NOEMPTY);

			listResponse.put("mform", mform);
			if ("301".equals(from_process_code)) {
				PositionPanelService.getPcses(listResponse, workingPf, user.getLine_id(), conn);
			} else if ("001".equals(from_process_code)) {
			}

			listResponse.put("snout_model" , SNOUT_MODEL_INCLUDED);
			listResponse.put("used_snout" , "");
	
			// 得到refer
			// 得到refer
			if ("801".equals(this_process_code)) {
			} else if ("002".equals(this_process_code)) {
				// 得到refer
				ProductService psservice = new ProductService();
				String refer = psservice.getRefers(mbean.getModel_id(), conn);
				listResponse.put("sReferChooser", refer);
			} else {
				// 得到refer
				String refer = sservice.getRefers(mbean.getModel_id(), conn);
				listResponse.put("sReferChooser", refer);
			}

			// 取得维修对象在本工位暂停信息。TODO
	
			// 取得维修对象的作业标准时间。
			String leagal_overline = RvsUtils.getZeroOverLine(mform.getModel_name(), mform.getCategory_name(), user, null);
			listResponse.put("leagal_overline", leagal_overline);
		}

		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.dounusesnout end");
	}

	/**
	 * 取得最新序号
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void getSerial(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("PositionPanelSnoutAction.getSerial start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		List<MaterialEntity> snoutsByMonth = new ArrayList<MaterialEntity>();
		listResponse.put("serial_no", sservice.loadSnoutsByMonth(snoutsByMonth, "11", conn));

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelSnoutAction.getSerial end");
	}
}