package com.osh.rvs.action.partial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.PartialBussinessStandardForm;
import com.osh.rvs.form.master.PartialForm;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.service.PartialBussinessStandardService;
import com.osh.rvs.service.PartialService;
import com.osh.rvs.service.partial.FactProductionFeatureService;
import com.osh.rvs.service.partial.PartialCollationService;
import com.osh.rvs.service.partial.PartialWarehouseDetailService;
import com.osh.rvs.service.partial.PartialWarehouseService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CommonStringUtil;

/**
 * 零件核对
 *
 * @author liuxb
 *
 */
public class PartialCollationAction extends BaseAction {
	private final Logger log = Logger.getLogger(getClass());

	private final PartialService partialService = new PartialService();

	// 零件入库单
	private final PartialWarehouseService partialWarehouseService = new PartialWarehouseService();
	// 零件入库明细
	private final PartialWarehouseDetailService partialWarehouseDetailService = new PartialWarehouseDetailService();
	// 现品作业信息
	private final FactProductionFeatureService factProductionFeatureService = new FactProductionFeatureService();

	private final PartialBussinessStandardService partialBussinessStandardService = new PartialBussinessStandardService();

	private final PartialCollationService partialCollationService = new PartialCollationService();

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
		log.info("PartialCollationAction.init start");

		actionForward = mapping.findForward(FW_INIT);

