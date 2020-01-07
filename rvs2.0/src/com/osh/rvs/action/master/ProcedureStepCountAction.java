/**
 * 系统名：OGZ-RVS<br>
 * 模块名：系统管理<br>
 * 机能名：作业步骤计数系统管理事件<br>
 * @author 龚镭敏
 * @version 2.5.516
 */
package com.osh.rvs.action.master;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.common.SocketCommunitcator;

import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.validator.Validators;

public class ProcedureStepCountAction extends BaseAction {

	private Logger _log = Logger.getLogger(getClass());

	/**
	 * 作业步骤计数管理画面初始表示处理
	 * @param mapping ActionMapping
	 * @param form 表单
	 * @param req 页面请求
	 * @param res 页面响应
	 * @param conn 数据库会话
	 * @throws Exception
	 */
	@Privacies(permit={1, 0})
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		_log.info("ProcedureStepCountAction.init start");

		// 迁移到页面
		actionForward = mapping.findForward(FW_INIT);

		_log.info("ProcedureStepCountAction.init end");
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
	public void test(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		_log.info("ProcedureStepCountAction.test start");
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		List<MsgInfo> errors = v.validate();

		SocketCommunitcator scUtil = new SocketCommunitcator();
		Map<String, String> map = new HashMap<String, String>();
		map.put("omr_notifi_no", "TEST_单号");
		map.put("model_name", "TEST_型号");
		map.put("serial_no", "TEST_序列");
		map.put("set_times", "7");
		String recv = scUtil.clientSendMessage("127.0.0.1", 50023, "In:" + JSON.encode(map));

		Thread.sleep(10000);

		recv = scUtil.clientSendMessage("127.0.0.1", 50023, "Out:" + JSON.encode(map));
		_log.info(recv);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		_log.info("ProcedureStepCountAction.test end");
	}

}
