package com.osh.rvs.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.mapper.QualityAssuranceMapper;

import framework.huiqing.common.util.copy.DateUtil;

public class FinalCheckService {

    private static final SimpleDateFormat objSimpleDateFormat = new SimpleDateFormat("M");
	@SuppressWarnings("unused")
	private static final String TARGET_RATE_146P = "99.8%";
	private static final String TARGET_RATE_149P = "99.6%";
	private static final Integer ID_613 = 65;
    /**
	 * 返回品保展示数据
	 * 
	 * @param selectedMonth 月底
	 * @return
	 */
	public Map<String, Object> getInspectResultData(Calendar selectedMonth, SqlSession conn) {
		Map<String, Object> ret = new HashMap<String, Object>();
        //Ｘ轴月份 和当前月份周
//		List<String> axisTexts = new ArrayList<String>();
		List<String> years = new ArrayList<String>();
		List<String> months = new ArrayList<String>();
		
		//取得本期内月
		int nowAxis = RvsUtils.getMonthAxisInHalfBussinessYear(selectedMonth.getTime(), true, years, months);

		// 本月文字
		String sSelectedMonth = objSimpleDateFormat.format(selectedMonth.getTime());
		
		QualityAssuranceMapper qaMapper = conn.getMapper(QualityAssuranceMapper.class);

		//放今年的数据到一个List
		List<HashMap<String, Object>> tLines = new ArrayList<HashMap<String, Object>>();
		
		//放去年的数据到一个List
		List<HashMap<String, Object>> lastYearInspectLines = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < months.size();) {
			String axisText = months.get(i);
			if (!axisText.equals(sSelectedMonth)) {
				Date start_Date = new Date();
				Date end_Date = new Date();
				Calendar adjustCal = Calendar.getInstance();
				adjustCal.set(Calendar.YEAR, Integer.parseInt(years.get(i)));
				adjustCal.set(Calendar.MONTH, Integer.parseInt(months.get(i)) - 1);
				adjustCal.set(Calendar.DATE, 1);
				adjustCal.set(Calendar.HOUR_OF_DAY, 0);
				adjustCal.set(Calendar.MINUTE, 0);
				adjustCal.set(Calendar.SECOND, 0);
				adjustCal.set(Calendar.MILLISECOND, 0);

				start_Date.setTime(adjustCal.getTimeInMillis());

				adjustCal.add(Calendar.MONTH, 1);
				end_Date.setTime(adjustCal.getTimeInMillis());

				//取得品保完成件数和不合格的件数
				HashMap<String, Object> tLine = qaMapper.getWorkresult(start_Date, end_Date);
				tLine.put("axisText", axisText + "月");
				//目标99.8%
				tLine.put("targetRate", TARGET_RATE_149P);
				tLines.add(tLine);

				if (i <= nowAxis) {
					adjustCal.add(Calendar.YEAR, -1);
					end_Date.setTime(adjustCal.getTimeInMillis());

					adjustCal.add(Calendar.MONTH, -1);
					start_Date.setTime(adjustCal.getTimeInMillis());

					//上期品保完成件数和不合格的件数
					HashMap<String, Object> lastYearInspectLine = qaMapper.getWorkresult(start_Date, end_Date);
					lastYearInspectLines.add(lastYearInspectLine);
				}

				i++;
			} else {

				List<Date> start_Dates = new ArrayList<Date>();
				List<Date> end_Dates = new ArrayList<Date>();
				RvsUtils.getWeekAxisInMonth(selectedMonth.getTime(), true, start_Dates, end_Dates);

				for (int iD=0; iD < start_Dates.size(); iD++) {
					Date start_Date = start_Dates.get(iD);
					Date end_Date = end_Dates.get(iD);
					HashMap<String, Object> tLine = qaMapper.getWorkresult(start_Date, end_Date);

					Calendar adjustCal = Calendar.getInstance();
					adjustCal.setTimeInMillis(end_Date.getTime());
					adjustCal.add(Calendar.DATE, 1);
					end_Date.setTime(adjustCal.getTimeInMillis());

					tLine.put("axisText", DateUtil.toString(start_Date, "MM-dd") + "～<br/>" +  DateUtil.toString(end_Date, "MM-dd"));
					tLine.put("targetRate", TARGET_RATE_149P);
					tLines.add(tLine);

					HashMap<String,Object> map=new HashMap<String, Object>();
					map.put("process_count", null);
					map.put("fail_count", null);
					
					lastYearInspectLines.add(map);
				}

				i++;
			}
		}
		ret.put("tLines", tLines);
		ret.put("lastYearInspectLines", lastYearInspectLines);
	
		return ret;
	}
	/**
	 * 取得当日品保的通过和不合格的数量
	 * @param listResponse
	 * @param conn
	 */
	public void getCounts(Map<String,Object> listResponse,SqlSession conn){
		QualityAssuranceMapper dao = conn.getMapper(QualityAssuranceMapper.class);
		int passCount = 0;
		int unqualifiedCount = 0;
		int waitingCount = 0;
		int waitingConfirmCount = 0;
		List<Map<String, Object>> passCounts = dao.getCurrentPassCount();
		List<Map<String, Object>> unqualifiedCounts = dao.getCurrentUnqualifiedCount();
		List<Map<String, Object>> waitingCounts = dao.getCurrentWaitingCount(true);
		List<Map<String, Object>> waitingConfirmCounts = dao.getCurrentWaitingCount(false);

		for(Map<String, Object> cntByProcess : passCounts) {
			int cnt = getAsInteger(cntByProcess, "cnt");
			if (ID_613.equals(getAsInteger(cntByProcess, "position_id"))) {
				listResponse.put("currentPassCountP", cnt);
			}
			passCount += cnt;
		}

		for(Map<String, Object> cntByProcess : unqualifiedCounts) {
			int cnt = getAsInteger(cntByProcess, "cnt");
			if (ID_613.equals(getAsInteger(cntByProcess, "position_id"))) {
				listResponse.put("currentUnqualifiedCountP", cnt);
			}
			unqualifiedCount += cnt;
		}

		for(Map<String, Object> cntByProcess : waitingCounts) {
			int cnt = getAsInteger(cntByProcess, "cnt");
			if (ID_613.equals(getAsInteger(cntByProcess, "position_id"))) {
				listResponse.put("currentWaitingCountP", cnt);
			}
			waitingCount += cnt;
		}

		for(Map<String, Object> cntByProcess : waitingConfirmCounts) {
			int cnt = getAsInteger(cntByProcess, "cnt");
			if (ID_613.equals(getAsInteger(cntByProcess, "position_id"))) {
				listResponse.put("currentWaitingConfirmCountP", cnt);
			}
			waitingConfirmCount += cnt;
		}

		listResponse.put("currentPassCount", passCount);
		listResponse.put("currentUnqualifiedCount", unqualifiedCount);
		listResponse.put("currentWaitingCount", waitingCount);
		listResponse.put("currentWaitingConfirmCount", waitingConfirmCount);
	}
	private int getAsInteger(Map<String, Object> cntByProcess, String key) {
		Object objCnt = cntByProcess.get(key);
		if (objCnt instanceof Long) {
			return ((Long) objCnt).intValue();
		} else if (objCnt instanceof Integer) {
			return ((Integer) objCnt).intValue();
		}
		return 0;
	}				
}

