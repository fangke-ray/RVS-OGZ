package com.osh.rvs.action.equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.form.equipment.DeviceJigInvoiceForm;
import com.osh.rvs.service.equipment.DeviceJigInvoiceService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.validator.Validators;

/**
 * 设备工具治具订购询价
 * 
 * @author liuxb
 * 
 */
public class DeviceJigInvoiceAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	/**
	 * 新建询价
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doInsert(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigInvoiceAction.doInsert start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		DeviceJigInvoiceForm deviceJigInvoiceForm = (DeviceJigInvoiceForm) form;
		String sendDate = deviceJigInvoiceForm.getSend_date();

		// 旧询价发送日期
		String oldSendDate = req.getParameter("old_send_date");

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		v.delete("invoice_id");

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (!CommonStringUtil.isEmpty(sendDate) && !CommonStringUtil.isEmpty(oldSendDate)) {
			int compare = DateUtil.compareDate(DateUtil.toDate(sendDate, DateUtil.DATE_PATTERN), DateUtil.toDate(oldSendDate, DateUtil.DATE_PATTERN));
			if (compare <= 0) {
				MsgInfo error = new MsgInfo();
				error.setErrmsg("询价发送日期不能早于等于" + oldSendDate);
				errors.add(error);
			}
		}

		if (errors.size() == 0) {
			DeviceJigInvoiceService service = new DeviceJigInvoiceService();
			service.insert(form, conn);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigInvoiceAction.doInsert end");
	}

	/**
	 * 更新价格
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpdatePrice(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceJigInvoiceAction.doUpdatePrice start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		v.only("invoice_id", "order_price", "origin_price");

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();
		if (errors.size() == 0) {
			DeviceJigInvoiceService service = new DeviceJigInvoiceService();
			service.updatePrice(form, conn);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceJigInvoiceAction.doUpdatePrice end");
	}
}
