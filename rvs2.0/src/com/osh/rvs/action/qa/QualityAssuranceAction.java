package com.osh.rvs.action.qa;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.AlarmMesssageEntity;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.infect.PeripheralInfectDeviceEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.common.FseBridgeUtil;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.PcsUtils;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.qa.QualityAssuranceMapper;
import com.osh.rvs.service.AlarmMesssageService;
import com.osh.rvs.service.CheckResultPageService;
import com.osh.rvs.service.MaterialProcessService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.PauseFeatureService;
import com.osh.rvs.service.PositionService;
import com.osh.rvs.service.ProductionFeatureService;
import com.osh.rvs.service.inline.ForSolutionAreaService;
import com.osh.rvs.service.inline.PositionPanelService;
import com.osh.rvs.service.product.ProductService;
import com.osh.rvs.service.qa.QualityAssuranceService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.message.ApplicationMessage;

public class QualityAssuranceAction extends BaseAction {
	private static String WORK_STATUS_FORBIDDEN = "-1";
	private static String WORK_STATUS_PREPAIRING = "0";
	private static String WORK_STATUS_WORKING = "1";
	private static String WORK_STATUS_PAUSING = "2";
	private static String WORK_STATUS_DECIDE = "1.5";
	private static String WORK_STATUS_DECIDE_PAUSING = "2.5";
//	private static String WORK_STATUS_CELL_WORKING = "1.9";
	private static String WORK_STATUS_PERIPHERAL_WORKING = "4";
	private static String WORK_STATUS_PERIPHERAL_PAUSING = "5";

	private Logger log = Logger.getLogger(getClass());

	private QualityAssuranceService service = new QualityAssuranceService();
	private PositionPanelService ppService = new PositionPanelService();
	private PauseFeatureService bfService = new PauseFeatureService();
	private ProductionFeatureService pfService = new ProductionFeatureService();

