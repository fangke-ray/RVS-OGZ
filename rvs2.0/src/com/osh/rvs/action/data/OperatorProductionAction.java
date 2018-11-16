package com.osh.rvs.action.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.common.TemplateReportUtils;
import com.osh.rvs.form.data.OperatorProductionForm;
import com.osh.rvs.service.DownloadService;
import com.osh.rvs.service.LineService;
import com.osh.rvs.service.OperatorProductionService;
import com.osh.rvs.service.SectionService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.validator.Validators;

public class OperatorProductionAction extends BaseAction {

	private SectionService sectionService = new SectionService();
	private LineService lineService = new LineService();
	private OperatorProductionService operatorProductionService = new OperatorProductionService();
	/**
	 * 初始化
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("OperatorProductionAction.init start");
		
		String lOptions = lineService.getOptions(conn);
		req.setAttribute("lOptions", lOptions);
		
		String sOptions = sectionService.getOptions(conn, "(全部)");
		req.setAttribute("sOptions", sOptions);
		
		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		boolean isOperator = isOperator(req.getSession());
		req.setAttribute("isOperator", Boolean.toString(isOperator));
		req.setAttribute("isLeader",isLeader(req.getSession()));
		
		log.info("OperatorProductionAction.init end");
	}

	@Privacies(permit={1, 0})
	public void initSearch(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("OperatorProductionAction.initSearch start");
		// Ajax回馈对象	
		Map<String, Object> listResponse = new HashMap<String, Object>();

		HttpSession session = req.getSession();
		List<OperatorProductionForm> lResultForm = searchBySession(session, conn, listResponse);
		
		// 查询结果放入Ajax响应对象
		listResponse.put("list", lResultForm);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("OperatorProductionAction.initSearch end");
	}
	
	@Privacies(permit={1, 0})
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("OperatorProductionAction.search start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<OperatorProductionForm> lResultForm = searchByCondition(form, conn);

		// 查询结果放入Ajax响应对象
		listResponse.put("list", lResultForm);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("OperatorProductionAction.search end");
	}
	
	@Privacies(permit={1, 0})
	public void getDetail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("OperatorProductionAction.getDetail start");
		// Ajax回馈对象	
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		
		List<MsgInfo> errors = v.validate();
		if (errors.size() == 0) {
			OperatorProductionForm detail = operatorProductionService.getDetail(form, conn);
			listResponse.put("detail", detail);
			
			OperatorProductionForm overtime = operatorProductionService.getPauseOvertime(form, conn);
			listResponse.put("overtime", overtime);

			List<OperatorProductionForm> list = 
					operatorProductionService.getProductionFeatureByKey(form, conn, req.getSession(), detail, overtime.getFinish_time(), listResponse);
			listResponse.put("list", list);
		}
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("OperatorProductionAction.getDetail end");
	}
	
	@Privacies(permit={1, 0})
	public void dosavepause(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("OperatorProductionAction.dosavepause start");
		// Ajax回馈对象	
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		
		List<MsgInfo> errors = v.validate();
		if (errors.size() == 0) {
			operatorProductionService.savePause(form, conn);
		}
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("OperatorProductionAction.dosavepause end");
	}
	
	@Privacies(permit={1, 0})
	public void dosaveoverwork(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("OperatorProductionAction.dosaveoverwork start");
		// Ajax回馈对象	
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		OperatorProductionForm newForm = (OperatorProductionForm) form;
		boolean emptyPost = CommonStringUtil.isEmpty(newForm.getPause_start_time()) &&
				CommonStringUtil.isEmpty(newForm.getPause_finish_time()) &&
				CommonStringUtil.isEmpty(newForm.getOverwork_reason()) &&
				CommonStringUtil.isEmpty(newForm.getComments());

		OperatorProductionForm existed = operatorProductionService.getPauseOvertime(form, conn);
		if (CommonStringUtil.isEmpty(existed.getOperator_id())) {//不存在加班记录
			if (!emptyPost) {
				// 检索条件表单合法性检查
				Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
				
				List<MsgInfo> errors = v.validate();
				if (errors.size() == 0) {
					operatorProductionService.saveoverwork(form, conn);
				}
			}
			
			// 检查发生错误时报告错误信息
			listResponse.put("errors", errors);
		} else {
			if (emptyPost) {
				operatorProductionService.deletePauseOvertime(form, conn);
			} else {
				operatorProductionService.updatePauseOvertime(form, conn);
			}
			
			// 检查发生错误时报告错误信息
			listResponse.put("errors", new ArrayList<MsgInfo>());
		}
		

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("OperatorProductionAction.dosaveoverwork end");
	}
	
	
	
	/*@Privacies(permit={1, 0})
	public void dodeletepause(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) {
		log.info("OperatorProductionAction.getDetial dosavepause");
		// Ajax回馈对象	
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		
		List<MsgInfo> errors = v.validate();
		if (errors.size() == 0) {
			operatorProductionService.deletePause(form, conn);
		}
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("OperatorProductionAction.getDetial dosavepause");
	}*/
	
	
	public void report(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("ScheduleAction.report start");
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 检索条件表单合法性检查
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		OperatorProductionForm detail = operatorProductionService.getDetail(form, conn);
		List<OperatorProductionForm> list = operatorProductionService.getReportData(form, conn);
		OperatorProductionForm overtime = operatorProductionService.getPauseOvertime(form, conn);
		
		try {
			String filePath = TemplateReportUtils.createWorkReport(detail, list, overtime);
			listResponse.put("filePath", filePath);
		} catch (Exception e) {
			log.info(e.getMessage());
			e.printStackTrace();
			MsgInfo info = new MsgInfo();
			info.setErrmsg("导出失败!");
			errors.add(info);
		}
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("ScheduleAction.report end");
	}
	
