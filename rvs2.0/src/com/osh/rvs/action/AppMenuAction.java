/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：维修对象机种系统管理事件<br>
 * @author 龚镭敏
 * @version 0.01
 */
package com.osh.rvs.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.common.RvsConsts;

import framework.huiqing.action.BaseAction;

public class AppMenuAction extends BaseAction {

	private Logger log = Logger.getLogger(getClass());
	private static final String LINE_ACCEPT_QUOTATE = "00000000011"; // 受理报价工程
	private static final String LINE_DECOM = "00000000012"; // 分解工程
	private static final String LINE_NS = "00000000013"; // NS工程
	private static final String LINE_COM = "00000000014"; // 总组工程

	private static final String LINE_QA = "00000000015"; // 品保工程
	private static final String LINE_SURGI= "00000000050"; // 外科硬镜修理工程 Rigid Flexible

	private static final String LINE_FEB_DECOM = "00000000060"; // 纤维镜分解工程
	private static final String LINE_FEB_COM = "00000000061"; // 纤维镜总组工程
	private static final String LINE_PERI = "00000000070"; // 周边设备修理
	private static final String LINE_LIGHTMED = "00000000054"; // 中小修

	/**
	 * 菜单初始表示处理
	 *
	 * @param mapping
	 *            ActionMapping
	 * @param form
	 *            表单
	 * @param req
	 *            页面请求
	 * @param res
	 *            页面响应
	 * @param conn
	 *            数据库会话
	 * @throws Exception
	 */
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("AppMenuAction.init start" + req.getParameter("ex"));

		// 取得登录用户权限
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

		String forward = FW_INIT;
		if (user.getDepartment() != null && user.getDepartment() == RvsConsts.DEPART_MANUFACT) {
			forward = "manufact";
		}
		if (req.getParameter("ex") != null) {
			// 迁移到页面
			forward += "-ex";
		}
		actionForward = mapping.findForward(forward);

		List<Integer> privacies = user.getPrivacies();
		// String now_position_id = user.getPosition_id();
		@SuppressWarnings("unused")
		String process_code = user.getProcess_code();
		List<PositionEntity> userPositions = user.getPositions();
		String section_id = user.getSection_id();
		String px = user.getPx();

		Map<String, Boolean> menuLinks = new HashMap<String, Boolean>();

		menuLinks.put("受理报价", false);

		// 受理报价全工位
		menuLinks.put("acceptance", false);
		String links = getLinksByPositions(userPositions, LINE_ACCEPT_QUOTATE, section_id, px);
		if (links.length() > 0) {
			menuLinks.put("acceptance", true);
			req.setAttribute("beforePosition", links);
			menuLinks.put("受理报价", true);
		}

		menuLinks.put("beforeline", false);
		// 受理线长
		if (LINE_ACCEPT_QUOTATE.equals(user.getLine_id()) || user.getPrivacies().contains(RvsConsts.PRIVACY_ADMIN)) {
			menuLinks.put("beforeline", true);
			menuLinks.put("受理报价", true);
		}

		//////////////////////////////////////////////////////////////////////////////////

		// 现品管理
		menuLinks.put("现品管理", false);

		// WIP管理
		if (privacies.contains(RvsConsts.PRIVACY_WIP)) {
			menuLinks.put("wip", true);
			menuLinks.put("现品管理", true);
		} else {
			menuLinks.put("wip", false);
		}

		// 现品投线
		if (privacies.contains(RvsConsts.PRIVACY_WIP)
				|| privacies.contains(RvsConsts.PRIVACY_FACT_MATERIAL))
		{
			menuLinks.put("fact_material", true);
			menuLinks.put("现品管理", true);
		} else {
			menuLinks.put("fact_material", false);
		}

		// 零件BO管理
		if (privacies.contains(RvsConsts.PRIVACY_FACT_MATERIAL)) {
			menuLinks.put("bo_partial", true);
			menuLinks.put("现品管理", true);
		} else {
			menuLinks.put("bo_partial", false);
		}

		/////////////////////////////////////////////////////////////////////////////////

		// 计划管理
		menuLinks.put("计划管理", false);

		// 计划管理
		if (privacies.contains(RvsConsts.PRIVACY_SCHEDULE)
				|| privacies.contains(RvsConsts.PRIVACY_PROCESSING)
				|| privacies.contains(RvsConsts.PRIVACY_SCHEDULE_VIEW)) {
			menuLinks.put("schedule", true);
			menuLinks.put("计划管理", true);
		} else {
			menuLinks.put("schedule", false);
		}

