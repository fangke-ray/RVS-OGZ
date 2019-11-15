package com.osh.rvs.service;

import static framework.huiqing.common.util.CommonStringUtil.isEmpty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.mapper.LineLeaderMapper;
import com.osh.rvs.mapper.LineTimespaceMapper;

public class LineTimespaceService {
	private static Map<String, String> TYPES = new HashMap<String, String>();
	static {
		TYPES.put("TJF", "JF");
		TYPES.put("PCF", "CF");
	}

	public List<Map<String, String>> getProductionFeatures(String line_id, SqlSession conn) {
		LineTimespaceMapper mapper = conn.getMapper(LineTimespaceMapper.class);

// 		Long now = (new Date().getTime() + 28800000) % 86400000 / 60000;

		List<Map<String, Object>> productionFeatures = mapper.getProductionFeatures(line_id);
		if ("00000000101".equals(line_id)) {
			productionFeatures.addAll(mapper.getProductionFeatures("00000000102"));
		}
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();

		String currentProcessCode = null;
		long pos = 480;

		// 取得LB和STR的标准值
		int iLineProcessLbAlarmLever = 0,
		iLineProcessLbWarnLever = 0,
		iLineProcessStrAlarmLever = 0,
		iLineProcessStrWarnLever = 0;

		List<Map<String, String>> levels = mapper.getLevels();
		for (Map<String, String> level : levels) {
			String code = level.get("code");
			String value = level.get("value");
			if (code == null || value == null) continue;

			int iValue = Integer.parseInt(value);
			switch(code) {
			case "LINE_PROCESS_LB_WARN_LEVER" : iLineProcessLbWarnLever = iValue; break;
			case "LINE_PROCESS_LB_ALARM_LEVER" : iLineProcessLbAlarmLever = iValue; break;
			case "LINE_PROCESS_STR_WARN_LEVER" : iLineProcessStrWarnLever = iValue; break;
			case "LINE_PROCESS_STR_ALARM_LEVER" : iLineProcessStrAlarmLever = iValue; break;
			}
		}

		// 工位结束标记所在时段
		Map<String, Map<String, String>> finishPos = new HashMap<String, Map<String, String>>(); 

		for (Map<String, Object> feature : productionFeatures) {
			Map<String, String> retPf = new HashMap<String, String>();
			String materialId = "" + feature.get("material_id");
			retPf.put("material_id", materialId);
			Long useSeconds = (Long) feature.get("use_seconds");

			String processCode = "" + feature.get("process_code");
			String countIntoProcessCode = PathConsts.POSITION_SETTINGS.getProperty("timing.into." + processCode);

			retPf.put("o_process_code", processCode);
			if (isEmpty(countIntoProcessCode)) {
				retPf.put("process_code", processCode);
			} else {
				retPf.put("process_code", countIntoProcessCode);
// 			不归并的工位是完结工位
//				useSeconds = null;
			}
// 			后做好的工位是完结工位

			String rework = "" + feature.get("rework");
			retPf.put("rework", rework);
			// 新的柱子
			if (!processCode.equals(currentProcessCode)) {
				// 建立
				currentProcessCode = processCode;
				pos = 480;
			}

			String modelName = "" + feature.get("model_name");
			Object serialNo = feature.get("serial_no");
			if (serialNo != null) {
				retPf.put("serial_no", "" + serialNo);
			}
			retPf.put("model_name",  modelName);
			retPf.put("model_group", getModelGroup(modelName));

			Long actionTime = (Long) feature.get("action_time");
			Long finishTime = (Long) feature.get("finish_time");
			Long spareMinutes = (finishTime - actionTime);

			if (actionTime < -475) actionTime = -475l;
			if ((actionTime - pos) > 2 && isEmpty(countIntoProcessCode)) {
				retPf.put("pauseFrom", "" + (pos - 475));
				retPf.put("pauseTime", "" + (actionTime - pos));
			}
			pos = finishTime;

			retPf.put("action_time",  Long.toString(actionTime - 480 + 5));
			retPf.put("finish_time",  Long.toString(finishTime - 480 + 5));
			retPf.put("spare_minutes",  Long.toString(spareMinutes));

			String strPosMaterial = processCode + materialId;
			if (countIntoProcessCode != null) {
				strPosMaterial = countIntoProcessCode + materialId;
			}

			if (useSeconds != null) {
				if (useSeconds == -1) {
					
				} else {
					String categoryName = "" + feature.get("CATEGORY_NAME");
					String level = "" + feature.get("level");
					String sOvertime = null;
					try {
						sOvertime = RvsUtils.getLevelOverLine(modelName, categoryName, level, null, processCode);
						Double dOverMinutes = Double.parseDouble(sOvertime);
						boolean overtime = false;
						if (dOverMinutes * 60 <= useSeconds) {
							retPf.put("overtime",  "true");
							retPf.put("use_seconds", "" + useSeconds);
							overtime = true;
						}

						if (finishPos.containsKey(strPosMaterial)) {
							Long thatFinishTime = Long.valueOf(finishPos.get(strPosMaterial).get("finish_time"));
							if ((finishTime - 480 + 5) < thatFinishTime) {
								// 本次不算结束
							} else {
								// 以前记录的结束不算
								finishPos.get(strPosMaterial).put("finish", null);
								retPf.put("finish",  "true");
								finishPos.put(strPosMaterial, retPf);
							}
						} else {
							retPf.put("finish",  "true");
							finishPos.put(strPosMaterial, retPf);
						}

						// 同一工位的前时段片也标上
						for (Map<String, String> retBefore : ret) {
							if (retBefore.get("o_process_code").equals(processCode)
									&& retBefore.get("material_id").equals(materialId)
									&& retBefore.get("rework").equals(rework)) {
								if (overtime) {
									retBefore.put("overtime",  "true");
								}
								retPf.put("use_seconds", "" + useSeconds);
							}
						}
					} catch (Exception e) {
						
					}
				}
			}

			ret.add(retPf);

			Long operate_result = (Long) feature.get("operate_result");
			if (processCode != null && ("471".equals(processCode) || "362".equals(processCode)
					 || (processCode.startsWith("262")) || "006".equals(processCode)) 
					&& operate_result == 2) {
				// 取得烘干时间
				String categoryName = "" + feature.get("CATEGORY_NAME");
				String lineName = "" + feature.get("line_name");
				String dryingTime = RvsUtils.getDryTime(modelName, categoryName, lineName);

				// 判断分解是否有烘干
				BigDecimal bdStr = new BigDecimal(0);
				if (lineName.startsWith("分解")) {
					if (mapper.checkNoDrying(materialId, line_id)) {
						dryingTime = "0";
					}
					// 取得完成信息
					bdStr = mapper.getDecWorkingStandingRate(materialId, line_id, dryingTime);
				} else {
					// 取得完成信息
					bdStr = mapper.getWorkingStandingRate(materialId, line_id, dryingTime);
				}
				retPf.put("WTR",  bdStr.toString());
				if (iLineProcessStrWarnLever > 0 && bdStr.doubleValue() > iLineProcessStrWarnLever) {
					retPf.put("WTRST",  "warn");
				}
				if (iLineProcessStrAlarmLever > 0 && bdStr.doubleValue() > iLineProcessStrAlarmLever) {
					retPf.put("WTRST",  "alarm");
				}

				BigDecimal bdLb = getLineBalancing(materialId, line_id, mapper);
				// 取得完成信息
				retPf.put("LB", bdLb.toString());
				if (iLineProcessLbWarnLever > 0 && bdLb.doubleValue() < iLineProcessLbWarnLever) {
					retPf.put("LBST",  "warn");
				}
				if (iLineProcessLbAlarmLever > 0 && bdLb.doubleValue() < iLineProcessLbAlarmLever) {
					retPf.put("LBST",  "alarm");
				}
			}
		}
		return ret;
	}

