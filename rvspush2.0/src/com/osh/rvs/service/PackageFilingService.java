package com.osh.rvs.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.entity.MaterialEntity;
import com.osh.rvs.mapper.push.ProductionFeatureMapper;

import framework.huiqing.common.util.copy.DateUtil;

public class PackageFilingService {

	public static Logger _log = Logger.getLogger("PackageFilingService");

	public void packDailyFile(Calendar date, Map<String, String> modelMap,
			SqlSession conn) {
		ProductionFeatureMapper mapper = conn.getMapper(ProductionFeatureMapper.class);

		// 查询各个型号当天有无打包
		for (String modelName : modelMap.keySet()) {
			String modelId = modelMap.get(modelName);

			List<MaterialEntity> list = mapper.getPackageMaterialByModelInDay(modelId, date.getTime());

			if (list.size() > 0) {
				String tFilePath = 
						PathConsts.BASE_PATH + PathConsts.REPORT_TEMPLATE + "\\package\\" + modelName + ".xls";

				Map<String, Integer> templateSetting = readTemplateSetting(tFilePath, modelName);

				fillTemplate(modelName, date, tFilePath, templateSetting, list);
			}
		}
		
	}

	/**
	 * 每日自动生成
	 * 
	 * @param modelName
	 * @param date
	 * @param tFilePath
	 * @param templateSetting
	 * @param list
	 */
	private void fillTemplate(String modelName, Calendar date,
			String tFilePath, Map<String, Integer> templateSetting, List<MaterialEntity> list) {
		Integer iSnCount = templateSetting.get("SN号 数");
		if (iSnCount == null) return;

		int iPageCnt = new Double((list.size() + 1.0) / iSnCount + 0.5).intValue();
		int listCursor = 0;
		String dateStringFile = DateUtil.toString(date.getTime(), "yyyy-MM-dd");
		String dateStringContent = DateUtil.toString(date.getTime(), "yyyy.MM.dd").replaceAll("\\.0", ". ");

		try {
			for (int iPg = 0; iPg < iPageCnt; iPg++) {
				String targetPath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\package\\" 
						+ dateStringFile.substring(0, 4) + dateStringFile.substring(5, 7)
						+ "\\" + modelName + "_ASSY包装检查表-" + dateStringFile + (iSnCount == 0 ? "" : "_" + (iPg + 1)) + ".xls";
						;

				FileUtils.copyFile(new File(tFilePath), new File(targetPath));

				FileInputStream fis = null;
				FileOutputStream fos = null;

				try {
					fis = new FileInputStream(targetPath);
					HSSFWorkbook work = new HSSFWorkbook(fis);

					HSSFSheet sheet = work.getSheetAt(0);
					HSSFPatriarch patriarch = sheet.getDrawingPatriarch();

					Font ftSerial = work.createFont();
					ftSerial.setFontHeight((short) 264); // 11 * 24

					// LOT数
					HSSFCell cell = getCellFromSheet(sheet, templateSetting.get("LOT数 X"), templateSetting.get("LOT数 Y"), 1, 0);
					if (cell != null) {
						cell.setCellValue("" + (iPg + 1));
					}

					// 作业日
					cell = getCellFromSheet(sheet, templateSetting.get("作业日 X"), templateSetting.get("作业日 Y"), 1, 0);
					cell.setCellValue(dateStringContent);

					// 作业者工号
					Set<String> jobNos = new LinkedHashSet<String>();

					// SN：
					for (int iSn = 0; iSn < iSnCount && listCursor < list.size(); iSn++, listCursor++) {
						MaterialEntity entity = list.get(listCursor);
						String snKey = "SN" + iSn + "： X";
						// SN：
						cell = getCellFromSheet(sheet, templateSetting.get(snKey), templateSetting.get("SN： Y"),  0, 0);
						if (cell == null) throw new Exception("SN位置定位错误!");

						// 工号记录
						jobNos.add(entity.getJob_no());

						String snString = "SN：\r\n " + entity.getSerial_no();
						int serialPos = "SN：\r\n".length();
						HSSFRichTextString rts = new HSSFRichTextString(snString); 
						rts.applyFont(serialPos, snString.length(), ftSerial);
						cell.setCellValue(rts);

						// 合否判定
						drawCheckOval(sheet, patriarch, templateSetting.get(snKey), templateSetting.get("SN： Y") + 1);

						// 作业手顺
						drawCheckOval(sheet, patriarch, templateSetting.get(snKey), templateSetting.get("作业手顺 Y"));

						// 编号
						drawCheckOval(sheet, patriarch, templateSetting.get(snKey), templateSetting.get("编号 Y"));

						// SN号
						drawCheckOval(sheet, patriarch, templateSetting.get(snKey), templateSetting.get("SN号 Y"));

						// 数量
						drawCheckOval(sheet, patriarch, templateSetting.get(snKey), templateSetting.get("数量 Y"));

						// 制品代码
						drawCheckOval(sheet, patriarch, templateSetting.get(snKey), templateSetting.get("制品代码 Y"));

						// 包装标签(KC标签)
						drawCheckOval(sheet, patriarch, templateSetting.get(snKey), templateSetting.get("包装标签(KC标签) Y"));
					}

					// 检查担当者
					cell = getCellFromSheet(sheet, templateSetting.get("检查担当者 X"), templateSetting.get("检查担当者 Y"), 1, 0);
					// 		日期
					cell.setCellValue("签名　　　　　日期 " + dateStringContent);
					//		签名
					int iStamp = 0;
					for (String jobNo : jobNos) {
						String stampPath = PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo;
						insertStamp(work, sheet, patriarch, templateSetting.get("检查担当者 X") + 1, templateSetting.get("检查担当者 Y"),
								iStamp, stampPath);
						iStamp++;
					}

					// 保存文件
					fos = new FileOutputStream(targetPath);
					work.write(fos);
				} catch (Exception e) {
					_log.error(e.getMessage(), e);
				} finally {
					if (fis != null) {
						try {
							fis.close();
							fis = null;
						} catch (IOException e) {
							_log.error(e.getMessage(), e);
						}
					}
					if (fos != null) {
						try {
							fos.close();
							fos = null;
						} catch (IOException e) {
							_log.error(e.getMessage(), e);
						}
					}
				}

				new File(targetPath).setLastModified(date.getTimeInMillis());
			}
		} catch (Exception e) {
			return;
		}
	}

