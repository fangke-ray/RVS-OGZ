package com.osh.rvs.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.bean.LineLeaderEntity;
import com.osh.rvs.common.PathConsts;
import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.mapper.AllPositionsMapper;
import com.osh.rvs.mapper.LineLeaderMapper;

public class LineLeaderService {

//	/**
//	 * 取得当前课室+工程下处理中的全部维修对象信息
//	 * @param section_id
//	 * @param line_id
//	 * @param conn
//	 * @return
//	 */
//	public List<LineLeaderForm> getPerformanceList(String section_id, String line_id, String position_id, SqlSession conn) {
//		List<LineLeaderForm> ret = new ArrayList<LineLeaderForm>();
//
//		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
//		if ("".equals(position_id)) position_id = null;
//		List<LineLeaderEntity> listEntities = dao.getWorkingMaterials(section_id, line_id, position_id);
//
//		CopyOptions cos = new CopyOptions();
//		cos.excludeEmptyString();
//		cos.excludeNull();
//
//		AlarmMesssageMapper amDao = conn.getMapper(AlarmMesssageMapper.class);
//		for (LineLeaderEntity entity : listEntities) {
//			LineLeaderForm retForm = new LineLeaderForm();
//			BeanUtil.copyToForm(entity, retForm, cos);
//			if (entity.getOperate_result() == 3) {
//				String amLevel = amDao.getBreakLevelByMaterialId(entity.getMaterial_id(), entity.getPosition_id());
//				retForm.setSymbol(CodeListUtils.getValue("alarm_symbol", amLevel));
//			}
//			ret.add(retForm);
//		}
//		
//		return ret;
//	}