	/**
	 * 计算线平衡
	 * @param materialId 维修对象
	 * @param lineId 工程
	 * @param mapper
	 * @return
	 */
	private BigDecimal getLineBalancing(String materialId, String lineId,
			LineTimespaceMapper mapper) {
		List<Map<String, Object>> positions = mapper.getLineBalancing(materialId, lineId);

		BigDecimal sumSeconds = new BigDecimal(-1);
		if (positions == null || positions.size() == 0) {
			return sumSeconds;
		}

		Map<String, Integer> pTiming = new HashMap<String, Integer>();

		// 每个工位的作业时间
		for (Map<String, Object> position : positions) {
			String processCode = "" + position.get("process_code");
			Object useSeconds = position.get("use_seconds");

			// 归并工位
			String timingIntoProcessCode = PathConsts.POSITION_SETTINGS.getProperty("timing.into." + processCode);
			if (isEmpty(timingIntoProcessCode)) {
				timingIntoProcessCode = processCode;
			}

			if (pTiming.containsKey(timingIntoProcessCode)) {
				// 合计作业时间
				pTiming.put(timingIntoProcessCode, (Integer) useSeconds + pTiming.get(timingIntoProcessCode));
			} else {
				// 记录作业时间
				pTiming.put(timingIntoProcessCode, (Integer) useSeconds);
			}
		}

		sumSeconds = new BigDecimal(0);
		int maxSeconds = 0;

		// 计算总作业时间和最大作业时间
		for (String processCode : pTiming.keySet()) {
			Integer useSeconds = pTiming.get(processCode);
			if (useSeconds > maxSeconds) maxSeconds = useSeconds;
			sumSeconds = sumSeconds.add(new BigDecimal(useSeconds));
		}

		return sumSeconds.multiply(new BigDecimal(100))
			.divide(new BigDecimal(maxSeconds * pTiming.size()), 1, BigDecimal.ROUND_HALF_UP);
	}