	private void drawCheckOval(HSSFSheet sheet, HSSFPatriarch patriarch, int col, int row) {
		CellRangeAddress range = getMergedRegion(sheet, row, col);
		// 椭圆
		HSSFClientAnchor anchor = null;
		if (range == null) {
			anchor = new HSSFClientAnchor(100, 80, 500, 200, (short)col, (short)row, (short)col, (short)row);
		} else {
			// 0 = dx1; 1=dx2; 2=col1; 3=col2 
			Integer[] anchorPos = calcAnchorPos(sheet, range, 1, 0);
			anchor = new HSSFClientAnchor(anchorPos[0], 80, anchorPos[1], 200, anchorPos[2].shortValue(), (short)row, 
					anchorPos[3].shortValue(), (short)row);
		}
		HSSFSimpleShape shape = patriarch.createSimpleShape(anchor);
		shape.setShapeType(HSSFSimpleShape.OBJECT_TYPE_OVAL);   
		shape.setLineStyle(HSSFSimpleShape.LINESTYLE_SOLID);
		shape.setLineStyleColor(255, 0, 0);
		shape.setNoFill(true);
	}

	private HSSFCell getCellFromSheet(HSSFSheet sheet, Integer posX, Integer posY, int switchX, int switchY) {
		if (posX == null || posY == null) return null;

		HSSFRow row = sheet.getRow(posY + switchY);
		if (row == null) return null;

		HSSFCell cell = row.getCell(posX + switchX);
		if (cell == null) cell = row.createCell(posX + switchX);
		return cell;
	}

