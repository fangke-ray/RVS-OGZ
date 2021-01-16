package com.osh.rvs.service.manage;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import com.jacob.com.Dispatch;
import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.manage.PcsInputLimitEntity;
import com.osh.rvs.bean.master.ModelEntity;
import com.osh.rvs.bean.master.OperatorEntity;
import com.osh.rvs.bean.master.PcsRequestEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.PcsUtils;
import com.osh.rvs.common.XlsUtil;
import com.osh.rvs.form.master.PcsRequestForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.manage.PcsRequestMapper;
import com.osh.rvs.mapper.master.ModelMapper;
import com.osh.rvs.mapper.master.OperatorMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.FileUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class PcsRequestService {

	@SuppressWarnings("unused")
	private static final String CHANGE_MEANS_CREATE = "1";
	private static final String CHANGE_MEANS_EDIT = "2";
	private static final String CHANGE_MEANS_REFRESH = "3";
	@SuppressWarnings("unused")
	private static final String CHANGE_MEANS_FORMAT_FIX = "4";

	private static final String CHECKED = "√";
	private static final String UNCHECKED = "　";
	private static final String FORBIDDEN = "×";
	private static final String NOCARE = "不操作";

	public static Logger _logger = Logger.getLogger(PcsRequestService.class);

	public List<PcsRequestForm> findHistory(ActionForm form, SqlSession conn) {
		PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);
		PcsRequestEntity cond = new PcsRequestEntity();
		BeanUtil.copyToBean(form, cond, CopyOptions.COPYOPTIONS_NOEMPTY);
		List<PcsRequestEntity> lEntites = mapper.searchPcsRequests(cond);
		List<PcsRequestForm> lForms = new ArrayList<PcsRequestForm> ();
		BeanUtil.copyToFormList(lEntites, lForms, CopyOptions.COPYOPTIONS_NOEMPTY, PcsRequestForm.class);

		return lForms;
	}

	private static final String SESSION_ENTITY = "insertBean";
	private static final String SESSION_FILE = "uploadfile";

	public void create(HttpSession session, SqlSessionManager conn) throws Exception {
		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);

		PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);

		PcsRequestEntity insert = (PcsRequestEntity) session.getAttribute(SESSION_ENTITY);
		FormFile uploadfile = (FormFile) session.getAttribute(SESSION_FILE);

		// 得到对应的文件目录
		Map<String, String> folderTypes = PcsUtils.getFolderTypes();
		String path = "";
		for (String key : folderTypes.keySet()) {
			String folderType = folderTypes.get(key);
			if (("" + insert.getLine_type()).equals(key)) { // TODO String
				path = folderType;
				break;
			}
		}

		Set<String> modelIds = new HashSet<String>();
		StringBuffer reactedModelNames = new StringBuffer("");

		// 按照新+旧文件取得相关的型号
		String sOrgFileName = insert.getOrg_file_name();
		if (sOrgFileName != null) {
			sOrgFileName += ".html";
			Map<String, String> map = PcsUtils.getFileComparedGroup(path);
			ModelMapper mMapper = conn.getMapper(ModelMapper.class);
			List<ModelEntity> lModels = mMapper.getAllModel();
			for (ModelEntity model :lModels) {
				String sPathOfM = PcsUtils.getFileModelsOnCode(path, map, model);
				if (sPathOfM!= null && (sPathOfM.endsWith("\\" + sOrgFileName))) {
					reactedModelNames.append(model.getName() + ",");
					modelIds.add(model.getModel_id());
				}
			}
			if (reactedModelNames.length() > 0) { // 最后的逗号
				reactedModelNames.deleteCharAt(reactedModelNames.length() - 1);
			}
			if (reactedModelNames.length() > 512) {
				reactedModelNames = new StringBuffer(reactedModelNames.substring(0, 509) + "...");
			}
			insert.setReacted_model(reactedModelNames.toString());
			for (ModelEntity model : insert.getModelList()) {
				modelIds.add(model.getModel_id());
			}
		}

		// 写入数据库
		for (ModelEntity model : insert.getModelList()) {
			insert.setTarget_model_id(model.getModel_id());
			mapper.createPcsRequest(insert);
			String lastInsertID = commonMapper.getLastInsertID();

			for (String modelId : modelIds) {
				mapper.setReactedModels(lastInsertID, modelId);
			}

			String oldTargetFile = null;
			if(CHANGE_MEANS_EDIT.equals(""+insert.getChange_means())
					|| CHANGE_MEANS_REFRESH.equals(""+insert.getChange_means())) {
				oldTargetFile = PcsUtils.getFileName(path, model, conn);
				// if(oldTargetFile!= null) oldTargetFile = oldTargetFile.replaceAll("\\.html$", ".xls");
			}
			String savePath = PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "\\_request\\" + lastInsertID;
			writeFile(uploadfile, savePath, oldTargetFile);
		}

		// 清除会话
		session.removeAttribute(SESSION_ENTITY);
		session.removeAttribute(SESSION_FILE);
	}

	private void writeFile(FormFile uploadfile, String savePath, String oldTargetFile) {
		FileOutputStream fileOutput;
		File fSavePPath = new File(savePath);
		if (!fSavePPath.exists()) {
			fSavePPath.mkdirs();
		}
		fSavePPath = null;
		try {
			fileOutput = new FileOutputStream(savePath + "\\new.xls");
			fileOutput.write(uploadfile.getFileData());
			fileOutput.flush();
			fileOutput.close();
			PcsUtils.convert2Page(savePath, savePath + "\\new.xls");

			if (oldTargetFile != null) {
				FileUtils.copyFile(oldTargetFile.replaceAll("\\\\xml\\\\", "\\\\excel\\\\").replaceAll("\\.html$", ".xls"),
						savePath + "\\old.xls");
				FileUtils.copyFile(oldTargetFile,
						savePath + "\\old.html");
				// PcsUtils.convert2Page(savePath, savePath + "\\old.xls");
			}
		} catch (FileNotFoundException e) {
			_logger.error("FileNotFound:" + e.getMessage());
		} catch (IOException e) {
			_logger.error("IO:" + e.getMessage());
		} finally {
			fileOutput=null;
		}
	}

	/***
	 * 业务检查
	 * @param form
	 * @param parameterMap
	 * @param httpSession 
	 * @param msgErrors
	 */
	public void customValidate(ActionForm form, Map<String, String[]> parameterMap, 
			HttpSession httpSession, List<MsgInfo> msgErrors, List<MsgInfo> msgInfoes
			, Map<String, Object> lResponseResult) {
		PcsRequestForm testForm = (PcsRequestForm) form;
		PcsRequestEntity insert = new PcsRequestEntity();

		if (testForm.getOrg_file_name() != null) {
			insert = (PcsRequestEntity) httpSession.getAttribute(SESSION_ENTITY);
			insert.setOrg_file_name(testForm.getOrg_file_name());
			httpSession.setAttribute(SESSION_ENTITY, insert);
			return;
		}

		// 文件Map
		String pathId = testForm.getLine_type();
		Map<String, String> folderTypes = PcsUtils.getFolderTypes();
		String path = folderTypes.get(pathId);

		// 设定对应工程
		testForm.setLine_id(testForm.getLine_type().substring(0, 2));
//			case "检查卡" : return "19";
//			case "外科硬镜修理工程" : return "20";
//			case "检查工程" : return "50";
//			case "出荷检查表" : return "51";
		switch(testForm.getLine_type()) {
		case "19" : testForm.setLine_id("70");break;
		case "20" : testForm.setLine_id("50");break;
		case "50" : testForm.setLine_id("101");break;
		case "51" : testForm.setLine_id("76");break;
		}

		// 上传的文件
		FormFile file = testForm.getFile();
		String fileName = "";
		if (file == null || CommonStringUtil.isEmpty(file.getFileName())) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("file.notExist");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
			msgErrors.add(error);
		} else {
			fileName = cropExt(file.getFileName());
			testForm.setFile_name(fileName);
		}

		BeanUtil.copyToBean(testForm, insert, CopyOptions.COPYOPTIONS_NOEMPTY);

		// 对应型号
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		List<ModelEntity> models = new AutofillArrayList<ModelEntity>(ModelEntity.class);
		// 整理提交数据取得对应的
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("models".equals(entity)) {
					String column = m.group(2);

					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("id".equals(column)) {
						int index = Integer.parseInt(m.group(3));
						models.get(index).setModel_id(value[0]);
					} else
					if ("name".equals(column)) {
						int index = Integer.parseInt(m.group(3));
						models.get(index).setName(value[0]);
					}
				}
			}
		}
		if (models.isEmpty()) {
			MsgInfo error = new MsgInfo();
			error.setErrcode("validator.required.multidetail");
			error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required.multidetail", "型号"));
			msgErrors.add(error);
			return;
		} else {
			insert.setModelList(models);
		}

		// 如果是变更,假如源文件不存在,要求选择对应
		if (CHANGE_MEANS_EDIT.equals(testForm.getChange_means())
				|| CHANGE_MEANS_REFRESH.equals(""+insert.getChange_means())) {

			if (path != null) {
				path = PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "\\excel\\" +  path.replaceAll("\n", "/");
				File nowTemplate = new File(path + "/" + testForm.getFile_name() + ".xls");
				if (!nowTemplate.exists()) {
					MsgInfo info = new MsgInfo();
					info.setErrcode("file.notExist");
					info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
					msgInfoes.add(info);

					String testSuffix = fileName;
					if (testSuffix.length() > 12) {
						testSuffix = testSuffix.substring(0, testSuffix.length() / 2);
					}
					// 提供文件列表
					ArrayList<String> files = new ArrayList<String>();
					File listPath = new File(path);
					if (listPath.isDirectory()) {
						for (File tfile : listPath.listFiles()) {
							String listFileName = tfile.getName();
							if (listFileName.startsWith(testSuffix)) {
								files.add(0, cropExt(listFileName));
							} else {
								files.add(cropExt(listFileName));
							}
						}
					}
					lResponseResult.put("orgFiles", files);
				} else {
					insert.setOrg_file_name(insert.getFile_name());
				}
			}
		}
		httpSession.setAttribute(SESSION_ENTITY, insert);
		httpSession.setAttribute(SESSION_FILE, file);

		return;
	}

	private String cropExt(String filename) {
		if (isEmpty(filename)) {
			return "";
		}
		String[] toks = filename.split("\\.");
		if (toks.length < 2) {
			return filename;
		} else {
			return filename.replaceAll("\\." + toks[toks.length - 1], "");
		}
	}

	public void getCompare(String pcs_request_key,
			Map<String, Object> lResponseResult, SqlSession conn) {
		String pcsNewHtml = "NEWNEWNEWNENWNEWN";
		String pcsOldHtml = "OLDODLODLODLDOODL";
		PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);
		PcsRequestEntity entity = mapper.getPcsRequest(pcs_request_key);

		String savePath = PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "\\_request\\" + trimZero(pcs_request_key);

		Map<String, String> setNew = new HashMap<String, String>();
		Map<String, String> setOld = new HashMap<String, String>();
		setNew.put("新模板", getXmlContent(savePath + "\\new.html"));
		setOld.put("原模板", getXmlContent(savePath + "\\old.html"));

		Map<String, String> newMap = PcsUtils.toHtmlBlank(setNew, entity.getTarget_model_name());
		Map<String, String> oldMap = PcsUtils.toHtmlBlank(setOld, entity.getTarget_model_name());

		pcsNewHtml = newMap.get("新模板");
		pcsOldHtml = oldMap.get("原模板");

		lResponseResult.put("pcs_content_new", pcsNewHtml);
		lResponseResult.put("pcs_content_old", pcsOldHtml);
	}

	public void getTest(String pcs_request_key,
			Map<String, Object> lResponseResult, SqlSession conn) {
		PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);
		PcsRequestEntity entity = mapper.getPcsRequest(pcs_request_key);

		String savePath = PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "\\_request\\" + trimZero(pcs_request_key);

		String newContent = PcsUtils.toHtmlTest(getXmlContent(savePath + "\\new.html"), entity.getTarget_model_name());

		lResponseResult.put("pcs_content_test", newContent);

		String line_id = entity.getLine_id();
		// 借用Line_id作为operator_id, Line_name作为job_no
		List<PositionEntity> pentities = mapper.getTestOflines(line_id);
		if ("00000000019".equals(line_id)) { // TODO
		}
		if ("00000000101".equals(line_id)) { // TODO
			pentities.addAll(mapper.getTestOflines("00000000102"));
		}
		for (PositionEntity pentity : pentities) {
			if (pentity.getLine_id() == null) {
				// 无指定主工位
				OperatorMapper oMapper = conn.getMapper(OperatorMapper.class);
				List<OperatorEntity> lOperators = oMapper.getOperatorWithPosition(pentity.getPosition_id());
				if (lOperators != null && lOperators.size() > 0) {
					pentity.setLine_id(lOperators.get(0).getOperator_id());
					pentity.setLine_name(lOperators.get(0).getJob_no());
				}
			}
		}
		lResponseResult.put("pentities", pentities);
	}

	public String makeTest(HttpServletRequest req, SqlSession conn) {
		PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);
		String key = req.getParameter("pcs_request_key");
		PcsRequestEntity entity = mapper.getPcsRequest(key);

		Date now = new Date();
		String dateFill = DateUtil.toString(now, "MM-dd");

		String templatePath = PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "\\_request\\" + trimZero(key) + "\\new.xls";

		String cacheFile = "pcsTest" + now.getTime();
		String cacheXlsPath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(now, "yyyyMM") + "\\"
				+ cacheFile + ".xls";
		String cachePdfPath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(now, "yyyyMM") + "\\"
				+ cacheFile + ".pdf";
		FileUtils.copyFile(templatePath, cacheXlsPath);

		XlsUtil xls = null;
		try {
			xls = new XlsUtil(cacheXlsPath);
			xls.SelectActiveSheet();

			// GS
			xls.Replace("@#GS???????", "(修理单号)");
			// GM
			xls.Replace("@#GM???????", entity.getTarget_model_name());
			// GC
			xls.Replace("@#GC???????", "(机身号)");
			// GR
			xls.Replace("@#GR???????", "(维修等级)");

			String mapPc2Jn = req.getParameter("mapPc2Jn");
			String sPcs_inputs = req.getParameter("pcs_inputs");
			String sPcs_comments = req.getParameter("pcs_comments");

			@SuppressWarnings("unchecked")
			Map<String, String> jsonMapPc2Jn = JSON.decode(mapPc2Jn, Map.class);
			@SuppressWarnings("unchecked")
			Map<String, String> jsonPcs_inputs = JSON.decode(sPcs_inputs, Map.class);

			for (String pcid : jsonPcs_inputs.keySet()) {
				// 输入值
				String sInput = jsonPcs_inputs.get(pcid);
				// 类别
				char sIype = pcid.charAt(1);

				if (!"".equals(sInput)) {
					switch (sIype) {
					
					case 'I': {
						// 输入：I
						if (!CommonStringUtil.isEmpty(sInput)) {
							xls.Replace("@#"+pcid+"??", sInput);
						}
						break;
					}
					case 'R': {
						// 单选：R
						if (!CommonStringUtil.isEmpty(sInput)) {
							xls.Replace("@#"+pcid+sInput, CHECKED);
							xls.Replace("@#"+pcid+"??", UNCHECKED);
						}
						break;
					}
					case 'M': {
						// 合格确认：M
						if ("1".equals(sInput)) {
							xls.Replace("@#"+pcid+"??", CHECKED);
						} else if ("-1".equals(sInput)) {
							Dispatch cell = xls.Locate("@#"+pcid+"??");
							if (cell != null) {
								XlsUtil.SetCellBackGroundColor(cell, "255"); 
								Dispatch font = xls.GetCellFont(cell);
								Dispatch.put(font, "Color", "16777215");
							}
							xls.Replace("@#"+pcid+"??", FORBIDDEN);
						}
						xls.Replace("@#"+pcid+"??", NOCARE);
						break;
					}
					case 'N': {
						// 签章：N
						if ("1".equals(sInput)) {
							// 按钮
							Dispatch cell = xls.Locate("@#"+pcid+"??");
							if (cell != null) {
								String jobNo = jsonMapPc2Jn.get(pcid.substring(2, 5));
								if (jobNo == null)
									xls.Replace("@#"+pcid+"??", "无对应章");
								else
									xls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" 
										+ jobNo.toUpperCase(), cell);
							}
							xls.Replace("@#"+pcid+"??", ""); // sign
							xls.Replace("@#"+pcid.replaceAll("EN", "ED").replaceAll("LN", "LD")+"??", dateFill);
						} else if ("-1".equals(sInput)) { 
							// 不做
							// if 611
							if (pcid.indexOf("N611") >= 0) {
								Dispatch cell = xls.Locate("@#"+pcid+"??");
								if (cell != null)  XlsUtil.SetCellBackGroundColor(cell, "12566463"); // BFBFBF;
								cell = xls.Locate("@#"+pcid.replaceAll("EN", "ED").replaceAll("LN", "LD")+"??");
								if (cell != null) XlsUtil.SetCellBackGroundColor(cell, "12566463"); // BFBFBF;
							} else {
								xls.Replace("@#"+pcid+"??", NOCARE);
							}
							xls.Replace("@#"+pcid.replaceAll("EN", "ED").replaceAll("LN", "LD")+"??", dateFill);
						}
						break;
					}
					case 'T': {
						// 合格总集：T
						if ("1".equals(sInput)) {
							Dispatch cell = xls.Locate("@#"+pcid+"??");
							xls.SetValue(cell, "合格");
						} else if ("-1".equals(sInput)) { 
							Dispatch cell = xls.Locate("@#"+pcid+"??");
							xls.SetValue(cell, "不合格");
							XlsUtil.SetCellBackGroundColor(cell, "255"); 
							Dispatch font = xls.GetCellFont(cell);
							Dispatch.put(font, "Color", "16777215"); // FFFFFF
						}
						break;
					}
					}
				}
			}

			if (!CommonStringUtil.isEmpty(sPcs_comments)) {
				@SuppressWarnings("unchecked")
				Map<String, String> jsonPcs_comments = JSON.decode(sPcs_comments, Map.class);
				for (String commentskey : jsonPcs_comments.keySet()) {
					String sComment = jsonPcs_comments.get(commentskey);
					if (commentskey.endsWith("_container")) {
						commentskey = commentskey.replaceAll("_container", "");
					}
					xls.Replace("@#"+commentskey+"??", sComment + "\n@#"+commentskey+"00");
				}
			}

			// 清除没赋值的标签 并且 变灰 14.7.8 edit
			Dispatch cell = xls.Locate("@#?????????");
			String FoundValue = null;
			if (cell != null) FoundValue = Dispatch.get(cell, "Value").toString();
			while (FoundValue != null) {
				if (FoundValue.indexOf("@#EC") < 0 && FoundValue.indexOf("@#LC") < 0 && FoundValue.indexOf("@#GI") < 0) {
					XlsUtil.SetCellBackGroundColor(cell, "12566463"); // BFBFBF;
				}
				xls.SetValue(cell, FoundValue.replaceAll("@#\\w{2}\\d{7}", ""));
				cell = xls.Locate("@#?????????");
				if (cell == null) {
					FoundValue = null;
				} else {
					FoundValue = Dispatch.get(cell, "Value").toString();
				}
			}
			xls.SaveAsPdf(cachePdfPath);

		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			try {
				xls.Release();
			} catch (Exception e1) {
			}
		} finally {
			xls = null;
		}
		
		return cacheFile + ".pdf";
	}
	
	private String getXmlContent(String xmlfile) {
		BufferedReader input = null;
		try {
			input = new BufferedReader(new InputStreamReader(new FileInputStream(xmlfile),"UTF-8"));
			StringBuffer buffer = new StringBuffer();
			String text;

			while ((text = input.readLine()) != null)
				buffer.append(text);

//			System.out.println(buffer.length());
			String content = buffer.toString();
			return content;
		} catch (IOException ioException) {
			return null;
		} finally {
			if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
			}
			}
			input = null;
		}
	}

	public void remove(Map<String, String[]> parameterMap, SqlSessionManager conn) {
		// keys.pcs_request_key
		PcsRequestMapper prMapper = conn.getMapper(PcsRequestMapper.class);
		// 对应型号
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		// 整理提交数据取得对应的
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("keys".equals(entity)) {
					String column = m.group(2);

					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("pcs_request_key".equals(column)) {
						String pcs_request_key = value[0];
						prMapper.removePcsRequest(pcs_request_key);
					}
				}
			}
		}
		
	}

	/**
	 * 导入系统
	 * @param parameterMap
	 * @param user
	 * @param conn
	 */
	public void importToSystem(Map<String, String[]> parameterMap, LoginData user,
			SqlSessionManager conn) {
		// keys.pcs_request_key
		PcsRequestMapper prMapper = conn.getMapper(PcsRequestMapper.class);
		// 对应型号
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		List<String> lPcsRequestKeys = new ArrayList<String>();
		HashMap<String, String> sm = new HashMap<String, String>();
		List<HashMap<String, String>> lIgnoreMaterials = 
				new AutofillArrayList<HashMap<String, String>>((Class<HashMap<String, String>>) sm.getClass());

		// 整理提交数据取得对应的
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("keys".equals(entity)) {
					String column = m.group(2);

					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("pcs_request_key".equals(column)) {
						String pcs_request_key = value[0];
						PcsRequestEntity importor = new PcsRequestEntity();
						importor.setPcs_request_key(pcs_request_key);
						importor.setImporter_id(user.getOperator_id());
						prMapper.importPcsRequest(importor);
						lPcsRequestKeys.add(pcs_request_key);
					}
				} else if ("ignore".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));

					String[] value = parameterMap.get(parameterKey);
					if ("materiai_id".equals(column)) {
						lIgnoreMaterials.get(icounts).put("materiai_id", value[0]);
					} else if ("pcs_request_key".equals(column)) {
						lIgnoreMaterials.get(icounts).put("pcs_request_key", value[0]);
					}
				}
			}
		}		

		for (HashMap<String, String> lIgnoreMaterial : lIgnoreMaterials) {
			prMapper.setOldType(lIgnoreMaterial.get("pcs_request_key"), lIgnoreMaterial.get("materiai_id"));
		}

		// 覆盖文件
		for (String pcs_request_key : lPcsRequestKeys) {
			PcsRequestEntity entity = prMapper.getPcsRequest(pcs_request_key);

			// 临时目录
			String cachePath = PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "\\_request\\" + trimZero(pcs_request_key);

			// 得到对应的文件目录
			Map<String, String> folderTypes = PcsUtils.getFolderTypes();
			// 找到目标目录

			String locatePath = "";

			for (String key : folderTypes.keySet()) {
				String folderType = folderTypes.get(key);
				if (("" + entity.getLine_type()).equals(key)) { // TODO String
					locatePath = folderType;
					break;
				}
			}

			// 如果有旧文件则删除
			String targetPath = PcsUtils.getFilePath(locatePath);
			if (new File(cachePath + "\\old.xls").exists()) {
				String oldTargetFileXls = PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + 
						"\\excel\\" + targetPath + entity.getOrg_file_name() + ".xls";
				String oldTargetFileHtml = PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + 
						"\\xml\\" + targetPath + entity.getOrg_file_name() + ".html";
				boolean removed = new File(oldTargetFileXls).delete();
				if (removed == false) _logger.warn("没有删除文件权限");
				new File(oldTargetFileHtml).delete();
			}

			// 复制到目标目录
			if (new File(cachePath + "\\new.xls").exists()) {
				FileUtils.copyFile(cachePath + "\\new.xls", 
						PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "\\excel\\" + targetPath + entity.getFile_name() + ".xls");
				FileUtils.copyFile(cachePath + "\\new.html", 
						PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "\\xml\\" + targetPath + entity.getFile_name() + ".html");
			}

			// 重新读取文件关系
			PcsUtils.reset();
		}
	}

	/**
	 * 取得正在修理中的相关维修对象
	 * @param parameterMap
	 * @param conn
	 * @return
	 */
	public List<MaterialEntity> getWorkingMaterials(Map<String, String[]> parameterMap,
			SqlSession conn) {
		// keys.pcs_request_key
		PcsRequestMapper prMapper = conn.getMapper(PcsRequestMapper.class);
		// 对应型号
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		Map<String, MaterialEntity> oa = new TreeMap<String, MaterialEntity>();

		// 整理提交数据取得对应的
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("keys".equals(entity)) {
					String column = m.group(2);

					String[] value = parameterMap.get(parameterKey);

					if ("pcs_request_key".equals(column)) {
						String pcs_request_key = value[0];
						List<String> reactedModels = prMapper.getReactedModelsByKey(pcs_request_key);

						for (String model_id : reactedModels){
							List<MaterialEntity> mlist = prMapper.getWorkingByModel(model_id);
							for (MaterialEntity mEntity : mlist) {
								mEntity.setPat_id(pcs_request_key); // 借用pat_id存放维修对象相应的改修Key
								oa.put(mEntity.getMaterial_id(), mEntity);
							}
						}
					}
				}
			}
		}
		List<MaterialEntity> mlist = new ArrayList<MaterialEntity>();
		for (String material_id : oa.keySet()) {
			mlist.add(oa.get(material_id));
		}

		return mlist;
	}

	public PcsRequestForm getForEdit(String pcs_request_key, SqlSession conn) {
		PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);
		PcsRequestEntity entity = mapper.getPcsRequest(pcs_request_key);
		PcsRequestForm retForm = new PcsRequestForm();
		BeanUtil.copyToForm(entity, retForm, CopyOptions.COPYOPTIONS_NOEMPTY);
		return retForm;
	}

	/**
	 * 更新时检查
	 * @param form
	 * @param httpSession
	 * @param msgInfos
	 */
	public void customValidateEdit(ActionForm form,
			HttpSession httpSession,
			List<MsgInfo> msgErrors) {

		PcsRequestForm testForm = (PcsRequestForm) form;
		PcsRequestEntity update = new PcsRequestEntity();

		if (testForm.getOrg_file_name() != null) {
			update = (PcsRequestEntity) httpSession.getAttribute(SESSION_ENTITY);
			update.setOrg_file_name(testForm.getOrg_file_name());
			httpSession.setAttribute(SESSION_ENTITY, update);
			return;
		}

		// 上传的文件
		FormFile file = testForm.getFile();
		if (file == null) {
			httpSession.setAttribute(SESSION_FILE, null);
		} else {
			String fileName = "";
			if (file == null || CommonStringUtil.isEmpty(file.getFileName())) {
				MsgInfo error = new MsgInfo();
				error.setErrcode("file.notExist");
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("file.notExist"));
				msgErrors.add(error);
			} else {
				fileName = cropExt(file.getFileName());
				testForm.setFile_name(fileName);
			}

			httpSession.setAttribute(SESSION_FILE, file);
		}

		httpSession.setAttribute(SESSION_ENTITY, update);
	}

	/**
	 * 更新操作
	 * 
	 * @param form
	 * @param conn
	 */
	public void update(ActionForm form, HttpSession httpSession, SqlSessionManager conn) {
		PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);
		PcsRequestEntity update = new PcsRequestEntity();
		BeanUtil.copyToBean(form, update, CopyOptions.COPYOPTIONS_NOEMPTY);	
		mapper.updatePcsRequest(update);

		FormFile uploadfile = (FormFile) httpSession.getAttribute(SESSION_FILE);
		if (uploadfile != null) { // 更新new文件
			
			String savePath = PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "\\_request\\" 
					+ Integer.parseInt(update.getPcs_request_key(), 10);
			writeFile(uploadfile, savePath, null);

			// 清除会话
			httpSession.removeAttribute(SESSION_FILE);
		}

		httpSession.removeAttribute(SESSION_ENTITY);
	}


	private static String trimZero(String pcs_request_key) {
		if (pcs_request_key == null)
			return null;
		else return pcs_request_key.replaceAll("^0*([^0].*)$", "$1");
	}

	public List<PcsRequestForm> getFileList(ActionForm form, SqlSession conn) {
		PcsRequestEntity entity = new PcsRequestEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);	

		List<PcsRequestForm> fileList = new ArrayList<PcsRequestForm>();

		// 取得路径
		Integer lineType = entity.getLine_type();
		String sLineType = "" + lineType;
		String folder = "";

		Map<String, String> folderTypes = PcsUtils.getFolderTypes();
		String fr = folderTypes.get(sLineType);
		if (fr != null) {
			folder = fr.replaceAll("\n", "/");
		}

		PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);
		List<PcsInputLimitEntity> lCountLimit = mapper.countLimitByFileNameOfTypeCode(sLineType);

		// 取得路径下文档
		File filepath = new File(PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "//excel//" + folder);

		if (filepath.exists() && filepath.isDirectory()) {
			// 如果选定型号
			if (entity.getTarget_model_id() != null) {
				ModelMapper mdlMapper = conn.getMapper(ModelMapper.class);
				ModelEntity mb = mdlMapper.getModelByID(entity.getTarget_model_id());

				String fileName = PcsUtils.getFileName(PcsUtils.getFolderTypes().get(sLineType), mb);
				if (fileName != null) {
					if (entity.getFile_name() != null && fileName.indexOf(entity.getFile_name()) < 0) {
					} else {
						File file = new File(fileName);
						if (file.exists()) {
							PcsRequestForm fileForm = new PcsRequestForm();
							String dfileName = cropExt(file.getName());
							fileForm.setFile_name(dfileName + ".xls");
							fileForm.setImport_time(DateUtil.toString(new Date(file.lastModified()), DateUtil.DATE_PATTERN));

							dfileName = getFileNamePacked(dfileName);
							for (PcsInputLimitEntity count : lCountLimit) {
								if (count.getPacked_file_name().equals(dfileName)) {
									fileForm.setItems("" + count.getCnt());
									break;
								}
							}
							
							fileList.add(fileForm);
						}
					}
				}
			} else {
				Map<String, String> fileNameCountMap = new HashMap<String, String>(); 
				for (PcsInputLimitEntity count : lCountLimit) {
					fileNameCountMap.put(count.getPacked_file_name(), "" + count.getCnt());
				}

				for (File file : filepath.listFiles()) {
					if (!file.isFile()) continue;
					if (entity.getFile_name() != null && file.getName().indexOf(entity.getFile_name()) < 0) continue;

					PcsRequestForm fileForm = new PcsRequestForm();
					fileForm.setFile_name(file.getName());
					fileForm.setImport_time(DateUtil.toString(new Date(file.lastModified()), DateUtil.DATE_PATTERN));

					fileForm.setItems(fileNameCountMap.get(cropExt(file.getName())));
					fileList.add(fileForm);
				}
			}
		}

		// 取得分类下已配置

		
		return fileList;
	}

	/**
	 * 取得Html文件内容
	 * 
	 * @param form
	 * @param lResponseResult 
	 * @param conn
	 * @return
	 */
	public String getPcsInputs(ActionForm form, Map<String, Object> lResponseResult, SqlSession conn) {

		PcsRequestEntity entity = new PcsRequestEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);	

		// 取得路径
		Integer lineType = entity.getLine_type();
		String sLineType = "" + lineType;
		String folder = "";

		Map<String, String> folderTypes = PcsUtils.getFolderTypes();
		String fr = folderTypes.get(sLineType);
		if (fr != null) {
			folder = fr.replaceAll("\n", "/");
		}

		// 取得路径下文档
		String xmlFileName = cropExt(entity.getFile_name());
		lResponseResult.put("file_name", xmlFileName);
		File filepath = new File(PathConsts.BASE_PATH + PathConsts.PCS_TEMPLATE + "/xml/" + folder + "/" + xmlFileName + ".html");
		if (!filepath.exists()) return null;

		String packed = null;

		packed = getFileNamePacked(xmlFileName);
