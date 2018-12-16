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
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.partial.FactPartialWarehouseForm;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.service.partial.FactPartialWarehouseService;
import com.osh.rvs.service.partial.FactProductionFeatureService;
import com.osh.rvs.service.partial.PartialCollationService;
import com.osh.rvs.service.partial.PartialOnShelfService;
import com.osh.rvs.service.partial.PartialUnpackService;
import com.osh.rvs.service.partial.PartialWarehouseService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;

/**
 * 零件上架
 *
 * @author liuxb
 *
 */
public class PartialOnShelfAction extends BaseAction {
	private final Logger log = Logger.getLogger(getClass());
	// 零件入库单
	private final PartialWarehouseService partialWarehouseService = new PartialWarehouseService();
	// 现品作业信息
	private final FactProductionFeatureService factProductionFeatureService = new FactProductionFeatureService();
	// 现品入库作业数
	private final FactPartialWarehouseService factPartialWarehouseService = new FactPartialWarehouseService();

	private final PartialUnpackService partialUnpackService = new PartialUnpackService();

	private final PartialCollationService partialCollationService = new PartialCollationService();

	private final PartialOnShelfService partialOnShelfService = new PartialOnShelfService();

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
		log.info("PartialOnShelfAction.init start");

		actionForward = mapping.findForward(FW_INIT);

