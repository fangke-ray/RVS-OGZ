package com.osh.rvs.service.qf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.data.ProductionFeatureEntity;
import com.osh.rvs.bean.inline.MaterialFactEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.form.qf.MaterialFactForm;
import com.osh.rvs.mapper.data.MaterialMapper;
import com.osh.rvs.mapper.master.PositionMapper;
import com.osh.rvs.mapper.qf.MaterialFactMapper;
// import com.osh.rvs.service.MaterialProcessAssignService;
import com.osh.rvs.service.MaterialProcessService;
import com.osh.rvs.service.MaterialService;
import com.osh.rvs.service.ProcessAssignService;
import com.osh.rvs.service.ProductionFeatureService;
import com.osh.rvs.service.inline.PositionPanelService;
import com.osh.rvs.service.partial.MaterialPartialService;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;

public class MaterialFactService {

	private static Set<String> CCD_MODEL_NAMES = null;
	private Logger log = Logger.getLogger(getClass());

	public List<MaterialFactForm> searchMaterial(ActionForm form, SqlSession conn, List<MsgInfo> errors) {
		// 表单复制到数据对象
		MaterialFactEntity conditionBean = new MaterialFactEntity();
		BeanUtil.copyToBean(form, conditionBean, null);

		if (CCD_MODEL_NAMES == null) {
			log.info("GET CCD Lists");
			getCcdModels();
		}

		// 从数据库中查询记录
		MaterialFactMapper dao = conn.getMapper(MaterialFactMapper.class);
		List<MaterialFactEntity> lResultBean = dao.searchMaterial(conditionBean);

		// 建立页面返回表单
		List<MaterialFactForm> lResultForm = new ArrayList<MaterialFactForm>();

		Set<String> ccdLineModels = RvsUtils.getCcdLineModels(conn);

		// 数据对象复制到表单
		for (MaterialFactEntity resultBean : lResultBean) {
			MaterialFactForm resultForm = new MaterialFactForm();
			BeanUtil.copyToForm(resultBean, resultForm, CopyOptions.COPYOPTIONS_NOEMPTY);
			char ccdModel = 0;
			if (CCD_MODEL_NAMES.contains(resultForm.getModel_name())) {
				ccdModel |= 1;
			}
			if (ccdLineModels.contains(resultForm.getModel_id())) {
				ccdModel |= 2;
			}
			resultForm.setCcd_model("" + (int) ccdModel);

			if ("9".equals(resultBean.getCcd_operate_result())) {
				resultForm.setCcd_operate_result("已指定");
			} else {
				resultForm.setCcd_operate_result("");
			}

			lResultForm.add(resultForm);
		}

		return lResultForm;
	}

	public List<MaterialFactForm> searchInlineMaterial(SqlSession conn) {

		MaterialFactMapper dao = conn.getMapper(MaterialFactMapper.class);
		List<MaterialFactEntity> lResultBean = dao.searchInlineMaterial();

		List<MaterialFactForm> lResultForm = new ArrayList<MaterialFactForm>();

		if (CCD_MODEL_NAMES == null) {
			log.info("GET CCD Lists");
			getCcdModels();
		}

		// 数据对象复制到表单
		for (MaterialFactEntity resultBean : lResultBean) {
			MaterialFactForm resultForm = new MaterialFactForm();
			BeanUtil.copyToForm(resultBean, resultForm, CopyOptions.COPYOPTIONS_NOEMPTY);
			if (CCD_MODEL_NAMES.contains(resultForm.getModel_name())) {
				resultForm.setCcd_model("1");
			}
			lResultForm.add(resultForm);
		}

		return lResultForm;
	}

