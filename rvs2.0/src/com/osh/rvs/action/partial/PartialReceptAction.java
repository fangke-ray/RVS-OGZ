package com.osh.rvs.action.partial;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.form.partial.PartialWarehouseDnForm;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.service.partial.FactProductionFeatureService;
import com.osh.rvs.service.partial.PartialReceptService;
import com.osh.rvs.service.partial.PartialWarehouseDetailService;
import com.osh.rvs.service.partial.PartialWarehouseDnSerice;
import com.osh.rvs.service.partial.PartialWarehouseService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.DateUtil;

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
	// 零件入库DN编号
	private final PartialWarehouseDnSerice partialWarehouseDnSerice = new PartialWarehouseDnSerice();
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
		callbackResponse.put("factProductionFeature", factProductionFeature);

		// 存在现品作业信息
		if (factProductionFeature != null) {
			partialReceptService.jsinit(factProductionFeature,req,callbackResponse,conn);

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
		log.info("PartialReceptAction.doReImport start");

		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();


		HttpSession session =  req.getSession();
		// 零件入库DN编号
		@SuppressWarnings("unchecked")
		List<PartialWarehouseDnForm> warehouseDnList = (List<PartialWarehouseDnForm>)session.getAttribute("warehouseDnList");

		// 零件入库明细
		@SuppressWarnings("unchecked")
		List<PartialWarehouseDetailForm> detailList = (List<PartialWarehouseDetailForm>)session.getAttribute("detailList");

		if(warehouseDnList != null){
			session.removeAttribute("warehouseDnList");
		}

		if(detailList != null){
			session.removeAttribute("detailList");
		}

//		// 进行中的作业信息
//		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(req, conn);
//		// 零件入库单 KEY
//		String key = factProductionFeatureForm.getPartial_warehouse_key();
//
//		// 存在零件入库单 KEY
//		if (!CommonStringUtil.isEmpty(key)) {
//			factProductionFeatureForm.setPartial_warehouse_key(null);
//			// 更新零件入库单 KEY
//			factProductionFeatureService.updateKey(factProductionFeatureForm, conn);
//
//			// 删除零件入库单
//			partialWarehouseService.delete(key, conn);
//
//			// 删除零件入库DN编号
//			partialWarehouseDnSerice.delete(key, conn);
//
//			// 删除零件入库明细
//			partialWarehouseDetailService.delete(key, conn);
//		}

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialReceptAction.doReImport end");
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

		HttpSession session =  req.getSession();
		// 零件入库DN编号
		@SuppressWarnings("unchecked")
		List<PartialWarehouseDnForm> warehouseDnList = (List<PartialWarehouseDnForm>)session.getAttribute("warehouseDnList");

		// 零件入库明细
		@SuppressWarnings("unchecked")
		List<PartialWarehouseDetailForm> detailList = (List<PartialWarehouseDetailForm>)session.getAttribute("detailList");

		CommonMapper commonDao = conn.getMapper(CommonMapper.class);

		// 零件入库单
		PartialWarehouseForm partialWarehouseForm = new PartialWarehouseForm();
		String warehouseNo = DateUtil.toString(Calendar.getInstance().getTime(), "yyyyMMdd");

		// 查询最大零件入库单号
		Integer maxWarehouseNo = partialWarehouseService.getMaxWarehouseNo(warehouseNo, conn);

		if (maxWarehouseNo == null) {
			partialWarehouseForm.setWarehouse_no(warehouseNo + "01");
		} else {
			partialWarehouseForm.setWarehouse_no(String.valueOf(maxWarehouseNo + 1));
		}

		//导入了入库单
		if(detailList != null){
			// 收货完成为1
			partialWarehouseForm.setStep("1");
		}else{
			// 收货未完成为0
			partialWarehouseForm.setStep("0");
		}

		// 新建零件入库单
		partialWarehouseService.insert(partialWarehouseForm, conn);
		String key = commonDao.getLastInsertID();

		if(detailList != null){
			for (int i = 0; i < warehouseDnList.size(); i++) {
				PartialWarehouseDnForm partialWarehouseDnForm = warehouseDnList.get(i);
				// KEY
				partialWarehouseDnForm.setKey(key);
				// 新建零件入库DN编号
				partialWarehouseDnSerice.insert(partialWarehouseDnForm, conn);
			}

			for (int i = 0; i < detailList.size(); i++) {
				PartialWarehouseDetailForm partialWarehouseDetailForm = detailList.get(i);
				// KEY
				partialWarehouseDetailForm.setKey(key);
				// 新建零件入库明细
				partialWarehouseDetailService.insert(partialWarehouseDetailForm, conn);
			}

			session.removeAttribute("warehouseDnList");
			session.removeAttribute("detailList");
		}

		//收货完成
		factProductionFeatureForm.setPartial_warehouse_key(key);
		factProductionFeatureService.finishRecept(factProductionFeatureForm, conn);

		/* 检查错误时报告错误信息 */
		callbackResponse.put("errors", errors);
		/* 返回Json格式响应信息 */
		returnJsonResponse(res, callbackResponse);

		log.info("PartialReceptAction.doFinish end");
	}

}
