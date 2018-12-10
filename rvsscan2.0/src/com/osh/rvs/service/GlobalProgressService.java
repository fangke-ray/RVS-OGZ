package com.osh.rvs.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.osh.rvs.common.RvsUtils;
import com.osh.rvs.mapper.GlobalProgressMapper;
import com.osh.rvs.mapper.UserDefineCodesMapper;

import framework.huiqing.common.util.CommonStringUtil;
import framework.huiqing.common.util.copy.DateUtil;


public class GlobalProgressService {

	/**
	 * 全局展示数据查询
	 * @param conn
	 * @return
	 */
	public Map<String, String> getSeries(SqlSession conn) {
		GlobalProgressMapper dao = conn.getMapper(GlobalProgressMapper.class);
		UserDefineCodesMapper userDefineCodesMapper =conn.getMapper(UserDefineCodesMapper.class);
		Map<String, String> ret = new HashMap<String, String>();

		String wipOnlineRepairUp = userDefineCodesMapper.getValue("WIP_ONLINE_REPAIR_UP");//WIP在修上限
		String wipOnlineRepairDown = userDefineCodesMapper.getValue("WIP_ONLINE_REPAIR_DOWN");//WIP在修下限
		ret.put("wipOnlineRepairUp",wipOnlineRepairUp);
		ret.put("wipOnlineRepairDown",wipOnlineRepairDown);

		Map<String,BigDecimal> wipMap = dao.getTodayWipResult();
		Integer wipLocationOther = wipMap.get("other").intValue();//WIP在库 普通内镜
		Integer wipLocationEndoeye = wipMap.get("endoeye").intValue();///WIP在库 ENDOEYE
		Integer wipLocationFibcrsope = wipMap.get("fibcrsope").intValue();///WIP在库 纤维镜

		Map<String,BigDecimal> wipAgreedMap = dao.getTodayWipAgreedResult();
		Integer wipLocationOtherAgreed = wipAgreedMap.get("other").intValue();//WIP在库 普通内镜(同意)
		Integer wipLocationEndoeyeAgreed = wipAgreedMap.get("endoeye").intValue();///WIP在库 ENDOEYE(同意)
		Integer wipLocationFibcrsopeAgreed = wipAgreedMap.get("fibcrsope").intValue();///WIP在库 纤维镜(同意)
		
		Map<String,BigDecimal> wipOnLineRepair = dao.getTodayOnRepairWipResult();
		Integer wipOnLineRepairOther =  wipOnLineRepair.get("other").intValue();//WIP在修 普通内镜
		Integer wipOnLineRepairEndoeye =  wipOnLineRepair.get("endoeye").intValue();///WIP在修 ENDOEYE
		Integer wipOnLineRepairFibcrsope =  wipOnLineRepair.get("fibcrsope").intValue();///WIP在修 纤维镜

		//今日业绩
		//WIP 普通内镜
		StringBuffer serie11 = new StringBuffer();
		serie11.append("[{name:'',y:" + wipLocationOther + ",color:'#83D1E4'},");
		serie11.append("{name:'',y:" + wipLocationFibcrsope + ",color:'#4682B4'},");
		serie11.append("{name:'',y:" + wipLocationEndoeye + ",color:'#E377C2'}]");
		ret.put("serie11", serie11.toString());

		StringBuffer serie101 = new StringBuffer();
		serie101.append("[{name:'同意',y:" + wipLocationOtherAgreed + ",color:'#A8DEEC'},");
		serie101.append("{name:'同意',y:" + wipLocationFibcrsopeAgreed + ",color:'#74A2C7'},");
		serie101.append("{name:'同意',y:" + wipLocationEndoeyeAgreed + ",color:'#EC9FD5'}]");
		ret.put("serie101", serie101.toString());

		//WIP在修 ENDOEYE
		StringBuffer serie13 = new StringBuffer();
		serie13.append("[" + wipOnLineRepairEndoeye + "]");
		ret.put("serie13", serie13.toString());

		//WIP在修 纤维镜
		StringBuffer serie14 = new StringBuffer();
		serie14.append("[" + wipOnLineRepairFibcrsope + "]");
		ret.put("serie14", serie14.toString());

		//WIP在修 普通内镜
		StringBuffer serie15 = new StringBuffer();
		if(wipOnLineRepairOther < Integer.valueOf(wipOnlineRepairDown) || wipOnLineRepairOther > Integer.valueOf(wipOnlineRepairUp)){
			serie15.append("[{color:'#EAC100',y:" + wipOnLineRepairOther + "}]"); //WIP在修 其他
		}else{
			serie15.append("[" + wipOnLineRepairOther + "]");
		}
		ret.put("serie15", serie15.toString());

//		StringBuffer series12 = new StringBuffer();
//		series12.append("[" + dao.getTodayRecieveResult() + ","); 	//到货台数
//		series12.append(dao.getTodaAgreedDateResult() +",");		//修理同意
//		series12.append(dao.getTodayInlineResult() + ","); 			//投线
//		series12.append(dao.getTodayShippingResult() + "]"); 		//出货台数
//		ret.put("serie12", series12.toString());

		//投线修理
		Map<String,BigDecimal> InlineTotalResultMap = dao.getInlineTotalResult();
//		//正常在线
//		Map<String,BigDecimal> InlineReagalResultMap = dao.getInlineReagalResult();
		//等待零件
		Map<String,BigDecimal> InlinePartialWaitingResultMap = dao.getInlinePartialWaitingResult();
		//延误
		Map<String,BigDecimal> InlineOvertimeResultMap = dao.getInlineOvertimeResult();
		//不良
		Map<String,BigDecimal> InlineFaultResultMap = dao.getInlineFaultResult();
		
		//OGZ 线上内镜分布 普通内镜
		int inlineTotal = InlineTotalResultMap.get("other").intValue();
		int inlinePartialWaiting = InlinePartialWaitingResultMap.get("other").intValue();
		StringBuffer series21 = new StringBuffer();
		series21.append("[");   	//投线修理
		series21.append((inlineTotal - inlinePartialWaiting) + ",");        	//零件齐备
		series21.append(inlinePartialWaiting + ",");	//等待零件
		series21.append("{color :'#E48E38',y:" + InlineOvertimeResultMap.get("other").intValue() + "},"); //延误
		series21.append("{color :'#E48E38',y:" + InlineFaultResultMap.get("other").intValue() + "}"); 	//不良
		series21.append("]"); 
		ret.put("serie21", series21.toString());

		//OGZ 线上内镜分布 ENDOEYE
		inlineTotal = InlineTotalResultMap.get("endoeye").intValue();
		inlinePartialWaiting = InlinePartialWaitingResultMap.get("endoeye").intValue();
		StringBuffer series22 = new StringBuffer();
		series22.append("[");   	//投线修理
		series22.append((inlineTotal - inlinePartialWaiting) + ",");        	//零件齐备
		series22.append(inlinePartialWaiting + ",");	//等待零件
		series22.append("{color :'#E48E38',y:" + InlineOvertimeResultMap.get("endoeye").intValue() + "},"); //延误
		series22.append("{color :'#E48E38',y:" + InlineFaultResultMap.get("endoeye").intValue() + "}"); 	//不良
		series22.append("]"); 
		ret.put("serie22", series22.toString());

		//OGZ 线上内镜分布 纤维镜
		inlineTotal = InlineTotalResultMap.get("fibcrsope").intValue();
		inlinePartialWaiting = InlinePartialWaitingResultMap.get("fibcrsope").intValue();
		StringBuffer series23 = new StringBuffer();
		series23.append("[");   	//投线修理
		series23.append((inlineTotal - inlinePartialWaiting) + ",");        	//零件齐备
		series23.append(inlinePartialWaiting + ",");	//等待零件
		series23.append("{color :'#E48E38',y:" + InlineOvertimeResultMap.get("fibcrsope").intValue() + "},"); //延误
		series23.append("{color :'#E48E38',y:" + InlineFaultResultMap.get("fibcrsope").intValue() + "}"); 	//不良
		series23.append("]"); 
		ret.put("serie23", series23.toString());

		Calendar cal = Calendar.getInstance();

		// 取得本期配置
		// 本期名称
		String current_period_name = RvsUtils.getBussinessHalfYearString(cal);

		// 本期出货累计计划数
		Date dPeriodStartDate = RvsUtils.getBussinessHalfStartDate(cal);

		String current_planned_amount = getCurrentPlannedAmount(dPeriodStartDate, dao);

		// 本期出货累计实绩校正数

		// 本期开始日期 yyyy/MM/dd 格式
		cal.set(Calendar.DAY_OF_MONTH, 1);
		String current_month_start_date = DateUtil.toString(cal.getTime(), DateUtil.DATE_PATTERN);

		// 累计出货情况
		ret.put("period_name", current_period_name);

		String series31 = "[#ogz_month_fact#, #ogz_period_fact#, {color: '#02FDA5',dataLabels: {y:-18, backgroundColor: 'rgba(2, 253, 165, 0.2)'}, y:#planned_amount#}]" ;
		series31 = series31.replaceAll("#ogz_month_fact#", "" + dao.getShippingInMonthResult(current_month_start_date));//本月出货累计
		series31 = series31.replaceAll("#ogz_period_fact#", "" + (dao.getShippingInPeriodResult(dPeriodStartDate)));//半期出货累计（实绩）
		series31 = series31.replaceAll("#planned_amount#", "" + current_planned_amount);//半期累计（计划）
		ret.put("serie31", series31);

		return ret;
	}

	// 取得本期出货累计实绩校正数
	private String getCurrentPlannedAmount(Date dPeriodStartDate,
			GlobalProgressMapper mapper) {
		String inStr = "";
		Calendar cal = Calendar.getInstance();
		cal.setTime(dPeriodStartDate);
		for (int i = 0;i < 6;i++) {
			inStr += (",(" + cal.get(Calendar.YEAR) + "," + (cal.get(Calendar.MONTH) + 1) + ")");
			cal.add(Calendar.MONTH, 1);
		}
		inStr = inStr.substring(1);

		String getsum = mapper.getPlanAmountOfPeriod(inStr);

		if (CommonStringUtil.isEmpty(getsum)) {
			return "0"; // (未定义)
		} else {
			return getsum;
		}
		// SELECT sum(shipment) FROM repair_plan where (plan_year,plan_month) in ((2017 ,1 ),(2017 ,2 ));
	}

}