		log.info("PartialOnShelfAction.init end");
	}

	public void jsinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialOnShelfAction.jsinit start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);
		callbackResponse.put("unfinish", factProductionFeature);

		if (factProductionFeature != null) {
			String key = factProductionFeature.getPartial_warehouse_key();

			// 核对完待上架的零件
			List<PartialWarehouseDetailForm> list = partialCollationService.filterCollation(key, "21", conn);

			Map<String, PartialWarehouseDetailForm> map = new HashMap<String, PartialWarehouseDetailForm>();
			for (PartialWarehouseDetailForm tempForm : list) {
				// 统计每种类别核对总数
				Integer collationQuantity = Integer.valueOf(tempForm.getCollation_quantity());
				String specKind = tempForm.getSpec_kind();

				if (map.containsKey(specKind)) {
					PartialWarehouseDetailForm partialWarehouseDetailForm = map.get(specKind);
					collationQuantity = Integer.valueOf(partialWarehouseDetailForm.getCollation_quantity()) + collationQuantity;
					partialWarehouseDetailForm.setCollation_quantity(collationQuantity.toString());

					map.put(specKind, partialWarehouseDetailForm);
				} else {
					map.put(specKind, tempForm);
				}
			}

			// 统计不同规格种别核对总数
			List<PartialWarehouseDetailForm> specKindQuantityList = new ArrayList<PartialWarehouseDetailForm>();
			for (String specKind : map.keySet()) {
				specKindQuantityList.add(map.get(specKind));
			}

			FactPartialWarehouseForm factPartialWarehouseForm = new FactPartialWarehouseForm();
			factPartialWarehouseForm.setKey(factProductionFeature.getPartial_warehouse_key());
			factPartialWarehouseForm.setOperator_id(factProductionFeature.getOperator_id());
			factPartialWarehouseForm.setProduction_type("40");

			// 统计不同规格种别已经上架总数
			List<FactPartialWarehouseForm> packList = factPartialWarehouseService.countQuantityOfSpecKind(factPartialWarehouseForm, conn);
			callbackResponse.put("packList", packList);

			callbackResponse.put("partialWarehouseDetailList", list);
			callbackResponse.put("specKindQuantityList", specKindQuantityList);

			// 作业标准时间
			String leagal_overline = partialOnShelfService.getStandardTime(list, conn);
			callbackResponse.put("leagal_overline", leagal_overline);

			// 作业经过时间
			String spent_mins = partialOnShelfService.getSpentTimes(factProductionFeature, conn);
			callbackResponse.put("spent_mins", spent_mins);

		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOnShelfAction.jsinit end");
	}

	/**
	 * 查询待上架零件入库单信息
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchUnOnShelf(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialOnShelfAction.searchUnOnShelf start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		// 操作者 ID
		String operatorID = user.getOperator_id();

		// 查询当前操作者未核对零件入库单
		PartialWarehouseForm partialWarehouseForm = new PartialWarehouseForm();
		partialWarehouseForm.setOperator_id(operatorID);
		partialWarehouseForm.setStep("2");// 2:表示核对结束

		// 零件入库单信息
		List<PartialWarehouseForm> partialWarehouseList = partialWarehouseService.searchStepPartialWarehouse(partialWarehouseForm, conn);

		List<PartialWarehouseForm> respList = new ArrayList<PartialWarehouseForm>();
		for (PartialWarehouseForm temp : partialWarehouseList) {
			boolean flg = partialOnShelfService.checkOnShelfinished(temp.getKey(), operatorID, conn);
			if (!flg) {
				respList.add(temp);
			}
		}

		callbackResponse.put("partialWarehouseList", respList);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOnShelfAction.searchUnOnShelf end");
	}

	public void checkQuantity(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialOnShelfAction.checkQuantity start");
		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String endFlg = req.getParameter("endFlg");

		List<FactPartialWarehouseForm> list = partialOnShelfService.collectData(req);

		// 数据合法性检查
		partialOnShelfService.checkData(list, errors);

		if (errors.size() == 0 && "1".equals(endFlg)) {
			// 进行中的作业信息
			FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);
			String key = factProductionFeature.getPartial_warehouse_key();
			String operatorID = factProductionFeature.getOperator_id();

			// 判断分装是否完成
			boolean packFlg = partialUnpackService.checkPackFinished(key, operatorID, conn);
			if (!packFlg) {
				MsgInfo error = new MsgInfo();
				error.setErrmsg("此单还未分装完毕！");
				errors.add(error);
			} else {
				// 上架数量与核对总数进行比较
				for (FactPartialWarehouseForm factPartialWarehouseForm : list) {
					// 核对总数
					Integer total = Integer.valueOf(factPartialWarehouseForm.getTotal_split_quantity());
					// 上次上架数量
					Integer splitQuantity = Integer.valueOf(factPartialWarehouseForm.getSplit_quantity());
					// 本次上架数量
					Integer quantity = Integer.valueOf(factPartialWarehouseForm.getQuantity());

					if (total != (splitQuantity + quantity)) {
						MsgInfo error = new MsgInfo();
						error.setErrmsg("此单还未上架完毕！");
						errors.add(error);
						break;
					}
				}
			}
		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOnShelfAction.checkQuantity end");
	}

	/**
	 * 中断
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doBreak(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialOnShelfAction.doBreak start");
		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);

		List<FactPartialWarehouseForm> list = partialOnShelfService.collectData(req);
		for (FactPartialWarehouseForm factPartialWarehouseForm : list) {
			// 新建现品入库作业数
			factPartialWarehouseForm.setFact_pf_key(factProductionFeature.getFact_pf_key());
			factPartialWarehouseService.insert(factPartialWarehouseForm, conn);
		}

		// 更新处理结束时间
		factProductionFeatureService.updateFinishTime(factProductionFeature, conn);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOnShelfAction.doBreak end");
	}

	public void doFinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialOnShelfAction.doFinish start");
		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);

		List<FactPartialWarehouseForm> list = partialOnShelfService.collectData(req);
		for (FactPartialWarehouseForm factPartialWarehouseForm : list) {
			// 新建现品入库作业数
			factPartialWarehouseForm.setFact_pf_key(factProductionFeature.getFact_pf_key());
			factPartialWarehouseService.insert(factPartialWarehouseForm, conn);
		}

		PartialWarehouseForm partialWarehouseForm = new PartialWarehouseForm();
		// KEY
		partialWarehouseForm.setKey(factProductionFeature.getPartial_warehouse_key());
		// 入库进展
		partialWarehouseForm.setStep(req.getParameter("step"));

		// 更新入库进展
		partialWarehouseService.updateStep(partialWarehouseForm, conn);

		// 更新处理结束时间
		factProductionFeatureService.updateFinishTime(factProductionFeature, conn);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialOnShelfAction.doFinish end");
	}

}
