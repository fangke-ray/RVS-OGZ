/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：维修对象机种系统管理事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.bean.master.SectionEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.mapper.master.LineMapper;
import com.osh.rvs.mapper.master.SectionMapper;
import com.osh.rvs.service.PositionService;
import com.osh.rvs.service.inline.PositionPanelService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.message.ApplicationMessage;


public class PanelAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());

	/**
	 * 菜单初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("PanelAction.init start");

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		log.info("PanelAction.init end");
	}

	public void dispatch(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("PanelAction.dispatch start");

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
		String roleId = user.getWorking_role_id();
		if (roleId == null) {
			roleId = user.getRole_id();
		}

		// 迁移到页面
		if (RvsConsts.ROLE_SCHEDULER.equals(roleId)) {
			actionForward = mapping.findForward("schedule");
		} else if (RvsConsts.ROLE_LINELEADER.equals(roleId)) {
			String line_id = user.getLine_id();
			if ("00000000011".equals(line_id)) { // 受理报价
				actionForward = mapping.findForward("blineleader");
			} else if ("00000000015".equals(line_id)) {
				actionForward = mapping.findForward("qualityAssurance");
			} else {
				actionForward = mapping.findForward("lineleader");
			}
		} else if (RvsConsts.ROLE_OPERATOR.equals(roleId)) {
			String positionId = user.getPosition_id();

			// 根据工位区分位置
			String specialPage = PositionService.getPositionSpecialPage(positionId, conn);
			if (specialPage != null) {
				actionForward = mapping.findForward(specialPage);
			}
			if (actionForward == null) 	{
				if (user.getSection_id() != null && "00000000001".equals(user.getSection_id())) {
					String px = user.getPx();
					if (px == null || "0".equals(px)) {
						Set<String> dividePositions = PositionService.getDividePositions(conn);
						if (dividePositions.contains(positionId)) {
							user.setPx("1");
							session.setAttribute(RvsConsts.SESSION_USER, user);
						}
					}
				}
				actionForward = mapping.findForward("position");
			}

		} else if (RvsConsts.ROLE_QAER.equals(roleId) || RvsConsts.ROLE_QA_MANAGER.equals(roleId)) {
			actionForward = mapping.findForward("qualityAssurance");
		} else if (RvsConsts.ROLE_PARTIAL_MANAGER.equals(roleId)) {
			actionForward = mapping.findForward("partialm");
		} else {
			actionForward = mapping.findForward("success");
		}

		log.info("PanelAction.dispatch end");
	}

	public void changeposition(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{
		log.info("PanelAction.changeposition start");
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		// Ajax响应对象
		Map<String, Object> callResponse = new HashMap<String, Object>();

		// 取得用户信息
		HttpSession session = req.getSession();
		LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);

		// 判断有没有进行中作业
		// 取得当前作业中作业信息
		String org_position_id = user.getPosition_id();
		String org_px = user.getPx();
		String new_position_id = req.getParameter("position_id");
		String new_px = req.getParameter("px");
		if (new_px == null) new_px = "0";

		if (new_position_id != null && new_position_id.equals(org_position_id)
				&& new_px.equals(org_px)) {

			callResponse.put("position_link", getLink(new_position_id, mapping, conn));

			// 检查发生错误时报告错误信息
			callResponse.put("errors", errors);

			// 返回Json格式响应信息
			returnJsonResponse(res, callResponse);

			log.info("PanelAction.changeposition end");

			return;
		}

		PositionPanelService service = new PositionPanelService();
		ProductionFeatureEntity workingPf = service.getProcessingPf(user, conn);
		if (workingPf != null) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setErrcode("info.linework.workingRemain");
			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.linework.workingRemain",
					(workingPf.getProcess_code() + " " +  workingPf.getPosition_name())));
			errors.add(msgInfo);
		}

		// 会话更新
		if (errors.size() == 0) {
			String new_section_id = req.getParameter("section_id");
			String new_line_id = req.getParameter("line_id");

			if (new_position_id != null || new_line_id != null) {

//				Boolean isInline = false;
//				isInline = checkInline(new_position_id, new_line_id, user.getPositions());

//				if (isInline) { // 受理
//					if (RvsConsts.ROLE_QUOTATOR.equals(org_role_id)
//						|| RvsConsts.ROLE_ACCEPTOR.equals(org_role_id)
//						|| RvsConsts.ROLE_SHIPPPER.equals(org_role_id)) {
//						user.setRole_id(RvsConsts.ROLE_OPERATOR);
//						refreshRole(user, conn);
//						callResponse.put("refresh", "1");
//					}
//				} else {
//					if (RvsConsts.ROLE_OPERATOR.equals(org_role_id)) {
//							user.setRole_id(RvsConsts.ROLE_QUOTATOR);
//							refreshRole(user, conn);
//							callResponse.put("refresh", "1");
//					}				
//				}

				
//				if (RvsConsts.POSITION_ACCEPTANCE.equals(new_position_id)) { // 受理
//					if (RvsConsts.ROLE_QUOTATOR.equals(org_role_id)
//						|| RvsConsts.ROLE_OPERATOR.equals(org_role_id)
//						|| RvsConsts.ROLE_QAER.equals(org_role_id)
//						|| RvsConsts.ROLE_SHIPPPER.equals(org_role_id)) {
//						user.setRole_id(RvsConsts.ROLE_ACCEPTOR);
//						refreshRole(user, conn);
//						listResponse.put("refresh", "1");
//					}
//				}
//
//				else if (RvsConsts.POSITION_QUOTATION_N.equals(new_position_id)
//					|| RvsConsts.POSITION_QUOTATION_D.equals(new_position_id)) { // 报价
//					if (RvsConsts.ROLE_ACCEPTOR.equals(org_role_id)
//						|| RvsConsts.ROLE_OPERATOR.equals(org_role_id)
//						|| RvsConsts.ROLE_QAER.equals(org_role_id)
//						|| RvsConsts.ROLE_SHIPPPER.equals(org_role_id)) {
//						user.setRole_id(RvsConsts.ROLE_QUOTATOR);
//						refreshRole(user, conn);
//						listResponse.put("refresh", "1");
//					}
//				}
//
//				else if (RvsConsts.POSITION_QA.equals(new_position_id)) { // 出检
//					if (RvsConsts.ROLE_QUOTATOR.equals(org_role_id)
//						|| RvsConsts.ROLE_OPERATOR.equals(org_role_id)
//						|| RvsConsts.ROLE_ACCEPTOR.equals(org_role_id)
//						|| RvsConsts.ROLE_SHIPPPER.equals(org_role_id)) {
//						user.setRole_id(RvsConsts.ROLE_QAER);
//						refreshRole(user, conn);
//						listResponse.put("refresh", "1");
//					}
//				}
//
//				else if (RvsConsts.POSITION_SHIPPING.equals(new_position_id)) { // 出货
//					if (RvsConsts.ROLE_QUOTATOR.equals(org_role_id)
//						|| RvsConsts.ROLE_OPERATOR.equals(org_role_id)
//						|| RvsConsts.ROLE_QAER.equals(org_role_id)
//						|| RvsConsts.ROLE_ACCEPTOR.equals(org_role_id)) {
//						user.setRole_id(RvsConsts.ROLE_SHIPPPER);
//						refreshRole(user, conn);
//						listResponse.put("refresh", "1");
//					}
//				}
//
//				else { // 操作界面
//					if (RvsConsts.ROLE_QUOTATOR.equals(org_role_id)
//						|| RvsConsts.ROLE_ACCEPTOR.equals(org_role_id)
//						|| RvsConsts.ROLE_QAER.equals(org_role_id)
//						|| RvsConsts.ROLE_SHIPPPER.equals(org_role_id)) {
//						user.setRole_id(RvsConsts.ROLE_OPERATOR);
//						refreshRole(user, conn);
//						listResponse.put("refresh", "1");
//					}
//				}
//
				if (new_position_id != null)
					for (PositionEntity pe : user.getPositions()) {
						String pe_id = pe.getPosition_id();
						if (new_position_id.equals(pe_id)) {
							user.setPosition_id(pe_id);
							user.setProcess_code(pe.getProcess_code());
							user.setPosition_name(pe.getName());
							user.setLine_id(pe.getLine_id());
							user.setLine_name(pe.getLine_name());
							break;
						}
					}

				callResponse.put("position_link", getLink(new_position_id, mapping, conn));
			}

			if (new_section_id != null) {
				user.setSection_id(new_section_id);
				SectionMapper sdao = conn.getMapper(SectionMapper.class);
				SectionEntity sbeam = sdao.getSectionByID(new_section_id);
				user.setSection_name(sbeam.getName());
			}

			if (new_line_id != null) {
				user.setLine_id(new_line_id);
				LineMapper ldao = conn.getMapper(LineMapper.class);
				LineEntity lbeam = ldao.getLineByID(new_line_id);
				user.setLine_name(lbeam.getName());
			}

			user.setPx(new_px);

			// 更新会话用户信息
			session.setAttribute(RvsConsts.SESSION_USER, user);

			// 维修流程提示清除
			session.removeAttribute(RvsConsts.JUST_FINISHED);
			session.removeAttribute(RvsConsts.JUST_WORKING);
		}

		// 检查发生错误时报告错误信息
		callResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, callResponse);

		log.info("PanelAction.changeposition end");
	}

	private String getLink(String new_position_id, ActionMapping mapping, SqlSession conn) {
		String specialPage = PositionService.getPositionSpecialPage(new_position_id, conn); // TODO
		String forwardStr = null;
		if (specialPage != null) {
			ActionForward forward = mapping.findForward(specialPage);
			if (forward != null)
				forwardStr = forward.getPath();
		}

		if (forwardStr != null) {
			return forwardStr.substring(1);
		}

		// 分流
		int rand = new Double(1.0d + Math.random() * 4).intValue();
		return "position_panel" + rand + ".do";
	}

}