	private Map<String, Integer> readTemplateSetting(String tFilePath, String modelName) {
		Map<String, Integer> templateSetting = new HashMap<String, Integer>();

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(tFilePath);
			HSSFWorkbook work = new HSSFWorkbook(fis);

			HSSFSheet sheet = work.getSheetAt(0);

			int iSn = 0;
			for (int iRow = 0; iRow <= sheet.getLastRowNum(); iRow++) {
				HSSFRow row = sheet.getRow(iRow);
				if (row != null) {

					for (int iCell = 0; iCell <= row.getLastCellNum(); iCell++) {
						HSSFCell cell = row.getCell(iCell);
						if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
							String cellValue = cell.getStringCellValue();
							if (cellValue != null) {
								cellValue = cellValue.trim();
								switch(cellValue) {
								case "LOT数" : {
									templateSetting.put("LOT数 X", iCell);
									templateSetting.put("LOT数 Y", iRow);
									break;
								}
								case "作业日" : {
									templateSetting.put("作业日 X", iCell);
									templateSetting.put("作业日 Y", iRow);
									break;
								}
								case "检查担当者" : {
									templateSetting.put("检查担当者 X", iCell);
									templateSetting.put("检查担当者 Y", iRow);
									break;
								}
								case "检查责任者" : {
									templateSetting.put("检查责任者 X", iCell);
									templateSetting.put("检查责任者 Y", iRow);
									break;
								}
								case "SN：" : {
									templateSetting.put("SN： Y", iRow);
									for (; iSn < 10; iSn++) {
										String snKey = "SN" + iSn + "： X";
										if (!templateSetting.containsKey(snKey)) {
											templateSetting.put(snKey, iCell);
											break;
										}
									}
									break;
								}
								case "作业手顺" : {
									templateSetting.put("作业手顺 Y", iRow);
									break;
								}
								case "编号" : {
									templateSetting.put("编号 Y", iRow);
									break;
								}
								case "SN号" : {
									templateSetting.put("SN号 Y", iRow);
									break;
								}
								case "数量" : {
									templateSetting.put("数量 Y", iRow);
									break;
								}
								case "制品代码" : {
									templateSetting.put("制品代码 Y", iRow);
									break;
								}
								case "包装标签(KC标签)" : {
									templateSetting.put("包装标签(KC标签) Y", iRow);
									break;
								}
								}
							}
						}
					}
				}
			}

			templateSetting.put("SN号 数", (iSn + 1));

