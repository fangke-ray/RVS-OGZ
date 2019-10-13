package com.osh.rvs.action.partial;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.master.LineForm;
import com.osh.rvs.form.partial.MaterialPartialForm;
import com.osh.rvs.service.DownloadService;
import com.osh.rvs.service.LineService;
import com.osh.rvs.service.MaterialProcessService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.PartialService;
import com.osh.rvs.service.partial.MaterialPartialService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.RequiredValidator;
import framework.huiqing.common.util.validator.Validators;

public class MaterialPartialAction extends BaseAction {
	
	private MaterialPartialService materialPartialService = new MaterialPartialService();
	private ModelService modelService = new ModelService();
	LineService lineService = new LineService();
	
	/**
	 * 画面初始化
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("MaterialAction.init start");
		
		//维修对象型号
		String mReferChooser = modelService.getRepairOptions(conn);
		req.setAttribute("mReferChooser", mReferChooser);
		
		//在线工程
		String lOptions = lineService.getInlineOptions(RvsConsts.DEPART_REPAIR, conn);
		req.setAttribute("lOptions", lOptions);
		
		String goMaterialLevel = CodeListUtils.getGridOptions("material_level");
		req.setAttribute("goMaterialLevel", goMaterialLevel);
		
		String goPartialBoFlg = CodeListUtils.getGridOptions("material_partial_bo_flg");
		req.setAttribute("goPartialBoFlg", goPartialBoFlg);

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		// 取得登录用户权限
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = user.getPrivacies();

		// 零件管理
		if (privacies.contains(RvsConsts.PRIVACY_ADMIN)) {
			req.setAttribute("privacy", "pm");
		}

		// 现品 (管理/订购)
		else if (privacies.contains(RvsConsts.PRIVACY_FACT_MATERIAL)) {
			req.setAttribute("privacy", "fact");
		}
		else if (privacies.contains(RvsConsts.PRIVACY_PROCESSING)) {
			req.setAttribute("privacy", "process");
		}
		
		log.info("MaterialAction.init end");
	}
	
	public void doUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("MaterialPartialAction.doUpdate start");
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		
		MaterialPartialForm materialPartialForm = (MaterialPartialForm)form;
		String bo_flg = materialPartialForm.getBo_flg();
		
		if(!"7".equals(bo_flg) && !"9".equals(bo_flg)){//非预提状态下（不包括待发放）  零件订购日不能为空
			v.add("order_date", new RequiredValidator("零件订购日"));
		}
		if("1".equals(bo_flg) || "0".equals(bo_flg) || "2".equals(bo_flg)){//有BO/无BO/BO解决  零件到货日不能为空
			v.add("arrival_date", new RequiredValidator("零件到货日"));
		}
		
		List<MsgInfo> errors = v != null ? v.validate(): new ArrayList<MsgInfo>();
		
		if (errors.size() == 0) {
			LoginData userdata = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

			materialPartialService.updateMaterialPartial(form, userdata, conn);
		}
		
		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);
		log.info("MaterialPartialAction.doUpdate end");
	}
	
	/**
	 * 新建维修对象预提订购单
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doInsert(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("MaterialPartialAction.doInsert start");
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
		
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v != null ? v.validate(): new ArrayList<MsgInfo>();
		
		if (errors.size() == 0) {
			materialPartialService.insertMaterialPartial(form, conn);
		}
		
		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);
		log.info("MaterialPartialAction.doInsert end");
	}
	
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {

		log.info("MaterialPartialAction.search start");
		// Ajax回馈对象	
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);

		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行检索
			List<MaterialPartialForm> lResultForm = materialPartialService.searchMaterial(form, conn, errors);
			
			// 查询结果放入Ajax响应对象
			listResponse.put("list", lResultForm);
			req.getSession().setAttribute("resultFormList", lResultForm);
			Date from = null;
			Date to = null;

			//取BO率
			String[] rate = materialPartialService.getBoRate(from, to, conn);
			listResponse.put("rate", "本周零件的 当天BO率：" + rate[0] + " % |  三天BO率：" + rate[1] + " %");
			
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("MaterialPartialAction.search end");
	}

	public void loadByTimes(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) {
		log.info("MaterialPartialAction.loadByTimes start");
		// Ajax回馈对象	
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		String id = req.getParameter("id");
		String times = req.getParameter("times");
		
		MaterialPartialForm partialForm = materialPartialService.loadMaterialPartial(conn, id, Integer.parseInt(times));
		
		listResponse.put("partialForm", partialForm);
		
		returnJsonResponse(res, listResponse);
		
		log.info("MaterialPartialAction.loadByTimes end");
	}
	
	public void report(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("MaterialPartialAction.report start");
		// Ajax回馈对象	
		Map<String, Object> listResponse = new HashMap<String, Object>();

		String fileName = "零件订购一览.xls";
		String filePath = materialPartialService.reportMaterialPartial(req);
		listResponse.put("filePath", filePath);
		
		listResponse.put("fileName", fileName);
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("MaterialPartialAction.report end");
	}
	
	public void export(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		String filePath = req.getParameter("filePath");
		String fileName = new String("BO零件管理一览.xls".getBytes("gbk"),"iso-8859-1");
		
		DownloadService dservice = new DownloadService();
		dservice.writeFile(res, DownloadService.CONTENT_TYPE_EXCEL, fileName, filePath);
	}
	
	/**
	 * 取得所有没有投线，也没有建立过维修对象零件订购单的维修对象
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 */
	public void searchNotOrderMaterail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn){
		log.info("MaterialPartialAction.searchNotOrderMaterail start");
		// Ajax回馈对象	
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		List<MaterialPartialForm> list = materialPartialService.searchNotOrderMaterail(form, conn);
		
		listResponse.put("list", list);
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("MaterialPartialAction.searchNotOrderMaterail end");
	}
	
	
	public void detail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn){
		log.info("MaterialPartialAction.detail start");

		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		//取得页面提交的维修对象ID
		String material_id = req.getParameter("material_id");
		
		//取得所有在线工程
		LineForm lineForm = new LineForm();
		lineForm.setInline_flg("true");
		LineService lineService = new LineService();
		List<LineForm> allInLineList = lineService.search(lineForm, conn, errors);
		
		//维修对象ID为空时则为新建
		if(CommonStringUtil.isEmpty(material_id)){
			req.setAttribute("status","create");
		}else{
			//取得维修对象进展工程
			List<LineForm> processLineList = new ArrayList<LineForm>();
			if(!CommonStringUtil.isEmpty(material_id)){
				MaterialProcessService materialProcessService = new MaterialProcessService();
				Map<String,String>  lineIdsMap = materialProcessService.getMaterialProcessLine(material_id, conn);

				if (!lineIdsMap.isEmpty()) {
					for(int i = 0;i < allInLineList.size();i++){
						//进展工程ID
						String line_id = allInLineList.get(i).getId();
						if(lineIdsMap.containsKey(line_id)){
							processLineList.add(allInLineList.get(i));
						}
					}
				} else {
					// 未投线则全显示
					processLineList = allInLineList;
				}
			}
			req.setAttribute("inlineList", processLineList);
		}
		
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = user.getPrivacies();
		if(privacies.contains(RvsConsts.PRIVACY_FACT_MATERIAL)){//现品操作
			req.setAttribute("privacy", "fact");
		}
		
		//维修对象零件BO
		String sBoflg = CodeListUtils.getSelectOptions("material_partial_bo_flg", null, "");
		req.setAttribute("sBoflg", sBoflg);
		
		//获取零件code
		PartialService partialService = new PartialService();
		List<Map<String, String>> partialCodeList = partialService.getPartialAutoCompletes(null, conn);
		JSON json = new JSON();
		req.setAttribute("partialCodeList", json.format(partialCodeList));
		
		// 迁移到页面
		actionForward = mapping.findForward("detail");

		log.info("MaterialPartialAction.detail end");
	}
}
