package com.osh.rvs.action.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.master.BrandEntity;
import com.osh.rvs.form.master.BrandForm;
import com.osh.rvs.service.BrandService;

import org.apache.log4j.Logger;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.validator.Validators;

public class BrandAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	private BrandService service = new BrandService();

	/**
	 * 画面初始化
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {
		log.info("BrandAction.init start");

		//特定设备工具种类
		req.setAttribute("businessRelationship", CodeListUtils.getSelectOptions("brand_business_relationship",null,""));
		req.setAttribute("goBusinessRelationship",CodeListUtils.getGridOptions("brand_business_relationship"));

		//特定设备工具种类
		req.setAttribute("deviceHazardousClassification", CodeListUtils.getSelectOptions("device_hazardous_classification",null,""));

		actionForward = mapping.findForward(FW_INIT);

		log.info("BrandAction.init end");
	}

	/**
	 * 设备工具品名一览
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 */
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response, SqlSession conn) {
		log.info("BrandAction .search start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		BrandForm brandForm = (BrandForm) form;
		BrandEntity brandEntity = new BrandEntity();

		BeanUtil.copyToBean(brandForm, brandEntity, CopyOptions.COPYOPTIONS_NOEMPTY);

		List<BrandForm> brandForms = service.searchBrand(brandEntity, conn);

		if (brandForm.getBrand_id() != null && brandForms.size() > 0) {
			brandForm = brandForms.get(0);
			brandForm.setBusiness_relationship_text(CodeListUtils.getValue("brand_business_relationship", brandForm.getBusiness_relationship()));
		}

		listResponse.put("brandForms", brandForms);

		listResponse.put("errors", errors);
		returnJsonResponse(response, listResponse);

		log.info("BrandAction .search end");
	}

	/**
	 * 新建设备工具品名
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 */
	@Privacies(permit = { 124, 1, 0 })
	public void doinsert(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response, SqlSessionManager conn) throws Exception {
		log.info("BrandAction .doupdate start");
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		
		/*验证*/
		Validators v=BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors=v.validate();

		// 插入新建设备工具品名
		service.insertBrand(form,conn,request.getSession(),errors);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(response, callbackResponse);
		log.info("BrandAction .doupdate end");
	}
	
	/**
	 * 删除设备工具品名
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
		log.info("BrandAction.dodelete start");
		// 盛放错误信息
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		// 检查时返回的验证信息
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ONLYKEY);
		List<MsgInfo> errors = v.validate();
		// 当检查错误信息无
		if (errors.size() == 0) {
			service.deleteBrand(form,conn, request.getSession(),errors);
		}
		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回JSON格式响应的信息
		returnJsonResponse(response, callbackResponse);
		log.info("BrandAction.dodelete end");
	}
	
	/**
	 * 双击修改设备工具品名
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 124, 1, 0 })
	public void doupdate(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response,
			SqlSessionManager conn) throws Exception {
		log.info("BrandAction.doupdate start");
		// 盛放错误信息
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		// 检查表单验证信息
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v.validate();
		
		service.updateBrand(form, conn, request.getSession(),errors);

		callbackResponse.put("errors", errors);
		returnJsonResponse(response, callbackResponse);
		log.info("BrandAction.doupdate end");
	}
}
