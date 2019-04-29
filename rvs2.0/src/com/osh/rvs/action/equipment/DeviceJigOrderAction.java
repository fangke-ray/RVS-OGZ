package com.osh.rvs.action.equipment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.equipment.DeviceJigInvoiceForm;
import com.osh.rvs.form.equipment.DeviceJigOrderDetailForm;
import com.osh.rvs.form.equipment.DeviceJigOrderForm;
import com.osh.rvs.form.equipment.DeviceJigQuotationForm;
import com.osh.rvs.form.equipment.DeviceSpareForm;
import com.osh.rvs.form.master.ToolsManageForm;
import com.osh.rvs.service.DevicesTypeService;
import com.osh.rvs.service.JigManageService;
import com.osh.rvs.service.OperatorService;
import com.osh.rvs.service.equipment.DeviceJigInvoiceService;
import com.osh.rvs.service.equipment.DeviceJigOrderDetailService;
import com.osh.rvs.service.equipment.DeviceJigOrderService;
import com.osh.rvs.service.equipment.DeviceJigQuotationService;
import com.osh.rvs.service.equipment.DeviceSpareService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

public class DeviceJigOrderAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	private OperatorService operatorService = new OperatorService();
	private DeviceJigInvoiceService deviceJigInvoiceService = new DeviceJigInvoiceService();
	private DeviceJigQuotationService deviceJigQuotationService = new DeviceJigQuotationService();
	private DeviceJigOrderService deviceJigOrderService = new DeviceJigOrderService();

	private DeviceJigOrderDetailService deviceJigOrderDetailService = new DeviceJigOrderDetailService();

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
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.init start");

		// 操作者
		String oReferChooser = operatorService.getAllOperatorName(conn);
		req.setAttribute("oReferChooser", oReferChooser);

		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		// 登录者ID
		req.setAttribute("loginID", user.getOperator_id());
		// 登录者名称
		req.setAttribute("loginName", user.getName());

		// 受注方
		req.setAttribute("sOrderFrom", CodeListUtils.getSelectOptions("device_jig_order_from", null, ""));

		// 对象类别
		req.setAttribute("sObjectType", CodeListUtils.getSelectOptions("device_jig_object_type", null, ""));

		// 确认结果
		req.setAttribute("sConfirmFlg", CodeListUtils.getSelectOptions("device_jig_confirm_flg", null, ""));

		// 备品种类
		req.setAttribute("goDeviceSpareType", CodeListUtils.getSelectOptions("device_spare_type", null, ""));

		DevicesTypeService devicesTypeService = new DevicesTypeService();
		req.setAttribute("devicesTypeReferChooser", devicesTypeService.getDevicesTypeReferChooser(conn));
		
		// 订单号
		String orderNo = req.getParameter("order_no");
		req.setAttribute("initOrderNo", orderNo);
		
		// 型号/规格
		String modelName = req.getParameter("model_name");
		req.setAttribute("initModelName", modelName);

		// 权限
		String privacy = "";
		// 角色
		String role = "";
		
		List<Integer> privacies = user.getPrivacies();

		// 经理
		if(privacies.contains(RvsConsts.PRIVACY_PROCESSING)){// 进度操作
			role = "manager";
		}else if (privacies.contains(RvsConsts.PRIVACY_LINE)) {// 线长操作
			role = "line";
		}
		req.setAttribute("role", role);
		
		// 设备管理(设备管理画面)
		if (privacies.contains(RvsConsts.PRIVACY_TECHNOLOGY)) {
			privacy = "technology";
		} 
		req.setAttribute("privacy", privacy);

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("DeviceJigOrderAction.init end");
	}

	/**
	 * 检索
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.search start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			List<DeviceJigOrderDetailForm> list = deviceJigOrderDetailService.search(form, conn);
			listResponse.put("orderList", list);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.search end");
	}

	/**
	 * 未发放申请单号
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchUnProvide(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.searchOrderNo start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		List<DeviceJigOrderForm> list = deviceJigOrderService.searchUnProvide(conn);
		listResponse.put("list", list);
		
		String tempOrderNo = deviceJigOrderService.getMaxTempOrderNo(conn);
		listResponse.put("tempOrderNo", tempOrderNo);

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.searchOrderNo end");
	}

	/**
	 * 新建订单号
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doInsertOrder(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigOrderAction.doInsertOrder start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		DeviceJigOrderDetailForm deviceJigOrderDetailForm = (DeviceJigOrderDetailForm) form;

		DeviceJigOrderForm deviceJigOrderForm = new DeviceJigOrderForm();
		deviceJigOrderForm.setOrder_no(deviceJigOrderDetailForm.getOrder_no());

		Validators v = BeanUtil.createBeanValidators(deviceJigOrderForm, BeanUtil.CHECK_TYPE_ALL);
		v.delete("order_key");

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();
		if (errors.size() == 0) {
			String order_key = deviceJigOrderService.insert(deviceJigOrderForm, conn, errors);
			listResponse.put("order_key", order_key);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.doInsertOrder end");

	}

	/**
	 * 订单明细
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchOrderDetail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.searchOrderDetail start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.only("order_key");

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();
		if (errors.size() == 0) {
			List<DeviceJigOrderDetailForm> list = deviceJigOrderDetailService.searchDetail(form, conn);

			listResponse.put("detailList", list);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.searchOrderDetail end");

	}

	/**
	 * 查询现有备品数
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchAvailableInventory(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.searchAvailableInventory start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();
		if (errors.size() == 0) {
			DeviceSpareService service = new DeviceSpareService();
			DeviceSpareForm deviceSpareForm = service.getDeviceSpare(form, conn);
			listResponse.put("deviceSpareForm", deviceSpareForm);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.searchAvailableInventory end");

	}

	/**
	 * 查询治具品名
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchJigName(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.searchJigName start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String jigNo = req.getParameter("jig_no");
		if (CommonStringUtil.isEmpty(jigNo)) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("validator.required");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required", "型号/规格"));
			errors.add(error);
		}

		if (errors.size() == 0) {
			JigManageService jigManageService = new JigManageService();

			List<ToolsManageForm> jigManageList = jigManageService.searchByJigNo(jigNo, conn);
			listResponse.put("jigManageList", jigManageList);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.searchJigName end");
	}

	/**
	 * 更新
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigOrderAction.doUpdate start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		deviceJigOrderDetailService.update(req, conn, errors);

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.doUpdate end");
	}

	/**
	 * 查询询价
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchInvoice(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.searchInvoice start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		List<DeviceJigOrderDetailForm> list = deviceJigOrderDetailService.searchInvoice(conn);
		listResponse.put("invoiceList", list);

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.searchInvoice end");
	}

	/**
	 * 查询结果
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchInvoiceResult(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.searchInvoiceResult start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		DeviceJigInvoiceForm deviceJigInvoiceForm = deviceJigInvoiceService.getLastTimeInvoice(form, conn);
		listResponse.put("deviceJigInvoiceForm", deviceJigInvoiceForm);

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.searchInvoiceResult end");
	}

	/**
	 * 应该明细查询
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 */
	public void searchApplicate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.searchApplicate start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		DeviceJigOrderDetailForm deviceJigOrderDetailForm = (DeviceJigOrderDetailForm) form;
		String flg = deviceJigOrderDetailForm.getConfirm_flg_name();
		String sendDate = deviceJigOrderDetailForm.getSend_date();
		String oldSendDate = req.getParameter("old_send_date");

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		if ("add".equals(flg)) {
			v.add("send_date", v.required("询价发送日期"));
		} else {
			v.add("order_invoice_id", v.required("询价ID"));
		}

		v.add("order_price", v.required("订购单价"));
		v.add("origin_price", v.required("原产单价"));

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if ("add".equals(flg) && !CommonStringUtil.isEmpty(sendDate) && !CommonStringUtil.isEmpty(oldSendDate)) {
			int compare = DateUtil.compareDate(DateUtil.toDate(sendDate, DateUtil.DATE_PATTERN), DateUtil.toDate(oldSendDate, DateUtil.DATE_PATTERN));
			if (compare <= 0) {
				MsgInfo error = new MsgInfo();
				error.setErrmsg("询价发送日期不能早于等于" + oldSendDate);
				errors.add(error);
			}
		}

		if (errors.size() == 0) {
			List<DeviceJigOrderDetailForm> list = deviceJigOrderDetailService.searchDetail(form, conn);
			listResponse.put("detailList", list);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.searchApplicate end");
	}

	/**
	 * 更新询价
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdateInvoice(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigOrderAction.doUpdateInvoice start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		DeviceJigOrderDetailForm deviceJigOrderDetailForm = (DeviceJigOrderDetailForm) form;
		String flg = deviceJigOrderDetailForm.getConfirm_flg_name();
		String sendDate = deviceJigOrderDetailForm.getSend_date();
		String oldSendDate = req.getParameter("old_send_date");

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		if ("add".equals(flg)) {
			v.add("send_date", v.required("询价发送日期"));
		} else {
			v.add("order_invoice_id", v.required("询价ID"));
		}

		v.add("order_price", v.required("订购单价"));
		v.add("origin_price", v.required("原产单价"));

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if ("add".equals(flg) && !CommonStringUtil.isEmpty(sendDate) && !CommonStringUtil.isEmpty(oldSendDate)) {
			int compare = DateUtil.compareDate(DateUtil.toDate(sendDate, DateUtil.DATE_PATTERN), DateUtil.toDate(oldSendDate, DateUtil.DATE_PATTERN));
			if (compare <= 0) {
				MsgInfo error = new MsgInfo();
				error.setErrmsg("询价发送日期不能早于等于" + oldSendDate);
				errors.add(error);
			}
		}

		if (errors.size() == 0) {
			Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
			List<DeviceJigOrderDetailForm> list = new AutofillArrayList<DeviceJigOrderDetailForm>(DeviceJigOrderDetailForm.class);
			Map<String, String[]> parameters = req.getParameterMap();

			// 整理提交数据
			for (String parameterKey : parameters.keySet()) {
				Matcher m = p.matcher(parameterKey);
				if (m.find()) {
					String entity = m.group(1);
					if ("device_jig_order_detail".equals(entity)) {
						String column = m.group(2);
						int icounts = Integer.parseInt(m.group(3));
						String[] value = parameters.get(parameterKey);
						if ("order_key".equals(column)) {
							list.get(icounts).setOrder_key(value[0]);
						} else if ("object_type".equals(column)) {
							list.get(icounts).setObject_type(value[0]);
						} else if ("device_type_id".equals(column)) {
							list.get(icounts).setDevice_type_id(value[0]);
						} else if ("model_name".equals(column)) {
							list.get(icounts).setModel_name(value[0]);
						} else if ("applicator_id".equals(column)) {
							list.get(icounts).setApplicator_id(value[0]);
						}
					}
				}
			}

			if (list.size() == 0) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("validator.required.multidetail");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.multidetail", "记录"));
				errors.add(error);
			} else {
				String invoiceId = null;
				if ("add".equals(flg)) {
					invoiceId = deviceJigInvoiceService.insert(deviceJigOrderDetailForm, conn);
				} else {
					invoiceId = deviceJigOrderDetailForm.getOrder_invoice_id();
					deviceJigOrderDetailForm.setInvoice_id(invoiceId);
					deviceJigInvoiceService.updatePrice(deviceJigOrderDetailForm, conn);
				}

				for (int index = 0; index < list.size(); index++) {
					deviceJigOrderDetailForm = list.get(index);
					deviceJigOrderDetailForm.setOrder_invoice_id(invoiceId);
					deviceJigOrderDetailService.updateInvoice(deviceJigOrderDetailForm, conn);
				}
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.doUpdateInvoice end");
	}

	/**
	 * 查询报价发送
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchQuotationSend(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigOrderAction.searchQuotationSend start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		List<DeviceJigOrderDetailForm> list = deviceJigOrderDetailService.searchUnQuotation(conn);
		listResponse.put("unQuotationList", list);

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.searchQuotationSend end");
	}

	/**
	 * 更新报价
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdateQuotation(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigOrderAction.doUpdateQuotation start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("quotation_no", v.required("报价单号"));

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();
		if (errors.size() == 0) {
			DeviceJigOrderDetailForm deviceJigOrderDetailForm = (DeviceJigOrderDetailForm) form;
			// 报价单号
			String quotationNo = deviceJigOrderDetailForm.getQuotation_no();

			DeviceJigQuotationForm deviceJigQuotationForm = deviceJigQuotationService.getQuotationByNO(quotationNo, conn);
			// 报价单号重复
			if (deviceJigQuotationForm != null) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("dbaccess.recordDuplicated");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", "报价单号[" + quotationNo + "]"));
				errors.add(error);
			} else {
				Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
				List<DeviceJigOrderDetailForm> list = new AutofillArrayList<DeviceJigOrderDetailForm>(DeviceJigOrderDetailForm.class);
				Map<String, String[]> parameters = req.getParameterMap();

				// 整理提交数据
				for (String parameterKey : parameters.keySet()) {
					Matcher m = p.matcher(parameterKey);
					if (m.find()) {
						String entity = m.group(1);
						if ("device_jig_order_detail".equals(entity)) {
							String column = m.group(2);
							int icounts = Integer.parseInt(m.group(3));
							String[] value = parameters.get(parameterKey);
							if ("order_key".equals(column)) {
								list.get(icounts).setOrder_key(value[0]);
							} else if ("object_type".equals(column)) {
								list.get(icounts).setObject_type(value[0]);
							} else if ("device_type_id".equals(column)) {
								list.get(icounts).setDevice_type_id(value[0]);
							} else if ("model_name".equals(column)) {
								list.get(icounts).setModel_name(value[0]);
							} else if ("applicator_id".equals(column)) {
								list.get(icounts).setApplicator_id(value[0]);
							}
						}
					}
				}

				String quotationId = deviceJigQuotationService.insertQuotation(deviceJigOrderDetailForm, conn);

				for (int index = 0; index < list.size(); index++) {
					deviceJigOrderDetailForm = list.get(index);
					deviceJigOrderDetailForm.setQuotation_id(quotationId);
					deviceJigOrderDetailService.updateQuotation(deviceJigOrderDetailForm, conn);
				}
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.doUpdateQuotation end");
	}

	/**
	 * 更新确认结果
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdateConfirm(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigOrderAction.doUpdateConfirm start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("confirm_flg", v.required("确认结果"));

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();
		if (errors.size() == 0) {
			deviceJigOrderDetailService.updateConfirm(form, conn);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.doUpdateConfirm end");
	}

	/**
	 * 加入备品
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doAddSpare(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigOrderAction.doAddSpare start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		DeviceJigOrderDetailForm pageForm = (DeviceJigOrderDetailForm) form;
		// 确认数量
		String confirmQuantity = pageForm.getConfirm_quantity();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("confirm_quantity", v.required("确认数量"));
		v.add("device_spare_type", v.required("备品种类"));

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			DeviceJigOrderDetailForm detailForm = deviceJigOrderDetailService.getOrderDetail(form, conn);
			// 订购数量
			Integer quantity = Integer.valueOf(detailForm.getQuantity());

			// 确认数量大于订购数量
			if (Integer.valueOf(confirmQuantity) > quantity) {
				MsgInfo error = new MsgInfo();
				error.setErrmsg("确认数量不能大于订购数量" + quantity);
				errors.add(error);
			} else {
				// 检查设备工具备品是否存在
				DeviceSpareService deviceSpareService = new DeviceSpareService();
				DeviceSpareForm deviceSpareForm = deviceSpareService.getDeviceSpare(form, conn);
				if (deviceSpareForm == null) {
					String msg = "品名为[" + pageForm.getDevice_type_name() + "],型号/规格为[" + pageForm.getModel_name() + "]备品种类为["
							+ CodeListUtils.getValue("device_spare_type", pageForm.getDevice_spare_type()) + "]";
					MsgInfo error = new MsgInfo();
					error.setErrcode("dbaccess.recordNotExist");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordNotExist", msg));
					errors.add(error);
				} else {
					// 作为OK收货
					pageForm.setConfirm_flg("1"); // 1 = OK
					deviceJigOrderDetailService.updateConfirm(pageForm, conn);
					// 入库
					deviceJigOrderDetailService.addSpare(pageForm, req, conn);
				}
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.doAddSpare end");
	}

	/**
	 * 验收
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdateRecept(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigOrderAction.doUpdateRecept start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
			DeviceJigOrderDetailForm deviceJigOrderDetailForm = (DeviceJigOrderDetailForm) form;
			// 验收人
			deviceJigOrderDetailForm.setInline_receptor_id(user.getOperator_id());
			// 验收日期
			deviceJigOrderDetailForm.setInline_recept_date(DateUtil.toString(Calendar.getInstance().getTime(), DateUtil.DATE_PATTERN));

			deviceJigOrderDetailService.updateInlineRecept(deviceJigOrderDetailForm, conn);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.doUpdateRecept end");
	}

	/**
	 * 预算
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdateBudget(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigOrderAction.doUpdateRecept start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			DeviceJigOrderDetailForm deviceJigOrderDetailForm = (DeviceJigOrderDetailForm) form;
			// 预算月
			String budgetMonth = deviceJigOrderDetailForm.getBudget_month();
			Pattern p = Pattern.compile("\\d{6}");
			Matcher matcher = p.matcher(budgetMonth);
			if (!matcher.matches()) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("validator.invalidParam.invalidDateValue");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.invalidParam.invalidDateValue", "预算月", "yyyyMM"));
				errors.add(error);
			}

			if (errors.size() == 0) {
				deviceJigOrderDetailService.updateBudget(deviceJigOrderDetailForm, conn);
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.doUpdateRecept end");
	}
	
	/**
	 * 修改订单号
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdateOrderNo(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("DeviceJigOrderAction.doUpdateOrderNo start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		DeviceJigOrderDetailForm deviceJigOrderDetailForm = (DeviceJigOrderDetailForm) form;
		String orderNo = deviceJigOrderDetailForm.getOrder_no();
		
		DeviceJigOrderForm deviceJigOrderForm = new DeviceJigOrderForm();
		deviceJigOrderForm.setOrder_no(orderNo);
		deviceJigOrderForm.setOrder_key(deviceJigOrderDetailForm.getOrder_key());
		
		Validators v = BeanUtil.createBeanValidators(deviceJigOrderForm, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();
		
		if(errors.size() == 0){
			DeviceJigOrderForm tempForm = deviceJigOrderService.getDeviceJigOrderByOrderNo(orderNo, conn);
			//订单号重复
			if(tempForm != null){
				MsgInfo error = new MsgInfo();
				error.setErrcode("dbaccess.recordDuplicated");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", "订单号" + orderNo));
				errors.add(error);
			}else{
				deviceJigOrderService.update(deviceJigOrderForm, conn);
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigOrderAction.doUpdateOrderNo end");
	}

	/**
	 * 发票登记
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdateTicket(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn)throws Exception{
		log.info("DeviceJigOrderAction.doUpdateTicket start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();
		
		if(errors.size() == 0){
			deviceJigOrderDetailService.updateTicket(form, conn);
		}
		
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("DeviceJigOrderAction.doUpdateTicket end");
	}
	
}
