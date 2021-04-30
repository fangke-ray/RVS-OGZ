package com.osh.rvs.action.manage;

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

import com.osh.rvs.form.manage.OperationStandardDocForm;
import com.osh.rvs.service.CategoryService;
import com.osh.rvs.service.LineService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.PositionService;
import com.osh.rvs.service.manage.OperationStandardDocService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;

/**
 * 作业基准书
 * 
 * @author liuxb
 * 
 */
public class OperationStandardDocAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());
	// 机种
	private CategoryService categoryService = new CategoryService();
	// 工程
	private LineService lineService = new LineService();
	// 型号
	private ModelService modelService = new ModelService();
	// 工位
	private PositionService positionService = new PositionService();

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
		log.info("OperationStandardDocAction.init start");

		// 机种
		String categoryOptions = categoryService.getAllOptions(conn);
		req.setAttribute("categoryOptions", categoryOptions);

		// 工程
		String lineOptions = lineService.getAllOptions(conn);
		req.setAttribute("lineOptions", lineOptions);

		// 型号
		String mReferChooser = modelService.getAllOptions(conn);
		req.setAttribute("mReferChooser", mReferChooser);

		// 工位代码
		String pReferChooser = positionService.getOptions(conn);
		req.setAttribute("pReferChooser", pReferChooser);

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("OperationStandardDocAction.init end");
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
		log.info("OperationStandardDocAction.search start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		OperationStandardDocService service = new OperationStandardDocService();
		List<OperationStandardDocForm> list = service.search(form, conn);

		listResponse.put("list", list);
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("OperationStandardDocAction.search end");
	}

	/**
	 * 删除配置
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 1, 104, 105 })
	public void doDelete(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("OperationStandardDocAction.doDelete start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			OperationStandardDocService service = new OperationStandardDocService();
			service.delete(form, conn);
		}
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("OperationStandardDocAction.doDelete end");
	}

	/**
	 * 查询所有来源型号
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void searchAllModel(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("OperationStandardDocAction.searchAllModel start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		OperationStandardDocService service = new OperationStandardDocService();
		String mReferChooser = service.getModelOptions(conn);

		listResponse.put("mReferChooser", mReferChooser);

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("OperationStandardDocAction.searchAllModel end");
	}

	/**
	 * 复制工位配置
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 1, 104, 105 })
	public void doCopy(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("OperationStandardDocAction.doCopy start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("model_id", v.required("配置型号"));
		v.add("copy_model_id", v.required("来源型号"));
		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			OperationStandardDocService service = new OperationStandardDocService();
			service.copy(form, conn, errors);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("OperationStandardDocAction.doCopy end");
	}

	/**
	 * 明细
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void getDetail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("OperationStandardDocAction.getDetail start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		v.add("model_id", v.required("型号"));
		v.add("position_id", v.required("工位"));

		List<MsgInfo> errors = v != null ? v.validate() : new ArrayList<MsgInfo>();

		if (errors.size() == 0) {
			OperationStandardDocService service = new OperationStandardDocService();
			List<OperationStandardDocForm> list = service.searchDetail(form, conn);

			listResponse.put("list", list);
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("OperationStandardDocAction.getDetail end");
	}

	@Privacies(permit = { 1, 104, 105 })
	public void doUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		log.info("OperationStandardDocAction.doUpdate start");

		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		List<OperationStandardDocForm> list = new AutofillArrayList<OperationStandardDocForm>(OperationStandardDocForm.class);
		Map<String, String[]> parameters = req.getParameterMap();

		// 整理提交数据
		for (String parameterKey : parameters.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("operation_standard_doc".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameters.get(parameterKey);
					if ("model_id".equals(column)) {
						list.get(icounts).setModel_id(value[0]);
					} else if ("position_id".equals(column)) {
						list.get(icounts).setPosition_id(value[0]);
					} else if ("doc_seq".equals(column)) {
						list.get(icounts).setDoc_seq(value[0]);
					} else if ("doc_url".equals(column)) {
						list.get(icounts).setDoc_url(value[0]);
					} else if ("page_no".equals(column)) {
						list.get(icounts).setPage_no(value[0]);
					}
				}
			}
		}

		if (list.size() == 0) {
			MsgInfo error = new MsgInfo();
			error.setErrmsg("请至少创建一条作业基准书明细。");
			errors.add(error);
		} else {
			String urlRegex = "(http(s)?:(\\d{2,4})?//)?([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
			p = Pattern.compile(urlRegex);

			// 验证
			for (int i = 0; i < list.size(); i++) {
				Validators v = BeanUtil.createBeanValidators(list.get(i), BeanUtil.CHECK_TYPE_ALL);
				List<MsgInfo> errs = v.validate();
				for (int j = 0; j < errs.size(); j++) {
					errs.get(j).setLineno("第" + (i + 1) + "行");
				}
				errors.addAll(errs);

				// 文档 URL
				String docUrl = list.get(i).getDoc_url();
				if (!CommonStringUtil.isEmpty(docUrl)) {
					// 验证URL
					Matcher m = p.matcher(docUrl);
					if (!m.matches()) {
						MsgInfo error = new MsgInfo();
						error.setLineno("第" + (i + 1) + "行");
						error.setErrmsg("请为文档 URL输入一个合法的值。");
						errors.add(error);
					}
				}
			}

			if (errors.size() == 0) {
				OperationStandardDocService service = new OperationStandardDocService();
				service.delete(form, conn);

				for (int i = 0; i < list.size(); i++) {
					service.insert(list.get(i), conn);
				}
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("OperationStandardDocAction.doUpdate end");
	}

}