	/**
	 * 工位仕挂一览For图表
	 * @param section_id
	 * @param line_id
	 * @param conn
	 * @param listResponse
	 */
	public List<Map<String, String>> getWorkingOfPositions(String section_id, String line_id, SqlSession conn) { // NO_UCD (use private)
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		return dao.getWorkingOfPositions(section_id, line_id);
	}
	public List<Map<String, String>> getWorkingOfPositionsS1passed(String section_id, String line_id, SqlSession conn) { // NO_UCD (use private)
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		return dao.getWorkingOfPositionsS1passed(section_id, line_id);
	}
	public void getChartContent(String section_id, String line_id, String s1pass, SqlSession conn, Map<String, Object> responseMap) {
		if (s1pass == null) {
			getChartContent(section_id, line_id, conn, responseMap);
		} else {
			getChartContent(section_id, line_id, responseMap, null, conn,true,false);
		}
	}
	public void getChartContent(String section_id, String line_id, SqlSession conn, Map<String, Object> responseMap) {
		getChartContent(section_id, line_id, responseMap, null, conn);
	}
	public void getChartContent(String section_id, String line_id, Map<String, Object> responseMap, String isPeriod, SqlSession conn) {
		getChartContent(section_id, line_id, responseMap, isPeriod, conn,true);
	}
	public void getChartContent(String section_id, String line_id, Map<String, Object> responseMap, String isPeriod, SqlSession conn, boolean delLast) {
		getChartContent(section_id, line_id, responseMap, isPeriod, conn,false,delLast);
	}
	public void getChartContent(String section_id, String line_id, Map<String, Object> responseMap, String isPeriod, String s1pass, SqlSession conn) {
		getChartContent(section_id, line_id, responseMap, isPeriod, conn, s1pass != null, true);
	}
	public void getChartContent(String section_id, String line_id, Map<String, Object> responseMap, String isPeriod, SqlSession conn, boolean s1pass, boolean delLast) {
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		List<Map<String, String>> workingOfPositions = getWorkingOfPositions(section_id, line_id, conn);
		if (s1pass) {
			workingOfPositions = getWorkingOfPositionsS1passed(section_id, line_id, conn);
		} else {
			workingOfPositions = getWorkingOfPositions(section_id, line_id, conn);
		}

		// 数据整合
		List<Map<String, String>> newWorkingOfPositions = new ArrayList<Map<String, String>>();

		for (int i=0; i < workingOfPositions.size(); i+=2){
			Map<String, String> workingOfPositionH = workingOfPositions.get(i);
			Map<String, String> workingOfPositionL = workingOfPositions.get(i+1);
			String light_division_flg = workingOfPositionH.get("LIGHT_DIVISION_FLG");
			String process_code = workingOfPositionH.get("PROCESS_CODE");

			boolean depar = false;
			if ("1".equals(light_division_flg)) { // 分线
				depar = true;
//				if ("0".equals(workingOfPositionL.get("material_count"))
//						&& "0".equals(workingOfPositionL.get("light_fix_count"))) { // B线无仕挂
//					depar = false;
//				}
			}

			if (depar) { // 分线
				workingOfPositionH.put("PROCESS_CODE", process_code + "A");
				workingOfPositionL.put("PROCESS_CODE", process_code + "B");
				newWorkingOfPositions.add(workingOfPositionH);
				newWorkingOfPositions.add(workingOfPositionL);
			} else { // 不分
				Float fCount = 0f;
				Float light_fix_count = 0f;
				try {
					fCount = Float.parseFloat(workingOfPositionH.get("material_count"))
							+ Float.parseFloat(workingOfPositionL.get("material_count"));
					light_fix_count = Float.parseFloat(workingOfPositionH.get("light_fix_count"))
							+ Float.parseFloat(workingOfPositionL.get("light_fix_count"));
				} catch (NumberFormatException e) {
				}
				workingOfPositionH.put("material_count", String.valueOf(fCount));
				workingOfPositionH.put("light_fix_count", String.valueOf(light_fix_count));
				newWorkingOfPositions.add(workingOfPositionH);
			}
		}

		List<String> positions = new ArrayList<String>();
		List<Integer> overlines = new ArrayList<Integer>();
		List<Object> counts = new ArrayList<Object>();
		List<Object> light_fix_counts = new ArrayList<Object>();

//		// 工位合并
//		Map<String, Float[]> processesCnt = new HashMap<String, Float[]>();
//		Map<String, Set<String>> countIntos = new HashMap<String, Set<String>>();
//
//		for (Map<String, String> workingOfPosition : newWorkingOfPositions) {
//			String processCode = "" + workingOfPosition.get("PROCESS_CODE");
//			if (processCode.endsWith("A") || processCode.endsWith("B")) {
//				processCode = processCode.substring(0, processCode.length() - 1);
//			}
//			String countIntoProcessCode = PathConsts.POSITION_SETTINGS.getProperty("count.into." + processCode);
//			if (countIntoProcessCode != null) {
//				workingOfPosition.put("PROCESS_CODE", null);
//				if (countIntos.containsKey(countIntoProcessCode)) {
//					Set<String> countInto = countIntos.get(countIntoProcessCode);
//					countInto.add(processCode);
//				} else {
//					Set<String> countInto = new HashSet<String>();
//					countInto.add(countIntoProcessCode);
//					countInto.add(processCode);
//					countIntos.put(countIntoProcessCode, countInto);
//				}
//				processCode = countIntoProcessCode;
//			}
//			if (!processesCnt.containsKey(processCode)) {
//				Float newInt[] = {0.0f,0.0f};
//				processesCnt.put(processCode, newInt);
//			}
//			// 大修理数据
//			Float fCount = 0f;
//			try {
//				fCount = Float.parseFloat(workingOfPosition.get("material_count"));
////				if ("400".equals(processesCnt)) {
////					fCount /= 10;
////				}
//			} catch (NumberFormatException e) {
//			}
//
//			// 小修理数据
//			Float light_fix_count = 0f;
//			try {
//				light_fix_count = Float.parseFloat(workingOfPosition.get("light_fix_count"));
////				if ("400".equals(processesCnt)) {
////					light_fix_count /= 10;
////				}
//			} catch (NumberFormatException e) {
//			}
//			Float[] cntOfProcess = processesCnt.get(processCode);
//			cntOfProcess[0] = cntOfProcess[0] + fCount;
//			cntOfProcess[1] = cntOfProcess[1] + light_fix_count;
//		}

		for (Map<String, String> workingOfPosition : newWorkingOfPositions){			
			String process_code = workingOfPosition.get("PROCESS_CODE");
			String processCodeKey = process_code;
			if (process_code == null) continue;
			if (processCodeKey.endsWith("A") || processCodeKey.endsWith("B")) {
				process_code = process_code.substring(0, process_code.length() - 1);
			}
//			if ("400".equals(process_code)) {
//				positions.add("" + workingOfPosition.get("PROCESS_CODE") + " " + workingOfPosition.get("NAME") + "\n(x 10)");
//			} else {
				positions.add("" + processCodeKey + "\r\n<br>" + workingOfPosition.get("NAME") + "");
//			}
			
			String sWaitingflow = RvsUtils.getWaitingflow(section_id, null, process_code);

			Integer iWaitingflow = null;
			if (sWaitingflow != null) {
				try {
					iWaitingflow = Integer.parseInt(sWaitingflow);
//					if (workingOfPosition.get("PROCESS_CODE").endsWith("B")) {
//						iWaitingflow = 6;
//					}
				} catch (NumberFormatException e) {
				}
			}
			overlines.add(iWaitingflow);
			overlines.add(iWaitingflow);
			overlines.add(iWaitingflow);
			overlines.add(null);

			// 大修理数据
			Float fCount = 0f;
			try {
				fCount = Float.parseFloat(workingOfPosition.get("material_count"));
//				if ("400".equals(process_code)) {
//					fCount /= 10;
//				}
			} catch (NumberFormatException e) {
			}

			// 小修理数据
			Float light_fix_count = 0f;
			try {
				light_fix_count = Float.parseFloat(workingOfPosition.get("light_fix_count"));
//				if ("400".equals(process_code)) {
//					light_fix_count /= 10;
//				}
			} catch (NumberFormatException e) {
			}
//
//			// 大修理数据
//			Float[] cntOfProcess = processesCnt.get(process_code);
//			Float fCount = cntOfProcess[0];
//			if (countIntos.containsKey(process_code)) {
//				// 取得合并后的仕挂数
//				fCount = dao.getComninedCount(section_id, countIntos.get(process_code)) + 0.0f;
//			}
//
//			// 小修理数据
//			Float light_fix_count = cntOfProcess[1];
//			// 合计
			Float total = fCount + light_fix_count;

			Map<String, Object> series = new HashMap<String, Object>();
			Map<String, Object> color = new HashMap<String, Object>();
			Map<String, Object> linearGradient = new HashMap<String, Object>();
			linearGradient.put("x1", "0");
			linearGradient.put("x2", "0");
			linearGradient.put("y1", "1");
			linearGradient.put("y2", "0");
			color.put("linearGradient", linearGradient);
			List<ArrayList<Object>> stops = new ArrayList<ArrayList<Object>>();
			ArrayList<Object> stop = new ArrayList<Object>();
			if (iWaitingflow != null && iWaitingflow != 0 && fCount > iWaitingflow && light_fix_count == 0){
				// 超过等待去上限的{color : '#f04e08', y : 47}
				stop = new ArrayList<Object>();
				stop.add("0.7");
				stop.add("#92D050");
				stops.add(stop);
				stop = new ArrayList<Object>();
				stop.add("0.8");
				stop.add("#FFC000");
				stops.add(stop);
				color.put("stops", stops);
				series.put("color", color);
				series.put("y", fCount);
				counts.add(series);
			} else {
				counts.add(fCount);
			}

			if (iWaitingflow != null && iWaitingflow != 0
					&& (light_fix_count > iWaitingflow || (total > iWaitingflow && light_fix_count > 0))) {
				stop = new ArrayList<Object>();
				stop.add("0.7");
				stop.add("#cc76cc");
				stops.add(stop);
				stop = new ArrayList<Object>();
				stop.add("0.8");
				stop.add("#FFC000");
				stops.add(stop);
				color.put("stops", stops);
				series.put("color", color);
				series.put("y", light_fix_count);
				light_fix_counts.add(series);
			} else {
				if (light_fix_count == 0) {
					light_fix_counts.add(null);
				} else {
					light_fix_counts.add(light_fix_count);
				}
			}
		}

		if(overlines.size() > 1 && delLast){
			overlines.remove(overlines.size() - 1);
		}

		// 分解工程取得分解库位信息
		if ("00000000012".equals(line_id)) {
			AllPositionsMapper sdMapper = conn.getMapper(AllPositionsMapper.class);
			int decomStorageCount = sdMapper.getDecomStorageCount();

			positions.add(1, "内镜分解库位");

			counts.add(1, decomStorageCount);

			overlines.add(4, null);
			overlines.add(5, null);
			overlines.add(6, null);
			overlines.add(7, null);
		}

		responseMap.put("categories", positions);
		responseMap.put("overlines", overlines);
		responseMap.put("counts", counts);
		responseMap.put("light_fix_counts", light_fix_counts);

//		series : [ {
//			name : '计划台数',
//			data : [null,2,2,2,2,null,null,2,2,2,2,null,null,5,5,5,5,null,null,6,6,6,6,null,null]
//		},{
//			name : '产出台数',
//			data : [null,null,1,1,1,1,null,null,3,3,3,3,null,null,4,4,4,4,null,null,0,0,0,0,null]
//		} ]

		if (isPeriod != null) {
			if ("manufactor".equals(isPeriod)) {
				List<Integer> plans = new ArrayList<Integer>();

				getManufactorPlanToPeriod(plans, dao);

				List<Integer> outs = new ArrayList<Integer>();

				outs.add(null);
				Integer out1 = dao.getOutPeriod(""+1, section_id, line_id, null);
				outs.add(null);outs.add(out1);outs.add(out1);outs.add(out1);outs.add(out1);outs.add(null);
				Integer out2 = dao.getOutPeriod(""+2.6, section_id, line_id, null);
				outs.add(null);outs.add(out2);outs.add(out2);outs.add(out2);outs.add(out2);outs.add(null);
				Integer out3 = dao.getOutPeriod(""+3.6, section_id, line_id, null);
				outs.add(null);outs.add(out3);outs.add(out3);outs.add(out3);outs.add(out3);outs.add(null);
				Integer out4 = dao.getOutPeriod(""+4, section_id, line_id, null);
				outs.add(null);outs.add(out4);outs.add(out4);outs.add(out4);outs.add(out4);outs.add(null);

				responseMap.put("plans", plans);
				responseMap.put("outs", outs);
				Calendar now = Calendar.getInstance();
				int hour = now.get(Calendar.HOUR_OF_DAY);
				if (hour < 10) {
					responseMap.put("now_period", 1);
				} else if (hour < 12) { // 11:30 后大休, 判断12点也可以
					responseMap.put("now_period", 2);
				} else if (hour < 15) {
					responseMap.put("now_period", 3);
				} else {
					responseMap.put("now_period", 4);
				}

			} else {
				List<Integer> plans = new ArrayList<Integer>();
				List<Integer> outs = new ArrayList<Integer>();
				List<Integer> outMinors = new ArrayList<Integer>();

				Integer plan1 = 0,plan2 = 0,plan3 = 0,plan4 = 0;
				try {
					if (line_id != null) {
						String lineName = "";

						switch(line_id) {
						case "00000000014": lineName = "总组工程"; break;
						case "00000000013": lineName = "NS 工程"; break;
						case "00000000012": lineName = "分解工程"; break;
						case "00000000201": lineName = "290拉线"; break;
						case "00000000202": lineName = "260拉线"; break;
						case "00000000203": lineName = "细镜拉线"; break;
						}
						plan1 = Integer.parseInt("" + PathConsts.SCHEDULE_SETTINGS.get("daily.schedule.1." + lineName));
						plan2 = Integer.parseInt("" + PathConsts.SCHEDULE_SETTINGS.get("daily.schedule.2." + lineName));
						plan3 = Integer.parseInt("" + PathConsts.SCHEDULE_SETTINGS.get("daily.schedule.3." + lineName));
						plan4 = Integer.parseInt("" + PathConsts.SCHEDULE_SETTINGS.get("daily.schedule.4." + lineName));
					}
				} catch (NumberFormatException e) {
				}
				plans.add(null);plans.add(plan1);plans.add(plan1);plans.add(plan1);plans.add(plan1);plans.add(null);
				plans.add(null);plans.add(plan2);plans.add(plan2);plans.add(plan2);plans.add(plan2);plans.add(null);
				plans.add(null);plans.add(plan3);plans.add(plan3);plans.add(plan3);plans.add(plan3);plans.add(null);
				plans.add(null);plans.add(plan4);plans.add(plan4);plans.add(plan4);plans.add(plan4);plans.add(null);
				plans.add(null);

				String rank = null;
				if (line_id != null) {
					switch(line_id) {
					case "00000000201": 
					case "00000000202": 
					case "00000000203": rank = "1"; break;
					}
				}

				{
					outs.add(null);
					Integer out1 = getOutPeriod(1, section_id, line_id, rank, dao);
					outs.add(null);outs.add(out1);outs.add(out1);outs.add(out1);outs.add(out1);outs.add(null);
					Integer out2 = getOutPeriod(2, section_id, line_id, rank, dao);
					outs.add(null);outs.add(out2);outs.add(out2);outs.add(out2);outs.add(out2);outs.add(null);
					Integer out3 = getOutPeriod(3, section_id, line_id, rank, dao);
					outs.add(null);outs.add(out3);outs.add(out3);outs.add(out3);outs.add(out3);outs.add(null);
					Integer out4 = getOutPeriod(4, section_id, line_id, rank, dao);
					outs.add(null);outs.add(out4);outs.add(out4);outs.add(out4);outs.add(out4);outs.add(null);
				}

				responseMap.put("plans", plans);
				responseMap.put("outs", outs);

				if (rank != null) {
					rank = "9";
					outMinors.add(null);
					Integer out1 = getOutPeriod(1, section_id, line_id, rank, dao);
					outMinors.add(null);outMinors.add(out1);outMinors.add(out1);outMinors.add(out1);outMinors.add(out1);outMinors.add(null);
					Integer out2 = getOutPeriod(2, section_id, line_id, rank, dao);
					outMinors.add(null);outMinors.add(out2);outMinors.add(out2);outMinors.add(out2);outMinors.add(out2);outMinors.add(null);
					Integer out3 = getOutPeriod(3, section_id, line_id, rank, dao);
					outMinors.add(null);outMinors.add(out3);outMinors.add(out3);outMinors.add(out3);outMinors.add(out3);outMinors.add(null);
					Integer out4 = getOutPeriod(4, section_id, line_id, rank, dao);
					outMinors.add(null);outMinors.add(out4);outMinors.add(out4);outMinors.add(out4);outMinors.add(out4);outMinors.add(null);
					responseMap.put("outMinors", outMinors);
				}

				Calendar now = Calendar.getInstance();
				int hour = now.get(Calendar.HOUR_OF_DAY);
				if (hour < 10) {
					responseMap.put("now_period", 1);
				} else if (hour < 12) {
					responseMap.put("now_period", 2);
				} else if (hour < 15) {
					responseMap.put("now_period", 3);
				} else {
					responseMap.put("now_period", 4);
				}
			}
		}
		return;
	}
	private Integer getOutPeriod(int i, String section_id, String line_id, String rank, LineLeaderMapper dao) {
		int ret = dao.getOutPeriod("" + i, section_id, line_id, rank);
		if ("00000000203".equals(line_id)) {
			ret += dao.getOutPeriod("" + i, section_id, "00000000061", rank);
		}
		return ret;
	}