	/**
	 * 品保画面初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("QualityAssuranceAction.init start");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String process_code = user.getProcess_code();
		String position_id = user.getPosition_id();
		String position_name = user.getPosition_name();

		String qs_position_id = position_id;

		if (qs_position_id == null) {
			qs_position_id = RvsConsts.POSITION_QA;

			PositionService pservice = new PositionService();

			PositionEntity positionEntity = pservice.getPositionEntityByKey(qs_position_id, conn);
			position_name = positionEntity.getName();
			process_code = positionEntity.getProcess_code();

			user.setPosition_id(qs_position_id);
			user.setPosition_name(position_name);
			user.setProcess_code(process_code);
			session.setAttribute(RvsConsts.SESSION_USER, user);
		}

		req.setAttribute("qs_position_id", qs_position_id);
		req.setAttribute("qs_position_name", position_name);

		String special_forward = PathConsts.POSITION_SETTINGS.getProperty("page." + process_code);
		boolean isPeripheral = (special_forward != null && special_forward.indexOf("peripheral") >= 0);
		if (isPeripheral) {
			req.setAttribute("peripheral", true);
		}
		String privacy = "";
		boolean isLeader = user.getPrivacies().contains(RvsConsts.PRIVACY_LINE);
		if(isLeader && ("00000000015".equals(user.getLine_id()) || "00000000076".equals(user.getLine_id()))){
			privacy = "lineLeader";
		}
		if (RvsConsts.DEPART_MANUFACT.equals(user.getDepartment())) {
			// 制造为之后确认
			privacy = "lineLeader";
		}
		req.setAttribute("privacy", privacy);

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("QualityAssuranceAction.init end");
	}

	/**
	 * 品保画面初始取值处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void jsinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("QualityAssuranceAction.jsinit start");
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String qs_position_id = req.getParameter("position_id");
		service.listRefresh(callbackResponse, qs_position_id, user.getDepartment(), conn);

		// 设定OCM文字
		callbackResponse.put("oOptions", CodeListUtils.getGridOptions("material_ocm"));
		// 设定等级文字
		callbackResponse.put("lOptions", CodeListUtils.getGridOptions("material_level"));

		// 判断是否线长
		boolean isLeader = user.getPrivacies().contains(RvsConsts.PRIVACY_LINE);

		// 取得待点检信息
		String section_id = user.getSection_id();
		user.setSection_id(null);
		user.setPosition_id(qs_position_id);

		PositionService pservice = new PositionService();
		user.setProcess_code(pservice.getPositionEntityByKey(qs_position_id, conn).getProcess_code());

		if (RvsConsts.DEPART_REPAIR.equals(user.getDepartment())) {
			user.setLine_id("00000000015");
		}

		// 设定待点检信息
		CheckResultPageService crService = new CheckResultPageService();
		crService.checkForPosition(section_id, qs_position_id, user.getLine_id(), conn);

		PositionPanelService ppservice = new PositionPanelService();
		String infectString = ppservice.getInfectMessageByPosition(section_id,
				user.getPosition_id(), user.getLine_id(), conn);

		callbackResponse.put("infectString", infectString);
		String process_code = user.getProcess_code(); 
		// 判断是否有特殊页面效果
		String special_forward = PathConsts.POSITION_SETTINGS
				.getProperty("page." + process_code);

		if (infectString.indexOf("限制工作") >= 0) {
			callbackResponse.put("workstauts", WORK_STATUS_FORBIDDEN);
		} else {
			// 判断是否有在进行中的品保对象
			ProductionFeatureEntity workingPf = ppService.getWorkingPf(user, conn);

			// 进行中的话
			if (workingPf != null) {
				// 取得作业信息
				boolean qa_checked = service.getProccessingData(callbackResponse, workingPf.getMaterial_id(), workingPf, user, conn);

				MaterialService msevice = new MaterialService();
				MaterialEntity mBean = msevice.loadMaterialDetailBean(conn, workingPf.getMaterial_id());

				// 小修理共用大修理表单
				if ("612".equals(workingPf.getProcess_code())) {
					workingPf.setProcess_code("611");
				}

//				// 单元无工程检查票
//				if (mBean.getFix_type() == 2) {
//					callbackResponse.put("workstauts", WORK_STATUS_CELL_WORKING);
//				} else
				// 页面设定为编辑模式
				if (qa_checked) {
					callbackResponse.put("workstauts", WORK_STATUS_DECIDE);
				} else {
					boolean infectFinishFlag = true;
					if ("peripheral".equals(special_forward)) {

						List<PeripheralInfectDeviceEntity> resultEntities = new ArrayList<PeripheralInfectDeviceEntity>();
						// 取得周边设备检查使用设备工具 
						infectFinishFlag = ppService.getPeripheralData(workingPf.getMaterial_id(), workingPf, resultEntities, false, conn);

						if (resultEntities != null && resultEntities.size() > 0) {
							callbackResponse.put("peripheralData", resultEntities);
						}
					}
					if (!infectFinishFlag) {
						callbackResponse.put("workstauts", WORK_STATUS_PERIPHERAL_WORKING);
					} else {
						callbackResponse.put("workstauts", WORK_STATUS_WORKING);
//						// 取得工程检查票
//						getPf(workingPf, qa_checked, isLeader, callbackResponse, conn);
					}
				}
				// 取得工程检查票
				getPf(workingPf, qa_checked, isLeader, user.getDepartment(), callbackResponse, conn);

				callbackResponse.put("leagal_overline", RvsUtils.getZeroOverLine(mBean.getModel_name(), mBean.getCategory_name(), user, process_code));

				// 取到等待作业记录的本次返工总时间
				Integer spentSecs = ppService.getTotalTimeByRework(workingPf, conn);
				Integer spentMins = spentSecs / 60;
				callbackResponse.put("spent_secs", spentSecs);
				callbackResponse.put("spent_mins", spentMins);
			} else {
				// 暂停中的话
				// 判断是否有在进行中的维修对象
				ProductionFeatureEntity pauseingPf = ppService.getPausingPf(user, conn);

				if (pauseingPf != null) {
					// 小修理共用大修理表单
					if ("612".equals(pauseingPf.getProcess_code())) {
						pauseingPf.setProcess_code("611");
					}

					MaterialService msevice = new MaterialService();
					MaterialEntity mBean = msevice.loadMaterialDetailBean(conn, pauseingPf.getMaterial_id());

					// 取得作业信息
					boolean qa_checked = service.getProccessingData(callbackResponse, pauseingPf.getMaterial_id(), pauseingPf, user, conn);

					// 页面设定为编辑模式
					if (qa_checked) {
						callbackResponse.put("workstauts", WORK_STATUS_DECIDE_PAUSING);

						// 取得工程检查票
						// PositionPanelService.getPcsesFinish(callbackResponse, pauseingPf, conn);
					} else {
						boolean infectFinishFlag = true;
						if ("peripheral".equals(special_forward)) {

							List<PeripheralInfectDeviceEntity> resultEntities = new ArrayList<PeripheralInfectDeviceEntity>();
							// 取得周边设备检查使用设备工具 
							infectFinishFlag = ppService.getPeripheralData(pauseingPf.getMaterial_id(), pauseingPf, resultEntities, false, conn);

							if (resultEntities != null && resultEntities.size() > 0) {
								callbackResponse.put("peripheralData", resultEntities);
							}
						}
						if (!infectFinishFlag) {
							callbackResponse.put("workstauts", WORK_STATUS_PERIPHERAL_PAUSING);
						} else {
							callbackResponse.put("workstauts", WORK_STATUS_PAUSING);
							// 取得工程检查票
							getPf(pauseingPf, qa_checked, isLeader, user.getDepartment(), callbackResponse, conn);
						}

					}

					// 取得维修对象备注信息
					msevice.getMaterialComment(pauseingPf.getMaterial_id(), callbackResponse, conn);

					callbackResponse.put("leagal_overline", RvsUtils.getZeroOverLine(mBean.getModel_name(), mBean.getCategory_name(), user, process_code));

					// 取到等待作业记录的本次返工总时间
					Integer spentSecs = ppService.getTotalTimeByRework(pauseingPf, conn);
					Integer spentMins = spentSecs / 60;
					callbackResponse.put("spent_secs", spentSecs);
					callbackResponse.put("spent_mins", spentMins);
				} else {
					// 准备中
					callbackResponse.put("workstauts", WORK_STATUS_PREPAIRING);
				}
			}

			// 设定暂停选项
			String pauseOptions = "";

			pauseOptions += PauseFeatureService.getPauseReasonSelectOptions();
			callbackResponse.put("pauseOptions", pauseOptions);
			
			String stepOptions = "";
			// 设定正常中断选项
			String steps = PathConsts.POSITION_SETTINGS.getProperty("steps." + process_code);
			if (steps != null) {
				String[] steparray = steps.split(",");
				for (String step : steparray) {
					step = step.trim();
					String stepname = PathConsts.POSITION_SETTINGS
							.getProperty("step." + process_code + "." + step);
					stepOptions += "<option value=\"" + step + "\">" + stepname
							+ "</option>";
				}
			}
			callbackResponse.put("stepOptions", stepOptions);
		}

		user.setSection_id(section_id); // TODO

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		log.info("QualityAssuranceAction.jsinit end");
	}

	/**
	 * 扫描开始/直接暂停重开
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void doscan(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("QualityAssuranceAction.scan start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String section_id = user.getSection_id();//TODO 
		String process_code = user.getProcess_code();
		user.setSection_id(null);
		// user.setPosition_id("00000000046");
		// user.setProcess_code("611");
		// user.setLine_id("00000000015");

		// 判断维修对象在等待区，并返回这一条作业信息
		ProductionFeatureEntity waitingPf = null;
		String material_id = null;
		if (user.getDepartment() == RvsConsts.DEPART_MANUFACT) {
			String serial_no = req.getParameter("material_id");
			ProductService pService = new ProductService();
			waitingPf = pService.checkSerialNo(serial_no, user, errors, conn);
			if(waitingPf == null) {
				
			} else {
				material_id = waitingPf.getMaterial_id();
			}
		} else {
			material_id = req.getParameter("material_id");
			// 判断维修对象在等待区，并返回这一条作业信息
			waitingPf = ppService.checkMaterialId(material_id, user, errors, conn);
		}

		if (errors.size() == 0) {
			boolean qa_checked = service.getProccessingData(listResponse, material_id, waitingPf, user, conn);

			// 作业信息状态改为，作业中
			ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
			waitingPf.setOperator_id(user.getOperator_id());
			dao.startProductionFeature(waitingPf);

			MaterialService msevice = new MaterialService();
			MaterialEntity mBean = msevice.loadMaterialDetailBean(conn, material_id);

//			// 单元无工程检查票
//			if (mBean.getFix_type() == 2) {
//				listResponse.put("workstauts", WORK_STATUS_CELL_WORKING);
//			} else
			// 如果等待中信息是暂停中，则结束掉暂停记录(有可能已经被结束)
			if (waitingPf.getOperate_result() == RvsConsts.OPERATE_RESULT_PAUSE) {
				bfService.finishPauseFeature(material_id, section_id, user.getPosition_id(), user.getOperator_id(), conn);

				if (waitingPf.getAction_time() == null) {
					if (qa_checked) {
						// 确认处理
						listResponse.put("workstauts", WORK_STATUS_DECIDE);
					} else {
						listResponse.put("workstauts", WORK_STATUS_WORKING);
					}
				} else {
					// 确认处理
					listResponse.put("workstauts", WORK_STATUS_DECIDE);
				}
			} else {
				boolean infectFinishFlag = true;
				// 判断是否有特殊页面效果
				String special_forward = PathConsts.POSITION_SETTINGS
						.getProperty("page." + process_code);

				if ("peripheral".equals(special_forward)) {

					List<PeripheralInfectDeviceEntity> resultEntities = new ArrayList<PeripheralInfectDeviceEntity>();
					// 取得周边设备检查使用设备工具 
					infectFinishFlag = ppService.getPeripheralData(material_id, waitingPf, resultEntities, false, conn);

					if (resultEntities != null && resultEntities.size() > 0) {
						listResponse.put("peripheralData", resultEntities);
					}
				}
				if (!infectFinishFlag) {
					listResponse.put("workstauts", WORK_STATUS_PERIPHERAL_WORKING);
				} else {
					// 点检处理
					listResponse.put("workstauts", WORK_STATUS_WORKING);
				}

				// 取得维修对象备注信息
				msevice.getMaterialComment(material_id, listResponse, conn);
			}
			// 判断是否线长
			boolean isLeader = user.getPrivacies().contains(RvsConsts.PRIVACY_LINE);

			// 取得工程检查票
			waitingPf.setProcess_code(process_code);
			getPf(waitingPf, qa_checked, isLeader, user.getDepartment(), listResponse, conn);

			listResponse.put("leagal_overline", RvsUtils.getZeroOverLine(mBean.getModel_name(), mBean.getCategory_name(), user, process_code));

			// 取到等待作业记录的本次返工总时间
			Integer spentSecs = ppService.getTotalTimeByRework(waitingPf, conn);
			Integer spentMins = spentSecs / 60;
			listResponse.put("spent_secs", spentSecs);
			listResponse.put("spent_mins", spentMins);
		}

		user.setSection_id(section_id); // TODO

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("QualityAssuranceAction.scan end");
	}

	/**
	 * 工程检查票确认完成
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void dopcsfinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("QualityAssuranceAction.dopcsfinish start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		String process_code = user.getProcess_code();
		// 小修理共用大修理表单
		if ("612".equals(process_code)) {
			process_code = "611";
		}

		// 取得当前作业中作业信息
		ProductionFeatureEntity workingPf = ppService.getWorkingPf(user, conn);

		if (errors.size() == 0) {

			// 检查维修对象表单
			MaterialEntity bean = new MaterialEntity();
			// 进行中的维修对象
			bean.setMaterial_id(workingPf.getMaterial_id());
			bean.setQa_check_time(new Date());

			// 更新维修对象。
			QualityAssuranceMapper dao = conn.getMapper(QualityAssuranceMapper.class);
			dao.updateMaterial(bean);

			// 作业信息内更新工程检查票输入
			ProductionFeatureMapper pfdao = conn.getMapper(ProductionFeatureMapper.class);


			// 作业信息状态还是作业中
			workingPf.setPcs_inputs(RvsUtils.setContentWithMemo(
					req.getParameter("pcs_inputs"), PcsUtils.PCS_INPUTS_SIZE, conn));
			workingPf.setPcs_comments(RvsUtils.setContentWithMemo(
					req.getParameter("pcs_comments"), PcsUtils.PCS_COMMENTS_SIZE, conn));

			pfdao.updatePcsProductionFeature(workingPf);

			service.getProccessingData(listResponse, workingPf.getMaterial_id(), workingPf, user, conn);

			listResponse.put("workstauts", WORK_STATUS_DECIDE);
			// 取得工程检查票
			workingPf.setProcess_code(process_code);
			// 取得工程检查票
			boolean isLeader = user.getPrivacies().contains(RvsConsts.PRIVACY_LINE);
			getPf(workingPf, true, isLeader, user.getDepartment(), listResponse, conn);


			// 取到等待作业记录的本次返工总时间
			Integer spentSecs = ppService.getTotalTimeByRework(workingPf, conn);
			Integer spentMins = spentSecs / 60;
			listResponse.put("spent_secs", spentSecs);
			listResponse.put("spent_mins", spentMins);
		}

		// listRefresh(listResponse, conn);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("QualityAssuranceAction.dopcsfinish end");
	}

	/**
	 * 作业完成/品保通过
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void dofinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("QualityAssuranceAction.dofinish start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		List<String> triggerList = new ArrayList<String>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		// 取得当前作业中作业信息
		ProductionFeatureEntity workingPf = ppService.getWorkingPf(user, conn);

		// 检查维修对象表单 TODO
		MaterialEntity bean = new MaterialEntity();
		// 进行中的维修对象
		bean.setMaterial_id(workingPf.getMaterial_id());
		bean.setOutline_time(new Date());

		// 更新维修对象。
		QualityAssuranceMapper dao = conn.getMapper(QualityAssuranceMapper.class);
		dao.updateMaterial(bean);

		if (errors.size() == 0) {
			// 计算一下总工时：
			Integer use_seconds = ppService.getTotalTimeByRework(workingPf, conn);

			boolean isLeader = user.getPrivacies().contains(RvsConsts.PRIVACY_LINE);

			// 作业信息状态改为，作业完成
			ProductionFeatureMapper pfdao = conn.getMapper(ProductionFeatureMapper.class);
			workingPf.setOperate_result(RvsConsts.OPERATE_RESULT_FINISH);
			workingPf.setUse_seconds(use_seconds);
			if (!isLeader) {
				workingPf.setPcs_inputs(RvsUtils.setContentWithMemo(
						req.getParameter("pcs_inputs"), PcsUtils.PCS_INPUTS_SIZE, conn));
				workingPf.setPcs_comments(RvsUtils.setContentWithMemo(
						req.getParameter("pcs_comments"), PcsUtils.PCS_COMMENTS_SIZE, conn));
			}
			if ("{}".equals(workingPf.getPcs_comments())) {
				workingPf.setPcs_comments(null);
			}
			if ("{}".equals(workingPf.getPcs_inputs())) {
				workingPf.setPcs_inputs(null);
			}

			pfdao.finishProductionFeature(workingPf);

			if (isLeader) {
				MaterialService mService = new  MaterialService();
				mService.saveLeaderInput(req, workingPf.getMaterial_id(), user, conn);
			}

			// 启动下个工位 就是出货
			pfService.fingerNextPosition(workingPf.getMaterial_id(), workingPf, conn, triggerList);

			conn.commit();

			// FSE 数据同步
			try{
				FseBridgeUtil.toUpdateMaterial(workingPf.getMaterial_id(), "611");
				FseBridgeUtil.toUpdateMaterialProcess(workingPf.getMaterial_id(), "611");
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 通知 SAP
//			service.notifiSapShipping(workingPf.getMaterial_id());
		}

		service.listRefresh(listResponse, workingPf.getPosition_id(), user.getDepartment(), conn);

		// 流水线生成工程检查票
		if (errors.size() == 0) {
//			MaterialService msevice = new MaterialService();
//			MaterialEntity mBean = msevice.loadMaterialDetailBean(conn, workingPf.getMaterial_id());
	
//			// 单元无工程检查票
//			if (mBean.getFix_type() == 1) {
				// conn.commit();
				try {
					URL url = new URL("http://localhost:8080/rvs/download.do?method=file&material_id=" + workingPf.getMaterial_id());
					url.getQuery();
					URLConnection urlconn = url.openConnection();
					urlconn.setReadTimeout(1); // 不等返回
					urlconn.connect();
					urlconn.getContentType(); // 这个就能触发
				} catch (Exception e) {
					log.error("Failed", e);
				}
//			}
		}
//		URL url = new URL("http://localhost:8080/rvs/qualityAssurance.do?method=file&material_id=" + workingPf.getMaterial_id());
//		url.getQuery();
//		URLConnection urlconn = url.openConnection();
//		urlconn.setReadTimeout(1); // 不等返回
//		urlconn.setConnectTimeout(1);
//		//conn.connect();
//		try {
//			urlconn.getContentType(); // 这个就能触发
//		} catch (Exception e) {
//			
//		}

		if (triggerList.size() > 0 && errors.size() == 0) {
			conn.commit();
			RvsUtils.sendTrigger(triggerList);
		}

		listResponse.put("material_id", workingPf.getMaterial_id());
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("QualityAssuranceAction.dofinish end");
	}

	/**
	 * 作业完成/品保不通过
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void doforbid(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("QualityAssuranceAction.doforbid start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		// 取得当前作业中作业信息
		ProductionFeatureEntity workingPf = ppService.getWorkingPf(user, conn);
		String material_id = workingPf.getMaterial_id();

		// 更新维修对象。
		QualityAssuranceMapper dao = conn.getMapper(QualityAssuranceMapper.class);
		dao.forbidMaterial(material_id);

		if (errors.size() == 0) {
			// 计算一下总工时：
			Integer use_seconds = ppService.getTotalTimeByRework(workingPf, conn);

			boolean isLeader = user.getPrivacies().contains(RvsConsts.PRIVACY_LINE);

			// 作业信息状态设为，作业不通过
			ProductionFeatureMapper pfdao = conn.getMapper(ProductionFeatureMapper.class);
			workingPf.setOperate_result(RvsConsts.OPERATE_RESULT_SENDBACK);
			workingPf.setUse_seconds(use_seconds);
			if (!isLeader) {
				workingPf.setPcs_inputs(RvsUtils.setContentWithMemo(
						req.getParameter("pcs_inputs"), PcsUtils.PCS_INPUTS_SIZE, conn));
				workingPf.setPcs_comments(RvsUtils.setContentWithMemo(
						req.getParameter("pcs_comments"), PcsUtils.PCS_COMMENTS_SIZE, conn));
			}
			
			pfdao.finishProductionFeature(workingPf);

			if (isLeader) {
				MaterialService mService = new  MaterialService();
				mService.saveLeaderInput(req, workingPf.getMaterial_id(), user, conn);
			}

//			MaterialService mService = new MaterialService();
//			MaterialForm material = mService.loadSimpleMaterialDetail(conn, material_id);
//			if ("1".equals(material.getFix_type())) {
				// 当流水线时处理

				// 取消总组工程结束时间
				MaterialProcessService mpService = new MaterialProcessService(); 
				mpService.undoLineComplete(material_id, conn);
	
				// 通知
				AlarmMesssageService amService = new AlarmMesssageService();
				amService.createDefectsAlarmMessage(workingPf, user.getDepartment(), user.getLine_id(), conn);

//			} else if ("2".equals(material.getFix_type())) {
//				workingPf.setOperate_result(0);
//				workingPf.setPace(0);
//				workingPf.setRework(workingPf.getRework() + 1);
//				workingPf.setOperator_id(null);
//				workingPf.setAction_time(null);
//				// 当单元时返回等待区
//				pfdao.insertProductionFeature(workingPf);
//			}

			// 推送邮件
			// 通知
			HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
			httpclient.start();
			try {
				conn.commit();
	            HttpGet request = new HttpGet("http://localhost:8080/rvspush/trigger/forbid/" + material_id + "/" + workingPf.getSection_id());
	            log.info("finger:"+request.getURI());
	            httpclient.execute(request, null);
	        } catch (Exception e) {
			} finally {
				Thread.sleep(80);
				httpclient.shutdown();
			}
		}

//		listRefresh(listResponse, conn);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("QualityAssuranceAction.doforbid end");
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
		log.info("PositionPanelAction.dopause start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String workstauts = req.getParameter("workstauts");

		if (errors.size() == 0) {
			// 取得当前作业中作业信息
			ProductionFeatureEntity workingPf = ppService.getWorkingPf(user, conn);

			// 作业信息状态改为，暂停
			ProductionFeatureMapper pfdao = conn.getMapper(ProductionFeatureMapper.class);
			workingPf.setOperate_result(RvsConsts.OPERATE_RESULT_PAUSE);
			workingPf.setUse_seconds(null);
			pfdao.finishProductionFeature(workingPf);

			// 制作暂停信息
			bfService.createPauseFeature(workingPf, req.getParameter("reason"), req.getParameter("comments"), null, conn);

			// 操作者暂停
			boolean qa_checked = service.getProccessingData(listResponse, workingPf.getMaterial_id(), workingPf, user, conn);

			// 根据作业信息生成新的等待作业信息－－有开始时间（仅作标记用，重开时需要覆盖掉），说明是操作者原因暂停，将由本人重开。
			pfService.pauseToSelf(workingPf, conn);

			if (qa_checked) {
				listResponse.put("workstauts", WORK_STATUS_DECIDE_PAUSING);
			} else {
				if (WORK_STATUS_PERIPHERAL_WORKING.equals(workstauts)) {
					listResponse.put("workstauts", WORK_STATUS_PERIPHERAL_PAUSING);
				} else {
					listResponse.put("workstauts", WORK_STATUS_PAUSING);
				}
			}
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelAction.dopause end");
	}

	/**
	 * 暂停再开
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={0})
	public void doendpause(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("QualityAssuranceAction.doendpause start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String material_id = req.getParameter("material_id");

		String workstauts = req.getParameter("workstauts");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		String section_id = user.getSection_id();

		// 得到暂停的维修对象，返回这一条作业信息
		ProductionFeatureEntity workwaitingPf = ppService.checkPausingMaterialId(material_id, user, errors, conn);

		if (errors.size() == 0) {
			boolean qa_checked = service.getProccessingData(listResponse, material_id, workwaitingPf, user, conn);

			// 作业信息状态改为，作业中
			ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
			workwaitingPf.setOperate_result(RvsConsts.OPERATE_RESULT_WORKING);
			dao.pauseWaitProductionFeature(workwaitingPf);

			// 只要开始做，就结束掉本人所有的暂停信息。
			bfService.finishPauseFeature(material_id, section_id, user.getPosition_id(), user.getOperator_id(), conn);

			if (qa_checked) {
				listResponse.put("workstauts", WORK_STATUS_DECIDE);
			} else {
				if (WORK_STATUS_PERIPHERAL_PAUSING.equals(workstauts)) {
					listResponse.put("workstauts", WORK_STATUS_PERIPHERAL_WORKING);
				} else {
					listResponse.put("workstauts", WORK_STATUS_WORKING);
				}
			}

			MaterialService msevice = new MaterialService();
			MaterialEntity mBean = msevice.loadMaterialDetailBean(conn, material_id);

			listResponse.put("leagal_overline", RvsUtils.getZeroOverLine(mBean.getModel_name(), mBean.getCategory_name(), user, workwaitingPf.getProcess_code()));

			// 取到等待作业记录的本次返工总时间
			Integer spentSecs = ppService.getTotalTimeByRework(workwaitingPf, conn);
			Integer spentMins = spentSecs / 60;
			listResponse.put("spent_secs", spentSecs);
			listResponse.put("spent_mins", spentMins);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("QualityAssuranceAction.doendpause end");
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
		PositionPanelService positionPanelService = new PositionPanelService();

		log.info("QualityAssuranceAction.dobreak start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		List<String> triggerList = new ArrayList<String>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String sReason = req.getParameter("reason");
		log.info("REASON:" + sReason);
		Integer iReason = null;

		try {
			iReason = Integer.parseInt(sReason.trim());
		} catch (Exception e) {
			// 选择不正常的中断代码
			log.error("ERROR:" + e.getMessage());
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("reason");
			msgInfo.setErrcode("validator.invalidParam.invalidIntegerValue");
			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidIntegerValue", "中断代码"));
			errors.add(msgInfo);
		}

		// 取得当前作业中作业信息
		ProductionFeatureEntity workingPf = positionPanelService.getWorkingPf(user, conn);

		if (errors.size() == 0) {
			positionPanelService.checkSupporting(workingPf.getMaterial_id(), workingPf.getPosition_id(), errors, conn);
		}

		if (errors.size() == 0) {

			// 中断警报序号
			String alarm_messsage_id = null;

			if (iReason <= 30) { // 异常中断
				// 制作中断警报
				AlarmMesssageService amservice = new AlarmMesssageService();
				AlarmMesssageEntity amEntity = amservice.createBreakAlarmMessage(workingPf);
				alarm_messsage_id = amservice.createAlarmMessage(amEntity, user.getDepartment(), conn, false, triggerList);

				// 加入等待处理区域
				ForSolutionAreaService fsoService = new ForSolutionAreaService();
				String reasonText = sReason;
				// 不良理由
				if (iReason < 10) {
					reasonText = req.getParameter("comments");
				} else if (iReason < 10) {
					reasonText = CodeListUtils.getValue("break_reason", "0" + iReason);
				} else {
					reasonText = PathConsts.POSITION_SETTINGS.getProperty("break."+ user.getProcess_code() +"." + iReason);
				}
				fsoService.create(workingPf.getMaterial_id(), reasonText, 2, user.getPosition_id(), conn, false);
			}

			// 制作暂停信息
			bfService.createPauseFeature(workingPf, sReason, req.getParameter("comments"), alarm_messsage_id, conn);

			if (iReason > 70) { // 业务流程-非直接工步操作

				// 作业信息状态改为，中断
				workingPf.setOperate_result(RvsConsts.OPERATE_RESULT_BREAK);
				workingPf.setUse_seconds(null);
//				workingPf.setPcs_inputs(req.getParameter("pcs_inputs"));
//				workingPf.setPcs_comments(req.getParameter("pcs_comments"));

				pfService.finishProductionFeature(workingPf, conn);

				// 根据作业信息生成新的等待作业信息－－无开始时间，说明进行非直接工步操作，回到等待区，可由他人接手
				pfService.pauseToNext(workingPf, conn);

				// 通知 TODO
			} else if (iReason <= 30) { // 不良中断
				// 作业信息状态改为，中断
				workingPf.setOperate_result(RvsConsts.OPERATE_RESULT_BREAK);
				workingPf.setUse_seconds(null);
				
				// 特殊工位需要工程检查票 TODO

				pfService.finishProductionFeature(workingPf, conn);

				// 根据作业信息生成新的中断作业信息
				pfService.breakToNext(workingPf, conn);

				// 通知 TODO

			} else {
				log.equals(user.getName() + "在" + user.getProcess_code() + "工位发生中断,但是前台提交了暂停理由" + iReason);
				pfService.pauseToSelf(workingPf, conn); // 为 TODO
			}
		}

		if (triggerList.size() > 0 && errors.size() == 0) {
			conn.commit();
			RvsUtils.sendTrigger(triggerList);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("QualityAssuranceAction.dobreak end");
	}

	/**
	 * 取得工程检查票(按完成检查票确认与权限)
	 * @param department 
	 */
	private void getPf(ProductionFeatureEntity pf, boolean qaChecked,
			boolean isLeader, Integer department, Map<String, Object> response, SqlSession conn) {
//		MaterialForm mform = (MaterialForm) response.get("mform");

//		if ("1".equals(mform.getFix_type())) {
			if (qaChecked && !isLeader) {
				// 取得工程检查票
				PositionPanelService.getPcsesFinish(response, pf, conn);
			} else {
				String line_id = "00000000015";
				if (RvsConsts.DEPART_MANUFACT.equals(department)) {
					line_id = "00000000076";
				}
				// 取得工程检查票
				PositionPanelService.getPcses(response, pf, line_id, (qaChecked && isLeader), conn);
			}
//		}
	}

}
