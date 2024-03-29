package com.osh.rvs.action.master;

import java.util.ArrayList;
import java.util.Calendar;
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

import com.osh.rvs.form.equipment.DeviceJigOrderDetailForm;
import com.osh.rvs.form.master.ToolsManageForm;
import com.osh.rvs.service.DevicesManageService;
import com.osh.rvs.service.LineService;
import com.osh.rvs.service.OperatorService;
import com.osh.rvs.service.SectionService;
import com.osh.rvs.service.JigManageService;
import com.osh.rvs.service.UploadService;
import com.osh.rvs.service.equipment.DeviceJigOrderDetailService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.validator.Validators;

public class ToolsManageAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	private SectionService sectionService = new SectionService();
	private LineService lineService = new LineService();

	private OperatorService operatorService = new OperatorService();

	private JigManageService service = new JigManageService();
	
	private DevicesManageService devicesManageService = new DevicesManageService();

	/**
	 * 治具管理画面初始化
	 * 
	 * @param mapping
	 *            ActionMapping
	 * @param form
	 *            表单
	 * @param req
	 *            页面请求
	 * @param res
	 *            页面响应
	 * @param conn
	 *            数据库会话
	 * @throws Exception
	 */
	@Privacies(permit = { 124, 1, 0 })
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("ToolsManageAction.init start");

		actionForward = mapping.findForward(FW_INIT);

		// 分发课室
		String sectionOptions = sectionService.getAllOptions(conn);
		req.setAttribute("sectionOptions", sectionOptions);

		// 责任工程
		String lineOptions = lineService.getAllOptions(conn);
		req.setAttribute("lineOptions", lineOptions);

		// 状态(有不选)
		req.setAttribute("status", CodeListUtils.getSelectOptions("devices_status", null, ""));
		// 状态(无不选)
		req.setAttribute("nCstatus", CodeListUtils.getSelectOptions("devices_status",""));
		req.setAttribute("goStatus", CodeListUtils.getGridOptions("devices_status"));

		// 工位
		String pReferChooser = devicesManageService.getOptionPtions(conn);
		req.setAttribute("pReferChooser", pReferChooser);

		// 责任人
		String rReferChooser = operatorService.getAllOperatorName(conn);
		req.setAttribute("rReferChooser", rReferChooser);

		//管理员
		DevicesManageService service = new DevicesManageService();
		String oReferChooser = service.getDevicesManageroptions(conn);
		req.setAttribute("oReferChooser", oReferChooser);

		log.info("ToolsManageAction.init end");
	}

	/**
	 * 治具管理一览
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 124, 1, 0 })
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response, SqlSession conn) throws Exception {
		log.info("ToolsManageAction.search start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 设备工具管理详细数据
		List<ToolsManageForm> toolsManageForms = service.searchToolsManage(form, conn, errors);

		// 获取当前时间
		Calendar calendar = Calendar.getInstance();
		String current_date = DateUtil.toString(calendar.getTime(), "yyyy/MM/dd");
		listResponse.put("current_date", current_date);
		
		listResponse.put("toolsManageForms", toolsManageForms);
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("ToolsManageAction.search end");
	}

	/**
	 * 新建
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 124, 1, 0 })
	public void doinsert(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response, SqlSessionManager conn) throws Exception {
		log.info("ToolsManageAction.doinsert start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		// 验证
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v.validate();
		ToolsManageForm toolsManageForm = (ToolsManageForm) form;

		service.customValidate(form, conn, errors);

		String add_method = request.getParameter("add_method");

		// 新建
		if (errors.size() == 0) {
			service.insertToolsManage(toolsManageForm, conn, request.getSession(), errors);
			if ("order".equals(add_method)) {
				// 订购品加入设备管理
				service.setAsManageCode(form, request, conn);
			}
		}

		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("ToolsManageAction.doinsert end");
	}

	/**
	 * 修改设备工具管理
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 124, 1, 0 })
	public void doupdate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response, SqlSessionManager conn) throws Exception {
		log.info("ToolsManageAction.doupdate end");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		//表单合法性检查
		Validators v=BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors=v.validate();

		service.customValidate(form, conn, errors);
		
		service.validateStatus(form, conn, errors);
		
		ToolsManageForm toolsManageForm = (ToolsManageForm) form;
		if (errors.size() == 0) {
			service.updateToolsManage(request.getParameter("compare_status"),toolsManageForm, conn, request.getSession(), errors);
		}
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("ToolsManageAction.doupdate end");
	}

	/**
	 * 删除设备管理
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 124, 1, 0 })
	public void dodelete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response, SqlSessionManager conn) throws Exception {
		log.info("ToolsManageAction.dodelete end");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		// 表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		service.deleteToolsManage(form, conn, request.getSession(), errors);

		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("ToolsManageAction.dodelete end");
	}
	
	/**
	 * 查询最大管理编号
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void searchMaxManageCode(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response, SqlSession conn)throws Exception{
		log.info("ToolsManageAction.searchMaxManageCode start");
		
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		String manage_code=service.searchMaxManageCode(form, conn);
		
		listResponse.put("manage_code", manage_code);
		listResponse.put("errors", errors);
		
		returnJsonResponse(response, listResponse);
		
		log.info("ToolsManageAction.searchMaxManageCode end");
	}
	
	/**
	 * 替换新品
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 124, 1, 0 })
	public void doReplace(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response, SqlSessionManager conn)throws Exception{
		log.info("ToolsManageAction.doReplace start");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		//表单合法性检查
		Validators v=BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors=v.validate();

		ToolsManageForm toolsManageForm = (ToolsManageForm) form;

		// 判断有同类管理要素
		List<MsgInfo> infoes = new ArrayList<MsgInfo>();
		String use_manage = request.getParameter("use_manage");
		if (errors.size() == 0 && use_manage == null) {
			// 判断订购品
			DeviceJigOrderDetailService djoService = new DeviceJigOrderDetailService();
			DeviceJigOrderDetailForm searchForm = new DeviceJigOrderDetailForm();
			searchForm.setObject_type("2");
			searchForm.setInline_recept_flg("1");
			searchForm.setDevice_type_id("00000000000");
			searchForm.setModel_name(toolsManageForm.getTools_no());

			List<Map<String, String>> hitOrders = service.checkExistsOrder(djoService.search(searchForm, conn), toolsManageForm.getCount_in());
			if (hitOrders.size() > 0) {
				MsgInfo info = new MsgInfo();
				info.setErrcode("use_order");
				info.setErrmsg("");
				infoes.add(info);
				listResponse.put("infoes", infoes);
				listResponse.put("hitOrders", hitOrders);
			}
		}

		if (infoes.size() == 0) {

			service.customValidate(form, conn, errors);
			
			service.validateStatus(form, conn, errors);

			if (errors.size() == 0) {
				service.replace(request.getParameter("compare_status"),request.getParameter("old_manage_code"),toolsManageForm, conn, request);
			}
			if ("2".equals(use_manage)) {
				// 订购品加入设备管理
				service.setAsManageCode(form, request, conn);
			}
		}
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		
		log.info("ToolsManageAction.doReplace end");
	}
	
	/**
	 * 批量交付
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 124, 1, 0 })
	public void dodeliver(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response, SqlSessionManager conn)throws Exception{
		log.info("ToolsManageAction.dodeliver end");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		//表单合法性检查
		List<MsgInfo> errors=new ArrayList<MsgInfo>();

		ToolsManageForm toolsManageForm = (ToolsManageForm) form;
		
		service.deliverToolsManage(toolsManageForm, conn, request.getSession(), errors,request);
		
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("ToolsManageAction.dodeliver end");
	}

	/**
	 * 上传照片
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void sourceImage(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("ToolsManageAction.sourceImage start");

		// Ajax回馈对象
		Map<String, Object> jsonResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		UploadService fservice = new UploadService();
		String tempFilePath = fservice.getFile2Local(form, errors);

		String photo_file_name = tempFilePath.substring(tempFilePath.lastIndexOf("\\") + 1);
		service.copyPhoto(req.getParameter("tools_no"), photo_file_name);

		// 检查发生错误时报告错误信息
		jsonResponse.put("errors", errors);
		// 返回Json格式响应信息
		returnJsonResponse(res, jsonResponse);

		log.info("ToolsManageAction.sourceImage end");
	}

}
