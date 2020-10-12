/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：作业步骤计数系统管理事件<br>
 * @author 龚镭敏
 * @version 2.5.516
 */
package com.osh.rvs.action.master;

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

import com.osh.rvs.bean.master.ProcedureStepCountEntity;
import com.osh.rvs.form.master.ProcedureStepCountForm;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.PositionService;
import com.osh.rvs.service.master.ProcedureStepCountService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;

public class ProcedureStepCountAction extends BaseAction {

	private Logger _log = Logger.getLogger(getClass());

	/**
	 * 作业步骤计数管理画面初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		_log.info("ProcedureStepCountAction.init start");

		// 工位信息取得
		PositionService positionService = new PositionService();
		String pReferChooser = positionService.getOptions(conn);
		// 工位信息设定
		req.setAttribute("pReferChooser", pReferChooser);

		ModelService modelService = new ModelService();
		String mReferChooser = modelService.getOptions(null, conn);
		req.setAttribute("mReferChooser", mReferChooser);

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		_log.info("ProcedureStepCountAction.init end");
	}

	/**
	 * 做测试
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void test(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		_log.info("ProcedureStepCountAction.test start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		ProcedureStepCountService service = new ProcedureStepCountService();
		service.test2(listResponse, conn);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		_log.info("ProcedureStepCountAction.test end");
	}

	/**
	 * 作业步骤计数一览查询
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		_log.info("ProcedureStepCountAction.search start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		ProcedureStepCountService service = new ProcedureStepCountService();
		List<ProcedureStepCountForm> list = service.search(form, conn);
		listResponse.put("list", list);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		_log.info("ProcedureStepCountAction.search end");
	}

	/**
	 * 作业步骤计数详细
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */	
	public void detail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		_log.info("ProcedureStepCountAction.detail start");
		// Ajax回馈对象
		Map<String, Object> dtlResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ONLYKEY);
		List<MsgInfo> errors = v.validate();

		ProcedureStepCountService service = new ProcedureStepCountService();

		if (errors.size() == 0) {
			service.getDetail(form, dtlResponse, conn, errors);
		}

		// 检查发生错误时报告错误信息
		dtlResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, dtlResponse);

		_log.info("ProcedureStepCountAction.detail end");
	}

	/**
	 * 设备型号数据新建登录实行处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void doinsert(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{

		log.info("ProcedureStepCountAction.doinsert start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 新建记录表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v.validate();

		ProcedureStepCountService service = new ProcedureStepCountService();
		List<ProcedureStepCountEntity> procedureStepOfModels = service.customValidate(req.getParameterMap(), errors);

		if (errors.size() == 0) {
			// 执行插入
			service.insert(form, procedureStepOfModels, req.getSession(), conn);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("ProcedureStepCountAction.doinsert end");
	}

	/**
	 * 设备型号数据更新实行处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void doupdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("ProcedureStepCountAction.doupdate start");
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v.validate();

		ProcedureStepCountService service = new ProcedureStepCountService();
		List<ProcedureStepCountEntity> procedureStepOfModels = service.customValidate(req.getParameterMap(), errors);

		if (errors.size() == 0) {
			// 执行更新
			service.update(form, procedureStepOfModels, req.getSession(), conn);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		log.info("ProcedureStepCountAction.doupdate end");
	}
}