	private String getModelGroup(String modelName) {
		if (modelName == null || !modelName.contains("-"))
			return "Other";

		String sType = modelName.split("-")[0];
		if (TYPES.containsKey(sType))
			return TYPES.get(sType);

		return sType;
	}

	private static final BigDecimal WORK_MINUTE_OF_DAY = new BigDecimal(475);
	static final int TEN_O_CLOCK = 125;
	static final int TWELVE_O_CLOCK = 245;
	static final int ELEVEN_O_CLOCK_AND_THIRTY = 215;
	static final int FIFTEEN_O_CLOCK = 425;
	private static final int WORK_MINUTE_OF_DAY_WITH_RESTS = 475 + 5 + 10 + 60 + 10;

	public Map<String, String> getStandardColumn(String lineName, SqlSession conn) {

		StringBuffer sbCss = new StringBuffer();
		StringBuffer sbDiv = new StringBuffer();

		if (lineName == null) {
			sbCss.append("#standard_column > div > div:nth-child(1) {bottom: 5px; height: 60px;}");
			sbCss.append("#standard_column > div > div:nth-child(2) {bottom: 65px; height: 60px;}");
			sbCss.append("#standard_column > div > div:nth-child(3) {bottom: 125px; height: 60px;}");
			sbCss.append("#standard_column > div > div:nth-child(4) {bottom: 185px; height: 60px;}");
			sbCss.append("#standard_column > div > div:nth-child(5) {bottom: 245px; height: 60px;}");
			sbCss.append("#standard_column > div > div:nth-child(6) {bottom: 305px; height: 60px;}");
			sbCss.append("#standard_column > div > div:nth-child(7) {bottom: 365px; height: 60px;}");
			sbCss.append("#standard_column > div > div:nth-child(8) {bottom: 425px; height: 60px;}");
			sbCss.append("#standard_column > div > div:nth-child(9) {bottom: 485px; height: 75px;}");

			sbDiv.append("<div></div>");
			sbDiv.append("<div></div>");
			sbDiv.append("<div></div>");
			sbDiv.append("<div></div>");
			sbDiv.append("<div></div>");
			sbDiv.append("<div></div>");
			sbDiv.append("<div></div>");
			sbDiv.append("<div></div>");
			sbDiv.append("<div></div>");
		} else if ("组装/检查".equals(lineName)){
			BigDecimal bdFactor = new BigDecimal(3); // 3

			// 根据机型取得标准时间
			BigDecimal bdCycleTime = new BigDecimal(10);

			// 取得当日作业计划
			LineLeaderMapper llMapper = conn.getMapper(LineLeaderMapper.class);
			List<Map<String, Object>> todayProductPlan = llMapper.getTodayProductPlan("101"); // TODO 101

			int posBx3 = -1, quBx3 = 0;
			BigDecimal bdBx3CycleTime = null;

			for (int i = 0; i < todayProductPlan.size(); i++) {
				if ("BX3".equals(todayProductPlan.get(i).get("model_name"))) {
					posBx3 = i;
					quBx3 = (Integer) todayProductPlan.get(i).get("quantity");
					String sCycleTime = PathConsts.POSITION_SETTINGS.getProperty("overline.0.003." + "BX3");
					if (sCycleTime != null) {
						bdBx3CycleTime = new BigDecimal(sCycleTime);
					} else {
						bdBx3CycleTime = new BigDecimal(7);
					}
					break;
				}
			}

			//	String modelName = mapper.getTodayManufatorModelName();
			// (overline.0.000._default)
			BigDecimal bdLocate = new BigDecimal(5).multiply(bdFactor);

			Integer iB = 0;

			for (int i = 0; i < todayProductPlan.size(); i++) {
				if (i == posBx3) {
					continue;
				}
				String modelName = "" + todayProductPlan.get(i).get("model_name");
				Integer iQuantity = (Integer) todayProductPlan.get(i).get("quantity");
				String sCycleTime = PathConsts.POSITION_SETTINGS.getProperty("overline.0.002." + modelName);
				if (sCycleTime != null) {
					bdCycleTime = new BigDecimal(sCycleTime);
				}
				for (int ii = 0; ii < iQuantity; ii++) {
					iB++;
					bdLocate = addBlock(iB, bdLocate, modelName, bdCycleTime, bdFactor, sbCss, sbDiv);
					if (i == posBx3 - 1) {
						// 与BX3一起生产的型号
						if (quBx3 > 0) {
							iB++;
							bdLocate = addBlock(iB, bdLocate, "BX3", bdBx3CycleTime, bdFactor, sbCss, sbDiv);
							quBx3--;
						}
					}
				}
			}

			// 多余的BX3
			if (quBx3 > 0) {
				for (int ii = 0; ii < quBx3; ii++) {
					iB++;
					bdLocate = addBlock(iB, bdLocate, "BX3", bdBx3CycleTime, bdFactor, sbCss, sbDiv);
				}
			}

			int bottom = bdLocate.intValue();
			int fWmodwr = bdFactor.multiply(new BigDecimal(WORK_MINUTE_OF_DAY_WITH_RESTS)).intValue();
			int height = fWmodwr - bottom;
			BigDecimal bdAbli = new BigDecimal(height).divide(bdCycleTime, 0, BigDecimal.ROUND_HALF_UP);
			int iAbli = bdAbli.intValue();
			while (height > iAbli) {
				// 按最后型号标准时间补到下班
				sbCss.append("#standard_column > div > div:nth-child(" + (iB+1) +") {bottom: " + bottom + "px; height: " + iAbli + "px;}");
				sbDiv.append("<div></div>");
				iB++;
				bottom += iAbli;
				height = fWmodwr - bottom;
			}

		} else {
			Object oAbli = PathConsts.SCHEDULE_SETTINGS.get("daily.schedule." + lineName+ "工程");
			String sAbli = "1";
			if (oAbli != null) {
				sAbli = oAbli.toString();
			}

			BigDecimal bdAbli = new BigDecimal(sAbli);

			int iAbli = bdAbli.intValue();
			BigDecimal bdCycleTime = WORK_MINUTE_OF_DAY.divide(bdAbli, 2, BigDecimal.ROUND_HALF_UP);

			BigDecimal bdLocate = new BigDecimal(5);

			for (int i = 0; i < iAbli - 1; i++) {
				int bottom = bdLocate.intValue();
				bdLocate = bdLocate.add(bdCycleTime);
				int top = bdLocate.intValue();
				if (bottom <= TEN_O_CLOCK && top >= TEN_O_CLOCK) bdLocate = bdLocate.add(new BigDecimal(10));
				if (bottom <= TWELVE_O_CLOCK && top >= TWELVE_O_CLOCK) bdLocate = bdLocate.add(new BigDecimal(60));
				if (bottom <= FIFTEEN_O_CLOCK && top >= FIFTEEN_O_CLOCK) bdLocate = bdLocate.add(new BigDecimal(10));
				top = bdLocate.intValue();
				int height = top - bottom;

				sbCss.append("#standard_column > div > div:nth-child(" + (i+1) +") {bottom: " + bottom + "px; height: " + height + "px;}");
				sbDiv.append("<div><div class=\"count_no\">" + (i+1) + "</div></div>");
			}
			int bottom = bdLocate.intValue();
			int height = WORK_MINUTE_OF_DAY_WITH_RESTS - bottom;
			sbCss.append("#standard_column > div > div:nth-child(" + iAbli +") {bottom: " + bottom + "px; height: " + height + "px;}");
			sbDiv.append("<div><div class=\"count_no\">" + iAbli + "</div></div>");
		}

		Map<String, String> retMap = new HashMap<String, String>();
		retMap.put("css", sbCss.toString());

		retMap.put("divHtml", sbDiv.toString());

		return retMap;
	}