		log.info("PartialCollationAction.init end");
	}

	public void jsinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialCollationAction.jsinit start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);
		callbackResponse.put("unfinish", factProductionFeature);

		if (factProductionFeature != null) {
			String key = factProductionFeature.getPartial_warehouse_key();
			// 作业内容
			String productionType = factProductionFeature.getProduction_type();

			// 当前作业单中所有零件
			List<PartialWarehouseDetailForm> list = partialWarehouseDetailService.searchByKey(key, conn);
			callbackResponse.put("allPartialList", list);

			// /过滤核对的数据
			List<PartialWarehouseDetailForm> partialWarehouseDetailList = partialCollationService.filterCollation(list, productionType);

			callbackResponse.put("partialWarehouseDetailList", partialWarehouseDetailList);

			// 作业标准时间
			String leagal_overline = partialCollationService.getStandardTime(list, productionType, conn);
			callbackResponse.put("leagal_overline", leagal_overline);

			// 作业经过时间
			String spent_mins = partialCollationService.getSpentTimes(factProductionFeature, conn);
			callbackResponse.put("spent_mins", spent_mins);
		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialCollationAction.jsinit end");
	}

	/**
	 * 查询未核对零件入库单信息
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchUnCollation(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialCollationAction.searchUnCollation start");

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
		partialWarehouseForm.setStep("1");// 1:表示收货结束
		// 零件入库单信息
		List<PartialWarehouseForm> partialWarehouseList = partialWarehouseService.searchStepPartialWarehouse(partialWarehouseForm, conn);
		callbackResponse.put("partialWarehouseList", partialWarehouseList);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialCollationAction.searchUnCollation end");
	}

	/**
	 * 扫描检查
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void checkScanner(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialCollationAction.checkScanner start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String code = req.getParameter("code");

		// 零件信息
		PartialForm partialForm = partialService.getDetail(code, conn, errors);

		if (errors.size() == 0) {
			// 零件出入库工时标准
			PartialBussinessStandardForm partialBussinessStandardForm = partialBussinessStandardService.getPartialBussinessStandardBySpecKind(partialForm.getSpec_kind(), conn);
			callbackResponse.put("partialBussinessStandardForm", partialBussinessStandardForm);
		}

		callbackResponse.put("partialForm", partialForm);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialCollationAction.checkScanner end");
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
		log.info("PartialCollationAction.doBreak start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(req, conn);

		String factPfKey = factProductionFeatureForm.getFact_pf_key();
		String key = factProductionFeatureForm.getPartial_warehouse_key();

		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		List<PartialWarehouseDetailForm> list = new AutofillArrayList<PartialWarehouseDetailForm>(PartialWarehouseDetailForm.class);

		Map<String, String[]> parameters = req.getParameterMap();

		for (String parameterKey : parameters.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("partial_warehouse_detail".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameters.get(parameterKey);

					if ("partial_id".equals(column)) {
						list.get(icounts).setPartial_id(value[0]);
					} else if ("collation_quantity".equals(column)) {
						list.get(icounts).setCollation_quantity(value[0]);
					} else if ("flg".equals(column)) {
						list.get(icounts).setFlg(value[0]);
					}

					list.get(icounts).setKey(key);
					list.get(icounts).setFact_pf_key(factPfKey);
				}
			}
		}

		for (PartialWarehouseDetailForm partialWarehouseDetailForm : list) {
			String flg = partialWarehouseDetailForm.getFlg();

			if ("1".equals(flg)) {// 零件在此单中不存在
				String collationQuantity = partialWarehouseDetailForm.getCollation_quantity();
				partialWarehouseDetailForm.setQuantity(collationQuantity);
				partialWarehouseDetailForm.setCollation_quantity("-" + collationQuantity);
				partialWarehouseDetailService.insert(partialWarehouseDetailForm, conn);
			} else if ("0".equals(flg)) {
				String collationQuantity = partialWarehouseDetailForm.getCollation_quantity();
				partialWarehouseDetailForm.setQuantity(collationQuantity);
				partialWarehouseDetailForm.setCollation_quantity("-" + collationQuantity);
				partialWarehouseDetailService.update(partialWarehouseDetailForm, conn);
			} else {
				partialWarehouseDetailService.update(partialWarehouseDetailForm, conn);
			}
		}

		// 更新处理结束时间
		factProductionFeatureService.updateFinishTime(factProductionFeatureForm, conn);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialCollationAction.doBreak end");
	}

	/**
	 * 检查是否核对完毕
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void checkCollationFinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialCollationAction.checkCollationFinish start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		Map<String, String> partialMap = new HashMap<String, String>();
		Map<String, String[]> parameters = req.getParameterMap();

		for (String parameterKey : parameters.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("partial_warehouse_detail".equals(entity)) {
					String column = m.group(2);
					String[] value = parameters.get(parameterKey);
					String partialID = "";
					String collationQuantity = "";
					if ("partial_id".equals(column)) {
						partialID = value[0];
					} else if ("collation_quantity".equals(column)) {
						collationQuantity = value[0];
					}
					partialMap.put(partialID, collationQuantity);
				}
			}
		}

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(req, conn);
		String key = factProductionFeatureForm.getPartial_warehouse_key();

		// 当前作业单中所有零件
		List<PartialWarehouseDetailForm> list = partialWarehouseDetailService.searchByKey(key, conn);
		for (PartialWarehouseDetailForm partialWarehouseDetailForm : list) {
			// 页面核对的零件不用检查
			if (partialMap.containsKey(partialWarehouseDetailForm.getPartial_id())) {
				continue;
			}

			String factPfKey = partialWarehouseDetailForm.getFact_pf_key();
			// 未核对
			if (CommonStringUtil.isEmpty(factPfKey)) {
				MsgInfo error = new MsgInfo();
				error.setErrmsg("此单未核对完毕！");
				errors.add(error);
				break;
			}
		}
		if (errors.size() == 0) {
			boolean flg = false;
			for (PartialWarehouseDetailForm partialWarehouseDetailForm : list) {
				String partialID = partialWarehouseDetailForm.getPartial_id();

				// 数量
				Integer quantity = Integer.valueOf(partialWarehouseDetailForm.getQuantity());

				// 核对数量
				Integer collationQuantity = null;

				if (partialMap.containsKey(partialID)) {
					collationQuantity = Integer.valueOf(partialMap.get(partialID));
				} else {
					collationQuantity = Integer.valueOf(partialWarehouseDetailForm.getCollation_quantity());
					if (collationQuantity < 0) {
						collationQuantity = collationQuantity * -1;
					}
				}

				// 核对数量不一致
				if (quantity != collationQuantity) {
					flg = true;
					break;
				}
			}

			callbackResponse.put("differ", flg);
		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialCollationAction.checkCollationFinish end");

	}

	public void doFinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialCollationAction.doFinish start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String step = req.getParameter("step");

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(req, conn);
		String factPfKey = factProductionFeatureForm.getFact_pf_key();
		String key = factProductionFeatureForm.getPartial_warehouse_key();

		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		List<PartialWarehouseDetailForm> list = new AutofillArrayList<PartialWarehouseDetailForm>(PartialWarehouseDetailForm.class);

		Map<String, String[]> parameters = req.getParameterMap();

		for (String parameterKey : parameters.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("partial_warehouse_detail".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameters.get(parameterKey);

					if ("partial_id".equals(column)) {
						list.get(icounts).setPartial_id(value[0]);
					} else if ("collation_quantity".equals(column)) {
						list.get(icounts).setCollation_quantity(value[0]);
					} else if ("flg".equals(column)) {
						list.get(icounts).setFlg(value[0]);
					}

					list.get(icounts).setKey(key);
					list.get(icounts).setFact_pf_key(factPfKey);
				}
			}
		}

		for (PartialWarehouseDetailForm partialWarehouseDetailForm : list) {
			String flg = partialWarehouseDetailForm.getFlg();

			if ("1".equals(flg)) {// 零件在此单中不存在
				String collationQuantity = partialWarehouseDetailForm.getCollation_quantity();
				partialWarehouseDetailForm.setQuantity(collationQuantity);
				partialWarehouseDetailForm.setCollation_quantity("-" + collationQuantity);
				partialWarehouseDetailService.insert(partialWarehouseDetailForm, conn);
			} else if ("0".equals(flg)) {
				String collationQuantity = partialWarehouseDetailForm.getCollation_quantity();
				partialWarehouseDetailForm.setQuantity(collationQuantity);
				partialWarehouseDetailForm.setCollation_quantity("-" + collationQuantity);
				partialWarehouseDetailService.update(partialWarehouseDetailForm, conn);
			} else {
				partialWarehouseDetailService.update(partialWarehouseDetailForm, conn);
			}
		}

		// 更新处理结束时间
		factProductionFeatureService.updateFinishTime(factProductionFeatureForm, conn);

		// 结束核对单
		PartialWarehouseForm partialWarehouseForm = new PartialWarehouseForm();
		// KEY
		partialWarehouseForm.setKey(key);
		// 入库进展
		partialWarehouseForm.setStep(step);

		// 更新入库进展
		partialWarehouseService.updateStep(partialWarehouseForm, conn);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialCollationAction.doFinish end");
	}

}
