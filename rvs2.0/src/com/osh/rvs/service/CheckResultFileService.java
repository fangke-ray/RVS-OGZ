package com.osh.rvs.service;

import static com.osh.rvs.service.CheckResultService.ELECTRIC_IRON_FILE_A;
import static com.osh.rvs.service.CheckResultService.TYPE_FILED_MONTH;
import static com.osh.rvs.service.CheckResultService.TYPE_FILED_WEEK_OF_MONTH;
import static com.osh.rvs.service.CheckResultService.TYPE_FILED_YEAR;
import static com.osh.rvs.service.CheckResultService.TYPE_ITEM_MONTH;
import static com.osh.rvs.service.CheckResultService.getDayOfAxis;
import static com.osh.rvs.service.CheckResultService.getMaxAxis;
import static com.osh.rvs.service.CheckResultService.getNoScale;
import static com.osh.rvs.service.CheckResultService.getStartOfPeriod;
import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.osh.rvs.bean.infect.CheckResultEntity;
import com.osh.rvs.bean.infect.CheckedFileStorageEntity;
import com.osh.rvs.bean.infect.ElectricIronDeviceEntity;
import com.osh.rvs.bean.infect.JigCheckResultEntity;
import com.osh.rvs.bean.infect.TorsionDeviceEntity;
import com.osh.rvs.bean.master.CheckFileManageEntity;
import com.osh.rvs.bean.master.DevicesManageEntity;
import com.osh.rvs.bean.master.JigManageEntity;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.bean.master.SectionEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.common.XlsUtil;
import com.osh.rvs.mapper.infect.CheckResultMapper;
import com.osh.rvs.mapper.infect.ElectricIronDeviceMapper;
import com.osh.rvs.mapper.infect.JigCheckResultMapper;
import com.osh.rvs.mapper.infect.TorsionDeviceMapper;
import com.osh.rvs.mapper.master.CheckFileManageMapper;
import com.osh.rvs.mapper.master.DevicesManageMapper;
import com.osh.rvs.mapper.master.JigManageMapper;
import com.osh.rvs.mapper.master.LineMapper;
import com.osh.rvs.mapper.master.PositionMapper;
import com.osh.rvs.mapper.master.SectionMapper;

import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.copy.DateUtil;
import framework.huiqing.common.util.message.ApplicationMessage;

public class CheckResultFileService {

	Logger _logger = Logger.getLogger(CheckResultFileService.class);