	private BigDecimal addBlock(Integer iBlockIndex, BigDecimal bdLocate, String modelName,
			BigDecimal bdCycleTime, BigDecimal bdFactor, StringBuffer sbCss, StringBuffer sbDiv) {

		int bottom = bdLocate.intValue();
		bdLocate = bdLocate.add(bdCycleTime.multiply(bdFactor));
		int top = bdLocate.intValue();
		System.out.println(bottom + ">>" + top);
		if (bottom <= TEN_O_CLOCK * bdFactor.doubleValue() && top >= TEN_O_CLOCK * bdFactor.doubleValue()) 
			bdLocate = bdLocate.add(new BigDecimal(10).multiply(bdFactor));
		if (bottom <= ELEVEN_O_CLOCK_AND_THIRTY * bdFactor.doubleValue() && top >= ELEVEN_O_CLOCK_AND_THIRTY * bdFactor.doubleValue()) 
			bdLocate = bdLocate.add(new BigDecimal(60).multiply(bdFactor));
		if (bottom <= FIFTEEN_O_CLOCK * bdFactor.doubleValue() && top >= FIFTEEN_O_CLOCK * bdFactor.doubleValue()) 
			bdLocate = bdLocate.add(new BigDecimal(10).multiply(bdFactor));
		top = bdLocate.intValue();
		int height = top - bottom;

		sbCss.append("#standard_column > div > div:nth-child(" + iBlockIndex +") {bottom: " + bottom + "px; height: " + height + "px;}");
		sbDiv.append("<div model_name=\"" + modelName + "\"><div class=\"count_no\">" + iBlockIndex + "</div></div>");

		return bdLocate;
	}