	public void getSimpleContent(String section_id, String line_id, Map<String, Object> responseMap, SqlSession conn) {
		List<Map<String, String>> workingOfPositions = getWorkingOfPositions(section_id, line_id, conn);
		int heap181 = 0;
		int heap811 = 0;
		for (Map<String, String> workingOfPosition : workingOfPositions) {
			String processCode = workingOfPosition.get("PROCESS_CODE");
			if ("181".equals(processCode)) {
				heap181 += Integer.parseInt(workingOfPosition.get("material_count"));
			} else if ("811".equals(processCode)) {
				heap811 += Integer.parseInt(workingOfPosition.get("material_count"));
			}
		}
		responseMap.put("waiting_quote", heap181);
		responseMap.put("waiting_repair", heap811);
	}

	public void getSituation(String section_id, String line_id, Map<String, Object> responseMap, SqlSession conn) {
		getSituation(section_id, line_id, responseMap, null, false, conn);
	}
	public void getSituation(String section_id, String line_id, Map<String, Object> responseMap, String isPeriod, boolean s1pass, SqlSession conn) {
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);

		if (line_id == null) {
			line_id = "00000000202";
		}

		String rank = null;
		String rankMinor = "9";
		if (line_id != null) {
			switch(line_id) {
			case "00000000201": 
			case "00000000202": 
			case "00000000203": rank = "1"; break;
			}
		}

