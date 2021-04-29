/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：工位平台事件<br>
 * @author 龚镭敏
 * @version 1.01
 */
package com.osh.rvs.action.inline.prototype;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.action.inline.PositionPanelAction;

import framework.huiqing.action.Privacies;

public class PositionPanel2Action extends PositionPanelAction {

	Logger log = Logger.getLogger(getClass());

	public void jsinit(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res, SqlSession conn)
			throws Exception {
		super.jsinit(mapping, form, req, res, conn);
	}

	public void doscan(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doscan(mapping, form, req, res, conn);
	}

	public void doendpause(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doendpause(mapping, form, req, res, conn);
	}

	public void dopause(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.dopause(mapping, form, req, res, conn);
	}

	public void dobreak(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.dobreak(mapping, form, req, res, conn);
	}

	public void dofinish(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.dofinish(mapping, form, req, res, conn);
	}

	public void jsinitf(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		super.jsinitf(mapping, form, req, res, conn);
	}

	public void doscanf(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doscanf(mapping, form, req, res, conn);
	}

	public void dofinishf(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.dofinishf(mapping, form, req, res, conn);
	}

	public void checkProcess(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		super.checkProcess(mapping, form, req, res, conn);
	}

	public void doProcess(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doProcess(mapping, form, req, res, conn);
	}

	public void doPointOut(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception{
		super.doPointOut(mapping, form, req, res, conn);
	}

	public void makeReport(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		super.makeReport(mapping, form, req, res, conn);
	}

	public void refreshWaitings(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res, SqlSession conn) throws Exception {
		super.refreshWaitings(mapping, form, req, res, conn);
	}
}
