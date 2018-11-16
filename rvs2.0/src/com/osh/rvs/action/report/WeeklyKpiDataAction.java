package com.osh.rvs.action.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.form.UploadForm;
import com.osh.rvs.form.report.WeeklyKpiDataForm;
import com.osh.rvs.service.report.WeeklyKpiDataService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

public class WeeklyKpiDataAction extends BaseAction{
	private Logger log = Logger.getLogger(getClass());
	
	public void init(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSession conn) throws Exception {
		log.info("WeeklyKpiDataAction.init start");
		
		LoginData userData = (LoginData)req.getSession().getAttribute(RvsConsts.SESSION_USER);
		List<Integer> privacies  = userData.getPrivacies();
		if (privacies.contains(RvsConsts.PRIVACY_LINE)) {
			req.setAttribute("privacy", "line");
		}
		
		actionForward = mapping.findForward(FW_INIT);
		
		log.info("WeeklyKpiDataAction.init end");
	}
	
	
	public void search(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSession conn) throws Exception {
		log.info("WeeklyKpiDataAction.search start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		WeeklyKpiDataService service = new WeeklyKpiDataService();
		List<WeeklyKpiDataForm> fileNameList = service.searchAll(conn);

		listResponse.put("fileNameList", fileNameList);
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("WeeklyKpiDataAction.search end");
	}
	
	//上传确认
	public void confirm(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSession conn) throws Exception {
		log.info("WeeklyKpiDataAction.confirm start");
		
		UploadForm upfileForm = (UploadForm) form;
		Map<String, Object> listResponse = new HashMap<String, Object>();
		// 取得上传的文件
		FormFile file = upfileForm.getFile();
		// 上传文件名字
		String filename = file.getFileName();
		// 文件输出流
		FileOutputStream fileOutput;
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		if (file == null || CommonStringUtil.isEmpty(filename)) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("file.notExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
			errors.add(error);
		}
		
		String uploadDate = req.getParameter("date");
		uploadDate = new String(uploadDate.getBytes("iso-8859-1"), "utf-8");
		String weekly_of_year = req.getParameter("weekly_of_year");
		if(CommonStringUtil.isEmpty(weekly_of_year)) {
			weekly_of_year ="";
		}
		
		String filepath = PathConsts.BASE_PATH + PathConsts.REPORT +"\\weeks\\confirm\\";
		File fMonthPath = new File(filepath);
		if (!fMonthPath.exists()) {
			fMonthPath.mkdirs();
		}
		
		try {
			filepath += "OGZ内视镜修理周报（" + uploadDate + "） "+ weekly_of_year + "W";
			fileOutput = new FileOutputStream(filepath + ".xls");
			fileOutput.write(file.getFileData());
			fileOutput.flush();
			fileOutput.close();
		} catch (FileNotFoundException e) {
			log.error("FileNotFound:" + e.getMessage());
		} catch (IOException e) {
			log.error("IO:" + e.getMessage());
		}
		
		
		listResponse.put("errors", errors);
		returnJsonResponse(res, listResponse);
		
		log.info("WeeklyKpiDataAction.confirm end");
	}
	
	//删除
	public void delete(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSession conn)throws Exception {
	
		log.info("WeeklyKpiDataAction.delete start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		String count_date_start = req.getParameter("count_date_start");
		String count_date_end = req.getParameter("count_date_end");
		
		try {
			HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
			httpclient.start();
			try {  
				String inUrl = "http://localhost:8080/rvspush/trigger/weeklykpi/" + count_date_start + "/" + count_date_end + "/delete";
	            HttpGet request = new HttpGet(inUrl);
	            log.info("finger:"+request.getURI());
	            httpclient.execute(request, null);

	        } catch (Exception e) {
			} finally {
				Thread.sleep(100);
				httpclient.shutdown();
			}
		} catch (IOReactorException | InterruptedException e1) {
			log.error(e1.getMessage(), e1);
		}
		
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("WeeklyKpiDataAction.delete end");
	}
	
	//详细信息
	public void detail(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSession conn)throws Exception{
		log.info("WeeklyKpiDataAction.detail start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		
		WeeklyKpiDataService service = new WeeklyKpiDataService();
		// 查询详细
		service.getDetailsForPage(form, listResponse, conn);

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("WeeklyKpiDataAction.detail end");
	}
	
	
	public void doUpdate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSessionManager conn)throws Exception{
		log.info("WeeklyKpiDataAction.doUpdate start");
		
		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_PASSEMPTY);
		errors = v.validate();
		
		if (errors.size() == 0) {
			WeeklyKpiDataService service = new WeeklyKpiDataService();
			service.update(form, conn);
		}
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("WeeklyKpiDataAction.doUpdate end");
	}
	
	public void createReport(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSession conn)throws Exception{
		log.info("WeeklyKpiDataAction.createReport start");

		// Ajax响应对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		WeeklyKpiDataService service = new WeeklyKpiDataService();
		service.createWeekReport(form, conn);
		
		// 检查发生错误时报告错误信息
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("WeeklyKpiDataAction.createReport end");
	}
	
	/**
	 * 周报计算
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void calculate(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,SqlSession conn)throws Exception{
		log.info("WeeklyKpiDataAction.calculate start");
		
		// Ajax回馈对象
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		
		// 检索条件表单合法性检查
		Validators v = BeanUtil.createBeanValidators(form, BeanUtil.CHECK_TYPE_ALL);
		errors = v.validate();
		
		if(errors.size() == 0){
			WeeklyKpiDataService service = new WeeklyKpiDataService();
			service.checkCountDateEndIsExist(form, errors, conn);
			
			if(errors.size() == 0){
				WeeklyKpiDataForm weeklyKpiDataForm = (WeeklyKpiDataForm)form;
				String count_date_start = weeklyKpiDataForm.getCount_date_start().replaceAll("/", "");
				String count_date_end =  weeklyKpiDataForm.getCount_date_end().replaceAll("/", "");
				
				try {
					HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
					httpclient.start();
					try {  
						String inUrl = "http://localhost:8080/rvspush/trigger/weeklykpi/" + count_date_start + "/" + count_date_end;
			            HttpGet request = new HttpGet(inUrl);
			            log.info("finger:"+request.getURI());
			            httpclient.execute(request, null);

			        } catch (Exception e) {
					} finally {
						Thread.sleep(100);
						httpclient.shutdown();
					}
				} catch (IOReactorException | InterruptedException e1) {
					log.error(e1.getMessage(), e1);
				}
			}
			
			
		}
		
		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		
		log.info("WeeklyKpiDataAction.calculate end");
	}
	
}
