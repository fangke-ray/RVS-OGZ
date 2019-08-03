package com.osh.rvs.service;

import static com.osh.rvs.service.CheckResultService.TYPE_FILED_WEEK_OF_MONTH;
import static com.osh.rvs.service.CheckResultService.TYPE_FILED_YEAR;
import static com.osh.rvs.service.CheckResultService.TYPE_ITEM_DAY;
import static com.osh.rvs.service.CheckResultService.TYPE_ITEM_MONTH;
import static com.osh.rvs.service.CheckResultService.TYPE_ITEM_WEEK;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.osh.rvs.bean.infect.DryingOvenDeviceEntity;
import com.osh.rvs.bean.infect.ElectricIronDeviceEntity;
import com.osh.rvs.bean.infect.JigCheckResultEntity;
import com.osh.rvs.bean.infect.TorsionDeviceEntity;
import com.osh.rvs.bean.master.CheckFileManageEntity;
import com.osh.rvs.bean.master.DeviceCheckItemEntity;
import com.osh.rvs.bean.master.DevicesManageEntity;
import com.osh.rvs.bean.master.JigManageEntity;
import com.osh.rvs.bean.master.LineEntity;
import com.osh.rvs.bean.master.PositionEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.common.XlsUtil;
import com.osh.rvs.mapper.infect.CheckResultMapper;
import com.osh.rvs.mapper.infect.DryingOvenDeviceMapper;
import com.osh.rvs.mapper.infect.ElectricIronDeviceMapper;
import com.osh.rvs.mapper.infect.JigCheckResultMapper;
import com.osh.rvs.mapper.infect.TorsionDeviceMapper;
import com.osh.rvs.mapper.master.CheckFileManageMapper;
import com.osh.rvs.mapper.master.DevicesManageMapper;
import com.osh.rvs.mapper.master.JigManageMapper;
import com.osh.rvs.mapper.master.LineMapper;
import com.osh.rvs.mapper.master.PositionMapper;

