package com.osh.rvs.action.manage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.service.LineService;
import com.osh.rvs.service.ModelService;
import com.osh.rvs.service.OperatorService;
import com.osh.rvs.service.manage.ProcessInspectService;

import framework.huiqing.action.BaseAction;

public class ProcessInspectAction extends BaseAction {

	private Logger _log = Logger.getLogger(getClass());

	private ProcessInspectService service = new ProcessInspectService();

	/**
	 * 作业监察 页面初始化
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		_log.info("ProcessInspectAction.init start");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		if (user.getPrivacies().contains(RvsConsts.PRIVACY_LINE)) {
			req.setAttribute("enableEdit", true);
		}

		LineService lineService = new LineService();
		// 工程信息取得
		String lOptions = lineService.getOptions(RvsConsts.DEPART_REPAIR, conn);
		// 工程信息设定
		req.setAttribute("lOptions", lOptions);

		ModelService modelService = new ModelService();
		req.setAttribute("mReferChooser", modelService.getOptions(user.getDepartment(), conn));

		OperatorService oService = new OperatorService();
		if (user.getDepartment() != null) {
			req.setAttribute("oReferChooser", oService.getAllOperatorName(user.getDepartment(), conn));
		} else {
			req.setAttribute("oReferChooser", oService.getAllOperatorName(conn));
		}

		req.setAttribute("iReferChooser", service.getInspectors(user.getDepartment(), conn));

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		_log.info("ProcessInspectAction.init end");
	}

}