	public void export(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		String filePath = req.getParameter("filePath");
		String addition = req.getParameter("addition");
		String fileName = new String(("工作日报一览"+addition+".xls").getBytes("gbk"),"iso-8859-1");
		
		DownloadService dservice = new DownloadService();
		dservice.writeFile(res, DownloadService.CONTENT_TYPE_EXCEL, fileName, filePath);
	}

	/**
	 * 得到上月作业日报文件名
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void getLastMonth(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("OperatorProductionAction.getLastMonth start");
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		Map<String, Object> listResponse = new HashMap<String, Object>();
		listResponse.put("fileName", null);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		// 月报文件名
		String monthName = DateUtil.toString(cal.getTime(), "MM月");
		String filename = "月度有效工时统计比率表" + monthName + "汇总(" + RvsUtils.getBussinessHalfYearString(cal) + ").xls";

		// 确认存在
		String filePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\works\\" + filename;
		File exts = new File(filePath);
		if (exts.exists()) {
			listResponse.put("fileName", filename);
		} else {
			log.info("月报文件:" + filename + "不存在.");
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);
		
		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("OperatorProductionAction.getLastMonth end");
	}

	/**
	 * 得到上月作业日报文件名
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void getLastMonthDetail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("OperatorProductionAction.getLastMonthDetail start");
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		Map<String, Object> listResponse = new HashMap<String, Object>();
		listResponse.put("fileName", null);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		// 月报文件名
		String monthName = DateUtil.toString(cal.getTime(), "MM月");
		String filename = "月度有效工时统计比率表" + monthName + "明细(" + RvsUtils.getBussinessHalfYearString(cal) + ").xls";

		// 确认存在
		String filePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\works\\" + filename;
		File exts = new File(filePath);
		if (exts.exists()) {
			listResponse.put("fileName", filename);
		} else {
			log.info("月报文件:" + filename + "不存在.");
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);
		
		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("OperatorProductionAction.getLastMonthDetail end");
	}

	private List<OperatorProductionForm> searchBySession(HttpSession session, SqlSession conn, Map<String, Object> listResponse) {
		LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		
		OperatorProductionForm form = new OperatorProductionForm();
		form.setSection_id(loginData.getSection_id());
		form.setLine_id(loginData.getLine_id());
		form.setAction_time_start(DateUtil.toString(new Date(), DateUtil.DATE_PATTERN));
		listResponse.put("line_id", loginData.getLine_id());
		if ("00000000012".equals(loginData.getLine_id())
				|| "00000000013".equals(loginData.getLine_id())
				|| "00000000014".equals(loginData.getLine_id()))
			listResponse.put("section_id", loginData.getSection_id());
		
		//List<OperatorProductionForm> lResultForm = operatorProductionService.searchByCondition(form, conn);
		return null;
	}

	private List<OperatorProductionForm> searchByCondition(ActionForm form, SqlSession conn) {
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		
		List<MsgInfo> errors = v.validate();
		
		List<OperatorProductionForm> lResultForm = new ArrayList<OperatorProductionForm>();
		if (errors.size() == 0) {
			lResultForm = operatorProductionService.searchByCondition(form, conn);
		}
		
		return lResultForm;
	}
	
	private boolean isOperator(HttpSession session) {
		List<Integer> privacies = getPrivacies(session);
		
		if (privacies.contains(RvsConsts.PRIVACY_POSITION_VIEW) || privacies.contains(RvsConsts.PRIVACY_POSITION)) {
			return true;
		}

		return false;
	}
	
	private List<Integer> getPrivacies(HttpSession session) {
		LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = loginData.getPrivacies();
		
		return privacies;
	}
	
	@SuppressWarnings("unused")
	private boolean isAdmin(HttpSession session) {
		LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = loginData.getPrivacies();
		if (privacies.contains(RvsConsts.PRIVACY_SA)
				|| privacies.contains(RvsConsts.PRIVACY_OVEREDIT)) {
			return true;
		}
		// 进度查询 L3
		return false;
	}

	private boolean isLeader(HttpSession session) {
		LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		// TODO who edit
		if (RvsConsts.ROLE_LINELEADER.equals(loginData.getRole_id())) return true;

		List<Integer> privacies = getPrivacies(session);
		if (privacies.contains(RvsConsts.PRIVACY_SCHEDULE) || privacies.contains(RvsConsts.PRIVACY_LINE) || 
				privacies.contains(RvsConsts.PRIVACY_INFO_EDIT)) {
			return true;
		}

		return false;
	}
}