	public String createReport(SqlSession conn) throws Exception {
		// 模板路径
		
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "今日投线一览.xls";
		String cacheName = "今日投线一览" + new Date().getTime() + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(new Date(), "yyyyMM") + "\\" + cacheName;
		try {
			FileUtils.copyFile(new File(path), new File(cachePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		SimpleDateFormat inlineTimeFormat=new SimpleDateFormat("HH:mm");
		SimpleDateFormat agreeDateFormat=new SimpleDateFormat("MM-dd ");
		MaterialFactMapper dao = conn.getMapper(MaterialFactMapper.class);
		List<MaterialFactEntity> lResultBean = dao.searchInlineMaterial();
		
		OutputStream out = null;
		InputStream in = null;
		try {
			in = new FileInputStream(cachePath);
			HSSFWorkbook work = new HSSFWorkbook(in);
			HSSFSheet sheet = work.getSheetAt(0);	
			
			/*设置单元格内容居中显示*/
			HSSFCellStyle styleAlignCenter = work.createCellStyle();
			styleAlignCenter.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
			styleAlignCenter.setBorderTop(HSSFCellStyle.BORDER_THIN); 
			styleAlignCenter.setBorderRight(HSSFCellStyle.BORDER_THIN); 
			styleAlignCenter.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			styleAlignCenter.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			styleAlignCenter.setWrapText(true);
			
			/*设置单元格内容居左显示*/
			HSSFCellStyle styleAlignLeft = work.createCellStyle();
			styleAlignLeft.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
			styleAlignLeft.setBorderTop(HSSFCellStyle.BORDER_THIN); 
			styleAlignLeft.setBorderRight(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			styleAlignLeft.setWrapText(true); 
			
			int index = 0;
			for (int i = 0; i < lResultBean.size(); i++) {					
				index++;
				MaterialFactEntity materialFactEntity = lResultBean.get(i);
				HSSFRow row = sheet.createRow(index);	
				
				//序列号
				HSSFCell indexCell = row.createCell(0);
				indexCell.setCellValue(index);
				indexCell.setCellStyle(styleAlignCenter);
                
				//投线时间
				HSSFCell inlineTimeCell= row.createCell(1);
				inlineTimeCell.setCellValue(inlineTimeFormat.format(materialFactEntity.getInline_time()));
				inlineTimeCell.setCellStyle(styleAlignCenter);
				
				//修理单号
				HSSFCell sorcNoCell = 	row.createCell(2);
				sorcNoCell.setCellValue(materialFactEntity.getSorc_no());
				sorcNoCell.setCellStyle(styleAlignLeft);
				
				//ESAS NO.
				HSSFCell esasNoCell = row.createCell(3);
				esasNoCell.setCellValue(materialFactEntity.getSorc_no());
				esasNoCell.setCellStyle(styleAlignLeft);
				
				//型号
				HSSFCell modelNameCell = row.createCell(4);
				modelNameCell.setCellValue(materialFactEntity.getModel_name());
				modelNameCell.setCellStyle(styleAlignLeft);
				
				//机身号
				HSSFCell serialNoCell = row.createCell(5);
				serialNoCell.setCellValue(materialFactEntity.getSerial_no());
				serialNoCell.setCellStyle(styleAlignLeft);
				
				//等级
				HSSFCell levelCell = row.createCell(6);
				/*if(materialFactEntity.getLevel()==1){
					levelCell.setCellValue("S1");
				}else if(materialFactEntity.getLevel()==2){
					levelCell.setCellValue("S2");
				}else if(materialFactEntity.getLevel()==3){
					levelCell.setCellValue("S3");
				}else if(materialFactEntity.getLevel()==6){
					levelCell.setCellValue("A");
				}else if(materialFactEntity.getLevel()==7){
					levelCell.setCellValue("B");
				}else if(materialFactEntity.getLevel()==8){
					levelCell.setCellValue("C");
				}else if(materialFactEntity.getLevel()==9){
					levelCell.setCellValue("D");
				}*/
				Integer level = materialFactEntity.getLevel();
				String levelData = CodeListUtils.getValue("material_level",level.toString());
				levelCell.setCellValue(levelData);
				levelCell.setCellStyle(styleAlignCenter);
				
				//投入科室
				HSSFCell sectionNameCell = row.createCell(7);
				sectionNameCell.setCellValue(materialFactEntity.getSection_name());
				sectionNameCell.setCellStyle(styleAlignCenter);
				
				//客户同意日
				HSSFCell agreedDateCell = row.createCell(8);
				agreedDateCell.setCellValue(agreeDateFormat.format(materialFactEntity.getAgreed_date()));
				agreedDateCell.setCellStyle(styleAlignCenter);
				
				//2日内投线
				HSSFCell twoDaysOfLinesCell = row.createCell(9);
				if ("0".equals(dao.getTwoDaysOfLines(materialFactEntity.getMaterial_id()))) {
					twoDaysOfLinesCell.setCellValue("╳");
				} else{
					twoDaysOfLinesCell.setCellValue("√");
				}		
				twoDaysOfLinesCell.setCellStyle(styleAlignCenter);
			}
			out = new FileOutputStream(cachePath);
			try {
				work.write(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
		return cacheName;
	}

	private void getCcdModels() {
		String sSnoutModels = PathConsts.POSITION_SETTINGS.getProperty("ccd.models");

		CCD_MODEL_NAMES = new LinkedHashSet<String>();
		if (sSnoutModels != null) {
			String[] model_names = sSnoutModels.split(",");

			for (String model_name : model_names) {
				CCD_MODEL_NAMES.add(model_name);
			}
		}
	}

	public void updateAgreedDate(ActionForm form, SqlSession conn) {
		// 表单复制到数据对象
		MaterialFactEntity entity = new MaterialFactEntity();
		BeanUtil.copyToBean(form, entity, null);

		MaterialMapper mMapper = conn.getMapper(MaterialMapper.class);
		MaterialEntity mBean = mMapper.loadMaterialDetail(entity.getMaterial_id());

		Date dSchedulePlan = RvsUtils.getTimeLimit(entity.getAgreed_date(), 
				mBean.getLevel(), null, conn, false)[0];
		entity.setScheduled_date(dSchedulePlan);

		MaterialFactMapper dao = conn.getMapper(MaterialFactMapper.class);
		dao.updateAgreedDate(entity);
//
//		// FSE 数据同步
//		try{
//			FseBridgeUtil.toUpdateMaterialProcess(entity.getMaterial_id(), "agree");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public void updateAgreedDateBySorc(String sorc_no, Date agreed_date, SqlSession conn) {
		// 表单复制到数据对象
		MaterialFactEntity entity = new MaterialFactEntity();
		entity.setSorc_no(sorc_no);
		entity.setAgreed_date(agreed_date);

		MaterialFactMapper dao = conn.getMapper(MaterialFactMapper.class);
		dao.updateAgreedDateBySorc(entity);
		// if (ret > 0) {
		// System.out.println(sorc_no);
		// }
	}

	public void updateUnrepairBySorc(String sorc_no, Date dAppove, SqlSession conn) {
		// 表单复制到数据对象
		MaterialFactEntity entity = new MaterialFactEntity();
		entity.setSorc_no(sorc_no);
		entity.setAgreed_date(dAppove);

		MaterialFactMapper dao = conn.getMapper(MaterialFactMapper.class);
		dao.updateUnrepairBySorc(entity);
	}

	public void updateInline(ActionForm form, SqlSessionManager conn) throws Exception {
		// 取得当日投线的话，应当的完成时间
		Date[] scheduleAssignTimes = MaterialProcessService.getScheduleAssignTimes(conn);

		// 表单复制到数据对象
		MaterialFactEntity entity = new MaterialFactEntity();
		BeanUtil.copyToBean(form, entity, null);
		updateInline(entity, scheduleAssignTimes, conn);

		MaterialService ms = new MaterialService();
		ms.addInlinePlan(entity.getMaterial_id(), conn);
//
//		// FSE 数据同步
//		try{
//			conn.commit();
//			FseBridgeUtil.toUpdateMaterial(entity.getMaterial_id(), "200inline");
//			FseBridgeUtil.toUpdateMaterialProcess(entity.getMaterial_id(), "200inline");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public void updateInline(MaterialFactEntity entity, Date[] scheduleAssignTimes, SqlSessionManager conn) throws Exception {
		Integer level = entity.getLevel();
		// boolean lightFix = RvsUtils.isLightFix(level);
		// lightFix = lightFix && (entity.getFix_type() == 1);

		Date agreedDate = entity.getAgreed_date();

		Date[] workDates = RvsUtils.getTimeLimit(agreedDate, level, null, conn, true);
		Date workDate = workDates[0];
		entity.setScheduled_date(workDate);

		// 更新维修对象的投线信息
		MaterialFactMapper dao = conn.getMapper(MaterialFactMapper.class);

		if ("".equals(entity.getPat_id())) {
			entity.setPat_id(null); // TODO
		}
		entity.setQuotation_first(0);

		dao.updateInline(entity);

		String materialId = entity.getMaterial_id();

		{ // //如果是流水线增加进展记录
			// 插入找到的记录到material_process
			String patId = entity.getPat_id();

			MaterialProcessService mps = new MaterialProcessService();
			ProcessAssignService pas = new ProcessAssignService();
			List<String> newHasLines = pas.checkPatHasLine(patId, "" + level, conn);
			mps.setMaterialProcess(materialId, level, newHasLines, workDates, scheduleAssignTimes, conn);

			// 插入作业
			List<ProductionFeatureEntity> featureEntities = new ArrayList<ProductionFeatureEntity> ();

			PositionPanelService pps = new PositionPanelService();

			// 302指定投线
			if (entity.getCcd_change() != null && "true".equals(entity.getCcd_change()) ) {
				addFeatureEntity(featureEntities, materialId, "00000000025", "00000000001"); // TODO CCD
			} else {
				List<String> firstPositionIds = pas.getFirstPositionIds(patId, conn);

				if (firstPositionIds.size() > 0) {
					for (String firstPositionId : firstPositionIds) {
						addFeatureEntity(featureEntities, materialId, firstPositionId, entity.getSection_id());
						pps.notifyPosition(entity.getSection_id(), firstPositionId, materialId);
					}
				} else {
					String firstPositionId = "00000000016";
					addFeatureEntity(featureEntities, materialId, firstPositionId, entity.getSection_id());
					pps.notifyPosition(entity.getSection_id(), firstPositionId, materialId);
				}

			}

			ProductionFeatureService pfService = new ProductionFeatureService();
			for (ProductionFeatureEntity featureEntity : featureEntities) {
				if ("99".equals(featureEntity.getPosition_id()) || "00000000099".equals(featureEntity.getPosition_id())) {
					MaterialPartialService mptlService = new MaterialPartialService();
					mptlService.createMaterialPartialWithExistCheck(materialId, conn);
				}
				boolean fixed = 
						("25".equals(featureEntity.getPosition_id()) || "00000000025".equals(featureEntity.getPosition_id()));
				pfService.fingerSpecifyPosition(materialId, fixed, featureEntity, new ArrayList<String>(), conn);
			}
		}
	}

	/**
	 * 设定起始工位信息
	 * @param featureEntities
	 * @param materialId
	 * @param positionId
	 * @param sectionId
	 */
	private void addFeatureEntity(List<ProductionFeatureEntity> featureEntities, String materialId,
			String positionId, String sectionId) {
		ProductionFeatureEntity featureEntity = new ProductionFeatureEntity ();

		featureEntity.setOperate_result(0);
		featureEntity.setPace(0);
		featureEntity.setRework(0);
		featureEntity.setMaterial_id(materialId);
		featureEntity.setPosition_id(positionId);
		featureEntity.setSection_id(sectionId);

		featureEntities.add(featureEntity);
	}

	/**
	 * 标为加急
	 * 
	 * @param ids
	 * @param conn
	 * @throws Exception
	 */
	public void updateExpedite(List<String> ids, SqlSessionManager conn) throws Exception {
		MaterialMapper mDao = conn.getMapper(MaterialMapper.class);
		mDao.updateMaterialExpedite(ids);
	}

	/**
	 * 指定进行Ccd盖玻璃更换（投线后）
	 * @param material_id
	 * @param conn
	 */
	public void assginCCDChange(String material_id, SqlSessionManager conn) {
		MaterialFactMapper mfMapper = conn.getMapper(MaterialFactMapper.class);
		mfMapper.assginCCDChange(material_id);
	}

	public List<MaterialForm> getInlinePlan(SqlSession conn) {
		MaterialFactMapper mfMapper = conn.getMapper(MaterialFactMapper.class);
		List<MaterialForm> resultForm = new ArrayList<MaterialForm>();

		List<MaterialEntity> result = mfMapper.getInlinePlan();

		CopyOptions cos = new CopyOptions();
		cos.excludeNull().excludeEmptyString().dateConverter("MM-dd", "agreed_date");

		for (MaterialEntity bean : result) {
			MaterialForm form = new MaterialForm();
			BeanUtil.copyToForm(bean, form, cos);
			int level = bean.getLevel();
			boolean isLightFix = RvsUtils.isLightFix(level);
			if (!isLightFix && !RvsUtils.isPeripheral(level)) {
				if (!RvsUtils.getCcdModels(conn).contains(form.getModel_id())) {
					form.setQuotation_first("-1");
				}
			}
			form.setService_repair_flg(CodeListUtils.getValue("material_service_repair", form.getService_repair_flg()));
			resultForm.add(form);
		}

		return resultForm;
	}

	public void changeinlinePlan(ActionForm form, SqlSessionManager conn) {
		MaterialFactMapper mfMapper = conn.getMapper(MaterialFactMapper.class);

		// 表单复制到数据对象
		MaterialFactEntity conditionBean = new MaterialFactEntity();
		BeanUtil.copyToBean(form, conditionBean, CopyOptions.COPYOPTIONS_NOEMPTY);

		if (conditionBean.getCcd_change() != null) {
			mfMapper.assginCCDChange(conditionBean.getMaterial_id());
		} else {
			mfMapper.changeInlinePlan(conditionBean);
		}
	}

	public void doBatchInline(String[] material_ids, SqlSessionManager conn) throws Exception {
		MaterialFactMapper mfMapper = conn.getMapper(MaterialFactMapper.class);
		// 取得当日投线的话，应当的完成时间
		Date[] scheduleAssignTimes = MaterialProcessService.getScheduleAssignTimes(conn);

		List<MaterialFactEntity> mfEntities = mfMapper.getInlinePlanInfo(material_ids);

		for (MaterialFactEntity mfEntity : mfEntities) {
			updateInline(mfEntity, scheduleAssignTimes, conn);
		}
//
//		// FSE 数据同步
//		try{
//			conn.commit();
//			for (String material_id : material_ids) {
//				FseBridgeUtil.toUpdateMaterial(material_id, "200inline");
//				FseBridgeUtil.toUpdateMaterialProcess(material_id, "200inline");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public String createInlineReport(SqlSession conn) throws Exception {

		// 模板路径
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "投线单一览模板.xls";
		String cacheName = "投线单一览" + new Date().getTime() + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(new Date(), "yyyyMM") + "\\" + cacheName;
		try {
			FileUtils.copyFile(new File(path), new File(cachePath));
		} catch (IOException e) {
			e.printStackTrace();
		}

		SimpleDateFormat printFormat=new SimpleDateFormat("MM-dd HH:mm");
		SimpleDateFormat agreeDateFormat=new SimpleDateFormat("MM-dd ");
		MaterialFactMapper dao = conn.getMapper(MaterialFactMapper.class);
		List<MaterialEntity> lResultBean = dao.getInlinePlan();
//		MaterialProcessAssignService mpas = new MaterialProcessAssignService();
		ProcessAssignService pas = new ProcessAssignService();
		PositionMapper pMapper = conn.getMapper(PositionMapper.class);

		Map<String, String> processCodeMap = new HashMap<String, String>();

		OutputStream out = null;
		InputStream in = null;
		try {
			in = new FileInputStream(cachePath);
			HSSFWorkbook work = new HSSFWorkbook(in);
			HSSFSheet sheet = work.getSheetAt(0);	
			
			/*设置单元格内容居中显示*/
			HSSFCellStyle styleAlignCenter = work.createCellStyle();
			styleAlignCenter.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
			styleAlignCenter.setBorderTop(HSSFCellStyle.BORDER_THIN); 
			styleAlignCenter.setBorderRight(HSSFCellStyle.BORDER_THIN); 
			styleAlignCenter.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			styleAlignCenter.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			styleAlignCenter.setWrapText(true);
			
			/*设置单元格内容居左显示*/
			HSSFCellStyle styleAlignLeft = work.createCellStyle();
			styleAlignLeft.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
			styleAlignLeft.setBorderTop(HSSFCellStyle.BORDER_THIN); 
			styleAlignLeft.setBorderRight(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			styleAlignLeft.setWrapText(true); 

			// 打印时间
			HSSFRow row3 = sheet.getRow(2);
			HSSFCell printTimeCell = row3.createCell(6);
			printTimeCell.setCellValue(printFormat.format(new Date()));

			int index = 4;
			for (int i = 0; i < lResultBean.size(); i++) {					
				index++;
				MaterialEntity materialEntity = lResultBean.get(i);
				HSSFRow row = sheet.createRow(index);	
				
				//序号
				HSSFCell indexCell = row.createCell(0);
				indexCell.setCellValue(i + 1);
				indexCell.setCellStyle(styleAlignCenter);

				//修理单号
				HSSFCell sorcNoCell = 	row.createCell(1);
				sorcNoCell.setCellValue(materialEntity.getSorc_no());
				sorcNoCell.setCellStyle(styleAlignLeft);
			
				//型号
				HSSFCell modelNameCell = row.createCell(2);
				modelNameCell.setCellValue(materialEntity.getModel_name());
				modelNameCell.setCellStyle(styleAlignLeft);
				
				//机身号
				HSSFCell serialNoCell = row.createCell(3);
				serialNoCell.setCellValue(materialEntity.getSerial_no());
				serialNoCell.setCellStyle(styleAlignLeft);

				//等级
				HSSFCell levelCell = row.createCell(4);
				Integer level = materialEntity.getLevel();
				String levelData = CodeListUtils.getValue("material_level",level.toString());
				levelCell.setCellValue(levelData);
				levelCell.setCellStyle(styleAlignCenter);

				//客户同意日
				HSSFCell agreedDateCell = row.createCell(5);
				agreedDateCell.setCellValue(agreeDateFormat.format(materialEntity.getAgreed_date()));
				agreedDateCell.setCellStyle(styleAlignCenter);

				//WIP 库位
				HSSFCell wipLocationCell = row.createCell(6);
				wipLocationCell.setCellValue(materialEntity.getWip_location());
				wipLocationCell.setCellStyle(styleAlignCenter);

				//投入科室
				HSSFCell sectionNameCell = row.createCell(7);
				if (materialEntity.getFix_type() == 1) {
					sectionNameCell.setCellValue(materialEntity.getSection_name());
				} else {
					sectionNameCell.setCellValue("单元");
				}
				sectionNameCell.setCellStyle(styleAlignCenter);
				
				//投入工位
				HSSFCell processCodesCell = row.createCell(8);
//				if (materialEntity.getFix_type() == 1) {
//					boolean isLightFix = RvsUtils.isLightFix(level);
//					if (level == 9 || level == 91 || level == 92 || level == 93) {
//						// 取得首工位
//						String firstPosition_id = mpas.getFirstPositionId(materialEntity.getMaterial_id(), conn);
//						String process_code = null;
//						if (!processCodeMap.containsKey(firstPosition_id)) {
//							PositionEntity pEntity = pMapper.getPositionByID(firstPosition_id);
//							processCodeMap.put(firstPosition_id, pEntity.getProcess_code());
//						}
//						process_code = processCodeMap.get(firstPosition_id);
//
//						processCodesCell.setCellValue(process_code);
//
//					} else {
						if (materialEntity.getQuotation_first() == 9) {
							processCodesCell.setCellValue("302");
						} else {
							List<String> firstPosition_ids = pas.getFirstPositionIds(materialEntity.getPat_id(), conn);

							String[] process_codes = new String[firstPosition_ids.size()];

							for (int iPc=0;iPc < firstPosition_ids.size();iPc++) {
								String firstPosition_id = firstPosition_ids.get(iPc);
								String process_code = null;
								if (!processCodeMap.containsKey(firstPosition_id)) {
									PositionEntity pEntity = pMapper.getPositionByID(firstPosition_id);
									processCodeMap.put(firstPosition_id, pEntity.getProcess_code());
								}
								process_code = processCodeMap.get(firstPosition_id);
								process_codes[iPc] = process_code;
							}

							processCodesCell.setCellValue(CommonStringUtil.joinBy("", process_codes));
						}
//					}
//				}
				processCodesCell.setCellStyle(styleAlignCenter);
			}
			out = new FileOutputStream(cachePath);
			try {
				work.write(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
		return cacheName;
	}
}