		// 工程仕挂总数
		if ("00000000203".equals(line_id) && !s1pass) {
			String sikakeText = "细镜：" + dao.getWorkingMaterialCounts(section_id, line_id, rank, null, null);
//			sikakeText += "｜ 纤维镜分解：" + dao.getWorkingMaterialCounts(section_id, "00000000060", null);
			sikakeText += "｜ 纤维镜总组：" + dao.getWorkingMaterialCounts(section_id, "00000000061", rank, null, null);
			responseMap.put("sikake", sikakeText);

			if (rank != null) {
				String sikakeMinorText = "细镜：" + dao.getWorkingMaterialCounts(section_id, line_id, rankMinor, null, null);
				sikakeMinorText += "｜ 纤维镜总组：" + dao.getWorkingMaterialCounts(section_id, "00000000061", rankMinor, null, null);
				responseMap.put("sikakeMinor", sikakeMinorText);
			}
		} else {
			responseMap.put("sikake", dao.getWorkingMaterialCounts(section_id, line_id, rank, null
					, (s1pass ?"s1_pass" : null)));
			if (rank != null) {
				responseMap.put("sikakeMinor", dao.getWorkingMaterialCounts(section_id, line_id, rankMinor, null
						, (s1pass ?"s1_pass" : null)));
			}
		}

