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

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.partial.FactProductionFeatureEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.service.ProductionFeatureService;
import com.osh.rvs.service.inline.PositionPanelService;
import com.osh.rvs.service.partial.FactProductionFeatureService;
import com.osh.rvs.service.partial.PartialOutStorageService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;

/**
 * 零件出库
 *
 * @author liuxb
 *
 */
public class PartialOutStorageAction extends BaseAction {
	private final Logger log = Logger.getLogger(getClass());
	// 现品作业信息
	private final FactProductionFeatureService factProductionFeatureService = new FactProductionFeatureService();

	private final PartialOutStorageService partialOutStorageService = new PartialOutStorageService();

	private final PositionPanelService positionPanelService = new PositionPanelService();
	private final ProductionFeatureService pfService = new ProductionFeatureService();

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
		log.info("PartialOutStorageAction.init start");

		actionForward = mapping.findForward(FW_INIT);

		log.info("PartialOutStorageAction.init end");
	}

	public void jsinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialOutStorageAction.jsinit start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 零件待出库一览
		List<FactProductionFeatureForm> materialList = factProductionFeatureService.searchWaitOutStorage(form, conn);
		callbackResponse.put("materialList", materialList);

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);
		callbackResponse.put("unfinish", factProductionFeature);

		if (factProductionFeature != null) {
			String materialId = factProductionFeature.getMaterial_id();
			FactProductionFeatureEntity factProductionFeatureEntity = partialOutStorageService.getMaterialPartial(materialId, conn, errors);

			if (factProductionFeatureEntity != null) {
				FactProductionFeatureForm materialPartial = new FactProductionFeatureForm();
				BeanUtil.copyToForm(factProductionFeatureEntity, materialPartial, CopyOptions.COPYOPTIONS_NOEMPTY);
				callbackResponse.put("materialPartial", materialPartial);

				// 工位代码
				String processCode = factProductionFeatureEntity.getProcess_code();
				if ("321".equals(processCode)) {
					callbackResponse.put("leagal_overline", "6");
				} else {
					callbackResponse.put("leagal_overline", "9");
				}

				// 作业经过时间
				String spent_mins = partialOutStorageService.getSpentTimes(factProductionFeature, conn);
				callbackResponse.put("spent_mins", spent_mins);
			}
		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOutStorageAction.jsinit end");
	}

	/**
	 * 扫描
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doScan(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialOutStorageAction.doScan start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		FactProductionFeatureForm factProductionFeatureForm = (FactProductionFeatureForm) form;

		partialOutStorageService.scan(factProductionFeatureForm, conn, req, errors);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOutStorageAction.doScan end");
	}

	public void doFinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialOutStorageAction.doFinish start");
		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		List<String> triggerList = new ArrayList<String>();

		// 现品作业信息KEY
		String fact_pf_key = req.getParameter("fact_pf_key");
		FactProductionFeatureForm factProductionFeatureForm = new FactProductionFeatureForm();
		factProductionFeatureForm.setFact_pf_key(fact_pf_key);
		factProductionFeatureService.updateFinishTime(factProductionFeatureForm, conn);

		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		// 取得当前作业中作业信息
		ProductionFeatureEntity workingPf = positionPanelService.getWorkingPf(user, conn);
		Integer use_seconds = positionPanelService.getTotalTimeByRework(workingPf, conn);

		// 作业信息状态改为，作业完成
		workingPf.setOperate_result(RvsConsts.OPERATE_RESULT_FINISH);
		workingPf.setUse_seconds(use_seconds);
		workingPf.setPcs_inputs("{\"EN" + workingPf.getProcess_code() + "01\":\"1\"}");
		workingPf.setPcs_comments("");
		pfService.finishProductionFeature(workingPf, conn);

		try {
			// 启动下个工位
			pfService.fingerNextPosition(workingPf.getMaterial_id(), workingPf, conn, triggerList, true);
		} catch (Exception e) {
			MsgInfo info = new MsgInfo();
			info.setErrmsg(e.getMessage());
			errors.add(info);
			conn.rollback();
		}

		conn.commit();
		RvsUtils.sendTrigger(triggerList);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOutStorageAction.doFinish end");
	}
}
