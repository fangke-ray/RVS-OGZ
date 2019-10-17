/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：工位平台事件<br>
 * @author 龚镭敏
 * @version 1.01
 */
package com.osh.rvs.action.inline.prototype;

import java.util.ArrayList;
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

import com.osh.rvs.action.inline.PositionPanelAction;
import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.product.ProductService;

import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.message.ApplicationMessage;

public class PositionPanelManufactAction extends PositionPanelAction {

	Logger log = Logger.getLogger(getClass());

	@Privacies(permit={1, 0})
	public void jsinit(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res, SqlSession conn)
			throws Exception {
		super.jsinit(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void doscan(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doscan(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void doendpause(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doendpause(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void dopause(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.dopause(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void dobreak(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.dobreak(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void dofinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.dofinish(mapping, form, req, res, conn);
	}

	@Privacies(permit={1, 0})
	public void jsinitf(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		super.jsinitf(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void doscanf(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doscanf(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void dofinishf(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.dofinishf(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void checkProcess(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		super.checkProcess(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void doProcess(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doProcess(mapping, form, req, res, conn);
	}

	@Privacies(permit={0})
	public void doPointOut(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doPointOut(mapping, form, req, res, conn);
	}

	@Privacies(permit={1, 0})
	public void makeReport(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		super.makeReport(mapping, form, req, res, conn);
	}

	public void refreshWaitings(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res, SqlSession conn) throws Exception {
		super.refreshWaitings(mapping, form, req, res, conn);
	}

	public void doCreateArm(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		log.info("PositionPanelAction.doCreateArm start");
		Map<String, Object> listResponse = new HashMap<String, Object>();

		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		String model_id = req.getParameter("model_id");
		String serial_no = req.getParameter("serial_no");

		MaterialService mService = new MaterialService();
		String existId = mService.checkModelSerialNo(form, conn);

		if (existId != null) {
			MsgInfo info = new MsgInfo();
			info.setErrcode("dbaccess.columnNotUnique");
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "机身号", serial_no, "ARM 制品"));
			errors.add(info);
		}

		if (errors.size() == 0) {
			ProductService productService = new ProductService();
			productService.createArm(model_id, serial_no, user, conn);
		}

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		log.info("PositionPanelAction.doCreateArm end");
	}
}
