package com.osh.rvs.action.manage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.manage.ProcessInspectConfirmForm;
import com.osh.rvs.service.manage.ProcessInspectConfirmService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.validator.Validators;


/**
 * 作业监察确认
 * @Description
 * @author liuxb
 * @date 2021-11-26 上午9:20:39
 */
public class ProcessInspectConfirmAction extends BaseAction {
	private Logger _log = Logger.getLogger(getClass());
	private ProcessInspectConfirmService service = new ProcessInspectConfirmService();
	
	
	/**
	 * 盖章（经理、部长）
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void doSign(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSessionManager conn) throws Exception {
		_log.info("ProcessInspectConfirmAction.doSign start");
		
		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();
		
		if (errors.size() == 0) {
			//页面数据
			ProcessInspectConfirmForm pageForm = (ProcessInspectConfirmForm)form;
			String processFlg = pageForm.getProcess_flg();//盖章标记
			
			// 取得用户信息
			HttpSession session = req.getSession();
			LoginData user = (LoginData) session.getAttribute(RvsConsts.SESSION_USER);
			
			//取得作业监察确认盖章信息
			ProcessInspectConfirmForm dbConfirm = service.getInspectConfirm(form, conn);
			
			Calendar cal = Calendar.getInstance();
			String strDate = DateUtil.toString(cal.getTime(), DateUtil.DATE_PATTERN);
			
			//判断盖章记录是否存在
			if(dbConfirm == null){//不存在盖章记录
				//创建盖章记录
				if("1".equals(processFlg)){//经理
					pageForm.setSign_manager_id(user.getOperator_id());
					pageForm.setSign_manager_date(strDate);
				} else {//部长
					pageForm.setSign_minister_id(user.getOperator_id());
					pageForm.setSign_minister_date(strDate);
				}
				service.insert(pageForm, conn);
			} else {
				//存在盖章记录（【经理】或者【部长】其中一人已经盖过章）
				if("1".equals(processFlg)){//【经理】盖章
					dbConfirm.setSign_manager_id(user.getOperator_id());
					dbConfirm.setSign_manager_date(strDate);
				} else {//【部长】盖章
					dbConfirm.setSign_minister_id(user.getOperator_id());
					dbConfirm.setSign_minister_date(strDate);
				}
				service.update(dbConfirm, conn);
			}
		}
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		_log.info("ProcessInspectConfirmAction.doSign end");
	}
	
}
