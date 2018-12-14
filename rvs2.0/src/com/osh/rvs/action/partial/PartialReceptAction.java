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

import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.service.partial.FactProductionFeatureService;
import com.osh.rvs.service.partial.PartialReceptService;
import com.osh.rvs.service.partial.PartialWarehouseDetailService;
import com.osh.rvs.service.partial.PartialWarehouseService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;

/**
 * 零件收货
 *
 * @author liuxb
 *
 */
public class PartialReceptAction extends BaseAction {
	private final Logger log = Logger.getLogger(getClass());
	// 零件入库单
	private final PartialWarehouseService partialWarehouseService = new PartialWarehouseService();
	// 零件收货
	private final PartialReceptService partialReceptService = new PartialReceptService();
	// 现品作业信息
	private final FactProductionFeatureService factProductionFeatureService = new FactProductionFeatureService();
	// 零件入库明细
	private final PartialWarehouseDetailService partialWarehouseDetailService = new PartialWarehouseDetailService();

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
		log.info("PartialReceptAction.init start");

		actionForward = mapping.findForward(FW_INIT);

		log.info("PartialReceptAction.init end");
	}

	public void jsinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialReceptAction.jsinit start");
		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);
		callbackResponse.put("unfinished", factProductionFeature);

		// 存在现品作业信息
		if (factProductionFeature != null) {
			String key = factProductionFeature.getPartial_warehouse_key();

			if (!CommonStringUtil.isEmpty(key)) {
				// 查询零件入库明细
				List<PartialWarehouseDetailForm> partialWarehouseDetailList = partialWarehouseDetailService.searchByKey(key, conn);

				PartialWarehouseDetailForm partialWarehouseDetailForm = new PartialWarehouseDetailForm();
				partialWarehouseDetailForm.setKey(key);
				// 统计各个规格种别总数量
				List<PartialWarehouseDetailForm> counttQuantityList = partialWarehouseDetailService.countQuantityOfSpecKind(partialWarehouseDetailForm, conn);

				// 零件入库明细
				callbackResponse.put("partialWarehouseDetailList", partialWarehouseDetailList);
				// 各个规格种别总数量
				callbackResponse.put("counttQuantityList", counttQuantityList);

				// 作业标准时间
				String leagal_overline = partialReceptService.getStandardTime(key,conn);
				callbackResponse.put("leagal_overline", leagal_overline);
			}

			// 作业经过时间
			String spent_mins = partialReceptService.getSpentTimes(factProductionFeature.getAction_time());
			callbackResponse.put("spent_mins", spent_mins);
		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialReceptAction.jsinit end");
	}

	/**
	 * 文件上传
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doUpload(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialReceptAction.doUpload start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		partialReceptService.upload(form, req, conn, errors);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialReceptAction.doUpload end");
	}

	/**
	 * 重新导入
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doReImport(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialReceptAction.doImport start");

		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(req, conn);
		// 零件入库单 KEY
		String key = factProductionFeatureForm.getPartial_warehouse_key();

		factProductionFeatureForm.setPartial_warehouse_key(null);

		// 更新零件入库单 KEY
		factProductionFeatureService.updateKey(factProductionFeatureForm, conn);

		// 存在零件入库单 KEY
		if (!CommonStringUtil.isEmpty(key)) {
			// 删除零件入库单
			partialWarehouseService.delete(key, conn);

			// 删除零件入库明细
			partialWarehouseDetailService.delete(key, conn);
		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialReceptAction.doImport end");
	}

	/**
	 * 结束作业
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doFinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialReceptAction.doFinish start");

		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(req, conn);

		// 更新处理结束时间
		factProductionFeatureService.updateFinishTime(factProductionFeatureForm, conn);

		PartialWarehouseForm partialWarehouseForm = new PartialWarehouseForm();
		// KEY
		partialWarehouseForm.setKey(factProductionFeatureForm.getPartial_warehouse_key());
		// 入库进展
		partialWarehouseForm.setStep(req.getParameter("step"));

		// 更新入库进展
		partialWarehouseService.updateStep(partialWarehouseForm, conn);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialReceptAction.doFinish end");
	}

}