		if ("00000000014".equals(line_id)) {
			// 取得今日计划件数
			responseMap.put("plan", PathConsts.SCHEDULE_SETTINGS.get("daily.schedule.总组工程"));
			// 取得今日计划内完成件数
			responseMap.put("plan_complete", dao.getTodayCompleteMaterialCounts(section_id, line_id, rank, ""));
		} else {
			if ("00000000012".equals(line_id)) {
				// 总组以外暂且取白板数字
				responseMap.put("plan", PathConsts.SCHEDULE_SETTINGS.get("daily.schedule.分解工程"));
				responseMap.put("plan_complete", dao.getProduceActualOfLine(section_id,line_id));
			} else if ("00000000013".equals(line_id)) {
				// 总组以外暂且取白板数字
				responseMap.put("plan", PathConsts.SCHEDULE_SETTINGS.get("daily.schedule.NS 工程"));
				responseMap.put("plan_complete", dao.getProduceActualOfNsByBoard(section_id));
				responseMap.put("sikake_in", dao.getWorkingMaterialCounts(section_id, line_id, "NS CELL", rank, null));
			} else {
				String lineName = "";
				switch(line_id) {
				case "00000000070": lineName = "周边维修工程"; break;
				case "00000000201": lineName = "290拉工程"; break;
				case "00000000202": lineName = "260拉工程"; break;
				case "00000000203": lineName = "细镜拉工程"; break;
				}

				String sPlan = "0";
				Object oPlan =PathConsts.SCHEDULE_SETTINGS.get("daily.schedule." + lineName);
				if (oPlan != null) {
					sPlan = oPlan.toString();
				}
				responseMap.put("plan", Integer.parseInt(sPlan));
				if ("00000000203".equals(line_id)) {
					{
					long cnt = dao.getTodayCompleteMaterialCounts(section_id, line_id, rank, "");
					cnt += dao.getTodayCompleteMaterialCounts(section_id, "00000000061", rank, "");
					responseMap.put("plan_complete", cnt);
					}
					if (rank != null) {
						long cnt = dao.getTodayCompleteMaterialCounts(section_id, line_id, rankMinor, "");
						cnt += dao.getTodayCompleteMaterialCounts(section_id, "00000000061", rankMinor, "");
						responseMap.put("plan_minor_complete", cnt);
					}
				} else {
					responseMap.put("plan_complete", 
							dao.getTodayCompleteMaterialCounts(section_id, line_id, rank, ""));
					if (rank != null) {
						responseMap.put("plan_minor_complete", 
								dao.getTodayCompleteMaterialCounts(section_id, line_id, rankMinor, ""));
					}
				}
			}
		}

//		responseMap.put("plan", new Double(Math.random() * 20).intValue()); // DUmmy
//		responseMap.put("plan_complete", new Double(Math.random() * 20).intValue());

