package com.osh.rvs.action.partial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.service.partial.PartialWarehouseDetailService;
import com.osh.rvs.service.partial.PartialWarehouseService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;

/**
 * 零件入库单
 *
 * @author liuxb
 *
 */
public class PartialWarehouseAction extends BaseAction {

	/**
	 * 页面初始化
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {

		log.info("PartialWarehouseAction.init start");

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("PartialWarehouseAction.init end");
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
		log.info("PartialWarehouseAction.search start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);

		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			PartialWarehouseService service = new PartialWarehouseService();

			List<PartialWarehouseForm> finish = service.search(form, conn);
			listResponse.put("finish", finish);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PartialWarehouseAction.search end");
	}

	public void detail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialWarehouseAction.detail start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		PartialWarehouseForm partialWarehouseForm = (PartialWarehouseForm) form;

		PartialWarehouseDetailService servie = new PartialWarehouseDetailService();

		List<PartialWarehouseDetailForm> list = servie.searchByKey(partialWarehouseForm.getKey(), conn);
		listResponse.put("list", list);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PartialWarehouseAction.detail end");
	}

}
