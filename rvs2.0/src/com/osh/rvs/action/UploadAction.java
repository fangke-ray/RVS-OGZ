package com.osh.rvs.action;

import static com.osh.rvs.service.UploadService.toXls2003;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.partial.MaterialPartialEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.UploadForm;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.service.UploadService;
import com.osh.rvs.service.inline.PositionPanelService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.FileUtils;
import framework.huiqing.common.util.message.ApplicationMessage;

public class UploadAction extends BaseAction {

	private static Logger logger = Logger.getLogger("Upload");

	/**
	 * 受理文档上传 execute
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void doAccept(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {
		logger.info("UploadAction.accept start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		UploadService uService = new UploadService();
		String tempfilename = uService.getFile2Local(form, errors);
		// 转换2003格式
		if (tempfilename.endsWith(".xlsx")) {
			tempfilename = toXls2003(tempfilename);
		}
		if (errors.size() == 0) {
			List<MaterialForm> readList = new ArrayList<MaterialForm>();
			readList = uService.readFile(tempfilename, conn, errors);

			if (readList.size() == 0 && errors.size() == 0) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("file.invalidFormat");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidFormat"));
				errors.add(error);
				listResponse.put("errors", errors);
			}
			// 查询结果放入Ajax响应对象
			listResponse.put("list", readList);
		}

		if (errors.size() == 0) {
			conn.commit();

			// 触发检测
			HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
			httpclient.start();
			try {
				HttpGet request = new HttpGet("http://localhost:8080/rvspush/trigger/lateinline/accept"
						+ "/" + new Random().nextInt());
				logger.info("finger:" + request.getURI());
				httpclient.execute(request, null);
			} catch (Exception e) {
			} finally {
				Thread.sleep(100);
				httpclient.shutdown();
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		logger.info("UploadAction.accept end");
	}

	/**
	 * 受理文档上传 execute
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void doagree(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {
		logger.info("UploadAction.agree start");
		// Json响应信息
		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();
		// 实际上是文件名字
		UploadService uService = new UploadService();
		String tempfilename = uService.getFile2Local(form, errors);

		if (errors.size() == 0) {
			// 获取了将EXCEL表格的内容
			List<MaterialForm> readList = uService.readAgreed(tempfilename, conn, errors);
			// 如果数据是空的话
			if (readList == null) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("file.invalidFormat");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidFormat"));
				errors.add(error);
				listResponse.put("error", errors);
			} else {
				// 查询结果放入Ajax响应对象
				listResponse.put("list", readList);

				// 触发检测
				HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
				httpclient.start();
				try {
					HttpGet request = new HttpGet("http://localhost:8080/rvspush/trigger/lateinline/" + readList.size()
							+ "/" + new Random().nextInt());
					logger.info("finger:" + request.getURI());
					httpclient.execute(request, null);
				} catch (Exception e) {
				} finally {
					Thread.sleep(100);
					httpclient.shutdown();
				}
			}
		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);
		logger.info("UploadAction.agree end");
	}

	/**
	 * 受理文档上传 execute
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void doPartOrder(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {
		logger.info("UploadAction.partOrder start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		UploadService uService = new UploadService();
		String tempfilename = uService.getFile2Local(form, errors);
		// 转换2003格式
		if (tempfilename.endsWith(".xlsx")) {
			tempfilename = toXls2003(tempfilename);
		}
		if (errors.size() == 0) {
			LoginData user = (LoginData) req.getSession().getAttribute(RvsConsts.SESSION_USER);

			List<MaterialPartialEntity> readList = new ArrayList<MaterialPartialEntity>();
			readList = uService.readPartOrderFile(tempfilename, user, conn, errors);

			if (readList.size() == 0 && errors.size() == 0) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("file.invalidFormat");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidFormat"));
				errors.add(error);
				listResponse.put("errors", errors);
			}

		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		logger.info("UploadAction.partOrder end");
	}

	/**
	 * 归档文件上传
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void confirm(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSession conn) throws Exception {
		logger.info("UploadAction.confirmInline start");
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
		UploadService uservice = new UploadService();
		String nameParam = req.getParameter("filepath").replaceAll("report_", "");
		String filepath = uservice.readFileName(uploadDate.substring(0, 4) + uploadDate.substring(5, 7), nameParam);

		try {
			if (nameParam.equals("inline")) {
				filepath += "内镜投线记录表-";
			} else if (nameParam.equals("accept")) {
				filepath += "QR-B31002-59 内镜受理记录表-";
			} else if (nameParam.equals("sterilize")) {
				filepath += "QR-B31002-62 内镜EOG灭菌记录表-";
			} else if (nameParam.equals("disinfect")) {
				filepath += "QR-B31002-60 内镜清洗消毒记录表-";
			} else if (nameParam.equals("schedule")) {
				filepath += "计划报告书-";
			} else if (nameParam.equals("shipping")) {
				filepath += "QR-B31002-63 内镜出货记录表-";
			}
			fileOutput = new FileOutputStream(filepath + uploadDate + ".xls");
			fileOutput.write(file.getFileData());
			fileOutput.flush();
			fileOutput.close();
		} catch (FileNotFoundException e) {
			logger.error("FileNotFound:" + e.getMessage());
		} catch (IOException e) {
			logger.error("IO:" + e.getMessage());
		}

		listResponse.put("errors", errors);
		returnJsonResponse(res, listResponse);
		logger.info("UploadAction.confirmInline end");
	}

	/**
	 * 上传配置文件
	 * @param mapping
	 * @param form
	 * @param req
	 * @param res
	 * @param conn
	 * @throws Exception
	 */
	public void doProp(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res,
			SqlSessionManager conn) throws Exception {
		logger.info("UploadAction.doProp start");

		Map<String, Object> listResponse = new HashMap<String, Object>();
		List<MsgInfo> errors = new ArrayList<MsgInfo>();

		UploadService uService = new UploadService();
		String tempfilename = uService.getFile2Local(form, errors);

		if (errors.size() == 0) {
			String propName = req.getParameter("prop_name");
			// 取得上传的文件
			String fileName = PathConsts.BASE_PATH + PathConsts.PROPERTIES + "\\" 
					+ propName + ".properties";

			try {
				FileUtils.copyFile(fileName, fileName.replaceAll(".properties", ".setting"));
				FileUtils.copyFile(tempfilename, fileName);

				PathConsts.load();
				RvsUtils.initAll(conn);
				PositionPanelService.clearPatLineStandards();

				List<String> triggerList = new ArrayList<String>();
				triggerList.add("http://localhost:8080/rvspush/trigger/prop/" + propName + "/" + new Date().getTime());
				triggerList.add("http://localhost:8080/rvsscan/trigger/prop/" + propName + "/" + new Date().getTime());
//				triggerList.add("http://localhost:8080/rvspushG/trigger/prop/" + propName + "/" + new Date().getTime());
//				triggerList.add("http://localhost:8080/rvsscanG/trigger/prop/" + propName + "/" + new Date().getTime());
				// 控制其他工程
				RvsUtils.sendTrigger(triggerList);
			} catch (Exception e) {
				FileUtils.copyFile(fileName.replaceAll(".properties", ".setting"), fileName);
				PathConsts.load();

				if (errors.size() == 0) {
					MsgInfo error = new MsgInfo();
					error.setErrcode("file.invalidFormat");
					error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.invalidFormat"));
					errors.add(error);
					listResponse.put("errors", errors);
				}
			}

		}

		listResponse.put("errors", errors);

		// 返回Json格式响应信息
		returnJsonResponse(res, listResponse);

		logger.info("UploadAction.doProp end");
	}
}