		if (isPeriod == null) {
			// 取得当前工程内中断维修对象
			responseMap.put("now_nogood", dao.getBreakingMaterials(section_id, line_id));
			// 取得当前工程内加急维修对象
			responseMap.put("now_expedited", dao.getExpeditingMaterials(section_id, line_id));
			// 取得当前工程内当日纳期维修对象
			responseMap.put("today_plan_outline", dao.getPlanOutlineMaterials(section_id, line_id));
			if ("00000000013".equals(line_id)) {
				responseMap.put("other_line_closer", dao.getOtherLineFinishMaterials(section_id, line_id));
			}
		}
	}

	public void getComAndNsMatch(String section_id, String line_id, SqlSession conn, Map<String, Object> responseMap) {
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		boolean isCom = "00000000012".equals(line_id);

		List<Map<String, String>> matchfor = null;
		if (isCom) {
			matchfor = dao.getComAndNsMatch(section_id);
		} else {
			matchfor = dao.getNsAndComMatch(section_id);
		}
		List<LineLeaderEntity> matches = new ArrayList<LineLeaderEntity>();

		String cp_sorc_no = "";
		LineLeaderEntity line = null;
		for (Map<String, String> matchfo : matchfor) {
			String sorc_no = matchfo.get("sorc_no");
			if (!cp_sorc_no.equals(sorc_no)) {
				cp_sorc_no = sorc_no;
				if (line != null) matches.add(line);
				line = new LineLeaderEntity();
				line.setSorc_no(sorc_no);
				line.setModel_name(matchfo.get("model_name"));
				line.setProcess_code("");
				line.setDirect_flg("1".equals(""+matchfo.get("direct_flg")) ? 1 : 0);
				String level = matchfo.get("level");
				if (level != null && !"".equals(level)) {
					line.setLevel(Integer.parseInt(matchfo.get("level")));
				}
			}
			if (isCom) {
				if (matchfo.get("ns_finish_date") != null) {
					line.setProcess_code(matchfo.get("ns_finish_date")+"完成");
				} else {
					String process_codes = line.getProcess_code();
					String process_code = matchfo.get("ns_process_code");
					if (process_code == null || process_codes.contains(process_code)) continue;
					if (process_codes.length() > 0) process_codes += "　";
					line.setProcess_code(process_codes + process_code);
				}
			} else {
				if (matchfo.get("com_finish_date") != null) {
					line.setProcess_code(matchfo.get("com_finish_date")+"完成");
				} else {
					String process_codes = line.getProcess_code();
					String process_code = matchfo.get("com_process_code");
					if (process_code == null || process_codes.contains(process_code)) continue;
					if (process_codes.length() > 0) process_codes += "　";
					line.setProcess_code(process_codes + process_code);
				}
			}
		}
		if (line != null) matches.add(line);

		responseMap.put("com_ns_matches", matches);
	}

	public Integer getPeriWaitingPart(SqlSession conn) {
		LineLeaderMapper dao = conn.getMapper(LineLeaderMapper.class);
		return dao.getPeriWaitingPart();
	}

	private void getManufactorPlanToPeriod(List<Integer> plans, LineLeaderMapper llMapper) {
		Integer plan1 = 0,plan2 = 0,plan3 = 0,plan4 = 0;

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

		// 根据机型取得标准时间
		BigDecimal bdCycleTime = new BigDecimal(10);

		BigDecimal bdLocate = new BigDecimal(5); // 8:05

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
				bdLocate = bdLocate.add(bdCycleTime);
				if (bdLocate.intValue() > LineTimespaceService.FIFTEEN_O_CLOCK) {
					if (plan4 == 0) bdLocate = bdLocate.add(new BigDecimal(10));
					plan4++;				
				} else if (bdLocate.intValue() > LineTimespaceService.ELEVEN_O_CLOCK_AND_THIRTY) {
					if (plan3 == 0) bdLocate = bdLocate.add(new BigDecimal(60));
					plan3++;
				} else if (bdLocate.intValue() > LineTimespaceService.TEN_O_CLOCK) {
					if (plan2 == 0) bdLocate = bdLocate.add(new BigDecimal(10));
					plan2++;
				} else {
					plan1++;
				}
				if (i == posBx3 - 1) {
					// 与BX3一起生产的型号
					if (quBx3 > 0) {
						bdLocate = bdLocate.add(bdBx3CycleTime);
						if (bdLocate.intValue() > LineTimespaceService.FIFTEEN_O_CLOCK) {
							if (plan4 == 0) bdLocate = bdLocate.add(new BigDecimal(10));
							plan4++;				
						} else if (bdLocate.intValue() > LineTimespaceService.ELEVEN_O_CLOCK_AND_THIRTY) {
							if (plan3 == 0) bdLocate = bdLocate.add(new BigDecimal(60));
							plan3++;
						} else if (bdLocate.intValue() > LineTimespaceService.TEN_O_CLOCK) {
							if (plan2 == 0) bdLocate = bdLocate.add(new BigDecimal(10));
							plan2++;
						} else {
							plan1++;
						}
						quBx3--;
					}
				}
			}
		}

		// 多余的BX3
		if (quBx3 > 0) {
			for (int ii = 0; ii < quBx3; ii++) {
				bdLocate = bdLocate.add(bdBx3CycleTime);
				if (bdLocate.intValue() > LineTimespaceService.FIFTEEN_O_CLOCK) {
					if (plan4 == 0) bdLocate = bdLocate.add(new BigDecimal(10));
					plan4++;				
				} else if (bdLocate.intValue() > LineTimespaceService.ELEVEN_O_CLOCK_AND_THIRTY) {
					if (plan3 == 0) bdLocate = bdLocate.add(new BigDecimal(60));
					plan3++;
				} else if (bdLocate.intValue() > LineTimespaceService.TEN_O_CLOCK) {
					if (plan2 == 0) bdLocate = bdLocate.add(new BigDecimal(10));
					plan2++;
				} else {
					plan1++;
				}
			}
		}

		plans.add(null);plans.add(plan1);plans.add(plan1);plans.add(plan1);plans.add(plan1);plans.add(null);
		plans.add(null);plans.add(plan2);plans.add(plan2);plans.add(plan2);plans.add(plan2);plans.add(null);
		plans.add(null);plans.add(plan3);plans.add(plan3);plans.add(plan3);plans.add(plan3);plans.add(null);
		plans.add(null);plans.add(plan4);plans.add(plan4);plans.add(plan4);plans.add(plan4);plans.add(null);
		plans.add(null);
	}
}
