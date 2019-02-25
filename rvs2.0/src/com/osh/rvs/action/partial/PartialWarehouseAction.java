package com.osh.rvs.action.partial;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.partial.FactProductionFeatureForm;
import com.osh.rvs.form.partial.PartialWarehouseDetailForm;
import com.osh.rvs.form.partial.PartialWarehouseForm;
import com.osh.rvs.service.partial.FactProductionFeatureService;
import com.osh.rvs.service.partial.PartialWarehouseDetailService;
import com.osh.rvs.service.partial.PartialWarehouseService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

/**
 * 零件入库单
 *
 * @author liuxb
 *
 */
public class PartialWarehouseAction extends BaseAction {
	private final FactProductionFeatureService factProductionFeatureService = new FactProductionFeatureService();

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

		Calendar cal = Calendar.getInstance();
		//默认零件入库单号
		req.setAttribute("default_warehouse_no",DateUtil.toString(cal.getTime(), "yyyyMMdd"));

		//返修分类
		req.setAttribute("goStep", CodeListUtils.getGridOptions("partial_warehouse_step"));

		LoginData userData = (LoginData)req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies  = userData.getPrivacies();
		if (privacies.contains(RvsConsts.PRIVACY_FACT_MATERIAL)) {
			req.setAttribute("privacy", "fact");
		}

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

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(req, conn);
		listResponse.put("factProductionFeatureForm", factProductionFeatureForm);

		if(factProductionFeatureForm!=null){
			PartialWarehouseService partialWarehouseService = new PartialWarehouseService();
			PartialWarehouseForm partialWarehouseForm = partialWarehouseService.getByKey(factProductionFeatureForm.getPartial_warehouse_key(), conn);

			listResponse.put("partialWarehouseForm", partialWarehouseForm);
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
		String key = partialWarehouseForm.getKey();
		String seq = partialWarehouseForm.getSeq();

		PartialWarehouseDetailService servie = new PartialWarehouseDetailService();

		List<PartialWarehouseDetailForm> list = servie.searchByKey(key, conn);
		List<PartialWarehouseDetailForm> resplist = new ArrayList<PartialWarehouseDetailForm>();

		for(int i = 0;i < list.size();i++){
			if(seq.equals(list.get(i).getSeq())){
				resplist.add(list.get(i));
			}
		}

		listResponse.put("list", resplist);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PartialWarehouseAction.detail end");
	}

	public void report(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, SqlSession conn) throws Exception {
		log.info("PartialWarehouseAction.report start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		PartialWarehouseService service = new PartialWarehouseService();

		String fileName ="入库单核对不一致一览.xlsx";
		String filePath = service.createUnmatchReport(form, conn);
		listResponse.put("fileName", fileName);
		listResponse.put("filePath", filePath);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);

		log.info("PartialWarehouseAction.report end");
	}

	public void doUpload(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, SqlSessionManager conn) throws Exception {
		log.info("PartialWarehouseAction.doUpload start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		PartialWarehouseService service = new PartialWarehouseService();
		service.supply(form,request,conn, errors);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);

		log.info("PartialWarehouseAction.doUpload end");
	}

	/**
	 * 开始
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void doStart(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, SqlSessionManager conn)throws Exception{
		log.info("PartialWarehouseAction.doStart start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(request, conn);
		if(factProductionFeatureForm != null){
			MsgInfo error = new MsgInfo();
			error.setComponentid("partial_warehouse_key");
			error.setErrcode("info.factwork.workingRemain");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.factwork.workingRemain","零件【" + factProductionFeatureForm.getProduction_type_name()+"】"));
			errors.add(error);
		} else {
			// 当前登录者
			LoginData user = (LoginData) request.getSession().getAttribute(RvsConsts.SESSION_USER);

			factProductionFeatureForm = new FactProductionFeatureForm();

			factProductionFeatureForm.setProduction_type("11");

			factProductionFeatureForm.setPartial_warehouse_key(request.getParameter("key"));

			// 操作者 ID
			factProductionFeatureForm.setOperator_id(user.getOperator_id());

			// 新建现品作业信息
			factProductionFeatureService.insert(factProductionFeatureForm, conn);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);

		log.info("PartialWarehouseAction.doStart end");
	}

	/**
	 * 结束
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param conn
	 * @throws Exception
	 */
	public void doFinish(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, SqlSessionManager conn)throws Exception{
		log.info("PartialWarehouseAction.doFinish start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 进行中的作业信息
		FactProductionFeatureForm factProductionFeatureForm = factProductionFeatureService.searchUnFinishProduction(request, conn);

		factProductionFeatureService.updateFinishTime(factProductionFeatureForm, conn);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(response, listResponse);

		log.info("PartialWarehouseAction.doFinish end");
	}

}