import framework.huiqing.common.util.CodeListUtils;
import framework.huiqing.common.util.CommonStringUtil;
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

		// 页数统计
		Integer linage = cfmEntity.getLinage();
		int pageCnt = 1;
		if ("1".equals(cfsEntity.getSpecialized()) || "true".equals(cfsEntity.getSpecialized())
				 || "2".equals(cfsEntity.getSpecialized())) {
		} else if (linage != null && linage != 1) {
			pageCnt = (sEncodedDeviceList.size() + linage - 1) / linage;
		}

		CheckResultMapper crMapper = conn.getMapper(CheckResultMapper.class);

		// 复制模板到临时文件
		String cacheFilename =  cfsEntity.getStorage_file_name() + today.getTime();
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM") + "\\" + cacheFilename + ext;
		String targetPath = PathConsts.BASE_PATH + PathConsts.INFECTIONS + "\\" +
				RvsUtils.getBussinessYearString(adjustCal) + "\\" +
				cfmEntity.getCheck_manage_code();
		String targetFile = targetPath + "\\" + cfsEntity.getStorage_file_name() + ".pdf";

		for (int iPage = 0; iPage < pageCnt; iPage++) {

			if (iPage > 0) 
				cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(today, "yyyyMM") + "\\" + cacheFilename + "_" + iPage + ext;
			FileUtils.copyFile(new File(srcPath), new File(cachePath));

			List<String> pageEncodedDeviceList = null;
			if (pageCnt == 1) {
				pageEncodedDeviceList = sEncodedDeviceList;
			} else {
				int iItem = iPage * linage;
				for (; ; iItem++) {
					if (iItem == sEncodedDeviceList.size()) break;
					if (iItem == (iPage + 1) * linage) break;
				}
				pageEncodedDeviceList = sEncodedDeviceList.subList(iPage * linage, iItem);
			}

			XlsUtil cacheXls = null;
			try {
				cacheXls = new XlsUtil(cachePath, false);
				cacheXls.SelectActiveSheet();

				// 取得页面缩放
				int pageZoom = cacheXls.getPageZoom();

				// 取得本期
				String bperiod = RvsUtils.getBussinessYearString(adjustCal);

				// 工程
				String line_id = cfsEntity.getLine_id();
				String sLineName = "";

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
					if(isEmpty(line_id) && pEntity != null && cacheXls.Hit("#G[LINE#")) {
						cacheXls.Replace("#G[LINE#", pEntity.getLine_name());
					}
				}

				// 替换共通数据
				cacheXls.Replace("#G[PERIOD#", bperiod);
				cacheXls.Replace("#G[PERIODC#", bperiod.replaceAll("P", ""));
				cacheXls.Replace("#G[YEAR#", DateUtil.toString(cfsEntity.getFiling_date(), "yyyy"));
				cacheXls.Replace("#G[MONTH#", DateUtil.toString(cfsEntity.getFiling_date(), "M"));

				// 页数
				cacheXls.Replace("#G[PAGE#", "" + (char)('１' + iPage));
				cacheXls.Replace("#G[PAGE[M#", "" + (char)('１' + (pageCnt - 1)));

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
				fileAxisType = cfmEntity.getCycle_type();

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

				// 取得输入项定位
				Map<String, CheckPosBean> checkPosData = new HashMap<String, CheckPosBean>();
				List<CheckPosBean> checkPosDate = new ArrayList<CheckPosBean>();
				List<CheckPosBean> checkPosName = new ArrayList<CheckPosBean>();
				CheckPosBean checkPosNo = new CheckPosBean();
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

				// #G[NO
				cell = cacheXls.Locate("#G[NO");
				if (cell != null) {
					FoundValue = Dispatch.get(cell, "Value").toString();
					checkPosNo.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
					checkPosNo.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
					String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
					String[] tags = tagText.split("\\[");
					for (String tag : tags) {
						if (tag.startsWith("U")) {
							// 单元格中的跳动
							checkPosNo.shiftY = Integer.parseInt(tag.substring(1));
						}
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

				// 特殊格式生成
				if ("1".equals(cfsEntity.getSpecialized()) || "true".equals(cfsEntity.getSpecialized())) {
					// 电烙铁工具
					setDeviceElectricIron(cacheXls, sEncodedDeviceList, fileAxisType,
							cfmEntity, crMapper, monCal, conn);
				} else if ("2".equals(cfsEntity.getSpecialized())) {
					// 力矩工具
					setDeviceTrosion(cacheXls, sEncodedDeviceList, fileAxisType,
							cfmEntity, crMapper, monCal, check_file_manage_id, conn);
				}

				// 日期定位
				cell = cacheXls.Locate("#T");
				while (cell != null) {
					FoundValue = Dispatch.get(cell, "Value").toString();
					CheckPosBean aCheckPosDate = new CheckPosBean();
					aCheckPosDate.startX = Integer.parseInt(XlsUtil.getExcelColNo(cell));
					aCheckPosDate.startY = Integer.parseInt(XlsUtil.getExcelRowNo(cell));
					String tagText = FoundValue.replaceAll("[^#]*#([^#]*)#[^#]*", "$1");
					String[] tags = tagText.split("\\[");
					aCheckPosDate.shiftY = 0;
					for (int i=1 ; i< tags.length; i++) {
						String tag = tags[i];
						if (tag.startsWith("T")) {
							// 单元格中的跳动
							aCheckPosDate.shiftX = Integer.parseInt(tag.substring(1));
						} else if (tag.startsWith("U")) {
							aCheckPosDate.shiftY = Integer.parseInt(tag.substring(1));
						} else if (tag.startsWith("I")) {
							aCheckPosDate.signType = 1;
						} else if (tag.startsWith("L")) {
							aCheckPosDate.signType = 2;
						}
					}
					if (tags[0].length() > 1) {
						char sCycleType = tags[0].charAt(1);
						switch (sCycleType) {
						case 'D' : aCheckPosDate.cycleType = 1; break; // 日
						case 'W' : aCheckPosDate.cycleType = 2; break; // 周
						case 'M' : aCheckPosDate.cycleType = 3; break; // 月
						case 'P' : aCheckPosDate.cycleType = 4; break; // 半期
						case 'Y' : aCheckPosDate.cycleType = 5; break; // 全年
						}
					}
					aCheckPosDate.content = tagText.substring(2, 3);
					checkPosDate.add(aCheckPosDate);
					cacheXls.SetValue(cell, "");
					cell = cacheXls.Locate("#T");
				}

				// 签名定位
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
						} else if (tag.startsWith("V")) {
							aCheckPosName.content = "V";
						} else if (tag.startsWith("I")) {
							aCheckPosName.signType = 1;
						} else if (tag.startsWith("L")) {
							aCheckPosName.signType = 2;
						} else if (tag.startsWith("N") && tag.length() > 1) {
							char sCycleType = tag.charAt(1);
							switch (sCycleType) {
							case 'D' : aCheckPosName.cycleType = 1; break; // 日
							case 'W' : aCheckPosName.cycleType = 2; break; // 周
							case 'M' : aCheckPosName.cycleType = 3; break; // 月
							case 'P' : aCheckPosName.cycleType = 4; break; // 半期
							case 'Y' : aCheckPosName.cycleType = 5; break; // 全年
							}
						}
					}
					checkPosName.add(aCheckPosName);
					cacheXls.SetValue(cell, "");
					cell = cacheXls.Locate("#N");
				}

				// 一般格式生成
				if ("1".equals(cfsEntity.getSpecialized()) || "true".equals(cfsEntity.getSpecialized())) {
				} else if ("2".equals(cfsEntity.getSpecialized())) {
				} else {
					// 普通设备工具

					// 取得点检项目
					List<DeviceCheckItemEntity> dCsis = cfmMapper.getSeqItemsByFile(check_file_manage_id);
					setDeviceNormal(cacheXls, pageEncodedDeviceList, checkPosData, checkPosNo, checkPosManageNo, checkPosModel,
							checkDeviceName, checkUseStart, checkUseEnd, checkPosDate, checkPosName, fileAxisType,
							cfmEntity, dCsis, pageZoom, crMapper, monCal, (linage != null ? iPage * linage : 0), conn);
				}

				setLeaderCheck(cacheXls, pageEncodedDeviceList, checkPosDate, checkPosName,
						cfmEntity, pageZoom, crMapper, monCal, conn);

				// #P QR-B31002-12A_B038_147P12月
				// 取得参照信息<refer
				cell = cacheXls.Locate("#P");
				FoundValue = null;

				// 取得设备的温度设置
				String lowerLimit = "70";
				String upperLimit = "75";
				if (cell != null) {
					FoundValue = Dispatch.get(cell, "Value").toString();

					DryingOvenDeviceMapper dodMapper = conn.getMapper(DryingOvenDeviceMapper.class);
					DryingOvenDeviceEntity dodCnd = new DryingOvenDeviceEntity();
					dodCnd.setDevice_manage_id(pageEncodedDeviceList.get(0));
					List<DryingOvenDeviceEntity> dodRet = dodMapper.search(dodCnd);
					if (dodRet.size() > 0) {
						String settingTemperature = "" + dodRet.get(0).getSetting_temperature();
						lowerLimit = CodeListUtils.getValue("drying_oven_lower_limit", settingTemperature);
						upperLimit = CodeListUtils.getValue("drying_oven_upper_limit", settingTemperature);
					}
				}
				while (FoundValue != null) {
					if (FoundValue.indexOf("L" + lowerLimit) >= 0 && FoundValue.indexOf("U" + upperLimit) >= 0) {
						FoundValue = FoundValue.replaceAll("#P\\[C[^#]*#", "■"); // √
						cacheXls.SetValue(cell, FoundValue);
					} else {
						FoundValue = FoundValue.replaceAll("#P\\[C[^#]*#", "□");
						cacheXls.SetValue(cell, FoundValue);
					}

					FoundValue = null;
					cell = cacheXls.Locate("#P");
					if (cell != null) FoundValue = Dispatch.get(cell, "Value").toString();
				}

