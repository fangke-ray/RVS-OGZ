package com.osh.rvs.action.equipment;

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

import com.osh.rvs.form.equipment.DeviceJigQuotationForm;
import com.osh.rvs.service.equipment.DeviceJigQuotationService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;

/**
 * 设备工具治具订购报价
 * 
 * @author liuxb
 * 
 */
public class DeviceJigQuotationAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	public void searchAll(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigQuotationAction.searchAll start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		DeviceJigQuotationService service = new DeviceJigQuotationService();
		List<DeviceJigQuotationForm> list = service.searchAll(conn);
		listResponse.put("allQuotationList", list);

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigQuotationAction.searchAll end");
	}

	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchQuotationById(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceJigQuotationAction.searchQuotationById start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("quotation_id", v.required("报价KEY"));

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();
		if (errors.size() == 0) {
			DeviceJigQuotationService service = new DeviceJigQuotationService();

			DeviceJigQuotationForm deviceJigQuotationForm = (DeviceJigQuotationForm) form;
			deviceJigQuotationForm = service.getQuotationById(deviceJigQuotationForm.getQuotation_id(), conn);

			listResponse.put("quotationForm", deviceJigQuotationForm);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigQuotationAction.searchQuotationById end");
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
		log.info("DeviceJigQuotationAction.doUpdate start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		DeviceJigQuotationForm deviceJigQuotationForm = (DeviceJigQuotationForm) form;
		// 委托单号
		String entrustNo = deviceJigQuotationForm.getEntrust_no();
		// 委托发送日期
		String entrustSendDate = deviceJigQuotationForm.getEntrust_send_date();

		Validators v = BeanUtil.createBeanValidators(deviceJigQuotationForm, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("quotation_id", v.required("报价KEY"));

		// “委托单号” 和 “委托发送日期” 是填了一个则必须填另一个。可以都不填。
		if (CommonStringUtil.isEmpty(entrustNo) && !CommonStringUtil.isEmpty(entrustSendDate)) {
			v.add("entrust_no", v.required("委托单号"));
		}
		if (!CommonStringUtil.isEmpty(entrustNo) && CommonStringUtil.isEmpty(entrustSendDate)) {
			v.add("entrust_send_date", v.required("委托发送日期"));
		}

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			DeviceJigQuotationService service = new DeviceJigQuotationService();
			service.updateQuotation(deviceJigQuotationForm, conn);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigQuotationAction.doUpdate end");
	}
}
