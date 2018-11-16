package com.osh.rvs.action.report;

import java.util.ArrayList;
import java.util.Calendar;
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
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.mapper.qa.QualityAssuranceMapper;
import com.osh.rvs.service.CategoryService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.SectionService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.DateUtil;

/**
 * 归档
 * 
 * @author Gong
 *
 */
public class FilingAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	/**
	 * 初始化
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	@Privacies(permit = { 1, 0 })
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("FilingAction.init start");

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		CategoryService categoryService = new CategoryService();
		String cOptions = categoryService.getOptions(conn);
		req.setAttribute("cOptions", cOptions);

		// level取得
		req.setAttribute("lOptions",CodeListUtils.getSelectOptions("material_level_inline", null, "", false));
		req.setAttribute("sLevel", CodeListUtils.getGridOptions("material_level"));

		ModelService modelService = new ModelService();
		String mReferChooser = modelService.getOptions(conn);
		req.setAttribute("mReferChooser", mReferChooser);
		
		SectionService sectionService = new SectionService();
		String sOptions = sectionService.getOptions(conn, "(全部)");
		req.setAttribute("sOptions", sOptions);

		// 一周前
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		req.setAttribute("scheduled_date_start", DateUtil.toString(cal.getTime(), DateUtil.DATE_PATTERN));

		// 取得登录用户权限
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = user.getPrivacies();

		if (privacies.contains(RvsConsts.PRIVACY_ADMIN)) {
			req.setAttribute("editor", "true");
		} else if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
			req.setAttribute("editor", "line");
		} else {
			req.setAttribute("editor", "false");
		}

		log.info("FilingAction.init end");
	}

	/**
	 * 查询
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("FilingAction.search start");
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> msgInfos = new ArrayList<MsgInfo>();

		MaterialService service = new MaterialService();
		List<MaterialForm> finishedForms = service.searchMaterialFiling(form, "2", conn, msgInfos);
		listResponse.put("finished", finishedForms);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", new ArrayList<MsgInfo>());

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("FilingAction.search end");
	}

}
