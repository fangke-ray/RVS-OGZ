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

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.equipment.DeviceJigOrderDetailForm;
import com.osh.rvs.form.master.DevicesManageForm;
import com.osh.rvs.service.BrandService;
import com.osh.rvs.service.DevicesManageService;
import com.osh.rvs.service.DevicesTypeService;
import com.osh.rvs.service.LineService;
import com.osh.rvs.service.SectionService;
import com.osh.rvs.service.equipment.DeviceJigOrderDetailService;
import com.osh.rvs.service.equipment.DeviceSpareService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.validator.Validators;

public class DevicesManageAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	private SectionService sectionService = new SectionService();
	private LineService lineService = new LineService();
	private DevicesTypeService devicesTypeService = new DevicesTypeService();
	
	private DevicesManageService service = new DevicesManageService();

	/**
	 * 设备工具管理画面初始化
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
	@Privacies(permit = { 1, 0 })
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("DevicesManageAction.init start");

		actionForward = mapping.findForward(FW_INIT);

		// 分发课室
		String sectionOptions = sectionService.getAllOptions(conn);
		req.setAttribute("sectionOptions", sectionOptions);

		// 责任工程
		String lineOptions = lineService.getOptions(conn);
		req.setAttribute("lineOptions", lineOptions);

		// 管理等级(有不选)
		req.setAttribute("manageLevel", CodeListUtils.getSelectOptions("devices_manage_level", null, ""));
		// 管理等级(无不选)
		req.setAttribute("nCmanageLevel", CodeListUtils.getSelectOptions("devices_manage_level", ""));
		req.setAttribute("goManageLevel", CodeListUtils.getGridOptions("devices_manage_level"));

		// 状态(有不选)
		req.setAttribute("status", CodeListUtils.getSelectOptions("devices_status", null, ""));
		// 状态(无不选)
		req.setAttribute("nCstatus", CodeListUtils.getSelectOptions("devices_status", ""));
		
		req.setAttribute("goStatus",CodeListUtils.getGridOptions("devices_status"));

		// 工位
		String pReferChooser = service.getOptionPtions(conn);
		req.setAttribute("pReferChooser", pReferChooser);
		
		//品名
		String nReferChooser=devicesTypeService.getDevicesTypeReferChooser(conn);
		req.setAttribute("nReferChooser", nReferChooser);

		//管理员
		String oReferChooser = service.getDevicesManageroptions(conn);
		req.setAttribute("oReferChooser", oReferChooser);

		// 廠商
		BrandService bService = new BrandService();
		req.setAttribute("bReferChooser", bService.getOptions(conn));

		/*//责任人
		String rReferChooser =operatorService.getAllOperatorName(conn);
		req.setAttribute("rReferChooser", rReferChooser);*/
		
		log.info("DevicesManageAction.init end");
	}

	/**
	 * 设备工具管理画面一览
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response, SqlSession conn) throws Exception {
		log.info("DevicesManageAction.search start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 设备工具管理详细数据
		List<DevicesManageForm> devicesManageForms = service.searchDevicesManage(form, conn, errors);

		String accessPlace = request.getParameter("access_place");
		if ("1".equals(accessPlace)) {
			List<String> ls = new ArrayList<String>();
			for (DevicesManageForm devicesManageForm : devicesManageForms) {
				ls.add(devicesManageForm.getManage_code());
			}
			listResponse.put("manageCodes", ls);
		}

		// 获取当前时间
		Calendar calendar = Calendar.getInstance();
		String current_date = DateUtil.toString(calendar.getTime(), "yyyy/MM/dd");
		listResponse.put("current_date", current_date);
		
		listResponse.put("devicesManageForms", devicesManageForms);
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("DevicesManageAction.search end");
	}
	
	/**
	 * 修改设备工具管理
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void doupdate(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn) throws Exception{
		log.info("DevicesManageAction.doupdate start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 新建记录表单合法性检查
		Validators v=BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors=v.validate();
		
		service.customValidate(form, conn, errors);
		
		service.validateStatus(form, conn, errors);
		
		DevicesManageForm devicesManageForm = (DevicesManageForm)form;
		if (errors.size() == 0) {
			service.updateDevicesManage(request.getParameter("compare_status"),devicesManageForm,conn,request.getSession(),errors);
		}
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("DevicesManageAction.doupdate end");
	}
	/**
	 * 新建设备管理
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void doinsert(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn) throws Exception{
		log.info("DevicesManageAction.doinsert start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		// 表单合法性检查
		Validators v=BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors=v.validate();

		String add_method = request.getParameter("add_method");

		DevicesManageForm devicesManageForm = (DevicesManageForm)form;
		
		service.customValidate(form, conn, errors);
		
		if (errors.size() == 0) {
			service.insertDevicesManage(devicesManageForm,conn,request.getSession(),errors);

			if ("spare".equals(add_method)) {
				// 备品加入设备管理
				DeviceSpareService dsService = new DeviceSpareService();
				dsService.setAsManageCode(form, 25, request, conn);
			}
			if ("order".equals(add_method)) {
				// 订购品加入设备管理
				service.setAsManageCode(form, conn);
			}
		}

		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("DevicesManageAction.doinsert end"); 
	}	
	
	/**
	 * 删除设备管理
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void dodelete(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn) throws Exception{
		log.info("DevicesManageAction.dodelete start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		// 表单合法性检查
		Validators v=BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors=v.validate();

        service.deleteDevicesManage(form,conn,request.getSession(),errors);
		
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("DevicesManageAction.dodelete end"); 
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
	public void searchMaxManageCode(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSession conn) throws Exception{
		log.info("DevicesManageAction.searchMaxManageCode start");
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		//查询序号最大的管理编号
		String manageCode = service.searchMaxManageCode(form, conn, request);
		
		listResponse.put("manageCode", manageCode);
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("DevicesManageAction.searchMaxManageCode end");
	}

	/**
	 * 替换设备
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void doexchange(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn) throws Exception{
		log.info("DevicesManageAction.doexchange start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		// 表单合法性检查
		Validators v=BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors=v.validate();

		DevicesManageForm devicesManageForm = (DevicesManageForm)form;

		// 当前操作者ID
		LoginData user = (LoginData) request.getSession().getAttribute(RvsConsts.SESSION_USER);

		if (errors.size() == 0) {
			// 同样制造新品
			service.exchange(devicesManageForm,user.getOperator_id(),conn,errors);
			// 废弃旧品
			service.disband(devicesManageForm,user.getOperator_id(),conn,errors);
		}
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("DevicesManageAction.doexchange end"); 
	}

	/**
	 * 替换新品--新建
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void doReplace(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response,SqlSessionManager conn) throws Exception{
		log.info("DevicesManageAction.doReplace start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		// 表单合法性检查
		Validators v=BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors=v.validate();

		DevicesManageForm devicesManageForm = (DevicesManageForm)form;

		// 判断有同类管理要素
		List<MsgInfo> infoes = new ArrayList<MsgInfo>();
		String use_manage = request.getParameter("use_manage");
		if (errors.size() == 0 && use_manage == null) {
			// 判断备品
			DeviceSpareService dsService = new DeviceSpareService();
			if (dsService.checkExistsStock(form, 1, conn) != null) {
				MsgInfo info = new MsgInfo();
				info.setErrcode("use_spare");
				info.setErrmsg("");
				infoes.add(info);
				listResponse.put("infoes", infoes);
			}
			// 判断订购品
			DeviceJigOrderDetailService djoService = new DeviceJigOrderDetailService();
			DeviceJigOrderDetailForm searchForm = new DeviceJigOrderDetailForm();
			searchForm.setObject_type("1");
			searchForm.setInline_recept_flg("1");
			searchForm.setDevice_type_id(devicesManageForm.getDevices_type_id());
			searchForm.setModel_name(devicesManageForm.getModel_name());

			List<Map<String, String>> hitOrders = service.checkExistsOrder(djoService.search(searchForm, conn));
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

			if (errors.size() == 0) {
				service.replaceDevicesManage(request.getParameter("compare_status"),request.getParameter("old_manage_code"),devicesManageForm,conn,request.getSession(),errors);

				if ("1".equals(use_manage)) {
					// 备品加入设备管理
					DeviceSpareService dsService = new DeviceSpareService();
					dsService.setAsManageCode(form, 24, request, conn);
				}
				if ("2".equals(use_manage)) {
					// 订购品加入设备管理
					service.setAsManageCode(form, conn);
				}
			}
		}

		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		
		log.info("DevicesManageAction.doReplace end");
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
	public void dodeliver(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response, SqlSessionManager conn)throws Exception{
		log.info("DevicesManageAction.dodeliver end");

		Map<String, Object> listResponse = new HashMap<String, Object>();

		//表单合法性检查
		List<MsgInfo> errors=new ArrayList<MsgInfo>();

		DevicesManageForm devicesManageForm = (DevicesManageForm)form;
		
		service.deliverDevicesManage(devicesManageForm, conn, request.getSession(), errors,request);
		
		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);
		log.info("DevicesManageAction.dodeliver end");
	}
}