	public Object getOperatorFeatures(String [] line_ids, SqlSession conn) {
		LineTimespaceMapper mapper = conn.getMapper(LineTimespaceMapper.class);

		List<Map<String, Object>> operatorFeatures = mapper.getOperatorFeatures(line_ids);
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();

		String currentJobNo = null;
		long pos = 480;

		for (Map<String, Object> feature : operatorFeatures) {
			Map<String, String> retPf = new HashMap<String, String>();
			String materialId = "" + feature.get("material_id");
			String jobNo = "" + feature.get("job_no");

			retPf.put("material_id", materialId);
			retPf.put("job_no", jobNo);
			retPf.put("operator_name", "" + feature.get("operator_name"));
			retPf.put("process_code", "" + feature.get("process_code"));
			retPf.put("sorc_no", "" + feature.get("sorc_no"));
			retPf.put("model_name", "" + feature.get("model_name"));
			retPf.put("d_type", "" + feature.get("d_type"));

			Integer workCountFlg = (Integer) feature.get("WORK_COUNT_FLG");
			retPf.put("work_count_flg", "" + workCountFlg);

			if (!jobNo.equals(currentJobNo)) {
				// 建立
				currentJobNo = jobNo;
				pos = 480;
			}

			// 是
			Long actionTime = (Long) feature.get("action_time");
			Long finishTime = (Long) feature.get("finish_time");
			Long spareMinutes = (finishTime - actionTime);
			if (spareMinutes == 0) spareMinutes = 1l;

			if (actionTime < 475) actionTime = 475l;
			if ((actionTime - pos) > 1 && workCountFlg == 1) {
				retPf.put("unknownFrom", "" + (pos - 475));
				retPf.put("unknownTime", "" + (actionTime - pos));
			}
			pos = finishTime;

			retPf.put("action_time",  Long.toString(actionTime - 480 + 5));
			retPf.put("finish_time",  Long.toString(finishTime - 480 + 5));
			retPf.put("spare_minutes",  Long.toString(spareMinutes));

			ret.add(retPf);
		}

		return ret;
	}

}