	/** ============ 归档 =========== 
	 * @throws IOException */
	public void makeFileGroup(CheckedFileStorageEntity cfsEntity,
			List<String> sEncodedDeviceList, SqlSession conn) throws IOException {
		Date today = new Date();

		// 当日
		Calendar adjustCal = Calendar.getInstance();
		adjustCal.setTime(cfsEntity.getFiling_date());
		adjustCal.set(Calendar.HOUR_OF_DAY, 0);
		adjustCal.set(Calendar.MINUTE, 0);
		adjustCal.set(Calendar.SECOND, 0);
		adjustCal.set(Calendar.MILLISECOND, 0);

		DevicesManageMapper dmMapper = conn.getMapper(DevicesManageMapper.class);

		// 取得管理票文件作为模板
		String check_file_manage_file_name = cfsEntity.getTemplate_file_name();
		String srcPathPart = PathConsts.BASE_PATH + PathConsts.DEVICEINFECTION + "\\" + check_file_manage_file_name;
		String ext = ".xlsx";
		String srcPath = srcPathPart + ext;
		File srcFile = new File(srcPath);
		if (!srcFile.exists()) {
			ext = ".xls";
			srcPath = srcPathPart + ext;
		}

		// 取得点检表信息
		CheckFileManageMapper cfmMapper = conn.getMapper(CheckFileManageMapper.class);
		String check_file_manage_id = cfsEntity.getCheck_file_manage_id();
		CheckFileManageEntity cfmEntity = cfmMapper.getByKey(check_file_manage_id);

		// 复制模板到临时文件
		String cacheFilename =  cfsEntity.getStorage_file_name() + today.getTime() + ext;
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM") + "\\" + cacheFilename;
		FileUtils.copyFile(new File(srcPath), new File(cachePath));
		// SAMPLE D:\rvs\Infections\147P\QR-B31002-10A\QR-B31002-10A_报价物料课 受理报价_147P12月.xls
		String targetPath = PathConsts.BASE_PATH + PathConsts.INFECTIONS + "\\" + 
				RvsUtils.getBussinessYearString(adjustCal) + "\\" +
				cfmEntity.getCheck_manage_code();
		String targetFile = targetPath + "\\" + cfsEntity.getStorage_file_name() + ".pdf";

		CheckResultMapper crMapper = conn.getMapper(CheckResultMapper.class);

		XlsUtil cacheXls = null;
		try {
			cacheXls = new XlsUtil(cachePath, false);
			cacheXls.SelectActiveSheet();

			// 取得本期
			String bperiod = RvsUtils.getBussinessYearString(adjustCal);

			// 工程
			String line_id = cfsEntity.getLine_id();
			String sLineName = "";
			SectionMapper sMapper = conn.getMapper(SectionMapper.class);
			SectionEntity sEntity = sMapper.getSectionByID(cfsEntity.getSection_id());
			if (sEntity != null) {
				sLineName += sEntity.getName() + "\n";
			}
			if (!isEmpty(line_id)) {
				LineMapper lMapper = conn.getMapper(LineMapper.class);
				LineEntity lEntity = lMapper.getLineByID(line_id);
				if (lEntity != null) {
					sLineName += lEntity.getName();
				}
				cacheXls.Replace("#G[LINE#", sLineName);
			}
			// 工位
			String position_id = cfsEntity.getPosition_id();
			if (!isEmpty(position_id)) {
				PositionMapper pMapper = conn.getMapper(PositionMapper.class);
				// 工位
				PositionEntity pEntity = pMapper.getPositionByID(cfsEntity.getPosition_id());
				if (pEntity != null) {
					sLineName += pEntity.getProcess_code() + " ";
				}
				Dispatch positionCell = cacheXls.Locate("#G[POSITION#");
				if (positionCell != null) {
					String FoundValue = Dispatch.get(positionCell, "Value").toString();
					if (FoundValue.equals("#G[POSITION#")) {
						cacheXls.SetValue(positionCell, sLineName);
					} else {
						cacheXls.Replace("#G[POSITION#", sLineName.replaceAll("\\\n", " "));
					}
				}
			}

			// 替换共通数据
			cacheXls.Replace("#G[PERIOD#", bperiod);
			cacheXls.Replace("#G[PERIODC#", bperiod.replaceAll("P", ""));
			cacheXls.Replace("#G[YEAR#", DateUtil.toString(cfsEntity.getFiling_date(), "yyyy"));
			cacheXls.Replace("#G[MONTH#", DateUtil.toString(cfsEntity.getFiling_date(), "M"));

			// 文档章
			//#J
			Dispatch cell = cacheXls.Locate("#J");
			while (cell != null) {
				String stamp = Dispatch.get(cell, "Value").toString();
				String jobNo = stamp.replaceAll("#J\\[(.*)#", "$1");
				cacheXls.SetValue(cell, "");
				cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell);
				cell = cacheXls.Locate("#J");
			}

			// 确定表单的归档类型
			int fileAxisType = 0; 
			if (CheckFileManageEntity.ACCESS_PLACE_DAILY == cfmEntity.getAccess_place()) {
				fileAxisType = TYPE_FILED_MONTH;
			}

			// 计算范围用日历
			Calendar monCal = Calendar.getInstance();
			if (CheckFileManageEntity.ACCESS_PLACE_DAILY == cfmEntity.getAccess_place()) {
				monCal.setTimeInMillis(adjustCal.getTimeInMillis());
				monCal.set(Calendar.DATE, 1);
			} else {
				if (TYPE_FILED_YEAR == fileAxisType) {
					// 去期间头
					monCal = getStartOfPeriod(adjustCal);
				} else {
					// 去月首
					monCal.setTimeInMillis(adjustCal.getTimeInMillis());
					monCal.set(Calendar.DATE, 1);
					if (TYPE_FILED_WEEK_OF_MONTH == fileAxisType) {
						int week = monCal.get(Calendar.DAY_OF_WEEK);
						if (week == Calendar.SUNDAY) {
							monCal.add(Calendar.DATE, 1);
						} else if (week == Calendar.MONDAY) {
						} else {
							monCal.add(Calendar.DATE, 9 - week);
						}
					}
				}
			}

			Integer itemType = 0;
			int axis = getMaxAxis(itemType, fileAxisType);

			// 取得输入项定位
			Map<String, CheckPosBean> checkPosData = new HashMap<String, CheckPosBean>();
			List<CheckPosBean> checkPosDate = new ArrayList<CheckPosBean>();
			List<CheckPosBean> checkPosName = new ArrayList<CheckPosBean>();
			CheckPosBean checkPosManageNo = new CheckPosBean();
			CheckPosBean checkPosModel = new CheckPosBean();
			CheckPosBean checkDeviceName = new CheckPosBean();

			CheckPosBean checkUseStart = new CheckPosBean();
			CheckPosBean checkUseEnd = new CheckPosBean();

			cell = cacheXls.Locate("#D");
			String FoundValue = null;
			if (cell != null) {
				FoundValue = Dispatch.get(cell, "Value").toString();
			}
			while (FoundValue != null) {
				cell = cacheXls.Locate("#D");
				if (cell == null) {
					FoundValue = null;
				} else {
					FoundValue = Dispatch.get(cell, "Value").toString();
					CheckPosBean cpBean = new CheckPosBean();
					cpBean.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
					cpBean.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
					cpBean.content = FoundValue.replaceAll("#[^#]*#", "#tag#");
					String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
					String[] tags = tagText.split("\\[");
					String seq = null; 
					for (String tag : tags) {
						if (tag.startsWith("S")) {
							// 取得序列信息
							seq = tag.substring(1, 3);
						}
						else if (tag.startsWith("T")) {
							// 单元格中的跳动
							cpBean.shiftX = Integer.parseInt(tag.substring(1));
						}
						else if (tag.startsWith("U")) {
							// 单元格中的跳动
							cpBean.shiftY = Integer.parseInt(tag.substring(1));
						}
					}
					checkPosData.put(seq, cpBean);

					cacheXls.SetValue(cell, "");
				}
			}

			// #G[MANAGENO
			cell = cacheXls.Locate("#G[MANAGENO");
			if (cell != null) {
				FoundValue = Dispatch.get(cell, "Value").toString();
				checkPosManageNo.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
				checkPosManageNo.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
				String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
				String[] tags = tagText.split("\\[");
				for (String tag : tags) {
					if (tag.startsWith("U")) {
						// 单元格中的跳动
						checkPosManageNo.shiftY = Integer.parseInt(tag.substring(1));
					}
				}
			}

			// #G[MODEL[U1#	#G[USESTART[U1#	#G[USELIMIT[U1#
			cell = cacheXls.Locate("#G[MODEL");
			if (cell != null) {
				FoundValue = Dispatch.get(cell, "Value").toString();
				checkPosModel.content = FoundValue;
				checkPosModel.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
				checkPosModel.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
				String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
				String[] tags = tagText.split("\\[");
				for (String tag : tags) {
					if (tag.startsWith("U")) {
						// 单元格中的跳动
						checkPosModel.shiftY = Integer.parseInt(tag.substring(1));
					}
				}
			}

			// #G[NAME
			cell = cacheXls.Locate("#G[NAME");
			if (cell != null) {
				FoundValue = Dispatch.get(cell, "Value").toString();
				checkDeviceName.content = FoundValue;
				checkDeviceName.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
				checkDeviceName.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
				String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
				String[] tags = tagText.split("\\[");
				for (String tag : tags) {
					if (tag.startsWith("U")) {
						// 单元格中的跳动
						checkDeviceName.shiftY = Integer.parseInt(tag.substring(1));
					}
				}
			}

			cell = cacheXls.Locate("#G[USESTART");
			if (cell != null) {
				FoundValue = Dispatch.get(cell, "Value").toString();
				checkUseStart.content = FoundValue;
				checkUseStart.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
				checkUseStart.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
				String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
				String[] tags = tagText.split("\\[");
				for (String tag : tags) {
					if (tag.startsWith("U") && !tag.startsWith("USE")) {
						// 单元格中的跳动
						checkUseStart.shiftY = Integer.parseInt(tag.substring(1));
					}
				}
			}

			cell = cacheXls.Locate("#G[USELIMIT");
			if (cell != null) {
				FoundValue = Dispatch.get(cell, "Value").toString();
				checkUseEnd.content = FoundValue;
				checkUseEnd.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
				checkUseEnd.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
				String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
				String[] tags = tagText.split("\\[");
				for (String tag : tags) {
					if (tag.startsWith("U") && !tag.startsWith("USE")) {
						// 单元格中的跳动
						checkUseEnd.shiftY = Integer.parseInt(tag.substring(1));
					} else if (tag.startsWith("P")) {
						checkUseEnd.shiftX = Integer.parseInt(tag.substring(1));
					}
				}
			}

			cell = cacheXls.Locate("#T");
			while (cell != null) {
				FoundValue = Dispatch.get(cell, "Value").toString();
				CheckPosBean aCheckPosDate = new CheckPosBean(); 
				aCheckPosDate.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
				aCheckPosDate.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
				String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
				String[] tags = tagText.split("\\[");
				for (int i=1 ; i< tags.length; i++) {
					String tag = tags[i];
					if (tag.startsWith("T")) {
						// 单元格中的跳动
						aCheckPosDate.shiftX = Integer.parseInt(tag.substring(1));
					}
				}
				aCheckPosDate.content = tagText.substring(2, 3);
				checkPosDate.add(aCheckPosDate);
				cacheXls.SetValue(cell, "");
				cell = cacheXls.Locate("#T");
			}

			cell = cacheXls.Locate("#N");
			while (cell != null) {
				FoundValue = Dispatch.get(cell, "Value").toString();
				CheckPosBean aCheckPosName = new CheckPosBean(); 
				aCheckPosName.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
				aCheckPosName.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
				String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
				String[] tags = tagText.split("\\[");
				aCheckPosName.shiftY = 0;
				for (String tag : tags) {
					if (tag.startsWith("T")) {
						// 单元格中的跳动
						aCheckPosName.shiftX = Integer.parseInt(tag.substring(1));
					} else if (tag.startsWith("U")) {
						aCheckPosName.shiftY = Integer.parseInt(tag.substring(1));
					}
				}
				checkPosName.add(aCheckPosName);
				cacheXls.SetValue(cell, "");
				cell = cacheXls.Locate("#N");
			}

			// 所有设备一览
//			if ("53".equals(check_file_manage_id)) {
//				// 电烙铁工具
//				setDeviceElectricIron(cacheXls, sEncodedDeviceList, axis, fileAxisType, 
//						cfmEntity, crMapper, monCal, conn);
//			} else if ("98".equals(check_file_manage_id)) {
//				// 力矩工具
//				setDeviceTrosion(cacheXls, sEncodedDeviceList, axis, fileAxisType, 
//						cfmEntity, crMapper, monCal, check_file_manage_id, conn);
//			} else {
				// 普通设备工具
				setDeviceNormal(cacheXls, sEncodedDeviceList, checkPosData, checkPosManageNo, checkPosModel, 
						checkDeviceName, checkUseStart, checkUseEnd, checkPosDate, checkPosName, axis, fileAxisType, 
						cfmEntity, crMapper, monCal, conn);
//			}

			// #P QR-B31002-12A_B038_147P12月
			// 取得参照信息<refer
			cell = cacheXls.Locate("#P");
			FoundValue = null;
			List<CheckResultEntity> refers = null;			
			if (cell != null) {
				FoundValue = Dispatch.get(cell, "Value").toString();

				CheckResultEntity cre = new CheckResultEntity();
				cre.setCheck_confirm_time_start(cfsEntity.getStart_record_date());
				cre.setCheck_confirm_time_end(cfsEntity.getFiling_date());
				cre.setCheck_file_manage_id(cfsEntity.getCheck_file_manage_id());
				cre.setManage_id(sEncodedDeviceList.get(0));
				refers = crMapper.getDeviceReferInPeriod(cre);
			}
			while (FoundValue != null) {
				int vIdex = FoundValue.indexOf("[I");
				if (vIdex < 0) {
					vIdex = FoundValue.indexOf("[C");
				}
				String seq = FoundValue.substring(vIdex + 2, vIdex + 4);
				if (refers.size() > 0) {
					for (CheckResultEntity refer : refers) {
						if (seq.equals(refer.getItem_seq())) {
							if (FoundValue.indexOf("#P[I") >= 0) {
								// 输入
								FoundValue = FoundValue.replaceAll("#P\\[I"+seq+"[^#]*#", getNoScale(refer.getDigit()));
							} if (FoundValue.indexOf("#P[C") >= 0) {
								// 选择项
								String choosedValue = getNoScale(refer.getDigit());
								FoundValue = FoundValue.replaceAll("#P\\[C"+seq+"[^#]*\\[V"+choosedValue+"[^#]*#", "☑"); // √
								FoundValue = FoundValue.replaceAll("#P\\[C"+seq+"[^#]*\\[V[^#]*#", "□");
							}
							cacheXls.SetValue(cell, FoundValue);
							break;
						}
					}
				} else {
					cacheXls.SetValue(cell, "");
				}

				FoundValue = null;
				cell = cacheXls.Locate("#P");
				if (cell != null) FoundValue = Dispatch.get(cell, "Value").toString();
			}

//			cacheXls.Replace(source, target);
//			"#[^#]*#"
			File fTargetPath = new File(targetPath);
			if (!fTargetPath.exists()) {
				fTargetPath.mkdirs();
			}
			cacheXls.SaveAsPdf(targetFile); // SaveAsPdf
			cacheXls.Release();

		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			if (cacheXls != null) {
				cacheXls.CloseExcel(false);
			}
		} finally {
			cacheXls = null;
		}

