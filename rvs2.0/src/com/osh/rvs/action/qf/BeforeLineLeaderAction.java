package com.osh.rvs.action.qf;

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
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.service.CustomerService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.PositionService;
import com.osh.rvs.service.ProcessAssignService;
import com.osh.rvs.service.inline.LineLeaderService;
import com.osh.rvs.service.qa.ServiceRepairManageService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;

/**
 * 受理报价线长画面
 * @author Gong
 *
 */
public class BeforeLineLeaderAction extends BaseAction {
	private static final String LINE_QF = "00000000011";

	private Logger log = Logger.getLogger(getClass());

	private LineLeaderService service = new LineLeaderService();

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

		log.info("BeforeLineLeaderAction.init start");

		ModelService modelService = new ModelService();
		String mReferChooser = modelService.getRepairOptions(conn);
		req.getSession().setAttribute("mReferChooser", mReferChooser);

		req.setAttribute("lOptions", CodeListUtils.getSelectOptions("material_level", "", ""));
		
		// 获得维修流程选项
		ProcessAssignService paService = new ProcessAssignService();
		String paOptions = paService.getGroupOptions("", conn);
		req.setAttribute("paOptions", paOptions);

		// 取得登录用户权限
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = user.getPrivacies();
		String loginLineId = user.getLine_id();

		if (privacies.contains(RvsConsts.PRIVACY_PROCESSING)) {
			req.setAttribute("manager", "true");
		} else if ((LINE_QF.equals(loginLineId) && privacies.contains(RvsConsts.PRIVACY_LINE))) {
			req.setAttribute("manager", "true");
		} else {
			req.setAttribute("manager", "false");
		}

		// 151工位作业者
		PositionService pService = new PositionService();
		if (pService.checkPositionKind("quotation", user.getPositions(), conn)) {
			req.setAttribute("editor", "true");
		} else {
			req.setAttribute("editor", "false");
		}

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("BeforeLineLeaderAction.init end");
	}

	/**
	 * 画面初始取值处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={107})
	public void jsinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("BeforeLineLeaderAction.jsinit start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 取得今日计划暨作业对象一览
		listResponse.put("performance", service.getBeforePerformanceList((MaterialForm)form, conn));

		// 文字选项
		listResponse.put("opt_level" ,CodeListUtils.getGridOptions("material_level"));
		listResponse.put("opt_operate_result", CodeListUtils.getGridOptions("material_operate_result"));

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		CustomerService service = new CustomerService();
		List<String> list = service.getAutoComplete(conn);
		
		// 查询结果放入Ajax响应对象
		listResponse.put("customers", list);
		// 配送区域信息
		listResponse.put("opt_bound_out_ocm", CodeListUtils.getGridOptions("material_direct_area"));

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("BeforeLineLeaderAction.jsinit end");
	}

	/**
	 * 结果刷新
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void refresh(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("BeforeLineLeaderAction.refresh start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 取得今日计划暨作业对象一览
		listResponse.put("performance", service.getBeforePerformanceList((MaterialForm)form, conn));

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("BeforeLineLeaderAction.refresh end");
	}

	/**
	 * 优先报价标记
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={107})
	public void doexpedite(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("BeforeLineLeaderAction.doexpedite start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String line_id = user.getLine_id();

		String material_id = req.getParameter("material_id");

		service.switchLeaderExpedite(material_id, line_id, conn);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("BeforeLineLeaderAction.doexpedite end");
	}

	/**
	 * 取得不良信息处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void getwarning(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("LineLeaderAction.getwarning start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		String material_id = req.getParameter("material_id");
		String position_id = req.getParameter("position_id");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		listResponse.put("warning", service.getWarning(material_id, user.getOperator_id(), position_id, conn));

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("LineLeaderAction.getwarning end");
	}

	/**
	 * 修改
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={106,107})
	public void doupdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("BeforeLineLeaderAction.doupdate start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		v.delete("ocm");
		v.delete("level"); // TODO js errorMsg
		List<MsgInfo> errors = v != null ? v.validate(): new ArrayList<MsgInfo>();

		String id = ((MaterialForm)form).getMaterial_id();
		MaterialService mservice = new MaterialService();
		mservice.checkRepeatNo(id, form, conn, errors);

		// 更新维修对象字段
		if (errors.size() == 0) {
			service.update(form, req.getSession(), conn, errors);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("BeforeLineLeaderAction.doupdate end");
	}

	/**
	 * 工程查询一览处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={111})
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("BeforeLineLeaderAction.search start");
		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行检索
			List<MaterialForm> lResultForm = service.getBeforePerformanceList((MaterialForm)form, conn);
			
			// 查询结果放入Ajax响应对象
			listResponse.put("list", lResultForm);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("BeforeLineLeaderAction.search end");
	}

	/**
	 * 发送到判定
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={106, 107})
	public void doJudge(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("BeforeLineLeaderAction.doJudge start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String material_id = req.getParameter("material_id");

		// 更新维修对象字段
		if (errors.size() == 0) {
			ServiceRepairManageService srmService = new ServiceRepairManageService();
			// 为保内判定新建记录
			srmService.insertServiceRepairManageFromMaterial(material_id, conn);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("BeforeLineLeaderAction.doJudge end");
	}

}
