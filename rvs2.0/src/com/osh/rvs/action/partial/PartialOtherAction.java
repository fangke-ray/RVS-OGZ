package com.osh.rvs.action.partial;

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

import com.osh.rvs.form.partial.FactProductionCommentForm;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.service.partial.FactProductionCommentService;
import com.osh.rvs.service.partial.FactProductionFeatureService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.validator.MaxlengthValidator;
import framework.huiqing.common.util.validator.Validators;

/**
 * 零件其他
 *
 * @author liuxb
 *
 */
public class PartialOtherAction extends BaseAction {

	private final Logger log = Logger.getLogger(getClass());

	// 现品作业信息
	private final FactProductionFeatureService factProductionFeatureService = new FactProductionFeatureService();

	private final FactProductionCommentService factProductionCommentService = new FactProductionCommentService();

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
		log.info("PartialOtherAction.init start");

		actionForward = mapping.findForward(FW_INIT);

		log.info("PartialOtherAction.init end");
	}

	public void jsinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialOtherAction.jsinit start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);
		callbackResponse.put("unfinish", factProductionFeature);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOtherAction.jsinit end");
	}

	public void doFinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialOtherAction.doFinish start");
		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("comment", req.getParameter("comment"));

		Validators v = new Validators(parameters);
		v.required("comment");
		v.add("comment", new MaxlengthValidator("作业备注内容", 250));

		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 进行中的作业信息
			FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);

			FactProductionCommentForm factProductionCommentForm = new FactProductionCommentForm();
			factProductionCommentForm.setFact_pf_key(factProductionFeature.getFact_pf_key());
			factProductionCommentForm.setComment(req.getParameter("comment"));

			// 新建现品作业备注
			factProductionCommentService.insert(factProductionCommentForm, conn);

			// 更新处理结束时间
			factProductionFeatureService.updateFinishTime(factProductionFeature, conn);
		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOtherAction.doFinish end");
	}

}
