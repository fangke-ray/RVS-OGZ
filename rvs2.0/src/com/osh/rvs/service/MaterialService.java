package com.osh.rvs.service;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.arnx.jsonic.JSON;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.LoginData;
import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.PostMessageEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.inline.MaterialFactEntity;
import com.osh.rvs.bean.master.PcsFixOrderEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.common.FseBridgeUtil;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.PcsUtils;
import com.osh.rvs.common.ReportUtils;
import com.osh.rvs.common.ReverseResolution;
import com.osh.rvs.common.RvsConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.common.ZipUtility;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.form.data.MonthFilesDownloadForm;
import com.osh.rvs.form.master.ModelForm;
import com.osh.rvs.mapper.CommonMapper;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.data.PostMessageMapper;
import com.osh.rvs.mapper.inline.LeaderPcsInputMapper;
import com.osh.rvs.mapper.inline.MaterialCommentMapper;
import com.osh.rvs.mapper.inline.ProductionFeatureMapper;
import com.osh.rvs.mapper.manage.PcsFixOrderMapper;
import com.osh.rvs.mapper.master.ModelMapper;
import com.osh.rvs.mapper.master.PositionMapper;
import com.osh.rvs.mapper.qf.AcceptanceMapper;
import com.osh.rvs.mapper.qf.MaterialFactMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.FileUtils;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;
import framework.huiqing.common.util.validator.Validators;

public class MaterialService {

	private static final Integer RESOLVED = 2;

	private static Logger logger = Logger.getLogger("ProcessCheckSheet");

	/**
	 * 维修对象条件查询
	 * @param form
	 * @param completed
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<MaterialForm> searchMaterial(ActionForm form, String completed, SqlSession conn, List<MsgInfo> errors) {
		MaterialEntity conditionBean = new MaterialEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

		conditionBean.setFind_history(completed);
		List<MaterialEntity> lResultBean = new ArrayList<MaterialEntity>();
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);

		Integer processType = conditionBean.getFix_type();

//		if (!"".equals(conditionBean.getScheduled_date_start()) || !"".equals(conditionBean.getScheduled_date_end())) {
//			List<String> materialIds = dao.searchMaterialIds(conditionBean);
//			List<String> processIds = dao.searchMaterialProcessIds(conditionBean);
//			materialIds.retainAll(processIds);
//			if (materialIds.size() != 0) {
//				lResultBean = dao.getMaterialDetail(materialIds);
//			}
//		} else {
		if (processType == null) {
			lResultBean = dao.searchMaterial(conditionBean);
		} else if (processType == RvsConsts.PROCESS_TYPE_MANUFACT_LINE) {
			lResultBean = dao.searchProduction(conditionBean);
		} else if (processType.equals(RvsConsts.PROCESS_TYPE_ALL)) {
			lResultBean = dao.searchMaterial(conditionBean);
			lResultBean.addAll(dao.searchProduction(conditionBean));
		}
//		}

		List<MaterialForm> lResultForm = new ArrayList<MaterialForm>();
		
		CopyOptions cos = new CopyOptions();
		cos.excludeEmptyString();
		cos.excludeNull();
		cos.dateConverter(DateUtil.ISO_DATE_PATTERN, "reception_time", "agreed_date", "scheduled_date", "scheduled_date_end", "outline_time", "partial_order_date", "arrival_plan_date");
		BeanUtil.copyToFormList(lResultBean, lResultForm, cos, MaterialForm.class);
		
		return lResultForm;
	}

	/**
	 * 维修对象条件查询（已完成）
	 * @param form
	 * @param completed
	 * @param conn
	 * @param errors
	 * @return
	 */
	public List<MaterialForm> searchMaterialFiling(ActionForm form, Integer department, String completed, SqlSession conn, List<MsgInfo> errors) {
		MaterialEntity conditionBean = new MaterialEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

		conditionBean.setFind_history(completed);
		conditionBean.setDepartment(department);
		List<MaterialEntity> lResultBean = new ArrayList<MaterialEntity>();
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
//		if (!"".equals(conditionBean.getScheduled_date_start()) || !"".equals(conditionBean.getScheduled_date_end())) {
//			List<String> materialIds = dao.searchMaterialIds(conditionBean);
//			List<String> processIds = dao.searchMaterialProcessIds(conditionBean);
//			materialIds.retainAll(processIds);
//			if (materialIds.size() != 0) {
//				lResultBean = dao.getMaterialDetail(materialIds);
//			}
//		} else {
			lResultBean = dao.searchMaterialFiling(conditionBean);
//		}

		List<MaterialForm> lResultForm = new ArrayList<MaterialForm>();

		for (MaterialEntity resultBean : lResultBean) {
			MaterialForm result = new MaterialForm();
			// 判断归档文件是否真的存在
			BeanUtil.copyToForm(resultBean, result, CopyOptions.COPYOPTIONS_NOEMPTY);
			String sorcNo = result.getSorc_no();
			String subPath = "";
			if (sorcNo== null) { // If Manuf
				if (result.getFinish_time() == null) {
					continue;
				}
				subPath = "MA" + result.getModel_name().substring(0, 2) + "-" + result.getFinish_time().substring(1, 3) + result.getSerial_no() + "________";
//				subPath = "SAPD-" + sorcNo + "________";
			} else if (sorcNo.length() == 8)
				subPath = "OMRN-" + sorcNo + "________";
			else 
				subPath = sorcNo;
			String sub8 = subPath.substring(0, 8);

			String packFilename = sorcNo;
			if (packFilename == null) packFilename = result.getSerial_no();

			String folderPath = PathConsts.BASE_PATH + PathConsts.PCS + "\\" + sub8 + "\\" + packFilename + ".zip";
			if (new File(folderPath).exists()) {
				result.setIsHistory("true");
			} else if (subPath.startsWith("SAPD-")) {
				subPath = "CELL-" + sorcNo + "________";
				sub8 = subPath.substring(0, 8);
				folderPath = PathConsts.BASE_PATH + PathConsts.PCS + "\\" + sub8 + "\\" + sorcNo + ".zip";
				if (new File(folderPath).exists()) {
					result.setIsHistory("true");
				}
			}
			lResultForm.add(result);
		}

		return lResultForm;
	}

	/**
	 * 读取维修对象详细信息
	 * @param conn
	 * @param id 维修对象ID
	 * @return
	 */
	public MaterialForm loadMaterialDetail(SqlSession conn, String id) {
		
		MaterialForm form = new MaterialForm();
		
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		MaterialEntity entity = dao.loadMaterialDetail(id);
		if (entity != null) {
			CopyOptions cos = new CopyOptions();
			cos.excludeEmptyString();
			cos.excludeNull();
			cos.dateConverter(DateUtil.DATE_PATTERN, "reception_time");
			BeanUtil.copyToForm(entity, form, cos);
		}
		
		return form;
	}

	/**
	 * 读取维修对象详细信息/基本信息
	 * @param conn
	 * @param id 维修对象ID
	 * @return
	 */
	public MaterialForm loadSimpleMaterialDetail(SqlSession conn, String id) {
		MaterialForm form = new MaterialForm();
		
		MaterialEntity entity = loadSimpleMaterialDetailEntity(conn, id);
		if (entity != null) {
			CopyOptions cos = new CopyOptions();
			cos.excludeEmptyString();
			cos.excludeNull();
			cos.dateConverter(DateUtil.DATE_PATTERN, "reception_time");
			BeanUtil.copyToForm(entity, form, cos);
		}
		
		return form;
	}
	public MaterialEntity loadSimpleMaterialDetailEntity(SqlSession conn, String id) {
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		MaterialEntity entity = dao.getMaterialNamedEntityByKey(id);
		return entity;
	}

