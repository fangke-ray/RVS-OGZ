package framework.huiqing.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.osh.rvs.common.RvsConsts;

import framework.huiqing.bean.BaseUserBean;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.mybatis.SqlSessionFactorySingletonHolder;
import framework.huiqing.common.util.BaseConst;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.XssShieldUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.server.CheckXssHttpServletRequest;

public class BaseAction extends DispatchAction {

	private Logger logger = Logger.getLogger("BaseAction");

	protected static final String FW_INIT = "init";
	protected static final String FW_SUCCESS = "success";
	protected static final String FW_PDA_INIT = "pda_init";
	protected static final String FW_PDA_MENU = "pda_menu";
	protected static final String FW_GLOBELBREAK = "error";

	protected ActionForward actionForward = null;

	protected String redirection = null;

	protected BaseUserBean userBean = null;

	protected ActionErrors errors = null;

	protected static JSON json = new JSON();

	static {
		json.setSuppressNull(true);
	}

	/**
	 * 构造重定向路径
	 * @param path URL路径
	 * @param method 方法属性
	 */
	protected synchronized void redirect(String path, String method) {

		redirection = path + ".do?method=" + method;

	}

	@Override
	@SuppressWarnings("rawtypes")
	public synchronized ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) {

		errors = new ActionErrors();

		String strMethod = req.getParameter(BaseConst.METHOD);
		logger.debug(this.getClass().getName() + strMethod);

		actionForward = null;

		this.userBean = (BaseUserBean) req.getSession().getAttribute(BaseConst.SESSION_USER);

		if (CommonStringUtil.isEmpty(strMethod)) {

			strMethod = "init";
		}

		boolean updatable = strMethod.startsWith("do");

		if (updatable) {
			// Xss攻击排除
			if (form != null && form instanceof ActionForm) {
				BeanUtil.checkPostXss(form);
			} else {
				if (checkRequestParamsXss(req)) {
					req = new CheckXssHttpServletRequest(req);
				}
			}
		}

		// 读取提交的方式
		String requestType = req.getHeader("RequestType");

		// 得到Session
		HttpSession session = req.getSession();

		@SuppressWarnings("static-access")
		SqlSessionFactory factory = SqlSessionFactorySingletonHolder.getInstance().getFactory();

		// 以方法开头带do来判断是否会写DB,提供不同的DB连接
		if (updatable) {
			// 有向DB写操作的场合
			SqlSessionManager conn = SqlSessionManager.newInstance(factory);

			Class[] params = { ActionMapping.class, ActionForm.class, HttpServletRequest.class, HttpServletResponse.class, SqlSessionManager.class };

			try {
				Method objMethod = this.getClass().getMethod(strMethod, params);

				if (!hasPrivacy(session, objMethod)) {
					breakByPrivacy(mapping, strMethod, "ajax".equals(requestType), req, res);
				} else {
					conn.startManagedSession(ExecutorType.BATCH, TransactionIsolationLevel.REPEATABLE_READ);

					objMethod.invoke(this, new Object[] { mapping, form, req, res, conn });

					if (conn != null && conn.isManagedSessionStarted()) {
						conn.commit();
					}
				}
			} catch (SecurityException e) {
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.rollback();
					res.setStatus(503);
					try {
						res.getWriter().close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				req.setAttribute("errormessage", e.getMessage());
				actionForward = mapping.findForward("error");
				logger.error(this.getClass().getName() + " SecurityException:", e);
			} catch (InvocationTargetException e) {
				logger.error(this.getClass().getName() + " InvocationTargetException:", e);
				sendBreakPage(mapping, getInvocationTargetMessage(e), strMethod, req, res);
			} catch (Exception e) {
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.rollback();
					res.setStatus(503);
//					try {
//						res.getWriter().println("{}");
//					} catch (IOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
				}
				if (e instanceof NoSuchMethodException) {
					this.unspecified(mapping, form, req, res);
				} else {
					sendBreakPage(mapping, e.getMessage(), strMethod, req, res);
					logger.error(this.getClass().getName() + " Exception:", e);
					e.printStackTrace();
				}
			} finally {
				if (conn != null && conn.isManagedSessionStarted()) {
					conn.close();
				}
				conn = null;
			}
		} else {
			// 仅从DB读取的 场合
			SqlSession conn = factory.openSession(TransactionIsolationLevel.READ_COMMITTED);

			Class[] params = { ActionMapping.class, ActionForm.class, HttpServletRequest.class, HttpServletResponse.class, SqlSession.class };

			try {

				Method objMethod = this.getClass().getMethod(strMethod, params);

				if (!hasPrivacy(session, objMethod)) {
					breakByPrivacy(mapping, strMethod, "ajax".equals(requestType), req, res);
				} else {
					objMethod.invoke(this, new Object[] { mapping, form, req, res, conn });
				}
			} catch (SecurityException e) {
				e.printStackTrace();
				logger.error(this.getClass().getName() + " SecurityException:", e);
			} catch (NoSuchMethodException e) {
				this.unspecified(mapping, form, req, res);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				logger.error(this.getClass().getName() + " IllegalArgumentException:", e);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				logger.error(this.getClass().getName() + " IllegalAccessException:", e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				logger.error(this.getClass().getName() + " InvocationTargetException:", e);
				sendBreakPage(mapping, getInvocationTargetMessage(e), strMethod, req, res);
			} catch (Exception e) {
				logger.error(this.getClass().getName() + " Exception:", e);
				if (e instanceof NoSuchMethodException) {
					this.unspecified(mapping, form, req, res);
				} else {
					sendBreakPage(mapping, e.getMessage(), strMethod, req, res);
					logger.error(this.getClass().getName() + " Exception:", e);
					e.printStackTrace();
				}
			} finally {
				if (conn != null) {
					conn.close();
				}
				conn = null;
			}
		}


		// }
		if ("ajax".equals(requestType)) {
			postJsonResponse(res);
		}

		saveErrors(req, errors);

		return actionForward;
	}

	private static String getInvocationTargetMessage(InvocationTargetException e) {
		if (e.getMessage() != null) {
			return e.getMessage();
		}
		if (e.getTargetException() != null) {
			String message = e.getTargetException().getMessage();
			if (message != null) {
				String[] messageLines = message.split("\r\n");
				for (String messageLine : messageLines) {
					if (messageLine.length() > 0) {
						message = messageLine;
						break;
					}
				}
				return message;
			}
		}

		return null;
	}

	/**
	 * 无form定义时，修改reqParameter
	 * @param req
	 */
	private boolean checkRequestParamsXss(HttpServletRequest req) {
		Map<String, String[]> parameterMap = req.getParameterMap();
		boolean needShield = false;
		for (String key : parameterMap.keySet()) {
			if (BaseConst.METHOD.equals(key)) continue;

			String[] vals = parameterMap.get(key);
			if (vals.length >= 1) {
				String val = vals[0];
				if (val != null && val.length() > 16) {
					String dVal = XssShieldUtil.stripXss(val);
					if (!val.equals(dVal)) {
						needShield = true;
						return needShield;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param mapping
	 * @param strMethod 
	 * @param req
	 * @param res
	 */
	private void breakByPrivacy(ActionMapping mapping, String strMethod, boolean isAjaxType, 
			HttpServletRequest req, HttpServletResponse res) {
		// 没有权限实行该方法的情况下
		MsgInfo error = new MsgInfo();
		error.setErrcode("privacy.noPrivacy");
		error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("privacy.noPrivacy"));
		List<MsgInfo> msgInfos = new ArrayList<MsgInfo>();
		msgInfos.add(error);

		Map<String, Object> callBackResponse = new HashMap<String, Object>();
		// 迁移画面设定为故障画面
		callBackResponse.put("redirect", "break.do"); // TODO 通用化
		callBackResponse.put("errors", msgInfos);
		callBackResponse.put("request_occur", req.getRequestURI());
		callBackResponse.put("request_method", strMethod);
		callBackResponse.put("occur_time", new Date().toString());
		req.getSession().setAttribute("break_cb", callBackResponse);

		if (isAjaxType) {
			// 如果是以AJAX方式提交

			returnJsonResponse(res, callBackResponse);
		} else {
			// 报告错误信息
			req.setAttribute("errors", msgInfos);
			req.setAttribute("request_occur", req.getRequestURI());
			req.setAttribute("request_method", strMethod);
			req.setAttribute("occur_time", new Date().toString());
			actionForward = mapping.findForward(FW_GLOBELBREAK);
		}
	}

	private void sendBreakPage(ActionMapping mapping, String exMessage, String strMethod,
			HttpServletRequest req,	HttpServletResponse res) {
		if (exMessage.contains("CommunicationsException")) {
			return;
		}

		MsgInfo error = new MsgInfo();
		error.setErrmsg(exMessage);
		List<MsgInfo> msgInfos = new ArrayList<MsgInfo>();
		msgInfos.add(error);

		// 读取提交的方式
		String requestType = req.getHeader("RequestType");
		if ("ajax".equals(requestType)) {
			// 如果是以AJAX方式提交

			Map<String, Object> callBackResponse = new HashMap<String, Object>();
			// 迁移画面设定为故障画面
			callBackResponse.put("redirect", "break.do"); // TODO 通用化
			callBackResponse.put("errors", msgInfos);
			callBackResponse.put("request_occur", req.getRequestURI());
			callBackResponse.put("request_method", strMethod);
			callBackResponse.put("occur_time", new Date().toString());
			req.getSession().setAttribute("break_cb", callBackResponse);
			res.setStatus(200);
			returnJsonResponse(res, callBackResponse);
		} else {
			// 报告错误信息
			req.setAttribute("errors", msgInfos);
			req.setAttribute("request_occur", req.getRequestURI());
			actionForward = mapping.findForward(FW_GLOBELBREAK);
		}
	}


	private boolean hasPrivacy(HttpSession session, Method objMethod) {
		// 无Session内容时以过滤器判断
		Object oUserdata = session.getAttribute(RvsConsts.SESSION_USER); // 不统一只能
		if (oUserdata == null) {
			return true;
		}
		BaseUserBean userdata = (BaseUserBean) oUserdata;
		if (userdata.getPrivacies() == null) {
			// 没有权限的用户
			return false;
		}

		Privacies methodPrivacies = objMethod.getAnnotation(Privacies.class);
		if (methodPrivacies == null) {
			// 不需权限的方法
			return true;
		}
		List<Integer> userPrivacies = userdata.getPrivacies();
		int[] permits = methodPrivacies.permit();
		for (int permit : permits) {
			if (userPrivacies.contains(permit)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 无方法属性时,默认转向success
	 */
	@Override
	protected ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

		logger.error("unspecified method :" + request.getParameter(BaseConst.METHOD));

		return mapping.findForward("success");
	}

	/**
	 * 写一个JSON格式的反响
	 */
	protected void returnJsonResponse(HttpServletResponse response, Object result) {
		PrintWriter out;
		try {
			response.setCharacterEncoding("UTF-8");
			response.resetBuffer();
			out = response.getWriter();
			out.print(json.format(result));
		} catch (IOException e) {
			// TODO 待处理
			e.printStackTrace();
		}
	}

	/**
	 * 写一个JSON格式的反响
	 */
	private void postJsonResponse(HttpServletResponse response) {
		PrintWriter out;
		try {
			out = response.getWriter();
			out.flush();
		} catch (IOException e) {
			// TODO 待处理
			e.printStackTrace();
		}
	}
}