		// 查询备注
		// 备注信息
		List<Map<String, String>> comments = new ArrayList<Map<String, String>>();
		DevicesManageEntity conditionOfDevice = new DevicesManageEntity();
		conditionOfDevice.setProvide_date_start(cfsEntity.getStart_record_date());
		conditionOfDevice.setProvide_date_end(cfsEntity.getFiling_date());
		CheckResultEntity conditionOfComment = new CheckResultEntity();
		conditionOfComment.setCheck_confirm_time_start(cfsEntity.getStart_record_date());
		conditionOfComment.setCheck_confirm_time_end(cfsEntity.getFiling_date());
		for (int iDev = 0; iDev < sEncodedDeviceList.size(); iDev ++) {
			// 取得管理设备信息
			String devices_manage_id = sEncodedDeviceList.get(iDev);
			conditionOfDevice.setDevices_manage_id(devices_manage_id);
			DevicesManageEntity provide_date = dmMapper.checkProvideInPeriod(conditionOfDevice);
			DevicesManageEntity waste_date = dmMapper.checkWasteInPeriod(conditionOfDevice);
			// 发布日期
			if (provide_date != null) {
				Map<String, String> comment = new HashMap<String, String>();
				comment.put("manage_code", provide_date.getManage_code());
				comment.put("job_no", provide_date.getProvider());
				comment.put("comment", ApplicationMessage.WARNING_MESSAGES.getMessage("info.infect.device.filing.provide", 
						provide_date.getManage_code(), provide_date.getProcess_code()));
				comment.put("comment_date", DateUtil.toString(provide_date.getProvide_date(), DateUtil.ISO_DATE_PATTERN));
				comments.add(comment);
			}
			// 废弃日期
			if (waste_date != null) {
				Map<String, String> comment = new HashMap<String, String>();
				comment.put("manage_code", waste_date.getManage_code());
				comment.put("job_no", waste_date.getProvider());
				comment.put("comment", ApplicationMessage.WARNING_MESSAGES.getMessage("info.infect.device.filing.waste", 
						waste_date.getManage_code(), waste_date.getProcess_code()));
				comment.put("comment_date", DateUtil.toString(waste_date.getWaste_date(), DateUtil.ISO_DATE_PATTERN));
				comments.add(comment);
			}
			// 备注信息\
			conditionOfComment.setManage_id(devices_manage_id);
			List<CheckResultEntity> commentsList = crMapper.getDeviceCheckCommentInPeriodByManageId(conditionOfComment);
			for (CheckResultEntity cre : commentsList) {
				Map<String, String> comment = new HashMap<String, String>();
				comment.put("manage_code", cre.getManage_code());
				comment.put("job_no", cre.getJob_no());
				comment.put("comment", cre.getComment());
				comment.put("comment_date", DateUtil.toString(cre.getCheck_confirm_time(), DateUtil.ISO_DATE_PATTERN));
				comments.add(comment);
			}
		}