			fis.close();
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		} finally {
			fis = null;
		}

		return templateSetting;
	}

	private CellRangeAddress getMergedRegion(HSSFSheet sheet, int row, int column) {
		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress range = sheet.getMergedRegion(i);
			int firstColumn = range.getFirstColumn();
			int lastColumn = range.getLastColumn();
			int firstRow = range.getFirstRow();
			int lastRow = range.getLastRow();
			if (row >= firstRow && row <= lastRow) {
				if (column >= firstColumn && column <= lastColumn) {
					return range;
				}
			}
		}
		return null;
	}

	/**
	 * 计算在合并单元格中, 画圈的起始坐标(x)
	 * 
	 * @param sheet
	 * @param range
	 * @param calcType 1=中间靠左 2=左侧图章
	 * @param px 加偏移
	 * @return 0 = dx1; 1=dx2; 2=col1; 3=col2 
	 */
	private Integer[] calcAnchorPos(HSSFSheet sheet, CellRangeAddress range, int calcType, int px) {
		Integer[] retObj = new Integer[4];

		int firstColumn = range.getFirstColumn();
		int lastColumn = range.getLastColumn();
		int firstRow = range.getFirstRow();

		// 总宽度
		int widthTotal = 0;
		List<Integer> widthCols = new ArrayList<Integer>();
		for (int i = firstColumn; i <= lastColumn; i++) {
			HSSFCell cell = getCellFromSheet(sheet, i, firstRow, 0, 0);
			int w = sheet.getColumnWidth(cell.getColumnIndex()); // 单位不是像素，是1/256个字符宽度
			widthCols.add(w);
			widthTotal += w;
		}

		// 插入图形的结束宽度
		int endW = 0;
		// 插入图形的起始宽度
		int startW = 0;

		switch(calcType) {
		case 1: {
			// 计算中间宽度与起点终点
			endW = widthTotal / 2 - 128; // 半个'/'
			startW = endW - 1024; // 'PASS';
			break;
		}
		case 2: {
			// [ ]签名
			startW = 1024 + 256 + px;
			endW = startW + 2560;// + 77px 图章宽度～10半角字符
			break;
		}
		}

		widthTotal = 0;
		for(int i = 0; i < widthCols.size(); i++) {
			if (retObj[0] != null && retObj[1] != null) break;

			int widthTotalAdded = widthTotal + widthCols.get(i);
			if (retObj[0] == null && widthTotalAdded > startW) { // 
				// 起点在这个格子里
				// col1
				retObj[2] = firstColumn + i;
				// dx1
				retObj[0] = 1023 * ((startW - widthTotal) * 100 / widthCols.get(i)) / 100;
			}
			if (retObj[1] == null && widthTotalAdded > endW) { // 
				// 起点在这个格子里
				// col1
				retObj[3] = firstColumn + i;
				// dx1
				retObj[1] = 1023 * ((endW - widthTotal) * 100 / widthCols.get(i)) / 100;
			}
			widthTotal = widthTotalAdded; 
		}

		return retObj;
	}

	/**
	 * 图片插入单元格
	 * @param work 工作簿
	 * @param sheet 工作表
	 * @param patriarch 图版
	 * @param iCol (首)单元格列
	 * @param iRow (首)单元格行
	 * @param px 偏移
	 * @param fileName 图片路径
	 */
	public void insertStamp(HSSFWorkbook work, HSSFSheet sheet, HSSFPatriarch patriarch,
			int iCol, int iRow, int px, String fileName) {

		try {
			BufferedImage bufferImg = ImageIO.read(new File(fileName));
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
			ImageIO.write(bufferImg, "png", byteArrayOut);

			CellRangeAddress range = getMergedRegion(sheet, iRow, iCol);
			if (range == null) range = new CellRangeAddress(iRow, iRow, iCol, iCol);
			// 0 = dx1; 1=dx2; 2=col1; 3=col2 
			Integer[] anchorPos = calcAnchorPos(sheet, range, 2, px * 768); // 姓的位置～3个半角字符
			HSSFClientAnchor anchor = new HSSFClientAnchor(anchorPos[0], 20, anchorPos[1], 254, anchorPos[2].shortValue(), iRow, 
					anchorPos[3].shortValue(), iRow);
			anchor.setAnchorType(ClientAnchor.MOVE_DONT_RESIZE);
			patriarch.createPicture(anchor, work.addPicture(byteArrayOut.toByteArray(),
							HSSFWorkbook.PICTURE_TYPE_PNG));
		} catch (Exception e) {
			_log.error("图片文件不存在" + fileName, e);
		}
	}

	public void respond(String fileName, String jobNo) {

		fileName = fileName.replaceAll("BZJCB", "包装检查表");
		String dateString = fileName.replaceAll(".*(\\d{4}\\-\\d{2}\\-\\d{2}).*", "$1");
		dateString = dateString.substring(0, 4) + dateString.substring(5, 7);
		String modelName = fileName.split("_") [0];

		String filePath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\package\\" 
				+ dateString + "\\" + fileName;
		String targetPath = PathConsts.BASE_PATH + PathConsts.REPORT + "\\package\\" 
				+ dateString + "\\confirm\\" + fileName;

		try {
			FileUtils.copyFile(new File(filePath), new File(targetPath));

			Map<String, Integer> templateSetting = readTemplateSetting(filePath, modelName);

			FileInputStream fis = null;
			FileOutputStream fos = null;

			try {
				fis = new FileInputStream(targetPath);
				HSSFWorkbook work = new HSSFWorkbook(fis);

				HSSFSheet sheet = work.getSheetAt(0);
				HSSFPatriarch patriarch = sheet.getDrawingPatriarch();

				String stampPath = PathConsts.BASE_PATH + PathConsts.IMAGES + "\\sign\\" + jobNo;
				insertStamp(work, sheet, patriarch, templateSetting.get("检查责任者 X") + 1, templateSetting.get("检查责任者 Y"),
						0, stampPath);
				// 		日期
				HSSFCell cell = getCellFromSheet(sheet, templateSetting.get("检查责任者 X"), templateSetting.get("检查责任者 Y"), 1, 0);
				cell.setCellValue("签名　　　　　日期 " + DateUtil.toString(new Date(), "yyyy.MM.dd").replaceAll("\\.0", ". "));

				// 保存文件
				fos = new FileOutputStream(targetPath);
				work.write(fos);

			} catch (Exception e) {
				_log.error(e.getMessage(), e);
			} finally {
				if (fis != null) {
					try {
						fis.close();
						fis = null;
					} catch (IOException e) {
						_log.error(e.getMessage(), e);
					}
				}
				if (fos != null) {
					try {
						fos.close();
						fos = null;
					} catch (IOException e) {
						_log.error(e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
		
	}
}
