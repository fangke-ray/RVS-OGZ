package com.osh.rvs.action;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.service.ImageService;
import com.osh.rvs.service.UploadService;

import framework.huiqing.action.BaseAction;
import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.FileUtils;

public class ImageAction extends BaseAction {

	private static Logger logger = Logger.getLogger("ImageUpload");

	/**
	 * 上传源图片(单个文件上传-file)
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void sourceImage(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res, SqlSession conn) throws Exception {
		logger.info("ImageAction.sourceImage start");

		// Ajax回馈对象
		Map<String, Object> jsonResponse = new HashMap<String, Object>();
		List<MsgInfo> msgs = new ArrayList<MsgInfo>();

		UploadService fservice = new UploadService();
		String tempFilePath = fservice.getFile2Local(form, msgs);//单个文件
		String tempfilename = "";

		if (msgs.size() == 0) {
			ImageService service = new ImageService();

			File confFile = new File(tempFilePath);
			if (confFile.exists()) {
				UUID uuid = UUID.randomUUID();
				tempfilename = uuid.toString().replaceAll("-", "");
				String targetPath = PathConsts.BASE_PATH + PathConsts.PHOTOS + "/upload/" + tempfilename.substring(0,4) + "/" + tempfilename;
				FileUtils.copyFile(tempFilePath, targetPath+".jpg", true);
				FileUtils.copyFile(tempFilePath, targetPath+"_fix.jpg", true);
				service.getOriginalImageSize(confFile, jsonResponse, 800);
			}
		}

		jsonResponse.put("photoFilename", tempfilename);
		// 检查发生错误时报告错误信息
		jsonResponse.put("errors", msgs);
		// 返回Json格式响应信息
		returnJsonResponse(res, jsonResponse);

		logger.info("ImageAction.sourceImage end");
	}
	/**
	 * 截取制作图片 execute
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public void crop(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res, SqlSession conn) throws Exception {
		logger.info("ImageAction.crop start");

		// Ajax回馈对象
		Map<String, Object> jsonResponse = new HashMap<String, Object>();
		List<MsgInfo> msgs = new ArrayList<MsgInfo>();

		ImageService service = new ImageService();
		String retPath = service.cutImage(req, msgs);

		jsonResponse.put("retPath", retPath);

		// 检查发生错误时报告错误信息
		jsonResponse.put("errors", msgs);
		// 返回Json格式响应信息
		returnJsonResponse(res, jsonResponse);

		logger.info("ImageAction.crop end");
	}

	public void reset(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res, SqlSession conn) throws Exception {
		logger.info("ImageAction.reset start");

		// Ajax回馈对象
		Map<String, Object> jsonResponse = new HashMap<String, Object>();
		List<MsgInfo> msgs = new ArrayList<MsgInfo>();

		ImageService service = new ImageService();
		String retPath = service.resetImage(req, msgs, jsonResponse);

		// 检查发生错误时报告错误信息
		jsonResponse.put("errors", msgs);
		// 返回Json格式响应信息
		returnJsonResponse(res, jsonResponse);

		logger.info("ImageAction.reset end");
	}

	public void rotate(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res, SqlSession conn) throws Exception {
		logger.info("ImageAction.rotate start");

		// Ajax回馈对象
		Map<String, Object> jsonResponse = new HashMap<String, Object>();
		List<MsgInfo> msgs = new ArrayList<MsgInfo>();

		ImageService service = new ImageService();
		service.rotateImage(req, msgs);

		// 检查发生错误时报告错误信息
		jsonResponse.put("errors", msgs);
		// 返回Json格式响应信息
		returnJsonResponse(res, jsonResponse);

		logger.info("ImageAction.rotate end");
	}

	public void test(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res, SqlSession conn) throws Exception{

		log.info("ImageAction.test start");

		

		log.info("ImageAction.test end");
	}
}