		if (comments.size() > 0) {
			// 取得点检表信息
			String templateCommentFileXls = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\点检备注.xlsx";
			FileUtils.copyFile(new File(templateCommentFileXls), new File(cachePath + "_comment.xls"));

			String targetCommentFileXls = targetPath + "\\" + cfsEntity.getStorage_file_name() + "_comment.pdf";
			cacheXls = null;
			try {
				cacheXls = new XlsUtil(cachePath + "_comment.xls", false);
				cacheXls.SelectActiveSheet();

				int setLine = 5; // Const
				for (Map<String, String> comment : comments) {
					cacheXls.SetValue("B" + setLine, comment.get("manage_code"));
					cacheXls.SetValue("C" + setLine, comment.get("comment_date"));
					cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + comment.get("job_no").toUpperCase(), "D" + setLine);
					cacheXls.SetValue("E" + setLine, comment.get("comment"));
					setLine +=2;
				}

//				/ 保存到 PDF
				cacheXls.SaveAsPdf(targetCommentFileXls);
			} catch (Exception e) {
				_logger.error(e.getMessage(), e);
				if (cacheXls != null) {
					cacheXls.CloseExcel(false);
				}
			} finally {
				cacheXls = null;
			}
			// PDF 合并
			joinPdf(targetFile, targetCommentFileXls);
		}
	}

	private void joinPdf(String thisFilePath, String appedixFilePath) {
		// 用iText合并文件
		try {
			PdfReader reader;
			Document document = new Document();

			FileUtils.copyFile(new File(thisFilePath), new File(thisFilePath + "_body.pdf"));

			PdfCopy copy = new PdfCopy(document, new FileOutputStream(thisFilePath));
			document.open();

			PdfImportedPage newPage;

			reader = new PdfReader(thisFilePath+ "_body.pdf");
			int iPageNum = reader.getNumberOfPages();
			for (int j = 1; j <= iPageNum; j++) {
				document.newPage();
				newPage = copy.getImportedPage(reader, j);

				copy.addPage(newPage);
			}

			reader = new PdfReader(appedixFilePath);
			iPageNum = reader.getNumberOfPages();
			for (int j = 1; j <= iPageNum; j++) {
				document.newPage();
				newPage = copy.getImportedPage(reader, j);

				copy.addPage(newPage);
			}

			document.close();

			reader.close();

			new File(appedixFilePath).delete();
			new File(thisFilePath+ "_body.pdf").delete();

		} catch (Exception de) {
			System.out.println("rvsreport_" + de.getMessage());
		}
	}

	private static final int INSERT_START_ROW_FOR_EI =4;
	private static final int INSERT_START_ROW_FOR_TR =6;
	private void setDeviceElectricIron(XlsUtil cacheXls,
			List<String> sEncodedDeviceList, int axis, int axisType,
			CheckFileManageEntity cfmEntity, CheckResultMapper crMapper,
			Calendar monCal, SqlSession conn) {
		String check_file_manage_id = ELECTRIC_IRON_FILE_A;

		Date monthStart = monCal.getTime();
		// 月底
		Calendar cMonthEnd = Calendar.getInstance();
		cMonthEnd.setTimeInMillis(monCal.getTimeInMillis());
		cMonthEnd.add(Calendar.MONTH, 1);
		cMonthEnd.add(Calendar.DATE, -1);
		Date monthEnd = cMonthEnd.getTime();

		ElectricIronDeviceMapper eidMapper = conn.getMapper(ElectricIronDeviceMapper.class);
		int insertRow = INSERT_START_ROW_FOR_EI;

		for (int iDev = 0; iDev < sEncodedDeviceList.size(); iDev ++) {
			// 取得管理设备信息
			String devices_manage_id = sEncodedDeviceList.get(iDev);
			// DevicesManageEntity dmEntity = dmMapper.getByKey(devices_manage_id);

			CheckResultEntity enti = new CheckResultEntity();
			enti.setManage_id(devices_manage_id);
			enti.setCheck_file_manage_id(check_file_manage_id);

			enti.setCheck_confirm_time_start(monthStart);
			enti.setCheck_confirm_time_end(monthEnd);

			List<CheckResultEntity> lMonth = crMapper.getDeviceCheckInPeriod(enti);

			ElectricIronDeviceEntity eidCond = new ElectricIronDeviceEntity();
			eidCond.setManage_id(devices_manage_id);
			List<ElectricIronDeviceEntity> rsts = eidMapper.searchElectricIronDevice(eidCond);

			if (rsts.size() == 1) {
				// 单温
				// Sheets("Sheet2").Select
				cacheXls.getAndActiveSheetBySeq(2);

				// Rows("1:3").Select
				Dispatch selection = cacheXls.Select("1:2");
				// Selection.Copy
				Dispatch.call(selection, "Copy");

				// Sheets("Sheet1").Select
				cacheXls.getAndActiveSheetBySeq(1);
				// Rows("4:4").Select
				selection = cacheXls.Select(insertRow + ":" + insertRow);
				// Selection.Insert Shift:=xlDown
				Dispatch.call(selection, "Insert", new Variant(1));

				// 管理编号
				cacheXls.SetValue("A" + insertRow, rsts.get(0).getManage_code());
				// 类型
				cacheXls.SetValue("A" + (insertRow+1), CodeListUtils.getValue("electric_iron_kind_simple", "" + rsts.get(0).getKind()));

//				// 接地
//				if (val50.getDigit() != null)
//					cacheXls.SetValue("C" + insertRow, getNoScale(val50.getDigit().toPlainString()) + "Ω");
//				// 绝缘电阻
//				if (val51.getDigit() != null)
//					cacheXls.SetValue("C" + (insertRow+1), getNoScale(val51.getDigit().toPlainString()) + "MΩ");

				// 上下限
				cacheXls.SetValue("D" + insertRow, rsts.get(0).getTemperature_lower_limit().toString());
				cacheXls.SetValue("F" + insertRow, rsts.get(0).getTemperature_upper_limit().toString());

				// 每个单元格取值
				for (int iAxis=0;iAxis<=axis;iAxis++) {
					Date[] dates = getDayOfAxis(monCal, iAxis, axisType, cfmEntity.getCycle_type());

					CheckResultEntity cre = new CheckResultEntity();
					cre.setCheck_confirm_time_start(dates[0]);
					cre.setCheck_confirm_time_end(dates[1]);
					cre.setManage_id(devices_manage_id);
					cre.setCheck_file_manage_id(check_file_manage_id);

					// 已点检或之前范围
					List<CheckResultEntity> listCre = crMapper.getDeviceCheckInPeriod(cre);
					String jobNo = null;
					Dispatch cell = null;
					// 取得已点检单元格信息
					if (listCre != null && listCre.size() > 0) {
						for (CheckResultEntity rCre : listCre) {
							String itemSeq = rCre.getItem_seq();
							if (!itemSeq.equals(rsts.get(0).getSeq())) {
								break;
							}

							String cellName = XlsUtil.getExcelColCode(9 + iAxis - 1) 
									+ (insertRow);
							cell = cacheXls.getRange(cellName);
							cacheXls.SetValue(cell, getFileStatusD(""+rCre.getChecked_status(), rCre.getDigit()));

							if (jobNo == null) jobNo = rCre.getJob_no();
						}
					} else {
						String cellName = XlsUtil.getExcelColCode(9 + iAxis - 1) 
								+ (insertRow);
						cell = cacheXls.getRange(cellName);
						cacheXls.SetValue(cell, "/");
					}

					// 签章
					if (!isEmpty(jobNo)) {
						
						String cellName = XlsUtil.getExcelColCode(9 + iAxis - 1) 
								+ (insertRow + 1);
						cell = cacheXls.getRange(cellName);
						if (cfmEntity.getAccess_place() == 1) {
							cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign_v\\" + jobNo.toUpperCase(), cell);
						} else {
							cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell);
						}
					}
				}

				insertRow += 2;

			} else if (rsts.size() == 2) {
				// 双温
				cacheXls.getAndActiveSheetBySeq(2);

				Dispatch selection = cacheXls.Select("5:7");
				Dispatch.call(selection, "Copy");

				cacheXls.getAndActiveSheetBySeq(1);
				selection = cacheXls.Select(insertRow + ":" + insertRow);
				Dispatch.call(selection, "Insert", new Variant(1));

				// 管理编号
				cacheXls.SetValue("A" + insertRow, rsts.get(0).getManage_code());
				// 类型
				cacheXls.SetValue("A" + (insertRow+1), CodeListUtils.getValue("electric_iron_kind_simple", "" + rsts.get(0).getKind()));

//				// 接地
//				cacheXls.SetValue("C" + insertRow, getNoScale(val50.getDigit().toPlainString()) + "Ω");
//				// 绝缘电阻
//				cacheXls.SetValue("C" + (insertRow+2), getNoScale(val51.getDigit().toPlainString()) + "MΩ");

				// 上下限
				cacheXls.SetValue("D" + insertRow, rsts.get(0).getTemperature_lower_limit().toString());
				cacheXls.SetValue("F" + insertRow, rsts.get(0).getTemperature_upper_limit().toString());
				cacheXls.SetValue("D" + (insertRow+1), rsts.get(1).getTemperature_lower_limit().toString());
				cacheXls.SetValue("F" + (insertRow+1), rsts.get(1).getTemperature_upper_limit().toString());

				// 每个单元格取值
				for (int iAxis=0;iAxis<=axis;iAxis++) {
					Date[] dates = getDayOfAxis(monCal, iAxis, axisType, cfmEntity.getCycle_type());

					CheckResultEntity cre = new CheckResultEntity();
					cre.setCheck_confirm_time_start(dates[0]);
					cre.setCheck_confirm_time_end(dates[1]);
					cre.setManage_id(devices_manage_id);
					cre.setCheck_file_manage_id(check_file_manage_id);

					// 已点检或之前范围
					List<CheckResultEntity> listCre = crMapper.getDeviceCheckInPeriod(cre);
					String jobNo = null;
					Dispatch cell = null;
					// 取得已点检单元格信息
					if (listCre != null && listCre.size() > 0) {
						for (CheckResultEntity rCre : listCre) {
							String itemSeq = rCre.getItem_seq();
							int rowShift = 0;
							if (itemSeq.equals(rsts.get(1).getSeq())) {
								rowShift = 1;
							}
							else if (itemSeq.equals(rsts.get(0).getSeq())) {
								rowShift = 0;
							} else break;

							String cellName = XlsUtil.getExcelColCode(9 + iAxis - 1) 
									+ (insertRow + rowShift);
							cell = cacheXls.getRange(cellName);
							cacheXls.SetValue(cell, getFileStatusD(""+rCre.getChecked_status(), rCre.getDigit()));

							if (jobNo == null) jobNo = rCre.getJob_no();
						}
					} else {
						String cellName = XlsUtil.getExcelColCode(9 + iAxis - 1) 
								+ (insertRow);
						cell = cacheXls.getRange(cellName);
						cacheXls.SetValue(cell, "/");
						cellName = XlsUtil.getExcelColCode(9 + iAxis - 1) 
								+ (insertRow + 1);
						cell = cacheXls.getRange(cellName);
						cacheXls.SetValue(cell, "/");
					}

					// 签章
					if (!isEmpty(jobNo)) {
						
						String cellName = XlsUtil.getExcelColCode(9 + iAxis - 1) 
								+ (insertRow + 2);
						cell = cacheXls.getRange(cellName);
						if (cfmEntity.getAccess_place() == 1) {
							cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign_v\\" + jobNo.toUpperCase(), cell);
						} else {
							cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell);
						}
					}
				}

				insertRow += 3;
			}
		}
	}

	/**
	 * 设定力矩工具表格
	 * 
	 * @param cacheXls
	 * @param sEncodedDeviceList
	 * @param axis
	 * @param axisType
	 * @param cfmEntity
	 * @param crMapper
	 * @param monCal
	 * @param conn
	 */
	private void setDeviceTrosion(XlsUtil cacheXls,
			List<String> sEncodedDeviceList, int axis, int axisType,
			CheckFileManageEntity cfmEntity, CheckResultMapper crMapper,
			Calendar monCal, String check_file_manage_id, SqlSession conn) {


		TorsionDeviceMapper tdMapper = conn.getMapper(TorsionDeviceMapper.class);
		int insertRow = INSERT_START_ROW_FOR_TR;

		for (int iDev = 0; iDev < sEncodedDeviceList.size(); iDev ++) {
			// 取得管理设备信息
			String devices_manage_id = sEncodedDeviceList.get(iDev);
			// DevicesManageEntity dmEntity = dmMapper.getByKey(devices_manage_id);

			TorsionDeviceEntity tdCond = new TorsionDeviceEntity();
			tdCond.setManage_id(devices_manage_id);
			List<TorsionDeviceEntity> rsts = tdMapper.searchTorsionDevice(tdCond);

			int rstsSize = rsts.size();
			if (rstsSize == 1) {

				// Sheets("Sheet2").Select
				cacheXls.getAndActiveSheetBySeq(2);

				// Rows("1:3").Select
				Dispatch selection = cacheXls.Select("2:4");
				// Selection.Copy
				Dispatch.call(selection, "Copy");

				// Sheets("Sheet1").Select
				cacheXls.getAndActiveSheetBySeq(1);
				// Rows("4:4").Select
				selection = cacheXls.Select(insertRow + ":" + insertRow);
				// Selection.Insert Shift:=xlDown
				Dispatch.call(selection, "Insert", new Variant(1));
				TorsionDeviceEntity trTorsion = rsts.get(0);

				// 规格力矩值 BASE
				cacheXls.SetValue("A" + insertRow, getNoScale(trTorsion.getRegular_torque()));
				// 规格力矩值 DIFF
				cacheXls.SetValue("C" + insertRow, getNoScale(trTorsion.getDeviation()));
				// 使用的工程
				cacheXls.SetValue("E" + insertRow, trTorsion.getUsage_point());
				// HP-10 HP-100
				cacheXls.SetValue("F" + insertRow, CodeListUtils.getValue("torsion_device_hp_scale", ""+trTorsion.getHp_scale(), ""));
				// 点检力矩[N·m]下限
				cacheXls.SetValue("G" + insertRow, trTorsion.getRegular_torque_lower_limit().toPlainString());
				// 点检力矩[N·m]上限
				cacheXls.SetValue("I" + insertRow, trTorsion.getRegular_torque_upper_limit().toPlainString());
				// 管理编号
				cacheXls.SetValue("J" + insertRow, trTorsion.getManage_code());

				// 每个单元格取值
				for (int iAxis=0;iAxis<=axis;iAxis++) {
					Date[] dates = getDayOfAxis(monCal, iAxis, axisType, cfmEntity.getCycle_type());

					CheckResultEntity cre = new CheckResultEntity();
					cre.setCheck_confirm_time_start(dates[0]);
					cre.setCheck_confirm_time_end(dates[1]);
					cre.setManage_id(devices_manage_id);
					cre.setCheck_file_manage_id(check_file_manage_id);

					// 已点检或之前范围
					List<CheckResultEntity> listCre = crMapper.getDeviceCheckInPeriod(cre);
					Dispatch cell = null;
					// 取得已点检单元格信息
					if (listCre != null && listCre.size() > 0) {
						String jobNo = null;
						Date getDate = null;

						for (CheckResultEntity rCre : listCre) {
							String itemSeq = rCre.getItem_seq();
							if (!itemSeq.equals(rsts.get(0).getSeq())) {
								break;
							}

							String cellName = XlsUtil.getExcelColCode(12 + iAxis - 1) 
									+ (insertRow);
							cell = cacheXls.getRange(cellName);
							cacheXls.SetValue(cell, getFileStatusD(""+rCre.getChecked_status(), rCre.getDigit()));

							if (jobNo == null) jobNo = rCre.getJob_no();
							if (getDate == null) getDate = rCre.getCheck_confirm_time();
						}

						// 日期行
						if (getDate != null) {
							String cellName = XlsUtil.getExcelColCode(12 + iAxis - 1) 
									+ (insertRow + 1);
							cell = cacheXls.getRange(cellName);
							cacheXls.SetValue(cell, DateUtil.toString(getDate, "MM-dd"));
							cacheXls.SetNumberFormatLocal(cell, "m-d");
						}

						// 签章行
						if (!isEmpty(jobNo)) {
							
							String cellName = XlsUtil.getExcelColCode(12 + iAxis - 1) 
									+ (insertRow + 2);
							cell = cacheXls.getRange(cellName);
							if (cfmEntity.getAccess_place() == 1) {
								cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign_v\\" + jobNo.toUpperCase(), cell);
							} else {
								cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell);
							}
						}
					} else {
						String cellName = XlsUtil.getExcelColCode(12 + iAxis - 1) 
								+ (insertRow);
						cell = cacheXls.getRange(cellName);
						cacheXls.SetValue(cell, "/");
					}
				}

				insertRow += 3;
			} else if (rstsSize > 1) {
				// Sheets("Sheet2").Select
				cacheXls.getAndActiveSheetBySeq(2);

				Dispatch selection = cacheXls.Select("6:6");
				Dispatch.call(selection, "Copy");
				cacheXls.getAndActiveSheetBySeq(1);
				selection = cacheXls.Select(insertRow + ":" + insertRow);
				Dispatch.call(selection, "Insert", new Variant(1));

				int iRowPlus = 1;
				for ( ; iRowPlus<rstsSize;iRowPlus++) {
					cacheXls.getAndActiveSheetBySeq(2);
					selection = cacheXls.Select("7:7");
					Dispatch.call(selection, "Copy");
					cacheXls.getAndActiveSheetBySeq(1);
					selection = cacheXls.Select((insertRow + iRowPlus) + ":" + (insertRow + iRowPlus));
					Dispatch.call(selection, "Insert", new Variant(1));
				}

				cacheXls.getAndActiveSheetBySeq(2);
				selection = cacheXls.Select("8:9");
				Dispatch.call(selection, "Copy");
				cacheXls.getAndActiveSheetBySeq(1);
				selection = cacheXls.Select((insertRow + iRowPlus) + ":" + (insertRow + iRowPlus));
				Dispatch.call(selection, "Insert", new Variant(1));

				for (int j=0;j< rstsSize;j++) {
					TorsionDeviceEntity trTorsion = rsts.get(j);
					// 规格力矩值 BASE
					cacheXls.SetValue("A" + (insertRow + j), getNoScale(trTorsion.getRegular_torque()));
					// 规格力矩值 DIFF
					cacheXls.SetValue("C" + (insertRow + j), getNoScale(trTorsion.getDeviation()));
					// HP-10 HP-100
					cacheXls.SetValue("F" + (insertRow + j), CodeListUtils.getValue("torsion_device_hp_scale", ""+trTorsion.getHp_scale(), ""));
					// 点检力矩[N·m]下限
					cacheXls.SetValue("G" + (insertRow + j), trTorsion.getRegular_torque_lower_limit().toPlainString());
					// 点检力矩[N·m]上限
					cacheXls.SetValue("I" + (insertRow + j), trTorsion.getRegular_torque_upper_limit().toPlainString());
					if (j==0) {
						// 使用的工程
						cacheXls.SetValue("E" + insertRow, trTorsion.getUsage_point());
						// 管理编号
						cacheXls.SetValue("J" + insertRow, trTorsion.getManage_code());
					}

					// 每个单元格取值
					for (int iAxis=0;iAxis<=axis;iAxis++) {
						Date[] dates = getDayOfAxis(monCal, iAxis, axisType, cfmEntity.getCycle_type());

						CheckResultEntity cre = new CheckResultEntity();
						cre.setCheck_confirm_time_start(dates[0]);
						cre.setCheck_confirm_time_end(dates[1]);
						cre.setManage_id(devices_manage_id);
						cre.setItem_seq(trTorsion.getSeq());
						cre.setCheck_file_manage_id(check_file_manage_id);

						// 已点检或之前范围
						List<CheckResultEntity> listCre = crMapper.getTorsionDeviceCheckInPeriod(cre);
						Dispatch cell = null;

						// 取得已点检单元格信息
						if (listCre != null && listCre.size() > 0) {

							String jobNo = null;
							Date getDate = null;

							for (CheckResultEntity rCre : listCre) {
								String cellName = XlsUtil.getExcelColCode(12 + iAxis - 1) 
										+ (insertRow + j);
								cell = cacheXls.getRange(cellName);
								cacheXls.SetValue(cell, getFileStatusD(""+rCre.getChecked_status(), rCre.getDigit()));
	
								if (jobNo == null) jobNo = rCre.getJob_no();
								if (getDate == null) getDate = rCre.getCheck_confirm_time();
							}

							// 日期行
							if (getDate != null) {
								String cellName = XlsUtil.getExcelColCode(12 + iAxis - 1) 
										+ (insertRow + rstsSize);
								cell = cacheXls.getRange(cellName);
								cacheXls.SetValue(cell, DateUtil.toString(getDate, "MM-dd"));
								cacheXls.SetNumberFormatLocal(cell, "m-d");
							}

							// 签章行
							if (!isEmpty(jobNo)) {
								
								String cellName = XlsUtil.getExcelColCode(12 + iAxis - 1) 
										+ (insertRow + rstsSize + 1);
								cell = cacheXls.getRange(cellName);
								if (cfmEntity.getAccess_place() == 1) {
									cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign_v\\" + jobNo.toUpperCase(), cell);
								} else {
									cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell);
								}
							}
						} else {
							String cellName = XlsUtil.getExcelColCode(12 + iAxis - 1) 
									+ (insertRow + j);
							cell = cacheXls.getRange(cellName);
							cacheXls.SetValue(cell, "/");
						}
					}
				}

				insertRow += (2 + rstsSize);
			}
		}
	}

	private void setDeviceNormal(XlsUtil cacheXls, List<String> sEncodedDeviceList,
			Map<String, CheckPosBean> checkPosData, CheckPosBean checkPosManageNo, CheckPosBean checkPosModel, 
			CheckPosBean checkDeviceName, CheckPosBean checkUseStart, CheckPosBean checkUseEnd, 
			List<CheckPosBean> checkPosDate, List<CheckPosBean> checkPosName, int axis, int axisType, 
			CheckFileManageEntity cfmEntity, CheckResultMapper crMapper, Calendar monCal,
			SqlSession conn) {
		DevicesManageMapper dmMapper = conn.getMapper(DevicesManageMapper.class);

		for (int iDev = 0; iDev < sEncodedDeviceList.size(); iDev ++) {
			// 取得管理设备信息
			DevicesManageEntity dmEntity = dmMapper.getByKey(sEncodedDeviceList.get(iDev));

			String cellName = null;
			Dispatch cell = null;

			// 设备管理编号
			if (checkPosManageNo.startX > 0 && checkPosManageNo.shiftY > 0) {
				cellName = XlsUtil.getExcelColCode(checkPosManageNo.startX - 1) 
						+ (checkPosManageNo.startY + checkPosManageNo.shiftY * iDev);
				cell = cacheXls.getRange(cellName);
				cacheXls.SetValue(cell, dmEntity.getManage_code());
			}

			// 型号
			if (!isEmpty(checkPosModel.content)) {
				cellName = XlsUtil.getExcelColCode(checkPosModel.startX - 1) 
						+ (checkPosModel.startY + checkPosModel.shiftY * iDev);
				cell = cacheXls.getRange(cellName);
				cacheXls.SetValue(cell, dmEntity.getModel_name());
			}
			// 名称
			if (!isEmpty(checkDeviceName.content)) {
				cellName = XlsUtil.getExcelColCode(checkDeviceName.startX - 1) 
						+ (checkDeviceName.startY + checkDeviceName.shiftY * iDev);
				cell = cacheXls.getRange(cellName);
				cacheXls.SetValue(cell, dmEntity.getName());
			}

			if (!isEmpty(checkUseStart.content)) {

				Calendar calUse = Calendar.getInstance();
				if (dmEntity.getImport_date()!=null) {
					calUse.setTime(dmEntity.getImport_date());
				}

				cellName = XlsUtil.getExcelColCode(checkUseStart.startX - 1) 
						+ (checkUseStart.startY + checkUseStart.shiftY * iDev);
				cell = cacheXls.getRange(cellName);
				cacheXls.SetValue(cell, DateUtil.toString(dmEntity.getImport_date(), "yyyy年 M月 d日"));

				if (!isEmpty(checkUseEnd.content)) {
					int iExpiration = checkUseEnd.shiftX;
					calUse.add(Calendar.MONTH, iExpiration);
					calUse.add(Calendar.DATE, -1);

					cellName = XlsUtil.getExcelColCode(checkUseEnd.startX - 1) 
							+ (checkUseEnd.startY + checkUseEnd.shiftY * iDev);
					cell = cacheXls.getRange(cellName);
					cacheXls.SetValue(cell, DateUtil.toString(calUse.getTime(), "yyyy年 M月 d日"));
				}
			}

			// 每个单元格取值
			for (int iAxis=0;iAxis<=axis;iAxis++) {
				Date[] dates = getDayOfAxis(monCal, iAxis, axisType, cfmEntity.getCycle_type());

				CheckResultEntity cre = new CheckResultEntity();
				cre.setCheck_confirm_time_start(dates[0]);
				cre.setCheck_confirm_time_end(dates[1]);
				cre.setManage_id(dmEntity.getDevices_manage_id());
				cre.setCheck_file_manage_id(cfmEntity.getCheck_file_manage_id());

				// 已点检或之前范围
				List<CheckResultEntity> listCre = crMapper.getDeviceCheckInPeriod(cre);
				String jobNo = null;
				Date checkedDate = new Date(0);

				// 预先全划掉
				for (String itemSeq : checkPosData.keySet()) {
					CheckPosBean checkPos = checkPosData.get(itemSeq);
					int shift = iAxis;
					cellName = XlsUtil.getExcelColCode(checkPos.startX + shift * checkPos.shiftX - 1) 
							+ (checkPos.startY + checkPos.shiftY * iDev);
					cell = cacheXls.getRange(cellName);
					cacheXls.SetValue(cell, "/");
				}

				// 取得已点检单元格信息
				if (listCre != null && listCre.size() > 0) {
					for (CheckResultEntity rCre : listCre) {
						String itemSeq = rCre.getItem_seq();

						CheckPosBean checkPos = checkPosData.get(itemSeq);
						int shift = iAxis;

//						// TODO 特殊对应 日常里的月
//						if (CheckFileManageEntity.ACCESS_PLACE_DAILY == cfmEntity.getAccess_place() && 
//								rCre.getCycle_type() == TYPE_MONTH_OF_YEAR) {
//							shift = 0;
//						}
//
						cellName = XlsUtil.getExcelColCode(checkPos.startX + shift * checkPos.shiftX - 1) 
								+ (checkPos.startY + checkPos.shiftY * iDev);
						cell = cacheXls.getRange(cellName);
						cacheXls.SetValue(cell, checkPos.content.replaceAll("#tag#", getFileStatusD(""+rCre.getChecked_status(), rCre.getDigit())));

						if (jobNo == null) jobNo = rCre.getJob_no();
						// 得到点检最终完成时间
						Date dCheckConfirmTime = rCre.getCheck_confirm_time();
						if (dCheckConfirmTime.after(checkedDate)) {
							checkedDate = dCheckConfirmTime;
						}
					}
				}

				for (CheckPosBean cpd : checkPosDate) {
					if (!isEmpty(cpd.content)) {
						cellName = XlsUtil.getExcelColCode(cpd.startX + iAxis * cpd.shiftX - 1) 
								+ cpd.startY;
						cell = cacheXls.getRange(cellName);
						if (checkedDate.getTime() > 0) {
							cacheXls.SetValue(cell, DateUtil.toString(checkedDate, "y-M-d"));
							if ("D".equalsIgnoreCase(cpd.content)) {
								cacheXls.SetNumberFormatLocal(cell, "m-d");
							} else if ("C".equalsIgnoreCase(cpd.content)) {
								cacheXls.SetNumberFormatLocal(cell, "m月d日");
							}
						} else {
//							String isTag = cacheXls.GetValue(cellName);
//							cacheXls.SetValue(cell, "");
						}
					}
				}

				// 签章
				if (!isEmpty(jobNo)) {
					for (CheckPosBean cpn : checkPosName) {
						if (checkPosManageNo.shiftY > 0) {
							cellName = XlsUtil.getExcelColCode(cpn.startX + iAxis * cpn.shiftX - 1) 
									+ (cpn.startY  + cpn.shiftY * iDev);
						} else {
							cellName = XlsUtil.getExcelColCode(cpn.startX + iAxis * cpn.shiftX - 1) 
									+ cpn.startY;
						}
						cell = cacheXls.getRange(cellName);
						String Taged = cacheXls.GetValue(cellName);
						if ("已千千千千千千千".equals(Taged)) {
							continue;
						}
						if (checkPosManageNo.shiftY > 0 || (iDev == 0)) {
							if (cfmEntity.getAccess_place() == 1) {
								cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign_v\\" + jobNo.toUpperCase(), cell);
							} else {
								cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell);
							}
							cacheXls.SetValue(cell, "已千千千千千千千");
						}
					}
				}
			}
		}
		cacheXls.Replace("已千千千千千千千", "");
	}

	private static final int INSERT_START_ROW_FOR_JIG =6;
	private static final int INSERT_START_COL_FOR_JIG =6; // G

	/**
	 * 生成治具文档
	 */
	public void makeFileJigs(CheckedFileStorageEntity cfsEntity,
			List<String> sEncodedJigList, SqlSession conn) {
		JigCheckResultMapper crMapper = conn.getMapper(JigCheckResultMapper.class);
		JigManageMapper tmMapper = conn.getMapper(JigManageMapper.class);

		Date filingDate = cfsEntity.getFiling_date();

		Calendar adjustCal = Calendar.getInstance();
		adjustCal.setTime(filingDate);
		adjustCal.set(Calendar.HOUR_OF_DAY, 0);
		adjustCal.set(Calendar.MINUTE, 0);
		adjustCal.set(Calendar.SECOND, 0);
		adjustCal.set(Calendar.MILLISECOND, 0);

		// 复制模板到临时文件
		String ext = ".xlsx";
		String srcPath = PathConsts.BASE_PATH + PathConsts.DEVICEINFECTION + "\\专用工具定期清点保养记录模板.xlsx";
		String cacheFilename =  cfsEntity.getStorage_file_name() + filingDate.getTime() + ext;
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(filingDate, "yyyyMM") + "\\" + cacheFilename;
		try {
			FileUtils.copyFile(new File(srcPath), new File(cachePath));
		} catch (IOException e) {
			_logger.error(e.getMessage(), e);
			return;
		}
		String targetPath = PathConsts.BASE_PATH + PathConsts.INFECTIONS + "\\" + 
				RvsUtils.getBussinessYearString(adjustCal) + "\\治具";
		String targetFile = targetPath + "\\" + cfsEntity.getStorage_file_name() + ".pdf";

		String section_id = cfsEntity.getSection_id();
		String line_id = cfsEntity.getLine_id();
		String position_id = cfsEntity.getPosition_id();

		XlsUtil cacheXls = null;
		try {
			cacheXls = new XlsUtil(cachePath, false);
			cacheXls.SelectActiveSheet();

			// 取得本期
			String bperiod = RvsUtils.getBussinessYearString(adjustCal);

			// 工程
			String sLineName = "";
			SectionMapper sMapper = conn.getMapper(SectionMapper.class);
			SectionEntity sEntity = sMapper.getSectionByID(section_id);
			if (sEntity != null) {
				sLineName += sEntity.getName() + "\n";
			}

			LineMapper lMapper = conn.getMapper(LineMapper.class);
			LineEntity lEntity = lMapper.getLineByID(line_id);
			if (lEntity != null) {
				sLineName += lEntity.getName();
			}
			cacheXls.Replace("#G[LINE#", sLineName);

			// 工位
			PositionMapper pMapper = conn.getMapper(PositionMapper.class);
			// 工位
			PositionEntity pEntity = pMapper.getPositionByID(position_id);
			if (pEntity != null) {
				sLineName += pEntity.getProcess_code() + " ";
			}
			Dispatch positionCell = cacheXls.Locate("#G[POSITION#");
			String FoundValue = Dispatch.get(positionCell, "Value").toString();
			if (FoundValue.equals("#G[POSITION#")) {
				cacheXls.SetValue(positionCell, sLineName);
			} else {
				cacheXls.Replace("#G[POSITION#", sLineName.replaceAll("\\\n", " "));
			}

			// 替换共通数据
			cacheXls.Replace("#G[PERIOD#", bperiod);
			cacheXls.Replace("#G[PERIODC#", bperiod.replaceAll("P", ""));

			// 确定表单的归档类型
			int axisType = TYPE_FILED_YEAR;

			// 计算范围用日历
			Calendar monCal = Calendar.getInstance();
			// 去期间头
			monCal = getStartOfPeriod(adjustCal);

			int axis = getMaxAxis(TYPE_ITEM_MONTH, TYPE_FILED_YEAR);

			// 普通设备工具
			setJig(cacheXls, sEncodedJigList, axis, axisType, crMapper, tmMapper, monCal, conn);

			File fTargetPath = new File(targetPath);
			if (!fTargetPath.exists()) {
				fTargetPath.mkdirs();
			}
			cacheXls.SaveAsPdf(targetFile); // SaveAsPdf
			cacheXls.Release();

		} catch (Exception e) {
			_logger.error(e.getMessage(), e);
			if (cacheXls != null) {
				cacheXls.CloseExcel(false);
			}
		} finally {
			cacheXls = null;
		}
	}

	/**
	 * 设定治具清点内容
	 * @param cacheXls
	 * @param sEncodedJigList 治具ID列表
	 * @param axis X坐标范围
	 * @param axisType
	 * @param cfsEntity 归档文件信息
	 * @param crMapper
	 * @param tmMapper
	 * @param monCal 去期间头
	 * @param conn
	 */
	private void setJig(XlsUtil cacheXls, List<String> sEncodedJigList,
			int axis, int axisType, 
			JigCheckResultMapper crMapper, JigManageMapper tmMapper, Calendar monCal, SqlSession conn) {
		// 循环填写各治具
		int insertRow = INSERT_START_ROW_FOR_JIG;

		for (int iJig = 0; iJig < sEncodedJigList.size(); iJig++) {
			insertRow++;

			String jig_id = sEncodedJigList.get(iJig);
			JigManageEntity tmEntity = tmMapper.getByKey(jig_id);

			String cellName = null;
			Dispatch cell = null;

			cacheXls.getAndActiveSheetBySeq(2);

			Dispatch selection = cacheXls.Select("1:2");
			Dispatch.call(selection, "Copy");

			// Sheets("Sheet1").Select
			cacheXls.getAndActiveSheetBySeq(1);
			// Rows("4:4").Select
			selection = cacheXls.Select(insertRow + ":" + insertRow);
			// Selection.Insert Shift:=xlDown
			Dispatch.call(selection, "Insert", new Variant(1));

			// No
			cacheXls.SetValue("A" + insertRow, "" + (iJig + 1));
			// 管理号码
			cacheXls.SetValue("B" + insertRow, getNoScale(tmEntity.getManage_code()));
			// 治具号码
			cacheXls.SetValue("C" + insertRow, getNoScale(tmEntity.getJig_no()));
			//专用工具名称
			cacheXls.SetValue("D" + insertRow, getNoScale(tmEntity.getJig_name()));
			// 备注 TODO
			// cacheXls.SetValue("S" + insertRow, getNoScale(tmEntity.getTools_name()));

			// 循环填写每月份 G->
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(monCal.getTime());
			Calendar endcal = Calendar.getInstance();
			endcal.setTime(monCal.getTime());

			for (int iM = 0; iM < axis; iM++) {
				endcal.add(Calendar.MONTH, 1);
				JigCheckResultEntity condition = new JigCheckResultEntity();
				condition.setManage_id(jig_id);
				condition.setFirstDate(DateUtil.toString(startCal.getTime(), DateUtil.DATE_PATTERN));
				condition.setLastDate(DateUtil.toString(endcal.getTime(), DateUtil.DATE_PATTERN));
				List<JigCheckResultEntity> result = crMapper.searchCheckResult(condition);
				if (result.size() > 0) {
					String sCheckedStatus = result.get(0).getChecked_status();
					cacheXls.SetValue(XlsUtil.getExcelColCode(INSERT_START_COL_FOR_JIG + iM)
							+ insertRow, getNoScale(getFileStatusD(sCheckedStatus, null)));
				}
			}
			
		}
	}

	private String getFileStatusD(String status, BigDecimal digit) {

		if (digit != null) {
			return getNoScale(digit);
		} else
		if (status == null || "0".equals(status)) {
			return "/";
		} else {
			if ("1".equals(status)) {
				return "〇";
			} else if ("2".equals(status)) {
				return "×";
			} else if ("3".equals(status)) {
				return "△";
			} else if ("4".equals(status)) {
				return "●";
			}
		}
		return "";
	}

	/**
	 * 定位用对象
	 * @author Gong
	 *
	 */
	private class CheckPosBean {
		private int startX = 0;
		private int startY = 0;
		private int shiftX = 1;
		private int shiftY = 1;
		private String content = "";
	}
}