package com.osh.rvs.action.equipment;

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

import com.osh.rvs.form.equipment.DeviceSpareAdjustForm;
import com.osh.rvs.form.equipment.DeviceSpareForm;
import com.osh.rvs.service.BrandService;
import com.osh.rvs.service.DevicesTypeService;
import com.osh.rvs.service.equipment.DeviceSpareAdjustService;
import com.osh.rvs.service.equipment.DeviceSpareService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.validator.Validators;

/**
 * 设备工具备品
 * 
 * @author liuxb
 * 
 */
public class DeviceSpareAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	private DevicesTypeService devicesTypeService = new DevicesTypeService();

	private BrandService brandService = new BrandService();

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
		log.info("DeviceSpareAction.init start");

		DeviceSpareService service = new DeviceSpareService();

		// 品名
		String nReferChooser = devicesTypeService.getDevicesTypeReferChooser(conn);
		req.setAttribute("nReferChooser", nReferChooser);

		// 备品种类
		req.setAttribute("goDeviceSpareType", CodeListUtils.getSelectOptions("device_spare_type", null, ""));

		// 品牌
		String brandNameReferChooser = brandService.getOptions(conn);
		req.setAttribute("brandNameReferChooser", brandNameReferChooser);

		// 计算开始时期，“起”的默认值为当前日期一个月前
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -30);
		req.setAttribute("startDate", DateUtil.toString(cal.getTime(), DateUtil.DATE_PATTERN));

		// 管理理由
		req.setAttribute("goManageReasonType", CodeListUtils.getSelectOptions("device_spare_adjust_manage_reason_type", null, ""));

		// 设备工具备品总价
		req.setAttribute("totalPrice", service.calculatePrice(conn));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("DeviceSpareAction.init end");
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
		log.info("DeviceSpareAction.search start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		DeviceSpareService service = new DeviceSpareService();
		List<DeviceSpareForm> list = service.search(form, conn);

		// 设备工具备品总价
		Map<String, Integer> map = service.calculatePrice(conn);
		listResponse.put("totalPrice", map);

		listResponse.put("finish", list);
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceSpareAction.search end");
	}

	/**
	 * 新建设备工具备品
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doInsert(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceSpareAction.doInsert start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		DeviceSpareForm dviceSpareForm = (DeviceSpareForm) form;
		// 备品种类
		String deviceSpareType = dviceSpareForm.getDevice_spare_type();

		Validators v = BeanUtil.createBeanValidators(dviceSpareForm, BeanUtil.CHECK_TYPE_ALL);

		if ("1".equals(deviceSpareType)) {// 消耗备品
			v.delete("location");
		} else if ("2".equals(deviceSpareType)) {// 备件
			v.delete("order_cycle");
		}

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			DeviceSpareService service = new DeviceSpareService();
			// 新建设备工具备品
			service.insert(dviceSpareForm, conn, req, errors);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceSpareAction.doInsert end");
	}

	/**
	 * 取消管理
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doCancelManage(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceSpareAction.doCancel start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			DeviceSpareService service = new DeviceSpareService();
			service.canceManage(form, req, conn);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceSpareAction.doCancel end");
	}

	/**
	 * 盘点
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception stock
	 */
	public void doStock(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceSpareAction.doStock start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("adjust_inventory", v.required("修正有效库存"));
		v.add("comment", v.required("调整备注"));
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			DeviceSpareService service = new DeviceSpareService();
			service.stock(form, req, conn);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceSpareAction.doStock end");
	}

	/**
	 * 详细
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void detail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("DeviceSpareAction.detail start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			DeviceSpareService service = new DeviceSpareService();
			DeviceSpareForm deviceSpareForm = service.getDeviceSpare(form, conn);

			DeviceSpareAdjustService deviceSpareAdjustService = new DeviceSpareAdjustService();
			// 调整记录
			List<DeviceSpareAdjustForm> adjustList = deviceSpareAdjustService.searchAdjustRecord(form, conn);

			listResponse.put("deviceSpareForm", deviceSpareForm);
			listResponse.put("adjustList", adjustList);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceSpareAction.detail end");
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
		log.info("DeviceSpareAction.doUpdate start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		DeviceSpareForm dviceSpareForm = (DeviceSpareForm) form;
		// 备品种类
		String deviceSpareType = dviceSpareForm.getDevice_spare_type();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		v.delete("available_inventory");

		if ("1".equals(deviceSpareType)) {// 消耗备品
			v.delete("location");
		} else if ("2".equals(deviceSpareType)) {// 备件
			v.delete("order_cycle");
		}

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			DeviceSpareService service = new DeviceSpareService();
			service.update(form, conn);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceSpareAction.doUpdate end");
	}

	/**
	 * 管理
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doManage(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("DeviceSpareAction.doUpdate start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		DeviceSpareForm dviceSpareForm = (DeviceSpareForm) form;

		// 理由
		String reasonType = dviceSpareForm.getReason_type();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("reason_type", v.required("理由"));
		v.add("adjust_inventory", v.required("数量"));

		if (!"11".equals(reasonType)) {// 除“入库”外，必须填写“调整备注”
			v.add("comment", v.required("调整备注"));
		}

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			DeviceSpareService service = new DeviceSpareService();
			service.manage(form, req, conn, errors);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("DeviceSpareAction.doUpdate end");
	}

}
