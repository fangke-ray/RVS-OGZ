/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：维修对象机种系统管理事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action.master;

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

import com.osh.rvs.form.master.CategoryForm;
import com.osh.rvs.service.CategoryService;
import com.osh.rvs.service.PositionService;
import com.osh.rvs.service.ProcessAssignService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;


public class CategoryAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	/**
	 * 维修对象机种系统管理处理
	 */
	private CategoryService service = new CategoryService();

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

		log.info("CategoryAction.init start");

		// 取得类别下拉框信息
		String kOptions = CodeListUtils.getSelectOptions("category_kind", null, "", false);
		req.setAttribute("kOptions", kOptions);

		ProcessAssignService paSevice = new ProcessAssignService();
		req.setAttribute("patOptions", paSevice.getOptions("(不选)", 1, conn));
		req.setAttribute("patMinorOptions", paSevice.getOptions("(不选)", 4, conn));
		req.setAttribute("patQuoteOptions", paSevice.getOptions("(不选)", 3, conn));

		String qaPositions = PositionService.getPositionOptionsBySpecialPage("qualityAssurance", conn);
		if (qaPositions != null) {
			req.setAttribute("qaOptions", qaPositions);
		}

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("CategoryAction.init end");
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

		log.info("CategoryAction.search start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);

		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行检索
			List<CategoryForm> lResultForm = service.search(form, conn, errors);
			
			// 查询结果放入Ajax响应对象
			listResponse.put("list", lResultForm);

			// 设定类别文字
			listResponse.put("kOptions", CodeListUtils.getGridOptions("category_kind"));
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("CategoryAction.search end");
	}

	/**
	 * 取得详细信息编辑用处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void showedit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("CategoryAction.detail start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ONLYKEY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 查询记录
			CategoryForm resultForm = service.getDetail(form, conn, errors);
			// 查询结果放入Ajax响应对象
			listResponse.put("categoryForm", resultForm);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("CategoryAction.detail end");
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
	@Privacies(permit={1, 0})
	public void detail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("CategoryAction.detail start");
		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ONLYKEY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 查询记录
			CategoryForm resultForm = service.getDetail(form, conn, errors);
			if (resultForm != null) {
				// 设定类别文字
				resultForm.setKind(CodeListUtils.getValue("category_kind", resultForm.getKind()));
				// 查询结果放入Ajax响应对象
				listResponse.put("categoryForm", resultForm);
			}
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("CategoryAction.detail end");
	}

	/**
	 * 设备类别数据新建登录实行处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void doinsert(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{

		log.info("CategoryAction.doinsert start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 新建记录表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行插入
			service.insert(form, req.getSession(), conn, errors);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		// 返回Json格式回馈信息
		returnJsonResponse(res, callbackResponse);

		log.info("CategoryAction.doinsert end");
	}

	/**
	 * 设备类别数据更新实行处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void doupdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("CategoryAction.doupdate start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 修改记录表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行更新
			service.update(form, req.getSession(), conn, errors);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		log.info("CategoryAction.doupdate end");
	}

	/**
	 * 设备类别数据删除实行处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void dodelete(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("CategoryAction.dodelete start");
		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 删除记录表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ONLYKEY);
		List<MsgInfo> errors = v.validate();

		if (errors.size() == 0) {
			// 执行删除
			service.delete(form, req.getSession(), conn, errors);
		}

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		log.info("CategoryAction.dodelete end");
	}
}