		// 进度管理
		if (privacies.contains(RvsConsts.PRIVACY_SCHEDULE)
				|| privacies.contains(RvsConsts.PRIVACY_PROCESSING)
				|| privacies.contains(RvsConsts.PRIVACY_SCHEDULE_VIEW)) {
			menuLinks.put("schedule_processing", true);
			menuLinks.put("计划管理", true);
		} else {
			menuLinks.put("schedule_processing", false);
		}

		/////////////////////////////////////////////////////////////////////////////////

		// 在线作业
		menuLinks.put("在线作业", false);

		menuLinks.put("inlinePosition", false);
		menuLinks.put("decomposeline", false);
		menuLinks.put("deposeStorage", false);
		boolean bRepairLine = false;

		String inlinePosition = "";

		// 分解
		if (LINE_DECOM.equals(user.getLine_id())) {
			bRepairLine = true;
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("decomposeline", true);
				menuLinks.put("在线作业", true);
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, LINE_DECOM, section_id, px);
				inlinePosition += links;
			}
		}

		menuLinks.put("nsline", false);

		// ＮＳ
		if (LINE_NS.equals(user.getLine_id())) {
			bRepairLine = true;
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("nsline", true);
				menuLinks.put("在线作业", true);
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, LINE_NS, section_id, px);
				inlinePosition += links;
			}
		}

		menuLinks.put("composeline", false);
		menuLinks.put("composeStorage", false);

		// 总组
		if (LINE_COM.equals(user.getLine_id())) {
			bRepairLine = true;
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("composeline", true);
				menuLinks.put("在线作业", true);
				// 总组库位
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, LINE_COM, section_id, px);
				inlinePosition += links;
			}
			if ("00000000001".equals(section_id)) {
				menuLinks.put("composeStorage", true);
			}
		}

		menuLinks.put("spline", false);
		// 外科硬镜修理工程
		if (LINE_SURGI.equals(user.getLine_id())) {
			bRepairLine = true;
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("spline", true);
				menuLinks.put("在线作业", true);
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, LINE_SURGI, section_id, px);
				inlinePosition += links;
			}
		}

		menuLinks.put("lmline", false);
		// 中小修修理工程
		if (LINE_LIGHTMED.equals(user.getLine_id())) {
			bRepairLine = true;
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("lmline", true);
				menuLinks.put("在线作业", true);
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, LINE_LIGHTMED, section_id, px);
				inlinePosition += links;
			}
		}

		menuLinks.put("febdecomline", false);
		// 纤维镜分解工程
		if (LINE_FEB_DECOM.equals(user.getLine_id())) {
			bRepairLine = true;
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("febdecomline", true);
				menuLinks.put("在线作业", true);
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, LINE_FEB_DECOM, section_id, px);
				inlinePosition += links;
			}
		}

		menuLinks.put("febcomline", false);
		// 纤维镜总组工程
		if (LINE_FEB_COM.equals(user.getLine_id())) {
			bRepairLine = true;
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("febcomline", true);
				menuLinks.put("在线作业", true);
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, LINE_FEB_COM, section_id, px);
				inlinePosition += links;
			}
		}

		menuLinks.put("periline", false);
		// 周边设备修理工程
		if (LINE_PERI.equals(user.getLine_id())) {
			bRepairLine = true;
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("periline", true);
				menuLinks.put("在线作业", true);
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, LINE_PERI, section_id, px);
				inlinePosition += links;
			}
		}

		// 生产工程
		menuLinks.put("manufactline", false);
		if (!bRepairLine && user.getLine_name() != null 
				&& (user.getLine_name().indexOf("组装") == 0
				|| user.getLine_name().indexOf("检查") == 0)) {
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("manufactline", true);
				menuLinks.put("在线作业", true);
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, user.getLine_id(), section_id, px);
				inlinePosition += links;
			}
		}
		if (!bRepairLine && user.getLine_name() != null 
				&& (user.getLine_name().equals("最终检查")
				|| user.getLine_name().indexOf("包装") == 0)) {
			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("在线作业", true);
			}
			if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
				links = getLinksByPositions(userPositions, user.getLine_id(), section_id, px);
				inlinePosition += links;
			}
		}

		// 辅助工作
		if (bRepairLine && privacies.contains(RvsConsts.PRIVACY_POSITION)) {
			menuLinks.put("support", true);
			menuLinks.put("在线作业", true);
		} else {
			menuLinks.put("support", false);
		}

		if (inlinePosition.length() > 0) {
			req.setAttribute("inlinePosition", inlinePosition);
			menuLinks.put("inlinePosition", true);
			menuLinks.put("在线作业", true);
		}

		///////////////////////////////////////////////////////////////////

		// 品保
		menuLinks.put("品保作业", false);
		menuLinks.put("qa_view", false);
		menuLinks.put("qa_work", false);
		menuLinks.put("qa_manage", false);

		if (LINE_QA.equals(user.getLine_id())) {
			if (privacies.contains(RvsConsts.PRIVACY_POSITION_VIEW)) {
				menuLinks.put("qa_view", true);
				menuLinks.put("品保作业", true);
			}

			if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
				menuLinks.put("qa_manage", true);
				menuLinks.put("品保作业", true);
			}
		}

		links = getLinksByPositions(userPositions, LINE_QA, section_id, px);
		if (links.length() > 0) {
//			links = links.replaceAll("javascript:getPositionWork\\('00000000046'\\);", "qualityAssurance.do")
//					.replaceAll("javascript:getPositionWork\\('00000000051'\\);", "service_repair_referee.do");
			req.setAttribute("qaPosition", links);
			menuLinks.put("qa_work", true);
			menuLinks.put("品保作业", true);
		}

		///////////////////////////////////////////////////////////////////

		// 文档管理
		menuLinks.put("文档管理", false);

		// 归档
		if (privacies.contains(RvsConsts.PRIVACY_FILING)
				|| privacies.contains(RvsConsts.PRIVACY_READFILE)) {
			menuLinks.put("filing", true);
			menuLinks.put("文档管理", true);
		} else {
			menuLinks.put("filing", false);
		}

		///////////////////////////////////////////////////////////////////

		// 进度查询
		if (privacies.contains(RvsConsts.PRIVACY_INFO_EDIT) || privacies.contains(RvsConsts.PRIVACY_INFO_VIEW)) {
			menuLinks.put("info", true);
		} else {
			menuLinks.put("info", false);
		}

		///////////////////////////////////////////////////////////////////

		// 展示
		if (privacies.contains(RvsConsts.PRIVACY_VIEW)) {
			menuLinks.put("viewer", true);
		} else {
			menuLinks.put("viewer", false);
		}

		///////////////////////////////////////////////////////////////////

		// 系统信息管理
		if (privacies.contains(RvsConsts.PRIVACY_SA) || privacies.contains(RvsConsts.PRIVACY_ADMIN)) {
			menuLinks.put("admin", true);
		} else {
			menuLinks.put("admin", false);
		}

		// 可用链接设定到画面
		req.setAttribute("menuLinks", menuLinks);
		req.setAttribute("linkto", req.getParameter("linkto"));

		log.info("AppMenuAction.init end");
	}

	/**
	 * 取得工位链接
	 * @param positions
	 * @param line_id
	 * @return
	 */
	private String getLinksByPositions(List<PositionEntity> positions, String line_id, String section_id, String px) {
		StringBuffer ret = new StringBuffer("");
		for (PositionEntity position : positions) {
			if (line_id.equals(position.getLine_id())) {
				if (position.getLight_division_flg() != null
						&& position.getLight_division_flg() == 1) {
					if ("2".equals(px)) {
						ret.append("<a href=\"javascript:getPositionWork('" 
								+ position.getPosition_id() + "', 2);\">" +
								position.getProcess_code() + " " + position.getName() + 
								"</a><br><px>");
						ret.append("<a href=\"javascript:getPositionWork('" 
								+ position.getPosition_id() + "', 1);\">" +
								" A线</a>");
						ret.append("<a class=\"px_on\" href=\"javascript:getPositionWork('" 
								+ position.getPosition_id() + "', 2);\">" + 
								" B线</a></px><br>");
					} else if ("1".equals(px)) { 
						ret.append("<a href=\"javascript:getPositionWork('" 
								+ position.getPosition_id() + "', 1);\">" +
								position.getProcess_code() + " " + position.getName() + 
								"</a><br><px>");
						ret.append("<a class=\"px_on\" href=\"javascript:getPositionWork('" 
								+ position.getPosition_id() + "', 1);\">" +
								" A线</a>");
						ret.append("<a href=\"javascript:getPositionWork('" 
								+ position.getPosition_id() + "', 2);\">" + 
								" B线</a></px><br>");
					} else {
						ret.append("<a href=\"javascript:getPositionWork('"
								+ position.getPosition_id() + "');\">" +
								position.getProcess_code() + " " + position.getName() +
								"</a><br>");
					}					
				} else {
					ret.append("<a href=\"javascript:getPositionWork('"
							+ position.getPosition_id() + "');\">" +
							position.getProcess_code() + " " + position.getName() +
							"</a><br>");
				}
			}
		}
		return ret.toString();
	}

	/**
	 * 零件菜单初始表示处理
	 *
	 * @param mapping
	 *            ActionMapping
	 * @param form
	 *            表单
	 * @param req
	 *            页面请求
	 * @param res
	 *            页面响应
	 * @param conn
	 *            数据库会话
	 * @throws Exception
	 */
	public void pinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("AppMenuAction.pinit start");

		if (req.getParameter("ex") != null) {
			// 迁移到页面
			actionForward = mapping.findForward("partial-ex");
		} else {
			// 迁移到页面
			actionForward = mapping.findForward("partial");
		}

		// 取得登录用户权限
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = user.getPrivacies();

		Map<String, Boolean> menuLinks = new HashMap<String, Boolean>();

		// 零件管理
		if (privacies.contains(RvsConsts.PRIVACY_PARTIAL_MANAGER)) {
			menuLinks.put("partial_admin", true);
		} else {
			menuLinks.put("partial_admin", false);
		}

		// 现品 (管理/订购)
		if (privacies.contains(RvsConsts.PRIVACY_PARTIAL_ORDER)) {
			menuLinks.put("fact", true);
		} else {
			menuLinks.put("fact", false);
		}

		// 可用链接设定到画面
		req.setAttribute("menuLinks", menuLinks);
		req.setAttribute("linkto", req.getParameter("linkto"));

		log.info("AppMenuAction.pinit end");
	}

	/**
	 * 设备工具+治具初始表示处理
	 *
	 * @param mapping
	 *            ActionMapping
	 * @param form
	 *            表单
	 * @param req
	 *            页面请求
	 * @param res
	 *            页面响应
	 * @param conn
	 *            数据库会话
	 * @throws Exception
	 */
	public void tinit(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {

		log.info("AppMenuAction.tinit start");

		if (req.getParameter("ex") != null) {
			// 迁移到页面
			actionForward = mapping.findForward("tools-ex");
		} else {
			// 迁移到页面
			actionForward = mapping.findForward("tools");
		}

		Map<String, Boolean> menuLinks = new HashMap<String, Boolean>();

		// 取得登录用户权限
		LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies = user.getPrivacies();

		if (privacies.contains(RvsConsts.PRIVACY_TECHNOLOGY)) {
			menuLinks.put("dt_admin", true);
		} else {
			menuLinks.put("dt_admin", false);
		}

		// 可用链接设定到画面
		req.setAttribute("menuLinks", menuLinks);
		req.setAttribute("linkto", req.getParameter("linkto"));

		log.info("AppMenuAction.tinit end");
	}

	public void pdaMenu(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {
		log.info("AppMenuAction.pdaMenu start");

		String flg = req.getParameter("flg");
		if (flg == null) {
			req.setAttribute("isFact", false);
			req.setAttribute("isRecept", false);
			// 得到会话用户信息
			HttpSession session = req.getSession();
			LoginData loginData = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
			List<Integer> privacies = loginData.getPrivacies();
			if (privacies != null) {
				if (privacies.contains(RvsConsts.PRIVACY_FACT_MATERIAL)) {
					req.setAttribute("isFact", true);
				}
				for (LineEntity lines : loginData.getLines()) {
					if ("00000000011".equals(lines.getLine_id())) {
						if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
							req.setAttribute("isRecept", true);
						}
						break;
					}
				}
			}
		} else if ("cs".equals(flg)) {
			req.setAttribute("isFact", true);
			req.setAttribute("isRecept", false);
		} else if ("tc".equals(flg)) {
			req.setAttribute("isFact", false);
			req.setAttribute("isRecept", true);
		}

		actionForward = mapping.findForward(FW_PDA_MENU);

		log.info("AppMenuAction.pdaMenu end");
	}

}
