package com.osh.rvs.action;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import com.osh.rvs.service.FinalCheckService;
import framework.huiqing.action.BaseAction;
import framework.huiqing.action.Privacies;
import framework.huiqing.bean.message.MsgInfo;

public class FinalCheckAction extends BaseAction {
	private Logger log = Logger.getLogger(getClass());

	/**
	 * 初始化 显示页面的top数据
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

		log.info("FinalCheckAction.init start");
		
		actionForward = mapping.findForward(FW_INIT);

		log.info("FinalCheckAction.init end");
	}

	@Privacies(permit = { 1, 0 })
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSession conn) throws Exception {
		
		log.info("FinalCheckAction.search start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();

		//错误信息
		List<MsgInfo> msgInfoes = new ArrayList<MsgInfo>();
		FinalCheckService service = new FinalCheckService();	
		
		Calendar calendar=Calendar.getInstance();	
		
		Map<String, Object> retData = service.getInspectResultData(calendar, conn);
		
		service.getCounts(listResponse, conn);
		//所有数据(月份和当前月份周、检查总数、不合格数、上期检查总数、上期检查不合格数)
		listResponse.put("retData", retData);

		// 检查发生错误时报告错误信息
		listResponse.put("errors", msgInfoes);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		log.info("FinalCheckAction.search end");
	}	
}