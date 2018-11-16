package com.osh.rvs.service.qa;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.struts.action.ActionForm;

import com.osh.rvs.bean.data.MaterialEntity;
import com.osh.rvs.bean.qa.ServiceRepairManageEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.form.data.MaterialForm;
import com.osh.rvs.form.qa.ServiceRepairManageForm;
import com.osh.rvs.mapper.inline.SoloProductionFeatureMapper;
import com.osh.rvs.mapper.qa.ServiceRepairManageMapper;

import framework.huiqing.bean.message.MsgInfo;
import framework.huiqing.common.util.AutofillArrayList;
import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.BeanUtil;
import framework.huiqing.common.util.copy.CopyOptions;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class ServiceRepairManageService {

	/** 保内QIS型号名字集合 **/
	public List<String> getModelNameAutoCompletes(SqlSession conn) {
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		return dao.getModelNameAutoCompletes();
	}
	
	/**无QA判定日的数据中最早的一个QA受理日**/
	public String getMinReceptionTime(SqlSession conn){
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		String strDate="";
		Date date=dao.getMinReceptionTime();
		strDate=DateUtil.toString(date, DateUtil.DATE_PATTERN);
		return strDate;
	}
	
	/**保内QIS管理一览**/
	public List<ServiceRepairManageForm> searchServiceRepair(ServiceRepairManageEntity instance,SqlSession conn){
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		List<ServiceRepairManageForm> lResultForm=new ArrayList<ServiceRepairManageForm>();
		if(instance!=null){
			List<ServiceRepairManageEntity> lResultBean=dao.searchServiceRepair(instance);
			//复制数据对象到表单
			CopyOptions cos = new CopyOptions();
			cos.excludeNull();
			cos.excludeEmptyString();
			// cos.dateConverter(DateUtil.DATETIME_PATTERN, "qa_reception_time", "qa_referee_time");
			BeanUtil.copyToFormList(lResultBean, lResultForm, cos, ServiceRepairManageForm.class);
			return lResultForm;
		}else{
			return null;
		}
		
	}
	
	/**保内QIS管理等级集合**/
	public List<String> getRankAutoCompletes(SqlSession conn){
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		return dao.getRankAutoCompletes();
	}
	
	
	/**维修对象对象集合，集合长度=0 or >1返回null,集合长度=1返回一个对象**/
	public MaterialForm getRecept(ActionForm form, SqlSession conn,List<MsgInfo> errors) {
		//复制表单数据到对象
		ServiceRepairManageEntity enity=new ServiceRepairManageEntity();
		BeanUtil.copyToBean(form, enity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		MaterialForm materialForm=new MaterialForm();
		
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		
		List<MaterialEntity> sList=dao.getRecept(enity);
		int length=sList.size();
		if(length<=0||length>1){
			return null;
		}else{
			MaterialEntity materialEntity=sList.get(0);
			//复制数据到表单对象
			BeanUtil.copyToForm(materialEntity, materialForm, CopyOptions.COPYOPTIONS_NOEMPTY);
			return materialForm;
		}
	}
	
	/**查询主键是否存在**/
	public void getPrimaryKey(ActionForm form,SqlSessionManager conn,List<MsgInfo> errors){
		//复制表单数据到对象
		ServiceRepairManageEntity serviceRepairManageEntity=new ServiceRepairManageEntity();
		BeanUtil.copyToBean(form, serviceRepairManageEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		List<ServiceRepairManageEntity> list=dao.getPrimaryKey(serviceRepairManageEntity);
		if(list.size()>=1){
			   MsgInfo error = new MsgInfo();
			   error.setComponentid("model_name");
			   error.setErrcode("dbaccess.recordDuplicated");
			   error.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("dbaccess.recordDuplicated", "保修期内返品+QIS品"));
			   errors.add(error);
		}
		
	}
	
	
	
	public void insert(ActionForm form,SqlSessionManager conn ,List<MsgInfo> errors) throws Exception{
		
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		ServiceRepairManageEntity serviceRepairManageEntity=new ServiceRepairManageEntity();
		
		//复制表单到数据对象
		BeanUtil.copyToBean(form, serviceRepairManageEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		List<MaterialEntity> list=dao.getMaterialIds(serviceRepairManageEntity);
		
		MaterialEntity mterialEntity=null;
		String material_id = null;
		Long closestReceptionTimeDiff = null;
		Date rc_mailsend_date = serviceRepairManageEntity.getRc_mailsend_date();

		for(int i=0;i<list.size();i++){
			mterialEntity=list.get(i);
			Date reception_time = mterialEntity.getReception_time();
			long diff = Math.abs(reception_time.getTime() - rc_mailsend_date.getTime());
			if (closestReceptionTimeDiff == null || closestReceptionTimeDiff > diff) {
				closestReceptionTimeDiff = diff;
				material_id = mterialEntity.getMaterial_id();
			}
		}
		serviceRepairManageEntity.setMaterial_id(material_id);
		
		Calendar cal = Calendar.getInstance();
		serviceRepairManageEntity.setQa_reception_time(cal.getTime());
		
		
		dao.insertServiceRepairManage(serviceRepairManageEntity);
	}
	
	
	/**创建Excel文件**/
	public String createWorkReport(HttpServletRequest request)throws Exception{
		String path = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\" + "保修期内返品管理日程表模板.xls";
		String cacheName ="保修期内返品管理日程表模板" + new Date().getTime() + ".xls";
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(new Date(), "yyyyMM") + "\\" +cacheName; 
		
		try {
			FileUtils.copyFile(new File(path), new File(cachePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<ServiceRepairManageForm> list=(List)request.getSession().getAttribute("result");
		OutputStream out = null;
		InputStream in = null;
		
		try {
			in = new FileInputStream(cachePath);//读取文件 
			HSSFWorkbook work=new HSSFWorkbook(in);//创建xls文件
			HSSFSheet sheet=work.getSheetAt(0);//取得第一个Sheet
			int index=0;
			HSSFFont font=work.createFont();
			font.setFontHeightInPoints((short)9);
			font.setFontName("微软雅黑");

			//只对"◎"字体的大小做改变(变大)
			HSSFFont sharpFont=work.createFont();
			sharpFont.setFontHeightInPoints((short)16);
			sharpFont.setFontName("微软雅黑");
			
			/*设置单元格内容居中显示*/
			HSSFCellStyle styleAlignCenter = work.createCellStyle();
			styleAlignCenter.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
			styleAlignCenter.setBorderTop(HSSFCellStyle.BORDER_THIN); 
			styleAlignCenter.setBorderRight(HSSFCellStyle.BORDER_THIN); 
			styleAlignCenter.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			styleAlignCenter.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			styleAlignCenter.setWrapText(true);
			styleAlignCenter.setFont(font);
			
			/*设置单元格内容居左显示*/
			HSSFCellStyle styleAlignLeft = work.createCellStyle();
			styleAlignLeft.setBorderLeft(HSSFCellStyle.BORDER_THIN);  
			styleAlignLeft.setBorderTop(HSSFCellStyle.BORDER_THIN); 
			styleAlignLeft.setBorderRight(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setBorderBottom(HSSFCellStyle.BORDER_THIN);
			styleAlignLeft.setAlignment(HSSFCellStyle.ALIGN_LEFT);
			styleAlignLeft.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			styleAlignLeft.setWrapText(true); 
			styleAlignLeft.setFont(font);

			HSSFFont fontRED = work.createFont();
			fontRED.setColor(HSSFColor.RED.index);
			fontRED.setFontHeightInPoints((short)6);
			HSSFCellStyle styleRed = work.createCellStyle();
			styleRed.cloneStyleFrom(styleAlignCenter);
			styleRed.setFont(fontRED);
			
			HSSFCellStyle sharpStyle= work.createCellStyle();
			sharpStyle.cloneStyleFrom(styleAlignCenter);
			sharpStyle.setFont(sharpFont);
			
			for(int i=0;i<list.size();i++){
				  ServiceRepairManageForm form=list.get(i);
				  index++;
				  HSSFRow row=sheet.createRow(index);//从第二行开始创建
				  
				  HSSFCell indexCell = row.createCell(0);
				  indexCell.setCellValue(index);
				  indexCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell modelnameCell =  row.createCell(1);
				  modelnameCell.setCellValue(form.getModel_name());
				  modelnameCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell serialnoCell =  row.createCell(2);
				  serialnoCell.setCellStyle(styleAlignLeft);
				  try {
					  int iSerial_no = Integer.parseInt(form.getSerial_no());
					  serialnoCell.setCellValue(iSerial_no);
					  serialnoCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				  } catch(NumberFormatException e) {
					  serialnoCell.setCellValue(form.getSerial_no());
				  }
				  
				  HSSFCell sorcnoCell =  row.createCell(3);
				  sorcnoCell.setCellValue(form.getSorc_no());
				  sorcnoCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell qamaterialservicerepairCell =  row.createCell(4);
				  qamaterialservicerepairCell.setCellValue(CodeListUtils.getValue("qa_material_service_repair", form.getService_repair_flg()));
				  qamaterialservicerepairCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell rcmailsenddateCell =  row.createCell(5);
				  rcmailsenddateCell.setCellValue(form.getRc_mailsend_date());
				  rcmailsenddateCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell rcshipassigndateCell =  row.createCell(6);
				  rcshipassigndateCell.setCellValue(form.getRc_ship_assign_date());
				  rcshipassigndateCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell receptiondateCell =  row.createCell(7);
				  receptiondateCell.setCellValue(form.getReception_date());
				  receptiondateCell.setCellStyle(styleAlignCenter);
				  
				  String qa_reception_time ="";
				  String qa_referee_time ="";
				  if(form.getQa_reception_time()!=null){
					  qa_reception_time=
							  DateUtil.toString(DateUtil.toDate(form.getQa_reception_time(), DateUtil.DATE_TIME_PATTERN
									  ), DateUtil.DATE_PATTERN); 
				  }
				  if(form.getQa_referee_time()!=null){
					  qa_referee_time= 
							  DateUtil.toString(DateUtil.toDate(form.getQa_referee_time(), DateUtil.DATE_TIME_PATTERN
									  ), DateUtil.DATE_PATTERN); 
				  }
				  HSSFCell qareceptiontimeCell =  row.createCell(8);
				  qareceptiontimeCell.setCellValue(qa_reception_time);
				  qareceptiontimeCell.setCellStyle(styleAlignCenter);
				  HSSFCell qarefereetimeCell =  row.createCell(9);
				  qarefereetimeCell.setCellStyle(styleAlignCenter);
				  qarefereetimeCell.setCellValue(qa_referee_time);
				  
				  HSSFCell answerindeadlineCell =  row.createCell(10);
				  if(form.getQa_referee_time()!=null){
					  if(form.getAnswer_in_deadline()!=null){
						  switch(form.getAnswer_in_deadline()){
						    case "2":
					  		  answerindeadlineCell.setCellValue("◎");
					  		  row.getCell(10).setCellStyle(sharpStyle);
					  		  break;	
						    case "1":
						  		  answerindeadlineCell.setCellValue("○");
						  		  row.getCell(10).setCellStyle(styleAlignCenter);
						  		  break;
						  	case "0":
						  		  answerindeadlineCell.setCellValue("╳");
						  		  row.getCell(10).setCellStyle(styleRed);
						  		  break;
						  	default:
						  		 break;
						  } 
					  }			
				  }else{
					  answerindeadlineCell.setCellValue("");
					  answerindeadlineCell.setCellStyle(styleAlignCenter);
				  }
				  
				  HSSFCell qasecondaryrefereedateCell =  row.createCell(11);
				  qasecondaryrefereedateCell.setCellValue(form.getQa_secondary_referee_date());
				  qasecondaryrefereedateCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell rankCell =  row.createCell(12);
				  rankCell.setCellValue(form.getRank());
				  rankCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell servicefreeflgCell =  row.createCell(13);
				  servicefreeflgCell.setCellValue(CodeListUtils.getValue("service_free_flg", form.getService_free_flg()));
				  servicefreeflgCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell countermeasuresCell =  row.createCell(14);
				  countermeasuresCell.setCellValue(form.getCountermeasures());
				  countermeasuresCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell workshopCell =  row.createCell(15);
				  workshopCell.setCellValue(CodeListUtils.getValue("workshop", form.getWorkshop())); 
				  workshopCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell quotationdateCell =  row.createCell(16);
				  quotationdateCell.setCellValue(form.getQuotation_date());
				  quotationdateCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell agreeddateCell =  row.createCell(17);
				  agreeddateCell.setCellValue(form.getAgreed_date()); 
				  agreeddateCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell inlinedateCell =  row.createCell(18);
				  inlinedateCell.setCellValue(form.getInline_date());
				  inlinedateCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell outlinedateCell =  row.createCell(19);
				  outlinedateCell.setCellValue(form.getOutline_date());
				  outlinedateCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell unfixbackflgCell =  row.createCell(20);
				  if(form.getUnfix_back_flg()!=null){
					  switch(form.getUnfix_back_flg()){
					  	case "1":
					  		unfixbackflgCell.setCellValue(form.getOutline_date());
					  		  break;
					  	case "0":
					  		unfixbackflgCell.setCellValue("");
					  		  break;
					  	default:
					  		 break;
					  }
				  }
				  unfixbackflgCell.setCellStyle(styleAlignCenter);

				  
				  HSSFCell commentCell =  row.createCell(21);
				  commentCell.setCellValue(form.getComment());
				  commentCell.setCellStyle(styleAlignLeft);
				  
				  /*保内QIS分析表详细数据*/
				  HSSFCell analysis_noCell =  row.createCell(22);
				  analysis_noCell.setCellValue(form.getAnalysis_no());
				  analysis_noCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell customer_nameCell =  row.createCell(23);
				  customer_nameCell.setCellValue(form.getCustomer_name());
				  customer_nameCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell fix_demandCell =  row.createCell(24);
				  fix_demandCell.setCellValue(form.getFix_demand());
				  fix_demandCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell trouble_discribeCell =  row.createCell(25);
				  trouble_discribeCell.setCellValue(form.getTrouble_discribe());
				  trouble_discribeCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell trouble_causeCell =  row.createCell(26);
				  trouble_causeCell.setCellValue(form.getTrouble_cause());
				  trouble_causeCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell analysis_resultCell =  row.createCell(27);
				  analysis_resultCell.setCellValue(CodeListUtils.getValue("analysis_result", form.getAnalysis_result()));
				  analysis_resultCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell liability_flgCell =  row.createCell(28);
				  liability_flgCell.setCellValue(CodeListUtils.getValue("liability_flg", form.getLiability_flg()));
				  liability_flgCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell OGZCommentCell =  row.createCell(29);
				  
				  //判断OGZ修理备注--如果service_repair_analysis-维修场所标记值是1时，则显示OGZ，否则为空
				  String OGZCommentCellValue = "";
				  String OGZComment = form.getManufactory_flg();
				  if("1".equals(OGZComment)){
					  OGZCommentCellValue ="OGZ";
				  }else{
					  OGZCommentCellValue ="";
				  }
				  OGZCommentCell.setCellValue(OGZCommentCellValue);
				  OGZCommentCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell append_componentCell =  row.createCell(30);
				  append_componentCell.setCellValue(form.getAppend_component());
				  append_componentCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell quantityCell =  row.createCell(31);
				  quantityCell.setCellValue(form.getQuantity());
				  quantityCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell loss_amountCell =  row.createCell(32);
				  loss_amountCell.setCellValue(form.getLoss_amount());
				  loss_amountCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell last_sorc_noCell =  row.createCell(33);
				  last_sorc_noCell.setCellValue(form.getLast_sorc_no());
				  last_sorc_noCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell last_shipping_dateCell =  row.createCell(34);
				  last_shipping_dateCell.setCellValue(form.getLast_shipping_date());
				  last_shipping_dateCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell last_rankCell =  row.createCell(35);
				  String last_rank =  form.getLast_rank();
				  String last_rankCellValue ="";
				  if(last_rank!=null && !"".equals(last_rank)){
					  last_rankCellValue=last_rank; // CodeListUtils.getValue("material_level", last_rank) +"级"
				  }
				  last_rankCell.setCellValue(last_rankCellValue);
				  last_rankCell.setCellStyle(styleAlignCenter);
				  
				  HSSFCell last_trouble_featureCell =  row.createCell(36);
				  last_trouble_featureCell.setCellValue(form.getLast_trouble_feature());
				  last_trouble_featureCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell wash_featureCell =  row.createCell(37);
				  wash_featureCell.setCellValue(form.getWash_feature());
				  wash_featureCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell disinfect_featureCell =  row.createCell(38);
				  disinfect_featureCell.setCellValue(form.getDisinfect_feature());
				  disinfect_featureCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell steriliza_featureCell =  row.createCell(39);
				  steriliza_featureCell.setCellValue(form.getSteriliza_feature());
				  steriliza_featureCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell usage_frequencyCell =  row.createCell(40);
				  usage_frequencyCell.setCellValue(form.getUsage_frequency());
				  usage_frequencyCell.setCellStyle(styleAlignLeft);
				  
				  HSSFCell quality_info_noCell = row.createCell(41);
				  quality_info_noCell.setCellStyle(styleAlignLeft);
				  if(!CommonStringUtil.isEmpty(form.getQuality_info_no())){
					  quality_info_noCell.setCellValue(form.getQuality_info_no());
				  }
				  
				  HSSFCell qis_invoice_date = row.createCell(42);
				  qis_invoice_date.setCellStyle(styleAlignLeft);
				  if(!CommonStringUtil.isEmpty(form.getQis_invoice_date())){
					  qis_invoice_date.setCellValue(form.getQis_invoice_date());
				  }
				  
				  HSSFCell qis3_info = row.createCell(43);
				  qis3_info.setCellStyle(styleAlignLeft);
				  if(!CommonStringUtil.isEmpty(form.getQis3_info())){
					  qis3_info.setCellValue(form.getQis3_info());
				  }
				
			}
			out= new FileOutputStream(cachePath);
			work.write(out);
		} catch (FileNotFoundException e) {
				throw e;
		} catch (IOException e) {
				throw e;
		}finally{
			if(in!=null){
				in.close();
			}
			if(out!=null){
				out.close();
			}
		}
		return cacheName;
	}
	
	/**更新service_repair_manage表
	 * @throws Exception **/
	public void updateServiceRepairManage(ActionForm form,SqlSessionManager conn) throws Exception{
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		ServiceRepairManageEntity entity=new ServiceRepairManageEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		dao.updateServiceRepairManage(entity);
	}
	
	/**插入数据**/
	public void insertServiceRpairManage(ActionForm form,SqlSessionManager conn)throws Exception{
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		ServiceRepairManageEntity entity=new ServiceRepairManageEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		String strMaterial_id=dao.getMaxMaterialId("Q");
		if (strMaterial_id == null) {
			entity.setMaterial_id("Q0000000001");
		} else {
			String tempMaterial_id=strMaterial_id.substring(1, strMaterial_id.length());
			int material_id=Integer.valueOf(tempMaterial_id)+1;
			String maxMaterial_id=String.valueOf(material_id);
			for(int i=maxMaterial_id.length();i<10;i++){
				maxMaterial_id="0"+maxMaterial_id;
			}
			maxMaterial_id="Q"+maxMaterial_id;
			
			entity.setMaterial_id(maxMaterial_id);
		}
		dao.insertServiceRepairManage(entity);
	}

	/**插入数据
	 * @throws Exception **/
	public void insertServiceRepairManageFromMaterial(String material_id,SqlSessionManager conn) throws Exception {
		ServiceRepairManageMapper mapper = conn.getMapper(ServiceRepairManageMapper.class);
		ServiceRepairManageEntity entity=new ServiceRepairManageEntity();
		// 维修对象信息
		entity.setMaterial_id(material_id);


		// 查询维修对象信息
		List<MaterialEntity> sList=mapper.getRecept(entity);
		if (sList.size() == 1) {

			MaterialEntity mEntity = sList.get(0);

			// 如果是QIS
			if (mEntity.getService_repair_flg() == 2) {
				// 按型号+机身号匹配
				List<String> qa_material_ids = mapper.matchQis(mEntity.getModel_name(), mEntity.getSerial_no());
				
				// 按机身号匹配
				if (qa_material_ids.size() != 1) {
					qa_material_ids = mapper.matchQis(null, mEntity.getSerial_no());
				}
				// 找到就替换成RVS的material_id
				if (qa_material_ids.size() == 1) {
					mapper.updateMaterialId(material_id, qa_material_ids.get(0));
					return;
				}
			}
			
			// 确认是否已经建立保内判定作业
			List<ServiceRepairManageEntity> serviceRepairs = mapper.searchServiceRepair(entity);
			if (serviceRepairs.size() == 0) {
				entity.setModel_name(mEntity.getModel_name());
				entity.setSerial_no(mEntity.getSerial_no());
				entity.setSorc_no(mEntity.getSorc_no());
				entity.setReception_date(mEntity.getReception_time());
				entity.setService_repair_flg(mEntity.getService_repair_flg());
				entity.setRc_mailsend_date(new Date());
				String levelText = CodeListUtils.getValue("material_level", "" + mEntity.getLevel());
				if (!isEmpty(levelText)) {
					entity.setRank(levelText+"级");
				}
				if (mEntity.getDirect_flg() != null && 1 == mEntity.getDirect_flg()) {
					entity.setWorkshop(80); // 直送 
				}

				// 插入
				mapper.insertServiceRepairManage(entity);
			}
		}
	}
	
	public void validate(ActionForm form, List<MsgInfo> errors){
		ServiceRepairManageForm serviceRepairManageForm=(ServiceRepairManageForm)form;
		if("1".equals(serviceRepairManageForm.getService_repair_flg()) && isEmpty(serviceRepairManageForm.getRank())){
			MsgInfo info=new MsgInfo();
			info.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("validator.required","等级"));
			info.setErrcode("validator.required");
			errors.add(info);
		}
	}

	public void deleteServiceRepairManage(ActionForm form, SqlSessionManager conn) throws Exception {
		ServiceRepairManageMapper mapper=conn.getMapper(ServiceRepairManageMapper.class);
		ServiceRepairManageEntity entity=new ServiceRepairManageEntity();
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		mapper.deleteServiceRepairManage(entity);
	}

	/**
	 * 取得报价需要的品保判定信息
	 * @param detailResponse
	 * @param material_id
	 * @param conn
	 */
	public void getQaInfo2Quotation(Map<String, Object> detailResponse, String material_id, SqlSession conn) {
		ServiceRepairManageMapper mapper=conn.getMapper(ServiceRepairManageMapper.class);

		// 考虑如果发生系统内返还
		List<ServiceRepairManageEntity> results = mapper.searchServiceRepairByMaterial_id(material_id);

		if (results.size() == 1) {
			ServiceRepairManageEntity result = results.get(0);
			detailResponse.put("qa_rank", result.getCountermeasures());
			detailResponse.put("qa_service_free", CodeListUtils.getValue("service_free_flg", "" + result.getService_free_flg()));
		}
	}
	/**
	 * 保期内返品+QIS品分析 详细数据
	 * @param instance
	 * @param conn
	 * @return
	 */
	public ServiceRepairManageForm searchServiceRepairAnalysis(ActionForm form,SqlSession conn,Map<String, Object> listResponse) throws Exception {
		
		ServiceRepairManageForm serviceRepairManageForm = new ServiceRepairManageForm();
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		
		ServiceRepairManageEntity entity=new ServiceRepairManageEntity();
		//复制表单数据到对象
		BeanUtil.copyToBean(form, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//获取分析表的数据
		ServiceRepairManageEntity resultEntity = dao.searchServiceRepairAnalysis(entity);
		
		if(resultEntity!=null){
			BeanUtil.copyToForm(resultEntity, serviceRepairManageForm,CopyOptions.COPYOPTIONS_NOEMPTY);
			serviceRepairManageForm.setService_repair_flg(CodeListUtils.getValue("qa_material_service_repair", serviceRepairManageForm.getService_repair_flg()));
		}
		
		return serviceRepairManageForm;
	}
	
	
	/**
	 * 更新分析表数据
	 * @param form
	 * @param conn
	 * @throws Exception
	 */
	public void updateServiceRepairManageAnalysis(ServiceRepairManageForm serviceRepairManageForm,SqlSessionManager conn)throws Exception{
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		ServiceRepairManageEntity entity=new ServiceRepairManageEntity();
		BeanUtil.copyToBean(serviceRepairManageForm, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		//更新保内QIS分析表数据
		dao.updateServiceRepairAnalysis(entity);
	}
	

	/**
	 * 读取上传多个图片的uuid和seq_no
	 * 
	 * @param request
	 *            页面请求
	 * @return 返回值
	 */
	public List<ServiceRepairManageForm> getPostKeys(Map<String, String[]> parameters) {

		Pattern p = Pattern.compile("(\\w+).(\\w+)\\[(\\d+)\\]");
		List<ServiceRepairManageForm>  serviceRepairManageForms= new AutofillArrayList<ServiceRepairManageForm>(ServiceRepairManageForm.class);
		// 整理提交数据
		for (String parameterKey : parameters.keySet()) {
			Matcher m = p.matcher(parameterKey);
			if (m.find()) {
				String entity = m.group(1);
				if ("append_images".equals(entity)) {
					String column = m.group(2);
					int icounts = Integer.parseInt(m.group(3));
					String[] value = parameters.get(parameterKey);

					if ("image_uuid".equals(column)) {
						serviceRepairManageForms.get(icounts).setImage_uuid(value[0]);
					} else if ("seq_no".equals(column)) {
						serviceRepairManageForms.get(icounts).setSeq_no(value[0]);
					}
				}
			}
		}

		return serviceRepairManageForms;
	}

	/**
	 * 更新分析表数据
	 * @param form
	 * @param conn
	 * @throws Exception
	 */
	public void updateMention(ServiceRepairManageForm serviceRepairManageForm,SqlSessionManager conn)throws Exception{
		ServiceRepairManageMapper dao=conn.getMapper(ServiceRepairManageMapper.class);
		ServiceRepairManageEntity entity=new ServiceRepairManageEntity();
		BeanUtil.copyToBean(serviceRepairManageForm, entity, CopyOptions.COPYOPTIONS_NOEMPTY);
		//更新提要内容
		dao.updateMention(entity);
	}

	/**
	 * 删除保内返品分析图像
	 * @param serviceRepairManageForm 页面表单
	 * @param conn 数据库会话
	 */
	public void deleteImage(ServiceRepairManageForm serviceRepairManageForm,
			SqlSessionManager conn) {
		
		ServiceRepairManageEntity conditionEntity = new ServiceRepairManageEntity();
		
		ServiceRepairManageMapper dao = conn.getMapper(ServiceRepairManageMapper.class);
		
		BeanUtil.copyToBean(serviceRepairManageForm, conditionEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		
		//删除
		dao.deleteAnalysisGram(conditionEntity);
		
	}

	/**
	 * 已开始判定任务退回等待
	 * @throws Exception 
	 */
	public void actionBack(ActionForm form, SqlSessionManager conn,
			List<MsgInfo> errors) throws Exception {
		ServiceRepairManageEntity conditionEntity = new ServiceRepairManageEntity();
		BeanUtil.copyToBean(form, conditionEntity, CopyOptions.COPYOPTIONS_NOEMPTY);
		ServiceRepairManageMapper srMapper = conn.getMapper(ServiceRepairManageMapper.class);
		SoloProductionFeatureMapper spfMapper = conn.getMapper(SoloProductionFeatureMapper.class);

		// 检查是否存在进行中任务
		List<String> lis = spfMapper.checkWorkingByModelName(conditionEntity);

		// 如果存在不允许退回
		if (lis.size() > 0) {
			MsgInfo msgInfo = new MsgInfo();
			msgInfo.setComponentid("qa_reception_time");
			msgInfo.setErrcode("info.qa.undo.working");
			msgInfo.setErrmsg(ApplicationMessage.WARNING_MESSAGES.getMessage("info.qa.undo.working"));
			errors.add(msgInfo);
			return;
		}
		
		// 修改管理信息
		srMapper.undoRefeeWork(conditionEntity);
		// 删除作业信息
		spfMapper.undoWorkingByModelName(conditionEntity);
	}
}
