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
import com.osh.rvs.form.partial.PartialWarehouseDnForm;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.service.partial.FactPartialWarehouseService;
import com.osh.rvs.service.partial.FactProductionFeatureService;
import com.osh.rvs.service.partial.PartialUnpackService;
import com.osh.rvs.service.partial.PartialWarehouseDetailService;
import com.osh.rvs.service.partial.PartialWarehouseDnSerice;
import com.osh.rvs.service.partial.PartialWarehouseService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;

/**
 * 零件仓作业零件分装
 *
 * @author liuxb
 *
 */
public class PartialUnpackAction extends BaseAction {
	private final Logger log = Logger.getLogger(getClass());

	// 零件入库单
	private final PartialWarehouseService partialWarehouseService = new PartialWarehouseService();
	// 零件入库单明细
	private final PartialWarehouseDetailService partialWarehouseDetailService = new PartialWarehouseDetailService();
	// 现品作业信息
	private final FactProductionFeatureService factProductionFeatureService = new FactProductionFeatureService();
	// 现品入库作业数
	private final FactPartialWarehouseService factPartialWarehouseService = new FactPartialWarehouseService();

	private final PartialUnpackService partialUnpackService = new PartialUnpackService();

	private final PartialWarehouseDnSerice partialWarehouseDnSerice = new PartialWarehouseDnSerice();

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
		log.info("PartialUnpackAction.init start");

		// 规格种别
		req.setAttribute("gridSpecKind", CodeListUtils.getGridOptions("partial_spec_kind"));

		actionForward = mapping.findForward(FW_INIT);

		log.info("PartialUnpackAction.init end");
	}

	public void jsinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialUnpackAction.jsinit start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);
		callbackResponse.put("unfinish", factProductionFeature);

		if (factProductionFeature != null) {
			String key = factProductionFeature.getPartial_warehouse_key();

			// 查询需要分装的零件入库明细
			List<PartialWarehouseDetailForm> partialWarehouseDetailList = partialWarehouseDetailService.searchUnpackByKey(key, conn);
			callbackResponse.put("partialWarehouseDetailList", partialWarehouseDetailList);

			// 统计不同规格种别分装总数
			List<PartialWarehouseDetailForm> specKindQuantityList = partialWarehouseDetailService.countUnpackOfSpecKindByKey(key, conn);
			callbackResponse.put("specKindQuantityList", specKindQuantityList);

			FactPartialWarehouseForm factPartialWarehouseForm = new FactPartialWarehouseForm();
			factPartialWarehouseForm.setKey(factProductionFeature.getPartial_warehouse_key());
			factPartialWarehouseForm.setOperator_id(factProductionFeature.getOperator_id());
			factPartialWarehouseForm.setProduction_type("30");

			// 统计不同规格种别已经分装总数
			List<FactPartialWarehouseForm> packList = factPartialWarehouseService.countQuantityOfSpecKind(factPartialWarehouseForm, conn);
			callbackResponse.put("packList", packList);

			// 查询零件入库DN编号
			List<PartialWarehouseDnForm> partialWarehouseDnList = partialWarehouseDnSerice.searchByKey(key, conn);
			callbackResponse.put("partialWarehouseDnList", partialWarehouseDnList);

			// 作业标准时间
			String leagal_overline = partialUnpackService.getStandardTime(partialWarehouseDetailList, conn);
			callbackResponse.put("leagal_overline", leagal_overline);

			// 作业经过时间
			String spent_mins = partialUnpackService.getSpentTimes(factProductionFeature, conn);
			callbackResponse.put("spent_mins", spent_mins);

		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialUnpackAction.jsinit end");
	}

	/**
	 * 查询待分装零件入库单信息
	 *
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchUnpack(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialUnpackAction.searchUnpack start");

		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 当前登录者
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		// 操作者 ID
		String operatorID = user.getOperator_id();

		String step = "2";// 2:表示核对结束

		// 零件入库单信息
		List<PartialWarehouseForm> partialWarehouseList = partialWarehouseService.searchPartialWarehouseByStep(step, conn);

		List<PartialWarehouseForm> respList = new ArrayList<PartialWarehouseForm>();
		for (PartialWarehouseForm temp : partialWarehouseList) {
			boolean flg = partialUnpackService.checkPackFinished(temp.getKey(), operatorID, conn);
			if (!flg) {
				respList.add(temp);
			}
		}

		callbackResponse.put("partialWarehouseList", respList);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialUnpackAction.searchUnpack end");
	}

	public void checkQuantity(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("PartialUnpackAction.checkQuantity start");
		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		String endFlg = req.getParameter("endFlg");

		List<FactPartialWarehouseForm> list = partialUnpackService.collectData(req);
		partialUnpackService.checkData(list, errors);

		// 分装数量与分装总数进行比较
		if (errors.size() == 0 && "1".equals(endFlg)) {
			for (FactPartialWarehouseForm factPartialWarehouseForm : list) {
				// 分装总数
				Integer total = Integer.valueOf(factPartialWarehouseForm.getTotal_split_quantity());
				// 上次分装数量
				Integer splitQuantity = Integer.valueOf(factPartialWarehouseForm.getSplit_quantity());
				// 本次分装数量
				Integer quantity = Integer.valueOf(factPartialWarehouseForm.getQuantity());

				if (total != (splitQuantity + quantity)) {
					MsgInfo error = new MsgInfo();
					error.setErrmsg("此单还未分装完毕！");
					errors.add(error);
					break;
				}

			}
		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialUnpackAction.checkQuantity end");
	}

	public void doFinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("PartialUnpackAction.doFinish start");
		/* Ajax反馈对象 */
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeature = factProductionFeatureService.searchUnFinishProduction(req, conn);

		List<FactPartialWarehouseForm> list = partialUnpackService.collectData(req);

		for (FactPartialWarehouseForm factPartialWarehouseForm : list) {
			factPartialWarehouseForm.setFact_pf_key(factProductionFeature.getFact_pf_key());
			factPartialWarehouseService.insert(factPartialWarehouseForm, conn);
		}

		// 更新处理结束时间
		factProductionFeatureService.updateFinishTime(factProductionFeature, conn);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialUnpackAction.doFinish end");
	}

}