	/**
	 * 读取维修对象详细信息
	 * @param conn
	 * @param id 维修对象ID
	 * @return
	 */
	public MaterialEntity loadMaterialDetailBean(SqlSession conn, String id) {
		
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		MaterialEntity entity = dao.loadMaterialDetailAccpetance(id);
		
		return entity;
	}

	/**
	 * 读取维修对象详细信息
	 * @param conn
	 * @param ids 维修对象ID(复数)
	 * @return
	 */
	public List<MaterialEntity> loadMaterialDetailBeans(List<String> ids, SqlSession conn) {
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		return dao.getMaterialDetailTicket(ids);
	}

	/**
	 * 更新维修对象信息
	 * @param form 维修对象信息
	 * @param conn
	 * @return
	 */
	public void update(ActionForm form, SqlSession conn) {
		MaterialEntity conditionBean = new MaterialEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		dao.updateMaterial(conditionBean);
		// FSE 数据同步
		try{
			FseBridgeUtil.toUpdateMaterial(conditionBean.getMaterial_id(), "plan");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateMaterialScheduledDate(ActionForm form, SqlSession conn) {
		MaterialEntity conditionBean = new MaterialEntity();
		BeanUtil.copyToBean(form, conditionBean, null);
		conditionBean.setScheduled_date(conditionBean.getScheduled_date_end());
		
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		dao.updateMaterialScheduledDate(conditionBean);
	}

	/**
	 * 新建维修对象信息
	 * @param form 维修对象信息
	 * @param conn
	 */
	public void insert(ActionForm form, SqlSession conn) {
		MaterialEntity insertBean = new MaterialEntity();
		BeanUtil.copyToBean(form, insertBean, null);
		
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		dao.insertMaterial(insertBean);
	}

	/**
	 * 新建产品
	 * @param section_id 
	 * @param form 产品信息
	 * @param conn
	 * @return 
	 */
	public String insertProduct(MaterialEntity insertBean, String section_id, SqlSession conn) {
		if (insertBean.getModel_id() == null) {
			String model_id = ReverseResolution.getModelByName(insertBean.getModel_name(), conn);
			insertBean.setModel_id(model_id);
		}
		insertBean.setSection_id(section_id);
		insertBean.setLevel(0);

		MaterialMapper mMapper = conn.getMapper(MaterialMapper.class);
		mMapper.insertMaterial(insertBean);

		CommonMapper cMapper = conn.getMapper(CommonMapper.class);
		return cMapper.getLastInsertID();
	}

	/**
	 * 检查修理单号是否重复
	 * @param form
	 * @param conn
	 * @return
	 */
	public String checkSorcNo(ActionForm form, SqlSession conn) {
		MaterialEntity insertBean = new MaterialEntity();
		BeanUtil.copyToBean(form, insertBean, CopyOptions.COPYOPTIONS_NOEMPTY);
		if (insertBean.getSorc_no() != null) {
			AcceptanceMapper dao = conn.getMapper(AcceptanceMapper.class);
			String id = dao.checkSorcNo(insertBean);
			
			return id;
		}
		return null;
	}

	/**
	 * 检查机身号是否重复
	 * @param form
	 * @param conn
	 * @return
	 */
	public String checkModelSerialNo(ActionForm form, SqlSession conn) {
		MaterialEntity insertBean = new MaterialEntity();
		BeanUtil.copyToBean(form, insertBean, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		AcceptanceMapper dao = conn.getMapper(AcceptanceMapper.class);
		String id = dao.checkModelSerialNo(insertBean);
		
		return id;
	}

	/**
	 * 检查维修对象编号重复
	 * @param id
	 * @param form
	 * @param conn
	 * @param errors
	 */
	public void checkRepeatNo(String id, ActionForm form, SqlSessionManager conn, List<MsgInfo> errors) {
		MaterialForm mForm = (MaterialForm)form;

		if ("".equals(id) || id == null) { //新增时判断已存在ID
			String existId1 = null;
			String existId3 = null;
			if (!isEmpty(mForm.getSorc_no())) {
				existId1 = checkSorcNo(form, conn);
			}

			existId3 = checkModelSerialNo(form, conn);
			
			if (existId1 != null) {
				MsgInfo info = new MsgInfo();
				info.setErrcode("dbaccess.columnNotUnique");
				info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "修理单号", mForm.getSorc_no(), "维修对象"));
				errors.add(info);
			}  else if (existId3 != null) {
				MsgInfo info = new MsgInfo();
				info.setErrcode("dbaccess.columnNotUnique");
				info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "型号 + 机身号", mForm.getModel_name() +","+ mForm.getSerial_no(), "维修对象"));
				errors.add(info);
			}
		} else { //更新时判断ID不相等
			String existId1 = checkSorcNo(form, conn);
			String existId3 = checkModelSerialNo(form, conn);

			if (existId1 != null && !id.equals(existId1)) {
				MsgInfo info = new MsgInfo();
				info.setErrcode("dbaccess.columnNotUnique");
				info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "修理单号", mForm.getSorc_no(), "维修对象"));
				errors.add(info);
			} else if (existId3 != null && !id.equals(existId3)) {
				MsgInfo info = new MsgInfo();
				info.setErrcode("dbaccess.columnNotUnique");
				info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.columnNotUnique", "型号 + 机身号", mForm.getModel_name() +","+ mForm.getSerial_no(), "维修对象"));
				errors.add(info);
			}
		}
	}

	/***
	 * 取得提交维修对象ID(复数)
	 * @param parameterMap
	 * @return
	 */
	public List<String> getIds(Map<String, String[]> parameterMap) {
		List<String> rets = new ArrayList<String>();
		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");

		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("materials".equals(entity)) {
					String column = m.group(2);

					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("material_id".equals(column)) {
						rets.add(value[0]);
					}
				}
			}
		}
		return rets;
	}

	/**
	 * 取得维修对象工程检查票
	 * @param listResponse 相应信息
	 * @param mform 维修对象信息
	 * @param material_id 维修对象ID
	 * @param sline_id 查看工程
	 * @param isLeader 线长身份
	 * @param conn
	 */
	public void getPcses(Map<String, Object> listResponse, MaterialForm mform, String material_id, String sline_id, boolean isLeader, 
			String getHistory, SqlSession conn) {

		List<Map<String, String>> pcses = new ArrayList<Map<String, String>>();

		// 完成后的修改权限
		int ext = 0;

		String[] showLines = {};
		String unpassCode = "xyz";

		if ("00000000016".equals(mform.getCategory_id())) {
			if (!isEmpty(mform.getOutline_time())) {
				showLines = new String[2];
				showLines[0] = "最终检验";
				showLines[1] = "外科硬镜修理工程";
				ext = 1;
			} else if ("00000000015".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "最终检验";
				showLines[1] = "外科硬镜修理工程";
			} else {
				showLines = new String[1];
				showLines[0] = "外科硬镜修理工程";
			}
		} else if (mform.getLevel() != null && mform.getLevel().startsWith("5")) {
			showLines = new String[1];
			showLines[0] = "检查卡";
		} else if ("0".equals(mform.getLevel())) { // 制品
			if ("00000000076".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "出荷检查表";
				showLines[1] = "检查工程";
			} else {
				showLines = new String[1];
				showLines[0] = "检查工程";
			}
			unpassCode = "0\\d{2}";
		} else {
			if (!isEmpty(mform.getOutline_time())) {
				showLines = new String[4];
				showLines[0] = "分解工程";
				showLines[1] = "NS 工程";
				showLines[2] = "总组工程";
				showLines[3] = "最终检验";
				ext = 2;
				if ("00000000012".equals(sline_id)) {
					ext = 0;
				} else if ("00000000013".equals(sline_id)) {
					ext = 1;
				} else if ("00000000014".equals(sline_id)) {
					ext = 2;
				} else if ("00000000015".equals(sline_id)) {
					ext = 3;
				}
			} else if ("00000000012".equals(sline_id)) {
				showLines = new String[1];
				showLines[0] = "分解工程";
			} else if ("00000000013".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "NS 工程";
				showLines[1] = "分解工程";
			} else if ("00000000014".equals(sline_id)) {
				showLines = new String[3];
				showLines[0] = "总组工程";
				showLines[1] = "分解工程";
				showLines[2] = "NS 工程";
			} else if ("00000000015".equals(sline_id)) {
				showLines = new String[4];
				showLines[0] = "最终检验";
				showLines[1] = "分解工程";
				showLines[2] = "NS 工程";
				showLines[3] = "总组工程";
			} else if ("00000000054".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "总组工程";
				showLines[1] = "NS 工程";
			} else if ("00000000060".equals(sline_id)) {
				showLines = new String[1];
				showLines[0] = "分解工程";
			} else if ("00000000061".equals(sline_id)) {
				showLines = new String[2];
				showLines[0] = "总组工程";
				showLines[1] = "分解工程";
			}
		}

		for (int i=0 ; i < showLines.length ; i++) {
			String showLine = showLines[i]; 
			logger.info(showLine);
			Map<String, String> fileTempl = PcsUtils.getXmlContents(showLine, mform.getModel_name(), 
					null, getHistory != null, mform.getMaterial_id(),   
					conn);

			if ("NS 工程".equals(showLine)) filterSolo(fileTempl, material_id, mform.getLevel(), conn);
			if ("总组工程".equals(showLine)) filterLight(fileTempl, material_id, mform.getLevel(), conn);

			Map<String, String> fileHtml = PcsUtils.toHtml(fileTempl, material_id, mform.getSorc_no(),
					mform.getModel_name(), mform.getSerial_no(), mform.getLevel(), unpassCode, (i == ext && isLeader ? sline_id : null), conn);
			fileHtml = RvsUtils.reverseLinkedMap(fileHtml);
			pcses.add(fileHtml);
		}

		listResponse.put("pcses", pcses);
		
	}

	/**
	 * 取得维修对象工程检查票
	 * @param listResponse 相应信息
	 * @param mform 维修对象信息
	 * @param material_id 维修对象ID
	 * @param sline_id 查看工程
	 * @param isLeader 线长身份
	 * @param conn
	 */
	public void getPcsesBlank(Map<String, Object> callbackResponse, String modelName, SqlSession conn) {

		List<Map<String, String>> pcses = new ArrayList<Map<String, String>>();

		String[] showLines = new String[8];
		showLines[0] = "最终检验";
		showLines[1] = "分解工程";
		showLines[2] = "NS 工程";
		showLines[3] = "总组工程";
		showLines[4] = "外科硬镜修理工程";
		showLines[5] = "检查卡";
		showLines[6] = "出荷检查表";
		showLines[7] = "检查工程";

		for (int i=0 ; i < showLines.length ; i++) {
			String showLine = showLines[i]; 
			logger.info(showLine);
			Map<String, String> fileTempl = PcsUtils.getXmlContents(showLine, modelName, null, conn);

			Map<String, String> fileHtml = PcsUtils.toHtmlBlank(fileTempl, modelName);
			fileHtml = RvsUtils.reverseLinkedMap(fileHtml);
			pcses.add(fileHtml);
		}

		callbackResponse.put("pcses", pcses);
		
	}

	public void filterSolo(Map<String, String> fileTempl, String material_id, String level, SqlSession conn) {

		if (fileTempl == null) return;

		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);

		if (level != null && level.charAt(0) == '9') {
		} else if (!dao.checkLineDid(material_id, "00000000013")) {
			fileTempl.clear();
			return;
		}

		List<String> snouts = new ArrayList<String>(); 
		List<String> ccds = new ArrayList<String>(); 
		List<String> lgs = new ArrayList<String>(); 
		List<String> ccdls = new ArrayList<String>(); 

		for (String key : fileTempl.keySet()) {
			if (key.contains("先端预制")) {
				snouts.add(key);
			}
			else if (key.contains("CCD盖玻璃")) {
				ccds.add(key);
			}
			else if (key.contains("LG")) {
				lgs.add(key);
			}
			else if (key.contains("CCD线")) { // TODO
				ccdls.add(key);
			}
		}

		// 如果有先端预制工程检查票
		if (snouts.size() > 0) {
			// 检查是否做过301工位
			if (!dao.checkPositionDid(material_id, "00000000024", null, null)) {
				for (String snout : snouts) {
					fileTempl.remove(snout);
				}
			}
		}
		
		// 如果有CCD盖玻璃工程检查票
		if (ccds.size() > 0) {
			// 检查是否做过302工位
			if (!dao.checkPositionDid(material_id, "00000000025", null, null)) {
				for (String ccd : ccds) {
					fileTempl.remove(ccd);
				}
			}
		}

		// 如果有LG玻璃工程检查票
		if (lgs.size() > 0) {
			// 检查是否做过303工位
			if (!dao.checkPositionDid(material_id, "00000000060", null, null)) {
				for (String lg : lgs) {
					fileTempl.remove(lg);
				}
			}
		}

		// 如果有CCD线工程检查票
		if (ccdls.size() > 0) {
			// 检查是否做过304工位
			if (!dao.checkPositionDid(material_id, "00000000066", null, null)) {
				for (String ccdl : ccdls) {
					fileTempl.remove(ccdl);
				}
			}
		}
	}

	public static void filterLight(Map<String, String> fileTempl, String material_id, String level, SqlSession conn) {
		if (level != null && level.charAt(0) == '9') {
			return;
		}
		for (String key : fileTempl.keySet()) {
			if (key.contains("A 橡皮")) {
				fileTempl.remove(key);
				break;
			}
		}
	}

	public void saveLeaderInput(HttpServletRequest req, LoginData user, SqlSessionManager conn) throws Exception {
		saveLeaderInput(req, req.getParameter("material_id"), user, conn);
	}

	public void saveLeaderInput(HttpServletRequest req, String material_id, LoginData user, SqlSessionManager conn) throws Exception {
		// 取得最新rework在line_id
		ProductionFeatureMapper pfMapper = conn.getMapper(ProductionFeatureMapper.class);
		int lineMaxRework = pfMapper.getReworkCountWithLine(material_id, user.getLine_id());

		// 保存到线长工检票记录
		LeaderPcsInputMapper dao = conn.getMapper(LeaderPcsInputMapper.class);

		ProductionFeatureEntity pfBean = new ProductionFeatureEntity();
		pfBean.setMaterial_id(material_id);
		pfBean.setPcs_inputs(RvsUtils.setContentWithMemo(
				req.getParameter("pcs_inputs"), PcsUtils.PCS_INPUTS_SIZE, conn));
		pfBean.setPcs_comments(RvsUtils.setContentWithMemo(
				req.getParameter("pcs_comments"), PcsUtils.PCS_COMMENTS_SIZE, conn));
		pfBean.setOperator_id(user.getOperator_id());
		pfBean.setLine_id(user.getLine_id());
		pfBean.setRework(lineMaxRework);

		dao.insert(pfBean);
	}

	public void saveManagerInput(HttpServletRequest req, LoginData user, SqlSessionManager conn) throws Exception {
		String line_id = req.getParameter("line_id");
		if (line_id == null || line_id.equals("")) {
			line_id = "00000000014";
		}
		// 取得最新rework在line_id
		ProductionFeatureMapper pfMapper = conn.getMapper(ProductionFeatureMapper.class);
		int lineMaxRework = pfMapper.getReworkCountWithLine(req.getParameter("material_id"), line_id);

		// 保存到线长工检票记录
		LeaderPcsInputMapper dao = conn.getMapper(LeaderPcsInputMapper.class);

		ProductionFeatureEntity pfBean = new ProductionFeatureEntity();
		pfBean.setMaterial_id(req.getParameter("material_id"));
		pfBean.setPcs_inputs("{}");
		pfBean.setPcs_comments(RvsUtils.setContentWithMemo(
				req.getParameter("pcs_comments"), PcsUtils.PCS_COMMENTS_SIZE, conn));
		pfBean.setOperator_id(user.getOperator_id());
		pfBean.setLine_id(line_id);
		pfBean.setRework(lineMaxRework);

		dao.insert(pfBean);
	}

	public List<Map<String, String>> getLastPositionAndStatus(String material_id, SqlSession conn) {
		// 取得
		ProductionFeatureMapper dao = conn.getMapper(ProductionFeatureMapper.class);
		List<Map<String, String>> ret = dao.getLastPositionAndStatus(material_id);
		return ret;
	}


	public List<MsgInfo> checkQuotation(MaterialForm materialForm, SqlSessionManager conn) {
		Validators v = BeanUtil.createBeanValidators(materialForm, BeanUtil.CHECK_TYPE_ALL);
		v.delete("model_id");
		v.delete("serial_no");
//		v.add("sorc_no", v.required("修理单号"));
//		v.add("esas_no", v.required("ESAS NO."));
		// v3 Add Start
		v.add("ocm_rank", v.required("OCM 修理等级"));
		v.add("ocm_deliver_date", v.required("OCM 配送日"));
//		v.add("osh_deliver_date", v.required("OSH 配送日"));
		v.add("customer_name", v.required("顾客名"));
		// v3 Add End
		if (CommonStringUtil.isEmpty(materialForm.getWip_location())) {
			v.add("agreed_date", v.required("同意日"));
		}
		v.add("bound_out_ocm", v.required("返送地区"));
		v.add("area", v.required("销售大区"));
		v.delete("scheduled_expedited");
		return v.validate();
	}

	public void createReport(String fileFullPath, List<MaterialForm> lResultForm) {
		FileUtils.copyFile(PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\维修对象一览报表模板.xls", fileFullPath);
		
		POIFSFileSystem fs;
		HSSFWorkbook book = null;

		try {
			fs = new POIFSFileSystem(new FileInputStream(fileFullPath));
			book = new HSSFWorkbook(fs);

			Map<String, String> ocms = new HashMap<String, String>();
			Map<String, String> levels = new HashMap<String, String>();
			// 一览列表生成
			HSSFSheet listSheet = book.getSheetAt(0);
			HSSFRow row = null;

			HSSFFont fontYH = book.createFont();
			fontYH.setFontName("Microsoft YaHei");
			fontYH.setFontHeightInPoints((short) 10);

			HSSFCellStyle defaultCell = book.createCellStyle();
			defaultCell.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
			defaultCell.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
			defaultCell.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
			defaultCell.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
			defaultCell.setFont(fontYH);

			HSSFCellStyle highlightCell = book.createCellStyle(); // 亮色
			highlightCell.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
			highlightCell.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
			highlightCell.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
			highlightCell.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
			highlightCell.setFillForegroundColor(HSSFColor.AQUA.index);
			highlightCell.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
			highlightCell.setFont(fontYH);

			HSSFCellStyle centerCell = book.createCellStyle(); // 居中
			centerCell.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
			centerCell.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
			centerCell.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
			centerCell.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
			centerCell.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			centerCell.setFont(fontYH);

			listSheet.setDefaultColumnStyle(0, highlightCell);
			listSheet.setDefaultColumnStyle(1, defaultCell);
			listSheet.setDefaultColumnStyle(2, defaultCell);
			listSheet.setDefaultColumnStyle(3, defaultCell);
			listSheet.setDefaultColumnStyle(4, defaultCell);
			listSheet.setDefaultColumnStyle(5, defaultCell);
			listSheet.setDefaultColumnStyle(6, centerCell);
			listSheet.setDefaultColumnStyle(7, centerCell);
			listSheet.setDefaultColumnStyle(8, defaultCell);
			listSheet.setDefaultColumnStyle(9, defaultCell);
			listSheet.setDefaultColumnStyle(10, defaultCell);
			listSheet.setDefaultColumnStyle(11, defaultCell);
			listSheet.setDefaultColumnStyle(12, defaultCell);
			listSheet.setDefaultColumnStyle(13, defaultCell);
			listSheet.setDefaultColumnStyle(14, defaultCell);
			listSheet.setDefaultColumnStyle(15, defaultCell);
			listSheet.setDefaultColumnStyle(16, defaultCell);

			for (int i=0; i < lResultForm.size(); i++) {
				MaterialForm resultForm = lResultForm.get(i);
				row = listSheet.createRow(i+1);
				// int excelRowNum = row.getRowNum();
				HSSFCell cell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(i+1);

				cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue("'" + resultForm.getSorc_no());

				cell = row.createCell(2, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getModel_name());

				cell = row.createCell(3, HSSFCell.CELL_TYPE_STRING);
				String sLevel = resultForm.getLevel();
				if (!levels.containsKey(sLevel)) {
					levels.put(sLevel, CodeListUtils.getValue("material_level", sLevel, " - "));
				}
				sLevel = levels.get(sLevel);
				cell.setCellValue(sLevel);

				cell = row.createCell(4, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue("'" + resultForm.getSerial_no());

				cell = row.createCell(5, HSSFCell.CELL_TYPE_STRING);
				String sOcm = resultForm.getOcm();
				if (!ocms.containsKey(sOcm)) {
					ocms.put(sOcm, CodeListUtils.getValue("material_ocm", sOcm, " - "));
				}
				sOcm = ocms.get(sOcm);
				cell.setCellValue(sOcm);

				cell = row.createCell(6, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getSection_name());

				cell = row.createCell(7, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getProcessing_position());

				cell = row.createCell(8, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getProcessing_position2());

				cell = row.createCell(9, HSSFCell.CELL_TYPE_STRING);
				String sReceptionTime = resultForm.getReception_time();
				if (sReceptionTime != null && sReceptionTime.length() > 10)
					sReceptionTime = sReceptionTime.substring(0, 10);
				cell.setCellValue(sReceptionTime);

				cell = row.createCell(10, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getAgreed_date());

				cell = row.createCell(11, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getScheduled_date());

				cell = row.createCell(12, HSSFCell.CELL_TYPE_STRING);
				if("9999-12-31".equals(resultForm.getScheduled_date_end())){
					cell.setCellValue("另行通知");
				}else{
					cell.setCellValue(resultForm.getScheduled_date_end());
				}

				cell = row.createCell(13, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getOutline_time());

				cell = row.createCell(14, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getPartial_order_date());

				cell = row.createCell(15, HSSFCell.CELL_TYPE_STRING);
				if("9999-12-31".equals(resultForm.getArrival_plan_date())){
					cell.setCellValue("未定");
				}else{
					cell.setCellValue(resultForm.getArrival_plan_date());
				}

				cell = row.createCell(16, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getIs_late());
			}
			// 保存文件
			FileOutputStream fileOut = new FileOutputStream(fileFullPath);
			book.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void getPcses4Fix(Map<String, Object> listResponse, MaterialForm mform, String material_id, SqlSession conn) {
		List<Map<String, String>> pcses = new ArrayList<Map<String, String>>();

		String[] showLines = {};

		if ("00000000016".equals(mform.getCategory_id())) {
			showLines = new String[2];
			showLines[0] = "最终检验";
			showLines[1] = "外科硬镜修理工程";
		} else if (mform.getLevel() != null && mform.getLevel().startsWith("5")) {
			showLines = new String[1];
			showLines[0] = "检查卡";
		} else if ("0".equals(mform.getLevel())) { // 制品
			showLines = new String[2];
			showLines[0] = "出荷检查表";
			showLines[1] = "检查工程";
		} else {
			showLines = new String[4];
			showLines[0] = "最终检验";
			showLines[1] = "分解工程";
			showLines[2] = "NS 工程";
			showLines[3] = "总组工程";
		}

		for (int i=0 ; i < showLines.length ; i++) {
			String showLine = showLines[i]; 
			logger.info(showLine);
			Map<String, String> fileTempl = PcsUtils.getXmlContents(showLine, mform.getModel_name(), null, conn);

			if ("NS 工程".equals(showLine)) {
				filterSolo(fileTempl, material_id, mform.getLevel(), conn);
			}
			if ("总组工程".equals(showLine)) filterLight(fileTempl, material_id, mform.getLevel(), conn);

			Map<String, String> fileHtml = PcsUtils.toHtml4Fix(fileTempl, material_id, mform.getSorc_no(),
					mform.getModel_name(), mform.getSerial_no(), conn);
			fileHtml = RvsUtils.reverseLinkedMap(fileHtml);
			pcses.add(fileHtml);
		}

		listResponse.put("pcses", pcses);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void fixInput(HttpServletRequest req, LoginData user, SqlSessionManager conn) throws Exception {

		String material_id = req.getParameter("material_id");

		Map<String, String[]> parameterMap = req.getParameterMap();

		List<HashMap> fixBeans = new AutofillArrayList<HashMap>(HashMap.class);
		List<HashMap> fixCommentsBeans = new AutofillArrayList<HashMap>(HashMap.class);
		Pattern p = Pattern.compile("([\\w\\_]+)\\[(\\d+)\\]\\[(\\w+)\\]");

		// 整理提交数据
		for (String parameterKey : parameterMap.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("pcs_values".equals(entity)) {
					String column = m.group(3);
					int icounts = Integer.parseInt(m.group(2));
					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("name".equals(column)) {
						fixBeans.get(icounts).put("name", value[0]);
					} else if ("value".equals(column)) {
						fixBeans.get(icounts).put("value", value[0]);
					} else if ("jam_code".equals(column)) {
						fixBeans.get(icounts).put("jam_code", value[0]);
					}
				}
				else if ("pcs_comments".equals(entity)) {
					String column = m.group(3);
					int icounts = Integer.parseInt(m.group(2));
					String[] value = parameterMap.get(parameterKey);

					// TODO 全
					if ("name".equals(column)) {
						fixCommentsBeans.get(icounts).put("name", value[0]);
					} else if ("value".equals(column)) {
						fixCommentsBeans.get(icounts).put("value", value[0]);
					} else if ("jam_code".equals(column)) {
						fixCommentsBeans.get(icounts).put("jam_code", value[0]);
					}
				}
			}
		}

		ProductionFeatureMapper pfMapper = conn.getMapper(ProductionFeatureMapper.class);
		PcsFixOrderMapper pfoMapper = conn.getMapper(PcsFixOrderMapper.class);
		LeaderPcsInputMapper lpiMapper = conn.getMapper(LeaderPcsInputMapper.class);

		for (Map fixBean : fixBeans) {
			String jam_code = (String) fixBean.get("jam_code");
			if (jam_code == null) {
			} else if (jam_code.indexOf("_") >= 0) {
				// 操作人员输入
				ProductionFeatureEntity condition = PcsUtils.putJam_codeToPf(jam_code);
				condition.setMaterial_id(material_id);

				List<ProductionFeatureEntity> retEntities = pfMapper.searchProductionFeature(condition);
				if (retEntities.size() > 0) {
					ProductionFeatureEntity target = retEntities.get(0);
					String oldPcsInputs = target.getPcs_inputs();
					oldPcsInputs = RvsUtils.getContentWithMemo(oldPcsInputs, conn);

					Map<String, String> jsonPcsInputs = JSON.decode(oldPcsInputs, Map.class);
					jsonPcsInputs.put(fixBean.get("name").toString(), fixBean.get("value").toString());

					String newPcsInputs = JSON.encode(jsonPcsInputs);
					newPcsInputs = RvsUtils.setContentWithMemo(newPcsInputs, PcsUtils.PCS_INPUTS_SIZE, conn);
					target.setPcs_inputs(newPcsInputs);

					pfoMapper.updateInputs(target);
				}
			} else {
				// 线长输入
				ProductionFeatureEntity leaderPcsInput = lpiMapper.getLeaderPcsInputByKey(jam_code);
				if (leaderPcsInput != null) {
					String oldPcsInputs = leaderPcsInput.getPcs_inputs();
					oldPcsInputs = RvsUtils.getContentWithMemo(oldPcsInputs, conn);

					Map<String, String> jsonPcsInputs = JSON.decode(oldPcsInputs, Map.class);
					jsonPcsInputs.put(fixBean.get("name").toString(), fixBean.get("value").toString());

					String newPcsInputs = JSON.encode(jsonPcsInputs);
					newPcsInputs = RvsUtils.setContentWithMemo(newPcsInputs, PcsUtils.PCS_INPUTS_SIZE, conn);

					pfoMapper.updateInputsLeader(jam_code, newPcsInputs);
				}
			}
		}

		for (Map fixBean : fixCommentsBeans) {
			String jam_code = (String) fixBean.get("jam_code");
			if (jam_code == null) {
			} else if (jam_code.indexOf("_") >= 0) {
				// 操作人员输入
				ProductionFeatureEntity condition = PcsUtils.putJam_codeToPf(jam_code);
				condition.setMaterial_id(material_id);

				List<ProductionFeatureEntity> retEntities = pfMapper.searchProductionFeature(condition);
				if (retEntities.size() > 0) {
					ProductionFeatureEntity target = retEntities.get(0);
					String oldPcsComments = target.getPcs_comments();
					oldPcsComments = RvsUtils.getContentWithMemo(oldPcsComments, conn);

					Map<String, String> jsonPcsComments = JSON.decode(oldPcsComments, Map.class);
					jsonPcsComments.put(fixBean.get("name").toString(), fixBean.get("value").toString());

					String newPcsComments = JSON.encode(jsonPcsComments);
					newPcsComments = RvsUtils.setContentWithMemo(newPcsComments, PcsUtils.PCS_COMMENTS_SIZE.intValue(), conn);
					target.setPcs_comments(newPcsComments);

					pfoMapper.updateComments(target);
				}
			} else {
				// 线长输入
				ProductionFeatureEntity leaderPcsInput = lpiMapper.getLeaderPcsInputByKey(jam_code);
				if (leaderPcsInput != null) {
					String oldPcsComments = leaderPcsInput.getPcs_comments();
					oldPcsComments = RvsUtils.getContentWithMemo(oldPcsComments, conn);

					Map<String, String> jsonPcsComments = JSON.decode(oldPcsComments, Map.class);
					jsonPcsComments.put(fixBean.get("name").toString(), fixBean.get("value").toString());

					String newPcsComments = JSON.encode(jsonPcsComments);
					newPcsComments = RvsUtils.setContentWithMemo(newPcsComments, PcsUtils.PCS_COMMENTS_SIZE.intValue(), conn);

					pfoMapper.updateCommentsLeader(jam_code, newPcsComments);
				}
			}
		}

		fixResolver(req, user, conn);
	}

	public void fixResolver(HttpServletRequest req, LoginData user, SqlSessionManager conn) throws Exception {

		String material_id = req.getParameter("material_id");
		String pcs_fix_order_key = req.getParameter("pcs_fix_order_key");
		PcsFixOrderMapper pfoMapper = conn.getMapper(PcsFixOrderMapper.class);

		// 处理完毕
		PcsFixOrderEntity resolveBean = new PcsFixOrderEntity();
		resolveBean.setStatus(RESOLVED);
		resolveBean.setPcs_fix_order_key(pcs_fix_order_key);
		pfoMapper.resolvePcsFixOrder(resolveBean);

		// 如果维修对象已经完成，则重新生成工程检查票
		MaterialMapper dao = conn.getMapper(MaterialMapper.class);
		MaterialEntity mBean = dao.getMaterialEntityByKey(material_id);
		if (mBean.getOutline_time() != null) {
			try {
				URL url = new URL("http://localhost:8080/rvs/download.do?method=file&material_id=" + material_id);
				url.getQuery();
				URLConnection urlconn = url.openConnection();
				urlconn.setReadTimeout(1); // 不等返回
				urlconn.connect();
				urlconn.getContentType(); // 这个就能触发
			} catch (Exception e) {
				logger.error("Failed", e);
			}
		}

		// 信息推送
		PostMessageMapper pmMapper = conn.getMapper(PostMessageMapper.class);
		PostMessageEntity pmEntity = new PostMessageEntity();
		pmEntity.setSender_id(user.getOperator_id());
		pmEntity.setContent("你的工程检查票修改已处理，请确认！");
		pmEntity.setLevel(1);
		pmEntity.setReason(PostMessageService.PCS_FIX_COMPLETE);

		pmMapper.createPostMessage(pmEntity);

		CommonMapper commonMapper = conn.getMapper(CommonMapper.class);
		String lastInsertID = commonMapper.getLastInsertID();
		pmEntity.setPost_message_id(lastInsertID);

		// 回信给申请者
		PcsFixOrderEntity pcsFixOrder = pfoMapper.getPcsFixOrder(pcs_fix_order_key);
		if (pcsFixOrder != null && !isEmpty(pcsFixOrder.getSender_id())) {
			pmEntity.setReceiver_id(pcsFixOrder.getSender_id());
			pmMapper.createPostMessageSendation(pmEntity);
		}

	}

	public void calcOverTime(List<MaterialForm> lResultForm) {
		for (MaterialForm retForm: lResultForm) {
			// 如果纳期已经超过，标注
			String sOutline_time = retForm.getOutline_time();
			String sScheduled_date = retForm.getScheduled_date();

			if (!isEmpty(sOutline_time) && !isEmpty(sScheduled_date) && sOutline_time.compareTo(sScheduled_date) > 0 ) {
				retForm.setIs_late("纳期延误");
			}
		}
	}

	public String getPcsesBlankXls(String modelName, SqlSession conn) {
		String uuid = UUID.randomUUID().toString();
		Date today = new Date();
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM") + "\\" + uuid + "\\";

		String[] showLines = new String[8];
		showLines[0] = "检查卡";
		showLines[1] = "最终检验";
		showLines[2] = "分解工程";
		showLines[3] = "NS 工程";
		showLines[4] = "总组工程";
		showLines[5] = "外科硬镜修理工程";
		showLines[6] = "出荷检查表";
		showLines[7] = "检查工程";

		for (int i=0 ; i < showLines.length ; i++) {
			String showLine = showLines[i]; 
			logger.info(showLine);
			Map<String, String> fileTempl = PcsUtils.getXlsContents(showLine, modelName, null, null, false, conn);

			try {
				PcsUtils.toTemplatesXls(fileTempl, modelName, cachePath, conn);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		// 打包
		ZipUtility.zipper(cachePath, cachePath + "\\..\\" + uuid + ".zip", "UTF-8");
		return uuid + ".zip";
	}

	public void checkModelDepacy(MaterialForm materialForm,
			SqlSession conn, List<MsgInfo> errors) {
		ModelMapper mapper = conn.getMapper(ModelMapper.class);

		String level = materialForm.getLevel();
		String end_date = null;
		if ("1".equals(level) ||"2".equals(level)|| "3".equals(level)) {
			end_date = mapper.checkModelDepacy(materialForm.getModel_id(), materialForm.getLevel());
		} else {
			end_date = mapper.checkModelDepacy(materialForm.getModel_id(), null);
		}

		if (end_date != null) {
			MsgInfo error = new MsgInfo();
			error.setComponentid("model_id");
			error.setErrcode("model.notDepacy");
			if ("1".equals(level) ||"2".equals(level)|| "3".equals(level)) {
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("model.notDepacy", materialForm.getModel_name(), 
						materialForm.getLevelName(), end_date));
			} else {
				error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("model.notDepacy", materialForm.getModel_name(), 
						"全部等", end_date));
			}
			errors.add(error);
		}
		
	}

	/**
	 * 个个
	 * @param entity
	 * @param conn
	 */
	public void updateComment(MaterialEntity entity, SqlSessionManager conn) {
		MaterialMapper mapper = conn.getMapper(MaterialMapper.class);
	
		mapper.updateMaterialComment(entity);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public List<MonthFilesDownloadForm> getMonthFiles(){
		List<MonthFilesDownloadForm> monthFilesDownloadForms = new ArrayList<MonthFilesDownloadForm>();
		
		String filePath = PathConsts.BASE_PATH+PathConsts.PCS+"\\_monthly";
		File file = new File(filePath);
		
		MonthFilesDownloadForm monthFilesDownloadForm;
		//如果该文件是个目录的话
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(File f:files){
				monthFilesDownloadForm = new MonthFilesDownloadForm();
				if(f.isDirectory()){
					continue;
				}
				
				String fileName = f.getName();
				
				long fileSize = f.length();//文件的大小(字节)
				double ds = (double)fileSize/1024;//文件字节大小/1024所得的就是以kb为单位的大小
				BigDecimal bd = new BigDecimal(ds);
				double resultSize = bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");//文件最后修改时间
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(f.lastModified());
				String fileTime = sdf.format(cal.getTime());
				
				monthFilesDownloadForm.setFile_name(fileName);
				monthFilesDownloadForm.setFile_size(resultSize+"kb");
				monthFilesDownloadForm.setFile_time(fileTime);
				
				monthFilesDownloadForms.add(monthFilesDownloadForm);
			}
		}
		
		//按照文件的最后编辑时间进行倒序排列
		Collections.sort(monthFilesDownloadForms, new Comparator<MonthFilesDownloadForm>() {
			@Override
			public int compare(MonthFilesDownloadForm o1, MonthFilesDownloadForm o2) {
				
				return o2.getFile_time().compareTo(o1.getFile_time());
			}
			
		});
		
		return monthFilesDownloadForms;
	}

	public void updateMaterialComment(String material_id, String operator_id,
			String comment, SqlSessionManager conn) {
		MaterialCommentMapper mapper = conn.getMapper(MaterialCommentMapper.class);
		Map<String, Object> materialComment = new HashMap<String, Object>();
		materialComment.put("material_id", material_id);
		materialComment.put("operator_id", operator_id);
		materialComment.put("comment", comment);
		materialComment.put("create_datetime", new Date());
		mapper.inputMaterialComment(materialComment);		
	}

	public void getMaterialCommentEdit(String material_id, String operator_id,
			Map<String, Object> callbackResponse, boolean writable, SqlSession conn) {
		MaterialCommentMapper mapper = conn.getMapper(MaterialCommentMapper.class);
		if (writable) {
			callbackResponse.put("material_comment_other", mapper.getMaterialComments(material_id, operator_id));
			callbackResponse.put("material_comment", mapper.getMyMaterialComment(material_id, operator_id));
		} else {
			callbackResponse.put("material_comment_other", mapper.getMaterialComments(material_id, null));
		}
	}

	public void getMaterialComment(String material_id,
			Map<String, Object> callbackResponse, SqlSession conn) {
		MaterialCommentMapper mapper = conn.getMapper(MaterialCommentMapper.class);
		callbackResponse.put("material_comment", mapper.getMaterialComments(material_id, null));
	}

	public void pxExchange(String material_id, SqlSessionManager conn) {
		MaterialMapper mapper = conn.getMapper(MaterialMapper.class);
		mapper.updatePx(material_id);
	}

	/**
	 * 给附加的维修对象一个投线计划顺序
	 * @param material_id 维修对象 ID
	 * @param conn
	 */
	public void addInlinePlan(String material_id, SqlSessionManager conn) {

		MaterialFactMapper mapper = conn.getMapper(MaterialFactMapper.class);
		// 判断是否已有计划，有则return。
		String[] materials = {material_id};
		List<MaterialFactEntity> resList = mapper.getInlinePlanInfo(materials);
		if (resList.size() > 0) return;

		// 取得当日最大计划排序
		Integer seq = null;
		seq = mapper.getMaxInlinePlanSeqToday();

		// 没有则按当日首条处理
		if (seq == null) {
			seq = RvsUtils.getTodayCounts() * 1000;
		}
		seq += 16;

		// 建立到投线计划表
		MaterialFactEntity insertEntity = new MaterialFactEntity();
		insertEntity.setMaterial_id(material_id);
		insertEntity.setSeq(seq);

		mapper.createInlinePlan(insertEntity);
	}

	/**
	 * @param form
	 * @param conn
	 * @return
	 */
	public String worktimeExport(HttpServletRequest req,SqlSession conn,List<MsgInfo> errors) throws Exception{
		@SuppressWarnings("unchecked")
		List<MaterialForm> lResultForm = (ArrayList<MaterialForm>) req.getSession().getAttribute(RvsConsts.SEARCH_RESULT);
		
		if(lResultForm.size() > 30000){
			MsgInfo info = new MsgInfo();
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.report.overflow"));
			errors.add(info);
			
			return null;
		}
		
		//Excel临时文件
		String cacheName ="工时统计 " + new Date().getTime() + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(new Date(), "yyyyMM") + "\\" +cacheName; 
		FileUtils.copyFile(PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\维修对象一览报表模板.xls", cachePath);
		
		//查询所有工位
		PositionMapper positionMapper = conn.getMapper(PositionMapper.class);
		List<PositionEntity> positionList = positionMapper.getAllPosition();
		
		for(int i = positionList.size()-1;i >=0;i--){
			if("301".equals(positionList.get(i).getProcess_code())){
				positionList.remove(i);
			}
			
			if("601".equals(positionList.get(i).getProcess_code())){
				positionList.remove(i);
			}
		}
		
		String [] materialIds = new String[lResultForm.size()];
		for(int i = 0;i < lResultForm.size();i++){
			materialIds[i] = lResultForm.get(i).getMaterial_id();
		}
		
		//取得维修对象每个工位的工时
		ProductionFeatureMapper productionFeatureMapper = conn.getMapper(ProductionFeatureMapper.class);
		List<ProductionFeatureEntity> workTimeList = productionFeatureMapper.getMaterialPositionWorkTime(materialIds);
		
		POIFSFileSystem fs;
		HSSFWorkbook book = null;

		try {
			fs = new POIFSFileSystem(new FileInputStream(cachePath));
			book = new HSSFWorkbook(fs);

			Map<String, String> ocms = new HashMap<String, String>();
			Map<String, String> levels = new HashMap<String, String>();
			
			//工位列索引
			Map<String,Integer> processCodeColIndex = new HashMap<String,Integer>();
			//维修对象所在行索引
			Map<String,Integer> materialIdRowIndex = new HashMap<String,Integer>();
			
			// 一览列表生成
			HSSFSheet listSheet = book.getSheetAt(0);
			
			//标题冻结
			listSheet.createFreezePane(2, 1);
			
			HSSFRow row = null;
			HSSFCell cell = null;
			
			HSSFFont fontYH = book.createFont();
			fontYH.setFontName("Microsoft YaHei");
			fontYH.setFontHeightInPoints((short) 10);
			
			HSSFFont fontBoldYH = book.createFont();
			fontBoldYH.setFontName("Microsoft YaHei");
			fontBoldYH.setFontHeightInPoints((short) 10);
			fontBoldYH.setBoldweight((short) 600);
			
			HSSFCellStyle defaultCell = book.createCellStyle();
			defaultCell.setBorderBottom(HSSFCellStyle.BORDER_THIN); //下边框
			defaultCell.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
			defaultCell.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
			defaultCell.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
			defaultCell.setFont(fontYH);

			HSSFCellStyle highlightCell = book.createCellStyle(); // 亮色
			highlightCell.cloneStyleFrom(defaultCell);
			highlightCell.setFillForegroundColor(HSSFColor.AQUA.index);
			highlightCell.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

			HSSFCellStyle centerCell = book.createCellStyle(); // 居中
			centerCell.cloneStyleFrom(defaultCell);
			centerCell.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			
			HSSFCellStyle highlightCenterCell = book.createCellStyle();//居中 亮色
			highlightCenterCell.cloneStyleFrom(highlightCell);
			highlightCenterCell.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			highlightCenterCell.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			highlightCenterCell.setFont(fontBoldYH);
			
			//黄色背景
			HSSFCellStyle yellowCell = book.createCellStyle(); 
			yellowCell.cloneStyleFrom(highlightCell);
			yellowCell.setFillForegroundColor(HSSFColor.YELLOW.index);

			listSheet.setDefaultColumnStyle(0, highlightCell);
			listSheet.setDefaultColumnStyle(1, defaultCell);
			listSheet.setDefaultColumnStyle(2, defaultCell);
			listSheet.setDefaultColumnStyle(3, defaultCell);
			listSheet.setDefaultColumnStyle(4, defaultCell);
			listSheet.setDefaultColumnStyle(5, defaultCell);
			listSheet.setDefaultColumnStyle(6, centerCell);
			listSheet.setDefaultColumnStyle(7, centerCell);
			listSheet.setDefaultColumnStyle(8, defaultCell);
			listSheet.setDefaultColumnStyle(9, defaultCell);
			listSheet.setDefaultColumnStyle(10, defaultCell);
			listSheet.setDefaultColumnStyle(11, defaultCell);
			listSheet.setDefaultColumnStyle(12, defaultCell);
			listSheet.setDefaultColumnStyle(13, defaultCell);
			listSheet.setDefaultColumnStyle(14, defaultCell);
			listSheet.setDefaultColumnStyle(15, defaultCell);
			listSheet.setDefaultColumnStyle(16, defaultCell);

			//取得第一行
			row = listSheet.getRow(0);
			
			//设置工位标题
			for(int i = 0;i < positionList.size();i++){
				cell = row.createCell(17 + i);
				cell.setCellValue(positionList.get(i).getProcess_code());
				cell.setCellStyle(highlightCenterCell);
				
				processCodeColIndex.put(positionList.get(i).getProcess_code(), 17 + i);
				//列样式
				listSheet.setDefaultColumnStyle(17 + i, defaultCell);
			}
			
			//总计
			cell = row.createCell(17 + positionList.size());
			cell.setCellValue("总计");
			cell.setCellStyle(highlightCenterCell);
			listSheet.setDefaultColumnStyle(17 + positionList.size(), defaultCell);
			
			//直接总计
			cell = row.createCell(17 + positionList.size() + 1);
			cell.setCellValue("直接总计");
			cell.setCellStyle(highlightCenterCell);
			listSheet.setDefaultColumnStyle(17 + positionList.size() + 1, defaultCell);
			
			for (int i=0; i < lResultForm.size(); i++) {
				MaterialForm resultForm = lResultForm.get(i);
				materialIdRowIndex.put(resultForm.getMaterial_id(), i+1);
				
				row = listSheet.createRow(i+1);
				//序号
				cell = row.createCell(0, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(i+1);

				//SORC NO.
				cell = row.createCell(1, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue("'" + resultForm.getSorc_no());

				//型号
				cell = row.createCell(2, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getModel_name());

				//等级
				cell = row.createCell(3, HSSFCell.CELL_TYPE_STRING);
				String sLevel = resultForm.getLevel();
				if (!levels.containsKey(sLevel)) {
					levels.put(sLevel, CodeListUtils.getValue("material_level", sLevel, " - "));
				}
				sLevel = levels.get(sLevel);
				cell.setCellValue(sLevel);

				//机身号
				cell = row.createCell(4, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue("'" + resultForm.getSerial_no());

				//委托处
				cell = row.createCell(5, HSSFCell.CELL_TYPE_STRING);
				String sOcm = resultForm.getOcm();
				if (!ocms.containsKey(sOcm)) {
					ocms.put(sOcm, CodeListUtils.getValue("material_ocm", sOcm, " - "));
				}
				sOcm = ocms.get(sOcm);
				cell.setCellValue(sOcm);

				//维修课室
				cell = row.createCell(6, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getSection_name());

				//当前位置
				cell = row.createCell(7, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getProcessing_position());

				//NS当前位置
				cell = row.createCell(8, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getProcessing_position2());

				//受理日期
				cell = row.createCell(9, HSSFCell.CELL_TYPE_STRING);
				String sReceptionTime = resultForm.getReception_time();
				if (sReceptionTime != null && sReceptionTime.length() > 10)
					sReceptionTime = sReceptionTime.substring(0, 10);
				cell.setCellValue(sReceptionTime);

				//同意日期
				cell = row.createCell(10, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getAgreed_date());

				//预定纳期
				cell = row.createCell(11, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getScheduled_date());

				//总组出货安排
				cell = row.createCell(12, HSSFCell.CELL_TYPE_STRING);
				if("9999-12-31".equals(resultForm.getScheduled_date_end())){
					cell.setCellValue("另行通知");
				}else{
					cell.setCellValue(resultForm.getScheduled_date_end());
				}

				//总组出货
				cell = row.createCell(13, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getOutline_time());

				//零件订购日
				cell = row.createCell(14, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getPartial_order_date());

				//入库预定日
				cell = row.createCell(15, HSSFCell.CELL_TYPE_STRING);
				if("9999-12-31".equals(resultForm.getArrival_plan_date())){
					cell.setCellValue("未定");
				}else{
					cell.setCellValue(resultForm.getArrival_plan_date());
				}

				//延误
				cell = row.createCell(16, HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(resultForm.getIs_late());
				
				//总计
				row.createCell(17 + positionList.size()).setCellFormula("SUM(R" + (i+2) + ":" + ReportUtils.getPosition(16 + positionList.size(), i+2) +")");
				
				//直接总计
				row.createCell(17 + positionList.size() + 1).setCellFormula("SUM(" + ReportUtils.getPosition(processCodeColIndex.get("211"),(i+2)) + ":" + ReportUtils.getPosition(processCodeColIndex.get("611"),(i+2)) +")");
			}
			
			
			//工时赋值
			for(int i = 0;i < workTimeList.size();i++){
				ProductionFeatureEntity productionFeatureEntity = workTimeList.get(i);
				
				//取得维修对象所在行的索引
				int rowIndex = materialIdRowIndex.get(productionFeatureEntity.getMaterial_id());
				
				//工位代码
				String process_code = productionFeatureEntity.getProcess_code();
				
				//工位不存在则不处理
				if(processCodeColIndex.get(process_code) == null){
					continue;
				}
				
				//取得工位代码在sheet中所在列的索引
				int colIndex= processCodeColIndex.get(process_code);
				
				row = listSheet.getRow(rowIndex);
				
				cell = row.createCell(colIndex);
				
				//工时
				cell.setCellValue(productionFeatureEntity.getSum_use_seconds().doubleValue());
				
				//如果工位存在返工，则单元格样式为黄色背景
				if(productionFeatureEntity.getRework() > 1){
					cell.setCellStyle(yellowCell);
				}
			}
			
			// 保存文件
			FileOutputStream fileOut = new FileOutputStream(cachePath);
			book.write(fileOut);
			fileOut.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return cacheName;

	}


	public void reaccpect(ActionForm form, String section_id, SqlSessionManager conn) throws Exception {

		MaterialEntity insertBean = new MaterialEntity();
		BeanUtil.copyToBean(form, insertBean, null);

		ModelService mdlService = new ModelService();
		ModelForm mdlEntity = mdlService.getDetail(insertBean.getModel_id(), conn);
		String pat_id = mdlEntity.getDefault_pat_id();
		if (isEmpty(pat_id)) {
			return; // TODO errInfo
		}

		insertBean.setPat_id(pat_id);
		insertBean.setScheduled_expedited(1);

		String materialId = insertProduct(insertBean, section_id, conn);

		ProcessAssignService pas = new ProcessAssignService();
		List<String> firstPosition_ids = pas.getFirstPositionIds(pat_id, conn);
		for (String position_id: firstPosition_ids) {
			ProductionFeatureEntity featureEntity = new ProductionFeatureEntity ();

			featureEntity.setOperate_result(0);
			featureEntity.setPace(0);
			featureEntity.setRework(0);
			featureEntity.setMaterial_id(materialId);
			featureEntity.setPosition_id(position_id);
			featureEntity.setSection_id(section_id);

			ProductionFeatureService pfService = new ProductionFeatureService();
			pfService.fingerSpecifyPosition(materialId, true, featureEntity, new ArrayList<String>(), conn);
		}
	}
}
