/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：质量提示事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action.master;

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
import com.osh.rvs.form.master.QualityTipForm;
import com.osh.rvs.service.CategoryService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.PositionService;
import com.osh.rvs.service.QualityTipService;
import com.osh.rvs.service.UploadService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

public class QualityTipAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	/**
	 * 质量提示处理
	 */
	private QualityTipService service = new QualityTipService();

	/**
	 * 设备类别管理画面初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("QualityTipAction.init start");

		// 取得维修对象机种
		CategoryService categoryService = new CategoryService();
		req.setAttribute("cOptions", categoryService.getOptions(conn));
		req.setAttribute("cReferChooser", categoryService.getReferChooser(conn));

		// 取得维修对象型号
		ModelService modelService = new ModelService();
		String mReferChooser = modelService.getOptions(conn);
		req.setAttribute("mReferChooser", mReferChooser);

		// 取得工位名称
		PositionService positionService = new PositionService();
		String pReferChooser = positionService.getOptions(conn);
		req.setAttribute("pReferChooser", pReferChooser);

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("QualityTipAction.init end");
	}

	/**
	 * 设备类别查询一览处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("QualityTipAction.search start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行检索
			List<QualityTipForm> lResultForm = service.search(form, conn, errors);
			
			// 查询结果放入Ajax响应对象
			listResponse.put("list", lResultForm);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("QualityTipAction.search end");
	}

	/**
	 * 设备型号数据新建登录实行处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void doinsert(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("QualityTipAction.doinsert start");

		QualityTipForm qform = (QualityTipForm)form;
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();
	
		// 新建记录表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v.validate();

		// 维修对象机种和维修对象型号必须选一个
		if (qform.getCategorys().size() == 0 && qform.getModels().size() == 0) {
			MsgInfo info = new MsgInfo();
			info.setErrcode("validator.required.multidetail");
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.multidetail", "维修对象机种和维修对象型号"));
			errors.add(info);
		} else if (CommonStringUtil.isEmpty(qform.getPhoto_file_name())) {
			MsgInfo info = new MsgInfo();
			info.setErrcode("");
			info.setErrmsg("图片信息不存在，请上传。");
			errors.add(info);
		}

		if (errors.size() == 0) {
			// 执行插入
			service.insert(qform, user, conn, errors);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("QualityTipAction.doinsert end");
	}

	/**
	 * 取得详细信息处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={2, 0})
	public void detail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("QualityTipAction.detail start");

		QualityTipForm qform = (QualityTipForm)form;

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(qform, BeanUtil.CHECK_TYPE_ONLYKEY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 查询记录
			QualityTipForm detailForm = service.getDetail(qform, conn, errors);

			// 查询结果放入Ajax响应对象
			listResponse.put("detailForm", detailForm);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("QualityTipAction.detail end");

	}

	/**
	 * 设备型号数据更新实行处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void doupdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("QualityTipAction.doupdate start");

		QualityTipForm qform = (QualityTipForm)form;
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		Validators v = BeanUtil.createBeanValidators(qform, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行更新
			service.update(qform, user, conn, errors);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		log.info("QualityTipAction.doupdate end");
	}

	/**
	 * 设备型号数据删除实行处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void dodelete(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("QualityTipAction.dodelete start");

		QualityTipForm qform = (QualityTipForm)form;

		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 删除记录表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ONLYKEY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行删除
			service.delete(qform, conn, errors);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		log.info("QualityTipAction.dodelete end");
	}

	/**
	 * 上传源图片(单个文件上传-file)
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void sourceImage(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res, SqlSession conn) throws Exception {
		log.info("QualityTipAction.sourceImage start");

		// Ajax回馈对象
		Map<String, Object> jsonResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		UploadService fservice = new UploadService();
		String tempFilePath = fservice.getFile2Local(form, errors);

		String photo_file_name = tempFilePath.substring(tempFilePath.lastIndexOf("\\") + 1);
		jsonResponse.put("photo_file_name", photo_file_name);
		// 检查发生错误时报告错误信息
		jsonResponse.put("errors", errors);
		// 返回Json格式响应信息
		returnJsonResponse(res, jsonResponse);

		log.info("QualityTipAction.sourceImage end");
	}
}