//				cacheXls.Replace(source, target);
//				"#[^#]*#"
				File fTargetPath = new File(targetPath);
				if (!fTargetPath.exists()) {
					fTargetPath.mkdirs();
				}
				if (iPage > 0) {
					String targetPageFile = targetPath + "\\" + cfsEntity.getStorage_file_name() + "_" + iPage + ".pdf";
					cacheXls.SaveAsPdf(targetPageFile); // SaveAsPdf
					cacheXls.Release();
					joinPdf(targetFile, targetPageFile);
				} else {
					cacheXls.SaveAsPdf(targetFile); // SaveAsPdf
					cacheXls.Release();
				}

			} catch (Exception e) {
				_logger.error(e.getMessage(), e);
				if (cacheXls != null) {
					cacheXls.CloseExcel(false);
				}
			} finally {
				cacheXls = null;
			}
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

			List<DevicesManageEntity> deviceManageRecordList = dmMapper.getDeviceManageRecordInPeriod(conditionOfDevice);
			for (DevicesManageEntity deviceManageRecord : deviceManageRecordList) {
				Map<String, String> comment = new HashMap<String, String>();

				String commentText = "";

				switch(deviceManageRecord.getEvent()) {
				case 1 :
					if (deviceManageRecord.getProcess_code() != null)
						commentText = ApplicationMessage.WARNING_MESSAGES.getMessage("info.infect.device.filing.provide",
								deviceManageRecord.getManage_code(), deviceManageRecord.getProcess_code());
					break; // 发布
				case 2 :
					commentText = ApplicationMessage.WARNING_MESSAGES.getMessage("info.infect.device.filing.waste",
							deviceManageRecord.getOld_manage_code(), deviceManageRecord.getProcess_code());
					break; // 废弃
				case 3 :
					boolean changeManageCode = false, changePosition = false;

//					info.infect.device.filing.trans=管理编号为{0}的设备/工具由{1}工位移动到{2}工位。
//					info.infect.device.filing.trans.rename=管理编号为{0}的设备/工具由{1}工位移动到{2}工位，更名为{3}。
//					info.infect.device.filing.rename=管理编号为{0}的设备/工具，更名为{1}。
					if (deviceManageRecord.getOld_manage_code() == null) {
						changeManageCode = true;
					} else if (!deviceManageRecord.getOld_manage_code().equals(deviceManageRecord.getManage_code())) {
						changeManageCode = true;
					}
					if (deviceManageRecord.getOld_position_id() == null) {
						if (deviceManageRecord.getProcess_code() == null) {
							changePosition = false;
						} else {
							changePosition = true;
						}
					} else if (!deviceManageRecord.getOld_position_id().equals(deviceManageRecord.getProcess_code())) {
						changePosition = true;
					}
					if (changeManageCode) {
						if (changePosition) {
							commentText = ApplicationMessage.WARNING_MESSAGES.getMessage("info.infect.device.filing.trans.rename",
									deviceManageRecord.getOld_manage_code(), CommonStringUtil.nullToAlter(deviceManageRecord.getOld_position_id(), "无"),
									CommonStringUtil.nullToAlter(deviceManageRecord.getProcess_code(), "无"), deviceManageRecord.getManage_code());
						} else {
							commentText = ApplicationMessage.WARNING_MESSAGES.getMessage("info.infect.device.filing.rename",
									deviceManageRecord.getOld_manage_code(), deviceManageRecord.getManage_code());
						}
					} else if (changePosition) {
						commentText = ApplicationMessage.WARNING_MESSAGES.getMessage("info.infect.device.filing.trans",
								deviceManageRecord.getManage_code(),
								CommonStringUtil.nullToAlter(deviceManageRecord.getOld_position_id(), "无"),
								CommonStringUtil.nullToAlter(deviceManageRecord.getProcess_code(), "无"));
					}
					break; // 变更
				}

				if(!isEmpty(commentText)) {
					comment.put("comment", commentText);
					comment.put("manage_code", deviceManageRecord.getManage_code());
					comment.put("job_no", deviceManageRecord.getUpdated_by());
					comment.put("comment_date", DateUtil.toString(deviceManageRecord.getUpdated_time(), DateUtil.ISO_DATE_PATTERN));
					comments.add(comment);
				}
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

		// 附加备注页
		addCommentPage(comments, cachePath, targetPath + "\\" + cfsEntity.getStorage_file_name());

	}

	private static final int COMMENTS_PAGE_ITEM = 14;

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
	private static final int INSERT_START_COL_FOR_EI =6;
	private static final int INSERT_START_ROW_FOR_TR =5;
	private void setDeviceElectricIron(XlsUtil cacheXls,
			List<String> sEncodedDeviceList, int fileAxisType,
			CheckFileManageEntity cfmEntity, CheckResultMapper crMapper,
			Calendar monCal, SqlSession conn) {

//		Date monthStart = monCal.getTime();
//		// 月底
//		Calendar cMonthEnd = Calendar.getInstance();
//		cMonthEnd.setTimeInMillis(monCal.getTimeInMillis());
//		cMonthEnd.add(Calendar.MONTH, 1);
//		cMonthEnd.add(Calendar.DATE, -1);
//		Date monthEnd = cMonthEnd.getTime();

		ElectricIronDeviceMapper eidMapper = conn.getMapper(ElectricIronDeviceMapper.class);
		int insertRow = INSERT_START_ROW_FOR_EI;

		int maxAxis = getMaxAxis(TYPE_ITEM_DAY, cfmEntity.getCycle_type());

		for (int iDev = 0; iDev < sEncodedDeviceList.size(); iDev ++) {
			// 取得管理设备信息
			String devices_manage_id = sEncodedDeviceList.get(iDev);
			// DevicesManageEntity dmEntity = dmMapper.getByKey(devices_manage_id);

			ElectricIronDeviceEntity eidCond = new ElectricIronDeviceEntity();
			eidCond.setManage_id(devices_manage_id);
			List<ElectricIronDeviceEntity> rsts = eidMapper.searchElectricIronDevice(eidCond);

			if (rsts.size() == 1) {
				// 单温
				// Sheets("Sheet2").Select
				cacheXls.getAndActiveSheetBySeq(2);

				// Rows("1:3").Select
				Dispatch selection = cacheXls.Select("1:4");
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

				// 上下限 +3C
				cacheXls.SetValue("C" + (insertRow + 2), rsts.get(0).getTemperature_lower_limit().toString() 
						+ " ～ " + rsts.get(0).getTemperature_upper_limit().toString());

				// 每个单元格取值
				for (int iAxis=0;iAxis<=maxAxis;iAxis++) {
					Date[] dates = getDayOfAxis(monCal, iAxis, TYPE_ITEM_DAY, cfmEntity.getCycle_type());

					CheckResultEntity cre = new CheckResultEntity();
					cre.setCheck_confirm_time_start(dates[0]);
					cre.setCheck_confirm_time_end(dates[1]);
					cre.setManage_id(devices_manage_id);
					cre.setCheck_file_manage_id(cfmEntity.getCheck_file_manage_id());

					// 已点检或之前范围
					List<CheckResultEntity> listCre = crMapper.getDeviceCheckInPeriod(cre);
					String jobNo = null;
					Dispatch cell = null;

					String cellName = XlsUtil.getExcelColCode(INSERT_START_COL_FOR_EI + iAxis - 1)
							+ (insertRow);
					cell = cacheXls.getRange(cellName);
					cacheXls.SetValue(cell, "/");
					cellName = XlsUtil.getExcelColCode(INSERT_START_COL_FOR_EI + iAxis - 1)
							+ (insertRow + 1);
					cell = cacheXls.getRange(cellName);
					cacheXls.SetValue(cell, "/");
					cellName = XlsUtil.getExcelColCode(INSERT_START_COL_FOR_EI + iAxis - 1)
							+ (insertRow + 2);
					cell = cacheXls.getRange(cellName);
					cacheXls.SetValue(cell, "/");

					// 取得已点检单元格信息
					if (listCre != null && listCre.size() > 0) {
						for (CheckResultEntity rCre : listCre) {
							String itemSeq = rCre.getItem_seq();
							int rowFix = 0;
							if (itemSeq.equals(rsts.get(0).getSeq())) {
								rowFix = 2;
							} else if ("50".equals(itemSeq)){ // 接地
								rowFix = 1;
							}

							cellName = XlsUtil.getExcelColCode(INSERT_START_COL_FOR_EI + iAxis - 1)
									+ (insertRow + rowFix);
							cell = cacheXls.getRange(cellName);
							cacheXls.SetValue(cell, getFileStatusD(""+rCre.getChecked_status(), rCre.getDigit()));

							if (jobNo == null) jobNo = rCre.getJob_no();
						}
					}

					// 签章
					if (!isEmpty(jobNo)) {

						cellName = XlsUtil.getExcelColCode(INSERT_START_COL_FOR_EI + iAxis - 1)
								+ (insertRow + 3);
						cell = cacheXls.getRange(cellName);
						cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign_v\\" + jobNo.toUpperCase(), cell);
					}
				}

//				insertRow += 4;

			} else if (rsts.size() == 2) {}
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
			List<String> sEncodedDeviceList, int axisType,
			CheckFileManageEntity cfmEntity, CheckResultMapper crMapper,
			Calendar monCal, String check_file_manage_id, SqlSession conn) {


		TorsionDeviceMapper tdMapper = conn.getMapper(TorsionDeviceMapper.class);
		int insertRow = INSERT_START_ROW_FOR_TR;

		int maxAxis = getMaxAxis(TYPE_ITEM_WEEK, cfmEntity.getCycle_type());

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
				Dispatch selection = cacheXls.Select("1:2");
				// Selection.Copy
				Dispatch.call(selection, "Copy");

				// Sheets("Sheet1").Select
				cacheXls.getAndActiveSheetBySeq(1);
				// Rows("4:4").Select
				selection = cacheXls.Select(insertRow + ":" + insertRow);
				// Selection.Insert Shift:=xlDown
				Dispatch.call(selection, "Insert", new Variant(1));
				TorsionDeviceEntity trTorsion = rsts.get(0);
				Integer hpScale = trTorsion.getHp_scale();

				// 规格力矩值 BASE
				cacheXls.SetValue("B" + insertRow, getNoScale(trTorsion.getRegular_torque()));
				if (hpScale == 1) cacheXls.SetNumberFormatLocal("B" + insertRow, "0.00");
				// 规格力矩值 DIFF
				cacheXls.SetValue("D" + insertRow, getNoScale(trTorsion.getDeviation()));
				if (hpScale == 1) cacheXls.SetNumberFormatLocal("D" + insertRow, "0.00");
				// 使用的工程
				cacheXls.SetValue("F" + insertRow, trTorsion.getUsage_point());
				// HP-10 HP-100
				cacheXls.SetValue("G" + insertRow, CodeListUtils.getValue("torsion_device_hp_scale", ""+hpScale, "").replaceAll("HP-", ""));
				// 点检力矩[N·m]下限
				cacheXls.SetValue("H" + insertRow, trTorsion.getRegular_torque_lower_limit().toPlainString());
				if (hpScale == 1) cacheXls.SetNumberFormatLocal("H" + insertRow, "0.00");
				// 点检力矩[N·m]上限
				cacheXls.SetValue("J" + insertRow, trTorsion.getRegular_torque_upper_limit().toPlainString());
				if (hpScale == 1) cacheXls.SetNumberFormatLocal("J" + insertRow, "0.00");
				// 管理编号
				cacheXls.SetValue("A" + insertRow, trTorsion.getManage_code());

				// 每个单元格取值
				for (int iAxis=0;iAxis<=maxAxis;iAxis++) {
					Date[] dates = getDayOfAxis(monCal, iAxis, TYPE_ITEM_WEEK, cfmEntity.getCycle_type());

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
						for (CheckResultEntity rCre : listCre) {
							String jobNo = null;
							Date getDate = null;

							String itemSeq = rCre.getItem_seq();
							if (!itemSeq.equals(rsts.get(0).getSeq())) {
								break;
							}

							String cellName = XlsUtil.getExcelColCode(11 + (iAxis * 3))
									+ (insertRow);
							cell = cacheXls.getRange(cellName);
							cacheXls.SetValue(cell, getFileStatusD(""+rCre.getChecked_status(), rCre.getDigit()));
							if (hpScale == 1) {
								cacheXls.SetNumberFormatLocal(cell, "0.00");
							} else if (hpScale == 2) {
								cacheXls.SetNumberFormatLocal(cell, "0.000");
							}

							jobNo = rCre.getJob_no();
							getDate = rCre.getCheck_confirm_time();

							// 日期行
							if (getDate != null) {
								cellName = XlsUtil.getExcelColCode(13 + (iAxis * 3))
										+ (insertRow + 1);
								cell = cacheXls.getRange(cellName);
								cacheXls.SetValue(cell, DateUtil.toString(getDate, "MM-dd"));
								cacheXls.SetNumberFormatLocal(cell, "m-d");
							}

							// 签章行
							if (!isEmpty(jobNo)) {

								cellName = XlsUtil.getExcelColCode(11 + (iAxis * 3))
										+ (insertRow + 1);
								cell = cacheXls.getRange(cellName);
								cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell, 38);
							}
						}

					} else {
						String cellName = XlsUtil.getExcelColCode(11 + (iAxis * 3))
								+ (insertRow);
						cell = cacheXls.getRange(cellName);
						cacheXls.SetValue(cell, "/");
					}
				}

				insertRow += 2;
			} else if (rstsSize > 1) {

				for (int iRowPlus = 0 ; iRowPlus<rstsSize;iRowPlus++) {
					cacheXls.getAndActiveSheetBySeq(2);

					Dispatch selection = cacheXls.Select("1:2");
					Dispatch.call(selection, "Copy");
					cacheXls.getAndActiveSheetBySeq(1);

					selection = cacheXls.Select(insertRow + ":" + insertRow);
					Dispatch.call(selection, "Insert", new Variant(1));
				}

				for (int j=0;j< rstsSize;j++) {
					TorsionDeviceEntity trTorsion = rsts.get(j);
					Integer hpScale = trTorsion.getHp_scale();

					int jnsertRow = (insertRow + j * 2);

					// 规格力矩值 BASE
					cacheXls.SetValue("B" + jnsertRow, getNoScale(trTorsion.getRegular_torque()));
					if (hpScale == 1) cacheXls.SetNumberFormatLocal("B" + jnsertRow, "0.00");
					// 规格力矩值 DIFF
					cacheXls.SetValue("D" + jnsertRow, getNoScale(trTorsion.getDeviation()));
					if (hpScale == 1) cacheXls.SetNumberFormatLocal("D" + jnsertRow, "0.00");
					// 使用的工程
					cacheXls.SetValue("F" + jnsertRow, trTorsion.getUsage_point());
					// HP-10 HP-100
					cacheXls.SetValue("G" + jnsertRow, CodeListUtils.getValue("torsion_device_hp_scale", ""+hpScale, "").replaceAll("HP-", ""));
					// 点检力矩[N·m]下限
					cacheXls.SetValue("H" + jnsertRow, trTorsion.getRegular_torque_lower_limit().toPlainString());
					if (hpScale == 1) cacheXls.SetNumberFormatLocal("H" + jnsertRow, "0.00");
					// 点检力矩[N·m]上限
					cacheXls.SetValue("J" + jnsertRow, trTorsion.getRegular_torque_upper_limit().toPlainString());
					if (hpScale == 1) cacheXls.SetNumberFormatLocal("J" + jnsertRow, "0.00");
					// 管理编号
					cacheXls.SetValue("A" + jnsertRow, trTorsion.getManage_code());

					// 每个单元格取值
					for (int iAxis=0;iAxis<=maxAxis;iAxis++) {
						Date[] dates = getDayOfAxis(monCal, iAxis, TYPE_ITEM_WEEK, cfmEntity.getCycle_type());

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
								String cellName = XlsUtil.getExcelColCode(11 + (iAxis * 3)) + jnsertRow;
								cell = cacheXls.getRange(cellName);
								cacheXls.SetValue(cell, getFileStatusD(""+rCre.getChecked_status(), rCre.getDigit()));
								if (hpScale == 1) {
									cacheXls.SetNumberFormatLocal(cell, "0.00");
								} else if (hpScale == 2) {
									cacheXls.SetNumberFormatLocal(cell, "0.000");
								}

								if (jobNo == null) jobNo = rCre.getJob_no();
								if (getDate == null) getDate = rCre.getCheck_confirm_time();
							}

							// 日期行
							if (getDate != null) {
								String cellName = XlsUtil.getExcelColCode(13 + (iAxis * 3))
										+ (jnsertRow + 1);
								cell = cacheXls.getRange(cellName);
								cacheXls.SetValue(cell, DateUtil.toString(getDate, "MM-dd"));
								cacheXls.SetNumberFormatLocal(cell, "m-d");
							}

							// 签章行
							if (!isEmpty(jobNo)) {

								String cellName = XlsUtil.getExcelColCode(11 + (iAxis * 3))
										+ (jnsertRow + 1);
								cell = cacheXls.getRange(cellName);
								cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell, 38);
							}
						} else {
							String cellName = XlsUtil.getExcelColCode(11 + (iAxis * 3)) + jnsertRow;
							cell = cacheXls.getRange(cellName);
							cacheXls.SetValue(cell, "/");
						}
					}
				}

				cacheXls.Merge("A" + insertRow + ":A" + (insertRow + rstsSize * 2 - 1));

				insertRow += (rstsSize * 2);
			}
		}
	}

	/**
	 * 建立普通点检表文档的点检内容
	 * @param cacheXls
	 * @param sEncodedDeviceList 设备ID列表
	 * @param checkPosData 输入项定位
	 * @param checkNo 序号定位
	 * @param checkPosManageNo 设备管理编号定位
	 * @param checkPosModel 型号定位
	 * @param checkDeviceName 设备品名定位
	 * @param checkUseStart 使用开始定位
	 * @param checkUseEnd 使用结束定位
	 * @param checkPosDate 点检日期定位
	 * @param checkPosName 点检名字定位
	 * @param fileAxisType 文档坐标类别
	 * @param cfmEntity 点检表实体
	 * @param dCsis 点检项目
	 * @param crMapper
	 * @param monCal
	 * @param conn
	 */
	private void setDeviceNormal(XlsUtil cacheXls, List<String> sEncodedDeviceList,
			Map<String, CheckPosBean> checkPosData, CheckPosBean checkNo, CheckPosBean checkPosManageNo, CheckPosBean checkPosModel,
			CheckPosBean checkDeviceName, CheckPosBean checkUseStart, CheckPosBean checkUseEnd,
			List<CheckPosBean> checkPosDate, List<CheckPosBean> checkPosName, int fileAxisType,
			CheckFileManageEntity cfmEntity, List<DeviceCheckItemEntity> dCsis, int pageZoom,
			CheckResultMapper crMapper, Calendar monCal, int startNo,
			SqlSession conn) {
		DevicesManageMapper dmMapper = conn.getMapper(DevicesManageMapper.class);

		for (int iDev = 0; iDev < sEncodedDeviceList.size(); iDev ++) {
			// 取得管理设备信息
			DevicesManageEntity dmEntity = dmMapper.getByKey(sEncodedDeviceList.get(iDev));

			String cellName = null;
			Dispatch cell = null;

			// "#G\\[RESPONCOR#"
			String sResponsibleOperator = dmEntity.getResponsible_operator();
			if (sResponsibleOperator == null) sResponsibleOperator = "/";
			cacheXls.Replace("#G[RESPONCOR#", sResponsibleOperator);

			// 序号
			if (checkNo.startX > 0 && checkNo.shiftY > 0) {
				cellName = XlsUtil.getExcelColCode(checkNo.startX - 1)
						+ (checkNo.startY + checkNo.shiftY * iDev);
				cell = cacheXls.getRange(cellName);
				cacheXls.SetValue(cell, "" + (startNo + iDev + 1));
			}

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
				cacheXls.SetValue(cell, DateUtil.toString(dmEntity.getImport_date(), "yyyy年M月d日"));

				if (!isEmpty(checkUseEnd.content)) {
					int iExpiration = checkUseEnd.shiftX;
					calUse.add(Calendar.MONTH, iExpiration);
					calUse.add(Calendar.DATE, -1);

					cellName = XlsUtil.getExcelColCode(checkUseEnd.startX - 1)
							+ (checkUseEnd.startY + checkUseEnd.shiftY * iDev);
					cell = cacheXls.getRange(cellName);
					cacheXls.SetValue(cell, DateUtil.toString(calUse.getTime(), "yyyy年M月d日"));
				}
			}

			for (DeviceCheckItemEntity dCsi : dCsis) {

				Integer itemType = dCsi.getCycle_type();
				int axis = getMaxAxis(itemType, fileAxisType);
				String itemSeq = dCsi.getItem_seq();
				boolean hitModel = true;

				// 判断型号
				if (!isEmpty(dCsi.getSpecified_model_name())) {
					if (isEmpty(dmEntity.getModel_name()) ||
							dCsi.getSpecified_model_name().indexOf(dmEntity.getModel_name()) < 0) {
						hitModel = false;
					}
				}

				// 每个单元格取值
				for (int iAxis=0;iAxis<=axis;iAxis++) {
					if (hitModel) {
						Date[] dates = getDayOfAxis(monCal, iAxis, itemType, cfmEntity.getCycle_type());

						CheckResultEntity cre = new CheckResultEntity();
						cre.setCheck_confirm_time_start(dates[0]);
						cre.setCheck_confirm_time_end(dates[1]);
						cre.setManage_id(dmEntity.getDevices_manage_id());
						cre.setCheck_file_manage_id(cfmEntity.getCheck_file_manage_id());
						cre.setItem_seq(dCsi.getItem_seq());

						// 已点检或之前范围
						List<CheckResultEntity> listCre = crMapper.getDeviceCheckInPeriod(cre);
						String jobNo = null;
						Date checkedDate = new Date(0);

						// 预先全划掉
						{
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
								CheckPosBean checkPos = checkPosData.get(itemSeq);
								int shift = iAxis;

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
							if (1 < cpd.signType) { // 操作者
								continue;
							}
							if (cpd.cycleType != itemType) { // 一致的周期
								continue;
							}
							cellName = XlsUtil.getExcelColCode(cpd.startX + iAxis * cpd.shiftX - 1)
									+ (cpd.startY + cpd.shiftY * iDev);
							cell = cacheXls.getRange(cellName);
							if (checkedDate.getTime() > 0) {
								cacheXls.SetValue(cell, DateUtil.toString(checkedDate, "y-M-d"));
								if ("D".equalsIgnoreCase(cpd.content)) {
									cacheXls.SetNumberFormatLocal(cell, "m-d");
								} else if ("C".equalsIgnoreCase(cpd.content)) {
									cacheXls.SetNumberFormatLocal(cell, "m月d日");
								}
							}
						}

						// 签章
						if (!isEmpty(jobNo)) {
							for (CheckPosBean cpn : checkPosName) {
								if (1 < cpn.signType) { // 操作者
									continue;
								}
								if (cpn.cycleType != itemType) { // 一致的周期
									continue;
								}
								if (checkPosManageNo.shiftY > 0) { // 多对象
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
									if ("V".equals(cpn.content)) {
										cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign_v\\" + jobNo.toUpperCase(), cell, pageZoom);
									} else {
										cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell, pageZoom);
									}
									cacheXls.SetValue(cell, "已千千千千千千千");
								}
							}
						}
					} else {
						// 不需要填写
						CheckPosBean checkPos = checkPosData.get(itemSeq);
						int shift = iAxis;
						cellName = XlsUtil.getExcelColCode(checkPos.startX + shift * checkPos.shiftX - 1)
								+ (checkPos.startY + checkPos.shiftY * iDev);
						cell = cacheXls.getRange(cellName);
						cacheXls.SetValue(cell, "-");
						XlsUtil.SetCellBackGroundColor(cell, "12566463"); // BFBFBF;
					}
				} // for Axis End
			} // for ChecksheetItem End
		} // for devices end
		cacheXls.Replace("已千千千千千千千", "");
	}

	/**
	 * 设定线长填写
	 *
	 * @param cacheXls
	 * @param sEncodedDeviceList
	 * @param checkPosDate
	 * @param checkPosName
	 * @param cfmEntity
	 * @param pageZoom
	 * @param crMapper
	 * @param monCal
	 * @param conn
	 */
	private void setLeaderCheck(XlsUtil cacheXls,
			List<String> sEncodedDeviceList, List<CheckPosBean> checkPosDate,
			List<CheckPosBean> checkPosName, CheckFileManageEntity cfmEntity,
			int pageZoom, CheckResultMapper crMapper, Calendar monCal,
			SqlSession conn) {
		Integer confirmCycle = cfmEntity.getConfirm_cycle();
		if (confirmCycle == null) return;
		for (CheckPosBean cpn : checkPosName) {
			if (2 == cpn.signType) {
				confirmCycle = cpn.cycleType;
				break;
			}
		}

		int maxAxis = getMaxAxis(confirmCycle, cfmEntity.getCycle_type());

		for (int iAxis=0;iAxis<=maxAxis;iAxis++) {
			Date[] dates = getDayOfAxis(monCal, iAxis, confirmCycle, cfmEntity.getCycle_type());

			CheckResultEntity dusEmtity = new CheckResultEntity();
			Set<String> manageIds = new HashSet<String>();
			dusEmtity.setCheck_confirm_time_start(dates[0]);
			dusEmtity.setCheck_confirm_time_end(dates[1]);
			dusEmtity.setCheck_file_manage_id(cfmEntity.getCheck_file_manage_id());
			for (String sEncodedDevice : sEncodedDeviceList) {
				manageIds.add(sEncodedDevice);
			}
			dusEmtity.setManage_ids(manageIds);
			List<CheckResultEntity> upperStamp = crMapper.getDeviceUpperStamp(dusEmtity);

			if (upperStamp.size() > 0) {
				Date checkedDate = upperStamp.get(0).getCheck_confirm_time();
				String jobNo = upperStamp.get(0).getJob_no();

				for (CheckPosBean cpd : checkPosDate) {
					if (2 != cpd.signType) { // 确认者
						continue;
					}

					String cellName = XlsUtil.getExcelColCode(cpd.startX + iAxis * cpd.shiftX - 1)
							+ cpd.startY;
					Dispatch cell = cacheXls.getRange(cellName);
					if (checkedDate.getTime() > 0) {
						cacheXls.SetValue(cell, DateUtil.toString(checkedDate, "y-M-d"));
						if ("D".equalsIgnoreCase(cpd.content)) {
							cacheXls.SetNumberFormatLocal(cell, "m-d");
						} else if ("C".equalsIgnoreCase(cpd.content)) {
							cacheXls.SetNumberFormatLocal(cell, "m月d日");
						}
					}
				}

				// 签章
				if (!isEmpty(jobNo)) {
					for (CheckPosBean cpn : checkPosName) {
						if (2 != cpn.signType) { // 确认者
							continue;
						}
						String cellName = XlsUtil.getExcelColCode(cpn.startX + iAxis * cpn.shiftX - 1)
								+ cpn.startY;
						Dispatch cell = cacheXls.getRange(cellName);

						if ("V".equals(cpn.content)) {
							cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign_v\\" + jobNo.toUpperCase(), cell, pageZoom);
						} else {
							cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell, pageZoom);
						}
					}
				}
			}
		}
	}

	private static final int INSERT_START_ROW_FOR_JIG =6;
	private static final int INSERT_START_COL_FOR_JIG =6; // G

	/**
	 * 生成治具文档
	 */
	public void makeFileJigs(CheckedFileStorageEntity cfsEntity,
			List<String> sEncodedJigList, String sJigOperaterId, SqlSession conn) {
		JigCheckResultMapper jcrMapper = conn.getMapper(JigCheckResultMapper.class);
		JigManageMapper tmMapper = conn.getMapper(JigManageMapper.class);

		Date filingDate = cfsEntity.getFiling_date();

		Calendar adjustCal = Calendar.getInstance();
		adjustCal.setTime(filingDate);
		adjustCal.set(Calendar.HOUR_OF_DAY, 0);
		adjustCal.set(Calendar.MINUTE, 0);
		adjustCal.set(Calendar.SECOND, 0);
		adjustCal.set(Calendar.MILLISECOND, 0);

		// 复制模板到临时文件
		String ext = ".xls";
		String srcPath = PathConsts.BASE_PATH + PathConsts.DEVICEINFECTION + "\\QF0601-5专用工具定期清点保养记录.xls";
		String cacheFilename =  cfsEntity.getStorage_file_name() + filingDate.getTime() + ext;
		String cachePath = PathConsts.BASE_PATH + PathConsts.LOAD_TEMP + "\\" + DateUtil.toString(filingDate, "yyyyMM") + "\\" + cacheFilename;
		try {
			FileUtils.copyFile(new File(srcPath), new File(cachePath));
		} catch (IOException e) {
			_logger.error(e.getMessage(), e);
			return;
		}
		String targetPath = PathConsts.BASE_PATH + PathConsts.INFECTIONS + "\\" +
				RvsUtils.getBussinessYearString(adjustCal) + "\\QF0601-5";
		String targetFile = targetPath + "\\" + cfsEntity.getStorage_file_name() + ".pdf";

		String line_id = cfsEntity.getLine_id();
		String position_id = cfsEntity.getPosition_id();

		XlsUtil cacheXls = null;
		try {
			cacheXls = new XlsUtil(cachePath, false);
			cacheXls.SelectActiveSheet();

			// 取得本期
			String bperiod = RvsUtils.getBussinessYearString(adjustCal);

			String sLineName = "";
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
				sLineName = pEntity.getProcess_code() + " " + pEntity.getName();
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
			CheckResultMapper crMapper = conn.getMapper(CheckResultMapper.class);
			setJig(cacheXls, sEncodedJigList, sJigOperaterId, axis, axisType, jcrMapper, tmMapper, crMapper, monCal, conn);

			File fTargetPath = new File(targetPath);
			if (!fTargetPath.exists()) {
				fTargetPath.mkdirs();
			}
			cacheXls.SaveAsPdf(targetFile); // SaveAsPdf
			cacheXls.Release();

			// 查询备注
			// 备注信息
			List<Map<String, String>> comments = new ArrayList<Map<String, String>>();
			JigManageEntity conditionOfJig = new JigManageEntity();
			conditionOfJig.setProvide_date_start(cfsEntity.getStart_record_date());
			conditionOfJig.setProvide_date_end(cfsEntity.getFiling_date());
			CheckResultEntity conditionOfComment = new CheckResultEntity();
			conditionOfComment.setCheck_confirm_time_start(cfsEntity.getStart_record_date());
			conditionOfComment.setCheck_confirm_time_end(cfsEntity.getFiling_date());
			for (int iDev = 0; iDev < sEncodedJigList.size(); iDev ++) {
				conditionOfJig.setJig_manage_id(sEncodedJigList.get(iDev));

				JigManageEntity provide_date = tmMapper.checkProvideInPeriod(conditionOfJig);
				JigManageEntity waste_date = tmMapper.checkWasteInPeriod(conditionOfJig);

				// 发布日期
				if (provide_date != null && provide_date.getProvide_date() != null) {
					Map<String, String> comment = new HashMap<String, String>();
					comment.put("manage_code", provide_date.getManage_code());
					comment.put("job_no", provide_date.getProvider());
					comment.put("comment", ApplicationMessage.WARNING_MESSAGES.getMessage("info.infect.jig.filing.provide", 
							provide_date.getManage_code(), provide_date.getProcess_code()));
					comment.put("comment_date", DateUtil.toString(provide_date.getProvide_date(), DateUtil.ISO_DATE_PATTERN));
					comments.add(comment);
				}
				// 废弃日期
				if (waste_date != null && waste_date.getWaste_date() != null) {
					Map<String, String> comment = new HashMap<String, String>();
					comment.put("manage_code", waste_date.getManage_code());
					comment.put("job_no", waste_date.getProvider());
					comment.put("comment", ApplicationMessage.WARNING_MESSAGES.getMessage("info.infect.jig.filing.waste", 
							waste_date.getManage_code(), waste_date.getProcess_code()));
					comment.put("comment_date", DateUtil.toString(waste_date.getWaste_date(), DateUtil.ISO_DATE_PATTERN));
					comments.add(comment);
				}

				// 备注信息
				conditionOfComment.setManage_id(sEncodedJigList.get(iDev));
				List<CheckResultEntity> commentsList = crMapper.getJigCheckCommentInPeriodByManageId(conditionOfComment);
				for (CheckResultEntity cre : commentsList) {
					Map<String, String> comment = new HashMap<String, String>();
					comment.put("manage_code", cre.getManage_code());
					comment.put("job_no", cre.getJob_no());
					comment.put("comment", cre.getComment());
					comment.put("comment_date", DateUtil.toString(cre.getCheck_confirm_time(), DateUtil.ISO_DATE_PATTERN));
					comments.add(comment);
				}

			}

			// 附加备注页
			addCommentPage(comments, cachePath, targetPath + "\\" + cfsEntity.getStorage_file_name());

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
	 * @param crMapper 
	 * @param monCal 去期间头
	 * @param conn
	 */
	private void setJig(XlsUtil cacheXls, List<String> sEncodedJigList, String sJigOperaterId,
			int axis, int axisType,
			JigCheckResultMapper jcrMapper, JigManageMapper tmMapper, CheckResultMapper crMapper, Calendar monCal, SqlSession conn) {
		// 循环填写各治具
		int insertRow = INSERT_START_ROW_FOR_JIG;

		Map<Integer, Map<String, Integer>> jobNoMap = new HashMap<Integer, Map<String, Integer>>();
		Map<Integer, String> checkTimeMap = new HashMap<Integer, String>();
		for (int iJig = 0; iJig < sEncodedJigList.size(); iJig++) {

			String jig_id = sEncodedJigList.get(iJig);
			JigManageEntity tmEntity = tmMapper.getByKey(jig_id);

			cacheXls.getAndActiveSheetBySeq(2);

			Dispatch selection = cacheXls.Select("1:1");
			Dispatch.call(selection, "Copy");

			// Sheets("Sheet1").Select
			cacheXls.getAndActiveSheetBySeq(1);
			// Rows("4:4").Select
			selection = cacheXls.SelectRow(insertRow + ":" + insertRow);
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
			// 树立
			cacheXls.SetValue("E" + insertRow, "" + tmEntity.getCount_in());

			// 循环填写每月份 G->
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(monCal.getTime());
			Calendar endcal = Calendar.getInstance();
			endcal.setTime(monCal.getTime());

			// 点检结果
			for (int iAxis = 0; iAxis <= axis; iAxis++) {
				endcal.add(Calendar.MONTH, 1);
				JigCheckResultEntity condition = new JigCheckResultEntity();
				condition.setManage_id(jig_id);
				condition.setFirstDate(DateUtil.toString(startCal.getTime(), DateUtil.DATE_PATTERN));
				condition.setLastDate(DateUtil.toString(endcal.getTime(), DateUtil.DATE_PATTERN));
				List<JigCheckResultEntity> result = jcrMapper.searchCheckResult(condition);
				if (result.size() > 0) {
					String sCheckedStatus = null;
					sCheckedStatus = result.get(0).getChecked_status();
					checkTimeMap.put(iAxis, result.get(0).getCheck_confirm_time());
					for(JigCheckResultEntity result0 : result) {
						String jobNo = result0.getJob_no();
						recordJobNoMap(jobNoMap, iAxis, jobNo);
					}

					cacheXls.SetValue(XlsUtil.getExcelColCode(INSERT_START_COL_FOR_JIG + iAxis)
							+ insertRow, getNoScale(getFileStatusD(sCheckedStatus, null)));
				} else {
					cacheXls.SetValue(XlsUtil.getExcelColCode(INSERT_START_COL_FOR_JIG + iAxis)
							+ insertRow, "/");
				}
				startCal.add(Calendar.MONTH, 1);
			}

			insertRow++;
		}

		// 担当印
		Dispatch sign = cacheXls.Locate("担当印");
		String rowSign = null;
		while (true) {
			rowSign = XlsUtil.getExcelRowNo(sign);
			String col = XlsUtil.getExcelColNo(sign);
			if ("6".equals(col)) {
				break;
			}
			sign = cacheXls.LocateNext(sign);
			if (sign == null) break;
		}
		String[] p_o = sJigOperaterId.split("_");
		if (rowSign != null) {
			Integer iRowSign = Integer.parseInt(rowSign);
			Calendar startCal = Calendar.getInstance();
			startCal.setTime(monCal.getTime());
			Calendar endcal = Calendar.getInstance();
			endcal.setTime(monCal.getTime());
			for (int iAxis = 0; iAxis <= axis; iAxis++) {
				endcal.add(Calendar.MONTH, 1);
				String jobNo = getBestJobNo(jobNoMap, iAxis);
				if (jobNo != null) {
					Dispatch cell = cacheXls.getRange(XlsUtil.getExcelColCode(INSERT_START_COL_FOR_JIG + iAxis) + rowSign);
					cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell);

					String checkedDate = checkTimeMap.get(iAxis);
					if (checkedDate != null) {
						cacheXls.SetValue(XlsUtil.getExcelColCode(INSERT_START_COL_FOR_JIG + iAxis) + (iRowSign + 1), 
								checkedDate);
						cacheXls.SetNumberFormatLocal(XlsUtil.getExcelColCode(INSERT_START_COL_FOR_JIG + iAxis) + (iRowSign + 1), "m-d");
					}

					// 线长 确认印
					CheckResultEntity dusEntity = new CheckResultEntity();
					dusEntity.setCheck_confirm_time_start(startCal.getTime());
					dusEntity.setCheck_confirm_time_end(endcal.getTime());
					dusEntity.setPosition_id(p_o[1]);
					dusEntity.setSection_id(p_o[0]);
					List<CheckResultEntity> upperStamp = crMapper.getJigUpperStamp(dusEntity);

					if (upperStamp.size() > 0) {
						Date dConfirmDate = upperStamp.get(0).getCheck_confirm_time();
						jobNo = upperStamp.get(0).getJob_no();

						cell = cacheXls.getRange(XlsUtil.getExcelColCode(INSERT_START_COL_FOR_JIG + iAxis) + (iRowSign + 2));
						cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo.toUpperCase(), cell);

						cacheXls.SetValue(XlsUtil.getExcelColCode(INSERT_START_COL_FOR_JIG + iAxis) + (iRowSign + 3), 
								DateUtil.toString(dConfirmDate, "M-d"));
						cacheXls.SetNumberFormatLocal(XlsUtil.getExcelColCode(INSERT_START_COL_FOR_JIG + iAxis) + (iRowSign + 3), "m-d");
					}
					startCal.add(Calendar.MONTH, 1);
				}
			}
		}

	}

	/**
	 * 附加备注页
	 * @param comments
	 * @param cachePath
	 * @param targetFileName
	 * @throws IOException
	 */
	private void addCommentPage(List<Map<String, String>> comments, String cachePath, String targetFileName) throws IOException {
		if (comments.size() > 0) {
			// Sort
			Collections.sort(comments, new Comparator<Map<String, String>> (){
				@Override
				public int compare(Map<String, String> comment1, Map<String, String> comment2) {
					return comment1.get("comment_date").compareTo(comment2.get("comment_date"));
				}
			});

			int pageNo = 0, itemNo = 0;
			// 取得点检表信息
			String templateCommentFileXls = PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\点检备注.xlsx";

			do {
				pageNo = 0;
				FileUtils.copyFile(new File(templateCommentFileXls), new File(cachePath + "_comment.xls"));

				String targetCommentFileXls = targetFileName + "_comment.pdf";
				XlsUtil cacheXls = null;
				try {
					cacheXls = new XlsUtil(cachePath + "_comment.xls", false);
					cacheXls.SelectActiveSheet();

					int setLine = 5; // Const
					for ( ; itemNo < comments.size(); itemNo++) {
						Map<String, String> comment = comments.get(itemNo);
						cacheXls.SetValue("B" + setLine, comment.get("manage_code"));
						cacheXls.SetValue("C" + setLine, comment.get("comment_date"));
						cacheXls.sign(PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + comment.get("job_no").toUpperCase(), "D" + setLine);
						cacheXls.SetValue("E" + setLine, comment.get("comment"));
						setLine +=2;
						pageNo++;
						if (pageNo > COMMENTS_PAGE_ITEM) break;
					}

//					/ 保存到 PDF
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
				joinPdf(targetFileName + ".pdf", targetCommentFileXls);
				if (itemNo >= comments.size()) break;
			} while(true);
		}
	}

	/**
	 * 点检最多人记录
	 * @param jobNoMap
	 * @param iAxis
	 * @param jobNo
	 */
	private void recordJobNoMap(Map<Integer, Map<String, Integer>> jobNoMap,
			Integer iAxis, String jobNo) {
		if (jobNo == null) return;
		if (!jobNoMap.containsKey(iAxis)) {
			jobNoMap.put(iAxis, new HashMap<String, Integer> ());
		}
		Map<String, Integer> jobNoMapOfAxis = jobNoMap.get(iAxis);
		if (!jobNoMapOfAxis.containsKey(jobNo)) {
			jobNoMapOfAxis.put(jobNo, 0);
		}
		jobNoMapOfAxis.put(jobNo, jobNoMapOfAxis.get(jobNo) + 1);
	}

	private String getBestJobNo(Map<Integer, Map<String, Integer>> jobNoMap,
			int iAxis) {
		Map<String, Integer> jobNoMapOfAxis = jobNoMap.get(iAxis);
		if (jobNoMapOfAxis == null) return null;
		int maxCount = 0; String bestJobNo = null;
		for (String jobNo : jobNoMapOfAxis.keySet()) {
			if (jobNoMapOfAxis.get(jobNo) > maxCount) {
				maxCount = jobNoMapOfAxis.get(jobNo);
				bestJobNo = jobNo;
			}
		}
		return bestJobNo;
	}

	private String getFileStatusD(String status, BigDecimal digit) {

		if (digit != null) {
//			if (scale == null) {
				return getNoScale(digit);
//			} else {
//				return getScale(digit, scale);
//			}
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
		private int cycleType = 0;
		private int signType = 0;
		private String content = "";
	}
}