//		} catch (IOException ioe) {
//			return null;
//		}

		PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);
		PcsInputLimitEntity lentity = new PcsInputLimitEntity();
		lentity.setType_code("" + entity.getLine_type());
		lentity.setPacked_file_name(packed);
		List<PcsInputLimitEntity> tags = mapper.getLimitByFileName(lentity);

		String xmlContent = PcsUtils.getXmlContentStringFromFile(filepath);
		xmlContent = PcsUtils.getInputsContent(xmlContent, tags);

		return xmlContent;
	}

	private Map<String, String> packedMap = new HashMap<String, String>();
	private String getFileNamePacked(String xmlFileName) {
		if (xmlFileName.length() <= 127) return xmlFileName;

		if (packedMap.containsKey(xmlFileName)) return packedMap.get(xmlFileName);

		StringBuffer sb = new StringBuffer("");

		int charCombine = 0;

		for (int i=0; i < xmlFileName.length(); i++) {
			switch (i%3) {
			case 0: sb.append(xmlFileName.charAt(i)); break;
			case 1: charCombine = ((int) xmlFileName.charAt(i)) % 256; break;
			case 2: charCombine += ((int) xmlFileName.charAt(i)) % 256 * 256;
				sb.append((char) charCombine);
				break;
			}
		}

		if (sb.length() > 127) {
			return getFileNamePacked(sb.toString());
		}

		packedMap.put(xmlFileName, sb.toString());
		return sb.toString();
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//		GZIPOutputStream gos = new GZIPOutputStream(bos);
//		gos.write(xmlFileName.getBytes(RvsConsts.UTF_8));
//		return bos.toString(RvsConsts.UTF_8);
	}

	/**
	 * 更新输入限制
	 * 
	 * @param form
	 * @param parameterMap
	 * @param msgInfos
	 * @param conn
	 * @throws IOException 
	 */
	public void setPcsInputLimits(ActionForm form,
			Map<String, String[]> parameterMap, List<MsgInfo> msgInfos,
			SqlSessionManager conn) throws IOException {

		PcsRequestEntity entity = new PcsRequestEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);	

		List<PcsInputLimitEntity> pcsInputLimitEntities = new AutofillArrayList<PcsInputLimitEntity>(PcsInputLimitEntity.class);
		Pattern p = Pattern.compile("(\\w+)\\[(\\d+)\\].(\\w+)");

		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String group = m.group(1);
				if ("pil_tag".equals(group)) {
					String column = m.group(3);
					int icounts = Integer.parseInt(m.group(2));
					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("tag_code".equals(column)) {
						pcsInputLimitEntities.get(icounts).setTag_code(value[0]);
					} else if ("lower_limit".equals(column)) {
						if (!isEmpty(value[0])) {
							pcsInputLimitEntities.get(icounts).setLower_limit(new BigDecimal(value[0]));
						}
					} else if ("upper_limit".equals(column)) {
						if (!isEmpty(value[0])) {
							pcsInputLimitEntities.get(icounts).setUpper_limit(new BigDecimal(value[0]));
						}
					} else if ("allow_pass".equals(column)) {
						if ("0".equals(value[0])) {
							pcsInputLimitEntities.get(icounts).setAllow_pass(false);
						} else {
							pcsInputLimitEntities.get(icounts).setAllow_pass(true);
						}
					}
				}
			}
		}

		// 取得路径
		Integer lineType = entity.getLine_type();
		String sLineType = "" + lineType;

		String packed = null;
		packed = getFileNamePacked(entity.getFile_name());

		try {
			PcsRequestMapper mapper = conn.getMapper(PcsRequestMapper.class);
			PcsInputLimitEntity dentity = new PcsInputLimitEntity();
			dentity.setType_code(sLineType);
			dentity.setPacked_file_name(packed);
			mapper.deleteLimitByFileName(dentity);

			for (PcsInputLimitEntity lentity : pcsInputLimitEntities) {
				lentity.setType_code(sLineType);
				lentity.setPacked_file_name(packed);
				mapper.insertLimit(lentity);
			}
		} catch(Exception e) {
			MsgInfo info = new MsgInfo();
			info.setErrmsg("更新失败，产生信息" + e.getMessage() + "，请检查原文件文档的标签是否有异常。");
			msgInfos.add(info);
			conn.rollback();
		}

	}
}
