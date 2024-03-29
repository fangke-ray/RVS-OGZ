package com.osh.rvs.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.inline.MaterialProcessEntity;
import com.osh.rvs.bean.inline.SoloProductionFeatureEntity;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.OperatorEntity;
import com.osh.rvs.bean.master.OperatorNamedEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.bean.master.SectionEntity;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.RedirectRes;
import com.osh.rvs.form.master.OperatorForm;
import com.osh.rvs.mapper.inline.MaterialProcessMapper;
import com.osh.rvs.mapper.master.OperatorMapper;
import com.osh.rvs.mapper.master.SectionMapper;
import com.osh.rvs.service.OperatorService;
import com.osh.rvs.service.PositionService;
import com.osh.rvs.service.RoleService;
import com.osh.rvs.service.SectionService;
import com.osh.rvs.service.inline.PositionPanelService;
import com.osh.rvs.service.inline.SoloSnoutService;
import com.osh.rvs.service.master.ProcedureStepCountService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CryptTool;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

public class LoginAction extends BaseAction {
	protected static final Logger _logger = Logger.getLogger("ACCOUNT");

	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		_logger.info("LoginAction.init start");

		String addr = req.getRemoteAddr();
		String from = req.getParameter("version");

		// 本机访问时，显示版本号
		if (from != null) {
			req.setAttribute("version", RvsConsts.VERSION + "<BR>" + "From Addr:" + addr);
		}

		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("dpt")) {
					req.setAttribute("department", cookie.getValue());
					break;
				}
			}
		}

		if (req.getSession().getAttribute("break_cb") != null) {
			req.getSession().removeAttribute("break_cb");
		}

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		_logger.info("LoginAction.init end");
	}

	public void pda(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {
		_logger.info("LoginAction.pda start");

		// 迁移到pda登陆页面
		actionForward = mapping.findForward("pda_init");

		_logger.info("LoginAction.pda end");
	}

	public void login(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		_logger.fine("LoginAction.login start");

		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		v.delete("role_id", "name");
		v.add("pwd", v.required("密码"));

		List<MsgInfo> errors = v.validate();

		// 检查不通过的情况下
		if (!errors.isEmpty()) {
			// 检查发生错误时报告错误信息
			callbackResponse.put("errors", errors);

			// 返回Json格式响应信息
			returnJsonResponse(res, callbackResponse);
			return;
		}

		// 建立会话用户信息
		Map<String, String> roles = makeSession((OperatorForm)form, req, res, errors, conn);

		// 检查发生错误时报告错误信息
		callbackResponse.put("errors", errors);
		callbackResponse.put("roles", roles);

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		_logger.fine("run end");
	}

	public void logoff(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		_logger.fine("LoginAction.logoff start");
		req.getSession().removeAttribute(RvsConsts.SESSION_USER);
		req.getSession().removeAttribute(RvsConsts.DONT_CARE);
		_logger.fine("run end");

		Cookie[] cookies = req.getCookies();
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("dpt")) {
				req.setAttribute("department", cookie.getValue());
				break;
			}
		}

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);
	}

	public void sessionTimeout(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		_logger.info("LoginAction.sessionTimeout start");

		// 写跳转
		PrintWriter out;
		try {
			res.setCharacterEncoding("UTF-8");
			out = res.getWriter();
			out.print("<script type=\"text/javascript\">window.top.location.href='login.do'</script>");
			out.flush();
		} catch (IOException e) {
			// 迁移到页面
			actionForward = mapping.findForward(FW_INIT);
		}

		_logger.info("LoginAction.sessionTimeout end");
	}

	public void selectrole(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception {
		_logger.fine("LoginAction.selectrole start");

		String role_id = req.getParameter("role_id");
		String role_name = req.getParameter("role_name");

		setRoleIntoSession(role_id, role_name, req.getSession(), conn);
		RedirectRes rres = new RedirectRes();
		// 返回Json格式响应信息
		returnJsonResponse(res, rres);

		_logger.fine("run end");
	}

	private void setRoleIntoSession(String role_id, String role_name, HttpSession session, SqlSession conn) {
		LoginData loginData = (LoginData) session.getAttribute("K" + RvsConsts.SESSION_USER);
		// TODO check role
		if (!"main".equals(role_id)) {
			loginData.setRole_id(role_id);
			loginData.setRole_name(role_name);
		}
		ProductionFeatureEntity workingPf = getWoringPf(loginData, conn);
		// 选择的角色
		setDetail(loginData, conn, workingPf);
		// 用户信息保存在会话中
		session.setAttribute(RvsConsts.SESSION_USER, loginData);
		// <Context path="/rvs" debug="0" reloadable="false" crossContext="true"/>
		session.removeAttribute("K" + RvsConsts.SESSION_USER);
		ServletContext contextMain =session.getServletContext();
		contextMain.setAttribute("login_name", loginData.getName());
		// contextMe.getContext("");
		// ServletContext contextMain= contextMe.getContext("/rvs");

		// 系统管理员的超时只有30分钟
		if (RvsConsts.ROLE_SYSTEM.equals(loginData.getRole_id())) {
			session.setMaxInactiveInterval(1800);
		}
	}

	private ProductionFeatureEntity getWoringPf(LoginData loginData, SqlSession conn) {
		PositionPanelService service = new PositionPanelService();
		ProductionFeatureEntity workingPf = service.getProcessingPf(loginData, conn);
		return workingPf;
	}

	private void setDetail(LoginData loginData, SqlSession conn, ProductionFeatureEntity workingPf) {
		loginData.setPrivacies(getPrivacies(loginData.getRole_id(), conn));
		setPositions(loginData, conn);

		// 判断有没有进行中作业
		// 取得当前作业中作业信息
		if (workingPf != null) {
			// 有的话切到进行中作业
			String now_position = workingPf.getPosition_id();

			// 平行分线
			if (workingPf.getSection_id() != null && "00000000001".equals(workingPf.getSection_id())) {
				Set<String> dividePositions = PositionService.getDividePositions(conn);
				if (dividePositions.contains(now_position)) {
					MaterialProcessMapper mapper = conn.getMapper(MaterialProcessMapper.class);
					MaterialProcessEntity mpEntity = mapper.loadMaterialProcessOfLine(workingPf.getMaterial_id(), workingPf.getLine_id());
					if (mpEntity == null || mpEntity.getPx() == null) {
					} else if (mpEntity.getPx() == 1) {
						loginData.setPx("2");
					} else {
						loginData.setPx("1");
					}
//					MaterialService mservice = new MaterialService();
//					MaterialEntity mEntity = mservice.loadMaterialDetailBean(conn, workingPf.getMaterial_id());
//					if (mEntity.getQuotation_first() == 1) {
//						loginData.setPx("2");
//					} else {
//						loginData.setPx("1");
//					}
				}
			}

			loginData.setPosition_id(now_position);
			loginData.setPosition_name(workingPf.getPosition_name());
			loginData.setProcess_code(workingPf.getProcess_code());
			loginData.setLine_id(workingPf.getLine_id());
			loginData.setLine_name(workingPf.getLine_name());
			if (workingPf.getSection_id() != null && !"00000000000".equals(workingPf.getSection_id())
					&& !workingPf.getSection_id().equals(loginData.getSection_id())) {
				loginData.setSection_id(workingPf.getSection_id());
				loginData.setSection_name(workingPf.getSection_name());
			}
		} else if (loginData.getLine_id() != null && loginData.getLine_id().equals("00000000202")) {
			SoloSnoutService service = new SoloSnoutService();
			SoloProductionFeatureEntity sworkingPf = service.checkWorkingPfServiceRepair(loginData.getOperator_id(), null, conn);
			if (sworkingPf != null) {
				// 有的话切到进行中作业
				String now_position = sworkingPf.getPosition_id();

				// 平行分线
				if (sworkingPf.getSection_id() != null && "00000000001".equals(sworkingPf.getSection_id())) {
					Set<String> dividePositions = PositionService.getDividePositions(conn);
					if (dividePositions.contains(now_position)) {
						loginData.setPx("2"); // 固定B线
					} else {
						loginData.setPx("0");
					}
				}

				loginData.setPosition_id(now_position);
				PositionService pService = new PositionService();
				PositionEntity pEntity = pService.getPositionEntityByKey(now_position, conn);
				loginData.setPosition_name(pEntity.getName());
				loginData.setProcess_code(pEntity.getProcess_code());
				loginData.setLine_id(pEntity.getLine_id());
				loginData.setLine_name(pEntity.getLine_name());

				if (sworkingPf.getSection_id() != null && "00000000001".equals(sworkingPf.getSection_id())) {
					loginData.setSection_id(sworkingPf.getSection_id());
					SectionMapper sdao = conn.getMapper(SectionMapper.class);
					SectionEntity sbeam = sdao.getSectionByID(sworkingPf.getSection_id());
					loginData.setSection_name(sbeam.getName());
				}
			}
		}
	}

	/**
	 * 做成登录的Session项目
	 *
	 * @param operator
	 * @param res 
	 * @param conn
	 * @param company
	 * @param errorsDto
	 * @return 是否多角色
	 */
	public Map<String, String> makeSession(OperatorForm operator, HttpServletRequest req, HttpServletResponse res, List<MsgInfo> errors, SqlSession conn) {

		HttpSession session = req.getSession();

		String clientIp = RvsUtils.getClientIp(req);
		// 锁定
		if (OperatorService.checkBruteForce(clientIp)) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("job_no");
			error.setErrcode("login.bruteForce");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("login.bruteForce"));
			errors.add(error);
			return null;
		}

		// 表单复制到数据对象
		OperatorEntity conditionBean = new OperatorEntity();
		// 按工号查询
		conditionBean.setJob_no(operator.getJob_no());

		OperatorMapper dao = conn.getMapper(OperatorMapper.class);

		// 从数据库中查询记录
		if ("000".equals(conditionBean.getJob_no()) || "superadmin".equalsIgnoreCase(conditionBean.getJob_no()) || "SA000000".equalsIgnoreCase(conditionBean.getJob_no())) {
			conditionBean.setJob_no("SA000000");
		} else {
			List<OperatorNamedEntity> lResultBean = dao.searchOperator(conditionBean);

			// 用户不存在
			if (lResultBean.isEmpty()) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("job_no");
				error.setErrcode("login.invalidUser");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("login.invalidUser"));
				errors.add(error);
				return null;
			} else {
			}
		}

		// 按工号密码查询
		long lNow = new Date().getTime();
		String decPassword = RvsUtils._decPwd(operator.getPwd(), lNow);

		String password = CryptTool.encrypttoStr(decPassword);
		password = CryptTool.encrypttoStr(password + conditionBean.getJob_no().toUpperCase());
		conditionBean.setPwd(password);
		LoginData loginData = dao.searchLoginOperator(conditionBean);

		// 密码不匹配
		if (loginData == null) {
			// 可能重拾超时

			try {
				password = RvsUtils._decPwd(operator.getPwd(), lNow - 600000); // 按10分钟前 
				if (!decPassword.equals(password)) {
					password = CryptTool.encrypttoStr(password);
					password = CryptTool.encrypttoStr(password + conditionBean.getJob_no().toUpperCase());
					conditionBean.setPwd(password);
					loginData = dao.searchLoginOperator(conditionBean);
				}
				if (loginData == null) {
					password = RvsUtils._decPwd(operator.getPwd(), lNow + 600000); // 按10分钟后 
					if (!decPassword.equals(password)) {
						password = CryptTool.encrypttoStr(password);
						password = CryptTool.encrypttoStr(password + conditionBean.getJob_no().toUpperCase());
						conditionBean.setPwd(password);
						loginData = dao.searchLoginOperator(conditionBean);
					}
				}
			}catch (Exception e) {
			}

			if (loginData == null) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("pwd");
				error.setErrcode("login.invalidPassword");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("login.invalidPassword"));
				errors.add(error);

				// 设定记录
				log.info("BruteForce clientIp:" + clientIp);
				OperatorService.recordBruteForce(clientIp);
				OperatorService.setIpRecorder(clientIp, operator.getJob_no());

				return null;
			}
		}

		if (RvsConsts.DEPART_REPAIR.equals(loginData.getDepartment())) {
			Cookie cookie = new Cookie("dpt", RvsConsts.DEPART_REPAIR + "");
			cookie.setMaxAge(240000000);
			res.addCookie(cookie);
		}

		if (errors.isEmpty()) {
			OperatorService oservice = new OperatorService();
			Map<String, String> uRoles = oservice.getUserRoles(loginData.getOperator_id(), conn);
			if (uRoles != null && uRoles.size() > 0) {
				// 判断是否有进行中工作，如果有则按工位直接决定角色
				ProductionFeatureEntity workingPf = getWoringPf(loginData, conn);
				if (workingPf != null) {
					String now_position_id = workingPf.getPosition_id();

//					if ("acceptance".equals(specPage)) { // 受理
//						loginData.setWorking_role_id(RvsConsts.ROLE_ACCEPTOR);
//					} else if ("quotation".equals(specPage)) { // 报价
//						loginData.setWorking_role_id(RvsConsts.ROLE_QUOTATOR);
//					} else if ("serviceRepairReferee".equals(specPage)
//							|| "qualityAssurance".equals(specPage)) { // 出检
//						loginData.setWorking_role_id(RvsConsts.ROLE_QAER);
//					} else if ("shipping".equals(specPage)) { // 出货
//						loginData.setWorking_role_id(RvsConsts.ROLE_SHIPPPER);
//					} else {
						loginData.setWorking_role_id(RvsConsts.ROLE_OPERATOR);
//					}

					// 如果当前角色无权限进行工位作业，则强制切换成进行中角色
					if (loginData.getWorking_role_id() != null) {
						if (!getPrivacies(loginData.getRole_id(), conn).contains(RvsConsts.PRIVACY_POSITION)) {
							loginData.setRole_id(loginData.getWorking_role_id());
							RoleService rServ = new RoleService();

							loginData.setRole_name(rServ.getRoleName(loginData.getWorking_role_id(), conn));
						}
					}

					setDetail(loginData, conn, workingPf);

					// 用户信息保存在会话中
					session.setAttribute(RvsConsts.SESSION_USER, loginData);
				} else {
					uRoles.put("main", loginData.getRole_name());
					// 多角色
					// 用户信息(暂定)保存在会话中
					session.setAttribute("K" + RvsConsts.SESSION_USER, loginData);
					return uRoles;
				}
			} else {
				// 单角色
				ProductionFeatureEntity workingPf = getWoringPf(loginData, conn);
				setDetail(loginData, conn, workingPf);

				// 用户信息保存在会话中
				session.setAttribute(RvsConsts.SESSION_USER, loginData);

				// 系统管理员的超时只有30分钟
				if (RvsConsts.ROLE_SYSTEM.equals(loginData.getRole_id())) {
					session.setMaxInactiveInterval(1800);
				}
			}
		}

		return null;
	}

	private List<Integer> getPrivacies(String role_id, SqlSession conn) {
		RoleService rService = new RoleService();
		return rService.getUserPrivacies(role_id, conn);
	}

	/**
	 * 设定可选工位/工程/课室
	 * @param loginData
	 * @param conn
	 */
	private void setPositions(LoginData loginData, SqlSession conn) {

		OperatorService oService = new OperatorService();
		List<PositionEntity> positionsList = new ArrayList<PositionEntity>();

		if (loginData.getPosition_id() != null) {
			PositionEntity mainPosition = new PositionEntity();
			// getPositionByID TODO
			mainPosition.setPosition_id(loginData.getPosition_id());
			mainPosition.setName(loginData.getPosition_name());
			mainPosition.setProcess_code(loginData.getProcess_code());
			mainPosition.setLine_id(loginData.getLine_id());
			mainPosition.setLine_name(loginData.getLine_name());
			if (PositionService.getDividePositions(conn).contains(loginData.getPosition_id())) {
				mainPosition.setLight_division_flg(1);
			} else {
				mainPosition.setLight_division_flg(0);
			}

			positionsList.add(mainPosition);
		}
		// positionsList.addAll();
		for (PositionEntity p : oService.getUserPositions(loginData.getOperator_id(), conn)) {
			if (!p.getPosition_id().equals(loginData.getPosition_id())) {
				positionsList.add(p);
			}
		}

		loginData.setPositions(positionsList);

		List<LineEntity> linesList = new ArrayList<LineEntity>();

		Set<String> line_ids = new HashSet<String>();
		for (PositionEntity position : positionsList) {
			// PositionEntity position =
			String line_id = position.getLine_id();
			if (!line_ids.contains(line_id) && !"00000000000".equals(line_id)) {
				line_ids.add(line_id);
				LineEntity line = new LineEntity();
				line.setLine_id(line_id);
				line.setName(position.getLine_name());
				linesList.add(line);
			}
		}
		loginData.setLines(linesList);

		SectionService sService = new SectionService();
		List<SectionEntity> sectionList = sService.getSectionsByOperate(loginData.getOperator_id(), conn);

		loginData.setSections(sectionList);

		return;
	}

	public void pdaLogin(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {
		_logger.fine("LoginAction.pdaLogin start");

		OperatorForm operator = (OperatorForm)form;
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(operator, BeanUtil.CHECK_TYPE_ALL);
		v.delete("role_id", "name");
		v.add("pwd", v.required("密码"));

		List<MsgInfo> errors = v.validate();

		boolean hasPrivacy = false;
		if (errors.isEmpty()) {
			req.setAttribute("isFact", false);
			req.setAttribute("isRecept", false);

			// 建立会话用户信息
			List<Integer> privacies = pdaMakeSession(operator, req, errors, conn);
			if (privacies != null) {
				if (privacies.contains(RvsConsts.PRIVACY_FACT_MATERIAL)) {
					req.setAttribute("isFact", true);
					hasPrivacy = true;
				}

				LoginData loginData = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);
				for (LineEntity lines : loginData.getLines()) {
					if ("00000000011".equals(lines.getLine_id())) {
						if (privacies.contains(RvsConsts.PRIVACY_POSITION)) {
							req.setAttribute("isRecept", true);
							hasPrivacy = true;
						}
						break;
					}
				}
			}
		}

		if (errors.isEmpty() && !hasPrivacy) {
			MsgInfo info = new MsgInfo();
			info.setComponentid("job_no");
			info.setErrcode("privacy.noPrivacy");
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("privacy.noPrivacy"));
			errors.add(info);
		}
		if (!errors.isEmpty()) {
			req.setAttribute("errors", getStrMsgInfo(errors));
			// 迁移到页面
			actionForward = mapping.findForward(FW_PDA_INIT);
		} else {
			// 迁移到页面
			actionForward = mapping.findForward(FW_PDA_MENU);
		}

		_logger.fine("LoginAction.pdaLogin end");
	}

	private String getStrMsgInfo(List<MsgInfo> errors) {
		StringBuffer msgInfo = new StringBuffer();
		for (int i = 0; i < errors.size(); i++) {
			MsgInfo error = errors.get(i);
			if (!CommonStringUtil.isEmpty(error.getErrmsg())) {
				if (i >= 1) {
					msgInfo.append("\\n");
				}
				msgInfo.append(error.getErrmsg());
			}
		}
		return msgInfo.toString();
	}

	/**
	 * 做成登录的Session项目
	 *
	 * @param operator
	 * @param conn
	 * @param company
	 * @param errorsDto
	 * @return 权限集
	 */
	public List<Integer> pdaMakeSession(OperatorForm operator, HttpServletRequest req, List<MsgInfo> errors, SqlSession conn) {

		// 表单复制到数据对象
		OperatorEntity conditionBean = new OperatorEntity();
		// 按工号查询
		conditionBean.setJob_no(operator.getJob_no());

		OperatorMapper dao = conn.getMapper(OperatorMapper.class);

		String clientIp = RvsUtils.getClientIp(req);
		// 锁定
		if (OperatorService.checkBruteForce(clientIp)) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("job_no");
			error.setErrcode("login.bruteForce");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("login.bruteForce"));
			errors.add(error);
			return null;
		}

		// 从数据库中查询记录
		if ("000".equals(conditionBean.getJob_no()) || "superadmin".equalsIgnoreCase(conditionBean.getJob_no())
				|| "SA000000".equalsIgnoreCase(conditionBean.getJob_no())) {
			conditionBean.setJob_no("SA000000");
		} else {
			List<OperatorNamedEntity> lResultBean = dao.searchOperator(conditionBean);

			// 用户不存在
			if (lResultBean.isEmpty()) {
				MsgInfo error = new MsgInfo();
				error.setComponentid("job_no");
				error.setErrcode("login.invalidUser");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("login.invalidUser"));
				errors.add(error);
				return null;
			}
		}

		// 按工号密码查询
		String password = operator.getPwd();
		password = CryptTool.encrypttoStr(password);
		password = CryptTool.encrypttoStr(password + conditionBean.getJob_no().toUpperCase());
		conditionBean.setPwd(password);
		LoginData loginData = dao.searchLoginOperator(conditionBean);

		// 密码不匹配
		if (loginData == null) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("pwd");
			error.setErrcode("login.invalidPassword");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("login.invalidPassword"));
			errors.add(error);

			// 设定记录
			log.info("BruteForce clientIp:" + clientIp);
			OperatorService.recordBruteForce(clientIp);
			OperatorService.setIpRecorder(clientIp, operator.getJob_no());

			return null;
		}

		if (errors.isEmpty()) {
			loginData.setPrivacies(getPrivacies(loginData.getRole_id(), conn));

			OperatorService oService = new OperatorService();
			List<PositionEntity> positionsList = oService.getUserPositions(loginData.getOperator_id(), conn);

			List<LineEntity> linesList = new ArrayList<LineEntity>();

			Set<String> line_ids = new HashSet<String>();
			for (PositionEntity position : positionsList) {
				String line_id = position.getLine_id();
				if (!line_ids.contains(line_id) && !"00000000000".equals(line_id)) {
					line_ids.add(line_id);
					LineEntity line = new LineEntity();
					line.setLine_id(line_id);
					line.setName(position.getLine_name());
					linesList.add(line);
				}
			}
			loginData.setLines(linesList);

			// 系统管理员的超时只有30分钟
			if (RvsConsts.ROLE_SYSTEM.equals(loginData.getRole_id())) {
				req.getSession().setMaxInactiveInterval(1800);
			}

			// 用户信息保存在会话中
			req.getSession().setAttribute(RvsConsts.SESSION_USER, loginData);
			req.getSession().setAttribute("user_name", loginData.getName());
			return loginData.getPrivacies();
		}
		return null;
	}

	/**
	 * 仪表客户端登录
	 * 
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doMeter(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSessionManager conn) throws Exception {
		_logger.fine("LoginAction.doMeter start");

		// Ajax响应对象
		Map<String, Object> callbackResponse = new HashMap<String, Object>();

		String client_ip = req.getParameter("client_ip");
		String process_code = req.getParameter("process_code");
		String line_part = req.getParameter("line_part");

		ProcedureStepCountService service = new ProcedureStepCountService();
		service.clientLogin(client_ip, process_code, line_part, callbackResponse, conn);

		// 返回Json格式响应信息
		returnJsonResponse(res, callbackResponse);

		_logger.fine("LoginAction.doMeter start");
	}
}
